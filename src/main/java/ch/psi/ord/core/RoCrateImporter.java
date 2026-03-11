package ch.psi.ord.core;

import ch.psi.ord.model.MissingDataError;
import ch.psi.ord.model.NoEntityFound;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ScicatDataset;
import ch.psi.ord.model.ValidationReport;
import ch.psi.ord.model.ValidationReport.Entity;
import ch.psi.rdf.RdfMapper;
import ch.psi.rdf.RdfUtils;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.scicat.cli.ScicatCli;
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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SchemaDO;
import org.jboss.resteasy.reactive.RestResponse;
import org.jspecify.annotations.NonNull;
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
  @Inject private ScicatCli scicatCli;

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
      } else if (entity.object() instanceof ScicatDataset dataset) {
        importDataset(importMap, dataset, scicatToken);
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

  public void importDataset(
      Map<String, String> importMap, ScicatDataset dataset, String scicatToken) {
    CreateDatasetDto dto = modelMapper.map(dataset, CreateDatasetDto.class);
    // scicatClient.createDataset(scicatToken, dto);
    try {
      String pid = scicatCli.createDataset(scicatToken, dto, dataset.getId().startsWith("nfs://"));
      importMap.put(dataset.getId(), pid);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public ValidationReport validate() throws RdfDeserializationException {
    ValidationReport report = new ValidationReport();
    inferredModel.listSubjects().forEach(s -> log.debug("Subject: {}", s.toString()));
    log.debug(findRoot().toString());

    List<MissingDataError> dataErrors = validateArchiveContent();
    if (!dataErrors.isEmpty()) {
      report.getErrors().addAll(dataErrors);
    }

    Set<Resource> scicatDatasets =
        this.listResourcesOfType(
            SchemaDO.Dataset,
            (r) -> !(r.toString().endsWith(this.crate.getBase().resolve("/").toString())));
    // !r.isURIResource()); // || (r.isURIResource() && !r.getURI().equals("file:///")));

    scicatDatasets.forEach(
        d ->
            log.debug(
                "{} is in inferred model {}", d.getURI(), d.getModel() == this.inferredModel));

    scicatDatasets.forEach(
        ds -> {
          ds.listProperties()
              .forEach(s -> log.debug("{} {} {}", s.getSubject(), s.getPredicate(), s.getObject()));

          DeserializationReport<@NonNull ScicatDataset> subreport;
          try {
            subreport = rdfMapper.deserialize(ds, ScicatDataset.class);
            log.debug("{}", subreport);
            if (subreport.isValid()) {
              report.addEntity(
                  new Entity<>(
                      ds.isURIResource() ? ds.getURI() : ds.getId().toString(), subreport.get()));
            } else {
              report.getErrors().addAll(subreport.getErrors());
            }
          } catch (RdfDeserializationException e) {
            e.printStackTrace();
          }
        });

    List<Resource> potentialPublications = listPublications();
    if (potentialPublications.isEmpty() && scicatDatasets.isEmpty()) {
      report.addError(new NoEntityFound());
      return report;
    }

    for (Resource p : potentialPublications) {
      var subreport = validatePublication(p);
      if (subreport.isValid()) {
        report.addEntity(
            new Entity<>(p.isURIResource() ? p.getURI() : p.getId().toString(), subreport.get()));
      } else {
        report.getErrors().addAll(subreport.getErrors());
      }
    }

    return report;
  }

  public List<MissingDataError> validateArchiveContent() {
    Set<Resource> referencedDatafiles =
        listResourcesOfType(
            SchemaDO.Dataset, r -> r.getURI() != null && r.getURI().startsWith("file:///"));

    referencedDatafiles.addAll(
        listResourcesOfType(
            SchemaDO.MediaObject, r -> r.getURI() != null && r.getURI().startsWith("file:///")));

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
    return new ArrayList<>(listResourcesOfType(SchemaDO.Collection, this::isPublication));
  }

  private Set<Resource> listResourcesOfType(Resource type, Predicate<Resource> predicate) {
    Set<Resource> res =
        inferredModel.listResourcesWithProperty(RDF.type, type).filterKeep(predicate).toSet();

    res.addAll(
        inferredModel
            .listResourcesWithProperty(RDF.type, RdfUtils.switchScheme(type))
            .filterKeep(predicate)
            .toSet());

    return res;
  }

  private Set<RDFNode> listProperties(Resource subject, Property p) {
    Set<RDFNode> res = inferredModel.listObjectsOfProperty(subject, p).toSet();
    res.addAll(inferredModel.listObjectsOfProperty(subject, RdfUtils.switchScheme(p)).toSet());

    return res;
  }

  private boolean checkType(Resource subject, Resource expectedType) {
    return subject
        .listProperties(RDF.type)
        .filterKeep(stmt -> stmt.getObject().isResource())
        .mapWith(stmt -> stmt.getObject().asResource())
        .filterKeep(
            type -> type.equals(expectedType) || type.equals(RdfUtils.switchScheme(expectedType)))
        .hasNext();
  }

  private boolean isPublication(Resource subject) {
    Set<RDFNode> identifierValues = listProperties(subject, SchemaDO.identifier);
    if (identifierValues.size() < 1) {
      log.info("{} has no property {}", subject, SchemaDO.identifier);
      return false;
    } else if (identifierValues.size() > 1) {
      log.info(
          "{} has too many values ({}) for the property {}",
          subject,
          identifierValues.size(),
          SchemaDO.identifier);
      return false;
    }

    RDFNode identifier = identifierValues.iterator().next();
    if (!identifier.isLiteral()) {
      log.info("{} has a property {} but it's not a literal", subject, SchemaDO.identifier);
      return false;
    }

    if (!DoiUtils.isDoi(identifier.asLiteral().getString())) {
      log.info("{} has a property {} but it's not a DOI", subject, SchemaDO.identifier);
      return false;
    }

    return true;
  }

  public DeserializationReport<Publication> validatePublication(Resource subject)
      throws RdfDeserializationException {
    return rdfMapper.deserialize(subject, Publication.class);
  }

  // See:
  // https://www.researchobject.org/ro-crate/specification/1.2/appendix/relative-uris#finding-ro-crate-root-in-rdf-triple-stores
  private Resource findRoot() {
    List<Resource> rootDataset =
        inferredModel
            .listSubjects()
            .filterKeep(
                subject ->
                    subject.toString().contains("ro-crate-metadata.json")
                        && (subject.hasProperty(SchemaDO.about)
                            || subject.hasProperty(RdfUtils.switchScheme(SchemaDO.about))))
            .toList()
            .stream()
            .flatMap(md -> this.listProperties(md, SchemaDO.about).stream())
            .filter(RDFNode::isResource)
            .map(RDFNode::asResource)
            .filter(about -> checkType(about, SchemaDO.Dataset))
            .distinct()
            .toList();

    if (rootDataset.size() == 1) return rootDataset.getFirst();

    return null;
  }
}
