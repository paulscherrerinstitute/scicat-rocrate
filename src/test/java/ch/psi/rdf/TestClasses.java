package ch.psi.rdf;

import ch.psi.rdf.TestClasses.CustomClassLevelDeser.customDeserializer;
import ch.psi.rdf.TestClasses.CustomClassLevelSer.customSerializer;
import ch.psi.rdf.annotations.RdfClass;
import ch.psi.rdf.annotations.RdfDeserialize;
import ch.psi.rdf.annotations.RdfProperty;
import ch.psi.rdf.annotations.RdfResourceUri;
import ch.psi.rdf.annotations.RdfSerialize;
import ch.psi.rdf.deser.RdfDeserializationContext;
import ch.psi.rdf.deser.RdfDeserializationException;
import ch.psi.rdf.deser.RdfDeserializer;
import ch.psi.rdf.ser.RdfSerializationContext;
import ch.psi.rdf.ser.RdfSerializationException;
import ch.psi.rdf.ser.RdfSerializer;
import java.util.List;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.SchemaDO;

public class TestClasses {
  public static final String NS = "https://testclasses.org/";
  public static final String NS_http = "http://testclasses.org/";

  @RdfClass(typesUri = NS + "Empty")
  public static class Empty {}

  @RdfClass(typesUri = NS_http + "Empty")
  public static class EmptyHttp {}

  static final Property PropstringArray = ResourceFactory.createProperty(NS + "stringArray");
  static final Property PropintegerArray = ResourceFactory.createProperty(NS + "integerArray");
  static final Property PropdoubleArray = ResourceFactory.createProperty(NS + "doubleArray");
  static final Property PropfloatArray = ResourceFactory.createProperty(NS + "floatArray");
  static final Property PropbooleanArray = ResourceFactory.createProperty(NS + "booleanArray");

  @RdfClass(typesUri = NS + "Arrays")
  public static class Arrays {
    @RdfProperty(uri = NS + "stringArray")
    List<String> stringArray;

    @RdfProperty(uri = NS + "integerArray")
    List<Integer> integerArray;

    @RdfProperty(uri = NS + "doubleArray")
    List<Double> doubleArray;

    @RdfProperty(uri = NS + "floatArray")
    List<Float> floatArray;

    @RdfProperty(uri = NS + "booleanArray")
    List<Boolean> booleanArray;
  }

  @RdfClass(typesUri = NS + "CustomUri")
  public static class CustomUri {
    @RdfResourceUri
    public String customUri() {
      return NS + "generated-uri";
    }
  }

  static final Resource ResPrimitiveTypes = ResourceFactory.createProperty(NS + "PrimitiveTypes");
  static final Property Propstring = ResourceFactory.createProperty(NS + "string");
  static final Property Propinteger = ResourceFactory.createProperty(NS + "integer");
  static final Property Propdouble = ResourceFactory.createProperty(NS + "double");
  static final Property Propfloat = ResourceFactory.createProperty(NS + "float");
  static final Property Propboolean = ResourceFactory.createProperty(NS + "boolean");
  static final Property PropintegerClass = ResourceFactory.createProperty(NS + "integerClass");
  static final Property PropdoubleClass = ResourceFactory.createProperty(NS + "doubleClass");
  static final Property PropfloatClass = ResourceFactory.createProperty(NS + "floatClass");
  static final Property PropbooleanClass = ResourceFactory.createProperty(NS + "booleanClass");

  @RdfClass(typesUri = NS + "PrimitiveTypes")
  public static class PrimitiveTypes {
    @RdfProperty(uri = NS + "string")
    String a;

    @RdfProperty(uri = NS + "integer")
    int b;

    @RdfProperty(uri = NS + "integerClass")
    Integer c;

    @RdfProperty(uri = NS + "double")
    double d;

    @RdfProperty(uri = NS + "doubleClass")
    Double e;

    @RdfProperty(uri = NS + "float")
    float f;

    @RdfProperty(uri = NS + "floatClass")
    Float g;

    @RdfProperty(uri = NS + "boolean")
    boolean h;

    @RdfProperty(uri = NS + "booleanClass")
    Boolean i;

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj != null && obj instanceof PrimitiveTypes other) {
        return (a == null ? other.a == null : a.equals(other.a))
            && b == other.b
            && (c == null ? other.c == null : c.equals(other.c))
            && Double.compare(d, other.d) == 0
            && (e == null ? other.e == null : e.equals(other.e))
            && Float.compare(f, other.f) == 0
            && (g == null ? other.g == null : g.equals(other.g))
            && h == other.h
            && (i == null ? other.i == null : i.equals(other.i));
      }
      return false;
    }
  }

  @RdfClass(typesUri = NS + "CustomSerializer")
  @RdfSerialize(using = customSerializer.class)
  public static class CustomClassLevelSer {
    String a = "value";

    public static class customSerializer implements RdfSerializer<CustomClassLevelSer> {
      @Override
      public List<RDFNode> serialize(CustomClassLevelSer value, RdfSerializationContext context)
          throws RdfSerializationException {
        return List.of(context.getModel().createResource().addProperty(SchemaDO.value, value.a));
      }
    }
  }

  @RdfClass(typesUri = NS + "CustomDeserializer")
  @RdfDeserialize(using = customDeserializer.class)
  public static class CustomClassLevelDeser {
    boolean hasName;

    public static class customDeserializer implements RdfDeserializer<CustomClassLevelDeser> {
      @Override
      public CustomClassLevelDeser deserialize(RDFNode node, RdfDeserializationContext context)
          throws RdfDeserializationException {
        CustomClassLevelDeser result = new CustomClassLevelDeser();
        result.hasName = node.asResource().hasProperty(SchemaDO.name);
        return result;
      }
    }
  }

  @RdfClass(typesUri = NS + "CustomSerializer")
  public static class CustomFieldLevelSer {
    @RdfProperty(uri = SchemaDO.NS + "value")
    @RdfSerialize(using = toUpperSerializer.class)
    String a = "value";

    public static class toUpperSerializer implements RdfSerializer<String> {
      @Override
      public List<RDFNode> serialize(String value, RdfSerializationContext context)
          throws RdfSerializationException {
        return List.of(context.getModel().createLiteral(value.toUpperCase()));
      }
    }
  }

  @RdfClass(typesUri = NS + "CustomDeserializer")
  public static class CustomFieldLevelDeser {
    @RdfProperty(uri = SchemaDO.NS + "name")
    @RdfDeserialize(using = toLowerDeserializer.class)
    String name;

    public static class toLowerDeserializer implements RdfDeserializer<String> {
      @Override
      public String deserialize(RDFNode node, RdfDeserializationContext context)
          throws RdfDeserializationException {
        return node.asLiteral().getString().toLowerCase();
      }
    }
  }
}
