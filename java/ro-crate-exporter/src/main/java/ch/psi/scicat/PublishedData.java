package ch.psi.scicat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PublishedData(String doi, List<String> creator, String publisher, int publicationYear, String title,
        String url, @JsonProperty("abstract") String _abstract, String dataDescription, String resourceType,
        int numberOfFiles, int sizeOfArchive, List<String> pidArray, String registeredTime, String status,
        String scicatUser, String thumbnail, List<String> relatedPublications, String downloadLink,
        String updatedBy, String createdAt, String updatedAt) {
}