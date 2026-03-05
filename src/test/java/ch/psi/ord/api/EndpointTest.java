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

  static boolean backendV4 = System.getenv("BACKEND_VERSION").equals("v4");

  protected String accessToken = "";
  public static String CONTENT_TYPE_JSON_RES = "application/json;charset=UTF-8";

  public String login() {
    try {
      String url =
          String.format("http://backend.localhost/api/v3/%s/login", backendV4 ? "auth" : "Users");
      String jsonInputString =
          """
          {
            "username":"rocrate",
            "password":"rocrate"
          }
          """;
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
      return jsonResponse.get(backendV4 ? "access_token" : "id").asText();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new RuntimeException("Failed to login to SciCat, aborting tests");
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
