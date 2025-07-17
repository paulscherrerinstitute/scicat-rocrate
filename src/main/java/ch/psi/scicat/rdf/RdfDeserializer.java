package ch.psi.scicat.rdf;

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

import ch.psi.scicat.model.PropertyError;
import ch.psi.scicat.model.ValidationError;

public class RdfDeserializer {
    private static final Logger logger = LoggerFactory.getLogger(RdfSerializer.class);

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
            report.getErrors().forEach(e -> System.err.println(e.getMessage()));
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
                builder.append("Contains: \n  ")
                        .append(value);
            } else {
                for (ValidationError e : errors) {
                    builder.append(e.getMessage()).append('\n');
                }
            }

            return builder.toString();
        }
    }

    public static <T> DeserializationReport<T> deserialize(Resource subject, Class<T> clazz) throws Exception {
        DeserializationReport<T> report = new DeserializationReport<>();

        Optional<T> obj = initInstance(clazz);
        if (subject == null || clazz == null || obj.isEmpty()) {
            return report;
        }

        if (clazz.isAnnotationPresent(RdfClass.class)) {
            RdfClass rdfClassAnnotation = clazz.getAnnotation(RdfClass.class);
            checkType(subject, rdfClassAnnotation.typesUri())
                    .ifPresent(e -> report.addError(e));

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(RdfProperty.class)) {
                    RdfProperty rdfPropertyAnnotation = field.getAnnotation(RdfProperty.class);
                    Property p = ResourceFactory.createProperty(rdfPropertyAnnotation.uri());

                    List<RDFNode> values = subject.listProperties(p)
                            .mapWith(s -> s.getObject())
                            .toList();
                    if (values.size() < rdfPropertyAnnotation.minCardinality()
                            || values.size() > rdfPropertyAnnotation.maxCardinality()) {
                        String message = String.format("Expected between %d and %d values but got %d",
                                rdfPropertyAnnotation.minCardinality(), rdfPropertyAnnotation.maxCardinality(),
                                values.size());
                        report.addError(new PropertyError(subject.getURI(), p.getURI(), message));
                    } else if (Collection.class.isAssignableFrom(field.getType())) {
                        logger.info("Found a collection");
                        Collection<Object> collection;
                        if (field.getType().isAssignableFrom(List.class)) {
                            collection = new ArrayList<>();
                            // FIXME: should be safe for Lists?
                            Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType())
                                    .getActualTypeArguments()[0];
                            for (RDFNode value : values) {
                                collection.add(convertValue(listType, value, report));
                            }
                            field.set(obj.get(), collection);
                        } else {
                            logger.error("Unsupported collection");
                        }
                    } else if (values.size() > 0) {
                        if (values.size() > 1) {
                            logger.warn("Field {} is not a collection, only the first value will be assigned");
                        }
                        RDFNode value = values.getFirst();
                        field.set(obj.get(), convertValue(field.getType(), value, report));
                    }
                }
            }

            if (report.getErrors().isEmpty()) {
                report.set(obj.get());
            }
        }

        return report;
    }

    private static <T> Optional<T> initInstance(Class<T> clazz) {
        return Optional.ofNullable(clazz)
                .flatMap(c -> {
                    try {
                        return Optional.of(c.getDeclaredConstructor().newInstance());
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        logger.error("Failed to instantiate instance of " + clazz.getName(), e);
                        return Optional.empty();
                    }
                });
    }

    private static Optional<PropertyError> checkType(Resource subject, String[] expectedTypes) {
        List<String> actualTypes = subject.listProperties(RDF.type)
                .mapWith(s -> s.getObject().toString())
                .toList();
        if (Arrays.stream(expectedTypes).noneMatch(actualTypes::contains)) {
            String message = "Expected '@type' to be one of [ " + String.join(", ", expectedTypes) + " ] but is [ "
                    + String.join(", ", actualTypes) + " ]";
            return Optional.of(new PropertyError(subject.getURI(), "@type", message));
        }
        return Optional.empty();
    }

    private static <T> Object convertValue(Class<?> fieldType, RDFNode value, DeserializationReport<T> report)
            throws Exception {
        if (value.isLiteral()) {
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
        } else if (value.isResource()) {
            Resource resourceValue = value.asResource();
            DeserializationReport<?> subreport = deserialize(resourceValue, fieldType);
            report.addErrors(subreport);
            // Should we set the field if there are errors?
            if (subreport.isValid()) {
                return subreport.get();
            }
        }
        // Never reached
        return null;
    }
}
