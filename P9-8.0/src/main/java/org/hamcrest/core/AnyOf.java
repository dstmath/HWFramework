package org.hamcrest.core;

import java.util.Arrays;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class AnyOf<T> extends ShortcutCombination<T> {
    public AnyOf(Iterable<Matcher<? super T>> matchers) {
        super(matchers);
    }

    public boolean matches(Object o) {
        return matches(o, true);
    }

    public void describeTo(Description description) {
        describeTo(description, "or");
    }

    public static <T> AnyOf<T> anyOf(Iterable<Matcher<? super T>> matchers) {
        return new AnyOf(matchers);
    }

    @SafeVarargs
    public static <T> AnyOf<T> anyOf(Matcher<? super T>... matchers) {
        return anyOf(Arrays.asList(matchers));
    }
}
