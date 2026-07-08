package ch.psi.ord.model;

import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;

import ch.psi.ord.model.Publication.Parts.PartsDeserializer;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceIdentifier;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SchemaDO;

@Getter
@Setter
@RdfClass(typesUri = SchemaDO.NS + "Collection")
public class Publication {
  @RdfResourceIdentifier String resourceIdentifier;

  @RdfProperty(uri = SchemaDO.NS + "identifier", maxCardinality = 1)
  private String identifier;

  @RdfProperty(uri = SchemaDO.NS + "creator", minCardinality = 1)
  private List<Person> creator = new ArrayList<>();

  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  private String title;

  @RdfProperty(uri = SchemaDO.NS + "publisher", minCardinality = 1)
  private Organization publisher;

  @RdfProperty(uri = SchemaDO.NS + "dateCreated", minCardinality = 0)
  private String dateCreated;

  @RdfProperty(uri = SchemaDO.NS + "datePublished")
  private String datePublished;

  @RdfProperty(uri = SchemaDO.NS + "dateModified", minCardinality = 0)
  private String dateModified;

  @Accessors(prefix = "_")
  @RdfProperty(uri = SchemaDO.NS + "abstract", minCardinality = 1)
  private String _abstract;

  @RdfProperty(uri = SchemaDO.NS + "description", minCardinality = 1)
  private String description;

  @RdfProperty(uri = SchemaDO.NS + "hasPart", minCardinality = 1)
  private Parts hasPart;

  @Data
  @RdfDeserialize(using = PartsDeserializer.class)
  public static class Parts {
    private List<Dataset> datasets = new ArrayList<>();
    private Map<String, String> files = new HashMap<>();

    @Slf4j
    public static class PartsDeserializer implements RdfDeserializer<Parts> {
      @Override
      public Parts deserialize(RDFNode node, RdfDeserializationContext context)
          throws RdfDeserializationException {
        Parts result = new Parts();
        Resource subject =
            context
                .getCurrentSubject()
                .orElseThrow(() -> new RdfDeserializationException("current subject is not set"));

        Set<RDFNode> parts = listProperties(subject, SchemaDO.hasPart, (n) -> n.isResource());
        for (RDFNode n : parts) {
          Resource part = n.asResource();
          if (isOfType(part, SchemaDO.Dataset)) {

          } else if (isOfType(part, SchemaDO.MediaObject)) {
            result.files.put(part.toString(), part.getURI().replace("file://", ""));
          }
        }

        if (result.datasets.isEmpty() && result.files.isEmpty()) {
          context.addError(
              new PropertyError(
                  node.toString(),
                  SchemaDO.hasPart.getURI(),
                  "sub-tree should contain at least one 'Dataset' or 'MediaObject'"));
        }

        return result;
      }
    }
  }
}
