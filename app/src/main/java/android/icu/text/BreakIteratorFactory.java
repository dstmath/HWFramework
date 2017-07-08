package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.ICUBinary;
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
import org.xmlpull.v1.XmlPullParser;

final class BreakIteratorFactory extends BreakIteratorServiceShim {
    private static final String[] KIND_NAMES = null;
    static final ICULocaleService service = null;

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
            return XmlPullParser.NO_NAMESPACE;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.BreakIteratorFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.BreakIteratorFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.BreakIteratorFactory.<clinit>():void");
    }

    BreakIteratorFactory() {
    }

    public Object registerInstance(BreakIterator iter, ULocale locale, int kind) {
        iter.setText(new StringCharacterIterator(XmlPullParser.NO_NAMESPACE));
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
        BreakIterator iter = null;
        ICUResourceBundle rb = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BRKITR_BASE_NAME, locale, OpenType.LOCALE_ROOT);
        String typeKeyExt = null;
        if (kind == 2) {
            String lbKeyValue = locale.getKeywordValue("lb");
            if (lbKeyValue != null && (lbKeyValue.equals("strict") || lbKeyValue.equals("normal") || lbKeyValue.equals("loose"))) {
                typeKeyExt = BaseLocale.SEP + lbKeyValue;
            }
        }
        if (typeKeyExt == null) {
            try {
                String typeKey = KIND_NAMES[kind];
            } catch (Exception e) {
                throw new MissingResourceException(e.toString(), XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE);
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
        return iter;
    }
}
