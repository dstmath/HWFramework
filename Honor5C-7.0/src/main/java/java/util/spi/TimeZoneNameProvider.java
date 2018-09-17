package java.util.spi;

import java.util.Locale;

public abstract class TimeZoneNameProvider extends LocaleServiceProvider {
    public abstract String getDisplayName(String str, boolean z, int i, Locale locale);

    protected TimeZoneNameProvider() {
    }
}
