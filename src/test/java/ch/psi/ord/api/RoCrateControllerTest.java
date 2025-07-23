package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasEntry;

import java.io.IOException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.client.ScicatServiceMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RoCrateControllerTest {
    ScicatServiceMock scicatServiceMock;

    @BeforeEach
    public void setUp() {
        scicatServiceMock = new ScicatServiceMock();
        QuarkusMock.installMockForType(scicatServiceMock, ScicatService.class, RestClient.LITERAL);
    }

    @Nested
    class ImportEndpoint {
        @Test
        @DisplayName("No Accept header")
        public void test00() {
            given()
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(415);
        }

        @Test
        @DisplayName("Empty body")
        public void test01() {
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
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .body("{}")
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Unauthenticated")
        public void test04() throws IOException, Exception {
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("One publication")
        public void test05() {
            scicatServiceMock.setAuthenticated(true);
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(201)
                    .body("$",
                            hasEntry("https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
                                    "10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"));

        }

        @Test
        @DisplayName("Import existing publication")
        public void test06() {
            scicatServiceMock.setAuthenticated(true);
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(201)
                    .body("$",
                            hasEntry("https://doi.org/10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5",
                                    "10.16907/d910159a-d48a-45fb-acf2-74b27cd5a8e5"));
            given()
                    .header("Content-Type", ExtraMediaType.APPLICATION_JSONLD)
                    .body(getClass().getClassLoader().getResourceAsStream("one-publication.json"))
                    .when()
                    .post("/ro-crate/import")
                    .then()
                    .statusCode(409);
        }
    }
}
