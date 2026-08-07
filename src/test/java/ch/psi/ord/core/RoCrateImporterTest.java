package ch.psi.ord.core;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RoCrateImporterTest {
  @Inject RoCrateImporter importer;
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
}
