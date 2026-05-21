package ch.psi.rdf.ser;

import java.util.List;
import org.apache.jena.rdf.model.RDFNode;

public class StringSerializer implements RdfSerializer<String> {
  @Override
  public List<RDFNode> serialize(String value, RdfSerializationContext context)
      throws RdfSerializationException {
    return List.of(context.getModel().createTypedLiteral(value));
  }
}
