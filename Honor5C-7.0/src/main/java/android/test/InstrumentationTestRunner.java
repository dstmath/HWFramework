package android.test;

import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PerformanceCollector;
import android.os.PerformanceCollector.PerformanceResultsWriter;
import android.test.suitebuilder.TestMethod;
import android.test.suitebuilder.TestPredicates;
import android.test.suitebuilder.TestSuiteBuilder;
import android.test.suitebuilder.annotation.HasAnnotation;
import android.util.Log;
import com.android.internal.util.Predicate;
import com.android.internal.util.Predicates;
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
    public static final String ARGUMENT_DELAY_MSEC = "delay_msec";
    private static final String ARGUMENT_LOG_ONLY = "log";
    static final String ARGUMENT_NOT_ANNOTATION = "notAnnotation";
    public static final String ARGUMENT_TEST_CLASS = "class";
    public static final String ARGUMENT_TEST_PACKAGE = "package";
    public static final String ARGUMENT_TEST_SIZE_PREDICATE = "size";
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
    private static final String SMALL_SUITE = "small";
    private static final float SMALL_SUITE_MAX_RUNTIME = 100.0f;
    private Bundle mArguments;
    private boolean mCoverage;
    private String mCoverageFilePath;
    private boolean mDebug;
    private int mDelayMsec;
    private boolean mJustCount;
    private String mPackageOfTests;
    private final Bundle mResults;
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
            this.mTestResult.putString("stream", test.getClass().getName() + "#" + ((TestCase) test).getName() + "\nin " + assignmentSuite + " suite\nrunTime: " + String.valueOf(runTime) + "\n");
            this.mTestResult.putFloat(InstrumentationTestRunner.REPORT_KEY_RUN_TIME, runTime);
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_SUITE_ASSIGNMENT, assignmentSuite);
            InstrumentationTestRunner.this.sendStatus(InstrumentationTestRunner.REPORT_VALUE_RESULT_OK, this.mTestResult);
        }
    }

    private class WatcherResultPrinter implements TestListener, PerformanceResultsWriter {
        boolean mIncludeDetailedStats;
        boolean mIsTimedTest;
        PerformanceCollector mPerfCollector;
        private final Bundle mResultTemplate;
        String mTestClass;
        int mTestNum;
        Bundle mTestResult;
        int mTestResultCode;

        public WatcherResultPrinter(int numTests) {
            this.mTestNum = InstrumentationTestRunner.REPORT_VALUE_RESULT_OK;
            this.mTestResultCode = InstrumentationTestRunner.REPORT_VALUE_RESULT_OK;
            this.mTestClass = null;
            this.mPerfCollector = new PerformanceCollector();
            this.mIsTimedTest = false;
            this.mIncludeDetailedStats = false;
            this.mResultTemplate = new Bundle();
            this.mResultTemplate.putString("id", InstrumentationTestRunner.REPORT_VALUE_ID);
            this.mResultTemplate.putInt(InstrumentationTestRunner.REPORT_KEY_NUM_TOTAL, numTests);
        }

        public void startTest(Test test) {
            String testClass = test.getClass().getName();
            String testName = ((TestCase) test).getName();
            this.mTestResult = new Bundle(this.mResultTemplate);
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_NAME_CLASS, testClass);
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_NAME_TEST, testName);
            Bundle bundle = this.mTestResult;
            String str = InstrumentationTestRunner.REPORT_KEY_NUM_CURRENT;
            int i = this.mTestNum + InstrumentationTestRunner.REPORT_VALUE_RESULT_START;
            this.mTestNum = i;
            bundle.putInt(str, i);
            if (testClass == null || testClass.equals(this.mTestClass)) {
                this.mTestResult.putString("stream", "");
            } else {
                Object[] objArr = new Object[InstrumentationTestRunner.REPORT_VALUE_RESULT_START];
                objArr[InstrumentationTestRunner.REPORT_VALUE_RESULT_OK] = testClass;
                this.mTestResult.putString("stream", String.format("\n%s:", objArr));
                this.mTestClass = testClass;
            }
            Method method = null;
            try {
                method = test.getClass().getMethod(testName, new Class[InstrumentationTestRunner.REPORT_VALUE_RESULT_OK]);
                if (method.isAnnotationPresent(RepetitiveTest.class)) {
                    this.mTestResult.putInt(InstrumentationTestRunner.REPORT_KEY_NUM_ITERATIONS, ((RepetitiveTest) method.getAnnotation(RepetitiveTest.class)).numIterations());
                }
            } catch (NoSuchMethodException e) {
            }
            try {
                if (this.mTestNum == InstrumentationTestRunner.REPORT_VALUE_RESULT_START) {
                    Thread.sleep((long) InstrumentationTestRunner.this.mDelayMsec);
                }
                InstrumentationTestRunner.this.sendStatus(InstrumentationTestRunner.REPORT_VALUE_RESULT_START, this.mTestResult);
                this.mTestResultCode = InstrumentationTestRunner.REPORT_VALUE_RESULT_OK;
                this.mIsTimedTest = false;
                this.mIncludeDetailedStats = false;
                if (method != null) {
                    try {
                        if (method.isAnnotationPresent(TimedTest.class)) {
                            this.mIsTimedTest = true;
                            this.mIncludeDetailedStats = ((TimedTest) method.getAnnotation(TimedTest.class)).includeDetailedStats();
                            if (!this.mIsTimedTest && this.mIncludeDetailedStats) {
                                this.mPerfCollector.beginSnapshot("");
                                return;
                            } else if (this.mIsTimedTest) {
                                this.mPerfCollector.startTiming("");
                            }
                        }
                    } catch (SecurityException e2) {
                    }
                }
                if (test.getClass().isAnnotationPresent(TimedTest.class)) {
                    this.mIsTimedTest = true;
                    this.mIncludeDetailedStats = ((TimedTest) test.getClass().getAnnotation(TimedTest.class)).includeDetailedStats();
                }
                if (!this.mIsTimedTest) {
                }
                if (this.mIsTimedTest) {
                    this.mPerfCollector.startTiming("");
                }
            } catch (InterruptedException e3) {
                throw new IllegalStateException(e3);
            }
        }

        public void addError(Test test, Throwable t) {
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace(t));
            this.mTestResultCode = InstrumentationTestRunner.REPORT_VALUE_RESULT_ERROR;
            this.mTestResult.putString("stream", String.format("\nError in %s:\n%s", new Object[]{((TestCase) test).getName(), BaseTestRunner.getFilteredTrace(t)}));
        }

        public void addFailure(Test test, AssertionFailedError t) {
            this.mTestResult.putString(InstrumentationTestRunner.REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace((Throwable) t));
            this.mTestResultCode = InstrumentationTestRunner.REPORT_VALUE_RESULT_FAILURE;
            this.mTestResult.putString("stream", String.format("\nFailure in %s:\n%s", new Object[]{((TestCase) test).getName(), BaseTestRunner.getFilteredTrace((Throwable) t)}));
        }

        public void endTest(Test test) {
            if (this.mIsTimedTest && this.mIncludeDetailedStats) {
                this.mTestResult.putAll(this.mPerfCollector.endSnapshot());
            } else if (this.mIsTimedTest) {
                writeStopTiming(this.mPerfCollector.stopTiming(""));
            }
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

        public void writeBeginSnapshot(String label) {
        }

        public void writeEndSnapshot(Bundle results) {
            InstrumentationTestRunner.this.mResults.putAll(results);
        }

        public void writeStartTiming(String label) {
        }

        public void writeStopTiming(Bundle results) {
            int i = InstrumentationTestRunner.REPORT_VALUE_RESULT_OK;
            for (Parcelable iteration : results.getParcelableArrayList("iterations")) {
                Bundle iteration2 = (Bundle) iteration;
                String index = "iteration" + i + ".";
                this.mTestResult.putString(index + "label", iteration2.getString("label"));
                this.mTestResult.putLong(index + "cpu_time", iteration2.getLong("cpu_time"));
                this.mTestResult.putLong(index + "execution_time", iteration2.getLong("execution_time"));
                i += InstrumentationTestRunner.REPORT_VALUE_RESULT_START;
            }
        }

        public void writeMeasurement(String label, long value) {
            this.mTestResult.putLong(label, value);
        }

        public void writeMeasurement(String label, float value) {
            this.mTestResult.putFloat(label, value);
        }

        public void writeMeasurement(String label, String value) {
            this.mTestResult.putString(label, value);
        }
    }

    public InstrumentationTestRunner() {
        this.mResults = new Bundle();
    }

    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        this.mArguments = arguments;
        ClassPathPackageInfoSource.setApkPaths(new String[]{getTargetContext().getPackageCodePath(), getContext().getPackageCodePath()});
        Predicate<TestMethod> predicate = null;
        Predicate<TestMethod> predicate2 = null;
        Predicate<TestMethod> predicate3 = null;
        String str = null;
        boolean z = false;
        if (arguments != null) {
            str = arguments.getString(REPORT_KEY_NAME_CLASS);
            this.mDebug = getBooleanArgument(arguments, "debug");
            this.mJustCount = getBooleanArgument(arguments, "count");
            this.mSuiteAssignmentMode = getBooleanArgument(arguments, "suiteAssignment");
            this.mPackageOfTests = arguments.getString(ARGUMENT_TEST_PACKAGE);
            predicate = getSizePredicateFromArg(arguments.getString(ARGUMENT_TEST_SIZE_PREDICATE));
            predicate2 = getAnnotationPredicate(arguments.getString(ARGUMENT_ANNOTATION));
            predicate3 = getNotAnnotationPredicate(arguments.getString(ARGUMENT_NOT_ANNOTATION));
            z = getBooleanArgument(arguments, ARGUMENT_LOG_ONLY);
            this.mCoverage = getBooleanArgument(arguments, "coverage");
            this.mCoverageFilePath = arguments.getString("coverageFile");
            try {
                Object delay = arguments.get(ARGUMENT_DELAY_MSEC);
                if (delay != null) {
                    this.mDelayMsec = Integer.parseInt(delay.toString());
                }
            } catch (NumberFormatException e) {
                Log.e(REPORT_VALUE_ID, "Invalid delay_msec parameter", e);
            }
        }
        TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder(getClass().getName(), getTargetContext().getClassLoader());
        if (predicate != null) {
            Predicate[] predicateArr = new Predicate[REPORT_VALUE_RESULT_START];
            predicateArr[REPORT_VALUE_RESULT_OK] = predicate;
            testSuiteBuilder.addRequirements(predicateArr);
        }
        if (predicate2 != null) {
            predicateArr = new Predicate[REPORT_VALUE_RESULT_START];
            predicateArr[REPORT_VALUE_RESULT_OK] = predicate2;
            testSuiteBuilder.addRequirements(predicateArr);
        }
        if (predicate3 != null) {
            predicateArr = new Predicate[REPORT_VALUE_RESULT_START];
            predicateArr[REPORT_VALUE_RESULT_OK] = predicate3;
            testSuiteBuilder.addRequirements(predicateArr);
        }
        if (str != null) {
            parseTestClasses(str, testSuiteBuilder);
        } else if (this.mPackageOfTests != null) {
            r13 = new String[REPORT_VALUE_RESULT_START];
            r13[REPORT_VALUE_RESULT_OK] = this.mPackageOfTests;
            testSuiteBuilder.includePackages(r13);
        } else {
            TestSuite testSuite = getTestSuite();
            if (testSuite != null) {
                testSuiteBuilder.addTestSuite(testSuite);
            } else {
                r13 = new String[REPORT_VALUE_RESULT_START];
                r13[REPORT_VALUE_RESULT_OK] = "";
                testSuiteBuilder.includePackages(r13);
            }
        }
        testSuiteBuilder.addRequirements(getBuilderRequirements());
        this.mTestRunner = getAndroidTestRunner();
        this.mTestRunner.setContext(getTargetContext());
        this.mTestRunner.setInstrumentation(this);
        this.mTestRunner.setSkipExecution(z);
        this.mTestRunner.setTest(testSuiteBuilder.build());
        this.mTestCount = this.mTestRunner.getTestCases().size();
        if (this.mSuiteAssignmentMode) {
            this.mTestRunner.addTestListener(new SuiteAssignmentPrinter());
        } else {
            WatcherResultPrinter resultPrinter = new WatcherResultPrinter(this.mTestCount);
            this.mTestRunner.addTestListener(new TestPrinter("TestRunner", false));
            this.mTestRunner.addTestListener(resultPrinter);
            this.mTestRunner.setPerformanceResultsWriter(resultPrinter);
        }
        start();
    }

    public Bundle getArguments() {
        return this.mArguments;
    }

    protected void addTestListener(TestListener listener) {
        if (this.mTestRunner != null && listener != null) {
            this.mTestRunner.addTestListener(listener);
        }
    }

    List<Predicate<TestMethod>> getBuilderRequirements() {
        return new ArrayList();
    }

    private void parseTestClasses(String testClassArg, TestSuiteBuilder testSuiteBuilder) {
        String[] testClasses = testClassArg.split(",");
        int length = testClasses.length;
        for (int i = REPORT_VALUE_RESULT_OK; i < length; i += REPORT_VALUE_RESULT_START) {
            parseTestClass(testClasses[i], testSuiteBuilder);
        }
    }

    private void parseTestClass(String testClassName, TestSuiteBuilder testSuiteBuilder) {
        int methodSeparatorIndex = testClassName.indexOf(35);
        String str = null;
        if (methodSeparatorIndex > 0) {
            str = testClassName.substring(methodSeparatorIndex + REPORT_VALUE_RESULT_START);
            testClassName = testClassName.substring(REPORT_VALUE_RESULT_OK, methodSeparatorIndex);
        }
        testSuiteBuilder.addTestClassByName(testClassName, str, getTargetContext());
    }

    protected AndroidTestRunner getAndroidTestRunner() {
        return new AndroidTestRunner();
    }

    private boolean getBooleanArgument(Bundle arguments, String tag) {
        String tagString = arguments.getString(tag);
        return tagString != null ? Boolean.parseBoolean(tagString) : false;
    }

    private Predicate<TestMethod> getSizePredicateFromArg(String sizeArg) {
        if (SMALL_SUITE.equals(sizeArg)) {
            return TestPredicates.SELECT_SMALL;
        }
        if (MEDIUM_SUITE.equals(sizeArg)) {
            return TestPredicates.SELECT_MEDIUM;
        }
        if (LARGE_SUITE.equals(sizeArg)) {
            return TestPredicates.SELECT_LARGE;
        }
        return null;
    }

    private Predicate<TestMethod> getAnnotationPredicate(String annotationClassName) {
        Class<? extends Annotation> annotationClass = getAnnotationClass(annotationClassName);
        if (annotationClass != null) {
            return new HasAnnotation(annotationClass);
        }
        return null;
    }

    private Predicate<TestMethod> getNotAnnotationPredicate(String annotationClassName) {
        Class<? extends Annotation> annotationClass = getAnnotationClass(annotationClassName);
        if (annotationClass != null) {
            return Predicates.not(new HasAnnotation(annotationClass));
        }
        return null;
    }

    private Class<? extends Annotation> getAnnotationClass(String annotationClassName) {
        if (annotationClassName == null) {
            return null;
        }
        String str;
        Object[] objArr;
        try {
            Class<?> annotationClass = Class.forName(annotationClassName);
            if (annotationClass.isAnnotation()) {
                return annotationClass;
            }
            str = REPORT_VALUE_ID;
            objArr = new Object[REPORT_VALUE_RESULT_START];
            objArr[REPORT_VALUE_RESULT_OK] = annotationClassName;
            Log.e(str, String.format("Provided annotation value %s is not an Annotation", objArr));
            return null;
        } catch (ClassNotFoundException e) {
            str = REPORT_VALUE_ID;
            objArr = new Object[REPORT_VALUE_RESULT_START];
            objArr[REPORT_VALUE_RESULT_OK] = annotationClassName;
            Log.e(str, String.format("Could not find class for specified annotation %s", objArr));
        }
    }

    void prepareLooper() {
        Looper.prepare();
    }

    public void onStart() {
        prepareLooper();
        if (this.mJustCount) {
            this.mResults.putString("id", REPORT_VALUE_ID);
            this.mResults.putInt(REPORT_KEY_NUM_TOTAL, this.mTestCount);
            finish(REPORT_VALUE_RESULT_ERROR, this.mResults);
            return;
        }
        if (this.mDebug) {
            Debug.waitForDebugger();
        }
        PrintStream writer = new PrintStream(new ByteArrayOutputStream());
        try {
            StringResultPrinter resultPrinter = new StringResultPrinter(writer);
            this.mTestRunner.addTestListener(resultPrinter);
            long startTime = System.currentTimeMillis();
            this.mTestRunner.runTest();
            resultPrinter.printResult(this.mTestRunner.getTestResult(), System.currentTimeMillis() - startTime);
            this.mResults.putString("stream", String.format("\nTest results for %s=%s", new Object[]{this.mTestRunner.getTestClassName(), byteArrayOutputStream.toString()}));
            if (this.mCoverage) {
                generateCoverageReport();
            }
            writer.close();
            Log.i(REPORT_VALUE_ID, "Add DontKillDeptProc in mResults!");
            this.mResults.putString("DontKillDeptProc", "true");
            finish(REPORT_VALUE_RESULT_ERROR, this.mResults);
        } catch (Throwable th) {
            this.mResults.putString("stream", String.format("\nTest results for %s=%s", new Object[]{this.mTestRunner.getTestClassName(), byteArrayOutputStream.toString()}));
            if (this.mCoverage) {
                generateCoverageReport();
            }
            writer.close();
            Log.i(REPORT_VALUE_ID, "Add DontKillDeptProc in mResults!");
            this.mResults.putString("DontKillDeptProc", "true");
            finish(REPORT_VALUE_RESULT_ERROR, this.mResults);
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
            Class.forName("com.vladium.emma.rt.RT").getMethod("dumpCoverageData", new Class[]{coverageFile.getClass(), Boolean.TYPE, Boolean.TYPE}).invoke(null, new Object[]{coverageFile, Boolean.valueOf(false), Boolean.valueOf(false)});
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
        if (this.mCoverageFilePath == null) {
            return getTargetContext().getFilesDir().getAbsolutePath() + File.separator + DEFAULT_COVERAGE_FILE_NAME;
        }
        return this.mCoverageFilePath;
    }

    private void reportEmmaError(Exception e) {
        reportEmmaError("", e);
    }

    private void reportEmmaError(String hint, Exception e) {
        String msg = "Failed to generate emma coverage. " + hint;
        Log.e(REPORT_VALUE_ID, msg, e);
        this.mResults.putString("stream", "\nError: " + msg);
    }
}
