package org.junit.internal.matchers;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class StacktracePrintingMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
    private final Matcher<T> throwableMatcher;

    public StacktracePrintingMatcher(Matcher<T> throwableMatcher) {
        this.throwableMatcher = throwableMatcher;
    }

    public void describeTo(Description description) {
        this.throwableMatcher.describeTo(description);
    }

    protected boolean matchesSafely(T item) {
        return this.throwableMatcher.matches(item);
    }

    protected void describeMismatchSafely(T item, Description description) {
        this.throwableMatcher.describeMismatch(item, description);
        description.appendText("\nStacktrace was: ");
        description.appendText(readStacktrace(item));
    }

    private String readStacktrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    @Factory
    public static <T extends Throwable> Matcher<T> isThrowable(Matcher<T> throwableMatcher) {
        return new StacktracePrintingMatcher(throwableMatcher);
    }

    @Factory
    public static <T extends Exception> Matcher<T> isException(Matcher<T> exceptionMatcher) {
        return new StacktracePrintingMatcher(exceptionMatcher);
    }
}
