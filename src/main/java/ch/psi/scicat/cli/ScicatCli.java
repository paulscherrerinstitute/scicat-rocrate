package ch.psi.scicat.cli;

import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.DatasetLifeCycle;
import ch.psi.scicat.model.v3.UpdateDatasetDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class ScicatCli {
  @ConfigProperty(name = "quarkus.rest-client.scicat.url")
  private String scicatUrl;

  @ConfigProperty(name = "rocrate.archive-directory", defaultValue = "/rocrate/archive")
  private String archiveDirectory;

  @Inject private ScicatClient scicatClient;

  private static final ObjectMapper mapper = Arc.container().instance(ObjectMapper.class).get();
  private static final Pattern PID_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+/[a-zA-Z0-9.-]+$");

  public static class DatasetCreationException extends RuntimeException {
    public DatasetCreationException(String message) {
      super(message);
    }

    public DatasetCreationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Ingests a dataset using the scicat-cli.
   *
   * @return The PID of the created dataset.
   * @throws DatasetCreationException if the process fails or PID is not found.
   */
  public String ingestDataset(
      String scicatToken, CreateDatasetDto dto, Collection<String> fileList) {
    Path metadataPath = null;
    Path fileListPath = null;

    try {
      metadataPath = Files.createTempFile("scicat-cli-metadata", ".tmp");
      mapper.writeValue(metadataPath.toFile(), dto);
      if (!fileList.isEmpty()) {
        fileListPath = Files.createTempFile("scicat-cli-filelist", ".tmp");
        List<String> relativePaths =
            fileList.stream()
                .map(
                    absolutePath ->
                        Path.of(dto.getSourceFolder()).relativize(Path.of(absolutePath)).toString())
                .toList();
        Files.write(fileListPath, relativePaths);
      }
      ProcessBuilder pb =
          new ProcessBuilder(
                  "/usr/local/bin/scicat-cli",
                  "datasetIngestor",
                  "--scicat-url",
                  String.format("%s/api/v3", scicatUrl),
                  "--token",
                  scicatToken,
                  "--nocopy",
                  "--ingest",
                  "--noninteractive",
                  "--allowexistingsource",
                  metadataPath.toAbsolutePath().toString())
              .redirectErrorStream(true);
      if (fileListPath != null) {
        pb.command().add(fileListPath.toAbsolutePath().toString());
      }

      Process process = pb.start();
      List<String> allLines = new ArrayList<>();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          log.debug("[scicat-cli] {}", line);
          allLines.add(line);
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        allLines.forEach(line -> log.error("[scicat-cli failure output] {}", line));
        throw new DatasetCreationException(
            "scicat-cli execution failed with exit code: " + exitCode);
      }

      String pid =
          allLines.stream()
              .map(String::trim)
              .filter(line -> PID_PATTERN.matcher(line).matches())
              .reduce((first, second) -> second)
              .orElseThrow(
                  () ->
                      new DatasetCreationException(
                          "CLI reported success, but no valid PID matching pattern was found in"
                              + " output."));

      symlinkData(pid, dto.getSourceFolder(), fileList);

      scicatClient.updateDataset(
          scicatToken,
          pid,
          new UpdateDatasetDto()
              .setSourceFolder("/")
              .setDatasetlifecycle(
                  new DatasetLifeCycle()
                      .setArchivable(true)
                      .setOnCentralDisk(false)
                      .setRetrievable(false)
                      .setArchiveStatusMessage("datasetCreated")));

      return pid;
    } catch (IOException e) {
      throw new DatasetCreationException(
          "Failed to read/write filesystem dependencies or start process", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DatasetCreationException("Dataset ingestion execution was interrupted", e);
    } finally {
      cleanupFile(metadataPath);
      cleanupFile(fileListPath);
    }
  }

  private void symlinkData(String pid, String sourceFolder, Collection<String> fileList)
      throws IOException {
    Path archiveRoot = Path.of(archiveDirectory, pid);
    log.warn("archiveRoot: {}", archiveRoot.toString());
    if (fileList.isEmpty()) {
      Files.createDirectories(archiveRoot.getParent());
      Files.createSymbolicLink(archiveRoot, Path.of(sourceFolder));
    } else {
      Files.createDirectories(archiveRoot);
      for (String file : fileList) {
        Path relativePath = Path.of(sourceFolder).relativize(Path.of(file));
        Path linkPath = archiveRoot.resolve(relativePath);
        log.warn("linkPath: {}", linkPath.toString());
        Files.createDirectories(linkPath.getParent());
        Files.createSymbolicLink(linkPath, Path.of(file));
      }
    }
  }

  public String ingestDataset(String scicatToken, CreateDatasetDto dto) {
    return ingestDataset(scicatToken, dto, Collections.emptyList());
  }

  private void cleanupFile(Path path) {
    if (path != null) {
      try {
        Files.deleteIfExists(path);
      } catch (IOException e) {
        log.error("Failed to clean up temporary file: {}", path.toAbsolutePath(), e);
      }
    }
  }
}
