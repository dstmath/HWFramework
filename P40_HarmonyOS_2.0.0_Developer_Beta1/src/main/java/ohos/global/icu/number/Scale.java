package ohos.global.icu.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.impl.number.RoundingUtils;

public class Scale {
    private static final BigDecimal BIG_DECIMAL_100 = BigDecimal.valueOf(100L);
    private static final BigDecimal BIG_DECIMAL_1000 = BigDecimal.valueOf(1000L);
    private static final Scale DEFAULT = new Scale(0, null);
    private static final Scale HUNDRED = new Scale(2, null);
    private static final Scale THOUSAND = new Scale(3, null);
    final BigDecimal arbitrary;
    final int magnitude;
    final MathContext mc;
    final BigDecimal reciprocal;

    private Scale(int i, BigDecimal bigDecimal) {
        this(i, bigDecimal, RoundingUtils.DEFAULT_MATH_CONTEXT_34_DIGITS);
    }

    private Scale(int i, BigDecimal bigDecimal, MathContext mathContext) {
        if (bigDecimal != null) {
            if (bigDecimal.compareTo(BigDecimal.ZERO) == 0) {
                bigDecimal = BigDecimal.ZERO;
            } else {
                bigDecimal = bigDecimal.stripTrailingZeros();
            }
            if (bigDecimal.precision() == 1 && bigDecimal.unscaledValue().equals(BigInteger.ONE)) {
                i -= bigDecimal.scale();
                bigDecimal = null;
            }
        }
        this.magnitude = i;
        this.arbitrary = bigDecimal;
        this.mc = mathContext;
        if (bigDecimal == null || BigDecimal.ZERO.compareTo(bigDecimal) == 0) {
            this.reciprocal = null;
        } else {
            this.reciprocal = BigDecimal.ONE.divide(bigDecimal, mathContext);
        }
    }

    public static Scale none() {
        return DEFAULT;
    }

    public static Scale powerOfTen(int i) {
        if (i == 0) {
            return DEFAULT;
        }
        if (i == 2) {
            return HUNDRED;
        }
        if (i == 3) {
            return THOUSAND;
        }
        return new Scale(i, null);
    }

    public static Scale byBigDecimal(BigDecimal bigDecimal) {
        if (bigDecimal.compareTo(BigDecimal.ONE) == 0) {
            return DEFAULT;
        }
        if (bigDecimal.compareTo(BIG_DECIMAL_100) == 0) {
            return HUNDRED;
        }
        if (bigDecimal.compareTo(BIG_DECIMAL_1000) == 0) {
            return THOUSAND;
        }
        return new Scale(0, bigDecimal);
    }

    public static Scale byDouble(double d) {
        if (d == 1.0d) {
            return DEFAULT;
        }
        if (d == 100.0d) {
            return HUNDRED;
        }
        if (d == 1000.0d) {
            return THOUSAND;
        }
        return new Scale(0, BigDecimal.valueOf(d));
    }

    public static Scale byDoubleAndPowerOfTen(double d, int i) {
        return new Scale(i, BigDecimal.valueOf(d));
    }

    /* access modifiers changed from: package-private */
    public boolean isValid() {
        return (this.magnitude == 0 && this.arbitrary == null) ? false : true;
    }

    @Deprecated
    public Scale withMathContext(MathContext mathContext) {
        if (this.mc.equals(mathContext)) {
            return this;
        }
        return new Scale(this.magnitude, this.arbitrary, mathContext);
    }

    @Deprecated
    public void applyTo(DecimalQuantity decimalQuantity) {
        decimalQuantity.adjustMagnitude(this.magnitude);
        BigDecimal bigDecimal = this.arbitrary;
        if (bigDecimal != null) {
            decimalQuantity.multiplyBy(bigDecimal);
        }
    }

    @Deprecated
    public void applyReciprocalTo(DecimalQuantity decimalQuantity) {
        decimalQuantity.adjustMagnitude(-this.magnitude);
        BigDecimal bigDecimal = this.reciprocal;
        if (bigDecimal != null) {
            decimalQuantity.multiplyBy(bigDecimal);
            decimalQuantity.roundToMagnitude(decimalQuantity.getMagnitude() - this.mc.getPrecision(), this.mc);
        }
    }
}
