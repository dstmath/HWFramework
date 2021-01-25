package ohos.global.icu.text;

import java.text.ParsePosition;
import java.util.Locale;
import ohos.global.icu.impl.number.DecimalFormatProperties;
import ohos.global.icu.util.CurrencyAmount;
import ohos.global.icu.util.ULocale;

public class CompactDecimalFormat extends DecimalFormat {
    private static final long serialVersionUID = 4716293295276629682L;

    public enum CompactStyle {
        SHORT,
        LONG
    }

    public static CompactDecimalFormat getInstance(ULocale uLocale, CompactStyle compactStyle) {
        return new CompactDecimalFormat(uLocale, compactStyle);
    }

    public static CompactDecimalFormat getInstance(Locale locale, CompactStyle compactStyle) {
        return new CompactDecimalFormat(ULocale.forLocale(locale), compactStyle);
    }

    CompactDecimalFormat(ULocale uLocale, CompactStyle compactStyle) {
        this.symbols = DecimalFormatSymbols.getInstance(uLocale);
        this.properties = new DecimalFormatProperties();
        this.properties.setCompactStyle(compactStyle);
        this.properties.setGroupingSize(-2);
        this.properties.setMinimumGroupingDigits(2);
        this.exportedProperties = new DecimalFormatProperties();
        refreshFormatter();
    }

    @Override // ohos.global.icu.text.DecimalFormat, ohos.global.icu.text.NumberFormat
    public Number parse(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    @Override // ohos.global.icu.text.DecimalFormat, ohos.global.icu.text.NumberFormat
    public CurrencyAmount parseCurrency(CharSequence charSequence, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }
}
