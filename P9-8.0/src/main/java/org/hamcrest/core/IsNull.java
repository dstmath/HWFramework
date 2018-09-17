package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class IsNull<T> extends BaseMatcher<T> {
    public boolean matches(Object o) {
        return o == null;
    }

    public void describeTo(Description description) {
        description.appendText("null");
    }

    public static Matcher<Object> nullValue() {
        return new IsNull();
    }

    public static Matcher<Object> notNullValue() {
        return IsNot.not(nullValue());
    }

    public static <T> Matcher<T> nullValue(Class<T> cls) {
        return new IsNull();
    }

    public static <T> Matcher<T> notNullValue(Class<T> type) {
        return IsNot.not(nullValue(type));
    }
}
