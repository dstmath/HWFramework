package org.junit.rules;

import java.util.ArrayList;
import java.util.List;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

public abstract class TestWatcher implements TestRule {
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<>();
                TestWatcher.this.startingQuietly(description, errors);
                try {
                    base.evaluate();
                    TestWatcher.this.succeededQuietly(description, errors);
                } catch (AssumptionViolatedException e) {
                    errors.add(e);
                    TestWatcher.this.skippedQuietly(e, description, errors);
                } catch (Throwable th) {
                    TestWatcher.this.finishedQuietly(description, errors);
                    throw th;
                }
                TestWatcher.this.finishedQuietly(description, errors);
                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    /* access modifiers changed from: private */
    public void succeededQuietly(Description description, List<Throwable> errors) {
        try {
            succeeded(description);
        } catch (Throwable e) {
            errors.add(e);
        }
    }

    /* access modifiers changed from: private */
    public void failedQuietly(Throwable e, Description description, List<Throwable> errors) {
        try {
            failed(e, description);
        } catch (Throwable e1) {
            errors.add(e1);
        }
    }

    /* access modifiers changed from: private */
    public void skippedQuietly(AssumptionViolatedException e, Description description, List<Throwable> errors) {
        try {
            if (e instanceof org.junit.AssumptionViolatedException) {
                skipped((org.junit.AssumptionViolatedException) e, description);
            } else {
                skipped(e, description);
            }
        } catch (Throwable e1) {
            errors.add(e1);
        }
    }

    /* access modifiers changed from: private */
    public void startingQuietly(Description description, List<Throwable> errors) {
        try {
            starting(description);
        } catch (Throwable e) {
            errors.add(e);
        }
    }

    /* access modifiers changed from: private */
    public void finishedQuietly(Description description, List<Throwable> errors) {
        try {
            finished(description);
        } catch (Throwable e) {
            errors.add(e);
        }
    }

    /* access modifiers changed from: protected */
    public void succeeded(Description description) {
    }

    /* access modifiers changed from: protected */
    public void failed(Throwable e, Description description) {
    }

    /* access modifiers changed from: protected */
    public void skipped(org.junit.AssumptionViolatedException e, Description description) {
        skipped((AssumptionViolatedException) e, description);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void skipped(AssumptionViolatedException e, Description description) {
    }

    /* access modifiers changed from: protected */
    public void starting(Description description) {
    }

    /* access modifiers changed from: protected */
    public void finished(Description description) {
    }
}
