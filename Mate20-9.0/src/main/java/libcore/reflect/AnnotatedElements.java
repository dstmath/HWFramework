package libcore.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public final class AnnotatedElements {
    public static <T extends Annotation> T[] getDirectOrIndirectAnnotationsByType(AnnotatedElement element, Class<T> annotationClass) {
        if (annotationClass != null) {
            Annotation[] annotations = element.getDeclaredAnnotations();
            ArrayList<T> unfoldedAnnotations = new ArrayList<>();
            Class<? extends Annotation> repeatableAnnotationClass = getRepeatableAnnotationContainerClassFor(annotationClass);
            for (int i = 0; i < annotations.length; i++) {
                if (annotationClass.isInstance(annotations[i])) {
                    unfoldedAnnotations.add(annotations[i]);
                } else if (repeatableAnnotationClass != null && repeatableAnnotationClass.isInstance(annotations[i])) {
                    insertAnnotationValues(annotations[i], annotationClass, unfoldedAnnotations);
                }
            }
            return (Annotation[]) unfoldedAnnotations.toArray((Annotation[]) Array.newInstance(annotationClass, 0));
        }
        throw new NullPointerException("annotationClass");
    }

    private static <T extends Annotation> void insertAnnotationValues(Annotation annotation, Class<T> annotationClass, ArrayList<T> unfoldedAnnotations) {
        Class<?> cls = ((Annotation[]) Array.newInstance(annotationClass, 0)).getClass();
        try {
            Method valuesMethod = annotation.getClass().getDeclaredMethod("value", new Class[0]);
            if (!valuesMethod.getReturnType().isArray()) {
                throw new AssertionError("annotation container = " + annotation + "annotation element class = " + annotationClass + "; value() doesn't return array");
            } else if (annotationClass.equals(valuesMethod.getReturnType().getComponentType())) {
                try {
                    T[] nestedAnnotations = (Annotation[]) valuesMethod.invoke(annotation, new Object[0]);
                    for (T add : nestedAnnotations) {
                        unfoldedAnnotations.add(add);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new AssertionError(e);
                }
            } else {
                throw new AssertionError("annotation container = " + annotation + "annotation element class = " + annotationClass + "; value() returns incorrect type");
            }
        } catch (NoSuchMethodException e2) {
            throw new AssertionError("annotation container = " + annotation + "annotation element class = " + annotationClass + "; missing value() method");
        } catch (SecurityException e3) {
            throw new IncompleteAnnotationException(annotation.getClass(), "value");
        }
    }

    private static <T extends Annotation> Class<? extends Annotation> getRepeatableAnnotationContainerClassFor(Class<T> annotationClass) {
        Repeatable repeatableAnnotation = (Repeatable) annotationClass.getDeclaredAnnotation(Repeatable.class);
        if (repeatableAnnotation == null) {
            return null;
        }
        return repeatableAnnotation.value();
    }

    private AnnotatedElements() {
    }
}
