package ch.psi.scicat.client.v4;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CountRequestFilter implements ClientRequestFilter {
  @Override
  public void filter(ClientRequestContext requestContext) {
    if (requestContext.getUri().getPath().endsWith("count")) {
      requestContext.setUri(
          UriBuilder.fromUri(requestContext.getUri())
              .replaceQuery(requestContext.getUri().getRawQuery().replace("where=", "filter="))
              .build());
    }
  }
}
