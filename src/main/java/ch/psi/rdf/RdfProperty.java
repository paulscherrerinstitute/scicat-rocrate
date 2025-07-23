package ch.psi.rdf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RdfProperty {
  String uri();

  int minCardinality() default 0;

  int maxCardinality() default Integer.MAX_VALUE;

  String[] equivalentProperties() default {};
}
