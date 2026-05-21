package ch.psi.rdf.ser;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.jena.rdf.model.RDFNode;

public class InstantSerializer implements RdfSerializer<Instant> {
  @Override
  public List<RDFNode> serialize(Instant value, RdfSerializationContext context)
      throws RdfSerializationException {
    ZonedDateTime zdt = ZonedDateTime.ofInstant(value, ZoneOffset.UTC);
    return List.of(
        context.getModel().createTypedLiteral(zdt.format(DateTimeFormatter.ISO_INSTANT)));
  }
}
