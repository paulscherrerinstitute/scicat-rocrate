package ch.psi.ord.api;

import ch.psi.ord.core.ZenodoExporter;
import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.PublishedData;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

@Path("zenodo")
public class ZenodoController {
  @Inject ZenodoExporter exporter;

  @Inject ScicatClient scicatClient;

  public ZenodoController(ZenodoExporter exporter) {
    this.exporter = exporter;
  }

  @GET
  @Path("/{doi}/export")
  @Produces(ExtraMediaType.APPLICATION_JSONLD)
  public Response exportPublication(@PathParam("doi") String doi) {
    try {
      RestResponse<PublishedData> publication = scicatClient.getPublishedDataById(doi);
      return Response.ok(exporter.toZenodoJsonLd(publication.getEntity())).build();
    } catch (WebApplicationException e) {
      Response res = e.getResponse();
      return Response.status(res.getStatus())
          .entity(res.getEntity())
          .type(res.getMediaType())
          .build();
    }
  }
}
