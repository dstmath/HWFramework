package huawei.android.security.secai.hookcase.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BackupMethod {
    String name() default "<init>";

    Class[] params() default {};

    String[] reflectionParams() default {};

    String reflectionTargetClass() default "";

    Class targetClass() default Object.class;
}
