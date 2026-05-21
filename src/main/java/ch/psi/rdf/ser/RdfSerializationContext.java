package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class RdfSerializationContext {
  private final RdfSerializerProvider serializerProvider;
  @Getter private Optional<Resource> currentSubject;
  @Getter Model model = ModelFactory.createDefaultModel();

  public RdfSerializationContext(RdfSerializerProvider serializerProvider) {
    this.serializerProvider = serializerProvider;
  }

  public Resource setCurrentSubject(@NonNull Resource subject) {
    currentSubject = Optional.of(subject);
    return subject;
  }

  public void resetCurrentSubject() {
    currentSubject = Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public RdfSerializer<Object> getSerializer(Class<? extends Object> clazz) {
    return (RdfSerializer<Object>) serializerProvider.getSerializer(clazz);
  }
}
