package android.test.suitebuilder;

public class UnitTestSuiteBuilder extends TestSuiteBuilder {
    public UnitTestSuiteBuilder(Class clazz) {
        this(clazz.getName(), clazz.getClassLoader());
    }

    public UnitTestSuiteBuilder(String name, ClassLoader classLoader) {
        super(name, classLoader);
        addRequirements(TestPredicates.REJECT_INSTRUMENTATION);
    }
}
