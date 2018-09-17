package junit.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class JUnit4TestAdapterCache extends HashMap<Description, Test> {
    private static final JUnit4TestAdapterCache fInstance = new JUnit4TestAdapterCache();
    private static final long serialVersionUID = 1;

    public static JUnit4TestAdapterCache getDefault() {
        return fInstance;
    }

    public Test asTest(Description description) {
        if (description.isSuite()) {
            return createTest(description);
        }
        if (!containsKey(description)) {
            put(description, createTest(description));
        }
        return (Test) get(description);
    }

    Test createTest(Description description) {
        if (description.isTest()) {
            return new JUnit4TestCaseFacade(description);
        }
        TestSuite suite = new TestSuite(description.getDisplayName());
        for (Description child : description.getChildren()) {
            suite.addTest(asTest(child));
        }
        return suite;
    }

    public RunNotifier getNotifier(final TestResult result, JUnit4TestAdapter adapter) {
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(new RunListener() {
            public void testFailure(Failure failure) throws Exception {
                result.addError(JUnit4TestAdapterCache.this.asTest(failure.getDescription()), failure.getException());
            }

            public void testFinished(Description description) throws Exception {
                result.endTest(JUnit4TestAdapterCache.this.asTest(description));
            }

            public void testStarted(Description description) throws Exception {
                result.startTest(JUnit4TestAdapterCache.this.asTest(description));
            }
        });
        return notifier;
    }

    public List<Test> asTestList(Description description) {
        if (description.isTest()) {
            return Arrays.asList(new Test[]{asTest(description)});
        }
        List<Test> returnThis = new ArrayList();
        for (Description child : description.getChildren()) {
            returnThis.add(asTest(child));
        }
        return returnThis;
    }
}
