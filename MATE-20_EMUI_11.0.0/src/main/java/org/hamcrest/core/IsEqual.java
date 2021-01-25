package org.hamcrest.core;

import java.lang.reflect.Array;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class IsEqual<T> extends BaseMatcher<T> {
    private final Object expectedValue;

    public IsEqual(T equalArg) {
        this.expectedValue = equalArg;
    }

    @Override // org.hamcrest.Matcher
    public boolean matches(Object actualValue) {
        return areEqual(actualValue, this.expectedValue);
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendValue(this.expectedValue);
    }

    private static boolean areEqual(Object actual, Object expected) {
        if (actual == null) {
            return expected == null;
        }
        if (expected == null || !isArray(actual)) {
            return actual.equals(expected);
        }
        return isArray(expected) && areArraysEqual(actual, expected);
    }

    private static boolean areArraysEqual(Object actualArray, Object expectedArray) {
        return areArrayLengthsEqual(actualArray, expectedArray) && areArrayElementsEqual(actualArray, expectedArray);
    }

    private static boolean areArrayLengthsEqual(Object actualArray, Object expectedArray) {
        return Array.getLength(actualArray) == Array.getLength(expectedArray);
    }

    private static boolean areArrayElementsEqual(Object actualArray, Object expectedArray) {
        for (int i = 0; i < Array.getLength(actualArray); i++) {
            if (!areEqual(Array.get(actualArray, i), Array.get(expectedArray, i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isArray(Object o) {
        return o.getClass().isArray();
    }

    public static <T> Matcher<T> equalTo(T operand) {
        return new IsEqual(operand);
    }

    public static Matcher<Object> equalToObject(Object operand) {
        return new IsEqual(operand);
    }
}
