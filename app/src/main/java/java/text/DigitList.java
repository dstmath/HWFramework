package java.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import sun.util.calendar.BaseCalendar;

final class DigitList implements Cloneable {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final /* synthetic */ int[] -java-math-RoundingModeSwitchesValues = null;
    private static final char[] LONG_MIN_REP = null;
    public static final int MAX_COUNT = 19;
    public int count;
    private char[] data;
    public int decimalAt;
    public char[] digits;
    private boolean isNegative;
    private RoundingMode roundingMode;
    private StringBuffer tempBuffer;

    private static /* synthetic */ int[] -getjava-math-RoundingModeSwitchesValues() {
        if (-java-math-RoundingModeSwitchesValues != null) {
            return -java-math-RoundingModeSwitchesValues;
        }
        int[] iArr = new int[RoundingMode.values().length];
        try {
            iArr[RoundingMode.CEILING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RoundingMode.DOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RoundingMode.FLOOR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[RoundingMode.HALF_DOWN.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[RoundingMode.HALF_EVEN.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[RoundingMode.HALF_UP.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[RoundingMode.UNNECESSARY.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[RoundingMode.UP.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -java-math-RoundingModeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.DigitList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.DigitList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.text.DigitList.<clinit>():void");
    }

    DigitList() {
        this.decimalAt = 0;
        this.count = 0;
        this.digits = new char[MAX_COUNT];
        this.roundingMode = RoundingMode.HALF_EVEN;
        this.isNegative = -assertionsDisabled;
    }

    boolean isZero() {
        for (int i = 0; i < this.count; i++) {
            if (this.digits[i] != '0') {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    void setRoundingMode(RoundingMode r) {
        this.roundingMode = r;
    }

    public void clear() {
        this.decimalAt = 0;
        this.count = 0;
    }

    public void append(char digit) {
        if (this.count == this.digits.length) {
            char[] data = new char[(this.count + 100)];
            System.arraycopy(this.digits, 0, data, 0, this.count);
            this.digits = data;
        }
        char[] cArr = this.digits;
        int i = this.count;
        this.count = i + 1;
        cArr[i] = digit;
    }

    public final double getDouble() {
        if (this.count == 0) {
            return 0.0d;
        }
        StringBuffer temp = getStringBuffer();
        temp.append('.');
        temp.append(this.digits, 0, this.count);
        temp.append('E');
        temp.append(this.decimalAt);
        return Double.parseDouble(temp.toString());
    }

    public final long getLong() {
        if (this.count == 0) {
            return 0;
        }
        if (isLongMIN_VALUE()) {
            return Long.MIN_VALUE;
        }
        StringBuffer temp = getStringBuffer();
        temp.append(this.digits, 0, this.count);
        for (int i = this.count; i < this.decimalAt; i++) {
            temp.append('0');
        }
        return Long.parseLong(temp.toString());
    }

    public final BigDecimal getBigDecimal() {
        if (this.count == 0) {
            if (this.decimalAt == 0) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal("0E" + this.decimalAt);
        } else if (this.decimalAt == this.count) {
            return new BigDecimal(this.digits, 0, this.count);
        } else {
            return new BigDecimal(this.digits, 0, this.count).scaleByPowerOfTen(this.decimalAt - this.count);
        }
    }

    boolean fitsIntoLong(boolean isPositive, boolean ignoreNegativeZero) {
        boolean z = true;
        while (this.count > 0 && this.digits[this.count - 1] == '0') {
            this.count--;
        }
        if (this.count == 0) {
            if (isPositive) {
                ignoreNegativeZero = true;
            }
            return ignoreNegativeZero;
        } else if (this.decimalAt < this.count || this.decimalAt > MAX_COUNT) {
            return -assertionsDisabled;
        } else {
            if (this.decimalAt < MAX_COUNT) {
                return true;
            }
            for (int i = 0; i < this.count; i++) {
                char dig = this.digits[i];
                char max = LONG_MIN_REP[i];
                if (dig > max) {
                    return -assertionsDisabled;
                }
                if (dig < max) {
                    return true;
                }
            }
            if (this.count < this.decimalAt) {
                return true;
            }
            if (isPositive) {
                z = -assertionsDisabled;
            }
            return z;
        }
    }

    public final void set(boolean isNegative, double source, int maximumFractionDigits) {
        set(isNegative, source, maximumFractionDigits, true);
    }

    final void set(boolean isNegative, double source, int maximumDigits, boolean fixedPoint) {
        set(isNegative, Double.toString(source), maximumDigits, fixedPoint);
    }

    final void set(boolean isNegative, String s, int maximumDigits, boolean fixedPoint) {
        this.isNegative = isNegative;
        int len = s.length();
        char[] source = getDataChars(len);
        s.getChars(0, len, source, 0);
        this.decimalAt = -1;
        this.count = 0;
        int exponent = 0;
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = -assertionsDisabled;
        int i = 0;
        while (i < len) {
            int i2 = i + 1;
            char c = source[i];
            if (c == '.') {
                this.decimalAt = this.count;
            } else if (c == 'e' || c == 'E') {
                exponent = parseInt(source, i2, len);
                break;
            } else {
                if (!nonZeroDigitSeen) {
                    nonZeroDigitSeen = c != '0' ? true : -assertionsDisabled;
                    if (!(nonZeroDigitSeen || this.decimalAt == -1)) {
                        leadingZerosAfterDecimal++;
                    }
                }
                if (nonZeroDigitSeen) {
                    char[] cArr = this.digits;
                    int i3 = this.count;
                    this.count = i3 + 1;
                    cArr[i3] = c;
                }
            }
            i = i2;
        }
        if (this.decimalAt == -1) {
            this.decimalAt = this.count;
        }
        if (nonZeroDigitSeen) {
            this.decimalAt += exponent - leadingZerosAfterDecimal;
        }
        if (fixedPoint) {
            if ((-this.decimalAt) > maximumDigits) {
                this.count = 0;
                return;
            } else if ((-this.decimalAt) == maximumDigits) {
                if (shouldRoundUp(0)) {
                    this.count = 1;
                    this.decimalAt++;
                    this.digits[0] = '1';
                } else {
                    this.count = 0;
                }
                return;
            }
        }
        while (this.count > 1 && this.digits[this.count - 1] == '0') {
            this.count--;
        }
        if (fixedPoint) {
            maximumDigits += this.decimalAt;
        }
        round(maximumDigits);
    }

    private final void round(int maximumDigits) {
        if (maximumDigits >= 0 && maximumDigits < this.count) {
            if (shouldRoundUp(maximumDigits)) {
                do {
                    maximumDigits--;
                    if (maximumDigits < 0) {
                        this.digits[0] = '1';
                        this.decimalAt++;
                        maximumDigits = 0;
                        break;
                    }
                    char[] cArr = this.digits;
                    cArr[maximumDigits] = (char) (cArr[maximumDigits] + 1);
                } while (this.digits[maximumDigits] > '9');
                maximumDigits++;
            }
            this.count = maximumDigits;
            while (this.count > 1 && this.digits[this.count - 1] == '0') {
                this.count--;
            }
        }
    }

    private boolean shouldRoundUp(int maximumDigits) {
        boolean z = -assertionsDisabled;
        if (maximumDigits < this.count) {
            int i;
            switch (-getjava-math-RoundingModeSwitchesValues()[this.roundingMode.ordinal()]) {
                case BaseCalendar.SUNDAY /*1*/:
                    for (i = maximumDigits; i < this.count; i++) {
                        if (this.digits[i] != '0') {
                            if (!this.isNegative) {
                                z = true;
                            }
                            return z;
                        }
                    }
                    break;
                case BaseCalendar.MONDAY /*2*/:
                    break;
                case BaseCalendar.TUESDAY /*3*/:
                    for (i = maximumDigits; i < this.count; i++) {
                        if (this.digits[i] != '0') {
                            return this.isNegative;
                        }
                    }
                    break;
                case BaseCalendar.WEDNESDAY /*4*/:
                    if (this.digits[maximumDigits] <= '5') {
                        if (this.digits[maximumDigits] == '5') {
                            for (i = maximumDigits + 1; i < this.count; i++) {
                                if (this.digits[i] != '0') {
                                    return true;
                                }
                            }
                            break;
                        }
                    }
                    return true;
                    break;
                case BaseCalendar.THURSDAY /*5*/:
                    if (this.digits[maximumDigits] > '5') {
                        return true;
                    }
                    if (this.digits[maximumDigits] == '5') {
                        for (i = maximumDigits + 1; i < this.count; i++) {
                            if (this.digits[i] != '0') {
                                return true;
                            }
                        }
                        if (maximumDigits > 0 && this.digits[maximumDigits - 1] % 2 != 0) {
                            z = true;
                        }
                        return z;
                    }
                    break;
                case BaseCalendar.JUNE /*6*/:
                    if (this.digits[maximumDigits] >= '5') {
                        return true;
                    }
                    break;
                case BaseCalendar.SATURDAY /*7*/:
                    for (i = maximumDigits; i < this.count; i++) {
                        if (this.digits[i] != '0') {
                            throw new ArithmeticException("Rounding needed with the rounding mode being set to RoundingMode.UNNECESSARY");
                        }
                    }
                    break;
                case BaseCalendar.AUGUST /*8*/:
                    for (i = maximumDigits; i < this.count; i++) {
                        if (this.digits[i] != '0') {
                            return true;
                        }
                    }
                    break;
                default:
                    if (!-assertionsDisabled) {
                        throw new AssertionError();
                    }
                    break;
            }
        }
        return -assertionsDisabled;
    }

    public final void set(boolean isNegative, long source) {
        set(isNegative, source, 0);
    }

    public final void set(boolean isNegative, long source, int maximumDigits) {
        this.isNegative = isNegative;
        if (source > 0) {
            int left = MAX_COUNT;
            while (source > 0) {
                left--;
                this.digits[left] = (char) ((int) ((source % 10) + 48));
                source /= 10;
            }
            this.decimalAt = 19 - left;
            int right = 18;
            while (this.digits[right] == '0') {
                right--;
            }
            this.count = (right - left) + 1;
            System.arraycopy(this.digits, left, this.digits, 0, this.count);
        } else if (source == Long.MIN_VALUE) {
            this.count = MAX_COUNT;
            this.decimalAt = MAX_COUNT;
            System.arraycopy(LONG_MIN_REP, 0, this.digits, 0, this.count);
        } else {
            this.count = 0;
            this.decimalAt = 0;
        }
        if (maximumDigits > 0) {
            round(maximumDigits);
        }
    }

    final void set(boolean isNegative, BigDecimal source, int maximumDigits, boolean fixedPoint) {
        String s = source.toString();
        extendDigits(s.length());
        set(isNegative, s, maximumDigits, fixedPoint);
    }

    final void set(boolean isNegative, BigInteger source, int maximumDigits) {
        this.isNegative = isNegative;
        String s = source.toString();
        int len = s.length();
        extendDigits(len);
        s.getChars(0, len, this.digits, 0);
        this.decimalAt = len;
        int right = len - 1;
        while (right >= 0 && this.digits[right] == '0') {
            right--;
        }
        this.count = right + 1;
        if (maximumDigits > 0) {
            round(maximumDigits);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DigitList)) {
            return -assertionsDisabled;
        }
        DigitList other = (DigitList) obj;
        if (this.count != other.count || this.decimalAt != other.decimalAt) {
            return -assertionsDisabled;
        }
        for (int i = 0; i < this.count; i++) {
            if (this.digits[i] != other.digits[i]) {
                return -assertionsDisabled;
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

    public Object clone() {
        try {
            DigitList other = (DigitList) super.clone();
            char[] newDigits = new char[this.digits.length];
            System.arraycopy(this.digits, 0, newDigits, 0, this.digits.length);
            other.digits = newDigits;
            other.tempBuffer = null;
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private boolean isLongMIN_VALUE() {
        if (this.decimalAt != this.count || this.count != MAX_COUNT) {
            return -assertionsDisabled;
        }
        for (int i = 0; i < this.count; i++) {
            if (this.digits[i] != LONG_MIN_REP[i]) {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static final int parseInt(char[] str, int offset, int strLen) {
        boolean positive = true;
        char c = str[offset];
        if (c == '-') {
            positive = -assertionsDisabled;
            offset++;
        } else if (c == '+') {
            offset++;
        }
        int value = 0;
        int offset2 = offset;
        while (offset2 < strLen) {
            offset = offset2 + 1;
            c = str[offset2];
            if (c >= '0' && c <= '9') {
                value = (value * 10) + (c - 48);
                offset2 = offset;
            }
        }
        return positive ? value : -value;
    }

    public String toString() {
        if (isZero()) {
            return "0";
        }
        StringBuffer buf = getStringBuffer();
        buf.append("0.");
        buf.append(this.digits, 0, this.count);
        buf.append("x10^");
        buf.append(this.decimalAt);
        return buf.toString();
    }

    private StringBuffer getStringBuffer() {
        if (this.tempBuffer == null) {
            this.tempBuffer = new StringBuffer((int) MAX_COUNT);
        } else {
            this.tempBuffer.setLength(0);
        }
        return this.tempBuffer;
    }

    private void extendDigits(int len) {
        if (len > this.digits.length) {
            this.digits = new char[len];
        }
    }

    private final char[] getDataChars(int length) {
        if (this.data == null || this.data.length < length) {
            this.data = new char[length];
        }
        return this.data;
    }
}
