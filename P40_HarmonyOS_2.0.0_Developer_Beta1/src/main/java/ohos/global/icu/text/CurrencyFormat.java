package ohos.global.icu.text;

import java.io.ObjectStreamException;
import java.text.FieldPosition;
import java.text.ParsePosition;
import ohos.global.icu.text.MeasureFormat;
import ohos.global.icu.util.CurrencyAmount;
import ohos.global.icu.util.ULocale;

/* access modifiers changed from: package-private */
public class CurrencyFormat extends MeasureFormat {
    static final long serialVersionUID = -931679363692504634L;

    public CurrencyFormat(ULocale uLocale) {
        super(uLocale, MeasureFormat.FormatWidth.DEFAULT_CURRENCY);
    }

    @Override // ohos.global.icu.text.MeasureFormat, java.text.Format
    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (obj instanceof CurrencyAmount) {
            return super.format(obj, stringBuffer, fieldPosition);
        }
        throw new IllegalArgumentException("Invalid type: " + obj.getClass().getName());
    }

    @Override // ohos.global.icu.text.MeasureFormat, java.text.Format
    public CurrencyAmount parseObject(String str, ParsePosition parsePosition) {
        return getNumberFormatInternal().parseCurrency(str, parsePosition);
    }

    private Object writeReplace() throws ObjectStreamException {
        return toCurrencyProxy();
    }

    private Object readResolve() throws ObjectStreamException {
        return new CurrencyFormat(getLocale(ULocale.ACTUAL_LOCALE));
    }
}
