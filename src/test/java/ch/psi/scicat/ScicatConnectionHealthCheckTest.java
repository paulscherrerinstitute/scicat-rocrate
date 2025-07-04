package ch.psi.scicat;

import static io.restassured.RestAssured.given;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ScicatConnectionHealthCheckTest {
    @Test
    public void healthyScicat() {
        QuarkusMock.installMockForType(ScicatServiceMock.generate(true), ScicatService.class, RestClient.LITERAL);
        given().when().get("health")
                .then()
                .statusCode(200);
    }

    @Test
    public void unhealthyScicat() {
        QuarkusMock.installMockForType(ScicatServiceMock.generate(false), ScicatService.class, RestClient.LITERAL);
        given().when().get("health")
                .then()
                .statusCode(503);
    }
}
