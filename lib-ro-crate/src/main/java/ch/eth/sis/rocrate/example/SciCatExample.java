package ch.eth.sis.rocrate.example;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.eth.sis.rocrate.SchemaFacade;
import ch.eth.sis.rocrate.facade.IMetadataEntry;
import ch.eth.sis.rocrate.facade.ISchemaFacade;
import ch.eth.sis.rocrate.facade.LiteralType;
import ch.eth.sis.rocrate.facade.MetadataEntry;
import ch.eth.sis.rocrate.facade.RdfsClass;
import ch.eth.sis.rocrate.facade.TypeProperty;
import edu.kit.datamanager.ro_crate.RoCrate.RoCrateBuilder;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;

public class SciCatExample {
    public static void main(String[] args) throws JsonProcessingException {
        RoCrateBuilder roCrateBuilder = new RoCrateBuilder();

        ISchemaFacade schemaFacade = SchemaFacade.of(roCrateBuilder.build());

        {
            RdfsClass publishedDataClass = new RdfsClass();
            publishedDataClass.setId("scicat:PublishedData");
            publishedDataClass.setSubClassOf(List.of("Thing"));
            schemaFacade.addType(publishedDataClass);

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:doi");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:creator");
                property.setTypes(List.of(LiteralType.STRING));
                property.setOntologicalAnnotations(List.of("datacite:creatorName"));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:publisher");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:publicationYear");
                property.setTypes(List.of(LiteralType.INTEGER));
                property.setOntologicalAnnotations(List.of("datacite:publicationYear"));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:title");
                property.setTypes(List.of(LiteralType.STRING));
                property.setOntologicalAnnotations(List.of("datacite:title"));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:url");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:abstract");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:dataDescription");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:resourceType");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:numberOfFiles");
                property.setTypes(List.of(LiteralType.INTEGER));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:sizeOfArchive");
                property.setTypes(List.of(LiteralType.INTEGER));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:pidArray");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:registeredTime");
                property.setTypes(List.of(LiteralType.DATETIME));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:status");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:scicatUser");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:thumbnail");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:relatedPublications");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:downloadLink");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:updatedBy");
                property.setTypes(List.of(LiteralType.STRING));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:createdAt");
                property.setTypes(List.of(LiteralType.DATETIME));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }

            {
                TypeProperty property = new TypeProperty();
                property.setId("scicat:updatedAt");
                property.setTypes(List.of(LiteralType.DATETIME));
                publishedDataClass.addProperty(property);
                schemaFacade.addPropertyType(property);
            }
        }

        {
            try {
                ScicatClient client = new ScicatClient("https://dacat.psi.ch/api/v3");
                client.getPublishedData("10.16907%2F808de0df-a9d3-4698-8e9f-d6e091516650")
                        .ifPresent(publishedData -> {
                            Map<String, Serializable> jsonld = Map.ofEntries(
                                    Map.entry("scicat:doi", publishedData.doi),
                                    Map.entry("scicat:creator",
                                            (Serializable) publishedData.creator),
                                    Map.entry("scicat:publisher", publishedData.publisher),
                                    Map.entry("scicat:publicationYear", publishedData.publicationYear),
                                    Map.entry("scicat:title", publishedData.title),
                                    Map.entry("scicat:url", publishedData.url),
                                    Map.entry("scicat:abstract", publishedData._abstract),
                                    Map.entry("scicat:dataDescription", publishedData.dataDescription),
                                    Map.entry("scicat:resourceType", publishedData.resourceType),
                                    Map.entry("scicat:numberOfFiles",
                                            publishedData.numberOfFiles != null ? publishedData.numberOfFiles : 0),
                                    Map.entry("scicat:sizeOfArchive",
                                            publishedData.sizeOfArchive != null ? publishedData.sizeOfArchive : 0),
                                    Map.entry("scicat:pidArray",
                                            (Serializable) publishedData.pidArray),
                                    Map.entry("scicat:registeredTime", publishedData.registeredTime),
                                    Map.entry("scicat:status", publishedData.status),
                                    Map.entry("scicat:scicatUser", publishedData.scicatUser),
                                    // Map.entry("scicat:thumbnail", (Serializable)
                                    // publishedData.thumbnail),
                                    Map.entry("scicat:relatedPublications",
                                            (Serializable) publishedData.relatedPublications),
                                    Map.entry("scicat:downloadLink",
                                            publishedData.downloadLink != null ? publishedData.downloadLink : ""),
                                    Map.entry("scicat:updatedBy",
                                            publishedData.updatedBy),
                                    Map.entry("scicat:createdAt",
                                            publishedData.createdAt),
                                    Map.entry("scicat:updatedAt",
                                            publishedData.updatedAt));

                            IMetadataEntry entry = new MetadataEntry(publishedData.doi,
                                    "scicat:PublishedData", jsonld, Map.of());
                            schemaFacade.addEntry(entry);
                        });
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

        }

        String path = args.length >= 1 ? args[0] : "out";

        roCrateBuilder.build();

        FolderWriter folderWriter = new FolderWriter();
        folderWriter.save(roCrateBuilder.build(), path);
    }

    public static class ScicatClient {
        private String baseUrl;
        private HttpClient client = HttpClient.newHttpClient();
        private ObjectMapper mapper = new ObjectMapper();

        public ScicatClient(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Optional<PublishedData> getPublishedData(String doi)
                throws URISyntaxException, IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(baseUrl + "/publisheddata/" + doi)).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                PublishedData publishedData = mapper.readValue(response.body(), PublishedData.class);
                return Optional.of(publishedData);
            } else {
                System.err.println(
                        "Failed to retreive PublishedData associated with the following DOI: "
                                + doi);
                System.err.println("HTTP Status code: " + response.statusCode());
                System.err.println("HTTP message: " + response.body());

                return Optional.empty();
            }
        }
    }

    public record PublishedData(String doi, List<String> creator, String publisher,
            String publicationYear, String title, String url,
            @JsonProperty("abstract") String _abstract, String dataDescription, String resourceType,
            String numberOfFiles, String sizeOfArchive, List<String> pidArray,
            String registeredTime, String status, String scicatUser, String thumbnail,
            List<String> relatedPublications, String downloadLink, String updatedBy,
            String createdAt, String updatedAt) {
    }
}
