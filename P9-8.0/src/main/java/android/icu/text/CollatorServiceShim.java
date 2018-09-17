package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICULocaleService;
import android.icu.impl.ICULocaleService.ICUResourceBundleFactory;
import android.icu.impl.ICULocaleService.LocaleKeyFactory;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import android.icu.impl.ICUService.Key;
import android.icu.impl.coll.CollationLoader;
import android.icu.text.Collator.CollatorFactory;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

final class CollatorServiceShim extends ServiceShim {
    private static ICULocaleService service = new CService();

    /* renamed from: android.icu.text.CollatorServiceShim$1CFactory */
    class AnonymousClass1CFactory extends LocaleKeyFactory {
        CollatorFactory delegate;

        AnonymousClass1CFactory(CollatorFactory fctry) {
            super(fctry.visible());
            this.delegate = fctry;
        }

        public Object handleCreate(ULocale loc, int kind, ICUService srvc) {
            return this.delegate.createCollator(loc);
        }

        public String getDisplayName(String id, ULocale displayLocale) {
            return this.delegate.getDisplayName(new ULocale(id), displayLocale);
        }

        public Set<String> getSupportedIDs() {
            return this.delegate.getSupportedLocaleIDs();
        }
    }

    private static class CService extends ICULocaleService {
        CService() {
            super("Collator");
            registerFactory(new ICUResourceBundleFactory() {
                {
                    String str = ICUData.ICU_COLLATION_BASE_NAME;
                }

                protected Object handleCreate(ULocale uloc, int kind, ICUService srvc) {
                    return CollatorServiceShim.makeInstance(uloc);
                }
            });
            markDefault();
        }

        public String validateFallbackLocale() {
            return "";
        }

        protected Object handleDefault(Key key, String[] actualIDReturn) {
            if (actualIDReturn != null) {
                actualIDReturn[0] = "root";
            }
            try {
                return CollatorServiceShim.makeInstance(ULocale.ROOT);
            } catch (MissingResourceException e) {
                return null;
            }
        }
    }

    CollatorServiceShim() {
    }

    Collator getInstance(ULocale locale) {
        try {
            Collator coll = (Collator) service.get(locale, new ULocale[1]);
            if (coll != null) {
                return (Collator) coll.clone();
            }
            throw new MissingResourceException("Could not locate Collator data", "", "");
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    Object registerInstance(Collator collator, ULocale locale) {
        collator.setLocale(locale, locale);
        return service.registerObject(collator, locale);
    }

    Object registerFactory(CollatorFactory f) {
        return service.registerFactory(new AnonymousClass1CFactory(f));
    }

    boolean unregister(Object registryKey) {
        return service.unregisterFactory((Factory) registryKey);
    }

    Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales(ICUData.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return service.getAvailableLocales();
    }

    ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales(ICUData.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return service.getAvailableULocales();
    }

    String getDisplayName(ULocale objectLocale, ULocale displayLocale) {
        return service.getDisplayName(objectLocale.getName(), displayLocale);
    }

    private static final Collator makeInstance(ULocale desiredLocale) {
        Output<ULocale> validLocale = new Output(ULocale.ROOT);
        return new RuleBasedCollator(CollationLoader.loadTailoring(desiredLocale, validLocale), (ULocale) validLocale.value);
    }
}
