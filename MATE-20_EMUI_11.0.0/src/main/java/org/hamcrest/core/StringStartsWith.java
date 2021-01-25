package org.hamcrest.core;

import org.hamcrest.Matcher;

public class StringStartsWith extends SubstringMatcher {
    public StringStartsWith(boolean ignoringCase, String substring) {
        super("starting with", ignoringCase, substring);
    }

    /* access modifiers changed from: protected */
    @Override // org.hamcrest.core.SubstringMatcher
    public boolean evalSubstringOf(String s) {
        return converted(s).startsWith(converted(this.substring));
    }

    public static Matcher<String> startsWith(String prefix) {
        return new StringStartsWith(false, prefix);
    }

    public static Matcher<String> startsWithIgnoringCase(String prefix) {
        return new StringStartsWith(true, prefix);
    }
}
