package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Objects;
import java.util.TimeZone;
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
    static final int PATTERN_ERA = 0;
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
    static final String patternChars = "GyMdkHmsSEDFwWahKzZYuXLc";
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
        initializeData(Locale.getDefault(Category.FORMAT));
    }

    public DateFormatSymbols(Locale locale) {
        initializeData(locale);
    }

    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    public static final DateFormatSymbols getInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormatSymbols getInstance(Locale locale) {
        return (DateFormatSymbols) getCachedInstance(locale).clone();
    }

    static final DateFormatSymbols getInstanceRef(Locale locale) {
        return getCachedInstance(locale);
    }

    private static DateFormatSymbols getCachedInstance(Locale locale) {
        DateFormatSymbols dfs;
        SoftReference<DateFormatSymbols> ref = (SoftReference) cachedInstances.get(locale);
        if (ref != null) {
            dfs = (DateFormatSymbols) ref.get();
            if (dfs != null) {
                return dfs;
            }
        }
        dfs = new DateFormatSymbols(locale);
        ref = new SoftReference(dfs);
        SoftReference<DateFormatSymbols> x = (SoftReference) cachedInstances.putIfAbsent(locale, ref);
        if (x == null) {
            return dfs;
        }
        DateFormatSymbols y = (DateFormatSymbols) x.get();
        if (y != null) {
            return y;
        }
        cachedInstances.put(locale, ref);
        return dfs;
    }

    public String[] getEras() {
        return (String[]) Arrays.copyOf(this.eras, this.eras.length);
    }

    public void setEras(String[] newEras) {
        this.eras = (String[]) Arrays.copyOf((Object[]) newEras, newEras.length);
        this.cachedHashCode = 0;
    }

    public String[] getMonths() {
        return (String[]) Arrays.copyOf(this.months, this.months.length);
    }

    public void setMonths(String[] newMonths) {
        this.months = (String[]) Arrays.copyOf((Object[]) newMonths, newMonths.length);
        this.cachedHashCode = 0;
    }

    public String[] getShortMonths() {
        return (String[]) Arrays.copyOf(this.shortMonths, this.shortMonths.length);
    }

    public void setShortMonths(String[] newShortMonths) {
        this.shortMonths = (String[]) Arrays.copyOf((Object[]) newShortMonths, newShortMonths.length);
        this.cachedHashCode = 0;
    }

    public String[] getWeekdays() {
        return (String[]) Arrays.copyOf(this.weekdays, this.weekdays.length);
    }

    public void setWeekdays(String[] newWeekdays) {
        this.weekdays = (String[]) Arrays.copyOf((Object[]) newWeekdays, newWeekdays.length);
        this.cachedHashCode = 0;
    }

    public String[] getShortWeekdays() {
        return (String[]) Arrays.copyOf(this.shortWeekdays, this.shortWeekdays.length);
    }

    public void setShortWeekdays(String[] newShortWeekdays) {
        this.shortWeekdays = (String[]) Arrays.copyOf((Object[]) newShortWeekdays, newShortWeekdays.length);
        this.cachedHashCode = 0;
    }

    public String[] getAmPmStrings() {
        return (String[]) Arrays.copyOf(this.ampms, this.ampms.length);
    }

    public void setAmPmStrings(String[] newAmpms) {
        this.ampms = (String[]) Arrays.copyOf((Object[]) newAmpms, newAmpms.length);
        this.cachedHashCode = 0;
    }

    public String[][] getZoneStrings() {
        return getZoneStringsImpl(true);
    }

    public void setZoneStrings(String[][] newZoneStrings) {
        String[][] aCopy = new String[newZoneStrings.length][];
        for (int i = 0; i < newZoneStrings.length; i++) {
            int len = newZoneStrings[i].length;
            if (len < 5) {
                throw new IllegalArgumentException();
            }
            aCopy[i] = (String[]) Arrays.copyOf(newZoneStrings[i], len);
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

    String[] getTinyMonths() {
        return this.tinyMonths;
    }

    String[] getStandAloneMonths() {
        return this.standAloneMonths;
    }

    String[] getShortStandAloneMonths() {
        return this.shortStandAloneMonths;
    }

    String[] getTinyStandAloneMonths() {
        return this.tinyStandAloneMonths;
    }

    String[] getTinyWeekdays() {
        return this.tinyWeekdays;
    }

    String[] getStandAloneWeekdays() {
        return this.standAloneWeekdays;
    }

    String[] getShortStandAloneWeekdays() {
        return this.shortStandAloneWeekdays;
    }

    String[] getTinyStandAloneWeekdays() {
        return this.tinyStandAloneWeekdays;
    }

    public Object clone() {
        try {
            DateFormatSymbols other = (DateFormatSymbols) super.clone();
            copyMembers(this, other);
            return other;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    public int hashCode() {
        int hashCode = this.cachedHashCode;
        if (hashCode != 0) {
            return hashCode;
        }
        hashCode = ((((((((((((Arrays.hashCode(this.eras) + 55) * 11) + Arrays.hashCode(this.months)) * 11) + Arrays.hashCode(this.shortMonths)) * 11) + Arrays.hashCode(this.weekdays)) * 11) + Arrays.hashCode(this.shortWeekdays)) * 11) + Arrays.hashCode(this.ampms)) * 11) + Objects.hashCode(this.localPatternChars);
        this.cachedHashCode = hashCode;
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateFormatSymbols that = (DateFormatSymbols) obj;
        boolean z = (Arrays.equals(this.eras, that.eras) && Arrays.equals(this.months, that.months) && Arrays.equals(this.shortMonths, that.shortMonths) && Arrays.equals(this.tinyMonths, that.tinyMonths) && Arrays.equals(this.weekdays, that.weekdays) && Arrays.equals(this.shortWeekdays, that.shortWeekdays) && Arrays.equals(this.tinyWeekdays, that.tinyWeekdays) && Arrays.equals(this.standAloneMonths, that.standAloneMonths) && Arrays.equals(this.shortStandAloneMonths, that.shortStandAloneMonths) && Arrays.equals(this.tinyStandAloneMonths, that.tinyStandAloneMonths) && Arrays.equals(this.standAloneWeekdays, that.standAloneWeekdays) && Arrays.equals(this.shortStandAloneWeekdays, that.shortStandAloneWeekdays) && Arrays.equals(this.tinyStandAloneWeekdays, that.tinyStandAloneWeekdays) && Arrays.equals(this.ampms, that.ampms)) ? (this.localPatternChars == null || !this.localPatternChars.equals(that.localPatternChars)) ? this.localPatternChars == null ? that.localPatternChars == null : false : true : false;
        if (!z) {
            return false;
        }
        if (this.isZoneStringsSet || (that.isZoneStringsSet ^ 1) == 0 || !Objects.equals(this.locale, that.locale)) {
            return Arrays.deepEquals(getZoneStringsWrapper(), that.getZoneStringsWrapper());
        }
        return true;
    }

    private void initializeData(Locale desiredLocale) {
        this.locale = desiredLocale;
        SoftReference<DateFormatSymbols> ref = (SoftReference) cachedInstances.get(this.locale);
        if (ref != null) {
            DateFormatSymbols dfs = (DateFormatSymbols) ref.get();
            if (dfs != null) {
                copyMembers(dfs, this);
                return;
            }
        }
        this.locale = LocaleData.mapInvalidAndNullLocales(this.locale);
        LocaleData localeData = LocaleData.get(this.locale);
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

    final int getZoneIndex(String ID) {
        String[][] zoneStrings = getZoneStringsWrapper();
        if (this.lastZoneIndex < zoneStrings.length && ID.equals(zoneStrings[this.lastZoneIndex][0])) {
            return this.lastZoneIndex;
        }
        for (int index = 0; index < zoneStrings.length; index++) {
            if (ID.equals(zoneStrings[index][0])) {
                this.lastZoneIndex = index;
                return index;
            }
        }
        return -1;
    }

    final String[][] getZoneStringsWrapper() {
        if (isSubclassObject()) {
            return getZoneStrings();
        }
        return getZoneStringsImpl(false);
    }

    private final synchronized String[][] internalZoneStrings() {
        synchronized (this) {
            if (this.zoneStrings == null) {
                this.zoneStrings = TimeZoneNames.getZoneStrings(this.locale);
                for (String[] zone : this.zoneStrings) {
                    String id = zone[0];
                    if (zone[1] == null) {
                        zone[1] = TimeZone.getTimeZone(id).getDisplayName(false, 1, this.locale);
                    }
                    if (zone[2] == null) {
                        zone[2] = TimeZone.getTimeZone(id).getDisplayName(false, 0, this.locale);
                    }
                    if (zone[3] == null) {
                        zone[3] = TimeZone.getTimeZone(id).getDisplayName(true, 1, this.locale);
                    }
                    if (zone[4] == null) {
                        zone[4] = TimeZone.getTimeZone(id).getDisplayName(true, 0, this.locale);
                    }
                }
            }
        }
        return this.zoneStrings;
    }

    private final String[][] getZoneStringsImpl(boolean needsCopy) {
        String[][] zoneStrings = internalZoneStrings();
        if (!needsCopy) {
            return zoneStrings;
        }
        int len = zoneStrings.length;
        String[][] aCopy = new String[len][];
        for (int i = 0; i < len; i++) {
            aCopy[i] = (String[]) Arrays.copyOf(zoneStrings[i], zoneStrings[i].length);
        }
        return aCopy;
    }

    private boolean isSubclassObject() {
        return getClass().getName().equals("java.text.DateFormatSymbols") ^ 1;
    }

    private void copyMembers(DateFormatSymbols src, DateFormatSymbols dst) {
        dst.eras = (String[]) Arrays.copyOf(src.eras, src.eras.length);
        dst.months = (String[]) Arrays.copyOf(src.months, src.months.length);
        dst.shortMonths = (String[]) Arrays.copyOf(src.shortMonths, src.shortMonths.length);
        dst.weekdays = (String[]) Arrays.copyOf(src.weekdays, src.weekdays.length);
        dst.shortWeekdays = (String[]) Arrays.copyOf(src.shortWeekdays, src.shortWeekdays.length);
        dst.ampms = (String[]) Arrays.copyOf(src.ampms, src.ampms.length);
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
