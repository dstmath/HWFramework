package java.lang.invoke;

public class VolatileCallSite extends CallSite {
    public VolatileCallSite(MethodType type) {
        super(type);
    }

    public VolatileCallSite(MethodHandle target) {
        super(target);
    }

    public final MethodHandle getTarget() {
        return getTargetVolatile();
    }

    public void setTarget(MethodHandle newTarget) {
        checkTargetChange(getTargetVolatile(), newTarget);
        setTargetVolatile(newTarget);
    }

    public final MethodHandle dynamicInvoker() {
        return makeDynamicInvoker();
    }
}
