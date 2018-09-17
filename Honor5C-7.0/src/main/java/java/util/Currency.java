package java.util;

import java.io.Serializable;
import libcore.icu.ICU;
import sun.util.locale.BaseLocale;

public final class Currency implements Serializable {
    private static HashSet<Currency> available = null;
    private static HashMap<String, Currency> instances = null;
    private static final long serialVersionUID = -158308464356906721L;
    private final String currencyCode;
    private final transient android.icu.util.Currency icuCurrency;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Currency.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Currency.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.Currency.<clinit>():void");
    }

    private Currency(android.icu.util.Currency icuCurrency) {
        this.icuCurrency = icuCurrency;
        this.currencyCode = icuCurrency.getCurrencyCode();
    }

    public static Currency getInstance(String currencyCode) {
        synchronized (instances) {
            Currency instance = (Currency) instances.get(currencyCode);
            if (instance == null) {
                android.icu.util.Currency icuInstance = android.icu.util.Currency.getInstance(currencyCode);
                if (icuInstance == null) {
                    return null;
                }
                instance = new Currency(icuInstance);
                instances.put(currencyCode, instance);
            }
            return instance;
        }
    }

    public static Currency getInstance(Locale locale) {
        android.icu.util.Currency icuInstance = android.icu.util.Currency.getInstance(locale);
        String variant = locale.getVariant();
        String country = locale.getCountry();
        if (!variant.isEmpty() && (variant.equals("EURO") || variant.equals("HK") || variant.equals("PREEURO"))) {
            country = country + BaseLocale.SEP + variant;
        }
        String currencyCode = ICU.getCurrencyCode(country);
        if (currencyCode == null) {
            throw new IllegalArgumentException("Unsupported ISO 3166 country: " + locale);
        } else if (icuInstance == null || icuInstance.getCurrencyCode().equals("XXX")) {
            return null;
        } else {
            return getInstance(currencyCode);
        }
    }

    public static Set<Currency> getAvailableCurrencies() {
        Set<Currency> set;
        synchronized (Currency.class) {
            if (available == null) {
                Set<android.icu.util.Currency> icuAvailableCurrencies = android.icu.util.Currency.getAvailableCurrencies();
                available = new HashSet();
                for (android.icu.util.Currency icuCurrency : icuAvailableCurrencies) {
                    Currency currency = getInstance(icuCurrency.getCurrencyCode());
                    if (currency == null) {
                        currency = new Currency(icuCurrency);
                        instances.put(currency.currencyCode, currency);
                    }
                    available.add(currency);
                }
            }
            set = (Set) available.clone();
        }
        return set;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public String getSymbol() {
        return this.icuCurrency.getSymbol();
    }

    public String getSymbol(Locale locale) {
        if (locale != null) {
            return this.icuCurrency.getSymbol(locale);
        }
        throw new NullPointerException("locale == null");
    }

    public int getDefaultFractionDigits() {
        if (this.icuCurrency.getCurrencyCode().equals("XXX")) {
            return -1;
        }
        return this.icuCurrency.getDefaultFractionDigits();
    }

    public int getNumericCode() {
        return this.icuCurrency.getNumericCode();
    }

    public String getDisplayName() {
        return this.icuCurrency.getDisplayName();
    }

    public String getDisplayName(Locale locale) {
        return this.icuCurrency.getDisplayName(locale);
    }

    public String toString() {
        return this.icuCurrency.toString();
    }

    private Object readResolve() {
        return getInstance(this.currencyCode);
    }
}
