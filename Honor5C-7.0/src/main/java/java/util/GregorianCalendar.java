package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.util.Locale.Category;
import libcore.util.ZoneInfo;
import sun.util.calendar.AbstractCalendar;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.BaseCalendar.Date;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.JulianCalendar;
import sun.util.logging.PlatformLogger;

public class GregorianCalendar extends Calendar {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int AD = 1;
    public static final int BC = 0;
    static final int BCE = 0;
    static final int CE = 1;
    static final long DEFAULT_GREGORIAN_CUTOVER = -12219292800000L;
    private static final int EPOCH_OFFSET = 719163;
    private static final int EPOCH_YEAR = 1970;
    static final int[] LEAP_MONTH_LENGTH = null;
    static final int[] LEAST_MAX_VALUES = null;
    static final int[] MAX_VALUES = null;
    static final int[] MIN_VALUES = null;
    static final int[] MONTH_LENGTH = null;
    private static final long ONE_DAY = 86400000;
    private static final int ONE_HOUR = 3600000;
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_SECOND = 1000;
    private static final long ONE_WEEK = 604800000;
    private static final Gregorian gcal = null;
    private static JulianCalendar jcal = null;
    private static Era[] jeras = null;
    static final long serialVersionUID = -8125100834729963327L;
    private transient long cachedFixedDate;
    private transient BaseCalendar calsys;
    private transient Date cdate;
    private transient Date gdate;
    private long gregorianCutover;
    private transient long gregorianCutoverDate;
    private transient int gregorianCutoverYear;
    private transient int gregorianCutoverYearJulian;
    private transient int[] originalFields;
    private transient int[] zoneOffsets;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.GregorianCalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.GregorianCalendar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.GregorianCalendar.<clinit>():void");
    }

    protected void computeFields() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.GregorianCalendar.computeFields():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.GregorianCalendar.computeFields():void");
    }

    public GregorianCalendar() {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Category.FORMAT));
        setZoneShared(true);
    }

    public GregorianCalendar(TimeZone zone) {
        this(zone, Locale.getDefault(Category.FORMAT));
    }

    public GregorianCalendar(Locale aLocale) {
        this(TimeZone.getDefaultRef(), aLocale);
        setZoneShared(true);
    }

    public GregorianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        this.gregorianCutover = DEFAULT_GREGORIAN_CUTOVER;
        this.gregorianCutoverDate = 577736;
        this.gregorianCutoverYear = 1582;
        this.gregorianCutoverYearJulian = 1582;
        this.cachedFixedDate = Long.MIN_VALUE;
        this.gdate = gcal.newCalendarDate(zone);
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(int year, int month, int dayOfMonth) {
        this(year, month, dayOfMonth, BCE, BCE, BCE, BCE);
    }

    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, BCE, BCE);
    }

    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        this(year, month, dayOfMonth, hourOfDay, minute, second, BCE);
    }

    GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int millis) {
        this.gregorianCutover = DEFAULT_GREGORIAN_CUTOVER;
        this.gregorianCutoverDate = 577736;
        this.gregorianCutoverYear = 1582;
        this.gregorianCutoverYearJulian = 1582;
        this.cachedFixedDate = Long.MIN_VALUE;
        this.gdate = gcal.newCalendarDate(getZone());
        set(CE, year);
        set(2, month);
        set(5, dayOfMonth);
        if (hourOfDay < 12 || hourOfDay > 23) {
            internalSet(10, hourOfDay);
        } else {
            internalSet(9, CE);
            internalSet(10, hourOfDay - 12);
        }
        setFieldsComputed(1536);
        set(11, hourOfDay);
        set(12, minute);
        set(13, second);
        internalSet(14, millis);
    }

    GregorianCalendar(long milliseconds) {
        this();
        setTimeInMillis(milliseconds);
    }

    public void setGregorianChange(Date date) {
        long cutoverTime = date.getTime();
        if (cutoverTime != this.gregorianCutover) {
            complete();
            setGregorianChange(cutoverTime);
        }
    }

    private void setGregorianChange(long cutoverTime) {
        this.gregorianCutover = cutoverTime;
        this.gregorianCutoverDate = CalendarUtils.floorDivide(cutoverTime, (long) ONE_DAY) + 719163;
        if (cutoverTime == Long.MAX_VALUE) {
            this.gregorianCutoverDate++;
        }
        this.gregorianCutoverYear = getGregorianCutoverDate().getYear();
        BaseCalendar jcal = getJulianCalendarSystem();
        Date d = (Date) jcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        jcal.getCalendarDateFromFixedDate(d, this.gregorianCutoverDate - 1);
        this.gregorianCutoverYearJulian = d.getNormalizedYear();
        if (this.time < this.gregorianCutover) {
            setUnnormalized();
        }
    }

    public final Date getGregorianChange() {
        return new Date(this.gregorianCutover);
    }

    public boolean isLeapYear(int year) {
        boolean z = true;
        if ((year & 3) != 0) {
            return -assertionsDisabled;
        }
        if (year > this.gregorianCutoverYear) {
            if (year % 100 == 0 && year % PlatformLogger.FINER != 0) {
                z = -assertionsDisabled;
            }
            return z;
        } else if (year < this.gregorianCutoverYearJulian) {
            return true;
        } else {
            boolean gregorian = this.gregorianCutoverYear == this.gregorianCutoverYearJulian ? getCalendarDate(this.gregorianCutoverDate).getMonth() < 3 ? true : -assertionsDisabled : year == this.gregorianCutoverYear ? true : -assertionsDisabled;
            if (gregorian && year % 100 == 0 && year % PlatformLogger.FINER != 0) {
                z = -assertionsDisabled;
            }
            return z;
        }
    }

    public boolean equals(Object obj) {
        if ((obj instanceof GregorianCalendar) && super.equals(obj) && this.gregorianCutover == ((GregorianCalendar) obj).gregorianCutover) {
            return true;
        }
        return -assertionsDisabled;
    }

    public int hashCode() {
        return super.hashCode() ^ ((int) this.gregorianCutoverDate);
    }

    public void add(int field, int amount) {
        if (amount != 0) {
            if (field < 0 || field >= 15) {
                throw new IllegalArgumentException();
            }
            complete();
            int year;
            if (field == CE) {
                year = internalGet(CE);
                if (internalGetEra() == CE) {
                    year += amount;
                    if (year > 0) {
                        set(CE, year);
                    } else {
                        set(CE, 1 - year);
                        set(BCE, BCE);
                    }
                } else {
                    year -= amount;
                    if (year > 0) {
                        set(CE, year);
                    } else {
                        set(CE, 1 - year);
                        set(BCE, CE);
                    }
                }
                pinDayOfMonth();
            } else if (field == 2) {
                int y_amount;
                int month = internalGet(2) + amount;
                year = internalGet(CE);
                if (month >= 0) {
                    y_amount = month / 12;
                } else {
                    y_amount = ((month + CE) / 12) - 1;
                }
                if (y_amount != 0) {
                    if (internalGetEra() == CE) {
                        year += y_amount;
                        if (year > 0) {
                            set(CE, year);
                        } else {
                            set(CE, 1 - year);
                            set(BCE, BCE);
                        }
                    } else {
                        year -= y_amount;
                        if (year > 0) {
                            set(CE, year);
                        } else {
                            set(CE, 1 - year);
                            set(BCE, CE);
                        }
                    }
                }
                if (month >= 0) {
                    set(2, month % 12);
                } else {
                    month %= 12;
                    if (month < 0) {
                        month += 12;
                    }
                    set(2, month + BCE);
                }
                pinDayOfMonth();
            } else if (field == 0) {
                int era = internalGet(BCE) + amount;
                if (era < 0) {
                    era = BCE;
                }
                if (era > CE) {
                    era = CE;
                }
                set(BCE, era);
            } else {
                long delta = (long) amount;
                long timeOfDay = 0;
                switch (field) {
                    case BaseCalendar.TUESDAY /*3*/:
                    case BaseCalendar.WEDNESDAY /*4*/:
                    case BaseCalendar.AUGUST /*8*/:
                        delta *= 7;
                        break;
                    case BaseCalendar.SEPTEMBER /*9*/:
                        delta = (long) (amount / 2);
                        timeOfDay = (long) ((amount % 2) * 12);
                        break;
                    case BaseCalendar.OCTOBER /*10*/:
                    case BaseCalendar.NOVEMBER /*11*/:
                        delta *= 3600000;
                        break;
                    case BaseCalendar.DECEMBER /*12*/:
                        delta *= 60000;
                        break;
                    case Calendar.SECOND /*13*/:
                        delta *= 1000;
                        break;
                }
                if (field >= 10) {
                    setTimeInMillis(this.time + delta);
                    return;
                }
                long fd = getCurrentFixedDate();
                long internalGet = (long) internalGet(11);
                internalGet = (long) internalGet(12);
                internalGet = (long) internalGet(13);
                timeOfDay = ((((((timeOfDay + r0) * 60) + r0) * 60) + r0) * 1000) + ((long) internalGet(14));
                if (timeOfDay >= ONE_DAY) {
                    fd++;
                    timeOfDay -= ONE_DAY;
                } else if (timeOfDay < 0) {
                    fd--;
                    timeOfDay += ONE_DAY;
                }
                setTimeInMillis(adjustForZoneAndDaylightSavingsTime(BCE, (((fd + delta) - 719163) * ONE_DAY) + timeOfDay, getZone()));
            }
        }
    }

    public void roll(int field, boolean up) {
        roll(field, up ? CE : -1);
    }

    public void roll(int field, int amount) {
        if (amount != 0) {
            if (field < 0 || field >= 15) {
                throw new IllegalArgumentException();
            }
            complete();
            int min = getMinimum(field);
            int max = getMaximum(field);
            long fd;
            BaseCalendar cal;
            Date d;
            long month1;
            int monthLength;
            BaseCalendar baseCalendar;
            switch (field) {
                case BaseCalendar.MONDAY /*2*/:
                    int mon;
                    int monthLen;
                    if (isCutoverYear(this.cdate.getNormalizedYear())) {
                        int yearLength = getActualMaximum(2) + CE;
                        mon = (internalGet(2) + amount) % yearLength;
                        if (mon < 0) {
                            mon += yearLength;
                        }
                        set(2, mon);
                        monthLen = getActualMaximum(5);
                        if (internalGet(5) > monthLen) {
                            set(5, monthLen);
                        }
                    } else {
                        mon = (internalGet(2) + amount) % 12;
                        if (mon < 0) {
                            mon += 12;
                        }
                        set(2, mon);
                        monthLen = monthLength(mon);
                        if (internalGet(5) > monthLen) {
                            set(5, monthLen);
                        }
                    }
                    return;
                case BaseCalendar.TUESDAY /*3*/:
                    int y = this.cdate.getNormalizedYear();
                    max = getActualMaximum(3);
                    set(7, internalGet(7));
                    int woy = internalGet(3);
                    int value = woy + amount;
                    if (isCutoverYear(y)) {
                        fd = getCurrentFixedDate();
                        if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                            cal = getCutoverCalendarSystem();
                        } else {
                            if (y == this.gregorianCutoverYear) {
                                cal = gcal;
                            } else {
                                cal = getJulianCalendarSystem();
                            }
                        }
                        long day1 = fd - ((long) ((woy - min) * 7));
                        if (cal.getYearFromFixedDate(day1) != y) {
                            min += CE;
                        }
                        fd += (long) ((max - woy) * 7);
                        if ((fd >= this.gregorianCutoverDate ? gcal : getJulianCalendarSystem()).getYearFromFixedDate(fd) != y) {
                            max--;
                        }
                        d = getCalendarDate(((long) ((getRolledValue(woy, amount, min, max) - 1) * 7)) + day1);
                        set(2, d.getMonth() - 1);
                        set(5, d.getDayOfMonth());
                        return;
                    } else if (value <= min || value >= max) {
                        fd = getCurrentFixedDate();
                        if (this.calsys.getYearFromFixedDate(fd - ((long) ((woy - min) * 7))) != y) {
                            min += CE;
                        }
                        if (this.calsys.getYearFromFixedDate(fd + ((long) ((max - internalGet(3)) * 7))) != y) {
                            max--;
                            break;
                        }
                    } else {
                        set(3, value);
                        return;
                    }
                    break;
                case BaseCalendar.WEDNESDAY /*4*/:
                    int dayOfMonth;
                    boolean isCutoverYear = isCutoverYear(this.cdate.getNormalizedYear());
                    int dow = internalGet(7) - getFirstDayOfWeek();
                    if (dow < 0) {
                        dow += 7;
                    }
                    fd = getCurrentFixedDate();
                    if (isCutoverYear) {
                        month1 = getFixedDateMonth1(this.cdate, fd);
                        monthLength = actualMonthLength();
                    } else {
                        month1 = (fd - ((long) internalGet(5))) + 1;
                        monthLength = this.calsys.getMonthLength(this.cdate);
                    }
                    baseCalendar = this.calsys;
                    long monthDay1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + month1, getFirstDayOfWeek());
                    if (((int) (monthDay1st - month1)) >= getMinimalDaysInFirstWeek()) {
                        monthDay1st -= 7;
                    }
                    long nfd = (((long) ((getRolledValue(internalGet(field), amount, CE, getActualMaximum(field)) - 1) * 7)) + monthDay1st) + ((long) dow);
                    if (nfd < month1) {
                        nfd = month1;
                    } else {
                        if (nfd >= ((long) monthLength) + month1) {
                            nfd = (((long) monthLength) + month1) - 1;
                        }
                    }
                    if (isCutoverYear) {
                        dayOfMonth = getCalendarDate(nfd).getDayOfMonth();
                    } else {
                        dayOfMonth = ((int) (nfd - month1)) + CE;
                    }
                    set(5, dayOfMonth);
                    return;
                case BaseCalendar.THURSDAY /*5*/:
                    if (!isCutoverYear(this.cdate.getNormalizedYear())) {
                        max = this.calsys.getMonthLength(this.cdate);
                        break;
                    }
                    fd = getCurrentFixedDate();
                    month1 = getFixedDateMonth1(this.cdate, fd);
                    d = getCalendarDate(((long) getRolledValue((int) (fd - month1), amount, BCE, actualMonthLength() - 1)) + month1);
                    if (!-assertionsDisabled) {
                        if ((d.getMonth() + -1 == internalGet(2) ? CE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    set(5, d.getDayOfMonth());
                    return;
                case BaseCalendar.JUNE /*6*/:
                    max = getActualMaximum(field);
                    if (isCutoverYear(this.cdate.getNormalizedYear())) {
                        fd = getCurrentFixedDate();
                        long jan1 = (fd - ((long) internalGet(6))) + 1;
                        d = getCalendarDate((((long) getRolledValue(((int) (fd - jan1)) + CE, amount, min, max)) + jan1) - 1);
                        set(2, d.getMonth() - 1);
                        set(5, d.getDayOfMonth());
                        return;
                    }
                    break;
                case BaseCalendar.SATURDAY /*7*/:
                    if (!isCutoverYear(this.cdate.getNormalizedYear())) {
                        int weekOfYear = internalGet(3);
                        if (weekOfYear > CE && weekOfYear < 52) {
                            set(3, weekOfYear);
                            max = 7;
                            break;
                        }
                    }
                    amount %= 7;
                    if (amount != 0) {
                        fd = getCurrentFixedDate();
                        baseCalendar = this.calsys;
                        long dowFirst = AbstractCalendar.getDayOfWeekDateOnOrBefore(fd, getFirstDayOfWeek());
                        fd += (long) amount;
                        if (fd < dowFirst) {
                            fd += 7;
                        } else if (fd >= 7 + dowFirst) {
                            fd -= 7;
                        }
                        d = getCalendarDate(fd);
                        set(BCE, d.getNormalizedYear() <= 0 ? BCE : CE);
                        set(d.getYear(), d.getMonth() - 1, d.getDayOfMonth());
                        return;
                    }
                    return;
                case BaseCalendar.AUGUST /*8*/:
                    min = CE;
                    if (!isCutoverYear(this.cdate.getNormalizedYear())) {
                        int dom = internalGet(5);
                        monthLength = this.calsys.getMonthLength(this.cdate);
                        max = monthLength / 7;
                        if ((dom - 1) % 7 < monthLength % 7) {
                            max += CE;
                        }
                        set(7, internalGet(7));
                        break;
                    }
                    fd = getCurrentFixedDate();
                    month1 = getFixedDateMonth1(this.cdate, fd);
                    monthLength = actualMonthLength();
                    max = monthLength / 7;
                    int x = ((int) (fd - month1)) % 7;
                    if (x < monthLength % 7) {
                        max += CE;
                    }
                    fd = (((long) ((getRolledValue(internalGet(field), amount, CE, max) - 1) * 7)) + month1) + ((long) x);
                    cal = fd >= this.gregorianCutoverDate ? gcal : getJulianCalendarSystem();
                    d = (Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    cal.getCalendarDateFromFixedDate(d, fd);
                    set(5, d.getDayOfMonth());
                    return;
                case BaseCalendar.OCTOBER /*10*/:
                case BaseCalendar.NOVEMBER /*11*/:
                    int unit = max + CE;
                    int h = internalGet(field);
                    int nh = (h + amount) % unit;
                    if (nh < 0) {
                        nh += unit;
                    }
                    this.time += (long) ((nh - h) * ONE_HOUR);
                    CalendarDate d2 = this.calsys.getCalendarDate(this.time, getZone());
                    if (internalGet(5) != d2.getDayOfMonth()) {
                        d2.setDate(internalGet(CE), internalGet(2) + CE, internalGet(5));
                        if (field == 10) {
                            if (!-assertionsDisabled) {
                                if ((internalGet(9) == CE ? CE : null) == null) {
                                    throw new AssertionError();
                                }
                            }
                            d2.addHours(12);
                        }
                        this.time = this.calsys.getTime(d2);
                    }
                    int hourOfDay = d2.getHours();
                    internalSet(field, hourOfDay % unit);
                    if (field == 10) {
                        internalSet(11, hourOfDay);
                    } else {
                        internalSet(9, hourOfDay / 12);
                        internalSet(10, hourOfDay % 12);
                    }
                    int zoneOffset = d2.getZoneOffset();
                    int saving = d2.getDaylightSaving();
                    internalSet(15, zoneOffset - saving);
                    internalSet(16, saving);
                    return;
            }
            set(field, getRolledValue(internalGet(field), amount, min, max));
        }
    }

    public int getMinimum(int field) {
        return MIN_VALUES[field];
    }

    public int getMaximum(int field) {
        switch (field) {
            case CE /*1*/:
            case BaseCalendar.MONDAY /*2*/:
            case BaseCalendar.TUESDAY /*3*/:
            case BaseCalendar.WEDNESDAY /*4*/:
            case BaseCalendar.THURSDAY /*5*/:
            case BaseCalendar.JUNE /*6*/:
            case BaseCalendar.AUGUST /*8*/:
                if (this.gregorianCutoverYear <= HttpURLConnection.HTTP_OK) {
                    GregorianCalendar gc = (GregorianCalendar) clone();
                    gc.setLenient(true);
                    gc.setTimeInMillis(this.gregorianCutover);
                    int v1 = gc.getActualMaximum(field);
                    gc.setTimeInMillis(this.gregorianCutover - 1);
                    return Math.max(MAX_VALUES[field], Math.max(v1, gc.getActualMaximum(field)));
                }
                break;
        }
        return MAX_VALUES[field];
    }

    public int getGreatestMinimum(int field) {
        if (field != 5) {
            return MIN_VALUES[field];
        }
        return Math.max(MIN_VALUES[field], getCalendarDate(getFixedDateMonth1(getGregorianCutoverDate(), this.gregorianCutoverDate)).getDayOfMonth());
    }

    public int getLeastMaximum(int field) {
        switch (field) {
            case CE /*1*/:
            case BaseCalendar.MONDAY /*2*/:
            case BaseCalendar.TUESDAY /*3*/:
            case BaseCalendar.WEDNESDAY /*4*/:
            case BaseCalendar.THURSDAY /*5*/:
            case BaseCalendar.JUNE /*6*/:
            case BaseCalendar.AUGUST /*8*/:
                GregorianCalendar gc = (GregorianCalendar) clone();
                gc.setLenient(true);
                gc.setTimeInMillis(this.gregorianCutover);
                int v1 = gc.getActualMaximum(field);
                gc.setTimeInMillis(this.gregorianCutover - 1);
                return Math.min(LEAST_MAX_VALUES[field], Math.min(v1, gc.getActualMaximum(field)));
            default:
                return LEAST_MAX_VALUES[field];
        }
    }

    public int getActualMinimum(int field) {
        if (field == 5) {
            GregorianCalendar gc = getNormalizedCalendar();
            int year = gc.cdate.getNormalizedYear();
            if (year == this.gregorianCutoverYear || year == this.gregorianCutoverYearJulian) {
                return getCalendarDate(getFixedDateMonth1(gc.cdate, gc.calsys.getFixedDate(gc.cdate))).getDayOfMonth();
            }
        }
        return getMinimum(field);
    }

    public int getActualMaximum(int field) {
        if (((CE << field) & 130689) != 0) {
            return getMaximum(field);
        }
        int value;
        GregorianCalendar gc = getNormalizedCalendar();
        Date date = gc.cdate;
        BaseCalendar cal = gc.calsys;
        int normalizedYear = date.getNormalizedYear();
        CalendarDate d;
        Object obj;
        long nextJan1;
        Date d2;
        int dayOfWeek;
        switch (field) {
            case CE /*1*/:
                if (gc == this) {
                    gc = (GregorianCalendar) clone();
                }
                long current = gc.getYearOffsetInMillis();
                if (gc.internalGetEra() != CE) {
                    CalendarSystem mincal = gc.getTimeInMillis() >= this.gregorianCutover ? gcal : getJulianCalendarSystem();
                    d = mincal.getCalendarDate(Long.MIN_VALUE, getZone());
                    long seconds = (long) d.getSeconds();
                    long maxEnd = ((((((((cal.getDayOfYear(d) - 1) * 24) + ((long) d.getHours())) * 60) + ((long) d.getMinutes())) * 60) + r0) * 1000) + ((long) d.getMillis());
                    value = d.getYear();
                    if (value <= 0) {
                        if (!-assertionsDisabled) {
                            if (mincal == gcal) {
                                obj = CE;
                            } else {
                                obj = null;
                            }
                            if (obj == null) {
                                throw new AssertionError();
                            }
                        }
                        value = 1 - value;
                    }
                    if (current < maxEnd) {
                        value--;
                        break;
                    }
                }
                gc.setTimeInMillis(Long.MAX_VALUE);
                value = gc.get(CE);
                if (current > gc.getYearOffsetInMillis()) {
                    value--;
                    break;
                }
                break;
            case BaseCalendar.MONDAY /*2*/:
                if (!gc.isCutoverYear(normalizedYear)) {
                    value = 11;
                    break;
                }
                do {
                    normalizedYear += CE;
                    nextJan1 = gcal.getFixedDate(normalizedYear, CE, CE, null);
                } while (nextJan1 < this.gregorianCutoverDate);
                d2 = (Date) date.clone();
                cal.getCalendarDateFromFixedDate(d2, nextJan1 - 1);
                value = d2.getMonth() - 1;
                break;
            case BaseCalendar.TUESDAY /*3*/:
                if (!gc.isCutoverYear(normalizedYear)) {
                    d = cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d.setDate(date.getYear(), CE, CE);
                    dayOfWeek = cal.getDayOfWeek(d) - getFirstDayOfWeek();
                    if (dayOfWeek < 0) {
                        dayOfWeek += 7;
                    }
                    value = 52;
                    int magic = (getMinimalDaysInFirstWeek() + dayOfWeek) - 1;
                    if (magic == 6 || (date.isLeapYear() && (magic == 5 || magic == 12))) {
                        value = 53;
                        break;
                    }
                }
                if (gc == this) {
                    gc = (GregorianCalendar) gc.clone();
                }
                int maxDayOfYear = getActualMaximum(6);
                gc.set(6, maxDayOfYear);
                value = gc.get(3);
                if (internalGet(CE) != gc.getWeekYear()) {
                    gc.set(6, maxDayOfYear - 7);
                    value = gc.get(3);
                    break;
                }
                break;
            case BaseCalendar.WEDNESDAY /*4*/:
                if (!gc.isCutoverYear(normalizedYear)) {
                    d = cal.newCalendarDate(null);
                    d.setDate(date.getYear(), date.getMonth(), CE);
                    dayOfWeek = cal.getDayOfWeek(d);
                    int monthLength = cal.getMonthLength(d);
                    dayOfWeek -= getFirstDayOfWeek();
                    if (dayOfWeek < 0) {
                        dayOfWeek += 7;
                    }
                    int nDaysFirstWeek = 7 - dayOfWeek;
                    value = 3;
                    if (nDaysFirstWeek >= getMinimalDaysInFirstWeek()) {
                        value = 4;
                    }
                    monthLength -= nDaysFirstWeek + 21;
                    if (monthLength > 0) {
                        value += CE;
                        if (monthLength > 7) {
                            value += CE;
                            break;
                        }
                    }
                }
                if (gc == this) {
                    gc = (GregorianCalendar) gc.clone();
                }
                int y = gc.internalGet(CE);
                int m = gc.internalGet(2);
                while (true) {
                    value = gc.get(4);
                    gc.add(4, CE);
                    if (gc.get(CE) == y && gc.get(2) == m) {
                    }
                }
                break;
            case BaseCalendar.THURSDAY /*5*/:
                value = cal.getMonthLength(date);
                if (gc.isCutoverYear(normalizedYear) && date.getDayOfMonth() != value) {
                    long fd = gc.getCurrentFixedDate();
                    if (fd < this.gregorianCutoverDate) {
                        value = gc.getCalendarDate((gc.getFixedDateMonth1(gc.cdate, fd) + ((long) gc.actualMonthLength())) - 1).getDayOfMonth();
                        break;
                    }
                }
                break;
            case BaseCalendar.JUNE /*6*/:
                if (!gc.isCutoverYear(normalizedYear)) {
                    value = cal.getYearLength(date);
                    break;
                }
                long jan1;
                if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                    jan1 = gc.getCutoverCalendarSystem().getFixedDate(normalizedYear, CE, CE, null);
                } else {
                    if (normalizedYear == this.gregorianCutoverYearJulian) {
                        jan1 = cal.getFixedDate(normalizedYear, CE, CE, null);
                    } else {
                        jan1 = this.gregorianCutoverDate;
                    }
                }
                nextJan1 = gcal.getFixedDate(normalizedYear + CE, CE, CE, null);
                if (nextJan1 < this.gregorianCutoverDate) {
                    nextJan1 = this.gregorianCutoverDate;
                }
                if (!-assertionsDisabled) {
                    if (jan1 <= cal.getFixedDate(date.getNormalizedYear(), date.getMonth(), date.getDayOfMonth(), date)) {
                        obj = CE;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (!-assertionsDisabled) {
                    if ((nextJan1 >= cal.getFixedDate(date.getNormalizedYear(), date.getMonth(), date.getDayOfMonth(), date) ? CE : null) == null) {
                        throw new AssertionError();
                    }
                }
                value = (int) (nextJan1 - jan1);
                break;
            case BaseCalendar.AUGUST /*8*/:
                int ndays;
                int dow1;
                int dow = date.getDayOfWeek();
                if (gc.isCutoverYear(normalizedYear)) {
                    if (gc == this) {
                        gc = (GregorianCalendar) clone();
                    }
                    ndays = gc.actualMonthLength();
                    gc.set(5, gc.getActualMinimum(5));
                    dow1 = gc.get(7);
                } else {
                    d2 = (Date) date.clone();
                    ndays = cal.getMonthLength(d2);
                    d2.setDayOfMonth(CE);
                    cal.normalize(d2);
                    dow1 = d2.getDayOfWeek();
                }
                int x = dow - dow1;
                if (x < 0) {
                    x += 7;
                }
                value = ((ndays - x) + 6) / 7;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(field);
        }
        return value;
    }

    private final long getYearOffsetInMillis() {
        return (((long) internalGet(14)) + ((((((((long) ((internalGet(6) - 1) * 24)) + ((long) internalGet(11))) * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000)) - ((long) (internalGet(15) + internalGet(16)));
    }

    public Object clone() {
        GregorianCalendar other = (GregorianCalendar) super.clone();
        other.gdate = (Date) this.gdate.clone();
        if (this.cdate != null) {
            if (this.cdate != this.gdate) {
                other.cdate = (Date) this.cdate.clone();
            } else {
                other.cdate = other.gdate;
            }
        }
        other.originalFields = null;
        other.zoneOffsets = null;
        return other;
    }

    public TimeZone getTimeZone() {
        TimeZone zone = super.getTimeZone();
        this.gdate.setZone(zone);
        if (!(this.cdate == null || this.cdate == this.gdate)) {
            this.cdate.setZone(zone);
        }
        return zone;
    }

    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        this.gdate.setZone(zone);
        if (this.cdate != null && this.cdate != this.gdate) {
            this.cdate.setZone(zone);
        }
    }

    public final boolean isWeekDateSupported() {
        return true;
    }

    public int getWeekYear() {
        int year = get(CE);
        if (internalGetEra() == 0) {
            year = 1 - year;
        }
        if (year > this.gregorianCutoverYear + CE) {
            int weekOfYear = internalGet(3);
            if (internalGet(2) == 0) {
                if (weekOfYear >= 52) {
                    year--;
                }
            } else if (weekOfYear == CE) {
                year += CE;
            }
            return year;
        }
        int dayOfYear = internalGet(6);
        int maxDayOfYear = getActualMaximum(6);
        int minimalDays = getMinimalDaysInFirstWeek();
        if (dayOfYear > minimalDays && dayOfYear < maxDayOfYear - 6) {
            return year;
        }
        GregorianCalendar cal = (GregorianCalendar) clone();
        cal.setLenient(true);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(6, CE);
        cal.complete();
        int delta = getFirstDayOfWeek() - cal.get(7);
        if (delta != 0) {
            if (delta < 0) {
                delta += 7;
            }
            cal.add(6, delta);
        }
        int minDayOfYear = cal.get(6);
        if (dayOfYear >= minDayOfYear) {
            cal.set(CE, year + CE);
            cal.set(6, CE);
            cal.complete();
            int del = getFirstDayOfWeek() - cal.get(7);
            if (del != 0) {
                if (del < 0) {
                    del += 7;
                }
                cal.add(6, del);
            }
            minDayOfYear = cal.get(6) - 1;
            if (minDayOfYear == 0) {
                minDayOfYear = 7;
            }
            if (minDayOfYear >= minimalDays && (maxDayOfYear - dayOfYear) + CE <= 7 - minDayOfYear) {
                year += CE;
            }
        } else if (minDayOfYear <= minimalDays) {
            year--;
        }
        return year;
    }

    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        if (dayOfWeek < CE || dayOfWeek > 7) {
            throw new IllegalArgumentException("invalid dayOfWeek: " + dayOfWeek);
        }
        GregorianCalendar gc = (GregorianCalendar) clone();
        gc.setLenient(true);
        int era = gc.get(BCE);
        gc.clear();
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        gc.set(BCE, era);
        gc.set(CE, weekYear);
        gc.set(3, CE);
        gc.set(7, getFirstDayOfWeek());
        int days = dayOfWeek - getFirstDayOfWeek();
        if (days < 0) {
            days += 7;
        }
        days += (weekOfYear - 1) * 7;
        if (days != 0) {
            gc.add(6, days);
        } else {
            gc.complete();
        }
        if (isLenient() || (gc.getWeekYear() == weekYear && gc.internalGet(3) == weekOfYear && gc.internalGet(7) == dayOfWeek)) {
            set(BCE, gc.internalGet(BCE));
            set(CE, gc.internalGet(CE));
            set(2, gc.internalGet(2));
            set(5, gc.internalGet(5));
            internalSet(3, weekOfYear);
            complete();
            return;
        }
        throw new IllegalArgumentException();
    }

    public int getWeeksInWeekYear() {
        GregorianCalendar gc = getNormalizedCalendar();
        int weekYear = gc.getWeekYear();
        if (weekYear == gc.internalGet(CE)) {
            return gc.getActualMaximum(3);
        }
        if (gc == this) {
            gc = (GregorianCalendar) gc.clone();
        }
        gc.setWeekDate(weekYear, 2, internalGet(7));
        return gc.getActualMaximum(3);
    }

    private int computeFields(int fieldMask, int tzMask) {
        int year;
        int zoneOffset = BCE;
        TimeZone tz = getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        if (tzMask != 98304) {
            if (tz instanceof ZoneInfo) {
                zoneOffset = ((ZoneInfo) tz).getOffsetsByUtcTime(this.time, this.zoneOffsets);
            } else {
                zoneOffset = tz.getOffset(this.time);
                this.zoneOffsets[BCE] = tz.getRawOffset();
                this.zoneOffsets[CE] = zoneOffset - this.zoneOffsets[BCE];
            }
        }
        if (tzMask != 0) {
            if (Calendar.isFieldSet(tzMask, 15)) {
                this.zoneOffsets[BCE] = internalGet(15);
            }
            if (Calendar.isFieldSet(tzMask, 16)) {
                this.zoneOffsets[CE] = internalGet(16);
            }
            zoneOffset = this.zoneOffsets[BCE] + this.zoneOffsets[CE];
        }
        long fixedDate = (((long) zoneOffset) / ONE_DAY) + (this.time / ONE_DAY);
        int timeOfDay = (zoneOffset % 86400000) + ((int) (this.time % ONE_DAY));
        if (((long) timeOfDay) >= ONE_DAY) {
            timeOfDay = (int) (((long) timeOfDay) - ONE_DAY);
            fixedDate++;
        } else {
            while (timeOfDay < 0) {
                timeOfDay = (int) (((long) timeOfDay) + ONE_DAY);
                fixedDate--;
            }
        }
        fixedDate += 719163;
        int era = CE;
        if (fixedDate >= this.gregorianCutoverDate) {
            if (!-assertionsDisabled) {
                if (!(this.cachedFixedDate != Long.MIN_VALUE ? this.gdate.isNormalized() : true)) {
                    throw new AssertionError((Object) "cache control: not normalized");
                }
            }
            if (!-assertionsDisabled) {
                Object obj;
                if (this.cachedFixedDate != Long.MIN_VALUE) {
                    obj = gcal.getFixedDate(this.gdate.getNormalizedYear(), this.gdate.getMonth(), this.gdate.getDayOfMonth(), this.gdate) == this.cachedFixedDate ? CE : null;
                } else {
                    obj = CE;
                }
                if (obj == null) {
                    throw new AssertionError("cache control: inconsictency, cachedFixedDate=" + this.cachedFixedDate + ", computed=" + gcal.getFixedDate(this.gdate.getNormalizedYear(), this.gdate.getMonth(), this.gdate.getDayOfMonth(), this.gdate) + ", date=" + this.gdate);
                }
            }
            if (fixedDate != this.cachedFixedDate) {
                gcal.getCalendarDateFromFixedDate(this.gdate, fixedDate);
                this.cachedFixedDate = fixedDate;
            }
            year = this.gdate.getYear();
            if (year <= 0) {
                year = 1 - year;
                era = BCE;
            }
            this.calsys = gcal;
            this.cdate = this.gdate;
            if (!-assertionsDisabled) {
                if ((this.cdate.getDayOfWeek() > 0 ? CE : null) == null) {
                    throw new AssertionError("dow=" + this.cdate.getDayOfWeek() + ", date=" + this.cdate);
                }
            }
        }
        this.calsys = getJulianCalendarSystem();
        this.cdate = jcal.newCalendarDate(getZone());
        jcal.getCalendarDateFromFixedDate(this.cdate, fixedDate);
        if (this.cdate.getEra() == jeras[BCE]) {
            era = BCE;
        }
        year = this.cdate.getYear();
        internalSet(BCE, era);
        internalSet(CE, year);
        int mask = fieldMask | 3;
        int month = this.cdate.getMonth() - 1;
        int dayOfMonth = this.cdate.getDayOfMonth();
        if ((fieldMask & 164) != 0) {
            internalSet(2, month);
            internalSet(5, dayOfMonth);
            internalSet(7, this.cdate.getDayOfWeek());
            mask |= 164;
        }
        if ((fieldMask & 32256) != 0) {
            if (timeOfDay != 0) {
                int hours = timeOfDay / ONE_HOUR;
                internalSet(11, hours);
                internalSet(9, hours / 12);
                internalSet(10, hours % 12);
                int r = timeOfDay % ONE_HOUR;
                internalSet(12, r / ONE_MINUTE);
                r %= ONE_MINUTE;
                internalSet(13, r / ONE_SECOND);
                internalSet(14, r % ONE_SECOND);
            } else {
                internalSet(11, BCE);
                internalSet(9, BCE);
                internalSet(10, BCE);
                internalSet(12, BCE);
                internalSet(13, BCE);
                internalSet(14, BCE);
            }
            mask |= 32256;
        }
        if ((98304 & fieldMask) != 0) {
            internalSet(15, this.zoneOffsets[BCE]);
            internalSet(16, this.zoneOffsets[CE]);
            mask |= 98304;
        }
        if ((fieldMask & 344) == 0) {
            return mask;
        }
        int cutoverYear;
        int normalizedYear = this.cdate.getNormalizedYear();
        long fixedDateJan1 = this.calsys.getFixedDate(normalizedYear, CE, CE, this.cdate);
        int dayOfYear = ((int) (fixedDate - fixedDateJan1)) + CE;
        long fixedDateMonth1 = (fixedDate - ((long) dayOfMonth)) + 1;
        if (this.calsys == gcal) {
            cutoverYear = this.gregorianCutoverYear;
        } else {
            cutoverYear = this.gregorianCutoverYearJulian;
        }
        int relativeDayOfMonth = dayOfMonth - 1;
        if (normalizedYear == cutoverYear) {
            if (this.gregorianCutoverYearJulian <= this.gregorianCutoverYear) {
                fixedDateJan1 = getFixedDateJan1(this.cdate, fixedDate);
                if (fixedDate >= this.gregorianCutoverDate) {
                    fixedDateMonth1 = getFixedDateMonth1(this.cdate, fixedDate);
                }
            }
            int realDayOfYear = ((int) (fixedDate - fixedDateJan1)) + CE;
            int cutoverGap = dayOfYear - realDayOfYear;
            dayOfYear = realDayOfYear;
            relativeDayOfMonth = (int) (fixedDate - fixedDateMonth1);
        }
        internalSet(6, dayOfYear);
        internalSet(8, (relativeDayOfMonth / 7) + CE);
        int weekOfYear = getWeekNumber(fixedDateJan1, fixedDate);
        BaseCalendar calForJan1;
        if (weekOfYear == 0) {
            long fixedDec31 = fixedDateJan1 - 1;
            long prevJan1 = fixedDateJan1 - 365;
            if (normalizedYear <= cutoverYear + CE) {
                if (normalizedYear > this.gregorianCutoverYearJulian) {
                    calForJan1 = this.calsys;
                    int prevYear = getCalendarDate(fixedDec31).getNormalizedYear();
                    if (prevYear == this.gregorianCutoverYear) {
                        calForJan1 = getCutoverCalendarSystem();
                        if (calForJan1 == jcal) {
                            prevJan1 = calForJan1.getFixedDate(prevYear, CE, CE, null);
                        } else {
                            prevJan1 = this.gregorianCutoverDate;
                            calForJan1 = gcal;
                        }
                    } else {
                        if (prevYear <= this.gregorianCutoverYearJulian) {
                            prevJan1 = getJulianCalendarSystem().getFixedDate(prevYear, CE, CE, null);
                        }
                    }
                } else if (CalendarUtils.isJulianLeapYear(normalizedYear - 1)) {
                    prevJan1--;
                }
            } else if (CalendarUtils.isGregorianLeapYear(normalizedYear - 1)) {
                prevJan1--;
            }
            weekOfYear = getWeekNumber(prevJan1, fixedDec31);
        } else {
            long nextJan1;
            long nextJan1st;
            if (normalizedYear <= this.gregorianCutoverYear) {
                if (normalizedYear >= this.gregorianCutoverYearJulian - 1) {
                    calForJan1 = this.calsys;
                    int nextYear = normalizedYear + CE;
                    if (nextYear == this.gregorianCutoverYearJulian + CE && nextYear < this.gregorianCutoverYear) {
                        nextYear = this.gregorianCutoverYear;
                    }
                    if (nextYear == this.gregorianCutoverYear) {
                        calForJan1 = getCutoverCalendarSystem();
                    }
                    if (nextYear > this.gregorianCutoverYear || this.gregorianCutoverYearJulian == this.gregorianCutoverYear || nextYear == this.gregorianCutoverYearJulian) {
                        nextJan1 = calForJan1.getFixedDate(nextYear, CE, CE, null);
                    } else {
                        nextJan1 = this.gregorianCutoverDate;
                        calForJan1 = gcal;
                    }
                    nextJan1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan1, getFirstDayOfWeek());
                    if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek() && fixedDate >= nextJan1st - 7) {
                        weekOfYear = CE;
                    }
                }
            }
            if (weekOfYear >= 52) {
                nextJan1 = fixedDateJan1 + 365;
                if (this.cdate.isLeapYear()) {
                    nextJan1++;
                }
                BaseCalendar baseCalendar = this.calsys;
                nextJan1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan1, getFirstDayOfWeek());
                if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek() && fixedDate >= nextJan1st - 7) {
                    weekOfYear = CE;
                }
            }
        }
        internalSet(3, weekOfYear);
        internalSet(4, getWeekNumber(fixedDateMonth1, fixedDate));
        return mask | 344;
    }

    private final int getWeekNumber(long fixedDay1, long fixedDate) {
        Object obj = null;
        Gregorian gregorian = gcal;
        long fixedDay1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + fixedDay1, getFirstDayOfWeek());
        int ndays = (int) (fixedDay1st - fixedDay1);
        if (!-assertionsDisabled) {
            if (ndays <= 7) {
                obj = CE;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (ndays >= getMinimalDaysInFirstWeek()) {
            fixedDay1st -= 7;
        }
        int normalizedDayOfPeriod = (int) (fixedDate - fixedDay1st);
        if (normalizedDayOfPeriod >= 0) {
            return (normalizedDayOfPeriod / 7) + CE;
        }
        return CalendarUtils.floorDivide(normalizedDayOfPeriod, 7) + CE;
    }

    protected void computeTime() {
        int field;
        long timeOfDay;
        if (!isLenient()) {
            if (this.originalFields == null) {
                this.originalFields = new int[17];
            }
            field = BCE;
            while (field < 17) {
                int value = internalGet(field);
                if (!isExternallySet(field) || (value >= getMinimum(field) && value <= getMaximum(field))) {
                    this.originalFields[field] = value;
                    field += CE;
                } else {
                    throw new IllegalArgumentException(Calendar.getFieldName(field));
                }
            }
        }
        int fieldMask = selectFields();
        int year = isSet(CE) ? internalGet(CE) : EPOCH_YEAR;
        int era = internalGetEra();
        if (era == 0) {
            year = 1 - year;
        } else if (era != CE) {
            throw new IllegalArgumentException("Invalid era");
        }
        if (year <= 0 && !isSet(BCE)) {
            fieldMask |= CE;
            setFieldsComputed(CE);
        }
        if (Calendar.isFieldSet(fieldMask, 11)) {
            timeOfDay = 0 + ((long) internalGet(11));
        } else {
            timeOfDay = 0 + ((long) internalGet(10));
            if (Calendar.isFieldSet(fieldMask, 9)) {
                timeOfDay += (long) (internalGet(9) * 12);
            }
        }
        long internalGet = (long) internalGet(13);
        timeOfDay = (((((timeOfDay * 60) + ((long) internalGet(12))) * 60) + r0) * 1000) + ((long) internalGet(14));
        long fixedDate = timeOfDay / ONE_DAY;
        timeOfDay %= ONE_DAY;
        while (timeOfDay < 0) {
            timeOfDay += ONE_DAY;
            fixedDate--;
        }
        long jfd;
        long gfd;
        if (year <= this.gregorianCutoverYear || year <= this.gregorianCutoverYearJulian) {
            if (year >= this.gregorianCutoverYear || year >= this.gregorianCutoverYearJulian) {
                jfd = fixedDate + getFixedDate(getJulianCalendarSystem(), year, fieldMask);
                gfd = fixedDate + getFixedDate(gcal, year, fieldMask);
                if (Calendar.isFieldSet(fieldMask, 6) || Calendar.isFieldSet(fieldMask, 3)) {
                    if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                        fixedDate = jfd;
                    } else {
                        if (year == this.gregorianCutoverYear) {
                            fixedDate = gfd;
                        }
                    }
                }
                if (gfd >= this.gregorianCutoverDate) {
                    if (jfd >= this.gregorianCutoverDate) {
                        fixedDate = gfd;
                    } else {
                        if (this.calsys != gcal || this.calsys == null) {
                            fixedDate = gfd;
                        } else {
                            fixedDate = jfd;
                        }
                    }
                } else {
                    if (jfd >= this.gregorianCutoverDate) {
                        fixedDate = jfd;
                    } else if (isLenient()) {
                        fixedDate = jfd;
                    } else {
                        throw new IllegalArgumentException("the specified date doesn't exist");
                    }
                }
            }
            jfd = fixedDate + getFixedDate(getJulianCalendarSystem(), year, fieldMask);
            if (jfd < this.gregorianCutoverDate) {
                fixedDate = jfd;
            } else {
                gfd = jfd;
                if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                    if (year == this.gregorianCutoverYear) {
                        fixedDate = gfd;
                    }
                    if (gfd >= this.gregorianCutoverDate) {
                        if (jfd >= this.gregorianCutoverDate) {
                            fixedDate = jfd;
                        } else if (isLenient()) {
                            fixedDate = jfd;
                        } else {
                            throw new IllegalArgumentException("the specified date doesn't exist");
                        }
                    }
                    if (jfd >= this.gregorianCutoverDate) {
                        if (this.calsys != gcal) {
                        }
                        fixedDate = gfd;
                    } else {
                        fixedDate = gfd;
                    }
                } else {
                    fixedDate = jfd;
                }
            }
        } else {
            gfd = fixedDate + getFixedDate(gcal, year, fieldMask);
            if (gfd >= this.gregorianCutoverDate) {
                fixedDate = gfd;
            } else {
                jfd = fixedDate + getFixedDate(getJulianCalendarSystem(), year, fieldMask);
                if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                    fixedDate = jfd;
                } else {
                    if (year == this.gregorianCutoverYear) {
                        fixedDate = gfd;
                    }
                    if (gfd >= this.gregorianCutoverDate) {
                        if (jfd >= this.gregorianCutoverDate) {
                            fixedDate = gfd;
                        } else {
                            if (this.calsys != gcal) {
                            }
                            fixedDate = gfd;
                        }
                    } else {
                        if (jfd >= this.gregorianCutoverDate) {
                            fixedDate = jfd;
                        } else if (isLenient()) {
                            throw new IllegalArgumentException("the specified date doesn't exist");
                        } else {
                            fixedDate = jfd;
                        }
                    }
                }
            }
        }
        int tzMask = fieldMask & 98304;
        this.time = adjustForZoneAndDaylightSavingsTime(tzMask, ((fixedDate - 719163) * ONE_DAY) + timeOfDay, getZone());
        int mask = computeFields(getSetStateFields() | fieldMask, tzMask);
        if (!isLenient()) {
            field = BCE;
            while (field < 17) {
                if (isExternallySet(field) && this.originalFields[field] != internalGet(field)) {
                    String s = this.originalFields[field] + " -> " + internalGet(field);
                    System.arraycopy(this.originalFields, BCE, this.fields, BCE, this.fields.length);
                    throw new IllegalArgumentException(Calendar.getFieldName(field) + ": " + s);
                }
                field += CE;
            }
        }
        setFieldsNormalized(mask);
    }

    private long adjustForZoneAndDaylightSavingsTime(int tzMask, long utcTimeInMillis, TimeZone zone) {
        int zoneOffset = BCE;
        int dstOffset = BCE;
        if (tzMask != 98304) {
            if (this.zoneOffsets == null) {
                this.zoneOffsets = new int[2];
            }
            long standardTimeInZone = utcTimeInMillis - ((long) (Calendar.isFieldSet(tzMask, 15) ? internalGet(15) : zone.getRawOffset()));
            if (zone instanceof ZoneInfo) {
                ((ZoneInfo) zone).getOffsetsByUtcTime(standardTimeInZone, this.zoneOffsets);
            } else {
                zone.getOffsets(standardTimeInZone, this.zoneOffsets);
            }
            zoneOffset = this.zoneOffsets[BCE];
            dstOffset = adjustDstOffsetForInvalidWallClock(standardTimeInZone, zone, this.zoneOffsets[CE]);
        }
        if (tzMask != 0) {
            if (Calendar.isFieldSet(tzMask, 15)) {
                zoneOffset = internalGet(15);
            }
            if (Calendar.isFieldSet(tzMask, 16)) {
                dstOffset = internalGet(16);
            }
        }
        return (utcTimeInMillis - ((long) zoneOffset)) - ((long) dstOffset);
    }

    private int adjustDstOffsetForInvalidWallClock(long standardTimeInZone, TimeZone zone, int dstOffset) {
        if (dstOffset == 0 || zone.inDaylightTime(new Date(standardTimeInZone - ((long) dstOffset)))) {
            return dstOffset;
        }
        return BCE;
    }

    private long getFixedDate(BaseCalendar cal, int year, int fieldMask) {
        int month = BCE;
        if (Calendar.isFieldSet(fieldMask, 2)) {
            month = internalGet(2);
            if (month > 11) {
                year += month / 12;
                month %= 12;
            } else if (month < 0) {
                int[] rem = new int[CE];
                year += CalendarUtils.floorDivide(month, 12, rem);
                month = rem[BCE];
            }
        }
        long fixedDate = cal.getFixedDate(year, month + CE, CE, cal == gcal ? this.gdate : null);
        long firstDayOfWeek;
        int dayOfWeek;
        if (!Calendar.isFieldSet(fieldMask, 2)) {
            if (year == this.gregorianCutoverYear && cal == gcal && fixedDate < this.gregorianCutoverDate && this.gregorianCutoverYear != this.gregorianCutoverYearJulian) {
                fixedDate = this.gregorianCutoverDate;
            }
            if (Calendar.isFieldSet(fieldMask, 6)) {
                return (fixedDate + ((long) internalGet(6))) - 1;
            }
            firstDayOfWeek = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + fixedDate, getFirstDayOfWeek());
            if (firstDayOfWeek - fixedDate >= ((long) getMinimalDaysInFirstWeek())) {
                firstDayOfWeek -= 7;
            }
            if (Calendar.isFieldSet(fieldMask, 7)) {
                dayOfWeek = internalGet(7);
                if (dayOfWeek != getFirstDayOfWeek()) {
                    firstDayOfWeek = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + firstDayOfWeek, dayOfWeek);
                }
            }
            return firstDayOfWeek + ((((long) internalGet(3)) - 1) * 7);
        } else if (Calendar.isFieldSet(fieldMask, 5)) {
            return isSet(5) ? (fixedDate + ((long) internalGet(5))) - 1 : fixedDate;
        } else {
            if (Calendar.isFieldSet(fieldMask, 4)) {
                firstDayOfWeek = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + fixedDate, getFirstDayOfWeek());
                if (firstDayOfWeek - fixedDate >= ((long) getMinimalDaysInFirstWeek())) {
                    firstDayOfWeek -= 7;
                }
                if (Calendar.isFieldSet(fieldMask, 7)) {
                    firstDayOfWeek = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + firstDayOfWeek, internalGet(7));
                }
                return firstDayOfWeek + ((long) ((internalGet(4) - 1) * 7));
            }
            int dowim;
            if (Calendar.isFieldSet(fieldMask, 7)) {
                dayOfWeek = internalGet(7);
            } else {
                dayOfWeek = getFirstDayOfWeek();
            }
            if (Calendar.isFieldSet(fieldMask, 8)) {
                dowim = internalGet(8);
            } else {
                dowim = CE;
            }
            if (dowim >= 0) {
                return AbstractCalendar.getDayOfWeekDateOnOrBefore((((long) (dowim * 7)) + fixedDate) - 1, dayOfWeek);
            }
            return AbstractCalendar.getDayOfWeekDateOnOrBefore((((long) (monthLength(month, year) + ((dowim + CE) * 7))) + fixedDate) - 1, dayOfWeek);
        }
    }

    private final GregorianCalendar getNormalizedCalendar() {
        if (isFullyNormalized()) {
            return this;
        }
        GregorianCalendar gc = (GregorianCalendar) clone();
        gc.setLenient(true);
        gc.complete();
        return gc;
    }

    private static final synchronized BaseCalendar getJulianCalendarSystem() {
        BaseCalendar baseCalendar;
        synchronized (GregorianCalendar.class) {
            if (jcal == null) {
                jcal = (JulianCalendar) CalendarSystem.forName("julian");
                jeras = jcal.getEras();
            }
            baseCalendar = jcal;
        }
        return baseCalendar;
    }

    private BaseCalendar getCutoverCalendarSystem() {
        if (this.gregorianCutoverYearJulian < this.gregorianCutoverYear) {
            return gcal;
        }
        return getJulianCalendarSystem();
    }

    private final boolean isCutoverYear(int normalizedYear) {
        return normalizedYear == (this.calsys == gcal ? this.gregorianCutoverYear : this.gregorianCutoverYearJulian) ? true : -assertionsDisabled;
    }

    private final long getFixedDateJan1(Date date, long fixedDate) {
        if (!-assertionsDisabled) {
            int i = date.getNormalizedYear() != this.gregorianCutoverYear ? date.getNormalizedYear() == this.gregorianCutoverYearJulian ? CE : BCE : CE;
            if (i == 0) {
                throw new AssertionError();
            }
        }
        if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian || fixedDate < this.gregorianCutoverDate) {
            return getJulianCalendarSystem().getFixedDate(date.getNormalizedYear(), CE, CE, null);
        }
        return this.gregorianCutoverDate;
    }

    private final long getFixedDateMonth1(Date date, long fixedDate) {
        if (!-assertionsDisabled) {
            int i = date.getNormalizedYear() != this.gregorianCutoverYear ? date.getNormalizedYear() == this.gregorianCutoverYearJulian ? CE : BCE : CE;
            if (i == 0) {
                throw new AssertionError();
            }
        }
        Date gCutover = getGregorianCutoverDate();
        if (gCutover.getMonth() == CE && gCutover.getDayOfMonth() == CE) {
            return (fixedDate - ((long) date.getDayOfMonth())) + 1;
        }
        long fixedDateMonth1;
        if (date.getMonth() == gCutover.getMonth()) {
            Date jLastDate = getLastJulianDate();
            if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian && gCutover.getMonth() == jLastDate.getMonth()) {
                fixedDateMonth1 = jcal.getFixedDate(date.getNormalizedYear(), date.getMonth(), CE, null);
            } else {
                fixedDateMonth1 = this.gregorianCutoverDate;
            }
        } else {
            fixedDateMonth1 = (fixedDate - ((long) date.getDayOfMonth())) + 1;
        }
        return fixedDateMonth1;
    }

    private final Date getCalendarDate(long fd) {
        BaseCalendar cal = fd >= this.gregorianCutoverDate ? gcal : getJulianCalendarSystem();
        Date d = (Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
        cal.getCalendarDateFromFixedDate(d, fd);
        return d;
    }

    private final Date getGregorianCutoverDate() {
        return getCalendarDate(this.gregorianCutoverDate);
    }

    private final Date getLastJulianDate() {
        return getCalendarDate(this.gregorianCutoverDate - 1);
    }

    private final int monthLength(int month, int year) {
        return isLeapYear(year) ? LEAP_MONTH_LENGTH[month] : MONTH_LENGTH[month];
    }

    private final int monthLength(int month) {
        int year = internalGet(CE);
        if (internalGetEra() == 0) {
            year = 1 - year;
        }
        return monthLength(month, year);
    }

    private final int actualMonthLength() {
        int year = this.cdate.getNormalizedYear();
        if (year != this.gregorianCutoverYear && year != this.gregorianCutoverYearJulian) {
            return this.calsys.getMonthLength(this.cdate);
        }
        Date date = (Date) this.cdate.clone();
        long month1 = getFixedDateMonth1(date, this.calsys.getFixedDate(date));
        long next1 = month1 + ((long) this.calsys.getMonthLength(date));
        if (next1 < this.gregorianCutoverDate) {
            return (int) (next1 - month1);
        }
        if (this.cdate != this.gdate) {
            date = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        }
        gcal.getCalendarDateFromFixedDate(date, next1);
        return (int) (getFixedDateMonth1(date, next1) - month1);
    }

    private final int yearLength(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    private final int yearLength() {
        int year = internalGet(CE);
        if (internalGetEra() == 0) {
            year = 1 - year;
        }
        return yearLength(year);
    }

    private final void pinDayOfMonth() {
        int monthLen;
        int year = internalGet(CE);
        if (year > this.gregorianCutoverYear || year < this.gregorianCutoverYearJulian) {
            monthLen = monthLength(internalGet(2));
        } else {
            monthLen = getNormalizedCalendar().getActualMaximum(5);
        }
        if (internalGet(5) > monthLen) {
            set(5, monthLen);
        }
    }

    private final long getCurrentFixedDate() {
        return this.calsys == gcal ? this.cachedFixedDate : this.calsys.getFixedDate(this.cdate);
    }

    private static final int getRolledValue(int value, int amount, int min, int max) {
        Object obj = CE;
        if (!-assertionsDisabled) {
            Object obj2 = (value < min || value > max) ? BCE : CE;
            if (obj2 == null) {
                throw new AssertionError();
            }
        }
        int range = (max - min) + CE;
        int n = value + (amount % range);
        if (n > max) {
            n -= range;
        } else if (n < min) {
            n += range;
        }
        if (!-assertionsDisabled) {
            if (n < min || n > max) {
                obj = BCE;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return n;
    }

    private final int internalGetEra() {
        return isSet(BCE) ? internalGet(BCE) : CE;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.gdate == null) {
            this.gdate = gcal.newCalendarDate(getZone());
            this.cachedFixedDate = Long.MIN_VALUE;
        }
        setGregorianChange(this.gregorianCutover);
    }
}
