package ch.psi.rdf.ser;

import java.util.List;
import org.apache.jena.rdf.model.RDFNode;

public class BooleanSerializer implements RdfSerializer<Boolean> {
  @Override
  public List<RDFNode> serialize(Boolean value, RdfSerializationContext context)
      throws RdfSerializationException {
    return List.of(context.getModel().createTypedLiteral(value.booleanValue()));
  }
}
