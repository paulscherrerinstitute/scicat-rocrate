package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ScicatConnectionHealthCheckTest extends EndpointTest {
  @Test
  public void healthyScicat() {
    when(scicatService.health()).thenReturn(RestResponse.ok());
    given().when().get("/api/v1/health").then().statusCode(200);
  }

  @Test
  public void unhealthyScicat() {
    when(scicatService.health()).thenReturn(RestResponse.status(Status.SERVICE_UNAVAILABLE));
    given().when().get("/api/v1/health").then().statusCode(503);
  }

  @Test
  public void unreachableScicat() {
    when(scicatService.health()).thenThrow(new WebApplicationException(503));
    given().when().get("/api/v1/health").then().statusCode(503);
  }
}
