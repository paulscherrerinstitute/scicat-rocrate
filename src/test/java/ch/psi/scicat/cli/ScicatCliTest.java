package ch.psi.scicat.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
public class ScicatCliTest {
  @InjectSpy ScicatCli scicatCli;

  @ParameterizedTest(name = "should throw if scicat.cli.path={0} is not a file or not executable")
  @ValueSource(strings = {"non-existing", "/usr", "/etc/localtime"})
  public void test00(String cliPath) {
    assertThrows(IllegalStateException.class, () -> new ScicatCli(cliPath, null, null, null));
  }
}
