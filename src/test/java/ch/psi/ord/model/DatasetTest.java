package ch.psi.ord.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DatasetTest {
  private static final String DATASET_ID = "https://example.org/dataset/1";

  private final RdfMapper rdfMapper = new RdfMapper();

  private Resource dataset(String relationships) {
    String jsonLd =
        """
        {
          "@context": {
            "@vocab": "https://schema.org/",
            "scicat": "https://scicatproject.org/"
          },
          "@id": "%s",
          "@type": "Dataset",
          "name": "A dataset",
          "scicat:relationships": %s
        }
        """
            .formatted(DATASET_ID, relationships);

    Model model = RDFParser.fromString(jsonLd, Lang.JSONLD11).toModel();
    return model.createResource(DATASET_ID);
  }

  @Nested
  @DisplayName("relationships")
  class Relationships {
    @Test
    @DisplayName("No relationship")
    public void test00() throws RdfDeserializationException {
      DeserializationReport<Dataset> report = rdfMapper.deserialize(dataset("[]"), Dataset.class);

      assertTrue(report.isValid(), report.toString());
      assertTrue(report.get().getRelationships().isEmpty());
    }

    @Test
    @DisplayName("All properties")
    public void test01() throws RdfDeserializationException {
      DeserializationReport<Dataset> report =
          rdfMapper.deserialize(
              dataset(
                  """
                  {
                    "@type": "scicat:Relationship",
                    "scicat:identifier": "PID.SAMPLE.PREFIX/psi_ds3",
                    "scicat:identifierType": "Handle",
                    "scicat:relationship": "IsDerivedFrom",
                    "scicat:entityType": "Dataset",
                    "scicat:externalId": "external-1"
                  }
                  """),
              Dataset.class);

      assertTrue(report.isValid(), report.toString());
      assertEquals(1, report.get().getRelationships().size());
      ScicatRelationship relationship = report.get().getRelationships().getFirst();
      assertEquals("PID.SAMPLE.PREFIX/psi_ds3", relationship.getIdentifier());
      assertEquals("Handle", relationship.getIdentifierType());
      assertEquals("IsDerivedFrom", relationship.getRelationship());
      assertEquals("Dataset", relationship.getEntityType());
      assertEquals("external-1", relationship.getExternalId());
    }

    @Test
    @DisplayName("Only the identifier")
    public void test02() throws RdfDeserializationException {
      DeserializationReport<Dataset> report =
          rdfMapper.deserialize(
              dataset(
                  """
                  {
                    "@type": "scicat:Relationship",
                    "scicat:identifier": "PID.SAMPLE.PREFIX/psi_ds3"
                  }
                  """),
              Dataset.class);

      assertTrue(report.isValid(), report.toString());
      ScicatRelationship relationship = report.get().getRelationships().getFirst();
      assertEquals("PID.SAMPLE.PREFIX/psi_ds3", relationship.getIdentifier());
      assertNull(relationship.getRelationship());
    }

    @Test
    @DisplayName("Multiple relationships")
    public void test03() throws RdfDeserializationException {
      DeserializationReport<Dataset> report =
          rdfMapper.deserialize(
              dataset(
                  """
                  [
                    {
                      "@type": "scicat:Relationship",
                      "scicat:identifier": "PID.SAMPLE.PREFIX/psi_ds3",
                      "scicat:relationship": "IsDerivedFrom"
                    },
                    {
                      "@type": "scicat:Relationship",
                      "scicat:identifier": "10.1364/oe.23.027975",
                      "scicat:identifierType": "DOI",
                      "scicat:relationship": "References"
                    }
                  ]
                  """),
              Dataset.class);

      assertTrue(report.isValid(), report.toString());
      assertEquals(2, report.get().getRelationships().size());
    }

    @Test
    @DisplayName("Missing identifier")
    public void test04() throws RdfDeserializationException {
      DeserializationReport<Dataset> report =
          rdfMapper.deserialize(
              dataset(
                  """
                  {
                    "@type": "scicat:Relationship",
                    "scicat:relationship": "IsDerivedFrom"
                  }
                  """),
              Dataset.class);

      assertFalse(report.isValid());
      assertEquals(
          Set.of(
              new PropertyError(
                  null, "https://scicatproject.org/identifier", "Missing required property")),
          report.getErrors());
    }

    @Test
    @DisplayName("Wrong type")
    public void test05() throws RdfDeserializationException {
      DeserializationReport<Dataset> report =
          rdfMapper.deserialize(
              dataset(
                  """
                  {
                    "@type": "CreativeWork",
                    "scicat:identifier": "PID.SAMPLE.PREFIX/psi_ds3"
                  }
                  """),
              Dataset.class);

      assertFalse(report.isValid());
      assertTrue(
          report.getErrors().stream()
              .anyMatch(
                  e ->
                      e.getMessage()
                          .equals(
                              "Expected '@type' to be one of [ https://scicatproject.org/Relationship"
                                  + " ] but is [ https://schema.org/CreativeWork ]")),
          report.toString());
    }
  }
}
