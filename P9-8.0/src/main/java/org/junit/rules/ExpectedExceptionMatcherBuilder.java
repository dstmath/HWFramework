package org.junit.rules;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.matchers.JUnitMatchers;

class ExpectedExceptionMatcherBuilder {
    private final List<Matcher<?>> matchers = new ArrayList();

    ExpectedExceptionMatcherBuilder() {
    }

    void add(Matcher<?> matcher) {
        this.matchers.add(matcher);
    }

    boolean expectsThrowable() {
        return this.matchers.isEmpty() ^ 1;
    }

    Matcher<Throwable> build() {
        return JUnitMatchers.isThrowable(allOfTheMatchers());
    }

    private Matcher<Throwable> allOfTheMatchers() {
        if (this.matchers.size() == 1) {
            return cast((Matcher) this.matchers.get(0));
        }
        return CoreMatchers.allOf(castedMatchers());
    }

    private List<Matcher<? super Throwable>> castedMatchers() {
        return new ArrayList(this.matchers);
    }

    private Matcher<Throwable> cast(Matcher<?> singleMatcher) {
        return singleMatcher;
    }
}
