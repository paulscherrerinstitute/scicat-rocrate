package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import ch.psi.ord.model.ExportFormat;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

@QuarkusTest
public class ValidateTest extends EndpointTest {
  @Data
  static class ValidateTestCase {
    String testName;
    ExportFormat exportFormat;
    byte[] body;
    int expectedStatusCode;
    Consumer<ValidatableResponse> assertions;

    @Override
    public String toString() {
      return String.format("[%s] - %s", testName, exportFormat.name());
    }
  }

  private static List<ValidateTestCase> createTestCase(
      String testName,
      String resourceName,
      int statusCode,
      Consumer<ValidatableResponse> assertions) {
    return List.of(
        new ValidateTestCase()
            .setTestName(testName)
            .setExportFormat(ExportFormat.JSONLD)
            .setBody(getResource(resourceName))
            .setExpectedStatusCode(statusCode)
            .setAssertions(assertions),
        new ValidateTestCase()
            .setTestName(testName)
            .setExportFormat(ExportFormat.ZIP)
            .setBody(zipResource(resourceName))
            .setExpectedStatusCode(statusCode)
            .setAssertions(assertions));
  }

  static List<ValidateTestCase> testMatrix =
      new ArrayList<>() {
        {
          addAll(
              createTestCase(
                  "One publication",
                  "one-publication.json",
                  200,
                  res ->
                      res.body("isValid", is(true))
                          .body(
                              "entities",
                              Matchers.contains(
                                  "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
                          .body("errors", emptyIterable())));

          addAll(
              createTestCase(
                  "Multiple publications",
                  "multiple-publications.json",
                  200,
                  res ->
                      res.body("isValid", is(true))
                          .body(
                              "entities",
                              Matchers.contains(
                                  "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
                                  "https://doi.org/10.16907/4b55cbae-ac98-445a-a15e-1534b2a8b01f"))
                          .body("errors", emptyIterable())));

          addAll(
              createTestCase(
                  "Publication missing schema:name",
                  "invalid-publication.json",
                  200,
                  res ->
                      res.body("isValid", is(false))
                          .body("entities", emptyIterable())
                          .body("errors", hasSize(1))
                          .body(
                              "errors[0]",
                              hasEntry(
                                  "nodeId",
                                  "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
                          .body("errors[0]", hasEntry("property", "https://schema.org/name"))
                          .body("errors[0]", hasEntry("message", "Missing required property"))
                          .body("errors[0]", hasEntry("type", "PropertyError"))));

          addAll(
              createTestCase(
                  "Mix of valid/invalid publications",
                  "valid-invalid.json",
                  200,
                  res ->
                      res.body("isValid", is(false))
                          .body(
                              "entities",
                              Matchers.contains(
                                  "https://doi.org/10.16907/4b55cbae-ac98-445a-a15e-1534b2a8b01f"))
                          .body("errors", hasSize(1))
                          .body(
                              "errors[0]",
                              hasEntry(
                                  "nodeId",
                                  "https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"))
                          .body("errors[0]", hasEntry("property", "https://schema.org/name"))
                          .body("errors[0]", hasEntry("message", "Missing required property"))
                          .body("errors[0]", hasEntry("type", "PropertyError"))));

          addAll(
              createTestCase(
                  "Empty graph",
                  "empty.json",
                  200,
                  res ->
                      res.body("isValid", is(false))
                          .body("entities", emptyIterable())
                          .body("errors", hasSize(1))
                          .body(
                              "errors[0]",
                              hasEntry("message", "No suitable entity found in the graph"))
                          .body("errors[0]", hasEntry("type", "NoEntityFound"))));

          addAll(
              createTestCase(
                  "Malformed metadata descriptor",
                  "malformed.json",
                  400,
                  res -> res.body("message", is("Failed to parse the metadata descriptor"))));

          add(
              new ValidateTestCase()
                  .setTestName("Malformed zip archive")
                  .setExportFormat(ExportFormat.ZIP)
                  .setBody(new byte[0])
                  .setExpectedStatusCode(400)
                  .setAssertions(res -> res.body("message", is("Invalid or empty zip archive"))));
        }
      };

  @ParameterizedTest
  @FieldSource("testMatrix")
  @DisplayName("Validation Matrix Execution")
  public void testValidationMatrix(ValidateTestCase testCase) {
    ValidatableResponse response =
        given()
            .when()
            .contentType(testCase.getExportFormat().toString())
            .body(testCase.getBody())
            .post("/api/v1/ro-crate/validate")
            .then()
            .statusCode(testCase.getExpectedStatusCode())
            .contentType(is(CONTENT_TYPE_JSON_RES));

    testCase.getAssertions().accept(response);
  }
}
