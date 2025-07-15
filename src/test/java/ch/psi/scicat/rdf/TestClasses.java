package ch.psi.scicat.rdf;

import java.util.List;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class TestClasses {
    public static final String NS = "http://testclasses.org/";

    @RdfClass(typesUri = NS + "Empty")
    public static class Empty {
    }

    @RdfClass(typesUri = NS + "StrArray")
    public static class StrArray {
        @RdfProperty(uri = NS + "strArray")
        List<String> strArray = List.of("a", "b", "c");
    }

    @RdfClass(typesUri = NS + "CustomUri")
    public static class CustomUri {
        @RdfResourceUri
        public String customUri() {
            return NS + "generated-uri";
        }
    }

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
                return (a == null ? other.a == null : a.equals(other.a)) && b == other.b
                        && (c == null ? other.c == null : c.equals(other.c)) && Double.compare(d, other.d) == 0
                        && (e == null ? other.e == null : e.equals(other.e)) && Float.compare(f, other.f) == 0
                        && (g == null ? other.g == null : g.equals(other.g)) && h == other.h
                        && (i == null ? other.i == null : i.equals(other.i));
            }
            return false;
        }

    }
}
