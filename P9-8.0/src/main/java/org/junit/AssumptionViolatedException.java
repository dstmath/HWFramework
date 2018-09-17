package org.junit;

import org.hamcrest.Matcher;

public class AssumptionViolatedException extends org.junit.internal.AssumptionViolatedException {
    private static final long serialVersionUID = 1;

    public <T> AssumptionViolatedException(T actual, Matcher<T> matcher) {
        super((Object) actual, (Matcher) matcher);
    }

    public <T> AssumptionViolatedException(String message, T expected, Matcher<T> matcher) {
        super(message, expected, matcher);
    }

    public AssumptionViolatedException(String message) {
        super(message);
    }

    public AssumptionViolatedException(String assumption, Throwable t) {
        super(assumption, t);
    }
}
