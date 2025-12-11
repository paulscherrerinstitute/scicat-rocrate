package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
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
      when(scicatClient.getPublishedDataById("10.9999%2Fpsi_pub1"))
          .thenReturn(RestResponse.ok(TestData.psiPub1));
    }

    String doiUrl = String.format(DoiUtils.buildStandardUrl(TestData.psiPub1.getDoi()));
    given()
        .when()
        .accept(ExtraMediaType.APPLICATION_JSONLD)
        .get("/zenodo/10.9999%2Fpsi_pub1/export")
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
            equalTo(TestData.psiPub1.getRegisteredTime().toString()));
  }
}
