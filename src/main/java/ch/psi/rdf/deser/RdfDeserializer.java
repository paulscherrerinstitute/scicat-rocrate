package ch.psi.rdf.deser;

import org.apache.jena.rdf.model.RDFNode;

@FunctionalInterface
public interface RdfDeserializer<T> {
  T deserialize(RDFNode node, RdfDeserializationContext context) throws RdfDeserializationException;
}
