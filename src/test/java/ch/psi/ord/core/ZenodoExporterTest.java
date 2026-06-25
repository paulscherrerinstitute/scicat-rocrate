package ch.psi.ord.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import ch.psi.s3_broker.client.S3BrokerService;
import ch.psi.s3_broker.model.DatasetUrls;
import ch.psi.s3_broker.model.PublishedDataUrls;
import ch.psi.s3_broker.model.S3Url;
import ch.psi.scicat.TestData;
import ch.psi.scicat.client.ScicatClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ZenodoExporterTest {
  @Inject ZenodoExporter zenodoExporter;
  @InjectMock protected ScicatClient scicatClient;
  @InjectMock @RestClient protected S3BrokerService s3BrokerService;

  @Test
  @DisplayName("distribution should be serialized as an array when single-valued")
  public void test00() {
    Instant expirationDate = Instant.parse("2036-02-28T09:55:19Z");
    when(scicatClient.getPublishedDataById(TestData.psiPub1.getDoi()))
        .thenReturn(RestResponse.ok(TestData.psiPub1));
    when(s3BrokerService.getPublishedDataUrls(TestData.psiPub1.getDoi()))
        .thenReturn(
            new PublishedDataUrls()
                .setExpires(expirationDate)
                .setUrls(
                    Map.of(
                        "PID.SAMPLE.PREFIX/psi_ds3",
                        new DatasetUrls()
                            .setExpires(Instant.parse("2036-02-28T09:55:19Z"))
                            .setUrls(
                                List.of(
                                    new S3Url()
                                        .setUrl(
                                            "https://example.com/tenant:bucket/PID.SAMPLE.PREFIX/psi_ds3/461f1e85-08fe-4972-85e9-1a5d8b0431ee_0_2025-10-03-11-12-56.tar")
                                        .setExpires(Instant.parse("2036-02-28T09:55:19Z")))))));

    assertDoesNotThrow(
        () ->
            zenodoExporter.exportDoi(TestData.psiPub1.getDoi()).get("distribution").asJsonArray());
  }
}
