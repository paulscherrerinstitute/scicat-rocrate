package ch.psi.scicat.client.v3;

import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.v3.UserInfos;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient
public interface ScicatServiceV3 extends ScicatService {
  @GET
  @Path("/")
  RestResponse<JsonObject> root();

  @GET
  @Path("/api/v3/Users/userInfos")
  RestResponse<UserInfos> userInfos(@HeaderParam("Authorization") String accessToken);
}
