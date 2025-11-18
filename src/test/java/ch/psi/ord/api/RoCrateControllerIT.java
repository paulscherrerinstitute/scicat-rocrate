package ch.psi.ord.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@QuarkusIntegrationTest
public class RoCrateControllerIT extends RoCrateControllerTest {
  public RoCrateControllerIT() throws Exception {
    this.accessToken = login();
  }

  public String login() throws IOException, InterruptedException {
    String url = "http://backend.localhost/api/v3/Users/login";
    String jsonInputString = "{\"username\":\"rocrate\", \"password\":\"rocrate\"}";
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
            .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonResponse = objectMapper.readTree(response.body());
    return jsonResponse.get("id").asText();
  }
}
