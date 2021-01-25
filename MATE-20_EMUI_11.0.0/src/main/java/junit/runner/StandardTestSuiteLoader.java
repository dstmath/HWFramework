package junit.runner;

public class StandardTestSuiteLoader implements TestSuiteLoader {
    @Override // junit.runner.TestSuiteLoader
    public Class load(String suiteClassName) throws ClassNotFoundException {
        return Class.forName(suiteClassName);
    }

    @Override // junit.runner.TestSuiteLoader
    public Class reload(Class aClass) throws ClassNotFoundException {
        return aClass;
    }
}
