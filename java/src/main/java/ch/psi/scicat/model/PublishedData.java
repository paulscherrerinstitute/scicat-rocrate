package ch.psi.scicat.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// TODO: Make sure it doesn't break on big values for 'number' types
@JsonIgnoreProperties(ignoreUnknown = true)
public record PublishedData(
        @JsonProperty(value = "doi", required = true) String doi,
        @JsonProperty(value = "creator", required = true) List<String> creator,
        @JsonProperty(value = "publisher", required = true) String publisher,
        @JsonProperty(value = "publicationYear", required = true) int publicationYear,
        @JsonProperty(value = "title", required = true) String title,
        @JsonProperty(value = "abstract", required = true) String _abstract,
        @JsonProperty(value = "dataDescription", required = true) String dataDescription,
        @JsonProperty(value = "resourceType", required = true) String resourceType,
        @JsonProperty(value = "pidArray", required = true) List<String> pidArray,
        String url,
        long numberOfFiles,
        long sizeOfArchive,
        String registeredTime,
        String status,
        String scicatUser,
        String thumbnail,
        List<String> relatedPublications,
        String downloadLink,
        String updatedBy,
        String createdAt,
        String updatedAt) {

    private PublishedData(PublishedDataBuilder builder) {
        this(
                builder.doi,
                builder.creator,
                builder.publisher,
                builder.publicationYear,
                builder.title,
                builder._abstract,
                builder.dataDescription,
                builder.resourceType,
                builder.pidArray,
                builder.url,
                builder.numberOfFiles,
                builder.sizeOfArchive,
                builder.registeredTime,
                builder.status,
                builder.scicatUser,
                builder.thumbnail,
                builder.relatedPublications,
                builder.downloadLink,
                builder.updatedBy,
                builder.createdAt,
                builder.updatedAt);
    }

    public static class PublishedDataBuilder {
        private String doi;
        private List<String> creator;
        private String publisher;
        private int publicationYear;
        private String title;
        private String url;
        @JsonProperty("abstract")
        String _abstract;
        private String dataDescription;
        private String resourceType;
        private long numberOfFiles;
        private long sizeOfArchive;
        private List<String> pidArray;
        private String registeredTime;
        private String status;
        private String scicatUser;
        private String thumbnail;
        private List<String> relatedPublications;
        private String downloadLink;
        private String updatedBy;
        private String createdAt;
        private String updatedAt;

        public PublishedDataBuilder doi(String doi) {
            this.doi = doi;
            return this;
        }

        public PublishedDataBuilder creator(List<String> creator) {
            this.creator = creator;
            return this;
        }

        public PublishedDataBuilder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }

        public PublishedDataBuilder publicationYear(int publicationYear) {
            this.publicationYear = publicationYear;
            return this;
        }

        public PublishedDataBuilder title(String title) {
            this.title = title;
            return this;
        }

        public PublishedDataBuilder url(String url) {
            this.url = url;
            return this;
        }

        public PublishedDataBuilder _abstract(String _abstract) {
            this._abstract = _abstract;
            return this;
        }

        public PublishedDataBuilder dataDescription(String dataDescription) {
            this.dataDescription = dataDescription;
            return this;
        }

        public PublishedDataBuilder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public PublishedDataBuilder numberOfFiles(long numberOfFiles) {
            this.numberOfFiles = numberOfFiles;
            return this;
        }

        public PublishedDataBuilder sizeOfArchive(long sizeOfArchive) {
            this.sizeOfArchive = sizeOfArchive;
            return this;
        }

        public PublishedDataBuilder pidArray(List<String> pidArray) {
            this.pidArray = pidArray;
            return this;
        }

        public PublishedDataBuilder registeredTime(String registeredTime) {
            this.registeredTime = registeredTime;
            return this;
        }

        public PublishedDataBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PublishedDataBuilder scicatUser(String scicatUser) {
            this.scicatUser = scicatUser;
            return this;
        }

        public PublishedDataBuilder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public PublishedDataBuilder relatedPublications(List<String> relatedPublications) {
            this.relatedPublications = relatedPublications;
            return this;
        }

        public PublishedDataBuilder downloadLink(String downloadLink) {
            this.downloadLink = downloadLink;
            return this;
        }

        public PublishedDataBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public PublishedDataBuilder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PublishedDataBuilder updatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PublishedData build() throws IllegalStateException {
            // if (doi == null) {
            // throw new IllegalStateException("PublishedData is missing required property
            // 'doi'");
            // } else if (creator == null) {
            // throw new IllegalStateException("PublishedData is missing required property
            // 'creator'");
            // } else if (publisher == null) {
            // throw new IllegalStateException("PublishedData is missing required property
            // 'publisher'");
            // } else if (title == null) {
            // throw new IllegalStateException("PublishedData is missing required property
            // 'title'");
            // } else if (_abstract == null) {
            // throw new IllegalStateException("PublishedData is missing required property
            // 'abstract'");
            // } else if (pidArray == null) {
            // throw new IllegalStateException("PublishedData is missing required property
            // 'pidArray'");
            // } else {
            return new PublishedData(this);
            // }
        }
    }
}
