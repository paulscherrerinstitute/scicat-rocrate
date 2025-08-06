package ch.psi.ord.core;

import ch.psi.scicat.client.ScicatClient;
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
import java.time.Year;
import java.util.List;
import org.apache.jena.vocabulary.SchemaDO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@RequestScoped
public class RoCrateExporter {
  private RoCrate crate = new RoCrate();
  private RoCrateMetadataContext context = new RoCrateMetadataContext(StaticEntities.CONTEXT_NODE);
  private boolean hasPublicationSchema = false;

  @Inject ScicatClient scicatClient;

  @Inject
  @ConfigProperty(name = "quarkus.rest-client.\"ch.psi.scicat.client.ScicatService\".url")
  String scicatServiceUrl;

  public RoCrateExporter() {
    crate.setMetadataContext(context);
  }

  private String formatScicatId(String s) {
    return String.format("scicat:%s", s);
  }

  // FIXME: Use ExceptionMapper
  public void addPublications(List<String> dois) throws ClientWebApplicationException {
    if (!hasPublicationSchema) {
      hasPublicationSchema = true;
      StaticEntities.PUBLISHEDDATA_SCHEMA.forEach(entity -> crate.addDataEntity(entity));
    }

    for (int i = 0; i < dois.size(); i++) {
      var res = scicatClient.getPublishedDataById(dois.get(i));
      // NOTE: we make the first DOI in the list the root of the RO-Crate
      addPublication(res.getEntity(), i == 0);
    }
  }

  public DataEntity addPublication(PublishedData publication, boolean asRootEntity) {
    if (asRootEntity) {
      RootDataEntity root = crate.getRootDataEntity();
      root.addProperty(SchemaDO.name.getLocalName(), publication.title());
      root.addProperty(SchemaDO.description.getLocalName(), publication.abstract_());
      root.addIdProperty(SchemaDO.license.getLocalName(), StaticEntities.LICENSE.getId());
      root.addProperty(
          SchemaDO.datePublished.getLocalName(), yearToISO3601(publication.publicationYear()));
    }

    DataEntityBuilder publicationBuilder = new DataEntityBuilder();
    publicationBuilder
        .addTypes(List.of(formatScicatId("PublishedData"), SchemaDO.CreativeWork.getLocalName()))
        .setId(DoiUtils.buildStandardUrl(publication.doi()))
        .addProperty(SchemaDO.identifier.getLocalName(), publication.doi());
    publication
        .creator()
        .forEach(
            creator -> {
              ContextualEntity creatorEntity = addPerson(creator);
              crate.addContextualEntity(creatorEntity);
              publicationBuilder.addIdProperty(
                  SchemaDO.creator.getLocalName(), creatorEntity.getId());
            });
    // Assuming that PSI publications all have the same Publisher/License
    if ("PSI".equals(publication.publisher().toUpperCase())) {
      crate.addContextualEntity(StaticEntities.PSI);
      publicationBuilder.addIdProperty(
          SchemaDO.publisher.getLocalName(), StaticEntities.PSI.getId());
      crate.addContextualEntity(StaticEntities.LICENSE);
      publicationBuilder.addIdProperty(
          SchemaDO.license.getLocalName(), StaticEntities.LICENSE.getId());
    }
    publicationBuilder
        .addProperty(
            SchemaDO.datePublished.getLocalName(), Long.toString(publication.publicationYear()))
        .addProperty(SchemaDO.title.getLocalName(), publication.title())
        .addProperty(SchemaDO._abstract.getLocalName(), publication.abstract_())
        .addProperty(SchemaDO.additionalType.getLocalName(), publication.resourceType())
        .addProperty(SchemaDO.sdDatePublished.getLocalName(), publication.registeredTime())
        .addProperty(SchemaDO.creativeWorkStatus.getLocalName(), publication.status())
        .addProperty(SchemaDO.dateCreated.getLocalName(), publication.createdAt())
        .addProperty(SchemaDO.dateModified.getLocalName(), publication.updatedAt())
        .addProperty(SchemaDO.description.getLocalName(), publication.dataDescription());
    publication
        .pidArray()
        .forEach(
            pid -> {
              publicationBuilder.addIdProperty(
                  SchemaDO.hasPart.getLocalName(),
                  scicatServiceUrl + "/datasets/" + pid.replace("/", "%2F"));
            });
    publication
        .relatedPublications()
        .forEach(
            p -> {
              publicationBuilder.addProperty(formatScicatId("relatedPublications"), p);
            });
    publicationBuilder
        .addProperty(formatScicatId("numberOfFiles"), publication.numberOfFiles())
        .addProperty(formatScicatId("sizeOfArchive"), publication.sizeOfArchive())
        .addProperty(formatScicatId("scicatUser"), publication.scicatUser());

    DataEntity publicationEntity = publicationBuilder.build();
    crate.addDataEntity(publicationEntity);

    return publicationEntity;
  }

  public ContextualEntity addPerson(String name) {
    ContextualEntityBuilder creatorBuilder =
        new ContextualEntityBuilder()
            .addType(SchemaDO.Person.getLocalName())
            .addProperty(SchemaDO.name.getLocalName(), name);

    return creatorBuilder.build();
  }

  public String getCrateMetadata() {
    return crate.getJsonMetadata();
  }

  private String yearToISO3601(int year) {
    return Year.of(year).atDay(1).toString();
  }
}
