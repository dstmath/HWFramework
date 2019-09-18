package sun.util.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class CalendarSystem {
    private static final Gregorian GREGORIAN_INSTANCE = new Gregorian();
    private static final ConcurrentMap<String, CalendarSystem> calendars = new ConcurrentHashMap();
    private static final Map<String, Class<?>> names = new HashMap();

    public abstract CalendarDate getCalendarDate();

    public abstract CalendarDate getCalendarDate(long j);

    public abstract CalendarDate getCalendarDate(long j, TimeZone timeZone);

    public abstract CalendarDate getCalendarDate(long j, CalendarDate calendarDate);

    public abstract Era getEra(String str);

    public abstract Era[] getEras();

    public abstract int getMonthLength(CalendarDate calendarDate);

    public abstract String getName();

    public abstract CalendarDate getNthDayOfWeek(int i, int i2, CalendarDate calendarDate);

    public abstract long getTime(CalendarDate calendarDate);

    public abstract int getWeekLength();

    public abstract int getYearLength(CalendarDate calendarDate);

    public abstract int getYearLengthInMonths(CalendarDate calendarDate);

    public abstract CalendarDate newCalendarDate();

    public abstract CalendarDate newCalendarDate(TimeZone timeZone);

    public abstract boolean normalize(CalendarDate calendarDate);

    public abstract void setEra(CalendarDate calendarDate, String str);

    public abstract CalendarDate setTimeOfDay(CalendarDate calendarDate, int i);

    public abstract boolean validate(CalendarDate calendarDate);

    static {
        names.put("gregorian", Gregorian.class);
        names.put("japanese", LocalGregorianCalendar.class);
        names.put("julian", JulianCalendar.class);
    }

    public static Gregorian getGregorianCalendar() {
        return GREGORIAN_INSTANCE;
    }

    public static CalendarSystem forName(String calendarName) {
        CalendarSystem cal;
        if ("gregorian".equals(calendarName)) {
            return GREGORIAN_INSTANCE;
        }
        CalendarSystem cal2 = calendars.get(calendarName);
        if (cal2 != null) {
            return cal2;
        }
        Class<?> calendarClass = names.get(calendarName);
        if (calendarClass == null) {
            return null;
        }
        if (calendarClass.isAssignableFrom(LocalGregorianCalendar.class)) {
            cal = LocalGregorianCalendar.getLocalGregorianCalendar(calendarName);
        } else {
            try {
                cal = (CalendarSystem) calendarClass.newInstance();
            } catch (Exception e) {
                throw new InternalError((Throwable) e);
            }
        }
        if (cal == null) {
            return null;
        }
        CalendarSystem cs = calendars.putIfAbsent(calendarName, cal);
        return cs == null ? cal : cs;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0019, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0015, code lost:
        r3 = null;
     */
    public static Properties getCalendarProperties() throws IOException {
        Properties calendarProps = new Properties();
        InputStream is = ClassLoader.getSystemResourceAsStream("calendars.properties");
        calendarProps.load(is);
        if (is != null) {
            is.close();
        }
        return calendarProps;
        if (is != null) {
            if (r3 != null) {
                try {
                    is.close();
                } catch (Throwable th) {
                    r3.addSuppressed(th);
                }
            } else {
                is.close();
            }
        }
        throw th;
        throw th;
    }
}
