package ch.psi.rdf;

import ch.psi.ord.model.PropertyError;
import ch.psi.ord.model.ValidationError;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfDeserializer {
  private static final Logger logger = LoggerFactory.getLogger(RdfDeserializer.class);

  public static class DeserializationReport<T> {
    private Set<ValidationError> errors = new HashSet<>();
    private T value = null;

    public boolean isValid() {
      return errors.isEmpty() && value != null;
    }

    public void addError(ValidationError e) {
      errors.add(e);
    }

    public void addErrors(DeserializationReport<?> report) {
      errors.addAll(report.getErrors());
    }

    public Set<ValidationError> getErrors() {
      return errors;
    }

    public void set(T value) {
      this.value = value;
    }

    public T get() {
      return value;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (isValid()) {
        builder.append("Contains: \n  ").append(value);
      } else {
        for (ValidationError e : errors) {
          builder.append(e.getMessage()).append('\n');
        }
      }

      return builder.toString();
    }
  }

  public <T> DeserializationReport<T> deserialize(Resource subject, Class<T> clazz) {
    DeserializationReport<T> report = new DeserializationReport<>();

    Optional<T> obj = initInstance(clazz);
    if (subject == null || clazz == null || obj.isEmpty()) {
      return report;
    }

    if (clazz.isAnnotationPresent(RdfClass.class)) {
      RdfClass rdfClassAnnotation = clazz.getAnnotation(RdfClass.class);
      checkType(subject, rdfClassAnnotation.typesUri()).ifPresent(e -> report.addError(e));

      for (Field field : clazz.getDeclaredFields()) {
        field.setAccessible(true);

        if (field.isAnnotationPresent(RdfProperty.class)) {
          RdfProperty rdfPropertyAnnotation = field.getAnnotation(RdfProperty.class);
          Property p = ResourceFactory.createProperty(rdfPropertyAnnotation.uri());

          List<RDFNode> values = subject.listProperties(p).mapWith(s -> s.getObject()).toList();

          checkCardinalities(subject, p, rdfPropertyAnnotation, values.size())
              .ifPresent(e -> report.addError(e));

          if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<Object> collection;
            if (field.getType().isAssignableFrom(List.class)) {
              collection = new ArrayList<>();
              // FIXME: should be safe for Lists?
              Class<?> listType =
                  (Class<?>)
                      ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
              for (RDFNode value : values) {
                collection.add(convertValue(listType, value, report));
              }
              setField(field, obj.get(), collection);
            } else {
              logger.error("Unsupported collection");
            }
          } else if (values.size() > 0) {
            if (values.size() > 1) {
              logger.warn(
                  "Field '{}' of class '{}' is not a collection, only the first value will be assigned",
                  field.getName(),
                  clazz.getName());
            }
            RDFNode node = values.getFirst();
            Object value = convertValue(field.getType(), node, report);
            setField(field, obj.get(), value);
          }
        }
      }

      if (report.getErrors().isEmpty()) {
        report.set(obj.get());
      }
    }

    return report;
  }

  private <T> Optional<T> initInstance(Class<T> clazz) {
    return Optional.ofNullable(clazz)
        .flatMap(
            c -> {
              try {
                return Optional.of(c.getDeclaredConstructor().newInstance());
              } catch (InstantiationException
                  | IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException
                  | NoSuchMethodException
                  | SecurityException e) {
                logger.error("Failed to instantiate instance of " + clazz.getName(), e);
                return Optional.empty();
              }
            });
  }

  private Optional<PropertyError> checkType(Resource subject, String[] expectedTypes) {
    List<String> actualTypes =
        subject.listProperties(RDF.type).mapWith(s -> s.getObject().toString()).toList();
    if (Arrays.stream(expectedTypes).noneMatch(actualTypes::contains)) {
      String message =
          "Expected '@type' to be one of [ "
              + String.join(", ", expectedTypes)
              + " ] but is [ "
              + String.join(", ", actualTypes)
              + " ]";
      return Optional.of(new PropertyError(subject.getURI(), "@type", message));
    }
    return Optional.empty();
  }

  private Optional<PropertyError> checkCardinalities(
      Resource subject, Property p, RdfProperty propertyAnnotation, int actualCardinality) {
    if (actualCardinality < propertyAnnotation.minCardinality()
        || actualCardinality > propertyAnnotation.maxCardinality()) {
      String message =
          String.format(
              "Expected between %d and %d values but got %d",
              propertyAnnotation.minCardinality(),
              propertyAnnotation.maxCardinality(),
              actualCardinality);
      return Optional.of(new PropertyError(subject.getURI(), p.getURI(), message));
    }
    return Optional.empty();
  }

  private <T> Object convertValue(
      Class<?> fieldType, RDFNode value, DeserializationReport<T> report) {
    if (value.isLiteral()) {
      return convertLiteralValue(fieldType, value, report);
    } else {
      return convertResourceValue(fieldType, value.asResource(), report);
    }
  }

  private <T> Object convertLiteralValue(
      Class<?> fieldType, RDFNode value, DeserializationReport<T> report) {
    switch (fieldType.getName()) {
      case "java.lang.String":
        return value.asLiteral().getString();
      case "java.lang.Integer":
      case "int":
        return value.asLiteral().getInt();
      case "java.lang.Double":
      case "double":
        return value.asLiteral().getDouble();
      case "java.lang.Float":
      case "float":
        return value.asLiteral().getFloat();
      case "java.lang.Boolean":
      case "boolean":
        return value.asLiteral().getBoolean();
      default:
        throw new IllegalStateException(
            "Deserializer doesn't support type: " + fieldType.getName());
    }
  }

  private <T> Object convertResourceValue(
      Class<?> fieldType, Resource resourceValue, DeserializationReport<T> report) {
    DeserializationReport<?> subreport = deserialize(resourceValue, fieldType);
    report.addErrors(subreport);
    return subreport.isValid() ? subreport.get() : null;
  }

  private void setField(Field field, Object instance, Object value) {
    if (instance == null) return;

    try {
      if (value == null) {
        logger.warn("Setting field {} to a null value", field.getName());
      }
      field.set(instance, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      logger.error("Unable to set field {}", field.getName(), e);
    }
  }
}
