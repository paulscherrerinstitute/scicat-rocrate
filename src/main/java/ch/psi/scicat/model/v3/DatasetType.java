package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DatasetType {
  RAW("raw"),
  DERIVED("derived");

  private final String value;

  DatasetType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
