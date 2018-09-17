package org.junit.matchers;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.CombinableMatcher.CombinableBothMatcher;
import org.hamcrest.core.CombinableMatcher.CombinableEitherMatcher;
import org.junit.internal.matchers.StacktracePrintingMatcher;

public class JUnitMatchers {
    @Deprecated
    public static <T> Matcher<Iterable<? super T>> hasItem(T element) {
        return CoreMatchers.hasItem((Object) element);
    }

    @Deprecated
    public static <T> Matcher<Iterable<? super T>> hasItem(Matcher<? super T> elementMatcher) {
        return CoreMatchers.hasItem((Matcher) elementMatcher);
    }

    @Deprecated
    public static <T> Matcher<Iterable<T>> hasItems(T... elements) {
        return CoreMatchers.hasItems((Object[]) elements);
    }

    @Deprecated
    public static <T> Matcher<Iterable<T>> hasItems(Matcher<? super T>... elementMatchers) {
        return CoreMatchers.hasItems((Matcher[]) elementMatchers);
    }

    @Deprecated
    public static <T> Matcher<Iterable<T>> everyItem(Matcher<T> elementMatcher) {
        return CoreMatchers.everyItem(elementMatcher);
    }

    @Deprecated
    public static Matcher<String> containsString(String substring) {
        return CoreMatchers.containsString(substring);
    }

    @Deprecated
    public static <T> CombinableBothMatcher<T> both(Matcher<? super T> matcher) {
        return CoreMatchers.both(matcher);
    }

    @Deprecated
    public static <T> CombinableEitherMatcher<T> either(Matcher<? super T> matcher) {
        return CoreMatchers.either(matcher);
    }

    public static <T extends Throwable> Matcher<T> isThrowable(Matcher<T> throwableMatcher) {
        return StacktracePrintingMatcher.isThrowable(throwableMatcher);
    }

    public static <T extends Exception> Matcher<T> isException(Matcher<T> exceptionMatcher) {
        return StacktracePrintingMatcher.isException(exceptionMatcher);
    }
}
