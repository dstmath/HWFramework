package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class Is<T> extends BaseMatcher<T> {
    private final Matcher<T> matcher;

    public Is(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    public boolean matches(Object arg) {
        return this.matcher.matches(arg);
    }

    public void describeTo(Description description) {
        description.appendText("is ").appendDescriptionOf(this.matcher);
    }

    public void describeMismatch(Object item, Description mismatchDescription) {
        this.matcher.describeMismatch(item, mismatchDescription);
    }

    public static <T> Matcher<T> is(Matcher<T> matcher) {
        return new Is(matcher);
    }

    public static <T> Matcher<T> is(T value) {
        return is(IsEqual.equalTo(value));
    }

    public static <T> Matcher<T> isA(Class<T> type) {
        return is(IsInstanceOf.instanceOf(type));
    }
}
