package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
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
import io.restassured.response.ValidatableResponse;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Data;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

@QuarkusTest
public class ImportTest extends EndpointTest {
  @Data
  static class ImportTestCase {
    String testName;
    String contentType;
    boolean includeApiKey;
    byte[] body;
    int expectedStatusCode;
    Consumer<ImportTest> mockSetup;
    Consumer<ValidatableResponse> assertions;

    @Override
    public String toString() {
      return String.format("[%s] - %s", testName, contentType);
    }
  }

  private static final Consumer<ImportTest> NO_MOCK_SETUP = test -> {};

  private static final Consumer<ValidatableResponse> NO_ASSERTIONS = res -> {};

  private static final String PUBLICATION_DOI = "10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5";

  private static final String PUBLICATION_URL = DoiUtils.buildStandardUrl(PUBLICATION_DOI);

  private static final String DATASET_PID = "dataset-pid";

  private static Consumer<ImportTest> checkTokenValidity(boolean isValid) {
    return test -> {
      if (test.scicatClient != null) {
        when(test.scicatClient.checkTokenValidity(any())).thenReturn(isValid);
      }
    };
  }

  private static final Consumer<ImportTest> MOCK_NEW_PUBLICATION =
      checkTokenValidity(true)
          .andThen(
              test -> {
                if (test.scicatClient != null) {
                  when(test.scicatClient.countPublishedData(
                          String.format(
                              RoCrateImporter.publicationExistsFilter,
                              DoiUtils.buildStandardUrl(PUBLICATION_DOI)),
                          null))
                      .thenReturn(RestResponse.ok(new CountResponse().setCount(0)));
                  when(test.scicatClient.myidentity(any()))
                      .thenReturn(RestResponse.ok(TestData.rocrateUser));
                  when(test.scicatClient.createDataset(any(), any(CreateDatasetDto.class)))
                      .thenReturn(RestResponse.ok(new Dataset().setPid("some-pid")));
                  when(test.scicatClient.createPublishedData(
                          any(), any(CreatePublishedDataDto.class)))
                      .thenReturn(RestResponse.ok(new PublishedData().setDoi("some-pid")));
                  when(test.scicatClient.registerPublishedData(any(), any()))
                      .thenReturn(RestResponse.ok());
                }
              });

  private static final Consumer<ImportTest> MOCK_NEW_PUBLICATION_WITH_FILES =
      MOCK_NEW_PUBLICATION.andThen(
          test -> {
            if (test.scicatCli != null) {
              when(test.scicatCli.ingestDataset(any(), any(), any())).thenReturn(DATASET_PID);
            }
          });

  private static final Consumer<ImportTest> MOCK_EXISTING_PUBLICATION =
      checkTokenValidity(true)
          .andThen(
              test -> {
                if (test.scicatClient != null) {
                  when(test.scicatClient.countPublishedData(
                          String.format(
                              RoCrateImporter.publicationExistsFilter,
                              DoiUtils.buildStandardUrl(PUBLICATION_DOI)),
                          null))
                      .thenReturn(RestResponse.ok(new CountResponse().setCount(1)));
                }
              });

  private static ImportTestCase createTestCase(
      String testName,
      String contentType,
      boolean includeApiKey,
      byte[] body,
      int expectedStatusCode,
      Consumer<ImportTest> mockSetup,
      Consumer<ValidatableResponse> assertions) {
    return new ImportTestCase()
        .setTestName(testName)
        .setContentType(contentType)
        .setIncludeApiKey(includeApiKey)
        .setBody(body)
        .setExpectedStatusCode(expectedStatusCode)
        .setMockSetup(mockSetup)
        .setAssertions(assertions);
  }

  static List<ImportTestCase> testMatrix =
      new ArrayList<>() {
        {
          add(
              createTestCase(
                  "No Accept header", null, false, null, 415, NO_MOCK_SETUP, NO_ASSERTIONS));

          add(
              createTestCase(
                  "Empty body",
                  ExtraMediaType.APPLICATION_JSONLD,
                  true,
                  null,
                  400,
                  checkTokenValidity(true),
                  NO_ASSERTIONS));

          add(
              createTestCase(
                  "Invalid JSON-LD",
                  ExtraMediaType.APPLICATION_JSONLD,
                  true,
                  "{".getBytes(),
                  400,
                  checkTokenValidity(true),
                  NO_ASSERTIONS));

          add(
              createTestCase(
                  "Empty JSON-LD",
                  ExtraMediaType.APPLICATION_JSONLD,
                  true,
                  "{}".getBytes(),
                  400,
                  checkTokenValidity(true),
                  NO_ASSERTIONS));

          add(
              createTestCase(
                  "Unauthenticated",
                  ExtraMediaType.APPLICATION_JSONLD,
                  false,
                  getResource("one-publication.json"),
                  401,
                  checkTokenValidity(false),
                  NO_ASSERTIONS));

          add(
              createTestCase(
                  "One publication",
                  ExtraMediaType.APPLICATION_JSONLD,
                  true,
                  getResource("one-publication.json"),
                  201,
                  MOCK_NEW_PUBLICATION,
                  res -> res.body("$", hasKey(PUBLICATION_URL))));

          add(
              createTestCase(
                  "Publication with attached files",
                  ExtraMediaType.APPLICATION_ZIP,
                  true,
                  zipResource(
                      "publication-with-files.json",
                      Map.of(
                          "data/file1.txt", BigInteger.valueOf(16),
                          "data/file2.txt", BigInteger.valueOf(32))),
                  201,
                  MOCK_NEW_PUBLICATION_WITH_FILES,
                  res ->
                      res.body(
                          "$",
                          allOf(
                              hasEntry("data/file1.txt", DATASET_PID),
                              hasEntry("data/file2.txt", DATASET_PID),
                              hasKey(PUBLICATION_URL)))));

          add(
              createTestCase(
                  "Import existing publication",
                  ExtraMediaType.APPLICATION_JSONLD,
                  true,
                  getResource("one-publication.json"),
                  409,
                  MOCK_EXISTING_PUBLICATION,
                  NO_ASSERTIONS));
        }
      };

  @ParameterizedTest
  @FieldSource("testMatrix")
  @DisplayName("Import Matrix Execution")
  public void testImportMatrix(ImportTestCase testCase) {
    testCase.getMockSetup().accept(this);
    ValidatableResponse response = executeImportRequest(testCase);
    testCase.getAssertions().accept(response);
  }

  private ValidatableResponse executeImportRequest(ImportTestCase testCase) {
    var request = given();

    if (testCase.getContentType() != null) {
      request = request.header("Content-Type", testCase.getContentType());
    }

    if (testCase.isIncludeApiKey()) {
      request = request.header("api-key", accessToken);
    }

    if (testCase.getBody() != null) {
      request = request.body(testCase.getBody());
    }

    return request
        .when()
        .post("/api/v1/ro-crate/import")
        .then()
        .statusCode(testCase.getExpectedStatusCode());
  }
}
