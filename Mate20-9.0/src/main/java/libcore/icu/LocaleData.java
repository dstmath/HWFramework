package libcore.icu;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import libcore.util.Objects;

public final class LocaleData {
    private static final HashMap<String, LocaleData> localeDataCache = new HashMap<>();
    public String NaN;
    public String[] amPm;
    public String currencyPattern;
    public String currencySymbol;
    public char decimalSeparator;
    public String[] eras;
    public String exponentSeparator;
    public Integer firstDayOfWeek;
    public String fullDateFormat;
    public String fullTimeFormat;
    public char groupingSeparator;
    public String infinity;
    public String integerPattern;
    public String internationalCurrencySymbol;
    public String longDateFormat;
    public String[] longMonthNames;
    public String[] longStandAloneMonthNames;
    public String[] longStandAloneWeekdayNames;
    public String longTimeFormat;
    public String[] longWeekdayNames;
    public String mediumDateFormat;
    public String mediumTimeFormat;
    public Integer minimalDaysInFirstWeek;
    public String minusSign;
    public char monetarySeparator;
    public String narrowAm;
    public String narrowPm;
    public String numberPattern;
    public char patternSeparator;
    public String perMill;
    public String percent;
    public String percentPattern;
    public String shortDateFormat;
    public String[] shortMonthNames;
    public String[] shortStandAloneMonthNames;
    public String[] shortStandAloneWeekdayNames;
    public String shortTimeFormat;
    public String[] shortWeekdayNames;
    public String timeFormat_Hm;
    public String timeFormat_Hms;
    public String timeFormat_hm;
    public String timeFormat_hms;
    public String[] tinyMonthNames;
    public String[] tinyStandAloneMonthNames;
    public String[] tinyStandAloneWeekdayNames;
    public String[] tinyWeekdayNames;
    public String today;
    public String tomorrow;
    public String yesterday;
    public char zeroDigit;

    static {
        get(Locale.ROOT);
        get(Locale.US);
        get(Locale.getDefault());
    }

    private LocaleData() {
    }

    public static Locale mapInvalidAndNullLocales(Locale locale) {
        if (locale == null) {
            return Locale.getDefault();
        }
        if ("und".equals(locale.toLanguageTag())) {
            return Locale.ROOT;
        }
        return locale;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r1 = localeDataCache.get(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
        if (r1 == null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        localeDataCache.put(r0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002e, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002f, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        r2 = initLocaleData(r5);
        r3 = localeDataCache;
     */
    public static LocaleData get(Locale locale) {
        if (locale != null) {
            String languageTag = locale.toLanguageTag();
            synchronized (localeDataCache) {
                LocaleData localeData = localeDataCache.get(languageTag);
                if (localeData != null) {
                    return localeData;
                }
            }
        } else {
            throw new NullPointerException("locale == null");
        }
    }

    public String toString() {
        return Objects.toString(this);
    }

    public String getDateFormat(int style) {
        switch (style) {
            case 0:
                return this.fullDateFormat;
            case 1:
                return this.longDateFormat;
            case 2:
                return this.mediumDateFormat;
            case 3:
                return this.shortDateFormat;
            default:
                throw new AssertionError();
        }
    }

    public String getTimeFormat(int style) {
        switch (style) {
            case 0:
                return this.fullTimeFormat;
            case 1:
                return this.longTimeFormat;
            case 2:
                if (DateFormat.is24Hour == null) {
                    return this.mediumTimeFormat;
                }
                return DateFormat.is24Hour.booleanValue() ? this.timeFormat_Hms : this.timeFormat_hms;
            case 3:
                if (DateFormat.is24Hour == null) {
                    return this.shortTimeFormat;
                }
                return DateFormat.is24Hour.booleanValue() ? this.timeFormat_Hm : this.timeFormat_hm;
            default:
                throw new AssertionError();
        }
    }

    private static LocaleData initLocaleData(Locale locale) {
        LocaleData localeData = new LocaleData();
        if (ICU.initLocaleDataNative(locale.toLanguageTag(), localeData)) {
            localeData.timeFormat_hm = ICU.getBestDateTimePattern("hm", locale);
            localeData.timeFormat_Hm = ICU.getBestDateTimePattern(android.icu.text.DateFormat.HOUR24_MINUTE, locale);
            localeData.timeFormat_hms = ICU.getBestDateTimePattern("hms", locale);
            localeData.timeFormat_Hms = ICU.getBestDateTimePattern(android.icu.text.DateFormat.HOUR24_MINUTE_SECOND, locale);
            if (localeData.fullTimeFormat != null) {
                localeData.fullTimeFormat = localeData.fullTimeFormat.replace('v', 'z');
            }
            if (localeData.numberPattern != null) {
                localeData.integerPattern = localeData.numberPattern.replaceAll("\\.[#,]*", "");
            }
            return localeData;
        }
        throw new AssertionError("couldn't initialize LocaleData for locale " + locale);
    }
}
