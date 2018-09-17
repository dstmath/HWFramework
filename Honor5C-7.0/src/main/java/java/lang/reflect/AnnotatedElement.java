package java.lang.reflect;

import java.lang.annotation.Annotation;
import libcore.reflect.AnnotatedElements;

public interface AnnotatedElement {
    <T extends Annotation> T getAnnotation(Class<T> cls);

    Annotation[] getAnnotations();

    Annotation[] getDeclaredAnnotations();

    boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    <T extends Annotation> Annotation getDeclaredAnnotation(Class<T> annotationClass) {
        return AnnotatedElements.getDeclaredAnnotation(this, annotationClass);
    }

    <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDeclaredAnnotationsByType(this, annotationClass);
    }

    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getAnnotationsByType(this, annotationClass);
    }
}
