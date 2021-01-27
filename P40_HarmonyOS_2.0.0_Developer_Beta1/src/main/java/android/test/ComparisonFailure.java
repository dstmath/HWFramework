package android.test;

@Deprecated
public class ComparisonFailure extends AssertionFailedError {
    private junit.framework.ComparisonFailure mComparison;

    public ComparisonFailure(String message, String expected, String actual) {
        this.mComparison = new junit.framework.ComparisonFailure(message, expected, actual);
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return this.mComparison.getMessage();
    }
}
