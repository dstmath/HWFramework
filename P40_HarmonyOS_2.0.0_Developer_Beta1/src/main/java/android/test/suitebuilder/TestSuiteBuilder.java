package android.test.suitebuilder;

import android.content.Context;
import android.test.AndroidTestRunner;
import android.test.TestCaseUtil;
import android.util.Log;
import com.android.internal.util.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@Deprecated
public class TestSuiteBuilder {
    private String currentClassname;
    private final Set<Predicate<TestMethod>> predicates;
    private TestSuite rootSuite;
    private TestSuite suiteForCurrentClass;
    private String suiteName;
    private List<TestCase> testCases;
    private final TestGrouping testGrouping;

    public TestSuiteBuilder(Class clazz) {
        this(clazz.getName(), clazz.getClassLoader());
    }

    public TestSuiteBuilder(String name, ClassLoader classLoader) {
        this.predicates = new HashSet();
        this.suiteName = name;
        this.testGrouping = new TestGrouping(TestGrouping.SORT_BY_FULLY_QUALIFIED_NAME, classLoader);
        this.testCases = new ArrayList();
        addRequirements(TestPredicates.REJECT_SUPPRESSED);
    }

    public TestSuiteBuilder addTestClassByName(String testClassName, String testMethodName, Context context) {
        AndroidTestRunner atr = new AndroidTestRunner();
        atr.setContext(context);
        atr.setTestClassName(testClassName, testMethodName);
        this.testCases.addAll(atr.getTestCases());
        return this;
    }

    public TestSuiteBuilder addTestSuite(TestSuite testSuite) {
        Iterator<? extends Test> it = TestCaseUtil.getTests(testSuite, true).iterator();
        while (it.hasNext()) {
            this.testCases.add((TestCase) it.next());
        }
        return this;
    }

    public TestSuiteBuilder includePackages(String... packageNames) {
        this.testGrouping.addPackagesRecursive(packageNames);
        return this;
    }

    public TestSuiteBuilder excludePackages(String... packageNames) {
        this.testGrouping.removePackagesRecursive(packageNames);
        return this;
    }

    public TestSuiteBuilder addRequirements(List<Predicate<TestMethod>> predicates2) {
        this.predicates.addAll(predicates2);
        return this;
    }

    public final TestSuiteBuilder includeAllPackagesUnderHere() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callingClassName = null;
        String thisClassName = TestSuiteBuilder.class.getName();
        int i = 0;
        while (true) {
            if (i >= stackTraceElements.length) {
                break;
            }
            StackTraceElement element = stackTraceElements[i];
            if (thisClassName.equals(element.getClassName()) && "includeAllPackagesUnderHere".equals(element.getMethodName())) {
                callingClassName = stackTraceElements[i + 1].getClassName();
                break;
            }
            i++;
        }
        return includePackages(parsePackageNameFromClassName(callingClassName));
    }

    public TestSuiteBuilder named(String newSuiteName) {
        this.suiteName = newSuiteName;
        return this;
    }

    public final TestSuite build() {
        this.rootSuite = new TestSuite(getSuiteName());
        this.currentClassname = null;
        try {
            for (TestMethod test : this.testGrouping.getTests()) {
                if (satisfiesAllPredicates(test)) {
                    addTest(test);
                }
            }
            if (this.testCases.size() > 0) {
                for (TestCase testCase : this.testCases) {
                    if (satisfiesAllPredicates(new TestMethod(testCase))) {
                        addTest((Test) testCase);
                    }
                }
            }
            return this.rootSuite;
        } catch (Exception exception) {
            Log.i("TestSuiteBuilder", "Failed to create test.", exception);
            TestSuite suite = new TestSuite(getSuiteName());
            suite.addTest(new FailedToCreateTests(exception));
            return suite;
        }
    }

    /* access modifiers changed from: protected */
    public String getSuiteName() {
        return this.suiteName;
    }

    public final TestSuiteBuilder addRequirements(Predicate<TestMethod>... predicates2) {
        ArrayList<Predicate<TestMethod>> list = new ArrayList<>();
        Collections.addAll(list, predicates2);
        return addRequirements(list);
    }

    @Deprecated
    public static class FailedToCreateTests extends TestCase {
        private final Exception exception;

        public FailedToCreateTests(Exception exception2) {
            super("testSuiteConstructionFailed");
            this.exception = exception2;
        }

        public void testSuiteConstructionFailed() {
            throw new RuntimeException("Exception during suite construction", this.exception);
        }
    }

    private boolean satisfiesAllPredicates(TestMethod test) {
        for (Predicate<TestMethod> predicate : this.predicates) {
            if (!predicate.apply(test)) {
                return false;
            }
        }
        return true;
    }

    private void addTest(TestMethod testMethod) throws Exception {
        addSuiteIfNecessary(testMethod.getEnclosingClassname());
        this.suiteForCurrentClass.addTest(testMethod.createTest());
    }

    private void addTest(Test test) {
        addSuiteIfNecessary(test.getClass().getName());
        this.suiteForCurrentClass.addTest(test);
    }

    private void addSuiteIfNecessary(String parentClassname) {
        if (!parentClassname.equals(this.currentClassname)) {
            this.currentClassname = parentClassname;
            this.suiteForCurrentClass = new TestSuite(parentClassname);
            this.rootSuite.addTest(this.suiteForCurrentClass);
        }
    }

    private static String parsePackageNameFromClassName(String className) {
        return className.substring(0, className.lastIndexOf(46));
    }
}
