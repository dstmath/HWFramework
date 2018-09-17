package junit.runner;

public interface TestSuiteLoader {
    Class load(String str) throws ClassNotFoundException;

    Class reload(Class cls) throws ClassNotFoundException;
}
