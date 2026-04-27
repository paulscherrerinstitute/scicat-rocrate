package ch.psi.ord.api;

import ch.psi.scicat.ScicatLive;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@QuarkusTestResource(ScicatLive.class)
public class ImportTestIT extends ImportTest {
  public ImportTestIT() {
    accessToken = login();
  }
}
