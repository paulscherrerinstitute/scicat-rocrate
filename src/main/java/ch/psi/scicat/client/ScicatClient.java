package ch.psi.scicat.client;

import ch.psi.scicat.model.CountResponse;
import ch.psi.scicat.model.CreateDatasetDto;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.UpdatePublishedDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ScicatClient {
  @Inject @RestClient ScicatService scicatService;

  @Inject
  @ConfigProperty(name = "scicat.client.use.bearer")
  boolean preprendBearer;

  private static final Logger logger = LoggerFactory.getLogger(ScicatClient.class);

  public boolean isHealthy() {
    try {
      if (preprendBearer) {
        RestResponse<Void> response = scicatService.isHealthy();
        return response.getStatus() == 200;
      } else {
        RestResponse<JsonObject> response = scicatService.root();
        return response.getStatus() == 200;
      }
    } catch (WebApplicationException e) {
      return false;
    }
  }

  public boolean checkTokenValidity(@HeaderParam("Authorization") String accessToken) {
    try {
      if (preprendBearer) {
        accessToken = "Bearer " + accessToken;

        return scicatService.myself(accessToken).getStatus() == 200;
      } else {
        return scicatService.userInfos(accessToken).getStatus() == 200;
      }
    } catch (WebApplicationException e) {
      return false;
    }
  }

  public RestResponse<Dataset> createDataset(
      @HeaderParam("Authorization") String accessToken, CreateDatasetDto createDatasetDto) {
    RestResponse<Dataset> clientResponse =
        scicatService.createDataset(accessToken, createDatasetDto);
    return RestResponse.fromResponse(clientResponse);
  }

  public RestResponse<PublishedData> getPublishedDataById(String doi) {
    RestResponse<PublishedData> clientResponse = scicatService.getPublishedDataById(doi);
    // Required on backend-next because of this bug:
    // https://github.com/SciCatProject/scicat-backend-next/issues/2036
    if (clientResponse.getStatus() == 200 && !clientResponse.hasEntity()) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
    }
    return RestResponse.fromResponse(clientResponse);
  }

  public RestResponse<PublishedData> updatePublishedData(
      @PathParam("doi") String doi,
      @QueryParam("access_token") String accessToken,
      UpdatePublishedDataDto dto) {

    return scicatService.updatePublishedData(doi, accessToken, dto);
  }

  public RestResponse<PublishedData> createPublishedData(
      CreatePublishedDataDto publishedData, String scicatToken) {
    try {
      logger.debug(new ObjectMapper().writeValueAsString(publishedData));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (preprendBearer) {
      scicatToken = "Bearer " + scicatToken;
    }
    RestResponse<PublishedData> clientResponse =
        scicatService.createPublishedData(scicatToken, publishedData);
    return RestResponse.fromResponse(clientResponse);
  }

  public RestResponse<CountResponse> countPublishedData(
      @QueryParam("filter") String filter, @HeaderParam("Authorization") String accessToken) {
    RestResponse<CountResponse> clientResponse =
        scicatService.countPublishedData(filter, accessToken);
    return RestResponse.fromResponse(clientResponse);
  }

  public RestResponse<Dataset> getDatasetByPid(String pid) {
    RestResponse<Dataset> clientResponse = scicatService.getDatasetByPid(pid);
    return RestResponse.fromResponse(clientResponse);
  }
}
