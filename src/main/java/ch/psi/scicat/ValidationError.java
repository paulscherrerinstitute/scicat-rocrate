package ch.psi.scicat;

import com.fasterxml.jackson.annotation.JsonGetter;

public interface ValidationError {
    @JsonGetter(value = "type")
    public String getType();

    @JsonGetter(value = "message")
    public String getMessage();
}
