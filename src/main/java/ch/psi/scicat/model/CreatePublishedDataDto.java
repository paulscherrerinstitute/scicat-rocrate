package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePublishedDataDto {
  @JsonProperty(value = "_id")
  private String _id;

  @JsonProperty(value = "doi")
  private String doi;

  private String affiliation;

  @JsonProperty(value = "creator", required = true)
  private List<String> creator;

  @JsonProperty(value = "publisher", required = true)
  private String publisher;

  @JsonProperty(value = "publicationYear", required = true)
  private int publicationYear;

  private String title;

  @Accessors(prefix = "_")
  @JsonProperty(value = "abstract", required = true)
  private String _abstract;

  private String url;

  @JsonProperty(value = "dataDescription", required = true)
  private String dataDescription;

  @JsonProperty(value = "resourceType", required = true)
  private String resourceType;

  private int numberOfFiles;
  private double sizeOfArchive;

  @JsonProperty(value = "pidArray", required = true)
  private List<String> pidArray = new ArrayList<>();

  @JsonProperty("authors")
  private List<String> authors;

  @JsonProperty("registeredTime")
  private String registeredTime;

  @JsonProperty("status")
  private String status;

  @JsonProperty("scicatUser")
  private String scicatUser;

  @JsonProperty("thumbnail")
  private String thumbnail;

  @JsonProperty("relatedPublications")
  private List<String> relatedPublications;

  @JsonProperty("downloadLink")
  private String downloadLink;
}
