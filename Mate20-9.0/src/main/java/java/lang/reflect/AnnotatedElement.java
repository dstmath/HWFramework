package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Objects;
import libcore.reflect.AnnotatedElements;

public interface AnnotatedElement {
    <T extends Annotation> T getAnnotation(Class<T> cls);

    Annotation[] getAnnotations();

    Annotation[] getDeclaredAnnotations();

    boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }

    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        for (Annotation annotation : getDeclaredAnnotations()) {
            if (annotationClass.equals(annotation.annotationType())) {
                return (Annotation) annotationClass.cast(annotation);
            }
        }
        return null;
    }

    <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }
}
