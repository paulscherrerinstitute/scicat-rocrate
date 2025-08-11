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
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
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
    List<Resource> publications = new ArrayList<>();

    Query query =
        QueryFactory.create(
            String.format(
                """
                SELECT ?creativeWork ?identifier
                WHERE {
                    ?creativeWork a <%s> .
                    ?creativeWork <%s> ?identifier .
                }
                """,
                SchemaDO.CreativeWork.getURI(), SchemaDO.identifier.getURI()));
    try (QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel)) {
      ResultSet results = qexec.execSelect();
      results.forEachRemaining(
          querySolution -> {
            RDFNode pub = querySolution.get("creativeWork");
            RDFNode identifier = querySolution.get("identifier");
            if (identifier != null
                && identifier.isLiteral()
                && DoiUtils.isDoi(identifier.toString())
                && pub != null
                && pub.isResource()) {
              publications.add(pub.asResource());
            }
          });
    }

    return publications;
  }

  public DeserializationReport<Publication> validatePublication(Resource subject) {
    return deserializer.deserialize(subject, Publication.class);
  }
}
