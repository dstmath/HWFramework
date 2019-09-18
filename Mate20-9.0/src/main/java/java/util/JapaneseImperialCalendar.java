package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.LocalGregorianCalendar;
import sun.util.locale.provider.CalendarDataUtility;

class JapaneseImperialCalendar extends Calendar {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int BEFORE_MEIJI = 0;
    private static final Era BEFORE_MEIJI_ERA;
    private static final int EPOCH_OFFSET = 719163;
    private static final int EPOCH_YEAR = 1970;
    public static final int HEISEI = 4;
    static final int[] LEAST_MAX_VALUES = {0, 0, 0, 0, 4, 28, 0, 7, 4, 1, 11, 23, 59, 59, 999, 50400000, 1200000};
    static final int[] MAX_VALUES = {0, 292278994, 11, 53, 6, 31, 366, 7, 6, 1, 11, 23, 59, 59, 999, 50400000, 7200000};
    public static final int MEIJI = 1;
    static final int[] MIN_VALUES = {0, -292275055, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, -46800000, 0};
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
    private transient LocalGregorianCalendar.Date jdate;
    private transient int[] originalFields;
    private transient int[] zoneOffsets;

    static {
        Era era = new Era("BeforeMeiji", "BM", Long.MIN_VALUE, $assertionsDisabled);
        BEFORE_MEIJI_ERA = era;
        Era[] es = jcal.getEras();
        int length = es.length + 1;
        eras = new Era[length];
        sinceFixedDates = new long[length];
        sinceFixedDates[0] = gcal.getFixedDate(BEFORE_MEIJI_ERA.getSinceDate());
        eras[0] = BEFORE_MEIJI_ERA;
        int index = es.length;
        int index2 = 0 + 1;
        int index3 = 0;
        while (index3 < index) {
            Era e = es[index3];
            sinceFixedDates[index2] = gcal.getFixedDate(e.getSinceDate());
            eras[index2] = e;
            index3++;
            index2++;
        }
        int[] iArr = LEAST_MAX_VALUES;
        int[] iArr2 = MAX_VALUES;
        int length2 = eras.length - 1;
        iArr2[0] = length2;
        iArr[0] = length2;
        CalendarDate date = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        int dayOfYear = Integer.MAX_VALUE;
        int year = Integer.MAX_VALUE;
        for (int i = 1; i < eras.length; i++) {
            long fd = sinceFixedDates[i];
            CalendarDate transitionDate = eras[i].getSinceDate();
            date.setDate(transitionDate.getYear(), 1, 1);
            long fdd = gcal.getFixedDate(date);
            if (fd != fdd) {
                dayOfYear = Math.min(((int) (fd - fdd)) + 1, dayOfYear);
            }
            date.setDate(transitionDate.getYear(), 12, 31);
            long fdd2 = gcal.getFixedDate(date);
            if (fd != fdd2) {
                dayOfYear = Math.min(((int) (fdd2 - fd)) + 1, dayOfYear);
            }
            LocalGregorianCalendar.Date lgd = getCalendarDate(fd - 1);
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
        if (!(obj instanceof JapaneseImperialCalendar) || !super.equals(obj)) {
            return $assertionsDisabled;
        }
        return true;
    }

    public int hashCode() {
        return super.hashCode() ^ this.jdate.hashCode();
    }

    public void add(int field, int amount) {
        int i = field;
        int i2 = amount;
        if (i2 != 0) {
            if (i < 0 || i >= 15) {
                throw new IllegalArgumentException();
            }
            complete();
            if (i == 1) {
                LocalGregorianCalendar.Date d = (LocalGregorianCalendar.Date) this.jdate.clone();
                d.addYear(i2);
                pinDayOfMonth(d);
                set(0, getEraIndex(d));
                set(1, d.getYear());
                set(2, d.getMonth() - 1);
                set(5, d.getDayOfMonth());
            } else if (i == 2) {
                LocalGregorianCalendar.Date d2 = (LocalGregorianCalendar.Date) this.jdate.clone();
                d2.addMonth(i2);
                pinDayOfMonth(d2);
                set(0, getEraIndex(d2));
                set(1, d2.getYear());
                set(2, d2.getMonth() - 1);
                set(5, d2.getDayOfMonth());
            } else if (i == 0) {
                int era = internalGet(0) + i2;
                if (era < 0) {
                    era = 0;
                } else if (era > eras.length - 1) {
                    era = eras.length - 1;
                }
                set(0, era);
            } else {
                long delta = (long) i2;
                long timeOfDay = 0;
                switch (i) {
                    case 3:
                    case 4:
                    case 8:
                        delta *= 7;
                        break;
                    case 9:
                        delta = (long) (i2 / 2);
                        timeOfDay = (long) ((i2 % 2) * 12);
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
                if (i >= 10) {
                    setTimeInMillis(this.time + delta);
                    return;
                }
                long fd = this.cachedFixedDate;
                long delta2 = delta;
                long timeOfDay2 = ((((((timeOfDay + ((long) internalGet(11))) * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000) + ((long) internalGet(14));
                if (timeOfDay2 >= ONE_DAY) {
                    fd++;
                    timeOfDay2 -= ONE_DAY;
                } else if (timeOfDay2 < 0) {
                    fd--;
                    timeOfDay2 += ONE_DAY;
                }
                long fd2 = fd + delta2;
                int zoneOffset = internalGet(15) + internalGet(16);
                setTimeInMillis((((fd2 - 719163) * ONE_DAY) + timeOfDay2) - ((long) zoneOffset));
                int zoneOffset2 = zoneOffset - (internalGet(15) + internalGet(16));
                if (zoneOffset2 != 0) {
                    setTimeInMillis(this.time + ((long) zoneOffset2));
                    if (this.cachedFixedDate != fd2) {
                        setTimeInMillis(this.time - ((long) zoneOffset2));
                    }
                }
            }
        }
    }

    public void roll(int field, boolean up) {
        roll(field, up ? 1 : -1);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0592, code lost:
        r6 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x0595, code lost:
        set(r1, getRolledValue(internalGet(r28), r2, r6, r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x05a0, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0192, code lost:
        r6 = r17;
     */
    public void roll(int field, int amount) {
        int min;
        int min2;
        int monthLength;
        long month1;
        int min3;
        int max;
        int i = field;
        int i2 = amount;
        if (i2 != 0) {
            if (i >= 0 && i < 15) {
                complete();
                int min4 = getMinimum(field);
                int max2 = getMaximum(field);
                switch (i) {
                    case 0:
                    case 9:
                    case 12:
                    case 13:
                    case 14:
                        min = min4;
                        break;
                    case 1:
                        min2 = getActualMinimum(field);
                        max2 = getActualMaximum(field);
                        break;
                    case 2:
                        int min5 = min4;
                        if (!isTransitionYear(this.jdate.getNormalizedYear())) {
                            int year = this.jdate.getYear();
                            if (year == getMaximum(1)) {
                                CalendarDate jd = jcal.getCalendarDate(this.time, getZone());
                                CalendarDate d = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                                int max3 = d.getMonth() - 1;
                                int n = getRolledValue(internalGet(field), i2, min5, max3);
                                if (n == max3) {
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
                            } else if (year == getMinimum(1)) {
                                CalendarDate jd2 = jcal.getCalendarDate(this.time, getZone());
                                CalendarDate d2 = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                                int min6 = d2.getMonth() - 1;
                                int n2 = getRolledValue(internalGet(field), i2, min6, max2);
                                if (n2 == min6) {
                                    jd2.addYear(HttpURLConnection.HTTP_BAD_REQUEST);
                                    jd2.setMonth(n2 + 1);
                                    if (jd2.getDayOfMonth() < d2.getDayOfMonth()) {
                                        jd2.setDayOfMonth(d2.getDayOfMonth());
                                        jcal.normalize(jd2);
                                    }
                                    if (jd2.getDayOfMonth() == d2.getDayOfMonth() && jd2.getTimeOfDay() < d2.getTimeOfDay()) {
                                        jd2.setMonth(n2 + 1);
                                        jd2.setDayOfMonth(d2.getDayOfMonth() + 1);
                                        jcal.normalize(jd2);
                                        n2 = jd2.getMonth() - 1;
                                    }
                                    set(5, jd2.getDayOfMonth());
                                }
                                set(2, n2);
                            } else {
                                int mon = (internalGet(2) + i2) % 12;
                                if (mon < 0) {
                                    mon += 12;
                                }
                                set(2, mon);
                                int monthLen = monthLength(mon);
                                if (internalGet(5) > monthLen) {
                                    set(5, monthLen);
                                }
                            }
                        } else {
                            int eraIndex = getEraIndex(this.jdate);
                            CalendarDate transition = null;
                            if (this.jdate.getYear() == 1) {
                                transition = eras[eraIndex].getSinceDate();
                                min5 = transition.getMonth() - 1;
                            } else if (eraIndex < eras.length - 1) {
                                transition = eras[eraIndex + 1].getSinceDate();
                                if (transition.getYear() == this.jdate.getNormalizedYear()) {
                                    max2 = transition.getMonth() - 1;
                                    if (transition.getDayOfMonth() == 1) {
                                        max2--;
                                    }
                                }
                            }
                            if (min5 != max2) {
                                int n3 = getRolledValue(internalGet(field), i2, min5, max2);
                                set(2, n3);
                                if (n3 == min5) {
                                    if (!(transition.getMonth() == 1 && transition.getDayOfMonth() == 1) && this.jdate.getDayOfMonth() < transition.getDayOfMonth()) {
                                        set(5, transition.getDayOfMonth());
                                    }
                                } else if (n3 == max2 && transition.getMonth() - 1 == n3) {
                                    if (this.jdate.getDayOfMonth() >= transition.getDayOfMonth()) {
                                        set(5, dom - 1);
                                    }
                                }
                            } else {
                                return;
                            }
                        }
                        return;
                    case 3:
                        min = min4;
                        int y = this.jdate.getNormalizedYear();
                        int max4 = getActualMaximum(3);
                        set(7, internalGet(7));
                        int woy = internalGet(3);
                        int value = woy + i2;
                        if (!isTransitionYear(this.jdate.getNormalizedYear())) {
                            int year2 = this.jdate.getYear();
                            if (year2 == getMaximum(1)) {
                                max4 = getActualMaximum(3);
                            } else if (year2 == getMinimum(1)) {
                                min = getActualMinimum(3);
                                max4 = getActualMaximum(3);
                                if (value > min && value < max4) {
                                    set(3, value);
                                    return;
                                }
                            }
                            if (value <= min || value >= max4) {
                                long fd = this.cachedFixedDate;
                                long day1 = fd - ((long) ((woy - min) * 7));
                                if (year2 != getMinimum(1)) {
                                    if (gcal.getYearFromFixedDate(day1) != y) {
                                        min++;
                                    }
                                } else {
                                    int i3 = value;
                                    int i4 = year2;
                                    if (day1 < jcal.getFixedDate(jcal.getCalendarDate(Long.MIN_VALUE, getZone()))) {
                                        min++;
                                    }
                                }
                                if (gcal.getYearFromFixedDate(fd + ((long) (7 * (max4 - internalGet(3))))) != y) {
                                    max4--;
                                }
                                max2 = max4;
                                break;
                            } else {
                                set(3, value);
                                return;
                            }
                        } else {
                            int i5 = value;
                            long fd2 = this.cachedFixedDate;
                            long day12 = fd2 - ((long) (7 * (woy - min)));
                            LocalGregorianCalendar.Date d3 = getCalendarDate(day12);
                            if (!(d3.getEra() == this.jdate.getEra() && d3.getYear() == this.jdate.getYear())) {
                                min++;
                            }
                            jcal.getCalendarDateFromFixedDate(d3, fd2 + ((long) (7 * (max4 - woy))));
                            if (!(d3.getEra() == this.jdate.getEra() && d3.getYear() == this.jdate.getYear())) {
                                max4--;
                            }
                            LocalGregorianCalendar.Date d4 = getCalendarDate(((long) ((getRolledValue(woy, i2, min, max4) - 1) * 7)) + day12);
                            set(2, d4.getMonth() - 1);
                            set(5, d4.getDayOfMonth());
                            return;
                        }
                    case 4:
                        boolean isTransitionYear = isTransitionYear(this.jdate.getNormalizedYear());
                        int dow = internalGet(7) - getFirstDayOfWeek();
                        if (dow < 0) {
                            dow += 7;
                        }
                        long fd3 = this.cachedFixedDate;
                        if (isTransitionYear) {
                            month1 = getFixedDateMonth1(this.jdate, fd3);
                            monthLength = actualMonthLength();
                        } else {
                            month1 = (fd3 - ((long) internalGet(5))) + 1;
                            monthLength = jcal.getMonthLength(this.jdate);
                        }
                        int monthLength2 = monthLength;
                        long monthDay1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(month1 + 6, getFirstDayOfWeek());
                        long j = fd3;
                        if (((int) (monthDay1st - month1)) >= getMinimalDaysInFirstWeek()) {
                            monthDay1st -= 7;
                        }
                        int value2 = getRolledValue(internalGet(field), i2, 1, getActualMaximum(field)) - 1;
                        int i6 = value2;
                        long j2 = monthDay1st;
                        long nfd = ((long) (value2 * 7)) + monthDay1st + ((long) dow);
                        if (nfd < month1) {
                            nfd = month1;
                        } else if (nfd >= ((long) monthLength2) + month1) {
                            nfd = (((long) monthLength2) + month1) - 1;
                        }
                        set(5, ((int) (nfd - month1)) + 1);
                        return;
                    case 5:
                        min = min4;
                        if (!isTransitionYear(this.jdate.getNormalizedYear())) {
                            max2 = jcal.getMonthLength(this.jdate);
                            break;
                        } else {
                            long month12 = getFixedDateMonth1(this.jdate, this.cachedFixedDate);
                            set(5, getCalendarDate(((long) getRolledValue((int) (this.cachedFixedDate - month12), i2, 0, actualMonthLength() - 1)) + month12).getDayOfMonth());
                            return;
                        }
                    case 6:
                        min3 = min4;
                        max = getActualMaximum(field);
                        if (isTransitionYear(this.jdate.getNormalizedYear())) {
                            LocalGregorianCalendar.Date d5 = getCalendarDate(((long) getRolledValue(internalGet(6), i2, min3, max)) + (this.cachedFixedDate - ((long) internalGet(6))));
                            set(2, d5.getMonth() - 1);
                            set(5, d5.getDayOfMonth());
                            return;
                        }
                        break;
                    case 7:
                        min3 = min4;
                        if (!isTransitionYear(this.jdate.getNormalizedYear()) && !isTransitionYear(normalizedYear - 1)) {
                            int weekOfYear = internalGet(3);
                            if (weekOfYear > 1 && weekOfYear < 52) {
                                set(3, internalGet(3));
                                max = 7;
                                break;
                            }
                        }
                        int amount2 = i2 % 7;
                        if (amount2 != 0) {
                            long fd4 = this.cachedFixedDate;
                            long dowFirst = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fd4, getFirstDayOfWeek());
                            long fd5 = fd4 + ((long) amount2);
                            if (fd5 < dowFirst) {
                                fd5 += 7;
                            } else if (fd5 >= dowFirst + 7) {
                                fd5 -= 7;
                            }
                            LocalGregorianCalendar.Date d6 = getCalendarDate(fd5);
                            set(0, getEraIndex(d6));
                            set(d6.getYear(), d6.getMonth() - 1, d6.getDayOfMonth());
                            return;
                        }
                        return;
                    case 8:
                        min2 = 1;
                        if (!isTransitionYear(this.jdate.getNormalizedYear())) {
                            int dom = internalGet(5);
                            int monthLength3 = jcal.getMonthLength(this.jdate);
                            max2 = monthLength3 / 7;
                            if ((dom - 1) % 7 < monthLength3 % 7) {
                                max2++;
                            }
                            set(7, internalGet(7));
                            break;
                        } else {
                            long fd6 = this.cachedFixedDate;
                            long month13 = getFixedDateMonth1(this.jdate, fd6);
                            int monthLength4 = actualMonthLength();
                            int max5 = monthLength4 / 7;
                            int x = ((int) (fd6 - month13)) % 7;
                            if (x < monthLength4 % 7) {
                                max5++;
                            }
                            int i7 = monthLength4;
                            set(5, getCalendarDate(((long) ((getRolledValue(internalGet(field), i2, 1, max5) - 1) * 7)) + month13 + ((long) x)).getDayOfMonth());
                            return;
                        }
                    case 10:
                    case 11:
                        int unit = max2 + 1;
                        int h = internalGet(field);
                        int nh = (h + i2) % unit;
                        if (nh < 0) {
                            nh += unit;
                        }
                        int i8 = min4;
                        this.time += (long) (ONE_HOUR * (nh - h));
                        CalendarDate d7 = jcal.getCalendarDate(this.time, getZone());
                        if (internalGet(5) != d7.getDayOfMonth()) {
                            d7.setEra(this.jdate.getEra());
                            d7.setDate(internalGet(1), internalGet(2) + 1, internalGet(5));
                            if (i == 10) {
                                d7.addHours(12);
                            }
                            this.time = jcal.getTime(d7);
                        }
                        int hourOfDay = d7.getHours();
                        internalSet(i, hourOfDay % unit);
                        if (i == 10) {
                            internalSet(11, hourOfDay);
                        } else {
                            internalSet(9, hourOfDay / 12);
                            internalSet(10, hourOfDay % 12);
                        }
                        int zoneOffset = d7.getZoneOffset();
                        int saving = d7.getDaylightSaving();
                        internalSet(15, zoneOffset - saving);
                        internalSet(16, saving);
                        return;
                    default:
                        min = min4;
                        break;
                }
            } else {
                throw new IllegalArgumentException();
            }
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
                Set<Integer> values = new HashSet<>();
                for (String key : names.keySet()) {
                    values.add(names.get(key));
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
        if (field != 1) {
            return MAX_VALUES[field];
        }
        return Math.max(LEAST_MAX_VALUES[1], jcal.getCalendarDate((long) Long.MAX_VALUE, getZone()).getYear());
    }

    public int getGreatestMinimum(int field) {
        if (field == 1) {
            return 1;
        }
        return MIN_VALUES[field];
    }

    public int getLeastMaximum(int field) {
        if (field != 1) {
            return LEAST_MAX_VALUES[field];
        }
        return Math.min(LEAST_MAX_VALUES[1], getMaximum(1));
    }

    public int getActualMinimum(int field) {
        long fd;
        int i = field;
        if (!isFieldSet(14, i)) {
            return getMinimum(field);
        }
        int value = 0;
        LocalGregorianCalendar.Date jd = jcal.getCalendarDate(getNormalizedCalendar().getTimeInMillis(), getZone());
        int eraIndex = getEraIndex(jd);
        switch (i) {
            case 1:
                if (eraIndex <= 0) {
                    value = getMinimum(field);
                    CalendarDate d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
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
                } else {
                    value = 1;
                    CalendarDate d2 = jcal.getCalendarDate(eras[eraIndex].getSince(getZone()), getZone());
                    jd.setYear(d2.getYear());
                    jcal.normalize(jd);
                    if (getYearOffsetInMillis(jd) < getYearOffsetInMillis(d2)) {
                        value = 1 + 1;
                        break;
                    }
                }
                break;
            case 2:
                if (eraIndex > 1 && jd.getYear() == 1) {
                    CalendarDate d3 = jcal.getCalendarDate(eras[eraIndex].getSince(getZone()), getZone());
                    int value2 = d3.getMonth() - 1;
                    if (jd.getDayOfMonth() < d3.getDayOfMonth()) {
                        value2++;
                    }
                    value = value2;
                    break;
                }
            case 3:
                value = 1;
                CalendarDate d4 = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                d4.addYear(HttpURLConnection.HTTP_BAD_REQUEST);
                jcal.normalize(d4);
                jd.setEra(d4.getEra());
                jd.setYear(d4.getYear());
                jcal.normalize(jd);
                long jan1 = jcal.getFixedDate(d4);
                long day1 = fd - ((long) (7 * (getWeekNumber(jan1, jcal.getFixedDate(jd)) - 1)));
                if (day1 < jan1 || (day1 == jan1 && jd.getTimeOfDay() < d4.getTimeOfDay())) {
                    value = 1 + 1;
                    break;
                }
        }
        return value;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: java.util.JapaneseImperialCalendar} */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Multi-variable type inference failed */
    public int getActualMaximum(int field) {
        int value;
        int value2;
        CalendarDate d;
        int fieldsForFixedMax;
        int fixedDate;
        int i = field;
        if (((1 << i) & 130689) != 0) {
            return getMaximum(field);
        }
        JapaneseImperialCalendar jc = getNormalizedCalendar();
        LocalGregorianCalendar.Date date = jc.jdate;
        int normalizedYear = date.getNormalizedYear();
        switch (i) {
            case 1:
                CalendarDate jd = jcal.getCalendarDate(jc.getTimeInMillis(), getZone());
                int eraIndex = getEraIndex(date);
                if (eraIndex == eras.length - 1) {
                    d = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    value2 = d.getYear();
                    if (value2 > 400) {
                        jd.setYear(value2 - 400);
                    }
                } else {
                    d = jcal.getCalendarDate(eras[eraIndex + 1].getSince(getZone()) - 1, getZone());
                    value2 = d.getYear();
                    jd.setYear(value2);
                }
                jcal.normalize(jd);
                if (getYearOffsetInMillis(jd) > getYearOffsetInMillis(d)) {
                    value2--;
                }
                value = value2;
                break;
            case 2:
                if (!isTransitionYear(date.getNormalizedYear())) {
                    LocalGregorianCalendar.Date d2 = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    if (date.getEra() != d2.getEra() || date.getYear() != d2.getYear()) {
                        value = 11;
                        break;
                    } else {
                        value = d2.getMonth() - 1;
                        break;
                    }
                } else {
                    int eraIndex2 = getEraIndex(date);
                    if (date.getYear() != 1) {
                        eraIndex2++;
                    }
                    long transition = sinceFixedDates[eraIndex2];
                    if (jc.cachedFixedDate >= transition) {
                        value = 11;
                        break;
                    } else {
                        LocalGregorianCalendar.Date ldate = (LocalGregorianCalendar.Date) date.clone();
                        jcal.getCalendarDateFromFixedDate(ldate, transition - 1);
                        value = ldate.getMonth() - 1;
                        break;
                    }
                }
                break;
            case 3:
                if (isTransitionYear(date.getNormalizedYear())) {
                    if (jc == this) {
                        jc = jc.clone();
                    }
                    int max = getActualMaximum(6);
                    jc.set(6, max);
                    int value3 = jc.get(3);
                    if (value3 == 1 && max > 7) {
                        jc.add(3, -1);
                        value = jc.get(3);
                        break;
                    } else {
                        value = value3;
                        break;
                    }
                } else {
                    LocalGregorianCalendar.Date jd2 = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    if (date.getEra() != jd2.getEra() || date.getYear() != jd2.getYear()) {
                        if (date.getEra() != null || date.getYear() != getMinimum(1)) {
                            CalendarDate d3 = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                            d3.setDate(date.getNormalizedYear(), 1, 1);
                            int dayOfWeek = gcal.getDayOfWeek(d3) - getFirstDayOfWeek();
                            if (dayOfWeek < 0) {
                                dayOfWeek += 7;
                            }
                            int value4 = 52;
                            int magic = (getMinimalDaysInFirstWeek() + dayOfWeek) - 1;
                            if (magic == 6 || (date.isLeapYear() && (magic == 5 || magic == 12))) {
                                value4 = 52 + 1;
                            }
                            value = value4;
                            break;
                        } else {
                            CalendarDate d4 = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                            d4.addYear(HttpURLConnection.HTTP_BAD_REQUEST);
                            jcal.normalize(d4);
                            jd2.setEra(d4.getEra());
                            jd2.setDate(d4.getYear() + 1, 1, 1);
                            jcal.normalize(jd2);
                            long jan1 = jcal.getFixedDate(d4);
                            long nextJan1 = jcal.getFixedDate(jd2);
                            long nextJan1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan1, getFirstDayOfWeek());
                            LocalGregorianCalendar.Date date2 = jd2;
                            if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek()) {
                                nextJan1st -= 7;
                            }
                            value = getWeekNumber(jan1, nextJan1st);
                            break;
                        }
                    } else {
                        long fd = jcal.getFixedDate(jd2);
                        value = getWeekNumber(getFixedDateJan1(jd2, fd), fd);
                        break;
                    }
                }
                break;
            case 4:
                LocalGregorianCalendar.Date jd3 = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                if (date.getEra() != jd3.getEra() || date.getYear() != jd3.getYear()) {
                    CalendarDate d5 = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d5.setDate(date.getNormalizedYear(), date.getMonth(), 1);
                    int dayOfWeek2 = gcal.getDayOfWeek(d5);
                    int monthLength = gcal.getMonthLength(d5);
                    int dayOfWeek3 = dayOfWeek2 - getFirstDayOfWeek();
                    if (dayOfWeek3 < 0) {
                        dayOfWeek3 += 7;
                    }
                    int nDaysFirstWeek = 7 - dayOfWeek3;
                    int value5 = 3;
                    if (nDaysFirstWeek >= getMinimalDaysInFirstWeek()) {
                        value5 = 3 + 1;
                    }
                    int monthLength2 = monthLength - (nDaysFirstWeek + 21);
                    if (monthLength2 > 0) {
                        value5++;
                        if (monthLength2 > 7) {
                            value5++;
                        }
                    }
                    value = value5;
                    break;
                } else {
                    long fd2 = jcal.getFixedDate(jd3);
                    value = getWeekNumber((fd2 - ((long) jd3.getDayOfMonth())) + 1, fd2);
                    break;
                }
            case 5:
                value = jcal.getMonthLength(date);
                break;
            case 6:
                if (!isTransitionYear(date.getNormalizedYear())) {
                    LocalGregorianCalendar.Date d6 = jcal.getCalendarDate((long) Long.MAX_VALUE, getZone());
                    if (date.getEra() != d6.getEra() || date.getYear() != d6.getYear()) {
                        if (date.getYear() == getMinimum(1)) {
                            CalendarDate d1 = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                            long fd1 = jcal.getFixedDate(d1);
                            d1.addYear(1);
                            d1.setMonth(1).setDayOfMonth(1);
                            jcal.normalize(d1);
                            fieldsForFixedMax = (int) (jcal.getFixedDate(d1) - fd1);
                        } else {
                            fieldsForFixedMax = jcal.getYearLength(date);
                        }
                        value = fieldsForFixedMax;
                        break;
                    } else {
                        long fd3 = jcal.getFixedDate(d6);
                        value = ((int) (fd3 - getFixedDateJan1(d6, fd3))) + 1;
                        break;
                    }
                } else {
                    int eraIndex3 = getEraIndex(date);
                    if (date.getYear() != 1) {
                        eraIndex3++;
                    }
                    long transition2 = sinceFixedDates[eraIndex3];
                    long fd4 = jc.cachedFixedDate;
                    CalendarDate d7 = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d7.setDate(date.getNormalizedYear(), 1, 1);
                    if (fd4 < transition2) {
                        fixedDate = (int) (transition2 - gcal.getFixedDate(d7));
                    } else {
                        d7.addYear(1);
                        fixedDate = (int) (gcal.getFixedDate(d7) - transition2);
                    }
                    value = fixedDate;
                }
                break;
            case 8:
                int dow = date.getDayOfWeek();
                BaseCalendar.Date d8 = (BaseCalendar.Date) date.clone();
                int ndays = jcal.getMonthLength(d8);
                d8.setDayOfMonth(1);
                jcal.normalize(d8);
                int x = dow - d8.getDayOfWeek();
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

    private long getYearOffsetInMillis(CalendarDate date) {
        return (date.getTimeOfDay() + ((jcal.getDayOfYear(date) - 1) * ONE_DAY)) - ((long) date.getZoneOffset());
    }

    public Object clone() {
        JapaneseImperialCalendar other = (JapaneseImperialCalendar) super.clone();
        other.jdate = (LocalGregorianCalendar.Date) this.jdate.clone();
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

    /* access modifiers changed from: protected */
    public void computeFields() {
        int mask;
        if (isPartiallyNormalized()) {
            mask = getSetStateFields();
            int fieldMask = (~mask) & 131071;
            if (fieldMask != 0 || this.cachedFixedDate == Long.MIN_VALUE) {
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
        long fixedDateJan1;
        int dayOfYear;
        int normalizedYear;
        long j;
        int i;
        long nextJan1;
        long prevJan1;
        int i2 = fieldMask;
        int i3 = tzMask;
        int zoneOffset = 0;
        TimeZone tz = getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        if (i3 != 98304) {
            zoneOffset = tz.getOffset(this.time);
            this.zoneOffsets[0] = tz.getRawOffset();
            this.zoneOffsets[1] = zoneOffset - this.zoneOffsets[0];
        }
        if (i3 != 0) {
            if (isFieldSet(i3, 15)) {
                this.zoneOffsets[0] = internalGet(15);
            }
            if (isFieldSet(i3, 16)) {
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
        if (fixedDate2 != this.cachedFixedDate || fixedDate2 < 0) {
            jcal.getCalendarDateFromFixedDate(this.jdate, fixedDate2);
            this.cachedFixedDate = fixedDate2;
        }
        int era = getEraIndex(this.jdate);
        int year = this.jdate.getYear();
        internalSet(0, era);
        internalSet(1, year);
        int mask = i2 | 3;
        int month = this.jdate.getMonth() - 1;
        int dayOfMonth = this.jdate.getDayOfMonth();
        if ((i2 & 164) != 0) {
            internalSet(2, month);
            internalSet(5, dayOfMonth);
            internalSet(7, this.jdate.getDayOfWeek());
            mask |= 164;
        }
        if ((i2 & 32256) != 0) {
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
            mask |= 32256;
        }
        if ((i2 & 98304) != 0) {
            internalSet(15, this.zoneOffsets[0]);
            internalSet(16, this.zoneOffsets[1]);
            mask |= 98304;
        }
        if ((i2 & 344) != 0) {
            int normalizedYear2 = this.jdate.getNormalizedYear();
            boolean transitionYear = isTransitionYear(this.jdate.getNormalizedYear());
            if (transitionYear) {
                fixedDateJan1 = getFixedDateJan1(this.jdate, fixedDate2);
                int i4 = zoneOffset;
                TimeZone timeZone = tz;
                dayOfYear = ((int) (fixedDate2 - fixedDateJan1)) + 1;
                int i5 = timeOfDay;
            } else {
                TimeZone timeZone2 = tz;
                if (normalizedYear2 == MIN_VALUES[1]) {
                    int i6 = timeOfDay;
                    fixedDateJan1 = jcal.getFixedDate(jcal.getCalendarDate(Long.MIN_VALUE, getZone()));
                    dayOfYear = ((int) (fixedDate2 - fixedDateJan1)) + 1;
                } else {
                    dayOfYear = (int) jcal.getDayOfYear(this.jdate);
                    fixedDateJan1 = (fixedDate2 - ((long) dayOfYear)) + 1;
                }
            }
            long fixedDateJan12 = fixedDateJan1;
            if (transitionYear) {
                j = getFixedDateMonth1(this.jdate, fixedDate2);
                normalizedYear = normalizedYear2;
            } else {
                normalizedYear = normalizedYear2;
                j = (fixedDate2 - ((long) dayOfMonth)) + 1;
            }
            long fixedDateMonth1 = j;
            internalSet(6, dayOfYear);
            internalSet(8, ((dayOfMonth - 1) / 7) + 1);
            int weekOfYear = getWeekNumber(fixedDateJan12, fixedDate2);
            if (weekOfYear == 0) {
                int i7 = month;
                int i8 = dayOfMonth;
                long fixedDec31 = fixedDateJan12 - 1;
                LocalGregorianCalendar.Date d = getCalendarDate(fixedDec31);
                if (!transitionYear) {
                    int i9 = dayOfYear;
                    if (isTransitionYear(d.getNormalizedYear()) == 0) {
                        prevJan1 = fixedDateJan12 - 365;
                        if (d.isLeapYear()) {
                            prevJan1--;
                        }
                        weekOfYear = getWeekNumber(prevJan1, fixedDec31);
                    }
                }
                if (transitionYear) {
                    int i10 = year;
                    if (this.jdate.getYear() == 1) {
                        if (era > 4) {
                            CalendarDate pd = eras[era - 1].getSinceDate();
                            int i11 = era;
                            int normalizedYear3 = normalizedYear;
                            if (normalizedYear3 == pd.getYear()) {
                                int i12 = normalizedYear3;
                                d.setMonth(pd.getMonth()).setDayOfMonth(pd.getDayOfMonth());
                            }
                        } else {
                            int i13 = normalizedYear;
                            d.setMonth(1).setDayOfMonth(1);
                        }
                        jcal.normalize(d);
                        prevJan1 = jcal.getFixedDate(d);
                    } else {
                        int i14 = normalizedYear;
                        prevJan1 = fixedDateJan12 - 365;
                        if (d.isLeapYear()) {
                            prevJan1--;
                        }
                    }
                } else {
                    int i15 = year;
                    int i16 = normalizedYear;
                    CalendarDate cd = eras[getEraIndex(this.jdate)].getSinceDate();
                    d.setMonth(cd.getMonth()).setDayOfMonth(cd.getDayOfMonth());
                    jcal.normalize(d);
                    prevJan1 = jcal.getFixedDate(d);
                }
                weekOfYear = getWeekNumber(prevJan1, fixedDec31);
            } else {
                int i17 = era;
                int i18 = year;
                int i19 = month;
                int i20 = dayOfMonth;
                int i21 = normalizedYear;
                if (transitionYear) {
                    LocalGregorianCalendar.Date d2 = (LocalGregorianCalendar.Date) this.jdate.clone();
                    if (this.jdate.getYear() == 1) {
                        d2.addYear(1);
                        d2.setMonth(1).setDayOfMonth(1);
                        nextJan1 = jcal.getFixedDate(d2);
                    } else {
                        int nextEraIndex = getEraIndex(d2) + 1;
                        CalendarDate cd2 = eras[nextEraIndex].getSinceDate();
                        d2.setEra(eras[nextEraIndex]);
                        d2.setDate(1, cd2.getMonth(), cd2.getDayOfMonth());
                        jcal.normalize(d2);
                        nextJan1 = jcal.getFixedDate(d2);
                    }
                    long nextJan1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan1, getFirstDayOfWeek());
                    LocalGregorianCalendar.Date date = d2;
                    i = weekOfYear;
                    if (((int) (nextJan1st - nextJan1)) >= getMinimalDaysInFirstWeek() && fixedDate2 >= nextJan1st - 7) {
                        weekOfYear = 1;
                    }
                } else if (weekOfYear >= 52) {
                    long nextJan12 = fixedDateJan12 + 365;
                    if (this.jdate.isLeapYear()) {
                        nextJan12++;
                    }
                    long nextJan1st2 = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(6 + nextJan12, getFirstDayOfWeek());
                    if (((int) (nextJan1st2 - nextJan12)) >= getMinimalDaysInFirstWeek() && fixedDate2 >= nextJan1st2 - 7) {
                        weekOfYear = 1;
                    }
                } else {
                    i = weekOfYear;
                }
                weekOfYear = i;
            }
            internalSet(3, weekOfYear);
            internalSet(4, getWeekNumber(fixedDateMonth1, fixedDate2));
            return mask | 344;
        }
        TimeZone timeZone3 = tz;
        int i22 = era;
        int i23 = year;
        int i24 = timeOfDay;
        int i25 = month;
        int i26 = dayOfMonth;
        return mask;
    }

    private int getWeekNumber(long fixedDay1, long fixedDate) {
        long fixedDay1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(6 + fixedDay1, getFirstDayOfWeek());
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
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0136  */
    public void computeTime() {
        int year;
        int era;
        long timeOfDay;
        char c;
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
        if (isSet(0)) {
            era = internalGet(0);
            year = isSet(1) ? internalGet(1) : 1;
        } else if (isSet(1) != 0) {
            era = eras.length - 1;
            year = internalGet(1);
        } else {
            era = 3;
            year = 45;
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
        long fixedDate = timeOfDay2 / ONE_DAY;
        long timeOfDay3 = timeOfDay2 % ONE_DAY;
        while (timeOfDay3 < 0) {
            timeOfDay3 += ONE_DAY;
            fixedDate--;
        }
        long millis = (((fixedDate + getFixedDate(era, year, fieldMask)) - 719163) * ONE_DAY) + timeOfDay3;
        TimeZone zone = getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        int tzMask = fieldMask & 98304;
        if (tzMask != 98304) {
            int i = year;
            zone.getOffsets(millis - ((long) zone.getRawOffset()), this.zoneOffsets);
        }
        if (tzMask != 0) {
            if (isFieldSet(tzMask, 15)) {
                this.zoneOffsets[0] = internalGet(15);
            }
            if (isFieldSet(tzMask, 16)) {
                c = 1;
                this.zoneOffsets[1] = internalGet(16);
                this.time = millis - ((long) (this.zoneOffsets[0] + this.zoneOffsets[c]));
                int mask = computeFields(getSetStateFields() | fieldMask, tzMask);
                if (!isLenient()) {
                    int field2 = 0;
                    for (int i2 = 17; field2 < i2; i2 = 17) {
                        if (isExternallySet(field2) && this.originalFields[field2] != internalGet(field2)) {
                            int wrongValue = internalGet(field2);
                            int i3 = fieldMask;
                            int i4 = era;
                            System.arraycopy((Object) this.originalFields, 0, (Object) this.fields, 0, this.fields.length);
                            throw new IllegalArgumentException(getFieldName(field2) + "=" + wrongValue + ", expected " + this.originalFields[field2]);
                        }
                        field2++;
                    }
                }
                int i5 = era;
                setFieldsNormalized(mask);
            }
        }
        c = 1;
        this.time = millis - ((long) (this.zoneOffsets[0] + this.zoneOffsets[c]));
        int mask2 = computeFields(getSetStateFields() | fieldMask, tzMask);
        if (!isLenient()) {
        }
        int i52 = era;
        setFieldsNormalized(mask2);
    }

    private long getFixedDate(int era, int year, int fieldMask) {
        long fixedDate;
        LocalGregorianCalendar.Date date;
        int dayOfWeek;
        int dowim;
        long fixedDate2;
        int year2 = year;
        int i = fieldMask;
        int month = 0;
        int firstDayOfMonth = 1;
        if (isFieldSet(i, 2)) {
            month = internalGet(2);
            if (month > 11) {
                year2 += month / 12;
                month %= 12;
            } else if (month < 0) {
                int[] rem = new int[1];
                year2 += CalendarUtils.floorDivide(month, 12, rem);
                month = rem[0];
            }
        } else if (year2 == 1 && era != 0) {
            CalendarDate d = eras[era].getSinceDate();
            month = d.getMonth() - 1;
            firstDayOfMonth = d.getDayOfMonth();
        }
        if (year2 == MIN_VALUES[1]) {
            CalendarDate dx = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
            int m = dx.getMonth() - 1;
            if (month < m) {
                month = m;
            }
            if (month == m) {
                firstDayOfMonth = dx.getDayOfMonth();
            }
        }
        LocalGregorianCalendar.Date date2 = jcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        date2.setEra(era > 0 ? eras[era] : null);
        date2.setDate(year2, month + 1, firstDayOfMonth);
        jcal.normalize(date2);
        long fixedDate3 = jcal.getFixedDate(date2);
        if (!isFieldSet(i, 2)) {
            LocalGregorianCalendar.Date date3 = date2;
            long fixedDate4 = fixedDate3;
            if (isFieldSet(i, 6)) {
                LocalGregorianCalendar.Date date4 = date3;
                if (isTransitionYear(date4.getNormalizedYear())) {
                    fixedDate = getFixedDateJan1(date4, fixedDate4);
                } else {
                    fixedDate = fixedDate4;
                }
                return (fixedDate + ((long) internalGet(6))) - 1;
            }
            long fixedDate5 = fixedDate4;
            long firstDayOfWeek = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fixedDate5 + 6, getFirstDayOfWeek());
            if (firstDayOfWeek - fixedDate5 >= ((long) getMinimalDaysInFirstWeek())) {
                firstDayOfWeek -= 7;
            }
            if (isFieldSet(i, 7)) {
                int dayOfWeek2 = internalGet(7);
                if (dayOfWeek2 != getFirstDayOfWeek()) {
                    firstDayOfWeek = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek + 6, dayOfWeek2);
                }
            }
            return firstDayOfWeek + (7 * (((long) internalGet(3)) - 1));
        } else if (!isFieldSet(i, 5)) {
            if (isFieldSet(i, 4)) {
                date = date2;
                long firstDayOfWeek2 = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fixedDate3 + 6, getFirstDayOfWeek());
                long j = fixedDate3;
                if (firstDayOfWeek2 - fixedDate3 >= ((long) getMinimalDaysInFirstWeek())) {
                    firstDayOfWeek2 -= 7;
                }
                if (isFieldSet(i, 7)) {
                    firstDayOfWeek2 = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek2 + 6, internalGet(7));
                }
                fixedDate2 = firstDayOfWeek2 + ((long) (7 * (internalGet(4) - 1)));
            } else {
                date = date2;
                long fixedDate6 = fixedDate3;
                if (isFieldSet(i, 7)) {
                    dayOfWeek = internalGet(7);
                } else {
                    dayOfWeek = getFirstDayOfWeek();
                }
                if (isFieldSet(i, 8)) {
                    dowim = internalGet(8);
                } else {
                    dowim = 1;
                }
                int dowim2 = dowim;
                if (dowim2 >= 0) {
                    fixedDate2 = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore((fixedDate6 + ((long) (7 * dowim2))) - 1, dayOfWeek);
                } else {
                    fixedDate2 = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore((fixedDate6 + ((long) (monthLength(month, year2) + (7 * (dowim2 + 1))))) - 1, dayOfWeek);
                }
            }
            return fixedDate2;
        } else if (isSet(5)) {
            return (fixedDate3 + ((long) internalGet(5))) - ((long) firstDayOfMonth);
        } else {
            return fixedDate3;
        }
    }

    private long getFixedDateJan1(LocalGregorianCalendar.Date date, long fixedDate) {
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

    private long getFixedDateMonth1(LocalGregorianCalendar.Date date, long fixedDate) {
        int eraIndex = getTransitionEraIndex(date);
        if (eraIndex != -1) {
            long transition = sinceFixedDates[eraIndex];
            if (transition <= fixedDate) {
                return transition;
            }
        }
        return (fixedDate - ((long) date.getDayOfMonth())) + 1;
    }

    private static LocalGregorianCalendar.Date getCalendarDate(long fd) {
        LocalGregorianCalendar.Date d = jcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        jcal.getCalendarDateFromFixedDate(d, fd);
        return d;
    }

    private int monthLength(int month, int gregorianYear) {
        return CalendarUtils.isGregorianLeapYear(gregorianYear) ? GregorianCalendar.LEAP_MONTH_LENGTH[month] : GregorianCalendar.MONTH_LENGTH[month];
    }

    private int monthLength(int month) {
        return this.jdate.isLeapYear() ? GregorianCalendar.LEAP_MONTH_LENGTH[month] : GregorianCalendar.MONTH_LENGTH[month];
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

    private static int getTransitionEraIndex(LocalGregorianCalendar.Date date) {
        int eraIndex = getEraIndex(date);
        CalendarDate transitionDate = eras[eraIndex].getSinceDate();
        if (transitionDate.getYear() == date.getNormalizedYear() && transitionDate.getMonth() == date.getMonth()) {
            return eraIndex;
        }
        if (eraIndex < eras.length - 1) {
            int eraIndex2 = eraIndex + 1;
            CalendarDate transitionDate2 = eras[eraIndex2].getSinceDate();
            if (transitionDate2.getYear() == date.getNormalizedYear() && transitionDate2.getMonth() == date.getMonth()) {
                return eraIndex2;
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
        return $assertionsDisabled;
    }

    private static int getEraIndex(LocalGregorianCalendar.Date date) {
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
        JapaneseImperialCalendar jc = (JapaneseImperialCalendar) clone();
        jc.setLenient(true);
        jc.complete();
        return jc;
    }

    private void pinDayOfMonth(LocalGregorianCalendar.Date date) {
        int year = date.getYear();
        int dom = date.getDayOfMonth();
        if (year != getMinimum(1)) {
            date.setDayOfMonth(1);
            jcal.normalize(date);
            int monthLength = jcal.getMonthLength(date);
            if (dom > monthLength) {
                date.setDayOfMonth(monthLength);
            } else {
                date.setDayOfMonth(dom);
            }
            jcal.normalize(date);
            return;
        }
        LocalGregorianCalendar.Date d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
        LocalGregorianCalendar.Date realDate = jcal.getCalendarDate(this.time, getZone());
        long tod = realDate.getTimeOfDay();
        realDate.addYear((int) HttpURLConnection.HTTP_BAD_REQUEST);
        realDate.setMonth(date.getMonth());
        realDate.setDayOfMonth(1);
        jcal.normalize(realDate);
        int monthLength2 = jcal.getMonthLength(realDate);
        if (dom > monthLength2) {
            realDate.setDayOfMonth(monthLength2);
        } else if (dom < d.getDayOfMonth()) {
            realDate.setDayOfMonth(d.getDayOfMonth());
        } else {
            realDate.setDayOfMonth(dom);
        }
        if (realDate.getDayOfMonth() == d.getDayOfMonth() && tod < d.getTimeOfDay()) {
            realDate.setDayOfMonth(Math.min(dom + 1, monthLength2));
        }
        date.setDate(year, realDate.getMonth(), realDate.getDayOfMonth());
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
