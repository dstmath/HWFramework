package ohos.global.icu.text;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import ohos.global.icu.impl.ICULocaleService;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.ICUService;
import ohos.global.icu.impl.coll.CollationLoader;
import ohos.global.icu.text.Collator;
import ohos.global.icu.util.ICUCloneNotSupportedException;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.ULocale;

final class CollatorServiceShim extends Collator.ServiceShim {
    private static ICULocaleService service = new CService();

    CollatorServiceShim() {
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator.ServiceShim
    public Collator getInstance(ULocale uLocale) {
        try {
            Collator collator = (Collator) service.get(uLocale, new ULocale[1]);
            if (collator != null) {
                return (Collator) collator.clone();
            }
            throw new MissingResourceException("Could not locate Collator data", "", "");
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator.ServiceShim
    public Object registerInstance(Collator collator, ULocale uLocale) {
        collator.setLocale(uLocale, uLocale);
        return service.registerObject(collator, uLocale);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator.ServiceShim
    public Object registerFactory(Collator.CollatorFactory collatorFactory) {
        return service.registerFactory(new ICULocaleService.LocaleKeyFactory(collatorFactory) {
            /* class ohos.global.icu.text.CollatorServiceShim.AnonymousClass1CFactory */
            Collator.CollatorFactory delegate;

            {
                this.delegate = r2;
            }

            public Object handleCreate(ULocale uLocale, int i, ICUService iCUService) {
                return this.delegate.createCollator(uLocale);
            }

            public String getDisplayName(String str, ULocale uLocale) {
                return this.delegate.getDisplayName(new ULocale(str), uLocale);
            }

            public Set<String> getSupportedIDs() {
                return this.delegate.getSupportedLocaleIDs();
            }
        });
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator.ServiceShim
    public boolean unregister(Object obj) {
        return service.unregisterFactory((ICUService.Factory) obj);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator.ServiceShim
    public Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales("ohos/global/icu/impl/data/icudt66b/coll", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return service.getAvailableLocales();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator.ServiceShim
    public ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales("ohos/global/icu/impl/data/icudt66b/coll", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return service.getAvailableULocales();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator.ServiceShim
    public String getDisplayName(ULocale uLocale, ULocale uLocale2) {
        return service.getDisplayName(uLocale.getName(), uLocale2);
    }

    private static class CService extends ICULocaleService {
        public String validateFallbackLocale() {
            return "";
        }

        CService() {
            super("Collator");
            registerFactory(new ICULocaleService.ICUResourceBundleFactory() {
                /* class ohos.global.icu.text.CollatorServiceShim.CService.AnonymousClass1CollatorFactory */

                /* access modifiers changed from: protected */
                public Object handleCreate(ULocale uLocale, int i, ICUService iCUService) {
                    return CollatorServiceShim.makeInstance(uLocale);
                }
            });
            markDefault();
        }

        /* access modifiers changed from: protected */
        public Object handleDefault(ICUService.Key key, String[] strArr) {
            if (strArr != null) {
                strArr[0] = "root";
            }
            try {
                return CollatorServiceShim.makeInstance(ULocale.ROOT);
            } catch (MissingResourceException unused) {
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final Collator makeInstance(ULocale uLocale) {
        Output output = new Output(ULocale.ROOT);
        return new RuleBasedCollator(CollationLoader.loadTailoring(uLocale, output), output.value);
    }
}
