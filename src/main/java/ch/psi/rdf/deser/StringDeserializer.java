package ch.psi.rdf.deser;

import org.apache.jena.rdf.model.RDFNode;

public class StringDeserializer implements RdfDeserializer<String> {
  @Override
  public String deserialize(RDFNode node, RdfDeserializationContext context)
      throws RdfDeserializationException {
    return node.asLiteral().getString();
  }
}
