package junit.runner;

public class SimpleTestCollector extends ClassPathTestCollector {
    protected boolean isTestClass(String classFileName) {
        if (!classFileName.endsWith(".class") || classFileName.indexOf(36) >= 0 || classFileName.indexOf("Test") <= 0) {
            return false;
        }
        return true;
    }
}
