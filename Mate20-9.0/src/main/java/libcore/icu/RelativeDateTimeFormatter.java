package libcore.icu;

import android.icu.text.ArabicShaping;
import android.icu.text.DisplayContext;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import java.util.Locale;
import java.util.TimeZone;
import libcore.util.BasicLruCache;

public final class RelativeDateTimeFormatter {
    private static final FormatterCache CACHED_FORMATTERS = new FormatterCache();
    public static final long DAY_IN_MILLIS = 86400000;
    private static final int DAY_IN_MS = 86400000;
    private static final int EPOCH_JULIAN_DAY = 2440588;
    public static final long HOUR_IN_MILLIS = 3600000;
    public static final long MINUTE_IN_MILLIS = 60000;
    public static final long SECOND_IN_MILLIS = 1000;
    public static final long WEEK_IN_MILLIS = 604800000;
    public static final long YEAR_IN_MILLIS = 31449600000L;

    static class FormatterCache extends BasicLruCache<String, android.icu.text.RelativeDateTimeFormatter> {
        FormatterCache() {
            super(8);
        }
    }

    private RelativeDateTimeFormatter() {
    }

    public static String getRelativeTimeSpanString(Locale locale, TimeZone tz, long time, long now, long minResolution, int flags) {
        return getRelativeTimeSpanString(locale, tz, time, now, minResolution, flags, DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE);
    }

    public static String getRelativeTimeSpanString(Locale locale, TimeZone tz, long time, long now, long minResolution, int flags, DisplayContext displayContext) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        } else if (tz != null) {
            return getRelativeTimeSpanString(ULocale.forLocale(locale), DateUtilsBridge.icuTimeZone(tz), time, now, minResolution, flags, displayContext);
        } else {
            throw new NullPointerException("tz == null");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:0x00f7 A[SYNTHETIC, Splitter:B:69:0x00f7] */
    private static String getRelativeTimeSpanString(ULocale icuLocale, android.icu.util.TimeZone icuTimeZone, long time, long now, long minResolution, int flags, DisplayContext displayContext) {
        RelativeDateTimeFormatter.Style style;
        RelativeDateTimeFormatter.Direction direction;
        ULocale uLocale;
        RelativeDateTimeFormatter.AbsoluteUnit aunit;
        RelativeDateTimeFormatter.Direction direction2;
        boolean relative;
        RelativeDateTimeFormatter.RelativeUnit unit;
        int count;
        int flags2;
        String str;
        ULocale uLocale2 = icuLocale;
        android.icu.util.TimeZone timeZone = icuTimeZone;
        long j = time;
        long j2 = now;
        DisplayContext displayContext2 = displayContext;
        long duration = Math.abs(j2 - j);
        boolean past = j2 >= j;
        if ((flags & ArabicShaping.TASHKEEL_REPLACE_BY_TATWEEL) != 0) {
            style = RelativeDateTimeFormatter.Style.SHORT;
        } else {
            style = RelativeDateTimeFormatter.Style.LONG;
        }
        RelativeDateTimeFormatter.Style style2 = style;
        if (past) {
            direction = RelativeDateTimeFormatter.Direction.LAST;
        } else {
            direction = RelativeDateTimeFormatter.Direction.NEXT;
        }
        RelativeDateTimeFormatter.Direction direction3 = direction;
        boolean relative2 = true;
        RelativeDateTimeFormatter.AbsoluteUnit aunit2 = null;
        if (duration < MINUTE_IN_MILLIS && minResolution < MINUTE_IN_MILLIS) {
            count = (int) (duration / 1000);
            unit = RelativeDateTimeFormatter.RelativeUnit.SECONDS;
        } else if (duration < HOUR_IN_MILLIS && minResolution < HOUR_IN_MILLIS) {
            count = (int) (duration / MINUTE_IN_MILLIS);
            unit = RelativeDateTimeFormatter.RelativeUnit.MINUTES;
        } else if (duration < 86400000 && minResolution < 86400000) {
            count = (int) (duration / HOUR_IN_MILLIS);
            unit = RelativeDateTimeFormatter.RelativeUnit.HOURS;
        } else if (duration >= WEEK_IN_MILLIS || minResolution >= WEEK_IN_MILLIS) {
            uLocale = icuLocale;
            if (minResolution == WEEK_IN_MILLIS) {
                count = (int) (duration / WEEK_IN_MILLIS);
                unit = RelativeDateTimeFormatter.RelativeUnit.WEEKS;
                direction2 = direction3;
                relative = true;
                aunit = null;
                int count2 = count;
                synchronized (CACHED_FORMATTERS) {
                    try {
                        android.icu.text.RelativeDateTimeFormatter formatter = getFormatter(uLocale, style2, displayContext2);
                        if (relative) {
                            boolean z = relative;
                            String format = formatter.format((double) count2, direction2, unit);
                            return format;
                        }
                        int i = count2;
                        String format2 = formatter.format(direction2, aunit);
                        return format2;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            } else {
                Calendar timeCalendar = DateUtilsBridge.createIcuCalendar(timeZone, uLocale, j);
                if ((flags & 12) == 0) {
                    if (timeCalendar.get(1) != DateUtilsBridge.createIcuCalendar(timeZone, uLocale, now).get(1)) {
                        flags2 = flags | 4;
                    } else {
                        flags2 = flags | 8;
                    }
                } else {
                    long j3 = now;
                    flags2 = flags;
                }
                return DateTimeFormat.format(uLocale, timeCalendar, flags2, displayContext2);
            }
        } else {
            int count3 = Math.abs(dayDistance(icuTimeZone, time, now));
            RelativeDateTimeFormatter.RelativeUnit relativeUnit = RelativeDateTimeFormatter.RelativeUnit.DAYS;
            if (count3 == 2) {
                if (past) {
                    synchronized (CACHED_FORMATTERS) {
                        boolean z2 = past;
                        uLocale = icuLocale;
                        str = getFormatter(uLocale, style2, displayContext2).format(RelativeDateTimeFormatter.Direction.LAST_2, RelativeDateTimeFormatter.AbsoluteUnit.DAY);
                    }
                } else {
                    uLocale = icuLocale;
                    synchronized (CACHED_FORMATTERS) {
                        str = getFormatter(uLocale, style2, displayContext2).format(RelativeDateTimeFormatter.Direction.NEXT_2, RelativeDateTimeFormatter.AbsoluteUnit.DAY);
                    }
                }
                if (str != null && !str.isEmpty()) {
                    return str;
                }
            } else {
                uLocale = icuLocale;
                if (count3 == 1) {
                    aunit2 = RelativeDateTimeFormatter.AbsoluteUnit.DAY;
                    relative2 = false;
                } else if (count3 == 0) {
                    aunit2 = RelativeDateTimeFormatter.AbsoluteUnit.DAY;
                    direction3 = RelativeDateTimeFormatter.Direction.THIS;
                    relative2 = false;
                }
            }
            count = count3;
            direction2 = direction3;
            relative = relative2;
            aunit = aunit2;
            unit = relativeUnit;
            int count22 = count;
            synchronized (CACHED_FORMATTERS) {
            }
        }
        direction2 = direction3;
        relative = true;
        aunit = null;
        uLocale = icuLocale;
        int count222 = count;
        synchronized (CACHED_FORMATTERS) {
        }
    }

    public static String getRelativeDateTimeString(Locale locale, TimeZone tz, long time, long now, long minResolution, long transitionResolution, int flags) {
        RelativeDateTimeFormatter.Style style;
        RelativeDateTimeFormatter.Style style2;
        ULocale icuLocale;
        Calendar timeCalendar;
        String dateClause;
        String combineDateAndTime;
        int flags2;
        long j = time;
        long j2 = now;
        if (locale == null) {
            throw new NullPointerException("locale == null");
        } else if (tz != null) {
            ULocale icuLocale2 = ULocale.forLocale(locale);
            android.icu.util.TimeZone icuTimeZone = DateUtilsBridge.icuTimeZone(tz);
            long duration = Math.abs(j2 - j);
            long transitionResolution2 = transitionResolution > WEEK_IN_MILLIS ? 604800000 : transitionResolution;
            if ((flags & ArabicShaping.TASHKEEL_REPLACE_BY_TATWEEL) != 0) {
                style = RelativeDateTimeFormatter.Style.SHORT;
            } else {
                style = RelativeDateTimeFormatter.Style.LONG;
            }
            RelativeDateTimeFormatter.Style style3 = style;
            Calendar timeCalendar2 = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale2, j);
            Calendar nowCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale2, j2);
            int days = Math.abs(DateUtilsBridge.dayDistance(timeCalendar2, nowCalendar));
            if (duration < transitionResolution2) {
                long minResolution2 = (days <= 0 || minResolution >= 86400000) ? minResolution : 86400000;
                long j3 = j;
                Calendar calendar = nowCalendar;
                style2 = style3;
                timeCalendar = timeCalendar2;
                android.icu.util.TimeZone timeZone = icuTimeZone;
                icuLocale = icuLocale2;
                dateClause = getRelativeTimeSpanString(icuLocale2, icuTimeZone, j3, j2, minResolution2, flags, DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE);
                int i = flags;
                long j4 = minResolution2;
            } else {
                style2 = style3;
                timeCalendar = timeCalendar2;
                android.icu.util.TimeZone timeZone2 = icuTimeZone;
                icuLocale = icuLocale2;
                if (timeCalendar.get(1) != nowCalendar.get(1)) {
                    flags2 = 131092;
                } else {
                    flags2 = 65560;
                }
                long j5 = minResolution;
                int i2 = flags2;
                dateClause = DateTimeFormat.format(icuLocale, timeCalendar, flags2, DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE);
            }
            String dateClause2 = dateClause;
            String timeClause = DateTimeFormat.format(icuLocale, timeCalendar, 1, DisplayContext.CAPITALIZATION_NONE);
            DisplayContext capitalizationContext = DisplayContext.CAPITALIZATION_NONE;
            synchronized (CACHED_FORMATTERS) {
                combineDateAndTime = getFormatter(icuLocale, style2, capitalizationContext).combineDateAndTime(dateClause2, timeClause);
            }
            return combineDateAndTime;
        } else {
            throw new NullPointerException("tz == null");
        }
    }

    private static android.icu.text.RelativeDateTimeFormatter getFormatter(ULocale locale, RelativeDateTimeFormatter.Style style, DisplayContext displayContext) {
        String key = locale + "\t" + style + "\t" + displayContext;
        android.icu.text.RelativeDateTimeFormatter formatter = (android.icu.text.RelativeDateTimeFormatter) CACHED_FORMATTERS.get(key);
        if (formatter != null) {
            return formatter;
        }
        android.icu.text.RelativeDateTimeFormatter formatter2 = android.icu.text.RelativeDateTimeFormatter.getInstance(locale, null, style, displayContext);
        CACHED_FORMATTERS.put(key, formatter2);
        return formatter2;
    }

    private static int dayDistance(android.icu.util.TimeZone icuTimeZone, long startTime, long endTime) {
        return julianDay(icuTimeZone, endTime) - julianDay(icuTimeZone, startTime);
    }

    private static int julianDay(android.icu.util.TimeZone icuTimeZone, long time) {
        return ((int) ((((long) icuTimeZone.getOffset(time)) + time) / 86400000)) + EPOCH_JULIAN_DAY;
    }
}
