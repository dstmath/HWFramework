package android.os.strictmode;

public class InstanceCountViolation extends Violation {
    private static final StackTraceElement[] FAKE_STACK = {new StackTraceElement("android.os.StrictMode", "setClassInstanceLimit", "StrictMode.java", 1)};
    private final long mInstances;

    public InstanceCountViolation(Class klass, long instances, int limit) {
        super(klass.toString() + "; instances=" + instances + "; limit=" + limit);
        setStackTrace(FAKE_STACK);
        this.mInstances = instances;
    }

    public long getNumberOfInstances() {
        return this.mInstances;
    }
}
