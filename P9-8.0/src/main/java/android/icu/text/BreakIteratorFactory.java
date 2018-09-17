package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.ICUBinary;
import android.icu.impl.ICUData;
import android.icu.impl.ICULocaleService;
import android.icu.impl.ICULocaleService.ICUResourceBundleFactory;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceBundle.OpenType;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import android.icu.impl.locale.BaseLocale;
import android.icu.util.ULocale;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.MissingResourceException;

final class BreakIteratorFactory extends BreakIteratorServiceShim {
    private static final String[] KIND_NAMES = new String[]{"grapheme", "word", "line", "sentence", "title"};
    static final ICULocaleService service = new BFService();

    private static class BFService extends ICULocaleService {
        BFService() {
            super("BreakIterator");
            registerFactory(new ICUResourceBundleFactory() {
                protected Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                    return BreakIteratorFactory.createBreakInstance(loc, kind);
                }
            });
            markDefault();
        }

        public String validateFallbackLocale() {
            return "";
        }
    }

    BreakIteratorFactory() {
    }

    public Object registerInstance(BreakIterator iter, ULocale locale, int kind) {
        iter.setText(new StringCharacterIterator(""));
        return service.registerObject((Object) iter, locale, kind);
    }

    public boolean unregister(Object key) {
        if (service.isDefault()) {
            return false;
        }
        return service.unregisterFactory((Factory) key);
    }

    public Locale[] getAvailableLocales() {
        if (service == null) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return service.getAvailableLocales();
    }

    public ULocale[] getAvailableULocales() {
        if (service == null) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return service.getAvailableULocales();
    }

    public BreakIterator createBreakIterator(ULocale locale, int kind) {
        if (service.isDefault()) {
            return createBreakInstance(locale, kind);
        }
        ULocale[] actualLoc = new ULocale[1];
        BreakIterator iter = (BreakIterator) service.get(locale, kind, actualLoc);
        iter.setLocale(actualLoc[0], actualLoc[0]);
        return iter;
    }

    private static BreakIterator createBreakInstance(ULocale locale, int kind) {
        String typeKey;
        RuleBasedBreakIterator iter = null;
        ICUResourceBundle rb = ICUResourceBundle.getBundleInstance(ICUData.ICU_BRKITR_BASE_NAME, locale, OpenType.LOCALE_ROOT);
        String typeKeyExt = null;
        if (kind == 2) {
            String lbKeyValue = locale.getKeywordValue("lb");
            if (lbKeyValue != null && (lbKeyValue.equals("strict") || lbKeyValue.equals("normal") || lbKeyValue.equals("loose"))) {
                typeKeyExt = BaseLocale.SEP + lbKeyValue;
            }
        }
        if (typeKeyExt == null) {
            try {
                typeKey = KIND_NAMES[kind];
            } catch (Exception e) {
                throw new MissingResourceException(e.toString(), "", "");
            }
        }
        typeKey = KIND_NAMES[kind] + typeKeyExt;
        try {
            iter = RuleBasedBreakIterator.getInstanceFromCompiledRules(ICUBinary.getData("brkitr/" + rb.getStringWithFallback("boundaries/" + typeKey)));
        } catch (Exception e2) {
            Assert.fail(e2);
        }
        ULocale uloc = ULocale.forLocale(rb.getLocale());
        iter.setLocale(uloc, uloc);
        iter.setBreakType(kind);
        if (kind == 3) {
            String ssKeyword = locale.getKeywordValue("ss");
            if (ssKeyword != null && ssKeyword.equals("standard")) {
                return FilteredBreakIteratorBuilder.createInstance(new ULocale(locale.getBaseName())).build(iter);
            }
        }
        return iter;
    }
}
