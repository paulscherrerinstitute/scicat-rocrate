package ch.psi.ord.model;

import ch.psi.ord.core.RoCrate;

public class NoMetadataDescriptor implements ValidationError {
  @Override
  public String getType() {
    return "NoMetadataDescriptor";
  }

  @Override
  public String getMessage() {
    return String.format("The archive doesn't contain a '%s' file", RoCrate.METADATA_DESCRIPTOR);
  }
}
