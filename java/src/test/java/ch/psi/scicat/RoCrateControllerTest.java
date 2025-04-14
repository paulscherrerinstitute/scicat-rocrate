// package ch.psi.scicat;

// import static io.restassured.RestAssured.given;

// import org.junit.jupiter.api.Test;

// import io.quarkus.test.common.QuarkusTestResource;
// import io.quarkus.test.junit.QuarkusTest;

// @QuarkusTest
// @QuarkusTestResource(value = ScicatContainers.class, parallel = true)
// public class RoCrateControllerTest {
// @Test
// public void testHealthCheck() {
// given().when().get("health")
// .then()
// .statusCode(200);
// }
// }
