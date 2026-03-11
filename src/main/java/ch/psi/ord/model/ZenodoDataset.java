package ch.psi.ord.model;

import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import ch.psi.rdf.RdfResourceUri;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@RdfClass(typesUri = SchemaDO.NS + "Dataset")
public class ZenodoDataset {
  @RdfProperty(uri = SchemaDO.NS + "identifier", minCardinality = 1)
  public String identifier;

  @RdfResourceUri()
  public String getIdentifier() {
    return this.identifier;
  }

  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  public String name;

  @RdfProperty(uri = SchemaDO.NS + "description", minCardinality = 1)
  public String description;

  @RdfProperty(uri = SchemaDO.NS + "dateCreated", minCardinality = 1)
  public String dateCreated;

  @RdfProperty(uri = SchemaDO.NS + "datePublished", minCardinality = 1)
  public String datePublished;

  @RdfProperty(uri = SchemaDO.NS + "publisher", minCardinality = 1)
  public Organization publisher;

  @RdfProperty(uri = SchemaDO.NS + "creator", minCardinality = 1)
  public List<Person> creators;

  @RdfProperty(uri = SchemaDO.NS + "distribution", minCardinality = 1)
  public List<DataDownload> distribution;

  @RdfProperty(uri = SchemaDO.NS + "expires", minCardinality = 1)
  public Instant expires;
}
