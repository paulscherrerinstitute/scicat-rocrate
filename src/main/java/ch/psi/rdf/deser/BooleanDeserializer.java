package ch.psi.rdf.deser;

import org.apache.jena.rdf.model.RDFNode;

public class BooleanDeserializer implements RdfDeserializer<Boolean> {
  @Override
  public Boolean deserialize(RDFNode node, RdfDeserializationContext context)
      throws RdfDeserializationException {
    return node.asLiteral().getBoolean();
  }
}
