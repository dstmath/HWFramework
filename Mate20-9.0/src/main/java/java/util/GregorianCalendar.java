package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Locale;
import libcore.util.ZoneInfo;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.JulianCalendar;

public class GregorianCalendar extends Calendar {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int AD = 1;
    public static final int BC = 0;
    static final int BCE = 0;
    static final int CE = 1;
    static final long DEFAULT_GREGORIAN_CUTOVER = -12219292800000L;
    private static final int EPOCH_OFFSET = 719163;
    private static final int EPOCH_YEAR = 1970;
    static final int[] LEAP_MONTH_LENGTH = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    static final int[] LEAST_MAX_VALUES = {1, 292269054, 11, 52, 4, 28, 365, 7, 4, 1, 11, 23, 59, 59, 999, 50400000, 1200000};
    static final int[] MAX_VALUES = {1, 292278994, 11, 53, 6, 31, 366, 7, 6, 1, 11, 23, 59, 59, 999, 50400000, 7200000};
    static final int[] MIN_VALUES = {0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, -46800000, 0};
    static final int[] MONTH_LENGTH = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final long ONE_DAY = 86400000;
    private static final int ONE_HOUR = 3600000;
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_SECOND = 1000;
    private static final long ONE_WEEK = 604800000;
    private static final Gregorian gcal = CalendarSystem.getGregorianCalendar();
    private static JulianCalendar jcal = null;
    private static Era[] jeras = null;
    static final long serialVersionUID = -8125100834729963327L;
    private transient long cachedFixedDate;
    private transient BaseCalendar calsys;
    private transient BaseCalendar.Date cdate;
    private transient BaseCalendar.Date gdate;
    private long gregorianCutover;
    private transient long gregorianCutoverDate;
    private transient int gregorianCutoverYear;
    private transient int gregorianCutoverYearJulian;
    private transient int[] originalFields;
    private transient int[] zoneOffsets;

    public GregorianCalendar() {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        setZoneShared(true);
    }

    public GregorianCalendar(TimeZone zone) {
        this(zone, Locale.getDefault(Locale.Category.FORMAT));
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
        this(year, month, dayOfMonth, 0, 0, 0, 0);
    }

    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0, 0);
    }

    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        this(year, month, dayOfMonth, hourOfDay, minute, second, 0);
    }

    GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int millis) {
        this.gregorianCutover = DEFAULT_GREGORIAN_CUTOVER;
        this.gregorianCutoverDate = 577736;
        this.gregorianCutoverYear = 1582;
        this.gregorianCutoverYearJulian = 1582;
        this.cachedFixedDate = Long.MIN_VALUE;
        this.gdate = gcal.newCalendarDate(getZone());
        set(1, year);
        set(2, month);
        set(5, dayOfMonth);
        if (hourOfDay < 12 || hourOfDay > 23) {
            internalSet(10, hourOfDay);
        } else {
            internalSet(9, 1);
            internalSet(10, hourOfDay - 12);
        }
        setFieldsComputed(1536);
        set(11, hourOfDay);
        set(12, minute);
        set(13, second);
        internalSet(14, millis);
    }

    GregorianCalendar(TimeZone zone, Locale locale, boolean flag) {
        super(zone, locale);
        this.gregorianCutover = DEFAULT_GREGORIAN_CUTOVER;
        this.gregorianCutoverDate = 577736;
        this.gregorianCutoverYear = 1582;
        this.gregorianCutoverYearJulian = 1582;
        this.cachedFixedDate = Long.MIN_VALUE;
        this.gdate = gcal.newCalendarDate(getZone());
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
        BaseCalendar julianCal = getJulianCalendarSystem();
        BaseCalendar.Date d = (BaseCalendar.Date) julianCal.newCalendarDate(TimeZone.NO_TIMEZONE);
        julianCal.getCalendarDateFromFixedDate(d, this.gregorianCutoverDate - 1);
        this.gregorianCutoverYearJulian = d.getNormalizedYear();
        if (this.time < this.gregorianCutover) {
            setUnnormalized();
        }
    }

    public final Date getGregorianChange() {
        return new Date(this.gregorianCutover);
    }

    public boolean isLeapYear(int year) {
        BaseCalendar.Date d;
        int i = year & 3;
        boolean z = $assertionsDisabled;
        if (i != 0) {
            return $assertionsDisabled;
        }
        boolean z2 = true;
        if (year > this.gregorianCutoverYear) {
            if (year % 100 != 0 || year % HttpURLConnection.HTTP_BAD_REQUEST == 0) {
                z = true;
            }
            return z;
        } else if (year < this.gregorianCutoverYearJulian) {
            return true;
        } else {
            if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                d = getCalendarDate(this.gregorianCutoverDate).getMonth() < 3 ? 1 : null;
            } else {
                d = year == this.gregorianCutoverYear ? 1 : null;
            }
            if (!(d == null || year % 100 != 0 || year % HttpURLConnection.HTTP_BAD_REQUEST == 0)) {
                z2 = false;
            }
            return z2;
        }
    }

    public String getCalendarType() {
        return "gregory";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GregorianCalendar) || !super.equals(obj) || this.gregorianCutover != ((GregorianCalendar) obj).gregorianCutover) {
            return $assertionsDisabled;
        }
        return true;
    }

    public int hashCode() {
        return super.hashCode() ^ ((int) this.gregorianCutoverDate);
    }

    public void add(int field, int amount) {
        int y_amount;
        if (amount != 0) {
            if (field < 0 || field >= 15) {
                throw new IllegalArgumentException();
            }
            complete();
            if (field == 1) {
                int year = internalGet(1);
                if (internalGetEra() == 1) {
                    int year2 = year + amount;
                    if (year2 > 0) {
                        set(1, year2);
                    } else {
                        set(1, 1 - year2);
                        set(0, 0);
                    }
                } else {
                    int year3 = year - amount;
                    if (year3 > 0) {
                        set(1, year3);
                    } else {
                        set(1, 1 - year3);
                        set(0, 1);
                    }
                }
                pinDayOfMonth();
            } else if (field == 2) {
                int month = internalGet(2) + amount;
                int year4 = internalGet(1);
                if (month >= 0) {
                    y_amount = month / 12;
                } else {
                    y_amount = ((month + 1) / 12) - 1;
                }
                if (y_amount != 0) {
                    if (internalGetEra() == 1) {
                        int year5 = year4 + y_amount;
                        if (year5 > 0) {
                            set(1, year5);
                        } else {
                            set(1, 1 - year5);
                            set(0, 0);
                        }
                    } else {
                        int year6 = year4 - y_amount;
                        if (year6 > 0) {
                            set(1, year6);
                        } else {
                            set(1, 1 - year6);
                            set(0, 1);
                        }
                    }
                }
                if (month >= 0) {
                    set(2, month % 12);
                } else {
                    int month2 = month % 12;
                    if (month2 < 0) {
                        month2 += 12;
                    }
                    set(2, 0 + month2);
                }
                pinDayOfMonth();
            } else if (field == 0) {
                int era = internalGet(0) + amount;
                if (era < 0) {
                    era = 0;
                }
                if (era > 1) {
                    era = 1;
                }
                set(0, era);
            } else {
                long delta = (long) amount;
                long timeOfDay = 0;
                switch (field) {
                    case 3:
                    case 4:
                    case 8:
                        delta *= 7;
                        break;
                    case 9:
                        delta = (long) (amount / 2);
                        timeOfDay = (long) ((amount % 2) * 12);
                        break;
                    case 10:
                    case 11:
                        delta *= 3600000;
                        break;
                    case 12:
                        delta *= 60000;
                        break;
                    case 13:
                        delta *= 1000;
                        break;
                }
                if (field >= 10) {
                    setTimeInMillis(this.time + delta);
                    return;
                }
                long fd = getCurrentFixedDate();
                long timeOfDay2 = ((((((timeOfDay + ((long) internalGet(11))) * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000) + ((long) internalGet(14));
                if (timeOfDay2 >= ONE_DAY) {
                    fd++;
                    timeOfDay2 -= ONE_DAY;
                } else if (timeOfDay2 < 0) {
                    fd--;
                    timeOfDay2 += ONE_DAY;
                }
                setTimeInMillis(adjustForZoneAndDaylightSavingsTime(0, (((fd + delta) - 719163) * ONE_DAY) + timeOfDay2, getZone()));
            }
        }
    }

    public void roll(int field, boolean up) {
        roll(field, up ? 1 : -1);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:152:0x03ca, code lost:
        r4 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x03cb, code lost:
        set(r1, getRolledValue(internalGet(r27), r2, r4, r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x03d6, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0199, code lost:
        r4 = r18;
     */
    public void roll(int field, int amount) {
        int min;
        int min2;
        BaseCalendar cal;
        int min3;
        long month1;
        int monthLength;
        int dayOfMonth;
        int min4;
        int max;
        int i = field;
        int amount2 = amount;
        if (amount2 != 0) {
            if (i >= 0 && i < 15) {
                complete();
                int min5 = getMinimum(field);
                int max2 = getMaximum(field);
                switch (i) {
                    case 0:
                    case 1:
                    case 9:
                    case 12:
                    case 13:
                    case 14:
                        min2 = min5;
                        break;
                    case 2:
                        if (!isCutoverYear(this.cdate.getNormalizedYear())) {
                            int mon = (internalGet(2) + amount2) % 12;
                            if (mon < 0) {
                                mon += 12;
                            }
                            set(2, mon);
                            int monthLen = monthLength(mon);
                            if (internalGet(5) > monthLen) {
                                set(5, monthLen);
                            }
                        } else {
                            int yearLength = getActualMaximum(2) + 1;
                            int mon2 = (internalGet(2) + amount2) % yearLength;
                            if (mon2 < 0) {
                                mon2 += yearLength;
                            }
                            set(2, mon2);
                            int monthLen2 = getActualMaximum(5);
                            if (internalGet(5) > monthLen2) {
                                set(5, monthLen2);
                            }
                        }
                        return;
                    case 3:
                        int min6 = min5;
                        int y = this.cdate.getNormalizedYear();
                        int max3 = getActualMaximum(3);
                        set(7, internalGet(7));
                        int woy = internalGet(3);
                        int value = woy + amount2;
                        if (!isCutoverYear(y)) {
                            int weekYear = getWeekYear();
                            if (weekYear == y) {
                                min3 = min6;
                                if (value <= min3 || value >= max3) {
                                    long fd = getCurrentFixedDate();
                                    if (this.calsys.getYearFromFixedDate(fd - ((long) ((woy - min3) * 7))) != y) {
                                        min3++;
                                    }
                                    if (this.calsys.getYearFromFixedDate(fd + ((long) (7 * (max3 - internalGet(3))))) != y) {
                                        max3--;
                                    }
                                } else {
                                    set(3, value);
                                    return;
                                }
                            } else {
                                min3 = min6;
                                if (weekYear > y) {
                                    if (amount2 < 0) {
                                        amount2++;
                                    }
                                    woy = max3;
                                } else {
                                    if (amount2 > 0) {
                                        amount2 -= woy - max3;
                                    }
                                    woy = min3;
                                }
                            }
                            set(i, getRolledValue(woy, amount2, min3, max3));
                            return;
                        }
                        int min7 = min6;
                        long fd2 = getCurrentFixedDate();
                        if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                            cal = getCutoverCalendarSystem();
                        } else if (y == this.gregorianCutoverYear) {
                            cal = gcal;
                        } else {
                            cal = getJulianCalendarSystem();
                        }
                        long day1 = fd2 - ((long) ((woy - min7) * 7));
                        if (cal.getYearFromFixedDate(day1) != y) {
                            min7++;
                        }
                        long fd3 = fd2 + ((long) (7 * (max3 - woy)));
                        if ((fd3 >= this.gregorianCutoverDate ? gcal : getJulianCalendarSystem()).getYearFromFixedDate(fd3) != y) {
                            max3--;
                        }
                        BaseCalendar.Date d = getCalendarDate(((long) ((getRolledValue(woy, amount2, min7, max3) - 1) * 7)) + day1);
                        set(2, d.getMonth() - 1);
                        set(5, d.getDayOfMonth());
                        return;
                    case 4:
                        boolean isCutoverYear = isCutoverYear(this.cdate.getNormalizedYear());
                        int dow = internalGet(7) - getFirstDayOfWeek();
                        if (dow < 0) {
                            dow += 7;
                        }
                        long fd4 = getCurrentFixedDate();
                        if (isCutoverYear) {
                            month1 = getFixedDateMonth1(this.cdate, fd4);
                            monthLength = actualMonthLength();
                        } else {
                            month1 = (fd4 - ((long) internalGet(5))) + 1;
                            monthLength = this.calsys.getMonthLength(this.cdate);
                        }
                        long monthDay1st = BaseCalendar.getDayOfWeekDateOnOrBefore(month1 + 6, getFirstDayOfWeek());
                        if (((int) (monthDay1st - month1)) >= getMinimalDaysInFirstWeek()) {
                            monthDay1st -= 7;
                        }
                        int value2 = getRolledValue(internalGet(field), amount2, 1, getActualMaximum(field)) - 1;
                        int i2 = value2;
                        long j = monthDay1st;
                        long nfd = ((long) (value2 * 7)) + monthDay1st + ((long) dow);
                        if (nfd < month1) {
                            nfd = month1;
                        } else if (nfd >= ((long) monthLength) + month1) {
                            nfd = (((long) monthLength) + month1) - 1;
                        }
                        if (isCutoverYear) {
                            dayOfMonth = getCalendarDate(nfd).getDayOfMonth();
                        } else {
                            dayOfMonth = ((int) (nfd - month1)) + 1;
                        }
                        set(5, dayOfMonth);
                        return;
                    case 5:
                        int min8 = min5;
                        if (!isCutoverYear(this.cdate.getNormalizedYear())) {
                            max2 = this.calsys.getMonthLength(this.cdate);
                            min = min8;
                            break;
                        } else {
                            long fd5 = getCurrentFixedDate();
                            long month12 = getFixedDateMonth1(this.cdate, fd5);
                            set(5, getCalendarDate(((long) getRolledValue((int) (fd5 - month12), amount2, 0, actualMonthLength() - 1)) + month12).getDayOfMonth());
                            return;
                        }
                    case 6:
                        min4 = min5;
                        max = getActualMaximum(field);
                        if (isCutoverYear(this.cdate.getNormalizedYear())) {
                            long fd6 = getCurrentFixedDate();
                            long jan1 = (fd6 - ((long) internalGet(6))) + 1;
                            int min9 = min4;
                            int i3 = min9;
                            BaseCalendar.Date d2 = getCalendarDate((((long) getRolledValue(((int) (fd6 - jan1)) + 1, amount2, min9, max)) + jan1) - 1);
                            set(2, d2.getMonth() - 1);
                            set(5, d2.getDayOfMonth());
                            return;
                        }
                        break;
                    case 7:
                        min4 = min5;
                        if (!isCutoverYear(this.cdate.getNormalizedYear())) {
                            int weekOfYear = internalGet(3);
                            if (weekOfYear > 1 && weekOfYear < 52) {
                                set(3, weekOfYear);
                                max = 7;
                                break;
                            }
                        }
                        int amount3 = amount2 % 7;
                        if (amount3 != 0) {
                            long fd7 = getCurrentFixedDate();
                            long dowFirst = BaseCalendar.getDayOfWeekDateOnOrBefore(fd7, getFirstDayOfWeek());
                            long fd8 = fd7 + ((long) amount3);
                            if (fd8 < dowFirst) {
                                fd8 += 7;
                            } else if (fd8 >= dowFirst + 7) {
                                fd8 -= 7;
                            }
                            BaseCalendar.Date d3 = getCalendarDate(fd8);
                            set(0, d3.getNormalizedYear() <= 0 ? 0 : 1);
                            set(d3.getYear(), d3.getMonth() - 1, d3.getDayOfMonth());
                            return;
                        }
                        return;
                    case 8:
                        min = 1;
                        if (!isCutoverYear(this.cdate.getNormalizedYear())) {
                            int dom = internalGet(5);
                            int monthLength2 = this.calsys.getMonthLength(this.cdate);
                            max2 = monthLength2 / 7;
                            if ((dom - 1) % 7 < monthLength2 % 7) {
                                max2++;
                            }
                            set(7, internalGet(7));
                            break;
                        } else {
                            long fd9 = getCurrentFixedDate();
                            long month13 = getFixedDateMonth1(this.cdate, fd9);
                            int monthLength3 = actualMonthLength();
                            int max4 = monthLength3 / 7;
                            int x = ((int) (fd9 - month13)) % 7;
                            if (x < monthLength3 % 7) {
                                max4++;
                            }
                            int i4 = monthLength3;
                            long fd10 = ((long) ((getRolledValue(internalGet(field), amount2, 1, max4) - 1) * 7)) + month13 + ((long) x);
                            BaseCalendar cal2 = fd10 >= this.gregorianCutoverDate ? gcal : getJulianCalendarSystem();
                            BaseCalendar.Date d4 = (BaseCalendar.Date) cal2.newCalendarDate(TimeZone.NO_TIMEZONE);
                            cal2.getCalendarDateFromFixedDate(d4, fd10);
                            set(5, d4.getDayOfMonth());
                            return;
                        }
                    case 10:
                    case 11:
                        int unit = max2 + 1;
                        int h = internalGet(field);
                        int nh = (h + amount2) % unit;
                        if (nh < 0) {
                            nh += unit;
                        }
                        int i5 = min5;
                        this.time += (long) (ONE_HOUR * (nh - h));
                        CalendarDate d5 = this.calsys.getCalendarDate(this.time, getZone());
                        if (internalGet(5) != d5.getDayOfMonth()) {
                            d5.setDate(internalGet(1), internalGet(2) + 1, internalGet(5));
                            if (i == 10) {
                                d5.addHours(12);
                            }
                            this.time = this.calsys.getTime(d5);
                        }
                        int hourOfDay = d5.getHours();
                        internalSet(i, hourOfDay % unit);
                        if (i == 10) {
                            internalSet(11, hourOfDay);
                        } else {
                            internalSet(9, hourOfDay / 12);
                            internalSet(10, hourOfDay % 12);
                        }
                        int zoneOffset = d5.getZoneOffset();
                        int saving = d5.getDaylightSaving();
                        internalSet(15, zoneOffset - saving);
                        internalSet(16, saving);
                        return;
                    default:
                        min2 = min5;
                        break;
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public int getMinimum(int field) {
        return MIN_VALUES[field];
    }

    public int getMaximum(int field) {
        switch (field) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 8:
                if (this.gregorianCutoverYear <= 200) {
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
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 8:
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v7, resolved type: java.util.GregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v10, resolved type: java.util.GregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v13, resolved type: java.util.GregorianCalendar} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v36, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v16, resolved type: java.util.GregorianCalendar} */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0185, code lost:
        r12 = r8;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    public int getActualMaximum(int field) {
        int value;
        GregorianCalendar gc;
        long nextJan1;
        int value2;
        long jan1;
        int ndays;
        int ndays2;
        int i = field;
        if (((1 << i) & 130689) != 0) {
            return getMaximum(field);
        }
        GregorianCalendar gc2 = getNormalizedCalendar();
        BaseCalendar.Date date = gc2.cdate;
        BaseCalendar cal = gc2.calsys;
        int normalizedYear = date.getNormalizedYear();
        switch (i) {
            case 1:
                if (gc2 == this) {
                    gc2 = clone();
                }
                long current = gc2.getYearOffsetInMillis();
                if (gc2.internalGetEra() == 1) {
                    gc2.setTimeInMillis(Long.MAX_VALUE);
                    int value3 = gc2.get(1);
                    if (current > gc2.getYearOffsetInMillis()) {
                        value3--;
                    }
                    value = value3;
                    gc = gc2;
                } else {
                    CalendarDate d = (gc2.getTimeInMillis() >= this.gregorianCutover ? gcal : getJulianCalendarSystem()).getCalendarDate(Long.MIN_VALUE, getZone());
                    gc = gc2;
                    long maxEnd = ((((((((cal.getDayOfYear(d) - 1) * 24) + ((long) d.getHours())) * 60) + ((long) d.getMinutes())) * 60) + ((long) d.getSeconds())) * 1000) + ((long) d.getMillis());
                    int value4 = d.getYear();
                    if (value4 <= 0) {
                        value4 = 1 - value4;
                    }
                    if (current < maxEnd) {
                        value4--;
                    }
                    value = value4;
                }
                GregorianCalendar gregorianCalendar = gc;
                break;
            case 2:
                if (gc2.isCutoverYear(normalizedYear)) {
                    do {
                        normalizedYear++;
                        nextJan1 = gcal.getFixedDate(normalizedYear, 1, 1, null);
                    } while (nextJan1 < this.gregorianCutoverDate);
                    BaseCalendar.Date d2 = (BaseCalendar.Date) date.clone();
                    cal.getCalendarDateFromFixedDate(d2, nextJan1 - 1);
                    value = d2.getMonth() - 1;
                    break;
                } else {
                    value = 11;
                    break;
                }
            case 3:
                if (gc2.isCutoverYear(normalizedYear)) {
                    if (gc2 == this) {
                        gc2 = gc2.clone();
                    }
                    gc2.set(6, getActualMaximum(6));
                    int value5 = gc2.get(3);
                    if (internalGet(1) == gc2.getWeekYear()) {
                        value = value5;
                        break;
                    } else {
                        gc2.set(6, maxDayOfYear - 7);
                        value = gc2.get(3);
                        break;
                    }
                } else {
                    CalendarDate d3 = cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d3.setDate(date.getYear(), 1, 1);
                    int dayOfWeek = cal.getDayOfWeek(d3) - getFirstDayOfWeek();
                    if (dayOfWeek < 0) {
                        dayOfWeek += 7;
                    }
                    value2 = 52;
                    int magic = (getMinimalDaysInFirstWeek() + dayOfWeek) - 1;
                    if (magic == 6 || (date.isLeapYear() && (magic == 5 || magic == 12))) {
                        value = 52 + 1;
                        break;
                    }
                }
            case 4:
                if (!gc2.isCutoverYear(normalizedYear)) {
                    CalendarDate d4 = cal.newCalendarDate(null);
                    d4.setDate(date.getYear(), date.getMonth(), 1);
                    int dayOfWeek2 = cal.getDayOfWeek(d4);
                    int monthLength = cal.getMonthLength(d4);
                    int dayOfWeek3 = dayOfWeek2 - getFirstDayOfWeek();
                    if (dayOfWeek3 < 0) {
                        dayOfWeek3 += 7;
                    }
                    int nDaysFirstWeek = 7 - dayOfWeek3;
                    int value6 = 3;
                    if (nDaysFirstWeek >= getMinimalDaysInFirstWeek()) {
                        value6 = 3 + 1;
                    }
                    value = value6;
                    int monthLength2 = monthLength - (nDaysFirstWeek + 21);
                    if (monthLength2 > 0) {
                        value++;
                        if (monthLength2 > 7) {
                            value++;
                            break;
                        }
                    }
                } else {
                    if (gc2 == this) {
                        gc2 = gc2.clone();
                    }
                    int y = gc2.internalGet(1);
                    int m = gc2.internalGet(2);
                    do {
                        value2 = gc2.get(4);
                        gc2.add(4, 1);
                        if (gc2.get(1) == y) {
                        }
                    } while (gc2.get(2) == m);
                }
                break;
            case 5:
                value = cal.getMonthLength(date);
                if (gc2.isCutoverYear(normalizedYear) && date.getDayOfMonth() != value) {
                    long fd = gc2.getCurrentFixedDate();
                    if (fd < this.gregorianCutoverDate) {
                        value = gc2.getCalendarDate((gc2.getFixedDateMonth1(gc2.cdate, fd) + ((long) gc2.actualMonthLength())) - 1).getDayOfMonth();
                        break;
                    }
                }
                break;
            case 6:
                if (gc2.isCutoverYear(normalizedYear)) {
                    if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                        jan1 = gc2.getCutoverCalendarSystem().getFixedDate(normalizedYear, 1, 1, null);
                    } else if (normalizedYear == this.gregorianCutoverYearJulian) {
                        jan1 = cal.getFixedDate(normalizedYear, 1, 1, null);
                    } else {
                        jan1 = this.gregorianCutoverDate;
                    }
                    long nextJan12 = gcal.getFixedDate(normalizedYear + 1, 1, 1, null);
                    if (nextJan12 < this.gregorianCutoverDate) {
                        nextJan12 = this.gregorianCutoverDate;
                    }
                    value = (int) (nextJan12 - jan1);
                    break;
                } else {
                    value = cal.getYearLength(date);
                    break;
                }
            case 8:
                int dow = date.getDayOfWeek();
                if (!gc2.isCutoverYear(normalizedYear)) {
                    BaseCalendar.Date d5 = (BaseCalendar.Date) date.clone();
                    ndays = cal.getMonthLength(d5);
                    d5.setDayOfMonth(1);
                    cal.normalize(d5);
                    ndays2 = d5.getDayOfWeek();
                } else {
                    if (gc2 == this) {
                        gc2 = clone();
                    }
                    int ndays3 = gc2.actualMonthLength();
                    gc2.set(5, gc2.getActualMinimum(5));
                    ndays = ndays3;
                    ndays2 = gc2.get(7);
                }
                int x = dow - ndays2;
                if (x < 0) {
                    x += 7;
                }
                value = ((ndays - x) + 6) / 7;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(i);
        }
        return value;
    }

    private long getYearOffsetInMillis() {
        return (((long) internalGet(14)) + ((((((((long) ((internalGet(6) - 1) * 24)) + ((long) internalGet(11))) * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000)) - ((long) (internalGet(15) + internalGet(16)));
    }

    public Object clone() {
        GregorianCalendar other = (GregorianCalendar) super.clone();
        other.gdate = (BaseCalendar.Date) this.gdate.clone();
        if (this.cdate != null) {
            if (this.cdate != this.gdate) {
                other.cdate = (BaseCalendar.Date) this.cdate.clone();
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
        int year = get(1);
        if (internalGetEra() == 0) {
            year = 1 - year;
        }
        if (year > this.gregorianCutoverYear + 1) {
            int weekOfYear = internalGet(3);
            if (internalGet(2) == 0) {
                if (weekOfYear >= 52) {
                    year--;
                }
            } else if (weekOfYear == 1) {
                year++;
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
        cal.set(6, 1);
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
            cal.set(1, year + 1);
            cal.set(6, 1);
            cal.complete();
            int del = getFirstDayOfWeek() - cal.get(7);
            if (del != 0) {
                if (del < 0) {
                    del += 7;
                }
                cal.add(6, del);
            }
            int minDayOfYear2 = cal.get(6) - 1;
            if (minDayOfYear2 == 0) {
                minDayOfYear2 = 7;
            }
            int minDayOfYear3 = minDayOfYear2;
            if (minDayOfYear3 >= minimalDays && (maxDayOfYear - dayOfYear) + 1 <= 7 - minDayOfYear3) {
                year++;
            }
        } else if (minDayOfYear <= minimalDays) {
            year--;
        }
        return year;
    }

    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("invalid dayOfWeek: " + dayOfWeek);
        }
        GregorianCalendar gc = (GregorianCalendar) clone();
        gc.setLenient(true);
        int era = gc.get(0);
        gc.clear();
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        gc.set(0, era);
        gc.set(1, weekYear);
        gc.set(3, 1);
        gc.set(7, getFirstDayOfWeek());
        int days = dayOfWeek - getFirstDayOfWeek();
        if (days < 0) {
            days += 7;
        }
        int days2 = days + ((weekOfYear - 1) * 7);
        if (days2 != 0) {
            gc.add(6, days2);
        } else {
            gc.complete();
        }
        if (isLenient() || (gc.getWeekYear() == weekYear && gc.internalGet(3) == weekOfYear && gc.internalGet(7) == dayOfWeek)) {
            set(0, gc.internalGet(0));
            set(1, gc.internalGet(1));
            set(2, gc.internalGet(2));
            set(5, gc.internalGet(5));
            internalSet(3, weekOfYear);
            complete();
            return;
        }
        throw new IllegalArgumentException();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.util.GregorianCalendar} */
    /* JADX WARNING: Multi-variable type inference failed */
    public int getWeeksInWeekYear() {
        GregorianCalendar gc = getNormalizedCalendar();
        int weekYear = gc.getWeekYear();
        if (weekYear == gc.internalGet(1)) {
            return gc.getActualMaximum(3);
        }
        if (gc == this) {
            gc = gc.clone();
        }
        gc.setWeekDate(weekYear, 2, internalGet(7));
        return gc.getActualMaximum(3);
    }

    /* access modifiers changed from: protected */
    public void computeFields() {
        int mask;
        if (isPartiallyNormalized()) {
            mask = getSetStateFields();
            int fieldMask = (~mask) & 131071;
            if (fieldMask != 0 || this.calsys == null) {
                mask |= computeFields(fieldMask, 98304 & mask);
            }
        } else {
            mask = 131071;
            computeFields(131071, 0);
        }
        setFieldsComputed(mask);
    }

    private int computeFields(int fieldMask, int tzMask) {
        int timeOfDay;
        int year;
        int relativeDayOfMonth;
        long fixedDateJan1;
        int mask;
        long nextJan1;
        long prevJan1;
        long prevJan12;
        int i = fieldMask;
        int i2 = tzMask;
        int zoneOffset = 0;
        ZoneInfo zoneInfo = getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        if (i2 != 98304) {
            if (zoneInfo instanceof ZoneInfo) {
                zoneOffset = zoneInfo.getOffsetsByUtcTime(this.time, this.zoneOffsets);
            } else {
                zoneOffset = zoneInfo.getOffset(this.time);
                this.zoneOffsets[0] = zoneInfo.getRawOffset();
                this.zoneOffsets[1] = zoneOffset - this.zoneOffsets[0];
            }
        }
        if (i2 != 0) {
            if (isFieldSet(i2, 15)) {
                this.zoneOffsets[0] = internalGet(15);
            }
            if (isFieldSet(i2, 16)) {
                this.zoneOffsets[1] = internalGet(16);
            }
            zoneOffset = this.zoneOffsets[0] + this.zoneOffsets[1];
        }
        long fixedDate = (((long) zoneOffset) / ONE_DAY) + (this.time / ONE_DAY);
        int timeOfDay2 = (zoneOffset % 86400000) + ((int) (this.time % ONE_DAY));
        if (((long) timeOfDay2) >= ONE_DAY) {
            timeOfDay = (int) (((long) timeOfDay2) - ONE_DAY);
            fixedDate++;
        } else {
            timeOfDay = timeOfDay2;
            while (timeOfDay < 0) {
                timeOfDay = (int) (((long) timeOfDay) + ONE_DAY);
                fixedDate--;
            }
        }
        long fixedDate2 = fixedDate + 719163;
        int era = 1;
        if (fixedDate2 >= this.gregorianCutoverDate) {
            if (fixedDate2 != this.cachedFixedDate) {
                gcal.getCalendarDateFromFixedDate(this.gdate, fixedDate2);
                this.cachedFixedDate = fixedDate2;
            }
            year = this.gdate.getYear();
            if (year <= 0) {
                year = 1 - year;
                era = 0;
            }
            this.calsys = gcal;
            this.cdate = this.gdate;
        } else {
            this.calsys = getJulianCalendarSystem();
            this.cdate = jcal.newCalendarDate(getZone());
            jcal.getCalendarDateFromFixedDate(this.cdate, fixedDate2);
            if (this.cdate.getEra() == jeras[0]) {
                era = 0;
            }
            year = this.cdate.getYear();
        }
        internalSet(0, era);
        internalSet(1, year);
        int mask2 = i | 3;
        int month = this.cdate.getMonth() - 1;
        int dayOfMonth = this.cdate.getDayOfMonth();
        if ((i & 164) != 0) {
            internalSet(2, month);
            internalSet(5, dayOfMonth);
            internalSet(7, this.cdate.getDayOfWeek());
            mask2 |= 164;
        }
        if ((i & 32256) != 0) {
            if (timeOfDay != 0) {
                int hours = timeOfDay / ONE_HOUR;
                internalSet(11, hours);
                internalSet(9, hours / 12);
                internalSet(10, hours % 12);
                int r = timeOfDay % ONE_HOUR;
                internalSet(12, r / ONE_MINUTE);
                int r2 = r % ONE_MINUTE;
                internalSet(13, r2 / 1000);
                internalSet(14, r2 % 1000);
            } else {
                internalSet(11, 0);
                internalSet(9, 0);
                internalSet(10, 0);
                internalSet(12, 0);
                internalSet(13, 0);
                internalSet(14, 0);
            }
            mask2 |= 32256;
        }
        if ((i & 98304) != 0) {
            internalSet(15, this.zoneOffsets[0]);
            internalSet(16, this.zoneOffsets[1]);
            mask2 |= 98304;
        }
        if ((i & 344) != 0) {
            int normalizedYear = this.cdate.getNormalizedYear();
            long fixedDateMonth1 = this.calsys.getFixedDate(normalizedYear, 1, 1, this.cdate);
            int i3 = zoneOffset;
            TimeZone timeZone = zoneInfo;
            int dayOfYear = ((int) (fixedDate2 - fixedDateMonth1)) + 1;
            int i4 = era;
            int cutoverGap = 0;
            long fixedDateMonth12 = (fixedDate2 - ((long) dayOfMonth)) + 1;
            int cutoverYear = this.calsys == gcal ? this.gregorianCutoverYear : this.gregorianCutoverYearJulian;
            int relativeDayOfMonth2 = dayOfMonth - 1;
            if (normalizedYear == cutoverYear) {
                int i5 = relativeDayOfMonth2;
                if (this.gregorianCutoverYearJulian <= this.gregorianCutoverYear) {
                    long fixedDateJan12 = getFixedDateJan1(this.cdate, fixedDate2);
                    if (fixedDate2 >= this.gregorianCutoverDate) {
                        long j = fixedDateJan12;
                        fixedDateJan1 = getFixedDateMonth1(this.cdate, fixedDate2);
                        fixedDateMonth1 = j;
                        int i6 = month;
                        int i7 = dayOfMonth;
                        int realDayOfYear = ((int) (fixedDate2 - fixedDateMonth1)) + 1;
                        cutoverGap = dayOfYear - realDayOfYear;
                        dayOfYear = realDayOfYear;
                        int i8 = year;
                        relativeDayOfMonth = (int) (fixedDate2 - fixedDateJan1);
                        fixedDateMonth1 = fixedDateMonth1;
                    } else {
                        fixedDateMonth1 = fixedDateJan12;
                    }
                }
                fixedDateJan1 = fixedDateMonth12;
                int i62 = month;
                int i72 = dayOfMonth;
                int realDayOfYear2 = ((int) (fixedDate2 - fixedDateMonth1)) + 1;
                cutoverGap = dayOfYear - realDayOfYear2;
                dayOfYear = realDayOfYear2;
                int i82 = year;
                relativeDayOfMonth = (int) (fixedDate2 - fixedDateJan1);
                fixedDateMonth1 = fixedDateMonth1;
            } else {
                relativeDayOfMonth = relativeDayOfMonth2;
                int i9 = year;
                int i10 = month;
                int i11 = dayOfMonth;
                fixedDateJan1 = fixedDateMonth12;
            }
            internalSet(6, dayOfYear);
            internalSet(8, (relativeDayOfMonth / 7) + 1);
            int weekOfYear = getWeekNumber(fixedDateMonth1, fixedDate2);
            if (weekOfYear == 0) {
                long fixedDec31 = fixedDateMonth1 - 1;
                long prevJan13 = fixedDateMonth1 - 365;
                int i12 = dayOfYear;
                if (normalizedYear > cutoverYear + 1) {
                    if (CalendarUtils.isGregorianLeapYear(normalizedYear - 1)) {
                        prevJan12 = prevJan13 - 1;
                    }
                    int i13 = timeOfDay;
                    mask = mask2;
                    int i14 = cutoverGap;
                    prevJan1 = prevJan13;
                    weekOfYear = getWeekNumber(prevJan1, fixedDec31);
                    int i15 = normalizedYear;
                } else if (normalizedYear <= this.gregorianCutoverYearJulian) {
                    if (CalendarUtils.isJulianLeapYear(normalizedYear - 1)) {
                        prevJan12 = prevJan13 - 1;
                    }
                    int i132 = timeOfDay;
                    mask = mask2;
                    int i142 = cutoverGap;
                    prevJan1 = prevJan13;
                    weekOfYear = getWeekNumber(prevJan1, fixedDec31);
                    int i152 = normalizedYear;
                } else {
                    BaseCalendar calForJan1 = this.calsys;
                    int i16 = cutoverYear;
                    int prevYear = getCalendarDate(fixedDec31).getNormalizedYear();
                    BaseCalendar baseCalendar = calForJan1;
                    if (prevYear == this.gregorianCutoverYear) {
                        BaseCalendar calForJan12 = getCutoverCalendarSystem();
                        int i17 = timeOfDay;
                        if (calForJan12 == jcal) {
                            int i18 = cutoverGap;
                            prevJan12 = calForJan12.getFixedDate(prevYear, 1, 1, null);
                            mask = mask2;
                        } else {
                            mask = mask2;
                            prevJan1 = this.gregorianCutoverDate;
                            BaseCalendar calForJan13 = gcal;
                            weekOfYear = getWeekNumber(prevJan1, fixedDec31);
                            int i1522 = normalizedYear;
                        }
                    } else {
                        mask = mask2;
                        int i19 = cutoverGap;
                        if (prevYear <= this.gregorianCutoverYearJulian) {
                            prevJan12 = getJulianCalendarSystem().getFixedDate(prevYear, 1, 1, null);
                        }
                        prevJan1 = prevJan13;
                        weekOfYear = getWeekNumber(prevJan1, fixedDec31);
                        int i15222 = normalizedYear;
                    }
                    prevJan1 = prevJan12;
                    weekOfYear = getWeekNumber(prevJan1, fixedDec31);
                    int i152222 = normalizedYear;
                }
                mask = mask2;
                prevJan1 = prevJan12;
                weekOfYear = getWeekNumber(prevJan1, fixedDec31);
                int i1522222 = normalizedYear;
            } else {
                int i20 = dayOfYear;
                int i21 = timeOfDay;
                mask = mask2;
                int i22 = cutoverGap;
                if (normalizedYear > this.gregorianCutoverYear) {
                } else if (normalizedYear < this.gregorianCutoverYearJulian - 1) {
                    int i23 = normalizedYear;
                } else {
                    BaseCalendar calForJan14 = this.calsys;
                    int nextYear = normalizedYear + 1;
                    if (nextYear == this.gregorianCutoverYearJulian + 1 && nextYear < this.gregorianCutoverYear) {
                        nextYear = this.gregorianCutoverYear;
                    }
                    if (nextYear == this.gregorianCutoverYear) {
                        calForJan14 = getCutoverCalendarSystem();
                    }
                    if (nextYear > this.gregorianCutoverYear || this.gregorianCutoverYearJulian == this.gregorianCutoverYear || nextYear == this.gregorianCutoverYearJulian) {
                        nextJan1 = calForJan14.getFixedDate(nextYear, 1, 1, null);
                    } else {
                        nextJan1 = this.gregorianCutoverDate;
                        calForJan14 = gcal;
                    }
                    BaseCalendar baseCalendar2 = calForJan14;
                    int i24 = normalizedYear;
                    long nextJan1st = BaseCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6, getFirstDayOfWeek());
                    int i25 = nextYear;
                    if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek() && fixedDate2 >= nextJan1st - 7) {
                        weekOfYear = 1;
                    }
                }
                if (weekOfYear >= 52) {
                    long nextJan12 = 365 + fixedDateMonth1;
                    if (this.cdate.isLeapYear()) {
                        nextJan12++;
                    }
                    long nextJan1st2 = BaseCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan12, getFirstDayOfWeek());
                    if (((int) (nextJan1st2 - nextJan12)) >= getMinimalDaysInFirstWeek() && fixedDate2 >= nextJan1st2 - 7) {
                        weekOfYear = 1;
                    }
                }
            }
            internalSet(3, weekOfYear);
            internalSet(4, getWeekNumber(fixedDateJan1, fixedDate2));
            return mask | 344;
        }
        TimeZone timeZone2 = zoneInfo;
        int i26 = era;
        int i27 = year;
        int i28 = timeOfDay;
        int i29 = month;
        int i30 = dayOfMonth;
        return mask2;
    }

    private int getWeekNumber(long fixedDay1, long fixedDate) {
        long fixedDay1st = Gregorian.getDayOfWeekDateOnOrBefore(6 + fixedDay1, getFirstDayOfWeek());
        if (((int) (fixedDay1st - fixedDay1)) >= getMinimalDaysInFirstWeek()) {
            fixedDay1st -= 7;
        }
        int normalizedDayOfPeriod = (int) (fixedDate - fixedDay1st);
        if (normalizedDayOfPeriod >= 0) {
            return (normalizedDayOfPeriod / 7) + 1;
        }
        return CalendarUtils.floorDivide(normalizedDayOfPeriod, 7) + 1;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0126  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0128  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0183  */
    public void computeTime() {
        long timeOfDay;
        long fixedDate;
        long gfd;
        long fixedDate2;
        long fixedDate3;
        if (!isLenient()) {
            if (this.originalFields == null) {
                this.originalFields = new int[17];
            }
            int field = 0;
            while (field < 17) {
                int value = internalGet(field);
                if (!isExternallySet(field) || (value >= getMinimum(field) && value <= getMaximum(field))) {
                    this.originalFields[field] = value;
                    field++;
                } else {
                    throw new IllegalArgumentException(getFieldName(field));
                }
            }
        }
        int fieldMask = selectFields();
        int year = isSet(1) ? internalGet(1) : EPOCH_YEAR;
        int era = internalGetEra();
        if (era == 0) {
            year = 1 - year;
        } else if (era != 1) {
            throw new IllegalArgumentException("Invalid era");
        }
        if (year <= 0 && !isSet(0)) {
            fieldMask |= 1;
            setFieldsComputed(1);
        }
        if (isFieldSet(fieldMask, 11)) {
            timeOfDay = 0 + ((long) internalGet(11));
        } else {
            timeOfDay = 0 + ((long) internalGet(10));
            if (isFieldSet(fieldMask, 9)) {
                timeOfDay += (long) (internalGet(9) * 12);
            }
        }
        long timeOfDay2 = (((((timeOfDay * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000) + ((long) internalGet(14));
        long fixedDate4 = timeOfDay2 / ONE_DAY;
        long timeOfDay3 = timeOfDay2 % ONE_DAY;
        while (timeOfDay3 < 0) {
            timeOfDay3 += ONE_DAY;
            fixedDate4--;
        }
        if (year > this.gregorianCutoverYear && year > this.gregorianCutoverYearJulian) {
            gfd = getFixedDate(gcal, year, fieldMask) + fixedDate4;
            if (gfd >= this.gregorianCutoverDate) {
                fixedDate2 = gfd;
            } else {
                fixedDate2 = getFixedDate(getJulianCalendarSystem(), year, fieldMask) + fixedDate4;
                if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                }
                fixedDate = fixedDate3;
                int tzMask = 98304 & fieldMask;
                long millis = adjustForZoneAndDaylightSavingsTime(tzMask, ((fixedDate - 719163) * ONE_DAY) + timeOfDay3, getZone());
                this.time = millis;
                int mask = computeFields(getSetStateFields() | fieldMask, tzMask);
                if (!isLenient()) {
                }
                long j = millis;
                setFieldsNormalized(mask);
            }
        } else if (year >= this.gregorianCutoverYear || year >= this.gregorianCutoverYearJulian) {
            fixedDate2 = getFixedDate(getJulianCalendarSystem(), year, fieldMask) + fixedDate4;
            gfd = getFixedDate(gcal, year, fieldMask) + fixedDate4;
            if (isFieldSet(fieldMask, 6) || isFieldSet(fieldMask, 3)) {
                if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                    fixedDate3 = fixedDate2;
                } else if (year == this.gregorianCutoverYear) {
                    fixedDate3 = gfd;
                }
                fixedDate = fixedDate3;
                int tzMask2 = 98304 & fieldMask;
                long millis2 = adjustForZoneAndDaylightSavingsTime(tzMask2, ((fixedDate - 719163) * ONE_DAY) + timeOfDay3, getZone());
                this.time = millis2;
                int mask2 = computeFields(getSetStateFields() | fieldMask, tzMask2);
                if (!isLenient()) {
                    for (int field2 = 0; field2 < 17; field2++) {
                        if (isExternallySet(field2) && this.originalFields[field2] != internalGet(field2)) {
                            String s = this.originalFields[field2] + " -> " + internalGet(field2);
                            int i = fieldMask;
                            long j2 = millis2;
                            System.arraycopy((Object) this.originalFields, 0, (Object) this.fields, 0, this.fields.length);
                            throw new IllegalArgumentException(getFieldName(field2) + ": " + s);
                        }
                    }
                }
                long j3 = millis2;
                setFieldsNormalized(mask2);
            }
            if (gfd >= this.gregorianCutoverDate) {
                fixedDate = fixedDate2 >= this.gregorianCutoverDate ? gfd : (this.calsys == gcal || this.calsys == null) ? gfd : fixedDate2;
            } else if (fixedDate2 < this.gregorianCutoverDate) {
                fixedDate = fixedDate2;
            } else if (!isLenient()) {
                throw new IllegalArgumentException("the specified date doesn't exist");
            }
            int tzMask22 = 98304 & fieldMask;
            long millis22 = adjustForZoneAndDaylightSavingsTime(tzMask22, ((fixedDate - 719163) * ONE_DAY) + timeOfDay3, getZone());
            this.time = millis22;
            int mask22 = computeFields(getSetStateFields() | fieldMask, tzMask22);
            if (!isLenient()) {
            }
            long j32 = millis22;
            setFieldsNormalized(mask22);
        } else {
            fixedDate2 = getFixedDate(getJulianCalendarSystem(), year, fieldMask) + fixedDate4;
            if (fixedDate2 < this.gregorianCutoverDate) {
                fixedDate3 = fixedDate2;
                fixedDate = fixedDate3;
                int tzMask222 = 98304 & fieldMask;
                long millis222 = adjustForZoneAndDaylightSavingsTime(tzMask222, ((fixedDate - 719163) * ONE_DAY) + timeOfDay3, getZone());
                this.time = millis222;
                int mask222 = computeFields(getSetStateFields() | fieldMask, tzMask222);
                if (!isLenient()) {
                }
                long j322 = millis222;
                setFieldsNormalized(mask222);
            }
            gfd = fixedDate2;
            if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
            }
            fixedDate = fixedDate3;
            int tzMask2222 = 98304 & fieldMask;
            long millis2222 = adjustForZoneAndDaylightSavingsTime(tzMask2222, ((fixedDate - 719163) * ONE_DAY) + timeOfDay3, getZone());
            this.time = millis2222;
            int mask2222 = computeFields(getSetStateFields() | fieldMask, tzMask2222);
            if (!isLenient()) {
            }
            long j3222 = millis2222;
            setFieldsNormalized(mask2222);
        }
        fixedDate = fixedDate2;
        int tzMask22222 = 98304 & fieldMask;
        long millis22222 = adjustForZoneAndDaylightSavingsTime(tzMask22222, ((fixedDate - 719163) * ONE_DAY) + timeOfDay3, getZone());
        this.time = millis22222;
        int mask22222 = computeFields(getSetStateFields() | fieldMask, tzMask22222);
        if (!isLenient()) {
        }
        long j32222 = millis22222;
        setFieldsNormalized(mask22222);
    }

    private long adjustForZoneAndDaylightSavingsTime(int tzMask, long utcTimeInMillis, TimeZone zone) {
        int zoneOffset = 0;
        int dstOffset = 0;
        if (tzMask != 98304) {
            if (this.zoneOffsets == null) {
                this.zoneOffsets = new int[2];
            }
            long standardTimeInZone = utcTimeInMillis - ((long) (isFieldSet(tzMask, 15) ? internalGet(15) : zone.getRawOffset()));
            if (zone instanceof ZoneInfo) {
                ((ZoneInfo) zone).getOffsetsByUtcTime(standardTimeInZone, this.zoneOffsets);
            } else {
                zone.getOffsets(standardTimeInZone, this.zoneOffsets);
            }
            zoneOffset = this.zoneOffsets[0];
            dstOffset = adjustDstOffsetForInvalidWallClock(standardTimeInZone, zone, this.zoneOffsets[1]);
        }
        if (tzMask != 0) {
            if (isFieldSet(tzMask, 15)) {
                zoneOffset = internalGet(15);
            }
            if (isFieldSet(tzMask, 16)) {
                dstOffset = internalGet(16);
            }
        }
        return (utcTimeInMillis - ((long) zoneOffset)) - ((long) dstOffset);
    }

    private int adjustDstOffsetForInvalidWallClock(long standardTimeInZone, TimeZone zone, int dstOffset) {
        if (dstOffset == 0 || zone.inDaylightTime(new Date(standardTimeInZone - ((long) dstOffset)))) {
            return dstOffset;
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00d5  */
    private long getFixedDate(BaseCalendar cal, int year, int fieldMask) {
        int year2;
        int dayOfWeek;
        BaseCalendar baseCalendar = cal;
        int i = fieldMask;
        int month = 0;
        int dowim = 1;
        if (isFieldSet(i, 2)) {
            month = internalGet(2);
            if (month > 11) {
                year2 = year + (month / 12);
                month %= 12;
            } else if (month < 0) {
                int[] rem = new int[1];
                year2 = year + CalendarUtils.floorDivide(month, 12, rem);
                month = rem[0];
            }
            long fixedDate = baseCalendar.getFixedDate(year2, month + 1, 1, baseCalendar != gcal ? this.gdate : null);
            if (isFieldSet(i, 2)) {
                if (year2 == this.gregorianCutoverYear && baseCalendar == gcal && fixedDate < this.gregorianCutoverDate && this.gregorianCutoverYear != this.gregorianCutoverYearJulian) {
                    fixedDate = this.gregorianCutoverDate;
                }
                if (isFieldSet(i, 6)) {
                    return (fixedDate + ((long) internalGet(6))) - 1;
                }
                long firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6, getFirstDayOfWeek());
                if (firstDayOfWeek - fixedDate >= ((long) getMinimalDaysInFirstWeek())) {
                    firstDayOfWeek -= 7;
                }
                if (isFieldSet(i, 7)) {
                    int dayOfWeek2 = internalGet(7);
                    if (dayOfWeek2 != getFirstDayOfWeek()) {
                        firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(6 + firstDayOfWeek, dayOfWeek2);
                    }
                }
                return firstDayOfWeek + (7 * (((long) internalGet(3)) - 1));
            } else if (isFieldSet(i, 5)) {
                if (isSet(5)) {
                    return (fixedDate + ((long) internalGet(5))) - 1;
                }
                return fixedDate;
            } else if (isFieldSet(i, 4)) {
                long firstDayOfWeek2 = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6, getFirstDayOfWeek());
                if (firstDayOfWeek2 - fixedDate >= ((long) getMinimalDaysInFirstWeek())) {
                    firstDayOfWeek2 -= 7;
                }
                if (isFieldSet(i, 7)) {
                    firstDayOfWeek2 = BaseCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek2 + 6, internalGet(7));
                }
                return firstDayOfWeek2 + ((long) (7 * (internalGet(4) - 1)));
            } else {
                if (isFieldSet(i, 7)) {
                    dayOfWeek = internalGet(7);
                } else {
                    dayOfWeek = getFirstDayOfWeek();
                }
                if (isFieldSet(i, 8)) {
                    dowim = internalGet(8);
                }
                if (dowim >= 0) {
                    return BaseCalendar.getDayOfWeekDateOnOrBefore((((long) (7 * dowim)) + fixedDate) - 1, dayOfWeek);
                }
                return BaseCalendar.getDayOfWeekDateOnOrBefore((((long) (monthLength(month, year2) + (7 * (dowim + 1)))) + fixedDate) - 1, dayOfWeek);
            }
        }
        year2 = year;
        long fixedDate2 = baseCalendar.getFixedDate(year2, month + 1, 1, baseCalendar != gcal ? this.gdate : null);
        if (isFieldSet(i, 2)) {
        }
    }

    private GregorianCalendar getNormalizedCalendar() {
        if (isFullyNormalized()) {
            return this;
        }
        GregorianCalendar gc = (GregorianCalendar) clone();
        gc.setLenient(true);
        gc.complete();
        return gc;
    }

    private static synchronized BaseCalendar getJulianCalendarSystem() {
        JulianCalendar julianCalendar;
        synchronized (GregorianCalendar.class) {
            if (jcal == null) {
                jcal = (JulianCalendar) CalendarSystem.forName("julian");
                jeras = jcal.getEras();
            }
            julianCalendar = jcal;
        }
        return julianCalendar;
    }

    private BaseCalendar getCutoverCalendarSystem() {
        if (this.gregorianCutoverYearJulian < this.gregorianCutoverYear) {
            return gcal;
        }
        return getJulianCalendarSystem();
    }

    private boolean isCutoverYear(int normalizedYear) {
        if (normalizedYear == (this.calsys == gcal ? this.gregorianCutoverYear : this.gregorianCutoverYearJulian)) {
            return true;
        }
        return $assertionsDisabled;
    }

    private long getFixedDateJan1(BaseCalendar.Date date, long fixedDate) {
        if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian || fixedDate < this.gregorianCutoverDate) {
            return getJulianCalendarSystem().getFixedDate(date.getNormalizedYear(), 1, 1, null);
        }
        return this.gregorianCutoverDate;
    }

    private long getFixedDateMonth1(BaseCalendar.Date date, long fixedDate) {
        long fixedDateMonth1;
        long fixedDateMonth12;
        BaseCalendar.Date gCutover = getGregorianCutoverDate();
        if (gCutover.getMonth() == 1 && gCutover.getDayOfMonth() == 1) {
            return (fixedDate - ((long) date.getDayOfMonth())) + 1;
        }
        if (date.getMonth() == gCutover.getMonth()) {
            BaseCalendar.Date jLastDate = getLastJulianDate();
            if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian && gCutover.getMonth() == jLastDate.getMonth()) {
                fixedDateMonth12 = jcal.getFixedDate(date.getNormalizedYear(), date.getMonth(), 1, null);
            } else {
                fixedDateMonth12 = this.gregorianCutoverDate;
            }
            fixedDateMonth1 = fixedDateMonth12;
        } else {
            fixedDateMonth1 = (fixedDate - ((long) date.getDayOfMonth())) + 1;
        }
        return fixedDateMonth1;
    }

    private BaseCalendar.Date getCalendarDate(long fd) {
        BaseCalendar cal = fd >= this.gregorianCutoverDate ? gcal : getJulianCalendarSystem();
        BaseCalendar.Date d = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
        cal.getCalendarDateFromFixedDate(d, fd);
        return d;
    }

    private BaseCalendar.Date getGregorianCutoverDate() {
        return getCalendarDate(this.gregorianCutoverDate);
    }

    private BaseCalendar.Date getLastJulianDate() {
        return getCalendarDate(this.gregorianCutoverDate - 1);
    }

    private int monthLength(int month, int year) {
        return isLeapYear(year) ? LEAP_MONTH_LENGTH[month] : MONTH_LENGTH[month];
    }

    private int monthLength(int month) {
        int year = internalGet(1);
        if (internalGetEra() == 0) {
            year = 1 - year;
        }
        return monthLength(month, year);
    }

    private int actualMonthLength() {
        int year = this.cdate.getNormalizedYear();
        if (year != this.gregorianCutoverYear && year != this.gregorianCutoverYearJulian) {
            return this.calsys.getMonthLength(this.cdate);
        }
        BaseCalendar.Date date = (BaseCalendar.Date) this.cdate.clone();
        long month1 = getFixedDateMonth1(date, this.calsys.getFixedDate(date));
        long next1 = ((long) this.calsys.getMonthLength(date)) + month1;
        if (next1 < this.gregorianCutoverDate) {
            return (int) (next1 - month1);
        }
        if (this.cdate != this.gdate) {
            date = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        }
        gcal.getCalendarDateFromFixedDate(date, next1);
        return (int) (getFixedDateMonth1(date, next1) - month1);
    }

    private int yearLength(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    private int yearLength() {
        int year = internalGet(1);
        if (internalGetEra() == 0) {
            year = 1 - year;
        }
        return yearLength(year);
    }

    private void pinDayOfMonth() {
        int monthLen;
        int year = internalGet(1);
        if (year > this.gregorianCutoverYear || year < this.gregorianCutoverYearJulian) {
            monthLen = monthLength(internalGet(2));
        } else {
            monthLen = getNormalizedCalendar().getActualMaximum(5);
        }
        if (internalGet(5) > monthLen) {
            set(5, monthLen);
        }
    }

    private long getCurrentFixedDate() {
        return this.calsys == gcal ? this.cachedFixedDate : this.calsys.getFixedDate(this.cdate);
    }

    private static int getRolledValue(int value, int amount, int min, int max) {
        int range = (max - min) + 1;
        int n = value + (amount % range);
        if (n > max) {
            return n - range;
        }
        if (n < min) {
            return n + range;
        }
        return n;
    }

    private int internalGetEra() {
        if (isSet(0)) {
            return internalGet(0);
        }
        return 1;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.gdate == null) {
            this.gdate = gcal.newCalendarDate(getZone());
            this.cachedFixedDate = Long.MIN_VALUE;
        }
        setGregorianChange(this.gregorianCutover);
    }

    public ZonedDateTime toZonedDateTime() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getTimeInMillis()), getTimeZone().toZoneId());
    }

    public static GregorianCalendar from(ZonedDateTime zdt) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(zdt.getZone()));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        cal.setFirstDayOfWeek(2);
        cal.setMinimalDaysInFirstWeek(4);
        try {
            cal.setTimeInMillis(Math.addExact(Math.multiplyExact(zdt.toEpochSecond(), 1000), (long) zdt.get(ChronoField.MILLI_OF_SECOND)));
            return cal;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException((Throwable) ex);
        }
    }
}
