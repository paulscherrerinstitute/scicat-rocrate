package ch.psi.rdf.deser;

import ch.psi.ord.model.PropertyError;
import ch.psi.rdf.RdfUtils;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

@Slf4j
public class ObjectDeserializer<T> implements RdfDeserializer<T> {
  private final Class<T> clazz;
  private final RdfClass rdfClassAnnotation;
  private final Map<Field, RdfProperty> annotatedFields;

  public ObjectDeserializer(Class<T> clazz) throws RdfDeserializationException {
    this.clazz = clazz;
    if (!clazz.isAnnotationPresent(RdfClass.class)) {
      throw new RdfDeserializationException(
          String.format(
              "Class %s is not annotated with %s", clazz.getName(), RdfClass.class.getName()));
    }
    this.rdfClassAnnotation = clazz.getAnnotation(RdfClass.class);
    this.annotatedFields =
        Stream.of(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(RdfProperty.class))
            .collect(Collectors.toMap(f -> f, f -> f.getAnnotation(RdfProperty.class)));
  }

  @Override
  public T deserialize(RDFNode node, RdfDeserializationContext context)
      throws RdfDeserializationException {
    if (!node.isResource()) {
      throw new RdfDeserializationException(
          String.format("Expected node '%s' to be a resource", node.toString()));
    }

    T obj = createInstance();
    Resource subject = node.asResource();
    checkType(subject, rdfClassAnnotation.typesUri()).ifPresent(e -> context.addError(e));

    for (Map.Entry<Field, RdfProperty> entry : annotatedFields.entrySet()) {
      Field field = entry.getKey();
      RdfProperty propertyAnnotation = entry.getValue();

      List<RDFNode> values = listValues(subject, propertyAnnotation.uri());
      checkCardinalities(subject, propertyAnnotation, values.size())
          .ifPresent(e -> context.addError(e));

      if (field.getType().isAssignableFrom(List.class)) {
        Class<?> listType =
            (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        RdfDeserializer<?> elementDeserializer = context.getDeserializer(listType);
        List<Object> collection = new ArrayList<>();
        for (RDFNode value : values) {
          collection.add(elementDeserializer.deserialize(value, context));
        }
        setField(field, obj, collection);
      } else if (!values.isEmpty()) {
        if (values.size() > 1) {
          log.warn(
              "Field '{}' of class '{}' is not a collection, only the first value will be assigned",
              field.getName(),
              clazz.getName());
        }
        RdfDeserializer<?> fieldDeserializer = context.getDeserializer(field.getType());
        Object value = fieldDeserializer.deserialize(values.getFirst(), context);
        setField(field, obj, value);
      }
    }

    return obj;
  }

  private T createInstance() throws RdfDeserializationException {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new RdfDeserializationException(
          String.format("Failed to create an instance of %s", clazz.getName()), e);
    }
  }

  private Optional<PropertyError> checkType(Resource subject, String[] expectedTypes) {
    List<String> normalizedExpectedTypes = new ArrayList<>();
    for (String type : expectedTypes) {
      normalizedExpectedTypes.add(type);
      normalizedExpectedTypes.add(RdfUtils.switchScheme(type));
    }

    List<String> actualTypes =
        subject.listProperties(RDF.type).mapWith(s -> s.getObject().toString()).toList();
    if (normalizedExpectedTypes.stream().noneMatch(actualTypes::contains)) {
      String message =
          String.format(
              "Expected '@type' to be one of [ %s ] but is [ %s ]",
              String.join(", ", expectedTypes), String.join(", ", actualTypes));
      return Optional.of(new PropertyError(subject.getURI(), "@type", message));
    }
    return Optional.empty();
  }

  private List<RDFNode> listValues(Resource subject, String propertyUri) {
    Property p = ResourceFactory.createProperty(propertyUri);
    List<RDFNode> values = subject.listProperties(p).mapWith(s -> s.getObject()).toList();
    if (values.size() == 0) {
      log.info("{} has no property {}", subject.toString(), propertyUri);
      p = ResourceFactory.createProperty(RdfUtils.switchScheme(propertyUri));
      log.info("Trying to switch property scheme to: '{}' ", p.toString());
      values = subject.listProperties(p).mapWith(s -> s.getObject()).toList();
    }

    return values;
  }

  private Optional<PropertyError> checkCardinalities(
      Resource subject, RdfProperty propertyAnnotation, int actualCardinality) {
    String message = null;
    if (actualCardinality < propertyAnnotation.minCardinality()) {
      message =
          (actualCardinality == 0)
              ? "Missing required property"
              : String.format(
                  "Too few values: expected at least %d but got %d",
                  propertyAnnotation.minCardinality(), actualCardinality);
    } else if (actualCardinality > propertyAnnotation.maxCardinality()) {
      message =
          String.format(
              "Too many values: expected at most %d but got %d",
              propertyAnnotation.maxCardinality(), actualCardinality);
    }
    return Optional.ofNullable(message)
        .map(msg -> new PropertyError(subject.getURI(), propertyAnnotation.uri(), msg));
  }

  private void setField(Field field, Object instance, Object value) {
    field.setAccessible(true);
    if (value == null) {
      log.warn("Setting field {} to a null value", field.getName());
    }
    try {
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      log.error("Unable to set field {}", field.getName(), e);
    }
  }
}
