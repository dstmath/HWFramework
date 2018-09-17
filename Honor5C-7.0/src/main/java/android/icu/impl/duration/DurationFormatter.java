package android.icu.impl.duration;

import java.util.Date;
import java.util.TimeZone;

public interface DurationFormatter {
    String formatDurationFrom(long j, long j2);

    String formatDurationFromNow(long j);

    String formatDurationFromNowTo(Date date);

    DurationFormatter withLocale(String str);

    DurationFormatter withTimeZone(TimeZone timeZone);
}
