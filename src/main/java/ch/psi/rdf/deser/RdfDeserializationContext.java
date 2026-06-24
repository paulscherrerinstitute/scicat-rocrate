package ch.psi.rdf.deser;

import ch.psi.ord.model.PropertyError;
import ch.psi.rdf.RdfDeserializerProvider;
import lombok.RequiredArgsConstructor;
import org.apache.jena.rdf.model.Literal;

@RequiredArgsConstructor
public class RdfDeserializationContext {
  private final RdfDeserializerProvider provider;
  private final DeserializationReport<?> report;

  public void addError(PropertyError e) {
    report.addError(e);
  }

  public RdfDeserializer<?> getDeserializer(Literal l) throws RdfDeserializationException {
    return provider.getDeserializer(l);
  }

  public RdfDeserializer<?> getDeserializer(Class<?> clazz) throws RdfDeserializationException {
    return provider.getDeserializer(clazz);
  }
}
