package ch.psi.rdf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfSerializer {
  private static final Logger logger = LoggerFactory.getLogger(RdfSerializer.class);

  // FIXME: transaction like logic should be implemented on the model
  public Optional<Resource> serialize(Model model, Object obj) throws Exception {
    Optional<Resource> serializedObject = Optional.empty();
    if (model == null || obj == null) {
      return serializedObject;
    }

    Class<?> clazz = obj.getClass();

    if (clazz.isAnnotationPresent(RdfClass.class)) {
      RdfClass rdfClass = clazz.getAnnotation(RdfClass.class);
      serializedObject =
          generateResourceUri(obj)
              .map(uri -> Optional.of(model.createResource(uri)))
              .orElseGet(() -> Optional.of(model.createResource()));

      // NOTE: what to do when types is empty?
      for (String type : rdfClass.typesUri()) {
        Resource typeResource = model.createResource(type);
        serializedObject.get().addProperty(RDF.type, typeResource);
      }

      for (Field field : clazz.getDeclaredFields()) {
        field.setAccessible(true);
        if (field.isAnnotationPresent(RdfProperty.class)) {
          RdfProperty rdfProperty = field.getAnnotation(RdfProperty.class);
          Property property = ResourceFactory.createProperty(rdfProperty.uri());
          Object value = field.get(obj);

          if (value != null) {
            serializeValue(serializedObject.get(), value, property);
          } else {
            logger.warn("Ignoring field '{}' because it's null", field.getName());
          }
        }
      }
    }

    return serializedObject;
  }

  public Optional<String> generateResourceUri(Object obj) {
    try {
      for (Method method : obj.getClass().getDeclaredMethods()) {
        if (method.isAnnotationPresent(RdfResourceUri.class)) {
          return Optional.ofNullable(method.invoke(obj)).map(Object::toString);
        }
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      logger.error(
          "URI generation function for class '" + obj.getClass().getName() + "' is not compliant",
          e);
    }

    return Optional.empty();
  }

  private void serializeValue(Resource serializedObject, Object value, Property property)
      throws Exception {
    if (value instanceof String str) {
      serializedObject.addProperty(property, str);
    } else if (value instanceof Boolean b) {
      serializedObject.addLiteral(property, b.booleanValue());
    } else if (value instanceof Integer i) {
      serializedObject.addLiteral(property, i.intValue());
    } else if (value instanceof Double d) {
      serializedObject.addLiteral(property, d.doubleValue());
    } else if (value instanceof Float f) {
      serializedObject.addLiteral(property, f.floatValue());
    } else if (value.getClass().isAnnotationPresent(RdfClass.class)) {
      Optional<Resource> nestedObject = serialize(serializedObject.getModel(), value);
      nestedObject.ifPresent(o -> serializedObject.addProperty(property, o));
    } else if (value instanceof Iterable) {
      for (Object item : (Iterable<?>) value) {
        serializeValue(serializedObject, item, property);
      }
    } else {
      throw new IllegalStateException("Unable to serialize type " + value.getClass().getName());
    }
  }
}
