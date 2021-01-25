package org.junit.internal.builders;

import java.lang.reflect.Modifier;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class AnnotatedBuilder extends RunnerBuilder {
    private static final String CONSTRUCTOR_ERROR_FORMAT = "Custom runner class %s should have a public constructor with signature %s(Class testClass)";
    private final RunnerBuilder suiteBuilder;

    public AnnotatedBuilder(RunnerBuilder suiteBuilder2) {
        this.suiteBuilder = suiteBuilder2;
    }

    @Override // org.junit.runners.model.RunnerBuilder
    public Runner runnerForClass(Class<?> testClass) throws Exception {
        Class<?> currentTestClass = testClass;
        while (currentTestClass != null) {
            RunWith annotation = (RunWith) currentTestClass.getAnnotation(RunWith.class);
            if (annotation != null) {
                return buildRunner(annotation.value(), testClass);
            }
            currentTestClass = getEnclosingClassForNonStaticMemberClass(currentTestClass);
        }
        return null;
    }

    private Class<?> getEnclosingClassForNonStaticMemberClass(Class<?> currentTestClass) {
        if (!currentTestClass.isMemberClass() || Modifier.isStatic(currentTestClass.getModifiers())) {
            return null;
        }
        return currentTestClass.getEnclosingClass();
    }

    public Runner buildRunner(Class<? extends Runner> runnerClass, Class<?> testClass) throws Exception {
        try {
            return (Runner) runnerClass.getConstructor(Class.class).newInstance(testClass);
        } catch (NoSuchMethodException e) {
            try {
                return (Runner) runnerClass.getConstructor(Class.class, RunnerBuilder.class).newInstance(testClass, this.suiteBuilder);
            } catch (NoSuchMethodException e2) {
                String simpleName = runnerClass.getSimpleName();
                throw new InitializationError(String.format(CONSTRUCTOR_ERROR_FORMAT, simpleName, simpleName));
            }
        }
    }
}
