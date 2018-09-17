package java.util.spi;

import java.util.Locale;

public abstract class LocaleNameProvider extends LocaleServiceProvider {
    public abstract String getDisplayCountry(String str, Locale locale);

    public abstract String getDisplayLanguage(String str, Locale locale);

    public abstract String getDisplayVariant(String str, Locale locale);

    protected LocaleNameProvider() {
    }

    public String getDisplayScript(String scriptCode, Locale locale) {
        return null;
    }
}
