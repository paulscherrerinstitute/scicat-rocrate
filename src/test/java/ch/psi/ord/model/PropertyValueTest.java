package ch.psi.ord.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PropertyValueTest {
  private static final String PROPERTY_ID = "https://example.org/crate/#property";
  private final RdfMapper rdfMapper = new RdfMapper();

  private PropertyValue deserialize(String jsonLd) throws RdfDeserializationException {
    Model model = RDFParser.fromString(jsonLd, Lang.JSONLD11).toModel();
    DeserializationReport<PropertyValue> report =
        rdfMapper.deserialize(model.createResource(PROPERTY_ID), PropertyValue.class);
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
        () -> assertEquals("300", property.getValue()),
        () -> assertEquals("K", property.getUnitText()));
  }

  @Test
  @DisplayName("Property without a value")
  public void test01() throws RdfDeserializationException {
    PropertyValue property =
        deserialize(
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

    assertAll(
        () -> assertEquals("temperature", property.getName()),
        () -> assertNull(property.getValue()),
        () -> assertNull(property.getUnitText()));
  }

  @Test
  @DisplayName("Literal values keep their datatype")
  public void test02() throws RdfDeserializationException {
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

    assertEquals(300.5, assertInstanceOf(Double.class, property.getValue()));
  }

  @Test
  @DisplayName("Several literal values are collected in a list")
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
                  "name": "detectors",
                  "value": [ "eiger", "pilatus" ]
                }
              ]
            }
            """);

    List<?> values = assertInstanceOf(List.class, property.getValue());
    assertEquals(Set.of("eiger", "pilatus"), Set.copyOf(values));
  }

  @Test
  @DisplayName("Resource values are deserialized as nested properties")
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

    List<?> values = assertInstanceOf(List.class, property.getValue());
    Set<String> names =
        values.stream()
            .map(v -> assertInstanceOf(PropertyValue.class, v))
            .map(PropertyValue::getName)
            .collect(Collectors.toSet());

    assertAll(
        () -> assertEquals("sample", property.getName()),
        () -> assertEquals(Set.of("name", "mass"), names));
  }
}
