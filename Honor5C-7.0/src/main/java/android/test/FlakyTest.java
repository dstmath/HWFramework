package android.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface FlakyTest {
    int tolerance() default 1;
}
