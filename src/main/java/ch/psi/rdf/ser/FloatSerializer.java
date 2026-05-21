package ch.psi.rdf.ser;

import java.util.List;
import org.apache.jena.rdf.model.RDFNode;

public class FloatSerializer implements RdfSerializer<Float> {
  @Override
  public List<RDFNode> serialize(Float value, RdfSerializationContext context)
      throws RdfSerializationException {
    return List.of(context.getModel().createTypedLiteral(value.floatValue()));
  }
}
