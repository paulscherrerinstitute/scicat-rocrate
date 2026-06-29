package ch.psi.ord.api;

import static io.restassured.RestAssured.given;

import ch.psi.ord.core.RoCrate;
import ch.psi.s3_broker.client.S3BrokerService;
import ch.psi.scicat.cli.ScicatCli;
import ch.psi.scicat.client.ScicatClient;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
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
    String loginPayload =
        """
        {
          "username":"rocrate",
          "password":"rocrate"
        }
        """;

    return given()
        .baseUri("http://backend.localhost")
        .basePath("")
        .port(80)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body(loginPayload)
        .when()
        .post("/api/v3/auth/login")
        .then()
        .statusCode(201)
        .extract()
        .path("access_token");
  }

  public byte[] zipResource(String resourceName) throws IOException {
    return zipResource(resourceName, Collections.emptyMap());
  }

  public byte[] zipResource(String resourceName, Map<String, BigInteger> fileList)
      throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Random random = new Random();
    Set<String> createdDirectories = new HashSet<>();
    try (ZipOutputStream zipStream = new ZipOutputStream(output)) {
      ZipEntry entry = new ZipEntry(RoCrate.METADATA_DESCRIPTOR);
      zipStream.putNextEntry(entry);
      byte[] content = getClass().getClassLoader().getResourceAsStream(resourceName).readAllBytes();
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
  }

  private void createParentDirectories(
      String filePath, ZipOutputStream zipStream, Set<String> createdDirectories)
      throws IOException {
    int lastSlash = filePath.lastIndexOf('/');
    if (lastSlash != -1) {
      String dirPath = filePath.substring(0, lastSlash + 1); // e.g., "parent/child/"
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
