package android.icu.util;

import android.icu.impl.ICULocaleService;
import android.icu.impl.ICULocaleService.ICUResourceBundleFactory;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import java.util.Locale;

final class CurrencyServiceShim extends ServiceShim {
    static final ICULocaleService service = null;

    private static class CFService extends ICULocaleService {
        CFService() {
            super("Currency");
            registerFactory(new ICUResourceBundleFactory() {
                protected Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                    return Currency.createCurrency(loc);
                }
            });
            markDefault();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.CurrencyServiceShim.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.CurrencyServiceShim.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.CurrencyServiceShim.<clinit>():void");
    }

    CurrencyServiceShim() {
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

    Currency createInstance(ULocale loc) {
        if (service.isDefault()) {
            return Currency.createCurrency(loc);
        }
        return (Currency) service.get(loc);
    }

    Object registerInstance(Currency currency, ULocale locale) {
        return service.registerObject(currency, locale);
    }

    boolean unregister(Object registryKey) {
        return service.unregisterFactory((Factory) registryKey);
    }
}
