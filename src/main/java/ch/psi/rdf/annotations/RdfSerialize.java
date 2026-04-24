package ch.psi.rdf.annotations;

import ch.psi.rdf.ser.RdfSerializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RdfSerialize {
  Class<? extends RdfSerializer<?>> using();
}
