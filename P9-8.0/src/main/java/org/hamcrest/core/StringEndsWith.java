package org.hamcrest.core;

import org.hamcrest.Matcher;

public class StringEndsWith extends SubstringMatcher {
    public StringEndsWith(boolean ignoringCase, String substring) {
        super("ending with", ignoringCase, substring);
    }

    protected boolean evalSubstringOf(String s) {
        return converted(s).endsWith(converted(this.substring));
    }

    public static Matcher<String> endsWith(String suffix) {
        return new StringEndsWith(false, suffix);
    }

    public static Matcher<String> endsWithIgnoringCase(String suffix) {
        return new StringEndsWith(true, suffix);
    }
}
