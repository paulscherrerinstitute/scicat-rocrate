package ch.psi.rdf.ser;

import java.util.List;
import org.apache.jena.rdf.model.RDFNode;

public class DoubleSerializer implements RdfSerializer<Double> {
  @Override
  public List<RDFNode> serialize(Double value, RdfSerializationContext context)
      throws RdfSerializationException {
    return List.of(context.getModel().createTypedLiteral(value.doubleValue()));
  }
}
