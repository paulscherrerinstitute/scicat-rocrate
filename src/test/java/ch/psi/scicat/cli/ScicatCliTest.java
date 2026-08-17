package ch.psi.scicat.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectSpy;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestProfile(ScicatCliTest.Profile.class)
public class ScicatCliTest {
  private static final Path tempDirectory = Path.of(System.getProperty("java.io.tmpdir"));
  private static final Path cliPath = tempDirectory.resolve("scicat-cli-stub");
  private static final Path archiveDirectory = tempDirectory.resolve("scicat-cli-archive");
  private static final String pid = "PID.SAMPLE.PREFIX/psi-ds1";
  private static final Path archiveRoot = archiveDirectory.resolve(pid);

  public static class Profile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "scicat.cli.path", cliPath.toString(),
          "rocrate.archive-directory", archiveDirectory.toString(),
          "rocrate.extract-directory", tempDirectory.toString());
    }
  }

  @InjectSpy ScicatCli scicatCli;
  @InjectMock @RestClient ScicatService scicatService;
  @TempDir Path sourceFolder;

  static {
    try {
      Files.createDirectories(archiveDirectory);
      Files.writeString(cliPath, String.format("#!/bin/sh%necho %s%n", pid));
      Files.setPosixFilePermissions(cliPath, PosixFilePermissions.fromString("rwxr-xr-x"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @BeforeEach
  public void resetArchiveDirectory() throws IOException {
    deleteRecursively(archiveDirectory);
  }

  @ParameterizedTest(name = "should throw if scicat.cli.path={0} is not a file or not executable")
  @ValueSource(strings = {"non-existing", "/usr", "/etc/localtime"})
  public void test00(String cliPath) {
    assertThrows(
        ConfigurationException.class, () -> new ScicatCli(cliPath, null, null, null, null));
  }

  @Test
  @DisplayName("should throw if the extract and archive directories are on different filesystems")
  public void test01() throws IOException {
    Path tmpfs = Path.of("/dev/shm");
    assumeTrue(Files.isDirectory(tmpfs), "/dev/shm is not usable");
    assumeFalse(
        Files.getFileStore(tmpfs).equals(Files.getFileStore(tempDirectory)),
        "/dev/shm is on the same filesystem as the extract directory");

    ConfigurationException e =
        assertThrows(
            ConfigurationException.class,
            () ->
                new ScicatCli(
                    cliPath.toString(), null, null, tmpfs.toString(), tempDirectory.toString()));

    assertTrue(e.getMessage().endsWith("must be located on the same filesystem."));
  }

  static final List<Arguments> moveMatrix =
      List.of(
          arguments(
              "flat file list",
              Set.of("first.txt", "second.txt"),
              Set.of("first.txt", "second.txt")),
          arguments(
              "nested directories",
              Set.of("data/first.txt", "data/nested/second.txt"),
              Set.of("data/first.txt", "data/nested/second.txt")),
          arguments(
              "partial file list", Set.of("listed.txt", "unlisted.txt"), Set.of("listed.txt")));

  @ParameterizedTest(name = "{0}")
  @FieldSource("moveMatrix")
  public void test02(String name, Set<String> sourceFiles, Set<String> ingested)
      throws IOException {
    for (String file : sourceFiles) {
      Files.createDirectories(sourceFolder.resolve(file).getParent());
      Files.writeString(sourceFolder.resolve(file), file);
    }

    scicatCli.ingestDataset(
        "token",
        new CreateDatasetDto().setSourceFolder(sourceFolder.toString()),
        ingested.stream().map(file -> sourceFolder.resolve(file).toString()).toList());

    Set<String> notIngested =
        sourceFiles.stream().filter(file -> !ingested.contains(file)).collect(Collectors.toSet());
    assertEquals(ingested, filesUnder(archiveRoot));
    assertEquals(notIngested, filesUnder(sourceFolder));
  }

  @Test
  @DisplayName("An empty file list moves the whole source folder")
  public void test03() throws IOException {
    Files.writeString(sourceFolder.resolve("first.txt"), "first.txt");

    scicatCli.ingestDataset(
        "token", new CreateDatasetDto().setSourceFolder(sourceFolder.toString()));

    assertFalse(Files.exists(sourceFolder));
    assertEquals(Set.of("first.txt"), filesUnder(archiveRoot));
  }

  private static Set<String> filesUnder(Path root) throws IOException {
    try (var paths = Files.walk(root)) {
      return paths
          .filter(Files::isRegularFile)
          .map(path -> root.relativize(path).toString())
          .collect(Collectors.toSet());
    }
  }

  private static void deleteRecursively(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }
    try (var paths = Files.walk(path)) {
      paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
  }
}
