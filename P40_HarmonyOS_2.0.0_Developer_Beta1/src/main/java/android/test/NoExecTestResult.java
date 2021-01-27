package android.test;

import junit.framework.TestCase;
import junit.framework.TestResult;

@Deprecated
class NoExecTestResult extends TestResult {
    NoExecTestResult() {
    }

    /* access modifiers changed from: protected */
    public void run(TestCase test) {
        startTest(test);
        endTest(test);
    }
}
