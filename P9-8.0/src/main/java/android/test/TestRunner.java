package android.test;

import android.content.Context;
import android.os.Debug;
import android.os.SystemClock;
import android.test.PerformanceTestCase.Intermediates;
import android.util.Log;
import com.google.android.collect.Lists;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

@Deprecated
public class TestRunner implements Intermediates {
    public static final int CLEARSCREEN = 0;
    public static final int PERFORMANCE = 1;
    public static final int PROFILING = 2;
    public static final int REGRESSION = 0;
    private static final String TAG = "TestHarness";
    private static Class mJUnitClass;
    private static Class mRunnableClass;
    private String mClassName;
    private Context mContext;
    private long mEndTime;
    private int mFailed;
    List<IntermediateTime> mIntermediates = null;
    private int mInternalIterations;
    private List<Listener> mListeners = Lists.newArrayList();
    private int mMode = 0;
    private int mPassed;
    private long mStartTime;

    public interface Listener {
        void failed(String str, Throwable th);

        void finished(String str);

        void passed(String str);

        void performance(String str, long j, int i, List<IntermediateTime> list);

        void started(String str);
    }

    public static class IntermediateTime {
        public String name;
        public long timeInNS;

        public IntermediateTime(String name, long timeInNS) {
            this.name = name;
            this.timeInNS = timeInNS;
        }
    }

    public class JunitTestSuite extends TestSuite implements TestListener {
        boolean mError = false;

        public void run(TestResult result) {
            result.addListener(this);
            super.run(result);
            result.removeListener(this);
        }

        public void startTest(Test test) {
            TestRunner.this.started(test.toString());
        }

        public void endTest(Test test) {
            TestRunner.this.finished(test.toString());
            if (!this.mError) {
                TestRunner.this.passed(test.toString());
            }
        }

        public void addError(Test test, Throwable t) {
            this.mError = true;
            TestRunner.this.failed(test.toString(), t);
        }

        public void addFailure(Test test, AssertionFailedError t) {
            this.mError = true;
            TestRunner.this.failed(test.toString(), t);
        }
    }

    static {
        try {
            mRunnableClass = Class.forName("java.lang.Runnable", false, null);
            mJUnitClass = Class.forName("junit.framework.TestCase", false, null);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("shouldn't happen", ex);
        }
    }

    public TestRunner(Context context) {
        this.mContext = context;
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void startProfiling() {
        new File("/tmp/trace").mkdir();
        Debug.startMethodTracing("/tmp/trace/" + this.mClassName + ".dmtrace", 8388608);
    }

    public void finishProfiling() {
        Debug.stopMethodTracing();
    }

    private void started(String className) {
        int count = this.mListeners.size();
        for (int i = 0; i < count; i++) {
            ((Listener) this.mListeners.get(i)).started(className);
        }
    }

    private void finished(String className) {
        int count = this.mListeners.size();
        for (int i = 0; i < count; i++) {
            ((Listener) this.mListeners.get(i)).finished(className);
        }
    }

    private void performance(String className, long itemTimeNS, int iterations, List<IntermediateTime> intermediates) {
        int count = this.mListeners.size();
        for (int i = 0; i < count; i++) {
            ((Listener) this.mListeners.get(i)).performance(className, itemTimeNS, iterations, intermediates);
        }
    }

    public void passed(String className) {
        this.mPassed++;
        int count = this.mListeners.size();
        for (int i = 0; i < count; i++) {
            ((Listener) this.mListeners.get(i)).passed(className);
        }
    }

    public void failed(String className, Throwable exception) {
        this.mFailed++;
        int count = this.mListeners.size();
        for (int i = 0; i < count; i++) {
            ((Listener) this.mListeners.get(i)).failed(className, exception);
        }
    }

    public int passedCount() {
        return this.mPassed;
    }

    public int failedCount() {
        return this.mFailed;
    }

    public void run(String[] classes) {
        for (String cl : classes) {
            run(cl);
        }
    }

    public void setInternalIterations(int count) {
        this.mInternalIterations = count;
    }

    public void startTiming(boolean realTime) {
        if (realTime) {
            this.mStartTime = System.currentTimeMillis();
        } else {
            this.mStartTime = SystemClock.currentThreadTimeMillis();
        }
    }

    public void addIntermediate(String name) {
        addIntermediate(name, (System.currentTimeMillis() - this.mStartTime) * 1000000);
    }

    public void addIntermediate(String name, long timeInNS) {
        this.mIntermediates.add(new IntermediateTime(name, timeInNS));
    }

    public void finishTiming(boolean realTime) {
        if (realTime) {
            this.mEndTime = System.currentTimeMillis();
        } else {
            this.mEndTime = SystemClock.currentThreadTimeMillis();
        }
    }

    public void setPerformanceMode(int mode) {
        this.mMode = mode;
    }

    private void missingTest(String className, Throwable e) {
        started(className);
        finished(className);
        failed(className, e);
    }

    public void run(String className) {
        try {
            this.mClassName = className;
            Class clazz = this.mContext.getClassLoader().loadClass(className);
            Method method = getChildrenMethod(clazz);
            Throwable e;
            if (method != null) {
                run(getChildren(method));
            } else if (mRunnableClass.isAssignableFrom(clazz)) {
                Runnable test = (Runnable) clazz.newInstance();
                TestCase testcase = null;
                if (test instanceof TestCase) {
                    testcase = (TestCase) test;
                }
                e = null;
                boolean didSetup = false;
                started(className);
                if (testcase != null) {
                    try {
                        testcase.setUp(this.mContext);
                        didSetup = true;
                    } catch (Throwable ex) {
                        e = ex;
                    }
                }
                if (this.mMode == 1) {
                    runInPerformanceMode(test, className, false, className);
                } else if (this.mMode == 2) {
                    startProfiling();
                    test.run();
                    finishProfiling();
                } else {
                    test.run();
                }
                if (testcase != null && didSetup) {
                    try {
                        testcase.tearDown();
                    } catch (Throwable ex2) {
                        e = ex2;
                    }
                }
                finished(className);
                if (e == null) {
                    passed(className);
                } else {
                    failed(className, e);
                }
            } else if (mJUnitClass.isAssignableFrom(clazz)) {
                e = null;
                Test junitTestSuite = new JunitTestSuite();
                for (Method m : getAllTestMethods(clazz)) {
                    TestCase test2 = (TestCase) clazz.newInstance();
                    test2.setName(m.getName());
                    if (test2 instanceof AndroidTestCase) {
                        AndroidTestCase testcase2 = (AndroidTestCase) test2;
                        try {
                            testcase2.setContext(this.mContext);
                            testcase2.setTestContext(this.mContext);
                        } catch (Exception ex3) {
                            Log.i(TAG, ex3.toString());
                        }
                    }
                    junitTestSuite.addTest(test2);
                }
                if (this.mMode == 1) {
                    int testCount = junitTestSuite.testCount();
                    for (int j = 0; j < testCount; j++) {
                        Test test3 = junitTestSuite.testAt(j);
                        started(test3.toString());
                        try {
                            runInPerformanceMode(test3, className, true, test3.toString());
                        } catch (Throwable ex22) {
                            e = ex22;
                        }
                        finished(test3.toString());
                        if (e == null) {
                            passed(test3.toString());
                        } else {
                            failed(test3.toString(), e);
                        }
                    }
                } else if (this.mMode == 2) {
                    startProfiling();
                    junit.textui.TestRunner.run(junitTestSuite);
                    finishProfiling();
                } else {
                    junit.textui.TestRunner.run(junitTestSuite);
                }
            } else {
                System.out.println("Test wasn't Runnable and didn't have a children method: " + className);
            }
        } catch (ClassNotFoundException e2) {
            Log.e("ClassNotFoundException for " + className, e2.toString());
            if (isJunitTest(className)) {
                runSingleJunitTest(className);
            } else {
                missingTest(className, e2);
            }
        } catch (InstantiationException e3) {
            System.out.println("InstantiationException for " + className);
            missingTest(className, e3);
        } catch (IllegalAccessException e4) {
            System.out.println("IllegalAccessException for " + className);
            missingTest(className, e4);
        }
    }

    public void runInPerformanceMode(Object testCase, String className, boolean junitTest, String testNameInDb) throws Exception {
        long duration;
        boolean increaseIterations = true;
        int iterations = 1;
        this.mIntermediates = null;
        this.mInternalIterations = 1;
        PerformanceTestCase perftest = this.mContext.getClassLoader().loadClass(className).newInstance();
        PerformanceTestCase perftestcase = null;
        if (perftest instanceof PerformanceTestCase) {
            perftestcase = perftest;
            if (this.mMode == 0 && perftestcase.isPerformanceOnly()) {
                return;
            }
        }
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        if (perftestcase != null) {
            this.mIntermediates = new ArrayList();
            iterations = perftestcase.startPerformance(this);
            if (iterations > 0) {
                increaseIterations = false;
            } else {
                iterations = 1;
            }
        }
        Thread.sleep(1000);
        while (true) {
            this.mEndTime = 0;
            if (increaseIterations) {
                this.mStartTime = SystemClock.currentThreadTimeMillis();
            } else {
                this.mStartTime = 0;
            }
            int i;
            if (junitTest) {
                for (i = 0; i < iterations; i++) {
                    junit.textui.TestRunner.run((Test) testCase);
                }
            } else {
                Runnable test = (Runnable) testCase;
                for (i = 0; i < iterations; i++) {
                    test.run();
                }
            }
            long endTime = this.mEndTime;
            if (endTime == 0) {
                endTime = SystemClock.currentThreadTimeMillis();
            }
            duration = endTime - this.mStartTime;
            if (increaseIterations) {
                if (duration > 1) {
                    if (duration > 10) {
                        if (duration >= 100) {
                            if (duration >= 1000) {
                                break;
                            }
                            iterations *= (int) ((1000 / duration) + 2);
                        } else {
                            iterations *= 10;
                        }
                    } else {
                        iterations *= 100;
                    }
                } else {
                    iterations *= 1000;
                }
            } else {
                break;
            }
        }
        if (duration != 0) {
            iterations *= this.mInternalIterations;
            performance(testNameInDb, (1000000 * duration) / ((long) iterations), iterations, this.mIntermediates);
        }
    }

    public void runSingleJunitTest(String className) {
        int index = className.lastIndexOf(36);
        String testName = "";
        String originalClassName = className;
        if (index >= 0) {
            className = className.substring(0, index);
            testName = originalClassName.substring(index + 1);
        }
        try {
            Class clazz = this.mContext.getClassLoader().loadClass(className);
            if (mJUnitClass.isAssignableFrom(clazz)) {
                TestCase test = (TestCase) clazz.newInstance();
                Test newSuite = new JunitTestSuite();
                test.setName(testName);
                if (test instanceof AndroidTestCase) {
                    try {
                        ((AndroidTestCase) test).setContext(this.mContext);
                    } catch (Exception ex) {
                        Log.w(TAG, "Exception encountered while trying to set the context.", ex);
                    }
                }
                newSuite.addTest(test);
                if (this.mMode == 1) {
                    try {
                        started(test.toString());
                        runInPerformanceMode(test, className, true, test.toString());
                        finished(test.toString());
                        passed(test.toString());
                    } catch (Throwable ex2) {
                        Throwable excep = ex2;
                    }
                } else if (this.mMode == 2) {
                    startProfiling();
                    junit.textui.TestRunner.run(newSuite);
                    finishProfiling();
                } else {
                    junit.textui.TestRunner.run(newSuite);
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "No test case to run", e);
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Illegal Access Exception", e2);
        } catch (InstantiationException e3) {
            Log.e(TAG, "Instantiation Exception", e3);
        }
    }

    public static Method getChildrenMethod(Class clazz) {
        try {
            return clazz.getMethod("children", (Class[]) null);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getChildrenMethod(Context c, String className) {
        try {
            return getChildrenMethod(c.getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static String[] getChildren(Context c, String className) {
        int i;
        int i2 = 1;
        Method m = getChildrenMethod(c, className);
        String[] testChildren = getTestChildren(c, className);
        if (m == null) {
            i = 1;
        } else {
            i = 0;
        }
        if (testChildren != null) {
            i2 = 0;
        }
        if ((i2 & i) != 0) {
            throw new RuntimeException("couldn't get children method for " + className);
        } else if (m != null) {
            String[] children = getChildren(m);
            if (testChildren == null) {
                return children;
            }
            String[] allChildren = new String[(testChildren.length + children.length)];
            System.arraycopy(children, 0, allChildren, 0, children.length);
            System.arraycopy(testChildren, 0, allChildren, children.length, testChildren.length);
            return allChildren;
        } else if (testChildren != null) {
            return testChildren;
        } else {
            return null;
        }
    }

    public static String[] getChildren(Method m) {
        try {
            if (Modifier.isStatic(m.getModifiers())) {
                return (String[]) m.invoke(null, (Object[]) null);
            }
            throw new RuntimeException("children method is not static");
        } catch (IllegalAccessException e) {
            return new String[0];
        } catch (InvocationTargetException e2) {
            return new String[0];
        }
    }

    public static String[] getTestChildren(Context c, String className) {
        try {
            Class clazz = c.getClassLoader().loadClass(className);
            if (mJUnitClass.isAssignableFrom(clazz)) {
                return getTestChildren(clazz);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "No class found", e);
        }
        return null;
    }

    public static String[] getTestChildren(Class clazz) {
        Method[] methods = getAllTestMethods(clazz);
        String[] onScreenTestNames = new String[methods.length];
        int index = 0;
        for (Method m : methods) {
            onScreenTestNames[index] = clazz.getName() + "$" + m.getName();
            index++;
        }
        return onScreenTestNames;
    }

    public static Method[] getAllTestMethods(Class clazz) {
        Method m;
        int i = 0;
        Method[] allMethods = clazz.getDeclaredMethods();
        int numOfMethods = 0;
        for (Method m2 : allMethods) {
            if (isTestMethod(m2)) {
                numOfMethods++;
            }
        }
        int index = 0;
        Method[] testMethods = new Method[numOfMethods];
        int length = allMethods.length;
        while (i < length) {
            m2 = allMethods[i];
            if (isTestMethod(m2)) {
                testMethods[index] = m2;
                index++;
            }
            i++;
        }
        return testMethods;
    }

    private static boolean isTestMethod(Method m) {
        if (m.getName().startsWith(InstrumentationTestRunner.REPORT_KEY_NAME_TEST) && m.getReturnType() == Void.TYPE && m.getParameterTypes().length == 0) {
            return true;
        }
        return false;
    }

    public static int countJunitTests(Class clazz) {
        return getAllTestMethods(clazz).length;
    }

    public static boolean isTestSuite(Context c, String className) {
        boolean childrenMethods = getChildrenMethod(c, className) != null;
        try {
            Class clazz = c.getClassLoader().loadClass(className);
            if (!mJUnitClass.isAssignableFrom(clazz) || countJunitTests(clazz) <= 0) {
                return childrenMethods;
            }
            return true;
        } catch (ClassNotFoundException e) {
            return childrenMethods;
        }
    }

    public boolean isJunitTest(String className) {
        int index = className.lastIndexOf(36);
        if (index >= 0) {
            className = className.substring(0, index);
        }
        try {
            if (mJUnitClass.isAssignableFrom(this.mContext.getClassLoader().loadClass(className))) {
                return true;
            }
        } catch (ClassNotFoundException e) {
        }
        return false;
    }

    public static int countTests(Context c, String className) {
        int i = 0;
        try {
            Class clazz = c.getClassLoader().loadClass(className);
            Method method = getChildrenMethod(clazz);
            if (method != null) {
                String[] children = getChildren(method);
                int rv = 0;
                while (i < children.length) {
                    rv += countTests(c, children[i]);
                    i++;
                }
                return rv;
            } else if (mRunnableClass.isAssignableFrom(clazz)) {
                return 1;
            } else {
                if (mJUnitClass.isAssignableFrom(clazz)) {
                    return countJunitTests(clazz);
                }
                return 0;
            }
        } catch (ClassNotFoundException e) {
            return 1;
        }
    }

    public static String getTitle(String className) {
        int indexDot = className.lastIndexOf(46);
        int indexDollar = className.lastIndexOf(36);
        int index = indexDot > indexDollar ? indexDot : indexDollar;
        if (index >= 0) {
            return className.substring(index + 1);
        }
        return className;
    }
}
