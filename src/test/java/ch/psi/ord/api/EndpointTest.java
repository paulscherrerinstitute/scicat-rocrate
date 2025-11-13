package ch.psi.ord.api;

import ch.psi.scicat.client.ScicatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public abstract class EndpointTest {
  @InjectMock @RestClient protected ScicatService scicatService;
  protected String accessToken = "";

  public static String CONTENT_TYPE_JSON_RES = "application/json;charset=UTF-8";

  public EndpointTest() {
    // Running integration tests
    if (scicatService != null) this.accessToken = login();
  }

  public String login() {
    try {
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
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to fetch SciCat access token, aborting tests");
    }
  }

  public byte[] zipResource(String name) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (ZipOutputStream zipStream = new ZipOutputStream(output)) {
      ZipEntry entry = new ZipEntry("ro-crate-metadata.json");
      zipStream.putNextEntry(entry);
      byte[] content = getClass().getClassLoader().getResourceAsStream(name).readAllBytes();
      zipStream.write(content, 0, content.length);
      zipStream.closeEntry();
    }

    return output.toByteArray();
  }
}
