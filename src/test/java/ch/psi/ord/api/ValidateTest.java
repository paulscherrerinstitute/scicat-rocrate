package ch.psi.ord.api;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ValidateTest extends EndpointTest {
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
