package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Objects;
import libcore.reflect.AnnotatedElements;

public final class Parameter implements AnnotatedElement {
    private final Executable executable;
    private final int index;
    private final int modifiers;
    private final String name;
    private volatile transient Class<?> parameterClassCache = null;
    private volatile transient Type parameterTypeCache = null;

    private static native <A extends Annotation> A getAnnotationNative(Executable executable, int i, Class<A> cls);

    Parameter(String name, int modifiers, Executable executable, int index) {
        this.name = name;
        this.modifiers = modifiers;
        this.executable = executable;
        this.index = index;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Parameter)) {
            return false;
        }
        Parameter other = (Parameter) obj;
        if (other.executable.lambda$-java_util_function_Predicate_4628(this.executable) && other.index == this.index) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.executable.hashCode() ^ this.index;
    }

    public boolean isNamePresent() {
        return this.executable.hasRealParameterData() && this.name != null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String typename = getParameterizedType().getTypeName();
        sb.append(Modifier.toString(getModifiers()));
        if (this.modifiers != 0) {
            sb.append(' ');
        }
        if (isVarArgs()) {
            sb.append(typename.replaceFirst("\\[\\]$", "..."));
        } else {
            sb.append(typename);
        }
        sb.append(' ');
        sb.append(getName());
        return sb.toString();
    }

    public Executable getDeclaringExecutable() {
        return this.executable;
    }

    public int getModifiers() {
        return this.modifiers;
    }

    public String getName() {
        if (this.name == null || this.name.equals("")) {
            return "arg" + this.index;
        }
        return this.name;
    }

    String getRealName() {
        return this.name;
    }

    public Type getParameterizedType() {
        Type tmp = this.parameterTypeCache;
        if (tmp != null) {
            return tmp;
        }
        tmp = this.executable.getAllGenericParameterTypes()[this.index];
        this.parameterTypeCache = tmp;
        return tmp;
    }

    public Class<?> getType() {
        Class<?> tmp = this.parameterClassCache;
        if (tmp != null) {
            return tmp;
        }
        tmp = this.executable.getParameterTypes()[this.index];
        this.parameterClassCache = tmp;
        return tmp;
    }

    public boolean isImplicit() {
        return Modifier.isMandated(getModifiers());
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    public boolean isVarArgs() {
        if (this.executable.isVarArgs() && this.index == this.executable.getParameterCount() - 1) {
            return true;
        }
        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return getAnnotationNative(this.executable, this.index, annotationClass);
    }

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }

    public Annotation[] getDeclaredAnnotations() {
        return this.executable.getParameterAnnotations()[this.index];
    }

    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return getAnnotation(annotationClass);
    }

    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return getAnnotationsByType(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }
}
