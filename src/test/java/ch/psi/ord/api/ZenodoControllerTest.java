package ch.psi.ord.api;

import static ch.psi.ord.matchers.DateIsExpired.isDateExpired;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.psi.ord.core.DoiUtils;
import ch.psi.scicat.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.WebApplicationException;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ZenodoControllerTest extends EndpointTest {
  @Test
  @DisplayName("Export should succeed if the datasets are retrieved")
  public void test00() {
    if (scicatClient != null) {
      when(scicatClient.getPublishedDataById(any())).thenReturn(RestResponse.ok(TestData.psiPub1));
    }
    if (s3BrokerService != null) {
      when(s3BrokerService.getPublishedDataUrls(any())).thenReturn(TestData.psiPub1S3Response);
    }

    String doiUrl = String.format(DoiUtils.buildStandardUrl(TestData.psiPub1.getDoi()));
    given()
        .when()
        .accept(ExtraMediaType.APPLICATION_JSONLD)
        .pathParam("doi", TestData.psiPub1.getDoi())
        .get("/zenodo/{doi}/export")
        .then()
        .statusCode(200)
        .body("@type", equalTo(SchemaDO.Dataset.getLocalName()))
        .body("@id", equalTo(doiUrl))
        .body(SchemaDO.identifier.getLocalName(), equalTo(doiUrl))
        .body(SchemaDO.name.getLocalName(), equalTo(TestData.psiPub1.getTitle()))
        .body(SchemaDO.description.getLocalName(), equalTo(TestData.psiPub1.getAbstract()))
        .body(SchemaDO.dateCreated.getLocalName(), equalTo(TestData.psiPub1.getCreatedAt()))
        .body(
            SchemaDO.datePublished.getLocalName(),
            equalTo(TestData.psiPub1.getRegisteredTime().toString()))
        .body(
            SchemaDO.publisher.getLocalName() + ".@type",
            equalTo(SchemaDO.Organization.getLocalName()))
        .body(
            SchemaDO.publisher.getLocalName() + "." + SchemaDO.name.getLocalName(),
            equalTo(TestData.psiPub1.getPublisher()))
        .body(SchemaDO.creator.getLocalName(), hasSize(TestData.psiPub1.getCreator().size()))
        .body(
            SchemaDO.creator.getLocalName() + ".@type",
            everyItem(equalTo(SchemaDO.Person.getLocalName())))
        .body(
            SchemaDO.creator.getLocalName() + "." + SchemaDO.name.getLocalName(),
            containsInAnyOrder(TestData.psiPub1.getCreator().toArray()))
        .body(SchemaDO.distribution.getLocalName(), hasSize(TestData.psiPub1.getPidArray().size()))
        .body(
            SchemaDO.distribution.getLocalName() + ".@type",
            everyItem(equalTo(SchemaDO.DataDownload.getLocalName())))
        .body(
            SchemaDO.distribution.getLocalName() + "." + SchemaDO.expires.getLocalName(),
            everyItem(not(isDateExpired())));
  }

  @Test
  @DisplayName("Export should succeed if the datasets links are expired")
  public void test01() {
    if (scicatClient != null) {
      when(scicatClient.getPublishedDataById(TestData.hzdrPub1.getDoi()))
          .thenReturn(RestResponse.ok(TestData.hzdrPub1));
    }
    if (s3BrokerService != null) {
      when(s3BrokerService.getPublishedDataUrls(TestData.hzdrPub1.getDoi()))
          .thenReturn(TestData.hzdrPub1S3Response);
    }
    String doiUrl = String.format(DoiUtils.buildStandardUrl(TestData.hzdrPub1.getDoi()));
    given()
        .when()
        .accept(ExtraMediaType.APPLICATION_JSONLD)
        .pathParam("doi", TestData.hzdrPub1.getDoi())
        .get("/zenodo/{doi}/export")
        .then()
        .statusCode(200)
        .body("@type", equalTo(SchemaDO.Dataset.getLocalName()))
        .body("@id", equalTo(doiUrl))
        .body(SchemaDO.identifier.getLocalName(), equalTo(doiUrl))
        .body(SchemaDO.name.getLocalName(), equalTo(TestData.hzdrPub1.getTitle()))
        .body(SchemaDO.description.getLocalName(), equalTo(TestData.hzdrPub1.getAbstract()))
        .body(SchemaDO.dateCreated.getLocalName(), equalTo(TestData.hzdrPub1.getCreatedAt()))
        .body(
            SchemaDO.datePublished.getLocalName(),
            equalTo(TestData.hzdrPub1.getRegisteredTime().toString()))
        .body(
            SchemaDO.publisher.getLocalName() + ".@type",
            equalTo(SchemaDO.Organization.getLocalName()))
        .body(
            SchemaDO.publisher.getLocalName() + "." + SchemaDO.name.getLocalName(),
            equalTo(TestData.hzdrPub1.getPublisher()))
        .body(SchemaDO.creator.getLocalName(), hasSize(TestData.hzdrPub1.getCreator().size()))
        .body(
            SchemaDO.creator.getLocalName() + ".@type",
            everyItem(equalTo(SchemaDO.Person.getLocalName())))
        .body(
            SchemaDO.creator.getLocalName() + "." + SchemaDO.name.getLocalName(),
            containsInAnyOrder(TestData.hzdrPub1.getCreator().toArray()))
        .body(
            SchemaDO.distribution.getLocalName() + ".@type",
            equalTo(SchemaDO.DataDownload.getLocalName()))
        .body(
            SchemaDO.distribution.getLocalName() + "." + SchemaDO.expires.getLocalName(),
            isDateExpired());
  }

  @Test
  @DisplayName("Export should fail if the doi doesn't exist")
  public void test02() {
    if (s3BrokerService != null) {
      when(s3BrokerService.getPublishedDataUrls(any())).thenThrow(new WebApplicationException(404));
    }

    given()
        .when()
        .accept(ExtraMediaType.APPLICATION_JSONLD)
        .pathParam("doi", "10.9999/non-existing")
        .get("/zenodo/{doi}/export")
        .then()
        .statusCode(404);
  }
}
