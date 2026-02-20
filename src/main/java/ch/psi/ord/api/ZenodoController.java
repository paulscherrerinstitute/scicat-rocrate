package ch.psi.ord.api;

import ch.psi.ord.core.ZenodoExporter;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("zenodo")
public class ZenodoController {
  @Inject ZenodoExporter exporter;

  @GET
  @Path("/{doi}/export")
  @Produces(ExtraMediaType.APPLICATION_JSONLD)
  public Response exportPublication(@PathParam("doi") String doi) throws Exception {
    try {
      return Response.ok(exporter.exportDoi(doi)).build();
    } catch (WebApplicationException e) {
      Response res = e.getResponse();
      return Response.status(res.getStatus())
          .entity(res.getEntity())
          .type(res.getMediaType())
          .build();
    }
  }
}
