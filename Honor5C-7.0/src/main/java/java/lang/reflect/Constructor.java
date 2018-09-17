package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.regex.Pattern;
import libcore.reflect.Types;

public final class Constructor<T> extends AbstractMethod implements GenericDeclaration, Member {
    private static final Comparator<Method> ORDER_BY_SIGNATURE = null;
    private final Class<?> serializationClass;
    private final Class<?> serializationCtor;

    private native <A extends Annotation> A getAnnotationNative(Class<A> cls);

    private native Annotation[][] getParameterAnnotationsNative();

    private native boolean isAnnotationPresentNative(Class<? extends Annotation> cls);

    private native T newInstance0(Object... objArr) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    private static native Object newInstanceFromSerialization(Class<?> cls, Class<?> cls2) throws InstantiationException, IllegalArgumentException, InvocationTargetException;

    public native Annotation[] getDeclaredAnnotations();

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

    public Class<T> getDeclaringClass() {
        return super.getDeclaringClass();
    }

    public String getName() {
        return getDeclaringClass().getName();
    }

    public int getModifiers() {
        return super.getModifiers();
    }

    public TypeVariable<Constructor<T>>[] getTypeParameters() {
        return (TypeVariable[]) getMethodOrConstructorGenericInfo().formalTypeParameters.clone();
    }

    public Class<?>[] getParameterTypes() {
        return super.getParameterTypes();
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
                Class<?>[] params1 = getParameterTypes();
                Class<?>[] params2 = other.getParameterTypes();
                if (params1.length == params2.length) {
                    for (int i = 0; i < params1.length; i++) {
                        if (params1[i] != params2[i]) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode();
    }

    public String toString() {
        try {
            StringBuffer sb = new StringBuffer();
            int mod = getModifiers() & Modifier.constructorModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod) + " ");
            }
            sb.append(Field.getTypeName(getDeclaringClass()));
            sb.append("(");
            Class<?>[] params = getParameterTypes();
            for (int j = 0; j < params.length; j++) {
                sb.append(Field.getTypeName(params[j]));
                if (j < params.length - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            Class<?>[] exceptions = getExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
                    if (k < exceptions.length - 1) {
                        sb.append(",");
                    }
                }
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    public String toGenericString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.constructorModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(" ");
            }
            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append("<");
                for (TypeVariable<?> typeparm : typeparms) {
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }
            sb.append(Field.getTypeName(getDeclaringClass()));
            sb.append("(");
            Type[] params = getGenericParameterTypes();
            int j = 0;
            while (j < params.length) {
                String param;
                if (params[j] instanceof Class) {
                    param = Field.getTypeName((Class) params[j]);
                } else {
                    param = params[j].toString();
                }
                if (isVarArgs() && j == params.length - 1) {
                    param = param.replaceFirst("\\[\\]$", "...");
                }
                sb.append(param);
                if (j < params.length - 1) {
                    sb.append(",");
                }
                j++;
            }
            sb.append(")");
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    String name;
                    if (exceptions[k] instanceof Class) {
                        name = ((Class) exceptions[k]).getName();
                    } else {
                        name = exceptions[k].toString();
                    }
                    sb.append(name);
                    if (k < exceptions.length - 1) {
                        sb.append(",");
                    }
                }
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    public T newInstance(Object... args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (this.serializationClass == null) {
            return newInstance0(args);
        }
        return newInstanceFromSerialization(this.serializationCtor, this.serializationClass);
    }

    public boolean isVarArgs() {
        return (getModifiers() & Pattern.CANON_EQ) != 0;
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    String getSignature() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (Class<?> parameterType : getParameterTypes()) {
            result.append(Types.getSignature(parameterType));
        }
        result.append(")V");
        return result.toString();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        if (annotationType != null) {
            return getAnnotationNative(annotationType);
        }
        throw new NullPointerException("annotationType == null");
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        if (annotationType != null) {
            return isAnnotationPresentNative(annotationType);
        }
        throw new NullPointerException("annotationType == null");
    }

    public Annotation[][] getParameterAnnotations() {
        Annotation[][] parameterAnnotations = getParameterAnnotationsNative();
        if (parameterAnnotations != null) {
            return parameterAnnotations;
        }
        return (Annotation[][]) Array.newInstance(Annotation.class, getParameterTypes().length, 0);
    }
}
