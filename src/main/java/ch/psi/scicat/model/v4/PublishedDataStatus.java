package ch.psi.scicat.model.v4;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PublishedDataStatus {
  @JsonProperty("private")
  PRIVATE,
  @JsonProperty("public")
  PUBLIC,
  @JsonProperty("registered")
  REGISTERED,
  @JsonProperty("amended")
  AMENDED
}
