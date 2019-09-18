package java.lang.annotation;

@Target({ElementType.METHOD})
@Repeatable(List.class)
@Retention(RetentionPolicy.CLASS)
public @interface RCLocalUnownedRef {

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.CLASS)
    public @interface List {
        RCLocalUnownedRef[] value();
    }

    String value() default "";
}
