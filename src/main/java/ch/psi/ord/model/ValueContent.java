package ch.psi.ord.model;

import ch.psi.ord.model.ValueContent.ListValue;
import ch.psi.ord.model.ValueContent.LiteralValue;
import java.util.List;
import org.apache.jena.rdf.model.Literal;

public sealed interface ValueContent permits LiteralValue, PropertyValue, ListValue {
  record LiteralValue(Literal literal) implements ValueContent {}

  record ListValue(List<ValueContent> values) implements ValueContent {}
}
