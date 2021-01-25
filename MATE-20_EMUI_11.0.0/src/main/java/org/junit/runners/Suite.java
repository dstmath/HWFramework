package org.junit.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class Suite extends ParentRunner<Runner> {
    private final List<Runner> runners;

    @Target({ElementType.TYPE})
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SuiteClasses {
        Class<?>[] value();
    }

    public static Runner emptySuite() {
        try {
            return new Suite((Class<?>) null, new Class[0]);
        } catch (InitializationError e) {
            throw new RuntimeException("This shouldn't be possible");
        }
    }

    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        SuiteClasses annotation = (SuiteClasses) klass.getAnnotation(SuiteClasses.class);
        if (annotation != null) {
            return annotation.value();
        }
        throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
    }

    public Suite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        this(builder, klass, getAnnotatedClasses(klass));
    }

    public Suite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        this((Class<?>) null, builder.runners((Class<?>) null, classes));
    }

    protected Suite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        this(new AllDefaultPossibilitiesBuilder(true), klass, suiteClasses);
    }

    protected Suite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        this(klass, builder.runners(klass, suiteClasses));
    }

    protected Suite(Class<?> klass, List<Runner> runners2) throws InitializationError {
        super(klass);
        this.runners = Collections.unmodifiableList(runners2);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.ParentRunner
    public List<Runner> getChildren() {
        return this.runners;
    }

    /* access modifiers changed from: protected */
    public Description describeChild(Runner child) {
        return child.getDescription();
    }

    /* access modifiers changed from: protected */
    public void runChild(Runner runner, RunNotifier notifier) {
        runner.run(notifier);
    }
}
