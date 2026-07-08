package ch.psi.ord.core;

import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.uri.UriValidationPolicy;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LangJSONLD11;
import org.apache.jena.sparql.util.Context;
import org.eclipse.microprofile.config.ConfigProvider;

@Slf4j
public class RoCrate implements AutoCloseable {
  public static final String METADATA_DESCRIPTOR = "ro-crate-metadata.json";
  public static final Path DEFAULT_BASE = Path.of("/");
  private static final String FILE_KEY = "file";
  private static final String DIR_KEY = "directory";

  private Map<String, List<Path>> files =
      Map.of(FILE_KEY, new ArrayList<>(), DIR_KEY, new ArrayList<>());
  @Getter private Path base = DEFAULT_BASE;
  @Getter private Model model;
  private String extractDir =
      ConfigProvider.getConfig()
          .getOptionalValue("rocrate.extract-directory", String.class)
          .orElse("/rocrate/extract");

  @Getter
  @Accessors(fluent = true)
  private boolean hasAttachedData = false;

  private RoCrate() {}

  public static RoCrate fromMetadata(InputStream metadataDescriptor)
      throws RiotException, IOException {
    RoCrate crate = new RoCrate();
    crate.createTempDirectory();
    Files.write(crate.base.resolve(METADATA_DESCRIPTOR), metadataDescriptor.readAllBytes());
    crate.parseMetadataDescriptor(metadataDescriptor);
    return crate;
  }

  public static RoCrate fromZip(InputStream zip) throws RiotException, ZipException, IOException {
    RoCrate crate = new RoCrate();
    crate.extract(zip);
    crate.readMetadataDescriptor();
    return crate;
  }

  /**
   * Creates a temporary directory and set it as the base path to extract the zip archive
   *
   * @throws IOException if the creation of the temporary directory fails
   */
  private void createTempDirectory() throws IOException {
    // Jena truncates the base to /tmp if the base doesn't end with a '/'
    Path extractionDir = Files.createTempDirectory(Path.of(extractDir), "scicat-rocrate");
    base = Path.of(extractionDir.toString(), "/");
    log.info("Created extraction directory {}", base);
  }

  /**
   * Extracts a zipped crate in a temporary directory
   *
   * @param zip
   * @throws IOException if an IO error occurs during the creation of a temporary directory or
   *     during the extraction of the zip archive
   */
  private void extract(InputStream stream) throws ZipException, IOException {
    createTempDirectory();
    Path targetDir = base;
    int entryCount = 0;
    try (ZipArchiveInputStream zip = new ZipArchiveInputStream(stream)) {
      ZipArchiveEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        entryCount++;

        Path resolvedPath = targetDir.resolve(entry.getName()).normalize();
        if (!resolvedPath.startsWith(targetDir)) {
          // see: https://snyk.io/research/zip-slip-vulnerability
          throw new RuntimeException("Entry with an illegal path: " + entry.getName());
        }

        if (entry.isDirectory()) {
          Files.createDirectories(resolvedPath);
          files.get(DIR_KEY).add(resolvedPath);
          log.debug("Created directory {}", resolvedPath);
        } else {
          Files.createDirectories(resolvedPath.getParent());
          Files.copy(zip, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
          files.get(FILE_KEY).add(resolvedPath);
          log.debug("Wrote file {}", resolvedPath);
        }
      }
    }

    if (entryCount == 0) {
      // With the ZipInputStream API there is no way of telling the difference between invalid
      // and empty zip archive
      throw new ZipException("Invalid or empty zip archive");
    }
  }

  @Override
  public void close() {
    // if (base.equals(DEFAULT_BASE)) {
    //   log.debug("Nothing to cleanup");
    //   return;
    // }

    // try (var paths = Files.walk(base)) {
    //   paths
    //       .sorted(Comparator.reverseOrder())
    //       .forEachOrdered(
    //           path -> {
    //             File f = path.toFile();
    //             String key = f.isFile() ? FILE_KEY : DIR_KEY;
    //             f.delete();
    //             files.get(key).remove(path);
    //             log.info("Deleted {} {}", key, path);
    //           });

    // } catch (IOException e) {
    //   log.error("Failed to cleanup crate located at {} ({})", base, e.getMessage());
    // }
  }

  public List<Path> listFiles() {
    List<Path> l = new ArrayList<>();
    l.add(base);
    l.addAll(files.get(FILE_KEY));
    l.addAll(files.get(DIR_KEY));

    return l;
  }

  private void parseMetadataDescriptor(InputStream document) throws RiotException {
    JsonLdOptions options = new JsonLdOptions();
    // https://github.com/apache/jena/issues/4025
    options.setUriValidation(UriValidationPolicy.SchemeOnly);
    model =
        RDFParser.create()
            .source(document)
            .lang(Lang.JSONLD11)
            .base(String.format(base.toUri().toString()))
            .context(Context.create().set(LangJSONLD11.JSONLD_OPTIONS, options))
            .build()
            .toModel();
  }

  private void readMetadataDescriptor() throws IOException, FileNotFoundException {
    if (base.equals(DEFAULT_BASE)) {
      throw new UnsupportedOperationException();
    }

    Path metadataDescriptor = base.resolve(METADATA_DESCRIPTOR);
    if (!metadataDescriptor.toFile().exists()) {
      throw new FileNotFoundException(
          String.format("Archive doesn't contain a \"%s\" file", METADATA_DESCRIPTOR));
    }

    try (InputStream content = new FileInputStream(metadataDescriptor.toFile())) {
      parseMetadataDescriptor(content);
    }
  }

  public String toRelativeId(String absoluteId) {
    String regex =
        String.format("file://(%s/)?", Pattern.quote(getBase().toAbsolutePath().toString()));
    return absoluteId.replaceFirst(regex, "");
  }
}
