package ch.psi.ord.model;

import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.vocabulary.SchemaDO;

@RdfClass(typesUri = SchemaDO.NS + "CreativeWork")
public class Publication {
  @RdfProperty(uri = SchemaDO.NS + "identifier", minCardinality = 1, maxCardinality = 1)
  String identifier;

  @RdfProperty(uri = SchemaDO.NS + "creator", minCardinality = 1)
  List<Person> creator = new ArrayList<>();

  @RdfProperty(uri = SchemaDO.NS + "title", minCardinality = 1)
  String title;

  @RdfProperty(uri = SchemaDO.NS + "publisher", minCardinality = 1)
  Organization publisher;

  @RdfProperty(uri = SchemaDO.NS + "dateCreated", minCardinality = 0)
  String dateCreated;

  @RdfProperty(uri = SchemaDO.NS + "datePublished", minCardinality = 1)
  String datePublished;

  @RdfProperty(uri = SchemaDO.NS + "dateModified", minCardinality = 0)
  String dateModified;

  @RdfProperty(uri = SchemaDO.NS + "abstract", minCardinality = 1)
  String _abstract;

  @RdfProperty(uri = SchemaDO.NS + "description", minCardinality = 1)
  String description;

  @RdfProperty(uri = SchemaDO.NS + "hasPart")
  List<Object> hasPart = new ArrayList<>();

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public List<Person> getCreator() {
    return creator;
  }

  public void setCreator(List<Person> creator) {
    this.creator = creator;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Organization getPublisher() {
    return publisher;
  }

  public void setPublisher(Organization publisher) {
    this.publisher = publisher;
  }

  public String getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(String dateCreated) {
    this.dateCreated = dateCreated;
  }

  public String getDatePublished() {
    return datePublished;
  }

  public void setDatePublished(String datePublished) {
    this.datePublished = datePublished;
  }

  public String getDateModified() {
    return dateModified;
  }

  public void setDateModified(String dateModified) {
    this.dateModified = dateModified;
  }

  public String getAbstract() {
    return _abstract;
  }

  public void setAbstract(String _abstract) {
    this._abstract = _abstract;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
