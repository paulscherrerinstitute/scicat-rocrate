package ch.psi.rdf;

import ch.psi.rdf.deser.BooleanDeserializer;
import ch.psi.rdf.deser.DoubleDeserializer;
import ch.psi.rdf.deser.FloatDeserializer;
import ch.psi.rdf.deser.IntegerDeserializer;
import ch.psi.rdf.deser.ObjectDeserializer;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import ch.psi.rdf.deser.StringDeserializer;
import java.util.HashMap;
import java.util.Map;

public class RdfDeserializerProvider {
  private final Map<Class<?>, RdfDeserializer<?>> registry = new HashMap<>();

  private static final RdfDeserializer<String> STRING = new StringDeserializer();
  private static final RdfDeserializer<Integer> INTEGER = new IntegerDeserializer();
  private static final RdfDeserializer<Float> FLOAT = new FloatDeserializer();
  private static final RdfDeserializer<Double> DOUBLE = new DoubleDeserializer();
  private static final RdfDeserializer<Boolean> BOOLEAN = new BooleanDeserializer();

  public RdfDeserializerProvider() {
    registry.put(String.class, STRING);
    registry.put(Integer.class, INTEGER);
    registry.put(int.class, INTEGER);
    registry.put(Float.class, FLOAT);
    registry.put(float.class, FLOAT);
    registry.put(Double.class, DOUBLE);
    registry.put(double.class, DOUBLE);
    registry.put(Boolean.class, BOOLEAN);
    registry.put(boolean.class, BOOLEAN);
  }

  @SuppressWarnings("unchecked")
  public <T> RdfDeserializer<T> getDeserializer(Class<T> clazz) throws RdfDeserializationException {
    RdfDeserializer<?> existing = registry.get(clazz);
    if (existing != null) return (RdfDeserializer<T>) existing;

    RdfDeserializer<T> created = new ObjectDeserializer<>(clazz);
    registry.putIfAbsent(clazz, created);
    return (RdfDeserializer<T>) registry.get(clazz);
  }
}
