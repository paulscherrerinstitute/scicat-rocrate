package ch.psi.ord.model;

public class NoMetadataDescriptor implements ValidationError {
  @Override
  public String getType() {
    return "NoMetadataDescriptor";
  }

  @Override
  public String getMessage() {
    return "The archive doesn't contain a 'ro-crate-metadata.json' file";
  }
}
