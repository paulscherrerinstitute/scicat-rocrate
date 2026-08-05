package ch.psi.ord.model;

import com.fasterxml.jackson.annotation.JsonGetter;

public interface ValidationError {
  @JsonGetter(value = "type")
  String getType();

  @JsonGetter(value = "message")
  String getMessage();
}
