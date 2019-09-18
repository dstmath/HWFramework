package android.test.suitebuilder;

import com.android.internal.util.Predicate;

public class UnitTestSuiteBuilder extends TestSuiteBuilder {
    public UnitTestSuiteBuilder(Class clazz) {
        this(clazz.getName(), clazz.getClassLoader());
    }

    public UnitTestSuiteBuilder(String name, ClassLoader classLoader) {
        super(name, classLoader);
        addRequirements((Predicate<TestMethod>[]) new Predicate[]{TestPredicates.REJECT_INSTRUMENTATION});
    }
}
