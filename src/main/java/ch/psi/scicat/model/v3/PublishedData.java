package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

// TODO: Make sure it doesn't break on big values for 'number' types
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishedData {
  @JsonProperty(value = "doi", required = true)
  String doi;

  String affiliation;

  @JsonProperty(value = "creator", required = true)
  List<String> creator;

  @JsonProperty(value = "publisher", required = true)
  String publisher;

  @JsonProperty(value = "publicationYear", required = true)
  int publicationYear;

  @JsonProperty(value = "title", required = true)
  String title;

  String url;

  @Accessors(prefix = "_")
  @JsonProperty(value = "abstract", required = true)
  String _abstract;

  @JsonProperty(value = "dataDescription", required = true)
  String dataDescription;

  @JsonProperty(value = "resourceType", required = true)
  String resourceType;

  long numberOfFiles;
  long sizeOfArchive;

  @JsonProperty(value = "pidArray", required = true)
  List<String> pidArray = new ArrayList<>();

  List<String> authors;

  @JsonProperty("registeredTime")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  Instant registeredTime;

  @JsonProperty("status")
  PublishedDataStatus status;

  String scicatUser;
  String thumbnail;
  List<String> relatedPublications;
  String downloadLink;
  String createdBy;
  String updatedBy;
  String createdAt;
  String updatedAt;
}
