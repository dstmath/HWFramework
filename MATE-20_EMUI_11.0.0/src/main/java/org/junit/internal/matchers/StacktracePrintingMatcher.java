package org.junit.internal.matchers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Throwable;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class StacktracePrintingMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
    private final Matcher<T> throwableMatcher;

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: org.junit.internal.matchers.StacktracePrintingMatcher<T extends java.lang.Throwable> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeMatcher
    public /* bridge */ /* synthetic */ void describeMismatchSafely(Object obj, Description description) {
        describeMismatchSafely((StacktracePrintingMatcher<T>) ((Throwable) obj), description);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: org.junit.internal.matchers.StacktracePrintingMatcher<T extends java.lang.Throwable> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeMatcher
    public /* bridge */ /* synthetic */ boolean matchesSafely(Object obj) {
        return matchesSafely((StacktracePrintingMatcher<T>) ((Throwable) obj));
    }

    public StacktracePrintingMatcher(Matcher<T> throwableMatcher2) {
        this.throwableMatcher = throwableMatcher2;
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        this.throwableMatcher.describeTo(description);
    }

    /* access modifiers changed from: protected */
    public boolean matchesSafely(T item) {
        return this.throwableMatcher.matches(item);
    }

    /* access modifiers changed from: protected */
    public void describeMismatchSafely(T item, Description description) {
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
    public static <T extends Throwable> Matcher<T> isThrowable(Matcher<T> throwableMatcher2) {
        return new StacktracePrintingMatcher(throwableMatcher2);
    }

    @Factory
    public static <T extends Exception> Matcher<T> isException(Matcher<T> exceptionMatcher) {
        return new StacktracePrintingMatcher(exceptionMatcher);
    }
}
