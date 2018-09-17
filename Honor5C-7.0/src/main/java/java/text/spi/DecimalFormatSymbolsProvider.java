package java.text.spi;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public abstract class DecimalFormatSymbolsProvider extends LocaleServiceProvider {
    public abstract DecimalFormatSymbols getInstance(Locale locale);

    protected DecimalFormatSymbolsProvider() {
    }
}
