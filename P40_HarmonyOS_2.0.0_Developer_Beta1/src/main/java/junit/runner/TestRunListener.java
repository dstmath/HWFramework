package junit.runner;

public interface TestRunListener {
    public static final int STATUS_ERROR = 1;
    public static final int STATUS_FAILURE = 2;

    void testEnded(String str);

    void testFailed(int i, String str, String str2);

    void testRunEnded(long j);

    void testRunStarted(String str, int i);

    void testRunStopped(long j);

    void testStarted(String str);
}
