package org.hamcrest;

public abstract class BaseMatcher<T> implements Matcher<T> {
    @Deprecated
    public final void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
    }

    public void describeMismatch(Object item, Description description) {
        description.appendText("was ").appendValue(item);
    }

    public String toString() {
        return StringDescription.toString(this);
    }
}
