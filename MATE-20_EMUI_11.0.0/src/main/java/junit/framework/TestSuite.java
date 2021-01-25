package junit.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.junit.internal.MethodSorter;

public class TestSuite implements Test {
    private String fName;
    private Vector<Test> fTests;

    public static Test createTest(Class<?> theClass, String name) {
        Object test;
        try {
            Constructor<?> constructor = getTestConstructor(theClass);
            try {
                if (constructor.getParameterTypes().length == 0) {
                    test = constructor.newInstance(new Object[0]);
                    if (test instanceof TestCase) {
                        ((TestCase) test).setName(name);
                    }
                } else {
                    test = constructor.newInstance(name);
                }
                return (Test) test;
            } catch (InstantiationException e) {
                return warning("Cannot instantiate test case: " + name + " (" + exceptionToString(e) + ")");
            } catch (InvocationTargetException e2) {
                return warning("Exception in constructor: " + name + " (" + exceptionToString(e2.getTargetException()) + ")");
            } catch (IllegalAccessException e3) {
                return warning("Cannot access test case: " + name + " (" + exceptionToString(e3) + ")");
            }
        } catch (NoSuchMethodException e4) {
            return warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()");
        }
    }

    public static Constructor<?> getTestConstructor(Class<?> theClass) throws NoSuchMethodException {
        try {
            return theClass.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            return theClass.getConstructor(new Class[0]);
        }
    }

    public static Test warning(final String message) {
        return new TestCase("warning") {
            /* class junit.framework.TestSuite.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // junit.framework.TestCase
            public void runTest() {
                fail(message);
            }
        };
    }

    private static String exceptionToString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    public TestSuite() {
        this.fTests = new Vector<>(10);
    }

    public TestSuite(Class<?> theClass) {
        this.fTests = new Vector<>(10);
        addTestsFromTestCase(theClass);
    }

    private void addTestsFromTestCase(Class<?> theClass) {
        this.fName = theClass.getName();
        try {
            getTestConstructor(theClass);
            if (!Modifier.isPublic(theClass.getModifiers())) {
                addTest(warning("Class " + theClass.getName() + " is not public"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (Class<?> superClass = theClass; Test.class.isAssignableFrom(superClass); superClass = superClass.getSuperclass()) {
                for (Method each : MethodSorter.getDeclaredMethods(superClass)) {
                    addTestMethod(each, names, theClass);
                }
            }
            if (this.fTests.size() == 0) {
                addTest(warning("No tests found in " + theClass.getName()));
            }
        } catch (NoSuchMethodException e) {
            addTest(warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()"));
        }
    }

    public TestSuite(Class<? extends TestCase> theClass, String name) {
        this(theClass);
        setName(name);
    }

    public TestSuite(String name) {
        this.fTests = new Vector<>(10);
        setName(name);
    }

    public TestSuite(Class<?>... classes) {
        this.fTests = new Vector<>(10);
        for (Class<?> each : classes) {
            addTest(testCaseForClass(each));
        }
    }

    private Test testCaseForClass(Class<?> each) {
        if (TestCase.class.isAssignableFrom(each)) {
            return new TestSuite(each.asSubclass(TestCase.class));
        }
        return warning(each.getCanonicalName() + " does not extend TestCase");
    }

    public TestSuite(Class<? extends TestCase>[] classes, String name) {
        this(classes);
        setName(name);
    }

    public void addTest(Test test) {
        this.fTests.add(test);
    }

    public void addTestSuite(Class<? extends TestCase> testClass) {
        addTest(new TestSuite(testClass));
    }

    @Override // junit.framework.Test
    public int countTestCases() {
        int count = 0;
        Iterator<Test> it = this.fTests.iterator();
        while (it.hasNext()) {
            count += it.next().countTestCases();
        }
        return count;
    }

    public String getName() {
        return this.fName;
    }

    @Override // junit.framework.Test
    public void run(TestResult result) {
        Iterator<Test> it = this.fTests.iterator();
        while (it.hasNext()) {
            Test each = it.next();
            if (!result.shouldStop()) {
                runTest(each, result);
            } else {
                return;
            }
        }
    }

    public void runTest(Test test, TestResult result) {
        test.run(result);
    }

    public void setName(String name) {
        this.fName = name;
    }

    public Test testAt(int index) {
        return this.fTests.get(index);
    }

    public int testCount() {
        return this.fTests.size();
    }

    public Enumeration<Test> tests() {
        return this.fTests.elements();
    }

    public String toString() {
        if (getName() != null) {
            return getName();
        }
        return super.toString();
    }

    private void addTestMethod(Method m, List<String> names, Class<?> theClass) {
        String name = m.getName();
        if (!names.contains(name)) {
            if (isPublicTestMethod(m)) {
                names.add(name);
                addTest(createTest(theClass, name));
            } else if (isTestMethod(m)) {
                addTest(warning("Test method isn't public: " + m.getName() + "(" + theClass.getCanonicalName() + ")"));
            }
        }
    }

    private boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

    private boolean isTestMethod(Method m) {
        return m.getParameterTypes().length == 0 && m.getName().startsWith("test") && m.getReturnType().equals(Void.TYPE);
    }
}
