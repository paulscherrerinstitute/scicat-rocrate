package ch.psi.ord.api;

import jakarta.ws.rs.core.MediaType;

public class ExtraMediaType {
  public static final MediaType APPLICATION_JSONLD_TYPE = new MediaType("application", "ld+json");
  public static final String APPLICATION_JSONLD = "application/ld+json";
  public static final MediaType APPLICATION_ZIP_TYPE = new MediaType("application", "zip");
  public static final String APPLICATION_ZIP = "application/zip";
  public static final MediaType APPLICATION_TAR_TYPE = new MediaType("application", "x-tar");
  public static final String APPLICATION_TAR = "application/x-tar";
}
