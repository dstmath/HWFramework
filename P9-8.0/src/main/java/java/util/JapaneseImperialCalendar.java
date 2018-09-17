package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import sun.util.calendar.AbstractCalendar;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.LocalGregorianCalendar;
import sun.util.calendar.LocalGregorianCalendar.Date;
import sun.util.locale.provider.CalendarDataUtility;

class JapaneseImperialCalendar extends Calendar {
    static final /* synthetic */ boolean -assertionsDisabled = (JapaneseImperialCalendar.class.desiredAssertionStatus() ^ 1);
    public static final int BEFORE_MEIJI = 0;
    private static final Era BEFORE_MEIJI_ERA = new Era("BeforeMeiji", "BM", Long.MIN_VALUE, -assertionsDisabled);
    private static final int EPOCH_OFFSET = 719163;
    private static final int EPOCH_YEAR = 1970;
    public static final int HEISEI = 4;
    static final int[] LEAST_MAX_VALUES = new int[]{0, 0, 0, 0, 4, 28, 0, 7, 4, 1, 11, 23, 59, 59, 999, 50400000, 1200000};
    static final int[] MAX_VALUES = new int[]{0, 292278994, 11, 53, 6, 31, 366, 7, 6, 1, 11, 23, 59, 59, 999, 50400000, 7200000};
    public static final int MEIJI = 1;
    static final int[] MIN_VALUES = new int[]{0, -292275055, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, -46800000, 0};
    private static final long ONE_DAY = 86400000;
    private static final int ONE_HOUR = 3600000;
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_SECOND = 1000;
    private static final long ONE_WEEK = 604800000;
    public static final int SHOWA = 3;
    public static final int TAISHO = 2;
    private static final Era[] eras;
    private static final Gregorian gcal = CalendarSystem.getGregorianCalendar();
    private static final LocalGregorianCalendar jcal = ((LocalGregorianCalendar) CalendarSystem.forName("japanese"));
    private static final long serialVersionUID = -3364572813905467929L;
    private static final long[] sinceFixedDates;
    private transient long cachedFixedDate = Long.MIN_VALUE;
    private transient Date jdate;
    private transient int[] originalFields;
    private transient int[] zoneOffsets;

    static {
        Era[] es = jcal.getEras();
        int length = es.length + 1;
        eras = new Era[length];
        sinceFixedDates = new long[length];
        sinceFixedDates[0] = gcal.getFixedDate(BEFORE_MEIJI_ERA.getSinceDate());
        eras[0] = BEFORE_MEIJI_ERA;
        int i = 0;
        int length2 = es.length;
        int index = 1;
        while (i < length2) {
            Era e = es[i];
            sinceFixedDates[index] = gcal.getFixedDate(e.getSinceDate());
            int index2 = index + 1;
            eras[index] = e;
            i++;
            index = index2;
        }
        int[] iArr = LEAST_MAX_VALUES;
        length2 = eras.length - 1;
        MAX_VALUES[0] = length2;
        iArr[0] = length2;
        int year = Integer.MAX_VALUE;
        int dayOfYear = Integer.MAX_VALUE;
        CalendarDate date = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        for (int i2 = 1; i2 < eras.length; i2++) {
            long fd = sinceFixedDates[i2];
            CalendarDate transitionDate = eras[i2].getSinceDate();
            date.setDate(transitionDate.getYear(), 1, 1);
            long fdd = gcal.getFixedDate(date);
            if (fd != fdd) {
                dayOfYear = Math.min(((int) (fd - fdd)) + 1, dayOfYear);
            }
            date.setDate(transitionDate.getYear(), 12, 31);
            fdd = gcal.getFixedDate(date);
            if (fd != fdd) {
                dayOfYear = Math.min(((int) (fdd - fd)) + 1, dayOfYear);
            }
            Date lgd = getCalendarDate(fd - 1);
            int y = lgd.getYear();
            if (lgd.getMonth() != 1 || lgd.getDayOfMonth() != 1) {
                y--;
            }
            year = Math.min(y, year);
        }
        LEAST_MAX_VALUES[1] = year;
        LEAST_MAX_VALUES[6] = dayOfYear;
    }

    JapaneseImperialCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        this.jdate = jcal.newCalendarDate(zone);
        setTimeInMillis(System.currentTimeMillis());
    }

    JapaneseImperialCalendar(TimeZone zone, Locale aLocale, boolean flag) {
        super(zone, aLocale);
        this.jdate = jcal.newCalendarDate(zone);
    }

    public String getCalendarType() {
        return "japanese";
    }

    public boolean equals(Object obj) {
        if (obj instanceof JapaneseImperialCalendar) {
            return super.equals(obj);
        }
        return -assertionsDisabled;
    }

    public int hashCode() {
        return super.hashCode() ^ this.jdate.hashCode();
    }

    public void add(int field, int amount) {
        if (amount != 0) {
            if (field < 0 || field >= 15) {
                throw new IllegalArgumentException();
            }
            complete();
            Date d;
            if (field == 1) {
                d = (Date) this.jdate.clone();
                d.addYear(amount);
                pinDayOfMonth(d);
                set(0, getEraIndex(d));
                set(1, d.getYear());
                set(2, d.getMonth() - 1);
                set(5, d.getDayOfMonth());
            } else if (field == 2) {
                d = (Date) this.jdate.clone();
                d.addMonth(amount);
                pinDayOfMonth(d);
                set(0, getEraIndex(d));
                set(1, d.getYear());
                set(2, d.getMonth() - 1);
                set(5, d.getDayOfMonth());
            } else if (field == 0) {
                int era = internalGet(0) + amount;
                if (era < 0) {
                    era = 0;
                } else if (era > eras.length - 1) {
                    era = eras.length - 1;
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
                long fd = this.cachedFixedDate;
                timeOfDay = ((((((timeOfDay + ((long) internalGet(11))) * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000) + ((long) internalGet(14));
                if (timeOfDay >= ONE_DAY) {
                    fd++;
                    timeOfDay -= ONE_DAY;
                } else if (timeOfDay < 0) {
                    fd--;
                    timeOfDay += ONE_DAY;
                }
                fd += delta;
                int zoneOffset = internalGet(15) + internalGet(16);
                setTimeInMillis((((fd - 719163) * ONE_DAY) + timeOfDay) - ((long) zoneOffset));
                zoneOffset -= internalGet(15) + internalGet(16);
                if (zoneOffset != 0) {
                    setTimeInMillis(this.time + ((long) zoneOffset));
                    if (this.cachedFixedDate != fd) {
                        setTimeInMillis(this.time - ((long) zoneOffset));
                    }
                }
            }
        }
    }

    public void roll(int field, boolean up) {
        roll(field, up ? 1 : -1);
    }

    public void roll(int field, int amount) {
        if (amount != 0) {
            if (field < 0 || field >= 15) {
                throw new IllegalArgumentException();
            }
            complete();
            int min = getMinimum(field);
            int max = getMaximum(field);
            int dom;
            int year;
            CalendarDate d;
            long fd;
            Date d2;
            long month1;
            int monthLength;
            switch (field) {
                case 1:
                    min = getActualMinimum(field);
                    max = getActualMaximum(field);
                    break;
                case 2:
                    int n;
                    if (isTransitionYear(this.jdate.getNormalizedYear())) {
                        int eraIndex = getEraIndex(this.jdate);
                        CalendarDate transition = null;
                        if (this.jdate.getYear() == 1) {
                            transition = eras[eraIndex].getSinceDate();
                            min = transition.getMonth() - 1;
                        } else if (eraIndex < eras.length - 1) {
                            transition = eras[eraIndex + 1].getSinceDate();
                            if (transition.getYear() == this.jdate.getNormalizedYear()) {
                                max = transition.getMonth() - 1;
                                if (transition.getDayOfMonth() == 1) {
                                    max--;
                                }
                            }
                        }
                        if (min != max) {
                            n = getRolledValue(internalGet(field), amount, min, max);
                            set(2, n);
                            if (n == min) {
                                if (!(transition.getMonth() == 1 && transition.getDayOfMonth() == 1) && this.jdate.getDayOfMonth() < transition.getDayOfMonth()) {
                                    set(5, transition.getDayOfMonth());
                                }
                            } else if (n == max && transition.getMonth() - 1 == n) {
                                dom = transition.getDayOfMonth();
                                if (this.jdate.getDayOfMonth() >= dom) {
                                    set(5, dom - 1);
                                }
                            }
                        } else {
                            return;
                        }
                    }
                    year = this.jdate.getYear();
                    CalendarDate jd;
                    if (year == getMaximum(1)) {
                        jd = jcal.getCalendarDate(this.time, getZone());
                        d = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                        max = d.getMonth() - 1;
                        n = getRolledValue(internalGet(field), amount, min, max);
                        if (n == max) {
                            jd.addYear(-400);
                            jd.setMonth(n + 1);
                            if (jd.getDayOfMonth() > d.getDayOfMonth()) {
                                jd.setDayOfMonth(d.getDayOfMonth());
                                jcal.normalize(jd);
                            }
                            if (jd.getDayOfMonth() == d.getDayOfMonth() && jd.getTimeOfDay() > d.getTimeOfDay()) {
                                jd.setMonth(n + 1);
                                jd.setDayOfMonth(d.getDayOfMonth() - 1);
                                jcal.normalize(jd);
                                n = jd.getMonth() - 1;
                            }
                            set(5, jd.getDayOfMonth());
                        }
                        set(2, n);
                    } else {
                        if (year == getMinimum(1)) {
                            jd = jcal.getCalendarDate(this.time, getZone());
                            d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                            min = d.getMonth() - 1;
                            n = getRolledValue(internalGet(field), amount, min, max);
                            if (n == min) {
                                jd.addYear(400);
                                jd.setMonth(n + 1);
                                if (jd.getDayOfMonth() < d.getDayOfMonth()) {
                                    jd.setDayOfMonth(d.getDayOfMonth());
                                    jcal.normalize(jd);
                                }
                                if (jd.getDayOfMonth() == d.getDayOfMonth() && jd.getTimeOfDay() < d.getTimeOfDay()) {
                                    jd.setMonth(n + 1);
                                    jd.setDayOfMonth(d.getDayOfMonth() + 1);
                                    jcal.normalize(jd);
                                    n = jd.getMonth() - 1;
                                }
                                set(5, jd.getDayOfMonth());
                            }
                            set(2, n);
                        } else {
                            int mon = (internalGet(2) + amount) % 12;
                            if (mon < 0) {
                                mon += 12;
                            }
                            set(2, mon);
                            int monthLen = monthLength(mon);
                            if (internalGet(5) > monthLen) {
                                set(5, monthLen);
                            }
                        }
                    }
                    return;
                case 3:
                    int y = this.jdate.getNormalizedYear();
                    max = getActualMaximum(3);
                    set(7, internalGet(7));
                    int woy = internalGet(3);
                    int value = woy + amount;
                    long day1;
                    if (isTransitionYear(this.jdate.getNormalizedYear())) {
                        fd = this.cachedFixedDate;
                        day1 = fd - ((long) ((woy - min) * 7));
                        d2 = getCalendarDate(day1);
                        if (!(d2.getEra() == this.jdate.getEra() && d2.getYear() == this.jdate.getYear())) {
                            min++;
                        }
                        jcal.getCalendarDateFromFixedDate(d2, fd + ((long) ((max - woy) * 7)));
                        if (!(d2.getEra() == this.jdate.getEra() && d2.getYear() == this.jdate.getYear())) {
                            max--;
                        }
                        d2 = getCalendarDate(((long) ((getRolledValue(woy, amount, min, max) - 1) * 7)) + day1);
                        set(2, d2.getMonth() - 1);
                        set(5, d2.getDayOfMonth());
                        return;
                    }
                    year = this.jdate.getYear();
                    if (year == getMaximum(1)) {
                        max = getActualMaximum(3);
                    } else {
                        if (year == getMinimum(1)) {
                            min = getActualMinimum(3);
                            max = getActualMaximum(3);
                            if (value > min && value < max) {
                                set(3, value);
                                return;
                            }
                        }
                    }
                    if (value <= min || value >= max) {
                        fd = this.cachedFixedDate;
                        day1 = fd - ((long) ((woy - min) * 7));
                        if (year == getMinimum(1)) {
                            if (day1 < jcal.getFixedDate(jcal.getCalendarDate(Long.MIN_VALUE, getZone()))) {
                                min++;
                            }
                        } else if (gcal.getYearFromFixedDate(day1) != y) {
                            min++;
                        }
                        if (gcal.getYearFromFixedDate(fd + ((long) ((max - internalGet(3)) * 7))) != y) {
                            max--;
                            break;
                        }
                    }
                    set(3, value);
                    return;
                    break;
                case 4:
                    boolean isTransitionYear = isTransitionYear(this.jdate.getNormalizedYear());
                    int dow = internalGet(7) - getFirstDayOfWeek();
                    if (dow < 0) {
                        dow += 7;
                    }
                    fd = this.cachedFixedDate;
                    if (isTransitionYear) {
                        month1 = getFixedDateMonth1(this.jdate, fd);
                        monthLength = actualMonthLength();
                    } else {
                        month1 = (fd - ((long) internalGet(5))) + 1;
                        monthLength = jcal.getMonthLength(this.jdate);
                    }
                    long monthDay1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + month1, getFirstDayOfWeek());
                    if (((int) (monthDay1st - month1)) >= getMinimalDaysInFirstWeek()) {
                        monthDay1st -= 7;
                    }
                    long nfd = (((long) ((getRolledValue(internalGet(field), amount, 1, getActualMaximum(field)) - 1) * 7)) + monthDay1st) + ((long) dow);
                    if (nfd < month1) {
                        nfd = month1;
                    } else if (nfd >= ((long) monthLength) + month1) {
                        nfd = (((long) monthLength) + month1) - 1;
                    }
                    set(5, ((int) (nfd - month1)) + 1);
                    return;
                case 5:
                    if (!isTransitionYear(this.jdate.getNormalizedYear())) {
                        max = jcal.getMonthLength(this.jdate);
                        break;
                    }
                    month1 = getFixedDateMonth1(this.jdate, this.cachedFixedDate);
                    d2 = getCalendarDate(((long) getRolledValue((int) (this.cachedFixedDate - month1), amount, 0, actualMonthLength() - 1)) + month1);
                    if (-assertionsDisabled || (getEraIndex(d2) == internalGetEra() && d2.getYear() == internalGet(1) && d2.getMonth() - 1 == internalGet(2))) {
                        set(5, d2.getDayOfMonth());
                        return;
                    }
                    throw new AssertionError();
                case 6:
                    max = getActualMaximum(field);
                    if (isTransitionYear(this.jdate.getNormalizedYear())) {
                        long jan0 = this.cachedFixedDate - ((long) internalGet(6));
                        d2 = getCalendarDate(((long) getRolledValue(internalGet(6), amount, min, max)) + jan0);
                        if (-assertionsDisabled || (getEraIndex(d2) == internalGetEra() && d2.getYear() == internalGet(1))) {
                            set(2, d2.getMonth() - 1);
                            set(5, d2.getDayOfMonth());
                            return;
                        }
                        throw new AssertionError();
                    }
                    break;
                case 7:
                    int normalizedYear = this.jdate.getNormalizedYear();
                    if (!(isTransitionYear(normalizedYear) || (isTransitionYear(normalizedYear - 1) ^ 1) == 0)) {
                        int weekOfYear = internalGet(3);
                        if (weekOfYear > 1 && weekOfYear < 52) {
                            set(3, internalGet(3));
                            max = 7;
                            break;
                        }
                    }
                    amount %= 7;
                    if (amount != 0) {
                        fd = this.cachedFixedDate;
                        long dowFirst = AbstractCalendar.getDayOfWeekDateOnOrBefore(fd, getFirstDayOfWeek());
                        fd += (long) amount;
                        if (fd < dowFirst) {
                            fd += 7;
                        } else if (fd >= 7 + dowFirst) {
                            fd -= 7;
                        }
                        d2 = getCalendarDate(fd);
                        set(0, getEraIndex(d2));
                        set(d2.getYear(), d2.getMonth() - 1, d2.getDayOfMonth());
                        return;
                    }
                    return;
                case 8:
                    min = 1;
                    if (!isTransitionYear(this.jdate.getNormalizedYear())) {
                        dom = internalGet(5);
                        monthLength = jcal.getMonthLength(this.jdate);
                        max = monthLength / 7;
                        if ((dom - 1) % 7 < monthLength % 7) {
                            max++;
                        }
                        set(7, internalGet(7));
                        break;
                    }
                    fd = this.cachedFixedDate;
                    month1 = getFixedDateMonth1(this.jdate, fd);
                    monthLength = actualMonthLength();
                    max = monthLength / 7;
                    int x = ((int) (fd - month1)) % 7;
                    if (x < monthLength % 7) {
                        max++;
                    }
                    set(5, getCalendarDate((((long) ((getRolledValue(internalGet(field), amount, 1, max) - 1) * 7)) + month1) + ((long) x)).getDayOfMonth());
                    return;
                case 10:
                case 11:
                    int unit = max + 1;
                    int h = internalGet(field);
                    int nh = (h + amount) % unit;
                    if (nh < 0) {
                        nh += unit;
                    }
                    this.time += (long) ((nh - h) * ONE_HOUR);
                    d = jcal.getCalendarDate(this.time, getZone());
                    if (internalGet(5) != d.getDayOfMonth()) {
                        d.setEra(this.jdate.getEra());
                        d.setDate(internalGet(1), internalGet(2) + 1, internalGet(5));
                        if (field == 10) {
                            if (-assertionsDisabled || internalGet(9) == 1) {
                                d.addHours(12);
                            } else {
                                throw new AssertionError();
                            }
                        }
                        this.time = jcal.getTime(d);
                    }
                    int hourOfDay = d.getHours();
                    internalSet(field, hourOfDay % unit);
                    if (field == 10) {
                        internalSet(11, hourOfDay);
                    } else {
                        internalSet(9, hourOfDay / 12);
                        internalSet(10, hourOfDay % 12);
                    }
                    int zoneOffset = d.getZoneOffset();
                    int saving = d.getDaylightSaving();
                    internalSet(15, zoneOffset - saving);
                    internalSet(16, saving);
                    return;
            }
            set(field, getRolledValue(internalGet(field), amount, min, max));
        }
    }

    public String getDisplayName(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, 1, 4, locale, 647)) {
            return null;
        }
        int fieldValue = get(field);
        if (field == 1 && (getBaseStyle(style) != 2 || fieldValue != 1 || get(0) == 0)) {
            return null;
        }
        String name = CalendarDataUtility.retrieveFieldValueName(getCalendarType(), field, fieldValue, style, locale);
        if (name == null && field == 0 && fieldValue < eras.length) {
            Era era = eras[fieldValue];
            name = style == 1 ? era.getAbbreviation() : era.getName();
        }
        return name;
    }

    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, 0, 4, locale, 647)) {
            return null;
        }
        Map<String, Integer> names = CalendarDataUtility.retrieveFieldValueNames(getCalendarType(), field, style, locale);
        if (names != null && field == 0) {
            int size = names.size();
            if (style == 0) {
                Set<Integer> values = new HashSet();
                for (String key : names.keySet()) {
                    values.add((Integer) names.get(key));
                }
                size = values.size();
            }
            if (size < eras.length) {
                int baseStyle = getBaseStyle(style);
                for (int i = size; i < eras.length; i++) {
                    Era era = eras[i];
                    if (baseStyle == 0 || baseStyle == 1 || baseStyle == 4) {
                        names.put(era.getAbbreviation(), Integer.valueOf(i));
                    }
                    if (baseStyle == 0 || baseStyle == 2) {
                        names.put(era.getName(), Integer.valueOf(i));
                    }
                }
            }
        }
        return names;
    }

    public int getMinimum(int field) {
        return MIN_VALUES[field];
    }

    public int getMaximum(int field) {
        switch (field) {
            case 1:
                return Math.max(LEAST_MAX_VALUES[1], jcal.getCalendarDate((long) Long.MAX_VALUE, getZone()).getYear());
            default:
                return MAX_VALUES[field];
        }
    }

    public int getGreatestMinimum(int field) {
        return field == 1 ? 1 : MIN_VALUES[field];
    }

    public int getLeastMaximum(int field) {
        switch (field) {
            case 1:
                return Math.min(LEAST_MAX_VALUES[1], getMaximum(1));
            default:
                return LEAST_MAX_VALUES[field];
        }
    }

    public int getActualMinimum(int field) {
        if (!Calendar.isFieldSet(14, field)) {
            return getMinimum(field);
        }
        int value = 0;
        Date jd = jcal.getCalendarDate(getNormalizedCalendar().getTimeInMillis(), getZone());
        int eraIndex = getEraIndex(jd);
        CalendarDate d;
        long since;
        switch (field) {
            case 1:
                if (eraIndex <= 0) {
                    value = getMinimum(field);
                    d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                    int y = d.getYear();
                    if (y > 400) {
                        y -= 400;
                    }
                    jd.setYear(y);
                    jcal.normalize(jd);
                    if (getYearOffsetInMillis(jd) < getYearOffsetInMillis(d)) {
                        value++;
                        break;
                    }
                }
                value = 1;
                since = eras[eraIndex].getSince(getZone());
                d = jcal.getCalendarDate(since, getZone());
                jd.setYear(d.getYear());
                jcal.normalize(jd);
                if (-assertionsDisabled || jd.isLeapYear() == d.isLeapYear()) {
                    if (getYearOffsetInMillis(jd) < getYearOffsetInMillis(d)) {
                        value = 2;
                        break;
                    }
                }
                throw new AssertionError();
                break;
            case 2:
                if (eraIndex > 1 && jd.getYear() == 1) {
                    since = eras[eraIndex].getSince(getZone());
                    d = jcal.getCalendarDate(since, getZone());
                    value = d.getMonth() - 1;
                    if (jd.getDayOfMonth() < d.getDayOfMonth()) {
                        value++;
                        break;
                    }
                }
                break;
            case 3:
                value = 1;
                d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                d.addYear(400);
                jcal.normalize(d);
                jd.setEra(d.getEra());
                jd.setYear(d.getYear());
                jcal.normalize(jd);
                long jan1 = jcal.getFixedDate(d);
                long fd = jcal.getFixedDate(jd);
                long day1 = fd - ((long) ((getWeekNumber(jan1, fd) - 1) * 7));
                if (day1 < jan1 || (day1 == jan1 && jd.getTimeOfDay() < d.getTimeOfDay())) {
                    value = 2;
                    break;
                }
        }
        return value;
    }

    public int getActualMaximum(int field) {
        if (((1 << field) & 130689) != 0) {
            return getMaximum(field);
        }
        int value;
        JapaneseImperialCalendar jc = getNormalizedCalendar();
        Date date = jc.jdate;
        int normalizedYear = date.getNormalizedYear();
        int eraIndex;
        CalendarDate d;
        Date d2;
        long transition;
        CalendarDate jd;
        int dayOfWeek;
        long fd;
        switch (field) {
            case 1:
                CalendarDate jd2 = jcal.getCalendarDate(jc.getTimeInMillis(), getZone());
                eraIndex = getEraIndex(date);
                if (eraIndex == eras.length - 1) {
                    d = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    value = d.getYear();
                    if (value > 400) {
                        jd2.setYear(value - 400);
                    }
                } else {
                    d = jcal.getCalendarDate(eras[eraIndex + 1].getSince(getZone()) - 1, getZone());
                    value = d.getYear();
                    jd2.setYear(value);
                }
                jcal.normalize(jd2);
                if (getYearOffsetInMillis(jd2) > getYearOffsetInMillis(d)) {
                    value--;
                    break;
                }
                break;
            case 2:
                value = 11;
                if (!isTransitionYear(date.getNormalizedYear())) {
                    d2 = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    if (date.getEra() == d2.getEra() && date.getYear() == d2.getYear()) {
                        value = d2.getMonth() - 1;
                        break;
                    }
                }
                eraIndex = getEraIndex(date);
                if (date.getYear() != 1) {
                    eraIndex++;
                    if (!-assertionsDisabled && eraIndex >= eras.length) {
                        throw new AssertionError();
                    }
                }
                transition = sinceFixedDates[eraIndex];
                if (jc.cachedFixedDate < transition) {
                    CalendarDate ldate = (Date) date.clone();
                    jcal.getCalendarDateFromFixedDate(ldate, transition - 1);
                    value = ldate.getMonth() - 1;
                    break;
                }
                break;
            case 3:
                if (!isTransitionYear(date.getNormalizedYear())) {
                    jd = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    if (date.getEra() != jd.getEra() || date.getYear() != jd.getYear()) {
                        if (date.getEra() != null || date.getYear() != getMinimum(1)) {
                            d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                            d.setDate(date.getNormalizedYear(), 1, 1);
                            dayOfWeek = gcal.getDayOfWeek(d) - getFirstDayOfWeek();
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
                        d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                        d.addYear(400);
                        jcal.normalize(d);
                        jd.setEra(d.getEra());
                        jd.setDate(d.getYear() + 1, 1, 1);
                        jcal.normalize(jd);
                        long jan1 = jcal.getFixedDate(d);
                        long nextJan1 = jcal.getFixedDate(jd);
                        long nextJan1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan1, getFirstDayOfWeek());
                        if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek()) {
                            nextJan1st -= 7;
                        }
                        value = getWeekNumber(jan1, nextJan1st);
                        break;
                    }
                    fd = jcal.getFixedDate(jd);
                    value = getWeekNumber(getFixedDateJan1(jd, fd), fd);
                    break;
                }
                if (jc == this) {
                    jc = (JapaneseImperialCalendar) jc.clone();
                }
                int max = getActualMaximum(6);
                jc.set(6, max);
                value = jc.get(3);
                if (value == 1 && max > 7) {
                    jc.add(3, -1);
                    value = jc.get(3);
                    break;
                }
                break;
            case 4:
                jd = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                if (date.getEra() != jd.getEra() || date.getYear() != jd.getYear()) {
                    d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d.setDate(date.getNormalizedYear(), date.getMonth(), 1);
                    dayOfWeek = gcal.getDayOfWeek(d);
                    int monthLength = gcal.getMonthLength(d);
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
                        value++;
                        if (monthLength > 7) {
                            value++;
                            break;
                        }
                    }
                }
                fd = jcal.getFixedDate(jd);
                value = getWeekNumber((fd - ((long) jd.getDayOfMonth())) + 1, fd);
                break;
                break;
            case 5:
                value = jcal.getMonthLength(date);
                break;
            case 6:
                if (!isTransitionYear(date.getNormalizedYear())) {
                    d2 = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    if (date.getEra() != d2.getEra() || date.getYear() != d2.getYear()) {
                        if (date.getYear() != getMinimum(1)) {
                            value = jcal.getYearLength(date);
                            break;
                        }
                        CalendarDate d1 = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                        long fd1 = jcal.getFixedDate(d1);
                        d1.addYear(1);
                        d1.setMonth(1).setDayOfMonth(1);
                        jcal.normalize(d1);
                        value = (int) (jcal.getFixedDate(d1) - fd1);
                        break;
                    }
                    fd = jcal.getFixedDate(d2);
                    value = ((int) (fd - getFixedDateJan1(d2, fd))) + 1;
                    break;
                }
                eraIndex = getEraIndex(date);
                if (date.getYear() != 1) {
                    eraIndex++;
                    if (!-assertionsDisabled && eraIndex >= eras.length) {
                        throw new AssertionError();
                    }
                }
                transition = sinceFixedDates[eraIndex];
                fd = jc.cachedFixedDate;
                d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                d.setDate(date.getNormalizedYear(), 1, 1);
                if (fd >= transition) {
                    d.addYear(1);
                    value = (int) (gcal.getFixedDate(d) - transition);
                    break;
                }
                value = (int) (transition - gcal.getFixedDate(d));
                break;
                break;
            case 8:
                int dow = date.getDayOfWeek();
                BaseCalendar.Date d3 = (BaseCalendar.Date) date.clone();
                int ndays = jcal.getMonthLength(d3);
                d3.setDayOfMonth(1);
                jcal.normalize(d3);
                int x = dow - d3.getDayOfWeek();
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

    private long getYearOffsetInMillis(CalendarDate date) {
        return (date.getTimeOfDay() + ((jcal.getDayOfYear(date) - 1) * ONE_DAY)) - ((long) date.getZoneOffset());
    }

    public Object clone() {
        JapaneseImperialCalendar other = (JapaneseImperialCalendar) super.clone();
        other.jdate = (Date) this.jdate.clone();
        other.originalFields = null;
        other.zoneOffsets = null;
        return other;
    }

    public TimeZone getTimeZone() {
        TimeZone zone = super.getTimeZone();
        this.jdate.setZone(zone);
        return zone;
    }

    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        this.jdate.setZone(zone);
    }

    protected void computeFields() {
        int mask;
        if (isPartiallyNormalized()) {
            mask = getSetStateFields();
            int fieldMask = (~mask) & 131071;
            if (fieldMask != 0 || this.cachedFixedDate == Long.MIN_VALUE) {
                mask |= computeFields(fieldMask, 98304 & mask);
                if (!(-assertionsDisabled || mask == 131071)) {
                    throw new AssertionError();
                }
            }
        }
        mask = 131071;
        computeFields(131071, 0);
        setFieldsComputed(mask);
    }

    private int computeFields(int fieldMask, int tzMask) {
        int zoneOffset = 0;
        TimeZone tz = getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        if (tzMask != 98304) {
            zoneOffset = tz.getOffset(this.time);
            this.zoneOffsets[0] = tz.getRawOffset();
            this.zoneOffsets[1] = zoneOffset - this.zoneOffsets[0];
        }
        if (tzMask != 0) {
            if (Calendar.isFieldSet(tzMask, 15)) {
                this.zoneOffsets[0] = internalGet(15);
            }
            if (Calendar.isFieldSet(tzMask, 16)) {
                this.zoneOffsets[1] = internalGet(16);
            }
            zoneOffset = this.zoneOffsets[0] + this.zoneOffsets[1];
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
        if (fixedDate != this.cachedFixedDate || fixedDate < 0) {
            jcal.getCalendarDateFromFixedDate(this.jdate, fixedDate);
            this.cachedFixedDate = fixedDate;
        }
        int era = getEraIndex(this.jdate);
        int year = this.jdate.getYear();
        internalSet(0, era);
        internalSet(1, year);
        int mask = fieldMask | 3;
        int month = this.jdate.getMonth() - 1;
        int dayOfMonth = this.jdate.getDayOfMonth();
        if ((fieldMask & 164) != 0) {
            internalSet(2, month);
            internalSet(5, dayOfMonth);
            internalSet(7, this.jdate.getDayOfWeek());
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
                internalSet(13, r / 1000);
                internalSet(14, r % 1000);
            } else {
                internalSet(11, 0);
                internalSet(9, 0);
                internalSet(10, 0);
                internalSet(12, 0);
                internalSet(13, 0);
                internalSet(14, 0);
            }
            mask |= 32256;
        }
        if ((98304 & fieldMask) != 0) {
            internalSet(15, this.zoneOffsets[0]);
            internalSet(16, this.zoneOffsets[1]);
            mask |= 98304;
        }
        if ((fieldMask & 344) == 0) {
            return mask;
        }
        long fixedDateJan1;
        int dayOfYear;
        int normalizedYear = this.jdate.getNormalizedYear();
        boolean transitionYear = isTransitionYear(this.jdate.getNormalizedYear());
        if (transitionYear) {
            fixedDateJan1 = getFixedDateJan1(this.jdate, fixedDate);
            dayOfYear = ((int) (fixedDate - fixedDateJan1)) + 1;
        } else if (normalizedYear == MIN_VALUES[1]) {
            fixedDateJan1 = jcal.getFixedDate(jcal.getCalendarDate(Long.MIN_VALUE, getZone()));
            dayOfYear = ((int) (fixedDate - fixedDateJan1)) + 1;
        } else {
            dayOfYear = (int) jcal.getDayOfYear(this.jdate);
            fixedDateJan1 = (fixedDate - ((long) dayOfYear)) + 1;
        }
        long fixedDateMonth1 = transitionYear ? getFixedDateMonth1(this.jdate, fixedDate) : (fixedDate - ((long) dayOfMonth)) + 1;
        internalSet(6, dayOfYear);
        internalSet(8, ((dayOfMonth - 1) / 7) + 1);
        int weekOfYear = getWeekNumber(fixedDateJan1, fixedDate);
        Date d;
        CalendarDate cd;
        long nextJan1;
        long nextJan1st;
        if (weekOfYear == 0) {
            long prevJan1;
            long fixedDec31 = fixedDateJan1 - 1;
            d = getCalendarDate(fixedDec31);
            if (!(!transitionYear ? isTransitionYear(d.getNormalizedYear()) : true)) {
                prevJan1 = fixedDateJan1 - 365;
                if (d.isLeapYear()) {
                    prevJan1--;
                }
            } else if (!transitionYear) {
                cd = eras[getEraIndex(this.jdate)].getSinceDate();
                d.setMonth(cd.getMonth()).setDayOfMonth(cd.getDayOfMonth());
                jcal.normalize(d);
                prevJan1 = jcal.getFixedDate(d);
            } else if (this.jdate.getYear() == 1) {
                if (era > 4) {
                    CalendarDate pd = eras[era - 1].getSinceDate();
                    if (normalizedYear == pd.getYear()) {
                        d.setMonth(pd.getMonth()).setDayOfMonth(pd.getDayOfMonth());
                    }
                } else {
                    d.setMonth(1).setDayOfMonth(1);
                }
                jcal.normalize(d);
                prevJan1 = jcal.getFixedDate(d);
            } else {
                prevJan1 = fixedDateJan1 - 365;
                if (d.isLeapYear()) {
                    prevJan1--;
                }
            }
            weekOfYear = getWeekNumber(prevJan1, fixedDec31);
        } else if (transitionYear) {
            d = (Date) this.jdate.clone();
            if (this.jdate.getYear() == 1) {
                d.addYear(1);
                d.setMonth(1).setDayOfMonth(1);
                nextJan1 = jcal.getFixedDate(d);
            } else {
                int nextEraIndex = getEraIndex(d) + 1;
                cd = eras[nextEraIndex].getSinceDate();
                d.setEra(eras[nextEraIndex]);
                d.setDate(1, cd.getMonth(), cd.getDayOfMonth());
                jcal.normalize(d);
                nextJan1 = jcal.getFixedDate(d);
            }
            nextJan1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan1, getFirstDayOfWeek());
            if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek() && fixedDate >= nextJan1st - 7) {
                weekOfYear = 1;
            }
        } else if (weekOfYear >= 52) {
            nextJan1 = fixedDateJan1 + 365;
            if (this.jdate.isLeapYear()) {
                nextJan1++;
            }
            nextJan1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan1, getFirstDayOfWeek());
            if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek() && fixedDate >= nextJan1st - 7) {
                weekOfYear = 1;
            }
        }
        internalSet(3, weekOfYear);
        internalSet(4, getWeekNumber(fixedDateMonth1, fixedDate));
        return mask | 344;
    }

    private int getWeekNumber(long fixedDay1, long fixedDate) {
        long fixedDay1st = AbstractCalendar.getDayOfWeekDateOnOrBefore(6 + fixedDay1, getFirstDayOfWeek());
        int ndays = (int) (fixedDay1st - fixedDay1);
        if (-assertionsDisabled || ndays <= 7) {
            if (ndays >= getMinimalDaysInFirstWeek()) {
                fixedDay1st -= 7;
            }
            int normalizedDayOfPeriod = (int) (fixedDate - fixedDay1st);
            if (normalizedDayOfPeriod >= 0) {
                return (normalizedDayOfPeriod / 7) + 1;
            }
            return CalendarUtils.floorDivide(normalizedDayOfPeriod, 7) + 1;
        }
        throw new AssertionError();
    }

    protected void computeTime() {
        int field;
        int era;
        int year;
        long timeOfDay;
        if (!isLenient()) {
            if (this.originalFields == null) {
                this.originalFields = new int[17];
            }
            field = 0;
            while (field < 17) {
                int value = internalGet(field);
                if (!isExternallySet(field) || (value >= getMinimum(field) && value <= getMaximum(field))) {
                    this.originalFields[field] = value;
                    field++;
                } else {
                    throw new IllegalArgumentException(Calendar.getFieldName(field));
                }
            }
        }
        int fieldMask = selectFields();
        if (isSet(0)) {
            era = internalGet(0);
            if (isSet(1)) {
                year = internalGet(1);
            } else {
                year = 1;
            }
        } else if (isSet(1)) {
            era = eras.length - 1;
            year = internalGet(1);
        } else {
            era = 3;
            year = 45;
        }
        if (Calendar.isFieldSet(fieldMask, 11)) {
            timeOfDay = 0 + ((long) internalGet(11));
        } else {
            timeOfDay = 0 + ((long) internalGet(10));
            if (Calendar.isFieldSet(fieldMask, 9)) {
                timeOfDay += (long) (internalGet(9) * 12);
            }
        }
        timeOfDay = (((((timeOfDay * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000) + ((long) internalGet(14));
        long fixedDate = timeOfDay / ONE_DAY;
        timeOfDay %= ONE_DAY;
        while (timeOfDay < 0) {
            timeOfDay += ONE_DAY;
            fixedDate--;
        }
        long millis = (((fixedDate + getFixedDate(era, year, fieldMask)) - 719163) * ONE_DAY) + timeOfDay;
        TimeZone zone = getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        int tzMask = fieldMask & 98304;
        if (tzMask != 98304) {
            zone.getOffsets(millis - ((long) zone.getRawOffset()), this.zoneOffsets);
        }
        if (tzMask != 0) {
            if (Calendar.isFieldSet(tzMask, 15)) {
                this.zoneOffsets[0] = internalGet(15);
            }
            if (Calendar.isFieldSet(tzMask, 16)) {
                this.zoneOffsets[1] = internalGet(16);
            }
        }
        this.time = millis - ((long) (this.zoneOffsets[0] + this.zoneOffsets[1]));
        int mask = computeFields(getSetStateFields() | fieldMask, tzMask);
        if (!isLenient()) {
            field = 0;
            while (field < 17) {
                if (isExternallySet(field) && this.originalFields[field] != internalGet(field)) {
                    int wrongValue = internalGet(field);
                    System.arraycopy(this.originalFields, 0, this.fields, 0, this.fields.length);
                    throw new IllegalArgumentException(Calendar.getFieldName(field) + "=" + wrongValue + ", expected " + this.originalFields[field]);
                }
                field++;
            }
        }
        setFieldsNormalized(mask);
    }

    private long getFixedDate(int era, int year, int fieldMask) {
        int month = 0;
        int firstDayOfMonth = 1;
        if (Calendar.isFieldSet(fieldMask, 2)) {
            month = internalGet(2);
            if (month > 11) {
                year += month / 12;
                month %= 12;
            } else if (month < 0) {
                int[] rem = new int[1];
                year += CalendarUtils.floorDivide(month, 12, rem);
                month = rem[0];
            }
        } else if (year == 1 && era != 0) {
            CalendarDate d = eras[era].getSinceDate();
            month = d.getMonth() - 1;
            firstDayOfMonth = d.getDayOfMonth();
        }
        if (year == MIN_VALUES[1]) {
            CalendarDate dx = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
            int m = dx.getMonth() - 1;
            if (month < m) {
                month = m;
            }
            if (month == m) {
                firstDayOfMonth = dx.getDayOfMonth();
            }
        }
        Date date = jcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        date.setEra(era > 0 ? eras[era] : null);
        date.setDate(year, month + 1, firstDayOfMonth);
        jcal.normalize(date);
        long fixedDate = jcal.getFixedDate(date);
        long firstDayOfWeek;
        int dayOfWeek;
        if (Calendar.isFieldSet(fieldMask, 2)) {
            if (Calendar.isFieldSet(fieldMask, 5)) {
                return isSet(5) ? (fixedDate + ((long) internalGet(5))) - ((long) firstDayOfMonth) : fixedDate;
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
                    dowim = 1;
                }
                if (dowim >= 0) {
                    return AbstractCalendar.getDayOfWeekDateOnOrBefore((((long) (dowim * 7)) + fixedDate) - 1, dayOfWeek);
                }
                return AbstractCalendar.getDayOfWeekDateOnOrBefore((((long) (monthLength(month, year) + ((dowim + 1) * 7))) + fixedDate) - 1, dayOfWeek);
            }
        } else if (Calendar.isFieldSet(fieldMask, 6)) {
            if (isTransitionYear(date.getNormalizedYear())) {
                fixedDate = getFixedDateJan1(date, fixedDate);
            }
            return (fixedDate + ((long) internalGet(6))) - 1;
        } else {
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
        }
    }

    private long getFixedDateJan1(Date date, long fixedDate) {
        Era era = date.getEra();
        if (date.getEra() != null && date.getYear() == 1) {
            for (int eraIndex = getEraIndex(date); eraIndex > 0; eraIndex--) {
                long fd = gcal.getFixedDate(eras[eraIndex].getSinceDate());
                if (fd <= fixedDate) {
                    return fd;
                }
            }
        }
        CalendarDate d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        d.setDate(date.getNormalizedYear(), 1, 1);
        return gcal.getFixedDate(d);
    }

    private long getFixedDateMonth1(Date date, long fixedDate) {
        int eraIndex = getTransitionEraIndex(date);
        if (eraIndex != -1) {
            long transition = sinceFixedDates[eraIndex];
            if (transition <= fixedDate) {
                return transition;
            }
        }
        return (fixedDate - ((long) date.getDayOfMonth())) + 1;
    }

    private static Date getCalendarDate(long fd) {
        Date d = jcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        jcal.getCalendarDateFromFixedDate(d, fd);
        return d;
    }

    private int monthLength(int month, int gregorianYear) {
        return CalendarUtils.isGregorianLeapYear(gregorianYear) ? GregorianCalendar.LEAP_MONTH_LENGTH[month] : GregorianCalendar.MONTH_LENGTH[month];
    }

    private int monthLength(int month) {
        if (-assertionsDisabled || this.jdate.isNormalized()) {
            return this.jdate.isLeapYear() ? GregorianCalendar.LEAP_MONTH_LENGTH[month] : GregorianCalendar.MONTH_LENGTH[month];
        } else {
            throw new AssertionError();
        }
    }

    private int actualMonthLength() {
        int length = jcal.getMonthLength(this.jdate);
        int eraIndex = getTransitionEraIndex(this.jdate);
        if (eraIndex != -1) {
            return length;
        }
        long transitionFixedDate = sinceFixedDates[eraIndex];
        CalendarDate d = eras[eraIndex].getSinceDate();
        if (transitionFixedDate <= this.cachedFixedDate) {
            return length - (d.getDayOfMonth() - 1);
        }
        return d.getDayOfMonth() - 1;
    }

    private static int getTransitionEraIndex(Date date) {
        int eraIndex = getEraIndex(date);
        CalendarDate transitionDate = eras[eraIndex].getSinceDate();
        if (transitionDate.getYear() == date.getNormalizedYear() && transitionDate.getMonth() == date.getMonth()) {
            return eraIndex;
        }
        if (eraIndex < eras.length - 1) {
            eraIndex++;
            transitionDate = eras[eraIndex].getSinceDate();
            if (transitionDate.getYear() == date.getNormalizedYear() && transitionDate.getMonth() == date.getMonth()) {
                return eraIndex;
            }
        }
        return -1;
    }

    private boolean isTransitionYear(int normalizedYear) {
        for (int i = eras.length - 1; i > 0; i--) {
            int transitionYear = eras[i].getSinceDate().getYear();
            if (normalizedYear == transitionYear) {
                return true;
            }
            if (normalizedYear > transitionYear) {
                break;
            }
        }
        return -assertionsDisabled;
    }

    private static int getEraIndex(Date date) {
        Era era = date.getEra();
        for (int i = eras.length - 1; i > 0; i--) {
            if (eras[i] == era) {
                return i;
            }
        }
        return 0;
    }

    private JapaneseImperialCalendar getNormalizedCalendar() {
        if (isFullyNormalized()) {
            return this;
        }
        JapaneseImperialCalendar thisR = (JapaneseImperialCalendar) clone();
        thisR.setLenient(true);
        thisR.complete();
        return thisR;
    }

    private void pinDayOfMonth(Date date) {
        int year = date.getYear();
        int dom = date.getDayOfMonth();
        int monthLength;
        if (year != getMinimum(1)) {
            date.setDayOfMonth(1);
            jcal.normalize(date);
            monthLength = jcal.getMonthLength(date);
            if (dom > monthLength) {
                date.setDayOfMonth(monthLength);
            } else {
                date.setDayOfMonth(dom);
            }
            jcal.normalize(date);
            return;
        }
        Date d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
        Date realDate = jcal.getCalendarDate(this.time, getZone());
        long tod = realDate.getTimeOfDay();
        realDate.addYear(400);
        realDate.setMonth(date.getMonth());
        realDate.setDayOfMonth(1);
        jcal.normalize(realDate);
        monthLength = jcal.getMonthLength(realDate);
        if (dom > monthLength) {
            realDate.setDayOfMonth(monthLength);
        } else if (dom < d.getDayOfMonth()) {
            realDate.setDayOfMonth(d.getDayOfMonth());
        } else {
            realDate.setDayOfMonth(dom);
        }
        if (realDate.getDayOfMonth() == d.getDayOfMonth() && tod < d.getTimeOfDay()) {
            realDate.setDayOfMonth(Math.min(dom + 1, monthLength));
        }
        date.setDate(year, realDate.getMonth(), realDate.getDayOfMonth());
    }

    private static int getRolledValue(int value, int amount, int min, int max) {
        if (-assertionsDisabled || (value >= min && value <= max)) {
            int range = (max - min) + 1;
            int n = value + (amount % range);
            if (n > max) {
                n -= range;
            } else if (n < min) {
                n += range;
            }
            if (-assertionsDisabled || (n >= min && n <= max)) {
                return n;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private int internalGetEra() {
        return isSet(0) ? internalGet(0) : eras.length - 1;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.jdate == null) {
            this.jdate = jcal.newCalendarDate(getZone());
            this.cachedFixedDate = Long.MIN_VALUE;
        }
    }
}
