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

    public boolean matches(Object actualValue) {
        return areEqual(actualValue, this.expectedValue);
    }

    public void describeTo(Description description) {
        description.appendValue(this.expectedValue);
    }

    private static boolean areEqual(Object actual, Object expected) {
        boolean z = false;
        if (actual == null) {
            if (expected == null) {
                z = true;
            }
            return z;
        } else if (expected == null || !isArray(actual)) {
            return actual.equals(expected);
        } else {
            if (isArray(expected)) {
                z = areArraysEqual(actual, expected);
            }
            return z;
        }
    }

    private static boolean areArraysEqual(Object actualArray, Object expectedArray) {
        return areArrayLengthsEqual(actualArray, expectedArray) ? areArrayElementsEqual(actualArray, expectedArray) : false;
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
