package android.icu.text;

import android.icu.impl.duration.BasicDurationFormat;
import android.icu.util.ULocale;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

@Deprecated
public abstract class DurationFormat extends UFormat {
    private static final long serialVersionUID = -2076961954727774282L;

    @Deprecated
    public abstract StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition);

    @Deprecated
    public abstract String formatDurationFrom(long j, long j2);

    @Deprecated
    public abstract String formatDurationFromNow(long j);

    @Deprecated
    public abstract String formatDurationFromNowTo(Date date);

    @Deprecated
    public static DurationFormat getInstance(ULocale locale) {
        return BasicDurationFormat.getInstance(locale);
    }

    @Deprecated
    protected DurationFormat() {
    }

    @Deprecated
    protected DurationFormat(ULocale locale) {
        setLocale(locale, locale);
    }

    @Deprecated
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }
}
