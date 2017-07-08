package junit.runner;

import java.lang.reflect.Modifier;
import junit.framework.Test;
import junit.framework.TestSuite;

public class LoadingTestCollector extends ClassPathTestCollector {
    TestCaseClassLoader fLoader;

    public LoadingTestCollector() {
        this.fLoader = new TestCaseClassLoader();
    }

    protected boolean isTestClass(String classFileName) {
        boolean z = false;
        try {
            if (classFileName.endsWith(".class")) {
                Class testClass = classFromFile(classFileName);
                if (testClass != null) {
                    z = isTestClass(testClass);
                }
                return z;
            }
        } catch (ClassNotFoundException e) {
        } catch (NoClassDefFoundError e2) {
        }
        return false;
    }

    Class classFromFile(String classFileName) throws ClassNotFoundException {
        String className = classNameFromFile(classFileName);
        if (this.fLoader.isExcluded(className)) {
            return null;
        }
        return this.fLoader.loadClass(className, false);
    }

    boolean isTestClass(Class testClass) {
        if (hasSuiteMethod(testClass)) {
            return true;
        }
        if (Test.class.isAssignableFrom(testClass) && Modifier.isPublic(testClass.getModifiers()) && hasPublicConstructor(testClass)) {
            return true;
        }
        return false;
    }

    boolean hasSuiteMethod(Class testClass) {
        try {
            testClass.getMethod(BaseTestRunner.SUITE_METHODNAME, new Class[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    boolean hasPublicConstructor(Class testClass) {
        try {
            TestSuite.getTestConstructor(testClass);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
