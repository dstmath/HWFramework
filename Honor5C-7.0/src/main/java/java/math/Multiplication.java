package java.math;

import android.icu.util.AnnualTimeZoneRule;

class Multiplication {
    static final BigInteger[] bigFivePows = null;
    static final BigInteger[] bigTenPows = null;
    static final int[] fivePows = null;
    static final int[] tenPows = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.math.Multiplication.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.math.Multiplication.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.Multiplication.<clinit>():void");
    }

    private Multiplication() {
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
        BigInteger powerOfFive = bigFivePows[1].pow(AnnualTimeZoneRule.MAX_YEAR);
        res = powerOfFive;
        intExp = (int) (exp % 2147483647L);
        for (longExp = exp - 2147483647L; longExp > 2147483647L; longExp -= 2147483647L) {
            res = res.multiply(powerOfFive);
        }
        res = res.multiply(bigFivePows[1].pow(intExp)).shiftLeft(AnnualTimeZoneRule.MAX_YEAR);
        for (longExp = exp - 2147483647L; longExp > 2147483647L; longExp -= 2147483647L) {
            res = res.shiftLeft(AnnualTimeZoneRule.MAX_YEAR);
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
