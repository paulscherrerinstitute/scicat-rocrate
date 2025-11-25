package ch.psi.scicat.client.v4;

import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.v3.MyIdentity;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient
@RegisterProvider(BearerRequestFilter.class)
public interface ScicatServiceV4 extends ScicatService {
  @GET
  @Path("/api/v3/health")
  RestResponse<Void> health();

  @GET
  @Path("/api/v3/users/my/identity")
  RestResponse<MyIdentity> myidentity(@HeaderParam("Authorization") String accessToken);
}
