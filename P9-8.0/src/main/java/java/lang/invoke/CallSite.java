package java.lang.invoke;

import java.lang.invoke.MethodHandles.Lookup;

public abstract class CallSite {
    private static MethodHandle GET_TARGET = null;
    private static final long TARGET_OFFSET;
    MethodHandle target;

    public abstract MethodHandle dynamicInvoker();

    public abstract MethodHandle getTarget();

    public abstract void setTarget(MethodHandle methodHandle);

    CallSite(MethodType type) {
        this.target = MethodHandles.throwException(type.returnType(), IllegalStateException.class);
        this.target = MethodHandles.insertArguments(this.target, 0, new IllegalStateException("uninitialized call site"));
        if (type.parameterCount() > 0) {
            this.target = MethodHandles.dropArguments(this.target, 0, type.ptypes());
        }
        initializeGetTarget();
    }

    CallSite(MethodHandle target) {
        target.type();
        this.target = target;
        initializeGetTarget();
    }

    CallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
        this(targetType);
        MethodHandle boundTarget = (MethodHandle) createTargetHook.invokeWithArguments((ConstantCallSite) this);
        checkTargetChange(this.target, boundTarget);
        this.target = boundTarget;
        initializeGetTarget();
    }

    public MethodType type() {
        return this.target.type();
    }

    void checkTargetChange(MethodHandle oldTarget, MethodHandle newTarget) {
        Object oldType = oldTarget.type();
        if (!newTarget.type().equals(oldType)) {
            throw wrongTargetType(newTarget, oldType);
        }
    }

    private static WrongMethodTypeException wrongTargetType(MethodHandle target, MethodType type) {
        return new WrongMethodTypeException(String.valueOf((Object) target) + " should be of type " + type);
    }

    MethodHandle makeDynamicInvoker() {
        return MethodHandles.foldArguments(MethodHandles.exactInvoker(type()), GET_TARGET.bindTo(this));
    }

    static {
        try {
            TARGET_OFFSET = MethodHandleStatics.UNSAFE.objectFieldOffset(CallSite.class.getDeclaredField("target"));
        } catch (Throwable ex) {
            throw new Error(ex);
        }
    }

    private void initializeGetTarget() {
        synchronized (CallSite.class) {
            if (GET_TARGET == null) {
                try {
                    GET_TARGET = Lookup.IMPL_LOOKUP.findVirtual(CallSite.class, "getTarget", MethodType.methodType(MethodHandle.class));
                } catch (Throwable e) {
                    throw new InternalError(e);
                }
            }
        }
    }

    void setTargetNormal(MethodHandle newTarget) {
        this.target = newTarget;
    }

    MethodHandle getTargetVolatile() {
        return (MethodHandle) MethodHandleStatics.UNSAFE.getObjectVolatile(this, TARGET_OFFSET);
    }

    void setTargetVolatile(MethodHandle newTarget) {
        MethodHandleStatics.UNSAFE.putObjectVolatile(this, TARGET_OFFSET, newTarget);
    }
}
