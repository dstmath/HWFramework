package android.icu.text;

import android.icu.math.BigDecimal;
import java.math.BigInteger;

public final class DigitList_Android {
    public static final int DBL_DIG = 17;
    private static byte[] LONG_MIN_REP = new byte[19];
    public static final int MAX_LONG_DIGITS = 19;
    public int count = 0;
    public int decimalAt = 0;
    private boolean didRound = false;
    public byte[] digits = new byte[19];

    private final void ensureCapacity(int digitCapacity, int digitsToCopy) {
        if (digitCapacity > this.digits.length) {
            byte[] newDigits = new byte[(digitCapacity * 2)];
            System.arraycopy(this.digits, 0, newDigits, 0, digitsToCopy);
            this.digits = newDigits;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isZero() {
        for (int i = 0; i < this.count; i++) {
            if (this.digits[i] != 48) {
                return false;
            }
        }
        return true;
    }

    public void append(int digit) {
        ensureCapacity(this.count + 1, this.count);
        byte[] bArr = this.digits;
        int i = this.count;
        this.count = i + 1;
        bArr[i] = (byte) digit;
    }

    public byte getDigitValue(int i) {
        return (byte) (this.digits[i] - 48);
    }

    public final double getDouble() {
        if (this.count == 0) {
            return 0.0d;
        }
        StringBuilder temp = new StringBuilder(this.count);
        temp.append('.');
        for (int i = 0; i < this.count; i++) {
            temp.append((char) this.digits[i]);
        }
        temp.append('E');
        temp.append(Integer.toString(this.decimalAt));
        return Double.valueOf(temp.toString()).doubleValue();
    }

    public final long getLong() {
        if (this.count == 0) {
            return 0;
        }
        if (isLongMIN_VALUE()) {
            return Long.MIN_VALUE;
        }
        StringBuilder temp = new StringBuilder(this.count);
        int i = 0;
        while (i < this.decimalAt) {
            temp.append(i < this.count ? (char) this.digits[i] : '0');
            i++;
        }
        return Long.parseLong(temp.toString());
    }

    public BigInteger getBigInteger(boolean isPositive) {
        int n;
        if (isZero()) {
            return BigInteger.valueOf(0);
        }
        int len = this.decimalAt > this.count ? this.decimalAt : this.count;
        if (!isPositive) {
            len++;
        }
        char[] text = new char[len];
        int i = 0;
        if (!isPositive) {
            text[0] = '-';
            while (i < this.count) {
                text[i + 1] = (char) this.digits[i];
                i++;
            }
            n = this.count + 1;
        } else {
            while (i < this.count) {
                text[i] = (char) this.digits[i];
                i++;
            }
            n = this.count;
        }
        for (int i2 = n; i2 < text.length; i2++) {
            text[i2] = '0';
        }
        return new BigInteger(new String(text));
    }

    private String getStringRep(boolean isPositive) {
        if (isZero()) {
            return AndroidHardcodedSystemProperties.JAVA_VERSION;
        }
        StringBuilder stringRep = new StringBuilder(this.count + 1);
        if (!isPositive) {
            stringRep.append('-');
        }
        int d = this.decimalAt;
        if (d < 0) {
            stringRep.append('.');
            while (d < 0) {
                stringRep.append('0');
                d++;
            }
            d = -1;
        }
        for (int i = 0; i < this.count; i++) {
            if (d == i) {
                stringRep.append('.');
            }
            stringRep.append((char) this.digits[i]);
        }
        while (true) {
            int d2 = d - 1;
            if (d <= this.count) {
                return stringRep.toString();
            }
            stringRep.append('0');
            d = d2;
        }
    }

    public BigDecimal getBigDecimalICU(boolean isPositive) {
        if (isZero()) {
            return BigDecimal.valueOf(0);
        }
        long scale = ((long) this.count) - ((long) this.decimalAt);
        if (scale <= 0) {
            return new BigDecimal(getStringRep(isPositive));
        }
        int numDigits = this.count;
        int i = 0;
        if (scale > 2147483647L) {
            long numShift = scale - 2147483647L;
            if (numShift >= ((long) this.count)) {
                return new BigDecimal(0);
            }
            numDigits = (int) (((long) numDigits) - numShift);
        }
        StringBuilder significantDigits = new StringBuilder(numDigits + 1);
        if (!isPositive) {
            significantDigits.append('-');
        }
        while (true) {
            int i2 = i;
            if (i2 >= numDigits) {
                return new BigDecimal(new BigInteger(significantDigits.toString()), (int) scale);
            }
            significantDigits.append((char) this.digits[i2]);
            i = i2 + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isIntegral() {
        while (this.count > 0 && this.digits[this.count - 1] == 48) {
            this.count--;
        }
        if (this.count == 0 || this.decimalAt >= this.count) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final void set(double source, int maximumDigits, boolean fixedPoint) {
        if (source == 0.0d) {
            source = 0.0d;
        }
        String rep = Double.toString(source);
        this.didRound = false;
        set(rep, 19);
        if (fixedPoint) {
            if ((-this.decimalAt) > maximumDigits) {
                this.count = 0;
                return;
            } else if ((-this.decimalAt) == maximumDigits) {
                if (shouldRoundUp(0)) {
                    this.count = 1;
                    this.decimalAt++;
                    this.digits[0] = 49;
                } else {
                    this.count = 0;
                }
                return;
            }
        }
        while (this.count > 1 && this.digits[this.count - 1] == 48) {
            this.count--;
        }
        round(fixedPoint ? this.decimalAt + maximumDigits : maximumDigits == 0 ? -1 : maximumDigits);
    }

    private void set(String rep, int maxCount) {
        this.decimalAt = -1;
        this.count = 0;
        int exponent = 0;
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = false;
        int i = 0;
        if (rep.charAt(0) == '-') {
            i = 0 + 1;
        }
        while (true) {
            if (i >= rep.length()) {
                break;
            }
            char c = rep.charAt(i);
            if (c == '.') {
                this.decimalAt = this.count;
            } else if (c == 'e' || c == 'E') {
                int i2 = i + 1;
            } else if (this.count < maxCount) {
                if (!nonZeroDigitSeen) {
                    nonZeroDigitSeen = c != '0';
                    if (!nonZeroDigitSeen && this.decimalAt != -1) {
                        leadingZerosAfterDecimal++;
                    }
                }
                if (nonZeroDigitSeen) {
                    ensureCapacity(this.count + 1, this.count);
                    byte[] bArr = this.digits;
                    int i3 = this.count;
                    this.count = i3 + 1;
                    bArr[i3] = (byte) c;
                }
            }
            i++;
        }
        int i22 = i + 1;
        if (rep.charAt(i22) == '+') {
            i22++;
        }
        exponent = Integer.valueOf(rep.substring(i22)).intValue();
        if (this.decimalAt == -1) {
            this.decimalAt = this.count;
        }
        this.decimalAt += exponent - leadingZerosAfterDecimal;
    }

    private boolean shouldRoundUp(int maximumDigits) {
        boolean z = false;
        if (maximumDigits < this.count) {
            if (this.digits[maximumDigits] > 53) {
                return true;
            }
            if (this.digits[maximumDigits] == 53) {
                for (int i = maximumDigits + 1; i < this.count; i++) {
                    if (this.digits[i] != 48) {
                        return true;
                    }
                }
                if (maximumDigits > 0 && this.digits[maximumDigits - 1] % 2 != 0) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    public final void round(int maximumDigits) {
        if (maximumDigits >= 0 && maximumDigits < this.count) {
            if (shouldRoundUp(maximumDigits)) {
                while (true) {
                    maximumDigits--;
                    if (maximumDigits >= 0) {
                        byte[] bArr = this.digits;
                        bArr[maximumDigits] = (byte) (bArr[maximumDigits] + 1);
                        this.didRound = true;
                        if (this.digits[maximumDigits] <= 57) {
                            break;
                        }
                    } else {
                        this.digits[0] = 49;
                        this.decimalAt++;
                        maximumDigits = 0;
                        this.didRound = true;
                        break;
                    }
                }
                maximumDigits++;
            }
            this.count = maximumDigits;
        }
        while (this.count > 1 && this.digits[this.count - 1] == 48) {
            this.count--;
        }
    }

    public boolean wasRounded() {
        return this.didRound;
    }

    public final void set(long source) {
        set(source, 0);
    }

    public final void set(long source, int maximumDigits) {
        int i = maximumDigits;
        this.didRound = false;
        if (source <= 0) {
            if (source == Long.MIN_VALUE) {
                this.count = 19;
                this.decimalAt = 19;
                System.arraycopy(LONG_MIN_REP, 0, this.digits, 0, this.count);
            } else {
                this.count = 0;
                this.decimalAt = 0;
            }
            long j = source;
        } else {
            long source2 = source;
            int left = 19;
            while (source2 > 0) {
                left--;
                this.digits[left] = (byte) ((int) (48 + (source2 % 10)));
                source2 /= 10;
            }
            this.decimalAt = 19 - left;
            int right = 18;
            while (this.digits[right] == 48) {
                right--;
            }
            this.count = (right - left) + 1;
            System.arraycopy(this.digits, left, this.digits, 0, this.count);
            long j2 = source2;
        }
        if (i > 0) {
            round(i);
        }
    }

    public final void set(BigInteger source, int maximumDigits) {
        String stringDigits = source.toString();
        int length = stringDigits.length();
        this.decimalAt = length;
        this.count = length;
        this.didRound = false;
        while (this.count > 1 && stringDigits.charAt(this.count - 1) == '0') {
            this.count--;
        }
        int offset = 0;
        if (stringDigits.charAt(0) == '-') {
            offset = 0 + 1;
            this.count--;
            this.decimalAt--;
        }
        ensureCapacity(this.count, 0);
        for (int i = 0; i < this.count; i++) {
            this.digits[i] = (byte) stringDigits.charAt(i + offset);
        }
        if (maximumDigits > 0) {
            round(maximumDigits);
        }
    }

    private void setBigDecimalDigits(String stringDigits, int maximumDigits, boolean fixedPoint) {
        this.didRound = false;
        set(stringDigits, stringDigits.length());
        round(fixedPoint ? this.decimalAt + maximumDigits : maximumDigits == 0 ? -1 : maximumDigits);
    }

    public final void set(java.math.BigDecimal source, int maximumDigits, boolean fixedPoint) {
        setBigDecimalDigits(source.toString(), maximumDigits, fixedPoint);
    }

    public final void set(BigDecimal source, int maximumDigits, boolean fixedPoint) {
        setBigDecimalDigits(source.toString(), maximumDigits, fixedPoint);
    }

    private boolean isLongMIN_VALUE() {
        if (this.decimalAt != this.count || this.count != 19) {
            return false;
        }
        for (int i = 0; i < this.count; i++) {
            if (this.digits[i] != LONG_MIN_REP[i]) {
                return false;
            }
        }
        return true;
    }

    static {
        String s = Long.toString(Long.MIN_VALUE);
        for (int i = 0; i < 19; i++) {
            LONG_MIN_REP[i] = (byte) s.charAt(i + 1);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DigitList_Android)) {
            return false;
        }
        DigitList_Android other = (DigitList_Android) obj;
        if (this.count != other.count || this.decimalAt != other.decimalAt) {
            return false;
        }
        for (int i = 0; i < this.count; i++) {
            if (this.digits[i] != other.digits[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hashcode = this.decimalAt;
        for (int i = 0; i < this.count; i++) {
            hashcode = (hashcode * 37) + this.digits[i];
        }
        return hashcode;
    }

    public String toString() {
        if (isZero()) {
            return AndroidHardcodedSystemProperties.JAVA_VERSION;
        }
        StringBuilder buf = new StringBuilder("0.");
        for (int i = 0; i < this.count; i++) {
            buf.append((char) this.digits[i]);
        }
        buf.append("x10^");
        buf.append(this.decimalAt);
        return buf.toString();
    }
}
