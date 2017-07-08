package android.icu.text;

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
import org.xmlpull.v1.XmlPullParser;

final class CollatorServiceShim extends ServiceShim {
    private static ICULocaleService service;

    /* renamed from: android.icu.text.CollatorServiceShim.1CFactory */
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
                    String str = ICUResourceBundle.ICU_COLLATION_BASE_NAME;
                }

                protected Object handleCreate(ULocale uloc, int kind, ICUService srvc) {
                    return CollatorServiceShim.makeInstance(uloc);
                }
            });
            markDefault();
        }

        public String validateFallbackLocale() {
            return XmlPullParser.NO_NAMESPACE;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CollatorServiceShim.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CollatorServiceShim.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CollatorServiceShim.<clinit>():void");
    }

    CollatorServiceShim() {
    }

    Collator getInstance(ULocale locale) {
        try {
            Collator coll = (Collator) service.get(locale, new ULocale[1]);
            if (coll != null) {
                return (Collator) coll.clone();
            }
            throw new MissingResourceException("Could not locate Collator data", XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE);
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
            return ICUResourceBundle.getAvailableLocales(ICUResourceBundle.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return service.getAvailableLocales();
    }

    ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales(ICUResourceBundle.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
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
