package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceUri;
import java.util.List;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@RdfClass(typesUri = SchemaDO.NS + "Dataset")
public class ScicatDataset {
  @RdfResourceUri String id;

  @RdfProperty(uri = SchemaDO.NS + "name")
  String name;

  @RdfProperty(uri = SchemaDO.NS + "description")
  String description;

  @RdfProperty(uri = SchemaDO.NS + "owner")
  List<Person> owner;

  @RdfProperty(uri = SchemaDO.NS + "creator")
  List<Person> creator;

  @RdfProperty(uri = SchemaDO.NS + "variableMeasured")
  List<PropertyValue> scientificMetadata;

  @RdfProperty(uri = SchemaDO.NS + "keywords")
  List<String> keywords;
}
