package ch.psi.ord.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MissingDataError implements ValidationError {
  String path;

  @Override
  public String getType() {
    return "MissingDataError";
  }

  @Override
  public String getMessage() {
    return String.format(
        "The path '%s' is referenced in the metadata descriptor but absent from the archive ",
        path);
  }
}
