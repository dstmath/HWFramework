package android.icu.util;

import android.icu.impl.ICULocaleService;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.util.Currency;
import java.util.Locale;

final class CurrencyServiceShim extends Currency.ServiceShim {
    static final ICULocaleService service = new CFService();

    private static class CFService extends ICULocaleService {
        CFService() {
            super("Currency");
            registerFactory(new ICULocaleService.ICUResourceBundleFactory() {
                /* access modifiers changed from: protected */
                public Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                    return Currency.createCurrency(loc);
                }
            });
            markDefault();
        }
    }

    CurrencyServiceShim() {
    }

    /* access modifiers changed from: package-private */
    public Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return service.getAvailableLocales();
    }

    /* access modifiers changed from: package-private */
    public ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return service.getAvailableULocales();
    }

    /* access modifiers changed from: package-private */
    public Currency createInstance(ULocale loc) {
        if (service.isDefault()) {
            return Currency.createCurrency(loc);
        }
        return (Currency) service.get(loc);
    }

    /* access modifiers changed from: package-private */
    public Object registerInstance(Currency currency, ULocale locale) {
        return service.registerObject(currency, locale);
    }

    /* access modifiers changed from: package-private */
    public boolean unregister(Object registryKey) {
        return service.unregisterFactory((ICUService.Factory) registryKey);
    }
}
