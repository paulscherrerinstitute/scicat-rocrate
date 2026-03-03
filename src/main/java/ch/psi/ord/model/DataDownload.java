package ch.psi.ord.model;

import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;

@RdfClass(typesUri = SchemaDO.NS + "DataDownload")
@Data
public class DataDownload {
  @RdfProperty(uri = SchemaDO.NS + "contentUrl")
  public String contentUrl;

  @RdfProperty(uri = SchemaDO.NS + "encodingFormat")
  public String encodingFormat;
}
