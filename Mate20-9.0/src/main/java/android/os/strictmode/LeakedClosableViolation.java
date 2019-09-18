package android.os.strictmode;

public final class LeakedClosableViolation extends Violation {
    public LeakedClosableViolation(String message, Throwable allocationSite) {
        super(message);
        initCause(allocationSite);
    }
}
