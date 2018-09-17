package android.test;

import android.test.TestRunner.IntermediateTime;
import android.test.TestRunner.Listener;
import android.util.Log;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

@Deprecated
public class TestPrinter implements Listener, TestListener {
    private Set<String> mFailedTests = new HashSet();
    private boolean mOnlyFailures;
    private String mTag;

    public TestPrinter(String tag, boolean onlyFailures) {
        this.mTag = tag;
        this.mOnlyFailures = onlyFailures;
    }

    public void started(String className) {
        if (!this.mOnlyFailures) {
            Log.i(this.mTag, "started: " + className);
        }
    }

    public void finished(String className) {
        if (!this.mOnlyFailures) {
            Log.i(this.mTag, "finished: " + className);
        }
    }

    public void performance(String className, long itemTimeNS, int iterations, List<IntermediateTime> intermediates) {
        Log.i(this.mTag, "perf: " + className + " = " + itemTimeNS + "ns/op (done " + iterations + " times)");
        if (intermediates != null && intermediates.size() > 0) {
            int N = intermediates.size();
            for (int i = 0; i < N; i++) {
                IntermediateTime time = (IntermediateTime) intermediates.get(i);
                Log.i(this.mTag, "  intermediate: " + time.name + " = " + time.timeInNS + "ns");
            }
        }
    }

    public void passed(String className) {
        if (!this.mOnlyFailures) {
            Log.i(this.mTag, "passed: " + className);
        }
    }

    public void failed(String className, Throwable exception) {
        Log.i(this.mTag, "failed: " + className);
        Log.i(this.mTag, "----- begin exception -----");
        Log.i(this.mTag, "", exception);
        Log.i(this.mTag, "----- end exception -----");
    }

    private void failed(Test test, Throwable t) {
        this.mFailedTests.add(test.toString());
        failed(test.toString(), t);
    }

    public void addError(Test test, Throwable t) {
        failed(test, t);
    }

    public void addFailure(Test test, AssertionFailedError t) {
        failed(test, (Throwable) t);
    }

    public void endTest(Test test) {
        finished(test.toString());
        if (!this.mFailedTests.contains(test.toString())) {
            passed(test.toString());
        }
        this.mFailedTests.remove(test.toString());
    }

    public void startTest(Test test) {
        started(test.toString());
    }
}
