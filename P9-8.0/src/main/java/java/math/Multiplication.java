package java.math;

import dalvik.system.VMRuntime;

class Multiplication {
    static final BigInteger[] bigFivePows = new BigInteger[32];
    static final BigInteger[] bigTenPows = new BigInteger[32];
    static final int[] fivePows = new int[]{1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125};
    static final int[] tenPows = new int[]{1, 10, 100, 1000, VMRuntime.SDK_VERSION_CUR_DEVELOPMENT, 100000, 1000000, 10000000, 100000000, 1000000000};

    private Multiplication() {
    }

    static {
        long fivePow = 1;
        int i = 0;
        while (i <= 18) {
            bigFivePows[i] = BigInteger.valueOf(fivePow);
            bigTenPows[i] = BigInteger.valueOf(fivePow << i);
            fivePow *= 5;
            i++;
        }
        while (i < bigTenPows.length) {
            bigFivePows[i] = bigFivePows[i - 1].multiply(bigFivePows[1]);
            bigTenPows[i] = bigTenPows[i - 1].multiply(BigInteger.TEN);
            i++;
        }
    }

    static BigInteger multiplyByPositiveInt(BigInteger val, int factor) {
        BigInt bi = val.getBigInt().copy();
        bi.multiplyByPositiveInt(factor);
        return new BigInteger(bi);
    }

    static BigInteger multiplyByTenPow(BigInteger val, long exp) {
        if (exp < ((long) tenPows.length)) {
            return multiplyByPositiveInt(val, tenPows[(int) exp]);
        }
        return val.multiply(powerOf10(exp));
    }

    static BigInteger powerOf10(long exp) {
        int intExp = (int) exp;
        if (exp < ((long) bigTenPows.length)) {
            return bigTenPows[intExp];
        }
        if (exp <= 50) {
            return BigInteger.TEN.pow(intExp);
        }
        BigInteger res;
        if (exp <= 2147483647L) {
            try {
                res = bigFivePows[1].pow(intExp).shiftLeft(intExp);
            } catch (OutOfMemoryError error) {
                throw new ArithmeticException(error.getMessage());
            }
        }
        long longExp;
        BigInteger powerOfFive = bigFivePows[1].pow(Integer.MAX_VALUE);
        res = powerOfFive;
        intExp = (int) (exp % 2147483647L);
        for (longExp = exp - 2147483647L; longExp > 2147483647L; longExp -= 2147483647L) {
            res = res.multiply(powerOfFive);
        }
        res = res.multiply(bigFivePows[1].pow(intExp)).shiftLeft(Integer.MAX_VALUE);
        for (longExp = exp - 2147483647L; longExp > 2147483647L; longExp -= 2147483647L) {
            res = res.shiftLeft(Integer.MAX_VALUE);
        }
        res = res.shiftLeft(intExp);
        return res;
    }

    static BigInteger multiplyByFivePow(BigInteger val, int exp) {
        if (exp < fivePows.length) {
            return multiplyByPositiveInt(val, fivePows[exp]);
        }
        if (exp < bigFivePows.length) {
            return val.multiply(bigFivePows[exp]);
        }
        return val.multiply(bigFivePows[1].pow(exp));
    }
}
