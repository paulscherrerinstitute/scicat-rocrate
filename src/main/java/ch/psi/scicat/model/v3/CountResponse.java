package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountResponse {
  @JsonProperty("count")
  private int count;
}
