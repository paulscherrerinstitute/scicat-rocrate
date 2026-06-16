package ch.psi.ord.model;

import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;

import ch.psi.ord.model.RootDataset.RootDatasetDeserializer;
import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SchemaDO;

@RdfClass(typesUri = SchemaDO.NS + "Dataset")
@RdfDeserialize(using = RootDatasetDeserializer.class)
public class RootDataset {
  @RdfProperty(uri = SchemaDO.NS + "hasPart", minCardinality = 1)
  @Getter
  HashMap<Resource, Publication> hasPart = new HashMap<>();

  public static class RootDatasetDeserializer implements RdfDeserializer<RootDataset> {
    RdfMapper rdfMapper = new RdfMapper();

    @Override
    public RootDataset deserialize(RDFNode node, RdfDeserializationContext context)
        throws RdfDeserializationException {
      RootDataset result = new RootDataset();
      List<Resource> publications =
          listProperties(node.asResource(), SchemaDO.hasPart).stream()
              .filter(n -> n.isResource() && isOfType(n.asResource(), SchemaDO.Collection))
              .map(RDFNode::asResource)
              .toList();

      for (Resource r : publications) {
        DeserializationReport<Publication> subreport = rdfMapper.deserialize(r, Publication.class);
        subreport.getErrors().forEach(e -> context.addError(e));
        if (subreport.isValid()) {
          result.hasPart.putIfAbsent(r, subreport.getValue());
        }
      }

      return result;
    }
  }
}
