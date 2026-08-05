package ch.psi.ord.api.health;

import ch.psi.scicat.client.ScicatService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Readiness
@ApplicationScoped
public class ScicatConnectionHealthCheck implements HealthCheck {
  @RestClient @Inject ScicatService scicatService;

  @Override
  public HealthCheckResponse call() {
    if (isHealthy()) {
      return HealthCheckResponse.up("scicat-api");
    }
    return HealthCheckResponse.down("scicat-api");
  }

  private boolean isHealthy() {
    try {
      return scicatService.health().getStatus() == 200;
    } catch (WebApplicationException e) {
      return false;
    }
  }
}
