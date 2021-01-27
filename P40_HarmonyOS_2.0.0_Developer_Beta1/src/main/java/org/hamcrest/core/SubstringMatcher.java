package org.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public abstract class SubstringMatcher extends TypeSafeMatcher<String> {
    private final boolean ignoringCase;
    private final String relationship;
    protected final String substring;

    /* access modifiers changed from: protected */
    public abstract boolean evalSubstringOf(String str);

    protected SubstringMatcher(String relationship2, boolean ignoringCase2, String substring2) {
        this.relationship = relationship2;
        this.ignoringCase = ignoringCase2;
        this.substring = substring2;
    }

    public boolean matchesSafely(String item) {
        return evalSubstringOf(this.ignoringCase ? item.toLowerCase() : item);
    }

    public void describeMismatchSafely(String item, Description mismatchDescription) {
        mismatchDescription.appendText("was \"").appendText(item).appendText("\"");
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("a string ").appendText(this.relationship).appendText(" ").appendValue(this.substring);
        if (this.ignoringCase) {
            description.appendText(" ignoring case");
        }
    }

    /* access modifiers changed from: protected */
    public String converted(String arg) {
        return this.ignoringCase ? arg.toLowerCase() : arg;
    }
}
