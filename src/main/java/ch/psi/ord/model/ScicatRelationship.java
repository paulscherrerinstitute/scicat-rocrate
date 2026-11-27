package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import lombok.Data;

@Data
@RdfClass(typesUri = "https://scicatproject.org/Relationship")
public class ScicatRelationship {
  @RdfProperty(uri = "https://scicatproject.org/identifier", minCardinality = 1)
  private String identifier;

  @RdfProperty(uri = "https://scicatproject.org/identifierType")
  private String identifierType;

  @RdfProperty(uri = "https://scicatproject.org/relationship")
  private String relationship;

  @RdfProperty(uri = "https://scicatproject.org/entityType")
  private String entityType;

  @RdfProperty(uri = "https://scicatproject.org/externalId")
  private String externalId;
}
