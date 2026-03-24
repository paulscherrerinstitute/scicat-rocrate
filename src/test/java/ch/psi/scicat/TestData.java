package ch.psi.scicat;

import ch.psi.s3_broker.model.DatasetUrls;
import ch.psi.s3_broker.model.PublishedDataUrls;
import ch.psi.s3_broker.model.S3Url;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.MyIdentity;
import ch.psi.scicat.model.v3.MyIdentity.Profile;
import ch.psi.scicat.model.v3.PublishedData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestData {
  public static final MyIdentity rocrateUser;
  public static final String validDoi = "10.000/abc";
  public static PublishedData psiPub1;
  public static PublishedData hzdrPub1;
  public static Dataset psiDs1;
  public static Dataset psiDs2;
  public static Dataset psiDs3;

  private static ObjectMapper jsonReader = new ObjectMapper().registerModule(new JavaTimeModule());

  public static PublishedDataUrls psiPub1S3Response =
      new PublishedDataUrls()
          .setExpires(Instant.parse("2036-02-28T09:55:19Z"))
          .setUrls(
              Map.of(
                  "PID.SAMPLE.PREFIX/psi_ds1",
                  new DatasetUrls()
                      .setExpires(Instant.parse("2036-02-28T09:55:19Z"))
                      .setUrls(
                          List.of(
                              new S3Url()
                                  .setUrl(
                                      "https://example.com/PID.SAMPLE.PREFIX/psi_ds1.tar?X-Amz-Date=20260302T095519Z&X-Amz-Expires=315360000")
                                  .setExpires(Instant.parse("2036-02-28T09:55:19Z")))),
                  "PID.SAMPLE.PREFIX/psi_ds2",
                  new DatasetUrls()
                      .setExpires(Instant.parse("2036-02-28T09:55:19Z"))
                      .setUrls(
                          List.of(
                              new S3Url()
                                  .setUrl(
                                      "https://example.com/PID.SAMPLE.PREFIX/psi_ds2.tar?X-Amz-Date=20260302T095519Z&X-Amz-Expires=315360000")
                                  .setExpires(Instant.parse("2036-02-28T09:55:19Z")))),
                  "PID.SAMPLE.PREFIX/psi_ds3",
                  new DatasetUrls()
                      .setExpires(Instant.parse("2036-02-28T09:55:19Z"))
                      .setUrls(
                          List.of(
                              new S3Url()
                                  .setUrl(
                                      "https://example.com/PID.SAMPLE.PREFIX/psi_ds3.tar?X-Amz-Date=20260302T095519Z&X-Amz-Expires=315360000")
                                  .setExpires(Instant.parse("2036-02-28T09:55:19Z"))))));

  public static PublishedDataUrls hzdrPub1S3Response =
      new PublishedDataUrls()
          .setExpires(Instant.parse("2036-02-28T09:55:19Z"))
          .setUrls(
              Map.of(
                  "PID.SAMPLE.PREFIX/hzdr_ds1",
                  new DatasetUrls()
                      .setExpires(Instant.parse("2020-03-01T01:00:00Z"))
                      .setUrls(
                          List.of(
                              new S3Url()
                                  .setUrl(
                                      "https://example.com/PID.SAMPLE.PREFIX/hzdr_ds1.tar?X-Amz-Date=20200301T000000Z&X-Amz-Expires=3600")
                                  .setExpires(Instant.parse("2020-03-01T01:00:00Z"))))));

  static {
    rocrateUser =
        new MyIdentity()
            .setProfile(
                new Profile().setUsername("rocrate").setAccessGroups(List.of("user", "rocrate")));
    try {
      psiPub1 =
          jsonReader.readValue(
              TestData.class.getClassLoader().getResourceAsStream("scicatlive/psi_pub1.json"),
              PublishedData.class);
      hzdrPub1 =
          jsonReader.readValue(
              TestData.class.getClassLoader().getResourceAsStream("scicatlive/hzdr_pub1.json"),
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
