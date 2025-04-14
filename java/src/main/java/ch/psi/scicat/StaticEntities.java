package ch.psi.scicat;

import java.util.ArrayList;
import java.util.Collection;

import org.schema.SchemaVocab;

import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity.ContextualEntityBuilder;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity.DataEntityBuilder;

public class StaticEntities {
    public static final ContextualEntity LICENSE;
    public static final ContextualEntity PSI;
    public static final Collection<DataEntity> PUBLISHEDDATA_SCHEMA = new ArrayList<>();
    public static final String DATACITE_URL = "https://schema.datacite.org/meta/kernel-4/metadata.xsd#";

    static {
        ContextualEntityBuilder licenseBuilder = new ContextualEntityBuilder();
        licenseBuilder.setId("https://creativecommons.org/licenses/by-sa/4.0/");
        licenseBuilder.addType(SchemaVocab.CreativeWork.getLocalName());
        licenseBuilder.addProperty("identifier", "https://creativecommons.org/licenses/by-sa/4.0/");
        licenseBuilder.addProperty("name", "Creative Commons Attribution Share Alike 4.0 International");
        LICENSE = licenseBuilder.build();
    }

    static {
        ContextualEntityBuilder psiBuilder = new ContextualEntityBuilder();
        psiBuilder.setId("https://ror.org/03eh3y714");
        psiBuilder.addType(SchemaVocab.Organization.getLocalName());
        psiBuilder.addProperty("name", "Paul Scherrer Institute");
        PSI = psiBuilder.build();
    }

    static {
        DataEntityBuilder builder = new DataEntityBuilder();
        builder.setId("scicat:PublishedData");
        builder.addType("rdfs:Class");
        builder.addIdProperty("owl:equivalentClass", SchemaVocab.CreativeWork.getURI());
        PUBLISHEDDATA_SCHEMA.add(builder.build());

        builder = new DataEntityBuilder();
        builder.setId("scicat:relatedPublications");
        builder.addType("rdfs:Property");
        builder.addIdProperty(SchemaVocab.rangeIncludes.getURI(), "xsd:integer");
        builder.addIdProperty(SchemaVocab.domainIncludes.getURI(), "PublishedData");
        builder.addIdProperty("owl:equivalentProperty", DATACITE_URL + "relatedIdentifer");
        PUBLISHEDDATA_SCHEMA.add(builder.build());

        builder = new DataEntityBuilder();
        builder.setId("scicat:numberOfFiles");
        builder.addType("rdfs:Property");
        builder.addIdProperty(SchemaVocab.rangeIncludes.getURI(), "xsd:integer");
        builder.addIdProperty(SchemaVocab.domainIncludes.getURI(), "PublishedData");
        PUBLISHEDDATA_SCHEMA.add(builder.build());

        builder = new DataEntityBuilder();
        builder.setId("scicat:sizeOfArchive");
        builder.addType("rdfs:Property");
        builder.addIdProperty(SchemaVocab.rangeIncludes.getURI(), "xsd:integer");
        builder.addIdProperty(SchemaVocab.domainIncludes.getURI(), "PublishedData");
        PUBLISHEDDATA_SCHEMA.add(builder.build());

        builder = new DataEntityBuilder();
        builder.setId("scicat:scicatUser");
        builder.addType("rdfs:Property");
        builder.addIdProperty(SchemaVocab.rangeIncludes.getURI(), "xsd:string");
        builder.addIdProperty(SchemaVocab.domainIncludes.getURI(), "PublishedData");
        PUBLISHEDDATA_SCHEMA.add(builder.build());
    }
}