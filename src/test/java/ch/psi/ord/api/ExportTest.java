package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import ch.psi.s3_broker.model.DatasetUrls;
import ch.psi.scicat.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ExportTest extends EndpointTest {
  @BeforeEach
  public void setupMock() {
    if (scicatClient != null) {
      when(scicatClient.getPublishedDataById(TestData.psiPub1.getDoi()))
          .thenReturn(RestResponse.ok(TestData.psiPub1));
      when(scicatClient.getDatasetByPid(TestData.psiDs1.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs1));
      when(scicatClient.getDatasetByPid(TestData.psiDs2.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs2));
      when(scicatClient.getDatasetByPid(TestData.psiDs3.getPid()))
          .thenReturn(RestResponse.ok(TestData.psiDs3));
    }

    if (s3BrokerService != null) {
      when(s3BrokerService.getPublishedDataUrls(TestData.psiPub1.getDoi()))
          .thenReturn(TestData.psiPub1S3Response);
    }
  }

  @Test
  @DisplayName("Export to zip")
  public void test00() {
    given()
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header("Accept", ExtraMediaType.APPLICATION_ZIP)
        .body(List.of(TestData.psiPub1.getDoi()))
        .when()
        .post("/ro-crate/export")
        .then()
        .statusCode(200);
  }

  @Test
  @DisplayName("Should include S3 links if not expired")
  public void test01() {
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

    given()
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header("Accept", ExtraMediaType.APPLICATION_JSONLD)
        .body(List.of(TestData.psiPub1.getDoi()))
        .when()
        .post("/ro-crate/export")
        .then()
        .statusCode(200)
        .body(
            "'@graph'.findAll { it.'@type' == 'MediaObject' }", containsInAnyOrder(expectedItems));
  }

  @Test
  @DisplayName("Should not include S3 links if expired")
  public void test02() {
    String s3Url =
        TestData.hzdrPub1S3Response
            .getUrls()
            .get("PID.SAMPLE.PREFIX/hzdr_ds1")
            .getUrls()
            .getFirst()
            .getUrl();

    given()
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header("Accept", ExtraMediaType.APPLICATION_JSONLD)
        .body(List.of(TestData.psiPub1.getDoi()))
        .when()
        .post("/ro-crate/export")
        .then()
        .statusCode(200)
        .body("@graph.@id", not(hasItems(s3Url)));
  }
}
