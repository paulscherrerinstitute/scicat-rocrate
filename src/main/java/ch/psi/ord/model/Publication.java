package ch.psi.ord.model;

import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;

import ch.psi.ord.model.Publication.Parts.PartsDeserializer;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
  @RdfProperty(uri = SchemaDO.NS + "identifier", minCardinality = 1, maxCardinality = 1)
  private String identifier;

  @RdfProperty(uri = SchemaDO.NS + "creator", minCardinality = 1)
  private List<Person> creator = new ArrayList<>();

  @RdfProperty(uri = SchemaDO.NS + "name", minCardinality = 1)
  private String title;

  @RdfProperty(uri = SchemaDO.NS + "publisher", minCardinality = 1)
  private Organization publisher;

  @RdfProperty(uri = SchemaDO.NS + "dateCreated", minCardinality = 0)
  private String dateCreated;

  @RdfProperty(uri = SchemaDO.NS + "datePublished", minCardinality = 1)
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

        collectDataEntries(subject, result, context);

        if (result.datasets.isEmpty() && result.files.isEmpty()) {
          context.addError(
              new PropertyError(
                  subject.toString(),
                  SchemaDO.hasPart.getURI(),
                  String.format(
                      "sub-tree should contain at least one '%s' or '%s'",
                      SchemaDO.Dataset, SchemaDO.MediaObject)));
        }

        return result;
      }

      private void collectDataEntries(
          Resource subject, Parts result, RdfDeserializationContext context)
          throws RdfDeserializationException {
        Set<Resource> parts =
            listProperties(subject, SchemaDO.hasPart).stream()
                .filter(node -> node.isResource())
                .map(node -> node.asResource())
                .collect(Collectors.toSet());

        for (Resource r : parts) {
          if (isOfType(r, SchemaDO.Dataset)) {
            Dataset d = context.getDeserializer(Dataset.class).deserialize(r, context);
            result.datasets.add(d);
          } else if (isOfType(r, SchemaDO.MediaObject)) {
            result.files.putIfAbsent(r.toString(), r.getURI().replace("file://", ""));
          } else {
            collectDataEntries(r, result, context);
          }
        }
      }
    }
  }
}
