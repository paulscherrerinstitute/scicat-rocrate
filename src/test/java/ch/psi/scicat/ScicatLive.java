package ch.psi.scicat;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScicatLive
    implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
  // renovate: datasource=github-releases depName=scicatproject/scicatlive
  static final String scicatliveVersion = "4.1.4";
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

  @Override
  public void setIntegrationTestContext(DevServicesContext context) {}

  @Override
  public Map<String, String> start() {
    try {
      Process process = new ProcessBuilder(cmdUp).redirectErrorStream(true).start();
      logProcessOutput(process);
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
      Process process = new ProcessBuilder(cmdDown).redirectErrorStream(true).start();
      logProcessOutput(process);
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        log.warn("scicatlive stop process exited with non-zero code: {}", exitCode);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to cleanly stop scicatlive.", e);
    }
  }

  private void logProcessOutput(Process process) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        log.info("[Docker Compose] {}", line);
      }
    }
  }
}
