package ch.psi.scicat.client;

import ch.psi.scicat.TestData;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.modelmapper.ModelMapper;

public class ScicatServiceMock implements ScicatService {
  private ModelMapper modelMapper = new ModelMapper();
  private List<PublishedData> publishedDataCollection = new ArrayList<>();
  private List<Dataset> datasetCollection =
      List.of(TestData.dataset1, TestData.dataset2, TestData.dataset3);
  private boolean isHealthy = true;
  private boolean isAuthenticated = false;

  @Override
  public RestResponse<Void> isHealthy() {
    if (isHealthy) return RestResponse.ok();

    throw new WebApplicationException("Mocked unhealthy SciCat");
  }

  @Override
  public RestResponse<PublishedData> getPublishedDataById(String doi) {
    Optional<PublishedData> publishedData =
        publishedDataCollection.stream().filter(p -> p.getDoi().equals(doi)).findFirst();

    if (publishedData.isPresent()) {
      return RestResponse.ok(publishedData.get(), MediaType.APPLICATION_JSON);
    }

    throw new WebApplicationException(
        Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build());
  }

  @Override
  public RestResponse<PublishedData> createPublishedData(
      String accessToken, CreatePublishedDataDto dto) {
    if (!isAuthenticated) {
      throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
    }

    if (publishedDataCollection.stream().anyMatch(p -> p.getDoi().equals(dto.getDoi()))) {
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
    publishedDataCollection.add(created);
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

  public void setHealthy(boolean b) {
    isHealthy = b;
  }

  public void setAuthenticated(boolean b) {
    isAuthenticated = b;
  }
}
