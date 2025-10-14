package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;

@Data
public class CreateDatasetDto {
  @JsonProperty("owner")
  String owner;

  @JsonProperty("contactEmail")
  String contactEmail;

  @JsonProperty("sourceFolder")
  String sourceFolder;

  @JsonProperty("creationTime")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant creationTime;

  @JsonProperty("type")
  DatasetType type;

  @JsonProperty("ownerGroup")
  String ownerGroup;
}
