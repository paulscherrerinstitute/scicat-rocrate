package ch.psi.scicat;

import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.PublishedData;
import java.util.List;
import org.modelmapper.ModelMapper;

public class TestData {
  public static final String validDoi = "10.000/abc";
  public static PublishedData examplePublishedData;
  public static CreatePublishedDataDto exampleCreatePublishedDataDto;

  static {
    examplePublishedData =
        new PublishedData()
            .doi("10.16907/publication-1")
            .affiliation("PSI")
            .creator(List.of("creator-1", "creator-2", "creator-3"))
            .publisher("PSI")
            .publicationYear(2025)
            .title("Publication 1 - Title")
            .url("https://doi.org/10.16907/publication-1")
            .abstract_("Publication 1 - Abstract")
            .dataDescription("Publication 1 - Data description")
            .resourceType("derived")
            .numberOfFiles(100)
            .sizeOfArchive(100)
            .pidArray(
                List.of(
                    "20.500.11935/dataset-1", "20.500.11935/dataset-2", "20.500.11935/dataset-3"))
            .authors(List.of("author-1", "author-2"))
            .registeredTime("2025-05-23T07:29:25.475Z")
            .status("registered")
            .scicatUser("creator-1")
            .relatedPublications(List.of())
            .downloadLink("")
            .createdBy("creator-1")
            .createdAt("2025-02-02T06:09:49.220Z")
            .updatedAt("2025-02-02T06:09:49.225Z")
            .updatedBy("creator-1");

    exampleCreatePublishedDataDto =
        new ModelMapper().map(examplePublishedData, CreatePublishedDataDto.class);
  }
}
