package ch.psi.ord.model;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfProperty;
import java.time.Instant;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;

@RdfClass(typesUri = SchemaDO.NS + "DataDownload")
@Data
public class DataDownload {
  @RdfProperty(uri = SchemaDO.NS + "contentUrl")
  public String contentUrl;

  @RdfProperty(uri = SchemaDO.NS + "encodingFormat")
  public String encodingFormat;

  @RdfProperty(uri = SchemaDO.NS + "expires")
  public Instant expirationDate;
}
