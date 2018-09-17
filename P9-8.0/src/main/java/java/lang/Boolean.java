package java.lang;

import java.io.Serializable;

public final class Boolean implements Serializable, Comparable<Boolean> {
    public static final Boolean FALSE = new Boolean(false);
    public static final Boolean TRUE = new Boolean(true);
    public static final Class<Boolean> TYPE = boolean[].class.getComponentType();
    private static final long serialVersionUID = -3665804199014368530L;
    private final boolean value;

    public Boolean(boolean value) {
        this.value = value;
    }

    public Boolean(String s) {
        this(parseBoolean(s));
    }

    public static boolean parseBoolean(String s) {
        return s != null ? s.equalsIgnoreCase("true") : false;
    }

    public boolean booleanValue() {
        return this.value;
    }

    public static Boolean valueOf(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static Boolean valueOf(String s) {
        return parseBoolean(s) ? TRUE : FALSE;
    }

    public static String toString(boolean b) {
        return b ? "true" : "false";
    }

    public String toString() {
        return this.value ? "true" : "false";
    }

    public int hashCode() {
        return hashCode(this.value);
    }

    public static int hashCode(boolean value) {
        return value ? 1231 : 1237;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Boolean)) {
            return false;
        }
        if (this.value == ((Boolean) obj).booleanValue()) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{Splitter: B:1:0x0001, ExcHandler: java.lang.IllegalArgumentException (e java.lang.IllegalArgumentException)} */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean getBoolean(String name) {
        boolean result = false;
        try {
            return parseBoolean(System.getProperty(name));
        } catch (IllegalArgumentException e) {
        }
    }

    public int compareTo(Boolean b) {
        return compare(this.value, b.value);
    }

    public static int compare(boolean x, boolean y) {
        if (x == y) {
            return 0;
        }
        return x ? 1 : -1;
    }

    public static boolean logicalAnd(boolean a, boolean b) {
        return a ? b : false;
    }

    public static boolean logicalOr(boolean a, boolean b) {
        return !a ? b : true;
    }

    public static boolean logicalXor(boolean a, boolean b) {
        return a ^ b;
    }
}
