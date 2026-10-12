package ch.psi.ord.core;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Periodically deletes old files from the extract and archive directories. */
@Slf4j
@ApplicationScoped
public class RoCrateCleanup {
  private final List<Path> directories;
  private final Duration maxAge;

  @Inject
  public RoCrateCleanup(
      @ConfigProperty(name = "rocrate.extract-directory", defaultValue = "/rocrate/extract")
          String extractDirectory,
      @ConfigProperty(name = "rocrate.archive-directory", defaultValue = "/rocrate/archive")
          String archiveDirectory,
      @ConfigProperty(name = "rocrate.cleanup.max-age-days", defaultValue = "7") int maxAgeDays) {
    this.directories = List.of(Path.of(extractDirectory), Path.of(archiveDirectory));
    this.maxAge = Duration.ofDays(maxAgeDays);
  }

  @Scheduled(cron = "${rocrate.cleanup.cron:0 0 0 * * ?}")
  void run() {
    Instant cutoff = Instant.now().minus(maxAge);
    for (Path root : directories) {
      // sorted() reads every modification time before the first deletion, and the reverse order
      // deletes the content of a directory before the directory itself
      try (Stream<Path> paths = Files.walk(root)) {
        paths
            .filter(p -> !p.equals(root) && isOlderThan(p, cutoff))
            .sorted(Comparator.reverseOrder())
            .forEach(RoCrateCleanup::delete);
      } catch (IOException | UncheckedIOException e) {
        log.error("Failed to cleanup {} ({})", root, e.getMessage());
      }
    }
  }

  private static boolean isOlderThan(Path p, Instant cutoff) {
    try {
      return Files.getLastModifiedTime(p).toInstant().isBefore(cutoff);
    } catch (IOException e) {
      return false;
    }
  }

  private static void delete(Path p) {
    try {
      Files.delete(p);
      log.info("Deleted {}", p);
    } catch (DirectoryNotEmptyException e) {
      // still contains recent files
    } catch (IOException e) {
      log.error("Failed to delete {} ({})", p, e.getMessage());
    }
  }
}
