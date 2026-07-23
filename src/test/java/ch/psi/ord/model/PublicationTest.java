package ch.psi.ord.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.vocabulary.SchemaDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PublicationTest {
  private final RdfMapper rdfMapper = new RdfMapper();

  private Resource publication(String hasPart) {
    String jsonLd =
        """
        {
          "@context": { "@vocab": "https://schema.org/" },
          "@id": "https://doi.org/10.1234/test",
          "@type": "Collection",
          "identifier": "10.1234/test",
          "creator": {
            "@id": "https://example.org/person/1",
            "@type": "Person",
            "name": "Jane Doe"
          },
          "name": "Test publication",
          "publisher": {
            "@id": "https://example.org/org/1",
            "@type": "Organization",
            "name": "PSI"
          },
          "datePublished": "2023-01-01",
          "abstract": "abstract",
          "description": "description",
          "hasPart": %s
        }
        """
            .formatted(hasPart);

    Model model = RDFParser.fromString(jsonLd, Lang.JSONLD11).toModel();
    return model.createResource("https://doi.org/10.1234/test");
  }

  @Nested
  @DisplayName("hasPart")
  class HasPart {
    @Test
    @DisplayName("Only files")
    public void test00() throws RdfDeserializationException {
      Resource publication =
          publication(
              """
              {
                "@id": "file:///data/file.bin",
                "@type": "MediaObject"
              }
              """);

      DeserializationReport<Publication> report =
          rdfMapper.deserialize(publication, Publication.class);

      assertTrue(report.isValid(), report.toString());
      assertTrue(report.get().getHasPart().getDatasets().isEmpty());
      assertEquals(1, report.get().getHasPart().getFiles().size());
    }

    @Test
    @DisplayName("Mixed datasets and files")
    public void test01() throws RdfDeserializationException {
      Resource publication =
          publication(
              """
              [
                {
                  "@id": "https://example.org/dataset/1",
                  "@type": "Dataset",
                  "name": "a dataset"
                },
                {
                  "@id": "file:///data/file.bin",
                  "@type": "MediaObject"
                }
              ]
              """);

      DeserializationReport<Publication> report =
          rdfMapper.deserialize(publication, Publication.class);

      assertTrue(report.isValid(), report.toString());
      assertEquals(1, report.get().getHasPart().getDatasets().size());
      assertEquals(1, report.get().getHasPart().getFiles().size());
    }

    @Test
    @DisplayName("File nested behind an intermediate resource")
    public void test02() throws RdfDeserializationException {
      Resource publication =
          publication(
              """
              {
                "@id": "https://example.org/collection/1",
                "@type": "CreativeWork",
                "hasPart": {
                  "@id": "file:///data/nested/file.bin",
                  "@type": "MediaObject"
                }
              }
              """);

      DeserializationReport<Publication> report =
          rdfMapper.deserialize(publication, Publication.class);

      assertTrue(report.isValid(), report.toString());
      assertEquals(1, report.get().getHasPart().getFiles().size());
    }

    @Test
    @DisplayName("Subtree without a Dataset or a MediaObject")
    public void test03() throws RdfDeserializationException {
      Resource publication =
          publication(
              """
              {
                "@id": "https://example.org/collection/1",
                "@type": "CreativeWork"
              }
              """);

      DeserializationReport<Publication> report =
          rdfMapper.deserialize(publication, Publication.class);

      assertFalse(report.isValid());
      assertTrue(
          report.getErrors().stream()
              .anyMatch(
                  e ->
                      e.getMessage()
                          .equals(
                              String.format(
                                  "sub-tree should contain at least one '%s' or '%s'",
                                  SchemaDO.Dataset, SchemaDO.MediaObject))));
    }
  }
}
