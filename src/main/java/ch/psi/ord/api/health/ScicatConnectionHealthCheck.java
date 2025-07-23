package ch.psi.ord.api.health;

import ch.psi.scicat.client.ScicatClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ScicatConnectionHealthCheck implements HealthCheck {
  @Inject ScicatClient scicatClient;

  @Override
  public HealthCheckResponse call() {
    if (scicatClient.isHealthy()) {
      return HealthCheckResponse.up("scicat-api");
    }
    return HealthCheckResponse.down("scicat-api");
  }
}
