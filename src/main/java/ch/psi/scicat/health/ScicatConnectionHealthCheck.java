package ch.psi.scicat.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import ch.psi.scicat.ScicatClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Readiness
@ApplicationScoped
public class ScicatConnectionHealthCheck implements HealthCheck {
    @Inject
    ScicatClient scicatClient;

    @Override
    public HealthCheckResponse call() {
        if (scicatClient.isHealthy()) {
            return HealthCheckResponse.up("scicat-api");
        }
        return HealthCheckResponse.down("scicat-api");
    }
}
