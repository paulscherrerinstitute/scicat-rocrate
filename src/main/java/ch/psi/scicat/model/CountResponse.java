package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountResponse {
  @JsonProperty("count")
  private int count;
}
