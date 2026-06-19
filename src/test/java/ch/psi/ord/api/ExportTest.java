package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import ch.psi.ord.core.DoiUtils;
import ch.psi.ord.core.RoCrate;
import ch.psi.s3_broker.model.DatasetUrls;
import ch.psi.scicat.TestData;
import ch.psi.scicat.model.v3.PublishedData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
public class ExportTest extends EndpointTest {
  @BeforeEach
  public void setupMock() {
    if (scicatClient != null) {
      when(scicatClient.getPublishedDataById(TestData.psiPub1.getDoi()))
          .thenReturn(RestResponse.ok(TestData.psiPub1));
      when(scicatClient.getPublishedDataById(TestData.hzdrPub1.getDoi()))
          .thenReturn(RestResponse.ok(TestData.hzdrPub1));

      when(scicatClient.getDatasetByPid(TestData.psiDs1.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs1));
      when(scicatClient.getDatasetByPid(TestData.psiDs2.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs2));
      when(scicatClient.getDatasetByPid(TestData.psiDs3.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs3));
      when(scicatClient.getDatasetByPid(TestData.hzdrDs1.getPid()))
          .thenReturn(RestResponse.ok(TestData.hzdrDs1));
    }

    if (s3BrokerService != null) {
      when(s3BrokerService.getPublishedDataUrls(TestData.psiPub1.getDoi()))
          .thenReturn(TestData.psiPub1S3Response);
      when(s3BrokerService.getPublishedDataUrls(TestData.hzdrPub1.getDoi()))
          .thenReturn(TestData.hzdrPub1S3Response);
    }
  }

  @ParameterizedTest(name = "Export one publication: {0}")
  @ValueSource(strings = {ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP})
  public void test00(String exportFormat) throws IOException {
    PublishedData pub = TestData.psiPub1;
    JsonPath jsonPath = executeExportRequest(List.of(pub.getDoi()), exportFormat);
    final JsonPath pubPath =
        jsonPath.setRootPath(
            String.format(
                "'@graph'.find { it.'@type' == 'Collection' && it.'@id' == '%s' }",
                DoiUtils.buildStandardUrl(pub.getDoi())));
    assertAll(
        () -> assertNotNull(pubPath.get()),
        () -> assertEquals(pubPath.get("identifier"), pub.getDoi()),
        () -> assertEquals(pubPath.get("name"), pub.getTitle()),
        () -> assertEquals(pubPath.get("abstract"), pub.getAbstract()),
        () -> assertEquals(pubPath.get("description"), pub.getDataDescription()),
        () -> assertEquals(pubPath.get("additionalType"), pub.getResourceType()),
        () -> assertEquals(pubPath.get("creativeWorkStatus"), pub.getStatus().toString()),
        () -> assertEquals(pubPath.get("dateCreated"), pub.getCreatedAt()),
        () -> assertEquals(pubPath.get("dateModified"), pub.getUpdatedAt()),
        () -> assertEquals(pubPath.get("datePublished"), String.valueOf(pub.getPublicationYear())),
        () -> assertEquals(pubPath.get("sdDatePublished"), pub.getRegisteredTime().toString()),
        () ->
            assertEquals(
                pubPath.get("expires"), TestData.psiPub1S3Response.getExpires().toString()),
        () -> assertThat(pubPath.get("creator"), hasSize(pub.getCreator().size())),
        () -> assertThat(pubPath.get("hasPart"), hasSize(pub.getPidArray().size())),
        () -> assertNotNull(pubPath.get("publisher")),
        () -> assertNotNull(pubPath.get("license")));
  }

  @ParameterizedTest(name = "Should include S3 links if not expired: {0}")
  @ValueSource(strings = {ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP})
  public void test01(String exportFormat) {
    var expectedItems =
        Stream.of(TestData.psiDs1, TestData.psiDs2, TestData.psiDs3)
            .map(
                ds -> {
                  DatasetUrls dsUrls = TestData.psiPub1S3Response.getUrls().get(ds.getPid());
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

    JsonPath jsonPath = executeExportRequest(List.of(TestData.psiPub1.getDoi()), exportFormat);
    assertThat(
        jsonPath.get("'@graph'.findAll { it.'@type' == 'MediaObject' }"),
        containsInAnyOrder(expectedItems));
  }

  @ParameterizedTest(name = "Should not include S3 links if expired: {0}")
  @ValueSource(strings = {ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP})
  public void test02(String exportFormat) {
    String s3Url =
        TestData.hzdrPub1S3Response
            .getUrls()
            .get(TestData.hzdrDs1.getPid())
            .getUrls()
            .getFirst()
            .getUrl();

    JsonPath jsonPath = executeExportRequest(List.of(TestData.hzdrPub1.getDoi()), exportFormat);
    assertThat(jsonPath.get("@graph.@id"), not(hasItems(s3Url)));
  }

  private JsonPath executeExportRequest(List<String> identifiers, String exportFormat) {
    Response response =
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .header("export", exportFormat)
            .body(identifiers)
            .when()
            .post("/ro-crate/export")
            .then()
            .statusCode(200)
            .extract()
            .response();

    if (ExtraMediaType.APPLICATION_JSONLD.equals(exportFormat)) {
      return JsonPath.from(response.asString());
    }

    return extractJsonFromZip(response)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Target file 'ro-crate-metadata.json' not found in ZIP archive."));
  }

  private Optional<JsonPath> extractJsonFromZip(Response response) {
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
          return Optional.of(JsonPath.from(out.toString("UTF-8")));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error processing ZIP input stream", e);
    }

    return Optional.empty();
  }
}
