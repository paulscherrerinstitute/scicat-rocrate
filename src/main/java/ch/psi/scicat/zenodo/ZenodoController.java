package ch.psi.scicat.zenodo;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("zenodo")
public class ZenodoController {
    @Inject
    ZenodoExporter exporter;

    public ZenodoController(ZenodoExporter exporter) {
        this.exporter = exporter;
    }

    @GET
    @Path("/{doi}/export")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportPublication(@PathParam("doi") String doi) {
        return Response.ok(exporter.exportPublication(doi).toString()).build();
    }
}
