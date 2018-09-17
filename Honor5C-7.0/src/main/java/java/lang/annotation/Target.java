package java.lang.annotation;

@Documented
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Target {
    ElementType[] value();
}
