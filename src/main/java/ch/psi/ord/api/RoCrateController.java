package ch.psi.ord.api;

import ch.psi.ord.core.DoiUtils;
import ch.psi.ord.core.RoCrate;
import ch.psi.ord.core.RoCrateExporter;
import ch.psi.ord.core.RoCrateImporter;
import ch.psi.ord.model.Error;
import ch.psi.ord.model.ExportFormat;
import ch.psi.ord.model.ValidationReport;
import ch.psi.scicat.client.ScicatClient;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.LRUDocumentCache;
import com.apicatalog.jsonld.uri.UriValidationPolicy;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RiotException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

@Path("ro-crate")
@Tag(name = "ro-crate")
@Slf4j
public class RoCrateController {
  private int cacheSize = 0;

  @Inject RoCrateExporter exporter;

  @Inject RoCrateImporter importer;

  @Inject ScicatClient scicatClient;

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

  @ServerExceptionMapper
  public Response mapRiotException(RiotException e) {
    return Response.status(Status.BAD_REQUEST)
        .entity(new Error("Failed to parse the metadata descriptor"))
        .build();
  }

  @ServerExceptionMapper
  public Response mapZipException(ZipException e) {
    return Response.status(Status.BAD_REQUEST).entity(new Error(e.getMessage())).build();
  }

  @ServerExceptionMapper
  public Response mapFileNotFoundException(FileNotFoundException e) {
    return Response.status(Status.BAD_REQUEST).entity(new Error(e.getMessage())).build();
  }

  @Target(ElementType.METHOD)
  @Retention(value = RetentionPolicy.RUNTIME)
  @NameBinding
  public @interface ScicatAuth {}

  @ScicatAuth
  @ServerRequestFilter()
  public Optional<Response> scicatAuthFilter(ContainerRequestContext requestContext) {
    String scicatToken = requestContext.getHeaderString("api-key");
    if (!scicatClient.checkTokenValidity(scicatToken)) {
      return Optional.of(Response.status(Status.UNAUTHORIZED).build());
    }
    return Optional.empty();
  }

  private Optional<Response> export(List<String> identifiers) {
    if (identifiers.stream().anyMatch(id -> !DoiUtils.isDoi(id))) {
      return Optional.of(
          Response.status(Status.BAD_REQUEST)
              .entity("{\"message\": \"Identifiers other than DOI are not implemented yet\"}")
              .type(MediaType.APPLICATION_JSON)
              .build());
    }

    // FIXME: Will need to add other types
    try {
      exporter.addPublications(identifiers);
    } catch (ClientWebApplicationException e) {
      Response res = e.getResponse();
      return Optional.of(
          Response.status(res.getStatus())
              .entity(res.getEntity())
              .type(res.getMediaType())
              .build());
    }

    return Optional.empty();
  }

  @POST
  @Path("/export")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response exportCrate(
      @RestHeader("export") ExportFormat exportFormat, List<String> identifiers) {
    if (exportFormat == null) {
      return Response.status(Status.BAD_REQUEST).entity("null").build();
    }

    Optional<Response> errorResponse = export(identifiers);
    if (errorResponse.isPresent()) {
      return errorResponse.get();
    }

    return switch (exportFormat) {
      case JSONLD ->
          Response.ok(exporter.getCrateMetadata())
              .type(ExtraMediaType.APPLICATION_JSONLD_TYPE)
              .build();
      case ZIP ->
          exporter
              .getZip()
              .map(bytes -> Response.ok(bytes).type(ExtraMediaType.APPLICATION_ZIP).build())
              .orElseGet(() -> Response.serverError().build());
      default -> Response.status(Status.BAD_REQUEST).build();
    };
  }

  @POST
  @Path("/validate")
  @Consumes(ExtraMediaType.APPLICATION_JSONLD)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateCrate(InputStream body) {
    try (RoCrate crate = new RoCrate(body)) {
      importer.loadCrate(crate);
      return Response.ok(importer.validate()).build();
    }
  }

  @POST
  @Path("/validate")
  @Consumes(ExtraMediaType.APPLICATION_ZIP)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateZippedCrate(InputStream body)
      throws RiotException, FileNotFoundException, ZipException, IOException {
    try (ZipInputStream zip = new ZipInputStream(body);
        RoCrate crate = new RoCrate(zip)) {
      importer.loadCrate(crate);
      return Response.ok(importer.validate()).build();
    }
  }

  @POST
  @Path("/import")
  @Consumes(ExtraMediaType.APPLICATION_JSONLD)
  @Produces(MediaType.APPLICATION_JSON)
  @ScicatAuth
  public Response importCrate(
      @HeaderParam(value = "api-key") String scicatToken, InputStream body) {
    try (RoCrate crate = new RoCrate(body)) {
      importer.loadCrate(crate);
      ValidationReport report = importer.validate();
      if (!report.isValid()) {
        return Response.status(Status.BAD_REQUEST).entity(report).build();
      }

      Map<String, String> importMap = importer.importCrate(report, scicatToken);
      return Response.status(importMap.isEmpty() ? Status.OK : Status.CREATED)
          .entity(importMap)
          .build();
    }
  }

  @POST
  @Path("/import")
  @Consumes(ExtraMediaType.APPLICATION_ZIP)
  @Produces(MediaType.APPLICATION_JSON)
  @ScicatAuth
  public Response importZippedCrate(
      @HeaderParam(value = "api-key") String scicatToken, InputStream body)
      throws RiotException, FileNotFoundException, ZipException, IOException {
    try (ZipInputStream zip = new ZipInputStream(body);
        RoCrate crate = new RoCrate(zip)) {
      importer.loadCrate(crate);
      ValidationReport report = importer.validate();
      if (!report.isValid()) {
        return Response.status(Status.BAD_REQUEST).entity(report).build();
      }

      Map<String, String> importMap = importer.importCrate(report, scicatToken);
      return Response.status(importMap.isEmpty() ? Status.OK : Status.CREATED)
          .entity(importMap)
          .build();
    }
  }
}
