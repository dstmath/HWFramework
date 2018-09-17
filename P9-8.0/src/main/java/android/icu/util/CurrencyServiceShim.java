package android.icu.util;

import android.icu.impl.ICULocaleService;
import android.icu.impl.ICULocaleService.ICUResourceBundleFactory;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import java.util.Locale;

final class CurrencyServiceShim extends ServiceShim {
    static final ICULocaleService service = new CFService();

    private static class CFService extends ICULocaleService {
        CFService() {
            super("Currency");
            registerFactory(new ICUResourceBundleFactory() {
                protected Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                    return Currency.createCurrency(loc);
                }
            });
            markDefault();
        }
    }

    CurrencyServiceShim() {
    }

    Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return service.getAvailableLocales();
    }

    ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return service.getAvailableULocales();
    }

    Currency createInstance(ULocale loc) {
        if (service.isDefault()) {
            return Currency.createCurrency(loc);
        }
        return (Currency) service.get(loc);
    }

    Object registerInstance(Currency currency, ULocale locale) {
        return service.registerObject(currency, locale);
    }

    boolean unregister(Object registryKey) {
        return service.unregisterFactory((Factory) registryKey);
    }
}
