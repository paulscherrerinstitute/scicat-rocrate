package ch.psi.ord.api;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
public class ExportTestIT extends ExportTest {
  public ExportTestIT() {
    accessToken = login();
  }
}
