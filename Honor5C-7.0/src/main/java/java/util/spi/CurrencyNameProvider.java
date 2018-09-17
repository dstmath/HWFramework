package java.util.spi;

import java.util.Arrays;
import java.util.Locale;

public abstract class CurrencyNameProvider extends LocaleServiceProvider {
    public abstract String getSymbol(String str, Locale locale);

    protected CurrencyNameProvider() {
    }

    public String getDisplayName(String currencyCode, Locale locale) {
        if (currencyCode == null || locale == null) {
            throw new NullPointerException();
        }
        char[] charray = currencyCode.toCharArray();
        if (charray.length != 3) {
            throw new IllegalArgumentException("The currencyCode is not in the form of three upper-case letters.");
        }
        for (char c : charray) {
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("The currencyCode is not in the form of three upper-case letters.");
            }
        }
        if (Arrays.asList(getAvailableLocales()).contains(locale)) {
            return null;
        }
        throw new IllegalArgumentException("The locale is not available");
    }
}
