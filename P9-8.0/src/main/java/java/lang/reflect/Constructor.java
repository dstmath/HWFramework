package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import libcore.util.EmptyArray;

public final class Constructor<T> extends Executable {
    private static final Comparator<Method> ORDER_BY_SIGNATURE = null;
    private final Class<?> serializationClass;
    private final Class<?> serializationCtor;

    private native T newInstance0(Object... objArr) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    private static native Object newInstanceFromSerialization(Class<?> cls, Class<?> cls2) throws InstantiationException, IllegalArgumentException, InvocationTargetException;

    public native Class<?>[] getExceptionTypes();

    private Constructor() {
        this(null, null);
    }

    private Constructor(Class<?> serializationCtor, Class<?> serializationClass) {
        this.serializationCtor = serializationCtor;
        this.serializationClass = serializationClass;
    }

    public Constructor<T> serializationCopy(Class<?> ctor, Class<?> cl) {
        return new Constructor(ctor, cl);
    }

    boolean hasGenericInformation() {
        return super.hasGenericInformationInternal();
    }

    public Class<T> getDeclaringClass() {
        return super.getDeclaringClassInternal();
    }

    public String getName() {
        return getDeclaringClass().getName();
    }

    public int getModifiers() {
        return super.getModifiersInternal();
    }

    public TypeVariable<Constructor<T>>[] getTypeParameters() {
        return (TypeVariable[]) getMethodOrConstructorGenericInfoInternal().formalTypeParameters.clone();
    }

    public Class<?>[] getParameterTypes() {
        Class<?>[] paramTypes = super.getParameterTypesInternal();
        if (paramTypes == null) {
            return EmptyArray.CLASS;
        }
        return paramTypes;
    }

    public int getParameterCount() {
        return super.getParameterCountInternal();
    }

    public Type[] getGenericParameterTypes() {
        return super.getGenericParameterTypes();
    }

    public Type[] getGenericExceptionTypes() {
        return super.getGenericExceptionTypes();
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Constructor)) {
            Constructor<?> other = (Constructor) obj;
            if (getDeclaringClass() == other.getDeclaringClass()) {
                return equalParamTypes(getParameterTypes(), other.getParameterTypes());
            }
        }
        return false;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode();
    }

    public String toString() {
        return sharedToString(Modifier.constructorModifiers(), false, getParameterTypes(), getExceptionTypes());
    }

    void specificToStringHeader(StringBuilder sb) {
        sb.append(getDeclaringClass().getTypeName());
    }

    public String toGenericString() {
        return sharedToGenericString(Modifier.constructorModifiers(), false);
    }

    void specificToGenericStringHeader(StringBuilder sb) {
        specificToStringHeader(sb);
    }

    public T newInstance(Object... initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (this.serializationClass == null) {
            return newInstance0(initargs);
        }
        return newInstanceFromSerialization(this.serializationCtor, this.serializationClass);
    }

    public boolean isVarArgs() {
        return super.isVarArgs();
    }

    public boolean isSynthetic() {
        return super.isSynthetic();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return super.getAnnotation(annotationClass);
    }

    public Annotation[] getDeclaredAnnotations() {
        return super.getDeclaredAnnotations();
    }

    public Annotation[][] getParameterAnnotations() {
        return super.getParameterAnnotationsInternal();
    }
}
