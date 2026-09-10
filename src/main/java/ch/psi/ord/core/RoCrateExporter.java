package ch.psi.ord.core;

import ch.psi.ord.api.ExtraMediaType;
import ch.psi.s3_broker.client.S3BrokerService;
import ch.psi.s3_broker.model.DatasetUrls;
import ch.psi.s3_broker.model.PublishedDataUrls;
import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v4.DataciteMetadata;
import ch.psi.scicat.model.v4.DataciteMetadata.DescriptionType;
import ch.psi.scicat.model.v4.PublishedData;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.context.RoCrateMetadataContext;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity.ContextualEntityBuilder;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity.DataEntityBuilder;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.jena.vocabulary.SchemaDO;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@RequestScoped
public class RoCrateExporter {
  private RoCrate crate = new RoCrate();
  private RoCrateMetadataContext context = new RoCrateMetadataContext(StaticEntities.CONTEXT_NODE);

  @RestClient @Inject ScicatService scicatService;
  @RestClient @Inject S3BrokerService s3BrokerService;

  public RoCrateExporter() {
    crate.setMetadataContext(context);
  }

  // FIXME: Use ExceptionMapper
  public void addPublications(List<String> dois) throws ClientWebApplicationException {
    for (int i = 0; i < dois.size(); i++) {
      var res = scicatService.getPublishedDataById(dois.get(i));
      // NOTE: we make the first DOI in the list the root of the RO-Crate
      addPublication(res.getEntity(), i == 0);
    }
  }

  public DataEntity addPublication(PublishedData publication, boolean asRootEntity) {
    PublishedDataUrls brokerResponse = s3BrokerService.getPublishedDataUrls(publication.getDoi());
    Map<String, DatasetUrls> urls =
        Optional.ofNullable(brokerResponse.getUrls()).orElse(Collections.emptyMap());

    // https://www.researchobject.org/ro-crate/specification/1.2/data-entities.html#web-based-data-entities
    // File Data Entities with an @id URI outside the RO-Crate Root SHOULD at the time of RO-Crate
    // creation be directly downloadable by a simple non-interactive retrieval (e.g. HTTP GET) of a
    // single data stream, permitting redirections and HTTP/HTTPS authentication
    boolean includeS3Urls = brokerResponse.getExpires().isAfter(Instant.now());

    if (asRootEntity) {
      RootDataEntity root = crate.getRootDataEntity();
      root.addProperty(SchemaDO.name.getLocalName(), publication.getTitle());
      root.addProperty(SchemaDO.description.getLocalName(), publication.getAbstract());
      root.addIdProperty(SchemaDO.license.getLocalName(), StaticEntities.LICENSE.getId());
      root.addProperty(
          SchemaDO.datePublished.getLocalName(),
          yearToISO3601(publication.getMetadata().getPublicationYear()));
    }

    String publicationId = DoiUtils.buildStandardUrl(publication.getDoi());
    DataEntityBuilder publicationBuilder = new DataEntityBuilder();
    publicationBuilder
        .addType(SchemaDO.Collection.getLocalName())
        .setId(publicationId)
        .addProperty(SchemaDO.identifier.getLocalName(), publication.getDoi());
    publication
        .getMetadata()
        .getCreators()
        .forEach(
            creator -> {
              ContextualEntity creatorEntity = addPerson(creator.getName());
              crate.addContextualEntity(creatorEntity);
              publicationBuilder.addIdProperty(
                  SchemaDO.creator.getLocalName(), creatorEntity.getId());
            });

    ContextualEntity publisher = addOrganization(publication.getMetadata().getPublisher());
    crate.addContextualEntity(publisher);
    publicationBuilder.addIdProperty(SchemaDO.publisher.getLocalName(), publisher.getId());

    ContextualEntity license =
        addCreativeWork(publication.getMetadata().getRightsList().getFirst());
    crate.addContextualEntity(license);
    publicationBuilder.addIdProperty(SchemaDO.license.getLocalName(), license.getId());

    publicationBuilder
        .addProperty(
            SchemaDO.datePublished.getLocalName(),
            Long.toString(publication.getMetadata().getPublicationYear()))
        .addProperty(SchemaDO.name.getLocalName(), publication.getTitle())
        .addProperty(SchemaDO._abstract.getLocalName(), publication.getAbstract())
        .addProperty(
            SchemaDO.sdDatePublished.getLocalName(), publication.getRegisteredTime().toString())
        .addProperty(SchemaDO.creativeWorkStatus.getLocalName(), publication.getStatus().toString())
        .addProperty(SchemaDO.dateCreated.getLocalName(), publication.getCreatedAt().toString())
        .addProperty(SchemaDO.dateModified.getLocalName(), publication.getUpdatedAt().toString())
        .addProperty(SchemaDO.expires.getLocalName(), brokerResponse.getExpires().toString());

    publication.getMetadata().getDescriptions().stream()
        .filter(d -> !d.getDescriptionType().equals(DescriptionType.ABSTRACT))
        .forEach(
            d -> {
              publicationBuilder.addProperty(
                  SchemaDO.description.getLocalName(), d.getDescription());
            });
    publication
        .getDatasetPids()
        .forEach(
            pid -> {
              Dataset dataset = scicatService.getDatasetByPid(pid).getEntity();
              DataEntityBuilder datasetBuilder =
                  new DataEntityBuilder()
                      .addType(SchemaDO.Dataset.getLocalName())
                      .addProperty(SchemaDO.name.getLocalName(), dataset.getDatasetName())
                      .addProperty(SchemaDO.description.getLocalName(), dataset.getDescription());

              if (urls.containsKey(pid)) {
                DatasetUrls datasetUrls = urls.get(pid);
                datasetBuilder.addProperty(
                    SchemaDO.expires.getLocalName(), datasetUrls.getExpires().toString());
                if (includeS3Urls) {
                  datasetUrls
                      .getUrls()
                      .forEach(
                          s3Info -> {
                            crate.addDataEntity(
                                new DataEntityBuilder()
                                    .addType(SchemaDO.MediaObject.getLocalName())
                                    .setId(s3Info.getUrl())
                                    .addProperty(
                                        SchemaDO.encodingFormat.getLocalName(),
                                        ExtraMediaType.APPLICATION_TAR)
                                    .addProperty(
                                        SchemaDO.expires.getLocalName(),
                                        s3Info.getExpires().toString())
                                    .build());

                            datasetBuilder.addIdProperty(
                                SchemaDO.hasPart.getLocalName(), s3Info.getUrl());
                          });
                }
              }

              DataEntity dsEntity = datasetBuilder.build();
              crate.addDataEntity(dsEntity);
              publicationBuilder.addIdProperty(SchemaDO.hasPart.getLocalName(), dsEntity.getId());
            });

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

  public ContextualEntity addOrganization(DataciteMetadata.Publisher publisher) {
    ContextualEntityBuilder organizationBuilder =
        new ContextualEntityBuilder()
            .setId(publisher.getPublisherIdentifier())
            .addType(SchemaDO.Organization.getLocalName())
            .addProperty(SchemaDO.name.getLocalName(), publisher.getName());

    return organizationBuilder.build();
  }

  public ContextualEntity addCreativeWork(DataciteMetadata.Right right) {
    ContextualEntityBuilder organizationBuilder =
        new ContextualEntityBuilder()
            .addType(SchemaDO.CreativeWork.getLocalName())
            .setId(right.getRightsIdentifier())
            .addProperty(SchemaDO.name.getLocalName(), right.getRights());

    return organizationBuilder.build();
  }

  public String getCrateMetadata() {
    return crate.getJsonMetadata();
  }

  public Optional<byte[]> getZip() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zipStream = new ZipOutputStream(outputStream)) {
      ZipEntry entry = new ZipEntry("ro-crate-metadata.json");
      zipStream.putNextEntry(entry);
      byte[] metadataDescriptor = getCrateMetadata().getBytes();
      zipStream.write(metadataDescriptor, 0, metadataDescriptor.length);
      zipStream.closeEntry();
    } catch (IOException e) {
      Log.error(e);
      return Optional.empty();
    }

    return Optional.of(outputStream.toByteArray());
  }

  private String yearToISO3601(int year) {
    return Year.of(year).atDay(1).toString();
  }
}
