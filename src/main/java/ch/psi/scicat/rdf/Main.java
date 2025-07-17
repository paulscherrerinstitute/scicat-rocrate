package ch.psi.scicat.rdf;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.vocabulary.SchemaDO;

import ch.psi.scicat.model.ValidationError;
import ch.psi.scicat.rdf.RdfDeserializer.DeserializationReport;

public class Main {
    static RdfSerializer serializer = new RdfSerializer();
    static RdfDeserializer deserializer = new RdfDeserializer();

    @RdfClass(typesUri = SchemaDO.NS + "Person")
    public static class Person {
        @RdfProperty(uri = SchemaDO.NS + "name")
        String name;
        @RdfProperty(uri = SchemaDO.NS + "sibling")
        Person sibling = null;
        @RdfProperty(uri = SchemaDO.NS + "test", maxCardinality = 5)
        List<String> test;

        public Person() {
        }

        public Person(String name, Person sibling) {
            this.name = name;
            this.sibling = sibling;
        }

        public String getName() {
            return name;
        }

        public Person getSibling() {
            return sibling;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder()
                    .append("Person: ")
                    .append(name)
                    .append("\n");
            if (sibling != null) {
                sb.append("  sibling:")
                        .append("\n    ")
                        .append(sibling);
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        Person alice = new Person("Alice", null);
        Person bob = new Person("Bob", alice);
        bob.test = List.of("a", "b");

        System.out.println(bob);

        Model model = ModelFactory.createDefaultModel();
        serializer.serialize(model, bob);
        RDFWriter.source(model)
                .format(RDFFormat.JSONLD11)
                .output(System.out);

        Resource serializedBob = model.listSubjectsWithProperty(SchemaDO.sibling).next();
        DeserializationReport<Person> report = deserializer.deserialize(serializedBob, Person.class);
        if (report.isValid()) {
            Person deserializedPerson = report.get();
            System.out.println(deserializedPerson.test);
        } else {
            for (ValidationError e : report.getErrors()) {
                System.err.println(e.getMessage());
            }
        }
    }
}
