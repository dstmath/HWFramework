package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import libcore.reflect.Types;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

public final class Method extends Executable {
    public static final Comparator<Method> ORDER_BY_SIGNATURE = new Comparator<Method>() {
        public int compare(Method a, Method b) {
            if (a == b) {
                return 0;
            }
            int comparison = a.getName().compareTo(b.getName());
            if (comparison == 0) {
                comparison = a.compareMethodParametersInternal(b);
                if (comparison == 0) {
                    Class<?> aReturnType = a.getReturnType();
                    Class<?> bReturnType = b.getReturnType();
                    if (aReturnType == bReturnType) {
                        comparison = 0;
                    } else {
                        comparison = aReturnType.getName().compareTo(bReturnType.getName());
                    }
                }
            }
            return comparison;
        }
    };

    public native Object getDefaultValue();

    public native Class<?>[] getExceptionTypes();

    @CallerSensitive
    public native Object invoke(Object obj, Object... objArr) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    private Method() {
    }

    /* access modifiers changed from: package-private */
    public boolean hasGenericInformation() {
        return super.hasGenericInformationInternal();
    }

    public Class<?> getDeclaringClass() {
        return super.getDeclaringClassInternal();
    }

    public String getName() {
        return getMethodNameInternal();
    }

    public int getModifiers() {
        return super.getModifiersInternal();
    }

    public TypeVariable<Method>[] getTypeParameters() {
        return (TypeVariable[]) getMethodOrConstructorGenericInfoInternal().formalTypeParameters.clone();
    }

    public Class<?> getReturnType() {
        return getMethodReturnTypeInternal();
    }

    public Type getGenericReturnType() {
        return Types.getType(getMethodOrConstructorGenericInfoInternal().genericReturnType);
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
        if (obj != null && (obj instanceof Method)) {
            Method other = (Method) obj;
            if (getDeclaringClass() == other.getDeclaringClass() && getName() == other.getName() && getReturnType().equals(other.getReturnType())) {
                return equalParamTypes(getParameterTypes(), other.getParameterTypes());
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    public String toString() {
        return sharedToString(Modifier.methodModifiers(), isDefault(), getParameterTypes(), getExceptionTypes());
    }

    /* access modifiers changed from: package-private */
    public void specificToStringHeader(StringBuilder sb) {
        sb.append(getReturnType().getTypeName());
        sb.append(' ');
        sb.append(getDeclaringClass().getTypeName());
        sb.append('.');
        sb.append(getName());
    }

    public String toGenericString() {
        return sharedToGenericString(Modifier.methodModifiers(), isDefault());
    }

    /* access modifiers changed from: package-private */
    public void specificToGenericStringHeader(StringBuilder sb) {
        sb.append(getGenericReturnType().getTypeName());
        sb.append(' ');
        sb.append(getDeclaringClass().getTypeName());
        sb.append('.');
        sb.append(getName());
    }

    public boolean isBridge() {
        return super.isBridgeMethodInternal();
    }

    public boolean isVarArgs() {
        return super.isVarArgs();
    }

    public boolean isSynthetic() {
        return super.isSynthetic();
    }

    public boolean isDefault() {
        return super.isDefaultMethodInternal();
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

    /* access modifiers changed from: package-private */
    public boolean equalNameAndParameters(Method m) {
        return equalNameAndParametersInternal(m);
    }
}
