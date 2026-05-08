package ch.psi.rdf;

import ch.psi.rdf.ser.RdfSerializationException;
import ch.psi.rdf.ser.RdfSerializer;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

public class RdfMapper {
  private final RdfSerializerProvider serializerProvider = new RdfSerializerProvider();

  public RDFNode serialize(Object obj) throws RdfSerializationException {
    Model model = ModelFactory.createDefaultModel();

    @SuppressWarnings("unchecked")
    RdfSerializer<Object> serializer =
        (RdfSerializer<Object>) serializerProvider.getSerializer(obj.getClass());

    List<RDFNode> rootNode = serializer.serialize(obj, model, serializerProvider);
    return rootNode.getFirst();
  }
}
