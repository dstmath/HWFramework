package ohos.utils.fastjson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ohos.utils.fastjson.PropertyNamingStrategy;
import ohos.utils.fastjson.parser.Feature;
import ohos.utils.fastjson.serializer.SerializerFeature;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONType {
    boolean alphabetic() default true;

    boolean asm() default true;

    String[] ignores() default {};

    Class<?> mappingTo() default Void.class;

    PropertyNamingStrategy naming() default PropertyNamingStrategy.CamelCase;

    String[] orders() default {};

    Feature[] parseFeatures() default {};

    Class<?>[] seeAlso() default {};

    SerializerFeature[] serialzeFeatures() default {};

    String typeKey() default "";

    String typeName() default "";
}
