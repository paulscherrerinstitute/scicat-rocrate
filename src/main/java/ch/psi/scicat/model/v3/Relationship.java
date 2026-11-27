package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Relationship {
  private String identifier;
  private String identifierType;
  private String relationship;
  private String entityType;
  private String externalId;
}
