package ch.psi.rdf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * A utility class to serialize Java objects in RDF.
 *
 * <p>This serializer uses reflection and custom annotations ({@link RdfClass}, {@link RdfProperty},
 * and {@link RdfResourceUri}) to serialize an annotated object to a Jena {@link Model}. It supports
 * primitive types, nested annotated objects, and collections.
 *
 * @see RdfClass
 * @see RdfProperty
 * @see RdfResourceUri
 */
@Slf4j
public class RdfSerializer {
  /**
   * Translates a Java object into an RDF Resource.
   *
   * @param obj The object instance to be serialized.
   * @return A {@link Resource} containing the object's data as RDF properties.
   * @throws RdfSerializationException If the class is improperly annotated, a field or method is
   *     has the wrong visibility or generating a URI for the object fails
   */
  public Resource serialize(Object obj) throws RdfSerializationException {
    Model model = ModelFactory.createDefaultModel();
    Class<?> clazz = obj.getClass();

    @NonNull RdfClass rdfClass = validateClassAnnotation(clazz);
    Resource serializedObject = createResourceWithUri(obj, model);

    for (String type : rdfClass.typesUri()) {
      Resource typeResource = model.createResource(type);
      serializedObject.addProperty(RDF.type, typeResource);
    }

    List<Field> annotatedFields =
        Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(RdfProperty.class))
            .collect(Collectors.toList());

    for (Field field : annotatedFields) {
      try {
        @NonNull RdfProperty rdfProperty = field.getAnnotation(RdfProperty.class);
        Property property = ResourceFactory.createProperty(rdfProperty.uri());
        Object value = field.get(obj);
        if (value != null) {
          serializeValue(serializedObject, value, property);
        } else {
          log.warn("Ignoring field '{}' because it's null", field.getName());
        }
      } catch (IllegalAccessException e) {
        throw new RdfSerializationException(
            String.format(
                "Failed to read field '%s' of class '%s'",
                field.getName(), clazz.getCanonicalName()),
            e);
      }
    }

    return serializedObject;
  }

  private Resource createResourceWithUri(Object obj, Model model) throws RdfSerializationException {
    List<Method> annotatedMethods =
        Arrays.stream(obj.getClass().getDeclaredMethods())
            .filter(
                m ->
                    m.isAnnotationPresent(RdfResourceUri.class)
                        && m.getParameterCount() == 0
                        && m.getReturnType() == String.class)
            .toList();

    if (annotatedMethods.isEmpty()) {
      return model.createResource();
    }

    for (Method method : annotatedMethods) {
      try {
        String generatedUri = (String) method.invoke(obj);
        return model.createResource(generatedUri);
      } catch (InvocationTargetException e) {
        throw new RdfSerializationException(
            String.format(
                "URI generation method '%s' of class '%s' threw an exception.",
                method.getName(), obj.getClass().getCanonicalName()),
            e);
      } catch (IllegalAccessException e) {
        throw new RdfSerializationException(
            String.format(
                "Failed to call method '%s' of class '%s'.",
                method.getName(), obj.getClass().getCanonicalName()),
            e);
      }
    }

    return model.createResource();
  }

  private void serializeValue(Resource serializedObject, Object value, Property property)
      throws RdfSerializationException {
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
      Resource nestedObject = serialize(value);
      serializedObject
          .getModel()
          .add(nestedObject.getModel())
          .add(serializedObject, property, nestedObject);
    } else if (value instanceof Iterable) {
      for (Object item : (Iterable<?>) value) {
        serializeValue(serializedObject, item, property);
      }
    } else {
      throw new IllegalStateException(
          String.format("Unable to serialize type %s", value.getClass().getName()));
    }
  }

  private RdfClass validateClassAnnotation(Class<?> clazz) throws RdfSerializationException {
    if (!clazz.isAnnotationPresent(RdfClass.class)) {
      throw new RdfSerializationException(
          String.format(
              "Can not serialize instance of '%s', missing '@RdfClass' annotation",
              clazz.getCanonicalName()));
    }
    RdfClass rdfClass = clazz.getAnnotation(RdfClass.class);
    if (rdfClass.typesUri().length < 1) {
      throw new RdfSerializationException(
          String.format(
              "Can not serialize instance of '%s', empty 'typesUri' annotation parameter",
              clazz.getCanonicalName()));
    }

    return rdfClass;
  }
}
