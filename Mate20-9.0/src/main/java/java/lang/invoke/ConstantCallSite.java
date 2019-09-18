package java.lang.invoke;

public class ConstantCallSite extends CallSite {
    private final boolean isFrozen = true;

    public ConstantCallSite(MethodHandle target) {
        super(target);
    }

    protected ConstantCallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
        super(targetType, createTargetHook);
    }

    public final MethodHandle getTarget() {
        if (this.isFrozen) {
            return this.target;
        }
        throw new IllegalStateException();
    }

    public final void setTarget(MethodHandle ignore) {
        throw new UnsupportedOperationException();
    }

    public final MethodHandle dynamicInvoker() {
        return getTarget();
    }
}
