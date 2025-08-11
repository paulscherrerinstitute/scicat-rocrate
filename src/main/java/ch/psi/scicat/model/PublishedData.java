package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

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

  public String getDoi() {
    return doi;
  }

  public PublishedData setDoi(String doi) {
    this.doi = doi;
    return this;
  }

  public String getAffiliation() {
    return affiliation;
  }

  public PublishedData setAffiliation(String affiliation) {
    this.affiliation = affiliation;
    return this;
  }

  public List<String> getCreator() {
    return creator;
  }

  public PublishedData setCreator(List<String> creator) {
    this.creator = creator;
    return this;
  }

  public String getPublisher() {
    return publisher;
  }

  public PublishedData setPublisher(String publisher) {
    this.publisher = publisher;
    return this;
  }

  public int getPublicationYear() {
    return publicationYear;
  }

  public PublishedData setPublicationYear(int publicationYear) {
    this.publicationYear = publicationYear;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public PublishedData setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public PublishedData setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getAbstract() {
    return _abstract;
  }

  public PublishedData setAbstract(String _abstract) {
    this._abstract = _abstract;
    return this;
  }

  public String getDataDescription() {
    return dataDescription;
  }

  public PublishedData setDataDescription(String dataDescription) {
    this.dataDescription = dataDescription;
    return this;
  }

  public String getResourceType() {
    return resourceType;
  }

  public PublishedData setResourceType(String resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  public long getNumberOfFiles() {
    return numberOfFiles;
  }

  public PublishedData setNumberOfFiles(long numberOfFiles) {
    this.numberOfFiles = numberOfFiles;
    return this;
  }

  public long getSizeOfArchive() {
    return sizeOfArchive;
  }

  public PublishedData setSizeOfArchive(long sizeOfArchive) {
    this.sizeOfArchive = sizeOfArchive;
    return this;
  }

  public List<String> getPidArray() {
    return pidArray;
  }

  public PublishedData setPidArray(List<String> pidArray) {
    this.pidArray = pidArray;
    return this;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public PublishedData setAuthors(List<String> authors) {
    this.authors = authors;
    return this;
  }

  public String getRegisteredTime() {
    return registeredTime;
  }

  public PublishedData setRegisteredTime(String registeredTime) {
    this.registeredTime = registeredTime;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public PublishedData setStatus(String status) {
    this.status = status;
    return this;
  }

  public String getScicatUser() {
    return scicatUser;
  }

  public PublishedData setScicatUser(String scicatUser) {
    this.scicatUser = scicatUser;
    return this;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public PublishedData setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
    return this;
  }

  public List<String> getRelatedPublications() {
    return relatedPublications;
  }

  public PublishedData setRelatedPublications(List<String> relatedPublications) {
    this.relatedPublications = relatedPublications;
    return this;
  }

  public String getDownloadLink() {
    return downloadLink;
  }

  public PublishedData setDownloadLink(String downloadLink) {
    this.downloadLink = downloadLink;
    return this;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public PublishedData setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public PublishedData setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public PublishedData setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public PublishedData setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
