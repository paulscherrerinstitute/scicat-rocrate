package ch.psi.scicat;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@RegisterRestClient(configKey = "scicat-api")
public interface ScicatService {
    @GET
    @Path("health")
    RestResponse<Void> isHealthy();

    @GET
    @Path("publisheddata/{doi}")
    RestResponse<PublishedData> getPublishedDataById(@PathParam("doi") String doi);

    @POST
    @Path("publisheddata")
    RestResponse<PublishedData> createPublishedData(
            @HeaderParam("Authorization") String accessToken,
            PublishedData publishedData);

    @GET
    @Path("datasets/{pid}")
    RestResponse<Dataset> getDatasetByPid(@PathParam("pid") String pid);
}
