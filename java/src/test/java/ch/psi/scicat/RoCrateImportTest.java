package ch.psi.scicat;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SchemaDO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class RoCrateImportTest {
    @Inject
    RoCrateImporter importer;

    @Nested
    @DisplayName("listCreativeWorks")
    class ListCreativeWorks {
        @Test
        @DisplayName("Empty graph")
        public void test01() {
            Assertions.assertEquals(0, importer.listPublications().size(), 0);
        }

        @Test
        @DisplayName("One CreativeWork")
        public void test02() {
            Model m = ModelFactory.createDefaultModel();
            m.createResource(SchemaDO.CreativeWork);

            importer.loadModel(m);
            Assertions.assertEquals(1, importer.listPublications().size());
        }

        @Test
        @DisplayName("Multiple CreativeWork")
        public void test03() {
            Model m = ModelFactory.createDefaultModel();
            for (int i = 0; i < 10; i++)
                m.createResource(SchemaDO.CreativeWork);

            importer.loadModel(m);
            Assertions.assertEquals(10, importer.listPublications().size());
        }
    }

    @Nested
    @DisplayName("Extract DOI")
    class ExtractDOI {
        String doi = "10.1234/56789";

        @Test
        @DisplayName("Schema.org identifier")
        public void test01() {
            Model m = ModelFactory.createDefaultModel();
            Resource r = m.createResource().addProperty(SchemaDO.identifier, doi);

            importer.loadModel(m);
            // Assertions.assertEquals(doi, importer.extractDoi(r).get());
        }

        @Test
        @DisplayName("Schema.org non-literal identifier")
        public void test02() {
            Model m = ModelFactory.createDefaultModel();
            Resource o = m.createResource();
            Resource r = m.createResource().addProperty(SchemaDO.identifier, o);

            importer.loadModel(m);
            // Assertions.assertTrue(importer.extractDoi(r).isEmpty());
        }

        @Test
        @DisplayName("Equivalent property identifier")
        public void test03() {
            Model m = ModelFactory.createDefaultModel();
            Property identifierEquivalent = m.createProperty("http://example.org/id");
            m.add(identifierEquivalent, OWL.equivalentProperty, SchemaDO.identifier);
            Resource r = m.createResource().addProperty(identifierEquivalent, doi);

            importer.loadModel(m);
            // Assertions.assertEquals(doi, importer.extractDoi(r).get());
        }
    }
}
