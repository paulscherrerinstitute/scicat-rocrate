package ch.psi.ord.api;

import ch.psi.ord.core.DoiUtils;
import ch.psi.ord.core.RoCrateExporter;
import ch.psi.ord.core.RoCrateImporter;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ValidationReport;
import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.CreatePublishedDataDto;
import ch.psi.scicat.model.PublishedData;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.LRUDocumentCache;
import com.apicatalog.jsonld.uri.UriValidationPolicy;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LangJSONLD11;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestResponse;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("ro-crate")
@Tag(name = "ro-crate")
public class RoCrateController {
  private int cacheSize = 0;

  private static final Logger logger = LoggerFactory.getLogger(RoCrateController.class);

  @Inject RoCrateExporter exporter;

  @Inject RoCrateImporter importer;

  @Inject ScicatClient scicatClient;

  @Inject ModelMapper modelMapper;

  // Cache JSON-LD remote documents across requests
  LRUDocumentCache documentLoader;

  private JsonLdOptions jsonLdOptions = new JsonLdOptions();

  public RoCrateController(
      RoCrateExporter exporter,
      RoCrateImporter importer,
      @ConfigProperty(name = "titanium.jsonld.cache.size") int cacheSize) {
    this.exporter = exporter;
    this.importer = importer;
    this.cacheSize = cacheSize;
    this.documentLoader = new LRUDocumentCache(HttpLoader.defaultInstance(), this.cacheSize);

    jsonLdOptions.setUndefinedTermsPolicy(ProcessingPolicy.Warn);
    jsonLdOptions.setDocumentLoader(documentLoader);
    jsonLdOptions.setUriValidation(UriValidationPolicy.None);
  }

  @POST
  @Path("/export")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA})
  @Produces({ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP})
  public Response exportPublication(
      @RestHeader("Accept") List<MediaType> acceptHeader,
      @RestHeader("Content-Type") MediaType contentTypeHeader,
      List<String> identifiers) {
    if (acceptHeader.contains(ExtraMediaType.APPLICATION_ZIP_TYPE)
        && !acceptHeader.contains(ExtraMediaType.APPLICATION_JSONLD_TYPE)) {
      return Response.status(Status.NOT_IMPLEMENTED)
          .entity("Export to ZIP file is not supported yet")
          .type(MediaType.TEXT_PLAIN)
          .build();
    }

    if (identifiers.stream().anyMatch(id -> !DoiUtils.isDoi(id))) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Identifiers other than DOI are not implemented yet")
          .type(MediaType.TEXT_PLAIN)
          .build();
    }

    // FIXME: Will need to add other types
    try {
      exporter.addPublications(identifiers);
    } catch (ClientWebApplicationException e) {
      Response res = e.getResponse();
      return Response.status(res.getStatus())
          .entity(res.getEntity())
          .type(res.getMediaType())
          .build();
    }

    return Response.ok(exporter.getCrateMetadata())
        .type(ExtraMediaType.APPLICATION_JSONLD_TYPE)
        .build();
  }

  @POST
  @Path("/validate")
  @Consumes({ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP})
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateRoCrate(InputStream body) {
    Optional<Response> response = isBodyEmpty(body);
    if (response.isPresent()) {
      return response.get();
    }

    Optional<Model> model = parseJsonLd(body);
    if (model.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    importer.loadModel(model.get());

    ValidationReport report = importer.validate();

    return Response.ok(report).build();
  }

  @POST
  @Path("/import")
  @Consumes(ExtraMediaType.APPLICATION_JSONLD)
  @Produces(MediaType.APPLICATION_JSON)
  public Response importRoCrate(
      @HeaderParam(value = "scicat-token") String scicatToken, InputStream body) {
    Optional<Response> response = isBodyEmpty(body);
    if (response.isPresent()) {
      return response.get();
    }

    Optional<Model> model = parseJsonLd(body);
    if (model.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    importer.loadModel(model.get());
    ValidationReport report = importer.validate();
    if (!report.isValid()) {
      return Response.status(Status.BAD_REQUEST).entity(report).build();
    }

    Map<String, String> importMap = new HashMap<>();
    try {
      report
          .getEntities()
          .forEach(
              entity -> {
                if (entity.object() instanceof Publication publication) {
                  // TODO: create the objects
                  CreatePublishedDataDto dto =
                      modelMapper.map(publication, CreatePublishedDataDto.class);
                  RestResponse<PublishedData> created =
                      scicatClient.createPublishedData(dto, scicatToken);
                  importMap.put(entity.id(), created.getEntity().doi());
                }
              });
    } catch (WebApplicationException e) {
      logger.error("", e);
      return e.getResponse();
    } catch (MappingException e) {
      logger.error("", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(
              "{\"message\":\"Failed to build SciCat payload: " + e.getCause().getMessage() + "\"}")
          .build();
    }

    return Response.status(importMap.isEmpty() ? Status.OK : Status.CREATED)
        .entity(importMap)
        .build();
  }

  private Optional<Response> isBodyEmpty(InputStream body) {
    try {
      if (body.available() == 0) return Optional.of(Response.status(Status.BAD_REQUEST).build());
    } catch (IOException e) {
      return Optional.of(Response.serverError().build());
    }

    return Optional.empty();
  }

  private Optional<Model> parseJsonLd(InputStream document) {
    try {
      Model model =
          RDFParser.create()
              .source(document)
              .lang(Lang.JSONLD11)
              .base("file:///")
              .context(
                  org.apache.jena.sparql.util.Context.create()
                      .set(LangJSONLD11.JSONLD_OPTIONS, jsonLdOptions))
              .build()
              .toModel();

      return Optional.of(model);
    } catch (RiotException e) {
      logger.error("", e);
    }

    return Optional.empty();
  }
}
