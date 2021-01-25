package android.hardware.camera2.utils;

import android.annotation.UnsupportedAppUsage;

public final class HashCodeHelpers {
    @UnsupportedAppUsage
    public static int hashCode(int... array) {
        if (array == null) {
            return 0;
        }
        int h = 1;
        for (int x : array) {
            h = ((h << 5) - h) ^ x;
        }
        return h;
    }

    public static int hashCode(float... array) {
        if (array == null) {
            return 0;
        }
        int h = 1;
        for (float f : array) {
            h = ((h << 5) - h) ^ Float.floatToIntBits(f);
        }
        return h;
    }

    public static <T> int hashCodeGeneric(T... array) {
        if (array == null) {
            return 0;
        }
        int length = array.length;
        int h = 1;
        for (int h2 = 0; h2 < length; h2++) {
            T o = array[h2];
            h = ((h << 5) - h) ^ (o == null ? 0 : o.hashCode());
        }
        return h;
    }
}
