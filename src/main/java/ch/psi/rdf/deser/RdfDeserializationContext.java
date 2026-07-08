package ch.psi.rdf.deser;

import ch.psi.ord.model.PropertyError;
import ch.psi.rdf.RdfDeserializerProvider;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public class RdfDeserializationContext {
  private final RdfDeserializerProvider provider;
  private final DeserializationReport<?> report;
  @Getter private Optional<Resource> currentSubject;

  public Resource setCurrentSubject(@NonNull Resource subject) {
    currentSubject = Optional.of(subject);
    return subject;
  }

  public void resetCurrentSubject() {
    currentSubject = Optional.empty();
  }

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
