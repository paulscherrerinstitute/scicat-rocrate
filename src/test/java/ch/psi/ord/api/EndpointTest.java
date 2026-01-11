package ch.psi.ord.api;

import ch.psi.scicat.client.ScicatClient;
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

public abstract class EndpointTest {
  @InjectMock protected ScicatClient scicatClient;

  private HttpClient client = HttpClient.newHttpClient();
  private final String loginPayload = "{\"username\":\"rocrate\", \"password\":\"rocrate\"}";
  private ObjectMapper objectMapper = new ObjectMapper();

  protected String accessToken = "";

  public static String CONTENT_TYPE_JSON_RES = "application/json;charset=UTF-8";

  public String login() {
    try {
      URI urlV3 = URI.create("http://backend.localhost/api/v3/Users/login");
      URI urlV4 = URI.create("http://backend.localhost/api/v3/auth/login");

      HttpResponse<String> response = sendLoginRequest(urlV3);
      boolean isBackendNext = response.statusCode() == 201;
      if (isBackendNext) {
        response = sendLoginRequest(urlV4);
      }

      JsonNode jsonResponse = objectMapper.readTree(response.body());
      return jsonResponse.get(isBackendNext ? "access_token" : "id").asText();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new RuntimeException("Failed to login to SciCat, aborting tests");
    }
  }

  private HttpResponse<String> sendLoginRequest(URI uri) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
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
