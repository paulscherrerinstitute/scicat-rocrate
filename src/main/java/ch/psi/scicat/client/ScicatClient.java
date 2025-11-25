package ch.psi.scicat.client;

import ch.psi.scicat.model.compat.UserDetails;

public abstract class ScicatClient implements ScicatService {
  public abstract boolean isHealthy();

  public abstract boolean checkTokenValidity(String accessToken);

  public abstract UserDetails userDetails(String accessToken);
}
