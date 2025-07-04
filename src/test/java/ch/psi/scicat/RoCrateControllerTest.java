package ch.psi.scicat;

import static io.restassured.RestAssured.given;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RoCrateControllerTest {
    @Nested
    class ImportEndpoint {
        @Test
        @DisplayName("No Accept header")
        public void test00() {
            QuarkusMock.installMockForType(ScicatServiceMock.generate(true), ScicatService.class, RestClient.LITERAL);
            given()
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(415);
        }

        @Test
        @DisplayName("Empty body")
        public void test01() {
            QuarkusMock.installMockForType(ScicatServiceMock.generate(true),
                    ScicatService.class, RestClient.LITERAL);
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Invalid JSON-LD")
        public void test02() {
            QuarkusMock.installMockForType(ScicatServiceMock.generate(true),
                    ScicatService.class, RestClient.LITERAL);
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .body("{")
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Empty JSON-LD")
        public void test03() {
            QuarkusMock.installMockForType(ScicatServiceMock.generate(true),
                    ScicatService.class, RestClient.LITERAL);
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .body("{}")
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(200);
        }
    }
}
