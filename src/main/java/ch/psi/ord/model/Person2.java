package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@RdfClass(typesUri = SchemaDO.NS + "Person")
public class Person2 {
  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  public String name;

  @RdfProperty(uri = SchemaDO.NS + "givenName", minCardinality = 1)
  public String givenName;

  @RdfProperty(uri = SchemaDO.NS + "familyName", minCardinality = 1)
  public String familyName;

  @RdfProperty(uri = SchemaDO.NS + "email", minCardinality = 1)
  public String email;
}
