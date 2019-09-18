package java.lang.invoke;

import dalvik.system.EmulatedStackFrame;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.Transformers;
import java.util.List;

public abstract class MethodHandle {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int IGET = 9;
    public static final int INVOKE_CALLSITE_TRANSFORM = 6;
    public static final int INVOKE_DIRECT = 2;
    public static final int INVOKE_INTERFACE = 4;
    public static final int INVOKE_STATIC = 3;
    public static final int INVOKE_SUPER = 1;
    public static final int INVOKE_TRANSFORM = 5;
    public static final int INVOKE_VAR_HANDLE = 7;
    public static final int INVOKE_VAR_HANDLE_EXACT = 8;
    public static final int INVOKE_VIRTUAL = 0;
    public static final int IPUT = 10;
    public static final int SGET = 11;
    public static final int SPUT = 12;
    protected final long artFieldOrMethod;
    private MethodHandle cachedSpreadInvoker;
    protected final int handleKind;
    private MethodType nominalType;
    private final MethodType type;

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PolymorphicSignature {
    }

    @PolymorphicSignature
    public final native Object invoke(Object... objArr) throws Throwable;

    @PolymorphicSignature
    public final native Object invokeExact(Object... objArr) throws Throwable;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.MethodHandle.invokeWithArguments(java.lang.Object[]):java.lang.Object, dex: boot_classes.dex
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
        	at jadx.core.ProcessClass.process(ProcessClass.java:36)
        	at java.util.ArrayList.forEach(ArrayList.java:1257)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:59)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
        	... 5 more
        */
    public java.lang.Object invokeWithArguments(java.lang.Object... r1) {
        /*
        // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.MethodHandle.invokeWithArguments(java.lang.Object[]):java.lang.Object, dex: boot_classes.dex
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.MethodHandle.invokeWithArguments(java.lang.Object[]):java.lang.Object");
    }

    protected MethodHandle(long artFieldOrMethod2, int handleKind2, MethodType type2) {
        this.artFieldOrMethod = artFieldOrMethod2;
        this.handleKind = handleKind2;
        this.type = type2;
    }

    public MethodType type() {
        if (this.nominalType != null) {
            return this.nominalType;
        }
        return this.type;
    }

    public Object invokeWithArguments(List<?> arguments) throws Throwable {
        return invokeWithArguments(arguments.toArray());
    }

    public MethodHandle asType(MethodType newType) {
        if (newType == this.type) {
            return this;
        }
        if (this.type.isConvertibleTo(newType)) {
            MethodHandle mh = duplicate();
            mh.nominalType = newType;
            return mh;
        }
        throw new WrongMethodTypeException("cannot convert " + this + " to " + newType);
    }

    public MethodHandle asSpreader(Class<?> arrayType, int arrayLength) {
        MethodType postSpreadType = asSpreaderChecks(arrayType, arrayLength);
        int targetParamCount = postSpreadType.parameterCount();
        return new Transformers.Spreader(this, postSpreadType.dropParameterTypes(targetParamCount - arrayLength, targetParamCount).appendParameterTypes((Class<?>[]) new Class[]{arrayType}), arrayLength);
    }

    private MethodType asSpreaderChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs < arrayLength || arrayLength < 0) {
            throw MethodHandleStatics.newIllegalArgumentException("bad spread array length");
        }
        Class<?> arrayElement = arrayType.getComponentType();
        MethodType mtype = type();
        boolean match = true;
        boolean fail = $assertionsDisabled;
        int i = nargs - arrayLength;
        while (true) {
            if (i >= nargs) {
                break;
            }
            Class<?> ptype = mtype.parameterType(i);
            if (ptype != arrayElement) {
                match = $assertionsDisabled;
                if (!MethodType.canConvert(arrayElement, ptype)) {
                    fail = true;
                    break;
                }
            }
            i++;
        }
        if (match) {
            return mtype;
        }
        MethodType needType = mtype.asSpreaderType(arrayType, arrayLength);
        if (!fail) {
            return needType;
        }
        asType(needType);
        throw MethodHandleStatics.newInternalError("should not return", null);
    }

    private void spreadArrayChecks(Class<?> arrayType, int arrayLength) {
        Class<?> arrayElement = arrayType.getComponentType();
        if (arrayElement == null) {
            throw MethodHandleStatics.newIllegalArgumentException("not an array type", arrayType);
        } else if ((arrayLength & 127) == arrayLength) {
        } else {
            if ((arrayLength & 255) != arrayLength) {
                throw MethodHandleStatics.newIllegalArgumentException("array length is not legal", Integer.valueOf(arrayLength));
            } else if (arrayElement == Long.TYPE || arrayElement == Double.TYPE) {
                throw MethodHandleStatics.newIllegalArgumentException("array length is not legal for long[] or double[]", Integer.valueOf(arrayLength));
            }
        }
    }

    public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
        asCollectorChecks(arrayType, arrayLength);
        return new Transformers.Collector(this, arrayType, arrayLength);
    }

    /* access modifiers changed from: package-private */
    public boolean asCollectorChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs != 0) {
            Class<?> lastParam = type().parameterType(nargs - 1);
            if (lastParam == arrayType) {
                return true;
            }
            if (lastParam.isAssignableFrom(arrayType)) {
                return $assertionsDisabled;
            }
        }
        throw MethodHandleStatics.newIllegalArgumentException("array type not assignable to trailing argument", this, arrayType);
    }

    public MethodHandle asVarargsCollector(Class<?> arrayType) {
        arrayType.getClass();
        boolean lastMatch = asCollectorChecks(arrayType, 0);
        if (!isVarargsCollector() || !lastMatch) {
            return new Transformers.VarargsCollector(this);
        }
        return this;
    }

    public boolean isVarargsCollector() {
        return $assertionsDisabled;
    }

    public MethodHandle asFixedArity() {
        if (isVarargsCollector()) {
            return ((Transformers.VarargsCollector) this).asFixedArity();
        }
        return this;
    }

    public MethodHandle bindTo(Object x) {
        return new Transformers.BindTo(this, this.type.leadingReferenceParameter().cast(x));
    }

    public String toString() {
        return "MethodHandle" + this.type;
    }

    public int getHandleKind() {
        return this.handleKind;
    }

    /* access modifiers changed from: protected */
    public void transform(EmulatedStackFrame arguments) throws Throwable {
        throw new AssertionError((Object) "MethodHandle.transform should never be called.");
    }

    /* access modifiers changed from: protected */
    public MethodHandle duplicate() {
        try {
            return (MethodHandle) clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError((Object) "Subclass of Transformer is not cloneable");
        }
    }

    private void transformInternal(EmulatedStackFrame arguments) throws Throwable {
        transform(arguments);
    }
}
