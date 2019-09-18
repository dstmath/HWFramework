package android.os.strictmode;

public final class ResourceMismatchViolation extends Violation {
    public ResourceMismatchViolation(Object tag) {
        super(tag.toString());
    }
}
