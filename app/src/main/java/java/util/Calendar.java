package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Locale.Category;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.LocaleData;
import sun.util.logging.PlatformLogger;

public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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
    private static final String[] FIELD_NAME = null;
    public static final int FRIDAY = 6;
    public static final int HOUR = 10;
    static final int HOUR_MASK = 1024;
    public static final int HOUR_OF_DAY = 11;
    static final int HOUR_OF_DAY_MASK = 2048;
    public static final int JANUARY = 0;
    public static final int JULY = 6;
    public static final int JUNE = 5;
    public static final int LONG = 2;
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
    public static final int NOVEMBER = 10;
    public static final int OCTOBER = 9;
    public static final int PM = 1;
    public static final int SATURDAY = 7;
    public static final int SECOND = 13;
    static final int SECOND_MASK = 8192;
    public static final int SEPTEMBER = 8;
    public static final int SHORT = 1;
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
    private static final ConcurrentMap<Locale, int[]> cachedLocaleData = null;
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

    private static class CalendarAccessControlContext {
        private static final AccessControlContext INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Calendar.CalendarAccessControlContext.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Calendar.CalendarAccessControlContext.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.Calendar.CalendarAccessControlContext.<clinit>():void");
        }

        private CalendarAccessControlContext() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Calendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Calendar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.Calendar.<clinit>():void");
    }

    public abstract void add(int i, int i2);

    protected abstract void computeFields();

    protected abstract void computeTime();

    public abstract int getGreatestMinimum(int i);

    public abstract int getLeastMaximum(int i);

    public abstract int getMaximum(int i);

    public abstract int getMinimum(int i);

    public abstract void roll(int i, boolean z);

    protected Calendar() {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Category.FORMAT));
        this.sharedZone = true;
    }

    protected Calendar(TimeZone zone, Locale aLocale) {
        this.lenient = true;
        this.sharedZone = -assertionsDisabled;
        this.nextStamp = YEAR_MASK;
        this.serialVersionOnStream = currentSerialVersion;
        if (aLocale == null) {
            aLocale = Locale.getDefault();
        }
        this.fields = new int[FIELD_COUNT];
        this.isSet = new boolean[FIELD_COUNT];
        this.stamp = new int[FIELD_COUNT];
        this.zone = zone;
        setWeekCountData(aLocale);
    }

    public static Calendar getInstance() {
        Calendar cal = createCalendar(TimeZone.getDefaultRef(), Locale.getDefault(Category.FORMAT));
        cal.sharedZone = true;
        return cal;
    }

    public static Calendar getInstance(TimeZone zone) {
        return createCalendar(zone, Locale.getDefault(Category.FORMAT));
    }

    public static Calendar getInstance(Locale aLocale) {
        Calendar cal = createCalendar(TimeZone.getDefaultRef(), aLocale);
        cal.sharedZone = true;
        return cal;
    }

    public static Calendar getInstance(TimeZone zone, Locale aLocale) {
        return createCalendar(zone, aLocale);
    }

    private static Calendar createCalendar(TimeZone zone, Locale aLocale) {
        return new GregorianCalendar(zone, aLocale);
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
            this.areFieldsSet = -assertionsDisabled;
            computeFields();
            this.areFieldsSet = true;
            this.areAllFieldsSet = true;
        }
    }

    public int get(int field) {
        complete();
        return internalGet(field);
    }

    protected final int internalGet(int field) {
        return this.fields[field];
    }

    final void internalSet(int field, int value) {
        this.fields[field] = value;
    }

    public void set(int field, int value) {
        if (this.areFieldsSet && !this.areAllFieldsSet) {
            computeFields();
        }
        internalSet(field, value);
        this.isTimeSet = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.isSet[field] = true;
        int[] iArr = this.stamp;
        int i = this.nextStamp;
        this.nextStamp = i + currentSerialVersion;
        iArr[field] = i;
        if (this.nextStamp == PlatformLogger.OFF) {
            adjustStamp();
        }
    }

    public final void set(int year, int month, int date) {
        set(currentSerialVersion, year);
        set(YEAR_MASK, month);
        set(THURSDAY, date);
    }

    public final void set(int year, int month, int date, int hourOfDay, int minute) {
        set(currentSerialVersion, year);
        set(YEAR_MASK, month);
        set(THURSDAY, date);
        set(HOUR_OF_DAY, hourOfDay);
        set(UNDECIMBER, minute);
    }

    public final void set(int year, int month, int date, int hourOfDay, int minute, int second) {
        set(currentSerialVersion, year);
        set(YEAR_MASK, month);
        set(THURSDAY, date);
        set(HOUR_OF_DAY, hourOfDay);
        set(UNDECIMBER, minute);
        set(SECOND, second);
    }

    public final void clear() {
        int i = UNSET;
        while (i < this.fields.length) {
            int[] iArr = this.stamp;
            this.fields[i] = UNSET;
            iArr[i] = UNSET;
            int i2 = i + currentSerialVersion;
            this.isSet[i] = -assertionsDisabled;
            i = i2;
        }
        this.areFieldsSet = -assertionsDisabled;
        this.areAllFieldsSet = -assertionsDisabled;
        this.isTimeSet = -assertionsDisabled;
    }

    public final void clear(int field) {
        this.fields[field] = UNSET;
        this.stamp[field] = UNSET;
        this.isSet[field] = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.areAllFieldsSet = -assertionsDisabled;
        this.isTimeSet = -assertionsDisabled;
    }

    public final boolean isSet(int field) {
        return this.stamp[field] != 0 ? true : -assertionsDisabled;
    }

    public String getDisplayName(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, UNSET, YEAR_MASK, locale, 645)) {
            return null;
        }
        String[] strings = getFieldStrings(field, style, DateFormatSymbols.getInstance(locale));
        if (strings != null) {
            int fieldValue = get(field);
            if (fieldValue < strings.length) {
                return strings[fieldValue];
            }
        }
        return null;
    }

    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, UNSET, YEAR_MASK, locale, 645)) {
            return null;
        }
        complete();
        if (style != 0) {
            return getDisplayNamesImpl(field, style, locale);
        }
        Map<String, Integer> shortNames = getDisplayNamesImpl(field, currentSerialVersion, locale);
        if (field == 0 || field == OCTOBER) {
            return shortNames;
        }
        Map<String, Integer> longNames = getDisplayNamesImpl(field, YEAR_MASK, locale);
        if (shortNames == null) {
            return longNames;
        }
        if (longNames != null) {
            shortNames.putAll(longNames);
        }
        return shortNames;
    }

    private Map<String, Integer> getDisplayNamesImpl(int field, int style, Locale locale) {
        String[] strings = getFieldStrings(field, style, DateFormatSymbols.getInstance(locale));
        if (strings == null) {
            return null;
        }
        Map<String, Integer> names = new HashMap();
        for (int i = UNSET; i < strings.length; i += currentSerialVersion) {
            if (strings[i].length() != 0) {
                names.put(strings[i], Integer.valueOf(i));
            }
        }
        return names;
    }

    boolean checkDisplayNameParams(int field, int style, int minStyle, int maxStyle, Locale locale, int fieldMask) {
        if (field < 0 || field >= this.fields.length || style < minStyle || style > maxStyle) {
            throw new IllegalArgumentException();
        } else if (locale != null) {
            return isFieldSet(fieldMask, field);
        } else {
            throw new NullPointerException();
        }
    }

    private String[] getFieldStrings(int field, int style, DateFormatSymbols symbols) {
        switch (field) {
            case UNSET /*0*/:
                return symbols.getEras();
            case YEAR_MASK /*2*/:
                return style == YEAR_MASK ? symbols.getMonths() : symbols.getShortMonths();
            case SATURDAY /*7*/:
                return style == YEAR_MASK ? symbols.getWeekdays() : symbols.getShortWeekdays();
            case OCTOBER /*9*/:
                return symbols.getAmPmStrings();
            default:
                return null;
        }
    }

    protected void complete() {
        if (!this.isTimeSet) {
            updateTime();
        }
        if (!this.areFieldsSet || !this.areAllFieldsSet) {
            computeFields();
            this.areFieldsSet = true;
            this.areAllFieldsSet = true;
        }
    }

    final boolean isExternallySet(int field) {
        return this.stamp[field] >= YEAR_MASK ? true : -assertionsDisabled;
    }

    final int getSetStateFields() {
        int mask = UNSET;
        for (int i = UNSET; i < this.fields.length; i += currentSerialVersion) {
            if (this.stamp[i] != 0) {
                mask |= currentSerialVersion << i;
            }
        }
        return mask;
    }

    final void setFieldsComputed(int fieldMask) {
        int i;
        if (fieldMask == ALL_FIELDS) {
            for (i = UNSET; i < this.fields.length; i += currentSerialVersion) {
                this.stamp[i] = currentSerialVersion;
                this.isSet[i] = true;
            }
            this.areAllFieldsSet = true;
            this.areFieldsSet = true;
            return;
        }
        i = UNSET;
        while (i < this.fields.length) {
            if ((fieldMask & currentSerialVersion) == currentSerialVersion) {
                this.stamp[i] = currentSerialVersion;
                this.isSet[i] = true;
            } else if (this.areAllFieldsSet && !this.isSet[i]) {
                this.areAllFieldsSet = -assertionsDisabled;
            }
            fieldMask >>>= currentSerialVersion;
            i += currentSerialVersion;
        }
    }

    final void setFieldsNormalized(int fieldMask) {
        if (fieldMask != ALL_FIELDS) {
            for (int i = UNSET; i < this.fields.length; i += currentSerialVersion) {
                if ((fieldMask & currentSerialVersion) == 0) {
                    int[] iArr = this.stamp;
                    this.fields[i] = UNSET;
                    iArr[i] = UNSET;
                    this.isSet[i] = -assertionsDisabled;
                }
                fieldMask >>= currentSerialVersion;
            }
        }
        this.areFieldsSet = true;
        this.areAllFieldsSet = -assertionsDisabled;
    }

    final boolean isPartiallyNormalized() {
        return (!this.areFieldsSet || this.areAllFieldsSet) ? -assertionsDisabled : true;
    }

    final boolean isFullyNormalized() {
        return this.areFieldsSet ? this.areAllFieldsSet : -assertionsDisabled;
    }

    final void setUnnormalized() {
        this.areAllFieldsSet = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
    }

    static final boolean isFieldSet(int fieldMask, int field) {
        return ((currentSerialVersion << field) & fieldMask) != 0 ? true : -assertionsDisabled;
    }

    final int selectFields() {
        int fieldMask = YEAR_MASK;
        if (this.stamp[UNSET] != 0) {
            fieldMask = WEEK_OF_YEAR;
        }
        int dowStamp = this.stamp[SATURDAY];
        int monthStamp = this.stamp[YEAR_MASK];
        int domStamp = this.stamp[THURSDAY];
        int womStamp = aggregateStamp(this.stamp[WEEK_OF_MONTH], dowStamp);
        int dowimStamp = aggregateStamp(this.stamp[WEEK_OF_YEAR_MASK], dowStamp);
        int doyStamp = this.stamp[JULY];
        int woyStamp = aggregateStamp(this.stamp[WEEK_OF_YEAR], dowStamp);
        int bestStamp = domStamp;
        if (womStamp > domStamp) {
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
            womStamp = this.stamp[WEEK_OF_MONTH];
            dowimStamp = Math.max(this.stamp[WEEK_OF_YEAR_MASK], dowStamp);
            woyStamp = this.stamp[WEEK_OF_YEAR];
            bestStamp = Math.max(Math.max(womStamp, dowimStamp), woyStamp);
            if (bestStamp == 0) {
                domStamp = monthStamp;
                bestStamp = monthStamp;
            }
        }
        Object obj;
        if (bestStamp == domStamp || ((bestStamp == womStamp && this.stamp[WEEK_OF_MONTH] >= this.stamp[WEEK_OF_YEAR]) || (bestStamp == dowimStamp && this.stamp[WEEK_OF_YEAR_MASK] >= this.stamp[WEEK_OF_YEAR]))) {
            fieldMask |= WEEK_OF_MONTH;
            if (bestStamp == domStamp) {
                fieldMask |= DAY_OF_MONTH_MASK;
            } else {
                if (!-assertionsDisabled) {
                    obj = (bestStamp == womStamp || bestStamp == dowimStamp) ? currentSerialVersion : null;
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (dowStamp != 0) {
                    fieldMask |= DAY_OF_WEEK_MASK;
                }
                if (womStamp == dowimStamp) {
                    if (this.stamp[WEEK_OF_MONTH] >= this.stamp[WEEK_OF_YEAR_MASK]) {
                        fieldMask |= WEEK_OF_MONTH_MASK;
                    } else {
                        fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
                    }
                } else if (bestStamp == womStamp) {
                    fieldMask |= WEEK_OF_MONTH_MASK;
                } else {
                    if (!-assertionsDisabled) {
                        if ((bestStamp == dowimStamp ? currentSerialVersion : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (this.stamp[WEEK_OF_YEAR_MASK] != 0) {
                        fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
                    }
                }
            }
        } else {
            if (!-assertionsDisabled) {
                obj = (bestStamp == doyStamp || bestStamp == woyStamp) ? currentSerialVersion : bestStamp == 0 ? currentSerialVersion : null;
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            if (bestStamp == doyStamp) {
                fieldMask |= DAY_OF_YEAR_MASK;
            } else {
                if (!-assertionsDisabled) {
                    if ((bestStamp == woyStamp ? currentSerialVersion : null) == null) {
                        throw new AssertionError();
                    }
                }
                if (dowStamp != 0) {
                    fieldMask |= DAY_OF_WEEK_MASK;
                }
                fieldMask |= WEEK_OF_YEAR_MASK;
            }
        }
        int hourOfDayStamp = this.stamp[HOUR_OF_DAY];
        int hourStamp = aggregateStamp(this.stamp[NOVEMBER], this.stamp[OCTOBER]);
        if (hourStamp > hourOfDayStamp) {
            bestStamp = hourStamp;
        } else {
            bestStamp = hourOfDayStamp;
        }
        if (bestStamp == 0) {
            bestStamp = Math.max(this.stamp[NOVEMBER], this.stamp[OCTOBER]);
        }
        if (bestStamp != 0) {
            if (bestStamp == hourOfDayStamp) {
                fieldMask |= HOUR_OF_DAY_MASK;
            } else {
                fieldMask |= HOUR_MASK;
                if (this.stamp[OCTOBER] != 0) {
                    fieldMask |= AM_PM_MASK;
                }
            }
        }
        if (this.stamp[UNDECIMBER] != 0) {
            fieldMask |= MINUTE_MASK;
        }
        if (this.stamp[SECOND] != 0) {
            fieldMask |= SECOND_MASK;
        }
        if (this.stamp[MILLISECOND] != 0) {
            fieldMask |= MILLISECOND_MASK;
        }
        if (this.stamp[ZONE_OFFSET] >= YEAR_MASK) {
            fieldMask |= ZONE_OFFSET_MASK;
        }
        if (this.stamp[WEEK_OF_MONTH_MASK] >= YEAR_MASK) {
            return fieldMask | DST_OFFSET_MASK;
        }
        return fieldMask;
    }

    private static final int aggregateStamp(int stamp_a, int stamp_b) {
        if (stamp_a == 0 || stamp_b == 0) {
            return UNSET;
        }
        if (stamp_a <= stamp_b) {
            stamp_a = stamp_b;
        }
        return stamp_a;
    }

    public boolean equals(Object obj) {
        boolean z = -assertionsDisabled;
        if (this == obj) {
            return true;
        }
        try {
            Calendar that = (Calendar) obj;
            if (compareTo(getMillisOf(that)) == 0 && this.lenient == that.lenient && this.firstDayOfWeek == that.firstDayOfWeek && this.minimalDaysInFirstWeek == that.minimalDaysInFirstWeek) {
                z = this.zone.equals(that.zone);
            }
            return z;
        } catch (Exception e) {
            return -assertionsDisabled;
        }
    }

    public int hashCode() {
        int otheritems = (((this.lenient ? currentSerialVersion : UNSET) | (this.firstDayOfWeek << currentSerialVersion)) | (this.minimalDaysInFirstWeek << WEEK_OF_MONTH)) | (this.zone.hashCode() << SATURDAY);
        long t = getMillisOf(this);
        return (((int) t) ^ ((int) (t >> DAY_OF_MONTH_MASK))) ^ otheritems;
    }

    public boolean before(Object when) {
        if (!(when instanceof Calendar) || compareTo((Calendar) when) >= 0) {
            return -assertionsDisabled;
        }
        return true;
    }

    public boolean after(Object when) {
        if (!(when instanceof Calendar) || compareTo((Calendar) when) <= 0) {
            return -assertionsDisabled;
        }
        return true;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object anotherCalendar) {
        return compareTo((Calendar) anotherCalendar);
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
            roll(field, (boolean) -assertionsDisabled);
            amount += currentSerialVersion;
        }
    }

    public void setTimeZone(TimeZone value) {
        this.zone = value;
        this.sharedZone = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.areAllFieldsSet = -assertionsDisabled;
    }

    public TimeZone getTimeZone() {
        if (this.sharedZone) {
            this.zone = (TimeZone) this.zone.clone();
            this.sharedZone = -assertionsDisabled;
        }
        return this.zone;
    }

    TimeZone getZone() {
        return this.zone;
    }

    void setZoneShared(boolean shared) {
        this.sharedZone = shared;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
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
        return -assertionsDisabled;
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
        int fieldValue = getGreatestMinimum(field);
        int endValue = getMinimum(field);
        if (fieldValue == endValue) {
            return fieldValue;
        }
        Calendar work = (Calendar) clone();
        work.setLenient(true);
        int result = fieldValue;
        while (true) {
            work.set(field, fieldValue);
            if (work.get(field) == fieldValue) {
                result = fieldValue;
                fieldValue--;
                if (fieldValue < endValue) {
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

    public int getActualMaximum(int field) {
        int fieldValue = getLeastMaximum(field);
        int endValue = getMaximum(field);
        if (fieldValue == endValue) {
            return fieldValue;
        }
        Calendar work = (Calendar) clone();
        work.setLenient(true);
        if (field == WEEK_OF_YEAR || field == WEEK_OF_MONTH) {
            work.set(SATURDAY, this.firstDayOfWeek);
        }
        int result = fieldValue;
        while (true) {
            work.set(field, fieldValue);
            if (work.get(field) == fieldValue) {
                result = fieldValue;
                fieldValue += currentSerialVersion;
                if (fieldValue > endValue) {
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

    public Object clone() {
        try {
            Calendar other = (Calendar) super.clone();
            other.fields = new int[FIELD_COUNT];
            other.isSet = new boolean[FIELD_COUNT];
            other.stamp = new int[FIELD_COUNT];
            for (int i = UNSET; i < FIELD_COUNT; i += currentSerialVersion) {
                other.fields[i] = this.fields[i];
                other.stamp[i] = this.stamp[i];
                other.isSet[i] = this.isSet[i];
            }
            other.zone = (TimeZone) this.zone.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    static final String getFieldName(int field) {
        return FIELD_NAME[field];
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder((int) PlatformLogger.INFO);
        buffer.append(getClass().getName()).append('[');
        appendValue(buffer, "time", this.isTimeSet, this.time);
        buffer.append(",areFieldsSet=").append(this.areFieldsSet);
        buffer.append(",areAllFieldsSet=").append(this.areAllFieldsSet);
        buffer.append(",lenient=").append(this.lenient);
        buffer.append(",zone=").append(this.zone);
        appendValue(buffer, ",firstDayOfWeek", true, (long) this.firstDayOfWeek);
        appendValue(buffer, ",minimalDaysInFirstWeek", true, (long) this.minimalDaysInFirstWeek);
        for (int i = UNSET; i < FIELD_COUNT; i += currentSerialVersion) {
            buffer.append(',');
            appendValue(buffer, FIELD_NAME[i], isSet(i), (long) this.fields[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    private static final void appendValue(StringBuilder sb, String item, boolean valid, long value) {
        sb.append(item).append('=');
        if (valid) {
            sb.append(value);
        } else {
            sb.append('?');
        }
    }

    private void setWeekCountData(Locale desiredLocale) {
        int[] data = (int[]) cachedLocaleData.get(desiredLocale);
        if (data == null) {
            LocaleData localeData = LocaleData.get(desiredLocale);
            data = new int[YEAR_MASK];
            data[UNSET] = localeData.firstDayOfWeek.intValue();
            data[currentSerialVersion] = localeData.minimalDaysInFirstWeek.intValue();
            cachedLocaleData.putIfAbsent(desiredLocale, data);
        }
        this.firstDayOfWeek = data[UNSET];
        this.minimalDaysInFirstWeek = data[currentSerialVersion];
    }

    private void updateTime() {
        computeTime();
        this.isTimeSet = true;
    }

    private int compareTo(long t) {
        long thisTime = getMillisOf(this);
        if (thisTime > t) {
            return currentSerialVersion;
        }
        return thisTime == t ? UNSET : -1;
    }

    private static final long getMillisOf(Calendar calendar) {
        if (calendar.isTimeSet) {
            return calendar.time;
        }
        Calendar cal = (Calendar) calendar.clone();
        cal.setLenient(true);
        return cal.getTimeInMillis();
    }

    private final void adjustStamp() {
        int max = YEAR_MASK;
        int newStamp = YEAR_MASK;
        int min;
        do {
            int i;
            min = PlatformLogger.OFF;
            for (i = UNSET; i < this.stamp.length; i += currentSerialVersion) {
                int v = this.stamp[i];
                if (v >= newStamp && min > v) {
                    min = v;
                }
                if (max < v) {
                    max = v;
                }
            }
            if (max != min && min == PlatformLogger.OFF) {
                break;
            }
            for (i = UNSET; i < this.stamp.length; i += currentSerialVersion) {
                if (this.stamp[i] == min) {
                    this.stamp[i] = newStamp;
                }
            }
            newStamp += currentSerialVersion;
        } while (min != max);
        this.nextStamp = newStamp;
    }

    private void invalidateWeekFields() {
        if (this.stamp[WEEK_OF_MONTH] == currentSerialVersion || this.stamp[WEEK_OF_YEAR] == currentSerialVersion) {
            Calendar cal = (Calendar) clone();
            cal.setLenient(true);
            cal.clear(WEEK_OF_MONTH);
            cal.clear(WEEK_OF_YEAR);
            if (this.stamp[WEEK_OF_MONTH] == currentSerialVersion) {
                int weekOfMonth = cal.get(WEEK_OF_MONTH);
                if (this.fields[WEEK_OF_MONTH] != weekOfMonth) {
                    this.fields[WEEK_OF_MONTH] = weekOfMonth;
                }
            }
            if (this.stamp[WEEK_OF_YEAR] == currentSerialVersion) {
                int weekOfYear = cal.get(WEEK_OF_YEAR);
                if (this.fields[WEEK_OF_YEAR] != weekOfYear) {
                    this.fields[WEEK_OF_YEAR] = weekOfYear;
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
        ObjectInputStream input = stream;
        stream.defaultReadObject();
        this.stamp = new int[FIELD_COUNT];
        if (this.serialVersionOnStream >= YEAR_MASK) {
            this.isTimeSet = true;
            if (this.fields == null) {
                this.fields = new int[FIELD_COUNT];
            }
            if (this.isSet == null) {
                this.isSet = new boolean[FIELD_COUNT];
            }
        } else if (this.serialVersionOnStream >= 0) {
            for (int i = UNSET; i < FIELD_COUNT; i += currentSerialVersion) {
                boolean z;
                int[] iArr = this.stamp;
                if (this.isSet[i]) {
                    z = true;
                } else {
                    z = UNSET;
                }
                iArr[i] = z;
            }
        }
        this.serialVersionOnStream = currentSerialVersion;
        if (this.zone instanceof SimpleTimeZone) {
            String id = this.zone.getID();
            TimeZone tz = TimeZone.getTimeZone(id);
            if (tz != null && tz.hasSameRules(this.zone) && tz.getID().equals(id)) {
                this.zone = tz;
            }
        }
    }
}
