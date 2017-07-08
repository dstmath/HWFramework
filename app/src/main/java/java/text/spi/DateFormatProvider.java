package java.text.spi;

import java.text.DateFormat;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public abstract class DateFormatProvider extends LocaleServiceProvider {
    public abstract DateFormat getDateInstance(int i, Locale locale);

    public abstract DateFormat getDateTimeInstance(int i, int i2, Locale locale);

    public abstract DateFormat getTimeInstance(int i, Locale locale);

    protected DateFormatProvider() {
    }
}
