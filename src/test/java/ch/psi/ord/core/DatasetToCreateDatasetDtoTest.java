package ch.psi.ord.core;

import ch.psi.ord.model.Dataset;
import ch.psi.ord.model.Person;
import ch.psi.ord.model.ScicatRelationship;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreateRelationshipDto;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.modelmapper.ModelMapper;

public class DatasetToCreateDatasetDtoTest {
  private final ModelMapper mapper = new ScicatModelMapper().createPublicationModelMapper();

  private CreateDatasetDto map(Dataset dataset) {
    return mapper.map(dataset, CreateDatasetDto.class);
  }

  private static Dataset dataset() {
    return new Dataset()
        .setResourceIdentifier("file:///crate/data/ds1/")
        .setName("A dataset")
        .setCreator(List.of(new Person().setEmail("jane.doe@example.com")));
  }

  @Test
  @DisplayName("Relationships")
  public void test00() {
    Dataset dataset =
        dataset()
            .setRelationships(
                List.of(
                    new ScicatRelationship()
                        .setIdentifier("PID.SAMPLE.PREFIX/psi_ds3")
                        .setIdentifierType("Handle")
                        .setRelationship("IsDerivedFrom")
                        .setEntityType("Dataset")
                        .setExternalId("external-1"),
                    new ScicatRelationship()
                        .setIdentifier("10.1364/oe.23.027975")
                        .setIdentifierType("DOI")
                        .setRelationship("References")));

    CreateDatasetDto dto = map(dataset);

    Assertions.assertEquals(
        List.of(
            new CreateRelationshipDto()
                .setIdentifier("PID.SAMPLE.PREFIX/psi_ds3")
                .setIdentifierType("Handle")
                .setRelationship("IsDerivedFrom")
                .setEntityType("Dataset")
                .setExternalId("external-1"),
            new CreateRelationshipDto()
                .setIdentifier("10.1364/oe.23.027975")
                .setIdentifierType("DOI")
                .setRelationship("References")),
        dto.getRelationships());
  }

  @ParameterizedTest
  @DisplayName("No relationship")
  @NullAndEmptySource
  public void test01(List<ScicatRelationship> relationships) {
    CreateDatasetDto dto = map(dataset().setRelationships(relationships));

    Assertions.assertTrue(dto.getRelationships() == null || dto.getRelationships().isEmpty());
  }

  @Test
  @DisplayName("Other fields are still mapped")
  public void test02() {
    CreateDatasetDto dto = map(dataset());

    Assertions.assertEquals("A dataset", dto.getDatasetName());
    Assertions.assertEquals("/crate/data/ds1/", dto.getSourceFolder());
    Assertions.assertEquals("jane.doe@example.com", dto.getContactEmail());
  }
}
