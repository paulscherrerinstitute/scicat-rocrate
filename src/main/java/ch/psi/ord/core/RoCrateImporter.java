package ch.psi.ord.core;

import static ch.psi.rdf.RdfUtils.isOfType;
import static ch.psi.rdf.RdfUtils.listProperties;
import static ch.psi.rdf.RdfUtils.listResourcesOfType;

import ch.psi.ord.model.MissingDataError;
import ch.psi.ord.model.NoEntityFound;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.RootDataset;
import ch.psi.ord.model.ValidationReport;
import ch.psi.ord.model.ValidationReport.Entity;
import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.v3.CountResponse;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.DatasetType;
import ch.psi.scicat.model.v3.MyIdentity;
import ch.psi.scicat.model.v3.PublishedData;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.modelmapper.ModelMapper;

@RequestScoped
@Slf4j
public class RoCrateImporter {
  private RoCrate crate;
  private Model model = ModelFactory.createDefaultModel();
  private Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
  private InfModel inferredModel = ModelFactory.createInfModel(reasoner, model);
  private RdfMapper rdfMapper = new RdfMapper();
  @Inject private ModelMapper modelMapper;
  @Inject private ScicatClient scicatClient;

  public static String publicationExistsFilter =
      """
          { "where": { "relatedPublications": "%s (IsIdenticalTo)" } }
      """;

  public void loadCrate(RoCrate crate) {
    this.crate = crate;
    loadModel(this.crate.getModel());
  }

  public void loadModel(Model model) {
    if (model != null) {
      this.model = model;
      inferredModel = ModelFactory.createInfModel(reasoner, this.model);
    }
  }

  public Map<String, String> importCrate(ValidationReport report, String scicatToken) {
    Map<String, String> importMap = new HashMap<>();
    for (var entity : report.getEntities()) {
      if (entity.object() instanceof Publication publication) {
        importPublication(importMap, publication, scicatToken);
      } else {
        throw new NotImplementedException("Only Publications are supported for now");
      }
    }

    return importMap;
  }

  public void importPublication(
      Map<String, String> importMap, Publication publication, String scicatToken) {
    CreatePublishedDataDto dto = modelMapper.map(publication, CreatePublishedDataDto.class);
    CountResponse count =
        scicatClient
            .countPublishedData(
                String.format(
                    publicationExistsFilter,
                    DoiUtils.buildStandardUrl(publication.getIdentifier())),
                scicatToken)
            .getEntity();

    if (count.getCount() > 0) {
      throw new WebApplicationException(
          "This Publication has already been imported", Status.CONFLICT);
    }

    if (dto.getPidArray().isEmpty()) {
      CreateDatasetDto datasetDto = createPlaceholderDataset(dto, scicatToken);
      RestResponse<Dataset> createdDataset = scicatClient.createDataset(scicatToken, datasetDto);

      dto.getPidArray().add(createdDataset.getEntity().getPid());
      importMap.put(publication.getIdentifier(), createdDataset.getEntity().getPid());
    }

    RestResponse<PublishedData> created = scicatClient.createPublishedData(scicatToken, dto);
    scicatClient.registerPublishedData(created.getEntity().getDoi(), scicatToken);
    importMap.put(publication.getIdentifier(), created.getEntity().getDoi());
  }

  private CreateDatasetDto createPlaceholderDataset(
      CreatePublishedDataDto publishedDatasetDto, String scicatToken) {
    CreateDatasetDto datasetDto = new CreateDatasetDto();
    MyIdentity userDetails = scicatClient.myidentity(scicatToken).getEntity();

    publishedDatasetDto.setScicatUser(userDetails.getProfile().getUsername());

    datasetDto
        .setDatasetName("Original RO-Crate")
        .setOwner(String.join("; ", publishedDatasetDto.getCreator()))
        .setPrincipalInvestigator(String.join("; ", publishedDatasetDto.getCreator()))
        .setContactEmail(userDetails.getProfile().getEmail())
        .setSourceFolder("/")
        .setCreationLocation("")
        .setCreationTime(Instant.now())
        .setType(DatasetType.RAW)
        .setPublished(false);
    if (userDetails.getProfile().getAccessGroups().size() > 0) {
      datasetDto.setOwnerGroup(userDetails.getProfile().getAccessGroups().getFirst());
    } else {
      log.error(
          "User is part of no groups, will not be able to update the PublishedData status to"
              + " 'registered'");
    }

    return datasetDto;
  }

  public ValidationReport validate() throws RdfDeserializationException {
    ValidationReport report = new ValidationReport();

    List<MissingDataError> dataErrors = validateArchiveContent();
    if (!dataErrors.isEmpty()) {
      report.getErrors().addAll(dataErrors);
    }

    DeserializationReport<RootDataset> rootDataset =
        rdfMapper.deserialize(
            inferredModel.getResource(crate.getRoot().getURI()), RootDataset.class);

    if (rootDataset.getValue() == null) {
      report.addError(new NoEntityFound());
      return report;
    }

    report.getErrors().addAll(rootDataset.getErrors());
    rootDataset
        .getValue()
        .getHasPart()
        .forEach((r, p) -> report.addEntity(new Entity<Publication>(r.toString(), p)));

    return report;
  }

  public List<MissingDataError> validateArchiveContent() {
    Set<Resource> referencedDatafiles =
        listResourcesOfType(
            inferredModel,
            SchemaDO.Dataset,
            r -> r.getURI() != null && r.getURI().startsWith("file:///"));

    referencedDatafiles.addAll(
        listResourcesOfType(
            inferredModel,
            SchemaDO.MediaObject,
            r -> r.getURI() != null && r.getURI().startsWith("file:///")));

    Set<URI> extractedFiles =
        this.crate.listFiles().stream().map(Path::toUri).collect(Collectors.toSet());
    List<MissingDataError> errors = new ArrayList<>();
    referencedDatafiles.forEach(
        r -> {
          if (!extractedFiles.contains(URI.create(r.getURI()))) {
            log.info("URI: {}", r.getURI());
            log.info("Base: {}", this.crate.getBase());
            errors.add(
                new MissingDataError(
                    r.getURI().replace("file://" + this.crate.getBase().toString(), "")));
          }
        });

    return errors;
  }

  public List<Resource> listPublications() {
    return listProperties(inferredModel.getResource(crate.getRoot().toString()), SchemaDO.hasPart)
        .stream()
        .filter(node -> node.isResource() && isOfType(node.asResource(), SchemaDO.Collection))
        .map(RDFNode::asResource)
        .toList();
  }
}
