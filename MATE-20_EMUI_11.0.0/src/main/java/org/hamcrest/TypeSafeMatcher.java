package org.hamcrest;

import org.hamcrest.internal.ReflectiveTypeFinder;

public abstract class TypeSafeMatcher<T> extends BaseMatcher<T> {
    private static final ReflectiveTypeFinder TYPE_FINDER = new ReflectiveTypeFinder("matchesSafely", 1, 0);
    private final Class<?> expectedType;

    /* access modifiers changed from: protected */
    public abstract boolean matchesSafely(T t);

    protected TypeSafeMatcher() {
        this(TYPE_FINDER);
    }

    protected TypeSafeMatcher(Class<?> expectedType2) {
        this.expectedType = expectedType2;
    }

    protected TypeSafeMatcher(ReflectiveTypeFinder typeFinder) {
        this.expectedType = typeFinder.findExpectedType(getClass());
    }

    /* access modifiers changed from: protected */
    public void describeMismatchSafely(T item, Description mismatchDescription) {
        super.describeMismatch(item, mismatchDescription);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.hamcrest.Matcher
    public final boolean matches(Object item) {
        return item != 0 && this.expectedType.isInstance(item) && matchesSafely(item);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.hamcrest.BaseMatcher, org.hamcrest.Matcher
    public final void describeMismatch(Object item, Description description) {
        if (item == 0) {
            super.describeMismatch(null, description);
        } else if (!this.expectedType.isInstance(item)) {
            description.appendText("was a ").appendText(item.getClass().getName()).appendText(" (").appendValue(item).appendText(")");
        } else {
            describeMismatchSafely(item, description);
        }
    }
}
