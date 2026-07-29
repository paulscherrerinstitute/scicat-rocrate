package ch.psi.scicat.cli;

import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.DatasetLifeCycle;
import ch.psi.scicat.model.v3.UpdateDatasetDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Slf4j
@ApplicationScoped
public class ScicatCli {
  @Inject private ObjectMapper mapper;
  private final String cliPath;
  private final String scicatUrl;
  private final Pattern pidPattern;
  private final String archiveDirectory;
  @Inject private ScicatClient scicatClient;

  @Inject
  public ScicatCli(
      @ConfigProperty(name = "scicat.cli.path", defaultValue = "/usr/local/bin/scicat-cli")
          String cliPath,
      @ConfigProperty(
              name = "quarkus.rest-client.scicat.url",
              defaultValue = "http://backend.localhost")
          String scicatBaseUrl,
      @ConfigProperty(name = "scicat.pid-prefix", defaultValue = "PID.SAMPLE.PREFIX")
          String pidPrefix,
      @ConfigProperty(name = "rocrate.archive-directory", defaultValue = "/rocrate/archive")
          String archiveDirectory) {
    Path p = Path.of(cliPath);
    if (Files.isDirectory(p) || !Files.isExecutable(p)) {
      throw new IllegalStateException(
          String.format("scicat-cli binary not found or not executable at: %s.", cliPath));
    }

    this.cliPath = cliPath;
    this.scicatUrl = String.format("%s/api/v3", scicatBaseUrl);
    this.pidPattern =
        Pattern.compile(String.format("^%s/[a-zA-Z0-9.-]+$", Pattern.quote(pidPrefix)));
    this.archiveDirectory = archiveDirectory;
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
        Files.write(fileListPath, fileList);
      }
      ProcessBuilder pb =
          new ProcessBuilder(
                  cliPath,
                  "datasetIngestor",
                  "--scicat-url",
                  scicatUrl,
                  "--token",
                  scicatToken,
                  "--nocopy",
                  "--ingest",
                  "--noninteractive",
                  "--allowexistingsource",
                  metadataPath.toAbsolutePath().toString())
              .redirectErrorStream(true);
      if (!fileList.isEmpty()) {
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
        allLines.forEach(line -> log.error("[scicat-cli] {}", line));
        throw new ScicatCliException("scicat-cli execution failed with exit code: " + exitCode);
      }

      String pid =
          allLines.stream()
              .map(String::trim)
              .filter(line -> pidPattern.matcher(line).matches())
              .reduce((first, second) -> second)
              .orElseThrow(
                  () ->
                      new ScicatCliException(
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
      throw new ScicatCliException(
          "Failed to read/write filesystem dependencies or start process", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ScicatCliException("Dataset ingestion execution was interrupted", e);
    } finally {
      cleanupFile(metadataPath);
      cleanupFile(fileListPath);
    }
  }

  public String ingestDataset(String scicatToken, CreateDatasetDto dto) {
    return ingestDataset(scicatToken, dto, Collections.emptyList());
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
