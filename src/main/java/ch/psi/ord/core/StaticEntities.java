package ch.psi.ord.core;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity.ContextualEntityBuilder;
import java.util.List;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SchemaDO;
import org.apache.jena.vocabulary.XSD;

public class StaticEntities {
  public static ObjectNode CONTEXT_NODE = JsonNodeFactory.instance.objectNode();
  public static final ContextualEntity LICENSE;
  public static final ContextualEntity PSI;

  static {
    CONTEXT_NODE.put("xsd", XSD.NS);
    CONTEXT_NODE.put("owl", OWL.NS);
    CONTEXT_NODE.put("rdfs", RDFS.uri);
    CONTEXT_NODE.put("conformsTo", "http://purl.org/dc/terms/conformsTo");

    List.of(SchemaDO.Dataset, SchemaDO.CreativeWork, SchemaDO.Person, SchemaDO.Organization)
        .forEach(c -> CONTEXT_NODE.put(c.getLocalName(), c.getURI()));

    List.of(
            SchemaDO.name,
            SchemaDO.description,
            SchemaDO.license,
            SchemaDO.datePublished,
            SchemaDO.hasPart,
            SchemaDO.about,
            SchemaDO.identifier,
            SchemaDO.creator,
            SchemaDO.publisher,
            SchemaDO.datePublished,
            SchemaDO.about,
            SchemaDO.additionalType,
            SchemaDO.sdDatePublished,
            SchemaDO.creativeWorkStatus,
            SchemaDO.dateCreated,
            SchemaDO.dateModified,
            SchemaDO.description,
            SchemaDO.domainIncludes,
            SchemaDO.rangeIncludes,
            SchemaDO._abstract)
        .forEach(p -> CONTEXT_NODE.put(p.getLocalName(), p.getURI()));
  }

  static {
    ContextualEntityBuilder licenseBuilder = new ContextualEntityBuilder();
    licenseBuilder
        .setId("https://creativecommons.org/licenses/by-sa/4.0/")
        .addType(SchemaDO.CreativeWork.getLocalName())
        .addProperty(
            SchemaDO.identifier.getLocalName(), "https://creativecommons.org/licenses/by-sa/4.0/")
        .addProperty(
            SchemaDO.name.getLocalName(),
            "Creative Commons Attribution Share Alike 4.0 International");
    LICENSE = licenseBuilder.build();
  }

  static {
    ContextualEntityBuilder psiBuilder = new ContextualEntityBuilder();
    psiBuilder
        .setId("https://ror.org/03eh3y714")
        .addType(SchemaDO.Organization.getLocalName())
        .addProperty(SchemaDO.name.getLocalName(), "Paul Scherrer Institute");
    PSI = psiBuilder.build();
  }
}
