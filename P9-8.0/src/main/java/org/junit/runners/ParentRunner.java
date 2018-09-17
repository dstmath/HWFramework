package org.junit.runners;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.rules.RuleMemberValidator;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationsValidator;
import org.junit.validator.PublicClassValidator;
import org.junit.validator.TestClassValidator;

public abstract class ParentRunner<T> extends Runner implements Filterable, Sortable {
    private static final List<TestClassValidator> VALIDATORS = Arrays.asList(new TestClassValidator[]{new AnnotationsValidator(), new PublicClassValidator()});
    private final Object childrenLock = new Object();
    private volatile Collection<T> filteredChildren = null;
    private volatile RunnerScheduler scheduler = new RunnerScheduler() {
        public void schedule(Runnable childStatement) {
            childStatement.run();
        }

        public void finished() {
        }
    };
    private final TestClass testClass;

    protected abstract Description describeChild(T t);

    protected abstract List<T> getChildren();

    protected abstract void runChild(T t, RunNotifier runNotifier);

    protected ParentRunner(Class<?> testClass) throws InitializationError {
        this.testClass = createTestClass(testClass);
        validate();
    }

    protected TestClass createTestClass(Class<?> testClass) {
        return new TestClass(testClass);
    }

    protected void collectInitializationErrors(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(BeforeClass.class, true, errors);
        validatePublicVoidNoArgMethods(AfterClass.class, true, errors);
        validateClassRules(errors);
        applyValidators(errors);
    }

    private void applyValidators(List<Throwable> errors) {
        if (getTestClass().getJavaClass() != null) {
            for (TestClassValidator each : VALIDATORS) {
                errors.addAll(each.validateTestClass(getTestClass()));
            }
        }
    }

    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
        for (FrameworkMethod eachTestMethod : getTestClass().getAnnotatedMethods(annotation)) {
            eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
        }
    }

    private void validateClassRules(List<Throwable> errors) {
        RuleMemberValidator.CLASS_RULE_VALIDATOR.validate(getTestClass(), errors);
        RuleMemberValidator.CLASS_RULE_METHOD_VALIDATOR.validate(getTestClass(), errors);
    }

    protected Statement classBlock(RunNotifier notifier) {
        Statement statement = childrenInvoker(notifier);
        if (areAllChildrenIgnored()) {
            return statement;
        }
        return withClassRules(withAfterClasses(withBeforeClasses(statement)));
    }

    private boolean areAllChildrenIgnored() {
        for (T child : getFilteredChildren()) {
            if (!isIgnored(child)) {
                return false;
            }
        }
        return true;
    }

    protected Statement withBeforeClasses(Statement statement) {
        List<FrameworkMethod> befores = this.testClass.getAnnotatedMethods(BeforeClass.class);
        if (befores.isEmpty()) {
            return statement;
        }
        return new RunBefores(statement, befores, null);
    }

    protected Statement withAfterClasses(Statement statement) {
        List<FrameworkMethod> afters = this.testClass.getAnnotatedMethods(AfterClass.class);
        if (afters.isEmpty()) {
            return statement;
        }
        return new RunAfters(statement, afters, null);
    }

    private Statement withClassRules(Statement statement) {
        List<TestRule> classRules = classRules();
        if (classRules.isEmpty()) {
            return statement;
        }
        return new RunRules(statement, classRules, getDescription());
    }

    protected List<TestRule> classRules() {
        List<TestRule> result = this.testClass.getAnnotatedMethodValues(null, ClassRule.class, TestRule.class);
        result.addAll(this.testClass.getAnnotatedFieldValues(null, ClassRule.class, TestRule.class));
        return result;
    }

    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            public void evaluate() {
                ParentRunner.this.runChildren(notifier);
            }
        };
    }

    protected boolean isIgnored(T t) {
        return false;
    }

    private void runChildren(final RunNotifier notifier) {
        RunnerScheduler currentScheduler = this.scheduler;
        try {
            for (final T each : getFilteredChildren()) {
                currentScheduler.schedule(new Runnable() {
                    public void run() {
                        ParentRunner.this.runChild(each, notifier);
                    }
                });
            }
        } finally {
            currentScheduler.finished();
        }
    }

    protected String getName() {
        return this.testClass.getName();
    }

    public final TestClass getTestClass() {
        return this.testClass;
    }

    protected final void runLeaf(Statement statement, Description description, RunNotifier notifier) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        try {
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e2) {
            eachNotifier.addFailure(e2);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }

    protected Annotation[] getRunnerAnnotations() {
        return this.testClass.getAnnotations();
    }

    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName(), getRunnerAnnotations());
        for (T child : getFilteredChildren()) {
            description.addChild(describeChild(child));
        }
        return description;
    }

    public void run(RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, getDescription());
        try {
            classBlock(notifier).evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e2) {
            throw e2;
        } catch (Throwable e3) {
            testNotifier.addFailure(e3);
        }
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        synchronized (this.childrenLock) {
            List<T> children = new ArrayList(getFilteredChildren());
            Iterator<T> iter = children.iterator();
            while (iter.hasNext()) {
                T each = iter.next();
                if (shouldRun(filter, each)) {
                    try {
                        filter.apply(each);
                    } catch (NoTestsRemainException e) {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
            this.filteredChildren = Collections.unmodifiableCollection(children);
            if (this.filteredChildren.isEmpty()) {
                throw new NoTestsRemainException();
            }
        }
    }

    public void sort(Sorter sorter) {
        synchronized (this.childrenLock) {
            for (T each : getFilteredChildren()) {
                sorter.apply(each);
            }
            List<T> sortedChildren = new ArrayList(getFilteredChildren());
            Collections.sort(sortedChildren, comparator(sorter));
            this.filteredChildren = Collections.unmodifiableCollection(sortedChildren);
        }
    }

    private void validate() throws InitializationError {
        List errors = new ArrayList();
        collectInitializationErrors(errors);
        if (!errors.isEmpty()) {
            throw new InitializationError(errors);
        }
    }

    private Collection<T> getFilteredChildren() {
        if (this.filteredChildren == null) {
            synchronized (this.childrenLock) {
                if (this.filteredChildren == null) {
                    this.filteredChildren = Collections.unmodifiableCollection(getChildren());
                }
            }
        }
        return this.filteredChildren;
    }

    private boolean shouldRun(Filter filter, T each) {
        return filter.shouldRun(describeChild(each));
    }

    private Comparator<? super T> comparator(final Sorter sorter) {
        return new Comparator<T>() {
            public int compare(T o1, T o2) {
                return sorter.compare(ParentRunner.this.describeChild(o1), ParentRunner.this.describeChild(o2));
            }
        };
    }

    public void setScheduler(RunnerScheduler scheduler) {
        this.scheduler = scheduler;
    }
}
