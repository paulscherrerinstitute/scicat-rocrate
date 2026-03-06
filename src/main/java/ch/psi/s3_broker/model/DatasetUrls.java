package ch.psi.s3_broker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class DatasetUrls {
  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  public Instant expires;

  @JsonProperty public List<S3Url> urls;
}
