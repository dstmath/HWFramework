package ohos.global.icu.number;

import java.util.Locale;
import ohos.global.icu.impl.number.DecimalFormatProperties;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.util.ULocale;

public final class NumberFormatter {
    private static final UnlocalizedNumberFormatter BASE = new UnlocalizedNumberFormatter();
    static final long DEFAULT_THRESHOLD = 3;

    public enum DecimalSeparatorDisplay {
        AUTO,
        ALWAYS
    }

    public enum GroupingStrategy {
        OFF,
        MIN2,
        AUTO,
        ON_ALIGNED,
        THOUSANDS
    }

    public enum SignDisplay {
        AUTO,
        ALWAYS,
        NEVER,
        ACCOUNTING,
        ACCOUNTING_ALWAYS,
        EXCEPT_ZERO,
        ACCOUNTING_EXCEPT_ZERO
    }

    public enum UnitWidth {
        NARROW,
        SHORT,
        FULL_NAME,
        ISO_CODE,
        HIDDEN
    }

    private NumberFormatter() {
    }

    public static UnlocalizedNumberFormatter with() {
        return BASE;
    }

    public static LocalizedNumberFormatter withLocale(Locale locale) {
        return BASE.locale(locale);
    }

    public static LocalizedNumberFormatter withLocale(ULocale uLocale) {
        return BASE.locale(uLocale);
    }

    public static UnlocalizedNumberFormatter forSkeleton(String str) {
        return NumberSkeletonImpl.getOrCreate(str);
    }

    @Deprecated
    public static UnlocalizedNumberFormatter fromDecimalFormat(DecimalFormatProperties decimalFormatProperties, DecimalFormatSymbols decimalFormatSymbols, DecimalFormatProperties decimalFormatProperties2) {
        return NumberPropertyMapper.create(decimalFormatProperties, decimalFormatSymbols, decimalFormatProperties2);
    }
}
