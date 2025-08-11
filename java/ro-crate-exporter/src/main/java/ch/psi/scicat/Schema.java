package ch.psi.scicat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;

public class Schema {
    private boolean scicatTypes;
    private boolean genericTypes;

    public List<DataEntity> entities = new ArrayList<>();

    public Schema(boolean scicatTypes, boolean genericTypes) {
        this.scicatTypes = scicatTypes;
        this.genericTypes = genericTypes;
        genPublishedDataClass(scicatTypes, genericTypes);
    }

    private void genPublishedDataClass(boolean scicatTypes, boolean genericTypes) {
        DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
        builder.addProperty("@id", ":PublishedData");
        builder.addType("rdfs:Class");
        builder.addIdProperty("rdfs:subClassOf", "Thing");
        entities.add(builder.build());

        if (scicatTypes) {
            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":doi");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":creator");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            builder.addIdProperty("owl:equivalentProperty", "datacite:creatorName");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":publisher");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":publicationYear");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:integer");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            builder.addIdProperty("owl:equivalentProperty", "datacite:publicationYear");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":title");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            builder.addIdProperty("owl:equivalentProperty", "datacite:title");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":abstract");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":dataDescription");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":resourceType");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":numberOfFiles");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:integer");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":pidArray");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":registeredTime");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:dateTime");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":status");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":scicatUser");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":relatedPublications");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":updatedBy");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:string");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":createdAt");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:dateTime");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());

            builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", ":updatedAt");
            builder.addType("rdfs:Property");
            builder.addIdProperty("schema:rangeIncludes", "xsd:dateTime");
            builder.addIdProperty("schema:domainIncludes", ":PublishedData");
            entities.add(builder.build());
        }
    }

    public List<AbstractEntity> genPublishedDataEntities(PublishedData pb, List<Dataset> datasets) {
        ArrayList<AbstractEntity> entities = new ArrayList<>();

        DataEntity.DataEntityBuilder publishedDataBuilder = new DataEntity.DataEntityBuilder();
        publishedDataBuilder.addProperty("@id", "https://doi.org/" + pb.doi());
        publishedDataBuilder.addTypes(List.of(":PublishedData", "CreativeWork"));

        Collection<AbstractEntity> parts = new ArrayList<>();
        datasets.forEach(d -> {
            DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
            builder.addProperty("@id", "https://dacat.psi.ch/api/v3/datasets/" + d.pid().replace("/", "%2F"));
            builder.addType("Dataset");
            builder.addProperty("name", d.datasetName());
            builder.addProperty("description", d.description());
            builder.addProperty("contentSize", d.size());
            builder.addProperty("numberOfFiles", d.numberOfFiles());
            parts.add(builder.build());
        });

        if (scicatTypes) {
            publishedDataBuilder.addProperty(":doi", pb.doi());

            Collection<AbstractEntity> creators = buildCreatorList(pb.creator(), true);
            publishedDataBuilder.addIdFromCollectionOfEntities(":creator", creators);
            entities.addAll(creators);

            publishedDataBuilder.addProperty(":publisher", pb.publisher());

            publishedDataBuilder.addProperty(":publicationYear", pb.publicationYear());

            publishedDataBuilder.addProperty(":title", pb.title());

            publishedDataBuilder.addProperty(":url", pb.url());

            publishedDataBuilder.addProperty(":abstract", pb._abstract());

            publishedDataBuilder.addProperty(":dataDescription", pb.dataDescription());

            publishedDataBuilder.addProperty(":resourceType", pb.resourceType());

            publishedDataBuilder.addProperty(":numberOfFiles", pb.numberOfFiles());

            publishedDataBuilder.addProperty(":sizeOfArchive", pb.sizeOfArchive());

            // Collection<AbstractEntity> pidArray = buildPidArray(pb.pidArray(), true);
            publishedDataBuilder.addIdFromCollectionOfEntities(":pidArray", parts);
            entities.addAll(parts);

            publishedDataBuilder.addProperty(":registeredTime", pb.registeredTime());

            publishedDataBuilder.addProperty(":status", pb.status());

            publishedDataBuilder.addProperty(":scicatUser", pb.scicatUser());

            Collection<AbstractEntity> relatedPublications = buildReleatedPublications(pb.relatedPublications(),
                    true);
            publishedDataBuilder.addIdFromCollectionOfEntities(":relatedPublications", relatedPublications);
            entities.addAll(relatedPublications);

            publishedDataBuilder.addProperty(":downloadLink", pb.downloadLink());

            publishedDataBuilder.addProperty(":updatedBy", pb.updatedBy());

            publishedDataBuilder.addProperty(":createdAt", pb.createdAt());

            publishedDataBuilder.addProperty(":updatedAt", pb.updatedAt());
        }

        if (genericTypes) {
            publishedDataBuilder.addIdProperty("publisher", "https://ror.org/03eh3y714");

            Collection<AbstractEntity> creators = buildCreatorList(pb.creator(), false);
            publishedDataBuilder.addIdFromCollectionOfEntities("creator", creators);
            entities.addAll(creators);

            publishedDataBuilder.addIdFromCollectionOfEntities("hasPart", parts);
            entities.addAll(parts);
        }

        DataEntity publishedDataEntity = publishedDataBuilder.build();

        entities.add(publishedDataEntity);
        return entities;
    }

    private Collection<AbstractEntity> buildReleatedPublications(List<String> relatedPublications, boolean b) {
        ArrayList<AbstractEntity> entities = new ArrayList<>();
        for (int i = 0; i < relatedPublications.size(); i++) {
            if (scicatTypes) {
                DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
                builder.addProperty("@id", "#publication_" + i);
                builder.addProperty("@value", relatedPublications.get(i));
                // builder.addProperty("@type", "xsd:string");
                entities.add(builder.build());
            }
        }

        return entities;

    }

    // private Collection<AbstractEntity> buildPidArray(List<String> pidArray,
    // boolean b) {
    // ArrayList<AbstractEntity> entities = new ArrayList<>();
    // for (int i = 0; i < pidArray.size(); i++) {
    // if (scicatTypes) {
    // DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
    // builder.addProperty("@id", "#pid_" + i);
    // builder.addProperty("@value", pidArray.get(i));
    // // builder.addProperty("@type", "xsd:string");
    // entities.add(builder.build());
    // }
    // }

    // return entities;
    // }

    private List<AbstractEntity> buildCreatorList(List<String> creators, boolean scicatTypes) {
        ArrayList<AbstractEntity> entities = new ArrayList<>();
        for (int i = 0; i < creators.size(); i++) {
            if (scicatTypes) {
                DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
                builder.addProperty("@id", "#creator_" + i);
                builder.addProperty("@value", creators.get(i));
                // builder.addType("xsd:string");
                entities.add(builder.build());
            } else {
                DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
                builder.addProperty("@id", "#person_" + i);
                builder.addProperty("name", creators.get(i));
                builder.addType("Person");
                entities.add(builder.build());

            }
        }

        return entities;
    }
}
