package ch.psi.scicat.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
public class BearerRequestFilter implements ClientRequestFilter {
  @Inject
  @ConfigProperty(name = "scicat.client.backend-v4")
  boolean backendV4;

  private static final String AUTH_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer";

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    if (backendV4 && requestContext.getHeaders().containsKey(AUTH_HEADER)) {
      log.debug(
          "Preprending 'Bearer ' to 'Authorization' header for {} {}",
          requestContext.getMethod(),
          requestContext.getUri());

      List<Object> originalAuth = requestContext.getHeaders().get(AUTH_HEADER);
      if (originalAuth.size() > 1) {
        log.warn("'Authorization' header has {} values", originalAuth.size());
      }

      List<Object> updatedAuth =
          originalAuth.stream()
              .filter(h -> !h.toString().startsWith(BEARER_PREFIX))
              .map(h -> String.format("%s %s", BEARER_PREFIX, h))
              .collect(Collectors.toList());

      requestContext.getHeaders().put(AUTH_HEADER, updatedAuth);
    }
  }
}
