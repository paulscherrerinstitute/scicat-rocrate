package ch.psi.ord.core;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SchemaDO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class RoCrateImporterTest {
  @Inject RoCrateImporter importer;

  private static final Logger logger = LoggerFactory.getLogger(RoCrateImporterTest.class);

  Model m;

  @BeforeEach
  void setup() {
    this.m = ModelFactory.createDefaultModel();
  }

  @Nested
  @DisplayName("loadModel")
  class LoadModel {
    @Test
    @DisplayName("Null model")
    public void test00() {
      importer.loadModel(null);
    }
  }

  @Nested
  @DisplayName("listPublications")
  class ListCreativeWorks {
    static final String validDoi = "10.123/abc123";
    static final String invalidDoi = "20.123/abc123";

    @Test
    @DisplayName("Empty graph")
    public void test01() {
      Assertions.assertEquals(0, importer.listPublications().size());
    }

    @Test
    @DisplayName("One Publication")
    public void test02() {
      m.createResource(SchemaDO.CreativeWork).addProperty(SchemaDO.identifier, validDoi);

      importer.loadModel(m);
      Assertions.assertEquals(1, importer.listPublications().size());
    }

    @Test
    @DisplayName("One Publication - invalid identifier")
    public void test03() {
      m.createResource(SchemaDO.CreativeWork).addProperty(SchemaDO.identifier, invalidDoi);

      importer.loadModel(m);
      Assertions.assertEquals(0, importer.listPublications().size());
    }

    @Test
    @DisplayName("Schema.org non-literal identifier")
    public void test04() {
      m.createResource(SchemaDO.CreativeWork).addProperty(SchemaDO.identifier, m.createResource());

      importer.loadModel(m);
      Assertions.assertEquals(0, importer.listPublications().size());
    }

    @Test
    @DisplayName("Equivalent property")
    public void test05() {
      Property identifierEquivalent = m.createProperty("http://example.org/id");
      m.add(identifierEquivalent, OWL.equivalentProperty, SchemaDO.identifier);
      m.createResource(SchemaDO.CreativeWork).addProperty(identifierEquivalent, validDoi);

      importer.loadModel(m);
      Assertions.assertEquals(1, importer.listPublications().size());
    }

    @Test
    @DisplayName("Equivalent property - invalid identifier")
    public void test06() {
      Property identifierEquivalent = m.createProperty("http://example.org/id");
      m.add(identifierEquivalent, OWL.equivalentProperty, SchemaDO.identifier);
      m.createResource(SchemaDO.CreativeWork).addProperty(identifierEquivalent, invalidDoi);

      importer.loadModel(m);
      Assertions.assertEquals(0, importer.listPublications().size());
    }

    @Test
    @DisplayName("Multiple Publications")
    public void test07() {
      for (int i = 0; i < 5; i++) {
        m.createResource(SchemaDO.CreativeWork).addProperty(SchemaDO.identifier, validDoi + i);
      }

      importer.loadModel(m);
      Assertions.assertEquals(5, importer.listPublications().size());
    }
  }
}
