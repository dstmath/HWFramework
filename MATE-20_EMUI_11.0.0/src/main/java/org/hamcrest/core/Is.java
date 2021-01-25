package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class Is<T> extends BaseMatcher<T> {
    private final Matcher<T> matcher;

    public Is(Matcher<T> matcher2) {
        this.matcher = matcher2;
    }

    @Override // org.hamcrest.Matcher
    public boolean matches(Object arg) {
        return this.matcher.matches(arg);
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("is ").appendDescriptionOf(this.matcher);
    }

    @Override // org.hamcrest.BaseMatcher, org.hamcrest.Matcher
    public void describeMismatch(Object item, Description mismatchDescription) {
        this.matcher.describeMismatch(item, mismatchDescription);
    }

    public static <T> Matcher<T> is(Matcher<T> matcher2) {
        return new Is(matcher2);
    }

    public static <T> Matcher<T> is(T value) {
        return is(IsEqual.equalTo(value));
    }

    public static <T> Matcher<T> isA(Class<T> type) {
        return is((Matcher) IsInstanceOf.instanceOf(type));
    }
}
