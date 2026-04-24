package ch.psi.rdf.deser;

import org.apache.jena.rdf.model.RDFNode;

public class IntegerDeserializer implements RdfDeserializer<Integer> {
  @Override
  public Integer deserialize(RDFNode node, RdfDeserializationContext context)
      throws RdfDeserializationException {
    return node.asLiteral().getInt();
  }
}
