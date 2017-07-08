package android.test.suitebuilder;

import android.test.InstrumentationTestRunner;
import android.test.PackageInfoSources;
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

public class TestGrouping {
    private static final String LOG_TAG = "TestGrouping";
    public static final Comparator<Class<? extends TestCase>> SORT_BY_FULLY_QUALIFIED_NAME = null;
    public static final Comparator<Class<? extends TestCase>> SORT_BY_SIMPLE_NAME = null;
    private ClassLoader classLoader;
    protected String firstIncludedPackage;
    SortedSet<Class<? extends TestCase>> testCaseClasses;

    private static class SortByFullyQualifiedName implements Comparator<Class<? extends TestCase>>, Serializable {
        private SortByFullyQualifiedName() {
        }

        public int compare(Class<? extends TestCase> class1, Class<? extends TestCase> class2) {
            return class1.getName().compareTo(class2.getName());
        }
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

    private static class TestCasePredicate implements Predicate<Class<?>> {
        private TestCasePredicate() {
        }

        public boolean apply(Class aClass) {
            int modifiers = aClass.getModifiers();
            if (TestCase.class.isAssignableFrom(aClass) && Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)) {
                return hasValidConstructor(aClass);
            }
            return false;
        }

        private boolean hasValidConstructor(Class<?> aClass) {
            for (Constructor<? extends TestCase> constructor : aClass.getConstructors()) {
                if (Modifier.isPublic(constructor.getModifiers())) {
                    Class[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length == 0 || (parameterTypes.length == 1 && parameterTypes[0] == String.class)) {
                        return true;
                    }
                }
            }
            Log.i(TestGrouping.LOG_TAG, String.format("TestCase class %s is missing a public constructor with no parameters or a single String parameter - skipping", new Object[]{aClass.getName()}));
            return false;
        }
    }

    private static class TestMethodPredicate implements Predicate<Method> {
        private TestMethodPredicate() {
        }

        public boolean apply(Method method) {
            if (method.getParameterTypes().length == 0 && method.getName().startsWith(InstrumentationTestRunner.REPORT_KEY_NAME_TEST)) {
                return method.getReturnType().getSimpleName().equals("void");
            }
            return false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.test.suitebuilder.TestGrouping.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.test.suitebuilder.TestGrouping.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.test.suitebuilder.TestGrouping.<clinit>():void");
    }

    public TestGrouping(Comparator<Class<? extends TestCase>> comparator) {
        this.firstIncludedPackage = null;
        this.testCaseClasses = new TreeSet(comparator);
    }

    public List<TestMethod> getTests() {
        List<TestMethod> testMethods = new ArrayList();
        for (Class testCase : this.testCaseClasses) {
            for (Method testMethod : getTestMethods(testCase)) {
                testMethods.add(new TestMethod(testMethod, testCase));
            }
        }
        return testMethods;
    }

    protected List<Method> getTestMethods(Class<? extends TestCase> testCaseClass) {
        return select(Arrays.asList(testCaseClass.getMethods()), new TestMethodPredicate());
    }

    SortedSet<Class<? extends TestCase>> getTestCaseClasses() {
        return this.testCaseClasses;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestGrouping other = (TestGrouping) o;
        if (this.testCaseClasses.equals(other.testCaseClasses)) {
            return this.testCaseClasses.comparator().equals(other.testCaseClasses.comparator());
        }
        return false;
    }

    public int hashCode() {
        return this.testCaseClasses.hashCode();
    }

    public TestGrouping addPackagesRecursive(String... packageNames) {
        for (String packageName : packageNames) {
            List<Class<? extends TestCase>> addedClasses = testCaseClassesInPackage(packageName);
            if (addedClasses.isEmpty()) {
                Log.w(LOG_TAG, "Invalid Package: '" + packageName + "' could not be found or has no tests");
            }
            this.testCaseClasses.addAll(addedClasses);
            if (this.firstIncludedPackage == null) {
                this.firstIncludedPackage = packageName;
            }
        }
        return this;
    }

    public TestGrouping removePackagesRecursive(String... packageNames) {
        for (String packageName : packageNames) {
            this.testCaseClasses.removeAll(testCaseClassesInPackage(packageName));
        }
        return this;
    }

    public String getFirstIncludedPackage() {
        return this.firstIncludedPackage;
    }

    private List<Class<? extends TestCase>> testCaseClassesInPackage(String packageName) {
        return selectTestClasses(PackageInfoSources.forClassPath(this.classLoader).getPackageInfo(packageName).getTopLevelClassesRecursive());
    }

    private List<Class<? extends TestCase>> selectTestClasses(Set<Class<?>> allClasses) {
        List<Class<? extends TestCase>> testClasses = new ArrayList();
        for (Class<?> testClass : select(allClasses, new TestCasePredicate())) {
            testClasses.add(testClass);
        }
        return testClasses;
    }

    private <T> List<T> select(Collection<T> items, Predicate<T> predicate) {
        ArrayList<T> selectedItems = new ArrayList();
        for (T item : items) {
            if (predicate.apply(item)) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
