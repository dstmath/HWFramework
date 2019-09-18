package org.hamcrest;

public abstract class CustomMatcher<T> extends BaseMatcher<T> {
    private final String fixedDescription;

    public CustomMatcher(String description) {
        if (description != null) {
            this.fixedDescription = description;
            return;
        }
        throw new IllegalArgumentException("Description should be non null!");
    }

    public final void describeTo(Description description) {
        description.appendText(this.fixedDescription);
    }
}
