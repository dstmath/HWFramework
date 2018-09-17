package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

@Deprecated
public class JUnit4ClassRunner extends Runner implements Filterable, Sortable {
    private TestClass testClass;
    private final List<Method> testMethods = getTestMethods();

    public JUnit4ClassRunner(Class<?> klass) throws InitializationError {
        this.testClass = new TestClass(klass);
        validate();
    }

    protected List<Method> getTestMethods() {
        return this.testClass.getTestMethods();
    }

    protected void validate() throws InitializationError {
        MethodValidator methodValidator = new MethodValidator(this.testClass);
        methodValidator.validateMethodsForDefaultRunner();
        methodValidator.assertValid();
    }

    public void run(final RunNotifier notifier) {
        new ClassRoadie(notifier, this.testClass, getDescription(), new Runnable() {
            public void run() {
                JUnit4ClassRunner.this.runMethods(notifier);
            }
        }).runProtected();
    }

    protected void runMethods(RunNotifier notifier) {
        for (Method method : this.testMethods) {
            invokeTestMethod(method, notifier);
        }
    }

    public Description getDescription() {
        Description spec = Description.createSuiteDescription(getName(), classAnnotations());
        for (Method method : this.testMethods) {
            spec.addChild(methodDescription(method));
        }
        return spec;
    }

    protected Annotation[] classAnnotations() {
        return this.testClass.getJavaClass().getAnnotations();
    }

    protected String getName() {
        return getTestClass().getName();
    }

    protected Object createTest() throws Exception {
        return getTestClass().getConstructor().newInstance(new Object[0]);
    }

    protected void invokeTestMethod(Method method, RunNotifier notifier) {
        Description description = methodDescription(method);
        try {
            new MethodRoadie(createTest(), wrapMethod(method), notifier, description).run();
        } catch (InvocationTargetException e) {
            testAborted(notifier, description, e.getCause());
        } catch (Exception e2) {
            testAborted(notifier, description, e2);
        }
    }

    private void testAborted(RunNotifier notifier, Description description, Throwable e) {
        notifier.fireTestStarted(description);
        notifier.fireTestFailure(new Failure(description, e));
        notifier.fireTestFinished(description);
    }

    protected TestMethod wrapMethod(Method method) {
        return new TestMethod(method, this.testClass);
    }

    protected String testName(Method method) {
        return method.getName();
    }

    protected Description methodDescription(Method method) {
        return Description.createTestDescription(getTestClass().getJavaClass(), testName(method), testAnnotations(method));
    }

    protected Annotation[] testAnnotations(Method method) {
        return method.getAnnotations();
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        Iterator<Method> iter = this.testMethods.iterator();
        while (iter.hasNext()) {
            if (!filter.shouldRun(methodDescription((Method) iter.next()))) {
                iter.remove();
            }
        }
        if (this.testMethods.isEmpty()) {
            throw new NoTestsRemainException();
        }
    }

    public void sort(final Sorter sorter) {
        Collections.sort(this.testMethods, new Comparator<Method>() {
            public int compare(Method o1, Method o2) {
                return sorter.compare(JUnit4ClassRunner.this.methodDescription(o1), JUnit4ClassRunner.this.methodDescription(o2));
            }
        });
    }

    protected TestClass getTestClass() {
        return this.testClass;
    }
}
