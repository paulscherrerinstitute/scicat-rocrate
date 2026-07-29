package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class RdfSerializationContext {
  private final RdfSerializerProvider serializerProvider;
  @Getter Model model = ModelFactory.createDefaultModel();
  private final Deque<Resource> subjects = new ArrayDeque<>();

  public RdfSerializationContext(RdfSerializerProvider serializerProvider) {
    this.serializerProvider = serializerProvider;
  }

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

  @SuppressWarnings("unchecked")
  public RdfSerializer<Object> getSerializer(Class<? extends Object> clazz) {
    return (RdfSerializer<Object>) serializerProvider.getSerializer(clazz);
  }
}
