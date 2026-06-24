package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class CreateDatasetDto {
  @JsonProperty(required = true)
  String datasetName;

  @JsonProperty(required = true)
  String owner;

  @JsonProperty(required = true)
  String contactEmail;

  @JsonProperty(required = true)
  String principalInvestigator;

  @JsonProperty(required = true)
  String sourceFolder;

  @JsonProperty() String sourceFolderHost;

  @JsonProperty(required = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant creationTime;

  @JsonProperty(required = true)
  DatasetType type;

  @JsonProperty(required = true)
  String ownerGroup;

  @JsonProperty(value = "isPublished")
  boolean published;

  @JsonProperty(required = true)
  String creationLocation;

  @JsonProperty() String description;

  @JsonProperty() ObjectNode scientificMetadata;

  @JsonProperty() List<String> keywords;
}
