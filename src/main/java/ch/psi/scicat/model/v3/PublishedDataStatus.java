package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PublishedDataStatus {
  PENDING_REGISTRATION("pending_registration"),
  REGISTERED("registered");

  private final String value;

  PublishedDataStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
