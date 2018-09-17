package android.icu.text;

import android.icu.math.BigDecimal;
import java.math.BigInteger;

public final class DigitList {
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

    boolean isZero() {
        for (int i = 0; i < this.count; i++) {
            if (this.digits[i] != (byte) 48) {
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
        if (isZero()) {
            return BigInteger.valueOf(0);
        }
        int i;
        int n;
        int len = this.decimalAt > this.count ? this.decimalAt : this.count;
        if (!isPositive) {
            len++;
        }
        char[] text = new char[len];
        if (isPositive) {
            for (i = 0; i < this.count; i++) {
                text[i] = (char) this.digits[i];
            }
            n = this.count;
        } else {
            text[0] = '-';
            for (i = 0; i < this.count; i++) {
                text[i + 1] = (char) this.digits[i];
            }
            n = this.count + 1;
        }
        for (i = n; i < text.length; i++) {
            text[i] = '0';
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
        for (int i = 0; i < numDigits; i++) {
            significantDigits.append((char) this.digits[i]);
        }
        return new BigDecimal(new BigInteger(significantDigits.toString()), (int) scale);
    }

    boolean isIntegral() {
        while (this.count > 0 && this.digits[this.count - 1] == (byte) 48) {
            this.count--;
        }
        if (this.count == 0 || this.decimalAt >= this.count) {
            return true;
        }
        return false;
    }

    final void set(double source, int maximumDigits, boolean fixedPoint) {
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
                    this.digits[0] = (byte) 49;
                } else {
                    this.count = 0;
                }
                return;
            }
        }
        while (this.count > 1 && this.digits[this.count - 1] == (byte) 48) {
            this.count--;
        }
        if (fixedPoint) {
            maximumDigits += this.decimalAt;
        } else if (maximumDigits == 0) {
            maximumDigits = -1;
        }
        round(maximumDigits);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x004c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void set(String rep, int maxCount) {
        this.decimalAt = -1;
        this.count = 0;
        int exponent = 0;
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = false;
        int i = 0;
        if (rep.charAt(0) == '-') {
            i = 1;
        }
        while (i < rep.length()) {
            char c = rep.charAt(i);
            if (c == '.') {
                this.decimalAt = this.count;
            } else if (c == 'e' || c == 'E') {
                i++;
                if (rep.charAt(i) == '+') {
                    i++;
                }
                exponent = Integer.valueOf(rep.substring(i)).intValue();
                if (this.decimalAt == -1) {
                    this.decimalAt = this.count;
                }
                this.decimalAt += exponent - leadingZerosAfterDecimal;
            } else if (this.count < maxCount) {
                if (!nonZeroDigitSeen) {
                    nonZeroDigitSeen = c != '0';
                    if (!(nonZeroDigitSeen || this.decimalAt == -1)) {
                        leadingZerosAfterDecimal++;
                    }
                }
                if (nonZeroDigitSeen) {
                    ensureCapacity(this.count + 1, this.count);
                    byte[] bArr = this.digits;
                    int i2 = this.count;
                    this.count = i2 + 1;
                    bArr[i2] = (byte) c;
                }
            }
            i++;
        }
        if (this.decimalAt == -1) {
        }
        this.decimalAt += exponent - leadingZerosAfterDecimal;
    }

    private boolean shouldRoundUp(int maximumDigits) {
        boolean z = true;
        if (maximumDigits < this.count) {
            if (this.digits[maximumDigits] > (byte) 53) {
                return true;
            }
            if (this.digits[maximumDigits] == (byte) 53) {
                for (int i = maximumDigits + 1; i < this.count; i++) {
                    if (this.digits[i] != (byte) 48) {
                        return true;
                    }
                }
                if (maximumDigits <= 0 || this.digits[maximumDigits - 1] % 2 == 0) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    public final void round(int maximumDigits) {
        if (maximumDigits >= 0 && maximumDigits < this.count) {
            if (shouldRoundUp(maximumDigits)) {
                do {
                    maximumDigits--;
                    if (maximumDigits < 0) {
                        this.digits[0] = (byte) 49;
                        this.decimalAt++;
                        maximumDigits = 0;
                        this.didRound = true;
                        break;
                    }
                    byte[] bArr = this.digits;
                    bArr[maximumDigits] = (byte) (bArr[maximumDigits] + 1);
                    this.didRound = true;
                } while (this.digits[maximumDigits] > (byte) 57);
                maximumDigits++;
            }
            this.count = maximumDigits;
        }
        while (this.count > 1 && this.digits[this.count - 1] == (byte) 48) {
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
        this.didRound = false;
        if (source > 0) {
            int left = 19;
            while (source > 0) {
                left--;
                this.digits[left] = (byte) ((int) ((source % 10) + 48));
                source /= 10;
            }
            this.decimalAt = 19 - left;
            int right = 18;
            while (this.digits[right] == (byte) 48) {
                right--;
            }
            this.count = (right - left) + 1;
            System.arraycopy(this.digits, left, this.digits, 0, this.count);
        } else if (source == Long.MIN_VALUE) {
            this.count = 19;
            this.decimalAt = 19;
            System.arraycopy(LONG_MIN_REP, 0, this.digits, 0, this.count);
        } else {
            this.count = 0;
            this.decimalAt = 0;
        }
        if (maximumDigits > 0) {
            round(maximumDigits);
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
            offset = 1;
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
        if (fixedPoint) {
            maximumDigits += this.decimalAt;
        } else if (maximumDigits == 0) {
            maximumDigits = -1;
        }
        round(maximumDigits);
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
        if (!(obj instanceof DigitList)) {
            return false;
        }
        DigitList other = (DigitList) obj;
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
