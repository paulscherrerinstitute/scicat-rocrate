package ch.psi.scicat.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class BearerRequestFilter implements ClientRequestFilter {
  @Inject
  @ConfigProperty(name = "scicat.client.backend-v4")
  boolean backendV4;

  private static final String AUTH_HEADER = "Authorization";

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    if (backendV4 && requestContext.getHeaders().containsKey(AUTH_HEADER)) {
      requestContext
          .getHeaders()
          .computeIfPresent(AUTH_HEADER, (k, v) -> List.of(String.format("Bearer %", v)));
    }
  }
}
