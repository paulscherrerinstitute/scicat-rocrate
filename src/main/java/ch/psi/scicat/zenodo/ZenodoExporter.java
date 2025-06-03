package ch.psi.scicat.zenodo;

import java.util.List;

import org.apache.jena.vocabulary.SchemaDO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import ch.psi.scicat.ExtraMediaType;
import ch.psi.scicat.ScicatClient;
import ch.psi.scicat.model.PublishedData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@ApplicationScoped
public class ZenodoExporter {
    @Inject
    ScicatClient scicatClient;

    @Inject
    @ConfigProperty(name = "quarkus.rest-client.\"ch.psi.scicat.ScicatService\".url")
    String scicatServiceUrl;

    public JsonObject exportPublication(String doi) {
        return toZenodoJsonLd(scicatClient.getPublishedDataById(doi).getEntity());
    }

    public JsonObject toZenodoJsonLd(PublishedData publishedData) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("@context", SchemaDO.NS);
        builder.add("@type", SchemaDO.Dataset.getURI());

        String doiUrl = String.format("https://doi.org/%s", publishedData.doi());
        builder.add("@id", doiUrl);
        builder.add(SchemaDO.identifier.getLocalName(), doiUrl);
        builder.add(SchemaDO.identifier.getLocalName(), doiUrl);

        builder.add(SchemaDO.name.getLocalName(), publishedData.title());
        builder.add(SchemaDO.dateCreated.getLocalName(), publishedData.createdAt());
        builder.add(SchemaDO.datePublished.getLocalName(), publishedData.registeredTime());
        builder.add(SchemaDO.dateModified.getLocalName(), publishedData.updatedAt());
        // FIXME: for now we only support one hardcoded license
        builder.add(SchemaDO.license.getLocalName(), "https://creativecommons.org/licenses/by-sa/4.0/");
        builder.add(SchemaDO.description.getLocalName(), publishedData._abstract());

        builder.add(SchemaDO.creator.getLocalName(), Json.createArrayBuilder(
                publishedData
                        .creator()
                        .stream()
                        .map(creatorName -> Json.createObjectBuilder()
                                .add("@type", SchemaDO.Person.getLocalName())
                                .add(SchemaDO.name.getLocalName(), creatorName)
                                .build())
                        .toList()));

        // FIXME: for now we only have one publisher
        builder.add(SchemaDO.publisher.getLocalName(),
                Json.createObjectBuilder()
                        .add("@type", SchemaDO.Organization.getLocalName())
                        .add(SchemaDO.name.getLocalName(), "Paul Scherrer Institute")
                        .build());

        builder.add(SchemaDO.url.getLocalName(),
                String.format("%s/publisheddata/%s", scicatServiceUrl, publishedData.doi().replace("/", "%2f")));

        builder.add(SchemaDO.distribution.getLocalName(), Json.createArrayBuilder(
                // TODO: How to get the actual URLs
                List.of("https://fixme-1-dl.psi.ch", "https://fixme-2-dl.psi.ch", "https://fixme-3-dl.psi.ch")
                        .stream()
                        .map(contentUrl -> Json.createObjectBuilder()
                                .add("@type", SchemaDO.DataDownload.getLocalName())
                                .add(SchemaDO.contentUrl.getLocalName(), contentUrl)
                                // FIXME: we expect only tarballs for now
                                .add(SchemaDO.encodingFormat.getLocalName(), ExtraMediaType.APPLICATION_TAR)
                                .build())
                        .toList()));

        return builder.build();
    }
}