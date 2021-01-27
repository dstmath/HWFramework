package ohos.global.icu.number;

import java.util.Locale;
import ohos.global.icu.util.ULocale;

public abstract class NumberRangeFormatter {
    private static final UnlocalizedNumberRangeFormatter BASE = new UnlocalizedNumberRangeFormatter();

    public enum RangeCollapse {
        AUTO,
        NONE,
        UNIT,
        ALL
    }

    public enum RangeIdentityFallback {
        SINGLE_VALUE,
        APPROXIMATELY_OR_SINGLE_VALUE,
        APPROXIMATELY,
        RANGE
    }

    public enum RangeIdentityResult {
        EQUAL_BEFORE_ROUNDING,
        EQUAL_AFTER_ROUNDING,
        NOT_EQUAL
    }

    public static UnlocalizedNumberRangeFormatter with() {
        return BASE;
    }

    public static LocalizedNumberRangeFormatter withLocale(Locale locale) {
        return BASE.locale(locale);
    }

    public static LocalizedNumberRangeFormatter withLocale(ULocale uLocale) {
        return BASE.locale(uLocale);
    }

    private NumberRangeFormatter() {
    }
}
