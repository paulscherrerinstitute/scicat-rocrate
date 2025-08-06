package ch.psi.ord.model;

import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import ch.psi.rdf.RdfResourceUri;
import org.apache.jena.vocabulary.SchemaDO;

@RdfClass(typesUri = SchemaDO.NS + "Organization")
public class Organization {
  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  String name;

  public String getName() {
    return this.name;
  }

  public Organization setName(String name) {
    this.name = name;
    return this;
  }

  @RdfResourceUri()
  String generateId() {
    if (this == PSI) {
      return "https://ror.org/03eh3y714";
    }

    return null;
  }

  public static Organization PSI = new Organization().setName("Paul Scherrer Institute");
}
