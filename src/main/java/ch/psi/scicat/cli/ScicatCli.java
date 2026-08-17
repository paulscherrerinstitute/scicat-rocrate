package ch.psi.scicat.cli;

import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.DatasetLifeCycle;
import ch.psi.scicat.model.v3.UpdateDatasetDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.configuration.ConfigurationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Startup
@ApplicationScoped
public class ScicatCli {
  @Inject private ObjectMapper mapper;
  private final String cliPath;
  private final String scicatUrl;
  private final Pattern pidPattern;
  private final String archiveDirectory;
  @RestClient @Inject private ScicatService scicatService;

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
          String archiveDirectory,
      @ConfigProperty(name = "rocrate.extract-directory", defaultValue = "/rocrate/extract")
          String extractDirectory) {
    Path p = Path.of(cliPath);
    if (Files.isDirectory(p) || !Files.isExecutable(p)) {
      throw new ConfigurationException(
          String.format("scicat-cli binary not found or not executable at: %s.", cliPath));
    }
    try {
      FileStore extractStore = Files.getFileStore(Path.of(extractDirectory));
      FileStore archiveStore = Files.getFileStore(Path.of(archiveDirectory));
      if (!extractStore.equals(archiveStore)) {
        throw new ConfigurationException(
            String.format(
                "The extract directory '%s' and the archive directory '%s' must be located on the"
                    + " same filesystem.",
                extractDirectory, archiveDirectory));
      }
    } catch (IOException e) {
      throw new ConfigurationException(
          String.format(
              "The extract directory '%s' and the archive directory '%s' must both exist.",
              extractDirectory, archiveDirectory),
          e);
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

      moveData(pid, dto.getSourceFolder(), fileList);

      scicatService.updateDataset(
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

  private void moveData(String pid, String sourceFolder, Collection<String> fileList)
      throws IOException {
    Path archiveRoot = Path.of(archiveDirectory, pid);
    if (fileList.isEmpty()) {
      Files.createDirectories(archiveRoot.getParent());
      move(Path.of(sourceFolder), archiveRoot);
    } else {
      Set<Path> createdDirectories = new HashSet<>();
      createdDirectories.add(Files.createDirectories(archiveRoot));
      for (String file : fileList) {
        Path relativePath = Path.of(sourceFolder).relativize(Path.of(file));
        Path targetPath = archiveRoot.resolve(relativePath);
        Path parent = targetPath.getParent();
        if (createdDirectories.add(parent)) {
          Files.createDirectories(parent);
        }
        move(Path.of(file), targetPath);
      }
    }
  }

  private void move(Path source, Path target) throws IOException {
    log.debug("Moving {} to {}", source, target);
    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
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
