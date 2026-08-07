package ch.psi.ord.model;

import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;

import ch.psi.ord.model.RootDataset.RootDatasetDeserializer;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SchemaDO;

@Slf4j
@RdfClass(typesUri = SchemaDO.NS + "Dataset")
@RdfDeserialize(using = RootDatasetDeserializer.class)
public class RootDataset {
  @Getter
  HashMap<Class<?>, Set<Resource>> hasPart =
      new HashMap<>(
          Map.of(
              Publication.class, new HashSet<>(),
              Dataset.class, new HashSet<>()));

  public boolean isEmpty() {
    return hasPart.values().stream().allMatch(set -> set.isEmpty());
  }

  public static class RootDatasetDeserializer implements RdfDeserializer<RootDataset> {
    @Override
    public RootDataset deserialize(RDFNode node, RdfDeserializationContext context)
        throws RdfDeserializationException {
      RootDataset result = new RootDataset();
      walkHasPartTree(result, node.asResource());

      return result;
    }

    private void walkHasPartTree(RootDataset result, Resource subject) {
      Set<Resource> parts =
          listProperties(subject, SchemaDO.hasPart).stream()
              .filter(node -> node.isResource())
              .map(node -> node.asResource())
              .collect(Collectors.toSet());

      for (Resource r : parts) {
        if (isOfType(r, SchemaDO.Collection)) {
          result.hasPart.get(Publication.class).add(r);
        } else if (isOfType(r, SchemaDO.Dataset)) {
          result.hasPart.get(Dataset.class).add(r);
        } else {
          walkHasPartTree(result, r);
        }
      }
    }
  }
}
