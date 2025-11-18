package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ValidateTest extends EndpointTest {
  @Test
  @DisplayName("One publication (ZIP)")
  public void test00() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
        .body(zipResource("one-publication.json"))
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(true))
        .body("entities", contains("https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
        .body("errors", emptyIterable());
  }

  @Test
  @DisplayName("One publication (JSON-LD)")
  public void test01() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .body(
            getClass().getClassLoader().getResourceAsStream("one-publication.json").readAllBytes())
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(true))
        .body("entities", contains("https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
        .body("errors", emptyIterable());
  }

  @Test
  @DisplayName("Multiple publications (JSON-LD)")
  public void test02() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .body(
            getClass()
                .getClassLoader()
                .getResourceAsStream("multiple-publications.json")
                .readAllBytes())
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(true))
        .body(
            "entities",
            contains(
                "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
                "https://doi.org/10.16907/4b55cbae-ac98-445a-a15e-1534b2a8b01f"))
        .body("errors", emptyIterable());
  }

  @Test
  @DisplayName("Multiple publications (ZIP)")
  public void test03() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
        .body(zipResource("multiple-publications.json"))
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(true))
        .body(
            "entities",
            contains(
                "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
                "https://doi.org/10.16907/4b55cbae-ac98-445a-a15e-1534b2a8b01f"))
        .body("errors", emptyIterable());
  }

  @Test
  @DisplayName("Publication missing schema:name (JSON-LD)")
  public void test04() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .body(
            getClass()
                .getClassLoader()
                .getResourceAsStream("invalid-publication.json")
                .readAllBytes())
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(false))
        .body("entities", emptyIterable())
        .body("errors", hasSize(1))
        .body(
            "errors[0]",
            hasEntry("nodeId", "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
        .body("errors[0]", hasEntry("property", "https://schema.org/name"))
        .body(
            "errors[0]", hasEntry("message", "Expected between 1 and 2147483647 values but got 0"))
        .body("errors[0]", hasEntry("type", "PropertyError"));
  }

  @Test
  @DisplayName("Publication missing schema:name (ZIP)")
  public void test05() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
        .body(zipResource("invalid-publication.json"))
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(false))
        .body("entities", emptyIterable())
        .body("errors", hasSize(1))
        .body(
            "errors[0]",
            hasEntry("nodeId", "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
        .body("errors[0]", hasEntry("property", "https://schema.org/name"))
        .body(
            "errors[0]", hasEntry("message", "Expected between 1 and 2147483647 values but got 0"))
        .body("errors[0]", hasEntry("type", "PropertyError"));
  }

  @Test
  @DisplayName("Mix of valid/invalid publications (JSON-LD)")
  public void test06() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .body(getClass().getClassLoader().getResourceAsStream("valid-invalid.json").readAllBytes())
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(false))
        .body("entities", contains("https://doi.org/10.16907/4b55cbae-ac98-445a-a15e-1534b2a8b01f"))
        .body("errors", hasSize(1))
        .body(
            "errors[0]",
            hasEntry("nodeId", "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
        .body("errors[0]", hasEntry("property", "https://schema.org/name"))
        .body(
            "errors[0]", hasEntry("message", "Expected between 1 and 2147483647 values but got 0"))
        .body("errors[0]", hasEntry("type", "PropertyError"));
  }

  @Test
  @DisplayName("Mix of valid/invalid publications (ZIP)")
  public void test07() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
        .body(zipResource("valid-invalid.json"))
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(false))
        .body("entities", contains("https://doi.org/10.16907/4b55cbae-ac98-445a-a15e-1534b2a8b01f"))
        .body("errors", hasSize(1))
        .body(
            "errors[0]",
            hasEntry("nodeId", "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
        .body("errors[0]", hasEntry("property", "https://schema.org/name"))
        .body(
            "errors[0]", hasEntry("message", "Expected between 1 and 2147483647 values but got 0"))
        .body("errors[0]", hasEntry("type", "PropertyError"));
  }

  @Test
  @DisplayName("Empty graph (JSON-LD)")
  public void test08() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .body("{}")
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(false))
        .body("entities", emptyIterable())
        .body("errors", hasSize(1))
        .body("errors[0]", hasEntry("message", "No suitable entity found in the graph"))
        .body("errors[0]", hasEntry("type", "NoEntityFound"));
  }

  @Test
  @DisplayName("Empty graph (ZIP)")
  public void test09() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
        .body(zipResource("empty.json"))
        .post("/ro-crate/validate")
        .then()
        .statusCode(200)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body("isValid", is(false))
        .body("entities", emptyIterable())
        .body("errors", hasSize(1))
        .body("errors[0]", hasEntry("message", "No suitable entity found in the graph"))
        .body("errors[0]", hasEntry("type", "NoEntityFound"));
  }

  @Test
  @DisplayName("Malformed metadata descriptor (JSON-LD)")
  public void test10() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
        .body(getClass().getClassLoader().getResourceAsStream("malformed.json").readAllBytes())
        .post("/ro-crate/validate")
        .then()
        .statusCode(400)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body(is("Failed to parse the metadata descriptor"));
  }

  @Test
  @DisplayName("Malformed metadata descriptor (ZIP)")
  public void test11() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
        .body(zipResource("malformed.json"))
        .post("/ro-crate/validate")
        .then()
        .statusCode(400)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body(is("Failed to parse the metadata descriptor"));
  }

  @Test
  @DisplayName("Malformed zip archive (ZIP)")
  public void test12() throws IOException {
    given()
        .when()
        .header("Content-Type", ExtraMediaType.APPLICATION_ZIP)
        .body(new byte[0])
        .post("/ro-crate/validate")
        .then()
        .statusCode(400)
        .contentType(is(CONTENT_TYPE_JSON_RES))
        .body(is("Invalid or empty zip archive"));
  }
}
