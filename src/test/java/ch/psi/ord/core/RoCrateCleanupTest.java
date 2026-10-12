package ch.psi.ord.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RoCrateCleanupTest {
  private static final Instant OLD = Instant.now().minus(Duration.ofDays(8));

  @TempDir Path extract;
  @TempDir Path archive;

  private RoCrateCleanup cleanup() {
    return new RoCrateCleanup(extract.toString(), archive.toString(), 7);
  }

  private static Path touch(Path p, Instant mtime) throws Exception {
    Files.createDirectories(p.getParent());
    if (!Files.exists(p)) {
      Files.createFile(p);
    }
    Files.setLastModifiedTime(p, FileTime.from(mtime));
    return p;
  }

  @Test
  @DisplayName("Old files are deleted, recent ones are kept")
  public void test00() throws Exception {
    Path oldFile = touch(extract.resolve("crate/old.txt"), OLD);
    Path newFile = touch(extract.resolve("crate/new.txt"), Instant.now());
    Path oldArchived = touch(archive.resolve("prefix/pid/data.txt"), OLD);

    cleanup().run();

    assertFalse(Files.exists(oldFile));
    assertTrue(Files.exists(newFile));
    assertFalse(Files.exists(oldArchived));
  }

  @Test
  @DisplayName("Old directories left empty are deleted, the roots are kept")
  public void test01() throws Exception {
    Path dir = extract.resolve("crate");
    touch(dir.resolve("nested/old.txt"), OLD);
    Files.setLastModifiedTime(dir.resolve("nested"), FileTime.from(OLD));
    Files.setLastModifiedTime(dir, FileTime.from(OLD));

    cleanup().run();

    assertFalse(Files.exists(dir));
    assertTrue(Files.isDirectory(extract));
    assertTrue(Files.isDirectory(archive));
  }

  @Test
  @DisplayName("Recent empty directories are kept")
  public void test02() throws Exception {
    Path dir = Files.createDirectories(extract.resolve("scicat-rocrate123"));

    cleanup().run();

    assertTrue(Files.exists(dir));
  }
}
