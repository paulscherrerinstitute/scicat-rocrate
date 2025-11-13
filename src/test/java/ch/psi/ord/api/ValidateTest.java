package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import org.hamcrest.Matchers;
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
        .body(
            "entities",
            Matchers.contains("https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
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
        .body(
            "entities",
            Matchers.contains("https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
        .body("errors", emptyIterable());
  }
}
