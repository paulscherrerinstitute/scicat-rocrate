package ch.psi.scicat;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class RoCrateImporterTest {
    @Inject
    RoCrateImporter importer;

    private static final Logger LOG = Logger.getLogger(RoCrateImporterTest.class);

    @Nested
    @DisplayName("listPublications")
    class ListCreativeWorks {
        static final String exampleDoi1 = "10.123/abc123";

        @Test
        @DisplayName("Empty graph")
        public void test01() {
            Assertions.assertEquals(0, importer.listPublications().size());
        }

        @Test
        @DisplayName("One Publication")
        public void test02() {
            Model m = ModelFactory.createDefaultModel();
            m.createResource(SchemaDO.CreativeWork).addProperty(SchemaDO.identifier, exampleDoi1);

            importer.loadModel(m);
            Assertions.assertEquals(1, importer.listPublications().size());
        }

        @Test
        @DisplayName("Schema.org non-literal identifier")
        public void test03() {
            Model m = ModelFactory.createDefaultModel();
            m.createResource(SchemaDO.CreativeWork).addProperty(SchemaDO.identifier, m.createResource());

            importer.loadModel(m);
            Assertions.assertEquals(0, importer.listPublications().size());
        }

        @Test
        @DisplayName("Equivalent property")
        public void test04() {
            Model m = ModelFactory.createDefaultModel();
            Property identifierEquivalent = m.createProperty("http://example.org/id");
            m.add(identifierEquivalent, OWL.equivalentProperty, SchemaDO.identifier);
            m.createResource(SchemaDO.CreativeWork).addProperty(identifierEquivalent, exampleDoi1);
            m.listStatements().forEach(s -> LOG.debug(s.toString()));

            importer.loadModel(m);
            Assertions.assertEquals(1, importer.listPublications().size());
        }
    }
}
