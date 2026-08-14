package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceIdentifier;
import java.util.List;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@RdfClass(typesUri = SchemaDO.NS + "Dataset")
public class Dataset {
  @RdfResourceIdentifier String resourceIdentifier;

  @RdfProperty(uri = SchemaDO.NS + "identifier")
  String identifier;

  @RdfProperty(uri = SchemaDO.NS + "name")
  String name;

  @RdfProperty(uri = SchemaDO.NS + "description")
  String description;

  @RdfProperty(uri = SchemaDO.NS + "creator")
  List<Person> creator;

  @RdfProperty(uri = SchemaDO.NS + "size")
  int size;

  @RdfProperty(uri = SchemaDO.NS + "variableMeasured")
  List<PropertyValue> variableMeasured;
}
