package junit.extensions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class ActiveTestSuite extends TestSuite {
    private volatile int fActiveTestDeathCount;

    /* renamed from: junit.extensions.ActiveTestSuite.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ TestResult val$result;
        final /* synthetic */ Test val$test;

        AnonymousClass1(Test val$test, TestResult val$result) {
            this.val$test = val$test;
            this.val$result = val$result;
        }

        public void run() {
            try {
                this.val$test.run(this.val$result);
            } finally {
                ActiveTestSuite.this.runFinished();
            }
        }
    }

    public ActiveTestSuite(Class<? extends TestCase> theClass) {
        super((Class) theClass);
    }

    public ActiveTestSuite(String name) {
        super(name);
    }

    public ActiveTestSuite(Class<? extends TestCase> theClass, String name) {
        super((Class) theClass, name);
    }

    public void run(TestResult result) {
        this.fActiveTestDeathCount = 0;
        super.run(result);
        waitUntilFinished();
    }

    public void runTest(Test test, TestResult result) {
        new AnonymousClass1(test, result).start();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void waitUntilFinished() {
        while (true) {
            if (this.fActiveTestDeathCount < testCount()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public synchronized void runFinished() {
        this.fActiveTestDeathCount++;
        notifyAll();
    }
}
