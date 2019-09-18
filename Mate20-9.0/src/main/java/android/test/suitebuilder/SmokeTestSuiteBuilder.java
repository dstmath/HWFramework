package android.test.suitebuilder;

import com.android.internal.util.Predicate;

public class SmokeTestSuiteBuilder extends TestSuiteBuilder {
    public SmokeTestSuiteBuilder(Class clazz) {
        this(clazz.getName(), clazz.getClassLoader());
    }

    public SmokeTestSuiteBuilder(String name, ClassLoader classLoader) {
        super(name, classLoader);
        addRequirements((Predicate<TestMethod>[]) new Predicate[]{TestPredicates.SELECT_SMOKE});
    }
}
