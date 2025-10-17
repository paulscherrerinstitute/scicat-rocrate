package ch.psi.scicat.client;

import ch.psi.scicat.model.CountResponse;
import ch.psi.scicat.model.CreateDatasetDto;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.UserInfos;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient(configKey = "scicat-api")
public interface ScicatService {
  /** Only available on legacy backend */
  @GET
  @Path("/")
  RestResponse<JsonObject> root();

  @GET
  @Path("/api/v3/health")
  RestResponse<Void> isHealthy();

  /** Only available on backend-next */
  @GET
  @Path("/api/v3/users/my/self")
  RestResponse<Void> myself(@HeaderParam("Authorization") String accessToken);

  /** Only available on legacy backend */
  @GET
  @Path("/api/v3/Users/userInfos")
  RestResponse<UserInfos> userInfos(@HeaderParam("Authorization") String accessToken);

  @POST
  @Path("/api/v3/datasets")
  RestResponse<Dataset> createDataset(
      @HeaderParam("Authorization") String accessToken, CreateDatasetDto datasetDto);

  @GET
  @Path("/api/v3/publisheddata/{doi}")
  RestResponse<PublishedData> getPublishedDataById(@PathParam("doi") String doi);

  @POST
  @Path("/api/v3/publisheddata/{doi}/resync")
  RestResponse<PublishedData> updatePublishedData(
      @PathParam("doi") String doi,
      @QueryParam("access_token") String accessToken,
      PublishedData publishedData);

  @GET
  @Path("/api/v3/publisheddata/count")
  RestResponse<CountResponse> countPublishedData(
      @QueryParam("where") String where, @HeaderParam("Authorization") String accessToken);

  @POST
  @Path("/api/v3/publisheddata")
  RestResponse<PublishedData> createPublishedData(
      @HeaderParam("Authorization") String accessToken, CreatePublishedDataDto publishedData);

  @GET
  @Path("/api/v3/datasets/{pid}")
  RestResponse<Dataset> getDatasetByPid(@PathParam("pid") String pid);
}
