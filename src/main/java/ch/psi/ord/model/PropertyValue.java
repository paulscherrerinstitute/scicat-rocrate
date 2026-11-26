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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Data;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SchemaDO;

@Data
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
          Literal literal = value.asLiteral();
          if (literal.getDatatype().isValid(literal.getLexicalForm())) {
            values.add(new LiteralValue(literal));
          } else {
            context.addError(
                new PropertyError(
                    subject.getURI(),
                    SchemaDO.value.getURI(),
                    String.format(
                        "Lexical form '%s' is not a valid '%s'",
                        literal.getLexicalForm(), literal.getDatatypeURI())));
          }
        } else if (isOfType(value.asResource(), SchemaDO.PropertyValue)) {
          PropertyValue nested =
              context.getDeserializer(PropertyValue.class).deserialize(value, context);
          if (nested != null) {
            values.add(nested);
          }
        } else {
          List<String> types =
              listProperties(value.asResource(), RDF.type).stream().map(RDFNode::toString).toList();
          context.addError(
              new PropertyError(
                  subject.getURI(),
                  SchemaDO.value.getURI(),
                  String.format(
                      "Expected a literal or a '%s' but '%s' is of type [ %s ]",
                      SchemaDO.PropertyValue, value, String.join(", ", types))));
        }
      }

      if (values.size() > 1 && values.stream().allMatch(v -> v instanceof PropertyValue)) {
        checkDuplicateNames(
            subject,
            SchemaDO.value,
            values.stream().map(PropertyValue.class::cast).toList(),
            context);
      }

      return values.size() == 1 ? values.getFirst() : new ListValue(values);
    }
  }

  static void checkDuplicateNames(
      Resource subject,
      Property property,
      List<PropertyValue> properties,
      RdfDeserializationContext context) {
    Set<String> seen = new HashSet<>();
    properties.stream()
        .map(PropertyValue::getName)
        .filter(Objects::nonNull)
        .filter(name -> !seen.add(name))
        .map(name ->
            new PropertyError(
                subject.getURI(),
                property.getURI(),
                String.format("Duplicate '%s' with name '%s'", SchemaDO.PropertyValue, name)))
        .forEach(context::addError);
  }
}
