package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/* access modifiers changed from: package-private */
public abstract class ShortcutCombination<T> extends BaseMatcher<T> {
    private final Iterable<Matcher<? super T>> matchers;

    @Override // org.hamcrest.SelfDescribing
    public abstract void describeTo(Description description);

    @Override // org.hamcrest.Matcher
    public abstract boolean matches(Object obj);

    public ShortcutCombination(Iterable<Matcher<? super T>> matchers2) {
        this.matchers = matchers2;
    }

    /* access modifiers changed from: protected */
    public boolean matches(Object o, boolean shortcut) {
        for (Matcher<? super T> matcher : this.matchers) {
            if (matcher.matches(o) == shortcut) {
                return shortcut;
            }
        }
        return !shortcut;
    }

    public void describeTo(Description description, String operator) {
        description.appendList("(", " " + operator + " ", ")", this.matchers);
    }
}
