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

    public final boolean matches(Object item) {
        return item != null && this.expectedType.isInstance(item) && matchesSafely(item);
    }

    public final void describeMismatch(Object item, Description description) {
        if (item == null) {
            super.describeMismatch(null, description);
        } else if (!this.expectedType.isInstance(item)) {
            description.appendText("was a ").appendText(item.getClass().getName()).appendText(" (").appendValue(item).appendText(")");
        } else {
            describeMismatchSafely(item, description);
        }
    }
}
