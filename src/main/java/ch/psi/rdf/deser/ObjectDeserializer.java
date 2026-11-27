package ch.psi.rdf.deser;

import static ch.psi.rdf.RdfUtils.createInstance;
import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;

import ch.psi.ord.model.PropertyError;
import ch.psi.rdf.annotations.RdfCardinality;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceIdentifier;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

@Slf4j
public class ObjectDeserializer<T> implements RdfDeserializer<T> {
  private final Class<T> clazz;
  private final RdfClass rdfClassAnnotation;
  private final Map<Field, RdfProperty> annotatedFields = new HashMap<>();
  private final List<Field> rdfResourceIdentifierFields = new ArrayList<>();

  public ObjectDeserializer(Class<T> clazz) throws RdfDeserializationException {
    this.clazz = clazz;
    if (!clazz.isAnnotationPresent(RdfClass.class)) {
      throw new RdfDeserializationException(
          String.format(
              "Class %s is not annotated with %s", clazz.getName(), RdfClass.class.getName()));
    }
    rdfClassAnnotation = clazz.getAnnotation(RdfClass.class);
    for (Field field : clazz.getDeclaredFields()) {
      RdfProperty rdfProperty = field.getAnnotation(RdfProperty.class);
      if (rdfProperty != null) {
        annotatedFields.put(field, rdfProperty);
      }

      if (field.isAnnotationPresent(RdfResourceIdentifier.class)) {
        rdfResourceIdentifierFields.add(field);
      }
    }
  }

  @Override
  public T deserialize(RDFNode node, RdfDeserializationContext context)
      throws RdfDeserializationException {
    if (!node.isResource()) {
      throw new RdfDeserializationException(
          String.format("Expected node '%s' to be a resource", node.toString()));
    }

    if (context.isBeingDeserialized(node.asResource())) {
      context.addError(
          new PropertyError(
              node.asResource().getURI(), "@id", String.format("Cyclic reference to '%s'", node)));
      return null;
    }

    T obj;
    try {
      obj = createInstance(clazz);
    } catch (ReflectiveOperationException e) {
      throw new RdfDeserializationException(
          String.format("Failed to create an instance of %s", clazz.getName()), e);
    }
    Resource subject = node.asResource();
    Map<String, RdfCardinality> overrides = context.getCardinalityOverrides();
    context.pushCurrentSubject(subject);
    try {
      setUriFields(subject, obj);
      checkType(subject, rdfClassAnnotation.typesUri()).ifPresent(e -> context.addError(e));

      for (Map.Entry<Field, RdfProperty> entry : annotatedFields.entrySet()) {
        Field field = entry.getKey();
        RdfProperty propertyAnnotation = entry.getValue();

        Set<RDFNode> values =
            listProperties(subject, ResourceFactory.createProperty(propertyAnnotation.uri()));
        checkCardinalities(subject, propertyAnnotation, overrides, values.size())
            .ifPresent(e -> context.addError(e));

        context.pushCardinalityOverrides(propertyAnnotation.nested());
        try {
          if (field.isAnnotationPresent(RdfDeserialize.class)) {
            if (!values.isEmpty()) {
              setField(
                  field,
                  obj,
                  customDeserializer(field).deserialize(values.iterator().next(), context));
            }
          } else if (field.getType().isAssignableFrom(List.class)) {
            Class<?> listType =
                (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            RdfDeserializer<?> elementDeserializer = context.getDeserializer(listType);
            List<Object> collection = new ArrayList<>();
            for (RDFNode value : values) {
              Optional<PropertyError> error =
                  checkNodeKind(subject, propertyAnnotation.uri(), listType, value);
              if (error.isPresent()) {
                context.addError(error.get());
                continue;
              }
              Object element = elementDeserializer.deserialize(value, context);
              if (element != null) {
                collection.add(element);
              }
            }
            setField(field, obj, collection);
          } else if (!values.isEmpty()) {
            if (values.size() > 1) {
              log.warn(
                  "Field '{}' of class '{}' is not a collection, only the first value will be"
                      + " assigned",
                  field.getName(),
                  clazz.getName());
            }
            RDFNode value = values.iterator().next();
            Optional<PropertyError> error =
                checkNodeKind(subject, propertyAnnotation.uri(), field.getType(), value);
            if (error.isPresent()) {
              context.addError(error.get());
            } else {
              RdfDeserializer<?> fieldDeserializer = context.getDeserializer(field.getType());
              setField(field, obj, fieldDeserializer.deserialize(value, context));
            }
          }
        } finally {
          context.popCardinalityOverrides();
        }
      }
    } finally {
      context.popCurrentSubject();
    }

    return obj;
  }

  private RdfDeserializer<?> customDeserializer(Field field) throws RdfDeserializationException {
    try {
      return createInstance(field.getAnnotation(RdfDeserialize.class).using());
    } catch (ReflectiveOperationException e) {
      throw new RdfDeserializationException(
          String.format(
              "Unable to instantiate the custom deserializer of field '%s' of class '%s'",
              field.getName(), clazz.getName()),
          e);
    }
  }

  private Optional<PropertyError> checkType(Resource subject, String[] expectedTypes) {
    boolean typeMatch = Arrays.stream(expectedTypes).anyMatch(type -> isOfType(subject, type));
    if (!typeMatch) {
      List<String> actualTypes =
          subject.listProperties(RDF.type).mapWith(s -> s.getObject().toString()).toList();
      String message =
          String.format(
              "Expected '@type' to be one of [ %s ] but is [ %s ]",
              String.join(", ", expectedTypes), String.join(", ", actualTypes));
      return Optional.of(new PropertyError(subject.getURI(), "@type", message));
    }

    return Optional.empty();
  }

  public static Optional<PropertyError> checkNodeKind(
      Resource subject, String propertyUri, Class<?> targetType, RDFNode value) {
    RdfClass targetClass = targetType.getAnnotation(RdfClass.class);
    if (targetClass == null || value.isResource()) {
      return Optional.empty();
    }

    String message =
        String.format(
            "Expected an object of type [ %s ] but got literal '%s'",
            String.join(", ", targetClass.typesUri()), value.asLiteral().getLexicalForm());
    return Optional.of(new PropertyError(subject.getURI(), propertyUri, message));
  }

  private Optional<PropertyError> checkCardinalities(
      Resource subject,
      RdfProperty propertyAnnotation,
      Map<String, RdfCardinality> overrides,
      int actualCardinality) {
    RdfCardinality override = overrides.get(propertyAnnotation.uri());
    int min = override != null ? override.min() : propertyAnnotation.minCardinality();
    int max = override != null ? override.max() : propertyAnnotation.maxCardinality();

    String message = null;
    if (actualCardinality < min) {
      message =
          (actualCardinality == 0)
              ? "Missing required property"
              : String.format(
                  "Too few values: expected at least %d but got %d", min, actualCardinality);
    } else if (actualCardinality > max) {
      message =
          String.format("Too many values: expected at most %d but got %d", max, actualCardinality);
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

  private void setUriFields(Resource subject, T obj) {
    String value = subject.toString();
    for (Field f : rdfResourceIdentifierFields) {
      this.setField(f, obj, value);
    }
  }
}
