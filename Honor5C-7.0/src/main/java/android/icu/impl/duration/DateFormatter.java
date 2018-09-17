package android.icu.impl.duration;

import java.util.Date;
import java.util.TimeZone;

public interface DateFormatter {
    String format(long j);

    String format(Date date);

    DateFormatter withLocale(String str);

    DateFormatter withTimeZone(TimeZone timeZone);
}
