package ch.psi.scicat.client;

import ch.psi.ord.core.ScicatModelMapper;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import io.quarkus.test.junit.QuarkusTest;
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

@QuarkusTest
public class ScicatServiceMock implements ScicatService {
  // FIXME: inject via annotation
  ModelMapper modelMapper = new ScicatModelMapper().createPublicationModelMapper();
  private List<PublishedData> publishedDataCollection = new ArrayList<>();
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
        publishedDataCollection.stream().filter(p -> p.doi().equals(doi)).findFirst();

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

    if (publishedDataCollection.stream().anyMatch(p -> p.doi().equals(dto.doi()))) {
      throw new WebApplicationException(
          Response.status(Status.CONFLICT).type(MediaType.APPLICATION_JSON).build());
    }

    // FIXME: _id generation is broken when importing a DOI
    // https://github.com/SciCatProject/scicat-backend-next/blob/ba71318d4a16ea872a1bcd7174e5896ce5b0d197/src/published-data/schemas/published-data.schema.ts#L19-L25
    PublishedData created = modelMapper.map(dto, PublishedData.class);

    // FIXME: Others attributes should probably be set but they are currently not in
    // backend-next (scicatUser/createdBy/updatedBy/url?)
    String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    created.createdAt(now);
    created.updatedAt(now);
    publishedDataCollection.add(created);
    return RestResponse.status(Status.CREATED, created);
  }

  @Override
  public RestResponse<Dataset> getDatasetByPid(String pid) {
    throw new UnsupportedOperationException("Unimplemented method 'getDatasetByPid'");
  }

  public void setHealthy(boolean b) {
    isHealthy = b;
  }

  public void setAuthenticated(boolean b) {
    isAuthenticated = b;
  }
}
