package android.test;

import android.app.Instrumentation;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

@Deprecated
public class InstrumentationTestSuite extends TestSuite {
    private final Instrumentation mInstrumentation;

    public InstrumentationTestSuite(Instrumentation instr) {
        this.mInstrumentation = instr;
    }

    public InstrumentationTestSuite(String name, Instrumentation instr) {
        super(name);
        this.mInstrumentation = instr;
    }

    public InstrumentationTestSuite(Class theClass, Instrumentation instr) {
        super(theClass);
        this.mInstrumentation = instr;
    }

    public void addTestSuite(Class testClass) {
        addTest(new InstrumentationTestSuite(testClass, this.mInstrumentation));
    }

    public void runTest(Test test, TestResult result) {
        if (test instanceof InstrumentationTestCase) {
            ((InstrumentationTestCase) test).injectInstrumentation(this.mInstrumentation);
        }
        super.runTest(test, result);
    }
}
