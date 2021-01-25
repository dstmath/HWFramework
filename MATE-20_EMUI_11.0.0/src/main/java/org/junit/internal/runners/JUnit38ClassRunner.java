package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import junit.extensions.TestDecorator;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnit38ClassRunner extends Runner implements Filterable, Sortable {
    private volatile Test test;

    /* access modifiers changed from: private */
    public static final class OldTestClassAdaptingListener implements TestListener {
        private final RunNotifier notifier;

        private OldTestClassAdaptingListener(RunNotifier notifier2) {
            this.notifier = notifier2;
        }

        @Override // junit.framework.TestListener
        public void endTest(Test test) {
            this.notifier.fireTestFinished(asDescription(test));
        }

        @Override // junit.framework.TestListener
        public void startTest(Test test) {
            this.notifier.fireTestStarted(asDescription(test));
        }

        @Override // junit.framework.TestListener
        public void addError(Test test, Throwable e) {
            this.notifier.fireTestFailure(new Failure(asDescription(test), e));
        }

        private Description asDescription(Test test) {
            if (test instanceof Describable) {
                return ((Describable) test).getDescription();
            }
            return Description.createTestDescription(getEffectiveClass(test), getName(test));
        }

        /* JADX DEBUG: Type inference failed for r0v0. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends junit.framework.Test> */
        private Class<? extends Test> getEffectiveClass(Test test) {
            return test.getClass();
        }

        private String getName(Test test) {
            if (test instanceof TestCase) {
                return ((TestCase) test).getName();
            }
            return test.toString();
        }

        @Override // junit.framework.TestListener
        public void addFailure(Test test, AssertionFailedError t) {
            addError(test, t);
        }
    }

    public JUnit38ClassRunner(Class<?> klass) {
        this(new TestSuite(klass.asSubclass(TestCase.class)));
    }

    public JUnit38ClassRunner(Test test2) {
        setTest(test2);
    }

    @Override // org.junit.runner.Runner
    public void run(RunNotifier notifier) {
        TestResult result = new TestResult();
        result.addListener(createAdaptingListener(notifier));
        getTest().run(result);
    }

    public TestListener createAdaptingListener(RunNotifier notifier) {
        return new OldTestClassAdaptingListener(notifier);
    }

    @Override // org.junit.runner.Runner, org.junit.runner.Describable
    public Description getDescription() {
        return makeDescription(getTest());
    }

    private static Description makeDescription(Test test2) {
        if (test2 instanceof TestCase) {
            TestCase tc = (TestCase) test2;
            return Description.createTestDescription(tc.getClass(), tc.getName(), getAnnotations(tc));
        } else if (test2 instanceof TestSuite) {
            TestSuite ts = (TestSuite) test2;
            Description description = Description.createSuiteDescription(ts.getName() == null ? createSuiteDescription(ts) : ts.getName(), new Annotation[0]);
            int n = ts.testCount();
            for (int i = 0; i < n; i++) {
                description.addChild(makeDescription(ts.testAt(i)));
            }
            return description;
        } else if (test2 instanceof Describable) {
            return ((Describable) test2).getDescription();
        } else {
            if (test2 instanceof TestDecorator) {
                return makeDescription(((TestDecorator) test2).getTest());
            }
            return Description.createSuiteDescription(test2.getClass());
        }
    }

    private static Annotation[] getAnnotations(TestCase test2) {
        try {
            return test2.getClass().getMethod(test2.getName(), new Class[0]).getDeclaredAnnotations();
        } catch (NoSuchMethodException | SecurityException e) {
            return new Annotation[0];
        }
    }

    private static String createSuiteDescription(TestSuite ts) {
        int count = ts.countTestCases();
        return String.format("TestSuite with %s tests%s", Integer.valueOf(count), count == 0 ? "" : String.format(" [example: %s]", ts.testAt(0)));
    }

    @Override // org.junit.runner.manipulation.Filterable
    public void filter(Filter filter) throws NoTestsRemainException {
        if (getTest() instanceof Filterable) {
            ((Filterable) getTest()).filter(filter);
        } else if (getTest() instanceof TestSuite) {
            TestSuite suite = (TestSuite) getTest();
            TestSuite filtered = new TestSuite(suite.getName());
            int n = suite.testCount();
            for (int i = 0; i < n; i++) {
                Test test2 = suite.testAt(i);
                if (filter.shouldRun(makeDescription(test2))) {
                    filtered.addTest(test2);
                }
            }
            setTest(filtered);
            if (filtered.testCount() == 0) {
                throw new NoTestsRemainException();
            }
        }
    }

    @Override // org.junit.runner.manipulation.Sortable
    public void sort(Sorter sorter) {
        if (getTest() instanceof Sortable) {
            ((Sortable) getTest()).sort(sorter);
        }
    }

    private void setTest(Test test2) {
        this.test = test2;
    }

    private Test getTest() {
        return this.test;
    }
}
