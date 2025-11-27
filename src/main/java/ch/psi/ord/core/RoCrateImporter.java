package ch.psi.ord.core;

import ch.psi.ord.model.MissingDataError;
import ch.psi.ord.model.NoEntityFound;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ValidationReport;
import ch.psi.ord.model.ValidationReport.Entity;
import ch.psi.rdf.RdfDeserializer;
import ch.psi.rdf.RdfDeserializer.DeserializationReport;
import ch.psi.rdf.RdfUtils;
import ch.psi.scicat.client.ScicatClient;
import ch.psi.scicat.model.compat.UserDetails;
import ch.psi.scicat.model.v3.CountResponse;
import ch.psi.scicat.model.v3.CreateDatasetDto;
import ch.psi.scicat.model.v3.CreatePublishedDataDto;
import ch.psi.scicat.model.v3.Dataset;
import ch.psi.scicat.model.v3.DatasetType;
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
import org.modelmapper.ModelMapper;

@RequestScoped
@Slf4j
public class RoCrateImporter {
  private RoCrate crate;
  private Model model = ModelFactory.createDefaultModel();
  private Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
  private InfModel inferredModel = ModelFactory.createInfModel(reasoner, model);
  private RdfDeserializer deserializer = new RdfDeserializer();
  @Inject private ModelMapper modelMapper;
  @Inject private ScicatClient scicatClient;

  public static String publicationExistsFilter =
      "{\"relatedPublications\": {\"inq\": [\"%s (IsIdenticalTo)\"]}}";

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
    UserDetails userDetails = scicatClient.userDetails(scicatToken);

    publishedDatasetDto.setScicatUser(userDetails.getUsername());

    datasetDto
        .setOwner(String.join("; ", publishedDatasetDto.getCreator()))
        .setContactEmail(userDetails.getEmail())
        .setSourceFolder("/")
        .setCreationTime(Instant.now())
        .setType(DatasetType.DERIVED)
        .setPublished(true);
    if (userDetails.getGroups().size() > 0) {
      datasetDto.setOwnerGroup(userDetails.getGroups().getFirst());
    } else {
      log.error(
          "User is part of no groups, will not be able to update the PublishedData status to"
              + " 'registered'");
    }

    return datasetDto;
  }

  public ValidationReport validate() {
    ValidationReport report = new ValidationReport();

    List<MissingDataError> dataErrors = validateArchiveContent();
    if (!dataErrors.isEmpty()) {
      report.getErrors().addAll(dataErrors);
    }

    List<Resource> potentialPublications = listPublications();
    if (potentialPublications.isEmpty()) {
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
    return new ArrayList<>(listResourcesOfType(SchemaDO.CreativeWork, this::isPublication));
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

  public DeserializationReport<Publication> validatePublication(Resource subject) {
    return deserializer.deserialize(subject, Publication.class);
  }
}
