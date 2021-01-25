package com.android.uiautomator.testrunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

public class TestCaseCollector {
    private ClassLoader mClassLoader;
    private TestCaseFilter mFilter;
    private List<TestCase> mTestCases = new ArrayList();

    public interface TestCaseFilter {
        boolean accept(Class<?> cls);

        boolean accept(Method method);
    }

    public TestCaseCollector(ClassLoader classLoader, TestCaseFilter filter) {
        this.mClassLoader = classLoader;
        this.mFilter = filter;
    }

    public void addTestClasses(List<String> classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            addTestClass(className);
        }
    }

    public void addTestClass(String className) throws ClassNotFoundException {
        int hashPos = className.indexOf(35);
        String methodName = null;
        if (hashPos != -1) {
            methodName = className.substring(hashPos + 1);
            className = className.substring(0, hashPos);
        }
        addTestClass(className, methodName);
    }

    public void addTestClass(String className, String methodName) throws ClassNotFoundException {
        Class<?> clazz = this.mClassLoader.loadClass(className);
        if (methodName != null) {
            addSingleTestMethod(clazz, methodName);
            return;
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (this.mFilter.accept(method)) {
                addSingleTestMethod(clazz, method.getName());
            }
        }
    }

    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(this.mTestCases);
    }

    /* access modifiers changed from: protected */
    public void addSingleTestMethod(Class<?> clazz, String method) {
        if (this.mFilter.accept(clazz)) {
            try {
                TestCase testCase = (TestCase) clazz.newInstance();
                testCase.setName(method);
                this.mTestCases.add(testCase);
            } catch (InstantiationException e) {
                List<TestCase> list = this.mTestCases;
                list.add(error(clazz, "InstantiationException: could not instantiate test class. Class: " + clazz.getName()));
            } catch (IllegalAccessException e2) {
                List<TestCase> list2 = this.mTestCases;
                list2.add(error(clazz, "IllegalAccessException: could not instantiate test class. Class: " + clazz.getName()));
            }
        } else {
            throw new RuntimeException("Test class must be derived from UiAutomatorTestCase");
        }
    }

    private UiAutomatorTestCase error(Class<?> clazz, final String message) {
        UiAutomatorTestCase warning = new UiAutomatorTestCase() {
            /* class com.android.uiautomator.testrunner.TestCaseCollector.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // junit.framework.TestCase
            public void runTest() {
                fail(message);
            }
        };
        warning.setName(clazz.getName());
        return warning;
    }
}
