package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.psi.ord.core.DoiUtils;
import ch.psi.scicat.TestData;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ZenodoControllerTest extends EndpointTest {
  @Test
  @DisplayName("Export to Zenodo JSON-LD")
  public void test00() {
    if (scicatClient != null) {
      when(scicatClient.getPublishedDataById(any())).thenReturn(RestResponse.ok(TestData.psiPub1));
    }

    String doiUrl = String.format(DoiUtils.buildStandardUrl(TestData.psiPub1.getDoi()));
    given()
        .when()
        .accept(ExtraMediaType.APPLICATION_JSONLD)
        .pathParam("doi", TestData.psiPub1.getDoi())
        .get("/zenodo/{doi}/export")
        .then()
        .statusCode(200)
        .body("@context", equalTo(SchemaDO.NS))
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
            equalTo("Paul Scherrer Institute"))
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
            everyItem(equalTo(SchemaDO.DataDownload.getLocalName())));
  }
}
