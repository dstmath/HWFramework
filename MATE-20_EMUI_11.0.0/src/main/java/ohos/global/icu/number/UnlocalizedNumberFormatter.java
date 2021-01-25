package ohos.global.icu.number;

import java.util.Locale;
import ohos.global.icu.util.ULocale;

public class UnlocalizedNumberFormatter extends NumberFormatterSettings<UnlocalizedNumberFormatter> {
    UnlocalizedNumberFormatter() {
        super(null, 14, new Long(3));
    }

    UnlocalizedNumberFormatter(NumberFormatterSettings<?> numberFormatterSettings, int i, Object obj) {
        super(numberFormatterSettings, i, obj);
    }

    public LocalizedNumberFormatter locale(Locale locale) {
        return new LocalizedNumberFormatter(this, 1, ULocale.forLocale(locale));
    }

    public LocalizedNumberFormatter locale(ULocale uLocale) {
        return new LocalizedNumberFormatter(this, 1, uLocale);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.number.NumberFormatterSettings
    public UnlocalizedNumberFormatter create(int i, Object obj) {
        return new UnlocalizedNumberFormatter(this, i, obj);
    }
}
