package ch.psi.ord.api;

import static org.junit.jupiter.api.Assertions.fail;

import ch.psi.ord.core.RoCrate;
import ch.psi.s3_broker.client.S3BrokerService;
import ch.psi.scicat.cli.ScicatCli;
import ch.psi.scicat.client.ScicatClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public abstract class EndpointTest {
  @InjectMock protected ScicatClient scicatClient;
  @InjectMock @RestClient protected S3BrokerService s3BrokerService;
  @InjectMock protected ScicatCli scicatCli;

  protected String accessToken = "";

  public static String CONTENT_TYPE_JSON_RES = "application/json;charset=UTF-8";

  public String login() {
    try {
      String url = "http://backend.localhost/api/v3/auth/login";
      String loginPayload =
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
              .POST(HttpRequest.BodyPublishers.ofString(loginPayload))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonResponse = objectMapper.readTree(response.body());
      return jsonResponse.get("access_token").asText();
    } catch (IOException | InterruptedException e) {
      fail("Failed to fetch SciCat access token, aborting tests");
      return "";
    }
  }

  public static byte[] getResource(String resourceName) {
    try {
      return EndpointTest.class.getClassLoader().getResourceAsStream(resourceName).readAllBytes();
    } catch (IOException e) {
      fail(String.format("Failed to read resource %s", resourceName));
      return new byte[0];
    }
  }

  public static byte[] zipResource(String resourceName, Map<String, BigInteger> fileList) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      Random random = new Random();
      Set<String> createdDirectories = new HashSet<>();
      try (ZipOutputStream zipStream = new ZipOutputStream(output)) {
        ZipEntry entry = new ZipEntry(RoCrate.METADATA_DESCRIPTOR);
        zipStream.putNextEntry(entry);
        byte[] content = getResource(resourceName);
        zipStream.write(content, 0, content.length);
        zipStream.closeEntry();

        for (Map.Entry<String, BigInteger> file : fileList.entrySet()) {
          createParentDirectories(file.getKey(), zipStream, createdDirectories);
          zipStream.putNextEntry(new ZipEntry(file.getKey()));
          byte[] randomBytes = new byte[file.getValue().intValue()];
          random.nextBytes(randomBytes);
          zipStream.write(randomBytes);
          zipStream.closeEntry();
        }
      }

      return output.toByteArray();
    } catch (IOException e) {
      fail(String.format("Failed to zip resource %s", resourceName));
      return new byte[0];
    }
  }

  public static byte[] zipResource(String resourceName) {
    return zipResource(resourceName, Collections.emptyMap());
  }

  private static void createParentDirectories(
      String filePath, ZipOutputStream zipStream, Set<String> createdDirectories)
      throws IOException {
    int lastSlash = filePath.lastIndexOf('/');
    if (lastSlash != -1) {
      String dirPath = filePath.substring(0, lastSlash + 1);
      if (!createdDirectories.contains(dirPath)) {
        if (dirPath.length() > 1) {
          String parentDir =
              dirPath.substring(0, dirPath.lastIndexOf('/', dirPath.length() - 2) + 1);
          if (!parentDir.isEmpty()) {
            createParentDirectories(parentDir, zipStream, createdDirectories);
          }
        }
        ZipEntry dirEntry = new ZipEntry(dirPath);
        zipStream.putNextEntry(dirEntry);
        zipStream.closeEntry();
        createdDirectories.add(dirPath);
      }
    }
  }
}
