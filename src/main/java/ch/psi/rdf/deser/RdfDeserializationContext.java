package ch.psi.rdf.deser;

import ch.psi.ord.model.PropertyError;
import ch.psi.rdf.RdfDeserializerProvider;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.jena.rdf.model.Resource;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public class RdfDeserializationContext {
  private final RdfDeserializerProvider provider;
  private final DeserializationReport<?> report;
  private final Deque<Resource> subjects = new ArrayDeque<>();

  public Optional<Resource> getCurrentSubject() {
    return Optional.ofNullable(subjects.peek());
  }

  public Resource pushCurrentSubject(@NonNull Resource subject) {
    subjects.push(subject);
    return subject;
  }

  public void popCurrentSubject() {
    subjects.pop();
  }

  public void addError(PropertyError e) {
    report.addError(e);
  }

  public <T> RdfDeserializer<T> getDeserializer(Class<T> clazz) throws RdfDeserializationException {
    return provider.getDeserializer(clazz);
  }
}
