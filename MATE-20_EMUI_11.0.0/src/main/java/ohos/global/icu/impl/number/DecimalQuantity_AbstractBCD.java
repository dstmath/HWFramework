package ohos.global.icu.impl.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.FieldPosition;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.text.UFieldPosition;

public abstract class DecimalQuantity_AbstractBCD implements DecimalQuantity {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final double[] DOUBLE_MULTIPLIERS = {1.0d, 10.0d, 100.0d, 1000.0d, 10000.0d, 100000.0d, 1000000.0d, 1.0E7d, 1.0E8d, 1.0E9d, 1.0E10d, 1.0E11d, 1.0E12d, 1.0E13d, 1.0E14d, 1.0E15d, 1.0E16d, 1.0E17d, 1.0E18d, 1.0E19d, 1.0E20d, 1.0E21d};
    protected static final int INFINITY_FLAG = 2;
    static final byte[] INT64_BCD = {9, 2, 2, 3, 3, 7, 2, 0, 3, 6, 8, 5, 4, 7, 7, 5, 8, 0, 8};
    protected static final int NAN_FLAG = 4;
    protected static final int NEGATIVE_FLAG = 1;
    private static final int SECTION_LOWER_EDGE = -1;
    private static final int SECTION_UPPER_EDGE = -2;
    @Deprecated
    public boolean explicitExactDouble = false;
    protected byte flags;
    protected boolean isApproximate;
    protected int lReqPos = 0;
    protected int origDelta;
    protected double origDouble;
    protected int precision;
    protected int rReqPos = 0;
    protected int scale;

    private static int safeSubtract(int i, int i2) {
        int i3 = i - i2;
        if (i2 < 0 && i3 < i) {
            return Integer.MAX_VALUE;
        }
        if (i2 <= 0 || i3 <= i) {
            return i3;
        }
        return Integer.MIN_VALUE;
    }

    /* access modifiers changed from: protected */
    public abstract BigDecimal bcdToBigDecimal();

    /* access modifiers changed from: protected */
    public abstract void compact();

    /* access modifiers changed from: protected */
    public abstract void copyBcdFrom(DecimalQuantity decimalQuantity);

    /* access modifiers changed from: protected */
    public abstract byte getDigitPos(int i);

    /* access modifiers changed from: protected */
    public abstract void popFromLeft(int i);

    /* access modifiers changed from: protected */
    public abstract void readBigIntegerToBcd(BigInteger bigInteger);

    /* access modifiers changed from: protected */
    public abstract void readIntToBcd(int i);

    /* access modifiers changed from: protected */
    public abstract void readLongToBcd(long j);

    /* access modifiers changed from: protected */
    public abstract void setBcdToZero();

    /* access modifiers changed from: protected */
    public abstract void setDigitPos(int i, byte b);

    /* access modifiers changed from: protected */
    public abstract void shiftLeft(int i);

    /* access modifiers changed from: protected */
    public abstract void shiftRight(int i);

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void copyFrom(DecimalQuantity decimalQuantity) {
        copyBcdFrom(decimalQuantity);
        DecimalQuantity_AbstractBCD decimalQuantity_AbstractBCD = (DecimalQuantity_AbstractBCD) decimalQuantity;
        this.lReqPos = decimalQuantity_AbstractBCD.lReqPos;
        this.rReqPos = decimalQuantity_AbstractBCD.rReqPos;
        this.scale = decimalQuantity_AbstractBCD.scale;
        this.precision = decimalQuantity_AbstractBCD.precision;
        this.flags = decimalQuantity_AbstractBCD.flags;
        this.origDouble = decimalQuantity_AbstractBCD.origDouble;
        this.origDelta = decimalQuantity_AbstractBCD.origDelta;
        this.isApproximate = decimalQuantity_AbstractBCD.isApproximate;
    }

    public DecimalQuantity_AbstractBCD clear() {
        this.lReqPos = 0;
        this.rReqPos = 0;
        this.flags = 0;
        setBcdToZero();
        return this;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void setMinInteger(int i) {
        int i2 = this.lReqPos;
        if (i < i2) {
            i = i2;
        }
        this.lReqPos = i;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void setMinFraction(int i) {
        this.rReqPos = -i;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void applyMaxInteger(int i) {
        if (this.precision != 0) {
            if (i <= this.scale) {
                setBcdToZero();
                return;
            }
            int magnitude = getMagnitude();
            if (i <= magnitude) {
                popFromLeft((magnitude - i) + 1);
                compact();
            }
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public long getPositionFingerprint() {
        return (((long) (this.lReqPos << 16)) ^ 0) ^ (((long) this.rReqPos) << 32);
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void roundToIncrement(BigDecimal bigDecimal, MathContext mathContext) {
        BigDecimal round = toBigDecimal().divide(bigDecimal, 0, mathContext.getRoundingMode()).multiply(bigDecimal).round(mathContext);
        if (round.signum() == 0) {
            setBcdToZero();
        } else {
            setToBigDecimal(round);
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void multiplyBy(BigDecimal bigDecimal) {
        if (!isZeroish()) {
            setToBigDecimal(toBigDecimal().multiply(bigDecimal));
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void negate() {
        this.flags = (byte) (this.flags ^ 1);
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public int getMagnitude() throws ArithmeticException {
        int i = this.precision;
        if (i != 0) {
            return (this.scale + i) - 1;
        }
        throw new ArithmeticException("Magnitude is not well-defined for zero");
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void adjustMagnitude(int i) {
        if (this.precision != 0) {
            this.scale = Utility.addExact(this.scale, i);
            this.origDelta = Utility.addExact(this.origDelta, i);
            Utility.addExact(this.scale, this.precision);
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public StandardPlural getStandardPlural(PluralRules pluralRules) {
        if (pluralRules == null) {
            return StandardPlural.OTHER;
        }
        return StandardPlural.orOtherFromString(pluralRules.select(this));
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.impl.number.DecimalQuantity_AbstractBCD$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$text$PluralRules$Operand = new int[PluralRules.Operand.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$text$PluralRules$Operand[PluralRules.Operand.i.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$PluralRules$Operand[PluralRules.Operand.f.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$PluralRules$Operand[PluralRules.Operand.t.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$PluralRules$Operand[PluralRules.Operand.v.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$PluralRules$Operand[PluralRules.Operand.w.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    @Override // ohos.global.icu.text.PluralRules.IFixedDecimal
    public double getPluralOperand(PluralRules.Operand operand) {
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$text$PluralRules$Operand[operand.ordinal()];
        if (i == 1) {
            return (double) (isNegative() ? -toLong(true) : toLong(true));
        } else if (i == 2) {
            return (double) toFractionLong(true);
        } else {
            if (i == 3) {
                return (double) toFractionLong(false);
            }
            if (i == 4) {
                return (double) fractionCount();
            }
            if (i != 5) {
                return Math.abs(toDouble());
            }
            return (double) fractionCountWithoutTrailingZeros();
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void populateUFieldPosition(FieldPosition fieldPosition) {
        if (fieldPosition instanceof UFieldPosition) {
            ((UFieldPosition) fieldPosition).setFractionDigits((int) getPluralOperand(PluralRules.Operand.v), (long) getPluralOperand(PluralRules.Operand.f));
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public int getUpperDisplayMagnitude() {
        int i = this.scale + this.precision;
        int i2 = this.lReqPos;
        if (i2 <= i) {
            i2 = i;
        }
        return i2 - 1;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public int getLowerDisplayMagnitude() {
        int i = this.scale;
        int i2 = this.rReqPos;
        return i2 < i ? i2 : i;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public byte getDigit(int i) {
        return getDigitPos(i - this.scale);
    }

    private int fractionCount() {
        return -getLowerDisplayMagnitude();
    }

    private int fractionCountWithoutTrailingZeros() {
        return Math.max(-this.scale, 0);
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public boolean isNegative() {
        return (this.flags & 1) != 0;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public int signum() {
        if (isNegative()) {
            return -1;
        }
        return (!isZeroish() || isInfinite()) ? 1 : 0;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity, ohos.global.icu.text.PluralRules.IFixedDecimal
    public boolean isInfinite() {
        return (this.flags & 2) != 0;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity, ohos.global.icu.text.PluralRules.IFixedDecimal
    public boolean isNaN() {
        return (this.flags & 4) != 0;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public boolean isZeroish() {
        return this.precision == 0;
    }

    public void setToInt(int i) {
        setBcdToZero();
        this.flags = 0;
        if (i < 0) {
            this.flags = (byte) (this.flags | 1);
            i = -i;
        }
        if (i != 0) {
            _setToInt(i);
            compact();
        }
    }

    private void _setToInt(int i) {
        if (i == Integer.MIN_VALUE) {
            readLongToBcd(-((long) i));
        } else {
            readIntToBcd(i);
        }
    }

    public void setToLong(long j) {
        setBcdToZero();
        this.flags = 0;
        if (j < 0) {
            this.flags = (byte) (this.flags | 1);
            j = -j;
        }
        if (j != 0) {
            _setToLong(j);
            compact();
        }
    }

    private void _setToLong(long j) {
        if (j == Long.MIN_VALUE) {
            readBigIntegerToBcd(BigInteger.valueOf(j).negate());
        } else if (j <= 2147483647L) {
            readIntToBcd((int) j);
        } else {
            readLongToBcd(j);
        }
    }

    public void setToBigInteger(BigInteger bigInteger) {
        setBcdToZero();
        this.flags = 0;
        if (bigInteger.signum() == -1) {
            this.flags = (byte) (this.flags | 1);
            bigInteger = bigInteger.negate();
        }
        if (bigInteger.signum() != 0) {
            _setToBigInteger(bigInteger);
            compact();
        }
    }

    private void _setToBigInteger(BigInteger bigInteger) {
        if (bigInteger.bitLength() < 32) {
            readIntToBcd(bigInteger.intValue());
        } else if (bigInteger.bitLength() < 64) {
            readLongToBcd(bigInteger.longValue());
        } else {
            readBigIntegerToBcd(bigInteger);
        }
    }

    public void setToDouble(double d) {
        setBcdToZero();
        this.flags = 0;
        if (Double.doubleToRawLongBits(d) < 0) {
            this.flags = (byte) (this.flags | 1);
            d = -d;
        }
        if (Double.isNaN(d)) {
            this.flags = (byte) (this.flags | 4);
        } else if (Double.isInfinite(d)) {
            this.flags = (byte) (this.flags | 2);
        } else if (d != XPath.MATCH_SCORE_QNAME) {
            _setToDoubleFast(d);
            compact();
        }
    }

    private void _setToDoubleFast(double d) {
        double d2;
        this.isApproximate = true;
        this.origDouble = d;
        this.origDelta = 0;
        int doubleToLongBits = ((int) ((Double.doubleToLongBits(d) & 9218868437227405312L) >> 52)) - 1023;
        if (doubleToLongBits <= 52) {
            long j = (long) d;
            if (((double) j) == d) {
                _setToLong(j);
                return;
            }
        }
        int i = (int) (((double) (52 - doubleToLongBits)) / 3.32192809489d);
        if (i >= 0) {
            double d3 = d;
            int i2 = i;
            while (i2 >= 22) {
                d3 *= 1.0E22d;
                i2 -= 22;
            }
            d2 = d3 * DOUBLE_MULTIPLIERS[i2];
        } else {
            double d4 = d;
            int i3 = i;
            while (i3 <= -22) {
                d4 /= 1.0E22d;
                i3 += 22;
            }
            d2 = d4 / DOUBLE_MULTIPLIERS[-i3];
        }
        long round = Math.round(d2);
        if (round != 0) {
            _setToLong(round);
            this.scale -= i;
        }
    }

    private void convertToAccurateDouble() {
        double d = this.origDouble;
        int i = this.origDelta;
        setBcdToZero();
        String d2 = Double.toString(d);
        if (d2.indexOf(69) != -1) {
            int indexOf = d2.indexOf(69);
            _setToLong(Long.parseLong(d2.charAt(0) + d2.substring(2, indexOf)));
            this.scale = this.scale + (Integer.parseInt(d2.substring(indexOf + 1)) - (indexOf - 1)) + 1;
        } else if (d2.charAt(0) == '0') {
            _setToLong(Long.parseLong(d2.substring(2)));
            this.scale += 2 - d2.length();
        } else if (d2.charAt(d2.length() - 1) == '0') {
            _setToLong(Long.parseLong(d2.substring(0, d2.length() - 2)));
        } else {
            int indexOf2 = d2.indexOf(46);
            _setToLong(Long.parseLong(d2.substring(0, indexOf2) + d2.substring(indexOf2 + 1)));
            this.scale = this.scale + (indexOf2 - d2.length()) + 1;
        }
        this.scale += i;
        compact();
        this.explicitExactDouble = true;
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void setToBigDecimal(BigDecimal bigDecimal) {
        setBcdToZero();
        this.flags = 0;
        if (bigDecimal.signum() == -1) {
            this.flags = (byte) (this.flags | 1);
            bigDecimal = bigDecimal.negate();
        }
        if (bigDecimal.signum() != 0) {
            _setToBigDecimal(bigDecimal);
            compact();
        }
    }

    private void _setToBigDecimal(BigDecimal bigDecimal) {
        int scale2 = bigDecimal.scale();
        _setToBigInteger(bigDecimal.scaleByPowerOfTen(scale2).toBigInteger());
        this.scale -= scale2;
    }

    public long toLong(boolean z) {
        long j = 0;
        int i = (this.scale + this.precision) - 1;
        if (z) {
            i = Math.min(i, 17);
        }
        while (i >= 0) {
            j = (j * 10) + ((long) getDigitPos(i - this.scale));
            i--;
        }
        return isNegative() ? -j : j;
    }

    public long toFractionLong(boolean z) {
        int i = this.scale;
        if (z) {
            i = Math.min(i, this.rReqPos);
        }
        long j = 0;
        for (int i2 = -1; i2 >= i && ((double) j) <= 1.0E17d; i2--) {
            j = (j * 10) + ((long) getDigitPos(i2 - this.scale));
        }
        if (!z) {
            while (j > 0 && j % 10 == 0) {
                j /= 10;
            }
        }
        return j;
    }

    public boolean fitsInLong() {
        if (isInfinite() || isNaN()) {
            return false;
        }
        if (isZeroish()) {
            return true;
        }
        if (this.scale < 0) {
            return false;
        }
        int magnitude = getMagnitude();
        if (magnitude < 18) {
            return true;
        }
        if (magnitude > 18) {
            return false;
        }
        for (int i = 0; i < this.precision; i++) {
            byte digit = getDigit(18 - i);
            byte[] bArr = INT64_BCD;
            if (digit < bArr[i]) {
                return true;
            }
            if (digit > bArr[i]) {
                return false;
            }
        }
        return isNegative();
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public double toDouble() {
        if (isNaN()) {
            return Double.NaN;
        }
        if (isInfinite()) {
            return isNegative() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        StringBuilder sb = new StringBuilder();
        toScientificString(sb);
        return Double.valueOf(sb.toString()).doubleValue();
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public BigDecimal toBigDecimal() {
        if (this.isApproximate) {
            convertToAccurateDouble();
        }
        return bcdToBigDecimal();
    }

    public void truncate() {
        int i = this.scale;
        if (i < 0) {
            shiftRight(-i);
            this.scale = 0;
            compact();
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void roundToNickel(int i, MathContext mathContext) {
        roundToMagnitude(i, mathContext, true);
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void roundToMagnitude(int i, MathContext mathContext) {
        roundToMagnitude(i, mathContext, false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004f, code lost:
        if (r3 < 7) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x011e, code lost:
        if (r6 == -2) goto L_0x0059;
     */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x014e  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0154  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0177  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0100 A[ADDED_TO_REGION] */
    private void roundToMagnitude(int i, MathContext mathContext, boolean z) {
        int i2;
        boolean roundingDirection;
        byte b;
        int safeSubtract = safeSubtract(i, this.scale);
        int precision2 = mathContext.getPrecision();
        if (precision2 > 0) {
            int i3 = this.precision;
            if (i3 - precision2 > safeSubtract) {
                safeSubtract = i3 - precision2;
            }
        }
        byte digitPos = getDigitPos(safeSubtract);
        if ((safeSubtract > 0 || this.isApproximate || !(!z || digitPos == 0 || digitPos == 5)) && this.precision != 0) {
            byte digitPos2 = getDigitPos(safeSubtract(safeSubtract, 1));
            if (!this.isApproximate) {
                if (!z || digitPos == 2 || digitPos == 7) {
                    if (digitPos2 >= 5) {
                        if (digitPos2 <= 5) {
                            int safeSubtract2 = safeSubtract(safeSubtract, 2);
                            while (true) {
                                if (safeSubtract2 < 0) {
                                    i2 = 2;
                                    break;
                                } else if (getDigitPos(safeSubtract2) != 0) {
                                    break;
                                } else {
                                    safeSubtract2--;
                                }
                            }
                        }
                    }
                } else if (digitPos >= 2) {
                    if (digitPos >= 5) {
                    }
                }
                i2 = 1;
                roundingDirection = RoundingUtils.getRoundingDirection(z ? digitPos % 2 == 0 : !(digitPos >= 2 && digitPos <= 7 && ((digitPos != 2 || i2 == 3) && !(digitPos == 7 && i2 == 3))), isNegative(), i2, mathContext.getRoundingMode().ordinal(), this);
                if (safeSubtract < this.precision) {
                    setBcdToZero();
                    this.scale = i;
                } else {
                    shiftRight(safeSubtract);
                }
                if (z) {
                    b = 9;
                } else if (digitPos < 5 && roundingDirection) {
                    setDigitPos(0, (byte) 0);
                    compact();
                    return;
                } else if (digitPos < 5 || roundingDirection) {
                    setDigitPos(0, (byte) 5);
                    return;
                } else {
                    b = 9;
                    setDigitPos(0, (byte) 9);
                    digitPos = 9;
                }
                if (!roundingDirection) {
                    if (digitPos == b) {
                        int i4 = 0;
                        while (getDigitPos(i4) == b) {
                            i4++;
                        }
                        shiftRight(i4);
                    }
                    setDigitPos(0, (byte) (getDigitPos(0) + 1));
                    this.precision++;
                }
                compact();
            }
            int safeSubtract3 = safeSubtract(safeSubtract, 2);
            int max = Math.max(0, this.precision - 14);
            if (digitPos2 == 0 && (!z || digitPos == 0 || digitPos == 5)) {
                while (true) {
                    if (safeSubtract3 < max) {
                        i2 = -1;
                        break;
                    } else if (getDigitPos(safeSubtract3) != 0) {
                        break;
                    } else {
                        safeSubtract3--;
                    }
                }
            } else {
                if (digitPos2 == 4 && (!z || digitPos == 2 || digitPos == 7)) {
                    while (true) {
                        if (safeSubtract3 < max) {
                            break;
                        } else if (getDigitPos(safeSubtract3) != 9) {
                            break;
                        } else {
                            safeSubtract3--;
                        }
                    }
                } else {
                    if (digitPos2 != 5 || (z && digitPos != 2 && digitPos != 7)) {
                        if (digitPos2 == 9 && (!z || digitPos == 4 || digitPos == 9)) {
                            while (true) {
                                if (safeSubtract3 < max) {
                                    i2 = -2;
                                    break;
                                } else if (getDigitPos(safeSubtract3) != 9) {
                                    break;
                                } else {
                                    safeSubtract3--;
                                }
                            }
                        } else if (z) {
                        }
                    } else {
                        while (true) {
                            if (safeSubtract3 < max) {
                                break;
                            } else if (getDigitPos(safeSubtract3) != 0) {
                                break;
                            } else {
                                safeSubtract3--;
                            }
                        }
                    }
                    i2 = 3;
                    boolean roundsAtMidpoint = RoundingUtils.roundsAtMidpoint(mathContext.getRoundingMode().ordinal());
                    if (safeSubtract(safeSubtract, 1) >= this.precision - 14 || ((roundsAtMidpoint && i2 == 2) || (!roundsAtMidpoint && i2 < 0))) {
                        convertToAccurateDouble();
                        roundToMagnitude(i, mathContext, z);
                        return;
                    }
                    this.isApproximate = false;
                    this.origDouble = XPath.MATCH_SCORE_QNAME;
                    this.origDelta = 0;
                    if (safeSubtract <= 0 && (!z || digitPos == 0 || digitPos == 5)) {
                        return;
                    }
                    if (i2 == -1) {
                        i2 = 1;
                    }
                }
                i2 = 2;
                boolean roundsAtMidpoint2 = RoundingUtils.roundsAtMidpoint(mathContext.getRoundingMode().ordinal());
                if (safeSubtract(safeSubtract, 1) >= this.precision - 14) {
                }
                convertToAccurateDouble();
                roundToMagnitude(i, mathContext, z);
                return;
            }
            i2 = 1;
            boolean roundsAtMidpoint22 = RoundingUtils.roundsAtMidpoint(mathContext.getRoundingMode().ordinal());
            if (safeSubtract(safeSubtract, 1) >= this.precision - 14) {
            }
            convertToAccurateDouble();
            roundToMagnitude(i, mathContext, z);
            return;
            i2 = 3;
            if (z) {
                roundingDirection = RoundingUtils.getRoundingDirection(z ? digitPos % 2 == 0 : !(digitPos >= 2 && digitPos <= 7 && ((digitPos != 2 || i2 == 3) && !(digitPos == 7 && i2 == 3))), isNegative(), i2, mathContext.getRoundingMode().ordinal(), this);
                if (safeSubtract < this.precision) {
                }
                if (z) {
                }
                if (!roundingDirection) {
                }
                compact();
            }
            roundingDirection = RoundingUtils.getRoundingDirection(z ? digitPos % 2 == 0 : !(digitPos >= 2 && digitPos <= 7 && ((digitPos != 2 || i2 == 3) && !(digitPos == 7 && i2 == 3))), isNegative(), i2, mathContext.getRoundingMode().ordinal(), this);
            if (safeSubtract < this.precision) {
            }
            if (z) {
            }
            if (!roundingDirection) {
            }
            compact();
            roundingDirection = RoundingUtils.getRoundingDirection(z ? digitPos % 2 == 0 : !(digitPos >= 2 && digitPos <= 7 && ((digitPos != 2 || i2 == 3) && !(digitPos == 7 && i2 == 3))), isNegative(), i2, mathContext.getRoundingMode().ordinal(), this);
            if (safeSubtract < this.precision) {
            }
            if (z) {
            }
            if (!roundingDirection) {
            }
            compact();
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public void roundToInfinity() {
        if (this.isApproximate) {
            convertToAccurateDouble();
        }
    }

    @Deprecated
    public void appendDigit(byte b, int i, boolean z) {
        if (b != 0) {
            int i2 = this.scale;
            if (i2 > 0) {
                i += i2;
                if (z) {
                    this.scale = 0;
                }
            }
            int i3 = i + 1;
            shiftLeft(i3);
            setDigitPos(0, b);
            if (z) {
                this.scale += i3;
            }
        } else if (z && this.precision != 0) {
            this.scale += i + 1;
        }
    }

    @Override // ohos.global.icu.impl.number.DecimalQuantity
    public String toPlainString() {
        StringBuilder sb = new StringBuilder();
        if (isNegative()) {
            sb.append(LocaleUtility.IETF_SEPARATOR);
        }
        if (this.precision == 0 || getMagnitude() < 0) {
            sb.append('0');
        }
        for (int upperDisplayMagnitude = getUpperDisplayMagnitude(); upperDisplayMagnitude >= getLowerDisplayMagnitude(); upperDisplayMagnitude--) {
            sb.append((char) (getDigit(upperDisplayMagnitude) + 48));
            if (upperDisplayMagnitude == 0) {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    public String toScientificString() {
        StringBuilder sb = new StringBuilder();
        toScientificString(sb);
        return sb.toString();
    }

    public void toScientificString(StringBuilder sb) {
        if (isNegative()) {
            sb.append(LocaleUtility.IETF_SEPARATOR);
        }
        int i = this.precision;
        if (i == 0) {
            sb.append("0E+0");
            return;
        }
        int i2 = i - 1;
        sb.append((char) (getDigitPos(i2) + 48));
        int i3 = i2 - 1;
        if (i3 >= 0) {
            sb.append('.');
            while (i3 >= 0) {
                sb.append((char) (getDigitPos(i3) + 48));
                i3--;
            }
        }
        sb.append('E');
        int i4 = i2 + this.scale;
        if (i4 == Integer.MIN_VALUE) {
            sb.append("-2147483648");
            return;
        }
        if (i4 < 0) {
            i4 *= -1;
            sb.append(LocaleUtility.IETF_SEPARATOR);
        } else {
            sb.append('+');
        }
        if (i4 == 0) {
            sb.append('0');
        }
        int length = sb.length();
        while (i4 > 0) {
            sb.insert(length, (char) ((i4 % 10) + 48));
            i4 /= 10;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof DecimalQuantity_AbstractBCD)) {
            return false;
        }
        DecimalQuantity_AbstractBCD decimalQuantity_AbstractBCD = (DecimalQuantity_AbstractBCD) obj;
        if (!(this.scale == decimalQuantity_AbstractBCD.scale && this.precision == decimalQuantity_AbstractBCD.precision && this.flags == decimalQuantity_AbstractBCD.flags && this.lReqPos == decimalQuantity_AbstractBCD.lReqPos && this.rReqPos == decimalQuantity_AbstractBCD.rReqPos && this.isApproximate == decimalQuantity_AbstractBCD.isApproximate)) {
            return false;
        }
        if (this.precision == 0) {
            return true;
        }
        if (this.isApproximate) {
            return this.origDouble == decimalQuantity_AbstractBCD.origDouble && this.origDelta == decimalQuantity_AbstractBCD.origDelta;
        }
        for (int upperDisplayMagnitude = getUpperDisplayMagnitude(); upperDisplayMagnitude >= getLowerDisplayMagnitude(); upperDisplayMagnitude--) {
            if (getDigit(upperDisplayMagnitude) != decimalQuantity_AbstractBCD.getDigit(upperDisplayMagnitude)) {
                return false;
            }
        }
        return true;
    }
}
