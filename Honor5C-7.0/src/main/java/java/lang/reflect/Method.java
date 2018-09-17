package java.lang.reflect;

import com.android.dex.Dex;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.regex.Pattern;
import libcore.reflect.Types;

public final class Method extends AbstractMethod implements GenericDeclaration, Member {
    public static final Comparator<Method> ORDER_BY_SIGNATURE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.reflect.Method.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.reflect.Method.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.reflect.Method.<clinit>():void");
    }

    private native <A extends Annotation> A getAnnotationNative(Class<A> cls);

    private native Annotation[][] getParameterAnnotationsNative();

    public native Object getDefaultValue();

    public native Class<?>[] getExceptionTypes();

    public native Object invoke(Object obj, Object... objArr) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    private Method() {
    }

    public Class<?> getDeclaringClass() {
        return super.getDeclaringClass();
    }

    public String getName() {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        return this.declaringClassOfOverriddenMethod.getDexCacheString(dex, dex.nameIndexFromMethodIndex(this.dexMethodIndex));
    }

    public int getModifiers() {
        return super.getModifiers();
    }

    public TypeVariable<Method>[] getTypeParameters() {
        return (TypeVariable[]) getMethodOrConstructorGenericInfo().formalTypeParameters.clone();
    }

    public Class<?> getReturnType() {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        return this.declaringClassOfOverriddenMethod.getDexCacheType(dex, dex.returnTypeIndexFromMethodIndex(this.dexMethodIndex));
    }

    public Type getGenericReturnType() {
        return Types.getType(getMethodOrConstructorGenericInfo().genericReturnType);
    }

    public Class<?>[] getParameterTypes() {
        return super.getParameterTypes();
    }

    public Type[] getGenericParameterTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericParameterTypes, false);
    }

    public Type[] getGenericExceptionTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericExceptionTypes, false);
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Method)) {
            Method other = (Method) obj;
            if (getDeclaringClass() != other.getDeclaringClass() || getName() != other.getName() || !getReturnType().equals(other.getReturnType())) {
                return false;
            }
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
        return false;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
            sb.append(Field.getTypeName(getReturnType())).append(' ');
            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
            Class<?>[] params = getParameterTypes();
            for (int j = 0; j < params.length; j++) {
                sb.append(Field.getTypeName(params[j]));
                if (j < params.length - 1) {
                    sb.append(',');
                }
            }
            sb.append(')');
            Class<?>[] exceptions = getExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
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

    public String toGenericString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
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
            Type genRetType = getGenericReturnType();
            sb.append(genRetType instanceof Class ? Field.getTypeName((Class) genRetType) : genRetType.toString()).append(' ');
            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
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

    public boolean isBridge() {
        return (getModifiers() & 64) != 0;
    }

    public boolean isVarArgs() {
        return (getModifiers() & Pattern.CANON_EQ) != 0;
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        if (annotationType != null) {
            return getAnnotationNative(annotationType);
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

    String getSignature() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (Class<?> parameterType : getParameterTypes()) {
            result.append(Types.getSignature(parameterType));
        }
        result.append(')');
        result.append(Types.getSignature(getReturnType()));
        return result.toString();
    }

    boolean equalNameAndParameters(Method m) {
        return getName().equals(m.getName()) ? equalMethodParameters(m.getParameterTypes()) : false;
    }

    public boolean isDefault() {
        return super.isDefault();
    }
}
