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
import org.xmlpull.v1.XmlPullParser;

class NumberFormatServiceShim extends NumberFormatShim {
    private static ICULocaleService service;

    private static final class NFFactory extends LocaleKeyFactory {
        private NumberFormatFactory delegate;

        NFFactory(NumberFormatFactory delegate) {
            super(delegate.visible());
            this.delegate = delegate;
        }

        public Object create(Key key, ICUService srvc) {
            if (!handlesKey(key) || !(key instanceof LocaleKey)) {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.NumberFormatServiceShim.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.NumberFormatServiceShim.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormatServiceShim.<clinit>():void");
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
            throw new MissingResourceException("Unable to construct NumberFormat", XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE);
        }
        ULocale uloc;
        fmt = (NumberFormat) fmt.clone();
        if (!(choice == 1 || choice == 5)) {
            if (choice == 6) {
            }
            uloc = actualLoc[0];
            fmt.setLocale(uloc, uloc);
            return fmt;
        }
        fmt.setCurrency(Currency.getInstance(desiredLocale));
        uloc = actualLoc[0];
        fmt.setLocale(uloc, uloc);
        return fmt;
    }
}
