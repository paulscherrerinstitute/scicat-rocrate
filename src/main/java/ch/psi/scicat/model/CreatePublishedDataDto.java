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
}
