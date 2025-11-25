package ch.psi.scicat.client.v4;

import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.compat.UserDetails;
import ch.psi.scicat.model.v3.CountResponse;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.MyIdentity;
import ch.psi.scicat.model.v3.PublishedData;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;

public class ScicatClientV4 extends ScicatClient {
  ScicatServiceV4 api;

  @Inject
  public ScicatClientV4(ScicatService api) {
    this.api = (ScicatServiceV4) api;
  }

  @Override
  public boolean isHealthy() {
    try {
      return api.health().getStatus() == 200;
    } catch (WebApplicationException e) {
      return false;
    }
  }

  @Override
  public boolean checkTokenValidity(String accessToken) {
    try {
      return api.myself(accessToken).getStatus() == 200;
    } catch (WebApplicationException e) {
      return false;
    }
  }

  @Override
  public UserDetails userDetails(String accessToken) {
    MyIdentity myself = api.myself(accessToken).getEntity();

    return new UserDetails()
        .setUsername(myself.getProfile().getUsername())
        .setGroups(myself.getProfile().getAccessGroups())
        .setEmail(myself.getProfile().getEmail());
  }

  @Override
  public RestResponse<Dataset> createDataset(String accessToken, CreateDatasetDto datasetDto) {
    return api.createDataset(accessToken, datasetDto);
  }

  @Override
  public RestResponse<PublishedData> getPublishedDataById(String doi) {
    return api.getPublishedDataById(doi);
  }

  @Override
  public RestResponse<PublishedData> registerPublishedData(String doi, String accessToken) {
    return api.registerPublishedData(doi, accessToken);
  }

  @Override
  public RestResponse<CountResponse> countPublishedData(String where, String accessToken) {
    return api.countPublishedData(where, accessToken);
  }

  @Override
  public RestResponse<PublishedData> createPublishedData(
      String accessToken, CreatePublishedDataDto publishedData) {
    return api.createPublishedData(accessToken, publishedData);
  }

  @Override
  public RestResponse<Dataset> getDatasetByPid(String pid) {
    return api.getDatasetByPid(pid);
  }
}
