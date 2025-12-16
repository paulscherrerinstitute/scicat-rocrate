package ch.psi.ord.model;

import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.jena.vocabulary.SchemaDO;

@Getter
@Setter
@RdfClass(typesUri = SchemaDO.NS + "Collection")
public class Publication {
  @RdfProperty(uri = SchemaDO.NS + "identifier", minCardinality = 1, maxCardinality = 1)
  private String identifier;

  @RdfProperty(uri = SchemaDO.NS + "creator", minCardinality = 1)
  private List<Person> creator = new ArrayList<>();

  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  private String title;

  @RdfProperty(uri = SchemaDO.NS + "publisher", minCardinality = 1)
  private Organization publisher;

  @RdfProperty(uri = SchemaDO.NS + "dateCreated", minCardinality = 0)
  private String dateCreated;

  @RdfProperty(uri = SchemaDO.NS + "datePublished", minCardinality = 1)
  private String datePublished;

  @RdfProperty(uri = SchemaDO.NS + "dateModified", minCardinality = 0)
  private String dateModified;

  @Accessors(prefix = "_")
  @RdfProperty(uri = SchemaDO.NS + "abstract", minCardinality = 1)
  private String _abstract;

  @RdfProperty(uri = SchemaDO.NS + "description", minCardinality = 1)
  private String description;

  @RdfProperty(uri = SchemaDO.NS + "hasPart")
  private List<Object> hasPart = new ArrayList<>();
}
