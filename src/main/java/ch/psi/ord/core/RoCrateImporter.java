package ch.psi.ord.core;

import ch.psi.ord.model.MissingDataError;
import ch.psi.ord.model.NoEntityFound;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ValidationReport;
import ch.psi.ord.model.ValidationReport.Entity;
import ch.psi.rdf.RdfDeserializer;
import ch.psi.rdf.RdfDeserializer.DeserializationReport;
import ch.psi.rdf.RdfUtils;
import jakarta.enterprise.context.RequestScoped;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
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

@RequestScoped
@Slf4j
public class RoCrateImporter {

  private RoCrate crate;
  private Model model = ModelFactory.createOntologyModel();
  private Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
  private InfModel inferredModel = ModelFactory.createInfModel(reasoner, model);
  private RdfDeserializer deserializer = new RdfDeserializer();

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

    List<URI> extractedFiles = this.crate.listFiles().stream().map(Path::toUri).toList();
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
