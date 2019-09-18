package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.ICU;
import libcore.icu.LocaleData;
import libcore.icu.TimeZoneNames;

public class DateFormatSymbols implements Serializable, Cloneable {
    static final int PATTERN_AM_PM = 14;
    static final int PATTERN_DAY_OF_MONTH = 3;
    static final int PATTERN_DAY_OF_WEEK = 9;
    static final int PATTERN_DAY_OF_WEEK_IN_MONTH = 11;
    static final int PATTERN_DAY_OF_YEAR = 10;
    static final int PATTERN_DAY_PERIOD = 24;
    static final int PATTERN_ERA = 0;
    static final int PATTERN_FLEXIBLE_DAY_PERIOD = 25;
    static final int PATTERN_HOUR0 = 16;
    static final int PATTERN_HOUR1 = 15;
    static final int PATTERN_HOUR_OF_DAY0 = 5;
    static final int PATTERN_HOUR_OF_DAY1 = 4;
    static final int PATTERN_ISO_DAY_OF_WEEK = 20;
    static final int PATTERN_ISO_ZONE = 21;
    static final int PATTERN_MILLISECOND = 8;
    static final int PATTERN_MINUTE = 6;
    static final int PATTERN_MONTH = 2;
    static final int PATTERN_MONTH_STANDALONE = 22;
    static final int PATTERN_SECOND = 7;
    static final int PATTERN_STANDALONE_DAY_OF_WEEK = 23;
    static final int PATTERN_WEEK_OF_MONTH = 13;
    static final int PATTERN_WEEK_OF_YEAR = 12;
    static final int PATTERN_WEEK_YEAR = 19;
    static final int PATTERN_YEAR = 1;
    static final int PATTERN_ZONE_NAME = 17;
    static final int PATTERN_ZONE_VALUE = 18;
    private static final ConcurrentMap<Locale, SoftReference<DateFormatSymbols>> cachedInstances = new ConcurrentHashMap(3);
    static final int currentSerialVersion = 1;
    static final int millisPerHour = 3600000;
    static final String patternChars = "GyMdkHmsSEDFwWahKzZYuXLcbB";
    static final long serialVersionUID = -5987973545549424702L;
    String[] ampms = null;
    volatile transient int cachedHashCode = 0;
    String[] eras = null;
    transient boolean isZoneStringsSet = false;
    private transient int lastZoneIndex = 0;
    String localPatternChars = null;
    Locale locale = null;
    String[] months = null;
    private int serialVersionOnStream = 1;
    String[] shortMonths = null;
    private String[] shortStandAloneMonths;
    private String[] shortStandAloneWeekdays;
    String[] shortWeekdays = null;
    private String[] standAloneMonths;
    private String[] standAloneWeekdays;
    private String[] tinyMonths;
    private String[] tinyStandAloneMonths;
    private String[] tinyStandAloneWeekdays;
    private String[] tinyWeekdays;
    String[] weekdays = null;
    String[][] zoneStrings = null;

    public DateFormatSymbols() {
        initializeData(Locale.getDefault(Locale.Category.FORMAT));
    }

    public DateFormatSymbols(Locale locale2) {
        initializeData(locale2);
    }

    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    public static final DateFormatSymbols getInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormatSymbols getInstance(Locale locale2) {
        return (DateFormatSymbols) getCachedInstance(locale2).clone();
    }

    static final DateFormatSymbols getInstanceRef(Locale locale2) {
        return getCachedInstance(locale2);
    }

    private static DateFormatSymbols getCachedInstance(Locale locale2) {
        SoftReference<DateFormatSymbols> ref = cachedInstances.get(locale2);
        if (ref != null) {
            DateFormatSymbols dateFormatSymbols = ref.get();
            DateFormatSymbols dfs = dateFormatSymbols;
            if (dateFormatSymbols != null) {
                return dfs;
            }
        }
        DateFormatSymbols dfs2 = new DateFormatSymbols(locale2);
        SoftReference softReference = new SoftReference(dfs2);
        SoftReference<DateFormatSymbols> x = cachedInstances.putIfAbsent(locale2, softReference);
        if (x == null) {
            return dfs2;
        }
        DateFormatSymbols y = x.get();
        if (y != null) {
            return y;
        }
        cachedInstances.put(locale2, softReference);
        return dfs2;
    }

    public String[] getEras() {
        return (String[]) Arrays.copyOf((T[]) this.eras, this.eras.length);
    }

    public void setEras(String[] newEras) {
        this.eras = (String[]) Arrays.copyOf((T[]) newEras, newEras.length);
        this.cachedHashCode = 0;
    }

    public String[] getMonths() {
        return (String[]) Arrays.copyOf((T[]) this.months, this.months.length);
    }

    public void setMonths(String[] newMonths) {
        this.months = (String[]) Arrays.copyOf((T[]) newMonths, newMonths.length);
        this.cachedHashCode = 0;
    }

    public String[] getShortMonths() {
        return (String[]) Arrays.copyOf((T[]) this.shortMonths, this.shortMonths.length);
    }

    public void setShortMonths(String[] newShortMonths) {
        this.shortMonths = (String[]) Arrays.copyOf((T[]) newShortMonths, newShortMonths.length);
        this.cachedHashCode = 0;
    }

    public String[] getWeekdays() {
        return (String[]) Arrays.copyOf((T[]) this.weekdays, this.weekdays.length);
    }

    public void setWeekdays(String[] newWeekdays) {
        this.weekdays = (String[]) Arrays.copyOf((T[]) newWeekdays, newWeekdays.length);
        this.cachedHashCode = 0;
    }

    public String[] getShortWeekdays() {
        return (String[]) Arrays.copyOf((T[]) this.shortWeekdays, this.shortWeekdays.length);
    }

    public void setShortWeekdays(String[] newShortWeekdays) {
        this.shortWeekdays = (String[]) Arrays.copyOf((T[]) newShortWeekdays, newShortWeekdays.length);
        this.cachedHashCode = 0;
    }

    public String[] getAmPmStrings() {
        return (String[]) Arrays.copyOf((T[]) this.ampms, this.ampms.length);
    }

    public void setAmPmStrings(String[] newAmpms) {
        this.ampms = (String[]) Arrays.copyOf((T[]) newAmpms, newAmpms.length);
        this.cachedHashCode = 0;
    }

    public String[][] getZoneStrings() {
        return getZoneStringsImpl(true);
    }

    public void setZoneStrings(String[][] newZoneStrings) {
        String[][] aCopy = new String[newZoneStrings.length][];
        int i = 0;
        while (i < newZoneStrings.length) {
            int len = newZoneStrings[i].length;
            if (len >= 5) {
                aCopy[i] = (String[]) Arrays.copyOf((T[]) newZoneStrings[i], len);
                i++;
            } else {
                throw new IllegalArgumentException();
            }
        }
        this.zoneStrings = aCopy;
        this.isZoneStringsSet = true;
    }

    public String getLocalPatternChars() {
        return this.localPatternChars;
    }

    public void setLocalPatternChars(String newLocalPatternChars) {
        this.localPatternChars = newLocalPatternChars.toString();
        this.cachedHashCode = 0;
    }

    /* access modifiers changed from: package-private */
    public String[] getTinyMonths() {
        return this.tinyMonths;
    }

    /* access modifiers changed from: package-private */
    public String[] getStandAloneMonths() {
        return this.standAloneMonths;
    }

    /* access modifiers changed from: package-private */
    public String[] getShortStandAloneMonths() {
        return this.shortStandAloneMonths;
    }

    /* access modifiers changed from: package-private */
    public String[] getTinyStandAloneMonths() {
        return this.tinyStandAloneMonths;
    }

    /* access modifiers changed from: package-private */
    public String[] getTinyWeekdays() {
        return this.tinyWeekdays;
    }

    /* access modifiers changed from: package-private */
    public String[] getStandAloneWeekdays() {
        return this.standAloneWeekdays;
    }

    /* access modifiers changed from: package-private */
    public String[] getShortStandAloneWeekdays() {
        return this.shortStandAloneWeekdays;
    }

    /* access modifiers changed from: package-private */
    public String[] getTinyStandAloneWeekdays() {
        return this.tinyStandAloneWeekdays;
    }

    public Object clone() {
        try {
            DateFormatSymbols other = (DateFormatSymbols) super.clone();
            copyMembers(this, other);
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    public int hashCode() {
        int hashCode = this.cachedHashCode;
        if (hashCode != 0) {
            return hashCode;
        }
        int hashCode2 = (11 * ((11 * ((11 * ((11 * ((11 * ((11 * ((11 * 5) + Arrays.hashCode((Object[]) this.eras))) + Arrays.hashCode((Object[]) this.months))) + Arrays.hashCode((Object[]) this.shortMonths))) + Arrays.hashCode((Object[]) this.weekdays))) + Arrays.hashCode((Object[]) this.shortWeekdays))) + Arrays.hashCode((Object[]) this.ampms))) + Objects.hashCode(this.localPatternChars);
        this.cachedHashCode = hashCode2;
        return hashCode2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateFormatSymbols that = (DateFormatSymbols) obj;
        if (!Arrays.equals((Object[]) this.eras, (Object[]) that.eras) || !Arrays.equals((Object[]) this.months, (Object[]) that.months) || !Arrays.equals((Object[]) this.shortMonths, (Object[]) that.shortMonths) || !Arrays.equals((Object[]) this.tinyMonths, (Object[]) that.tinyMonths) || !Arrays.equals((Object[]) this.weekdays, (Object[]) that.weekdays) || !Arrays.equals((Object[]) this.shortWeekdays, (Object[]) that.shortWeekdays) || !Arrays.equals((Object[]) this.tinyWeekdays, (Object[]) that.tinyWeekdays) || !Arrays.equals((Object[]) this.standAloneMonths, (Object[]) that.standAloneMonths) || !Arrays.equals((Object[]) this.shortStandAloneMonths, (Object[]) that.shortStandAloneMonths) || !Arrays.equals((Object[]) this.tinyStandAloneMonths, (Object[]) that.tinyStandAloneMonths) || !Arrays.equals((Object[]) this.standAloneWeekdays, (Object[]) that.standAloneWeekdays) || !Arrays.equals((Object[]) this.shortStandAloneWeekdays, (Object[]) that.shortStandAloneWeekdays) || !Arrays.equals((Object[]) this.tinyStandAloneWeekdays, (Object[]) that.tinyStandAloneWeekdays) || !Arrays.equals((Object[]) this.ampms, (Object[]) that.ampms) || ((this.localPatternChars == null || !this.localPatternChars.equals(that.localPatternChars)) && (this.localPatternChars != null || that.localPatternChars != null))) {
            return false;
        }
        if (this.isZoneStringsSet || that.isZoneStringsSet || !Objects.equals(this.locale, that.locale)) {
            return Arrays.deepEquals(getZoneStringsWrapper(), that.getZoneStringsWrapper());
        }
        return true;
    }

    private void initializeData(Locale locale2) {
        SoftReference<DateFormatSymbols> ref = cachedInstances.get(locale2);
        if (ref != null) {
            DateFormatSymbols dateFormatSymbols = ref.get();
            DateFormatSymbols dfs = dateFormatSymbols;
            if (dateFormatSymbols != null) {
                copyMembers(dfs, this);
                return;
            }
        }
        Locale locale3 = LocaleData.mapInvalidAndNullLocales(locale2);
        LocaleData localeData = LocaleData.get(locale3);
        this.locale = locale3;
        this.eras = localeData.eras;
        this.months = localeData.longMonthNames;
        this.shortMonths = localeData.shortMonthNames;
        this.ampms = localeData.amPm;
        this.localPatternChars = patternChars;
        this.weekdays = localeData.longWeekdayNames;
        this.shortWeekdays = localeData.shortWeekdayNames;
        initializeSupplementaryData(localeData);
    }

    private void initializeSupplementaryData(LocaleData localeData) {
        this.tinyMonths = localeData.tinyMonthNames;
        this.tinyWeekdays = localeData.tinyWeekdayNames;
        this.standAloneMonths = localeData.longStandAloneMonthNames;
        this.shortStandAloneMonths = localeData.shortStandAloneMonthNames;
        this.tinyStandAloneMonths = localeData.tinyStandAloneMonthNames;
        this.standAloneWeekdays = localeData.longStandAloneWeekdayNames;
        this.shortStandAloneWeekdays = localeData.shortStandAloneWeekdayNames;
        this.tinyStandAloneWeekdays = localeData.tinyStandAloneWeekdayNames;
    }

    /* access modifiers changed from: package-private */
    public final int getZoneIndex(String ID) {
        String[][] zoneStrings2 = getZoneStringsWrapper();
        if (this.lastZoneIndex < zoneStrings2.length && ID.equals(zoneStrings2[this.lastZoneIndex][0])) {
            return this.lastZoneIndex;
        }
        for (int index = 0; index < zoneStrings2.length; index++) {
            if (ID.equals(zoneStrings2[index][0])) {
                this.lastZoneIndex = index;
                return index;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public final String[][] getZoneStringsWrapper() {
        if (isSubclassObject()) {
            return getZoneStrings();
        }
        return getZoneStringsImpl(false);
    }

    private synchronized String[][] internalZoneStrings() {
        if (this.zoneStrings == null) {
            this.zoneStrings = TimeZoneNames.getZoneStrings(this.locale);
        }
        return this.zoneStrings;
    }

    private String[][] getZoneStringsImpl(boolean needsCopy) {
        String[][] zoneStrings2 = internalZoneStrings();
        if (!needsCopy) {
            return zoneStrings2;
        }
        int len = zoneStrings2.length;
        String[][] aCopy = new String[len][];
        for (int i = 0; i < len; i++) {
            aCopy[i] = (String[]) Arrays.copyOf((T[]) zoneStrings2[i], zoneStrings2[i].length);
        }
        return aCopy;
    }

    private boolean isSubclassObject() {
        return !getClass().getName().equals("java.text.DateFormatSymbols");
    }

    private void copyMembers(DateFormatSymbols src, DateFormatSymbols dst) {
        dst.locale = src.locale;
        dst.eras = (String[]) Arrays.copyOf((T[]) src.eras, src.eras.length);
        dst.months = (String[]) Arrays.copyOf((T[]) src.months, src.months.length);
        dst.shortMonths = (String[]) Arrays.copyOf((T[]) src.shortMonths, src.shortMonths.length);
        dst.weekdays = (String[]) Arrays.copyOf((T[]) src.weekdays, src.weekdays.length);
        dst.shortWeekdays = (String[]) Arrays.copyOf((T[]) src.shortWeekdays, src.shortWeekdays.length);
        dst.ampms = (String[]) Arrays.copyOf((T[]) src.ampms, src.ampms.length);
        if (src.zoneStrings != null) {
            dst.zoneStrings = src.getZoneStringsImpl(true);
        } else {
            dst.zoneStrings = null;
        }
        dst.localPatternChars = src.localPatternChars;
        dst.cachedHashCode = 0;
        dst.tinyMonths = src.tinyMonths;
        dst.tinyWeekdays = src.tinyWeekdays;
        dst.standAloneMonths = src.standAloneMonths;
        dst.shortStandAloneMonths = src.shortStandAloneMonths;
        dst.tinyStandAloneMonths = src.tinyStandAloneMonths;
        dst.standAloneWeekdays = src.standAloneWeekdays;
        dst.shortStandAloneWeekdays = src.shortStandAloneWeekdays;
        dst.tinyStandAloneWeekdays = src.tinyStandAloneWeekdays;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            initializeSupplementaryData(LocaleData.get(this.locale));
        }
        this.serialVersionOnStream = 1;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        internalZoneStrings();
        stream.defaultWriteObject();
    }
}
