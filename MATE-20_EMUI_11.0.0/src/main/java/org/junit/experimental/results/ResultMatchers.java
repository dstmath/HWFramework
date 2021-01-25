package org.junit.experimental.results;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ResultMatchers {
    public static Matcher<PrintableResult> isSuccessful() {
        return failureCountIs(0);
    }

    public static Matcher<PrintableResult> failureCountIs(final int count) {
        return new TypeSafeMatcher<PrintableResult>() {
            /* class org.junit.experimental.results.ResultMatchers.AnonymousClass1 */

            @Override // org.hamcrest.SelfDescribing
            public void describeTo(Description description) {
                description.appendText("has " + count + " failures");
            }

            public boolean matchesSafely(PrintableResult item) {
                return item.failureCount() == count;
            }
        };
    }

    public static Matcher<Object> hasSingleFailureContaining(final String string) {
        return new BaseMatcher<Object>() {
            /* class org.junit.experimental.results.ResultMatchers.AnonymousClass2 */

            @Override // org.hamcrest.Matcher
            public boolean matches(Object item) {
                return item.toString().contains(string) && ResultMatchers.failureCountIs(1).matches(item);
            }

            @Override // org.hamcrest.SelfDescribing
            public void describeTo(Description description) {
                description.appendText("has single failure containing " + string);
            }
        };
    }

    public static Matcher<PrintableResult> hasFailureContaining(final String string) {
        return new BaseMatcher<PrintableResult>() {
            /* class org.junit.experimental.results.ResultMatchers.AnonymousClass3 */

            @Override // org.hamcrest.Matcher
            public boolean matches(Object item) {
                return item.toString().contains(string);
            }

            @Override // org.hamcrest.SelfDescribing
            public void describeTo(Description description) {
                description.appendText("has failure containing " + string);
            }
        };
    }
}
