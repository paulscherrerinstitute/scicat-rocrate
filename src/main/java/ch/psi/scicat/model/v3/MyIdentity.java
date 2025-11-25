package ch.psi.scicat.model.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyIdentity {
  @JsonProperty("_id")
  private String id;

  @JsonProperty("authStrategy")
  private String authStrategy;

  @JsonProperty("externalId")
  private String externalId;

  @JsonProperty("profile")
  private Profile profile;

  @JsonProperty("provider")
  private String provider;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("created")
  private String created;

  @JsonProperty("modified")
  private String modified;

  @JsonProperty("__v")
  private int version;

  @JsonProperty("id")
  private String uniqueId;

  @Data
  public static class Profile {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("thumbnailPhoto")
    private String thumbnailPhoto;

    @JsonProperty("id")
    private String id;

    @JsonProperty("emails")
    private List<Email> emails;

    @JsonProperty("accessGroups")
    private List<String> accessGroups;

    @JsonProperty("_id")
    private String profileId;

    @Data
    public static class Email {
      @JsonProperty("value")
      private String value;
    }
  }
}
