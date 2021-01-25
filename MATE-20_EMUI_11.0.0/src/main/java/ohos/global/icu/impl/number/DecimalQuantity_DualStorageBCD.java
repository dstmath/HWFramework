package ohos.global.icu.impl.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.text.DateFormat;

public final class DecimalQuantity_DualStorageBCD extends DecimalQuantity_AbstractBCD {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private byte[] bcdBytes;
    private long bcdLong = 0;
    private boolean usingBytes = false;

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public int maxRepresentableDigits() {
        return Integer.MAX_VALUE;
    }

    public DecimalQuantity_DualStorageBCD() {
        setBcdToZero();
        this.flags = 0;
    }

    public DecimalQuantity_DualStorageBCD(long j) {
        setToLong(j);
    }

    public DecimalQuantity_DualStorageBCD(int i) {
        setToInt(i);
    }

    public DecimalQuantity_DualStorageBCD(double d) {
        setToDouble(d);
    }

    public DecimalQuantity_DualStorageBCD(BigInteger bigInteger) {
        setToBigInteger(bigInteger);
    }

    public DecimalQuantity_DualStorageBCD(BigDecimal bigDecimal) {
        setToBigDecimal(bigDecimal);
    }

    public DecimalQuantity_DualStorageBCD(DecimalQuantity_DualStorageBCD decimalQuantity_DualStorageBCD) {
        copyFrom(decimalQuantity_DualStorageBCD);
    }

    public DecimalQuantity_DualStorageBCD(Number number) {
        if (number instanceof Long) {
            setToLong(number.longValue());
        } else if (number instanceof Integer) {
            setToInt(number.intValue());
        } else if (number instanceof Float) {
            setToDouble(number.doubleValue());
        } else if (number instanceof Double) {
            setToDouble(number.doubleValue());
        } else if (number instanceof BigInteger) {
            setToBigInteger((BigInteger) number);
        } else if (number instanceof BigDecimal) {
            setToBigDecimal((BigDecimal) number);
        } else if (number instanceof ohos.global.icu.math.BigDecimal) {
            setToBigDecimal(((ohos.global.icu.math.BigDecimal) number).toBigDecimal());
        } else {
            throw new IllegalArgumentException("Number is of an unsupported type: " + number.getClass().getName());
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public DecimalQuantity createCopy() {
        return new DecimalQuantity_DualStorageBCD(this);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public byte getDigitPos(int i) {
        if (this.usingBytes) {
            if (i < 0 || i >= this.precision) {
                return 0;
            }
            return this.bcdBytes[i];
        } else if (i < 0 || i >= 16) {
            return 0;
        } else {
            return (byte) ((int) ((this.bcdLong >>> (i * 4)) & 15));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void setDigitPos(int i, byte b) {
        if (this.usingBytes) {
            ensureCapacity(i + 1);
            this.bcdBytes[i] = b;
        } else if (i >= 16) {
            switchStorage();
            ensureCapacity(i + 1);
            this.bcdBytes[i] = b;
        } else {
            int i2 = i * 4;
            this.bcdLong = (((long) b) << i2) | (this.bcdLong & (~(15 << i2)));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void shiftLeft(int i) {
        if (!this.usingBytes && this.precision + i > 16) {
            switchStorage();
        }
        if (this.usingBytes) {
            ensureCapacity(this.precision + i);
            int i2 = (this.precision + i) - 1;
            while (i2 >= i) {
                byte[] bArr = this.bcdBytes;
                bArr[i2] = bArr[i2 - i];
                i2--;
            }
            while (i2 >= 0) {
                this.bcdBytes[i2] = 0;
                i2--;
            }
        } else {
            this.bcdLong <<= i * 4;
        }
        this.scale -= i;
        this.precision += i;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void shiftRight(int i) {
        if (this.usingBytes) {
            int i2 = 0;
            while (i2 < this.precision - i) {
                byte[] bArr = this.bcdBytes;
                bArr[i2] = bArr[i2 + i];
                i2++;
            }
            while (i2 < this.precision) {
                this.bcdBytes[i2] = 0;
                i2++;
            }
        } else {
            this.bcdLong >>>= i * 4;
        }
        this.scale += i;
        this.precision -= i;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void popFromLeft(int i) {
        if (this.usingBytes) {
            int i2 = this.precision;
            while (true) {
                i2--;
                if (i2 < this.precision - i) {
                    break;
                }
                this.bcdBytes[i2] = 0;
            }
        } else {
            this.bcdLong &= (1 << ((this.precision - i) * 4)) - 1;
        }
        this.precision -= i;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void setBcdToZero() {
        if (this.usingBytes) {
            this.bcdBytes = null;
            this.usingBytes = false;
        }
        this.bcdLong = 0;
        this.scale = 0;
        this.precision = 0;
        this.isApproximate = false;
        this.origDouble = XPath.MATCH_SCORE_QNAME;
        this.origDelta = 0;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void readIntToBcd(int i) {
        long j = 0;
        int i2 = 16;
        while (i != 0) {
            j = (j >>> 4) + ((((long) i) % 10) << 60);
            i /= 10;
            i2--;
        }
        this.bcdLong = j >>> (i2 * 4);
        this.scale = 0;
        this.precision = 16 - i2;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void readLongToBcd(long j) {
        if (j >= 10000000000000000L) {
            ensureCapacity();
            int i = 0;
            while (j != 0) {
                this.bcdBytes[i] = (byte) ((int) (j % 10));
                j /= 10;
                i++;
            }
            this.scale = 0;
            this.precision = i;
            return;
        }
        int i2 = 16;
        long j2 = 0;
        while (j != 0) {
            j2 = (j2 >>> 4) + ((j % 10) << 60);
            j /= 10;
            i2--;
        }
        this.bcdLong = j2 >>> (i2 * 4);
        this.scale = 0;
        this.precision = 16 - i2;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void readBigIntegerToBcd(BigInteger bigInteger) {
        ensureCapacity();
        int i = 0;
        while (bigInteger.signum() != 0) {
            BigInteger[] divideAndRemainder = bigInteger.divideAndRemainder(BigInteger.TEN);
            int i2 = i + 1;
            ensureCapacity(i2);
            this.bcdBytes[i] = divideAndRemainder[1].byteValue();
            bigInteger = divideAndRemainder[0];
            i = i2;
        }
        this.scale = 0;
        this.precision = i;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public BigDecimal bcdToBigDecimal() {
        BigDecimal bigDecimal;
        if (this.usingBytes) {
            BigDecimal bigDecimal2 = new BigDecimal(toNumberString());
            return isNegative() ? bigDecimal2.negate() : bigDecimal2;
        }
        long j = 0;
        for (int i = this.precision - 1; i >= 0; i--) {
            j = (j * 10) + ((long) getDigitPos(i));
        }
        BigDecimal valueOf = BigDecimal.valueOf(j);
        if (((long) (valueOf.scale() + this.scale)) <= -2147483648L) {
            bigDecimal = BigDecimal.ZERO;
        } else {
            bigDecimal = valueOf.scaleByPowerOfTen(this.scale);
        }
        return isNegative() ? bigDecimal.negate() : bigDecimal;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void compact() {
        if (this.usingBytes) {
            int i = 0;
            while (i < this.precision && this.bcdBytes[i] == 0) {
                i++;
            }
            if (i == this.precision) {
                setBcdToZero();
                return;
            }
            shiftRight(i);
            int i2 = this.precision - 1;
            while (i2 >= 0 && this.bcdBytes[i2] == 0) {
                i2--;
            }
            this.precision = i2 + 1;
            if (this.precision <= 16) {
                switchStorage();
                return;
            }
            return;
        }
        long j = this.bcdLong;
        if (j == 0) {
            setBcdToZero();
            return;
        }
        int numberOfTrailingZeros = Long.numberOfTrailingZeros(j) / 4;
        this.bcdLong >>>= numberOfTrailingZeros * 4;
        this.scale += numberOfTrailingZeros;
        this.precision = 16 - (Long.numberOfLeadingZeros(this.bcdLong) / 4);
    }

    private void ensureCapacity() {
        ensureCapacity(40);
    }

    private void ensureCapacity(int i) {
        if (i != 0) {
            int length = this.usingBytes ? this.bcdBytes.length : 0;
            if (!this.usingBytes) {
                this.bcdBytes = new byte[i];
            } else if (length < i) {
                byte[] bArr = new byte[(i * 2)];
                System.arraycopy(this.bcdBytes, 0, bArr, 0, length);
                this.bcdBytes = bArr;
            }
            this.usingBytes = true;
        }
    }

    private void switchStorage() {
        if (this.usingBytes) {
            this.bcdLong = 0;
            for (int i = this.precision - 1; i >= 0; i--) {
                this.bcdLong <<= 4;
                this.bcdLong |= (long) this.bcdBytes[i];
            }
            this.bcdBytes = null;
            this.usingBytes = false;
            return;
        }
        ensureCapacity();
        for (int i2 = 0; i2 < this.precision; i2++) {
            byte[] bArr = this.bcdBytes;
            long j = this.bcdLong;
            bArr[i2] = (byte) ((int) (15 & j));
            this.bcdLong = j >>> 4;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD
    public void copyBcdFrom(DecimalQuantity decimalQuantity) {
        DecimalQuantity_DualStorageBCD decimalQuantity_DualStorageBCD = (DecimalQuantity_DualStorageBCD) decimalQuantity;
        setBcdToZero();
        if (decimalQuantity_DualStorageBCD.usingBytes) {
            ensureCapacity(decimalQuantity_DualStorageBCD.precision);
            System.arraycopy(decimalQuantity_DualStorageBCD.bcdBytes, 0, this.bcdBytes, 0, decimalQuantity_DualStorageBCD.precision);
            return;
        }
        this.bcdLong = decimalQuantity_DualStorageBCD.bcdLong;
    }

    @Deprecated
    public String checkHealth() {
        int i = 0;
        if (!this.usingBytes) {
            if (this.bcdBytes != null) {
                int i2 = 0;
                while (true) {
                    byte[] bArr = this.bcdBytes;
                    if (i2 >= bArr.length) {
                        break;
                    } else if (bArr[i2] != 0) {
                        return "Nonzero digits in byte array but we are in long mode";
                    } else {
                        i2++;
                    }
                }
            }
            if (this.precision == 0 && this.bcdLong != 0) {
                return "Value in bcdLong even though precision is zero";
            }
            if (this.precision > 16) {
                return "Precision exceeds length of long";
            }
            if (this.precision != 0 && getDigitPos(this.precision - 1) == 0) {
                return "Most significant digit is zero in long mode";
            }
            if (this.precision != 0 && getDigitPos(0) == 0) {
                return "Least significant digit is zero in long mode";
            }
            while (i < this.precision) {
                if (getDigitPos(i) >= 10) {
                    return "Digit exceeding 10 in long";
                }
                if (getDigitPos(i) < 0) {
                    return "Digit below 0 in long (?!)";
                }
                i++;
            }
            for (int i3 = this.precision; i3 < 16; i3++) {
                if (getDigitPos(i3) != 0) {
                    return "Nonzero digits outside of range in long";
                }
            }
            return null;
        } else if (this.bcdLong != 0) {
            return "Value in bcdLong but we are in byte mode";
        } else {
            if (this.precision == 0) {
                return "Zero precision but we are in byte mode";
            }
            if (this.precision > this.bcdBytes.length) {
                return "Precision exceeds length of byte array";
            }
            if (getDigitPos(this.precision - 1) == 0) {
                return "Most significant digit is zero in byte mode";
            }
            if (getDigitPos(0) == 0) {
                return "Least significant digit is zero in long mode";
            }
            while (i < this.precision) {
                if (getDigitPos(i) >= 10) {
                    return "Digit exceeding 10 in byte array";
                }
                if (getDigitPos(i) < 0) {
                    return "Digit below 0 in byte array";
                }
                i++;
            }
            for (int i4 = this.precision; i4 < this.bcdBytes.length; i4++) {
                if (getDigitPos(i4) != 0) {
                    return "Nonzero digits outside of range in byte array";
                }
            }
            return null;
        }
    }

    @Deprecated
    public boolean isUsingBytes() {
        return this.usingBytes;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001f: APUT  (r0v1 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r1v5 java.lang.String) */
    public String toString() {
        Object[] objArr = new Object[5];
        objArr[0] = Integer.valueOf(this.lReqPos);
        objArr[1] = Integer.valueOf(this.rReqPos);
        objArr[2] = this.usingBytes ? "bytes" : "long";
        objArr[3] = isNegative() ? LanguageTag.SEP : "";
        objArr[4] = toNumberString();
        return String.format("<DecimalQuantity %d:%d %s %s%s>", objArr);
    }

    private String toNumberString() {
        StringBuilder sb = new StringBuilder();
        if (this.usingBytes) {
            if (this.precision == 0) {
                sb.append('0');
            }
            for (int i = this.precision - 1; i >= 0; i--) {
                sb.append((int) this.bcdBytes[i]);
            }
        } else {
            sb.append(Long.toHexString(this.bcdLong));
        }
        sb.append(DateFormat.ABBR_WEEKDAY);
        sb.append(this.scale);
        return sb.toString();
    }
}
