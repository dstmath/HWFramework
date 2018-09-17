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

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0022 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x000c  */
    /* JADX WARNING: Missing block: B:14:0x0017, code:
            if (r1 != null) goto L_0x000a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                if (exception == null) {
                }
            }
        } catch (Throwable tearingDown2) {
            exception = tearingDown2;
        }
        if (exception == null) {
            throw exception;
        }
    }

    protected void runTest() throws Throwable {
        assertNotNull("TestCase.fName cannot be null", this.fName);
        Method runMethod = null;
        try {
            runMethod = getClass().getMethod(this.fName, (Class[]) null);
        } catch (NoSuchMethodException e) {
            fail("Method \"" + this.fName + "\" not found");
        }
        if (!Modifier.isPublic(runMethod.getModifiers())) {
            fail("Method \"" + this.fName + "\" should be public");
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

    public static void assertTrue(String message, boolean condition) {
        Assert.assertTrue(message, condition);
    }

    public static void assertTrue(boolean condition) {
        Assert.assertTrue(condition);
    }

    public static void assertFalse(String message, boolean condition) {
        Assert.assertFalse(message, condition);
    }

    public static void assertFalse(boolean condition) {
        Assert.assertFalse(condition);
    }

    public static void fail(String message) {
        Assert.fail(message);
    }

    public static void fail() {
        Assert.fail();
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(String message, String expected, String actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(String expected, String actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(String message, double expected, double actual, double delta) {
        Assert.assertEquals(message, expected, actual, delta);
    }

    public static void assertEquals(double expected, double actual, double delta) {
        Assert.assertEquals(expected, actual, delta);
    }

    public static void assertEquals(String message, float expected, float actual, float delta) {
        Assert.assertEquals(message, expected, actual, delta);
    }

    public static void assertEquals(float expected, float actual, float delta) {
        Assert.assertEquals(expected, actual, delta);
    }

    public static void assertEquals(String message, long expected, long actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(long expected, long actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(String message, boolean expected, boolean actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(boolean expected, boolean actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(String message, byte expected, byte actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(byte expected, byte actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(String message, char expected, char actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(char expected, char actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(String message, short expected, short actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(short expected, short actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(String message, int expected, int actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(int expected, int actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertNotNull(Object object) {
        Assert.assertNotNull(object);
    }

    public static void assertNotNull(String message, Object object) {
        Assert.assertNotNull(message, object);
    }

    public static void assertNull(Object object) {
        Assert.assertNull(object);
    }

    public static void assertNull(String message, Object object) {
        Assert.assertNull(message, object);
    }

    public static void assertSame(String message, Object expected, Object actual) {
        Assert.assertSame(message, expected, actual);
    }

    public static void assertSame(Object expected, Object actual) {
        Assert.assertSame(expected, actual);
    }

    public static void assertNotSame(String message, Object expected, Object actual) {
        Assert.assertNotSame(message, expected, actual);
    }

    public static void assertNotSame(Object expected, Object actual) {
        Assert.assertNotSame(expected, actual);
    }

    public static void failSame(String message) {
        Assert.failSame(message);
    }

    public static void failNotSame(String message, Object expected, Object actual) {
        Assert.failNotSame(message, expected, actual);
    }

    public static void failNotEquals(String message, Object expected, Object actual) {
        Assert.failNotEquals(message, expected, actual);
    }

    public static String format(String message, Object expected, Object actual) {
        return Assert.format(message, expected, actual);
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
