package ch.psi.ord.model;

import ch.psi.ord.api.ExtraMediaType;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExportFormat {
  @JsonProperty(ExtraMediaType.APPLICATION_JSONLD)
  JSONLD(ExtraMediaType.APPLICATION_JSONLD),

  @JsonProperty(ExtraMediaType.APPLICATION_ZIP)
  ZIP(ExtraMediaType.APPLICATION_ZIP);

  private final String value;

  ExportFormat(String value) {
    this.value = value;
  }

  public static ExportFormat fromString(String value) {
    for (ExportFormat format : ExportFormat.values()) {
      if (format.value.equalsIgnoreCase(value)) {
        return format;
      }
    }
    throw new IllegalArgumentException("Unknown format: " + value);
  }
}
