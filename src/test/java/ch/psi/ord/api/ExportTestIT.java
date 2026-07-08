package ch.psi.ord.api;

import ch.psi.scicat.ScicatLive;
import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.BeforeEach;

@QuarkusIntegrationTest
@WithTestResource(value = ScicatLive.class, scope = TestResourceScope.MATCHING_RESOURCES)
public class ExportTestIT extends ExportTest {
  @BeforeEach
  public void setup() {
    accessToken = login();
  }
}
