package sun.util.calendar;

import java.util.TimeZone;

public class JulianCalendar extends BaseCalendar {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int BCE = 0;
    private static final int CE = 1;
    private static final int JULIAN_EPOCH = -1;
    private static final Era[] eras = null;

    private static class Date extends sun.util.calendar.BaseCalendar.Date {
        protected Date() {
            setCache(JulianCalendar.CE, -1, 365);
        }

        protected Date(TimeZone zone) {
            super(zone);
            setCache(JulianCalendar.CE, -1, 365);
        }

        public Date setEra(Era era) {
            if (era == null) {
                throw new NullPointerException();
            } else if (era == JulianCalendar.eras[JulianCalendar.BCE] && era == JulianCalendar.eras[JulianCalendar.CE]) {
                super.setEra(era);
                return this;
            } else {
                throw new IllegalArgumentException("unknown era: " + era);
            }
        }

        protected void setKnownEra(Era era) {
            super.setEra(era);
        }

        public int getNormalizedYear() {
            if (getEra() == JulianCalendar.eras[JulianCalendar.BCE]) {
                return 1 - getYear();
            }
            return getYear();
        }

        public void setNormalizedYear(int year) {
            if (year <= 0) {
                setYear(1 - year);
                setKnownEra(JulianCalendar.eras[JulianCalendar.BCE]);
                return;
            }
            setYear(year);
            setKnownEra(JulianCalendar.eras[JulianCalendar.CE]);
        }

        public String toString() {
            String time = super.toString();
            time = time.substring(time.indexOf(84));
            StringBuffer sb = new StringBuffer();
            Era era = getEra();
            if (era != null) {
                String n = era.getAbbreviation();
                if (n != null) {
                    sb.append(n).append(' ');
                }
            }
            sb.append(getYear()).append('-');
            CalendarUtils.sprintf0d(sb, getMonth(), 2).append('-');
            CalendarUtils.sprintf0d(sb, getDayOfMonth(), 2);
            sb.append(time);
            return sb.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.calendar.JulianCalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.calendar.JulianCalendar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.JulianCalendar.<clinit>():void");
    }

    JulianCalendar() {
        setEras(eras);
    }

    public String getName() {
        return "julian";
    }

    public Date getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
    }

    public Date getCalendarDate(long millis) {
        return getCalendarDate(millis, newCalendarDate());
    }

    public Date getCalendarDate(long millis, CalendarDate date) {
        return (Date) super.getCalendarDate(millis, date);
    }

    public Date getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, newCalendarDate(zone));
    }

    public Date newCalendarDate() {
        return new Date();
    }

    public Date newCalendarDate(TimeZone zone) {
        return new Date(zone);
    }

    public long getFixedDate(int jyear, int month, int dayOfMonth, sun.util.calendar.BaseCalendar.Date cache) {
        boolean isJan1 = (month == CE && dayOfMonth == CE) ? true : -assertionsDisabled;
        if (cache == null || !cache.hit(jyear)) {
            long y = (long) jyear;
            long days = (((y - 1) * 365) - 2) + ((long) dayOfMonth);
            if (y > 0) {
                days += (y - 1) / 4;
            } else {
                days += CalendarUtils.floorDivide(y - 1, 4);
            }
            if (month > 0) {
                days += ((((long) month) * 367) - 362) / 12;
            } else {
                days += CalendarUtils.floorDivide((((long) month) * 367) - 362, 12);
            }
            if (month > 2) {
                days -= (long) (CalendarUtils.isJulianLeapYear(jyear) ? CE : 2);
            }
            if (cache != null && isJan1) {
                cache.setCache(jyear, days, CalendarUtils.isJulianLeapYear(jyear) ? 366 : 365);
            }
            return days;
        } else if (isJan1) {
            return cache.getCachedJan1();
        } else {
            return (cache.getCachedJan1() + getDayOfYear(jyear, month, dayOfMonth)) - 1;
        }
    }

    public void getCalendarDateFromFixedDate(CalendarDate date, long fixedDate) {
        int year;
        Date jdate = (Date) date;
        long fd = ((fixedDate - -1) * 4) + 1464;
        if (fd >= 0) {
            year = (int) (fd / 1461);
        } else {
            year = (int) CalendarUtils.floorDivide(fd, 1461);
        }
        int priorDays = (int) (fixedDate - getFixedDate(year, CE, CE, jdate));
        boolean isLeap = CalendarUtils.isJulianLeapYear(year);
        if (fixedDate >= getFixedDate(year, 3, CE, jdate)) {
            priorDays += isLeap ? CE : 2;
        }
        int month = (priorDays * 12) + 373;
        if (month > 0) {
            month /= 367;
        } else {
            month = CalendarUtils.floorDivide(month, 367);
        }
        int dayOfMonth = ((int) (fixedDate - getFixedDate(year, month, CE, jdate))) + CE;
        int dayOfWeek = BaseCalendar.getDayOfWeekFromFixedDate(fixedDate);
        if (!-assertionsDisabled) {
            Object obj;
            if (dayOfWeek > 0) {
                obj = CE;
            } else {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError("negative day of week " + dayOfWeek);
            }
        }
        jdate.setNormalizedYear(year);
        jdate.setMonth(month);
        jdate.setDayOfMonth(dayOfMonth);
        jdate.setDayOfWeek(dayOfWeek);
        jdate.setLeapYear(isLeap);
        jdate.setNormalized(true);
    }

    public int getYearFromFixedDate(long fixedDate) {
        return (int) CalendarUtils.floorDivide(((fixedDate - -1) * 4) + 1464, 1461);
    }

    public int getDayOfWeek(CalendarDate date) {
        return BaseCalendar.getDayOfWeekFromFixedDate(getFixedDate(date));
    }

    boolean isLeapYear(int jyear) {
        return CalendarUtils.isJulianLeapYear(jyear);
    }
}
