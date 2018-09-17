package org.hamcrest;

public abstract class CustomMatcher<T> extends BaseMatcher<T> {
    private final String fixedDescription;

    public CustomMatcher(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description should be non null!");
        }
        this.fixedDescription = description;
    }

    public final void describeTo(Description description) {
        description.appendText(this.fixedDescription);
    }
}
