package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

@FunctionalInterface
public interface RdfSerializer<T> {
  /**
   * Translates a Java object into an RDF Node (Resource or Literal).
   *
   * @param value The value to serialize
   * @param model The shared Jena Model
   * @param provider The provider to look up serializers for nested fields
   * @return An RDFNode representing the serialized value
   */
  List<RDFNode> serialize(T value, Model model, RdfSerializerProvider provider)
      throws RdfSerializationException;
}
