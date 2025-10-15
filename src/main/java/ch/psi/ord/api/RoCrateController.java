package ch.psi.ord.api;

import ch.psi.ord.core.DoiUtils;
import ch.psi.ord.core.RoCrate;
import ch.psi.ord.core.RoCrateExporter;
import ch.psi.ord.core.RoCrateImporter;
import ch.psi.ord.model.NoMetadataDescriptor;
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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
  @Produces(ExtraMediaType.APPLICATION_JSONLD)
  public Response exportMetadataDescriptor(List<String> identifiers) {
    Optional<Response> err = export(identifiers);
    if (err.isPresent()) {
      return err.get();
    }

    return Response.ok(exporter.getCrateMetadata())
        .type(ExtraMediaType.APPLICATION_JSONLD_TYPE)
        .build();
  }

  @POST
  @Path("/export")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(ExtraMediaType.APPLICATION_ZIP)
  public Response exportZip(List<String> identifiers) {
    Optional<Response> err = export(identifiers);
    if (err.isPresent()) {
      return err.get();
    }

    Optional<byte[]> zip = exporter.getZip();
    if (zip.isPresent()) {
      return Response.ok(zip.get()).type(ExtraMediaType.APPLICATION_ZIP).build();
    }

    return Response.serverError().build();
  }

  @POST
  @Path("/validate")
  @Consumes(ExtraMediaType.APPLICATION_JSONLD)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateCrate(InputStream body) {
    try (RoCrate crate = new RoCrate(body)) {
      importer.loadCrate(crate);
      return Response.ok(importer.validate()).build();
    } catch (RiotException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Failed to parse the metadata descriptor")
          .build();
    }
  }

  @POST
  @Path("/validate")
  @Consumes(ExtraMediaType.APPLICATION_ZIP)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateZippedCrate(InputStream body) {
    try (ZipInputStream zip = new ZipInputStream(body);
        RoCrate crate = new RoCrate(zip)) {
      importer.loadCrate(crate);
      return Response.ok(importer.validate()).build();
    } catch (RiotException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Failed to parse the metadata descriptor")
          .build();
    } catch (ZipException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (FileNotFoundException e) {
      return Response.status(Status.BAD_REQUEST).entity(new NoMetadataDescriptor()).build();
    } catch (IOException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Path("/import")
  @Consumes(ExtraMediaType.APPLICATION_JSONLD)
  @Produces(MediaType.APPLICATION_JSON)
  public Response importCrate(
      @HeaderParam(value = "scicat-token") String scicatToken, InputStream body) {
    if (!scicatClient.checkTokenValidity(scicatToken)) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

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
    } catch (RiotException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Failed to parse the metadata descriptor")
          .build();
    }
  }

  @POST
  @Path("/import")
  @Consumes(ExtraMediaType.APPLICATION_ZIP)
  @Produces(MediaType.APPLICATION_JSON)
  public Response importZippedCrate(
      @HeaderParam(value = "scicat-token") String scicatToken, InputStream body) {
    if (!scicatClient.checkTokenValidity(scicatToken)) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

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
    } catch (RiotException e) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Failed to parse the metadata descriptor")
          .build();
    } catch (ZipException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (FileNotFoundException e) {
      return Response.status(Status.BAD_REQUEST).entity(new NoMetadataDescriptor()).build();
    } catch (IOException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
