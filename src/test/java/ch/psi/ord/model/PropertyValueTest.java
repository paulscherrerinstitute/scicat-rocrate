package ch.psi.ord.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.ord.model.ValueContent.ListValue;
import ch.psi.ord.model.ValueContent.LiteralValue;
import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PropertyValueTest {
  private static final String PROPERTY_ID = "https://example.org/crate/#property";
  private final RdfMapper rdfMapper = new RdfMapper();

  private DeserializationReport<PropertyValue> report(String jsonLd)
      throws RdfDeserializationException {
    Model model = RDFParser.fromString(jsonLd, Lang.JSONLD11).toModel();
    return rdfMapper.deserialize(model.createResource(PROPERTY_ID), PropertyValue.class);
  }

  private PropertyValue deserialize(String jsonLd) throws RdfDeserializationException {
    DeserializationReport<PropertyValue> report = report(jsonLd);
    assertTrue(report.isValid(), report.toString());

    return report.get();
  }

  @Test
  @DisplayName("Property with a single literal value")
  public void test00() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "temperature",
                  "value": "300",
                  "unitText": "K"
                }
              ]
            }
            """);

    assertAll(
        () -> assertEquals("temperature", property.getName()),
        () ->
            assertEquals(
                new LiteralValue(ResourceFactory.createStringLiteral("300")), property.getValue()),
        () -> assertEquals("K", property.getUnitText()));
  }

  @Test
  @DisplayName("A property without a value is reported as invalid")
  public void test01() throws RdfDeserializationException {
    DeserializationReport<PropertyValue> report =
        report(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "temperature"
                }
              ]
            }
            """);

    assertFalse(report.isValid(), report.toString());
  }

  @Test
  @DisplayName("A property without a name is reported as invalid")
  public void test02() throws RdfDeserializationException {
    DeserializationReport<PropertyValue> report =
        report(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "value": "300"
                }
              ]
            }
            """);

    assertFalse(report.isValid(), report.toString());
  }

  @Test
  @DisplayName("Literal values keep their datatype")
  public void test03() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "temperature",
                  "value": 300.5
                }
              ]
            }
            """);

    // Asserted through the literal rather than by equality: Jena compares lexical forms and the
    // parser canonicalises 300.5 to "3.005E2".
    Literal literal = assertInstanceOf(LiteralValue.class, property.getValue()).literal();
    assertAll(
        () -> assertEquals(300.5, literal.getDouble()),
        () -> assertEquals(XSDDatatype.XSDdouble, literal.getDatatype()));
  }

  @Test
  @DisplayName("The language tag of a literal value is kept")
  public void test04() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "description",
                  "value": { "@value": "Bonjour", "@language": "fr" }
                }
              ]
            }
            """);

    assertEquals(
        new LiteralValue(ResourceFactory.createLangLiteral("Bonjour", "fr")), property.getValue());
  }

  @Test
  @DisplayName("Several literal values are collected in a list")
  public void test05() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "detectors",
                  "value": [ "eiger", "pilatus" ]
                }
              ]
            }
            """);

    ListValue values = assertInstanceOf(ListValue.class, property.getValue());
    assertEquals(
        Set.of(
            new LiteralValue(ResourceFactory.createStringLiteral("eiger")),
            new LiteralValue(ResourceFactory.createStringLiteral("pilatus"))),
        Set.copyOf(values.values()));
  }

  @Test
  @DisplayName("A single resource value is deserialized as a nested property")
  public void test06() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "sample",
                  "value": { "@id": "https://example.org/crate/#name" }
                },
                {
                  "@id": "https://example.org/crate/#name",
                  "@type": "PropertyValue",
                  "name": "name",
                  "value": "LaB6"
                }
              ]
            }
            """);

    PropertyValue nested = assertInstanceOf(PropertyValue.class, property.getValue());
    assertAll(
        () -> assertEquals("name", nested.getName()),
        () ->
            assertEquals(
                new LiteralValue(ResourceFactory.createStringLiteral("LaB6")), nested.getValue()));
  }

  @Test
  @DisplayName("Several resource values are deserialized as nested properties")
  public void test07() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "sample",
                  "value": [
                    { "@id": "https://example.org/crate/#name" },
                    { "@id": "https://example.org/crate/#mass" }
                  ]
                },
                {
                  "@id": "https://example.org/crate/#name",
                  "@type": "PropertyValue",
                  "name": "name",
                  "value": "LaB6"
                },
                {
                  "@id": "https://example.org/crate/#mass",
                  "@type": "PropertyValue",
                  "name": "mass",
                  "value": "12",
                  "unitText": "mg"
                }
              ]
            }
            """);

    ListValue values = assertInstanceOf(ListValue.class, property.getValue());
    Set<String> names =
        values.values().stream()
            .map(v -> assertInstanceOf(PropertyValue.class, v))
            .map(PropertyValue::getName)
            .collect(Collectors.toSet());

    assertAll(
        () -> assertEquals("sample", property.getName()),
        () -> assertEquals(Set.of("name", "mass"), names));
  }

  @Test
  @DisplayName("Literal and resource values can be mixed in the same list")
  public void test08() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "sample",
                  "value": [
                    "orphan",
                    { "@id": "https://example.org/crate/#name" }
                  ]
                },
                {
                  "@id": "https://example.org/crate/#name",
                  "@type": "PropertyValue",
                  "name": "name",
                  "value": "LaB6"
                }
              ]
            }
            """);

    ListValue values = assertInstanceOf(ListValue.class, property.getValue());
    assertAll(
        () -> assertEquals(2, values.values().size()),
        () ->
            assertEquals(
                Set.of(LiteralValue.class, PropertyValue.class),
                values.values().stream().map(Object::getClass).collect(Collectors.toSet())));
  }

  @Test
  @DisplayName("A property referencing itself is reported as invalid")
  public void test09() throws RdfDeserializationException {
    DeserializationReport<PropertyValue> report =
        report(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "loop",
                  "value": { "@id": "https://example.org/crate/#property" }
                }
              ]
            }
            """);

    assertFalse(report.isValid());
    assertEquals(
        Set.of(new PropertyError(PROPERTY_ID, "@id", "Cyclic reference to '" + PROPERTY_ID + "'")),
        report.getErrors());
  }

  @Test
  @DisplayName("A cycle between two properties is reported as invalid")
  public void test10() throws RdfDeserializationException {
    DeserializationReport<PropertyValue> report =
        report(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "a",
                  "value": { "@id": "https://example.org/crate/#other" }
                },
                {
                  "@id": "https://example.org/crate/#other",
                  "@type": "PropertyValue",
                  "name": "b",
                  "value": { "@id": "https://example.org/crate/#property" }
                }
              ]
            }
            """);

    assertFalse(report.isValid());
    assertEquals(
        Set.of(new PropertyError(PROPERTY_ID, "@id", "Cyclic reference to '" + PROPERTY_ID + "'")),
        report.getErrors());
  }

  @Test
  @DisplayName("A property shared by two parents is not a cycle")
  public void test11() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "root",
                  "value": [
                    { "@id": "https://example.org/crate/#a" },
                    { "@id": "https://example.org/crate/#b" }
                  ]
                },
                {
                  "@id": "https://example.org/crate/#a",
                  "@type": "PropertyValue",
                  "name": "a",
                  "value": { "@id": "https://example.org/crate/#shared" }
                },
                {
                  "@id": "https://example.org/crate/#b",
                  "@type": "PropertyValue",
                  "name": "b",
                  "value": { "@id": "https://example.org/crate/#shared" }
                },
                {
                  "@id": "https://example.org/crate/#shared",
                  "@type": "PropertyValue",
                  "name": "shared",
                  "value": 1
                }
              ]
            }
            """);

    ListValue values = assertInstanceOf(ListValue.class, property.getValue());
    assertEquals(2, values.values().size());
  }

  @Test
  @DisplayName("A language-tagged literal value is valid")
  public void test13() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "comment",
                  "value": { "@value": "bonjour", "@language": "fr" }
                }
              ]
            }
            """);

    assertEquals(
        new LiteralValue(ResourceFactory.createLangLiteral("bonjour", "fr")), property.getValue());
  }

  @Test
  @DisplayName("An ill-typed literal value is reported as invalid")
  public void test12() throws RdfDeserializationException {
    DeserializationReport<PropertyValue> report =
        report(
            """
            {
              "@context": {
                "@vocab": "https://schema.org/",
                "xsd": "http://www.w3.org/2001/XMLSchema#"
              },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "count",
                  "value": { "@value": "abc", "@type": "xsd:integer" }
                }
              ]
            }
            """);

    assertFalse(report.isValid());
    assertEquals(
        Set.of(
            new PropertyError(
                PROPERTY_ID,
                "https://schema.org/value",
                "Lexical form 'abc' is not a valid 'http://www.w3.org/2001/XMLSchema#integer'")),
        report.getErrors());
  }

  @Test
  @DisplayName("A value that is neither a literal nor a PropertyValue is reported as invalid")
  public void test14() throws RdfDeserializationException {
    DeserializationReport<PropertyValue> report =
        report(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/#property",
                  "@type": "PropertyValue",
                  "name": "sample",
                  "value": { "@id": "https://example.org/crate/#thing" }
                },
                {
                  "@id": "https://example.org/crate/#thing",
                  "@type": "Thing"
                }
              ]
            }
            """);

    assertFalse(report.isValid());
    assertEquals(
        Set.of(
            new PropertyError(
                PROPERTY_ID,
                "https://schema.org/value",
                "Expected a literal or a 'https://schema.org/PropertyValue' but"
                    + " 'https://example.org/crate/#thing' is of type [ https://schema.org/Thing ]")),
        report.getErrors());
  }
}
