package ch.psi.ord.api;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

import ch.psi.ord.core.RoCrate;
import ch.psi.s3_broker.client.S3BrokerService;
import ch.psi.scicat.cli.ScicatCli;
import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.v3.CredentialsDto;
import ch.psi.scicat.model.v3.Dataset;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
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
  @InjectMock @RestClient protected ScicatService scicatService;
  @InjectMock @RestClient protected S3BrokerService s3BrokerService;
  @InjectMock protected ScicatCli scicatCli;

  protected String accessToken = "";
  protected String noGroupAccessToken = "";
  protected CredentialsDto rocrateCredentials =
      new CredentialsDto().setUsername("rocrate").setPassword("rocrate");
  protected CredentialsDto noGroupCredentials =
      new CredentialsDto().setUsername("nogroup").setPassword("nogroup");

  public static String CONTENT_TYPE_JSON_RES = "application/json;charset=UTF-8";

  public String login(CredentialsDto credentials) {
    return given()
        .baseUri("http://backend.localhost")
        .port(80)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body(credentials)
        .when()
        .post("/api/v3/auth/login")
        .then()
        .statusCode(201)
        .extract()
        .path("access_token");
  }

  public Dataset getDatasetByPid(String pid) {
    return given()
        .baseUri("http://backend.localhost")
        .port(80)
        .accept(ContentType.JSON)
        .header("Authorization", String.format("Bearer %s", accessToken))
        .pathParam("pid", pid)
        .when()
        .get("/api/v3/datasets/{pid}")
        .then()
        .statusCode(200)
        .extract()
        .as(Dataset.class);
  }

  public ValidatableResponse publishPublishedData(String doi) {
    return given()
        .baseUri("http://backend.localhost")
        .port(80)
        .accept(ContentType.JSON)
        .header("Authorization", String.format("Bearer %s", accessToken))
        .pathParam("doi", doi)
        .when()
        .post("/api/v4/publisheddata/{doi}/publish")
        .then();
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
    return zipResource(resourceName, fileList, true);
  }

  public static byte[] zipResource(
      String resourceName, Map<String, BigInteger> fileList, boolean withDirectoryEntries) {
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
          if (withDirectoryEntries) {
            createParentDirectories(file.getKey(), zipStream, createdDirectories);
          }
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
