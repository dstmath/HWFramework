package java.lang.reflect;

import java.awt.font.NumericShaper;
import java.lang.annotation.Annotation;
import java.util.Objects;
import libcore.reflect.AnnotatedElements;
import libcore.reflect.GenericSignatureParser;
import libcore.reflect.ListOfTypes;
import libcore.reflect.Types;

public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration {
    private int accessFlags;
    private long artMethod;
    private Class<?> declaringClass;
    private Class<?> declaringClassOfOverriddenMethod;
    private int dexMethodIndex;
    private volatile transient boolean hasRealParameterData;
    private volatile transient Parameter[] parameters;

    static final class GenericInfo {
        final TypeVariable<?>[] formalTypeParameters;
        final ListOfTypes genericExceptionTypes;
        final ListOfTypes genericParameterTypes;
        final Type genericReturnType;

        GenericInfo(ListOfTypes exceptions, ListOfTypes parameters, Type ret, TypeVariable<?>[] formal) {
            this.genericExceptionTypes = exceptions;
            this.genericParameterTypes = parameters;
            this.genericReturnType = ret;
            this.formalTypeParameters = formal;
        }
    }

    private native <T extends Annotation> T getAnnotationNative(Class<T> cls);

    private native Annotation[] getDeclaredAnnotationsNative();

    private native Annotation[][] getParameterAnnotationsNative();

    private native Parameter[] getParameters0();

    private native String[] getSignatureAnnotation();

    private native boolean isAnnotationPresentNative(Class<? extends Annotation> cls);

    native int compareMethodParametersInternal(Method method);

    public abstract Class<?> getDeclaringClass();

    public abstract Class<?>[] getExceptionTypes();

    final native String getMethodNameInternal();

    final native Class<?> getMethodReturnTypeInternal();

    public abstract int getModifiers();

    public abstract String getName();

    public abstract Annotation[][] getParameterAnnotations();

    final native int getParameterCountInternal();

    public abstract Class<?>[] getParameterTypes();

    final native Class<?>[] getParameterTypesInternal();

    public abstract TypeVariable<?>[] getTypeParameters();

    abstract boolean hasGenericInformation();

    abstract void specificToGenericStringHeader(StringBuilder stringBuilder);

    abstract void specificToStringHeader(StringBuilder stringBuilder);

    public abstract String toGenericString();

    Executable() {
    }

    boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
        if (params1.length != params2.length) {
            return false;
        }
        for (int i = 0; i < params1.length; i++) {
            if (params1[i] != params2[i]) {
                return false;
            }
        }
        return true;
    }

    void separateWithCommas(Class<?>[] types, StringBuilder sb) {
        for (int j = 0; j < types.length; j++) {
            sb.append(types[j].getTypeName());
            if (j < types.length - 1) {
                sb.append(",");
            }
        }
    }

    void printModifiersIfNonzero(StringBuilder sb, int mask, boolean isDefault) {
        int mod = getModifiers() & mask;
        if (mod == 0 || (isDefault ^ 1) == 0) {
            int access_mod = mod & 7;
            if (access_mod != 0) {
                sb.append(Modifier.toString(access_mod)).append(' ');
            }
            if (isDefault) {
                sb.append("default ");
            }
            mod &= -8;
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
                return;
            }
            return;
        }
        sb.append(Modifier.toString(mod)).append(' ');
    }

    String sharedToString(int modifierMask, boolean isDefault, Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
        try {
            StringBuilder sb = new StringBuilder();
            printModifiersIfNonzero(sb, modifierMask, isDefault);
            specificToStringHeader(sb);
            sb.append('(');
            separateWithCommas(parameterTypes, sb);
            sb.append(')');
            if (exceptionTypes.length > 0) {
                sb.append(" throws ");
                separateWithCommas(exceptionTypes, sb);
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    String sharedToGenericString(int modifierMask, boolean isDefault) {
        try {
            StringBuilder sb = new StringBuilder();
            printModifiersIfNonzero(sb, modifierMask, isDefault);
            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for (TypeVariable<?> typeparm : typeparms) {
                    if (!first) {
                        sb.append(',');
                    }
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }
            specificToGenericStringHeader(sb);
            sb.append('(');
            Type[] params = getGenericParameterTypes();
            int j = 0;
            while (j < params.length) {
                String param = params[j].getTypeName();
                if (isVarArgs() && j == params.length - 1) {
                    param = param.replaceFirst("\\[\\]$", "...");
                }
                sb.append(param);
                if (j < params.length - 1) {
                    sb.append(',');
                }
                j++;
            }
            sb.append(')');
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
                        sb.append(',');
                    }
                }
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    public int getParameterCount() {
        throw new AbstractMethodError();
    }

    public Type[] getGenericParameterTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfoInternal().genericParameterTypes, false);
    }

    Type[] getAllGenericParameterTypes() {
        if (!hasGenericInformation()) {
            return getParameterTypes();
        }
        boolean realParamData = hasRealParameterData();
        Type[] genericParamTypes = getGenericParameterTypes();
        Type[] nonGenericParamTypes = getParameterTypes();
        Type[] out = new Type[nonGenericParamTypes.length];
        Parameter[] params = getParameters();
        int fromidx = 0;
        if (realParamData) {
            for (int i = 0; i < out.length; i++) {
                Parameter param = params[i];
                if (param.isSynthetic() || param.isImplicit()) {
                    out[i] = nonGenericParamTypes[i];
                } else {
                    out[i] = genericParamTypes[fromidx];
                    fromidx++;
                }
            }
            return out;
        }
        if (genericParamTypes.length != nonGenericParamTypes.length) {
            genericParamTypes = nonGenericParamTypes;
        }
        return genericParamTypes;
    }

    public Parameter[] getParameters() {
        return (Parameter[]) privateGetParameters().clone();
    }

    private Parameter[] synthesizeAllParams() {
        int realparams = getParameterCount();
        Parameter[] out = new Parameter[realparams];
        for (int i = 0; i < realparams; i++) {
            out[i] = new Parameter("arg" + i, 0, this, i);
        }
        return out;
    }

    private void verifyParameters(Parameter[] parameters) {
        if (getParameterTypes().length != parameters.length) {
            throw new MalformedParametersException("Wrong number of parameters in MethodParameters attribute");
        }
        int i = 0;
        int length = parameters.length;
        while (i < length) {
            Parameter parameter = parameters[i];
            String name = parameter.getRealName();
            int mods = parameter.getModifiers();
            if (name != null && (name.isEmpty() || name.indexOf(46) != -1 || name.indexOf(59) != -1 || name.indexOf(91) != -1 || name.indexOf(47) != -1)) {
                throw new MalformedParametersException("Invalid parameter name \"" + name + "\"");
            } else if (mods != (36880 & mods)) {
                throw new MalformedParametersException("Invalid parameter modifiers");
            } else {
                i++;
            }
        }
    }

    private Parameter[] privateGetParameters() {
        Parameter[] tmp = this.parameters;
        if (tmp == null) {
            try {
                tmp = getParameters0();
                if (tmp == null) {
                    this.hasRealParameterData = false;
                    tmp = synthesizeAllParams();
                } else {
                    this.hasRealParameterData = true;
                    verifyParameters(tmp);
                }
                this.parameters = tmp;
            } catch (IllegalArgumentException e) {
                MalformedParametersException e2 = new MalformedParametersException("Invalid parameter metadata in class file");
                e2.initCause(e);
                throw e2;
            }
        }
        return tmp;
    }

    boolean hasRealParameterData() {
        if (this.parameters == null) {
            privateGetParameters();
        }
        return this.hasRealParameterData;
    }

    public Type[] getGenericExceptionTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfoInternal().genericExceptionTypes, false);
    }

    public boolean isVarArgs() {
        return (this.accessFlags & 128) != 0;
    }

    public boolean isSynthetic() {
        return (this.accessFlags & 4096) != 0;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return getAnnotationNative(annotationClass);
    }

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
    }

    public Annotation[] getDeclaredAnnotations() {
        return getDeclaredAnnotationsNative();
    }

    private static int fixMethodFlags(int flags) {
        if ((flags & 1024) != 0) {
            flags &= -257;
        }
        flags &= -33;
        if ((NumericShaper.KHMER & flags) != 0) {
            flags |= 32;
        }
        return 65535 & flags;
    }

    final int getModifiersInternal() {
        return fixMethodFlags(this.accessFlags);
    }

    final Class<?> getDeclaringClassInternal() {
        return this.declaringClass;
    }

    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        Objects.requireNonNull(annotationType);
        return isAnnotationPresentNative(annotationType);
    }

    final Annotation[][] getParameterAnnotationsInternal() {
        Annotation[][] parameterAnnotations = getParameterAnnotationsNative();
        if (parameterAnnotations != null) {
            return parameterAnnotations;
        }
        return (Annotation[][]) Array.newInstance(Annotation.class, getParameterTypes().length, 0);
    }

    public final int getAccessFlags() {
        return this.accessFlags;
    }

    public final long getArtMethod() {
        return this.artMethod;
    }

    final boolean hasGenericInformationInternal() {
        return getSignatureAnnotation() != null;
    }

    final GenericInfo getMethodOrConstructorGenericInfoInternal() {
        String signatureAttribute = getSignatureAttribute();
        Class<?>[] exceptionTypes = getExceptionTypes();
        GenericSignatureParser parser = new GenericSignatureParser(getDeclaringClass().getClassLoader());
        if (this instanceof Method) {
            parser.parseForMethod(this, signatureAttribute, exceptionTypes);
        } else {
            parser.parseForConstructor(this, signatureAttribute, exceptionTypes);
        }
        return new GenericInfo(parser.exceptionTypes, parser.parameterTypes, parser.returnType, parser.formalTypeParameters);
    }

    private String getSignatureAttribute() {
        String[] annotation = getSignatureAnnotation();
        if (annotation == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String s : annotation) {
            result.append(s);
        }
        return result.toString();
    }

    final boolean equalNameAndParametersInternal(Method m) {
        return getName().equals(m.getName()) && compareMethodParametersInternal(m) == 0;
    }

    final boolean isDefaultMethodInternal() {
        return (this.accessFlags & Modifier.DEFAULT) != 0;
    }

    final boolean isBridgeMethodInternal() {
        return (this.accessFlags & 64) != 0;
    }
}
