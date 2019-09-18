package android.os.strictmode;

public final class UntaggedSocketViolation extends Violation {
    public static final String MESSAGE = "Untagged socket detected; use TrafficStats.setThreadSocketTag() to track all network usage";

    public UntaggedSocketViolation() {
        super(MESSAGE);
    }
}
