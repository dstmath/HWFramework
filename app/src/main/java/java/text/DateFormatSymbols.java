package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.spi.DateFormatSymbolsProvider;
import java.util.Arrays;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.LocaleData;
import libcore.icu.TimeZoneNames;
import sun.util.LocaleServiceProviderPool;
import sun.util.LocaleServiceProviderPool.LocalizedObjectGetter;

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
    static final int PATTERN_SECOND = 7;
    static final int PATTERN_STANDALONE_DAY_OF_WEEK = 23;
    static final int PATTERN_STANDALONE_MONTH = 22;
    static final int PATTERN_WEEK_OF_MONTH = 13;
    static final int PATTERN_WEEK_OF_YEAR = 12;
    static final int PATTERN_WEEK_YEAR = 19;
    static final int PATTERN_YEAR = 1;
    static final int PATTERN_ZONE_NAME = 17;
    static final int PATTERN_ZONE_VALUE = 18;
    private static final ConcurrentMap<Locale, SoftReference<DateFormatSymbols>> cachedInstances = null;
    static final int currentSerialVersion = 1;
    static final int millisPerHour = 3600000;
    static final String patternChars = "GyMdkHmsSEDFwWahKzZYuXLc";
    static final long serialVersionUID = -5987973545549424702L;
    String[] ampms;
    String[] eras;
    transient boolean isZoneStringsSet;
    private transient int lastZoneIndex;
    String localPatternChars;
    Locale locale;
    String[] months;
    private int serialVersionOnStream;
    String[] shortMonths;
    private String[] shortStandAloneMonths;
    private String[] shortStandAloneWeekdays;
    String[] shortWeekdays;
    private String[] standAloneMonths;
    private String[] standAloneWeekdays;
    private String[] tinyMonths;
    private String[] tinyStandAloneMonths;
    private String[] tinyStandAloneWeekdays;
    private String[] tinyWeekdays;
    String[] weekdays;
    String[][] zoneStrings;

    private static class DateFormatSymbolsGetter implements LocalizedObjectGetter<DateFormatSymbolsProvider, DateFormatSymbols> {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final DateFormatSymbolsGetter INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.DateFormatSymbols.DateFormatSymbolsGetter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.DateFormatSymbols.DateFormatSymbolsGetter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.text.DateFormatSymbols.DateFormatSymbolsGetter.<clinit>():void");
        }

        private DateFormatSymbolsGetter() {
        }

        public DateFormatSymbols getObject(DateFormatSymbolsProvider dateFormatSymbolsProvider, Locale locale, String key, Object... params) {
            Object obj = null;
            if (!-assertionsDisabled) {
                if (params.length == 0) {
                    obj = DateFormatSymbols.currentSerialVersion;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return dateFormatSymbolsProvider.getInstance(locale);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.DateFormatSymbols.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.DateFormatSymbols.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.text.DateFormatSymbols.<clinit>():void");
    }

    public DateFormatSymbols() {
        this.eras = null;
        this.months = null;
        this.shortMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.ampms = null;
        this.zoneStrings = null;
        this.isZoneStringsSet = false;
        this.localPatternChars = null;
        this.locale = null;
        this.serialVersionOnStream = currentSerialVersion;
        this.lastZoneIndex = PATTERN_ERA;
        initializeData(Locale.getDefault(Category.FORMAT));
    }

    public DateFormatSymbols(Locale locale) {
        this.eras = null;
        this.months = null;
        this.shortMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.ampms = null;
        this.zoneStrings = null;
        this.isZoneStringsSet = false;
        this.localPatternChars = null;
        this.locale = null;
        this.serialVersionOnStream = currentSerialVersion;
        this.lastZoneIndex = PATTERN_ERA;
        initializeData(locale);
    }

    public static Locale[] getAvailableLocales() {
        return LocaleServiceProviderPool.getPool(DateFormatSymbolsProvider.class).getAvailableLocales();
    }

    public static final DateFormatSymbols getInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormatSymbols getInstance(Locale locale) {
        DateFormatSymbols dfs = getProviderInstance(locale);
        if (dfs != null) {
            return dfs;
        }
        return (DateFormatSymbols) getCachedInstance(locale).clone();
    }

    static final DateFormatSymbols getInstanceRef(Locale locale) {
        DateFormatSymbols dfs = getProviderInstance(locale);
        if (dfs != null) {
            return dfs;
        }
        return getCachedInstance(locale);
    }

    private static DateFormatSymbols getProviderInstance(Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(DateFormatSymbolsProvider.class);
        if (pool.hasProviders()) {
            return (DateFormatSymbols) pool.getLocalizedObject(DateFormatSymbolsGetter.INSTANCE, locale, new Object[PATTERN_ERA]);
        }
        return null;
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
    }

    public String[] getMonths() {
        return (String[]) Arrays.copyOf(this.months, this.months.length);
    }

    public void setMonths(String[] newMonths) {
        this.months = (String[]) Arrays.copyOf((Object[]) newMonths, newMonths.length);
    }

    public String[] getShortMonths() {
        return (String[]) Arrays.copyOf(this.shortMonths, this.shortMonths.length);
    }

    public void setShortMonths(String[] newShortMonths) {
        this.shortMonths = (String[]) Arrays.copyOf((Object[]) newShortMonths, newShortMonths.length);
    }

    public String[] getWeekdays() {
        return (String[]) Arrays.copyOf(this.weekdays, this.weekdays.length);
    }

    public void setWeekdays(String[] newWeekdays) {
        this.weekdays = (String[]) Arrays.copyOf((Object[]) newWeekdays, newWeekdays.length);
    }

    public String[] getShortWeekdays() {
        return (String[]) Arrays.copyOf(this.shortWeekdays, this.shortWeekdays.length);
    }

    public void setShortWeekdays(String[] newShortWeekdays) {
        this.shortWeekdays = (String[]) Arrays.copyOf((Object[]) newShortWeekdays, newShortWeekdays.length);
    }

    public String[] getAmPmStrings() {
        return (String[]) Arrays.copyOf(this.ampms, this.ampms.length);
    }

    public void setAmPmStrings(String[] newAmpms) {
        this.ampms = (String[]) Arrays.copyOf((Object[]) newAmpms, newAmpms.length);
    }

    public String[][] getZoneStrings() {
        return getZoneStringsImpl(true);
    }

    public void setZoneStrings(String[][] newZoneStrings) {
        String[][] aCopy = new String[newZoneStrings.length][];
        for (int i = PATTERN_ERA; i < newZoneStrings.length; i += currentSerialVersion) {
            int len = newZoneStrings[i].length;
            if (len < PATTERN_HOUR_OF_DAY0) {
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
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public int hashCode() {
        int hashcode = PATTERN_ERA;
        String[][] zoneStrings = getZoneStringsWrapper();
        for (int index = PATTERN_ERA; index < zoneStrings[PATTERN_ERA].length; index += currentSerialVersion) {
            hashcode ^= zoneStrings[PATTERN_ERA][index].hashCode();
        }
        return hashcode;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateFormatSymbols that = (DateFormatSymbols) obj;
        if (!Arrays.equals(this.eras, that.eras) || !Arrays.equals(this.months, that.months) || !Arrays.equals(this.shortMonths, that.shortMonths) || !Arrays.equals(this.tinyMonths, that.tinyMonths) || !Arrays.equals(this.weekdays, that.weekdays) || !Arrays.equals(this.shortWeekdays, that.shortWeekdays) || !Arrays.equals(this.tinyWeekdays, that.tinyWeekdays) || !Arrays.equals(this.standAloneMonths, that.standAloneMonths) || !Arrays.equals(this.shortStandAloneMonths, that.shortStandAloneMonths) || !Arrays.equals(this.tinyStandAloneMonths, that.tinyStandAloneMonths) || !Arrays.equals(this.standAloneWeekdays, that.standAloneWeekdays) || !Arrays.equals(this.shortStandAloneWeekdays, that.shortStandAloneWeekdays) || !Arrays.equals(this.tinyStandAloneWeekdays, that.tinyStandAloneWeekdays) || !Arrays.equals(this.ampms, that.ampms) || !Arrays.deepEquals(getZoneStringsWrapper(), that.getZoneStringsWrapper())) {
            z = false;
        } else if (this.localPatternChars == null || !this.localPatternChars.equals(that.localPatternChars)) {
            if (this.localPatternChars != null) {
                z = false;
            } else if (that.localPatternChars != null) {
                z = false;
            }
        }
        return z;
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
        if (this.lastZoneIndex < zoneStrings.length && ID.equals(zoneStrings[this.lastZoneIndex][PATTERN_ERA])) {
            return this.lastZoneIndex;
        }
        for (int index = PATTERN_ERA; index < zoneStrings.length; index += currentSerialVersion) {
            if (ID.equals(zoneStrings[index][PATTERN_ERA])) {
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
        String[][] strArr;
        synchronized (this) {
            if (this.zoneStrings == null) {
                this.zoneStrings = TimeZoneNames.getZoneStrings(this.locale);
                String[][] strArr2 = this.zoneStrings;
                int length = strArr2.length;
                for (int i = PATTERN_ERA; i < length; i += currentSerialVersion) {
                    String[] zone = strArr2[i];
                    String id = zone[PATTERN_ERA];
                    if (zone[currentSerialVersion] == null) {
                        zone[currentSerialVersion] = TimeZone.getTimeZone(id).getDisplayName(false, currentSerialVersion, this.locale);
                    }
                    if (zone[PATTERN_MONTH] == null) {
                        zone[PATTERN_MONTH] = TimeZone.getTimeZone(id).getDisplayName(false, PATTERN_ERA, this.locale);
                    }
                    if (zone[PATTERN_DAY_OF_MONTH] == null) {
                        zone[PATTERN_DAY_OF_MONTH] = TimeZone.getTimeZone(id).getDisplayName(true, currentSerialVersion, this.locale);
                    }
                    if (zone[PATTERN_HOUR_OF_DAY1] == null) {
                        zone[PATTERN_HOUR_OF_DAY1] = TimeZone.getTimeZone(id).getDisplayName(true, PATTERN_ERA, this.locale);
                    }
                }
            }
            strArr = this.zoneStrings;
        }
        return strArr;
    }

    private final String[][] getZoneStringsImpl(boolean needsCopy) {
        String[][] zoneStrings = internalZoneStrings();
        if (!needsCopy) {
            return zoneStrings;
        }
        int len = zoneStrings.length;
        String[][] aCopy = new String[len][];
        for (int i = PATTERN_ERA; i < len; i += currentSerialVersion) {
            aCopy[i] = (String[]) Arrays.copyOf(zoneStrings[i], zoneStrings[i].length);
        }
        return aCopy;
    }

    private final boolean isSubclassObject() {
        return !getClass().getName().equals("java.text.DateFormatSymbols");
    }

    private final void copyMembers(DateFormatSymbols src, DateFormatSymbols dst) {
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
        if (this.serialVersionOnStream < currentSerialVersion) {
            initializeSupplementaryData(LocaleData.get(this.locale));
        }
        this.serialVersionOnStream = currentSerialVersion;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        internalZoneStrings();
        stream.defaultWriteObject();
    }
}
