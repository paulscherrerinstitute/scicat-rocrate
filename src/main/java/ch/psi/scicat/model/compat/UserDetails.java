package ch.psi.scicat.model.compat;

import java.util.List;
import lombok.Data;

@Data
public class UserDetails {
  private String username;
  private String email;
  private List<String> groups;
}
