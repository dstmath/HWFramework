package org.hamcrest.core;

import org.hamcrest.Matcher;

public class StringStartsWith extends SubstringMatcher {
    public StringStartsWith(boolean ignoringCase, String substring) {
        super("starting with", ignoringCase, substring);
    }

    protected boolean evalSubstringOf(String s) {
        return converted(s).startsWith(converted(this.substring));
    }

    public static Matcher<String> startsWith(String prefix) {
        return new StringStartsWith(false, prefix);
    }

    public static Matcher<String> startsWithIgnoringCase(String prefix) {
        return new StringStartsWith(true, prefix);
    }
}
