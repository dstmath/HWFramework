package com.android.org.bouncycastle.math.ec;

import com.android.org.bouncycastle.math.ec.ECFieldElement.F2m;
import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;

class LongArray implements Cloneable {
    private static final short[] INTERLEAVE2_TABLE = null;
    private static final int[] INTERLEAVE3_TABLE = null;
    private static final int[] INTERLEAVE4_TABLE = null;
    private static final int[] INTERLEAVE5_TABLE = null;
    private static final long[] INTERLEAVE7_TABLE = null;
    private static final String ZEROES = "0000000000000000000000000000000000000000000000000000000000000000";
    static final byte[] bitLengths = null;
    private long[] m_ints;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.math.ec.LongArray.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.math.ec.LongArray.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.LongArray.<clinit>():void");
    }

    public LongArray(int intLen) {
        this.m_ints = new long[intLen];
    }

    public LongArray(long[] ints) {
        this.m_ints = ints;
    }

    public LongArray(long[] ints, int off, int len) {
        if (off == 0 && len == ints.length) {
            this.m_ints = ints;
            return;
        }
        this.m_ints = new long[len];
        System.arraycopy(ints, off, this.m_ints, 0, len);
    }

    public LongArray(BigInteger bigInt) {
        if (bigInt == null || bigInt.signum() < 0) {
            throw new IllegalArgumentException("invalid F2m field value");
        } else if (bigInt.signum() == 0) {
            this.m_ints = new long[]{0};
        } else {
            byte[] barr = bigInt.toByteArray();
            int barrLen = barr.length;
            int barrStart = 0;
            if (barr[0] == null) {
                barrLen--;
                barrStart = 1;
            }
            int intLen = (barrLen + 7) / 8;
            this.m_ints = new long[intLen];
            int iarrJ = intLen - 1;
            int rem = (barrLen % 8) + barrStart;
            long temp = 0;
            int barrI = barrStart;
            if (barrStart < rem) {
                while (barrI < rem) {
                    temp = (temp << 8) | ((long) (barr[barrI] & 255));
                    barrI++;
                }
                int iarrJ2 = iarrJ - 1;
                this.m_ints[iarrJ] = temp;
                iarrJ = iarrJ2;
            }
            while (iarrJ >= 0) {
                temp = 0;
                int i = 0;
                int barrI2 = barrI;
                while (i < 8) {
                    temp = (temp << 8) | ((long) (barr[barrI2] & 255));
                    i++;
                    barrI2++;
                }
                this.m_ints[iarrJ] = temp;
                iarrJ--;
                barrI = barrI2;
            }
        }
    }

    public boolean isOne() {
        long[] a = this.m_ints;
        if (a[0] != 1) {
            return false;
        }
        for (int i = 1; i < a.length; i++) {
            if (a[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isZero() {
        long[] a = this.m_ints;
        for (long j : a) {
            if (j != 0) {
                return false;
            }
        }
        return true;
    }

    public int getUsedLength() {
        return getUsedLengthFrom(this.m_ints.length);
    }

    public int getUsedLengthFrom(int from) {
        long[] a = this.m_ints;
        from = Math.min(from, a.length);
        if (from < 1) {
            return 0;
        }
        if (a[0] != 0) {
            do {
                from--;
            } while (a[from] == 0);
            return from + 1;
        }
        do {
            from--;
            if (a[from] != 0) {
                return from + 1;
            }
        } while (from > 0);
        return 0;
    }

    public int degree() {
        int i = this.m_ints.length;
        while (i != 0) {
            i--;
            long w = this.m_ints[i];
            if (w != 0) {
                return (i << 6) + bitLength(w);
            }
        }
        return 0;
    }

    private int degreeFrom(int limit) {
        int i = (limit + 62) >>> 6;
        while (i != 0) {
            i--;
            long w = this.m_ints[i];
            if (w != 0) {
                return (i << 6) + bitLength(w);
            }
        }
        return 0;
    }

    private static int bitLength(long w) {
        int b;
        int k;
        int u = (int) (w >>> 32);
        if (u == 0) {
            u = (int) w;
            b = 0;
        } else {
            b = 32;
        }
        int t = u >>> 16;
        if (t == 0) {
            t = u >>> 8;
            k = t == 0 ? bitLengths[u] : bitLengths[t] + 8;
        } else {
            int v = t >>> 8;
            k = v == 0 ? bitLengths[t] + 16 : bitLengths[v] + 24;
        }
        return b + k;
    }

    private long[] resizedInts(int newLen) {
        long[] newInts = new long[newLen];
        System.arraycopy(this.m_ints, 0, newInts, 0, Math.min(this.m_ints.length, newLen));
        return newInts;
    }

    public BigInteger toBigInteger() {
        int usedLen = getUsedLength();
        if (usedLen == 0) {
            return ECConstants.ZERO;
        }
        int barrI;
        long highestInt = this.m_ints[usedLen - 1];
        byte[] temp = new byte[8];
        boolean trailingZeroBytesDone = false;
        int j = 7;
        int barrI2 = 0;
        while (j >= 0) {
            byte thisByte = (byte) ((int) (highestInt >>> (j * 8)));
            if (trailingZeroBytesDone || thisByte != null) {
                trailingZeroBytesDone = true;
                barrI = barrI2 + 1;
                temp[barrI2] = thisByte;
            } else {
                barrI = barrI2;
            }
            j--;
            barrI2 = barrI;
        }
        byte[] barr = new byte[(((usedLen - 1) * 8) + barrI2)];
        for (j = 0; j < barrI2; j++) {
            barr[j] = temp[j];
        }
        int iarrJ = usedLen - 2;
        barrI = barrI2;
        while (iarrJ >= 0) {
            long mi = this.m_ints[iarrJ];
            j = 7;
            barrI2 = barrI;
            while (j >= 0) {
                barrI = barrI2 + 1;
                barr[barrI2] = (byte) ((int) (mi >>> (j * 8)));
                j--;
                barrI2 = barrI;
            }
            iarrJ--;
            barrI = barrI2;
        }
        return new BigInteger(1, barr);
    }

    private static long shiftUp(long[] x, int xOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        for (int i = 0; i < count; i++) {
            long next = x[xOff + i];
            x[xOff + i] = (next << shift) | prev;
            prev = next >>> shiftInv;
        }
        return prev;
    }

    private static long shiftUp(long[] x, int xOff, long[] z, int zOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        for (int i = 0; i < count; i++) {
            long next = x[xOff + i];
            z[zOff + i] = (next << shift) | prev;
            prev = next >>> shiftInv;
        }
        return prev;
    }

    public LongArray addOne() {
        if (this.m_ints.length == 0) {
            return new LongArray(new long[]{1});
        }
        long[] ints = resizedInts(Math.max(1, getUsedLength()));
        ints[0] = ints[0] ^ 1;
        return new LongArray(ints);
    }

    private void addShiftedByBitsSafe(LongArray other, int otherDegree, int bits) {
        int otherLen = (otherDegree + 63) >>> 6;
        int words = bits >>> 6;
        int shift = bits & 63;
        if (shift == 0) {
            add(this.m_ints, words, other.m_ints, 0, otherLen);
            return;
        }
        long carry = addShiftedUp(this.m_ints, words, other.m_ints, 0, otherLen, shift);
        if (carry != 0) {
            long[] jArr = this.m_ints;
            int i = otherLen + words;
            jArr[i] = jArr[i] ^ carry;
        }
    }

    private static long addShiftedUp(long[] x, int xOff, long[] y, int yOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        for (int i = 0; i < count; i++) {
            long next = y[yOff + i];
            int i2 = xOff + i;
            x[i2] = x[i2] ^ ((next << shift) | prev);
            prev = next >>> shiftInv;
        }
        return prev;
    }

    private static long addShiftedDown(long[] x, int xOff, long[] y, int yOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        int i = count;
        while (true) {
            i--;
            if (i < 0) {
                return prev;
            }
            long next = y[yOff + i];
            int i2 = xOff + i;
            x[i2] = x[i2] ^ ((next >>> shift) | prev);
            prev = next << shiftInv;
        }
    }

    public void addShiftedByWords(LongArray other, int words) {
        int otherUsedLen = other.getUsedLength();
        if (otherUsedLen != 0) {
            int minLen = otherUsedLen + words;
            if (minLen > this.m_ints.length) {
                this.m_ints = resizedInts(minLen);
            }
            add(this.m_ints, words, other.m_ints, 0, otherUsedLen);
        }
    }

    private static void add(long[] x, int xOff, long[] y, int yOff, int count) {
        for (int i = 0; i < count; i++) {
            int i2 = xOff + i;
            x[i2] = x[i2] ^ y[yOff + i];
        }
    }

    private static void add(long[] x, int xOff, long[] y, int yOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = x[xOff + i] ^ y[yOff + i];
        }
    }

    private static void addBoth(long[] x, int xOff, long[] y1, int y1Off, long[] y2, int y2Off, int count) {
        for (int i = 0; i < count; i++) {
            int i2 = xOff + i;
            x[i2] = x[i2] ^ (y1[y1Off + i] ^ y2[y2Off + i]);
        }
    }

    private static void distribute(long[] x, int src, int dst1, int dst2, int count) {
        for (int i = 0; i < count; i++) {
            long v = x[src + i];
            int i2 = dst1 + i;
            x[i2] = x[i2] ^ v;
            i2 = dst2 + i;
            x[i2] = x[i2] ^ v;
        }
    }

    public int getLength() {
        return this.m_ints.length;
    }

    private static void flipWord(long[] buf, int off, int bit, long word) {
        int n = off + (bit >>> 6);
        int shift = bit & 63;
        if (shift == 0) {
            buf[n] = buf[n] ^ word;
            return;
        }
        buf[n] = buf[n] ^ (word << shift);
        word >>>= 64 - shift;
        if (word != 0) {
            n++;
            buf[n] = buf[n] ^ word;
        }
    }

    public boolean testBitZero() {
        return this.m_ints.length > 0 && (this.m_ints[0] & 1) != 0;
    }

    private static boolean testBit(long[] buf, int off, int n) {
        return (buf[off + (n >>> 6)] & (1 << (n & 63))) != 0;
    }

    private static void flipBit(long[] buf, int off, int n) {
        int i = off + (n >>> 6);
        buf[i] = buf[i] ^ (1 << (n & 63));
    }

    private static void multiplyWord(long a, long[] b, int bLen, long[] c, int cOff) {
        if ((1 & a) != 0) {
            add(c, cOff, b, 0, bLen);
        }
        int k = 1;
        while (true) {
            a >>>= 1;
            if (a != 0) {
                if ((1 & a) != 0) {
                    long carry = addShiftedUp(c, cOff, b, 0, bLen, k);
                    if (carry != 0) {
                        int i = cOff + bLen;
                        c[i] = c[i] ^ carry;
                    }
                }
                k++;
            } else {
                return;
            }
        }
    }

    public LongArray modMultiplyLD(LongArray other, int m, int[] ks) {
        int aDeg = degree();
        if (aDeg == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A = this;
        LongArray B = other;
        if (aDeg > bDeg) {
            A = other;
            B = this;
            int tmp = aDeg;
            aDeg = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A.m_ints[0];
            if (a0 == 1) {
                return B;
            }
            long[] c0 = new long[cLen];
            multiplyWord(a0, B.m_ints, bLen, c0, 0);
            return reduceResult(c0, 0, cLen, m, ks);
        }
        int k;
        int bMax = ((bDeg + 7) + 63) >>> 6;
        int[] ti = new int[16];
        long[] T0 = new long[(bMax << 4)];
        int tOff = bMax;
        ti[1] = bMax;
        System.arraycopy(B.m_ints, 0, T0, bMax, bLen);
        for (int i = 2; i < 16; i++) {
            tOff += bMax;
            ti[i] = tOff;
            if ((i & 1) == 0) {
                shiftUp(T0, tOff >>> 1, T0, tOff, bMax, 1);
            } else {
                add(T0, bMax, T0, tOff - bMax, T0, tOff, bMax);
            }
        }
        long[] T1 = new long[T0.length];
        long[] jArr = T0;
        shiftUp(jArr, 0, T1, 0, T0.length, 4);
        long[] a = A.m_ints;
        long[] c = new long[cLen];
        for (k = 56; k >= 0; k -= 8) {
            for (int j = 1; j < aLen; j += 2) {
                int aVal = (int) (a[j] >>> k);
                long[] jArr2 = T0;
                long[] jArr3 = T1;
                addBoth(c, j - 1, jArr2, ti[aVal & 15], jArr3, ti[(aVal >>> 4) & 15], bMax);
            }
            shiftUp(c, 0, cLen, 8);
        }
        for (k = 56; k >= 0; k -= 8) {
            for (int j2 = 0; j2 < aLen; j2 += 2) {
                aVal = (int) (a[j2] >>> k);
                jArr2 = T0;
                jArr3 = T1;
                addBoth(c, j2, jArr2, ti[aVal & 15], jArr3, ti[(aVal >>> 4) & 15], bMax);
            }
            if (k > 0) {
                shiftUp(c, 0, cLen, 8);
            }
        }
        return reduceResult(c, 0, cLen, m, ks);
    }

    public LongArray modMultiply(LongArray other, int m, int[] ks) {
        int aDeg = degree();
        if (aDeg == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A = this;
        LongArray B = other;
        if (aDeg > bDeg) {
            A = other;
            B = this;
            int tmp = aDeg;
            aDeg = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A.m_ints[0];
            if (a0 == 1) {
                return B;
            }
            long[] c0 = new long[cLen];
            multiplyWord(a0, B.m_ints, bLen, c0, 0);
            return reduceResult(c0, 0, cLen, m, ks);
        }
        int cOff;
        int bMax = ((bDeg + 7) + 63) >>> 6;
        int[] ti = new int[16];
        long[] T0 = new long[(bMax << 4)];
        int tOff = bMax;
        ti[1] = bMax;
        System.arraycopy(B.m_ints, 0, T0, bMax, bLen);
        for (int i = 2; i < 16; i++) {
            tOff += bMax;
            ti[i] = tOff;
            if ((i & 1) == 0) {
                shiftUp(T0, tOff >>> 1, T0, tOff, bMax, 1);
            } else {
                add(T0, bMax, T0, tOff - bMax, T0, tOff, bMax);
            }
        }
        long[] T1 = new long[T0.length];
        long[] jArr = T0;
        shiftUp(jArr, 0, T1, 0, T0.length, 4);
        long[] a = A.m_ints;
        long[] c = new long[(cLen << 3)];
        for (int aPos = 0; aPos < aLen; aPos++) {
            long aVal = a[aPos];
            cOff = aPos;
            while (true) {
                int u = ((int) aVal) & 15;
                aVal >>>= 4;
                long[] jArr2 = T0;
                long[] jArr3 = T1;
                addBoth(c, cOff, jArr2, ti[u], jArr3, ti[((int) aVal) & 15], bMax);
                aVal >>>= 4;
                if (aVal == 0) {
                    break;
                }
                cOff += cLen;
            }
        }
        cOff = c.length;
        while (true) {
            cOff -= cLen;
            if (cOff == 0) {
                return reduceResult(c, 0, cLen, m, ks);
            }
            addShiftedUp(c, cOff - cLen, c, cOff, cLen, 8);
        }
    }

    public LongArray modMultiplyAlt(LongArray other, int m, int[] ks) {
        int aDeg = degree();
        if (aDeg == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A = this;
        LongArray B = other;
        if (aDeg > bDeg) {
            A = other;
            B = this;
            int tmp = aDeg;
            aDeg = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A.m_ints[0];
            if (a0 == 1) {
                return B;
            }
            long[] c0 = new long[cLen];
            multiplyWord(a0, B.m_ints, bLen, c0, 0);
            return reduceResult(c0, 0, cLen, m, ks);
        }
        int bank;
        int bMax = ((bDeg + 15) + 63) >>> 6;
        int bTotal = bMax * 8;
        int[] ci = new int[16];
        int cTotal = aLen;
        ci[0] = aLen;
        cTotal = aLen + bTotal;
        ci[1] = cTotal;
        for (int i = 2; i < ci.length; i++) {
            cTotal += cLen;
            ci[i] = cTotal;
        }
        long[] c = new long[((cTotal + cLen) + 1)];
        interleave(A.m_ints, 0, c, 0, aLen, 4);
        int bOff = aLen;
        System.arraycopy(B.m_ints, 0, c, aLen, bLen);
        for (bank = 1; bank < 8; bank++) {
            bOff += bMax;
            shiftUp(c, aLen, c, bOff, bMax, bank);
        }
        int MASK = 16 - 1;
        int k = 0;
        while (true) {
            int aPos = 0;
            do {
                long aVal = c[aPos] >>> k;
                bank = 0;
                bOff = aLen;
                while (true) {
                    int index = ((int) aVal) & MASK;
                    if (index != 0) {
                        add(c, ci[index] + aPos, c, bOff, bMax);
                    }
                    bank++;
                    if (bank == 8) {
                        break;
                    }
                    bOff += bMax;
                    aVal >>>= 4;
                }
                aPos++;
            } while (aPos < aLen);
            k += 32;
            if (k >= 64) {
                if (k >= 64) {
                    break;
                }
                k = 60;
                MASK &= MASK << 4;
            }
            shiftUp(c, aLen, bTotal, 8);
        }
        int ciPos = ci.length;
        while (true) {
            ciPos--;
            if (ciPos <= 1) {
                return reduceResult(c, ci[1], cLen, m, ks);
            }
            if ((((long) ciPos) & 1) == 0) {
                addShiftedUp(c, ci[ciPos >>> 1], c, ci[ciPos], cLen, 16);
            } else {
                distribute(c, ci[ciPos], ci[ciPos - 1], ci[1], cLen);
            }
        }
    }

    public LongArray modReduce(int m, int[] ks) {
        long[] buf = Arrays.clone(this.m_ints);
        return new LongArray(buf, 0, reduceInPlace(buf, 0, buf.length, m, ks));
    }

    public LongArray multiply(LongArray other, int m, int[] ks) {
        int aDeg = degree();
        if (aDeg == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A = this;
        LongArray B = other;
        if (aDeg > bDeg) {
            A = other;
            B = this;
            int tmp = aDeg;
            aDeg = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A.m_ints[0];
            if (a0 == 1) {
                return B;
            }
            long[] c0 = new long[cLen];
            multiplyWord(a0, B.m_ints, bLen, c0, 0);
            return new LongArray(c0, 0, cLen);
        }
        int cOff;
        int bMax = ((bDeg + 7) + 63) >>> 6;
        int[] ti = new int[16];
        long[] T0 = new long[(bMax << 4)];
        int tOff = bMax;
        ti[1] = bMax;
        System.arraycopy(B.m_ints, 0, T0, bMax, bLen);
        for (int i = 2; i < 16; i++) {
            tOff += bMax;
            ti[i] = tOff;
            if ((i & 1) == 0) {
                shiftUp(T0, tOff >>> 1, T0, tOff, bMax, 1);
            } else {
                add(T0, bMax, T0, tOff - bMax, T0, tOff, bMax);
            }
        }
        long[] T1 = new long[T0.length];
        long[] jArr = T0;
        shiftUp(jArr, 0, T1, 0, T0.length, 4);
        long[] a = A.m_ints;
        long[] c = new long[(cLen << 3)];
        for (int aPos = 0; aPos < aLen; aPos++) {
            long aVal = a[aPos];
            cOff = aPos;
            while (true) {
                int u = ((int) aVal) & 15;
                aVal >>>= 4;
                long[] jArr2 = T0;
                long[] jArr3 = T1;
                addBoth(c, cOff, jArr2, ti[u], jArr3, ti[((int) aVal) & 15], bMax);
                aVal >>>= 4;
                if (aVal == 0) {
                    break;
                }
                cOff += cLen;
            }
        }
        cOff = c.length;
        while (true) {
            cOff -= cLen;
            if (cOff == 0) {
                return new LongArray(c, 0, cLen);
            }
            addShiftedUp(c, cOff - cLen, c, cOff, cLen, 8);
        }
    }

    public void reduce(int m, int[] ks) {
        long[] buf = this.m_ints;
        int rLen = reduceInPlace(buf, 0, buf.length, m, ks);
        if (rLen < buf.length) {
            this.m_ints = new long[rLen];
            System.arraycopy(buf, 0, this.m_ints, 0, rLen);
        }
    }

    private static LongArray reduceResult(long[] buf, int off, int len, int m, int[] ks) {
        return new LongArray(buf, off, reduceInPlace(buf, off, len, m, ks));
    }

    private static int reduceInPlace(long[] buf, int off, int len, int m, int[] ks) {
        int mLen = (m + 63) >>> 6;
        if (len < mLen) {
            return len;
        }
        int numBits = Math.min(len << 6, (m << 1) - 1);
        int excessBits = (len << 6) - numBits;
        while (excessBits >= 64) {
            len--;
            excessBits -= 64;
        }
        int kLen = ks.length;
        int kMax = ks[kLen - 1];
        int kNext = kLen > 1 ? ks[kLen - 2] : 0;
        int wordWiseLimit = Math.max(m, kMax + 64);
        int vectorableWords = (Math.min(numBits - wordWiseLimit, m - kNext) + excessBits) >> 6;
        if (vectorableWords > 1) {
            int vectorWiseWords = len - vectorableWords;
            reduceVectorWise(buf, off, len, vectorWiseWords, m, ks);
            while (len > vectorWiseWords) {
                len--;
                buf[off + len] = 0;
            }
            numBits = vectorWiseWords << 6;
        }
        if (numBits > wordWiseLimit) {
            reduceWordWise(buf, off, len, wordWiseLimit, m, ks);
            numBits = wordWiseLimit;
        }
        if (numBits > m) {
            reduceBitWise(buf, off, numBits, m, ks);
        }
        return mLen;
    }

    private static void reduceBitWise(long[] buf, int off, int bitlength, int m, int[] ks) {
        while (true) {
            bitlength--;
            if (bitlength < m) {
                return;
            }
            if (testBit(buf, off, bitlength)) {
                reduceBit(buf, off, bitlength, m, ks);
            }
        }
    }

    private static void reduceBit(long[] buf, int off, int bit, int m, int[] ks) {
        flipBit(buf, off, bit);
        int n = bit - m;
        int j = ks.length;
        while (true) {
            j--;
            if (j >= 0) {
                flipBit(buf, off, ks[j] + n);
            } else {
                flipBit(buf, off, n);
                return;
            }
        }
    }

    private static void reduceWordWise(long[] buf, int off, int len, int toBit, int m, int[] ks) {
        long word;
        int toPos = toBit >>> 6;
        while (true) {
            len--;
            if (len <= toPos) {
                break;
            }
            word = buf[off + len];
            if (word != 0) {
                buf[off + len] = 0;
                reduceWord(buf, off, len << 6, word, m, ks);
            }
        }
        int partial = toBit & 63;
        word = buf[off + toPos] >>> partial;
        if (word != 0) {
            int i = off + toPos;
            buf[i] = buf[i] ^ (word << partial);
            reduceWord(buf, off, toBit, word, m, ks);
        }
    }

    private static void reduceWord(long[] buf, int off, int bit, long word, int m, int[] ks) {
        int offset = bit - m;
        int j = ks.length;
        while (true) {
            j--;
            if (j >= 0) {
                flipWord(buf, off, ks[j] + offset, word);
            } else {
                flipWord(buf, off, offset, word);
                return;
            }
        }
    }

    private static void reduceVectorWise(long[] buf, int off, int len, int words, int m, int[] ks) {
        int baseBit = (words << 6) - m;
        int j = ks.length;
        while (true) {
            j--;
            if (j >= 0) {
                flipVector(buf, off, buf, off + words, len - words, baseBit + ks[j]);
            } else {
                flipVector(buf, off, buf, off + words, len - words, baseBit);
                return;
            }
        }
    }

    private static void flipVector(long[] x, int xOff, long[] y, int yOff, int yLen, int bits) {
        xOff += bits >>> 6;
        bits &= 63;
        if (bits == 0) {
            add(x, xOff, y, yOff, yLen);
            return;
        }
        x[xOff] = x[xOff] ^ addShiftedDown(x, xOff + 1, y, yOff, yLen, 64 - bits);
    }

    public LongArray modSquare(int m, int[] ks) {
        int len = getUsedLength();
        if (len == 0) {
            return this;
        }
        int _2len = len << 1;
        long[] r = new long[_2len];
        int i = 0;
        while (i < _2len) {
            long mi = this.m_ints[i >>> 1];
            int i2 = i + 1;
            r[i] = interleave2_32to64((int) mi);
            i = i2 + 1;
            r[i2] = interleave2_32to64((int) (mi >>> 32));
        }
        return new LongArray(r, 0, reduceInPlace(r, 0, r.length, m, ks));
    }

    public LongArray modSquareN(int n, int m, int[] ks) {
        int len = getUsedLength();
        if (len == 0) {
            return this;
        }
        long[] r = new long[(((m + 63) >>> 6) << 1)];
        System.arraycopy(this.m_ints, 0, r, 0, len);
        while (true) {
            n--;
            if (n < 0) {
                return new LongArray(r, 0, len);
            }
            squareInPlace(r, len, m, ks);
            len = reduceInPlace(r, 0, r.length, m, ks);
        }
    }

    public LongArray square(int m, int[] ks) {
        int len = getUsedLength();
        if (len == 0) {
            return this;
        }
        int _2len = len << 1;
        long[] r = new long[_2len];
        int i = 0;
        while (i < _2len) {
            long mi = this.m_ints[i >>> 1];
            int i2 = i + 1;
            r[i] = interleave2_32to64((int) mi);
            i = i2 + 1;
            r[i2] = interleave2_32to64((int) (mi >>> 32));
        }
        return new LongArray(r, 0, r.length);
    }

    private static void squareInPlace(long[] x, int xLen, int m, int[] ks) {
        int pos = xLen << 1;
        while (true) {
            xLen--;
            if (xLen >= 0) {
                long xVal = x[xLen];
                pos--;
                x[pos] = interleave2_32to64((int) (xVal >>> 32));
                pos--;
                x[pos] = interleave2_32to64((int) xVal);
            } else {
                return;
            }
        }
    }

    private static void interleave(long[] x, int xOff, long[] z, int zOff, int count, int width) {
        switch (width) {
            case F2m.PPB /*3*/:
                interleave3(x, xOff, z, zOff, count);
            case ECCurve.COORD_LAMBDA_AFFINE /*5*/:
                interleave5(x, xOff, z, zOff, count);
            case ECCurve.COORD_SKEWED /*7*/:
                interleave7(x, xOff, z, zOff, count);
            default:
                interleave2_n(x, xOff, z, zOff, count, bitLengths[width] - 1);
        }
    }

    private static void interleave3(long[] x, int xOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave3(x[xOff + i]);
        }
    }

    private static long interleave3(long x) {
        return ((interleave3_21to63(((int) x) & 2097151) | (x & Long.MIN_VALUE)) | (interleave3_21to63(((int) (x >>> 21)) & 2097151) << 1)) | (interleave3_21to63(((int) (x >>> 42)) & 2097151) << 2);
    }

    private static long interleave3_21to63(int x) {
        int r00 = INTERLEAVE3_TABLE[x & 127];
        return (((((long) INTERLEAVE3_TABLE[x >>> 14]) & 4294967295L) << 42) | ((((long) INTERLEAVE3_TABLE[(x >>> 7) & 127]) & 4294967295L) << 21)) | (((long) r00) & 4294967295L);
    }

    private static void interleave5(long[] x, int xOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave5(x[xOff + i]);
        }
    }

    private static long interleave5(long x) {
        return (((interleave3_13to65(((int) x) & 8191) | (interleave3_13to65(((int) (x >>> 13)) & 8191) << 1)) | (interleave3_13to65(((int) (x >>> 26)) & 8191) << 2)) | (interleave3_13to65(((int) (x >>> 39)) & 8191) << 3)) | (interleave3_13to65(((int) (x >>> 52)) & 8191) << 4);
    }

    private static long interleave3_13to65(int x) {
        return ((((long) INTERLEAVE5_TABLE[x >>> 7]) & 4294967295L) << 35) | (((long) INTERLEAVE5_TABLE[x & 127]) & 4294967295L);
    }

    private static void interleave7(long[] x, int xOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave7(x[xOff + i]);
        }
    }

    private static long interleave7(long x) {
        return ((((((INTERLEAVE7_TABLE[((int) x) & 511] | (x & Long.MIN_VALUE)) | (INTERLEAVE7_TABLE[((int) (x >>> 9)) & 511] << 1)) | (INTERLEAVE7_TABLE[((int) (x >>> 18)) & 511] << 2)) | (INTERLEAVE7_TABLE[((int) (x >>> 27)) & 511] << 3)) | (INTERLEAVE7_TABLE[((int) (x >>> 36)) & 511] << 4)) | (INTERLEAVE7_TABLE[((int) (x >>> 45)) & 511] << 5)) | (INTERLEAVE7_TABLE[((int) (x >>> 54)) & 511] << 6);
    }

    private static void interleave2_n(long[] x, int xOff, long[] z, int zOff, int count, int rounds) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave2_n(x[xOff + i], rounds);
        }
    }

    private static long interleave2_n(long x, int rounds) {
        while (rounds > 1) {
            rounds -= 2;
            x = ((interleave4_16to64(((int) x) & 65535) | (interleave4_16to64(((int) (x >>> 16)) & 65535) << 1)) | (interleave4_16to64(((int) (x >>> 32)) & 65535) << 2)) | (interleave4_16to64(((int) (x >>> 48)) & 65535) << 3);
        }
        if (rounds > 0) {
            return interleave2_32to64((int) x) | (interleave2_32to64((int) (x >>> 32)) << 1);
        }
        return x;
    }

    private static long interleave4_16to64(int x) {
        return ((((long) INTERLEAVE4_TABLE[x >>> 8]) & 4294967295L) << 32) | (((long) INTERLEAVE4_TABLE[x & 255]) & 4294967295L);
    }

    private static long interleave2_32to64(int x) {
        return ((((long) (INTERLEAVE2_TABLE[(x >>> 16) & 255] | (INTERLEAVE2_TABLE[x >>> 24] << 16))) & 4294967295L) << 32) | (((long) (INTERLEAVE2_TABLE[x & 255] | (INTERLEAVE2_TABLE[(x >>> 8) & 255] << 16))) & 4294967295L);
    }

    public LongArray modInverse(int m, int[] ks) {
        int uzDegree = degree();
        if (uzDegree == 0) {
            throw new IllegalStateException();
        } else if (uzDegree == 1) {
            return this;
        } else {
            LongArray uz = (LongArray) clone();
            int t = (m + 63) >>> 6;
            reduceBit(new LongArray(t).m_ints, 0, m, m, ks);
            new LongArray(t).m_ints[0] = 1;
            LongArray g2z = new LongArray(t);
            int[] uvDeg = new int[]{uzDegree, m + 1};
            LongArray[] uv = new LongArray[]{uz, r0};
            int[] ggDeg = new int[]{1, 0};
            LongArray[] gg = new LongArray[]{g1z, g2z};
            int b = 1;
            int duv1 = uvDeg[1];
            int dgg1 = ggDeg[1];
            int j = duv1 - uvDeg[0];
            while (true) {
                if (j < 0) {
                    j = -j;
                    uvDeg[b] = duv1;
                    ggDeg[b] = dgg1;
                    b = 1 - b;
                    duv1 = uvDeg[b];
                    dgg1 = ggDeg[b];
                }
                uv[b].addShiftedByBitsSafe(uv[1 - b], uvDeg[1 - b], j);
                int duv2 = uv[b].degreeFrom(duv1);
                if (duv2 == 0) {
                    return gg[1 - b];
                }
                int dgg2 = ggDeg[1 - b];
                gg[b].addShiftedByBitsSafe(gg[1 - b], dgg2, j);
                dgg2 += j;
                if (dgg2 > dgg1) {
                    dgg1 = dgg2;
                } else if (dgg2 == dgg1) {
                    dgg1 = gg[b].degreeFrom(dgg1);
                }
                j += duv2 - duv1;
                duv1 = duv2;
            }
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof LongArray)) {
            return false;
        }
        LongArray other = (LongArray) o;
        int usedLen = getUsedLength();
        if (other.getUsedLength() != usedLen) {
            return false;
        }
        for (int i = 0; i < usedLen; i++) {
            if (this.m_ints[i] != other.m_ints[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int usedLen = getUsedLength();
        int hash = 1;
        for (int i = 0; i < usedLen; i++) {
            long mi = this.m_ints[i];
            hash = (((hash * 31) ^ ((int) mi)) * 31) ^ ((int) (mi >>> 32));
        }
        return hash;
    }

    public Object clone() {
        return new LongArray(Arrays.clone(this.m_ints));
    }

    public String toString() {
        int i = getUsedLength();
        if (i == 0) {
            return "0";
        }
        i--;
        StringBuffer sb = new StringBuffer(Long.toBinaryString(this.m_ints[i]));
        while (true) {
            i--;
            if (i < 0) {
                return sb.toString();
            }
            String s = Long.toBinaryString(this.m_ints[i]);
            int len = s.length();
            if (len < 64) {
                sb.append(ZEROES.substring(len));
            }
            sb.append(s);
        }
    }
}
