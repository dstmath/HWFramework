package org.junit;

import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

public class Assume {
    public static void assumeTrue(boolean b) {
        assumeThat(Boolean.valueOf(b), CoreMatchers.is(Boolean.valueOf(true)));
    }

    public static void assumeFalse(boolean b) {
        assumeTrue(b ^ 1);
    }

    public static void assumeTrue(String message, boolean b) {
        if (!b) {
            throw new AssumptionViolatedException(message);
        }
    }

    public static void assumeFalse(String message, boolean b) {
        assumeTrue(message, b ^ 1);
    }

    public static void assumeNotNull(Object... objects) {
        assumeThat(Arrays.asList(objects), CoreMatchers.everyItem(CoreMatchers.notNullValue()));
    }

    public static <T> void assumeThat(T actual, Matcher<T> matcher) {
        if (!matcher.matches(actual)) {
            throw new AssumptionViolatedException((Object) actual, (Matcher) matcher);
        }
    }

    public static <T> void assumeThat(String message, T actual, Matcher<T> matcher) {
        if (!matcher.matches(actual)) {
            throw new AssumptionViolatedException(message, actual, matcher);
        }
    }

    public static void assumeNoException(Throwable e) {
        assumeThat(e, CoreMatchers.nullValue());
    }

    public static void assumeNoException(String message, Throwable e) {
        assumeThat(message, e, CoreMatchers.nullValue());
    }
}
