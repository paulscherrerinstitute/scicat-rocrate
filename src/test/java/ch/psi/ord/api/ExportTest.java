package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

import ch.psi.scicat.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ExportTest extends EndpointTest {
  @Test
  @DisplayName("Export to zip")
  public void test00() {
    if (scicatService != null) {
      when(scicatService.getPublishedDataById(TestData.psiPub1.getDoi()))
          .thenReturn(RestResponse.ok(TestData.psiPub1));
      when(scicatService.getDatasetByPid(TestData.psiDs1.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs1));
      when(scicatService.getDatasetByPid(TestData.psiDs2.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs2));
      when(scicatService.getDatasetByPid(TestData.psiDs3.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs3));
    }
    given()
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header("Accept", ExtraMediaType.APPLICATION_ZIP)
        .body(List.of(TestData.psiPub1.getDoi()))
        .when()
        .post("/ro-crate/export")
        .then()
        .statusCode(200);
  }
}
