package java.lang.reflect;

import java.lang.annotation.Annotation;

public class AccessibleObject implements AnnotatedElement {
    boolean override;

    public static void setAccessible(AccessibleObject[] array, boolean flag) throws SecurityException {
        for (AccessibleObject accessible0 : array) {
            setAccessible0(accessible0, flag);
        }
    }

    public void setAccessible(boolean flag) throws SecurityException {
        setAccessible0(this, flag);
    }

    private static void setAccessible0(AccessibleObject obj, boolean flag) throws SecurityException {
        if ((obj instanceof Constructor) && flag) {
            Constructor<?> c = (Constructor) obj;
            Class<?> clazz = c.getDeclaringClass();
            if (c.getDeclaringClass() == Class.class) {
                throw new SecurityException("Can not make a java.lang.Class constructor accessible");
            } else if (clazz == Method.class) {
                throw new SecurityException("Can not make a java.lang.reflect.Method constructor accessible");
            } else if (clazz == Field.class) {
                throw new SecurityException("Can not make a java.lang.reflect.Field constructor accessible");
            }
        }
        obj.override = flag;
    }

    public boolean isAccessible() {
        return this.override;
    }

    protected AccessibleObject() {
    }

    public <T extends Annotation> T getAnnotation(Class<T> cls) {
        throw new AssertionError((Object) "All subclasses should override this method");
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return super.isAnnotationPresent(annotationClass);
    }

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> cls) {
        throw new AssertionError((Object) "All subclasses should override this method");
    }

    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return getAnnotation(annotationClass);
    }

    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return getAnnotationsByType(annotationClass);
    }

    public Annotation[] getDeclaredAnnotations() {
        throw new AssertionError((Object) "All subclasses should override this method");
    }
}
