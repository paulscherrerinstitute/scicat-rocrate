package ch.psi.ord.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RootDatasetTest {
  private static final String ROOT_ID = "https://example.org/crate/";

  private final RdfMapper rdfMapper = new RdfMapper();

  private RootDataset deserialize(String jsonLd) throws RdfDeserializationException {
    Model model = RDFParser.fromString(jsonLd, Lang.JSONLD11).toModel();
    DeserializationReport<RootDataset> report =
        rdfMapper.deserialize(model.createResource(ROOT_ID), RootDataset.class);
    assertTrue(report.isValid(), report.toString());

    return report.get();
  }

  private Set<String> ids(RootDataset rootDataset, Class<?> type) {
    return rootDataset.getHasPart().get(type).stream()
        .map(Resource::toString)
        .collect(Collectors.toSet());
  }

  @Test
  @DisplayName("Root without hasPart is empty")
  public void test00() throws RdfDeserializationException {
    RootDataset rootDataset =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/",
                  "@type": "Dataset",
                  "hasPart": []
                }
              ]
            }
            """);

    assertTrue(rootDataset.isEmpty());
  }

  @Test
  @DisplayName("Collections and Datasets are collected separately")
  public void test01() throws RdfDeserializationException {
    RootDataset rootDataset =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/",
                  "@type": "Dataset",
                  "hasPart": [
                    { "@id": "https://doi.org/10.1234/pub1" },
                    { "@id": "https://example.org/ds1" }
                  ]
                },
                {
                  "@id": "https://doi.org/10.1234/pub1",
                  "@type": "Collection"
                },
                {
                  "@id": "https://example.org/ds1",
                  "@type": "Dataset"
                }
              ]
            }
            """);

    assertEquals(Set.of("https://doi.org/10.1234/pub1"), ids(rootDataset, Publication.class));
    assertEquals(Set.of("https://example.org/ds1"), ids(rootDataset, Dataset.class));
  }

  @Test
  @DisplayName("Entities behind an intermediate node are collected")
  public void test02() throws RdfDeserializationException {
    RootDataset rootDataset =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/",
                  "@type": "Dataset",
                  "hasPart": [{ "@id": "https://example.org/group" }]
                },
                {
                  "@id": "https://example.org/group",
                  "@type": "CreativeWork",
                  "hasPart": [{ "@id": "https://doi.org/10.1234/pub1" }]
                },
                {
                  "@id": "https://doi.org/10.1234/pub1",
                  "@type": "Collection"
                }
              ]
            }
            """);

    assertEquals(Set.of("https://doi.org/10.1234/pub1"), ids(rootDataset, Publication.class));
  }

  @Test
  @DisplayName("The parts of a Publication are not collected as top level entities")
  public void test03() throws RdfDeserializationException {
    RootDataset rootDataset =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/",
                  "@type": "Dataset",
                  "hasPart": [{ "@id": "https://doi.org/10.1234/pub1" }]
                },
                {
                  "@id": "https://doi.org/10.1234/pub1",
                  "@type": "Collection",
                  "hasPart": [{ "@id": "https://example.org/ds1" }]
                },
                {
                  "@id": "https://example.org/ds1",
                  "@type": "Dataset"
                }
              ]
            }
            """);

    assertEquals(Set.of("https://doi.org/10.1234/pub1"), ids(rootDataset, Publication.class));
    assertTrue(
        rootDataset.getHasPart().get(Dataset.class).isEmpty(),
        "Datasets of a Publication must be imported through that Publication");
  }

  @Test
  @DisplayName("A cycle in the hasPart tree terminates")
  public void test04() throws RdfDeserializationException {
    RootDataset rootDataset =
        deserialize(
            """
            {
              "@context": { "@vocab": "https://schema.org/" },
              "@graph": [
                {
                  "@id": "https://example.org/crate/",
                  "@type": "Dataset",
                  "hasPart": [{ "@id": "https://example.org/a" }]
                },
                {
                  "@id": "https://example.org/a",
                  "@type": "CreativeWork",
                  "hasPart": [{ "@id": "https://example.org/b" }]
                },
                {
                  "@id": "https://example.org/b",
                  "@type": "CreativeWork",
                  "hasPart": [
                    { "@id": "https://example.org/a" },
                    { "@id": "https://doi.org/10.1234/pub1" }
                  ]
                },
                {
                  "@id": "https://doi.org/10.1234/pub1",
                  "@type": "Collection"
                }
              ]
            }
            """);

    assertEquals(Set.of("https://doi.org/10.1234/pub1"), ids(rootDataset, Publication.class));
  }
}
