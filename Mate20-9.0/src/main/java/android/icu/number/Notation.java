package android.icu.number;

import android.icu.number.NumberFormatter;
import android.icu.text.CompactDecimalFormat;

public class Notation {
    private static final CompactNotation COMPACT_LONG = new CompactNotation(CompactDecimalFormat.CompactStyle.LONG);
    private static final CompactNotation COMPACT_SHORT = new CompactNotation(CompactDecimalFormat.CompactStyle.SHORT);
    private static final ScientificNotation ENGINEERING = new ScientificNotation(3, false, 1, NumberFormatter.SignDisplay.AUTO);
    private static final ScientificNotation SCIENTIFIC = new ScientificNotation(1, false, 1, NumberFormatter.SignDisplay.AUTO);
    private static final SimpleNotation SIMPLE = new SimpleNotation();

    Notation() {
    }

    public static ScientificNotation scientific() {
        return SCIENTIFIC;
    }

    public static ScientificNotation engineering() {
        return ENGINEERING;
    }

    public static CompactNotation compactShort() {
        return COMPACT_SHORT;
    }

    public static CompactNotation compactLong() {
        return COMPACT_LONG;
    }

    public static SimpleNotation simple() {
        return SIMPLE;
    }
}
