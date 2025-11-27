package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ScicatConnectionHealthCheckTest extends EndpointTest {
  @Test
  public void healthyScicat() {
    when(scicatClient.isHealthy()).thenReturn(true);
    given().when().get("health").then().statusCode(200);
  }

  @Test
  public void unhealthyScicat() {
    when(scicatClient.isHealthy()).thenReturn(false);
    given().when().get("health").then().statusCode(503);
  }
}
