package ch.psi.rdf.ser;

public class RdfSerializationException extends Exception {
  public RdfSerializationException(String message) {
    super(message);
  }

  public RdfSerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
