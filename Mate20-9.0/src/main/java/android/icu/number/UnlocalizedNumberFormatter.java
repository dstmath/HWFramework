package android.icu.number;

import android.icu.util.ULocale;
import java.util.Locale;

public class UnlocalizedNumberFormatter extends NumberFormatterSettings<UnlocalizedNumberFormatter> {
    UnlocalizedNumberFormatter() {
        super(null, 12, new Long(3));
    }

    UnlocalizedNumberFormatter(NumberFormatterSettings<?> parent, int key, Object value) {
        super(parent, key, value);
    }

    public LocalizedNumberFormatter locale(Locale locale) {
        return new LocalizedNumberFormatter(this, 1, ULocale.forLocale(locale));
    }

    public LocalizedNumberFormatter locale(ULocale locale) {
        return new LocalizedNumberFormatter(this, 1, locale);
    }

    /* access modifiers changed from: package-private */
    public UnlocalizedNumberFormatter create(int key, Object value) {
        return new UnlocalizedNumberFormatter(this, key, value);
    }
}
