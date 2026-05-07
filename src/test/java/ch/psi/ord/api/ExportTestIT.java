package ch.psi.ord.api;

import ch.psi.scicat.ScicatLive;
import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@WithTestResource(value = ScicatLive.class, scope = TestResourceScope.MATCHING_RESOURCES)
public class ExportTestIT extends ExportTest {
  public ExportTestIT() {
    accessToken = login();
  }
}
