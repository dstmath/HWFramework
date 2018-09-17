package com.android.org.bouncycastle.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class BigIntegers {
    private static final int MAX_ITERATIONS = 1000;
    private static final BigInteger ZERO = BigInteger.valueOf(0);

    public static byte[] asUnsignedByteArray(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes[0] != (byte) 0) {
            return bytes;
        }
        byte[] tmp = new byte[(bytes.length - 1)];
        System.arraycopy(bytes, 1, tmp, 0, tmp.length);
        return tmp;
    }

    public static byte[] asUnsignedByteArray(int length, BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }
        int start = bytes[0] == (byte) 0 ? 1 : 0;
        int count = bytes.length - start;
        if (count > length) {
            throw new IllegalArgumentException("standard length exceeded for value");
        }
        byte[] tmp = new byte[length];
        System.arraycopy(bytes, start, tmp, tmp.length - count, count);
        return tmp;
    }

    public static BigInteger createRandomInRange(BigInteger min, BigInteger max, SecureRandom random) {
        int cmp = min.compareTo(max);
        if (cmp >= 0) {
            if (cmp <= 0) {
                return min;
            }
            throw new IllegalArgumentException("'min' may not be greater than 'max'");
        } else if (min.bitLength() > max.bitLength() / 2) {
            return createRandomInRange(ZERO, max.subtract(min), random).add(min);
        } else {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                BigInteger x = new BigInteger(max.bitLength(), random);
                if (x.compareTo(min) >= 0 && x.compareTo(max) <= 0) {
                    return x;
                }
            }
            return new BigInteger(max.subtract(min).bitLength() - 1, random).add(min);
        }
    }

    public static BigInteger fromUnsignedByteArray(byte[] buf) {
        return new BigInteger(1, buf);
    }

    public static BigInteger fromUnsignedByteArray(byte[] buf, int off, int length) {
        byte[] mag = buf;
        if (!(off == 0 && length == buf.length)) {
            mag = new byte[length];
            System.arraycopy(buf, off, mag, 0, length);
        }
        return new BigInteger(1, mag);
    }
}
