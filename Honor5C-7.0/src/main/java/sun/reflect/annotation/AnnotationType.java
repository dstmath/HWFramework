package sun.reflect.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

public class AnnotationType {
    private boolean inherited;
    private final Map<String, Object> memberDefaults;
    private final Map<String, Class<?>> memberTypes;
    private final Map<String, Method> members;
    private RetentionPolicy retention;

    /* renamed from: sun.reflect.annotation.AnnotationType.1 */
    class AnonymousClass1 implements PrivilegedAction<Method[]> {
        final /* synthetic */ Class val$annotationClass;

        AnonymousClass1(Class val$annotationClass) {
            this.val$annotationClass = val$annotationClass;
        }

        public Method[] run() {
            return this.val$annotationClass.getDeclaredMethods();
        }
    }

    public static synchronized AnnotationType getInstance(Class<? extends Annotation> annotationClass) {
        AnnotationType result;
        synchronized (AnnotationType.class) {
            result = annotationClass.getAnnotationType();
            if (result == null) {
                result = new AnnotationType(annotationClass);
            }
        }
        return result;
    }

    private AnnotationType(Class<? extends Annotation> annotationClass) {
        int i = 0;
        this.memberTypes = new HashMap();
        this.memberDefaults = new HashMap();
        this.members = new HashMap();
        this.retention = RetentionPolicy.RUNTIME;
        this.inherited = false;
        if (annotationClass.isAnnotation()) {
            Method[] methods = (Method[]) AccessController.doPrivileged(new AnonymousClass1(annotationClass));
            int length = methods.length;
            while (i < length) {
                Object method = methods[i];
                if (method.getParameterTypes().length != 0) {
                    throw new IllegalArgumentException(method + " has params");
                }
                String name = method.getName();
                this.memberTypes.put(name, invocationHandlerReturnType(method.getReturnType()));
                this.members.put(name, method);
                Object defaultValue = method.getDefaultValue();
                if (defaultValue != null) {
                    this.memberDefaults.put(name, defaultValue);
                }
                this.members.put(name, method);
                i++;
            }
            annotationClass.setAnnotationType(this);
            if (annotationClass != Retention.class && annotationClass != Inherited.class) {
                Retention ret = (Retention) annotationClass.getAnnotation(Retention.class);
                this.retention = ret == null ? RetentionPolicy.CLASS : ret.value();
                this.inherited = annotationClass.isAnnotationPresent(Inherited.class);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Not an annotation type");
    }

    public static Class<?> invocationHandlerReturnType(Class<?> type) {
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        return type;
    }

    public Map<String, Class<?>> memberTypes() {
        return this.memberTypes;
    }

    public Map<String, Method> members() {
        return this.members;
    }

    public Map<String, Object> memberDefaults() {
        return this.memberDefaults;
    }

    public RetentionPolicy retention() {
        return this.retention;
    }

    public boolean isInherited() {
        return this.inherited;
    }

    public String toString() {
        StringBuffer s = new StringBuffer("Annotation Type:\n");
        s.append("   Member types: " + this.memberTypes + "\n");
        s.append("   Member defaults: " + this.memberDefaults + "\n");
        s.append("   Retention policy: " + this.retention + "\n");
        s.append("   Inherited: " + this.inherited);
        return s.toString();
    }
}
