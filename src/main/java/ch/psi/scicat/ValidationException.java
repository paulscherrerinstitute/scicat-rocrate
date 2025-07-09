package ch.psi.scicat;

import java.util.HashSet;
import java.util.Set;

import ch.psi.scicat.model.ValidationError;

public class ValidationException extends Throwable {
    private Set<ValidationError> errors = new HashSet<>();

    public ValidationException() {
    }

    public ValidationException(ValidationError e) {
        errors.add(e);
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }

    public void addError(ValidationError e) {
        errors.add(e);
    }

    public Set<ValidationError> getErrors() {
        return errors;
    }

    public void addError(ValidationException e) {
        errors.addAll(e.getErrors());
    }
}
