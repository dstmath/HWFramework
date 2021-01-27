package org.junit.runners;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.rules.RuleMemberValidator;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.MethodRule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class BlockJUnit4ClassRunner extends ParentRunner<FrameworkMethod> {
    private final ConcurrentHashMap<FrameworkMethod, Description> methodDescriptions = new ConcurrentHashMap<>();

    public BlockJUnit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /* access modifiers changed from: protected */
    public void runChild(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (isIgnored(method)) {
            notifier.fireTestIgnored(description);
        } else {
            runLeaf(methodBlock(method), description, notifier);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isIgnored(FrameworkMethod child) {
        return child.getAnnotation(Ignore.class) != null;
    }

    /* access modifiers changed from: protected */
    public Description describeChild(FrameworkMethod method) {
        Description description = this.methodDescriptions.get(method);
        if (description != null) {
            return description;
        }
        Description description2 = Description.createTestDescription(getTestClass().getJavaClass(), testName(method), method.getAnnotations());
        this.methodDescriptions.putIfAbsent(method, description2);
        return description2;
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.ParentRunner
    public List<FrameworkMethod> getChildren() {
        return computeTestMethods();
    }

    /* access modifiers changed from: protected */
    public List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(Test.class);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.ParentRunner
    public void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        validateNoNonStaticInnerClass(errors);
        validateConstructor(errors);
        validateInstanceMethods(errors);
        validateFields(errors);
        validateMethods(errors);
    }

    /* access modifiers changed from: protected */
    public void validateNoNonStaticInnerClass(List<Throwable> errors) {
        if (getTestClass().isANonStaticInnerClass()) {
            errors.add(new Exception("The inner class " + getTestClass().getName() + " is not static."));
        }
    }

    /* access modifiers changed from: protected */
    public void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
        validateZeroArgConstructor(errors);
    }

    /* access modifiers changed from: protected */
    public void validateOnlyOneConstructor(List<Throwable> errors) {
        if (!hasOneConstructor()) {
            errors.add(new Exception("Test class should have exactly one public constructor"));
        }
    }

    /* access modifiers changed from: protected */
    public void validateZeroArgConstructor(List<Throwable> errors) {
        if (!getTestClass().isANonStaticInnerClass() && hasOneConstructor() && getTestClass().getOnlyConstructor().getParameterTypes().length != 0) {
            errors.add(new Exception("Test class should have exactly one public zero-argument constructor"));
        }
    }

    private boolean hasOneConstructor() {
        return getTestClass().getJavaClass().getConstructors().length == 1;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);
        if (computeTestMethods().size() == 0) {
            errors.add(new Exception("No runnable methods"));
        }
    }

    /* access modifiers changed from: protected */
    public void validateFields(List<Throwable> errors) {
        RuleMemberValidator.RULE_VALIDATOR.validate(getTestClass(), errors);
    }

    private void validateMethods(List<Throwable> errors) {
        RuleMemberValidator.RULE_METHOD_VALIDATOR.validate(getTestClass(), errors);
    }

    /* access modifiers changed from: protected */
    public void validateTestMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(Test.class, false, errors);
    }

    /* access modifiers changed from: protected */
    public Object createTest() throws Exception {
        return getTestClass().getOnlyConstructor().newInstance(new Object[0]);
    }

    /* access modifiers changed from: protected */
    public String testName(FrameworkMethod method) {
        return method.getName();
    }

    /* access modifiers changed from: protected */
    public Statement methodBlock(FrameworkMethod method) {
        try {
            Object test = new ReflectiveCallable() {
                /* class org.junit.runners.BlockJUnit4ClassRunner.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // org.junit.internal.runners.model.ReflectiveCallable
                public Object runReflectiveCall() throws Throwable {
                    return BlockJUnit4ClassRunner.this.createTest();
                }
            }.run();
            return withRules(method, test, withAfters(method, test, withBefores(method, test, withPotentialTimeout(method, test, possiblyExpectingExceptions(method, test, methodInvoker(method, test))))));
        } catch (Throwable e) {
            return new Fail(e);
        }
    }

    /* access modifiers changed from: protected */
    public Statement methodInvoker(FrameworkMethod method, Object test) {
        return new InvokeMethod(method, test);
    }

    /* access modifiers changed from: protected */
    public Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
        Test annotation = (Test) method.getAnnotation(Test.class);
        if (expectsException(annotation)) {
            return new ExpectException(next, getExpectedException(annotation));
        }
        return next;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
        long timeout = getTimeout((Test) method.getAnnotation(Test.class));
        if (timeout <= 0) {
            return next;
        }
        return FailOnTimeout.builder().withTimeout(timeout, TimeUnit.MILLISECONDS).build(next);
    }

    /* access modifiers changed from: protected */
    public Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
        return befores.isEmpty() ? statement : new RunBefores(statement, befores, target);
    }

    /* access modifiers changed from: protected */
    public Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters, target);
    }

    private Statement withRules(FrameworkMethod method, Object target, Statement statement) {
        List<TestRule> testRules = getTestRules(target);
        return withTestRules(method, testRules, withMethodRules(method, testRules, target, statement));
    }

    private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules, Object target, Statement result) {
        for (MethodRule each : getMethodRules(target)) {
            if (!testRules.contains(each)) {
                result = each.apply(result, method, target);
            }
        }
        return result;
    }

    private List<MethodRule> getMethodRules(Object target) {
        return rules(target);
    }

    /* access modifiers changed from: protected */
    public List<MethodRule> rules(Object target) {
        List<MethodRule> rules = getTestClass().getAnnotatedMethodValues(target, Rule.class, MethodRule.class);
        rules.addAll(getTestClass().getAnnotatedFieldValues(target, Rule.class, MethodRule.class));
        return rules;
    }

    private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules, Statement statement) {
        if (testRules.isEmpty()) {
            return statement;
        }
        return new RunRules(statement, testRules, describeChild(method));
    }

    /* access modifiers changed from: protected */
    public List<TestRule> getTestRules(Object target) {
        List<TestRule> result = getTestClass().getAnnotatedMethodValues(target, Rule.class, TestRule.class);
        result.addAll(getTestClass().getAnnotatedFieldValues(target, Rule.class, TestRule.class));
        return result;
    }

    private Class<? extends Throwable> getExpectedException(Test annotation) {
        if (annotation == null || annotation.expected() == Test.None.class) {
            return null;
        }
        return annotation.expected();
    }

    private boolean expectsException(Test annotation) {
        return getExpectedException(annotation) != null;
    }

    private long getTimeout(Test annotation) {
        if (annotation == null) {
            return 0;
        }
        return annotation.timeout();
    }
}
