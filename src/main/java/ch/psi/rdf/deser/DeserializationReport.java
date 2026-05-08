package ch.psi.rdf.deser;

import ch.psi.ord.model.ValidationError;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

public class DeserializationReport<T> {
  @Getter private Set<ValidationError> errors = new HashSet<>();
  private Optional<T> value = Optional.empty();

  public boolean isValid() {
    return errors.isEmpty() && value.isPresent();
  }

  public void addError(ValidationError e) {
    errors.add(e);
  }

  public void addErrors(DeserializationReport<?> report) {
    errors.addAll(report.getErrors());
  }

  public void set(T value) {
    this.value = Optional.ofNullable(value);
  }

  public T get() {
    return value.orElseThrow(
        () -> new IllegalStateException("Invalid access to deserialized entity"));
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
