package android.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface TimedTest {
    boolean includeDetailedStats() default false;
}
