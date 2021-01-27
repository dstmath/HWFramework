package android.test.suitebuilder;

import android.test.ClassPathPackageInfoSource;
import android.test.InstrumentationTestRunner;
import android.util.Log;
import com.android.internal.util.Predicate;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import junit.framework.TestCase;

/* access modifiers changed from: package-private */
public class TestGrouping {
    private static final String LOG_TAG = "TestGrouping";
    static final Comparator<Class<? extends TestCase>> SORT_BY_FULLY_QUALIFIED_NAME = new SortByFullyQualifiedName();
    static final Comparator<Class<? extends TestCase>> SORT_BY_SIMPLE_NAME = new SortBySimpleName();
    private final ClassLoader classLoader;
    private final SortedSet<Class<? extends TestCase>> testCaseClasses;

    TestGrouping(Comparator<Class<? extends TestCase>> comparator, ClassLoader classLoader2) {
        this.testCaseClasses = new TreeSet(comparator);
        this.classLoader = classLoader2;
    }

    public List<TestMethod> getTests() {
        List<TestMethod> testMethods = new ArrayList<>();
        for (Class<? extends TestCase> testCase : this.testCaseClasses) {
            for (Method testMethod : getTestMethods(testCase)) {
                testMethods.add(new TestMethod(testMethod, testCase));
            }
        }
        return testMethods;
    }

    private List<Method> getTestMethods(Class<? extends TestCase> testCaseClass) {
        return select(Arrays.asList(testCaseClass.getMethods()), new TestMethodPredicate());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestGrouping other = (TestGrouping) o;
        if (!this.testCaseClasses.equals(other.testCaseClasses)) {
            return false;
        }
        return this.testCaseClasses.comparator().equals(other.testCaseClasses.comparator());
    }

    public int hashCode() {
        return this.testCaseClasses.hashCode();
    }

    /* access modifiers changed from: package-private */
    public void addPackagesRecursive(String... packageNames) {
        for (String packageName : packageNames) {
            List<Class<? extends TestCase>> addedClasses = testCaseClassesInPackage(packageName);
            if (addedClasses.isEmpty()) {
                Log.w(LOG_TAG, "Invalid Package: '" + packageName + "' could not be found or has no tests");
            }
            this.testCaseClasses.addAll(addedClasses);
        }
    }

    /* access modifiers changed from: package-private */
    public void removePackagesRecursive(String... packageNames) {
        for (String packageName : packageNames) {
            this.testCaseClasses.removeAll(testCaseClassesInPackage(packageName));
        }
    }

    private List<Class<? extends TestCase>> testCaseClassesInPackage(String packageName) {
        return selectTestClasses(ClassPathPackageInfoSource.forClassPath(this.classLoader).getTopLevelClassesRecursive(packageName));
    }

    private List<Class<? extends TestCase>> selectTestClasses(Set<Class<?>> allClasses) {
        List<Class<? extends TestCase>> testClasses = new ArrayList<>();
        for (Class<? extends TestCase> cls : select(allClasses, new TestCasePredicate())) {
            testClasses.add(cls);
        }
        return testClasses;
    }

    private <T> List<T> select(Collection<T> items, Predicate<T> predicate) {
        ArrayList<T> selectedItems = new ArrayList<>();
        for (T item : items) {
            if (predicate.apply(item)) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    private static class SortBySimpleName implements Comparator<Class<? extends TestCase>>, Serializable {
        private SortBySimpleName() {
        }

        public int compare(Class<? extends TestCase> class1, Class<? extends TestCase> class2) {
            int result = class1.getSimpleName().compareTo(class2.getSimpleName());
            if (result != 0) {
                return result;
            }
            return class1.getName().compareTo(class2.getName());
        }
    }

    private static class SortByFullyQualifiedName implements Comparator<Class<? extends TestCase>>, Serializable {
        private SortByFullyQualifiedName() {
        }

        public int compare(Class<? extends TestCase> class1, Class<? extends TestCase> class2) {
            return class1.getName().compareTo(class2.getName());
        }
    }

    /* access modifiers changed from: private */
    public static class TestCasePredicate implements Predicate<Class<?>> {
        private TestCasePredicate() {
        }

        public boolean apply(Class aClass) {
            int modifiers = aClass.getModifiers();
            return TestCase.class.isAssignableFrom(aClass) && Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && hasValidConstructor(aClass);
        }

        private boolean hasValidConstructor(Class<?> aClass) {
            Constructor<?>[] constructors = aClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (Modifier.isPublic(constructor.getModifiers())) {
                    Class[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length == 0 || (parameterTypes.length == 1 && parameterTypes[0] == String.class)) {
                        return true;
                    }
                }
            }
            Log.i(TestGrouping.LOG_TAG, String.format("TestCase class %s is missing a public constructor with no parameters or a single String parameter - skipping", aClass.getName()));
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class TestMethodPredicate implements Predicate<Method> {
        private TestMethodPredicate() {
        }

        public boolean apply(Method method) {
            return method.getParameterTypes().length == 0 && method.getName().startsWith(InstrumentationTestRunner.REPORT_KEY_NAME_TEST) && method.getReturnType().getSimpleName().equals("void");
        }
    }
}
