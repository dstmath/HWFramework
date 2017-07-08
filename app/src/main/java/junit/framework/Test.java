package junit.framework;

public interface Test {
    int countTestCases();

    void run(TestResult testResult);
}
