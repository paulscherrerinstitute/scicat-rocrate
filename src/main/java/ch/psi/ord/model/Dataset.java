package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import java.util.List;
import org.apache.jena.vocabulary.SchemaDO;

@RdfClass(typesUri = SchemaDO.NS + "Dataset")
public class Dataset {
  @RdfProperty(uri = SchemaDO.NS + "identifier")
  String identifier;

  @RdfProperty(uri = SchemaDO.NS + "name")
  String name;

  @RdfProperty(uri = SchemaDO.NS + "description")
  String description;

  @RdfProperty(uri = SchemaDO.NS + "creators")
  List<Person> creators;

  @RdfProperty(uri = SchemaDO.NS + "size")
  int size;
}
