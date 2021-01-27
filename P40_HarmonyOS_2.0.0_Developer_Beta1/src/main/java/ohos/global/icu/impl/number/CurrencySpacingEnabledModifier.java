package ohos.global.icu.impl.number;

import java.text.Format;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.text.UnicodeSet;

public class CurrencySpacingEnabledModifier extends ConstantMultiFieldModifier {
    static final short IN_CURRENCY = 0;
    static final short IN_NUMBER = 1;
    static final byte PREFIX = 0;
    static final byte SUFFIX = 1;
    private static final UnicodeSet UNISET_DIGIT = new UnicodeSet("[:digit:]").freeze();
    private static final UnicodeSet UNISET_NOTS = new UnicodeSet("[:^S:]").freeze();
    private final String afterPrefixInsert;
    private final UnicodeSet afterPrefixUnicodeSet;
    private final String beforeSuffixInsert;
    private final UnicodeSet beforeSuffixUnicodeSet;

    public CurrencySpacingEnabledModifier(FormattedStringBuilder formattedStringBuilder, FormattedStringBuilder formattedStringBuilder2, boolean z, boolean z2, DecimalFormatSymbols decimalFormatSymbols) {
        super(formattedStringBuilder, formattedStringBuilder2, z, z2);
        if (formattedStringBuilder.length() <= 0 || formattedStringBuilder.fieldAt(formattedStringBuilder.length() - 1) != NumberFormat.Field.CURRENCY) {
            this.afterPrefixUnicodeSet = null;
            this.afterPrefixInsert = null;
        } else {
            if (getUnicodeSet(decimalFormatSymbols, 0, (byte) 0).contains(formattedStringBuilder.getLastCodePoint())) {
                this.afterPrefixUnicodeSet = getUnicodeSet(decimalFormatSymbols, 1, (byte) 0);
                this.afterPrefixUnicodeSet.freeze();
                this.afterPrefixInsert = getInsertString(decimalFormatSymbols, (byte) 0);
            } else {
                this.afterPrefixUnicodeSet = null;
                this.afterPrefixInsert = null;
            }
        }
        if (formattedStringBuilder2.length() <= 0 || formattedStringBuilder2.fieldAt(0) != NumberFormat.Field.CURRENCY) {
            this.beforeSuffixUnicodeSet = null;
            this.beforeSuffixInsert = null;
            return;
        }
        if (getUnicodeSet(decimalFormatSymbols, 0, (byte) 1).contains(formattedStringBuilder2.getLastCodePoint())) {
            this.beforeSuffixUnicodeSet = getUnicodeSet(decimalFormatSymbols, 1, (byte) 1);
            this.beforeSuffixUnicodeSet.freeze();
            this.beforeSuffixInsert = getInsertString(decimalFormatSymbols, (byte) 1);
            return;
        }
        this.beforeSuffixUnicodeSet = null;
        this.beforeSuffixInsert = null;
    }

    @Override // ohos.global.icu.impl.number.ConstantMultiFieldModifier, ohos.global.icu.impl.number.Modifier
    public int apply(FormattedStringBuilder formattedStringBuilder, int i, int i2) {
        UnicodeSet unicodeSet;
        UnicodeSet unicodeSet2;
        int i3 = i2 - i;
        int i4 = 0;
        if (i3 > 0 && (unicodeSet2 = this.afterPrefixUnicodeSet) != null && unicodeSet2.contains(formattedStringBuilder.codePointAt(i))) {
            i4 = 0 + formattedStringBuilder.insert(i, this.afterPrefixInsert, (Format.Field) null);
        }
        if (i3 > 0 && (unicodeSet = this.beforeSuffixUnicodeSet) != null && unicodeSet.contains(formattedStringBuilder.codePointBefore(i2))) {
            i4 += formattedStringBuilder.insert(i2 + i4, this.beforeSuffixInsert, (Format.Field) null);
        }
        return i4 + super.apply(formattedStringBuilder, i, i2 + i4);
    }

    public static int applyCurrencySpacing(FormattedStringBuilder formattedStringBuilder, int i, int i2, int i3, int i4, DecimalFormatSymbols decimalFormatSymbols) {
        int i5 = 0;
        boolean z = i2 > 0;
        boolean z2 = i4 > 0;
        boolean z3 = (i3 - i) - i2 > 0;
        if (z && z3) {
            i5 = 0 + applyCurrencySpacingAffix(formattedStringBuilder, i + i2, (byte) 0, decimalFormatSymbols);
        }
        return (!z2 || !z3) ? i5 : i5 + applyCurrencySpacingAffix(formattedStringBuilder, i3 + i5, (byte) 1, decimalFormatSymbols);
    }

    private static int applyCurrencySpacingAffix(FormattedStringBuilder formattedStringBuilder, int i, byte b, DecimalFormatSymbols decimalFormatSymbols) {
        Format.Field field;
        if (b == 0) {
            field = formattedStringBuilder.fieldAt(i - 1);
        } else {
            field = formattedStringBuilder.fieldAt(i);
        }
        if (field != NumberFormat.Field.CURRENCY) {
            return 0;
        }
        if (!getUnicodeSet(decimalFormatSymbols, 0, b).contains(b == 0 ? formattedStringBuilder.codePointBefore(i) : formattedStringBuilder.codePointAt(i))) {
            return 0;
        }
        if (!getUnicodeSet(decimalFormatSymbols, 1, b).contains(b == 0 ? formattedStringBuilder.codePointAt(i) : formattedStringBuilder.codePointBefore(i))) {
            return 0;
        }
        return formattedStringBuilder.insert(i, getInsertString(decimalFormatSymbols, b), (Format.Field) null);
    }

    private static UnicodeSet getUnicodeSet(DecimalFormatSymbols decimalFormatSymbols, short s, byte b) {
        boolean z = false;
        int i = s == 0 ? 0 : 1;
        if (b == 1) {
            z = true;
        }
        String patternForCurrencySpacing = decimalFormatSymbols.getPatternForCurrencySpacing(i, z);
        if (patternForCurrencySpacing.equals("[:digit:]")) {
            return UNISET_DIGIT;
        }
        if (patternForCurrencySpacing.equals("[:^S:]")) {
            return UNISET_NOTS;
        }
        return new UnicodeSet(patternForCurrencySpacing);
    }

    private static String getInsertString(DecimalFormatSymbols decimalFormatSymbols, byte b) {
        boolean z = true;
        if (b != 1) {
            z = false;
        }
        return decimalFormatSymbols.getPatternForCurrencySpacing(2, z);
    }
}
