package ch.psi.ord.core;

import static ch.psi.rdf.RdfUtils.hasProperty;
import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;
import static ch.psi.rdf.RdfUtils.listResourcesOfType;

import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.uri.UriValidationPolicy;
import io.smallrye.config.Config;
import io.smallrye.config.SmallRyeConfig;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LangJSONLD11;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.SchemaDO;
import org.eclipse.microprofile.config.ConfigProvider;

@Slf4j
public class RoCrate implements AutoCloseable {
  public static final String METADATA_DESCRIPTOR = "ro-crate-metadata.json";
  private static final String FILE_KEY = "file";
  private static final String DIR_KEY = "directory";
  private static Config config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
  private static String extractDir =
      config.getOptionalValue("rocrate.extract-directory", String.class).orElse("/rocrate/extract");
  private static final int maxPathLength =
      config.getOptionalValue("rocrate.max-path-length", Integer.class).orElse(4096);
  private static final int maxPathSegmentLength =
      config.getOptionalValue("rocrate.max-path-segment-length", Integer.class).orElse(256);
  private static Integer jsonLdTimeout =
      config.getOptionalValue("jsonld.processing-timeout", Integer.class).orElse(10);
  private static final DocumentLoader documentLoader = new LoggingDocumentLoader();

  private Map<String, List<Path>> files =
      Map.of(FILE_KEY, new ArrayList<>(), DIR_KEY, new ArrayList<>());
  @Getter private Path base;
  @Getter private Model model;
  @Getter private Resource metadataDescriptor;
  @Getter private Resource root;

  @Getter
  @Accessors(fluent = true)
  private boolean hasAttachedData = false;

  @Getter @Setter private boolean scheduledForArchival = false;

  private JsonLdOptions jsonLdOptions = new JsonLdOptions();

  private RoCrate() {
    // required to support percent encoded @id's
    // https://github.com/apache/jena/issues/4025
    jsonLdOptions.setUriValidation(UriValidationPolicy.SchemeOnly);
    jsonLdOptions.setTimeout(Duration.ofSeconds(jsonLdTimeout));
    jsonLdOptions.setDocumentLoader(documentLoader);
  }

  public static RoCrate fromMetadata(String metadata) throws Exception {
    return RoCrate.fromMetadata(
        new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8)));
  }

  public static RoCrate fromMetadata(InputStream metadataDescriptor)
      throws RoCrateException, IOException {
    RoCrate crate = new RoCrate();
    try {
      crate.createTempDirectory();
      Files.write(crate.base.resolve(METADATA_DESCRIPTOR), metadataDescriptor.readAllBytes());
      crate.readMetadataDescriptor();
    } catch (Throwable t) {
      crate.close();
      throw t;
    }
    return crate;
  }

  public static RoCrate fromZip(InputStream zip)
      throws RoCrateException, ZipException, IOException {
    RoCrate crate = new RoCrate();
    try {
      crate.extract(zip);
      crate.readMetadataDescriptor();
      crate.hasAttachedData = true;
    } catch (Throwable t) {
      crate.close();
      throw t;
    }
    return crate;
  }

  /**
   * Creates a temporary directory and set it as the base path to extract the zip archive
   *
   * @throws IOException if the creation of the temporary directory fails
   */
  private void createTempDirectory() throws IOException {
    // Jena truncates the base to /${extractDir} if the base doesn't end with a '/'
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

        Path resolvedPath = targetDir.resolve(entry.getName()).normalize().toAbsolutePath();
        if (!isValidPath(resolvedPath)) {
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
    if (base == null || !Files.exists(base)) {
      return;
    }

    if (scheduledForArchival) {
      log.info(
          "Crate at '{}' is scheduled for archival, skipping filesystem cleanup",
          base.toAbsolutePath());
      return;
    }

    try (var paths = Files.walk(base)) {
      Map<String, Integer> deleted = new HashMap<>(Map.of(FILE_KEY, 0, DIR_KEY, 0));
      paths
          .sorted(Comparator.reverseOrder())
          .forEachOrdered(
              path -> {
                File f = path.toFile();
                String key = f.isFile() ? FILE_KEY : DIR_KEY;
                f.delete();
                files.get(key).remove(path);
                deleted.merge(key, 1, Integer::sum);
                log.debug("Deleted {} {}", key, path);
              });
      log.info(
          "Deleted crate at {} ({} files, {} directories)",
          base,
          deleted.get(FILE_KEY),
          deleted.get(DIR_KEY));

    } catch (IOException e) {
      log.error("Failed to cleanup crate located at {} ({})", base, e.getMessage());
    }
  }

  public boolean contains(Path p) {
    return base.equals(p) || files.get(FILE_KEY).contains(p) || files.get(DIR_KEY).contains(p);
  }

  private void parseMetadataDescriptor(InputStream document) throws RoCrateException {
    try {

      model =
          RDFParser.create()
              .source(document)
              .lang(Lang.JSONLD11)
              .base(String.format(base.toUri().toString()))
              .context(Context.create().set(LangJSONLD11.JSONLD_OPTIONS, jsonLdOptions))
              .build()
              .toModel();
      findRoot();
    } catch (RiotException e) {
      throw new RoCrateException("Failed to parse the metadata descriptor", e);
    }
  }

  private void readMetadataDescriptor() throws IOException, RoCrateException {
    Path metadataDescriptor = base.resolve(METADATA_DESCRIPTOR);
    if (!metadataDescriptor.toFile().exists()) {
      throw new RoCrateException(
          String.format("Archive doesn't contain a \"%s\" file", METADATA_DESCRIPTOR));
    }

    try (InputStream content = new FileInputStream(metadataDescriptor.toFile())) {
      parseMetadataDescriptor(content);
    }
  }

  // https://www.researchobject.org/ro-crate/specification/1.3/appendix/relative-uris#finding-ro-crate-root-in-rdf-triple-stores
  private Resource findRoot() throws RoCrateException {
    Set<Resource> metadataDescriptor =
        listResourcesOfType(
            model,
            SchemaDO.CreativeWork,
            subject ->
                subject.toString().contains(METADATA_DESCRIPTOR)
                    && hasProperty(subject, SchemaDO.about));

    if (metadataDescriptor.size() != 1) {
      throw new RoCrateException(
          "Expected exactly one metadata descriptor, but found " + metadataDescriptor.size());
    }

    this.metadataDescriptor = metadataDescriptor.iterator().next();

    Set<RDFNode> root =
        listProperties(
            this.metadataDescriptor,
            SchemaDO.about,
            node -> node.isResource() && isOfType(node.asResource(), SchemaDO.Dataset));

    if (root.size() != 1) {
      throw new RoCrateException("Expected exactly one root dataset, but found " + root.size());
    }

    this.root = root.iterator().next().asResource();
    return this.root;
  }

  public String toRelativeId(String absoluteId) {
    String regex =
        String.format("file://(%s/)?", Pattern.quote(getBase().toAbsolutePath().toString()));
    return absoluteId.replaceFirst(regex, "");
  }

  private boolean isValidPath(Path p) {
    if (p.toString().getBytes(StandardCharsets.UTF_8).length >= maxPathLength) {
      log.error("Path '{}' exceeds 'rocrate.max-path-length' ({})", p, maxPathLength);
      return false;
    }

    for (Path segment : p) {
      if (segment.toString().getBytes(StandardCharsets.UTF_8).length >= maxPathSegmentLength) {
        log.error(
            "Path '{}' exceeds 'rocrate.max-path-segment-length' ({})", p, maxPathSegmentLength);
        return false;
      }
    }

    if (!p.startsWith(base)) {
      // see: https://snyk.io/research/zip-slip-vulnerability
      log.error(
          "Path '{}' is outside of of the base extraction directory of the crate '{}'", p, base);
      return false;
    }

    return true;
  }
}
