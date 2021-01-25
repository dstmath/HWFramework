package ohos.global.icu.text;

import java.io.IOException;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import ohos.agp.styles.attributes.ToolbarAttrsConstants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICULocaleService;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.ICUService;
import ohos.global.icu.text.BreakIterator;
import ohos.global.icu.util.ULocale;

final class BreakIteratorFactory extends BreakIterator.BreakIteratorServiceShim {
    private static final String[] KIND_NAMES = {"grapheme", "word", "line", "sentence", ToolbarAttrsConstants.TITLE};
    static final ICULocaleService service = new BFService();

    BreakIteratorFactory() {
    }

    @Override // ohos.global.icu.text.BreakIterator.BreakIteratorServiceShim
    public Object registerInstance(BreakIterator breakIterator, ULocale uLocale, int i) {
        breakIterator.setText(new StringCharacterIterator(""));
        return service.registerObject(breakIterator, uLocale, i);
    }

    @Override // ohos.global.icu.text.BreakIterator.BreakIteratorServiceShim
    public boolean unregister(Object obj) {
        if (service.isDefault()) {
            return false;
        }
        return service.unregisterFactory((ICUService.Factory) obj);
    }

    @Override // ohos.global.icu.text.BreakIterator.BreakIteratorServiceShim
    public Locale[] getAvailableLocales() {
        ICULocaleService iCULocaleService = service;
        if (iCULocaleService == null) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return iCULocaleService.getAvailableLocales();
    }

    @Override // ohos.global.icu.text.BreakIterator.BreakIteratorServiceShim
    public ULocale[] getAvailableULocales() {
        ICULocaleService iCULocaleService = service;
        if (iCULocaleService == null) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return iCULocaleService.getAvailableULocales();
    }

    @Override // ohos.global.icu.text.BreakIterator.BreakIteratorServiceShim
    public BreakIterator createBreakIterator(ULocale uLocale, int i) {
        if (service.isDefault()) {
            return createBreakInstance(uLocale, i);
        }
        ULocale[] uLocaleArr = new ULocale[1];
        BreakIterator breakIterator = (BreakIterator) service.get(uLocale, i, uLocaleArr);
        breakIterator.setLocale(uLocaleArr[0], uLocaleArr[0]);
        return breakIterator;
    }

    private static class BFService extends ICULocaleService {
        @Override // ohos.global.icu.impl.ICULocaleService
        public String validateFallbackLocale() {
            return "";
        }

        BFService() {
            super("BreakIterator");
            registerFactory(new ICULocaleService.ICUResourceBundleFactory() {
                /* class ohos.global.icu.text.BreakIteratorFactory.BFService.AnonymousClass1RBBreakIteratorFactory */

                /* access modifiers changed from: protected */
                @Override // ohos.global.icu.impl.ICULocaleService.ICUResourceBundleFactory, ohos.global.icu.impl.ICULocaleService.LocaleKeyFactory
                public Object handleCreate(ULocale uLocale, int i, ICUService iCUService) {
                    return BreakIteratorFactory.createBreakInstance(uLocale, i);
                }
            });
            markDefault();
        }
    }

    /* access modifiers changed from: private */
    public static BreakIterator createBreakInstance(ULocale uLocale, int i) {
        String str;
        String str2;
        String keywordValue;
        String keywordValue2;
        ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(ICUData.ICU_BRKITR_BASE_NAME, uLocale, ICUResourceBundle.OpenType.LOCALE_ROOT);
        RuleBasedBreakIterator ruleBasedBreakIterator = null;
        if (i != 2 || (keywordValue2 = uLocale.getKeywordValue("lb")) == null || (!keywordValue2.equals(SchemaSymbols.ATTVAL_STRICT) && !keywordValue2.equals("normal") && !keywordValue2.equals("loose"))) {
            str = null;
        } else {
            str = "_" + keywordValue2;
        }
        if (str == null) {
            try {
                str2 = KIND_NAMES[i];
            } catch (Exception e) {
                throw new MissingResourceException(e.toString(), "", "");
            }
        } else {
            str2 = KIND_NAMES[i] + str;
        }
        try {
            ruleBasedBreakIterator = RuleBasedBreakIterator.getInstanceFromCompiledRules(ICUBinary.getData("brkitr/" + bundleInstance.getStringWithFallback("boundaries/" + str2)));
        } catch (IOException e2) {
            Assert.fail(e2);
        }
        ULocale forLocale = ULocale.forLocale(bundleInstance.getLocale());
        ruleBasedBreakIterator.setLocale(forLocale, forLocale);
        return (i != 3 || (keywordValue = uLocale.getKeywordValue("ss")) == null || !keywordValue.equals("standard")) ? ruleBasedBreakIterator : FilteredBreakIteratorBuilder.getInstance(new ULocale(uLocale.getBaseName())).wrapIteratorWithFilter(ruleBasedBreakIterator);
    }
}
