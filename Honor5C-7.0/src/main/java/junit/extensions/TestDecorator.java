package junit.extensions;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestResult;

public class TestDecorator extends Assert implements Test {
    protected Test fTest;

    public TestDecorator(Test test) {
        this.fTest = test;
    }

    public void basicRun(TestResult result) {
        this.fTest.run(result);
    }

    public int countTestCases() {
        return this.fTest.countTestCases();
    }

    public void run(TestResult result) {
        basicRun(result);
    }

    public String toString() {
        return this.fTest.toString();
    }

    public Test getTest() {
        return this.fTest;
    }
}
