package android.test.suitebuilder;

public class InstrumentationTestSuiteBuilder extends TestSuiteBuilder {
    public InstrumentationTestSuiteBuilder(Class clazz) {
        this(clazz.getName(), clazz.getClassLoader());
    }

    public InstrumentationTestSuiteBuilder(String name, ClassLoader classLoader) {
        super(name, classLoader);
        addRequirements(TestPredicates.SELECT_INSTRUMENTATION);
    }
}
