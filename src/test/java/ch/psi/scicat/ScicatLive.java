package ch.psi.scicat;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScicatLive
    implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
  // renovate: datasource=github-releases depName=scicatproject/scicatlive
  static final String scicatliveVersion = "4.1.0";
  static final String[] cmd = {
    "docker",
    "compose",
    "-f",
    String.format("oci://ghcr.io/scicatproject/scicatlive:%s-full", scicatliveVersion),
    "-f",
    "src/test/resources/scicatlive/compose.override.yaml",
  };

  static final List<String> cmdUp =
      Stream.concat(
              Arrays.stream(cmd),
              Arrays.stream(new String[] {"up", "--wait", "--wait-timeout", "120"}))
          .collect(Collectors.toList());

  static final List<String> cmdDown =
      Stream.concat(Arrays.stream(cmd), Arrays.stream(new String[] {"down", "--volumes"}))
          .collect(Collectors.toList());

  ProcessBuilder processBuilder = new ProcessBuilder();

  @Override
  public void setIntegrationTestContext(DevServicesContext context) {}

  @Override
  public Map<String, String> start() {
    try {
      Process process = new ProcessBuilder(cmdUp).inheritIO().start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new RuntimeException("scicatlive failed to start. Exit code: " + exitCode);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("scicatlive failed to start.", e);
    }
    return Collections.emptyMap();
  }

  @Override
  public void stop() {
    try {
      new ProcessBuilder(cmdDown).inheritIO().start().waitFor();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to cleanly stop scicatlive.", e);
    }
  }
}
