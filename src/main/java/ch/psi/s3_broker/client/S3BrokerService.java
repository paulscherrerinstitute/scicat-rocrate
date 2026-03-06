package ch.psi.s3_broker.client;

import ch.psi.s3_broker.model.DatasetUrls;
import ch.psi.s3_broker.model.PublishedDataUrls;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "s3broker")
public interface S3BrokerService {
  @GET
  @Path("/datasets/urls")
  DatasetUrls getDatasetUrls(@QueryParam("pid") String pid);

  @GET
  @Path("/publisheddata/urls")
  PublishedDataUrls getPublishedDataUrls(@QueryParam("id") String doi);
}
