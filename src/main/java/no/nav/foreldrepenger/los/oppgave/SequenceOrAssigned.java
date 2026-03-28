package no.nav.foreldrepenger.los.oppgave;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(SequenceOrAssignedGenerator.class)
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface SequenceOrAssigned {
    String sequence() default "SEQ_GLOBAL_PK";
}
