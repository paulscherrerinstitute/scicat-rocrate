package ch.psi.ord.model;

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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@RdfClass(typesUri = SchemaDO.NS + "PropertyValue")
public class PropertyValue {
  @RdfProperty(uri = SchemaDO.NS + "name")
  String name;

  @RdfProperty(uri = SchemaDO.NS + "value")
  @RdfDeserialize(using = ValueDeserializer.class)
  Object value;

  @RdfProperty(uri = SchemaDO.NS + "unitText")
  String unitText;

  public static class ValueDeserializer implements RdfDeserializer<Object> {
    @Override
    public Object deserialize(RDFNode node, RdfDeserializationContext context)
        throws RdfDeserializationException {
      Resource subject =
          context
              .getCurrentSubject()
              .orElseThrow(() -> new RdfDeserializationException("current subject is not set"));

      List<Object> values = new ArrayList<>();
      for (RDFNode value : listProperties(subject, SchemaDO.value)) {
        values.add(
            value.isResource()
                ? context.getDeserializer(PropertyValue.class).deserialize(value, context)
                : value.asLiteral().getValue());
      }

      if (values.size() == 1) {
        return values.getFirst();
      }
      return values.isEmpty() ? null : values;
    }
  }
}
