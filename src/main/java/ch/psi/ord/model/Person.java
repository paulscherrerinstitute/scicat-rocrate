package ch.psi.ord.model;

import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.vocabulary.SchemaDO;

@Getter
@Setter
@RdfClass(typesUri = SchemaDO.NS + "Person")
public class Person {
  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  public String name;

  @RdfProperty(uri = SchemaDO.NS + "givenName")
  public String givenName;

  @RdfProperty(uri = SchemaDO.NS + "familyName")
  public String familyName;
}
