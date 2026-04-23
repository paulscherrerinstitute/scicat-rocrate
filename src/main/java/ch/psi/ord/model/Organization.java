package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceUri;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.vocabulary.SchemaDO;

@Getter
@Setter
@RdfClass(typesUri = SchemaDO.NS + "Organization")
public class Organization {
  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  public String name;

  @RdfResourceUri()
  public String generateId() {
    if (this == PSI) {
      return "https://ror.org/03eh3y714";
    }

    return null;
  }

  public static Organization PSI = new Organization().setName("Paul Scherrer Institute");
}
