package org.junit.internal.matchers;

import java.lang.Throwable;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ThrowableCauseMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
    private final Matcher<? extends Throwable> causeMatcher;

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: org.junit.internal.matchers.ThrowableCauseMatcher<T extends java.lang.Throwable> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeMatcher
    public /* bridge */ /* synthetic */ void describeMismatchSafely(Object obj, Description description) {
        describeMismatchSafely((ThrowableCauseMatcher<T>) ((Throwable) obj), description);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: org.junit.internal.matchers.ThrowableCauseMatcher<T extends java.lang.Throwable> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeMatcher
    public /* bridge */ /* synthetic */ boolean matchesSafely(Object obj) {
        return matchesSafely((ThrowableCauseMatcher<T>) ((Throwable) obj));
    }

    public ThrowableCauseMatcher(Matcher<? extends Throwable> causeMatcher2) {
        this.causeMatcher = causeMatcher2;
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("exception with cause ");
        description.appendDescriptionOf(this.causeMatcher);
    }

    /* access modifiers changed from: protected */
    public boolean matchesSafely(T item) {
        return this.causeMatcher.matches(item.getCause());
    }

    /* access modifiers changed from: protected */
    public void describeMismatchSafely(T item, Description description) {
        description.appendText("cause ");
        this.causeMatcher.describeMismatch(item.getCause(), description);
    }

    @Factory
    public static <T extends Throwable> Matcher<T> hasCause(Matcher<? extends Throwable> matcher) {
        return new ThrowableCauseMatcher(matcher);
    }
}
