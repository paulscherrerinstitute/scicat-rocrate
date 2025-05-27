package ch.psi.scicat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LangJSONLD11;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestHeader;

import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.LRUDocumentCache;
import com.apicatalog.jsonld.uri.UriValidationPolicy;

import ch.psi.scicat.model.PublishedData;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("ro-crate")
@Tag(name = "ro-crate")
public class RoCrateController {
    private int cacheSize = 0;

    private static final Logger LOG = Logger.getLogger(RoCrateController.class);

    @Inject
    RoCrateExporter exporter;

    @Inject
    RoCrateImporter importer;

    // Cache JSON-LD remote documents across requests
    LRUDocumentCache documentLoader;

    private JsonLdOptions jsonLdOptions = new JsonLdOptions();

    private static final Pattern DOI_PATTERN = Pattern
            .compile("(10[.][0-9]{2,}(?:[.][0-9]+)*/(?:(?![%" + "\"#? ])\\S)+)");

    // TODO: move to a global file?
    public static boolean isDoi(String input) {
        return DOI_PATTERN.matcher(input).matches();
    }

    public RoCrateController(RoCrateExporter exporter, RoCrateImporter importer,
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
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA })
    @Produces({ ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP })
    public Response exportPublication(@RestHeader("Accept") List<MediaType> acceptHeader,
            @RestHeader("Content-Type") MediaType contentTypeHeader,
            List<String> identifiers) {
        if (acceptHeader.contains(ExtraMediaType.APPLICATION_ZIP_TYPE)
                && !acceptHeader.contains(ExtraMediaType.APPLICATION_JSONLD_TYPE)) {
            return Response.status(Status.NOT_IMPLEMENTED)
                    .entity("Export to ZIP file is not supported yet")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        if (identifiers.stream().anyMatch(id -> !isDoi(id))) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Identifiers other than DOI are not accepted for now")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        // FIXME: Will need to add other types
        try {
            exporter.addPublications(identifiers);
        } catch (ClientWebApplicationException e) {
            Response res = e.getResponse();
            return Response
                    .status(res.getStatus())
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
    @Consumes({ ExtraMediaType.APPLICATION_JSONLD, ExtraMediaType.APPLICATION_ZIP })
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateRoCrate(InputStream inputStream) {
        Model model;
        try {
            model = RDFParser.create()
                    .source(inputStream)
                    .lang(Lang.JSONLD11)
                    .base("file:///")
                    .context(org.apache.jena.sparql.util.Context.create().set(LangJSONLD11.JSONLD_OPTIONS,
                            jsonLdOptions))
                    .build()
                    .toModel();
            importer.loadModel(model);
        } catch (RiotException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        ValidationReport report = importer.validate();

        return Response.ok(report).build();
    }

    @POST
    @Path("/import")
    @Consumes(ExtraMediaType.APPLICATION_JSONLD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importRoCrate(InputStream inputStream) {
        Model model = RDFParser.create()
                .source(inputStream)
                .lang(Lang.JSONLD11)
                .base("file:///")
                .context(org.apache.jena.sparql.util.Context.create().set(LangJSONLD11.JSONLD_OPTIONS, jsonLdOptions))
                .build()
                .toModel();

        importer.loadModel(model);
        ValidationReport report = importer.validate();
        Map<String, String> importMap = new HashMap<>();
        report.getEntities().forEach(e -> {
            if (e.object() instanceof PublishedData publishedData) {
                // TODO: create the objects
                importMap.put(e.id(), "NOT CREATED");
            }
        });

        return Response
                .status(Status.CREATED)
                .entity(importMap)
                .build();
    }
}
