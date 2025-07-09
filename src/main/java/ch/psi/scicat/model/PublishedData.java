package ch.psi.scicat.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// TODO: Make sure it doesn't break on big values for 'number' types
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishedData {
    @JsonProperty(value = "doi", required = true)
    String doi;
    String affiliation;
    @JsonProperty(value = "creator", required = true)
    List<String> creator;
    @JsonProperty(value = "publisher", required = true)
    String publisher;
    @JsonProperty(value = "publicationYear", required = true)
    int publicationYear;
    @JsonProperty(value = "title", required = true)
    String title;
    String url;
    @JsonProperty(value = "abstract", required = true)
    String _abstract;
    @JsonProperty(value = "dataDescription", required = true)
    String dataDescription;
    @JsonProperty(value = "resourceType", required = true)
    String resourceType;
    long numberOfFiles;
    long sizeOfArchive;
    @JsonProperty(value = "pidArray", required = true)
    List<String> pidArray = new ArrayList<>();
    List<String> authors;
    String registeredTime;
    String status;
    String scicatUser;
    String thumbnail;
    List<String> relatedPublications;
    String downloadLink;
    String createdBy;
    String updatedBy;
    String createdAt;
    String updatedAt;

    public PublishedData() {
    }

    private PublishedData(PublishedDataBuilder builder) {
        this.doi = builder.doi;
        this.affiliation = builder.affiliation;
        this.creator = builder.creator;
        this.publisher = builder.publisher;
        this.publicationYear = builder.publicationYear;
        this.title = builder.title;
        this.url = builder.url;
        this._abstract = builder._abstract;
        this.dataDescription = builder.dataDescription;
        this.resourceType = builder.resourceType;
        this.numberOfFiles = builder.numberOfFiles;
        this.sizeOfArchive = builder.sizeOfArchive;
        this.pidArray = builder.pidArray;
        this.authors = builder.authors;
        this.registeredTime = builder.registeredTime;
        this.status = builder.status;
        this.scicatUser = builder.scicatUser;
        this.thumbnail = builder.thumbnail;
        this.relatedPublications = builder.relatedPublications;
        this.downloadLink = builder.downloadLink;
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static class PublishedDataBuilder {
        public String affiliation;
        public List<String> authors;
        private String doi;
        private List<String> creator;
        private String publisher;
        private int publicationYear;
        private String title;
        private String url;
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
        private String createdBy;
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

        public void addCreator(String creator) {
            if (this.creator == null) {
                this.creator = new ArrayList<>();
            }

            this.creator.add(creator);
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

        public PublishedDataBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
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
            PublishedData result = new PublishedData(this);

            return result;
            // }
        }

        public PublishedDataBuilder affiliation(String affiliation) {
            this.affiliation = affiliation;
            return this;
        }

        public PublishedDataBuilder authors(List<String> authors) {
            this.authors = authors;
            return this;
        }
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public List<String> getCreator() {
        return creator;
    }

    public void setCreator(List<String> creator) {
        this.creator = creator;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public String getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(String dataDescription) {
        this.dataDescription = dataDescription;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public long getSizeOfArchive() {
        return sizeOfArchive;
    }

    public void setSizeOfArchive(long sizeOfArchive) {
        this.sizeOfArchive = sizeOfArchive;
    }

    public List<String> getPidArray() {
        return pidArray;
    }

    public void setPidArray(List<String> pidArray) {
        this.pidArray = pidArray;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getRegisteredTime() {
        return registeredTime;
    }

    public void setRegisteredTime(String registeredTime) {
        this.registeredTime = registeredTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScicatUser() {
        return scicatUser;
    }

    public void setScicatUser(String scicatUser) {
        this.scicatUser = scicatUser;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<String> getRelatedPublications() {
        return relatedPublications;
    }

    public void setRelatedPublications(List<String> relatedPublications) {
        this.relatedPublications = relatedPublications;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
