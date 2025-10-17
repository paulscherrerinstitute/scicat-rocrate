package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class UserInfos {
  @JsonProperty("accessToken")
  private AccessToken accessToken;

  @JsonProperty("authorizedRoles")
  private AuthorizedRoles authorizedRoles;

  @JsonProperty("modelName")
  private String modelName;

  @JsonProperty("currentUser")
  private String currentUser;

  @JsonProperty("currentUserEmail")
  private String currentUserEmail;

  @JsonProperty("currentGroups")
  private List<String> currentGroups;

  @Data
  public static class AccessToken {
    @JsonProperty("id")
    private String id;

    @JsonProperty("ttl")
    private int ttl;

    @JsonProperty("created")
    private String created;

    @JsonProperty("userId")
    private String userId;
  }

  @Data
  public static class AuthorizedRoles {
    @JsonProperty("$authenticated")
    private boolean authenticated;
  }
}
