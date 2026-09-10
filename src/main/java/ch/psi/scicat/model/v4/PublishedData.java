package ch.psi.scicat.model.v4;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishedData {
  @JsonProperty(required = true)
  private String doi;

  @JsonProperty(required = true)
  private String title;

  @Accessors(prefix = "_")
  @JsonProperty(value = "abstract", required = true)
  private String _abstract;

  @JsonProperty(required = true)
  private List<String> datasetPids = new ArrayList<>();

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant registeredTime;

  @JsonProperty(required = true)
  private PublishedDataStatus status;

  @JsonProperty(required = true)
  private DataciteMetadata metadata = new DataciteMetadata();

  @JsonProperty(required = true)
  private String createdBy;

  @JsonProperty(required = true)
  private String updatedBy;

  @JsonProperty(required = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant createdAt;

  @JsonProperty(required = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant updatedAt;
}
