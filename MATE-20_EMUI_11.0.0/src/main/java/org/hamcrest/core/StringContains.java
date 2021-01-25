package org.hamcrest.core;

import org.hamcrest.Matcher;

public class StringContains extends SubstringMatcher {
    public StringContains(boolean ignoringCase, String substring) {
        super("containing", ignoringCase, substring);
    }

    /* access modifiers changed from: protected */
    @Override // org.hamcrest.core.SubstringMatcher
    public boolean evalSubstringOf(String s) {
        return converted(s).contains(converted(this.substring));
    }

    public static Matcher<String> containsString(String substring) {
        return new StringContains(false, substring);
    }

    public static Matcher<String> containsStringIgnoringCase(String substring) {
        return new StringContains(true, substring);
    }
}
