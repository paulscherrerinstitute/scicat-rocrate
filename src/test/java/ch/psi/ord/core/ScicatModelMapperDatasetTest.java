package ch.psi.ord.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.psi.ord.model.Dataset;
import ch.psi.ord.model.Person;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.DatasetType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

@QuarkusTest
public class ScicatModelMapperDatasetTest {
  @Inject ModelMapper modelMapper;

  private static Dataset dataset() {
    return new Dataset().setCreator(List.of());
  }

  private CreateDatasetDto map(Dataset dataset) {
    return modelMapper.map(dataset, CreateDatasetDto.class);
  }

  @Nested
  @DisplayName("datasetName")
  class DatasetName {
    @Test
    @DisplayName("The name of the dataset is mapped")
    public void test00() {
      assertEquals("Minimal dataset", map(dataset().setName("Minimal dataset")).getDatasetName());
    }
  }

  @Nested
  @DisplayName("description")
  class Description {
    @Test
    @DisplayName("The description of the dataset is mapped")
    public void test00() {
      assertEquals(
          "The description of the dataset",
          map(dataset().setDescription("The description of the dataset")).getDescription());
    }

    @Test
    @DisplayName("A dataset without a description")
    public void test01() {
      assertNull(map(dataset()).getDescription());
    }
  }

  @Nested
  @DisplayName("type")
  class Type {
    @Test
    @DisplayName("Datasets are imported as 'base' datasets")
    public void test00() {
      assertEquals(DatasetType.BASE, map(dataset()).getType());
    }
  }

  @Nested
  @DisplayName("sourceFolder")
  class SourceFolder {
    @Test
    @DisplayName("The path of an absolute identifier is used")
    public void test00() {
      assertEquals(
          "/data/ds1/",
          map(dataset().setResourceIdentifier("https://example.org/data/ds1/")).getSourceFolder());
    }

    @Test
    @DisplayName("A relative identifier is used as is")
    public void test01() {
      assertEquals(
          "data/ds1/", map(dataset().setResourceIdentifier("data/ds1/")).getSourceFolder());
    }

    @Test
    @DisplayName("An identifier that is not a valid URI is used as is")
    public void test02() {
      assertEquals(
          "data/with space/",
          map(dataset().setResourceIdentifier("data/with space/")).getSourceFolder());
    }

    @Test
    @DisplayName("The path of a remote identifier is used")
    public void test03() {
      assertEquals(
          "/das/work/p18/p18844/ds1",
          map(dataset().setResourceIdentifier("nfs://ra.psi.ch/das/work/p18/p18844/ds1"))
              .getSourceFolder());
    }
  }

  @Nested
  @DisplayName("sourceFolderHost")
  class SourceFolderHost {
    @Test
    @DisplayName("The host of a remote identifier is used")
    public void test00() {
      assertEquals(
          "ra.psi.ch",
          map(dataset().setResourceIdentifier("nfs://ra.psi.ch/das/work/p18/p18844/ds1"))
              .getSourceFolderHost());
    }

    @Test
    @DisplayName("An identifier without a host has no source folder host")
    public void test01() {
      assertNull(map(dataset().setResourceIdentifier("data/ds1/")).getSourceFolderHost());
    }

    @Test
    @DisplayName("An identifier that is not a valid URI has no source folder host")
    public void test02() {
      assertEquals(
          "", map(dataset().setResourceIdentifier("data/with space/")).getSourceFolderHost());
    }

    @Test
    @DisplayName("A dataset without an identifier has no source folder host")
    public void test03() {
      assertNull(map(dataset()).getSourceFolderHost());
    }
  }

  @Nested
  @DisplayName("contactEmail")
  class ContactEmail {
    @Test
    @DisplayName("The email of the creator is mapped")
    public void test00() {
      Dataset dataset =
          dataset().setCreator(List.of(new Person().setEmail("doru.constantin@example.com")));

      assertEquals("doru.constantin@example.com", map(dataset).getContactEmail());
    }

    @Test
    @DisplayName("The emails of several creators are joined")
    public void test01() {
      Dataset dataset =
          dataset()
              .setCreator(
                  List.of(
                      new Person().setEmail("doru.constantin@example.com"),
                      new Person().setEmail("maik.kahnt@example.com")));

      assertEquals(
          "doru.constantin@example.com; maik.kahnt@example.com", map(dataset).getContactEmail());
    }
  }
}
