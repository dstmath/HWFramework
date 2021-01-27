package org.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.internal.ReflectiveTypeFinder;

public abstract class TypeSafeDiagnosingMatcher<T> extends BaseMatcher<T> {
    private static final ReflectiveTypeFinder TYPE_FINDER = new ReflectiveTypeFinder("matchesSafely", 2, 0);
    private final Class<?> expectedType;

    /* access modifiers changed from: protected */
    public abstract boolean matchesSafely(T t, Description description);

    protected TypeSafeDiagnosingMatcher(Class<?> expectedType2) {
        this.expectedType = expectedType2;
    }

    protected TypeSafeDiagnosingMatcher(ReflectiveTypeFinder typeFinder) {
        this.expectedType = typeFinder.findExpectedType(getClass());
    }

    protected TypeSafeDiagnosingMatcher() {
        this(TYPE_FINDER);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.hamcrest.Matcher
    public final boolean matches(Object item) {
        return item != 0 && this.expectedType.isInstance(item) && matchesSafely(item, new Description.NullDescription());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.hamcrest.BaseMatcher, org.hamcrest.Matcher
    public final void describeMismatch(Object item, Description mismatchDescription) {
        if (item == 0 || !this.expectedType.isInstance(item)) {
            super.describeMismatch(item, mismatchDescription);
        } else {
            matchesSafely(item, mismatchDescription);
        }
    }
}
