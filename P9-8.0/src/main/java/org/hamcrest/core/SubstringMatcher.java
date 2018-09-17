package org.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public abstract class SubstringMatcher extends TypeSafeMatcher<String> {
    private final boolean ignoringCase;
    private final String relationship;
    protected final String substring;

    protected abstract boolean evalSubstringOf(String str);

    protected SubstringMatcher(String relationship, boolean ignoringCase, String substring) {
        this.relationship = relationship;
        this.ignoringCase = ignoringCase;
        this.substring = substring;
    }

    public boolean matchesSafely(String item) {
        if (this.ignoringCase) {
            item = item.toLowerCase();
        }
        return evalSubstringOf(item);
    }

    public void describeMismatchSafely(String item, Description mismatchDescription) {
        mismatchDescription.appendText("was \"").appendText(item).appendText("\"");
    }

    public void describeTo(Description description) {
        description.appendText("a string ").appendText(this.relationship).appendText(" ").appendValue(this.substring);
        if (this.ignoringCase) {
            description.appendText(" ignoring case");
        }
    }

    protected String converted(String arg) {
        return this.ignoringCase ? arg.toLowerCase() : arg;
    }
}
