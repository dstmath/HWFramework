package ohos.global.icu.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.impl.number.MultiplierProducer;
import ohos.global.icu.impl.number.RoundingUtils;
import ohos.global.icu.util.Currency;

public abstract class Precision implements Cloneable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final FracSigRounderImpl COMPACT_STRATEGY = new FracSigRounderImpl(0, 0, 2, -1);
    static final FractionRounderImpl DEFAULT_MAX_FRAC_6 = new FractionRounderImpl(0, 6);
    static final FractionRounderImpl FIXED_FRAC_0 = new FractionRounderImpl(0, 0);
    static final FractionRounderImpl FIXED_FRAC_2 = new FractionRounderImpl(2, 2);
    static final SignificantRounderImpl FIXED_SIG_2 = new SignificantRounderImpl(2, 2);
    static final SignificantRounderImpl FIXED_SIG_3 = new SignificantRounderImpl(3, 3);
    static final CurrencyRounderImpl MONETARY_CASH = new CurrencyRounderImpl(Currency.CurrencyUsage.CASH);
    static final CurrencyRounderImpl MONETARY_STANDARD = new CurrencyRounderImpl(Currency.CurrencyUsage.STANDARD);
    static final IncrementFiveRounderImpl NICKEL = new IncrementFiveRounderImpl(new BigDecimal("0.05"), 2, 2);
    static final InfiniteRounderImpl NONE = new InfiniteRounderImpl();
    static final PassThroughRounderImpl PASS_THROUGH = new PassThroughRounderImpl();
    static final SignificantRounderImpl RANGE_SIG_2_3 = new SignificantRounderImpl(2, 3);
    MathContext mathContext = RoundingUtils.DEFAULT_MATH_CONTEXT_UNLIMITED;

    static class PassThroughRounderImpl extends Precision {
        @Override // ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
        }
    }

    /* access modifiers changed from: private */
    public static int getDisplayMagnitudeFraction(int i) {
        if (i == 0) {
            return Integer.MAX_VALUE;
        }
        return -i;
    }

    /* access modifiers changed from: private */
    public static int getRoundingMagnitudeFraction(int i) {
        if (i == -1) {
            return Integer.MIN_VALUE;
        }
        return -i;
    }

    @Deprecated
    public abstract void apply(DecimalQuantity decimalQuantity);

    Precision() {
    }

    public static Precision unlimited() {
        return constructInfinite();
    }

    public static FractionPrecision integer() {
        return constructFraction(0, 0);
    }

    public static FractionPrecision fixedFraction(int i) {
        if (i >= 0 && i <= 999) {
            return constructFraction(i, i);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static FractionPrecision minFraction(int i) {
        if (i >= 0 && i <= 999) {
            return constructFraction(i, -1);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static FractionPrecision maxFraction(int i) {
        if (i >= 0 && i <= 999) {
            return constructFraction(0, i);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static FractionPrecision minMaxFraction(int i, int i2) {
        if (i >= 0 && i2 <= 999 && i <= i2) {
            return constructFraction(i, i2);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static Precision fixedSignificantDigits(int i) {
        if (i >= 1 && i <= 999) {
            return constructSignificant(i, i);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision minSignificantDigits(int i) {
        if (i >= 1 && i <= 999) {
            return constructSignificant(i, -1);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision maxSignificantDigits(int i) {
        if (i >= 1 && i <= 999) {
            return constructSignificant(1, i);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision minMaxSignificantDigits(int i, int i2) {
        if (i >= 1 && i2 <= 999 && i <= i2) {
            return constructSignificant(i, i2);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision increment(BigDecimal bigDecimal) {
        if (bigDecimal != null && bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
            return constructIncrement(bigDecimal);
        }
        throw new IllegalArgumentException("Rounding increment must be positive and non-null");
    }

    public static CurrencyPrecision currency(Currency.CurrencyUsage currencyUsage) {
        if (currencyUsage != null) {
            return constructCurrency(currencyUsage);
        }
        throw new IllegalArgumentException("CurrencyUsage must be non-null");
    }

    @Deprecated
    public Precision withMode(MathContext mathContext2) {
        if (this.mathContext.equals(mathContext2)) {
            return this;
        }
        Precision precision = (Precision) clone();
        precision.mathContext = mathContext2;
        return precision;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    static Precision constructInfinite() {
        return NONE;
    }

    static FractionPrecision constructFraction(int i, int i2) {
        if (i == 0 && i2 == 0) {
            return FIXED_FRAC_0;
        }
        if (i == 2 && i2 == 2) {
            return FIXED_FRAC_2;
        }
        if (i == 0 && i2 == 6) {
            return DEFAULT_MAX_FRAC_6;
        }
        return new FractionRounderImpl(i, i2);
    }

    static Precision constructSignificant(int i, int i2) {
        if (i == 2 && i2 == 2) {
            return FIXED_SIG_2;
        }
        if (i == 3 && i2 == 3) {
            return FIXED_SIG_3;
        }
        if (i == 2 && i2 == 3) {
            return RANGE_SIG_2_3;
        }
        return new SignificantRounderImpl(i, i2);
    }

    static Precision constructFractionSignificant(FractionPrecision fractionPrecision, int i, int i2) {
        FracSigRounderImpl fracSigRounderImpl;
        FractionRounderImpl fractionRounderImpl = (FractionRounderImpl) fractionPrecision;
        if (fractionRounderImpl.minFrac == 0 && fractionRounderImpl.maxFrac == 0 && i == 2) {
            fracSigRounderImpl = COMPACT_STRATEGY;
        } else {
            fracSigRounderImpl = new FracSigRounderImpl(fractionRounderImpl.minFrac, fractionRounderImpl.maxFrac, i, i2);
        }
        return fracSigRounderImpl.withMode(fractionRounderImpl.mathContext);
    }

    static Precision constructIncrement(BigDecimal bigDecimal) {
        if (bigDecimal.equals(NICKEL.increment)) {
            return NICKEL;
        }
        BigDecimal stripTrailingZeros = bigDecimal.stripTrailingZeros();
        if (stripTrailingZeros.precision() == 1) {
            int scale = bigDecimal.scale();
            int scale2 = stripTrailingZeros.scale();
            BigInteger unscaledValue = stripTrailingZeros.unscaledValue();
            if (unscaledValue.intValue() == 1) {
                return new IncrementOneRounderImpl(bigDecimal, scale, scale2);
            }
            if (unscaledValue.intValue() == 5) {
                return new IncrementFiveRounderImpl(bigDecimal, scale, scale2);
            }
        }
        return new IncrementRounderImpl(bigDecimal);
    }

    static CurrencyPrecision constructCurrency(Currency.CurrencyUsage currencyUsage) {
        if (currencyUsage == Currency.CurrencyUsage.STANDARD) {
            return MONETARY_STANDARD;
        }
        if (currencyUsage == Currency.CurrencyUsage.CASH) {
            return MONETARY_CASH;
        }
        throw new AssertionError();
    }

    static Precision constructFromCurrency(CurrencyPrecision currencyPrecision, Currency currency) {
        Precision precision;
        CurrencyRounderImpl currencyRounderImpl = (CurrencyRounderImpl) currencyPrecision;
        double roundingIncrement = currency.getRoundingIncrement(currencyRounderImpl.usage);
        if (roundingIncrement != XPath.MATCH_SCORE_QNAME) {
            precision = constructIncrement(BigDecimal.valueOf(roundingIncrement));
        } else {
            int defaultFractionDigits = currency.getDefaultFractionDigits(currencyRounderImpl.usage);
            precision = constructFraction(defaultFractionDigits, defaultFractionDigits);
        }
        return precision.withMode(currencyRounderImpl.mathContext);
    }

    static Precision constructPassThrough() {
        return PASS_THROUGH;
    }

    /* access modifiers changed from: package-private */
    public Precision withLocaleData(Currency currency) {
        return this instanceof CurrencyPrecision ? ((CurrencyPrecision) this).withCurrency(currency) : this;
    }

    /* access modifiers changed from: package-private */
    public int chooseMultiplierAndApply(DecimalQuantity decimalQuantity, MultiplierProducer multiplierProducer) {
        int multiplier;
        int magnitude = decimalQuantity.getMagnitude();
        int multiplier2 = multiplierProducer.getMultiplier(magnitude);
        decimalQuantity.adjustMagnitude(multiplier2);
        apply(decimalQuantity);
        if (decimalQuantity.isZeroish() || decimalQuantity.getMagnitude() == magnitude + multiplier2 || multiplier2 == (multiplier = multiplierProducer.getMultiplier(magnitude + 1))) {
            return multiplier2;
        }
        decimalQuantity.adjustMagnitude(multiplier - multiplier2);
        apply(decimalQuantity);
        return multiplier;
    }

    /* access modifiers changed from: package-private */
    public static class InfiniteRounderImpl extends Precision {
        @Override // ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            decimalQuantity.roundToInfinity();
            decimalQuantity.setMinFraction(0);
        }
    }

    /* access modifiers changed from: package-private */
    public static class FractionRounderImpl extends FractionPrecision {
        final int maxFrac;
        final int minFrac;

        public FractionRounderImpl(int i, int i2) {
            this.minFrac = i;
            this.maxFrac = i2;
        }

        @Override // ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            decimalQuantity.roundToMagnitude(Precision.getRoundingMagnitudeFraction(this.maxFrac), this.mathContext);
            decimalQuantity.setMinFraction(Math.max(0, -Precision.getDisplayMagnitudeFraction(this.minFrac)));
        }
    }

    /* access modifiers changed from: package-private */
    public static class SignificantRounderImpl extends Precision {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        final int maxSig;
        final int minSig;

        public SignificantRounderImpl(int i, int i2) {
            this.minSig = i;
            this.maxSig = i2;
        }

        @Override // ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            decimalQuantity.roundToMagnitude(Precision.getRoundingMagnitudeSignificant(decimalQuantity, this.maxSig), this.mathContext);
            decimalQuantity.setMinFraction(Math.max(0, -Precision.getDisplayMagnitudeSignificant(decimalQuantity, this.minSig)));
            if (decimalQuantity.isZeroish() && this.minSig > 0) {
                decimalQuantity.setMinInteger(1);
            }
        }

        public void apply(DecimalQuantity decimalQuantity, int i) {
            decimalQuantity.setMinFraction(this.minSig - i);
        }
    }

    static class FracSigRounderImpl extends Precision {
        final int maxFrac;
        final int maxSig;
        final int minFrac;
        final int minSig;

        public FracSigRounderImpl(int i, int i2, int i3, int i4) {
            this.minFrac = i;
            this.maxFrac = i2;
            this.minSig = i3;
            this.maxSig = i4;
        }

        @Override // ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            int i;
            int displayMagnitudeFraction = Precision.getDisplayMagnitudeFraction(this.minFrac);
            int roundingMagnitudeFraction = Precision.getRoundingMagnitudeFraction(this.maxFrac);
            int i2 = this.minSig;
            if (i2 == -1) {
                i = Math.max(roundingMagnitudeFraction, Precision.getRoundingMagnitudeSignificant(decimalQuantity, this.maxSig));
            } else {
                i = Math.min(roundingMagnitudeFraction, Precision.getDisplayMagnitudeSignificant(decimalQuantity, i2));
            }
            decimalQuantity.roundToMagnitude(i, this.mathContext);
            decimalQuantity.setMinFraction(Math.max(0, -displayMagnitudeFraction));
        }
    }

    /* access modifiers changed from: package-private */
    public static class IncrementRounderImpl extends Precision {
        final BigDecimal increment;

        public IncrementRounderImpl(BigDecimal bigDecimal) {
            this.increment = bigDecimal;
        }

        @Override // ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            decimalQuantity.roundToIncrement(this.increment, this.mathContext);
            decimalQuantity.setMinFraction(this.increment.scale());
        }
    }

    /* access modifiers changed from: package-private */
    public static class IncrementOneRounderImpl extends IncrementRounderImpl {
        final int maxFrac;
        final int minFrac;

        public IncrementOneRounderImpl(BigDecimal bigDecimal, int i, int i2) {
            super(bigDecimal);
            this.minFrac = i;
            this.maxFrac = i2;
        }

        @Override // ohos.global.icu.number.Precision.IncrementRounderImpl, ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            decimalQuantity.roundToMagnitude(-this.maxFrac, this.mathContext);
            decimalQuantity.setMinFraction(this.minFrac);
        }
    }

    /* access modifiers changed from: package-private */
    public static class IncrementFiveRounderImpl extends IncrementRounderImpl {
        final int maxFrac;
        final int minFrac;

        public IncrementFiveRounderImpl(BigDecimal bigDecimal, int i, int i2) {
            super(bigDecimal);
            this.minFrac = i;
            this.maxFrac = i2;
        }

        @Override // ohos.global.icu.number.Precision.IncrementRounderImpl, ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            decimalQuantity.roundToNickel(-this.maxFrac, this.mathContext);
            decimalQuantity.setMinFraction(this.minFrac);
        }
    }

    /* access modifiers changed from: package-private */
    public static class CurrencyRounderImpl extends CurrencyPrecision {
        final Currency.CurrencyUsage usage;

        public CurrencyRounderImpl(Currency.CurrencyUsage currencyUsage) {
            this.usage = currencyUsage;
        }

        @Override // ohos.global.icu.number.Precision
        public void apply(DecimalQuantity decimalQuantity) {
            throw new AssertionError();
        }
    }

    /* access modifiers changed from: private */
    public static int getRoundingMagnitudeSignificant(DecimalQuantity decimalQuantity, int i) {
        if (i == -1) {
            return Integer.MIN_VALUE;
        }
        return ((decimalQuantity.isZeroish() ? 0 : decimalQuantity.getMagnitude()) - i) + 1;
    }

    /* access modifiers changed from: private */
    public static int getDisplayMagnitudeSignificant(DecimalQuantity decimalQuantity, int i) {
        return ((decimalQuantity.isZeroish() ? 0 : decimalQuantity.getMagnitude()) - i) + 1;
    }
}
