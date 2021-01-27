package org.hamcrest;

public abstract class DiagnosingMatcher<T> extends BaseMatcher<T> {
    /* access modifiers changed from: protected */
    public abstract boolean matches(Object obj, Description description);

    @Override // org.hamcrest.Matcher
    public final boolean matches(Object item) {
        return matches(item, Description.NONE);
    }

    @Override // org.hamcrest.BaseMatcher, org.hamcrest.Matcher
    public final void describeMismatch(Object item, Description mismatchDescription) {
        matches(item, mismatchDescription);
    }
}
