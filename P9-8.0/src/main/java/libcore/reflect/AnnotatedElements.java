package libcore.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;

public final class AnnotatedElements {
    public static <T extends Annotation> T[] getDirectOrIndirectAnnotationsByType(AnnotatedElement element, Class<T> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException("annotationClass");
        }
        Annotation[] annotations = element.getDeclaredAnnotations();
        ArrayList<T> unfoldedAnnotations = new ArrayList();
        Class<? extends Annotation> repeatableAnnotationClass = getRepeatableAnnotationContainerClassFor(annotationClass);
        int i = 0;
        while (i < annotations.length) {
            if (annotationClass.isInstance(annotations[i])) {
                unfoldedAnnotations.add(annotations[i]);
            } else if (repeatableAnnotationClass != null && repeatableAnnotationClass.isInstance(annotations[i])) {
                insertAnnotationValues(annotations[i], annotationClass, unfoldedAnnotations);
            }
            i++;
        }
        return (Annotation[]) unfoldedAnnotations.toArray((Annotation[]) Array.newInstance(annotationClass, 0));
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00d9 A:{Splitter: B:18:0x00c5, ExcHandler: java.lang.IllegalAccessException (r2_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:24:0x00d9, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x00df, code:
            throw new java.lang.AssertionError(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static <T extends Annotation> void insertAnnotationValues(Annotation annotation, Class<T> annotationClass, ArrayList<T> unfoldedAnnotations) {
        Class<T[]> annotationArrayClass = ((Annotation[]) Array.newInstance(annotationClass, 0)).getClass();
        try {
            Method valuesMethod = annotation.getClass().getDeclaredMethod("value", new Class[0]);
            if (!valuesMethod.getReturnType().isArray()) {
                throw new AssertionError("annotation container = " + annotation + "annotation element class = " + annotationClass + "; value() doesn't return array");
            } else if (annotationClass.equals(valuesMethod.getReturnType().getComponentType())) {
                try {
                    Annotation[] nestedAnnotations = (Annotation[]) valuesMethod.invoke(annotation, new Object[0]);
                    for (Object add : nestedAnnotations) {
                        unfoldedAnnotations.add(add);
                    }
                } catch (ReflectiveOperationException e) {
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
