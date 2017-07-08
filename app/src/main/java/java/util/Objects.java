package java.util;

import java.util.function.Supplier;

public final class Objects {
    private Objects() {
        throw new AssertionError((Object) "No java.util.Objects instances for you!");
    }

    public static boolean equals(Object a, Object b) {
        if (a != b) {
            return a != null ? a.equals(b) : false;
        } else {
            return true;
        }
    }

    public static boolean deepEquals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Arrays.deepEquals0(a, b);
    }

    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    public static String toString(Object o) {
        return String.valueOf(o);
    }

    public static String toString(Object o, String nullDefault) {
        return o != null ? o.toString() : nullDefault;
    }

    public static <T> int compare(T a, T b, Comparator<? super T> c) {
        return a == b ? 0 : c.compare(a, b);
    }

    public static <T> T requireNonNull(T obj) {
        if (obj != null) {
            return obj;
        }
        throw new NullPointerException();
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj != null) {
            return obj;
        }
        throw new NullPointerException(message);
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static boolean nonNull(Object obj) {
        return obj != null;
    }

    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) {
        if (obj != null) {
            return obj;
        }
        throw new NullPointerException((String) messageSupplier.get());
    }
}
