package android.os.strictmode;

public final class SqliteObjectLeakedViolation extends Violation {
    public SqliteObjectLeakedViolation(String message, Throwable originStack) {
        super(message);
        initCause(originStack);
    }
}
