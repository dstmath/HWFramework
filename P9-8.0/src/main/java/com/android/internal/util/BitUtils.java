package com.android.internal.util;

import java.util.Arrays;
import java.util.UUID;
import libcore.util.Objects;

public class BitUtils {
    private BitUtils() {
    }

    public static boolean maskedEquals(long a, long b, long mask) {
        return (a & mask) == (b & mask);
    }

    public static boolean maskedEquals(byte a, byte b, byte mask) {
        return (a & mask) == (b & mask);
    }

    public static boolean maskedEquals(byte[] a, byte[] b, byte[] mask) {
        boolean z = true;
        if (a == null || b == null) {
            if (a != b) {
                z = false;
            }
            return z;
        }
        boolean z2;
        if (a.length == b.length) {
            z2 = true;
        } else {
            z2 = false;
        }
        Preconditions.checkArgument(z2, "Inputs must be of same size");
        if (mask == null) {
            return Arrays.equals(a, b);
        }
        if (a.length == mask.length) {
            z2 = true;
        } else {
            z2 = false;
        }
        Preconditions.checkArgument(z2, "Mask must be of same size as inputs");
        for (int i = 0; i < mask.length; i++) {
            if (!maskedEquals(a[i], b[i], mask[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean maskedEquals(UUID a, UUID b, UUID mask) {
        if (mask == null) {
            return Objects.equal(a, b);
        }
        boolean maskedEquals;
        if (maskedEquals(a.getLeastSignificantBits(), b.getLeastSignificantBits(), mask.getLeastSignificantBits())) {
            maskedEquals = maskedEquals(a.getMostSignificantBits(), b.getMostSignificantBits(), mask.getMostSignificantBits());
        } else {
            maskedEquals = false;
        }
        return maskedEquals;
    }

    public static int[] unpackBits(long val) {
        int[] result = new int[Long.bitCount(val)];
        int index = 0;
        int bitPos = 0;
        while (true) {
            int index2 = index;
            if (val <= 0) {
                return result;
            }
            if ((val & 1) == 1) {
                index = index2 + 1;
                result[index2] = bitPos;
            } else {
                index = index2;
            }
            val >>= 1;
            bitPos++;
        }
    }

    public static long packBits(int[] bits) {
        long packed = 0;
        for (int b : bits) {
            packed |= (long) (1 << b);
        }
        return packed;
    }
}
