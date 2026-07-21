package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Data
@JsonInclude(Include.NON_NULL)
public class DatasetLifeCycle {
  @JsonProperty() private boolean archivable;
  @JsonProperty() private boolean retrievable;
  @JsonProperty() private String archiveStatusMessage;

  @JsonProperty()
  @Getter(onMethod_ = {@JsonProperty("isOnCentralDisk")})
  private boolean isOnCentralDisk;
}
