package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import ch.psi.ord.core.DoiUtils;
import ch.psi.ord.core.RoCrate;
import ch.psi.s3_broker.model.DatasetUrls;
import ch.psi.scicat.TestData;
import ch.psi.scicat.model.v3.PublishedData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

@QuarkusTest
public class ExportTest extends EndpointTest {
  @Data
  static class ExportTestCase {
    String testName;
    String exportFormat;
    List<String> identifiers;
    Consumer<ExportTest> mockSetup;
    Consumer<ValidatableResponse> assertions;

    @Override
    public String toString() {
      return String.format("[%s] - %s", testName, exportFormat);
    }
  }

  private static final Consumer<ExportTest> DEFAULT_MOCK_SETUP =
      test -> {
        if (test.scicatClient != null) {
          when(test.scicatClient.getPublishedDataById(TestData.psiPub1.getDoi()))
              .thenReturn(RestResponse.ok(TestData.psiPub1));
          when(test.scicatClient.getPublishedDataById(TestData.hzdrPub1.getDoi()))
              .thenReturn(RestResponse.ok(TestData.hzdrPub1));

          when(test.scicatClient.getDatasetByPid(TestData.psiDs1.getPid()))
              .thenReturn(RestResponse.ok(TestData.psiDs1));
          when(test.scicatClient.getDatasetByPid(TestData.psiDs2.getPid()))
              .thenReturn(RestResponse.ok(TestData.psiDs2));
          when(test.scicatClient.getDatasetByPid(TestData.psiDs3.getPid()))
              .thenReturn(RestResponse.ok(TestData.psiDs3));
          when(test.scicatClient.getDatasetByPid(TestData.hzdrDs1.getPid()))
              .thenReturn(RestResponse.ok(TestData.hzdrDs1));
        }

        if (test.s3BrokerService != null) {
          when(test.s3BrokerService.getPublishedDataUrls(TestData.psiPub1.getDoi()))
              .thenReturn(TestData.psiPub1S3Response);
          when(test.s3BrokerService.getPublishedDataUrls(TestData.hzdrPub1.getDoi()))
              .thenReturn(TestData.hzdrPub1S3Response);
        }
      };

  private static List<ExportTestCase> createTestCase(
      String testName, List<String> identifiers, Consumer<ValidatableResponse> assertions) {
    return createTestCase(testName, identifiers, DEFAULT_MOCK_SETUP, assertions);
  }

  private static List<ExportTestCase> createTestCase(
      String testName,
      List<String> identifiers,
      Consumer<ExportTest> mockSetup,
      Consumer<ValidatableResponse> assertions) {
    return List.of(
        new ExportTestCase()
            .setTestName(testName)
            .setExportFormat(ExtraMediaType.APPLICATION_JSONLD)
            .setIdentifiers(identifiers)
            .setMockSetup(mockSetup)
            .setAssertions(assertions),
        new ExportTestCase()
            .setTestName(testName)
            .setExportFormat(ExtraMediaType.APPLICATION_ZIP)
            .setIdentifiers(identifiers)
            .setMockSetup(mockSetup)
            .setAssertions(assertions));
  }

  static List<ExportTestCase> testMatrix =
      new ArrayList<>() {
        {
          addAll(
              createTestCase(
                  "Export one publication",
                  List.of(TestData.psiPub1.getDoi()),
                  res -> {
                    PublishedData pub = TestData.psiPub1;
                    String rootPath =
                        String.format(
                            "'@graph'.find { it.'@type' == 'Collection' && it.'@id' == '%s' }",
                            DoiUtils.buildStandardUrl(pub.getDoi()));
                    res.body(rootPath, notNullValue())
                        .body(rootPath + ".identifier", is(pub.getDoi()))
                        .body(rootPath + ".name", is(pub.getTitle()))
                        .body(rootPath + ".abstract", is(pub.getAbstract()))
                        .body(rootPath + ".description", is(pub.getDataDescription()))
                        .body(rootPath + ".additionalType", is(pub.getResourceType()))
                        .body(rootPath + ".creativeWorkStatus", is(pub.getStatus().toString()))
                        .body(rootPath + ".dateCreated", is(pub.getCreatedAt()))
                        .body(rootPath + ".dateModified", is(pub.getUpdatedAt()))
                        .body(
                            rootPath + ".datePublished",
                            is(String.valueOf(pub.getPublicationYear())))
                        .body(rootPath + ".sdDatePublished", is(pub.getRegisteredTime().toString()))
                        .body(
                            rootPath + ".expires",
                            is(TestData.psiPub1S3Response.getExpires().toString()))
                        .body(rootPath + ".creator", hasSize(pub.getCreator().size()))
                        .body(rootPath + ".hasPart", hasSize(pub.getPidArray().size()))
                        .body(rootPath + ".publisher", notNullValue())
                        .body(rootPath + ".license", notNullValue());
                  }));

          addAll(
              createTestCase(
                  "Should include S3 links if not expired",
                  List.of(TestData.psiPub1.getDoi()),
                  res -> {
                    var expectedItems =
                        Stream.of(TestData.psiDs1, TestData.psiDs2, TestData.psiDs3)
                            .map(
                                ds -> {
                                  DatasetUrls dsUrls =
                                      TestData.psiPub1S3Response.getUrls().get(ds.getPid());
                                  return Map.of(
                                      "@id",
                                      dsUrls.getUrls().getFirst().getUrl(),
                                      "@type",
                                      SchemaDO.MediaObject.getLocalName(),
                                      "expires",
                                      dsUrls.getUrls().getFirst().getExpires().toString(),
                                      "encodingFormat",
                                      ExtraMediaType.APPLICATION_TAR);
                                })
                            .toArray();

                    res.body(
                        "'@graph'.findAll { it.'@type' == 'MediaObject' }",
                        containsInAnyOrder(expectedItems));
                  }));

          addAll(
              createTestCase(
                  "Should not include S3 links if expired",
                  List.of(TestData.hzdrPub1.getDoi()),
                  res -> {
                    String s3Url =
                        TestData.hzdrPub1S3Response
                            .getUrls()
                            .get(TestData.hzdrDs1.getPid())
                            .getUrls()
                            .getFirst()
                            .getUrl();

                    res.body("@graph.@id", not(hasItems(s3Url)));
                  }));
        }
      };

  @ParameterizedTest
  @FieldSource("testMatrix")
  @DisplayName("Export Matrix Execution")
  public void testExportMatrix(ExportTestCase testCase) {
    testCase.getMockSetup().accept(this);
    ValidatableResponse response =
        executeExportRequest(testCase.getIdentifiers(), testCase.getExportFormat());
    testCase.getAssertions().accept(response);
  }

  private ValidatableResponse executeExportRequest(List<String> identifiers, String exportFormat) {
    Response response =
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .header("export", exportFormat)
            .body(identifiers)
            .when()
            .post("/api/v1/ro-crate/export")
            .then()
            .statusCode(200)
            .extract()
            .response();

    if (ExtraMediaType.APPLICATION_JSONLD.equals(exportFormat)) {
      return response.then();
    }

    return extractJsonFromZip(response)
        .map(
            json ->
                new ResponseBuilder()
                    .clone(response)
                    .setContentType(ContentType.JSON)
                    .setBody(json)
                    .build()
                    .then())
        .orElseGet(() -> fail("Target file 'ro-crate-metadata.json' not found in ZIP archive."));
  }

  private Optional<String> extractJsonFromZip(Response response) {
    try (ZipInputStream zis = new ZipInputStream(response.asInputStream())) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (RoCrate.METADATA_DESCRIPTOR.equals(entry.getName())) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          byte[] buffer = new byte[1024];
          int len;
          while ((len = zis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          return Optional.of(out.toString("UTF-8"));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error processing ZIP input stream", e);
    }

    return Optional.empty();
  }
}
