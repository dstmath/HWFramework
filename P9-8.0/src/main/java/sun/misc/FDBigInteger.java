package sun.misc;

import java.math.BigInteger;
import java.util.Arrays;

public class FDBigInteger {
    static final /* synthetic */ boolean -assertionsDisabled = (FDBigInteger.class.desiredAssertionStatus() ^ 1);
    static final long[] LONG_5_POW = new long[]{1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125, 6103515625L, 30517578125L, 152587890625L, 762939453125L, 3814697265625L, 19073486328125L, 95367431640625L, 476837158203125L, 2384185791015625L, 11920928955078125L, 59604644775390625L, 298023223876953125L, 1490116119384765625L};
    private static final long LONG_MASK = 4294967295L;
    private static final int MAX_FIVE_POW = 340;
    private static final FDBigInteger[] POW_5_CACHE = new FDBigInteger[MAX_FIVE_POW];
    static final int[] SMALL_5_POW = new int[]{1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125};
    public static final FDBigInteger ZERO = new FDBigInteger(new int[0], 0);
    private int[] data;
    private boolean isImmutable = -assertionsDisabled;
    private int nWords;
    private int offset;

    static {
        int i = 0;
        while (i < SMALL_5_POW.length) {
            FDBigInteger pow5 = new FDBigInteger(new int[]{SMALL_5_POW[i]}, 0);
            pow5.makeImmutable();
            POW_5_CACHE[i] = pow5;
            i++;
        }
        FDBigInteger prev = POW_5_CACHE[i - 1];
        while (i < MAX_FIVE_POW) {
            FDBigInteger[] fDBigIntegerArr = POW_5_CACHE;
            prev = prev.mult(5);
            fDBigIntegerArr[i] = prev;
            prev.makeImmutable();
            i++;
        }
        ZERO.makeImmutable();
    }

    private FDBigInteger(int[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.nWords = data.length;
        trimLeadingZeros();
    }

    public FDBigInteger(long lValue, char[] digits, int kDigits, int nDigits) {
        int v;
        this.data = new int[Math.max((nDigits + 8) / 9, 2)];
        this.data[0] = (int) lValue;
        this.data[1] = (int) (lValue >>> 32);
        this.offset = 0;
        this.nWords = 2;
        int limit = nDigits - 5;
        int i = kDigits;
        while (i < limit) {
            int ilim = i + 5;
            v = digits[i] - 48;
            i++;
            while (i < ilim) {
                v = ((v * 10) + digits[i]) - 48;
                i++;
            }
            multAddMe(100000, v);
        }
        int factor = 1;
        v = 0;
        while (i < nDigits) {
            v = ((v * 10) + digits[i]) - 48;
            factor *= 10;
            i++;
        }
        if (factor != 1) {
            multAddMe(factor, v);
        }
        trimLeadingZeros();
    }

    public static FDBigInteger valueOfPow52(int p5, int p2) {
        if (p5 == 0) {
            return valueOfPow2(p2);
        }
        if (p2 == 0) {
            return big5pow(p5);
        }
        if (p5 >= SMALL_5_POW.length) {
            return big5pow(p5).leftShift(p2);
        }
        int pow5 = SMALL_5_POW[p5];
        int wordcount = p2 >> 5;
        if ((p2 & 31) == 0) {
            return new FDBigInteger(new int[]{pow5}, wordcount);
        }
        return new FDBigInteger(new int[]{pow5 << (p2 & 31), pow5 >>> (32 - (p2 & 31))}, wordcount);
    }

    public static FDBigInteger valueOfMulPow52(long value, int p5, int p2) {
        if (!-assertionsDisabled && p5 < 0) {
            throw new AssertionError(Integer.valueOf(p5));
        } else if (-assertionsDisabled || p2 >= 0) {
            int v0 = (int) value;
            int v1 = (int) (value >>> 32);
            int wordcount = p2 >> 5;
            int bitcount = p2 & 31;
            if (p5 != 0) {
                if (p5 < SMALL_5_POW.length) {
                    long pow5 = ((long) SMALL_5_POW[p5]) & LONG_MASK;
                    long carry = (((long) v0) & LONG_MASK) * pow5;
                    v0 = (int) carry;
                    carry = (carry >>> 32) + ((((long) v1) & LONG_MASK) * pow5);
                    v1 = (int) carry;
                    int v2 = (int) (carry >>> 32);
                    if (bitcount == 0) {
                        return new FDBigInteger(new int[]{v0, v1, v2}, wordcount);
                    }
                    return new FDBigInteger(new int[]{v0 << bitcount, (v1 << bitcount) | (v0 >>> (32 - bitcount)), (v2 << bitcount) | (v1 >>> (32 - bitcount)), v2 >>> (32 - bitcount)}, wordcount);
                }
                int[] r;
                FDBigInteger pow52 = big5pow(p5);
                if (v1 == 0) {
                    r = new int[((p2 != 0 ? 1 : 0) + (pow52.nWords + 1))];
                    mult(pow52.data, pow52.nWords, v0, r);
                } else {
                    r = new int[((p2 != 0 ? 1 : 0) + (pow52.nWords + 2))];
                    mult(pow52.data, pow52.nWords, v0, v1, r);
                }
                return new FDBigInteger(r, pow52.offset).leftShift(p2);
            } else if (p2 == 0) {
                return new FDBigInteger(new int[]{v0, v1}, 0);
            } else if (bitcount == 0) {
                return new FDBigInteger(new int[]{v0, v1}, wordcount);
            } else {
                return new FDBigInteger(new int[]{v0 << bitcount, (v1 << bitcount) | (v0 >>> (32 - bitcount)), v1 >>> (32 - bitcount)}, wordcount);
            }
        } else {
            throw new AssertionError(Integer.valueOf(p2));
        }
    }

    private static FDBigInteger valueOfPow2(int p2) {
        return new FDBigInteger(new int[]{1 << (p2 & 31)}, p2 >> 5);
    }

    private void trimLeadingZeros() {
        int i = this.nWords;
        if (i > 0) {
            i--;
            if (this.data[i] == 0) {
                while (i > 0 && this.data[i - 1] == 0) {
                    i--;
                }
                this.nWords = i;
                if (i == 0) {
                    this.offset = 0;
                }
            }
        }
    }

    public int getNormalizationBias() {
        if (this.nWords == 0) {
            throw new IllegalArgumentException("Zero value cannot be normalized");
        }
        int zeros = Integer.numberOfLeadingZeros(this.data[this.nWords - 1]);
        return zeros < 4 ? zeros + 28 : zeros - 4;
    }

    private static void leftShift(int[] src, int idx, int[] result, int bitcount, int anticount, int prev) {
        while (idx > 0) {
            int v = prev << bitcount;
            prev = src[idx - 1];
            result[idx] = v | (prev >>> anticount);
            idx--;
        }
        result[0] = prev << bitcount;
    }

    public FDBigInteger leftShift(int shift) {
        if (shift == 0 || this.nWords == 0) {
            return this;
        }
        int wordcount = shift >> 5;
        int bitcount = shift & 31;
        int anticount;
        int idx;
        int prev;
        int hi;
        int[] result;
        if (!this.isImmutable) {
            if (bitcount != 0) {
                anticount = 32 - bitcount;
                if ((this.data[0] << bitcount) == 0) {
                    int v;
                    idx = 0;
                    prev = this.data[0];
                    while (idx < this.nWords - 1) {
                        v = prev >>> anticount;
                        prev = this.data[idx + 1];
                        this.data[idx] = v | (prev << bitcount);
                        idx++;
                    }
                    v = prev >>> anticount;
                    this.data[idx] = v;
                    if (v == 0) {
                        this.nWords--;
                    }
                    this.offset++;
                } else {
                    idx = this.nWords - 1;
                    prev = this.data[idx];
                    hi = prev >>> anticount;
                    result = this.data;
                    int[] src = this.data;
                    if (hi != 0) {
                        if (this.nWords == this.data.length) {
                            result = new int[(this.nWords + 1)];
                            this.data = result;
                        }
                        int i = this.nWords;
                        this.nWords = i + 1;
                        result[i] = hi;
                    }
                    leftShift(src, idx, result, bitcount, anticount, prev);
                }
            }
            this.offset += wordcount;
            return this;
        } else if (bitcount == 0) {
            return new FDBigInteger(Arrays.copyOf(this.data, this.nWords), this.offset + wordcount);
        } else {
            anticount = 32 - bitcount;
            idx = this.nWords - 1;
            prev = this.data[idx];
            hi = prev >>> anticount;
            if (hi != 0) {
                result = new int[(this.nWords + 1)];
                result[this.nWords] = hi;
            } else {
                result = new int[this.nWords];
            }
            leftShift(this.data, idx, result, bitcount, anticount, prev);
            return new FDBigInteger(result, this.offset + wordcount);
        }
    }

    private int size() {
        return this.nWords + this.offset;
    }

    public int quoRemIteration(FDBigInteger S) throws IllegalArgumentException {
        if (-assertionsDisabled || !this.isImmutable) {
            int thSize = size();
            int sSize = S.size();
            int p;
            if (thSize < sSize) {
                p = multAndCarryBy10(this.data, this.nWords, this.data);
                if (p != 0) {
                    int[] iArr = this.data;
                    int i = this.nWords;
                    this.nWords = i + 1;
                    iArr[i] = p;
                } else {
                    trimLeadingZeros();
                }
                return 0;
            } else if (thSize > sSize) {
                throw new IllegalArgumentException("disparate values");
            } else {
                long q = (((long) this.data[this.nWords - 1]) & LONG_MASK) / (((long) S.data[S.nWords - 1]) & LONG_MASK);
                if (multDiffMe(q, S) != 0) {
                    long sum = 0;
                    int tStart = S.offset - this.offset;
                    int[] sd = S.data;
                    int[] td = this.data;
                    while (sum == 0) {
                        int sIndex = 0;
                        for (int tIndex = tStart; tIndex < this.nWords; tIndex++) {
                            sum += (((long) td[tIndex]) & LONG_MASK) + (((long) sd[sIndex]) & LONG_MASK);
                            td[tIndex] = (int) sum;
                            sum >>>= 32;
                            sIndex++;
                        }
                        if (-assertionsDisabled || sum == 0 || sum == 1) {
                            q--;
                        } else {
                            throw new AssertionError(Long.valueOf(sum));
                        }
                    }
                }
                p = multAndCarryBy10(this.data, this.nWords, this.data);
                if (-assertionsDisabled || p == 0) {
                    trimLeadingZeros();
                    return (int) q;
                }
                throw new AssertionError(Integer.valueOf(p));
            }
        }
        throw new AssertionError((Object) "cannot modify immutable value");
    }

    public FDBigInteger multBy10() {
        if (this.nWords == 0) {
            return this;
        }
        if (this.isImmutable) {
            int[] res = new int[(this.nWords + 1)];
            res[this.nWords] = multAndCarryBy10(this.data, this.nWords, res);
            return new FDBigInteger(res, this.offset);
        }
        int p = multAndCarryBy10(this.data, this.nWords, this.data);
        if (p != 0) {
            int[] iArr;
            if (this.nWords == this.data.length) {
                if (this.data[0] == 0) {
                    iArr = this.data;
                    int[] iArr2 = this.data;
                    int i = this.nWords - 1;
                    this.nWords = i;
                    System.arraycopy(iArr, 1, iArr2, 0, i);
                    this.offset++;
                } else {
                    this.data = Arrays.copyOf(this.data, this.data.length + 1);
                }
            }
            iArr = this.data;
            int i2 = this.nWords;
            this.nWords = i2 + 1;
            iArr[i2] = p;
        } else {
            trimLeadingZeros();
        }
        return this;
    }

    public FDBigInteger multByPow52(int p5, int p2) {
        if (this.nWords == 0) {
            return this;
        }
        FDBigInteger res = this;
        if (p5 != 0) {
            int extraSize = p2 != 0 ? 1 : 0;
            int[] r;
            if (p5 < SMALL_5_POW.length) {
                r = new int[((this.nWords + 1) + extraSize)];
                mult(this.data, this.nWords, SMALL_5_POW[p5], r);
                res = new FDBigInteger(r, this.offset);
            } else {
                FDBigInteger pow5 = big5pow(p5);
                r = new int[((this.nWords + pow5.size()) + extraSize)];
                mult(this.data, this.nWords, pow5.data, pow5.nWords, r);
                res = new FDBigInteger(r, this.offset + pow5.offset);
            }
        }
        return res.leftShift(p2);
    }

    private static void mult(int[] s1, int s1Len, int[] s2, int s2Len, int[] dst) {
        for (int i = 0; i < s1Len; i++) {
            long v = ((long) s1[i]) & LONG_MASK;
            long p = 0;
            for (int j = 0; j < s2Len; j++) {
                p += (((long) dst[i + j]) & LONG_MASK) + ((((long) s2[j]) & LONG_MASK) * v);
                dst[i + j] = (int) p;
                p >>>= 32;
            }
            dst[i + s2Len] = (int) p;
        }
    }

    public FDBigInteger leftInplaceSub(FDBigInteger subtrahend) {
        if (-assertionsDisabled || size() >= subtrahend.size()) {
            FDBigInteger minuend;
            long diff;
            if (this.isImmutable) {
                minuend = new FDBigInteger((int[]) this.data.clone(), this.offset);
            } else {
                minuend = this;
            }
            int offsetDiff = subtrahend.offset - minuend.offset;
            int[] sData = subtrahend.data;
            int[] mData = minuend.data;
            int subLen = subtrahend.nWords;
            int minLen = minuend.nWords;
            if (offsetDiff < 0) {
                int rLen = minLen - offsetDiff;
                if (rLen < mData.length) {
                    System.arraycopy(mData, 0, mData, -offsetDiff, minLen);
                    Arrays.fill(mData, 0, -offsetDiff, 0);
                } else {
                    int[] r = new int[rLen];
                    System.arraycopy(mData, 0, r, -offsetDiff, minLen);
                    mData = r;
                    minuend.data = r;
                }
                minuend.offset = subtrahend.offset;
                minLen = rLen;
                minuend.nWords = rLen;
                offsetDiff = 0;
            }
            long borrow = 0;
            int mIndex = offsetDiff;
            int sIndex = 0;
            while (sIndex < subLen && mIndex < minLen) {
                diff = ((((long) mData[mIndex]) & LONG_MASK) - (((long) sData[sIndex]) & LONG_MASK)) + borrow;
                mData[mIndex] = (int) diff;
                borrow = diff >> 32;
                sIndex++;
                mIndex++;
            }
            while (borrow != 0 && mIndex < minLen) {
                diff = (((long) mData[mIndex]) & LONG_MASK) + borrow;
                mData[mIndex] = (int) diff;
                borrow = diff >> 32;
                mIndex++;
            }
            if (-assertionsDisabled || borrow == 0) {
                minuend.trimLeadingZeros();
                return minuend;
            }
            throw new AssertionError(Long.valueOf(borrow));
        }
        throw new AssertionError((Object) "result should be positive");
    }

    public FDBigInteger rightInplaceSub(FDBigInteger subtrahend) {
        if (-assertionsDisabled || size() >= subtrahend.size()) {
            long diff;
            if (subtrahend.isImmutable) {
                subtrahend = new FDBigInteger((int[]) subtrahend.data.clone(), subtrahend.offset);
            }
            int offsetDiff = this.offset - subtrahend.offset;
            int[] sData = subtrahend.data;
            int[] mData = this.data;
            int subLen = subtrahend.nWords;
            int minLen = this.nWords;
            int rLen;
            if (offsetDiff < 0) {
                rLen = minLen;
                if (minLen < sData.length) {
                    System.arraycopy(sData, 0, sData, -offsetDiff, subLen);
                    Arrays.fill(sData, 0, -offsetDiff, 0);
                } else {
                    int[] r = new int[minLen];
                    System.arraycopy(sData, 0, r, -offsetDiff, subLen);
                    sData = r;
                    subtrahend.data = r;
                }
                subtrahend.offset = this.offset;
                subLen -= offsetDiff;
                offsetDiff = 0;
            } else {
                rLen = minLen + offsetDiff;
                if (rLen >= sData.length) {
                    sData = Arrays.copyOf(sData, rLen);
                    subtrahend.data = sData;
                }
            }
            int sIndex = 0;
            long borrow = 0;
            while (sIndex < offsetDiff) {
                diff = (0 - (((long) sData[sIndex]) & LONG_MASK)) + borrow;
                sData[sIndex] = (int) diff;
                borrow = diff >> 32;
                sIndex++;
            }
            for (int mIndex = 0; mIndex < minLen; mIndex++) {
                diff = ((((long) mData[mIndex]) & LONG_MASK) - (((long) sData[sIndex]) & LONG_MASK)) + borrow;
                sData[sIndex] = (int) diff;
                borrow = diff >> 32;
                sIndex++;
            }
            if (-assertionsDisabled || borrow == 0) {
                subtrahend.nWords = sIndex;
                subtrahend.trimLeadingZeros();
                return subtrahend;
            }
            throw new AssertionError(Long.valueOf(borrow));
        }
        throw new AssertionError((Object) "result should be positive");
    }

    private static int checkZeroTail(int[] a, int from) {
        while (from > 0) {
            from--;
            if (a[from] != 0) {
                return 1;
            }
        }
        return 0;
    }

    public int cmp(FDBigInteger other) {
        int i = -1;
        int aSize = this.nWords + this.offset;
        int bSize = other.nWords + other.offset;
        if (aSize > bSize) {
            return 1;
        }
        if (aSize < bSize) {
            return -1;
        }
        int aLen = this.nWords;
        int bLen = other.nWords;
        while (aLen > 0 && bLen > 0) {
            aLen--;
            int a = this.data[aLen];
            bLen--;
            int b = other.data[bLen];
            if (a != b) {
                if ((((long) a) & LONG_MASK) >= (((long) b) & LONG_MASK)) {
                    i = 1;
                }
                return i;
            }
        }
        if (aLen > 0) {
            return checkZeroTail(this.data, aLen);
        }
        if (bLen > 0) {
            return -checkZeroTail(other.data, bLen);
        }
        return 0;
    }

    public int cmpPow52(int p5, int p2) {
        if (p5 != 0) {
            return cmp(big5pow(p5).leftShift(p2));
        }
        int wordcount = p2 >> 5;
        int bitcount = p2 & 31;
        int size = this.nWords + this.offset;
        if (size > wordcount + 1) {
            return 1;
        }
        if (size < wordcount + 1) {
            return -1;
        }
        int a = this.data[this.nWords - 1];
        int b = 1 << bitcount;
        if (a == b) {
            return checkZeroTail(this.data, this.nWords - 1);
        }
        return (((long) a) & LONG_MASK) < (((long) b) & LONG_MASK) ? -1 : 1;
    }

    public int addAndCmp(FDBigInteger x, FDBigInteger y) {
        FDBigInteger big;
        FDBigInteger small;
        int bSize;
        int sSize;
        int xSize = x.size();
        int ySize = y.size();
        if (xSize >= ySize) {
            big = x;
            small = y;
            bSize = xSize;
            sSize = ySize;
        } else {
            big = y;
            small = x;
            bSize = ySize;
            sSize = xSize;
        }
        int thSize = size();
        if (bSize == 0) {
            int i;
            if (thSize == 0) {
                i = 0;
            } else {
                i = 1;
            }
            return i;
        } else if (sSize == 0) {
            return cmp(big);
        } else {
            if (bSize > thSize) {
                return -1;
            }
            if (bSize + 1 < thSize) {
                return 1;
            }
            long top = ((long) big.data[big.nWords - 1]) & LONG_MASK;
            if (sSize == bSize) {
                top += ((long) small.data[small.nWords - 1]) & LONG_MASK;
            }
            long v;
            if ((top >>> 32) == 0) {
                if (((1 + top) >>> 32) == 0) {
                    if (bSize < thSize) {
                        return 1;
                    }
                    v = ((long) this.data[this.nWords - 1]) & LONG_MASK;
                    if (v < top) {
                        return -1;
                    }
                    if (v > 1 + top) {
                        return 1;
                    }
                }
            } else if (bSize + 1 > thSize) {
                return -1;
            } else {
                top >>>= 32;
                v = ((long) this.data[this.nWords - 1]) & LONG_MASK;
                if (v < top) {
                    return -1;
                }
                if (v > 1 + top) {
                    return 1;
                }
            }
            return cmp(big.add(small));
        }
    }

    public void makeImmutable() {
        this.isImmutable = true;
    }

    private FDBigInteger mult(int i) {
        if (this.nWords == 0) {
            return this;
        }
        int[] r = new int[(this.nWords + 1)];
        mult(this.data, this.nWords, i, r);
        return new FDBigInteger(r, this.offset);
    }

    private FDBigInteger mult(FDBigInteger other) {
        if (this.nWords == 0) {
            return this;
        }
        if (size() == 1) {
            return other.mult(this.data[0]);
        }
        if (other.nWords == 0) {
            return other;
        }
        if (other.size() == 1) {
            return mult(other.data[0]);
        }
        int[] r = new int[(this.nWords + other.nWords)];
        mult(this.data, this.nWords, other.data, other.nWords, r);
        return new FDBigInteger(r, this.offset + other.offset);
    }

    private FDBigInteger add(FDBigInteger other) {
        FDBigInteger big;
        int bigLen;
        FDBigInteger small;
        int smallLen;
        int tSize = size();
        int oSize = other.size();
        if (tSize >= oSize) {
            big = this;
            bigLen = tSize;
            small = other;
            smallLen = oSize;
        } else {
            big = other;
            bigLen = oSize;
            small = this;
            smallLen = tSize;
        }
        int[] r = new int[(bigLen + 1)];
        int i = 0;
        long carry = 0;
        while (i < smallLen) {
            carry += (i < big.offset ? 0 : ((long) big.data[i - big.offset]) & LONG_MASK) + (i < small.offset ? 0 : ((long) small.data[i - small.offset]) & LONG_MASK);
            r[i] = (int) carry;
            carry >>= 32;
            i++;
        }
        while (i < bigLen) {
            carry += i < big.offset ? 0 : ((long) big.data[i - big.offset]) & LONG_MASK;
            r[i] = (int) carry;
            carry >>= 32;
            i++;
        }
        r[bigLen] = (int) carry;
        return new FDBigInteger(r, 0);
    }

    private void multAddMe(int iv, int addend) {
        long v = ((long) iv) & LONG_MASK;
        long p = ((((long) this.data[0]) & LONG_MASK) * v) + (((long) addend) & LONG_MASK);
        this.data[0] = (int) p;
        p >>>= 32;
        for (int i = 1; i < this.nWords; i++) {
            p += (((long) this.data[i]) & LONG_MASK) * v;
            this.data[i] = (int) p;
            p >>>= 32;
        }
        if (p != 0) {
            int[] iArr = this.data;
            int i2 = this.nWords;
            this.nWords = i2 + 1;
            iArr[i2] = (int) p;
        }
    }

    private long multDiffMe(long q, FDBigInteger S) {
        long diff = 0;
        if (q != 0) {
            int deltaSize = S.offset - this.offset;
            int[] sd;
            int[] td;
            int sIndex;
            int tIndex;
            if (deltaSize >= 0) {
                sd = S.data;
                td = this.data;
                sIndex = 0;
                tIndex = deltaSize;
                while (sIndex < S.nWords) {
                    diff += (((long) td[tIndex]) & LONG_MASK) - ((((long) sd[sIndex]) & LONG_MASK) * q);
                    td[tIndex] = (int) diff;
                    diff >>= 32;
                    sIndex++;
                    tIndex++;
                }
            } else {
                deltaSize = -deltaSize;
                int[] rd = new int[(this.nWords + deltaSize)];
                sIndex = 0;
                int rIndex = 0;
                sd = S.data;
                while (rIndex < deltaSize && sIndex < S.nWords) {
                    diff -= (((long) sd[sIndex]) & LONG_MASK) * q;
                    rd[rIndex] = (int) diff;
                    diff >>= 32;
                    sIndex++;
                    rIndex++;
                }
                tIndex = 0;
                td = this.data;
                while (sIndex < S.nWords) {
                    diff += (((long) td[tIndex]) & LONG_MASK) - ((((long) sd[sIndex]) & LONG_MASK) * q);
                    rd[rIndex] = (int) diff;
                    diff >>= 32;
                    sIndex++;
                    tIndex++;
                    rIndex++;
                }
                this.nWords += deltaSize;
                this.offset -= deltaSize;
                this.data = rd;
            }
        }
        return diff;
    }

    private static int multAndCarryBy10(int[] src, int srcLen, int[] dst) {
        long carry = 0;
        for (int i = 0; i < srcLen; i++) {
            long product = ((((long) src[i]) & LONG_MASK) * 10) + carry;
            dst[i] = (int) product;
            carry = product >>> 32;
        }
        return (int) carry;
    }

    private static void mult(int[] src, int srcLen, int value, int[] dst) {
        long val = ((long) value) & LONG_MASK;
        long carry = 0;
        for (int i = 0; i < srcLen; i++) {
            long product = ((((long) src[i]) & LONG_MASK) * val) + carry;
            dst[i] = (int) product;
            carry = product >>> 32;
        }
        dst[srcLen] = (int) carry;
    }

    private static void mult(int[] src, int srcLen, int v0, int v1, int[] dst) {
        int j;
        long product;
        long v = ((long) v0) & LONG_MASK;
        long carry = 0;
        for (j = 0; j < srcLen; j++) {
            product = ((((long) src[j]) & LONG_MASK) * v) + carry;
            dst[j] = (int) product;
            carry = product >>> 32;
        }
        dst[srcLen] = (int) carry;
        v = ((long) v1) & LONG_MASK;
        carry = 0;
        for (j = 0; j < srcLen; j++) {
            product = ((((long) dst[j + 1]) & LONG_MASK) + ((((long) src[j]) & LONG_MASK) * v)) + carry;
            dst[j + 1] = (int) product;
            carry = product >>> 32;
        }
        dst[srcLen + 1] = (int) carry;
    }

    private static FDBigInteger big5pow(int p) {
        if (!-assertionsDisabled && p < 0) {
            throw new AssertionError(Integer.valueOf(p));
        } else if (p < MAX_FIVE_POW) {
            return POW_5_CACHE[p];
        } else {
            return big5powRec(p);
        }
    }

    private static FDBigInteger big5powRec(int p) {
        if (p < MAX_FIVE_POW) {
            return POW_5_CACHE[p];
        }
        int q = p >> 1;
        int r = p - q;
        FDBigInteger bigq = big5powRec(q);
        if (r < SMALL_5_POW.length) {
            return bigq.mult(SMALL_5_POW[r]);
        }
        return bigq.mult(big5powRec(r));
    }

    public String toHexString() {
        if (this.nWords == 0) {
            return "0";
        }
        int i;
        StringBuilder sb = new StringBuilder((this.nWords + this.offset) * 8);
        for (i = this.nWords - 1; i >= 0; i--) {
            String subStr = Integer.toHexString(this.data[i]);
            for (int j = subStr.length(); j < 8; j++) {
                sb.append('0');
            }
            sb.append(subStr);
        }
        for (i = this.offset; i > 0; i--) {
            sb.append("00000000");
        }
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    public BigInteger toBigInteger() {
        byte[] magnitude = new byte[((this.nWords * 4) + 1)];
        for (int i = 0; i < this.nWords; i++) {
            int w = this.data[i];
            magnitude[(magnitude.length - (i * 4)) - 1] = (byte) w;
            magnitude[(magnitude.length - (i * 4)) - 2] = (byte) (w >> 8);
            magnitude[(magnitude.length - (i * 4)) - 3] = (byte) (w >> 16);
            magnitude[(magnitude.length - (i * 4)) - 4] = (byte) (w >> 24);
        }
        return new BigInteger(magnitude).shiftLeft(this.offset * 32);
    }

    public String toString() {
        return toBigInteger().toString();
    }
}
