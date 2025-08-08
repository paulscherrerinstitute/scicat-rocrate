package ch.psi.ord.core;

import ch.psi.ord.model.NoEntityFound;
import ch.psi.ord.model.Publication;
import ch.psi.ord.model.ValidationReport;
import ch.psi.ord.model.ValidationReport.Entity;
import ch.psi.rdf.RdfDeserializer;
import ch.psi.rdf.RdfDeserializer.DeserializationReport;
import jakarta.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SchemaDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class RoCrateImporter {
  private static final Logger logger = LoggerFactory.getLogger(RoCrateImporter.class);

  private Model model = ModelFactory.createOntologyModel();
  private Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
  private InfModel inferredModel = ModelFactory.createInfModel(reasoner, model);
  private RdfDeserializer deserializer = new RdfDeserializer();

  public void loadModel(Model model) {
    if (model != null) {
      this.model = model;
      inferredModel = ModelFactory.createInfModel(reasoner, this.model);
      inferredModel.setDerivationLogging(true);
    }
  }

  public ValidationReport validate() {
    ValidationReport report = new ValidationReport();
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

  public List<Resource> listPublications() {
    final Resource creativeWorkHttp =
        ResourceFactory.createResource(SchemaDO.CreativeWork.getURI().replace("https", "http"));
    final Property identifierHttp =
        ResourceFactory.createProperty(SchemaDO.identifier.getURI().replace("https", "http"));

    List<Resource> publications = new ArrayList<>();

    Set<Resource> creativeWorks =
        inferredModel
            .listStatements(null, RDF.type, SchemaDO.CreativeWork)
            .mapWith(Statement::getSubject)
            .toSet();

    creativeWorks.addAll(
        inferredModel
            .listStatements(null, RDF.type, creativeWorkHttp)
            .mapWith(Statement::getSubject)
            .toSet());

    for (Resource cw : creativeWorks) {
      Statement s = cw.getProperty(SchemaDO.identifier);
      if (s == null && (s = cw.getProperty(identifierHttp)) == null) {
        logger.info(
            "{} has no property {}, trying the http version", cw.toString(), SchemaDO.identifier);
        continue;
      }
      if (s.getObject().isLiteral() && DoiUtils.isDoi(s.getObject().asLiteral().getString())) {
        publications.add(s.getSubject());
      }
    }

    return publications;
  }

  public DeserializationReport<Publication> validatePublication(Resource subject) {
    return deserializer.deserialize(subject, Publication.class);
  }
}
