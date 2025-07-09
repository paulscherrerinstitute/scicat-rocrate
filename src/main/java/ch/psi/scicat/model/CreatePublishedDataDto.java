package ch.psi.scicat.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePublishedDataDto {
    @JsonProperty(value = "doi", required = true)
    private String doi;
    private String affiliation;
    @JsonProperty(value = "creator", required = true)
    private List<String> creator;
    @JsonProperty(value = "publisher", required = true)
    private String publisher;
    @JsonProperty(value = "publicationYear", required = true)
    private int publicationYear;
    private String title;
    @JsonProperty(value = "abstract", required = true)
    private String _abstract;
    private String url;
    @JsonProperty(value = "dataDescription", required = true)
    private String dataDescription;
    @JsonProperty(value = "resourceType", required = true)
    private String resourceType;
    private int numberOfFiles;
    private double sizeOfArchive;
    @JsonProperty(value = "pidArray", required = true)
    private List<String> pidArray = new ArrayList<>();
    private List<String> authors;
    private String registeredTime;
    private String status;
    private String scicatUser;
    private String thumbnail;
    private List<String> relatedPublications;
    private String downloadLink;

    public CreatePublishedDataDto() {
    }

    private CreatePublishedDataDto(Builder builder) {
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

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public double getSizeOfArchive() {
        return sizeOfArchive;
    }

    public void setSizeOfArchive(double sizeOfArchive) {
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

    public static class Builder {
        private String doi;
        private String affiliation;
        private List<String> creator = new ArrayList<>();
        private String publisher;
        private int publicationYear;
        private String title;
        private String _abstract;
        private String url;
        private String dataDescription;
        private String resourceType;
        private int numberOfFiles;
        private double sizeOfArchive;
        private List<String> pidArray = new ArrayList<>();
        private List<String> authors = new ArrayList<>();
        private String registeredTime;
        private String status;
        private String scicatUser;
        private String thumbnail;
        private List<String> relatedPublications = new ArrayList<>();
        private String downloadLink;

        public Builder doi(String doi) {
            this.doi = doi;
            return this;
        }

        public Builder affiliation(String affiliation) {
            this.affiliation = affiliation;
            return this;
        }

        public Builder creator(String creator) {
            this.creator.add(creator);
            return this;
        }

        public Builder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder publicationYear(int publicationYear) {
            this.publicationYear = publicationYear;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder _abstract(String _abstract) {
            this._abstract = _abstract;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder dataDescription(String dataDescription) {
            this.dataDescription = dataDescription;
            return this;
        }

        public Builder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder numberOfFiles(int numberOfFiles) {
            this.numberOfFiles = numberOfFiles;
            return this;
        }

        public Builder sizeOfArchive(double sizeOfArchive) {
            this.sizeOfArchive = sizeOfArchive;
            return this;
        }

        public Builder pid(String pid) {
            this.pidArray.add(pid);
            return this;
        }

        public Builder author(String author) {
            this.authors.add(author);
            return this;
        }

        public Builder registeredTime(String registeredTime) {
            this.registeredTime = registeredTime;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder scicatUser(String scicatUser) {
            this.scicatUser = scicatUser;
            return this;
        }

        public Builder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder relatedPublication(String relatedPublication) {
            this.relatedPublications.add(relatedPublication);
            return this;
        }

        public Builder downloadLink(String downloadLink) {
            this.downloadLink = downloadLink;
            return this;
        }

        public CreatePublishedDataDto build() {
            return new CreatePublishedDataDto(this);
        }
    }

}
