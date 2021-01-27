package org.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;

public class IsInstanceOf extends DiagnosingMatcher<Object> {
    private final Class<?> expectedClass;
    private final Class<?> matchableClass;

    public IsInstanceOf(Class<?> expectedClass2) {
        this.expectedClass = expectedClass2;
        this.matchableClass = matchableClass(expectedClass2);
    }

    private static Class<?> matchableClass(Class<?> expectedClass2) {
        if (Boolean.TYPE.equals(expectedClass2)) {
            return Boolean.class;
        }
        if (Byte.TYPE.equals(expectedClass2)) {
            return Byte.class;
        }
        if (Character.TYPE.equals(expectedClass2)) {
            return Character.class;
        }
        if (Double.TYPE.equals(expectedClass2)) {
            return Double.class;
        }
        if (Float.TYPE.equals(expectedClass2)) {
            return Float.class;
        }
        if (Integer.TYPE.equals(expectedClass2)) {
            return Integer.class;
        }
        if (Long.TYPE.equals(expectedClass2)) {
            return Long.class;
        }
        if (Short.TYPE.equals(expectedClass2)) {
            return Short.class;
        }
        return expectedClass2;
    }

    /* access modifiers changed from: protected */
    @Override // org.hamcrest.DiagnosingMatcher
    public boolean matches(Object item, Description mismatch) {
        if (item == null) {
            mismatch.appendText("null");
            return false;
        } else if (this.matchableClass.isInstance(item)) {
            return true;
        } else {
            Description appendValue = mismatch.appendValue(item);
            appendValue.appendText(" is a " + item.getClass().getName());
            return false;
        }
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("an instance of ").appendText(this.expectedClass.getName());
    }

    public static <T> Matcher<T> instanceOf(Class<?> type) {
        return new IsInstanceOf(type);
    }

    public static <T> Matcher<T> any(Class<T> type) {
        return new IsInstanceOf(type);
    }
}
