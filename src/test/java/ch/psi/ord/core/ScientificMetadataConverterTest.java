package ch.psi.ord.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.ord.model.Dataset;
import ch.psi.ord.model.PropertyValue;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

@QuarkusTest
public class ScientificMetadataConverterTest {
  private static final ObjectMapper jsonMapper = new ObjectMapper();

  @Inject ModelMapper modelMapper;

  @Test
  @DisplayName("Dataset without measured variables")
  public void test00() {
    Dataset dataset = new Dataset().setCreator(List.of());

    assertNull(modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());

    dataset.setVariableMeasured(List.of());
    assertTrue(modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata().isEmpty());
  }

  @Test
  @DisplayName("String value")
  public void test01() throws JsonProcessingException {
    Dataset dataset =
        new Dataset()
            .setCreator(List.of())
            .setVariableMeasured(
                List.of(new PropertyValue().setName("beamline").setValue("X12SA")));

    assertEquals(
        jsonMapper.readTree(
            """
            { "beamline": "X12SA" }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }

  @Test
  @DisplayName("Value with a unit")
  public void test02() throws JsonProcessingException {
    Dataset dataset =
        new Dataset()
            .setCreator(List.of())
            .setVariableMeasured(
                List.of(
                    new PropertyValue().setName("temperature").setValue("300").setUnitText("K")));

    assertEquals(
        jsonMapper.readTree(
            """
            { "temperature": { "value": "300", "unit": "K" } }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }

  @Test
  @DisplayName("Numeric and boolean values")
  public void test03() throws JsonProcessingException {
    Dataset dataset =
        new Dataset()
            .setCreator(List.of())
            .setVariableMeasured(
                List.of(
                    new PropertyValue().setName("photonEnergy").setValue(6.5).setUnitText("keV"),
                    new PropertyValue().setName("shutterOpen").setValue(true),
                    new PropertyValue().setName("runs").setValue(12)));

    assertEquals(
        jsonMapper.readTree(
            """
            {
              "photonEnergy": { "value": 6.5, "unit": "keV" },
              "shutterOpen": true,
              "runs": 12
            }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }

  @Test
  @DisplayName("Several literal values are mapped to an array")
  public void test04() throws JsonProcessingException {
    Dataset dataset =
        new Dataset()
            .setCreator(List.of())
            .setVariableMeasured(
                List.of(
                    new PropertyValue()
                        .setName("detectors")
                        .setValue(List.of("eiger", "pilatus"))));

    assertEquals(
        jsonMapper.readTree(
            """
            { "detectors": [ "eiger", "pilatus" ] }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }

  @Test
  @DisplayName("Nested properties are mapped to an object")
  public void test05() throws JsonProcessingException {
    PropertyValue environment =
        new PropertyValue()
            .setName("environment")
            .setValue(
                List.of(
                    new PropertyValue().setName("pressure").setValue("1013").setUnitText("mbar"),
                    new PropertyValue().setName("atmosphere").setValue("He")));
    PropertyValue sample =
        new PropertyValue()
            .setName("sample")
            .setValue(
                List.of(
                    new PropertyValue().setName("name").setValue("LaB6"),
                    new PropertyValue().setName("mass").setValue("12").setUnitText("mg"),
                    environment));
    Dataset dataset = new Dataset().setCreator(List.of()).setVariableMeasured(List.of(sample));

    assertEquals(
        jsonMapper.readTree(
            """
            {
              "sample": {
                "name": "LaB6",
                "mass": { "value": "12", "unit": "mg" },
                "environment": {
                  "pressure": { "value": "1013", "unit": "mbar" },
                  "atmosphere": "He"
                }
              }
            }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }

  @Test
  @DisplayName("A single nested property is mapped to an object")
  public void test06() throws JsonProcessingException {
    PropertyValue sample =
        new PropertyValue()
            .setName("sample")
            .setValue(new PropertyValue().setName("name").setValue("LaB6"));
    Dataset dataset = new Dataset().setCreator(List.of()).setVariableMeasured(List.of(sample));

    assertEquals(
        jsonMapper.readTree(
            """
            { "sample": { "name": "LaB6" } }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }

  @Test
  @DisplayName("Properties without a name or without a value are skipped")
  public void test07() throws JsonProcessingException {
    Dataset dataset =
        new Dataset()
            .setCreator(List.of())
            .setVariableMeasured(
                Arrays.asList(
                    new PropertyValue().setName("beamline").setValue("X12SA"),
                    new PropertyValue().setName("temperature").setUnitText("K"),
                    new PropertyValue().setValue("300")));

    assertEquals(
        jsonMapper.readTree(
            """
            { "beamline": "X12SA" }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }

  @Test
  @DisplayName("The measured variables of 'one-dataset.zip' are mapped")
  public void test08() throws JsonProcessingException {
    PropertyValue environment =
        new PropertyValue()
            .setName("environment")
            .setValue(
                List.of(
                    new PropertyValue().setName("pressure").setValue("1013").setUnitText("mbar"),
                    new PropertyValue().setName("atmosphere").setValue("He")));
    PropertyValue sample =
        new PropertyValue()
            .setName("sample")
            .setValue(
                List.of(
                    new PropertyValue().setName("name").setValue("LaB6"),
                    new PropertyValue().setName("mass").setValue("12").setUnitText("mg"),
                    environment));
    Dataset dataset =
        new Dataset()
            .setCreator(List.of())
            .setVariableMeasured(
                List.of(
                    new PropertyValue().setName("beamline").setValue("X12SA"),
                    new PropertyValue().setName("temperature").setValue("300").setUnitText("K"),
                    new PropertyValue().setName("photonEnergy").setValue(6.5).setUnitText("keV"),
                    new PropertyValue().setName("detectors").setValue(List.of("eiger", "pilatus")),
                    new PropertyValue().setName("shutterOpen").setValue(true),
                    sample));

    assertEquals(
        jsonMapper.readTree(
            """
            {
              "beamline": "X12SA",
              "temperature": { "value": "300", "unit": "K" },
              "photonEnergy": { "value": 6.5, "unit": "keV" },
              "detectors": [ "eiger", "pilatus" ],
              "shutterOpen": true,
              "sample": {
                "name": "LaB6",
                "mass": { "value": "12", "unit": "mg" },
                "environment": {
                  "pressure": { "value": "1013", "unit": "mbar" },
                  "atmosphere": "He"
                }
              }
            }
            """),
        modelMapper.map(dataset, CreateDatasetDto.class).getScientificMetadata());
  }
}
