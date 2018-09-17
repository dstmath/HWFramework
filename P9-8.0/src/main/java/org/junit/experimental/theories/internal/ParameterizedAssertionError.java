package org.junit.experimental.theories.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class ParameterizedAssertionError extends AssertionError {
    private static final long serialVersionUID = 1;

    public ParameterizedAssertionError(Throwable targetException, String methodName, Object... params) {
        super(String.format("%s(%s)", new Object[]{methodName, join(", ", params)}));
        initCause(targetException);
    }

    public boolean equals(Object obj) {
        return obj instanceof ParameterizedAssertionError ? toString().equals(obj.toString()) : false;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public static String join(String delimiter, Object... params) {
        return join(delimiter, Arrays.asList(params));
    }

    public static String join(String delimiter, Collection<Object> values) {
        StringBuilder sb = new StringBuilder();
        Iterator<Object> iter = values.iterator();
        while (iter.hasNext()) {
            sb.append(stringValueOf(iter.next()));
            if (iter.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    private static String stringValueOf(Object next) {
        try {
            return String.valueOf(next);
        } catch (Throwable th) {
            return "[toString failed]";
        }
    }
}
