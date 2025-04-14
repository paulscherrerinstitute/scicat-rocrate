package ch.psi.scicat;

import java.io.InputStream;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.LangJSONLD11;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.LRUDocumentCache;
import com.apicatalog.jsonld.uri.UriValidationPolicy;

import ch.psi.scicat.model.PublishedData;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("ro-crate")
@Tag(name = "ro-crate")
public class RoCrateController {
  private int cacheSize = 0;

  @Inject
  ScicatClient scicatClient;

  @Inject
  RoCrateExporter exporter;

  @Inject
  RoCrateImporter importer;

  // Cache JSON-LD remote documents across requests
  LRUDocumentCache documentLoader;

  public RoCrateController(ScicatClient scicatClient, RoCrateExporter exporter,
      RoCrateImporter importer,
      @ConfigProperty(name = "titanium.jsonld.cache.size") int cacheSize) {
    this.scicatClient = scicatClient;
    this.exporter = exporter;
    this.importer = importer;
    this.cacheSize = cacheSize;
    this.documentLoader = new LRUDocumentCache(HttpLoader.defaultInstance(), this.cacheSize);
  }

  @GET
  @Path("/publications/{doi}")
  @Produces({ ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP })
  public Response exportPublication(@Context HttpHeaders headers,
      @PathParam("doi") String doi) {
    List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
    if (acceptableMediaTypes.stream().anyMatch(
        mt -> mt.equals(ExtraMediaType.APPLICATION_ZIP_TYPE)) &&
        acceptableMediaTypes.stream().noneMatch(
            mt -> mt.equals(ExtraMediaType.APPLICATION_JSONLD_TYPE))) {
      return Response.status(Status.NOT_IMPLEMENTED)
          .entity("Export to ZIP file is not supported yet")
          .type(MediaType.TEXT_PLAIN)
          .build();
    }

    RestResponse<PublishedData> res = scicatClient.getPublishedDataById(doi);
    if (!res.hasEntity()) {
      // FIXME: scicatClient throws anyway
      return Response.status(Status.NOT_FOUND).build();
    }

    PublishedData publication = res.getEntity();
    exporter.addPublication(publication, true);

    return Response.ok(exporter.getCrateMetadata())
        .type(ExtraMediaType.APPLICATION_JSONLD_TYPE)
        .build();
  }

  @POST
  @Path("")
  @Consumes(ExtraMediaType.APPLICATION_JSONLD)
  @Produces(MediaType.APPLICATION_JSON)
  public Response importRoCrate(InputStream inputStream) {
    JsonLdOptions options = new JsonLdOptions();
    options.setUndefinedTermsPolicy(ProcessingPolicy.Warn);
    options.setDocumentLoader(documentLoader);
    options.setUriValidation(UriValidationPolicy.Full);

    Model model = RDFParser.create()
        .source(inputStream)
        .lang(Lang.JSONLD11)
        .base("file:///")
        .context(org.apache.jena.sparql.util.Context.create().set(LangJSONLD11.JSONLD_OPTIONS, options))
        .build()
        .toModel();

    // importer.loadRoCrate(documentLoader, inputStream);
    importer.loadModel(model);
    var publications = importer.listPublications();

    return Response.ok(publications).build(); // TODO: should be a created
  }
}
