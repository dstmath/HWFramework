package java.util;

import java.io.Serializable;
import java.util.Locale.Category;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.ICU;
import sun.util.locale.BaseLocale;

public final class Currency implements Serializable {
    private static HashSet<Currency> available = null;
    private static ConcurrentMap<String, Currency> instances = new ConcurrentHashMap(7);
    private static final long serialVersionUID = -158308464356906721L;
    private final String currencyCode;
    private final transient android.icu.util.Currency icuCurrency;

    private Currency(android.icu.util.Currency icuCurrency) {
        this.icuCurrency = icuCurrency;
        this.currencyCode = icuCurrency.getCurrencyCode();
    }

    public static Currency getInstance(String currencyCode) {
        Currency instance = (Currency) instances.get(currencyCode);
        if (instance != null) {
            return instance;
        }
        android.icu.util.Currency icuInstance = android.icu.util.Currency.getInstance(currencyCode);
        if (icuInstance == null) {
            return null;
        }
        Currency currencyVal = new Currency(icuInstance);
        instance = (Currency) instances.putIfAbsent(currencyCode, currencyVal);
        if (instance == null) {
            instance = currencyVal;
        }
        return instance;
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
        }
        return (Set) available.clone();
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public String getSymbol() {
        return getSymbol(Locale.getDefault(Category.DISPLAY));
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
        return getDisplayName(Locale.getDefault(Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        return this.icuCurrency.getDisplayName((Locale) Objects.requireNonNull(locale));
    }

    public String toString() {
        return this.icuCurrency.toString();
    }

    private Object readResolve() {
        return getInstance(this.currencyCode);
    }
}
