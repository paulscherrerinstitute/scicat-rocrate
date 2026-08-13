package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Dataset {
  private String pid;
  private String datasetName;
  private String description;
  private String ownerGroup;
  private BigInteger size;
}
