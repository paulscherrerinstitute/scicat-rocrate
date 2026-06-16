package ch.psi.rdf.deser;

import ch.psi.ord.model.ValidationError;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.jena.rdf.model.Resource;

@RequiredArgsConstructor
public class DeserializationReport<T> {
  @Getter private Set<ValidationError> errors = new HashSet<>();
  @Getter @Setter private T value;
  @Getter private final Resource subject;

  public boolean isValid() {
    return errors.isEmpty() && value != null;
  }

  public void addError(ValidationError e) {
    errors.add(e);
  }

  public void addErrors(DeserializationReport<?> report) {
    errors.addAll(report.getErrors());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (isValid()) {
      builder.append("Contains: \n  ").append(value);
    } else {
      for (ValidationError e : errors) {
        builder.append(e.getMessage()).append('\n');
      }
    }

    return builder.toString();
  }
}
