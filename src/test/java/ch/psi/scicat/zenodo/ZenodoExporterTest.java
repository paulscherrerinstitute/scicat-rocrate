package ch.psi.scicat.zenodo;

import org.apache.jena.vocabulary.SchemaDO;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import ch.psi.scicat.DoiUtils;
import ch.psi.scicat.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

@QuarkusTest
public class ZenodoExporterTest {
    @Inject
    ZenodoExporter exporter;

    @Test
    public void exportExamplePublication() {
        JsonObject export = exporter.toZenodoJsonLd(TestData.examplePublishedData);
        Assertions.assertEquals(SchemaDO.NS, export.getString("@context"));
        Assertions.assertEquals(DoiUtils.buildStandardUrl(TestData.examplePublishedData.getDoi()),
                export.getString("@id"));
    }
}
