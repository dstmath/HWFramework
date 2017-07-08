package android.hardware.camera2.utils;

public final class HashCodeHelpers {
    public static int hashCode(int... array) {
        int i = 0;
        if (array == null) {
            return 0;
        }
        int h = 1;
        while (i < array.length) {
            h = ((h << 5) - h) ^ array[i];
            i++;
        }
        return h;
    }

    public static int hashCode(float... array) {
        int i = 0;
        if (array == null) {
            return 0;
        }
        int h = 1;
        while (i < array.length) {
            h = ((h << 5) - h) ^ Float.floatToIntBits(array[i]);
            i++;
        }
        return h;
    }

    public static <T> int hashCodeGeneric(T... array) {
        if (array == null) {
            return 0;
        }
        int h = 1;
        for (T o : array) {
            h = ((h << 5) - h) ^ (o == null ? 0 : o.hashCode());
        }
        return h;
    }
}
