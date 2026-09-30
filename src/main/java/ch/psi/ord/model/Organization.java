package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceIdentifier;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.vocabulary.SchemaDO;

@Getter
@Setter
@RdfClass(typesUri = SchemaDO.NS + "Organization")
public class Organization {
  @RdfResourceIdentifier String resourceIdentifier;

  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  public String name;

  @RdfResourceIdentifier()
  public String generateId() {
    return resourceIdentifier;
  }
}
