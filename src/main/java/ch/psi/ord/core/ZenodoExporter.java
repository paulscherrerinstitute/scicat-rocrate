package ch.psi.ord.core;

import ch.psi.ord.api.ExtraMediaType;
import ch.psi.ord.model.DataDownload;
import ch.psi.ord.model.ZenodoDataset;
import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.ser.RdfSerializationException;
import ch.psi.s3_broker.client.S3BrokerService;
import ch.psi.s3_broker.model.PublishedDataUrls;
import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.v3.PublishedData;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.modelmapper.ModelMapper;

/**
 * The {@code ZenodoExporter} class is responsible for exporting data from SciCat in the Zenodo
 * dataset format.
 */
@Slf4j
@ApplicationScoped
public class ZenodoExporter {
  @Inject ScicatClient scicatClient;
  @RestClient @Inject S3BrokerService s3BrokerService;
  @Inject private ModelMapper modelMapper;
  private RdfMapper rdfMapper = new RdfMapper();
  private Document zenodoFrame;

  public ZenodoExporter() {
    try {
      this.zenodoFrame =
          JsonDocument.of(
              new StringReader(
                  """
                      {
                        "@context": {"@vocab": "https://schema.org/"},
                        "@type": "Dataset",
                        "@embed": "@always"
                      }
                  """));
    } catch (JsonLdError e) {
      log.error("Failed to parse static frame", e);
    }
  }

  public String exportDoi(String doi) throws RdfSerializationException, JsonLdError {
    PublishedDataUrls brokerResponse = s3BrokerService.getPublishedDataUrls(doi);
    List<DataDownload> distribution =
        brokerResponse.getUrls().values().stream()
            .flatMap(datasetUrls -> datasetUrls.getUrls().stream())
            .map(
                s3Url ->
                    new DataDownload()
                        .setContentUrl(s3Url.getUrl())
                        .setEncodingFormat(ExtraMediaType.APPLICATION_TAR)
                        .setExpirationDate(s3Url.getExpires()))
            .toList();

    RestResponse<PublishedData> res = scicatClient.getPublishedDataById(doi);
    PublishedData publishedData = res.getEntity();
    ZenodoDataset zenodoDataset = modelMapper.map(publishedData, ZenodoDataset.class);
    zenodoDataset.setDistribution(distribution).setExpires(brokerResponse.getExpires());

    RDFNode serializedDataset = rdfMapper.serialize(zenodoDataset);
    StringWriter sw = new StringWriter();
    RDFWriter.create()
        .source(serializedDataset.getModel())
        .format(RDFFormat.JSONLD11)
        .build()
        .output(sw);
    Document doc = JsonDocument.of(new StringReader(sw.toString()));

    return JsonLd.frame(doc, zenodoFrame).get().toString();
  }
}
