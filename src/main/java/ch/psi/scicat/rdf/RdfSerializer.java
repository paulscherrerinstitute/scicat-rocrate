package ch.psi.scicat.rdf;

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

    public static Resource serialize(Model model, Object obj) throws Exception {
        Resource serializedObject = null;

        Class<?> clazz = obj.getClass();

        if (clazz.isAnnotationPresent(RdfClass.class)) {
            RdfClass rdfClass = clazz.getAnnotation(RdfClass.class);
            serializedObject = generateResourceURI(obj)
                    .map(uri -> model.createResource(uri))
                    .orElseGet(() -> model.createResource());

            // NOTE: what to do when types is empty?
            for (String type : rdfClass.typesUri()) {
                Resource typeResource = model.createResource(type);
                serializedObject.addProperty(RDF.type, typeResource);
            }

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(RdfProperty.class)) {
                    RdfProperty rdfProperty = field.getAnnotation(RdfProperty.class);
                    Property property = ResourceFactory.createProperty(rdfProperty.uri());
                    Object value = field.get(obj);

                    if (value != null) {
                        if (value instanceof String str) {
                            serializedObject.addProperty(property, str);
                        } else if (value instanceof Boolean b) {
                            serializedObject.addLiteral(property, b.booleanValue());
                        } else if (value instanceof Number n) {
                            serializedObject.addLiteral(property, n.doubleValue());
                        } else if (value.getClass().isAnnotationPresent(RdfClass.class)) {
                            Resource nestedObject = serialize(model, value);
                            serializedObject.addProperty(property, nestedObject);
                        } else if (value instanceof Iterable) {
                            for (Object item : (Iterable<?>) value) {
                                if (item instanceof String str) {
                                    serializedObject.addProperty(property, str);
                                } else {
                                    Resource nestedObject = serialize(model, item);
                                    serializedObject.addProperty(property, nestedObject);
                                }
                            }
                        } else {
                            throw new IllegalStateException("Unable to serialize type " + value.getClass().getName());
                        }
                    } else {
                        logger.warn("Ignoring field '{}' because it's null", field.getName());
                    }
                }
            }

        }

        return serializedObject;

    }

    public static Optional<String> generateResourceURI(Object obj) {
        try {
            for (Method method : obj.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(RdfResourceUri.class)) {
                    return Optional.of(method.invoke(obj).toString());
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
