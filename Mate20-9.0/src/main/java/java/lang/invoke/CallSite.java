package java.lang.invoke;

import java.lang.invoke.MethodHandles;

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
            this.target = MethodHandles.dropArguments(this.target, 0, (Class<?>[]) type.ptypes());
        }
        initializeGetTarget();
    }

    CallSite(MethodHandle target2) {
        target2.type();
        this.target = target2;
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

    /* access modifiers changed from: package-private */
    public void checkTargetChange(MethodHandle oldTarget, MethodHandle newTarget) {
        MethodType oldType = oldTarget.type();
        if (!newTarget.type().equals((Object) oldType)) {
            throw wrongTargetType(newTarget, oldType);
        }
    }

    private static WrongMethodTypeException wrongTargetType(MethodHandle target2, MethodType type) {
        return new WrongMethodTypeException(String.valueOf((Object) target2) + " should be of type " + type);
    }

    /* access modifiers changed from: package-private */
    public MethodHandle makeDynamicInvoker() {
        return MethodHandles.foldArguments(MethodHandles.exactInvoker(type()), GET_TARGET.bindTo(this));
    }

    static {
        try {
            TARGET_OFFSET = MethodHandleStatics.UNSAFE.objectFieldOffset(CallSite.class.getDeclaredField("target"));
        } catch (Exception ex) {
            throw new Error((Throwable) ex);
        }
    }

    private void initializeGetTarget() {
        synchronized (CallSite.class) {
            if (GET_TARGET == null) {
                try {
                    GET_TARGET = MethodHandles.Lookup.IMPL_LOOKUP.findVirtual(CallSite.class, "getTarget", MethodType.methodType(MethodHandle.class));
                } catch (ReflectiveOperationException e) {
                    throw new InternalError((Throwable) e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setTargetNormal(MethodHandle newTarget) {
        this.target = newTarget;
    }

    /* access modifiers changed from: package-private */
    public MethodHandle getTargetVolatile() {
        return (MethodHandle) MethodHandleStatics.UNSAFE.getObjectVolatile(this, TARGET_OFFSET);
    }

    /* access modifiers changed from: package-private */
    public void setTargetVolatile(MethodHandle newTarget) {
        MethodHandleStatics.UNSAFE.putObjectVolatile(this, TARGET_OFFSET, newTarget);
    }
}
