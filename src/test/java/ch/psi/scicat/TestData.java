package ch.psi.scicat;

import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.PublishedDataStatus;
import java.time.Instant;
import java.util.List;
import org.modelmapper.ModelMapper;

public class TestData {
  public static final String validDoi = "10.000/abc";
  public static PublishedData examplePublishedData;
  public static CreatePublishedDataDto exampleCreatePublishedDataDto;
  public static Dataset dataset1;
  public static Dataset dataset2;
  public static Dataset dataset3;

  static {
    examplePublishedData =
        new PublishedData()
            .setDoi("10.16907/publication-1")
            .setAffiliation("PSI")
            .setCreator(List.of("creator-1", "creator-2", "creator-3"))
            .setPublisher("PSI")
            .setPublicationYear(2025)
            .setTitle("Publication 1 - Title")
            .setUrl("https://doi.org/10.16907/publication-1")
            .setAbstract("Publication 1 - Abstract")
            .setDataDescription("Publication 1 - Data description")
            .setResourceType("derived")
            .setNumberOfFiles(100)
            .setSizeOfArchive(100)
            .setPidArray(
                List.of(
                    "20.500.11935/dataset-1", "20.500.11935/dataset-2", "20.500.11935/dataset-3"))
            .setAuthors(List.of("author-1", "author-2"))
            .setRegisteredTime(Instant.parse("2025-05-23T07:29:25.475Z"))
            .setStatus(PublishedDataStatus.REGISTERED)
            .setScicatUser("creator-1")
            .setRelatedPublications(List.of())
            .setDownloadLink("")
            .setCreatedBy("creator-1")
            .setCreatedAt("2025-02-02T06:09:49.220Z")
            .setUpdatedAt("2025-02-02T06:09:49.225Z")
            .setUpdatedBy("creator-1");

    dataset1 =
        new Dataset()
            .setPid("20.500.11935/dataset-1")
            .setDatasetName("dataset-1")
            .setDescription("Dataset 1");
    dataset2 =
        new Dataset()
            .setPid("20.500.11935/dataset-2")
            .setDatasetName("dataset-2")
            .setDescription("Dataset 2");
    dataset3 =
        new Dataset()
            .setPid("20.500.11935/dataset-3")
            .setDatasetName("dataset-3")
            .setDescription("Dataset 3");

    exampleCreatePublishedDataDto =
        new ModelMapper().map(examplePublishedData, CreatePublishedDataDto.class);
  }
}
