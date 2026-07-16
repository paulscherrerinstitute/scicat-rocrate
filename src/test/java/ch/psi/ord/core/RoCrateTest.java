package ch.psi.ord.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.ord.api.EndpointTest;
import io.quarkus.test.junit.QuarkusTest;
import java.io.ByteArrayInputStream;
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
          RoCrate.fromMetadata(new ByteArrayInputStream(EndpointTest.getResource("empty.json")))) {
        assertFalse(crate.hasAttachedData());
      }
    }

    @Test
    @DisplayName("Zip archive")
    public void test01() throws Exception {
      try (RoCrate crate =
          RoCrate.fromZip(new ByteArrayInputStream(EndpointTest.zipResource("empty.json")))) {
        assertTrue(crate.hasAttachedData());
      }
    }
  }
}
