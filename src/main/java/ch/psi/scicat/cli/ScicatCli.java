package ch.psi.scicat.cli;

import ch.psi.scicat.model.v3.CreateDatasetDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class ScicatCli {
  @ConfigProperty(name = "quarkus.rest-client.scicat.url")
  private String scicatUrl;

  private static final ObjectMapper mapper = Arc.container().instance(ObjectMapper.class).get();
  private static final Pattern PID_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+/[a-zA-Z0-9.-]+$");

  public String createDataset(String scicatToken, CreateDatasetDto dto, boolean isOnCentralDisk)
      throws IOException, InterruptedException {
    Path path = Files.createTempFile("scicat-cli-", ".tmp");
    mapper.writeValue(path.toFile(), dto);

    ProcessBuilder pb =
        new ProcessBuilder(
                "/usr/local/bin/scicat-cli",
                "--scicat-url",
                String.format("%s/api/v3", scicatUrl),
                "--token",
                scicatToken,
                "datasetIngestor",
                path.toAbsolutePath().toString(),
                isOnCentralDisk ? "--nocopy" : "",
                "--ingest",
                "--allowexistingsource",
                "--noninteractive")
            .redirectErrorStream(true);

    Process process = pb.start();

    List<String> allLines = new ArrayList<>();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        log.info(line);
        allLines.add(line);
      }
    }

    int exitCode = process.waitFor();
    Files.delete(path);

    if (exitCode != 0) {
      System.err.println("scicat-cli failed with exit code: " + exitCode);
      allLines.forEach(line -> System.err.println("[scicat-cli-out]: " + line));
      return null;
    }

    return allLines.stream()
        .map(String::trim)
        .filter(line -> PID_PATTERN.matcher(line).matches())
        .reduce((first, second) -> second)
        .orElse(null);
  }
}
