package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  private String doi;

  private String affiliation;

  @JsonProperty(value = "creator", required = true)
  private List<String> creator;

  @JsonProperty(value = "publisher", required = true)
  private String publisher;

  @JsonProperty(value = "publicationYear", required = true)
  private int publicationYear;

  @JsonProperty(value = "title", required = true)
  private String title;

  private String url;

  @Accessors(prefix = "_")
  @JsonProperty(value = "abstract", required = true)
  private String _abstract;

  @JsonProperty(value = "dataDescription", required = true)
  private String dataDescription;

  @JsonProperty(value = "resourceType", required = true)
  private String resourceType;

  private long numberOfFiles;

  private long sizeOfArchive;

  @JsonProperty(value = "pidArray", required = true)
  private List<String> pidArray = new ArrayList<>();

  private List<String> authors;

  private String registeredTime;

  private String status;

  private String scicatUser;

  private String thumbnail;

  private List<String> relatedPublications;

  private String downloadLink;

  private String createdBy;

  private String updatedBy;

  private String createdAt;

  private String updatedAt;
}
