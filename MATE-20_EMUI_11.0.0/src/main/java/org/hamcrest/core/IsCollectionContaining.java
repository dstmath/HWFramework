package org.hamcrest.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsCollectionContaining<T> extends TypeSafeDiagnosingMatcher<Iterable<? super T>> {
    private final Matcher<? super T> elementMatcher;

    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeDiagnosingMatcher
    public /* bridge */ /* synthetic */ boolean matchesSafely(Object obj, Description description) {
        return matchesSafely((Iterable) ((Iterable) obj), description);
    }

    public IsCollectionContaining(Matcher<? super T> elementMatcher2) {
        this.elementMatcher = elementMatcher2;
    }

    /* access modifiers changed from: protected */
    public boolean matchesSafely(Iterable<? super T> collection, Description mismatchDescription) {
        if (isEmpty(collection)) {
            mismatchDescription.appendText("was empty");
            return false;
        }
        Iterator<? super T> it = collection.iterator();
        while (it.hasNext()) {
            if (this.elementMatcher.matches(it.next())) {
                return true;
            }
        }
        mismatchDescription.appendText("mismatches were: [");
        boolean isPastFirst = false;
        for (Object item : collection) {
            if (isPastFirst) {
                mismatchDescription.appendText(", ");
            }
            this.elementMatcher.describeMismatch(item, mismatchDescription);
            isPastFirst = true;
        }
        mismatchDescription.appendText("]");
        return false;
    }

    private boolean isEmpty(Iterable<? super T> iterable) {
        return !iterable.iterator().hasNext();
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("a collection containing ").appendDescriptionOf(this.elementMatcher);
    }

    public static <T> Matcher<Iterable<? super T>> hasItem(Matcher<? super T> itemMatcher) {
        return new IsCollectionContaining(itemMatcher);
    }

    public static <T> Matcher<Iterable<? super T>> hasItem(T item) {
        return new IsCollectionContaining(IsEqual.equalTo(item));
    }

    @SafeVarargs
    public static <T> Matcher<Iterable<T>> hasItems(Matcher<? super T>... itemMatchers) {
        List<Matcher<? super Iterable<T>>> all = new ArrayList<>(itemMatchers.length);
        for (Matcher<? super T> elementMatcher2 : itemMatchers) {
            all.add(new IsCollectionContaining<>(elementMatcher2));
        }
        return AllOf.allOf(all);
    }

    @SafeVarargs
    public static <T> Matcher<Iterable<T>> hasItems(T... items) {
        List<Matcher<? super Iterable<T>>> all = new ArrayList<>(items.length);
        for (T item : items) {
            all.add(hasItem((Object) item));
        }
        return AllOf.allOf(all);
    }
}
