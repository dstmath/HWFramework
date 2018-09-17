package java.lang.invoke;

import dalvik.system.EmulatedStackFrame;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.Transformers.BindTo;
import java.util.List;

public abstract class MethodHandle {
    static final /* synthetic */ boolean -assertionsDisabled = (MethodHandle.class.desiredAssertionStatus() ^ 1);
    public static final int IGET = 7;
    public static final int INVOKE_CALLSITE_TRANSFORM = 6;
    public static final int INVOKE_DIRECT = 2;
    public static final int INVOKE_INTERFACE = 4;
    public static final int INVOKE_STATIC = 3;
    public static final int INVOKE_SUPER = 1;
    public static final int INVOKE_TRANSFORM = 5;
    public static final int INVOKE_VIRTUAL = 0;
    public static final int IPUT = 8;
    public static final int SGET = 9;
    public static final int SPUT = 10;
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
        jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.MethodHandle.invokeWithArguments(java.lang.Object[]):java.lang.Object, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-polymorphic'
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public java.lang.Object invokeWithArguments(java.lang.Object... r1) throws java.lang.Throwable {
        /*
        // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-polymorphic' in method: java.lang.invoke.MethodHandle.invokeWithArguments(java.lang.Object[]):java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.invoke.MethodHandle.invokeWithArguments(java.lang.Object[]):java.lang.Object");
    }

    protected MethodHandle(long artFieldOrMethod, int handleKind, MethodType type) {
        this.artFieldOrMethod = artFieldOrMethod;
        this.handleKind = handleKind;
        this.type = type;
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
        return new Spreader(this, postSpreadType.dropParameterTypes(targetParamCount - arrayLength, targetParamCount).appendParameterTypes(arrayType), arrayLength);
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
        boolean fail = -assertionsDisabled;
        for (int i = nargs - arrayLength; i < nargs; i++) {
            Class<?> ptype = mtype.parameterType(i);
            if (ptype != arrayElement) {
                match = -assertionsDisabled;
                if (!MethodType.canConvert(arrayElement, ptype)) {
                    fail = true;
                    break;
                }
            }
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
            } else if (!-assertionsDisabled && arrayLength < 128) {
                throw new AssertionError();
            } else if (arrayElement == Long.TYPE || arrayElement == Double.TYPE) {
                throw MethodHandleStatics.newIllegalArgumentException("array length is not legal for long[] or double[]", Integer.valueOf(arrayLength));
            }
        }
    }

    public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
        asCollectorChecks(arrayType, arrayLength);
        return new Collector(this, arrayType, arrayLength);
    }

    boolean asCollectorChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs != 0) {
            Class<?> lastParam = type().parameterType(nargs - 1);
            if (lastParam == arrayType) {
                return true;
            }
            if (lastParam.isAssignableFrom(arrayType)) {
                return -assertionsDisabled;
            }
        }
        throw MethodHandleStatics.newIllegalArgumentException("array type not assignable to trailing argument", this, arrayType);
    }

    public MethodHandle asVarargsCollector(Class<?> arrayType) {
        arrayType.getClass();
        boolean lastMatch = asCollectorChecks(arrayType, 0);
        if (isVarargsCollector() && lastMatch) {
            return this;
        }
        return new VarargsCollector(this);
    }

    public boolean isVarargsCollector() {
        return -assertionsDisabled;
    }

    public MethodHandle asFixedArity() {
        MethodHandle mh = this;
        if (isVarargsCollector()) {
            mh = ((VarargsCollector) this).asFixedArity();
        }
        if (-assertionsDisabled || !mh.isVarargsCollector()) {
            return mh;
        }
        throw new AssertionError();
    }

    public MethodHandle bindTo(Object x) {
        return new BindTo(this, this.type.leadingReferenceParameter().cast(x));
    }

    public String toString() {
        return "MethodHandle" + this.type;
    }

    public int getHandleKind() {
        return this.handleKind;
    }

    protected void transform(EmulatedStackFrame arguments) throws Throwable {
        throw new AssertionError((Object) "MethodHandle.transform should never be called.");
    }

    protected MethodHandle duplicate() {
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
