package ohos.global.icu.util;

import java.util.Locale;
import ohos.global.icu.impl.ICULocaleService;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.ICUService;
import ohos.global.icu.util.Currency;

final class CurrencyServiceShim extends Currency.ServiceShim {
    static final ICULocaleService service = new CFService();

    CurrencyServiceShim() {
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.util.Currency.ServiceShim
    public Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return service.getAvailableLocales();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.util.Currency.ServiceShim
    public ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return service.getAvailableULocales();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.util.Currency.ServiceShim
    public Currency createInstance(ULocale uLocale) {
        if (service.isDefault()) {
            return Currency.createCurrency(uLocale);
        }
        return (Currency) service.get(uLocale);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.util.Currency.ServiceShim
    public Object registerInstance(Currency currency, ULocale uLocale) {
        return service.registerObject(currency, uLocale);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.util.Currency.ServiceShim
    public boolean unregister(Object obj) {
        return service.unregisterFactory((ICUService.Factory) obj);
    }

    private static class CFService extends ICULocaleService {
        CFService() {
            super("Currency");
            registerFactory(new ICULocaleService.ICUResourceBundleFactory() {
                /* class ohos.global.icu.util.CurrencyServiceShim.CFService.AnonymousClass1CurrencyFactory */

                /* access modifiers changed from: protected */
                public Object handleCreate(ULocale uLocale, int i, ICUService iCUService) {
                    return Currency.createCurrency(uLocale);
                }
            });
            markDefault();
        }
    }
}
