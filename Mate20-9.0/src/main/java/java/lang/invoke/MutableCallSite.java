package java.lang.invoke;

public class MutableCallSite extends CallSite {
    public MutableCallSite(MethodType type) {
        super(type);
    }

    public MutableCallSite(MethodHandle target) {
        super(target);
    }

    public final MethodHandle getTarget() {
        return this.target;
    }

    public void setTarget(MethodHandle newTarget) {
        checkTargetChange(this.target, newTarget);
        setTargetNormal(newTarget);
    }

    public final MethodHandle dynamicInvoker() {
        return makeDynamicInvoker();
    }
}
