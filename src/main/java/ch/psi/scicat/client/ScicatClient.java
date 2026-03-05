package ch.psi.scicat.client;

import ch.psi.scicat.model.v3.CountResponse;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.MyIdentity;
import ch.psi.scicat.model.v3.PublishedData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class ScicatClient {
  @RestClient @Inject ScicatService api;

  public boolean isHealthy() {
    try {
      return api.health().getStatus() == 200;
    } catch (WebApplicationException e) {
      return false;
    }
  }

  public boolean checkTokenValidity(String accessToken) {
    try {
      return api.myidentity(accessToken).getStatus() == 200;
    } catch (WebApplicationException e) {
      return false;
    }
  }

  public RestResponse<MyIdentity> myidentity(String accessToken) {
    return api.myidentity(accessToken);
  }

  public RestResponse<Dataset> createDataset(String accessToken, CreateDatasetDto datasetDto) {
    return api.createDataset(accessToken, datasetDto);
  }

  public RestResponse<PublishedData> getPublishedDataById(String doi) {
    return api.getPublishedDataById(doi);
  }

  public RestResponse<Void> registerPublishedData(String doi, String accessToken) {
    return api.registerPublishedData(doi, accessToken);
  }

  public RestResponse<CountResponse> countPublishedData(String where, String accessToken) {
    return api.countPublishedData(where, accessToken);
  }

  public RestResponse<PublishedData> createPublishedData(
      String accessToken, CreatePublishedDataDto publishedData) {
    return api.createPublishedData(accessToken, publishedData);
  }

  public RestResponse<Dataset> getDatasetByPid(String pid) {
    return api.getDatasetByPid(pid);
  }
}
