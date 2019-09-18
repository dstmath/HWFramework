package android.icu.number;

import android.icu.impl.number.DecimalQuantity;
import android.icu.impl.number.PatternStringParser;

@Deprecated
public class Grouper {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final byte B2 = 2;
    private static final byte B3 = 3;
    private static final Grouper DEFAULTS = new Grouper(N2, N2, false);
    private static final Grouper GROUPING_3 = new Grouper((byte) 3, (byte) 3, false);
    private static final Grouper GROUPING_3_2 = new Grouper((byte) 3, (byte) 2, false);
    private static final Grouper GROUPING_3_2_MIN2 = new Grouper((byte) 3, (byte) 2, true);
    private static final Grouper GROUPING_3_MIN2 = new Grouper((byte) 3, (byte) 3, true);
    private static final Grouper MIN2 = new Grouper(N2, N2, true);
    private static final byte N1 = -1;
    private static final byte N2 = -2;
    private static final Grouper NONE = new Grouper((byte) -1, (byte) -1, false);
    private final byte grouping1;
    private final byte grouping2;
    private final boolean min2;

    private Grouper(byte grouping12, byte grouping22, boolean min22) {
        this.grouping1 = grouping12;
        this.grouping2 = grouping22;
        this.min2 = min22;
    }

    @Deprecated
    public static Grouper defaults() {
        return DEFAULTS;
    }

    @Deprecated
    public static Grouper minTwoDigits() {
        return MIN2;
    }

    @Deprecated
    public static Grouper none() {
        return NONE;
    }

    static Grouper getInstance(byte grouping12, byte grouping22, boolean min22) {
        if (grouping12 == -1) {
            return NONE;
        }
        if (!min22 && grouping12 == 3 && grouping22 == 3) {
            return GROUPING_3;
        }
        if (!min22 && grouping12 == 3 && grouping22 == 2) {
            return GROUPING_3_2;
        }
        if (min22 && grouping12 == 3 && grouping22 == 3) {
            return GROUPING_3_MIN2;
        }
        if (min22 && grouping12 == 3 && grouping22 == 2) {
            return GROUPING_3_2_MIN2;
        }
        return new Grouper(grouping12, grouping22, min22);
    }

    /* access modifiers changed from: package-private */
    public Grouper withLocaleData(PatternStringParser.ParsedPatternInfo patternInfo) {
        if (this.grouping1 != -2) {
            return this;
        }
        byte grouping12 = (byte) ((int) (patternInfo.positive.groupingSizes & 65535));
        byte grouping22 = (byte) ((int) ((patternInfo.positive.groupingSizes >>> 16) & 65535));
        byte grouping3 = (byte) ((int) (65535 & (patternInfo.positive.groupingSizes >>> 32)));
        if (grouping22 == -1) {
            grouping12 = -1;
        }
        if (grouping3 == -1) {
            grouping22 = grouping12;
        }
        return getInstance(grouping12, grouping22, this.min2);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        if (((r6.getUpperDisplayMagnitude() - r4.grouping1) + 1) >= (r4.min2 ? 2 : 1)) goto L_0x002b;
     */
    public boolean groupAtPosition(int position, DecimalQuantity value) {
        if (this.grouping1 == -1 || this.grouping1 == 0) {
            return false;
        }
        int position2 = position - this.grouping1;
        boolean z = true;
        if (position2 >= 0 && position2 % this.grouping2 == 0) {
        }
        z = false;
        return z;
    }
}
