package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class ListSerializer<T> implements RdfSerializer<List<T>> {
  @SuppressWarnings("unchecked")
  @Override
  public List<RDFNode> serialize(List<T> values, Model model, RdfSerializerProvider provider)
      throws RdfSerializationException {
    List<RDFNode> items = new ArrayList<>();
    for (T value : values) {
      if (value == null) continue;
      RdfSerializer<T> serializer = (RdfSerializer<T>) provider.getSerializer(value.getClass());
      items.addAll(serializer.serialize(value, model, provider));
    }
    return items;
  }
}
