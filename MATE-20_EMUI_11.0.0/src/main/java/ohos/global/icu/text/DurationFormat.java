package ohos.global.icu.text;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import ohos.global.icu.impl.duration.BasicDurationFormat;
import ohos.global.icu.util.ULocale;

@Deprecated
public abstract class DurationFormat extends UFormat {
    private static final long serialVersionUID = -2076961954727774282L;

    @Override // java.text.Format
    @Deprecated
    public abstract StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition);

    @Deprecated
    public abstract String formatDurationFrom(long j, long j2);

    @Deprecated
    public abstract String formatDurationFromNow(long j);

    @Deprecated
    public abstract String formatDurationFromNowTo(Date date);

    @Deprecated
    public static DurationFormat getInstance(ULocale uLocale) {
        return BasicDurationFormat.getInstance(uLocale);
    }

    @Deprecated
    protected DurationFormat() {
    }

    @Deprecated
    protected DurationFormat(ULocale uLocale) {
        setLocale(uLocale, uLocale);
    }

    @Override // java.text.Format
    @Deprecated
    public Object parseObject(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }
}
