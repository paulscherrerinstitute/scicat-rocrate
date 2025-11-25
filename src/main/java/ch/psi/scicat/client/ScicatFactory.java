package ch.psi.scicat.client;

import ch.psi.scicat.client.v3.ScicatClientV3;
import ch.psi.scicat.client.v3.ScicatServiceV3;
import ch.psi.scicat.client.v4.ScicatClientV4;
import ch.psi.scicat.client.v4.ScicatServiceV4;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
public class ScicatFactory {
  @Inject
  @ConfigProperty(name = "scicat.v4")
  boolean backendV4;

  @Inject
  @ConfigProperty(name = "scicat.url")
  String url;

  @Produces
  @ApplicationScoped
  public ScicatService createScicatService() {
    QuarkusRestClientBuilder builder =
        QuarkusRestClientBuilder.newBuilder().baseUri(URI.create(url));

    if (backendV4) {
      log.info("Configured to run against scicat-backend-next");
      return builder.build(ScicatServiceV4.class);
    }

    log.info("Configured to run against legacy backend");
    return builder.build(ScicatServiceV3.class);
  }

  @Produces
  @ApplicationScoped
  public ScicatClient createScicatClient() {
    return backendV4 ? new ScicatClientV4() : new ScicatClientV3();
  }
}
