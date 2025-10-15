package ch.psi.scicat.client;

import ch.psi.scicat.TestData;
import ch.psi.scicat.model.CountResponse;
import ch.psi.scicat.model.CreateDatasetDto;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.UpdatePublishedDataDto;
import jakarta.json.JsonObject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Setter;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.modelmapper.ModelMapper;

public class ScicatServiceMock implements ScicatService {
  private ModelMapper modelMapper = new ModelMapper();
  private Map<String, PublishedData> publishedDataCollection = new HashMap<>();
  private List<Dataset> datasetCollection =
      List.of(TestData.dataset1, TestData.dataset2, TestData.dataset3);
  @Setter private boolean isHealthy = true;
  @Setter private boolean isAuthenticated = false;
  @Setter private int publicationCount = 0;

  @Override
  public RestResponse<JsonObject> root() {
    if (isHealthy) return RestResponse.ok();

    throw new WebApplicationException("Mocked unhealthy SciCat");
  }

  @Override
  public RestResponse<Void> isHealthy() {
    if (isHealthy) return RestResponse.ok();

    throw new WebApplicationException("Mocked unhealthy SciCat");
  }

  @Override
  public RestResponse<PublishedData> getPublishedDataById(String doi) {
    PublishedData publishedData = publishedDataCollection.get(doi);

    if (publishedData == null) {
      throw new WebApplicationException(
          Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build());
    }

    return RestResponse.ok(publishedData, MediaType.APPLICATION_JSON);
  }

  @Override
  public RestResponse<PublishedData> createPublishedData(
      String accessToken, CreatePublishedDataDto dto) {
    if (!isAuthenticated) {
      throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
    }

    PublishedData publishedData = publishedDataCollection.get(dto.getDoi());
    if (publishedData != null) {
      throw new WebApplicationException(
          Response.status(Status.CONFLICT).type(MediaType.APPLICATION_JSON).build());
    }

    // FIXME: _id generation is broken when importing a DOI
    // https://github.com/SciCatProject/scicat-backend-next/blob/ba71318d4a16ea872a1bcd7174e5896ce5b0d197/src/published-data/schemas/published-data.schema.ts#L19-L25
    PublishedData created = modelMapper.map(dto, PublishedData.class);

    // FIXME: Others attributes should probably be set but they are currently not in
    // backend-next (scicatUser/createdBy/updatedBy/url?)
    String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    created.setCreatedAt(now);
    created.setUpdatedAt(now);
    publishedDataCollection.put(created.getDoi(), created);
    return RestResponse.status(Status.CREATED, created);
  }

  @Override
  public RestResponse<Dataset> getDatasetByPid(String pid) {
    Optional<Dataset> dataset =
        datasetCollection.stream().filter(p -> p.getPid().equals(pid)).findFirst();

    if (dataset.isPresent()) {
      return RestResponse.ok(dataset.get(), MediaType.APPLICATION_JSON);
    }

    throw new WebApplicationException(
        Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build());
  }

  @Override
  public RestResponse<Void> myself(String accessToken) {
    if (isAuthenticated) {
      return RestResponse.ok();
    } else {
      return RestResponse.status(Status.UNAUTHORIZED);
    }
  }

  @Override
  public RestResponse<Void> userInfos(String accessToken) {
    if (isAuthenticated) {
      return RestResponse.ok();
    } else {
      return RestResponse.status(Status.UNAUTHORIZED);
    }
  }

  @Override
  public RestResponse<Dataset> createDataset(String accessToken, CreateDatasetDto datasetDto) {
    return RestResponse.status(Status.CREATED, new Dataset().setPid(UUID.randomUUID().toString()));
  }

  @Override
  public RestResponse<CountResponse> countPublishedData(String filter, String accessToken) {
    return RestResponse.ok(new CountResponse().setCount(publicationCount));
  }

  @Override
  public RestResponse<PublishedData> updatePublishedData(
      String doi, String accessToken, UpdatePublishedDataDto dto) {
    if (!isAuthenticated) {
      return RestResponse.status(Status.UNAUTHORIZED);
    }

    PublishedData publishedData = publishedDataCollection.get(doi);
    if (publishedData == null) {
      throw new WebApplicationException(
          Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build());
    }

    publishedData.setStatus(dto.getStatus());

    return RestResponse.ok(publishedData);
  }
}
