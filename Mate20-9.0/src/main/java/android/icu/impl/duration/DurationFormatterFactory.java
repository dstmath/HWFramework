package android.icu.impl.duration;

import java.util.TimeZone;

public interface DurationFormatterFactory {
    DurationFormatter getFormatter();

    DurationFormatterFactory setFallback(DateFormatter dateFormatter);

    DurationFormatterFactory setFallbackLimit(long j);

    DurationFormatterFactory setLocale(String str);

    DurationFormatterFactory setPeriodBuilder(PeriodBuilder periodBuilder);

    DurationFormatterFactory setPeriodFormatter(PeriodFormatter periodFormatter);

    DurationFormatterFactory setTimeZone(TimeZone timeZone);
}
