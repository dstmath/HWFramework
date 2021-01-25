package android.test;

@Deprecated
public class AssertionFailedError extends Error {
    public AssertionFailedError() {
    }

    public AssertionFailedError(String errorMessage) {
        super(errorMessage);
    }
}
