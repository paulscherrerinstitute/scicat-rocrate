package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PublishedDataStatus {
  PENDING_REGISTRATION("pending_registration"),
  REGISTERED("REGISTERED");

  private final String value;

  PublishedDataStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
