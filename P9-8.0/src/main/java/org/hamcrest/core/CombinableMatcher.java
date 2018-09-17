package org.hamcrest.core;

import java.util.ArrayList;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class CombinableMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
    private final Matcher<? super T> matcher;

    public static final class CombinableBothMatcher<X> {
        private final Matcher<? super X> first;

        public CombinableBothMatcher(Matcher<? super X> matcher) {
            this.first = matcher;
        }

        public CombinableMatcher<X> and(Matcher<? super X> other) {
            return new CombinableMatcher(this.first).and(other);
        }
    }

    public static final class CombinableEitherMatcher<X> {
        private final Matcher<? super X> first;

        public CombinableEitherMatcher(Matcher<? super X> matcher) {
            this.first = matcher;
        }

        public CombinableMatcher<X> or(Matcher<? super X> other) {
            return new CombinableMatcher(this.first).or(other);
        }
    }

    public CombinableMatcher(Matcher<? super T> matcher) {
        this.matcher = matcher;
    }

    protected boolean matchesSafely(T item, Description mismatch) {
        if (this.matcher.matches(item)) {
            return true;
        }
        this.matcher.describeMismatch(item, mismatch);
        return false;
    }

    public void describeTo(Description description) {
        description.appendDescriptionOf(this.matcher);
    }

    public CombinableMatcher<T> and(Matcher<? super T> other) {
        return new CombinableMatcher(new AllOf(templatedListWith(other)));
    }

    public CombinableMatcher<T> or(Matcher<? super T> other) {
        return new CombinableMatcher(new AnyOf(templatedListWith(other)));
    }

    private ArrayList<Matcher<? super T>> templatedListWith(Matcher<? super T> other) {
        ArrayList<Matcher<? super T>> matchers = new ArrayList();
        matchers.add(this.matcher);
        matchers.add(other);
        return matchers;
    }

    public static <LHS> CombinableBothMatcher<LHS> both(Matcher<? super LHS> matcher) {
        return new CombinableBothMatcher(matcher);
    }

    public static <LHS> CombinableEitherMatcher<LHS> either(Matcher<? super LHS> matcher) {
        return new CombinableEitherMatcher(matcher);
    }
}
