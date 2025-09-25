package ch.psi.ord.core;

import com.apicatalog.jsonld.JsonLdOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LangJSONLD11;
import org.apache.jena.sparql.util.Context;

@Slf4j
public class RoCrate implements AutoCloseable {
  public static final Path DEFAULT_BASE = Path.of("/");
  private static final String FILE_KEY = "file";
  private static final String DIR_KEY = "directory";

  private Map<String, List<Path>> files =
      Map.of(FILE_KEY, new ArrayList<>(), DIR_KEY, new ArrayList<>());
  @Getter private Path base = DEFAULT_BASE;
  @Getter private Model model;

  /**
   * @param metadataDescriptor
   * @throws RiotException
   * @throws IOException
   */
  public RoCrate(InputStream metadataDescriptor) throws RiotException {
    parseMetadataDescriptor(metadataDescriptor);
  }

  /**
   * @param zip
   * @throws RiotException
   * @throws FileNotFoundException
   * @throws IOException
   */
  public RoCrate(ZipInputStream zip)
      throws RiotException, FileNotFoundException, ZipException, IOException {
    extract(zip);
    readMetadataDescriptor();
  }

  /**
   * Creates a temporary directory and set it as the base path to extract the zip archive
   *
   * @throws IOException if the creation of the temporary directory fails
   */
  private void createTempDirectory() throws IOException {
    // Jena truncates the base to /tmp if the base doesn't end with a '/'
    base = Path.of(Files.createTempDirectory("scicat-rocrate").toString(), "/");
    log.info("Created extraction directory {}", base);
  }

  /**
   * Extracts a zipped crate in a temporary directory
   *
   * @param zip
   * @throws IOException if an IO error occurs during the creation of a temporary directory or
   *     during the extraction of the zip archive
   */
  private void extract(ZipInputStream zip) throws ZipException, IOException {
    createTempDirectory();

    Path targetDir = base;
    int entryCount = 0;
    for (ZipEntry entry; (entry = zip.getNextEntry()) != null; entryCount++) {
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
        Files.copy(zip, resolvedPath);
        files.get(FILE_KEY).add(resolvedPath);
        log.debug("Wrote file {}", resolvedPath);
      }
    }

    if (entryCount == 0) {
      // With the ZipInputStream API there is no way of telling the difference between invalid and
      // empty zip archive
      throw new ZipException("Invalid or empty zip archive");
    }
  }

  @Override
  public void close() {
    if (base.equals(DEFAULT_BASE)) {
      log.debug("Nothing to cleanup");
      return;
    }

    try (var paths = Files.walk(base)) {
      paths
          .sorted(Comparator.reverseOrder())
          .forEachOrdered(
              path -> {
                File f = path.toFile();
                String key = f.isFile() ? FILE_KEY : DIR_KEY;
                f.delete();
                files.get(key).remove(path);
                log.info("Deleted {} {}", key, path);
              });

    } catch (IOException e) {
      log.error("Failed to cleanup crate located at {} ({})", base, e.getMessage());
    }
  }

  public List<Path> listFiles() {
    List<Path> l = new ArrayList<>();
    l.add(base);
    l.addAll(files.get(FILE_KEY));
    l.addAll(files.get(DIR_KEY));

    return l;
  }

  private void parseMetadataDescriptor(InputStream document) throws RiotException {
    model =
        RDFParser.create()
            .source(document)
            .lang(Lang.JSONLD11)
            .base(String.format(base.toUri().toString()))
            .context(Context.create().set(LangJSONLD11.JSONLD_OPTIONS, new JsonLdOptions()))
            .build()
            .toModel();
  }

  private void readMetadataDescriptor() throws IOException, FileNotFoundException {
    if (base.equals(DEFAULT_BASE)) {
      throw new UnsupportedOperationException();
    }

    Path metadataDescriptor = base.resolve("ro-crate-metadata.json");
    if (!metadataDescriptor.toFile().exists()) {
      throw new FileNotFoundException("Archive doesn't contain a \"ro-crate-metadata.json\" file");
    }

    try (InputStream content = new FileInputStream(metadataDescriptor.toFile())) {
      parseMetadataDescriptor(content);
    }
  }
}
