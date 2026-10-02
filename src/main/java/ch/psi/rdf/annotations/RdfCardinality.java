package ch.psi.rdf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface RdfCardinality {
  String uri();

  int min() default 0;

  int max() default Integer.MAX_VALUE;
}
