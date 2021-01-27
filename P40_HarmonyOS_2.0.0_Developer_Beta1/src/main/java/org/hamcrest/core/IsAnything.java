package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class IsAnything<T> extends BaseMatcher<T> {
    private final String message;

    public IsAnything() {
        this("ANYTHING");
    }

    public IsAnything(String message2) {
        this.message = message2;
    }

    @Override // org.hamcrest.Matcher
    public boolean matches(Object o) {
        return true;
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText(this.message);
    }

    public static Matcher<Object> anything() {
        return new IsAnything();
    }

    public static Matcher<Object> anything(String description) {
        return new IsAnything(description);
    }
}
