package ch.psi.rdf.deser;

public class RdfDeserializationException extends Exception {
  public RdfDeserializationException(String message) {
    super(message);
  }

  public RdfDeserializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
