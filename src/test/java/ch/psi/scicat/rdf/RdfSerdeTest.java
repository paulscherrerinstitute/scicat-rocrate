package ch.psi.scicat.rdf;

import java.util.HashSet;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.psi.scicat.rdf.RdfDeserializer.DeserializationReport;
import ch.psi.scicat.rdf.TestClasses.PrimitiveTypes;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RdfSerdeTest {
    private Model model;

    @BeforeEach
    public void setup() {
        model = ModelFactory.createDefaultModel();
    }

    public String getTypeUri(Resource r) {
        return r.getRequiredProperty(RDF.type)
                .getObject()
                .asResource()
                .getURI();
    }

    @Test
    @DisplayName("Empty class")
    public void test01() throws Exception {
        Resource r = RdfSerializer.serialize(model, new TestClasses.Empty());
        Assertions.assertEquals(getTypeUri(r), TestClasses.NS + "Empty");
    }

    @Test
    @DisplayName("Arrays class")
    public void test02() throws Exception {
        TestClasses.Arrays instance = new TestClasses.Arrays();
        instance.stringArray = List.of("a", "b", "c");
        instance.integerArray = List.of(1, 2, 3);
        instance.doubleArray = List.of(4.4, 5.5, 6.6);
        instance.floatArray = List.of(7.7f, 8.8f, 9.9f);
        instance.booleanArray = List.of(true, false, true);

        Resource r = RdfSerializer.serialize(model, instance);
        Assertions.assertEquals(getTypeUri(r), TestClasses.NS + "Arrays");

        DeserializationReport<TestClasses.Arrays> report = RdfDeserializer.deserialize(r, TestClasses.Arrays.class);
        Assertions.assertTrue(report.isValid());
        Assertions.assertNotNull(report.get());
        // Order is not preserved
        TestClasses.Arrays deserializedInstance = report.get();
        Assertions.assertEquals(new HashSet<>(instance.stringArray), new HashSet<>(deserializedInstance.stringArray));
        Assertions.assertEquals(new HashSet<>(instance.integerArray), new HashSet<>(deserializedInstance.integerArray));
        Assertions.assertEquals(new HashSet<>(instance.doubleArray), new HashSet<>(deserializedInstance.doubleArray));
        Assertions.assertEquals(new HashSet<>(instance.floatArray), new HashSet<>(deserializedInstance.floatArray));
        Assertions.assertEquals(new HashSet<>(instance.booleanArray), new HashSet<>(deserializedInstance.booleanArray));
    }

    @Test
    @DisplayName("Custom URI")
    public void test03() throws Exception {
        TestClasses.CustomUri customURI = new TestClasses.CustomUri();
        Resource r = RdfSerializer.serialize(model, customURI);
        Assertions.assertEquals(getTypeUri(r), TestClasses.NS + "CustomUri");
        Assertions.assertEquals(customURI.customUri(), r.getURI());
    }

    @Test
    @DisplayName("Primitive types")
    public void test04() throws Exception {
        TestClasses.PrimitiveTypes primitiveTypes = new TestClasses.PrimitiveTypes();
        primitiveTypes.a = "test04";
        primitiveTypes.b = 1;
        primitiveTypes.c = Integer.valueOf(2);
        primitiveTypes.d = 3;
        primitiveTypes.e = Double.valueOf(4);
        primitiveTypes.f = 5;
        primitiveTypes.g = Float.valueOf(6);
        primitiveTypes.h = false;
        primitiveTypes.i = Boolean.TRUE;
        Resource r = RdfSerializer.serialize(model, primitiveTypes);
        Assertions.assertEquals(getTypeUri(r), TestClasses.NS + "PrimitiveTypes");

        Assertions.assertEquals(primitiveTypes.a,
                r.getProperty(TestClasses.Propstring).getObject().asLiteral().getString());
        Assertions.assertEquals(primitiveTypes.b,
                r.getProperty(TestClasses.Propinteger).getObject().asLiteral().getInt());
        Assertions.assertEquals(primitiveTypes.c,
                r.getProperty(TestClasses.PropintegerClass).getObject().asLiteral().getInt());
        Assertions.assertEquals(primitiveTypes.d,
                r.getProperty(TestClasses.Propdouble).getObject().asLiteral().getInt());
        Assertions.assertEquals(primitiveTypes.e,
                r.getProperty(TestClasses.PropdoubleClass).getObject().asLiteral().getDouble());
        Assertions.assertEquals(primitiveTypes.f,
                r.getProperty(TestClasses.Propfloat).getObject().asLiteral().getFloat());
        Assertions.assertEquals(primitiveTypes.g,
                r.getProperty(TestClasses.PropfloatClass).getObject().asLiteral().getFloat());
        Assertions.assertEquals(primitiveTypes.h,
                r.getProperty(TestClasses.Propboolean).getObject().asLiteral().getBoolean());
        Assertions.assertEquals(primitiveTypes.i,
                r.getProperty(TestClasses.PropbooleanClass).getObject().asLiteral().getBoolean());

        DeserializationReport<PrimitiveTypes> report = RdfDeserializer.deserialize(r, PrimitiveTypes.class);
        Assertions.assertTrue(report.isValid());
        Assertions.assertNotNull(report.get());
        Assertions.assertEquals(primitiveTypes, report.get());
    }
}
