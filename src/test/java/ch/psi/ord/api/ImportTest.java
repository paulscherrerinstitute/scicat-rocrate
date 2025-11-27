package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.psi.ord.core.DoiUtils;
import ch.psi.ord.core.RoCrateImporter;
import ch.psi.scicat.TestData;
import ch.psi.scicat.model.v3.CountResponse;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.PublishedData;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ImportTest extends EndpointTest {
  @Test
  @DisplayName("No Accept header")
  public void test00() {
    given().when().post("/ro-crate/import").then().statusCode(415);
  }

  @Test
  @DisplayName("Empty body")
  public void test01() {
    if (scicatClient != null) {
      when(scicatClient.checkTokenValidity(any())).thenReturn(true);
    }

    given()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .header("api-key", accessToken)
        .when()
        .post("/ro-crate/import")
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Invalid JSON-LD")
  public void test02() {
    if (scicatClient != null) {
      when(scicatClient.checkTokenValidity(any())).thenReturn(true);
    }

    given()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .header("api-key", accessToken)
        .body("{")
        .when()
        .post("/ro-crate/import")
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Empty JSON-LD")
  public void test03() {
    if (scicatClient != null) {
      when(scicatClient.checkTokenValidity(any())).thenReturn(true);
    }

    given()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .header("api-key", accessToken)
        .body("{}")
        .when()
        .post("/ro-crate/import")
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Unauthenticated")
  public void test04() throws IOException, Exception {
    if (scicatClient != null) {
      when(scicatClient.checkTokenValidity(any())).thenReturn(false);
    }

    given()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
        .when()
        .post("/ro-crate/import")
        .then()
        .statusCode(401);
  }

  @Test
  @DisplayName("One publication")
  public void test05() {
    if (scicatClient != null) {
      when(scicatClient.checkTokenValidity(any())).thenReturn(true);
      when(scicatClient.countPublishedData(
              String.format(
                  RoCrateImporter.publicationExistsFilter,
                  DoiUtils.buildStandardUrl("10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5")),
              null))
          .thenReturn(RestResponse.ok(new CountResponse().setCount(0)));
      when(scicatClient.userDetails(any())).thenReturn(TestData.rocrateUser);
      when(scicatClient.createDataset(any(), any(CreateDatasetDto.class)))
          .thenReturn(RestResponse.ok(new Dataset().setPid("some-pid")));
      when(scicatClient.createPublishedData(any(), any(CreatePublishedDataDto.class)))
          .thenReturn(RestResponse.ok(new PublishedData().setDoi("some-pid")));
      when(scicatClient.registerPublishedData(any(), any())).thenReturn(RestResponse.ok());
    }

    given()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .header("api-key", accessToken)
        .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
        .when()
        .post("/ro-crate/import")
        .then()
        .statusCode(201)
        .body("$", Matchers.hasKey("10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"));
  }

  @Test
  @DisplayName("Import existing publication")
  public void test06() {
    if (scicatClient != null) {
      when(scicatClient.checkTokenValidity(any())).thenReturn(true);
      when(scicatClient.countPublishedData(
              String.format(
                  RoCrateImporter.publicationExistsFilter,
                  DoiUtils.buildStandardUrl("10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5")),
              null))
          .thenReturn(RestResponse.ok(new CountResponse().setCount(1)));
    }
    given()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .header("api-key", accessToken)
        .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
        .when()
        .post("/ro-crate/import")
        .then()
        .statusCode(409);
  }
}
