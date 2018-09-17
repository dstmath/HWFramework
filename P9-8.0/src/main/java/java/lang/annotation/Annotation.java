package java.lang.annotation;

public interface Annotation {
    Class<? extends Annotation> annotationType();

    boolean equals(Object obj);

    int hashCode();

    String toString();
}
