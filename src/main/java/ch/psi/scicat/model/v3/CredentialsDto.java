package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class CredentialsDto {
  @JsonProperty(required = true)
  String username;

  @JsonProperty(required = true)
  String password;
}
