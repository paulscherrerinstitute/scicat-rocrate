package ch.psi.ord.core;

public class RoCrateException extends Exception {
  public RoCrateException(String message) {
    super(message);
  }

  public RoCrateException(String message, Throwable cause) {
    super(message, cause);
  }
}
