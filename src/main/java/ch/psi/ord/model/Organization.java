package ch.psi.ord.model;

import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import ch.psi.rdf.RdfResourceUri;
import org.apache.jena.vocabulary.SchemaDO;

@RdfClass(typesUri = SchemaDO.NS + "Organization")
public class Organization {
  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  String name;

  public Organization() {}

  public Organization(String name) {
    this.name = name;
  }

  @RdfResourceUri()
  String generateId() {
    if (this == PSI) {
      return "https://ror.org/03eh3y714";
    }

    return null;
  }

  public static Organization PSI = new Organization("Paul Scherrer Institute");
}
