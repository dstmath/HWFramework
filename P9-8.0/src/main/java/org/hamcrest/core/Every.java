package org.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class Every<T> extends TypeSafeDiagnosingMatcher<Iterable<? extends T>> {
    private final Matcher<? super T> matcher;

    public Every(Matcher<? super T> matcher) {
        this.matcher = matcher;
    }

    public boolean matchesSafely(Iterable<? extends T> collection, Description mismatchDescription) {
        for (T t : collection) {
            if (!this.matcher.matches(t)) {
                mismatchDescription.appendText("an item ");
                this.matcher.describeMismatch(t, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    public void describeTo(Description description) {
        description.appendText("every item is ").appendDescriptionOf(this.matcher);
    }

    public static <U> Matcher<Iterable<? extends U>> everyItem(Matcher<U> itemMatcher) {
        return new Every(itemMatcher);
    }
}
