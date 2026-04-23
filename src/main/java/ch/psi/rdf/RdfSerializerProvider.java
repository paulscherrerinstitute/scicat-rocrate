package ch.psi.rdf;

import ch.psi.rdf.ser.BooleanSerializer;
import ch.psi.rdf.ser.DoubleSerializer;
import ch.psi.rdf.ser.FloatSerializer;
import ch.psi.rdf.ser.InstantSerializer;
import ch.psi.rdf.ser.IntegerSerializer;
import ch.psi.rdf.ser.ListSerializer;
import ch.psi.rdf.ser.ObjectSerializer;
import ch.psi.rdf.ser.RdfSerializer;
import ch.psi.rdf.ser.StringSerializer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdfSerializerProvider {
  private final Map<Class<?>, RdfSerializer<?>> registry = new HashMap<>();
  private final RdfSerializer<Object> defaultSerializer = new ObjectSerializer();
  private final RdfSerializer<List<Object>> listSerializer = new ListSerializer<Object>();

  public RdfSerializerProvider() {
    this.register(String.class, new StringSerializer());
    this.register(Integer.class, new IntegerSerializer());
    this.register(Float.class, new FloatSerializer());
    this.register(Double.class, new DoubleSerializer());
    this.register(Boolean.class, new BooleanSerializer());
    this.register(Instant.class, new InstantSerializer());
  }

  public <T> void register(Class<T> clazz, RdfSerializer<T> serializer) {
    registry.put(clazz, serializer);
  }

  @SuppressWarnings("unchecked")
  public <T> RdfSerializer<T> getSerializer(Class<T> clazz) {
    if (List.class.isAssignableFrom(clazz)) {
      return (RdfSerializer<T>) listSerializer;
    }

    RdfSerializer<T> s = (RdfSerializer<T>) registry.getOrDefault(clazz, defaultSerializer);
    return s;
  }
}
