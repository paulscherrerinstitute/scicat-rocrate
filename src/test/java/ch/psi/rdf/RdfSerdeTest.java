package ch.psi.rdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    RdfSerializationException e =
        assertThrows(RdfSerializationException.class, () -> serializer.serialize(Instant.now()));
    assertEquals(
        e.getMessage(),
        "Can not serialize instance of 'java.time.Instant', missing '@RdfClass' annotation");

    assertFalse(deserializer.deserialize(model.createResource(), Instant.class).isValid());
    assertFalse(deserializer.deserialize(null, Instant.class).isValid());
    assertFalse(deserializer.deserialize(model.createResource(), null).isValid());
    assertFalse(deserializer.deserialize(null, null).isValid());
  }

  @Test
  @DisplayName("Empty class")
  public void test01() throws Exception {
    Resource r = serializer.serialize(new TestClasses.Empty());
    assertEquals(getTypeUri(r), TestClasses.NS + "Empty");
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

    Resource r = serializer.serialize(instance);
    assertEquals(getTypeUri(r), TestClasses.NS + "Arrays");

    DeserializationReport<TestClasses.Arrays> report =
        deserializer.deserialize(r, TestClasses.Arrays.class);
    assertTrue(report.isValid());
    assertNotNull(report.get());
    // Order is not preserved
    TestClasses.Arrays deserializedInstance = report.get();
    assertEquals(
        new HashSet<>(instance.stringArray), new HashSet<>(deserializedInstance.stringArray));
    assertEquals(
        new HashSet<>(instance.integerArray), new HashSet<>(deserializedInstance.integerArray));
    assertEquals(
        new HashSet<>(instance.doubleArray), new HashSet<>(deserializedInstance.doubleArray));
    assertEquals(
        new HashSet<>(instance.floatArray), new HashSet<>(deserializedInstance.floatArray));
    assertEquals(
        new HashSet<>(instance.booleanArray), new HashSet<>(deserializedInstance.booleanArray));
  }

  @Test
  @DisplayName("Custom URI")
  public void test03() throws Exception {
    TestClasses.CustomUri customURI = new TestClasses.CustomUri();
    Resource r = serializer.serialize(customURI);
    assertEquals(getTypeUri(r), TestClasses.NS + "CustomUri");
    assertEquals(customURI.customUri(), r.getURI());
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
    Resource r = serializer.serialize(primitiveTypes);
    assertEquals(getTypeUri(r), TestClasses.NS + "PrimitiveTypes");

    assertEquals(
        primitiveTypes.a,
        r.getProperty(TestClasses.Propstring).getObject().asLiteral().getString());
    assertEquals(
        primitiveTypes.b, r.getProperty(TestClasses.Propinteger).getObject().asLiteral().getInt());
    assertEquals(
        primitiveTypes.c,
        r.getProperty(TestClasses.PropintegerClass).getObject().asLiteral().getInt());
    assertEquals(
        primitiveTypes.d, r.getProperty(TestClasses.Propdouble).getObject().asLiteral().getInt());
    assertEquals(
        primitiveTypes.e,
        r.getProperty(TestClasses.PropdoubleClass).getObject().asLiteral().getDouble());
    assertEquals(
        primitiveTypes.f, r.getProperty(TestClasses.Propfloat).getObject().asLiteral().getFloat());
    assertEquals(
        primitiveTypes.g,
        r.getProperty(TestClasses.PropfloatClass).getObject().asLiteral().getFloat());
    assertEquals(
        primitiveTypes.h,
        r.getProperty(TestClasses.Propboolean).getObject().asLiteral().getBoolean());
    assertEquals(
        primitiveTypes.i,
        r.getProperty(TestClasses.PropbooleanClass).getObject().asLiteral().getBoolean());

    DeserializationReport<PrimitiveTypes> report =
        deserializer.deserialize(r, PrimitiveTypes.class);
    assertTrue(report.isValid());
    assertNotNull(report.get());
    assertEquals(primitiveTypes, report.get());
  }

  @Test
  @DisplayName("Type URI scheme mismatch (http instead of https)")
  public void test05() {
    model.createResource(ResourceFactory.createResource(TestClasses.NS_http + "Empty"));
    Resource subject = model.listSubjects().toList().getFirst();
    assertTrue(deserializer.deserialize(subject, TestClasses.Empty.class).isValid());
  }

  @Test
  @DisplayName("Type URI scheme mismatch (https instead of http)")
  public void test06() throws Exception {
    Resource serialized = serializer.serialize(new TestClasses.Empty());
    Resource subject = serialized.getModel().listSubjects().toList().getFirst();
    assertTrue(deserializer.deserialize(subject, TestClasses.EmptyHttp.class).isValid());
  }

  @Test
  @DisplayName("Property URI scheme mismatch")
  public void test07() {
    model
        .createResource(TestClasses.ResPrimitiveTypes)
        .addLiteral(
            ResourceFactory.createProperty(
                TestClasses.NS_http + TestClasses.Propdouble.getLocalName()),
            12.0);

    Resource subject = model.listSubjects().toList().getFirst();
    var report = deserializer.deserialize(subject, TestClasses.PrimitiveTypes.class);
    assertTrue(report.isValid());
    assertEquals(12.0, report.get().d);
  }
}
