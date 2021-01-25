package ohos.global.icu.impl.number.parse;

import java.util.Comparator;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.impl.number.DecimalQuantity_DualStorageBCD;

public class ParsedNumber {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final Comparator<ParsedNumber> COMPARATOR = new Comparator<ParsedNumber>() {
        /* class ohos.global.icu.impl.number.parse.ParsedNumber.AnonymousClass1 */

        public int compare(ParsedNumber parsedNumber, ParsedNumber parsedNumber2) {
            return parsedNumber.charEnd - parsedNumber2.charEnd;
        }
    };
    public static final int FLAG_FAIL = 256;
    public static final int FLAG_HAS_DECIMAL_SEPARATOR = 32;
    public static final int FLAG_HAS_EXPONENT = 8;
    public static final int FLAG_INFINITY = 128;
    public static final int FLAG_NAN = 64;
    public static final int FLAG_NEGATIVE = 1;
    public static final int FLAG_PERCENT = 2;
    public static final int FLAG_PERMILLE = 4;
    public int charEnd;
    public String currencyCode;
    public int flags;
    public String prefix;
    public DecimalQuantity_DualStorageBCD quantity;
    public String suffix;

    public ParsedNumber() {
        clear();
    }

    public void clear() {
        this.quantity = null;
        this.charEnd = 0;
        this.flags = 0;
        this.prefix = null;
        this.suffix = null;
        this.currencyCode = null;
    }

    public void copyFrom(ParsedNumber parsedNumber) {
        DecimalQuantity_DualStorageBCD decimalQuantity_DualStorageBCD;
        DecimalQuantity_DualStorageBCD decimalQuantity_DualStorageBCD2 = parsedNumber.quantity;
        if (decimalQuantity_DualStorageBCD2 == null) {
            decimalQuantity_DualStorageBCD = null;
        } else {
            decimalQuantity_DualStorageBCD = (DecimalQuantity_DualStorageBCD) decimalQuantity_DualStorageBCD2.createCopy();
        }
        this.quantity = decimalQuantity_DualStorageBCD;
        this.charEnd = parsedNumber.charEnd;
        this.flags = parsedNumber.flags;
        this.prefix = parsedNumber.prefix;
        this.suffix = parsedNumber.suffix;
        this.currencyCode = parsedNumber.currencyCode;
    }

    public void setCharsConsumed(StringSegment stringSegment) {
        this.charEnd = stringSegment.getOffset();
    }

    public void postProcess() {
        DecimalQuantity_DualStorageBCD decimalQuantity_DualStorageBCD = this.quantity;
        if (decimalQuantity_DualStorageBCD != null && (this.flags & 1) != 0) {
            decimalQuantity_DualStorageBCD.negate();
        }
    }

    public boolean success() {
        return this.charEnd > 0 && (this.flags & 256) == 0;
    }

    public boolean seenNumber() {
        if (this.quantity == null) {
            int i = this.flags;
            if ((i & 64) == 0 && (i & 128) == 0) {
                return false;
            }
        }
        return true;
    }

    public Number getNumber() {
        return getNumber(0);
    }

    public Number getNumber(int i) {
        boolean z = (this.flags & 64) != 0;
        boolean z2 = (this.flags & 128) != 0;
        boolean z3 = (i & 4096) != 0;
        boolean z4 = (i & 16) != 0;
        if (z) {
            return Double.valueOf(Double.NaN);
        }
        if (z2) {
            if ((this.flags & 1) != 0) {
                return Double.valueOf(Double.NEGATIVE_INFINITY);
            }
            return Double.valueOf(Double.POSITIVE_INFINITY);
        } else if (this.quantity.isZeroish() && this.quantity.isNegative() && !z4) {
            return Double.valueOf(-0.0d);
        } else {
            if (!this.quantity.fitsInLong() || z3) {
                return this.quantity.toBigDecimal();
            }
            return Long.valueOf(this.quantity.toLong(false));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isBetterThan(ParsedNumber parsedNumber) {
        return COMPARATOR.compare(this, parsedNumber) > 0;
    }
}
