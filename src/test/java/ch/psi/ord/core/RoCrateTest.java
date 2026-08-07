package ch.psi.ord.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.ord.api.EndpointTest;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.Config;
import io.smallrye.config.SmallRyeConfig;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RoCrateTest {
  @Nested
  @DisplayName("hasAttachedData")
  class HasAttachedData {
    @Test
    @DisplayName("Metadata descriptor only (JSON-LD)")
    public void test00() throws Exception {
      try (RoCrate crate =
          RoCrate.fromMetadata(
              new ByteArrayInputStream(EndpointTest.getResource("one-publication.json")))) {
        assertFalse(crate.hasAttachedData());
      }
    }

    @Test
    @DisplayName("Zip archive")
    public void test01() throws Exception {
      try (RoCrate crate =
          RoCrate.fromZip(
              new ByteArrayInputStream(EndpointTest.zipResource("one-publication.json")))) {
        assertTrue(crate.hasAttachedData());
      }
    }
  }

  @Nested
  @DisplayName("Path validation")
  class PathValidation {
    private static Config config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
    private static final int maxPathLength =
        config.getOptionalValue("rocrate.max-path-length", Integer.class).orElse(4096);
    private static final int maxPathSegmentLength =
        config.getOptionalValue("rocrate.max-path-segment-length", Integer.class).orElse(256);

    @Test
    @DisplayName("Nested entries within the extraction directory are extracted")
    public void test00() throws Exception {
      assertExtractedPaths("data/nested/file.txt", "data/nested/file.txt", "data/nested");
    }

    @Test
    @DisplayName("Entry just below the segment length limit is extracted")
    public void test01() throws Exception {
      String name = "a".repeat(maxPathSegmentLength - 2);
      assertExtractedPaths(name, name);
    }

    @Test
    @DisplayName("Entry escaping the extraction directory with '..' is rejected")
    public void test02() {
      assertIllegalPath("../escaped.txt");
    }

    @Test
    @DisplayName("Entry escaping the extraction directory with a nested '..' is rejected")
    public void test03() {
      assertIllegalPath("data/../../escaped.txt");
    }

    @Test
    @DisplayName("Entry with an absolute path is rejected")
    public void test04() {
      assertIllegalPath("/etc/cron.d/escaped");
    }

    @Test
    @DisplayName("Entry with an oversized path segment is rejected")
    public void test05() {
      assertIllegalPath("a".repeat(maxPathSegmentLength));
    }

    @Test
    @DisplayName("Path segments are measured in bytes, not characters")
    public void test06() {
      assertIllegalPath("é".repeat(maxPathSegmentLength / 2));
    }

    @Test
    @DisplayName("Entry with an oversized path is rejected")
    public void test07() {
      String segment = "a".repeat(maxPathSegmentLength / 2);
      int segmentCount = maxPathLength / (segment.length() + 1) + 2;
      assertIllegalPath(String.join("/", Collections.nCopies(segmentCount, segment)));
    }

    private static byte[] archiveWith(String entryName) {
      return EndpointTest.zipResource(
          "one-publication.json", Map.of(entryName, BigInteger.valueOf(7)));
    }

    private void assertExtractedPaths(String entryName, String... expectedPaths) throws Exception {
      try (RoCrate crate = RoCrate.fromZip(new ByteArrayInputStream(archiveWith(entryName)))) {
        Path base = crate.getBase();
        for (String expected : expectedPaths) {
          assertTrue(
              crate.contains(base.resolve(expected)),
              String.format("Path was not extracted: %s", expected));
        }
      }
    }

    private void assertIllegalPath(String entryName) {
      RuntimeException e =
          assertThrows(
              RuntimeException.class,
              () -> RoCrate.fromZip(new ByteArrayInputStream(archiveWith(entryName))));
      assertTrue(
          e.getMessage().startsWith("Entry with an illegal path:"),
          String.format("Unexpected error message: %s", e.getMessage()));
    }
  }

  @Test
  @DisplayName("Percent encoded @id is not skipped by titanium json-ld")
  public void test00() throws Exception {
    try (RoCrate crate =
        RoCrate.fromMetadata(
"""
{
  "@context": "https://w3id.org/ro/crate/1.1/context",
  "@graph": [
    {
      "@type": "CreativeWork",
      "@id": "ro-crate-metadata.json",
      "conformsTo": { "@id": "https://w3id.org/ro/crate/1.1" },
      "about": { "@id": "./" }
    },
    {
      "@id": "./",
      "@type": "Dataset",
      "hasPart": [ { "@id": "percent%20encoded%20filename" } ]
    },
    {
      "@id": "percent%20encoded%20filename",
      "@type": "Dataset"
    }
  ]
}
""")) {
      assertEquals(3, crate.getModel().listSubjects().toList().size());
    }
  }

  @Nested
  @DisplayName("Root discovery")
  class RootDiscovery {
    @Test
    @DisplayName("The metadata descriptor and the root dataset are identified")
    public void test00() throws Exception {
      try (RoCrate crate =
          RoCrate.fromMetadata(
"""
{
  "@graph": [
    {
      "@id": "ro-crate-metadata.json",
      "@type": "http://schema.org/CreativeWork",
      "http://schema.org/about": { "@id": "./" }
    },
    {
      "@id": "./",
      "@type": "http://schema.org/Dataset"
    }
  ]
}
""")) {
        assertTrue(
            crate.getMetadataDescriptor().getURI().endsWith(RoCrate.METADATA_DESCRIPTOR),
            String.format("Unexpected metadata descriptor: %s", crate.getMetadataDescriptor()));
        assertEquals(crate.getBase().toUri().toString(), crate.getRoot().getURI());
      }
    }

    @Test
    @DisplayName("A crate without a root is rejected")
    public void test01() {
      RoCrateException e = assertThrows(RoCrateException.class, () -> RoCrate.fromMetadata("{ }"));
      assertEquals("Expected exactly one metadata descriptor, but found 0", e.getMessage());
    }
  }
}
