package junit.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class TestResult {
    protected List<TestFailure> fErrors = new ArrayList();
    protected List<TestFailure> fFailures = new ArrayList();
    protected List<TestListener> fListeners = new ArrayList();
    protected int fRunTests = 0;
    private boolean fStop = false;

    public synchronized void addError(Test test, Throwable e) {
        this.fErrors.add(new TestFailure(test, e));
        for (TestListener each : cloneListeners()) {
            each.addError(test, e);
        }
    }

    public synchronized void addFailure(Test test, AssertionFailedError e) {
        this.fFailures.add(new TestFailure(test, e));
        for (TestListener each : cloneListeners()) {
            each.addFailure(test, e);
        }
    }

    public synchronized void addListener(TestListener listener) {
        this.fListeners.add(listener);
    }

    public synchronized void removeListener(TestListener listener) {
        this.fListeners.remove(listener);
    }

    private synchronized List<TestListener> cloneListeners() {
        List<TestListener> result;
        result = new ArrayList<>();
        result.addAll(this.fListeners);
        return result;
    }

    public void endTest(Test test) {
        for (TestListener each : cloneListeners()) {
            each.endTest(test);
        }
    }

    public synchronized int errorCount() {
        return this.fErrors.size();
    }

    public synchronized Enumeration<TestFailure> errors() {
        return Collections.enumeration(this.fErrors);
    }

    public synchronized int failureCount() {
        return this.fFailures.size();
    }

    public synchronized Enumeration<TestFailure> failures() {
        return Collections.enumeration(this.fFailures);
    }

    /* access modifiers changed from: protected */
    public void run(final TestCase test) {
        startTest(test);
        runProtected(test, new Protectable() {
            /* class junit.framework.TestResult.AnonymousClass1 */

            @Override // junit.framework.Protectable
            public void protect() throws Throwable {
                test.runBare();
            }
        });
        endTest(test);
    }

    public synchronized int runCount() {
        return this.fRunTests;
    }

    public void runProtected(Test test, Protectable p) {
        try {
            p.protect();
        } catch (AssertionFailedError e) {
            addFailure(test, e);
        } catch (ThreadDeath e2) {
            throw e2;
        } catch (Throwable e3) {
            addError(test, e3);
        }
    }

    public synchronized boolean shouldStop() {
        return this.fStop;
    }

    public void startTest(Test test) {
        int count = test.countTestCases();
        synchronized (this) {
            this.fRunTests += count;
        }
        for (TestListener each : cloneListeners()) {
            each.startTest(test);
        }
    }

    public synchronized void stop() {
        this.fStop = true;
    }

    public synchronized boolean wasSuccessful() {
        return failureCount() == 0 && errorCount() == 0;
    }
}
