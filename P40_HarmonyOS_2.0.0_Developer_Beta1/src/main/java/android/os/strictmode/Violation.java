package android.os.strictmode;

public abstract class Violation extends Throwable {
    Violation(String message) {
        super(message);
    }
}
