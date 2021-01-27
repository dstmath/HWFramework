package ohos.global.icu.impl.number;

import java.math.MathContext;
import java.math.RoundingMode;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.number.Precision;
import ohos.global.icu.number.Scale;
import ohos.global.icu.text.PluralRules;

public class RoundingUtils {
    public static final MathContext DEFAULT_MATH_CONTEXT_34_DIGITS = MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS[DEFAULT_ROUNDING_MODE.ordinal()];
    public static final MathContext DEFAULT_MATH_CONTEXT_UNLIMITED = MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED[DEFAULT_ROUNDING_MODE.ordinal()];
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;
    private static final MathContext[] MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS = new MathContext[RoundingMode.values().length];
    private static final MathContext[] MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED = new MathContext[RoundingMode.values().length];
    public static final int MAX_INT_FRAC_SIG = 999;
    public static final int SECTION_LOWER = 1;
    public static final int SECTION_MIDPOINT = 2;
    public static final int SECTION_UPPER = 3;

    public static boolean roundsAtMidpoint(int i) {
        return (i == 0 || i == 1 || i == 2 || i == 3) ? false : true;
    }

    static {
        for (int i = 0; i < MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS.length; i++) {
            MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED[i] = new MathContext(0, RoundingMode.valueOf(i));
            MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS[i] = new MathContext(34);
        }
    }

    public static boolean getRoundingDirection(boolean z, boolean z2, int i, int i2, Object obj) {
        switch (i2) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return z2;
            case 3:
                return !z2;
            case 4:
                if (i == 1) {
                    return true;
                }
                if (i == 2 || i == 3) {
                    return false;
                }
            case 5:
                if (i == 1 || i == 2) {
                    return true;
                }
                if (i == 3) {
                    return false;
                }
                break;
            case 6:
                if (i == 1) {
                    return true;
                }
                if (i == 2) {
                    return z;
                }
                if (i == 3) {
                    return false;
                }
                break;
        }
        throw new ArithmeticException("Rounding is required on " + obj.toString());
    }

    public static MathContext getMathContextOrUnlimited(DecimalFormatProperties decimalFormatProperties) {
        MathContext mathContext = decimalFormatProperties.getMathContext();
        if (mathContext != null) {
            return mathContext;
        }
        RoundingMode roundingMode = decimalFormatProperties.getRoundingMode();
        if (roundingMode == null) {
            roundingMode = RoundingMode.HALF_EVEN;
        }
        return MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED[roundingMode.ordinal()];
    }

    public static MathContext getMathContextOr34Digits(DecimalFormatProperties decimalFormatProperties) {
        MathContext mathContext = decimalFormatProperties.getMathContext();
        if (mathContext != null) {
            return mathContext;
        }
        RoundingMode roundingMode = decimalFormatProperties.getRoundingMode();
        if (roundingMode == null) {
            roundingMode = RoundingMode.HALF_EVEN;
        }
        return MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS[roundingMode.ordinal()];
    }

    public static MathContext mathContextUnlimited(RoundingMode roundingMode) {
        return MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED[roundingMode.ordinal()];
    }

    public static Scale scaleFromProperties(DecimalFormatProperties decimalFormatProperties) {
        MathContext mathContextOr34Digits = getMathContextOr34Digits(decimalFormatProperties);
        if (decimalFormatProperties.getMagnitudeMultiplier() != 0) {
            return Scale.powerOfTen(decimalFormatProperties.getMagnitudeMultiplier()).withMathContext(mathContextOr34Digits);
        }
        if (decimalFormatProperties.getMultiplier() != null) {
            return Scale.byBigDecimal(decimalFormatProperties.getMultiplier()).withMathContext(mathContextOr34Digits);
        }
        return null;
    }

    public static StandardPlural getPluralSafe(Precision precision, PluralRules pluralRules, DecimalQuantity decimalQuantity) {
        DecimalQuantity createCopy = decimalQuantity.createCopy();
        precision.apply(createCopy);
        return createCopy.getStandardPlural(pluralRules);
    }
}
