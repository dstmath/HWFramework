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
        if ("gregorian".equals(calendarName)) {
            return GREGORIAN_INSTANCE;
        }
        CalendarSystem cal = (CalendarSystem) calendars.get(calendarName);
        if (cal != null) {
            return cal;
        }
        Class<?> calendarClass = (Class) names.get(calendarName);
        if (calendarClass == null) {
            return null;
        }
        if (calendarClass.isAssignableFrom(LocalGregorianCalendar.class)) {
            cal = LocalGregorianCalendar.getLocalGregorianCalendar(calendarName);
        } else {
            try {
                cal = (CalendarSystem) calendarClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("internal error", e);
            }
        }
        if (cal == null) {
            return null;
        }
        CalendarSystem cs = (CalendarSystem) calendars.putIfAbsent(calendarName, cal);
        if (cs != null) {
            cal = cs;
        }
        return cal;
    }

    public static Properties getCalendarProperties() throws IOException {
        Throwable th;
        Throwable th2 = null;
        Properties calendarProps = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = ClassLoader.getSystemResourceAsStream("calendars.properties");
            calendarProps.load(inputStream);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 == null) {
                return calendarProps;
            }
            throw th2;
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }
}
