package ch.psi.ord.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/")
public class RootController {
  @ConfigProperty(name = "quarkus.swagger-ui.path", defaultValue = "swagger-ui")
  String swaggerUiPath;

  @GET
  @Operation(hidden = true)
  public Response redirectToSwaggerUi() {
    return Response.seeOther(URI.create(swaggerUiPath)).build();
  }
}
