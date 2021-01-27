package android.test.suitebuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.TestCase;

@Deprecated
public class TestMethod {
    private final Class<? extends TestCase> enclosingClass;
    private final String enclosingClassname;
    private final String testMethodName;

    public TestMethod(Method method, Class<? extends TestCase> enclosingClass2) {
        this(method.getName(), enclosingClass2);
    }

    public TestMethod(String methodName, Class<? extends TestCase> enclosingClass2) {
        this.enclosingClass = enclosingClass2;
        this.enclosingClassname = enclosingClass2.getName();
        this.testMethodName = methodName;
    }

    public TestMethod(TestCase testCase) {
        this(testCase.getName(), (Class<? extends TestCase>) testCase.getClass());
    }

    public String getName() {
        return this.testMethodName;
    }

    public String getEnclosingClassname() {
        return this.enclosingClassname;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        try {
            return (T) getEnclosingClass().getMethod(getName(), new Class[0]).getAnnotation(annotationClass);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public Class<? extends TestCase> getEnclosingClass() {
        return this.enclosingClass;
    }

    public TestCase createTest() throws InvocationTargetException, IllegalAccessException, InstantiationException {
        return instantiateTest(this.enclosingClass, this.testMethodName);
    }

    private TestCase instantiateTest(Class testCaseClass, String testName) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor[] constructors = testCaseClass.getConstructors();
        if (constructors.length == 0) {
            return instantiateTest(testCaseClass.getSuperclass(), testName);
        }
        for (Constructor constructor : constructors) {
            Class[] params = constructor.getParameterTypes();
            if (noargsConstructor(params)) {
                TestCase test = (TestCase) constructor.newInstance(new Object[0]);
                test.setName(testName);
                return test;
            } else if (singleStringConstructor(params)) {
                return (TestCase) constructor.newInstance(testName);
            }
        }
        throw new RuntimeException("Unable to locate a constructor for " + testCaseClass.getName());
    }

    private boolean singleStringConstructor(Class[] params) {
        return params.length == 1 && params[0].equals(String.class);
    }

    private boolean noargsConstructor(Class[] params) {
        return params.length == 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestMethod that = (TestMethod) o;
        String str = this.enclosingClassname;
        if (str == null ? that.enclosingClassname != null : !str.equals(that.enclosingClassname)) {
            return false;
        }
        String str2 = this.testMethodName;
        if (str2 == null ? that.testMethodName == null : str2.equals(that.testMethodName)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        String str = this.enclosingClassname;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.testMethodName;
        if (str2 != null) {
            i = str2.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return this.enclosingClassname + "." + this.testMethodName;
    }
}
