package ch.psi.scicat.cli;

public class ScicatCliException extends RuntimeException {
  public ScicatCliException(String message) {
    super(message);
  }

  public ScicatCliException(String message, Throwable cause) {
    super(message, cause);
  }
}
