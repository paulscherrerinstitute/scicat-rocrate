package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRelationshipDto {
  @JsonProperty(required = true)
  private String identifier;

  @JsonProperty() private String identifierType;
  @JsonProperty() private String relationship;
  @JsonProperty() private String entityType;
  @JsonProperty() private String externalId;
}
