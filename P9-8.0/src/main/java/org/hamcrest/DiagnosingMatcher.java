package org.hamcrest;

public abstract class DiagnosingMatcher<T> extends BaseMatcher<T> {
    protected abstract boolean matches(Object obj, Description description);

    public final boolean matches(Object item) {
        return matches(item, Description.NONE);
    }

    public final void describeMismatch(Object item, Description mismatchDescription) {
        matches(item, mismatchDescription);
    }
}
