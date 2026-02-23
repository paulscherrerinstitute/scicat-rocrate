package ch.psi.rdf;

public class RdfSerializationException extends Exception {
  public RdfSerializationException(String message) {
    super(message);
  }

  public RdfSerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
