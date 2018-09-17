package android.test;

import junit.framework.TestCase;
import junit.framework.TestResult;

@Deprecated
class NoExecTestResult extends TestResult {
    NoExecTestResult() {
    }

    protected void run(TestCase test) {
        startTest(test);
        endTest(test);
    }
}
