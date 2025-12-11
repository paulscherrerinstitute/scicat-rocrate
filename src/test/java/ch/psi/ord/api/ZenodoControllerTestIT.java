package ch.psi.ord.api;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
public class ZenodoControllerTestIT extends ZenodoControllerTest {
  public ZenodoControllerTestIT() {
    accessToken = login();
  }
}
