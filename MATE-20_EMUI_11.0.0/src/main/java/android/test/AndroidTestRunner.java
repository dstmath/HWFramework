package android.test;

import android.app.Instrumentation;
import android.content.Context;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;

@Deprecated
public class AndroidTestRunner extends BaseTestRunner {
    private Context mContext;
    private Instrumentation mInstrumentation;
    private boolean mSkipExecution = false;
    private List<TestCase> mTestCases;
    private String mTestClassName;
    private List<TestListener> mTestListeners = new ArrayList();
    private TestResult mTestResult;

    public void setTestClassName(String testClassName, String testMethodName) {
        Class testClass = loadTestClass(testClassName);
        if (shouldRunSingleTestMethod(testMethodName, testClass)) {
            TestCase testCase = buildSingleTestMethod(testClass, testMethodName);
            this.mTestCases = new ArrayList();
            this.mTestCases.add(testCase);
            this.mTestClassName = testClass.getSimpleName();
            return;
        }
        setTest(getTest(testClass), testClass);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.test.AndroidTestRunner */
    /* JADX WARN: Multi-variable type inference failed */
    public void setTest(Test test) {
        setTest(test, test.getClass());
    }

    private void setTest(Test test, Class<? extends Test> testClass) {
        this.mTestCases = TestCaseUtil.getTests(test, true);
        if (TestSuite.class.isAssignableFrom(testClass)) {
            this.mTestClassName = TestCaseUtil.getTestName(test);
        } else {
            this.mTestClassName = testClass.getSimpleName();
        }
    }

    public void clearTestListeners() {
        this.mTestListeners.clear();
    }

    public void addTestListener(TestListener testListener) {
        if (testListener != null) {
            this.mTestListeners.add(testListener);
        }
    }

    /* JADX DEBUG: Type inference failed for r0v4. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends junit.framework.Test> */
    private Class<? extends Test> loadTestClass(String testClassName) {
        try {
            return this.mContext.getClassLoader().loadClass(testClassName);
        } catch (ClassNotFoundException e) {
            runFailed("Could not find test class. Class: " + testClassName);
            return null;
        }
    }

    private TestCase buildSingleTestMethod(Class testClass, String testMethodName) {
        try {
            return newSingleTestMethod(testClass, testMethodName, testClass.getConstructor(new Class[0]), new Object[0]);
        } catch (NoSuchMethodException e) {
            try {
                return newSingleTestMethod(testClass, testMethodName, testClass.getConstructor(String.class), testMethodName);
            } catch (NoSuchMethodException e2) {
                return null;
            }
        }
    }

    private TestCase newSingleTestMethod(Class testClass, String testMethodName, Constructor constructor, Object... args) {
        try {
            TestCase testCase = (TestCase) constructor.newInstance(args);
            testCase.setName(testMethodName);
            return testCase;
        } catch (IllegalAccessException e) {
            runFailed("Could not access test class. Class: " + testClass.getName());
            return null;
        } catch (InstantiationException e2) {
            runFailed("Could not instantiate test class. Class: " + testClass.getName());
            return null;
        } catch (IllegalArgumentException e3) {
            runFailed("Illegal argument passed to constructor. Class: " + testClass.getName());
            return null;
        } catch (InvocationTargetException e4) {
            runFailed("Constructor thew an exception. Class: " + testClass.getName());
            return null;
        }
    }

    private boolean shouldRunSingleTestMethod(String testMethodName, Class<? extends Test> testClass) {
        return testMethodName != null && TestCase.class.isAssignableFrom(testClass);
    }

    private Test getTest(Class clazz) {
        if (TestSuiteProvider.class.isAssignableFrom(clazz)) {
            try {
                return ((TestSuiteProvider) clazz.getConstructor(new Class[0]).newInstance(new Object[0])).getTestSuite();
            } catch (InstantiationException e) {
                runFailed("Could not instantiate test suite provider. Class: " + clazz.getName());
            } catch (IllegalAccessException e2) {
                runFailed("Illegal access of test suite provider. Class: " + clazz.getName());
            } catch (InvocationTargetException e3) {
                runFailed("Invocation exception test suite provider. Class: " + clazz.getName());
            } catch (NoSuchMethodException e4) {
                runFailed("No such method on test suite provider. Class: " + clazz.getName());
            }
        }
        return getTest(clazz.getName());
    }

    /* access modifiers changed from: protected */
    public TestResult createTestResult() {
        if (this.mSkipExecution) {
            return new NoExecTestResult();
        }
        return new TestResult();
    }

    /* access modifiers changed from: package-private */
    public void setSkipExecution(boolean skip) {
        this.mSkipExecution = skip;
    }

    public List<TestCase> getTestCases() {
        return this.mTestCases;
    }

    public String getTestClassName() {
        return this.mTestClassName;
    }

    public TestResult getTestResult() {
        return this.mTestResult;
    }

    public void runTest() {
        runTest(createTestResult());
    }

    public void runTest(TestResult testResult) {
        this.mTestResult = testResult;
        for (TestListener testListener : this.mTestListeners) {
            this.mTestResult.addListener(testListener);
        }
        Instrumentation instrumentation = this.mInstrumentation;
        Context testContext = instrumentation == null ? this.mContext : instrumentation.getContext();
        for (TestCase testCase : this.mTestCases) {
            setContextIfAndroidTestCase(testCase, this.mContext, testContext);
            setInstrumentationIfInstrumentationTestCase(testCase, this.mInstrumentation);
            testCase.run(this.mTestResult);
        }
    }

    private void setContextIfAndroidTestCase(Test test, Context context, Context testContext) {
        if (AndroidTestCase.class.isAssignableFrom(test.getClass())) {
            ((AndroidTestCase) test).setContext(context);
            ((AndroidTestCase) test).setTestContext(testContext);
        }
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    private void setInstrumentationIfInstrumentationTestCase(Test test, Instrumentation instrumentation) {
        if (InstrumentationTestCase.class.isAssignableFrom(test.getClass())) {
            ((InstrumentationTestCase) test).injectInstrumentation(instrumentation);
        }
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        this.mInstrumentation = instrumentation;
    }

    @Deprecated
    public void setInstrumentaiton(Instrumentation instrumentation) {
        setInstrumentation(instrumentation);
    }

    /* access modifiers changed from: protected */
    @Override // junit.runner.BaseTestRunner
    public Class loadSuiteClass(String suiteClassName) throws ClassNotFoundException {
        return this.mContext.getClassLoader().loadClass(suiteClassName);
    }

    @Override // junit.runner.BaseTestRunner
    public void testStarted(String testName) {
    }

    @Override // junit.runner.BaseTestRunner
    public void testEnded(String testName) {
    }

    @Override // junit.runner.BaseTestRunner
    public void testFailed(int status, Test test, Throwable t) {
    }

    /* access modifiers changed from: protected */
    @Override // junit.runner.BaseTestRunner
    public void runFailed(String message) {
        throw new RuntimeException(message);
    }
}
