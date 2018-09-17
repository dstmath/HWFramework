package android.test.suitebuilder;

import com.android.internal.util.Predicate;

class AssignableFrom implements Predicate<TestMethod> {
    private final Class root;

    AssignableFrom(Class root) {
        this.root = root;
    }

    public boolean apply(TestMethod testMethod) {
        return this.root.isAssignableFrom(testMethod.getEnclosingClass());
    }
}
