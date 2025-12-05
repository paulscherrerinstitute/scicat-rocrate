package ch.psi.scicat.client;

import ch.psi.scicat.model.v3.CountResponse;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.PublishedData;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient(configKey = "scicat")
public interface ScicatService {
  @GET
  @Path("/api/v3/datasets/{pid}")
  public RestResponse<Dataset> getDatasetByPid(@PathParam("pid") String pid);

  @POST
  @Path("/api/v3/datasets")
  public RestResponse<Dataset> createDataset(
      @HeaderParam("Authorization") String accessToken, CreateDatasetDto datasetDto);

  @GET
  @Path("/api/v3/publisheddata/{doi}")
  public RestResponse<PublishedData> getPublishedDataById(@PathParam("doi") String doi);

  @POST
  @Path("/api/v3/publisheddata/{doi}/register")
  public RestResponse<Void> registerPublishedData(
      @PathParam("doi") String doi, @HeaderParam("Authorization") String accessToken);

  @GET
  @Path("/api/v3/publisheddata/count")
  public RestResponse<CountResponse> countPublishedData(
      @QueryParam("where") String where, @HeaderParam("Authorization") String accessToken);

  @POST
  @Path("/api/v3/publisheddata")
  public RestResponse<PublishedData> createPublishedData(
      @HeaderParam("Authorization") String accessToken, CreatePublishedDataDto publishedData);
}
