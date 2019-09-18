package android.test;

import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.test.suitebuilder.TestMethod;
import android.test.suitebuilder.TestPredicates;
import android.test.suitebuilder.TestSuiteBuilder;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.android.internal.util.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import junit.textui.ResultPrinter;

@Deprecated
public class InstrumentationTestRunner extends Instrumentation implements TestSuiteProvider {
    static final String ARGUMENT_ANNOTATION = "annotation";
    static final String ARGUMENT_DELAY_MSEC = "delay_msec";
    private static final String ARGUMENT_LOG_ONLY = "log";
    static final String ARGUMENT_NOT_ANNOTATION = "notAnnotation";
    static final String ARGUMENT_TEST_CLASS = "class";
    private static final String ARGUMENT_TEST_PACKAGE = "package";
    private static final String ARGUMENT_TEST_SIZE_PREDICATE = "size";
    private static final String DEFAULT_COVERAGE_FILE_NAME = "coverage.ec";
    private static final String LARGE_SUITE = "large";
    private static final String LOG_TAG = "InstrumentationTestRunner";
    private static final String MEDIUM_SUITE = "medium";
    private static final float MEDIUM_SUITE_MAX_RUNTIME = 1000.0f;
    private static final String REPORT_KEY_COVERAGE_PATH = "coverageFilePath";
    public static final String REPORT_KEY_NAME_CLASS = "class";
    public static final String REPORT_KEY_NAME_TEST = "test";
    public static final String REPORT_KEY_NUM_CURRENT = "current";
    private static final String REPORT_KEY_NUM_ITERATIONS = "numiterations";
    public static final String REPORT_KEY_NUM_TOTAL = "numtests";
    private static final String REPORT_KEY_RUN_TIME = "runtime";
    public static final String REPORT_KEY_STACK = "stack";
    private static final String REPORT_KEY_SUITE_ASSIGNMENT = "suiteassignment";
    public static final String REPORT_VALUE_ID = "InstrumentationTestRunner";
    public static final int REPORT_VALUE_RESULT_ERROR = -1;
    public static final int REPORT_VALUE_RESULT_FAILURE = -2;
    public static final int REPORT_VALUE_RESULT_OK = 0;
    public static final int REPORT_VALUE_RESULT_START = 1;
    private static final Predicate<TestMethod> SELECT_LARGE = TestPredicates.hasAnnotation(LargeTest.class);
    private static final Predicate<TestMethod> SELECT_MEDIUM = TestPredicates.hasAnnotation(MediumTest.class);
    private static final Predicate<TestMethod> SELECT_SMALL = TestPredicates.hasAnnotation(SmallTest.class);
    private static final String SMALL_SUITE = "small";
    private static final float SMALL_SUITE_MAX_RUNTIME = 100.0f;
    private Bundle mArguments;
    private boolean mCoverage;
    private String mCoverageFilePath;
    private boolean mDebug;
    /* access modifiers changed from: private */
    public int mDelayMsec;
    private boolean mJustCount;
    private String mPackageOfTests;
    private final Bundle mResults = new Bundle();
    private boolean mSuiteAssignmentMode;
    private int mTestCount;
    private AndroidTestRunner mTestRunner;

    private class StringResultPrinter extends ResultPrinter {
        public StringResultPrinter(PrintStream writer) {
            super(writer);
        }

        public synchronized void printResult(TestResult result, long runTime) {
            printHeader(runTime);
            printFooter(result);
        }
    }

    private class SuiteAssignmentPrinter implements TestListener {
        private long mEndTime;
        private long mStartTime;
        private Bundle mTestResult;
        private boolean mTimingValid;

        public SuiteAssignmentPrinter() {
        }

        public void startTest(Test test) {
            this.mTimingValid = true;
            this.mStartTime = System.currentTimeMillis();
        }

        public void addError(Test test, Throwable t) {
            this.mTimingValid = false;
        }

        public void addFailure(Test test, AssertionFailedError t) {
            this.mTimingValid = false;
        }

        public void endTest(Test test) {
            String assignmentSuite;
            float runTime;
            this.mEndTime = System.currentTimeMillis();
            this.mTestResult = new Bundle();
            if (!this.mTimingValid || this.mStartTime < 0) {
                assignmentSuite = "NA";
                runTime = -1.0f;
            } else {
                runTime = (float) (this.mEndTime - this.mStartTime);
                if (runTime < InstrumentationTestRunner.SMALL_SUITE_MAX_RUNTIME && !InstrumentationTestCase.class.isAssignableFrom(test.getClass())) {
                    assignmentSuite = InstrumentationTestRunner.SMALL_SUITE;
                } else if (runTime < InstrumentationTestRunner.MEDIUM_SUITE_MAX_RUNTIME) {
                    assignmentSuite = InstrumentationTestRunner.MEDIUM_SUITE;
                } else {
                    assignmentSuite = InstrumentationTestRunner.LARGE_SUITE;
                }
            }
            this.mStartTime = -1;
            Bundle bundle = this.mTestResult;
            bundle.putString("stream", test.getClass().getName() + "#" + ((TestCase) test).getName() + "\nin " + assignmentSuite + " suite\nrunTime: " + String.valueOf(runTime) + "\n");
            this.mTestResult.putFloat(InstrumentationTestRunner.REPORT_KEY_RUN_TIME, runTime);
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_SUITE_ASSIGNMENT, assignmentSuite);
            InstrumentationTestRunner.this.sendStatus(0, this.mTestResult);
        }
    }

    private class WatcherResultPrinter implements TestListener {
        private final Bundle mResultTemplate = new Bundle();
        String mTestClass = null;
        int mTestNum = 0;
        Bundle mTestResult;
        int mTestResultCode = 0;

        public WatcherResultPrinter(int numTests) {
            this.mResultTemplate.putString("id", "InstrumentationTestRunner");
            this.mResultTemplate.putInt(InstrumentationTestRunner.REPORT_KEY_NUM_TOTAL, numTests);
        }

        public void startTest(Test test) {
            String testClass = test.getClass().getName();
            String testName = ((TestCase) test).getName();
            this.mTestResult = new Bundle(this.mResultTemplate);
            this.mTestResult.putString("class", testClass);
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_NAME_TEST, testName);
            Bundle bundle = this.mTestResult;
            int i = this.mTestNum + 1;
            this.mTestNum = i;
            bundle.putInt(InstrumentationTestRunner.REPORT_KEY_NUM_CURRENT, i);
            if (testClass == null || testClass.equals(this.mTestClass)) {
                this.mTestResult.putString("stream", "");
            } else {
                this.mTestResult.putString("stream", String.format("\n%s:", new Object[]{testClass}));
                this.mTestClass = testClass;
            }
            try {
                Method testMethod = test.getClass().getMethod(testName, new Class[0]);
                if (testMethod.isAnnotationPresent(RepetitiveTest.class)) {
                    this.mTestResult.putInt(InstrumentationTestRunner.REPORT_KEY_NUM_ITERATIONS, testMethod.getAnnotation(RepetitiveTest.class).numIterations());
                }
            } catch (NoSuchMethodException e) {
            }
            try {
                if (this.mTestNum == 1) {
                    Thread.sleep((long) InstrumentationTestRunner.this.mDelayMsec);
                }
                InstrumentationTestRunner.this.sendStatus(1, this.mTestResult);
                this.mTestResultCode = 0;
            } catch (InterruptedException e2) {
                throw new IllegalStateException(e2);
            }
        }

        public void addError(Test test, Throwable t) {
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace(t));
            this.mTestResultCode = -1;
            this.mTestResult.putString("stream", String.format("\nError in %s:\n%s", new Object[]{((TestCase) test).getName(), BaseTestRunner.getFilteredTrace(t)}));
        }

        public void addFailure(Test test, AssertionFailedError t) {
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace((Throwable) t));
            this.mTestResultCode = -2;
            this.mTestResult.putString("stream", String.format("\nFailure in %s:\n%s", new Object[]{((TestCase) test).getName(), BaseTestRunner.getFilteredTrace((Throwable) t)}));
        }

        public void endTest(Test test) {
            if (this.mTestResultCode == 0) {
                this.mTestResult.putString("stream", ".");
            }
            InstrumentationTestRunner.this.sendStatus(this.mTestResultCode, this.mTestResult);
            try {
                Thread.sleep((long) InstrumentationTestRunner.this.mDelayMsec);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        this.mArguments = arguments;
        ClassPathPackageInfoSource.setApkPaths(new String[]{getTargetContext().getPackageCodePath(), getContext().getPackageCodePath()});
        Predicate<TestMethod> testSizePredicate = null;
        Predicate<TestMethod> testAnnotationPredicate = null;
        Predicate<TestMethod> testNotAnnotationPredicate = null;
        String testClassesArg = null;
        boolean logOnly = false;
        if (arguments != null) {
            testClassesArg = arguments.getString("class");
            this.mDebug = getBooleanArgument(arguments, "debug");
            this.mJustCount = getBooleanArgument(arguments, "count");
            this.mSuiteAssignmentMode = getBooleanArgument(arguments, "suiteAssignment");
            this.mPackageOfTests = arguments.getString(ARGUMENT_TEST_PACKAGE);
            testSizePredicate = getSizePredicateFromArg(arguments.getString(ARGUMENT_TEST_SIZE_PREDICATE));
            testAnnotationPredicate = getAnnotationPredicate(arguments.getString(ARGUMENT_ANNOTATION));
            testNotAnnotationPredicate = getNotAnnotationPredicate(arguments.getString(ARGUMENT_NOT_ANNOTATION));
            logOnly = getBooleanArgument(arguments, ARGUMENT_LOG_ONLY);
            this.mCoverage = getBooleanArgument(arguments, "coverage");
            this.mCoverageFilePath = arguments.getString("coverageFile");
            try {
                Object delay = arguments.get(ARGUMENT_DELAY_MSEC);
                if (delay != null) {
                    this.mDelayMsec = Integer.parseInt(delay.toString());
                }
            } catch (NumberFormatException e) {
                Log.e("InstrumentationTestRunner", "Invalid delay_msec parameter", e);
            }
        }
        TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder(getClass().getName(), getTargetContext().getClassLoader());
        if (testSizePredicate != null) {
            testSuiteBuilder.addRequirements((Predicate<TestMethod>[]) new Predicate[]{testSizePredicate});
        }
        if (testAnnotationPredicate != null) {
            testSuiteBuilder.addRequirements((Predicate<TestMethod>[]) new Predicate[]{testAnnotationPredicate});
        }
        if (testNotAnnotationPredicate != null) {
            testSuiteBuilder.addRequirements((Predicate<TestMethod>[]) new Predicate[]{testNotAnnotationPredicate});
        }
        if (testClassesArg != null) {
            parseTestClasses(testClassesArg, testSuiteBuilder);
        } else if (this.mPackageOfTests != null) {
            testSuiteBuilder.includePackages(this.mPackageOfTests);
        } else {
            TestSuite testSuite = getTestSuite();
            if (testSuite != null) {
                testSuiteBuilder.addTestSuite(testSuite);
            } else {
                testSuiteBuilder.includePackages("");
            }
        }
        testSuiteBuilder.addRequirements(getBuilderRequirements());
        this.mTestRunner = getAndroidTestRunner();
        this.mTestRunner.setContext(getTargetContext());
        this.mTestRunner.setInstrumentation(this);
        this.mTestRunner.setSkipExecution(logOnly);
        this.mTestRunner.setTest(testSuiteBuilder.build());
        this.mTestCount = this.mTestRunner.getTestCases().size();
        if (this.mSuiteAssignmentMode) {
            this.mTestRunner.addTestListener(new SuiteAssignmentPrinter());
        } else {
            WatcherResultPrinter resultPrinter = new WatcherResultPrinter(this.mTestCount);
            this.mTestRunner.addTestListener(new TestPrinter("TestRunner", false));
            this.mTestRunner.addTestListener(resultPrinter);
        }
        start();
    }

    public Bundle getArguments() {
        return this.mArguments;
    }

    /* access modifiers changed from: protected */
    public void addTestListener(TestListener listener) {
        if (this.mTestRunner != null && listener != null) {
            this.mTestRunner.addTestListener(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public List<Predicate<TestMethod>> getBuilderRequirements() {
        return new ArrayList();
    }

    private void parseTestClasses(String testClassArg, TestSuiteBuilder testSuiteBuilder) {
        for (String testClass : testClassArg.split(",")) {
            parseTestClass(testClass, testSuiteBuilder);
        }
    }

    private void parseTestClass(String testClassName, TestSuiteBuilder testSuiteBuilder) {
        int methodSeparatorIndex = testClassName.indexOf(35);
        String testMethodName = null;
        if (methodSeparatorIndex > 0) {
            testMethodName = testClassName.substring(methodSeparatorIndex + 1);
            testClassName = testClassName.substring(0, methodSeparatorIndex);
        }
        testSuiteBuilder.addTestClassByName(testClassName, testMethodName, getTargetContext());
    }

    /* access modifiers changed from: protected */
    public AndroidTestRunner getAndroidTestRunner() {
        return new AndroidTestRunner();
    }

    private boolean getBooleanArgument(Bundle arguments, String tag) {
        String tagString = arguments.getString(tag);
        return tagString != null && Boolean.parseBoolean(tagString);
    }

    private Predicate<TestMethod> getSizePredicateFromArg(String sizeArg) {
        if (SMALL_SUITE.equals(sizeArg)) {
            return SELECT_SMALL;
        }
        if (MEDIUM_SUITE.equals(sizeArg)) {
            return SELECT_MEDIUM;
        }
        if (LARGE_SUITE.equals(sizeArg)) {
            return SELECT_LARGE;
        }
        return null;
    }

    private Predicate<TestMethod> getAnnotationPredicate(String annotationClassName) {
        Class<? extends Annotation> annotationClass = getAnnotationClass(annotationClassName);
        if (annotationClass != null) {
            return TestPredicates.hasAnnotation(annotationClass);
        }
        return null;
    }

    private Predicate<TestMethod> getNotAnnotationPredicate(String annotationClassName) {
        Class<? extends Annotation> annotationClass = getAnnotationClass(annotationClassName);
        if (annotationClass != null) {
            return TestPredicates.not(TestPredicates.hasAnnotation(annotationClass));
        }
        return null;
    }

    private Class<? extends Annotation> getAnnotationClass(String annotationClassName) {
        if (annotationClassName == null) {
            return null;
        }
        try {
            Class<?> annotationClass = Class.forName(annotationClassName);
            if (annotationClass.isAnnotation()) {
                return annotationClass;
            }
            Log.e("InstrumentationTestRunner", String.format("Provided annotation value %s is not an Annotation", new Object[]{annotationClassName}));
            return null;
        } catch (ClassNotFoundException e) {
            Log.e("InstrumentationTestRunner", String.format("Could not find class for specified annotation %s", new Object[]{annotationClassName}));
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareLooper() {
        Looper.prepare();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0075, code lost:
        if (r12.mCoverage != false) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00ae, code lost:
        if (r12.mCoverage == false) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00b0, code lost:
        generateCoverageReport();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00b3, code lost:
        r2.close();
        android.util.Log.i("InstrumentationTestRunner", "Add DontKillDeptProc in mResults!");
        r12.mResults.putString("DontKillDeptProc", "true");
        finish(-1, r12.mResults);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return;
     */
    public void onStart() {
        prepareLooper();
        if (this.mJustCount) {
            this.mResults.putString("id", "InstrumentationTestRunner");
            this.mResults.putInt(REPORT_KEY_NUM_TOTAL, this.mTestCount);
            finish(-1, this.mResults);
            return;
        }
        if (this.mDebug) {
            Debug.waitForDebugger();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream writer = new PrintStream(byteArrayOutputStream);
        try {
            StringResultPrinter resultPrinter = new StringResultPrinter(writer);
            this.mTestRunner.addTestListener(resultPrinter);
            long startTime = System.currentTimeMillis();
            this.mTestRunner.runTest();
            resultPrinter.printResult(this.mTestRunner.getTestResult(), System.currentTimeMillis() - startTime);
            this.mResults.putString("stream", String.format("\nTest results for %s=%s", new Object[]{this.mTestRunner.getTestClassName(), byteArrayOutputStream.toString()}));
        } catch (Throwable th) {
            this.mResults.putString("stream", String.format("\nTest results for %s=%s", new Object[]{this.mTestRunner.getTestClassName(), byteArrayOutputStream.toString()}));
            if (this.mCoverage) {
                generateCoverageReport();
            }
            writer.close();
            Log.i("InstrumentationTestRunner", "Add DontKillDeptProc in mResults!");
            this.mResults.putString("DontKillDeptProc", "true");
            finish(-1, this.mResults);
            throw th;
        }
    }

    public TestSuite getTestSuite() {
        return getAllTests();
    }

    public TestSuite getAllTests() {
        return null;
    }

    public ClassLoader getLoader() {
        return null;
    }

    private void generateCoverageReport() {
        String coverageFilePath = getCoverageFilePath();
        File coverageFile = new File(coverageFilePath);
        try {
            Class.forName("com.vladium.emma.rt.RT").getMethod("dumpCoverageData", new Class[]{coverageFile.getClass(), Boolean.TYPE, Boolean.TYPE}).invoke(null, new Object[]{coverageFile, false, false});
            this.mResults.putString(REPORT_KEY_COVERAGE_PATH, coverageFilePath);
            String currentStream = this.mResults.getString("stream");
            this.mResults.putString("stream", String.format("%s\nGenerated code coverage data to %s", new Object[]{currentStream, coverageFilePath}));
        } catch (ClassNotFoundException e) {
            reportEmmaError("Is emma jar on classpath?", e);
        } catch (SecurityException e2) {
            reportEmmaError(e2);
        } catch (NoSuchMethodException e3) {
            reportEmmaError(e3);
        } catch (IllegalArgumentException e4) {
            reportEmmaError(e4);
        } catch (IllegalAccessException e5) {
            reportEmmaError(e5);
        } catch (InvocationTargetException e6) {
            reportEmmaError(e6);
        }
    }

    private String getCoverageFilePath() {
        if (this.mCoverageFilePath != null) {
            return this.mCoverageFilePath;
        }
        return getTargetContext().getFilesDir().getAbsolutePath() + File.separator + DEFAULT_COVERAGE_FILE_NAME;
    }

    private void reportEmmaError(Exception e) {
        reportEmmaError("", e);
    }

    private void reportEmmaError(String hint, Exception e) {
        Log.e("InstrumentationTestRunner", "Failed to generate emma coverage. " + hint, e);
        Bundle bundle = this.mResults;
        bundle.putString("stream", "\nError: " + msg);
    }
}
