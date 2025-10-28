package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import ch.psi.scicat.TestData;
import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.client.ScicatServiceMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RoCrateControllerTest {
  ScicatServiceMock scicatServiceMock;

  @BeforeEach
  public void setUp() {
    scicatServiceMock = new ScicatServiceMock();
    QuarkusMock.installMockForType(scicatServiceMock, ScicatService.class, RestClient.LITERAL);
  }

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
      scicatServiceMock.setAuthenticated(true);
      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Invalid JSON-LD")
    public void test02() {
      scicatServiceMock.setAuthenticated(true);
      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .body("{")
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Empty JSON-LD")
    public void test03() {
      scicatServiceMock.setAuthenticated(true);
      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .body("{}")
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Unauthenticated")
    public void test04() throws IOException, Exception {
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
      scicatServiceMock.setAuthenticated(true);
      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(201)
          .body("$", hasKey("10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"));
    }

    @Test
    @DisplayName("Import existing publication")
    public void test06() {
      scicatServiceMock.setAuthenticated(true).setPublicationCount(1);

      given()
          .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
          .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
          .when()
          .post("/ro-crate/import")
          .then()
          .statusCode(409);

      scicatServiceMock.setPublicationCount(0);
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
          .body("isValid", is(true))
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
      scicatServiceMock.setAuthenticated(true);
      scicatServiceMock.createPublishedData(null, TestData.exampleCreatePublishedDataDto);

      given()
          .header("Content-Type", MediaType.APPLICATION_JSON)
          .header("Accept", ExtraMediaType.APPLICATION_ZIP)
          .body(List.of(TestData.exampleCreatePublishedDataDto.getDoi()))
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
