package ch.psi.ord.model;

import static ch.psi.rdf.RdfUtils.listProperties;

import ch.psi.ord.model.PropertyValue.PropertyValueDeserializer;
import ch.psi.rdf.RdfUtils;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@Slf4j
@RdfClass(typesUri = SchemaDO.NS + "PropertyValue")
@RdfDeserialize(using = PropertyValueDeserializer.class)
public class PropertyValue {
  @RdfProperty(uri = SchemaDO.NS + "name")
  String name;

  @RdfProperty(uri = SchemaDO.NS + "value")
  Object value;

  @RdfProperty(uri = SchemaDO.NS + "unitText")
  String unitText;

  public static class PropertyValueDeserializer implements RdfDeserializer<PropertyValue> {
    @SuppressWarnings("unchecked")
    @Override
    public PropertyValue deserialize(RDFNode node, RdfDeserializationContext context)
        throws RdfDeserializationException {
      if (!node.isResource()) {
        throw new RdfDeserializationException(
            "Expected a resource for PropertyValue, but found: " + node);
      }

      Resource res = node.asResource();
      PropertyValue result = new PropertyValue();

      RdfDeserializer<String> stringDeserializer =
          (RdfDeserializer<String>) context.getDeserializer(String.class);

      Set<RDFNode> name = listProperties(res, SchemaDO.name);
      name.removeIf((n) -> !n.isLiteral());
      if (!name.isEmpty()) {
        result.name = stringDeserializer.deserialize(name.iterator().next(), context);
      }

      Set<RDFNode> unitText = listProperties(res, RdfUtils.switchScheme(SchemaDO.unitText));
      unitText.removeIf((n) -> !n.isLiteral());
      if (!unitText.isEmpty()) {
        result.unitText = stringDeserializer.deserialize(unitText.iterator().next(), context);
      }

      List<Object> valueList = new ArrayList<>();
      for (RDFNode v : listProperties(res, SchemaDO.value)) {
        if (v.isResource()) {
          valueList.add(deserialize(v, context));
        } else {
          valueList.add(v.asLiteral().getValue());
        }
      }

      if (valueList.size() == 1) {
        result.value = valueList.get(0);
      } else if (!valueList.isEmpty()) {
        result.value = valueList;
      }
      return result;
    }
  }
}
