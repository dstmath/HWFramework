package org.hamcrest;

import org.hamcrest.core.AllOf;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.CombinableMatcher;
import org.hamcrest.core.CombinableMatcher.CombinableBothMatcher;
import org.hamcrest.core.CombinableMatcher.CombinableEitherMatcher;
import org.hamcrest.core.DescribedAs;
import org.hamcrest.core.Every;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;

public class CoreMatchers {
    public static <T> Matcher<T> allOf(Iterable<Matcher<? super T>> matchers) {
        return AllOf.allOf((Iterable) matchers);
    }

    @SafeVarargs
    public static <T> Matcher<T> allOf(Matcher<? super T>... matchers) {
        return AllOf.allOf((Matcher[]) matchers);
    }

    public static <T> AnyOf<T> anyOf(Iterable<Matcher<? super T>> matchers) {
        return AnyOf.anyOf((Iterable) matchers);
    }

    @SafeVarargs
    public static <T> AnyOf<T> anyOf(Matcher<? super T>... matchers) {
        return AnyOf.anyOf((Matcher[]) matchers);
    }

    public static <LHS> CombinableBothMatcher<LHS> both(Matcher<? super LHS> matcher) {
        return CombinableMatcher.both(matcher);
    }

    public static <LHS> CombinableEitherMatcher<LHS> either(Matcher<? super LHS> matcher) {
        return CombinableMatcher.either(matcher);
    }

    public static <T> Matcher<T> describedAs(String description, Matcher<T> matcher, Object... values) {
        return DescribedAs.describedAs(description, matcher, values);
    }

    public static <U> Matcher<Iterable<? extends U>> everyItem(Matcher<U> itemMatcher) {
        return Every.everyItem(itemMatcher);
    }

    public static <T> Matcher<T> is(Matcher<T> matcher) {
        return Is.is((Matcher) matcher);
    }

    public static <T> Matcher<T> is(T value) {
        return Is.is((Object) value);
    }

    public static void is(Class<?> cls) {
    }

    public static <T> Matcher<T> isA(Class<T> type) {
        return Is.isA(type);
    }

    public static Matcher<Object> anything() {
        return IsAnything.anything();
    }

    public static Matcher<Object> anything(String description) {
        return IsAnything.anything(description);
    }

    public static <T> Matcher<Iterable<? super T>> hasItem(Matcher<? super T> itemMatcher) {
        return IsCollectionContaining.hasItem((Matcher) itemMatcher);
    }

    public static <T> Matcher<Iterable<? super T>> hasItem(T item) {
        return IsCollectionContaining.hasItem((Object) item);
    }

    @SafeVarargs
    public static <T> Matcher<Iterable<T>> hasItems(Matcher<? super T>... itemMatchers) {
        return IsCollectionContaining.hasItems((Matcher[]) itemMatchers);
    }

    @SafeVarargs
    public static <T> Matcher<Iterable<T>> hasItems(T... items) {
        return IsCollectionContaining.hasItems((Object[]) items);
    }

    public static <T> Matcher<T> equalTo(T operand) {
        return IsEqual.equalTo(operand);
    }

    public static Matcher<Object> equalToObject(Object operand) {
        return IsEqual.equalToObject(operand);
    }

    public static <T> Matcher<T> any(Class<T> type) {
        return IsInstanceOf.any(type);
    }

    public static <T> Matcher<T> instanceOf(Class<?> type) {
        return IsInstanceOf.instanceOf(type);
    }

    public static <T> Matcher<T> not(Matcher<T> matcher) {
        return IsNot.not((Matcher) matcher);
    }

    public static <T> Matcher<T> not(T value) {
        return IsNot.not((Object) value);
    }

    public static Matcher<Object> notNullValue() {
        return IsNull.notNullValue();
    }

    public static <T> Matcher<T> notNullValue(Class<T> type) {
        return IsNull.notNullValue(type);
    }

    public static Matcher<Object> nullValue() {
        return IsNull.nullValue();
    }

    public static <T> Matcher<T> nullValue(Class<T> type) {
        return IsNull.nullValue(type);
    }

    public static <T> Matcher<T> sameInstance(T target) {
        return IsSame.sameInstance(target);
    }

    public static <T> Matcher<T> theInstance(T target) {
        return IsSame.theInstance(target);
    }

    public static Matcher<String> containsString(String substring) {
        return StringContains.containsString(substring);
    }

    public static Matcher<String> containsStringIgnoringCase(String substring) {
        return StringContains.containsStringIgnoringCase(substring);
    }

    public static Matcher<String> startsWith(String prefix) {
        return StringStartsWith.startsWith(prefix);
    }

    public static Matcher<String> startsWithIgnoringCase(String prefix) {
        return StringStartsWith.startsWithIgnoringCase(prefix);
    }

    public static Matcher<String> endsWith(String suffix) {
        return StringEndsWith.endsWith(suffix);
    }

    public static Matcher<String> endsWithIgnoringCase(String suffix) {
        return StringEndsWith.endsWithIgnoringCase(suffix);
    }
}
