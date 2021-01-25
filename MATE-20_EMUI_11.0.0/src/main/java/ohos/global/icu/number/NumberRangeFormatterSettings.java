package ohos.global.icu.number;

import ohos.global.icu.impl.number.range.RangeMacroProps;
import ohos.global.icu.number.NumberRangeFormatter;
import ohos.global.icu.number.NumberRangeFormatterSettings;
import ohos.global.icu.util.ULocale;

public abstract class NumberRangeFormatterSettings<T extends NumberRangeFormatterSettings<?>> {
    static final int KEY_COLLAPSE = 5;
    static final int KEY_FORMATTER_1 = 2;
    static final int KEY_FORMATTER_2 = 3;
    static final int KEY_IDENTITY_FALLBACK = 6;
    static final int KEY_LOCALE = 1;
    static final int KEY_MACROS = 0;
    static final int KEY_MAX = 7;
    static final int KEY_SAME_FORMATTERS = 4;
    private final int key;
    private final NumberRangeFormatterSettings<?> parent;
    private volatile RangeMacroProps resolvedMacros;
    private final Object value;

    /* access modifiers changed from: package-private */
    public abstract T create(int i, Object obj);

    NumberRangeFormatterSettings(NumberRangeFormatterSettings<?> numberRangeFormatterSettings, int i, Object obj) {
        this.parent = numberRangeFormatterSettings;
        this.key = i;
        this.value = obj;
    }

    public T numberFormatterBoth(UnlocalizedNumberFormatter unlocalizedNumberFormatter) {
        return (T) create(4, true).create(2, unlocalizedNumberFormatter);
    }

    public T numberFormatterFirst(UnlocalizedNumberFormatter unlocalizedNumberFormatter) {
        return (T) create(4, false).create(2, unlocalizedNumberFormatter);
    }

    public T numberFormatterSecond(UnlocalizedNumberFormatter unlocalizedNumberFormatter) {
        return (T) create(4, false).create(3, unlocalizedNumberFormatter);
    }

    public T collapse(NumberRangeFormatter.RangeCollapse rangeCollapse) {
        return create(5, rangeCollapse);
    }

    public T identityFallback(NumberRangeFormatter.RangeIdentityFallback rangeIdentityFallback) {
        return create(6, rangeIdentityFallback);
    }

    /* access modifiers changed from: package-private */
    public RangeMacroProps resolve() {
        if (this.resolvedMacros != null) {
            return this.resolvedMacros;
        }
        RangeMacroProps rangeMacroProps = new RangeMacroProps();
        for (NumberRangeFormatterSettings numberRangeFormatterSettings = this; numberRangeFormatterSettings != null; numberRangeFormatterSettings = numberRangeFormatterSettings.parent) {
            switch (numberRangeFormatterSettings.key) {
                case 0:
                    break;
                case 1:
                    if (rangeMacroProps.loc == null) {
                        rangeMacroProps.loc = (ULocale) numberRangeFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 2:
                    if (rangeMacroProps.formatter1 == null) {
                        rangeMacroProps.formatter1 = (UnlocalizedNumberFormatter) numberRangeFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 3:
                    if (rangeMacroProps.formatter2 == null) {
                        rangeMacroProps.formatter2 = (UnlocalizedNumberFormatter) numberRangeFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 4:
                    if (rangeMacroProps.sameFormatters == -1) {
                        rangeMacroProps.sameFormatters = ((Boolean) numberRangeFormatterSettings.value).booleanValue() ? 1 : 0;
                        break;
                    } else {
                        break;
                    }
                case 5:
                    if (rangeMacroProps.collapse == null) {
                        rangeMacroProps.collapse = (NumberRangeFormatter.RangeCollapse) numberRangeFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 6:
                    if (rangeMacroProps.identityFallback == null) {
                        rangeMacroProps.identityFallback = (NumberRangeFormatter.RangeIdentityFallback) numberRangeFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                default:
                    throw new AssertionError("Unknown key: " + numberRangeFormatterSettings.key);
            }
        }
        if (rangeMacroProps.formatter1 != null) {
            rangeMacroProps.formatter1.resolve().loc = rangeMacroProps.loc;
        }
        if (rangeMacroProps.formatter2 != null) {
            rangeMacroProps.formatter2.resolve().loc = rangeMacroProps.loc;
        }
        this.resolvedMacros = rangeMacroProps;
        return rangeMacroProps;
    }

    public int hashCode() {
        return resolve().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof NumberRangeFormatterSettings)) {
            return resolve().equals(((NumberRangeFormatterSettings) obj).resolve());
        }
        return false;
    }
}
