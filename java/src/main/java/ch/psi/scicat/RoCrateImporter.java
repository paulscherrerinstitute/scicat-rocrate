package ch.psi.scicat;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.vocabulary.RDF;
import org.jboss.logging.Logger;
import org.schema.SchemaVocab;

import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.PublishedData.PublishedDataBuilder;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class RoCrateImporter {
    private static final Logger LOG = Logger.getLogger(RoCrateImporter.class);

    private Model model = ModelFactory.createDefaultModel();
    private Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
    private InfModel infModel = ModelFactory.createInfModel(reasoner, model);

    public void loadModel(Model model) {
        this.model = model;
        infModel = ModelFactory.createInfModel(reasoner, this.model);
        infModel.setDerivationLogging(true);
    }

    public List<PublishedData> listPublications() {
        List<PublishedData> publications = new ArrayList<>();

        listCreativeWorks().forEach(creativeWork -> {
            LOG.infof("Trying to convert %s to PublishedData", creativeWork.getURI());
            tryIntoPublishedData(creativeWork).ifPresentOrElse(p -> {
                LOG.infof("Conversion of %s to PublishedData succeeded", creativeWork.getURI());
                publications.add(p);
            }, () -> {
                LOG.infof("Conversion of %s to PublishedData failed", creativeWork.getURI());
            });
        });

        return publications;
    }

    public List<Resource> listCreativeWorks() {
        return infModel
                .listStatements(null, RDF.type, SchemaVocab.CreativeWork)
                .toList()
                .stream()
                .map(s -> s.getSubject())
                .filter(s -> s.getURI() == null // Blank nodes have a null URI
                        || !s.getURI().contains("ro-crate-metadata.json"))
                .collect(Collectors.toList());
    }

    public Optional<PublishedData> tryIntoPublishedData(Resource creativeWork) {
        PublishedDataBuilder builder = new PublishedDataBuilder();
        // builder.doi(extractDoi(creativeWork))
        // .creator(extractCreators(creativeWork))
        // .publisher(extractPublisherInfo(creativeWork))
        // .publicationYear(extractPublicationYear(creativeWork))
        // .title(extractTitle(creativeWork))
        // ._abstract(extractAbstract(creativeWork))
        // .dataDescription(extractDataDescription(creativeWork))
        // .pidArray(extractPidArray(creativeWork));
        extractDoi(creativeWork).ifPresent(doi -> builder.doi(doi));
        builder.creator(extractCreators(creativeWork));
        extractPublisherInfo(creativeWork).ifPresent(publisher -> builder.publisher(publisher));
        extractPublicationYear(creativeWork).ifPresent(publicationYear -> builder.publicationYear(publicationYear));
        extractTitle(creativeWork).ifPresent(title -> builder.title(title));
        extractAbstract(creativeWork).ifPresent(abstract_ -> builder._abstract(abstract_));
        extractDataDescription(creativeWork).ifPresent(dataDescription -> builder.dataDescription(dataDescription));

        try {
            return Optional.of(builder.build());
        } catch (IllegalStateException e) {
            LOG.error(e);
            return Optional.empty();
        }
    }

    public Optional<String> extractDoi(Resource subject) {
        Optional<Literal> literal = extractLiteralProperty(subject, SchemaVocab.identifier_PROP);
        return literal.map(Literal::getString);
    }

    public Optional<Literal> extractLiteralProperty(Resource subject, Property property) {
        // We assume the subject is valid but might be from the non inferred model
        Statement s;
        if (!subject.getModel().equals(infModel)) {
            s = (subject.isURIResource()
                    ? infModel.getResource(subject.getURI())
                    : infModel.getResource(subject.getId()))
                    .getProperty(property);
        } else {
            s = subject.getProperty(property);
        }

        if (s != null) {
            RDFNode o = s.getObject();
            if (o.isLiteral()) {
                return Optional.of(o.asLiteral());
            } else {
                LOG.warnf("Ignoring non-literal property %s of subject %s", property.toString(),
                        subject.toString());
                return Optional.empty();
            }
        }

        LOG.warnf("Resource %s has no property %s", subject.toString(), property.toString());
        return Optional.empty();
    }

    public Optional<Resource> extractResourceProperty(Resource subject, Property property) {
        Statement s;
        if (!subject.getModel().equals(infModel)) {
            s = (subject.isURIResource()
                    ? infModel.getResource(subject.getURI())
                    : infModel.getResource(subject.getId()))
                    .getProperty(property);
        } else {
            s = subject.getProperty(property);
        }

        if (s != null) {
            RDFNode o = s.getObject();
            if (o.isResource()) {
                return Optional.of(o.asResource());
            } else {
                LOG.warnf("Ignoring non-resource property %s of subject %s", property.toString(), subject.toString());
                return Optional.empty();
            }
        }
        LOG.warnf("Resource %s has no property %s", subject.toString(), property.toString());

        return Optional.empty();
    }

    public List<String> extractCreators(Resource subject) {
        List<String> creator = new ArrayList<>();

        StmtIterator creatorIterator = subject.listProperties(SchemaVocab.creator);
        if (!creatorIterator.hasNext()) {
            LOG.errorf("Resource %s has no property %", subject.toString(), SchemaVocab.creator.toString());
            return creator;
        }

        creatorIterator.forEach(s -> {
            if (s.getObject().isResource()) {
                Resource creatorResource = s.getObject().asResource();
                LOG.infof("Found creator with id: %s", creatorResource);
                extractCreatorInfo(creatorResource).ifPresent(c -> creator.add(c));
            } else {
                // TODO: error message
            }
        });

        return creator;
    }

    public Optional<String> extractCreatorInfo(Resource creator) {
        Optional<Literal> name = extractLiteralProperty(creator, SchemaVocab.name);
        if (name.isPresent()) {
            String creatorName = name.get().toString();
            LOG.infof("Found name %s", creatorName);
            return Optional.of(creatorName);
        }

        Optional<Literal> givenName = extractLiteralProperty(creator, SchemaVocab.givenName);
        Optional<Literal> familyName = extractLiteralProperty(creator, SchemaVocab.familyName);
        String creatorName = Stream.of(givenName, familyName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        if (!creatorName.isEmpty()) {
            LOG.infof("Found name %s", creatorName);
            return Optional.of(creatorName);
        }

        return Optional.empty();
    }

    public Optional<String> extractPublisherInfo(Resource subject) {
        Optional<Resource> publisher = extractResourceProperty(subject, SchemaVocab.publisher);
        if (publisher.isPresent()) {
            return extractLiteralProperty(publisher.get(), SchemaVocab.name)
                    .map(Literal::toString);
        }

        return Optional.empty();
    }

    public Optional<Integer> extractPublicationYear(Resource subject) {
        Optional<Literal> publicationYear = extractLiteralProperty(subject, SchemaVocab.datePublished);
        if (publicationYear.isPresent()) {
            // TODO: We should use typed literals
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
                OffsetDateTime dateTime = OffsetDateTime.parse(publicationYear.get().toString(), formatter);
                return Optional.of(dateTime.getYear());
            } catch (DateTimeParseException e) {
                LOG.errorf("Failed to parse date: %s", e);
            }
        }

        return Optional.empty();
    }

    // TODO: for now this return URLs instead of PIDs
    public List<String> extractPidArray(Resource subject) throws PropertyNotFoundException {
        return subject
                .listProperties(SchemaVocab.hasPart)
                .toList()
                .stream()
                .map(Statement::getObject)
                .map(RDFNode::toString)
                .collect(Collectors.toList());
    }

    public String extractStringProperty(Resource subject, Property property)
            throws PropertyNotFoundException {
        return subject
                .getRequiredProperty(property)
                .getObject()
                .asLiteral()
                .getString();
    }

    public Optional<String> extractTitle(Resource subject) {
        return extractLiteralProperty(subject, SchemaVocab.title_PROP)
                .map(Literal::getString);
    }

    public Optional<String> extractAbstract(Resource subject) {
        return extractLiteralProperty(subject, SchemaVocab.abstract_)
                .map(Literal::getString);
    }

    public Optional<String> extractDataDescription(Resource subject) {
        return extractLiteralProperty(subject, SchemaVocab.description_PROP)
                .map(Literal::getString);
    }
}
