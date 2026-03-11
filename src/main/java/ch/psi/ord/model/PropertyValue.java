package ch.psi.ord.model;

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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
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

      Statement nameStmt = res.getProperty(RdfUtils.switchScheme(SchemaDO.name));
      if (nameStmt != null) {
        result.name = stringDeserializer.deserialize(nameStmt.getObject(), context);
      }

      Statement unitStmt = res.getProperty(RdfUtils.switchScheme(SchemaDO.unitText));
      if (unitStmt != null) {
        result.unitText = stringDeserializer.deserialize(unitStmt.getObject(), context);
      }

      List<Object> valueList = new ArrayList<>();
      res.listProperties(RdfUtils.switchScheme(SchemaDO.value))
          .forEachRemaining(
              stmt -> {
                RDFNode valueNode = stmt.getObject();
                try {
                  if (valueNode.isResource()) {
                    valueList.add(this.deserialize(valueNode, context));
                  } else {
                    valueList.add(valueNode.asLiteral().getString());
                  }
                } catch (RdfDeserializationException e) {
                  log.error("Failed to deserialize nested property value", e);
                }
              });

      if (valueList.size() == 1) {
        result.value = valueList.get(0);
      } else if (!valueList.isEmpty()) {
        result.value = valueList;
      }
      return result;
    }
  }
}
