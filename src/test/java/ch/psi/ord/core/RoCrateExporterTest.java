package ch.psi.ord.core;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class RoCrateExporterTest {
    @Inject
    RoCrateExporter exporter;
}
