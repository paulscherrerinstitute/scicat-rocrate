package ch.psi.rdf;

import ch.psi.rdf.RdfDeserializer.DeserializationReport;
import ch.psi.rdf.TestClasses.PrimitiveTypes;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RdfSerdeTest {
  private Model model;
  private RdfSerializer serializer = new RdfSerializer();
  private RdfDeserializer deserializer = new RdfDeserializer();

  @BeforeEach
  public void setup() {
    model = ModelFactory.createDefaultModel();
  }

  public String getTypeUri(Resource r) {
    return r.getRequiredProperty(RDF.type).getObject().asResource().getURI();
  }

  @Test
  @DisplayName("Bad input")
  public void test00() throws Exception {
    Assertions.assertTrue(serializer.serialize(model, Instant.now()).isEmpty());
    Assertions.assertTrue(serializer.serialize(null, new TestClasses.Empty()).isEmpty());
    Assertions.assertTrue(serializer.serialize(model, null).isEmpty());
    Assertions.assertTrue(serializer.serialize(null, null).isEmpty());

    Assertions.assertFalse(
        deserializer.deserialize(model.createResource(), Instant.class).isValid());
    Assertions.assertFalse(deserializer.deserialize(null, Instant.class).isValid());
    Assertions.assertFalse(deserializer.deserialize(model.createResource(), null).isValid());
    Assertions.assertFalse(deserializer.deserialize(null, null).isValid());
  }

  @Test
  @DisplayName("Empty class")
  public void test01() throws Exception {
    Resource r = serializer.serialize(model, new TestClasses.Empty()).get();
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

    Resource r = serializer.serialize(model, instance).get();
    Assertions.assertEquals(getTypeUri(r), TestClasses.NS + "Arrays");

    DeserializationReport<TestClasses.Arrays> report =
        deserializer.deserialize(r, TestClasses.Arrays.class);
    Assertions.assertTrue(report.isValid());
    Assertions.assertNotNull(report.get());
    // Order is not preserved
    TestClasses.Arrays deserializedInstance = report.get();
    Assertions.assertEquals(
        new HashSet<>(instance.stringArray), new HashSet<>(deserializedInstance.stringArray));
    Assertions.assertEquals(
        new HashSet<>(instance.integerArray), new HashSet<>(deserializedInstance.integerArray));
    Assertions.assertEquals(
        new HashSet<>(instance.doubleArray), new HashSet<>(deserializedInstance.doubleArray));
    Assertions.assertEquals(
        new HashSet<>(instance.floatArray), new HashSet<>(deserializedInstance.floatArray));
    Assertions.assertEquals(
        new HashSet<>(instance.booleanArray), new HashSet<>(deserializedInstance.booleanArray));
  }

  @Test
  @DisplayName("Custom URI")
  public void test03() throws Exception {
    TestClasses.CustomUri customURI = new TestClasses.CustomUri();
    Resource r = serializer.serialize(model, customURI).get();
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
    Resource r = serializer.serialize(model, primitiveTypes).get();
    Assertions.assertEquals(getTypeUri(r), TestClasses.NS + "PrimitiveTypes");

    Assertions.assertEquals(
        primitiveTypes.a,
        r.getProperty(TestClasses.Propstring).getObject().asLiteral().getString());
    Assertions.assertEquals(
        primitiveTypes.b, r.getProperty(TestClasses.Propinteger).getObject().asLiteral().getInt());
    Assertions.assertEquals(
        primitiveTypes.c,
        r.getProperty(TestClasses.PropintegerClass).getObject().asLiteral().getInt());
    Assertions.assertEquals(
        primitiveTypes.d, r.getProperty(TestClasses.Propdouble).getObject().asLiteral().getInt());
    Assertions.assertEquals(
        primitiveTypes.e,
        r.getProperty(TestClasses.PropdoubleClass).getObject().asLiteral().getDouble());
    Assertions.assertEquals(
        primitiveTypes.f, r.getProperty(TestClasses.Propfloat).getObject().asLiteral().getFloat());
    Assertions.assertEquals(
        primitiveTypes.g,
        r.getProperty(TestClasses.PropfloatClass).getObject().asLiteral().getFloat());
    Assertions.assertEquals(
        primitiveTypes.h,
        r.getProperty(TestClasses.Propboolean).getObject().asLiteral().getBoolean());
    Assertions.assertEquals(
        primitiveTypes.i,
        r.getProperty(TestClasses.PropbooleanClass).getObject().asLiteral().getBoolean());

    DeserializationReport<PrimitiveTypes> report =
        deserializer.deserialize(r, PrimitiveTypes.class);
    Assertions.assertTrue(report.isValid());
    Assertions.assertNotNull(report.get());
    Assertions.assertEquals(primitiveTypes, report.get());
  }

  @Test
  @DisplayName("Https annotation with http type")
  public void test05() {
    model.createResource(ResourceFactory.createResource(TestClasses.NS_http + "Empty"));
    Resource subject = model.listSubjects().toList().getFirst();
    Assertions.assertTrue(deserializer.deserialize(subject, TestClasses.Empty.class).isValid());
  }
}
