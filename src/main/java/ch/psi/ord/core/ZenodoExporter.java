package ch.psi.ord.core;

import ch.psi.ord.model.ZenodoDataset;
import ch.psi.rdf.RdfSerializationException;
import ch.psi.rdf.RdfSerializer;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
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
  @Inject private ModelMapper modelMapper;
  private RdfSerializer serializer = new RdfSerializer();
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
    RestResponse<PublishedData> res = scicatClient.getPublishedDataById(doi);
    PublishedData publishedData = res.getEntity();
    ZenodoDataset zenodoDataset = modelMapper.map(publishedData, ZenodoDataset.class);

    Resource serializedDataset = serializer.serialize(zenodoDataset);
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
