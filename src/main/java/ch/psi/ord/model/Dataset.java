package ch.psi.ord.model;

import static ch.psi.rdf.RdfUtils.listProperties;
import static ch.psi.rdf.deser.ObjectDeserializer.checkNodeKind;

import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceIdentifier;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
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

  @RdfProperty(uri = "https://scicatproject.org/relationships")
  List<ScicatRelationship> relationships;

  @RdfProperty(uri = SchemaDO.NS + "variableMeasured")
  @RdfDeserialize(using = VariableMeasuredDeserializer.class)
  List<PropertyValue> variableMeasured;

  public static class VariableMeasuredDeserializer implements RdfDeserializer<List<PropertyValue>> {
    @Override
    public List<PropertyValue> deserialize(RDFNode node, RdfDeserializationContext context)
        throws RdfDeserializationException {
      Resource subject =
          context
              .getCurrentSubject()
              .orElseThrow(() -> new RdfDeserializationException("current subject is not set"));

      List<PropertyValue> properties = new ArrayList<>();
      for (RDFNode value : listProperties(subject, SchemaDO.variableMeasured)) {
        Optional<PropertyError> error =
            checkNodeKind(subject, SchemaDO.variableMeasured.getURI(), PropertyValue.class, value);
        if (error.isPresent()) {
          context.addError(error.get());
          continue;
        }

        PropertyValue property =
            context.getDeserializer(PropertyValue.class).deserialize(value, context);
        if (property != null) {
          properties.add(property);
        }
      }

      PropertyValue.checkDuplicateNames(subject, SchemaDO.variableMeasured, properties, context);
      return properties;
    }
  }
}
