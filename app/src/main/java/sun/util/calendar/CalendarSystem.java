package sun.util.calendar;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

public abstract class CalendarSystem {
    private static final Gregorian GREGORIAN_INSTANCE = null;
    private static final ConcurrentMap<String, CalendarSystem> calendars = null;
    private static final Map<String, Class<?>> names = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.calendar.CalendarSystem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.calendar.CalendarSystem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.CalendarSystem.<clinit>():void");
    }

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
}
