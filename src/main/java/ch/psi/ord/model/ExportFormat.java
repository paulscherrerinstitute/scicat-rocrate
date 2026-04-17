package ch.psi.ord.model;

import ch.psi.ord.api.ExtraMediaType;

public enum ExportFormat {
  JSONLD(ExtraMediaType.APPLICATION_JSONLD),
  ZIP(ExtraMediaType.APPLICATION_ZIP);

  private final String mediaType;

  ExportFormat(String mediaType) {
    this.mediaType = mediaType;
  }

  public static ExportFormat fromString(String s) {
    if (s == null) return null;

    for (ExportFormat format : ExportFormat.values()) {
      if (format.mediaType.equals(s)) {
        return format;
      }
    }
    return null;
  }
}
