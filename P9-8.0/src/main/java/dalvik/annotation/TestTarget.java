package dalvik.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface TestTarget {
    String conceptName() default "";

    Class<?>[] methodArgs() default {};

    String methodName() default "";
}
