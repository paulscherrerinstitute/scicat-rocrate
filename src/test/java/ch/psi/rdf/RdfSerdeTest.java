package ch.psi.rdf;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.psi.rdf.TestClasses.CustomClassLevelSer;
import ch.psi.rdf.TestClasses.PrimitiveTypes;
import ch.psi.rdf.deser.DeserializationReport;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.ser.RdfSerializationException;
import io.quarkus.test.junit.QuarkusTest;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SchemaDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RdfSerdeTest {
  private Model model;
  private final RdfMapper rdfMapper = new RdfMapper();

  @BeforeEach
  public void setup() {
    model = ModelFactory.createDefaultModel();
  }

  private Resource serializeToResource(Object value) {
    return assertDoesNotThrow(() -> rdfMapper.serialize(value).asResource());
  }

  private <T> DeserializationReport<T> getReport(Resource subject, Class<T> clazz) {
    return assertDoesNotThrow(() -> rdfMapper.deserialize(subject, clazz));
  }

  private String getTypeUri(Resource r) {
    return r.getRequiredProperty(RDF.type).getObject().asResource().getURI();
  }

  @Test
  @DisplayName("Bad input")
  public void test00() {
    RdfSerializationException e =
        assertThrows(RdfSerializationException.class, () -> rdfMapper.serialize(new LongAdder()));

    assertTrue(e.getMessage().contains("missing '@RdfClass'"));

    assertAll(
        "Deserialization error cases",
        () ->
            assertThrows(
                RdfDeserializationException.class,
                () -> rdfMapper.deserialize(model.createResource(), LongAdder.class)),
        () ->
            assertThrows(
                RdfDeserializationException.class,
                () -> rdfMapper.deserialize(null, LongAdder.class)),
        () ->
            assertThrows(
                NullPointerException.class,
                () -> rdfMapper.deserialize(model.createResource(), null)),
        () -> assertThrows(NullPointerException.class, () -> rdfMapper.deserialize(null, null)));
  }

  @Test
  @DisplayName("Empty class")
  public void test01() {
    Resource r = serializeToResource(new TestClasses.Empty());
    assertEquals(TestClasses.NS + "Empty", getTypeUri(r));
  }

  @Test
  @DisplayName("Arrays class")
  public void test02() {
    TestClasses.Arrays instance = new TestClasses.Arrays();
    instance.stringArray = List.of("a", "b", "c");
    instance.integerArray = List.of(1, 2, 3);
    instance.doubleArray = List.of(4.4, 5.5, 6.6);
    instance.floatArray = List.of(7.7f, 8.8f, 9.9f);
    instance.booleanArray = List.of(true, false, true);

    Resource r = serializeToResource(instance);
    assertEquals(TestClasses.NS + "Arrays", getTypeUri(r));

    DeserializationReport<TestClasses.Arrays> report = getReport(r, TestClasses.Arrays.class);
    assertTrue(report.isValid());

    TestClasses.Arrays actual = report.get();
    assertAll(
        "Collection equality (order insensitive)",
        () -> assertEquals(new HashSet<>(instance.stringArray), new HashSet<>(actual.stringArray)),
        () ->
            assertEquals(new HashSet<>(instance.integerArray), new HashSet<>(actual.integerArray)),
        () -> assertEquals(new HashSet<>(instance.doubleArray), new HashSet<>(actual.doubleArray)),
        () -> assertEquals(new HashSet<>(instance.floatArray), new HashSet<>(actual.floatArray)),
        () ->
            assertEquals(new HashSet<>(instance.booleanArray), new HashSet<>(actual.booleanArray)));
  }

  @Test
  @DisplayName("Custom URI")
  public void test03() {
    TestClasses.CustomUri customURI = new TestClasses.CustomUri();
    Resource r = serializeToResource(customURI);

    assertAll(
        () -> assertEquals(TestClasses.NS + "CustomUri", getTypeUri(r)),
        () -> assertEquals(customURI.customUri(), r.getURI()));
  }

  @Test
  @DisplayName("Primitive types")
  public void test04() {
    TestClasses.PrimitiveTypes expected = new TestClasses.PrimitiveTypes();
    expected.a = "test04";
    expected.b = 1;
    expected.c = 2;
    expected.d = 3;
    expected.e = 4.0;
    expected.f = 5.0f;
    expected.g = 6.0f;
    expected.h = false;
    expected.i = Boolean.TRUE;

    Resource r = serializeToResource(expected);
    assertAll(
        "RDF Property mapping",
        () -> assertEquals(expected.a, r.getProperty(TestClasses.Propstring).getString()),
        () -> assertEquals(expected.b, r.getProperty(TestClasses.Propinteger).getInt()),
        () -> assertEquals(expected.e, r.getProperty(TestClasses.PropdoubleClass).getDouble()),
        () -> assertEquals(expected.h, r.getProperty(TestClasses.Propboolean).getBoolean()));

    DeserializationReport<PrimitiveTypes> report = getReport(r, PrimitiveTypes.class);
    assertTrue(report.isValid());
    assertEquals(expected, report.get());
  }

  @Test
  @DisplayName("Type URI scheme mismatch (http instead of https)")
  public void test05() {
    Resource subject =
        model
            .createResource("http://example.com/some-resource")
            .addProperty(RDF.type, model.createResource(TestClasses.NS_http + "Empty"));
    assertTrue(getReport(subject, TestClasses.Empty.class).isValid());
  }

  @Test
  @DisplayName("Type URI scheme mismatch (https instead of http)")
  public void test06() {
    Resource serialized = serializeToResource(new TestClasses.Empty());
    assertTrue(getReport(serialized, TestClasses.EmptyHttp.class).isValid());
  }

  @Test
  @DisplayName("Property URI scheme mismatch")
  public void test07() {
    Property httpProp =
        ResourceFactory.createProperty(TestClasses.NS_http + TestClasses.Propdouble.getLocalName());
    Resource subject =
        model.createResource(TestClasses.ResPrimitiveTypes).addLiteral(httpProp, 12.0);

    var report = getReport(subject, TestClasses.PrimitiveTypes.class);
    assertTrue(report.isValid());
    assertEquals(12.0, report.get().d);
  }

  @Test
  @DisplayName("Class level custom serializer")
  public void test08() {
    CustomClassLevelSer instance = new CustomClassLevelSer();
    Resource r = serializeToResource(instance);
    assertEquals(instance.a, r.getProperty(SchemaDO.value).getString());
  }
}
