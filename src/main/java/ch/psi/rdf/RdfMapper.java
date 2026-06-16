package ch.psi.rdf;

import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import ch.psi.rdf.ser.RdfSerializationContext;
import ch.psi.rdf.ser.RdfSerializationException;
import ch.psi.rdf.ser.RdfSerializer;
import java.util.List;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class RdfMapper {
  private final RdfSerializerProvider serializerProvider = new RdfSerializerProvider();
  private final RdfDeserializerProvider deserializerProvider = new RdfDeserializerProvider();

  public RDFNode serialize(Object obj) throws RdfSerializationException {
    RdfSerializationContext context = new RdfSerializationContext(serializerProvider);
    RdfSerializer<Object> serializer =
        (RdfSerializer<Object>) context.getSerializer(obj.getClass());

    List<RDFNode> rootNode = serializer.serialize(obj, context);
    return rootNode.getFirst();
  }

  public <T> DeserializationReport<T> deserialize(Resource subject, Class<T> clazz)
      throws RdfDeserializationException {
    DeserializationReport<T> report = new DeserializationReport<>(subject);
    RdfDeserializationContext context = new RdfDeserializationContext(deserializerProvider, report);
    RdfDeserializer<T> deserializer = deserializerProvider.getDeserializer(clazz);
    T result = deserializer.deserialize(subject, context);
    report.setValue(result);
    return report;
  }
}
