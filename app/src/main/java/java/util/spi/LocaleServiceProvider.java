package java.util.spi;

import java.util.Locale;

public abstract class LocaleServiceProvider {
    public abstract Locale[] getAvailableLocales();

    protected LocaleServiceProvider() {
    }
}
