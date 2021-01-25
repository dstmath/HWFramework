package android.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Repeatable(Container.class)
@Retention(RetentionPolicy.CLASS)
public @interface UnsupportedAppUsage {

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.CLASS)
    public @interface Container {
        UnsupportedAppUsage[] value();
    }

    String expectedSignature() default "";

    String implicitMember() default "";

    int maxTargetSdk() default Integer.MAX_VALUE;

    long trackingBug() default 0;
}
