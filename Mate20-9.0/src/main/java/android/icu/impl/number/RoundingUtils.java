package android.icu.impl.number;

import java.math.MathContext;
import java.math.RoundingMode;

public class RoundingUtils {
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;
    private static final MathContext[] MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS = new MathContext[RoundingMode.values().length];
    private static final MathContext[] MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED = new MathContext[RoundingMode.values().length];
    public static final int MAX_INT_FRAC_SIG = 100;
    public static final int SECTION_LOWER = 1;
    public static final int SECTION_MIDPOINT = 2;
    public static final int SECTION_UPPER = 3;

    static {
        for (int i = 0; i < MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS.length; i++) {
            MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED[i] = new MathContext(0, RoundingMode.valueOf(i));
            MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS[i] = new MathContext(34);
        }
    }

    public static boolean getRoundingDirection(boolean isEven, boolean isNegative, int section, int roundingMode, Object reference) {
        switch (roundingMode) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return isNegative;
            case 3:
                return !isNegative;
            case 4:
                switch (section) {
                    case 1:
                        return true;
                    case 2:
                        return false;
                    case 3:
                        return false;
                }
            case 5:
                switch (section) {
                    case 1:
                        return true;
                    case 2:
                        return true;
                    case 3:
                        return false;
                }
            case 6:
                switch (section) {
                    case 1:
                        return true;
                    case 2:
                        return isEven;
                    case 3:
                        return false;
                }
        }
        throw new ArithmeticException("Rounding is required on " + reference.toString());
    }

    public static boolean roundsAtMidpoint(int roundingMode) {
        switch (roundingMode) {
            case 0:
            case 1:
            case 2:
            case 3:
                return false;
            default:
                return true;
        }
    }

    public static MathContext getMathContextOrUnlimited(DecimalFormatProperties properties) {
        MathContext mathContext = properties.getMathContext();
        if (mathContext != null) {
            return mathContext;
        }
        RoundingMode roundingMode = properties.getRoundingMode();
        if (roundingMode == null) {
            roundingMode = RoundingMode.HALF_EVEN;
        }
        return MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED[roundingMode.ordinal()];
    }

    public static MathContext getMathContextOr34Digits(DecimalFormatProperties properties) {
        MathContext mathContext = properties.getMathContext();
        if (mathContext != null) {
            return mathContext;
        }
        RoundingMode roundingMode = properties.getRoundingMode();
        if (roundingMode == null) {
            roundingMode = RoundingMode.HALF_EVEN;
        }
        return MATH_CONTEXT_BY_ROUNDING_MODE_34_DIGITS[roundingMode.ordinal()];
    }

    public static MathContext mathContextUnlimited(RoundingMode roundingMode) {
        return MATH_CONTEXT_BY_ROUNDING_MODE_UNLIMITED[roundingMode.ordinal()];
    }
}
