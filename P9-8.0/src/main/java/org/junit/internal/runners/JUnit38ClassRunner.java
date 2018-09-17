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

    private static final class OldTestClassAdaptingListener implements TestListener {
        private final RunNotifier notifier;

        /* synthetic */ OldTestClassAdaptingListener(RunNotifier notifier, OldTestClassAdaptingListener -this1) {
            this(notifier);
        }

        private OldTestClassAdaptingListener(RunNotifier notifier) {
            this.notifier = notifier;
        }

        public void endTest(Test test) {
            this.notifier.fireTestFinished(asDescription(test));
        }

        public void startTest(Test test) {
            this.notifier.fireTestStarted(asDescription(test));
        }

        public void addError(Test test, Throwable e) {
            this.notifier.fireTestFailure(new Failure(asDescription(test), e));
        }

        private Description asDescription(Test test) {
            if (test instanceof Describable) {
                return ((Describable) test).getDescription();
            }
            return Description.createTestDescription(getEffectiveClass(test), getName(test));
        }

        private Class<? extends Test> getEffectiveClass(Test test) {
            return test.getClass();
        }

        private String getName(Test test) {
            if (test instanceof TestCase) {
                return ((TestCase) test).getName();
            }
            return test.toString();
        }

        public void addFailure(Test test, AssertionFailedError t) {
            addError(test, t);
        }
    }

    public JUnit38ClassRunner(Class<?> klass) {
        this(new TestSuite(klass.asSubclass(TestCase.class)));
    }

    public JUnit38ClassRunner(Test test) {
        setTest(test);
    }

    public void run(RunNotifier notifier) {
        TestResult result = new TestResult();
        result.addListener(createAdaptingListener(notifier));
        getTest().run(result);
    }

    public TestListener createAdaptingListener(RunNotifier notifier) {
        return new OldTestClassAdaptingListener(notifier, null);
    }

    public Description getDescription() {
        return makeDescription(getTest());
    }

    private static Description makeDescription(Test test) {
        if (test instanceof TestCase) {
            TestCase tc = (TestCase) test;
            return Description.createTestDescription(tc.getClass(), tc.getName(), getAnnotations(tc));
        } else if (test instanceof TestSuite) {
            TestSuite ts = (TestSuite) test;
            Description description = Description.createSuiteDescription(ts.getName() == null ? createSuiteDescription(ts) : ts.getName(), new Annotation[0]);
            int n = ts.testCount();
            for (int i = 0; i < n; i++) {
                description.addChild(makeDescription(ts.testAt(i)));
            }
            return description;
        } else if (test instanceof Describable) {
            return ((Describable) test).getDescription();
        } else {
            if (test instanceof TestDecorator) {
                return makeDescription(((TestDecorator) test).getTest());
            }
            return Description.createSuiteDescription(test.getClass());
        }
    }

    private static Annotation[] getAnnotations(TestCase test) {
        try {
            return test.getClass().getMethod(test.getName(), new Class[0]).getDeclaredAnnotations();
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e2) {
        }
        return new Annotation[0];
    }

    private static String createSuiteDescription(TestSuite ts) {
        String example;
        if (ts.countTestCases() == 0) {
            example = "";
        } else {
            example = String.format(" [example: %s]", new Object[]{ts.testAt(0)});
        }
        return String.format("TestSuite with %s tests%s", new Object[]{Integer.valueOf(count), example});
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        if (getTest() instanceof Filterable) {
            ((Filterable) getTest()).filter(filter);
        } else if (getTest() instanceof TestSuite) {
            TestSuite suite = (TestSuite) getTest();
            TestSuite filtered = new TestSuite(suite.getName());
            int n = suite.testCount();
            for (int i = 0; i < n; i++) {
                Test test = suite.testAt(i);
                if (filter.shouldRun(makeDescription(test))) {
                    filtered.addTest(test);
                }
            }
            setTest(filtered);
            if (filtered.testCount() == 0) {
                throw new NoTestsRemainException();
            }
        }
    }

    public void sort(Sorter sorter) {
        if (getTest() instanceof Sortable) {
            ((Sortable) getTest()).sort(sorter);
        }
    }

    private void setTest(Test test) {
        this.test = test;
    }

    private Test getTest() {
        return this.test;
    }
}
