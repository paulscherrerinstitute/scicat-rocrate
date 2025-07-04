package ch.psi.scicat.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import ch.psi.scicat.ScicatClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@Readiness
@ApplicationScoped
public class ScicatConnectionHealthCheck implements HealthCheck {
    @Inject
    ScicatClient scicatClient;

    @Override
    public HealthCheckResponse call() {
        try {
            scicatClient.isHealthy();
            return HealthCheckResponse.up("scicat-api");
        } catch (WebApplicationException e) {
            return HealthCheckResponse.down("scicat-api");
        }
    }
}
