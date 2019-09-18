package libcore.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE_USE})
@Retention(RetentionPolicy.SOURCE)
public @interface Nullable {
    int from() default Integer.MIN_VALUE;

    int to() default Integer.MAX_VALUE;
}
