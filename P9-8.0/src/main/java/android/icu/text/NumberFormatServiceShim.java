package android.icu.text;

import android.icu.impl.ICULocaleService;
import android.icu.impl.ICULocaleService.ICUResourceBundleFactory;
import android.icu.impl.ICULocaleService.LocaleKey;
import android.icu.impl.ICULocaleService.LocaleKeyFactory;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import android.icu.impl.ICUService.Key;
import android.icu.text.NumberFormat.NumberFormatFactory;
import android.icu.util.Currency;
import android.icu.util.ULocale;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

class NumberFormatServiceShim extends NumberFormatShim {
    private static ICULocaleService service = new NFService();

    private static final class NFFactory extends LocaleKeyFactory {
        private NumberFormatFactory delegate;

        NFFactory(NumberFormatFactory delegate) {
            super(delegate.visible());
            this.delegate = delegate;
        }

        public Object create(Key key, ICUService srvc) {
            if (!handlesKey(key) || ((key instanceof LocaleKey) ^ 1) != 0) {
                return null;
            }
            LocaleKey lkey = (LocaleKey) key;
            Object result = this.delegate.createFormat(lkey.canonicalLocale(), lkey.kind());
            if (result == null) {
                result = srvc.getKey(key, null, this);
            }
            return result;
        }

        protected Set<String> getSupportedIDs() {
            return this.delegate.getSupportedLocaleNames();
        }
    }

    private static class NFService extends ICULocaleService {
        NFService() {
            super("NumberFormat");
            registerFactory(new ICUResourceBundleFactory() {
                protected Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                    return NumberFormat.createInstance(loc, kind);
                }
            });
            markDefault();
        }
    }

    NumberFormatServiceShim() {
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

    Object registerFactory(NumberFormatFactory factory) {
        return service.registerFactory(new NFFactory(factory));
    }

    boolean unregister(Object registryKey) {
        return service.unregisterFactory((Factory) registryKey);
    }

    NumberFormat createInstance(ULocale desiredLocale, int choice) {
        ULocale[] actualLoc = new ULocale[1];
        NumberFormat fmt = (NumberFormat) service.get(desiredLocale, choice, actualLoc);
        if (fmt == null) {
            throw new MissingResourceException("Unable to construct NumberFormat", "", "");
        }
        fmt = (NumberFormat) fmt.clone();
        if (choice == 1 || choice == 5 || choice == 6) {
            fmt.setCurrency(Currency.getInstance(desiredLocale));
        }
        ULocale uloc = actualLoc[0];
        fmt.setLocale(uloc, uloc);
        return fmt;
    }
}
