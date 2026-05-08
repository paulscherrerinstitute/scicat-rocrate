package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class BooleanSerializer implements RdfSerializer<Boolean> {
  @Override
  public List<RDFNode> serialize(Boolean value, Model model, RdfSerializerProvider provider)
      throws RdfSerializationException {
    return List.of(model.createTypedLiteral(value.booleanValue()));
  }
}
