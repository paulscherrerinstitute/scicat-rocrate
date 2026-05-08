package ch.psi.rdf.ser;

import ch.psi.rdf.RdfSerializerProvider;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class InstantSerializer implements RdfSerializer<Instant> {
  @Override
  public List<RDFNode> serialize(Instant value, Model model, RdfSerializerProvider provider)
      throws RdfSerializationException {
    ZonedDateTime zdt = ZonedDateTime.ofInstant(value, ZoneOffset.UTC);
    return List.of(model.createTypedLiteral(zdt.format(DateTimeFormatter.ISO_INSTANT)));
  }
}
