package android.os.strictmode;

public final class ServiceConnectionLeakedViolation extends Violation {
    public ServiceConnectionLeakedViolation(Throwable originStack) {
        super(null);
        setStackTrace(originStack.getStackTrace());
    }
}
