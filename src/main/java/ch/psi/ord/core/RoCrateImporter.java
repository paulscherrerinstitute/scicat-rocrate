package ch.psi.ord.core;

import static ch.psi.rdf.RdfUtils.listResourcesOfType;

import ch.psi.ord.model.Dataset;
import ch.psi.ord.model.MissingDataError;
import ch.psi.ord.model.NoEntityFound;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.RootDataset;
import ch.psi.ord.model.ValidationReport;
import ch.psi.ord.model.ValidationReport.Entity;
import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.scicat.cli.ScicatCli;
import ch.psi.scicat.client.ScicatService;
import ch.psi.scicat.model.v3.CountResponse;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreateJobDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.DatasetType;
import ch.psi.scicat.model.v3.MyIdentity;
import ch.psi.scicat.model.v3.OutputJobDto;
import ch.psi.scicat.model.v3.PublishedData;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.SchemaDO;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.modelmapper.ModelMapper;

@RequestScoped
@Slf4j
public class RoCrateImporter {
  private RoCrate crate;
  private Model model = ModelFactory.createDefaultModel();
  private Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
  private InfModel inferredModel = ModelFactory.createInfModel(reasoner, model);
  private RdfMapper rdfMapper = new RdfMapper();
  private Set<String> datasetsToArchive = new HashSet<>();
  @Inject private ModelMapper modelMapper;
  @RestClient @Inject private ScicatService scicatService;
  @Inject private ScicatCli scicatCli;
  private MyIdentity userIdentity;
  private String ownerGroup;

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

  public Map<String, String> importCrate(
      ValidationReport report, String scicatToken, String ownerGroup) {
    Map<String, String> importMap = new HashMap<>();
    userIdentity = scicatService.myidentity(scicatToken).getEntity();
    this.ownerGroup = resolveOwnerGroup(ownerGroup);
    for (var entity : report.getEntities()) {
      if (entity.object() instanceof Publication publication) {
        importPublication(importMap, publication, scicatToken);
      } else if (entity.object() instanceof Dataset dataset) {
        importDataset(importMap, dataset, scicatToken);
      } else {
        throw new NotImplementedException("Only Publications and Datasets are supported for now");
      }
    }
    submitArchiveJob(scicatToken);

    return importMap;
  }

  private String resolveOwnerGroup(String requestedOwnerGroup) {
    if (requestedOwnerGroup != null && !requestedOwnerGroup.isBlank()) {
      return requestedOwnerGroup;
    }
    List<String> accessGroups = userIdentity.getProfile().getAccessGroups();
    if (!accessGroups.isEmpty()) {
      return accessGroups.getFirst();
    }
    throw new HttpException(
        StatusCode.FORBIDDEN,
        "User needs to be part of at least one group to create resources in SciCat");
  }

  private void submitArchiveJob(String scicatToken) {
    if (datasetsToArchive.isEmpty()) {
      return;
    }

    OutputJobDto archiveJob =
        scicatService
            .createJob(
                scicatToken,
                new CreateJobDto()
                    .setType("archive")
                    .setJobParams(new CreateJobDto.JobParameters().setTapeCopies("one"))
                    .setDatasetList(
                        datasetsToArchive.stream()
                            .map(pid -> new CreateJobDto.DatasetEntry().setPid(pid))
                            .toList()))
            .getEntity();
    log.info("Submitted archive job: {}", archiveJob.getId());
  }

  private void scheduleForArchival(String pid) {
    datasetsToArchive.add(pid);
  }

  private void importDataset(Map<String, String> importMap, Dataset dataset, String scicatToken) {
    CreateDatasetDto dto = modelMapper.map(dataset, CreateDatasetDto.class);
    dto.setOwnerGroup(ownerGroup);
    String pid = scicatCli.ingestDataset(scicatToken, dto);
    scheduleForArchival(pid);
    importMap.put(crate.toRelativeId(dataset.getResourceIdentifier()), pid);
  }

  public void importPublication(
      Map<String, String> importMap, Publication publication, String scicatToken) {
    if (publication.getIdentifier() != null) {
      CountResponse count =
          scicatService
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
    }

    CreatePublishedDataDto dto = modelMapper.map(publication, CreatePublishedDataDto.class);

    if (dto.getPidArray().isEmpty()) {
      CreateDatasetDto datasetDto = createPlaceholderDataset(dto, scicatToken);
      String pid =
          scicatCli.ingestDataset(
              scicatToken,
              datasetDto,
              List.of(
                  crate
                      .getBase()
                      .resolve(RoCrate.METADATA_DESCRIPTOR)
                      .toAbsolutePath()
                      .toString()));
      importMap.put(RoCrate.METADATA_DESCRIPTOR, pid);
      scheduleForArchival(pid);
    }

    if (!publication.getHasPart().getFiles().isEmpty()) {
      CreateDatasetDto datasetDto = modelMapper.map(publication, CreateDatasetDto.class);
      datasetDto.setSourceFolder(crate.getBase().toString()).setOwnerGroup(ownerGroup);
      String datasetPid =
          scicatCli.ingestDataset(
              scicatToken, datasetDto, publication.getHasPart().getFiles().values());
      scheduleForArchival(datasetPid);
      dto.getPidArray().add(datasetPid);
      publication
          .getHasPart()
          .getFiles()
          .keySet()
          .forEach(id -> importMap.put(crate.toRelativeId(id), datasetPid));
    }

    RestResponse<PublishedData> created = scicatService.createPublishedData(scicatToken, dto);
    importMap.put(
        crate.toRelativeId(publication.getResourceIdentifier()), created.getEntity().getDoi());
  }

  private CreateDatasetDto createPlaceholderDataset(
      CreatePublishedDataDto publishedDatasetDto, String scicatToken) {
    CreateDatasetDto datasetDto = new CreateDatasetDto();

    publishedDatasetDto.setScicatUser(userIdentity.getProfile().getUsername());

    datasetDto
        .setDatasetName("Original RO-Crate")
        .setOwner(String.join("; ", publishedDatasetDto.getCreator()))
        .setPrincipalInvestigator(String.join("; ", publishedDatasetDto.getCreator()))
        .setContactEmail(userIdentity.getProfile().getEmail())
        .setSourceFolder(crate.getBase().toString())
        .setCreationLocation("")
        .setCreationTime(Instant.now())
        .setType(DatasetType.RAW)
        .setPublished(false)
        .setOwnerGroup(ownerGroup);

    return datasetDto;
  }

  public ValidationReport validate() throws RdfDeserializationException {
    ValidationReport report = new ValidationReport();

    List<MissingDataError> dataErrors = validateArchiveContent();
    if (!dataErrors.isEmpty()) {
      report.getErrors().addAll(dataErrors);
    }

    RootDataset rootDataset =
        rdfMapper
            .deserialize(inferredModel.getResource(crate.getRoot().getURI()), RootDataset.class)
            .get();

    if (rootDataset.isEmpty()) {
      report.addError(new NoEntityFound());
      return report;
    }

    for (Map.Entry<Class<?>, Set<Resource>> entry : rootDataset.getHasPart().entrySet()) {
      Class<?> targetClass = entry.getKey();
      Set<Resource> resources = entry.getValue();

      for (Resource r : resources) {
        var subreport = rdfMapper.deserialize(r, targetClass);
        if (subreport.isValid()) {
          String id = crate.toRelativeId(r.toString());
          report.addEntity(new Entity<>(id, subreport.get()));
        } else {
          report.getErrors().addAll(subreport.getErrors());
        }
      }
    }

    return report;
  }

  public List<MissingDataError> validateArchiveContent() {
    // NOTE: when only the metadata descriptor is uploaded we can't validate the archive content
    if (!crate.hasAttachedData()) {
      return Collections.emptyList();
    }

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

    Set<Path> pathsToCheck =
        referencedDatafiles.stream()
            .map(r -> Paths.get(r.getURI().replace("file://", "/")).toAbsolutePath())
            .collect(Collectors.toSet());

    List<MissingDataError> errors = new ArrayList<>();
    pathsToCheck.forEach(
        dataEntityPath -> {
          if (!crate.contains(dataEntityPath)) {
            errors.add(new MissingDataError(crate.getBase().relativize(dataEntityPath).toString()));
          }
        });

    return errors;
  }
}
