package ch.psi.rdf.deser;

import ch.psi.ord.model.ValidationError;
import ch.psi.rdf.RdfDeserializerProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RdfDeserializationContext {
  private final RdfDeserializerProvider provider;
  private final DeserializationReport<?> report;

  public void addError(ValidationError e) {
    report.addError(e);
  }

  public RdfDeserializer<?> getDeserializer(Class<?> clazz) throws RdfDeserializationException {
    return provider.getDeserializer(clazz);
  }
}
