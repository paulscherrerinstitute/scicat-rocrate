package ch.psi.scicat;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.SchemaDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import ch.psi.scicat.model.NoEntityFound;
import ch.psi.scicat.model.PropertyError;
import ch.psi.scicat.model.PropertyRequirements;
import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.PublishedData.PublishedDataBuilder;
import ch.psi.scicat.model.ValidationReport;
import ch.psi.scicat.model.ValidationReport.Entity;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class RoCrateImporter {
    private static final Logger logger = LoggerFactory.getLogger(RoCrateImporter.class);

    private Model model = ModelFactory.createOntologyModel();
    private Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
    private InfModel inferredModel = ModelFactory.createInfModel(reasoner, model);
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

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
            try {
                report.addEntity(validatePublication(p));
            } catch (ValidationException e) {
                report.addError(e);
            }
        }

        return report;
    }

    public List<Resource> listPublications() {
        List<Resource> publications = new ArrayList<>();

        Query query = QueryFactory.create(String.format("""
                SELECT ?creativeWork ?identifier
                WHERE {
                    ?creativeWork a <%s> .
                    ?creativeWork <%s> ?identifier .
                }
                """, SchemaDO.CreativeWork.getURI(), SchemaDO.identifier.getURI()));
        try (QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel)) {
            ResultSet results = qexec.execSelect();
            results.forEachRemaining(querySolution -> {
                RDFNode pub = querySolution.get("creativeWork");
                RDFNode identifier = querySolution.get("identifier");
                if (identifier != null && identifier.isLiteral()
                        && DoiUtils.isDoi(identifier.toString())
                        && pub != null && pub.isResource()) {
                    publications.add(pub.asResource());
                }
            });
        }

        return publications;
    }

    public Entity<PublishedData> validatePublication(Resource publication) throws ValidationException {
        Map<String, Object> foundProperties = validateProperties(publication,
                Map.ofEntries(
                        Map.entry(SchemaDO.identifier, new PropertyRequirements(true)),
                        Map.entry(SchemaDO.creator, new PropertyRequirements(true, true, true)),
                        Map.entry(SchemaDO.title, new PropertyRequirements(true)),
                        Map.entry(SchemaDO.publisher, new PropertyRequirements(true, true)),
                        Map.entry(SchemaDO.dateCreated, new PropertyRequirements(true)),
                        Map.entry(SchemaDO.datePublished, new PropertyRequirements(true)),
                        Map.entry(SchemaDO.dateModified, new PropertyRequirements(true)),
                        Map.entry(SchemaDO._abstract, new PropertyRequirements(true)),
                        Map.entry(SchemaDO.description, new PropertyRequirements(false)),
                        // NOTE: License is required even though SciCat doesn't process it?
                        Map.entry(SchemaDO.license, new PropertyRequirements(true, true)),
                        Map.entry(SchemaDO.hasPart, new PropertyRequirements(true, true, true))
                // Map.entry(SchemaDO.additionalType, new PropertyRequirements(false, true)),
                // Map.entry(SchemaDO.sdDatePublished, new PropertyRequirements(false, true)),
                // Map.entry(SchemaDO.creativeWorkStatus, new PropertyRequirements(false,
                // true)),
                // Map.entry(SchemaDO.description, new PropertyRequirements(false, true))
                ));

        PublishedDataBuilder builder = new PublishedDataBuilder();
        ValidationException errors = new ValidationException();

        builder.doi(foundProperties.get(SchemaDO.identifier.getLocalName()).toString());
        builder.title(foundProperties.get(SchemaDO.title.getLocalName()).toString());
        builder._abstract(foundProperties.get(SchemaDO._abstract.getLocalName()).toString());
        builder.dataDescription(foundProperties.get(SchemaDO.description.getLocalName()).toString());

        List<Resource> creatorList = (List<Resource>) foundProperties.get(SchemaDO.creator.getLocalName());
        for (Resource creator : creatorList) {
            try {
                Map<String, Object> creatorProperties = validateProperties(creator,
                        Map.ofEntries(Map.entry(SchemaDO.name, new PropertyRequirements(true))));
                builder.addCreator(creatorProperties.get(SchemaDO.name.getLocalName()).toString());
            } catch (ValidationException e) {
                errors.addError(e);
            }
        }

        try {
            Resource publisher = (Resource) foundProperties.get(SchemaDO.publisher.getLocalName());

            Map<String, Object> publisherProperties = validateProperties(publisher,
                    Map.ofEntries(Map.entry(SchemaDO.name, new PropertyRequirements(true))));
            builder.publisher(publisherProperties.get(SchemaDO.name.getLocalName()).toString());
        } catch (ValidationException e) {
            errors.addError(e);
        }

        try {
            Literal datePublished = (Literal) foundProperties.get(SchemaDO.datePublished.getLocalName());
            builder.publicationYear(parseDateLiteral(datePublished).getYear());
        } catch (ParseException e) {
            errors.addError(new PropertyError(publication.getURI(), SchemaDO.datePublished.toString(),
                    "Failed to parse date, make sure it is ISO-8601 compliant"));
        }

        try {
            Literal dateCreated = (Literal) foundProperties.get(SchemaDO.dateCreated.getLocalName());
            builder.createdAt(parseDateLiteral(dateCreated).format(formatter));
        } catch (ParseException e) {
            errors.addError(new PropertyError(publication.getURI(), SchemaDO.datePublished.toString(),
                    "Failed to parse date, make sure it is ISO-8601 compliant"));
        }

        try {
            Literal dateModified = (Literal) foundProperties.get(SchemaDO.dateModified.getLocalName());
            builder.updatedAt(parseDateLiteral(dateModified).format(formatter));
        } catch (ParseException e) {
            errors.addError(new PropertyError(publication.getURI(), SchemaDO.datePublished.toString(),
                    "Failed to parse date, make sure it is ISO-8601 compliant"));
        }

        // FIXME:
        builder.pidArray(List.of("fixme"));
        builder.resourceType("derived");

        if (!errors.isEmpty()) {
            throw errors;
        }

        return new Entity<PublishedData>(publication.getURI(), builder.build());
    }

    private Query buildValidationQuery(Resource subject, Map<Property, PropertyRequirements> properties) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        properties.keySet()
                .forEach(p -> queryBuilder.append(String.format("?%s ", p.getLocalName())));
        queryBuilder.append("\nWHERE {\n");
        properties.keySet()
                .forEach(p -> queryBuilder.append(
                        String.format("\tOPTIONAL { <%s> <%s> ?%s . }\n",
                                subject.getURI(), p.getURI(), p.getLocalName())));
        queryBuilder.append("}");

        logger.debug("Query generated:\n{}", queryBuilder.toString());

        return QueryFactory.create(queryBuilder.toString());
    }

    private Map<String, Object> validateProperties(Resource subject, Map<Property, PropertyRequirements> properties)
            throws ValidationException {
        Query query = buildValidationQuery(subject, properties);
        Map<String, Object> validProperties = new HashMap<>();
        ValidationException errors = new ValidationException();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel)) {
            ResultSet results = qexec.execSelect();
            results.forEachRemaining(row -> {
                properties.forEach((p, req) -> {
                    if (!row.contains(p.getLocalName()) && req.isRequired()) {
                        errors.addError(new PropertyError(subject.getURI(), p.getURI(), "Missing required property"));
                    } else if (req.isResource() != row.get(p.getLocalName()).isResource()) {
                        String first = req.isResource() ? "Resource" : "Literal";
                        String second = !req.isResource() ? "Resource" : "Literal";
                        errors.addError(new PropertyError(subject.getURI(), p.getURI(),
                                String.format("Expected a %s but got a %s", first, second)));
                    } else {
                        Object value = req.isResource() ? row.getResource(p.getLocalName())
                                : row.getLiteral(p.getLocalName());
                        if (req.isMultivalued()) {
                            if (validProperties.containsKey(p.getLocalName())) {
                                ((List<Object>) validProperties.get(p.getLocalName())).add(value);
                            } else {
                                List<Object> l = new ArrayList<>();
                                l.add(value);
                                validProperties.put(p.getLocalName(), l);
                            }
                        } else {
                            // NOTE: We override properties, maybe should log it
                            validProperties.put(p.getLocalName(), value);
                        }
                    }
                });
            });
        }

        if (!errors.isEmpty()) {
            throw errors;
        }

        return validProperties;
    }

    private OffsetDateTime parseDateLiteral(Literal l) throws ParseException {
        Date d = new StdDateFormat().parse(l.toString());
        return d.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
