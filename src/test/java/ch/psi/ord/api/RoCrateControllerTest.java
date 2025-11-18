package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.psi.ord.core.DoiUtils;
import ch.psi.ord.core.RoCrateImporter;
import ch.psi.scicat.TestData;
import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.CountResponse;
import ch.psi.scicat.model.CreateDatasetDto;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RoCrateControllerTest {
  @InjectMock @RestClient ScicatService scicatService;
  protected String accessToken = "";

  @Nested
  class ImportEndpoint {
    @Test
    @DisplayName("No Accept header")
    public void test00() {
      given().when().post("/ro-crate/import").then().statusCode(415);
    }

    @Test
    @DisplayName("Empty body")
    public void test01() {
      if (scicatService != null) {
        when(scicatService.userInfos(any())).thenReturn(RestResponse.status(Status.OK));
      }

      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .header("scicat-token", accessToken)
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Invalid JSON-LD")
    public void test02() {
      if (scicatService != null) {
        when(scicatService.userInfos(any())).thenReturn(RestResponse.status(Status.OK));
      }

      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .header("scicat-token", accessToken)
          .body("{")
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Empty JSON-LD")
    public void test03() {
      if (scicatService != null) {
        when(scicatService.userInfos(any())).thenReturn(RestResponse.ok(TestData.rocrateUser));
      }

      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .header("scicat-token", accessToken)
          .body("{}")
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Unauthenticated")
    public void test04() throws IOException, Exception {
      if (scicatService != null) {
        when(scicatService.userInfos(any())).thenReturn(RestResponse.status(Status.UNAUTHORIZED));
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
      if (scicatService != null) {
        when(scicatService.userInfos(any())).thenReturn(RestResponse.ok(TestData.rocrateUser));
        when(scicatService.countPublishedData(
                String.format(
                    RoCrateImporter.publicationExistsFilter,
                    DoiUtils.buildStandardUrl("10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5")),
                null))
            .thenReturn(RestResponse.ok(new CountResponse().setCount(0)));
        when(scicatService.userInfos(null)).thenReturn(RestResponse.ok(TestData.rocrateUser));
        when(scicatService.createDataset(any(), any(CreateDatasetDto.class)))
            .thenReturn(RestResponse.ok(new Dataset().setPid("some-pid")));
        when(scicatService.createPublishedData(any(), any(CreatePublishedDataDto.class)))
            .thenReturn(RestResponse.ok(new PublishedData().setDoi("some-pid")));
        when(scicatService.registerPublishedData(any(), any())).thenReturn(RestResponse.ok());
      }

      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .header("scicat-token", accessToken)
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
      if (scicatService != null) {
        when(scicatService.userInfos(null)).thenReturn(RestResponse.status(Status.OK));
        when(scicatService.countPublishedData(
                String.format(
                    RoCrateImporter.publicationExistsFilter,
                    DoiUtils.buildStandardUrl("10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5")),
                null))
            .thenReturn(RestResponse.ok(new CountResponse().setCount(1)));
      }
      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .header("scicat-token", accessToken)
          .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(409);
    }
  }

  @Nested
  class ValidateEndpoint {
    @Test
    @DisplayName("One publication zipped")
    public void test00() throws IOException {
      given()
          .when()
          .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
          .body(zipResource("one-publication.json"))
          .post("/ro-crate/validate")
          .then()
          .statusCode(200)
          .body("isValid", Matchers.is(true))
          .body(
              "entities",
              Matchers.contains("https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
          .body("errors", Matchers.emptyIterable());
    }
  }

  @Nested
  class ExportEndpoint {
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

  public byte[] zipResource(String name) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (ZipOutputStream zipStream = new ZipOutputStream(output)) {
      ZipEntry entry = new ZipEntry("ro-crate-metadata.json");
      zipStream.putNextEntry(entry);
      byte[] content = getClass().getClassLoader().getResourceAsStream(name).readAllBytes();
      zipStream.write(content, 0, content.length);
      zipStream.closeEntry();
    }

    return output.toByteArray();
  }
}
