package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICULocaleService;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.impl.coll.CollationLoader;
import android.icu.text.Collator;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

final class CollatorServiceShim extends Collator.ServiceShim {
    private static ICULocaleService service = new CService();

    private static class CService extends ICULocaleService {
        CService() {
            super("Collator");
            registerFactory(new ICULocaleService.ICUResourceBundleFactory() {
                /* access modifiers changed from: protected */
                public Object handleCreate(ULocale uloc, int kind, ICUService srvc) {
                    return CollatorServiceShim.makeInstance(uloc);
                }
            });
            markDefault();
        }

        public String validateFallbackLocale() {
            return "";
        }

        /* access modifiers changed from: protected */
        public Object handleDefault(ICUService.Key key, String[] actualIDReturn) {
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

    /* access modifiers changed from: package-private */
    public Collator getInstance(ULocale locale) {
        try {
            Collator coll = (Collator) service.get(locale, new ULocale[1]);
            if (coll != null) {
                return (Collator) coll.clone();
            }
            throw new MissingResourceException("Could not locate Collator data", "", "");
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException((Throwable) e);
        }
    }

    /* access modifiers changed from: package-private */
    public Object registerInstance(Collator collator, ULocale locale) {
        collator.setLocale(locale, locale);
        return service.registerObject(collator, locale);
    }

    /* access modifiers changed from: package-private */
    public Object registerFactory(Collator.CollatorFactory f) {
        return service.registerFactory(new ICULocaleService.LocaleKeyFactory(f) {
            Collator.CollatorFactory delegate;

            {
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
        });
    }

    /* access modifiers changed from: package-private */
    public boolean unregister(Object registryKey) {
        return service.unregisterFactory((ICUService.Factory) registryKey);
    }

    /* access modifiers changed from: package-private */
    public Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales(ICUData.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return service.getAvailableLocales();
    }

    /* access modifiers changed from: package-private */
    public ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales(ICUData.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return service.getAvailableULocales();
    }

    /* access modifiers changed from: package-private */
    public String getDisplayName(ULocale objectLocale, ULocale displayLocale) {
        return service.getDisplayName(objectLocale.getName(), displayLocale);
    }

    /* access modifiers changed from: private */
    public static final Collator makeInstance(ULocale desiredLocale) {
        Output<ULocale> validLocale = new Output<>(ULocale.ROOT);
        return new RuleBasedCollator(CollationLoader.loadTailoring(desiredLocale, validLocale), (ULocale) validLocale.value);
    }
}
