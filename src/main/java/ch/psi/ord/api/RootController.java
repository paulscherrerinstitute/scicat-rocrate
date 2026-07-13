package ch.psi.ord.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class RootController {
  @GET
  public Response redirectToSwaggerUi() {
    return Response.seeOther(URI.create("/explorer")).build();
  }
}
