package ch.psi.rdf.deser;

import org.apache.jena.rdf.model.RDFNode;

public class FloatDeserializer implements RdfDeserializer<Float> {
  @Override
  public Float deserialize(RDFNode node, RdfDeserializationContext context)
      throws RdfDeserializationException {
    return node.asLiteral().getFloat();
  }
}
