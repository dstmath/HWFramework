package java.lang.annotation;

@Target({ElementType.METHOD})
@Repeatable(List.class)
@Retention(RetentionPolicy.CLASS)
public @interface RCUnownedCapRef {

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.CLASS)
    public @interface List {
        RCUnownedCapRef[] value();
    }

    String value() default "";
}
