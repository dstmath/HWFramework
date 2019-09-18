package java.util;

import java.awt.font.NumericShaper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.LocaleData;
import sun.util.locale.provider.CalendarDataUtility;

public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int ALL_FIELDS = 131071;
    public static final int ALL_STYLES = 0;
    public static final int AM = 0;
    public static final int AM_PM = 9;
    static final int AM_PM_MASK = 512;
    public static final int APRIL = 3;
    public static final int AUGUST = 7;
    private static final int COMPUTED = 1;
    public static final int DATE = 5;
    static final int DATE_MASK = 32;
    public static final int DAY_OF_MONTH = 5;
    static final int DAY_OF_MONTH_MASK = 32;
    public static final int DAY_OF_WEEK = 7;
    public static final int DAY_OF_WEEK_IN_MONTH = 8;
    static final int DAY_OF_WEEK_IN_MONTH_MASK = 256;
    static final int DAY_OF_WEEK_MASK = 128;
    public static final int DAY_OF_YEAR = 6;
    static final int DAY_OF_YEAR_MASK = 64;
    public static final int DECEMBER = 11;
    public static final int DST_OFFSET = 16;
    static final int DST_OFFSET_MASK = 65536;
    public static final int ERA = 0;
    static final int ERA_MASK = 1;
    public static final int FEBRUARY = 1;
    public static final int FIELD_COUNT = 17;
    private static final String[] FIELD_NAME = {"ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET", "DST_OFFSET"};
    public static final int FRIDAY = 6;
    public static final int HOUR = 10;
    static final int HOUR_MASK = 1024;
    public static final int HOUR_OF_DAY = 11;
    static final int HOUR_OF_DAY_MASK = 2048;
    public static final int JANUARY = 0;
    public static final int JULY = 6;
    public static final int JUNE = 5;
    public static final int LONG = 2;
    public static final int LONG_FORMAT = 2;
    public static final int LONG_STANDALONE = 32770;
    public static final int MARCH = 2;
    public static final int MAY = 4;
    public static final int MILLISECOND = 14;
    static final int MILLISECOND_MASK = 16384;
    private static final int MINIMUM_USER_STAMP = 2;
    public static final int MINUTE = 12;
    static final int MINUTE_MASK = 4096;
    public static final int MONDAY = 2;
    public static final int MONTH = 2;
    static final int MONTH_MASK = 4;
    public static final int NARROW_FORMAT = 4;
    public static final int NARROW_STANDALONE = 32772;
    public static final int NOVEMBER = 10;
    public static final int OCTOBER = 9;
    public static final int PM = 1;
    public static final int SATURDAY = 7;
    public static final int SECOND = 13;
    static final int SECOND_MASK = 8192;
    public static final int SEPTEMBER = 8;
    public static final int SHORT = 1;
    public static final int SHORT_FORMAT = 1;
    public static final int SHORT_STANDALONE = 32769;
    static final int STANDALONE_MASK = 32768;
    public static final int SUNDAY = 1;
    public static final int THURSDAY = 5;
    public static final int TUESDAY = 3;
    public static final int UNDECIMBER = 12;
    private static final int UNSET = 0;
    public static final int WEDNESDAY = 4;
    public static final int WEEK_OF_MONTH = 4;
    static final int WEEK_OF_MONTH_MASK = 16;
    public static final int WEEK_OF_YEAR = 3;
    static final int WEEK_OF_YEAR_MASK = 8;
    public static final int YEAR = 1;
    static final int YEAR_MASK = 2;
    public static final int ZONE_OFFSET = 15;
    static final int ZONE_OFFSET_MASK = 32768;
    private static final ConcurrentMap<Locale, int[]> cachedLocaleData = new ConcurrentHashMap(3);
    static final int currentSerialVersion = 1;
    static final long serialVersionUID = -1807547505821590642L;
    transient boolean areAllFieldsSet;
    protected boolean areFieldsSet;
    protected int[] fields;
    private int firstDayOfWeek;
    protected boolean[] isSet;
    protected boolean isTimeSet;
    private boolean lenient;
    private int minimalDaysInFirstWeek;
    private int nextStamp;
    private int serialVersionOnStream;
    private transient boolean sharedZone;
    private transient int[] stamp;
    protected long time;
    private TimeZone zone;

    private static class AvailableCalendarTypes {
        /* access modifiers changed from: private */
        public static final Set<String> SET;

        static {
            Set<String> set = new HashSet<>(3);
            set.add("gregory");
            SET = Collections.unmodifiableSet(set);
        }

        private AvailableCalendarTypes() {
        }
    }

    public static class Builder {
        private static final int NFIELDS = 18;
        private static final int WEEK_YEAR = 17;
        private int[] fields;
        private int firstDayOfWeek;
        private long instant;
        private boolean lenient = true;
        private Locale locale;
        private int maxFieldIndex;
        private int minimalDaysInFirstWeek;
        private int nextStamp;
        private String type;
        private TimeZone zone;

        public Builder setInstant(long instant2) {
            if (this.fields == null) {
                this.instant = instant2;
                this.nextStamp = 1;
                return this;
            }
            throw new IllegalStateException();
        }

        public Builder setInstant(Date instant2) {
            return setInstant(instant2.getTime());
        }

        public Builder set(int field, int value) {
            if (field < 0 || field >= 17) {
                throw new IllegalArgumentException("field is invalid");
            } else if (!isInstantSet()) {
                allocateFields();
                internalSet(field, value);
                return this;
            } else {
                throw new IllegalStateException("instant has been set");
            }
        }

        public Builder setFields(int... fieldValuePairs) {
            int len = fieldValuePairs.length;
            if (len % 2 != 0) {
                throw new IllegalArgumentException();
            } else if (isInstantSet()) {
                throw new IllegalStateException("instant has been set");
            } else if (this.nextStamp + (len / 2) >= 0) {
                allocateFields();
                int i = 0;
                while (i < len) {
                    int i2 = i + 1;
                    int field = fieldValuePairs[i];
                    if (field < 0 || field >= 17) {
                        throw new IllegalArgumentException("field is invalid");
                    }
                    internalSet(field, fieldValuePairs[i2]);
                    i = i2 + 1;
                }
                return this;
            } else {
                throw new IllegalStateException("stamp counter overflow");
            }
        }

        public Builder setDate(int year, int month, int dayOfMonth) {
            return setFields(1, year, 2, month, 5, dayOfMonth);
        }

        public Builder setTimeOfDay(int hourOfDay, int minute, int second) {
            return setTimeOfDay(hourOfDay, minute, second, 0);
        }

        public Builder setTimeOfDay(int hourOfDay, int minute, int second, int millis) {
            return setFields(11, hourOfDay, 12, minute, 13, second, 14, millis);
        }

        public Builder setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
            allocateFields();
            internalSet(17, weekYear);
            internalSet(3, weekOfYear);
            internalSet(7, dayOfWeek);
            return this;
        }

        public Builder setTimeZone(TimeZone zone2) {
            if (zone2 != null) {
                this.zone = zone2;
                return this;
            }
            throw new NullPointerException();
        }

        public Builder setLenient(boolean lenient2) {
            this.lenient = lenient2;
            return this;
        }

        public Builder setCalendarType(String type2) {
            if (type2.equals("gregorian")) {
                type2 = "gregory";
            }
            if (Calendar.getAvailableCalendarTypes().contains(type2) || type2.equals("iso8601")) {
                if (this.type == null) {
                    this.type = type2;
                } else if (!this.type.equals(type2)) {
                    throw new IllegalStateException("calendar type override");
                }
                return this;
            }
            throw new IllegalArgumentException("unknown calendar type: " + type2);
        }

        public Builder setLocale(Locale locale2) {
            if (locale2 != null) {
                this.locale = locale2;
                return this;
            }
            throw new NullPointerException();
        }

        public Builder setWeekDefinition(int firstDayOfWeek2, int minimalDaysInFirstWeek2) {
            if (!isValidWeekParameter(firstDayOfWeek2) || !isValidWeekParameter(minimalDaysInFirstWeek2)) {
                throw new IllegalArgumentException();
            }
            this.firstDayOfWeek = firstDayOfWeek2;
            this.minimalDaysInFirstWeek = minimalDaysInFirstWeek2;
            return this;
        }

        public Calendar build() {
            Calendar cal;
            if (this.locale == null) {
                this.locale = Locale.getDefault();
            }
            if (this.zone == null) {
                this.zone = TimeZone.getDefault();
            }
            if (this.type == null) {
                this.type = this.locale.getUnicodeLocaleType("ca");
            }
            if (this.type == null) {
                this.type = "gregory";
            }
            String str = this.type;
            char c = 65535;
            int hashCode = str.hashCode();
            int weekOfYear = 1;
            if (hashCode != 283776265) {
                if (hashCode == 2095190916 && str.equals("iso8601")) {
                    c = 1;
                }
            } else if (str.equals("gregory")) {
                c = 0;
            }
            switch (c) {
                case 0:
                    cal = new GregorianCalendar(this.zone, this.locale, true);
                    break;
                case 1:
                    GregorianCalendar gcal = new GregorianCalendar(this.zone, this.locale, true);
                    gcal.setGregorianChange(new Date(Long.MIN_VALUE));
                    setWeekDefinition(2, 4);
                    cal = gcal;
                    break;
                default:
                    throw new IllegalArgumentException("unknown calendar type: " + this.type);
            }
            Calendar cal2 = cal;
            cal2.setLenient(this.lenient);
            if (this.firstDayOfWeek != 0) {
                cal2.setFirstDayOfWeek(this.firstDayOfWeek);
                cal2.setMinimalDaysInFirstWeek(this.minimalDaysInFirstWeek);
            }
            if (isInstantSet()) {
                cal2.setTimeInMillis(this.instant);
                cal2.complete();
                return cal2;
            }
            if (this.fields != null) {
                boolean weekDate = isSet(17) && this.fields[17] > this.fields[1];
                if (!weekDate || cal2.isWeekDateSupported()) {
                    for (int stamp = 2; stamp < this.nextStamp; stamp++) {
                        int index = 0;
                        while (true) {
                            if (index <= this.maxFieldIndex) {
                                if (this.fields[index] == stamp) {
                                    cal2.set(index, this.fields[18 + index]);
                                } else {
                                    index++;
                                }
                            }
                        }
                    }
                    if (weekDate) {
                        if (isSet(3)) {
                            weekOfYear = this.fields[21];
                        }
                        cal2.setWeekDate(this.fields[35], weekOfYear, isSet(7) ? this.fields[25] : cal2.getFirstDayOfWeek());
                    }
                    cal2.complete();
                } else {
                    throw new IllegalArgumentException("week date is unsupported by " + this.type);
                }
            }
            return cal2;
        }

        private void allocateFields() {
            if (this.fields == null) {
                this.fields = new int[36];
                this.nextStamp = 2;
                this.maxFieldIndex = -1;
            }
        }

        private void internalSet(int field, int value) {
            int[] iArr = this.fields;
            int i = this.nextStamp;
            this.nextStamp = i + 1;
            iArr[field] = i;
            if (this.nextStamp >= 0) {
                this.fields[18 + field] = value;
                if (field > this.maxFieldIndex && field < 17) {
                    this.maxFieldIndex = field;
                    return;
                }
                return;
            }
            throw new IllegalStateException("stamp counter overflow");
        }

        private boolean isInstantSet() {
            if (this.nextStamp == 1) {
                return true;
            }
            return Calendar.$assertionsDisabled;
        }

        private boolean isSet(int index) {
            if (this.fields == null || this.fields[index] <= 0) {
                return Calendar.$assertionsDisabled;
            }
            return true;
        }

        private boolean isValidWeekParameter(int value) {
            if (value <= 0 || value > 7) {
                return Calendar.$assertionsDisabled;
            }
            return true;
        }
    }

    private static class CalendarAccessControlContext {
        private static final AccessControlContext INSTANCE;

        static {
            RuntimePermission perm = new RuntimePermission("accessClassInPackage.sun.util.calendar");
            PermissionCollection perms = perm.newPermissionCollection();
            perms.add(perm);
            INSTANCE = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, perms)});
        }

        private CalendarAccessControlContext() {
        }
    }

    public abstract void add(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract void computeFields();

    /* access modifiers changed from: protected */
    public abstract void computeTime();

    public abstract int getGreatestMinimum(int i);

    public abstract int getLeastMaximum(int i);

    public abstract int getMaximum(int i);

    public abstract int getMinimum(int i);

    public abstract void roll(int i, boolean z);

    protected Calendar() {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        this.sharedZone = true;
    }

    protected Calendar(TimeZone zone2, Locale aLocale) {
        this.lenient = true;
        this.sharedZone = $assertionsDisabled;
        this.nextStamp = 2;
        this.serialVersionOnStream = 1;
        aLocale = aLocale == null ? Locale.getDefault() : aLocale;
        this.fields = new int[17];
        this.isSet = new boolean[17];
        this.stamp = new int[17];
        this.zone = zone2;
        setWeekCountData(aLocale);
    }

    public static Calendar getInstance() {
        return createCalendar(TimeZone.getDefault(), Locale.getDefault(Locale.Category.FORMAT));
    }

    public static Calendar getInstance(TimeZone zone2) {
        return createCalendar(zone2, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static Calendar getInstance(Locale aLocale) {
        return createCalendar(TimeZone.getDefault(), aLocale);
    }

    public static Calendar getInstance(TimeZone zone2, Locale aLocale) {
        return createCalendar(zone2, aLocale);
    }

    public static Calendar getJapaneseImperialInstance(TimeZone zone2, Locale aLocale) {
        return new JapaneseImperialCalendar(zone2, aLocale);
    }

    private static Calendar createCalendar(TimeZone zone2, Locale aLocale) {
        return new GregorianCalendar(zone2, aLocale);
    }

    public static synchronized Locale[] getAvailableLocales() {
        Locale[] availableLocales;
        synchronized (Calendar.class) {
            availableLocales = DateFormat.getAvailableLocales();
        }
        return availableLocales;
    }

    public final Date getTime() {
        return new Date(getTimeInMillis());
    }

    public final void setTime(Date date) {
        setTimeInMillis(date.getTime());
    }

    public long getTimeInMillis() {
        if (!this.isTimeSet) {
            updateTime();
        }
        return this.time;
    }

    public void setTimeInMillis(long millis) {
        if (this.time != millis || !this.isTimeSet || !this.areFieldsSet || !this.areAllFieldsSet) {
            this.time = millis;
            this.isTimeSet = true;
            this.areFieldsSet = $assertionsDisabled;
            computeFields();
            this.areFieldsSet = true;
            this.areAllFieldsSet = true;
        }
    }

    public int get(int field) {
        complete();
        return internalGet(field);
    }

    /* access modifiers changed from: protected */
    public final int internalGet(int field) {
        return this.fields[field];
    }

    /* access modifiers changed from: package-private */
    public final void internalSet(int field, int value) {
        this.fields[field] = value;
    }

    public void set(int field, int value) {
        if (this.areFieldsSet && !this.areAllFieldsSet) {
            computeFields();
        }
        internalSet(field, value);
        this.isTimeSet = $assertionsDisabled;
        this.areFieldsSet = $assertionsDisabled;
        this.isSet[field] = true;
        int[] iArr = this.stamp;
        int i = this.nextStamp;
        this.nextStamp = i + 1;
        iArr[field] = i;
        if (this.nextStamp == Integer.MAX_VALUE) {
            adjustStamp();
        }
    }

    public final void set(int year, int month, int date) {
        set(1, year);
        set(2, month);
        set(5, date);
    }

    public final void set(int year, int month, int date, int hourOfDay, int minute) {
        set(1, year);
        set(2, month);
        set(5, date);
        set(11, hourOfDay);
        set(12, minute);
    }

    public final void set(int year, int month, int date, int hourOfDay, int minute, int second) {
        set(1, year);
        set(2, month);
        set(5, date);
        set(11, hourOfDay);
        set(12, minute);
        set(13, second);
    }

    public final void clear() {
        for (int i = 0; i < this.fields.length; i++) {
            int[] iArr = this.stamp;
            this.fields[i] = 0;
            iArr[i] = 0;
            this.isSet[i] = $assertionsDisabled;
        }
        this.areFieldsSet = $assertionsDisabled;
        this.areAllFieldsSet = $assertionsDisabled;
        this.isTimeSet = $assertionsDisabled;
    }

    public final void clear(int field) {
        this.fields[field] = 0;
        this.stamp[field] = 0;
        this.isSet[field] = $assertionsDisabled;
        this.areFieldsSet = $assertionsDisabled;
        this.areAllFieldsSet = $assertionsDisabled;
        this.isTimeSet = $assertionsDisabled;
    }

    public final boolean isSet(int field) {
        if (this.stamp[field] != 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    public String getDisplayName(int field, int style, Locale locale) {
        if (style == 0) {
            style = 1;
        }
        if (!checkDisplayNameParams(field, style, 1, 4, locale, 645)) {
            return null;
        }
        String calendarType = getCalendarType();
        int fieldValue = get(field);
        if (isStandaloneStyle(style) || isNarrowFormatStyle(style)) {
            String val = CalendarDataUtility.retrieveFieldValueName(calendarType, field, fieldValue, style, locale);
            if (val == null) {
                if (isNarrowFormatStyle(style)) {
                    val = CalendarDataUtility.retrieveFieldValueName(calendarType, field, fieldValue, toStandaloneStyle(style), locale);
                } else if (isStandaloneStyle(style)) {
                    val = CalendarDataUtility.retrieveFieldValueName(calendarType, field, fieldValue, getBaseStyle(style), locale);
                }
            }
            return val;
        }
        String[] strings = getFieldStrings(field, style, DateFormatSymbols.getInstance(locale));
        if (strings == null || fieldValue >= strings.length) {
            return null;
        }
        return strings[fieldValue];
    }

    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, 0, 4, locale, 645)) {
            return null;
        }
        complete();
        String calendarType = getCalendarType();
        if (style != 0 && !isStandaloneStyle(style) && !isNarrowFormatStyle(style)) {
            return getDisplayNamesImpl(field, style, locale);
        }
        Map<String, Integer> map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field, style, locale);
        if (map == null) {
            if (isNarrowFormatStyle(style)) {
                map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field, toStandaloneStyle(style), locale);
            } else if (style != 0) {
                map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field, getBaseStyle(style), locale);
            }
        }
        return map;
    }

    private Map<String, Integer> getDisplayNamesImpl(int field, int style, Locale locale) {
        String[] strings = getFieldStrings(field, style, DateFormatSymbols.getInstance(locale));
        if (strings == null) {
            return null;
        }
        Map<String, Integer> names = new HashMap<>();
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].length() != 0) {
                names.put(strings[i], Integer.valueOf(i));
            }
        }
        return names;
    }

    /* access modifiers changed from: package-private */
    public boolean checkDisplayNameParams(int field, int style, int minStyle, int maxStyle, Locale locale, int fieldMask) {
        int baseStyle = getBaseStyle(style);
        if (field < 0 || field >= this.fields.length || baseStyle < minStyle || baseStyle > maxStyle) {
            throw new IllegalArgumentException();
        } else if (baseStyle == 3) {
            throw new IllegalArgumentException();
        } else if (locale != null) {
            return isFieldSet(fieldMask, field);
        } else {
            throw new NullPointerException();
        }
    }

    private String[] getFieldStrings(int field, int style, DateFormatSymbols symbols) {
        int baseStyle = getBaseStyle(style);
        if (baseStyle == 4) {
            return null;
        }
        String[] strings = null;
        if (field == 0) {
            strings = symbols.getEras();
        } else if (field == 2) {
            strings = baseStyle == 2 ? symbols.getMonths() : symbols.getShortMonths();
        } else if (field == 7) {
            strings = baseStyle == 2 ? symbols.getWeekdays() : symbols.getShortWeekdays();
        } else if (field == 9) {
            strings = symbols.getAmPmStrings();
        }
        return strings;
    }

    /* access modifiers changed from: protected */
    public void complete() {
        if (!this.isTimeSet) {
            updateTime();
        }
        if (!this.areFieldsSet || !this.areAllFieldsSet) {
            computeFields();
            this.areFieldsSet = true;
            this.areAllFieldsSet = true;
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean isExternallySet(int field) {
        if (this.stamp[field] >= 2) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: package-private */
    public final int getSetStateFields() {
        int mask = 0;
        for (int i = 0; i < this.fields.length; i++) {
            if (this.stamp[i] != 0) {
                mask |= 1 << i;
            }
        }
        return mask;
    }

    /* access modifiers changed from: package-private */
    public final void setFieldsComputed(int fieldMask) {
        if (fieldMask == ALL_FIELDS) {
            for (int i = 0; i < this.fields.length; i++) {
                this.stamp[i] = 1;
                this.isSet[i] = true;
            }
            this.areAllFieldsSet = true;
            this.areFieldsSet = true;
            return;
        }
        int fieldMask2 = fieldMask;
        for (int i2 = 0; i2 < this.fields.length; i2++) {
            if ((fieldMask2 & 1) == 1) {
                this.stamp[i2] = 1;
                this.isSet[i2] = true;
            } else if (this.areAllFieldsSet && !this.isSet[i2]) {
                this.areAllFieldsSet = $assertionsDisabled;
            }
            fieldMask2 >>>= 1;
        }
        int i3 = fieldMask2;
    }

    /* access modifiers changed from: package-private */
    public final void setFieldsNormalized(int fieldMask) {
        if (fieldMask != ALL_FIELDS) {
            int fieldMask2 = fieldMask;
            for (int i = 0; i < this.fields.length; i++) {
                if ((fieldMask2 & 1) == 0) {
                    int[] iArr = this.stamp;
                    this.fields[i] = 0;
                    iArr[i] = 0;
                    this.isSet[i] = $assertionsDisabled;
                }
                fieldMask2 >>= 1;
            }
            int i2 = fieldMask2;
        }
        this.areFieldsSet = true;
        this.areAllFieldsSet = $assertionsDisabled;
    }

    /* access modifiers changed from: package-private */
    public final boolean isPartiallyNormalized() {
        if (!this.areFieldsSet || this.areAllFieldsSet) {
            return $assertionsDisabled;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final boolean isFullyNormalized() {
        if (!this.areFieldsSet || !this.areAllFieldsSet) {
            return $assertionsDisabled;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void setUnnormalized() {
        this.areAllFieldsSet = $assertionsDisabled;
        this.areFieldsSet = $assertionsDisabled;
    }

    static boolean isFieldSet(int fieldMask, int field) {
        if (((1 << field) & fieldMask) != 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: package-private */
    public final int selectFields() {
        int fieldMask;
        int fieldMask2 = 2;
        if (this.stamp[0] != 0) {
            fieldMask2 = 2 | 1;
        }
        int dowStamp = this.stamp[7];
        int monthStamp = this.stamp[2];
        int domStamp = this.stamp[5];
        int womStamp = aggregateStamp(this.stamp[4], dowStamp);
        int dowimStamp = aggregateStamp(this.stamp[8], dowStamp);
        int doyStamp = this.stamp[6];
        int woyStamp = aggregateStamp(this.stamp[3], dowStamp);
        int bestStamp = domStamp;
        if (womStamp > bestStamp) {
            bestStamp = womStamp;
        }
        if (dowimStamp > bestStamp) {
            bestStamp = dowimStamp;
        }
        if (doyStamp > bestStamp) {
            bestStamp = doyStamp;
        }
        if (woyStamp > bestStamp) {
            bestStamp = woyStamp;
        }
        if (bestStamp == 0) {
            womStamp = this.stamp[4];
            dowimStamp = Math.max(this.stamp[8], dowStamp);
            bestStamp = Math.max(Math.max(womStamp, dowimStamp), this.stamp[3]);
            if (bestStamp == 0) {
                domStamp = monthStamp;
                bestStamp = monthStamp;
            }
        }
        if (bestStamp == domStamp || ((bestStamp == womStamp && this.stamp[4] >= this.stamp[3]) || (bestStamp == dowimStamp && this.stamp[8] >= this.stamp[3]))) {
            fieldMask = fieldMask2 | 4;
            if (bestStamp == domStamp) {
                fieldMask |= 32;
            } else {
                if (dowStamp != 0) {
                    fieldMask |= 128;
                }
                if (womStamp == dowimStamp) {
                    if (this.stamp[4] >= this.stamp[8]) {
                        fieldMask |= 16;
                    } else {
                        fieldMask |= 256;
                    }
                } else if (bestStamp == womStamp) {
                    fieldMask |= 16;
                } else if (this.stamp[8] != 0) {
                    fieldMask |= 256;
                }
            }
        } else if (bestStamp == doyStamp) {
            fieldMask = fieldMask2 | 64;
        } else {
            if (dowStamp != 0) {
                fieldMask2 |= 128;
            }
            fieldMask = fieldMask2 | 8;
        }
        int hourOfDayStamp = this.stamp[11];
        int hourStamp = aggregateStamp(this.stamp[10], this.stamp[9]);
        int bestStamp2 = hourStamp > hourOfDayStamp ? hourStamp : hourOfDayStamp;
        if (bestStamp2 == 0) {
            bestStamp2 = Math.max(this.stamp[10], this.stamp[9]);
        }
        if (bestStamp2 != 0) {
            if (bestStamp2 == hourOfDayStamp) {
                fieldMask |= 2048;
            } else {
                fieldMask |= 1024;
                if (this.stamp[9] != 0) {
                    fieldMask |= 512;
                }
            }
        }
        if (this.stamp[12] != 0) {
            fieldMask |= 4096;
        }
        if (this.stamp[13] != 0) {
            fieldMask |= 8192;
        }
        if (this.stamp[14] != 0) {
            fieldMask |= 16384;
        }
        if (this.stamp[15] >= 2) {
            fieldMask |= NumericShaper.MYANMAR;
        }
        if (this.stamp[16] >= 2) {
            return fieldMask | 65536;
        }
        return fieldMask;
    }

    /* access modifiers changed from: package-private */
    public int getBaseStyle(int style) {
        return -32769 & style;
    }

    private int toStandaloneStyle(int style) {
        return 32768 | style;
    }

    private boolean isStandaloneStyle(int style) {
        if ((32768 & style) != 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    private boolean isNarrowStyle(int style) {
        if (style == 4 || style == 32772) {
            return true;
        }
        return $assertionsDisabled;
    }

    private boolean isNarrowFormatStyle(int style) {
        if (style == 4) {
            return true;
        }
        return $assertionsDisabled;
    }

    private static int aggregateStamp(int stamp_a, int stamp_b) {
        if (stamp_a == 0 || stamp_b == 0) {
            return 0;
        }
        return stamp_a > stamp_b ? stamp_a : stamp_b;
    }

    public static Set<String> getAvailableCalendarTypes() {
        return AvailableCalendarTypes.SET;
    }

    public String getCalendarType() {
        return getClass().getName();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        try {
            Calendar that = (Calendar) obj;
            if (!(compareTo(getMillisOf(that)) == 0 && this.lenient == that.lenient && this.firstDayOfWeek == that.firstDayOfWeek && this.minimalDaysInFirstWeek == that.minimalDaysInFirstWeek && this.zone.equals(that.zone))) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            return $assertionsDisabled;
        }
    }

    public int hashCode() {
        int otheritems = this.lenient | (this.firstDayOfWeek << 1) | (this.minimalDaysInFirstWeek << 4) | (this.zone.hashCode() << 7);
        long t = getMillisOf(this);
        return (int) ((((int) t) ^ ((int) (t >> 32))) ^ otheritems);
    }

    public boolean before(Object when) {
        if (!(when instanceof Calendar) || compareTo((Calendar) when) >= 0) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean after(Object when) {
        if (!(when instanceof Calendar) || compareTo((Calendar) when) <= 0) {
            return $assertionsDisabled;
        }
        return true;
    }

    public int compareTo(Calendar anotherCalendar) {
        return compareTo(getMillisOf(anotherCalendar));
    }

    public void roll(int field, int amount) {
        while (amount > 0) {
            roll(field, true);
            amount--;
        }
        while (amount < 0) {
            roll(field, (boolean) $assertionsDisabled);
            amount++;
        }
    }

    public void setTimeZone(TimeZone value) {
        this.zone = value;
        this.sharedZone = $assertionsDisabled;
        this.areFieldsSet = $assertionsDisabled;
        this.areAllFieldsSet = $assertionsDisabled;
    }

    public TimeZone getTimeZone() {
        if (this.sharedZone) {
            this.zone = (TimeZone) this.zone.clone();
            this.sharedZone = $assertionsDisabled;
        }
        return this.zone;
    }

    /* access modifiers changed from: package-private */
    public TimeZone getZone() {
        return this.zone;
    }

    /* access modifiers changed from: package-private */
    public void setZoneShared(boolean shared) {
        this.sharedZone = shared;
    }

    public void setLenient(boolean lenient2) {
        this.lenient = lenient2;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public void setFirstDayOfWeek(int value) {
        if (this.firstDayOfWeek != value) {
            this.firstDayOfWeek = value;
            invalidateWeekFields();
        }
    }

    public int getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    public void setMinimalDaysInFirstWeek(int value) {
        if (this.minimalDaysInFirstWeek != value) {
            this.minimalDaysInFirstWeek = value;
            invalidateWeekFields();
        }
    }

    public int getMinimalDaysInFirstWeek() {
        return this.minimalDaysInFirstWeek;
    }

    public boolean isWeekDateSupported() {
        return $assertionsDisabled;
    }

    public int getWeekYear() {
        throw new UnsupportedOperationException();
    }

    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        throw new UnsupportedOperationException();
    }

    public int getWeeksInWeekYear() {
        throw new UnsupportedOperationException();
    }

    public int getActualMinimum(int field) {
        int result = getGreatestMinimum(field);
        int endValue = getMinimum(field);
        if (result == endValue) {
            return result;
        }
        Calendar work = (Calendar) clone();
        work.setLenient(true);
        int fieldValue = result;
        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) {
                break;
            }
            result = fieldValue;
            fieldValue--;
        } while (fieldValue >= endValue);
        return result;
    }

    public int getActualMaximum(int field) {
        int result = getLeastMaximum(field);
        int endValue = getMaximum(field);
        if (result == endValue) {
            return result;
        }
        Calendar work = (Calendar) clone();
        work.setLenient(true);
        if (field == 3 || field == 4) {
            work.set(7, this.firstDayOfWeek);
        }
        int fieldValue = result;
        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) {
                break;
            }
            result = fieldValue;
            fieldValue++;
        } while (fieldValue <= endValue);
        return result;
    }

    public Object clone() {
        try {
            Calendar other = (Calendar) super.clone();
            other.fields = new int[17];
            other.isSet = new boolean[17];
            other.stamp = new int[17];
            for (int i = 0; i < 17; i++) {
                other.fields[i] = this.fields[i];
                other.stamp[i] = this.stamp[i];
                other.isSet[i] = this.isSet[i];
            }
            other.zone = (TimeZone) this.zone.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    static String getFieldName(int field) {
        return FIELD_NAME[field];
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(800);
        buffer.append(getClass().getName());
        buffer.append('[');
        appendValue(buffer, "time", this.isTimeSet, this.time);
        buffer.append(",areFieldsSet=");
        buffer.append(this.areFieldsSet);
        buffer.append(",areAllFieldsSet=");
        buffer.append(this.areAllFieldsSet);
        buffer.append(",lenient=");
        buffer.append(this.lenient);
        buffer.append(",zone=");
        buffer.append((Object) this.zone);
        appendValue(buffer, ",firstDayOfWeek", true, (long) this.firstDayOfWeek);
        appendValue(buffer, ",minimalDaysInFirstWeek", true, (long) this.minimalDaysInFirstWeek);
        for (int i = 0; i < 17; i++) {
            buffer.append(',');
            appendValue(buffer, FIELD_NAME[i], isSet(i), (long) this.fields[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    private static void appendValue(StringBuilder sb, String item, boolean valid, long value) {
        sb.append(item);
        sb.append('=');
        if (valid) {
            sb.append(value);
        } else {
            sb.append('?');
        }
    }

    private void setWeekCountData(Locale desiredLocale) {
        int[] data = cachedLocaleData.get(desiredLocale);
        if (data == null) {
            LocaleData localeData = LocaleData.get(desiredLocale);
            data = new int[]{localeData.firstDayOfWeek.intValue(), localeData.minimalDaysInFirstWeek.intValue()};
            cachedLocaleData.putIfAbsent(desiredLocale, data);
        }
        this.firstDayOfWeek = data[0];
        this.minimalDaysInFirstWeek = data[1];
    }

    private void updateTime() {
        computeTime();
        this.isTimeSet = true;
    }

    private int compareTo(long t) {
        long thisTime = getMillisOf(this);
        if (thisTime > t) {
            return 1;
        }
        return thisTime == t ? 0 : -1;
    }

    private static long getMillisOf(Calendar calendar) {
        if (calendar.isTimeSet) {
            return calendar.time;
        }
        Calendar cal = (Calendar) calendar.clone();
        cal.setLenient(true);
        return cal.getTimeInMillis();
    }

    private void adjustStamp() {
        int max = 2;
        int newStamp = 2;
        while (true) {
            int i = 0;
            int min = Integer.MAX_VALUE;
            int max2 = max;
            for (int v : this.stamp) {
                if (v >= newStamp && min > v) {
                    min = v;
                }
                if (max2 < v) {
                    max2 = v;
                }
            }
            if (max2 != min && min == Integer.MAX_VALUE) {
                break;
            }
            while (true) {
                int i2 = i;
                if (i2 >= this.stamp.length) {
                    break;
                }
                if (this.stamp[i2] == min) {
                    this.stamp[i2] = newStamp;
                }
                i = i2 + 1;
            }
            newStamp++;
            if (min == max2) {
                break;
            }
            max = max2;
        }
        this.nextStamp = newStamp;
    }

    private void invalidateWeekFields() {
        if (this.stamp[4] == 1 || this.stamp[3] == 1) {
            Calendar cal = (Calendar) clone();
            cal.setLenient(true);
            cal.clear(4);
            cal.clear(3);
            if (this.stamp[4] == 1) {
                int weekOfMonth = cal.get(4);
                if (this.fields[4] != weekOfMonth) {
                    this.fields[4] = weekOfMonth;
                }
            }
            if (this.stamp[3] == 1) {
                int weekOfYear = cal.get(3);
                if (this.fields[3] != weekOfYear) {
                    this.fields[3] = weekOfYear;
                }
            }
        }
    }

    private synchronized void writeObject(ObjectOutputStream stream) throws IOException {
        if (!this.isTimeSet) {
            try {
                updateTime();
            } catch (IllegalArgumentException e) {
            }
        }
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.stamp = new int[17];
        if (this.serialVersionOnStream >= 2) {
            this.isTimeSet = true;
            if (this.fields == null) {
                this.fields = new int[17];
            }
            if (this.isSet == null) {
                this.isSet = new boolean[17];
            }
        } else if (this.serialVersionOnStream >= 0) {
            for (int i = 0; i < 17; i++) {
                this.stamp[i] = this.isSet[i];
            }
        }
        this.serialVersionOnStream = 1;
        if (this.zone instanceof SimpleTimeZone) {
            String id = this.zone.getID();
            TimeZone tz = TimeZone.getTimeZone(id);
            if (tz != null && tz.hasSameRules(this.zone) && tz.getID().equals(id)) {
                this.zone = tz;
            }
        }
    }

    public final Instant toInstant() {
        return Instant.ofEpochMilli(getTimeInMillis());
    }
}
