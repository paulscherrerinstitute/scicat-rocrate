package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class CreateJobDto {
  @JsonProperty(required = true)
  String type;

  @JsonProperty() JobParameters jobParams;
  @JsonProperty() String jobStatusMessage;

  @JsonProperty(required = true)
  List<DatasetEntry> datasetList;

  @JsonProperty() String emailJobInitiator;
  @JsonProperty() String executionTime;

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class JobParameters {
    @JsonProperty() String tapeCopies;
    @JsonProperty() String username;
    @JsonProperty() String ownerGroup;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class DatasetEntry {
    @JsonProperty() String pid;
    @JsonProperty() List<String> files = Collections.emptyList();
  }
}
