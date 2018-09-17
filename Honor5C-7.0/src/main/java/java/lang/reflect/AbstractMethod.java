package java.lang.reflect;

import com.android.dex.Dex;
import com.android.dex.DexFormat;
import dalvik.system.VMDebug;
import java.lang.annotation.Annotation;
import libcore.icu.DateUtilsBridge;
import libcore.reflect.GenericSignatureParser;
import libcore.reflect.ListOfTypes;
import libcore.reflect.Types;
import libcore.util.EmptyArray;
import org.w3c.dom.traversal.NodeFilter;

public abstract class AbstractMethod extends AccessibleObject {
    protected int accessFlags;
    protected long artMethod;
    protected Class<?> declaringClass;
    protected Class<?> declaringClassOfOverriddenMethod;
    protected int dexMethodIndex;

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

    private native String[] getSignatureAnnotation();

    private native boolean isAnnotationPresentNative(Class<? extends Annotation> cls);

    public native Annotation[] getDeclaredAnnotations();

    public abstract String getName();

    public abstract Annotation[][] getParameterAnnotations();

    abstract String getSignature();

    protected AbstractMethod() {
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return super.getAnnotation(annotationClass);
    }

    private static int fixMethodFlags(int flags) {
        if ((flags & NodeFilter.SHOW_DOCUMENT_FRAGMENT) != 0) {
            flags &= -257;
        }
        flags &= -33;
        if ((DateUtilsBridge.FORMAT_NUMERIC_DATE & flags) != 0) {
            flags |= 32;
        }
        return DexFormat.MAX_TYPE_IDX & flags;
    }

    int getModifiers() {
        return fixMethodFlags(this.accessFlags);
    }

    boolean isVarArgs() {
        return (this.accessFlags & NodeFilter.SHOW_COMMENT) != 0;
    }

    boolean isBridge() {
        return (this.accessFlags & 64) != 0;
    }

    boolean isSynthetic() {
        return (this.accessFlags & VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS) != 0;
    }

    boolean isDefault() {
        return (this.accessFlags & VMDebug.KIND_THREAD_CLASS_INIT_TIME) != 0;
    }

    public final int getAccessFlags() {
        return this.accessFlags;
    }

    Class<?> getDeclaringClass() {
        return this.declaringClass;
    }

    public final int getDexMethodIndex() {
        return this.dexMethodIndex;
    }

    Class<?>[] getParameterTypes() {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        short[] types = dex.parameterTypeIndicesFromMethodIndex(this.dexMethodIndex);
        if (types.length == 0) {
            return EmptyArray.CLASS;
        }
        Class<?>[] parametersArray = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            parametersArray[i] = this.declaringClassOfOverriddenMethod.getDexCacheType(dex, types[i]);
        }
        return parametersArray;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof AbstractMethod)) {
            return false;
        }
        AbstractMethod otherMethod = (AbstractMethod) other;
        if (this.declaringClass == otherMethod.declaringClass && this.dexMethodIndex == otherMethod.dexMethodIndex) {
            z = true;
        }
        return z;
    }

    String toGenericString() {
        return toGenericStringHelper();
    }

    Type[] getGenericParameterTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericParameterTypes, false);
    }

    Type[] getGenericExceptionTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericExceptionTypes, false);
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        if (annotationType != null) {
            return isAnnotationPresentNative(annotationType);
        }
        throw new NullPointerException("annotationType == null");
    }

    public Annotation[] getAnnotations() {
        return super.getAnnotations();
    }

    final GenericInfo getMethodOrConstructorGenericInfo() {
        Member member;
        Class<?>[] exceptionTypes;
        String signatureAttribute = getSignatureAttribute();
        boolean method = this instanceof Method;
        if (method) {
            Method m = (Method) this;
            member = m;
            exceptionTypes = m.getExceptionTypes();
        } else {
            Constructor<?> c = (Constructor) this;
            Object member2 = c;
            exceptionTypes = c.getExceptionTypes();
        }
        GenericSignatureParser parser = new GenericSignatureParser(member.getDeclaringClass().getClassLoader());
        if (method) {
            parser.parseForMethod((GenericDeclaration) this, signatureAttribute, exceptionTypes);
        } else {
            parser.parseForConstructor((GenericDeclaration) this, signatureAttribute, exceptionTypes);
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

    protected boolean equalMethodParameters(Class<?>[] params) {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        short[] types = dex.parameterTypeIndicesFromMethodIndex(this.dexMethodIndex);
        if (types.length != params.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (this.declaringClassOfOverriddenMethod.getDexCacheType(dex, types[i]) != params[i]) {
                return false;
            }
        }
        return true;
    }

    protected int compareParameters(Class<?>[] params) {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        short[] types = dex.parameterTypeIndicesFromMethodIndex(this.dexMethodIndex);
        int length = Math.min(types.length, params.length);
        for (int i = 0; i < length; i++) {
            Class<?> aType = this.declaringClassOfOverriddenMethod.getDexCacheType(dex, types[i]);
            Class<?> bType = params[i];
            if (aType != bType) {
                int comparison = aType.getName().compareTo(bType.getName());
                if (comparison != 0) {
                    return comparison;
                }
            }
        }
        return types.length - params.length;
    }

    final String toGenericStringHelper() {
        StringBuilder sb = new StringBuilder(80);
        GenericInfo info = getMethodOrConstructorGenericInfo();
        int modifiers = ((Member) this).getModifiers();
        if (modifiers != 0) {
            sb.append(Modifier.toString(modifiers & -129)).append(' ');
        }
        if (info.formalTypeParameters != null && info.formalTypeParameters.length > 0) {
            sb.append('<');
            for (int i = 0; i < info.formalTypeParameters.length; i++) {
                Types.appendGenericType(sb, info.formalTypeParameters[i]);
                if (i < info.formalTypeParameters.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("> ");
        }
        Class<?> declaringClass = ((Member) this).getDeclaringClass();
        if (this instanceof Constructor) {
            Types.appendTypeName(sb, declaringClass);
        } else {
            Types.appendGenericType(sb, Types.getType(info.genericReturnType));
            sb.append(' ');
            Types.appendTypeName(sb, declaringClass);
            sb.append(".").append(((Method) this).getName());
        }
        sb.append('(');
        Types.appendArrayGenericType(sb, info.genericParameterTypes.getResolvedTypes());
        sb.append(')');
        Type[] genericExceptionTypeArray = Types.getTypeArray(info.genericExceptionTypes, false);
        if (genericExceptionTypeArray.length > 0) {
            sb.append(" throws ");
            Types.appendArrayGenericType(sb, genericExceptionTypeArray);
        }
        return sb.toString();
    }
}
