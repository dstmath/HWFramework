package org.junit.experimental.theories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.experimental.theories.internal.SpecificDataPointsSupplier;

@Target({ElementType.PARAMETER})
@ParametersSuppliedBy(SpecificDataPointsSupplier.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface FromDataPoints {
    String value();
}
