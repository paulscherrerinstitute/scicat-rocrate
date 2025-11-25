package ch.psi.scicat;

import ch.psi.scicat.model.compat.UserDetails;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.PublishedData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestData {
  public static final UserDetails rocrateUser;
  public static final String validDoi = "10.000/abc";
  public static PublishedData psiPub1;
  public static Dataset psiDs1;
  public static Dataset psiDs2;
  public static Dataset psiDs3;

  private static ObjectMapper jsonReader = new ObjectMapper().registerModule(new JavaTimeModule());

  static {
    rocrateUser = new UserDetails().setUsername("rocrate").setGroups(List.of("user", "rocrate"));
    try {
      psiPub1 =
          jsonReader.readValue(
              TestData.class.getClassLoader().getResourceAsStream("scicatlive/psi_pub1.json"),
              PublishedData.class);
      psiDs1 =
          jsonReader.readValue(
              TestData.class.getClassLoader().getResourceAsStream("scicatlive/psi_ds1.json"),
              Dataset.class);
      psiDs2 =
          jsonReader.readValue(
              TestData.class.getClassLoader().getResourceAsStream("scicatlive/psi_ds2.json"),
              Dataset.class);
      psiDs3 =
          jsonReader.readValue(
              TestData.class.getClassLoader().getResourceAsStream("scicatlive/psi_ds3.json"),
              Dataset.class);
    } catch (Exception e) {
      log.error("Failed to initialize test data", e);
    }
  }
}
