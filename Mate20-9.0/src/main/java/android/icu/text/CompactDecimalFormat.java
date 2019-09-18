package android.icu.text;

import android.icu.impl.number.DecimalFormatProperties;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
import java.text.ParsePosition;
import java.util.Locale;

public class CompactDecimalFormat extends DecimalFormat {
    private static final long serialVersionUID = 4716293295276629682L;

    public enum CompactStyle {
        SHORT,
        LONG
    }

    public static CompactDecimalFormat getInstance(ULocale locale, CompactStyle style) {
        return new CompactDecimalFormat(locale, style);
    }

    public static CompactDecimalFormat getInstance(Locale locale, CompactStyle style) {
        return new CompactDecimalFormat(ULocale.forLocale(locale), style);
    }

    CompactDecimalFormat(ULocale locale, CompactStyle style) {
        this.symbols = DecimalFormatSymbols.getInstance(locale);
        this.properties = new DecimalFormatProperties();
        this.properties.setCompactStyle(style);
        this.properties.setGroupingSize(-2);
        this.properties.setMinimumGroupingDigits(2);
        this.exportedProperties = new DecimalFormatProperties();
        refreshFormatter();
    }

    public Number parse(String text, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    public CurrencyAmount parseCurrency(CharSequence text, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }
}
