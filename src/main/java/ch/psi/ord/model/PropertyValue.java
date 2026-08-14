package ch.psi.ord.model;

import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@Slf4j
@RdfClass(typesUri = SchemaDO.NS + "PropertyValue")
public non-sealed class PropertyValue implements ValueContent {
  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  String name;

  @RdfProperty(uri = SchemaDO.NS + "value", minCardinality = 1)
  @RdfDeserialize(using = ValueDeserializer.class)
  ValueContent value;

  @RdfProperty(uri = SchemaDO.NS + "unitText")
  String unitText;

  public static class ValueDeserializer implements RdfDeserializer<ValueContent> {
    @Override
    public ValueContent deserialize(RDFNode node, RdfDeserializationContext context)
        throws RdfDeserializationException {
      Resource subject =
          context
              .getCurrentSubject()
              .orElseThrow(() -> new RdfDeserializationException("current subject is not set"));

      List<ValueContent> values = new ArrayList<>();
      for (RDFNode value : listProperties(subject, SchemaDO.value)) {
        if (value.isLiteral()) {
          values.add(new LiteralValue(value.asLiteral()));
        } else if (isOfType(value.asResource(), SchemaDO.PropertyValue)) {
          values.add(context.getDeserializer(PropertyValue.class).deserialize(value, context));
        } else {
          log.warn(
              "Dropping value '{}' of property '{}': not a literal nor a '{}'",
              value,
              subject,
              SchemaDO.PropertyValue);
        }
      }

      return values.size() == 1 ? values.getFirst() : new ListValue(values);
    }
  }
}
