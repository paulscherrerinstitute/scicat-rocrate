package ch.psi.scicat;

import ch.psi.scicat.model.PublishedData;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.context.RoCrateMetadataContext;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity.ContextualEntityBuilder;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity.DataEntityBuilder;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
public class RoCrateExporter {
  private RoCrate crate = new RoCrate();
  private RoCrateMetadataContext context = new RoCrateMetadataContext();

  @Inject
  @ConfigProperty(name =
                      "quarkus.rest-client.\"ch.psi.scicat.ScicatService\".url")
  String scicatServiceUrl;

  public RoCrateExporter() { crate.setMetadataContext(context); }

  public DataEntity addPublication(PublishedData publication,
                                   boolean asRootEntity) {
    context.addToContext("xsd", XSD.NS);
    context.addToContext("owl", OWL.NS);
    context.addToContext("rdfs", RDFS.uri);
    context.addToContext("scicat", "#");

    crate.addFromCollection(StaticEntities.PUBLISHEDDATA_SCHEMA);

    if (asRootEntity) {
      RootDataEntity root = crate.getRootDataEntity();
      root.addProperty("name", publication.title());
      root.addProperty("description", publication._abstract());
      root.addIdProperty("license", StaticEntities.LICENSE.getId());
      root.addProperty("datePublished",
                       Long.toString(publication.publicationYear()));
    }

    DataEntityBuilder publicationBuilder = new DataEntityBuilder();
    publicationBuilder.addTypes(
        List.of("scicat:PublishedData", "CreativeWork"));
    publicationBuilder.setId("https://doi.org/" + publication.doi());
    publicationBuilder.addProperty("identifier", publication.doi());
    publication.creator().forEach(creator -> {
      ContextualEntity creatorEntity = addPerson(creator);
      crate.addContextualEntity(creatorEntity);
      publicationBuilder.addIdProperty("creator", creatorEntity.getId());
    });
    // Assuming that PSI publications all have the same Publisher/License
    if ("PSI".equals(publication.publisher().toUpperCase())) {
      crate.addContextualEntity(StaticEntities.PSI);
      publicationBuilder.addIdProperty("publisher", StaticEntities.PSI.getId());
      crate.addContextualEntity(StaticEntities.LICENSE);
      publicationBuilder.addIdProperty("license",
                                       StaticEntities.LICENSE.getId());
    }
    publicationBuilder.addProperty(
        "datePublished", Long.toString(publication.publicationYear()));
    publicationBuilder.addProperty("title", publication.title());
    publicationBuilder.addProperty("abstract", publication._abstract());
    publicationBuilder.addProperty("additionalType",
                                   publication.resourceType());
    publicationBuilder.addProperty("sdDatePublished",
                                   publication.registeredTime());
    publicationBuilder.addProperty("creativeWorkStatus", publication.status());
    publicationBuilder.addProperty("dateCreated", publication.createdAt());
    publicationBuilder.addProperty("dateModified", publication.updatedAt());
    publicationBuilder.addProperty("description",
                                   publication.dataDescription());
    publication.pidArray().forEach(pid -> {
      publicationBuilder.addIdProperty(
          "hasPart", scicatServiceUrl + "/datasets/" + pid.replace("/", "%2F"));
    });
    publication.relatedPublications().forEach(p -> {
      publicationBuilder.addProperty("scicat:relatedPublications", p);
    });
    publicationBuilder.addProperty("scicat:numberOfFiles",
                                   publication.numberOfFiles());
    publicationBuilder.addProperty("scicat:sizeOfArchive",
                                   publication.sizeOfArchive());
    publicationBuilder.addProperty("sciat:scicatUser",
                                   publication.scicatUser());

    DataEntity publicationEntity = publicationBuilder.build();
    crate.addDataEntity(publicationEntity);

    return publicationEntity;
  }

  public ContextualEntity addPerson(String name) {
    ContextualEntityBuilder creatorBuilder = new ContextualEntityBuilder();
    creatorBuilder.addType("Person");
    creatorBuilder.addProperty("name", name);
    ContextualEntity person = creatorBuilder.build();

    return person;
  }

  public String getCrateMetadata() { return crate.getJsonMetadata(); }
}
