package junit.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class TestCase extends Assert implements Test {
    private String fName;

    public TestCase() {
        this.fName = null;
    }

    public TestCase(String name) {
        this.fName = name;
    }

    public int countTestCases() {
        return 1;
    }

    protected TestResult createResult() {
        return new TestResult();
    }

    public TestResult run() {
        TestResult result = createResult();
        run(result);
        return result;
    }

    public void run(TestResult result) {
        result.run(this);
    }

    public void runBare() throws Throwable {
        Throwable tearingDown;
        Throwable exception = null;
        setUp();
        try {
            runTest();
            try {
                tearDown();
            } catch (Throwable th) {
                tearingDown = th;
                exception = tearingDown;
                if (exception != null) {
                    throw exception;
                }
            }
        } catch (Throwable tearingDown2) {
            exception = tearingDown2;
        }
        if (exception != null) {
            throw exception;
        }
    }

    protected void runTest() throws Throwable {
        Assert.assertNotNull("TestCase.fName cannot be null", this.fName);
        Method runMethod = null;
        try {
            runMethod = getClass().getMethod(this.fName, (Class[]) null);
        } catch (NoSuchMethodException e) {
            Assert.fail("Method \"" + this.fName + "\" not found");
        }
        if (!Modifier.isPublic(runMethod.getModifiers())) {
            Assert.fail("Method \"" + this.fName + "\" should be public");
        }
        try {
            runMethod.invoke(this, new Object[0]);
        } catch (InvocationTargetException e2) {
            e2.fillInStackTrace();
            throw e2.getTargetException();
        } catch (IllegalAccessException e3) {
            e3.fillInStackTrace();
            throw e3;
        }
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public String toString() {
        return getName() + "(" + getClass().getName() + ")";
    }

    public String getName() {
        return this.fName;
    }

    public void setName(String name) {
        this.fName = name;
    }
}
