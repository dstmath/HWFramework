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
            return getEnclosingClass().getMethod(getName(), new Class[0]).getAnnotation(annotationClass);
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
        int length = constructors.length;
        int i = 0;
        while (i < length) {
            Constructor constructor = constructors[i];
            Class[] params = constructor.getParameterTypes();
            if (noargsConstructor(params)) {
                TestCase test = (TestCase) constructor.newInstance(new Object[0]);
                test.setName(testName);
                return test;
            } else if (singleStringConstructor(params)) {
                return (TestCase) constructor.newInstance(new Object[]{testName});
            } else {
                i++;
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
        if (this.enclosingClassname == null ? that.enclosingClassname != null : !this.enclosingClassname.equals(that.enclosingClassname)) {
            return false;
        }
        if (this.testMethodName == null ? that.testMethodName == null : this.testMethodName.equals(that.testMethodName)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = 31 * (this.enclosingClassname != null ? this.enclosingClassname.hashCode() : 0);
        if (this.testMethodName != null) {
            i = this.testMethodName.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return this.enclosingClassname + "." + this.testMethodName;
    }
}
