package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PublishedDataStatus {
  @JsonProperty("pending_registration")
  PENDING_REGISTRATION,

  @JsonProperty("registered")
  REGISTERED;
}
