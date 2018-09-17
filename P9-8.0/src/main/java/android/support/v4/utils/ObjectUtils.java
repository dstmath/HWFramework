package android.support.v4.utils;

public class ObjectUtils {
    public static boolean objectEquals(Object a, Object b) {
        if (a != b) {
            return a != null ? a.equals(b) : false;
        } else {
            return true;
        }
    }

    private ObjectUtils() {
    }
}
