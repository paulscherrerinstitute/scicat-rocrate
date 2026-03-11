package ch.psi.s3_broker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;

@Data
public class S3Url {
  @JsonProperty public String url;

  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  public Instant expires;
}
