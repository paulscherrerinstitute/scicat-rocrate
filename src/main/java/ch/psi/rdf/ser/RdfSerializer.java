package ch.psi.rdf.ser;

import java.util.List;
import org.apache.jena.rdf.model.RDFNode;

@FunctionalInterface
public interface RdfSerializer<T> {
  List<RDFNode> serialize(T value, RdfSerializationContext context)
      throws RdfSerializationException;
}
