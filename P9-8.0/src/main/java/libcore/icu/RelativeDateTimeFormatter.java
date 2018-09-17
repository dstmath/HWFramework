package libcore.icu;

import android.icu.text.ArabicShaping;
import android.icu.text.DisplayContext;
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit;
import android.icu.text.RelativeDateTimeFormatter.Direction;
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit;
import android.icu.text.RelativeDateTimeFormatter.Style;
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

    private static String getRelativeTimeSpanString(ULocale icuLocale, android.icu.util.TimeZone icuTimeZone, long time, long now, long minResolution, int flags, DisplayContext displayContext) {
        Style style;
        Direction direction;
        int count;
        RelativeUnit unit;
        long duration = Math.abs(now - time);
        boolean past = now >= time;
        if ((ArabicShaping.TASHKEEL_REPLACE_BY_TATWEEL & flags) != 0) {
            style = Style.SHORT;
        } else {
            style = Style.LONG;
        }
        if (past) {
            direction = Direction.LAST;
        } else {
            direction = Direction.NEXT;
        }
        boolean relative = true;
        AbsoluteUnit aunit = null;
        if (duration < MINUTE_IN_MILLIS && minResolution < MINUTE_IN_MILLIS) {
            count = (int) (duration / 1000);
            unit = RelativeUnit.SECONDS;
        } else if (duration < HOUR_IN_MILLIS && minResolution < HOUR_IN_MILLIS) {
            count = (int) (duration / MINUTE_IN_MILLIS);
            unit = RelativeUnit.MINUTES;
        } else if (duration < 86400000 && minResolution < 86400000) {
            count = (int) (duration / HOUR_IN_MILLIS);
            unit = RelativeUnit.HOURS;
        } else if (duration < WEEK_IN_MILLIS && minResolution < WEEK_IN_MILLIS) {
            count = Math.abs(dayDistance(icuTimeZone, time, now));
            unit = RelativeUnit.DAYS;
            if (count == 2) {
                String str;
                FormatterCache formatterCache;
                if (past) {
                    formatterCache = CACHED_FORMATTERS;
                    synchronized (formatterCache) {
                        str = getFormatter(icuLocale, style, displayContext).format(Direction.LAST_2, AbsoluteUnit.DAY);
                    }
                } else {
                    formatterCache = CACHED_FORMATTERS;
                    synchronized (formatterCache) {
                        str = getFormatter(icuLocale, style, displayContext).format(Direction.NEXT_2, AbsoluteUnit.DAY);
                    }
                }
                if (!(str == null || (str.isEmpty() ^ 1) == 0)) {
                    return str;
                }
            } else if (count == 1) {
                aunit = AbsoluteUnit.DAY;
                relative = false;
            } else if (count == 0) {
                aunit = AbsoluteUnit.DAY;
                direction = Direction.THIS;
                relative = false;
            }
        } else if (minResolution == WEEK_IN_MILLIS) {
            count = (int) (duration / WEEK_IN_MILLIS);
            unit = RelativeUnit.WEEKS;
        } else {
            Calendar timeCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, time);
            if ((flags & 12) == 0) {
                if (timeCalendar.get(1) != DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, now).get(1)) {
                    flags |= 4;
                } else {
                    flags |= 8;
                }
            }
            return DateTimeFormat.format(icuLocale, timeCalendar, flags, displayContext);
        }
        synchronized (CACHED_FORMATTERS) {
            android.icu.text.RelativeDateTimeFormatter formatter = getFormatter(icuLocale, style, displayContext);
            String format;
            if (relative) {
                format = formatter.format((double) count, direction, unit);
                return format;
            }
            format = formatter.format(direction, aunit);
            return format;
        }
    }

    public static String getRelativeDateTimeString(Locale locale, TimeZone tz, long time, long now, long minResolution, long transitionResolution, int flags) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        } else if (tz == null) {
            throw new NullPointerException("tz == null");
        } else {
            Style style;
            String dateClause;
            String combineDateAndTime;
            ULocale icuLocale = ULocale.forLocale(locale);
            android.icu.util.TimeZone icuTimeZone = DateUtilsBridge.icuTimeZone(tz);
            long duration = Math.abs(now - time);
            if (transitionResolution > WEEK_IN_MILLIS) {
                transitionResolution = WEEK_IN_MILLIS;
            }
            if ((ArabicShaping.TASHKEEL_REPLACE_BY_TATWEEL & flags) != 0) {
                style = Style.SHORT;
            } else {
                style = Style.LONG;
            }
            Calendar timeCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, time);
            Calendar nowCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, now);
            int days = Math.abs(DateUtilsBridge.dayDistance(timeCalendar, nowCalendar));
            if (duration < transitionResolution) {
                if (days > 0 && minResolution < 86400000) {
                    minResolution = 86400000;
                }
                dateClause = getRelativeTimeSpanString(icuLocale, icuTimeZone, time, now, minResolution, flags, DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE);
            } else {
                if (timeCalendar.get(1) != nowCalendar.get(1)) {
                    flags = 131092;
                } else {
                    flags = 65560;
                }
                dateClause = DateTimeFormat.format(icuLocale, timeCalendar, flags, DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE);
            }
            String timeClause = DateTimeFormat.format(icuLocale, timeCalendar, 1, DisplayContext.CAPITALIZATION_NONE);
            DisplayContext capitalizationContext = DisplayContext.CAPITALIZATION_NONE;
            synchronized (CACHED_FORMATTERS) {
                combineDateAndTime = getFormatter(icuLocale, style, capitalizationContext).combineDateAndTime(dateClause, timeClause);
            }
            return combineDateAndTime;
        }
    }

    private static android.icu.text.RelativeDateTimeFormatter getFormatter(ULocale locale, Style style, DisplayContext displayContext) {
        String key = locale + "\t" + style + "\t" + displayContext;
        android.icu.text.RelativeDateTimeFormatter formatter = (android.icu.text.RelativeDateTimeFormatter) CACHED_FORMATTERS.get(key);
        if (formatter != null) {
            return formatter;
        }
        formatter = android.icu.text.RelativeDateTimeFormatter.getInstance(locale, null, style, displayContext);
        CACHED_FORMATTERS.put(key, formatter);
        return formatter;
    }

    private static int dayDistance(android.icu.util.TimeZone icuTimeZone, long startTime, long endTime) {
        return julianDay(icuTimeZone, endTime) - julianDay(icuTimeZone, startTime);
    }

    private static int julianDay(android.icu.util.TimeZone icuTimeZone, long time) {
        return ((int) ((time + ((long) icuTimeZone.getOffset(time))) / 86400000)) + EPOCH_JULIAN_DAY;
    }
}
