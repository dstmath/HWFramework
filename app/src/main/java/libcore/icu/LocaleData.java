package libcore.icu;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import libcore.util.Objects;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class LocaleData {
    private static final HashMap<String, LocaleData> localeDataCache = null;
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
    public char perMill;
    public String percent;
    public String percentPattern;
    public String shortDateFormat;
    public String shortDateFormat4;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.icu.LocaleData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.icu.LocaleData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.LocaleData.<clinit>():void");
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

    public static LocaleData get(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        String languageTag = locale.toLanguageTag();
        synchronized (localeDataCache) {
            LocaleData localeData = (LocaleData) localeDataCache.get(languageTag);
            if (localeData != null) {
                return localeData;
            }
            LocaleData newLocaleData = initLocaleData(locale);
            synchronized (localeDataCache) {
                localeData = (LocaleData) localeDataCache.get(languageTag);
                if (localeData != null) {
                    return localeData;
                }
                localeDataCache.put(languageTag, newLocaleData);
                return newLocaleData;
            }
        }
    }

    public String toString() {
        return Objects.toString(this);
    }

    public String getDateFormat(int style) {
        switch (style) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                return this.fullDateFormat;
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return this.longDateFormat;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return this.mediumDateFormat;
            case XmlPullParser.END_TAG /*3*/:
                return this.shortDateFormat;
            default:
                throw new AssertionError();
        }
    }

    public String getTimeFormat(int style) {
        switch (style) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                return this.fullTimeFormat;
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return this.longTimeFormat;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                if (DateFormat.is24Hour == null) {
                    return this.mediumTimeFormat;
                }
                return DateFormat.is24Hour.booleanValue() ? this.timeFormat_Hms : this.timeFormat_hms;
            case XmlPullParser.END_TAG /*3*/:
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
                localeData.integerPattern = localeData.numberPattern.replaceAll("\\.[#,]*", XmlPullParser.NO_NAMESPACE);
            }
            localeData.shortDateFormat4 = localeData.shortDateFormat.replaceAll("\\byy\\b", android.icu.text.DateFormat.YEAR);
            return localeData;
        }
        throw new AssertionError("couldn't initialize LocaleData for locale " + locale);
    }
}
