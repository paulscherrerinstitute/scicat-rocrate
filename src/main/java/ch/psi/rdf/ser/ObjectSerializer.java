package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceUri;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class ObjectSerializer implements RdfSerializer<Object> {
  @Override
  public List<RDFNode> serialize(Object value, Model model, RdfSerializerProvider provider)
      throws RdfSerializationException {
    Class<?> clazz = value.getClass();
    @NonNull RdfClass rdfClass = validateClassAnnotation(clazz);
    Resource serializedObject = createResourceWithUri(value, model);

    for (String type : rdfClass.typesUri()) {
      serializedObject.addProperty(RDF.type, model.createResource(type));
    }

    for (Field field : clazz.getDeclaredFields()) {
      if (!field.isAnnotationPresent(RdfProperty.class)) {
        continue;
      }

      try {
        field.setAccessible(true);
        Object fieldValue = field.get(value);
        if (fieldValue == null) {
          continue;
        }
        RdfProperty rdfProperty = field.getAnnotation(RdfProperty.class);
        Property property = model.createProperty(rdfProperty.uri());

        @SuppressWarnings("unchecked")
        RdfSerializer<Object> serializer =
            (RdfSerializer<Object>) provider.getSerializer(fieldValue.getClass());
        serializer
            .serialize(fieldValue, model, provider)
            .forEach(node -> serializedObject.addProperty(property, node));
      } catch (Exception e) {
        throw new RdfSerializationException(
            "An error occured while serializing field " + field.getName(), e);
      }
    }

    return List.of(serializedObject);
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
}
