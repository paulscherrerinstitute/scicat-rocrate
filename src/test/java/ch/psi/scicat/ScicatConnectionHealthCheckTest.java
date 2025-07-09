package ch.psi.scicat;

import static io.restassured.RestAssured.given;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ScicatConnectionHealthCheckTest {
    ScicatServiceMock scicatServiceMock;

    @BeforeEach
    public void setUp() {
        scicatServiceMock = new ScicatServiceMock();
        QuarkusMock.installMockForType(scicatServiceMock, ScicatService.class, RestClient.LITERAL);
    }

    @Test
    public void healthyScicat() {
        QuarkusMock.installMockForType(scicatServiceMock, ScicatService.class, RestClient.LITERAL);
        given().when().get("health")
                .then()
                .statusCode(200);
    }

    @Test
    public void unhealthyScicat() {
        scicatServiceMock.setHealthy(false);
        given().when().get("health")
                .then()
                .statusCode(503);
    }
}
