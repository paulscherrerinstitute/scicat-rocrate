package ch.psi.rdf.deser;

import org.apache.jena.rdf.model.RDFNode;

public class DoubleDeserializer implements RdfDeserializer<Double> {
  @Override
  public Double deserialize(RDFNode node, RdfDeserializationContext context)
      throws RdfDeserializationException {
    return node.asLiteral().getDouble();
  }
}
