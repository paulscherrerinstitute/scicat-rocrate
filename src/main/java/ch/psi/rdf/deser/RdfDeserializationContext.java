package ch.psi.rdf.deser;

import ch.psi.ord.model.PropertyError;
import ch.psi.rdf.RdfDeserializerProvider;
import ch.psi.rdf.annotations.RdfCardinality;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.jena.rdf.model.Resource;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public class RdfDeserializationContext {
  private final RdfDeserializerProvider provider;
  private final DeserializationReport<?> report;
  private final Deque<Resource> subjects = new ArrayDeque<>();
  private final Deque<Map<String, RdfCardinality>> cardinalityOverrides = new ArrayDeque<>();

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

  public Map<String, RdfCardinality> getCardinalityOverrides() {
    return cardinalityOverrides.isEmpty() ? Map.of() : cardinalityOverrides.peek();
  }

  public void pushCardinalityOverrides(RdfCardinality[] overrides) {
    cardinalityOverrides.push(
        Arrays.stream(overrides).collect(Collectors.toMap(RdfCardinality::uri, c -> c)));
  }

  public void popCardinalityOverrides() {
    cardinalityOverrides.pop();
  }

  public void addError(PropertyError e) {
    report.addError(e);
  }

  public <T> RdfDeserializer<T> getDeserializer(Class<T> clazz) throws RdfDeserializationException {
    return provider.getDeserializer(clazz);
  }
}
