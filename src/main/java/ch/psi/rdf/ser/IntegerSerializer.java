package ch.psi.rdf.ser;

import java.util.List;
import org.apache.jena.rdf.model.RDFNode;

public class IntegerSerializer implements RdfSerializer<Integer> {
  @Override
  public List<RDFNode> serialize(Integer value, RdfSerializationContext context)
      throws RdfSerializationException {
    return List.of(context.getModel().createTypedLiteral(value.intValue()));
  }
}
