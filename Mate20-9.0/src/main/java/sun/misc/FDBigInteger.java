package sun.misc;

import java.math.BigInteger;
import java.util.Arrays;

public class FDBigInteger {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final long[] LONG_5_POW = {1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125, 6103515625L, 30517578125L, 152587890625L, 762939453125L, 3814697265625L, 19073486328125L, 95367431640625L, 476837158203125L, 2384185791015625L, 11920928955078125L, 59604644775390625L, 298023223876953125L, 1490116119384765625L};
    private static final long LONG_MASK = 4294967295L;
    private static final int MAX_FIVE_POW = 340;
    private static final FDBigInteger[] POW_5_CACHE = new FDBigInteger[MAX_FIVE_POW];
    static final int[] SMALL_5_POW = {1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125};
    public static final FDBigInteger ZERO = new FDBigInteger(new int[0], 0);
    private int[] data;
    private boolean isImmutable = $assertionsDisabled;
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
            FDBigInteger mult = prev.mult(5);
            prev = mult;
            fDBigIntegerArr[i] = mult;
            prev.makeImmutable();
            i++;
        }
        ZERO.makeImmutable();
    }

    private FDBigInteger(int[] data2, int offset2) {
        this.data = data2;
        this.offset = offset2;
        this.nWords = data2.length;
        trimLeadingZeros();
    }

    public FDBigInteger(long lValue, char[] digits, int kDigits, int nDigits) {
        int v = 0;
        this.data = new int[Math.max((nDigits + 8) / 9, 2)];
        this.data[0] = (int) lValue;
        this.data[1] = (int) (lValue >>> 32);
        this.offset = 0;
        this.nWords = 2;
        int v2 = kDigits;
        int limit = nDigits - 5;
        while (v2 < limit) {
            int ilim = v2 + 5;
            int i = v2 + 1;
            int v3 = digits[v2] - '0';
            while (i < ilim) {
                v3 = ((10 * v3) + digits[i]) - 48;
                i++;
            }
            multAddMe(100000, v3);
            v2 = i;
        }
        int factor = 1;
        while (v2 < nDigits) {
            v = ((10 * v) + digits[v2]) - 48;
            factor *= 10;
            v2++;
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
        int bitcount = p2 & 31;
        if (bitcount == 0) {
            return new FDBigInteger(new int[]{pow5}, wordcount);
        }
        return new FDBigInteger(new int[]{pow5 << bitcount, pow5 >>> (32 - bitcount)}, wordcount);
    }

    public static FDBigInteger valueOfMulPow52(long value, int p5, int p2) {
        int[] r;
        long j = value;
        int i = p5;
        int i2 = p2;
        int v0 = (int) j;
        int v1 = (int) (j >>> 32);
        int wordcount = i2 >> 5;
        int bitcount = i2 & 31;
        if (i != 0) {
            if (i < SMALL_5_POW.length) {
                long pow5 = ((long) SMALL_5_POW[i]) & LONG_MASK;
                long carry = (((long) v0) & LONG_MASK) * pow5;
                int v02 = (int) carry;
                long carry2 = ((((long) v1) & LONG_MASK) * pow5) + (carry >>> 32);
                int v12 = (int) carry2;
                long j2 = pow5;
                int v2 = (int) (carry2 >>> 32);
                if (bitcount == 0) {
                    return new FDBigInteger(new int[]{v02, v12, v2}, wordcount);
                }
                return new FDBigInteger(new int[]{v02 << bitcount, (v12 << bitcount) | (v02 >>> (32 - bitcount)), (v2 << bitcount) | (v12 >>> (32 - bitcount)), v2 >>> (32 - bitcount)}, wordcount);
            }
            FDBigInteger pow52 = big5pow(p5);
            if (v1 == 0) {
                r = new int[(pow52.nWords + 1 + (i2 != 0 ? 1 : 0))];
                mult(pow52.data, pow52.nWords, v0, r);
            } else {
                r = new int[(pow52.nWords + 2 + (i2 != 0 ? 1 : 0))];
                mult(pow52.data, pow52.nWords, v0, v1, r);
            }
            return new FDBigInteger(r, pow52.offset).leftShift(i2);
        } else if (i2 == 0) {
            return new FDBigInteger(new int[]{v0, v1}, 0);
        } else if (bitcount == 0) {
            return new FDBigInteger(new int[]{v0, v1}, wordcount);
        } else {
            return new FDBigInteger(new int[]{v0 << bitcount, (v1 << bitcount) | (v0 >>> (32 - bitcount)), v1 >>> (32 - bitcount)}, wordcount);
        }
    }

    private static FDBigInteger valueOfPow2(int p2) {
        return new FDBigInteger(new int[]{1 << (p2 & 31)}, p2 >> 5);
    }

    private void trimLeadingZeros() {
        int i = this.nWords;
        if (i > 0) {
            int i2 = i - 1;
            if (this.data[i2] == 0) {
                while (i2 > 0 && this.data[i2 - 1] == 0) {
                    i2--;
                }
                this.nWords = i2;
                if (i2 == 0) {
                    this.offset = 0;
                }
            }
        }
    }

    public int getNormalizationBias() {
        if (this.nWords != 0) {
            int zeros = Integer.numberOfLeadingZeros(this.data[this.nWords - 1]);
            return zeros < 4 ? 28 + zeros : zeros - 4;
        }
        throw new IllegalArgumentException("Zero value cannot be normalized");
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
        int[] result;
        if (shift == 0 || this.nWords == 0) {
            return this;
        }
        int wordcount = shift >> 5;
        int bitcount = shift & 31;
        if (!this.isImmutable) {
            if (bitcount != 0) {
                int anticount = 32 - bitcount;
                if ((this.data[0] << bitcount) == 0) {
                    int idx = 0;
                    int prev = this.data[0];
                    while (idx < this.nWords - 1) {
                        int v = prev >>> anticount;
                        prev = this.data[idx + 1];
                        this.data[idx] = v | (prev << bitcount);
                        idx++;
                    }
                    int v2 = prev >>> anticount;
                    this.data[idx] = v2;
                    if (v2 == 0) {
                        this.nWords--;
                    }
                    this.offset++;
                } else {
                    int idx2 = this.nWords - 1;
                    int prev2 = this.data[idx2];
                    int hi = prev2 >>> anticount;
                    int[] result2 = this.data;
                    int[] src = this.data;
                    if (hi != 0) {
                        if (this.nWords == this.data.length) {
                            int[] iArr = new int[(this.nWords + 1)];
                            result2 = iArr;
                            this.data = iArr;
                        }
                        int i = this.nWords;
                        this.nWords = i + 1;
                        result2[i] = hi;
                    }
                    leftShift(src, idx2, result2, bitcount, anticount, prev2);
                }
            }
            this.offset += wordcount;
            return this;
        } else if (bitcount == 0) {
            return new FDBigInteger(Arrays.copyOf(this.data, this.nWords), this.offset + wordcount);
        } else {
            int anticount2 = 32 - bitcount;
            int idx3 = this.nWords - 1;
            int prev3 = this.data[idx3];
            int hi2 = prev3 >>> anticount2;
            if (hi2 != 0) {
                result = new int[(this.nWords + 1)];
                result[this.nWords] = hi2;
            } else {
                result = new int[this.nWords];
            }
            int[] result3 = result;
            leftShift(this.data, idx3, result3, bitcount, anticount2, prev3);
            return new FDBigInteger(result3, this.offset + wordcount);
        }
    }

    private int size() {
        return this.nWords + this.offset;
    }

    public int quoRemIteration(FDBigInteger S) throws IllegalArgumentException {
        FDBigInteger fDBigInteger = S;
        int thSize = size();
        int sSize = S.size();
        if (thSize < sSize) {
            int p = multAndCarryBy10(this.data, this.nWords, this.data);
            if (p != 0) {
                int[] iArr = this.data;
                int i = this.nWords;
                this.nWords = i + 1;
                iArr[i] = p;
            } else {
                trimLeadingZeros();
            }
            return 0;
        } else if (thSize <= sSize) {
            long q = (((long) this.data[this.nWords - 1]) & LONG_MASK) / (((long) fDBigInteger.data[fDBigInteger.nWords - 1]) & LONG_MASK);
            if (multDiffMe(q, fDBigInteger) != 0) {
                long sum = 0;
                int tStart = fDBigInteger.offset - this.offset;
                int[] sd = fDBigInteger.data;
                int[] td = this.data;
                for (long j = 0; sum == j; j = 0) {
                    long sum2 = sum;
                    int sIndex = 0;
                    int tIndex = tStart;
                    while (tIndex < this.nWords) {
                        long sum3 = sum2 + (((long) td[tIndex]) & LONG_MASK) + (((long) sd[sIndex]) & LONG_MASK);
                        td[tIndex] = (int) sum3;
                        sum2 = sum3 >>> 32;
                        sIndex++;
                        tIndex++;
                        thSize = thSize;
                        FDBigInteger fDBigInteger2 = S;
                    }
                    q--;
                    sum = sum2;
                    thSize = thSize;
                    FDBigInteger fDBigInteger3 = S;
                }
            }
            int multAndCarryBy10 = multAndCarryBy10(this.data, this.nWords, this.data);
            trimLeadingZeros();
            return (int) q;
        } else {
            throw new IllegalArgumentException("disparate values");
        }
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
            if (this.nWords == this.data.length) {
                if (this.data[0] == 0) {
                    int[] iArr = this.data;
                    int[] iArr2 = this.data;
                    int i = this.nWords - 1;
                    this.nWords = i;
                    System.arraycopy((Object) iArr, 1, (Object) iArr2, 0, i);
                    this.offset++;
                } else {
                    this.data = Arrays.copyOf(this.data, this.data.length + 1);
                }
            }
            int[] iArr3 = this.data;
            int i2 = this.nWords;
            this.nWords = i2 + 1;
            iArr3[i2] = p;
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
            if (p5 < SMALL_5_POW.length) {
                int[] r = new int[(this.nWords + 1 + extraSize)];
                mult(this.data, this.nWords, SMALL_5_POW[p5], r);
                res = new FDBigInteger(r, this.offset);
            } else {
                FDBigInteger pow5 = big5pow(p5);
                int[] r2 = new int[(this.nWords + pow5.size() + extraSize)];
                mult(this.data, this.nWords, pow5.data, pow5.nWords, r2);
                res = new FDBigInteger(r2, this.offset + pow5.offset);
            }
        }
        return res.leftShift(p2);
    }

    private static void mult(int[] s1, int s1Len, int[] s2, int s2Len, int[] dst) {
        int i = s2Len;
        for (int i2 = 0; i2 < s1Len; i2++) {
            long v = ((long) s1[i2]) & LONG_MASK;
            long p = 0;
            int j = 0;
            while (j < i) {
                long p2 = p + (((long) dst[i2 + j]) & LONG_MASK) + ((((long) s2[j]) & LONG_MASK) * v);
                dst[i2 + j] = (int) p2;
                p = p2 >>> 32;
                j++;
                int i3 = s1Len;
            }
            dst[i2 + i] = (int) p;
        }
    }

    public FDBigInteger leftInplaceSub(FDBigInteger subtrahend) {
        FDBigInteger minuend;
        FDBigInteger fDBigInteger = subtrahend;
        if (this.isImmutable) {
            minuend = new FDBigInteger((int[]) this.data.clone(), this.offset);
        } else {
            minuend = this;
        }
        int offsetDiff = fDBigInteger.offset - minuend.offset;
        int[] sData = fDBigInteger.data;
        int[] mData = minuend.data;
        int subLen = fDBigInteger.nWords;
        int minLen = minuend.nWords;
        int sIndex = 0;
        if (offsetDiff < 0) {
            int rLen = minLen - offsetDiff;
            if (rLen < mData.length) {
                System.arraycopy((Object) mData, 0, (Object) mData, -offsetDiff, minLen);
                Arrays.fill(mData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[rLen];
                System.arraycopy((Object) mData, 0, (Object) r, -offsetDiff, minLen);
                mData = r;
                minuend.data = r;
            }
            minuend.offset = fDBigInteger.offset;
            minLen = rLen;
            minuend.nWords = rLen;
            offsetDiff = 0;
        }
        long borrow = 0;
        int mIndex = offsetDiff;
        while (sIndex < subLen && mIndex < minLen) {
            long diff = ((((long) mData[mIndex]) & LONG_MASK) - (((long) sData[sIndex]) & LONG_MASK)) + borrow;
            mData[mIndex] = (int) diff;
            borrow = diff >> 32;
            sIndex++;
            mIndex++;
            sData = sData;
            offsetDiff = offsetDiff;
            FDBigInteger fDBigInteger2 = subtrahend;
        }
        int[] iArr = sData;
        while (borrow != 0 && mIndex < minLen) {
            long diff2 = (((long) mData[mIndex]) & LONG_MASK) + borrow;
            mData[mIndex] = (int) diff2;
            borrow = diff2 >> 32;
            mIndex++;
        }
        minuend.trimLeadingZeros();
        return minuend;
    }

    public FDBigInteger rightInplaceSub(FDBigInteger subtrahend) {
        FDBigInteger subtrahend2 = subtrahend;
        FDBigInteger minuend = this;
        if (subtrahend2.isImmutable) {
            subtrahend2 = new FDBigInteger((int[]) subtrahend2.data.clone(), subtrahend2.offset);
        }
        int offsetDiff = minuend.offset - subtrahend2.offset;
        int[] sData = subtrahend2.data;
        int[] mData = minuend.data;
        int subLen = subtrahend2.nWords;
        int minLen = minuend.nWords;
        if (offsetDiff < 0) {
            int rLen = minLen;
            if (rLen < sData.length) {
                System.arraycopy((Object) sData, 0, (Object) sData, -offsetDiff, subLen);
                Arrays.fill(sData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[rLen];
                System.arraycopy((Object) sData, 0, (Object) r, -offsetDiff, subLen);
                sData = r;
                subtrahend2.data = r;
            }
            subtrahend2.offset = minuend.offset;
            subLen -= offsetDiff;
            offsetDiff = 0;
        } else {
            int rLen2 = minLen + offsetDiff;
            if (rLen2 >= sData.length) {
                int[] copyOf = Arrays.copyOf(sData, rLen2);
                sData = copyOf;
                subtrahend2.data = copyOf;
            }
        }
        int sIndex = 0;
        long borrow = 0;
        while (sIndex < offsetDiff) {
            FDBigInteger minuend2 = minuend;
            long diff = (0 - (((long) sData[sIndex]) & LONG_MASK)) + borrow;
            sData[sIndex] = (int) diff;
            borrow = diff >> 32;
            sIndex++;
            minuend = minuend2;
            offsetDiff = offsetDiff;
        }
        int i = offsetDiff;
        int mIndex = 0;
        while (true) {
            int mIndex2 = mIndex;
            if (mIndex2 < minLen) {
                long diff2 = ((((long) mData[mIndex2]) & LONG_MASK) - (((long) sData[sIndex]) & LONG_MASK)) + borrow;
                sData[sIndex] = (int) diff2;
                borrow = diff2 >> 32;
                sIndex++;
                mIndex = mIndex2 + 1;
                mData = mData;
                subLen = subLen;
            } else {
                int i2 = subLen;
                subtrahend2.nWords = sIndex;
                subtrahend2.trimLeadingZeros();
                return subtrahend2;
            }
        }
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
        int aSize = this.nWords + this.offset;
        int bSize = other.nWords + other.offset;
        int i = 1;
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
                if ((((long) a) & LONG_MASK) < (LONG_MASK & ((long) b))) {
                    i = -1;
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
        int i = 1;
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
        if ((((long) a) & LONG_MASK) < (LONG_MASK & ((long) b))) {
            i = -1;
        }
        return i;
    }

    public int addAndCmp(FDBigInteger x, FDBigInteger y) {
        int sSize;
        int bSize;
        FDBigInteger small;
        FDBigInteger big;
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
        int i = 1;
        if (bSize == 0) {
            if (thSize == 0) {
                i = 0;
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
            if ((top >>> 32) == 0) {
                if (((top + 1) >>> 32) == 0) {
                    if (bSize < thSize) {
                        return 1;
                    }
                    long v = LONG_MASK & ((long) this.data[this.nWords - 1]);
                    if (v < top) {
                        return -1;
                    }
                    if (v > top + 1) {
                        return 1;
                    }
                }
            } else if (bSize + 1 > thSize) {
                return -1;
            } else {
                long top2 = top >>> 32;
                long v2 = LONG_MASK & ((long) this.data[this.nWords - 1]);
                if (v2 < top2) {
                    return -1;
                }
                if (v2 > top2 + 1) {
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
        int smallLen;
        FDBigInteger small;
        int bigLen;
        FDBigInteger big;
        long j;
        int oSize;
        int tSize;
        long j2;
        int tSize2 = size();
        int oSize2 = other.size();
        if (tSize2 >= oSize2) {
            big = this;
            bigLen = tSize2;
            small = other;
            smallLen = oSize2;
        } else {
            big = other;
            bigLen = oSize2;
            small = this;
            smallLen = tSize2;
        }
        int[] r = new int[(bigLen + 1)];
        int i = 0;
        long carry = 0;
        while (i < smallLen) {
            if (i < big.offset) {
                j = 0;
            } else {
                j = ((long) big.data[i - big.offset]) & LONG_MASK;
            }
            if (i < small.offset) {
                tSize = tSize2;
                oSize = oSize2;
                j2 = 0;
            } else {
                tSize = tSize2;
                oSize = oSize2;
                j2 = ((long) small.data[i - small.offset]) & LONG_MASK;
            }
            long carry2 = carry + j + j2;
            r[i] = (int) carry2;
            carry = carry2 >> 32;
            i++;
            tSize2 = tSize;
            oSize2 = oSize;
        }
        int i2 = oSize2;
        while (i < bigLen) {
            long carry3 = carry + (i < big.offset ? 0 : ((long) big.data[i - big.offset]) & LONG_MASK);
            r[i] = (int) carry3;
            carry = carry3 >> 32;
            i++;
        }
        r[bigLen] = (int) carry;
        return new FDBigInteger(r, 0);
    }

    private void multAddMe(int iv, int addend) {
        long v = ((long) iv) & LONG_MASK;
        long p = ((((long) this.data[0]) & LONG_MASK) * v) + (((long) addend) & LONG_MASK);
        this.data[0] = (int) p;
        long p2 = p >>> 32;
        for (int i = 1; i < this.nWords; i++) {
            long p3 = p2 + ((((long) this.data[i]) & LONG_MASK) * v);
            this.data[i] = (int) p3;
            p2 = p3 >>> 32;
        }
        if (p2 != 0) {
            int[] iArr = this.data;
            int i2 = this.nWords;
            this.nWords = i2 + 1;
            iArr[i2] = (int) p2;
        }
    }

    private long multDiffMe(long q, FDBigInteger S) {
        FDBigInteger fDBigInteger = S;
        long diff = 0;
        if (q == 0) {
            return 0;
        }
        int deltaSize = fDBigInteger.offset - this.offset;
        if (deltaSize >= 0) {
            int[] sd = fDBigInteger.data;
            int[] td = this.data;
            int sIndex = 0;
            long diff2 = 0;
            int tIndex = deltaSize;
            while (sIndex < fDBigInteger.nWords) {
                int tIndex2 = tIndex;
                long diff3 = diff2 + ((((long) td[tIndex]) & LONG_MASK) - ((((long) sd[sIndex]) & LONG_MASK) * q));
                td[tIndex2] = (int) diff3;
                diff2 = diff3 >> 32;
                sIndex++;
                tIndex = tIndex2 + 1;
                deltaSize = deltaSize;
                fDBigInteger = S;
            }
            return diff2;
        }
        int deltaSize2 = -deltaSize;
        int[] rd = new int[(this.nWords + deltaSize2)];
        int sIndex2 = 0;
        int rIndex = 0;
        FDBigInteger fDBigInteger2 = S;
        int[] sd2 = fDBigInteger2.data;
        while (rIndex < deltaSize2 && sIndex2 < fDBigInteger2.nWords) {
            long diff4 = diff - ((((long) sd2[sIndex2]) & LONG_MASK) * q);
            rd[rIndex] = (int) diff4;
            diff = diff4 >> 32;
            sIndex2++;
            rIndex++;
        }
        int tIndex3 = 0;
        int[] td2 = this.data;
        while (sIndex2 < fDBigInteger2.nWords) {
            long diff5 = diff + ((((long) td2[tIndex3]) & LONG_MASK) - ((((long) sd2[sIndex2]) & LONG_MASK) * q));
            rd[rIndex] = (int) diff5;
            diff = diff5 >> 32;
            sIndex2++;
            tIndex3++;
            rIndex++;
            td2 = td2;
            sd2 = sd2;
            fDBigInteger2 = S;
        }
        int[] iArr = td2;
        this.nWords += deltaSize2;
        this.offset -= deltaSize2;
        this.data = rd;
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
        int i = srcLen;
        long v = ((long) v0) & LONG_MASK;
        int j = 0;
        long carry = 0;
        for (int j2 = 0; j2 < i; j2++) {
            long product = ((((long) src[j2]) & LONG_MASK) * v) + carry;
            dst[j2] = (int) product;
            carry = product >>> 32;
        }
        dst[i] = (int) carry;
        long v2 = ((long) v1) & LONG_MASK;
        long carry2 = 0;
        while (j < i) {
            int j3 = j;
            long product2 = (((long) dst[j + 1]) & LONG_MASK) + ((((long) src[j]) & LONG_MASK) * v2) + carry2;
            dst[j3 + 1] = (int) product2;
            carry2 = product2 >>> 32;
            j = j3 + 1;
        }
        dst[i + 1] = (int) carry2;
    }

    private static FDBigInteger big5pow(int p) {
        if (p < MAX_FIVE_POW) {
            return POW_5_CACHE[p];
        }
        return big5powRec(p);
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
        StringBuilder sb = new StringBuilder((this.nWords + this.offset) * 8);
        for (int i = this.nWords - 1; i >= 0; i--) {
            String subStr = Integer.toHexString(this.data[i]);
            for (int j = subStr.length(); j < 8; j++) {
                sb.append('0');
            }
            sb.append(subStr);
        }
        for (int i2 = this.offset; i2 > 0; i2--) {
            sb.append("00000000");
        }
        return sb.toString();
    }

    public BigInteger toBigInteger() {
        byte[] magnitude = new byte[((this.nWords * 4) + 1)];
        for (int i = 0; i < this.nWords; i++) {
            int w = this.data[i];
            magnitude[(magnitude.length - (4 * i)) - 1] = (byte) w;
            magnitude[(magnitude.length - (4 * i)) - 2] = (byte) (w >> 8);
            magnitude[(magnitude.length - (4 * i)) - 3] = (byte) (w >> 16);
            magnitude[(magnitude.length - (4 * i)) - 4] = (byte) (w >> 24);
        }
        return new BigInteger(magnitude).shiftLeft(this.offset * 32);
    }

    public String toString() {
        return toBigInteger().toString();
    }
}
