package ohos.global.icu.text;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import ohos.global.icu.impl.ICULocaleService;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.ICUService;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.ULocale;

class NumberFormatServiceShim extends NumberFormat.NumberFormatShim {
    private static ICULocaleService service = new NFService();

    NumberFormatServiceShim() {
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NumberFormat.NumberFormatShim
    public Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return service.getAvailableLocales();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NumberFormat.NumberFormatShim
    public ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return service.getAvailableULocales();
    }

    private static final class NFFactory extends ICULocaleService.LocaleKeyFactory {
        private NumberFormat.NumberFormatFactory delegate;

        NFFactory(NumberFormat.NumberFormatFactory numberFormatFactory) {
            super(numberFormatFactory.visible());
            this.delegate = numberFormatFactory;
        }

        @Override // ohos.global.icu.impl.ICULocaleService.LocaleKeyFactory, ohos.global.icu.impl.ICUService.Factory
        public Object create(ICUService.Key key, ICUService iCUService) {
            if (!handlesKey(key) || !(key instanceof ICULocaleService.LocaleKey)) {
                return null;
            }
            ICULocaleService.LocaleKey localeKey = (ICULocaleService.LocaleKey) key;
            NumberFormat createFormat = this.delegate.createFormat(localeKey.canonicalLocale(), localeKey.kind());
            return createFormat == null ? iCUService.getKey(key, null, this) : createFormat;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.impl.ICULocaleService.LocaleKeyFactory
        public Set<String> getSupportedIDs() {
            return this.delegate.getSupportedLocaleNames();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NumberFormat.NumberFormatShim
    public Object registerFactory(NumberFormat.NumberFormatFactory numberFormatFactory) {
        return service.registerFactory(new NFFactory(numberFormatFactory));
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NumberFormat.NumberFormatShim
    public boolean unregister(Object obj) {
        return service.unregisterFactory((ICUService.Factory) obj);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NumberFormat.NumberFormatShim
    public NumberFormat createInstance(ULocale uLocale, int i) {
        ULocale[] uLocaleArr = new ULocale[1];
        NumberFormat numberFormat = (NumberFormat) service.get(uLocale, i, uLocaleArr);
        if (numberFormat != null) {
            NumberFormat numberFormat2 = (NumberFormat) numberFormat.clone();
            if (i == 1 || i == 5 || i == 6 || i == 7 || i == 8 || i == 9) {
                numberFormat2.setCurrency(Currency.getInstance(uLocale));
            }
            ULocale uLocale2 = uLocaleArr[0];
            numberFormat2.setLocale(uLocale2, uLocale2);
            return numberFormat2;
        }
        throw new MissingResourceException("Unable to construct NumberFormat", "", "");
    }

    private static class NFService extends ICULocaleService {
        NFService() {
            super("NumberFormat");
            registerFactory(new ICULocaleService.ICUResourceBundleFactory() {
                /* class ohos.global.icu.text.NumberFormatServiceShim.NFService.AnonymousClass1RBNumberFormatFactory */

                /* access modifiers changed from: protected */
                @Override // ohos.global.icu.impl.ICULocaleService.ICUResourceBundleFactory, ohos.global.icu.impl.ICULocaleService.LocaleKeyFactory
                public Object handleCreate(ULocale uLocale, int i, ICUService iCUService) {
                    return NumberFormat.createInstance(uLocale, i);
                }
            });
            markDefault();
        }
    }
}
