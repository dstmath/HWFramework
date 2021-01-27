package org.hamcrest;

import org.hamcrest.internal.ReflectiveTypeFinder;

public abstract class FeatureMatcher<T, U> extends TypeSafeDiagnosingMatcher<T> {
    private static final ReflectiveTypeFinder TYPE_FINDER = new ReflectiveTypeFinder("featureValueOf", 1, 0);
    private final String featureDescription;
    private final String featureName;
    private final Matcher<? super U> subMatcher;

    /* access modifiers changed from: protected */
    public abstract U featureValueOf(T t);

    public FeatureMatcher(Matcher<? super U> subMatcher2, String featureDescription2, String featureName2) {
        super(TYPE_FINDER);
        this.subMatcher = subMatcher2;
        this.featureDescription = featureDescription2;
        this.featureName = featureName2;
    }

    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeDiagnosingMatcher
    public boolean matchesSafely(T actual, Description mismatch) {
        U featureValue = featureValueOf(actual);
        if (this.subMatcher.matches(featureValue)) {
            return true;
        }
        mismatch.appendText(this.featureName).appendText(" ");
        this.subMatcher.describeMismatch(featureValue, mismatch);
        return false;
    }

    @Override // org.hamcrest.SelfDescribing
    public final void describeTo(Description description) {
        description.appendText(this.featureDescription).appendText(" ").appendDescriptionOf(this.subMatcher);
    }
}
