package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

import ch.psi.scicat.client.ScicatService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ScicatConnectionHealthCheckTest {
  @InjectMock @RestClient ScicatService scicatService;

  @Test
  public void healthyScicat() {
    when(scicatService.root()).thenReturn(RestResponse.status(Status.OK));
    given().when().get("health").then().statusCode(200);
  }

  @Test
  public void unhealthyScicat() {
    when(scicatService.root()).thenReturn(RestResponse.status(Status.SERVICE_UNAVAILABLE));
    given().when().get("health").then().statusCode(503);
  }
}
