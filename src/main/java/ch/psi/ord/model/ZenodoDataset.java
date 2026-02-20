package ch.psi.ord.model;

import ch.psi.ord.api.ExtraMediaType;
import ch.psi.rdf.RdfClass;
import ch.psi.rdf.RdfProperty;
import ch.psi.rdf.RdfResourceUri;
import java.util.List;
import lombok.Data;
import org.apache.jena.vocabulary.SchemaDO;

@Data
@RdfClass(typesUri = SchemaDO.NS + "Dataset")
public class ZenodoDataset {
  @RdfProperty(uri = SchemaDO.NS + "identifier", minCardinality = 1)
  String identifier;

  @RdfResourceUri()
  public String getIdentifier() {
    return this.identifier;
  }

  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  String name;

  @RdfProperty(uri = SchemaDO.NS + "description", minCardinality = 1)
  String description;

  @RdfProperty(uri = SchemaDO.NS + "dateCreated", minCardinality = 1)
  String dateCreated;

  @RdfProperty(uri = SchemaDO.NS + "datePublished", minCardinality = 1)
  String datePublished;

  @RdfProperty(uri = SchemaDO.NS + "publisher", minCardinality = 1)
  Organization publisher;

  @RdfProperty(uri = SchemaDO.NS + "creator", minCardinality = 1)
  List<Person> creators;

  @RdfProperty(uri = SchemaDO.NS + "distribution", minCardinality = 1)
  List<DataDownload> distribution =
      List.of(
          new DataDownload()
              .setContentUrl("https://fixme-1-dl.psi.ch")
              .setEncodingFormat(ExtraMediaType.APPLICATION_TAR),
          new DataDownload()
              .setContentUrl("https://fixme-2-dl.psi.ch")
              .setEncodingFormat(ExtraMediaType.APPLICATION_TAR),
          new DataDownload()
              .setContentUrl("https://fixme-3-dl.psi.ch")
              .setEncodingFormat(ExtraMediaType.APPLICATION_TAR));
}
