package ohos.global.icu.number;

import java.util.Locale;
import ohos.global.icu.util.ULocale;

public class UnlocalizedNumberRangeFormatter extends NumberRangeFormatterSettings<UnlocalizedNumberRangeFormatter> {
    UnlocalizedNumberRangeFormatter() {
        super(null, 0, null);
    }

    UnlocalizedNumberRangeFormatter(NumberRangeFormatterSettings<?> numberRangeFormatterSettings, int i, Object obj) {
        super(numberRangeFormatterSettings, i, obj);
    }

    public LocalizedNumberRangeFormatter locale(Locale locale) {
        return new LocalizedNumberRangeFormatter(this, 1, ULocale.forLocale(locale));
    }

    public LocalizedNumberRangeFormatter locale(ULocale uLocale) {
        return new LocalizedNumberRangeFormatter(this, 1, uLocale);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.number.NumberRangeFormatterSettings
    public UnlocalizedNumberRangeFormatter create(int i, Object obj) {
        return new UnlocalizedNumberRangeFormatter(this, i, obj);
    }
}
