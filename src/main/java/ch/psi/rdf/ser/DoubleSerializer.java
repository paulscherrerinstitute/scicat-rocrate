package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class DoubleSerializer implements RdfSerializer<Double> {
  @Override
  public List<RDFNode> serialize(Double value, Model model, RdfSerializerProvider provider)
      throws RdfSerializationException {
    return List.of(model.createTypedLiteral(value.doubleValue()));
  }
}
