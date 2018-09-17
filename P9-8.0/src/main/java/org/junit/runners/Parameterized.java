package org.junit.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.runner.Runner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParametersFactory;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;

public class Parameterized extends Suite {
    private static final ParametersRunnerFactory DEFAULT_FACTORY = new BlockJUnit4ClassRunnerWithParametersFactory();
    private static final List<Runner> NO_RUNNERS = Collections.emptyList();
    private final List<Runner> runners;

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parameter {
        int value() default 0;
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parameters {
        String name() default "{index}";
    }

    @Inherited
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UseParametersRunnerFactory {
        Class<? extends ParametersRunnerFactory> value() default BlockJUnit4ClassRunnerWithParametersFactory.class;
    }

    public Parameterized(Class<?> klass) throws Throwable {
        super((Class) klass, NO_RUNNERS);
        this.runners = Collections.unmodifiableList(createRunnersForParameters(allParameters(), ((Parameters) getParametersMethod().getAnnotation(Parameters.class)).name(), getParametersRunnerFactory(klass)));
    }

    private ParametersRunnerFactory getParametersRunnerFactory(Class<?> klass) throws InstantiationException, IllegalAccessException {
        UseParametersRunnerFactory annotation = (UseParametersRunnerFactory) klass.getAnnotation(UseParametersRunnerFactory.class);
        if (annotation == null) {
            return DEFAULT_FACTORY;
        }
        return (ParametersRunnerFactory) annotation.value().newInstance();
    }

    protected List<Runner> getChildren() {
        return this.runners;
    }

    private TestWithParameters createTestWithNotNormalizedParameters(String pattern, int index, Object parametersOrSingleParameter) {
        return createTestWithParameters(getTestClass(), pattern, index, parametersOrSingleParameter instanceof Object[] ? (Object[]) parametersOrSingleParameter : new Object[]{parametersOrSingleParameter});
    }

    private Iterable<Object> allParameters() throws Throwable {
        Object parameters = getParametersMethod().invokeExplosively(null, new Object[0]);
        if (parameters instanceof Iterable) {
            return (Iterable) parameters;
        }
        if (parameters instanceof Object[]) {
            return Arrays.asList((Object[]) parameters);
        }
        throw parametersMethodReturnedWrongType();
    }

    private FrameworkMethod getParametersMethod() throws Exception {
        for (FrameworkMethod each : getTestClass().getAnnotatedMethods(Parameters.class)) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }
        throw new Exception("No public static parameters method on class " + getTestClass().getName());
    }

    private List<Runner> createRunnersForParameters(Iterable<Object> allParameters, String namePattern, ParametersRunnerFactory runnerFactory) throws InitializationError, Exception {
        try {
            List<TestWithParameters> tests = createTestsForParameters(allParameters, namePattern);
            List<Runner> runners = new ArrayList();
            for (TestWithParameters test : tests) {
                runners.add(runnerFactory.createRunnerForTestWithParameters(test));
            }
            return runners;
        } catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    private List<TestWithParameters> createTestsForParameters(Iterable<Object> allParameters, String namePattern) throws Exception {
        int i = 0;
        List<TestWithParameters> children = new ArrayList();
        for (Object parametersOfSingleTest : allParameters) {
            int i2 = i + 1;
            children.add(createTestWithNotNormalizedParameters(namePattern, i, parametersOfSingleTest));
            i = i2;
        }
        return children;
    }

    private Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        return new Exception(MessageFormat.format("{0}.{1}() must return an Iterable of arrays.", new Object[]{className, methodName}));
    }

    private static TestWithParameters createTestWithParameters(TestClass testClass, String pattern, int index, Object[] parameters) {
        return new TestWithParameters("[" + MessageFormat.format(pattern.replaceAll("\\{index\\}", Integer.toString(index)), parameters) + "]", testClass, Arrays.asList(parameters));
    }
}
