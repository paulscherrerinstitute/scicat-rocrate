package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePublishedDataDto {
  @JsonProperty(value = "_id")
  private String _id;

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

  @JsonProperty(value = "abstract", required = true)
  private String abstract_;

  private String url;

  @JsonProperty(value = "dataDescription", required = true)
  private String dataDescription;

  @JsonProperty(value = "resourceType", required = true)
  private String resourceType;

  private int numberOfFiles;

  private double sizeOfArchive;

  @JsonProperty(value = "pidArray", required = true)
  private List<String> pidArray = new ArrayList<>();

  private List<String> authors;

  private String registeredTime;

  private String status;

  private String scicatUser;

  private String thumbnail;

  private List<String> relatedPublications;

  private String downloadLink;
}
