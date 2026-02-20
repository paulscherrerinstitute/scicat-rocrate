package ch.psi.ord.core;

import ch.psi.ord.model.ZenodoDataset;
import ch.psi.rdf.RdfSerializer;
import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.v3.PublishedData;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.modelmapper.ModelMapper;

@ApplicationScoped
public class ZenodoExporter {
  @Inject ScicatClient scicatClient;
  @Inject private ModelMapper modelMapper;
  private RdfSerializer serializer = new RdfSerializer();

  public String exportDoi(String doi) throws Exception {
    RestResponse<PublishedData> res = scicatClient.getPublishedDataById(doi);
    PublishedData publishedData = res.getEntity();
    ZenodoDataset zenodoDataset = modelMapper.map(publishedData, ZenodoDataset.class);

    Model model = ModelFactory.createDefaultModel().setNsPrefix("", SchemaDO.NS);
    if (serializer.serialize(model, zenodoDataset).isEmpty()) {
      throw new WebApplicationException();
    }
    StringWriter sw = new StringWriter();
    RDFWriter.create().source(model).format(RDFFormat.JSONLD11).build().output(sw);

    Document doc = JsonDocument.of(new StringReader(sw.toString()));
    String frameJson =
        """
        {
          "@context": {"@vocab": "https://schema.org/"},
          "@type": "Dataset",
          "@embed": "@always"
        }
        """;

    Document frame = JsonDocument.of(new StringReader(frameJson));
    return JsonLd.frame(doc, frame).get().toString();
  }
}
