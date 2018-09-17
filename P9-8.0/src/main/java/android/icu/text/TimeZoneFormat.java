package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.SoftCache;
import android.icu.impl.TZDBTimeZoneNames;
import android.icu.impl.TextTrieMap;
import android.icu.impl.TimeZoneGenericNames;
import android.icu.impl.TimeZoneGenericNames.GenericMatchInfo;
import android.icu.impl.TimeZoneGenericNames.GenericNameType;
import android.icu.impl.TimeZoneNamesImpl;
import android.icu.impl.ZoneMeta;
import android.icu.lang.UCharacter;
import android.icu.text.DateFormat.Field;
import android.icu.text.TimeZoneNames.MatchInfo;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.Calendar;
import android.icu.util.Freezable;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

public class TimeZoneFormat extends UFormat implements Freezable<TimeZoneFormat>, Serializable {
    private static final /* synthetic */ int[] -android-icu-text-TimeZoneFormat$StyleSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = (TimeZoneFormat.class.desiredAssertionStatus() ^ 1);
    private static final EnumSet<GenericNameType> ALL_GENERIC_NAME_TYPES = EnumSet.of(GenericNameType.LOCATION, GenericNameType.LONG, GenericNameType.SHORT);
    private static final EnumSet<NameType> ALL_SIMPLE_NAME_TYPES = EnumSet.of(NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT, NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT, NameType.EXEMPLAR_LOCATION);
    private static final String[] ALT_GMT_STRINGS = new String[]{DEFAULT_GMT_ZERO, "UTC", "UT"};
    private static final String ASCII_DIGITS = "0123456789";
    private static final String[] DEFAULT_GMT_DIGITS = new String[]{AndroidHardcodedSystemProperties.JAVA_VERSION, "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final char DEFAULT_GMT_OFFSET_SEP = ':';
    private static final String DEFAULT_GMT_PATTERN = "GMT{0}";
    private static final String DEFAULT_GMT_ZERO = "GMT";
    private static final String ISO8601_UTC = "Z";
    private static final int ISO_LOCAL_STYLE_FLAG = 256;
    private static final int ISO_Z_STYLE_FLAG = 128;
    private static final int MAX_OFFSET = 86400000;
    private static final int MAX_OFFSET_HOUR = 23;
    private static final int MAX_OFFSET_MINUTE = 59;
    private static final int MAX_OFFSET_SECOND = 59;
    private static final int MILLIS_PER_HOUR = 3600000;
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final int MILLIS_PER_SECOND = 1000;
    private static final GMTOffsetPatternType[] PARSE_GMT_OFFSET_TYPES = new GMTOffsetPatternType[]{GMTOffsetPatternType.POSITIVE_HMS, GMTOffsetPatternType.NEGATIVE_HMS, GMTOffsetPatternType.POSITIVE_HM, GMTOffsetPatternType.NEGATIVE_HM, GMTOffsetPatternType.POSITIVE_H, GMTOffsetPatternType.NEGATIVE_H};
    private static volatile TextTrieMap<String> SHORT_ZONE_ID_TRIE = null;
    private static final String TZID_GMT = "Etc/GMT";
    private static final String UNKNOWN_LOCATION = "Unknown";
    private static final int UNKNOWN_OFFSET = Integer.MAX_VALUE;
    private static final String UNKNOWN_SHORT_ZONE_ID = "unk";
    private static final String UNKNOWN_ZONE_ID = "Etc/Unknown";
    private static volatile TextTrieMap<String> ZONE_ID_TRIE = null;
    private static TimeZoneFormatCache _tzfCache = new TimeZoneFormatCache();
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("_locale", ULocale.class), new ObjectStreamField("_tznames", TimeZoneNames.class), new ObjectStreamField("_gmtPattern", String.class), new ObjectStreamField("_gmtOffsetPatterns", String[].class), new ObjectStreamField("_gmtOffsetDigits", String[].class), new ObjectStreamField("_gmtZeroFormat", String.class), new ObjectStreamField("_parseAllStyles", Boolean.TYPE)};
    private static final long serialVersionUID = 2281246852693575022L;
    private transient boolean _abuttingOffsetHoursAndMinutes;
    private volatile transient boolean _frozen;
    private String[] _gmtOffsetDigits;
    private transient Object[][] _gmtOffsetPatternItems;
    private String[] _gmtOffsetPatterns;
    private String _gmtPattern;
    private transient String _gmtPatternPrefix;
    private transient String _gmtPatternSuffix;
    private String _gmtZeroFormat = DEFAULT_GMT_ZERO;
    private volatile transient TimeZoneGenericNames _gnames;
    private ULocale _locale;
    private boolean _parseAllStyles;
    private boolean _parseTZDBNames;
    private transient String _region;
    private volatile transient TimeZoneNames _tzdbNames;
    private TimeZoneNames _tznames;

    private static class GMTOffsetField {
        final char _type;
        final int _width;

        GMTOffsetField(char type, int width) {
            this._type = type;
            this._width = width;
        }

        char getType() {
            return this._type;
        }

        int getWidth() {
            return this._width;
        }

        static boolean isValid(char type, int width) {
            return width == 1 || width == 2;
        }
    }

    public enum GMTOffsetPatternType {
        POSITIVE_HM("+H:mm", DateFormat.HOUR24_MINUTE, true),
        POSITIVE_HMS("+H:mm:ss", DateFormat.HOUR24_MINUTE_SECOND, true),
        NEGATIVE_HM("-H:mm", DateFormat.HOUR24_MINUTE, false),
        NEGATIVE_HMS("-H:mm:ss", DateFormat.HOUR24_MINUTE_SECOND, false),
        POSITIVE_H("+H", DateFormat.HOUR24, true),
        NEGATIVE_H("-H", DateFormat.HOUR24, false);
        
        private String _defaultPattern;
        private boolean _isPositive;
        private String _required;

        private GMTOffsetPatternType(String defaultPattern, String required, boolean isPositive) {
            this._defaultPattern = defaultPattern;
            this._required = required;
            this._isPositive = isPositive;
        }

        private String defaultPattern() {
            return this._defaultPattern;
        }

        private String required() {
            return this._required;
        }

        private boolean isPositive() {
            return this._isPositive;
        }
    }

    private enum OffsetFields {
        H,
        HM,
        HMS
    }

    public enum ParseOption {
        ALL_STYLES,
        TZ_DATABASE_ABBREVIATIONS
    }

    public enum Style {
        GENERIC_LOCATION(1),
        GENERIC_LONG(2),
        GENERIC_SHORT(4),
        SPECIFIC_LONG(8),
        SPECIFIC_SHORT(16),
        LOCALIZED_GMT(32),
        LOCALIZED_GMT_SHORT(64),
        ISO_BASIC_SHORT(128),
        ISO_BASIC_LOCAL_SHORT(256),
        ISO_BASIC_FIXED(128),
        ISO_BASIC_LOCAL_FIXED(256),
        ISO_BASIC_FULL(128),
        ISO_BASIC_LOCAL_FULL(256),
        ISO_EXTENDED_FIXED(128),
        ISO_EXTENDED_LOCAL_FIXED(256),
        ISO_EXTENDED_FULL(128),
        ISO_EXTENDED_LOCAL_FULL(256),
        ZONE_ID(512),
        ZONE_ID_SHORT(1024),
        EXEMPLAR_LOCATION(2048);
        
        final int flag;

        private Style(int flag) {
            this.flag = flag;
        }
    }

    public enum TimeType {
        UNKNOWN,
        STANDARD,
        DAYLIGHT
    }

    private static class TimeZoneFormatCache extends SoftCache<ULocale, TimeZoneFormat, ULocale> {
        /* synthetic */ TimeZoneFormatCache(TimeZoneFormatCache -this0) {
            this();
        }

        private TimeZoneFormatCache() {
        }

        protected TimeZoneFormat createInstance(ULocale key, ULocale data) {
            TimeZoneFormat fmt = new TimeZoneFormat(data);
            fmt.freeze();
            return fmt;
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-text-TimeZoneFormat$StyleSwitchesValues() {
        if (-android-icu-text-TimeZoneFormat$StyleSwitchesValues != null) {
            return -android-icu-text-TimeZoneFormat$StyleSwitchesValues;
        }
        int[] iArr = new int[Style.values().length];
        try {
            iArr[Style.EXEMPLAR_LOCATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Style.GENERIC_LOCATION.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Style.GENERIC_LONG.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Style.GENERIC_SHORT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Style.ISO_BASIC_FIXED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Style.ISO_BASIC_FULL.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Style.ISO_BASIC_LOCAL_FIXED.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Style.ISO_BASIC_LOCAL_FULL.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Style.ISO_BASIC_LOCAL_SHORT.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Style.ISO_BASIC_SHORT.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Style.ISO_EXTENDED_FIXED.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Style.ISO_EXTENDED_FULL.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Style.ISO_EXTENDED_LOCAL_FIXED.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Style.ISO_EXTENDED_LOCAL_FULL.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Style.LOCALIZED_GMT.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Style.LOCALIZED_GMT_SHORT.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Style.SPECIFIC_LONG.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Style.SPECIFIC_SHORT.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Style.ZONE_ID.ordinal()] = 19;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Style.ZONE_ID_SHORT.ordinal()] = 20;
        } catch (NoSuchFieldError e20) {
        }
        -android-icu-text-TimeZoneFormat$StyleSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues() {
        if (-android-icu-text-TimeZoneNames$NameTypeSwitchesValues != null) {
            return -android-icu-text-TimeZoneNames$NameTypeSwitchesValues;
        }
        int[] iArr = new int[NameType.values().length];
        try {
            iArr[NameType.EXEMPLAR_LOCATION.ordinal()] = 25;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[NameType.LONG_DAYLIGHT.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[NameType.LONG_GENERIC.ordinal()] = 26;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[NameType.LONG_STANDARD.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[NameType.SHORT_DAYLIGHT.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[NameType.SHORT_GENERIC.ordinal()] = 27;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[NameType.SHORT_STANDARD.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = iArr;
        return iArr;
    }

    protected TimeZoneFormat(ULocale locale) {
        int i = 0;
        this._locale = locale;
        this._tznames = TimeZoneNames.getInstance(locale);
        String gmtPattern = null;
        String hourFormats = null;
        try {
            ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_ZONE_BASE_NAME, locale);
            try {
                gmtPattern = bundle.getStringWithFallback("zoneStrings/gmtFormat");
            } catch (MissingResourceException e) {
            }
            try {
                hourFormats = bundle.getStringWithFallback("zoneStrings/hourFormat");
            } catch (MissingResourceException e2) {
            }
            try {
                this._gmtZeroFormat = bundle.getStringWithFallback("zoneStrings/gmtZeroFormat");
            } catch (MissingResourceException e3) {
            }
        } catch (MissingResourceException e4) {
        }
        if (gmtPattern == null) {
            gmtPattern = DEFAULT_GMT_PATTERN;
        }
        initGMTPattern(gmtPattern);
        String[] gmtOffsetPatterns = new String[GMTOffsetPatternType.values().length];
        if (hourFormats != null) {
            String[] hourPatterns = hourFormats.split(";", 2);
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_H.ordinal()] = truncateOffsetPattern(hourPatterns[0]);
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()] = hourPatterns[0];
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[0]);
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_H.ordinal()] = truncateOffsetPattern(hourPatterns[1]);
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()] = hourPatterns[1];
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[1]);
        } else {
            GMTOffsetPatternType[] values = GMTOffsetPatternType.values();
            int length = values.length;
            while (i < length) {
                GMTOffsetPatternType patType = values[i];
                gmtOffsetPatterns[patType.ordinal()] = patType.defaultPattern();
                i++;
            }
        }
        initGMTOffsetPatterns(gmtOffsetPatterns);
        this._gmtOffsetDigits = DEFAULT_GMT_DIGITS;
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        if (!ns.isAlgorithmic()) {
            this._gmtOffsetDigits = toCodePoints(ns.getDescription());
        }
    }

    public static TimeZoneFormat getInstance(ULocale locale) {
        if (locale != null) {
            return (TimeZoneFormat) _tzfCache.getInstance(locale, locale);
        }
        throw new NullPointerException("locale is null");
    }

    public static TimeZoneFormat getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public TimeZoneNames getTimeZoneNames() {
        return this._tznames;
    }

    private TimeZoneGenericNames getTimeZoneGenericNames() {
        if (this._gnames == null) {
            synchronized (this) {
                if (this._gnames == null) {
                    this._gnames = TimeZoneGenericNames.getInstance(this._locale);
                }
            }
        }
        return this._gnames;
    }

    private TimeZoneNames getTZDBTimeZoneNames() {
        if (this._tzdbNames == null) {
            synchronized (this) {
                if (this._tzdbNames == null) {
                    this._tzdbNames = new TZDBTimeZoneNames(this._locale);
                }
            }
        }
        return this._tzdbNames;
    }

    public TimeZoneFormat setTimeZoneNames(TimeZoneNames tznames) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        this._tznames = tznames;
        this._gnames = new TimeZoneGenericNames(this._locale, this._tznames);
        return this;
    }

    public String getGMTPattern() {
        return this._gmtPattern;
    }

    public TimeZoneFormat setGMTPattern(String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        initGMTPattern(pattern);
        return this;
    }

    public String getGMTOffsetPattern(GMTOffsetPatternType type) {
        return this._gmtOffsetPatterns[type.ordinal()];
    }

    public TimeZoneFormat setGMTOffsetPattern(GMTOffsetPatternType type, String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        } else if (pattern == null) {
            throw new NullPointerException("Null GMT offset pattern");
        } else {
            Object[] parsedItems = parseOffsetPattern(pattern, type.required());
            this._gmtOffsetPatterns[type.ordinal()] = pattern;
            this._gmtOffsetPatternItems[type.ordinal()] = parsedItems;
            checkAbuttingHoursAndMinutes();
            return this;
        }
    }

    public String getGMTOffsetDigits() {
        StringBuilder buf = new StringBuilder(this._gmtOffsetDigits.length);
        for (String digit : this._gmtOffsetDigits) {
            buf.append(digit);
        }
        return buf.toString();
    }

    public TimeZoneFormat setGMTOffsetDigits(String digits) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        } else if (digits == null) {
            throw new NullPointerException("Null GMT offset digits");
        } else {
            String[] digitArray = toCodePoints(digits);
            if (digitArray.length != 10) {
                throw new IllegalArgumentException("Length of digits must be 10");
            }
            this._gmtOffsetDigits = digitArray;
            return this;
        }
    }

    public String getGMTZeroFormat() {
        return this._gmtZeroFormat;
    }

    public TimeZoneFormat setGMTZeroFormat(String gmtZeroFormat) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        } else if (gmtZeroFormat == null) {
            throw new NullPointerException("Null GMT zero format");
        } else if (gmtZeroFormat.length() == 0) {
            throw new IllegalArgumentException("Empty GMT zero format");
        } else {
            this._gmtZeroFormat = gmtZeroFormat;
            return this;
        }
    }

    public TimeZoneFormat setDefaultParseOptions(EnumSet<ParseOption> options) {
        this._parseAllStyles = options.contains(ParseOption.ALL_STYLES);
        this._parseTZDBNames = options.contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        return this;
    }

    public EnumSet<ParseOption> getDefaultParseOptions() {
        if (this._parseAllStyles && this._parseTZDBNames) {
            return EnumSet.of(ParseOption.ALL_STYLES, ParseOption.TZ_DATABASE_ABBREVIATIONS);
        }
        if (this._parseAllStyles) {
            return EnumSet.of(ParseOption.ALL_STYLES);
        }
        if (this._parseTZDBNames) {
            return EnumSet.of(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        }
        return EnumSet.noneOf(ParseOption.class);
    }

    public final String formatOffsetISO8601Basic(int offset, boolean useUtcIndicator, boolean isShort, boolean ignoreSeconds) {
        return formatOffsetISO8601(offset, true, useUtcIndicator, isShort, ignoreSeconds);
    }

    public final String formatOffsetISO8601Extended(int offset, boolean useUtcIndicator, boolean isShort, boolean ignoreSeconds) {
        return formatOffsetISO8601(offset, false, useUtcIndicator, isShort, ignoreSeconds);
    }

    public String formatOffsetLocalizedGMT(int offset) {
        return formatOffsetLocalizedGMT(offset, false);
    }

    public String formatOffsetShortLocalizedGMT(int offset) {
        return formatOffsetLocalizedGMT(offset, true);
    }

    public final String format(Style style, TimeZone tz, long date) {
        return format(style, tz, date, null);
    }

    public String format(Style style, TimeZone tz, long date, Output<TimeType> timeType) {
        String result = null;
        if (timeType != null) {
            timeType.value = TimeType.UNKNOWN;
        }
        boolean noOffsetFormatFallback = false;
        switch (-getandroid-icu-text-TimeZoneFormat$StyleSwitchesValues()[style.ordinal()]) {
            case 1:
                result = formatExemplarLocation(tz);
                noOffsetFormatFallback = true;
                break;
            case 2:
                result = getTimeZoneGenericNames().getGenericLocationName(ZoneMeta.getCanonicalCLDRID(tz));
                break;
            case 3:
                result = getTimeZoneGenericNames().getDisplayName(tz, GenericNameType.LONG, date);
                break;
            case 4:
                result = getTimeZoneGenericNames().getDisplayName(tz, GenericNameType.SHORT, date);
                break;
            case 17:
                result = formatSpecific(tz, NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT, date, timeType);
                break;
            case 18:
                result = formatSpecific(tz, NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT, date, timeType);
                break;
            case 19:
                result = tz.getID();
                noOffsetFormatFallback = true;
                break;
            case 20:
                result = ZoneMeta.getShortID(tz);
                if (result == null) {
                    result = UNKNOWN_SHORT_ZONE_ID;
                }
                noOffsetFormatFallback = true;
                break;
        }
        if (result == null && (noOffsetFormatFallback ^ 1) != 0) {
            int[] offsets = new int[]{0, 0};
            tz.getOffset(date, false, offsets);
            int offset = offsets[0] + offsets[1];
            switch (-getandroid-icu-text-TimeZoneFormat$StyleSwitchesValues()[style.ordinal()]) {
                case 2:
                case 3:
                case 15:
                case 17:
                    result = formatOffsetLocalizedGMT(offset);
                    break;
                case 4:
                case 16:
                case 18:
                    result = formatOffsetShortLocalizedGMT(offset);
                    break;
                case 5:
                    result = formatOffsetISO8601Basic(offset, true, false, true);
                    break;
                case 6:
                    result = formatOffsetISO8601Basic(offset, true, false, false);
                    break;
                case 7:
                    result = formatOffsetISO8601Basic(offset, false, false, true);
                    break;
                case 8:
                    result = formatOffsetISO8601Basic(offset, false, false, false);
                    break;
                case 9:
                    result = formatOffsetISO8601Basic(offset, false, true, true);
                    break;
                case 10:
                    result = formatOffsetISO8601Basic(offset, true, true, true);
                    break;
                case 11:
                    result = formatOffsetISO8601Extended(offset, true, false, true);
                    break;
                case 12:
                    result = formatOffsetISO8601Extended(offset, true, false, false);
                    break;
                case 13:
                    result = formatOffsetISO8601Extended(offset, false, false, true);
                    break;
                case 14:
                    result = formatOffsetISO8601Extended(offset, false, false, false);
                    break;
                default:
                    if (!-assertionsDisabled) {
                        throw new AssertionError();
                    }
                    break;
            }
            if (timeType != null) {
                timeType.value = offsets[1] != 0 ? TimeType.DAYLIGHT : TimeType.STANDARD;
            }
        }
        if (-assertionsDisabled || result != null) {
            return result;
        }
        throw new AssertionError();
    }

    public final int parseOffsetISO8601(String text, ParsePosition pos) {
        return parseOffsetISO8601(text, pos, false, null);
    }

    public int parseOffsetLocalizedGMT(String text, ParsePosition pos) {
        return parseOffsetLocalizedGMT(text, pos, false, null);
    }

    public int parseOffsetShortLocalizedGMT(String text, ParsePosition pos) {
        return parseOffsetLocalizedGMT(text, pos, true, null);
    }

    public TimeZone parse(Style style, String text, ParsePosition pos, EnumSet<ParseOption> options, Output<TimeType> timeType) {
        Output<Boolean> hasDigitOffset;
        int offset;
        boolean parseTZDBAbbrev;
        String id;
        Collection<MatchInfo> specificMatches;
        MatchInfo specificMatch;
        Collection<MatchInfo> tzdbNameMatches;
        MatchInfo tzdbNameMatch;
        if (timeType == null) {
            Output<TimeType> output = new Output(TimeType.UNKNOWN);
        } else {
            timeType.value = TimeType.UNKNOWN;
        }
        int startIdx = pos.getIndex();
        int maxPos = text.length();
        boolean fallbackLocalizedGMT = style == Style.SPECIFIC_LONG || style == Style.GENERIC_LONG || style == Style.GENERIC_LOCATION;
        boolean fallbackShortLocalizedGMT = style == Style.SPECIFIC_SHORT || style == Style.GENERIC_SHORT;
        int evaluated = 0;
        ParsePosition parsePosition = new ParsePosition(startIdx);
        int parsedOffset = Integer.MAX_VALUE;
        int parsedPos = -1;
        if (fallbackLocalizedGMT || fallbackShortLocalizedGMT) {
            hasDigitOffset = new Output(Boolean.valueOf(false));
            offset = parseOffsetLocalizedGMT(text, parsePosition, fallbackShortLocalizedGMT, hasDigitOffset);
            if (parsePosition.getErrorIndex() == -1) {
                if (parsePosition.getIndex() == maxPos || ((Boolean) hasDigitOffset.value).booleanValue()) {
                    pos.setIndex(parsePosition.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                parsedOffset = offset;
                parsedPos = parsePosition.getIndex();
            }
            evaluated = (Style.LOCALIZED_GMT.flag | Style.LOCALIZED_GMT_SHORT.flag) | 0;
        }
        if (options == null) {
            parseTZDBAbbrev = getDefaultParseOptions().contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        } else {
            parseTZDBAbbrev = options.contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        }
        switch (-getandroid-icu-text-TimeZoneFormat$StyleSwitchesValues()[style.ordinal()]) {
            case 1:
                parsePosition.setIndex(startIdx);
                parsePosition.setErrorIndex(-1);
                id = parseExemplarLocation(text, parsePosition);
                if (parsePosition.getErrorIndex() == -1) {
                    pos.setIndex(parsePosition.getIndex());
                    return TimeZone.getTimeZone(id);
                }
                break;
            case 2:
            case 3:
            case 4:
                EnumSet genericNameTypes = null;
                switch (-getandroid-icu-text-TimeZoneFormat$StyleSwitchesValues()[style.ordinal()]) {
                    case 2:
                        genericNameTypes = EnumSet.of(GenericNameType.LOCATION);
                        break;
                    case 3:
                        genericNameTypes = EnumSet.of(GenericNameType.LONG, GenericNameType.LOCATION);
                        break;
                    case 4:
                        genericNameTypes = EnumSet.of(GenericNameType.SHORT, GenericNameType.LOCATION);
                        break;
                    default:
                        if (!-assertionsDisabled) {
                            throw new AssertionError();
                        }
                        break;
                }
                GenericMatchInfo bestGeneric = getTimeZoneGenericNames().findBestMatch(text, startIdx, genericNameTypes);
                if (bestGeneric != null && bestGeneric.matchLength() + startIdx > parsedPos) {
                    timeType.value = bestGeneric.timeType();
                    pos.setIndex(bestGeneric.matchLength() + startIdx);
                    return TimeZone.getTimeZone(bestGeneric.tzID());
                }
            case 5:
            case 6:
            case 10:
            case 11:
            case 12:
                parsePosition.setIndex(startIdx);
                parsePosition.setErrorIndex(-1);
                offset = parseOffsetISO8601(text, parsePosition);
                if (parsePosition.getErrorIndex() == -1) {
                    pos.setIndex(parsePosition.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                break;
            case 7:
            case 8:
            case 9:
            case 13:
            case 14:
                parsePosition.setIndex(startIdx);
                parsePosition.setErrorIndex(-1);
                hasDigitOffset = new Output(Boolean.valueOf(false));
                offset = parseOffsetISO8601(text, parsePosition, false, hasDigitOffset);
                if (parsePosition.getErrorIndex() == -1 && ((Boolean) hasDigitOffset.value).booleanValue()) {
                    pos.setIndex(parsePosition.getIndex());
                    return getTimeZoneForOffset(offset);
                }
            case 15:
                parsePosition.setIndex(startIdx);
                parsePosition.setErrorIndex(-1);
                offset = parseOffsetLocalizedGMT(text, parsePosition);
                if (parsePosition.getErrorIndex() != -1) {
                    evaluated |= Style.LOCALIZED_GMT_SHORT.flag;
                    break;
                }
                pos.setIndex(parsePosition.getIndex());
                return getTimeZoneForOffset(offset);
            case 16:
                parsePosition.setIndex(startIdx);
                parsePosition.setErrorIndex(-1);
                offset = parseOffsetShortLocalizedGMT(text, parsePosition);
                if (parsePosition.getErrorIndex() != -1) {
                    evaluated |= Style.LOCALIZED_GMT.flag;
                    break;
                }
                pos.setIndex(parsePosition.getIndex());
                return getTimeZoneForOffset(offset);
            case 17:
            case 18:
                EnumSet<NameType> nameTypes;
                if (style == Style.SPECIFIC_LONG) {
                    nameTypes = EnumSet.of(NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT);
                } else if (-assertionsDisabled || style == Style.SPECIFIC_SHORT) {
                    nameTypes = EnumSet.of(NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT);
                } else {
                    throw new AssertionError();
                }
                specificMatches = this._tznames.find(text, startIdx, nameTypes);
                if (specificMatches != null) {
                    specificMatch = null;
                    for (MatchInfo match : specificMatches) {
                        if (match.matchLength() + startIdx > parsedPos) {
                            specificMatch = match;
                            parsedPos = startIdx + match.matchLength();
                        }
                    }
                    if (specificMatch != null) {
                        timeType.value = getTimeType(specificMatch.nameType());
                        pos.setIndex(parsedPos);
                        return TimeZone.getTimeZone(getTimeZoneID(specificMatch.tzID(), specificMatch.mzID()));
                    }
                }
                if (parseTZDBAbbrev && style == Style.SPECIFIC_SHORT) {
                    if (!-assertionsDisabled && !nameTypes.contains(NameType.SHORT_STANDARD)) {
                        throw new AssertionError();
                    } else if (-assertionsDisabled || nameTypes.contains(NameType.SHORT_DAYLIGHT)) {
                        tzdbNameMatches = getTZDBTimeZoneNames().find(text, startIdx, nameTypes);
                        if (tzdbNameMatches != null) {
                            tzdbNameMatch = null;
                            for (MatchInfo match2 : tzdbNameMatches) {
                                if (match2.matchLength() + startIdx > parsedPos) {
                                    tzdbNameMatch = match2;
                                    parsedPos = startIdx + match2.matchLength();
                                }
                            }
                            if (tzdbNameMatch != null) {
                                timeType.value = getTimeType(tzdbNameMatch.nameType());
                                pos.setIndex(parsedPos);
                                return TimeZone.getTimeZone(getTimeZoneID(tzdbNameMatch.tzID(), tzdbNameMatch.mzID()));
                            }
                        }
                    } else {
                        throw new AssertionError();
                    }
                }
                break;
            case 19:
                parsePosition.setIndex(startIdx);
                parsePosition.setErrorIndex(-1);
                id = parseZoneID(text, parsePosition);
                if (parsePosition.getErrorIndex() == -1) {
                    pos.setIndex(parsePosition.getIndex());
                    return TimeZone.getTimeZone(id);
                }
                break;
            case 20:
                parsePosition.setIndex(startIdx);
                parsePosition.setErrorIndex(-1);
                id = parseShortZoneID(text, parsePosition);
                if (parsePosition.getErrorIndex() == -1) {
                    pos.setIndex(parsePosition.getIndex());
                    return TimeZone.getTimeZone(id);
                }
                break;
        }
        evaluated |= style.flag;
        if (parsedPos <= startIdx) {
            String parsedID = null;
            TimeType parsedTimeType = TimeType.UNKNOWN;
            if (!-assertionsDisabled && parsedPos >= 0) {
                throw new AssertionError();
            } else if (-assertionsDisabled || parsedOffset == Integer.MAX_VALUE) {
                boolean parseAllStyles;
                if (parsedPos < maxPos && ((evaluated & 128) == 0 || (evaluated & 256) == 0)) {
                    parsePosition.setIndex(startIdx);
                    parsePosition.setErrorIndex(-1);
                    hasDigitOffset = new Output(Boolean.valueOf(false));
                    offset = parseOffsetISO8601(text, parsePosition, false, hasDigitOffset);
                    if (parsePosition.getErrorIndex() == -1) {
                        if (parsePosition.getIndex() == maxPos || ((Boolean) hasDigitOffset.value).booleanValue()) {
                            pos.setIndex(parsePosition.getIndex());
                            return getTimeZoneForOffset(offset);
                        }
                        if (parsedPos < parsePosition.getIndex()) {
                            parsedOffset = offset;
                            parsedID = null;
                            parsedTimeType = TimeType.UNKNOWN;
                            parsedPos = parsePosition.getIndex();
                            if (!(-assertionsDisabled || parsedPos == startIdx + 1)) {
                                throw new AssertionError();
                            }
                        }
                    }
                }
                if (parsedPos < maxPos && (Style.LOCALIZED_GMT.flag & evaluated) == 0) {
                    parsePosition.setIndex(startIdx);
                    parsePosition.setErrorIndex(-1);
                    hasDigitOffset = new Output(Boolean.valueOf(false));
                    offset = parseOffsetLocalizedGMT(text, parsePosition, false, hasDigitOffset);
                    if (parsePosition.getErrorIndex() == -1) {
                        if (parsePosition.getIndex() == maxPos || ((Boolean) hasDigitOffset.value).booleanValue()) {
                            pos.setIndex(parsePosition.getIndex());
                            return getTimeZoneForOffset(offset);
                        }
                        if (parsedPos < parsePosition.getIndex()) {
                            parsedOffset = offset;
                            parsedID = null;
                            parsedTimeType = TimeType.UNKNOWN;
                            parsedPos = parsePosition.getIndex();
                        }
                    }
                }
                if (parsedPos < maxPos && (Style.LOCALIZED_GMT_SHORT.flag & evaluated) == 0) {
                    parsePosition.setIndex(startIdx);
                    parsePosition.setErrorIndex(-1);
                    hasDigitOffset = new Output(Boolean.valueOf(false));
                    offset = parseOffsetLocalizedGMT(text, parsePosition, true, hasDigitOffset);
                    if (parsePosition.getErrorIndex() == -1) {
                        if (parsePosition.getIndex() == maxPos || ((Boolean) hasDigitOffset.value).booleanValue()) {
                            pos.setIndex(parsePosition.getIndex());
                            return getTimeZoneForOffset(offset);
                        }
                        if (parsedPos < parsePosition.getIndex()) {
                            parsedOffset = offset;
                            parsedID = null;
                            parsedTimeType = TimeType.UNKNOWN;
                            parsedPos = parsePosition.getIndex();
                        }
                    }
                }
                if (options == null) {
                    parseAllStyles = getDefaultParseOptions().contains(ParseOption.ALL_STYLES);
                } else {
                    parseAllStyles = options.contains(ParseOption.ALL_STYLES);
                }
                if (parseAllStyles) {
                    int matchPos;
                    if (parsedPos < maxPos) {
                        specificMatches = this._tznames.find(text, startIdx, ALL_SIMPLE_NAME_TYPES);
                        specificMatch = null;
                        matchPos = -1;
                        if (specificMatches != null) {
                            for (MatchInfo match22 : specificMatches) {
                                if (match22.matchLength() + startIdx > matchPos) {
                                    specificMatch = match22;
                                    matchPos = startIdx + match22.matchLength();
                                }
                            }
                        }
                        if (parsedPos < matchPos) {
                            parsedPos = matchPos;
                            parsedID = getTimeZoneID(specificMatch.tzID(), specificMatch.mzID());
                            parsedTimeType = getTimeType(specificMatch.nameType());
                            parsedOffset = Integer.MAX_VALUE;
                        }
                    }
                    if (parseTZDBAbbrev && parsedPos < maxPos && (Style.SPECIFIC_SHORT.flag & evaluated) == 0) {
                        tzdbNameMatches = getTZDBTimeZoneNames().find(text, startIdx, ALL_SIMPLE_NAME_TYPES);
                        tzdbNameMatch = null;
                        matchPos = -1;
                        if (tzdbNameMatches != null) {
                            for (MatchInfo match222 : tzdbNameMatches) {
                                if (match222.matchLength() + startIdx > matchPos) {
                                    tzdbNameMatch = match222;
                                    matchPos = startIdx + match222.matchLength();
                                }
                            }
                            if (parsedPos < matchPos) {
                                parsedPos = matchPos;
                                parsedID = getTimeZoneID(tzdbNameMatch.tzID(), tzdbNameMatch.mzID());
                                parsedTimeType = getTimeType(tzdbNameMatch.nameType());
                                parsedOffset = Integer.MAX_VALUE;
                            }
                        }
                    }
                    if (parsedPos < maxPos) {
                        GenericMatchInfo genericMatch = getTimeZoneGenericNames().findBestMatch(text, startIdx, ALL_GENERIC_NAME_TYPES);
                        if (genericMatch != null && parsedPos < genericMatch.matchLength() + startIdx) {
                            parsedPos = startIdx + genericMatch.matchLength();
                            parsedID = genericMatch.tzID();
                            parsedTimeType = genericMatch.timeType();
                            parsedOffset = Integer.MAX_VALUE;
                        }
                    }
                    if (parsedPos < maxPos && (Style.ZONE_ID.flag & evaluated) == 0) {
                        parsePosition.setIndex(startIdx);
                        parsePosition.setErrorIndex(-1);
                        id = parseZoneID(text, parsePosition);
                        if (parsePosition.getErrorIndex() == -1 && parsedPos < parsePosition.getIndex()) {
                            parsedPos = parsePosition.getIndex();
                            parsedID = id;
                            parsedTimeType = TimeType.UNKNOWN;
                            parsedOffset = Integer.MAX_VALUE;
                        }
                    }
                    if (parsedPos < maxPos && (Style.ZONE_ID_SHORT.flag & evaluated) == 0) {
                        parsePosition.setIndex(startIdx);
                        parsePosition.setErrorIndex(-1);
                        id = parseShortZoneID(text, parsePosition);
                        if (parsePosition.getErrorIndex() == -1 && parsedPos < parsePosition.getIndex()) {
                            parsedPos = parsePosition.getIndex();
                            parsedID = id;
                            parsedTimeType = TimeType.UNKNOWN;
                            parsedOffset = Integer.MAX_VALUE;
                        }
                    }
                }
                if (parsedPos > startIdx) {
                    TimeZone parsedTZ;
                    if (parsedID != null) {
                        parsedTZ = TimeZone.getTimeZone(parsedID);
                    } else if (-assertionsDisabled || parsedOffset != Integer.MAX_VALUE) {
                        parsedTZ = getTimeZoneForOffset(parsedOffset);
                    } else {
                        throw new AssertionError();
                    }
                    timeType.value = parsedTimeType;
                    pos.setIndex(parsedPos);
                    return parsedTZ;
                }
                pos.setErrorIndex(startIdx);
                return null;
            } else {
                throw new AssertionError();
            }
        } else if (-assertionsDisabled || parsedOffset != Integer.MAX_VALUE) {
            pos.setIndex(parsedPos);
            return getTimeZoneForOffset(parsedOffset);
        } else {
            throw new AssertionError();
        }
    }

    public TimeZone parse(Style style, String text, ParsePosition pos, Output<TimeType> timeType) {
        return parse(style, text, pos, null, timeType);
    }

    public final TimeZone parse(String text, ParsePosition pos) {
        return parse(Style.GENERIC_LOCATION, text, pos, EnumSet.of(ParseOption.ALL_STYLES), null);
    }

    public final TimeZone parse(String text) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        TimeZone tz = parse(text, pos);
        if (pos.getErrorIndex() >= 0) {
            throw new ParseException("Unparseable time zone: \"" + text + "\"", 0);
        } else if (-assertionsDisabled || tz != null) {
            return tz;
        } else {
            throw new AssertionError();
        }
    }

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        TimeZone tz;
        long date = System.currentTimeMillis();
        if (obj instanceof TimeZone) {
            tz = (TimeZone) obj;
        } else if (obj instanceof Calendar) {
            tz = ((Calendar) obj).getTimeZone();
            date = ((Calendar) obj).getTimeInMillis();
        } else {
            throw new IllegalArgumentException("Cannot format given Object (" + obj.getClass().getName() + ") as a time zone");
        }
        if (-assertionsDisabled || tz != null) {
            String result = formatOffsetLocalizedGMT(tz.getOffset(date));
            toAppendTo.append(result);
            if (pos.getFieldAttribute() == Field.TIME_ZONE || pos.getField() == 17) {
                pos.setBeginIndex(0);
                pos.setEndIndex(result.length());
            }
            return toAppendTo;
        }
        throw new AssertionError();
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        AttributedString as = new AttributedString(format(obj, new StringBuffer(), new FieldPosition(0)).toString());
        as.addAttribute(Field.TIME_ZONE, Field.TIME_ZONE);
        return as.getIterator();
    }

    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    private String formatOffsetLocalizedGMT(int offset, boolean isShort) {
        if (offset == 0) {
            return this._gmtZeroFormat;
        }
        StringBuilder buf = new StringBuilder();
        boolean positive = true;
        if (offset < 0) {
            offset = -offset;
            positive = false;
        }
        int offsetH = offset / 3600000;
        offset %= 3600000;
        int offsetM = offset / 60000;
        offset %= 60000;
        int offsetS = offset / 1000;
        if (offsetH > 23 || offsetM > 59 || offsetS > 59) {
            throw new IllegalArgumentException("Offset out of range :" + offset);
        }
        Object[] offsetPatternItems;
        if (positive) {
            if (offsetS != 0) {
                offsetPatternItems = this._gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HMS.ordinal()];
            } else if (offsetM == 0 && (isShort ^ 1) == 0) {
                offsetPatternItems = this._gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_H.ordinal()];
            } else {
                offsetPatternItems = this._gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HM.ordinal()];
            }
        } else if (offsetS != 0) {
            offsetPatternItems = this._gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()];
        } else if (offsetM == 0 && (isShort ^ 1) == 0) {
            offsetPatternItems = this._gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_H.ordinal()];
        } else {
            offsetPatternItems = this._gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HM.ordinal()];
        }
        buf.append(this._gmtPatternPrefix);
        for (GMTOffsetField item : offsetPatternItems) {
            if (item instanceof String) {
                buf.append((String) item);
            } else if (item instanceof GMTOffsetField) {
                switch (item.getType()) {
                    case 'H':
                        appendOffsetDigits(buf, offsetH, isShort ? 1 : 2);
                        break;
                    case 'm':
                        appendOffsetDigits(buf, offsetM, 2);
                        break;
                    case 's':
                        appendOffsetDigits(buf, offsetS, 2);
                        break;
                    default:
                        break;
                }
            }
        }
        buf.append(this._gmtPatternSuffix);
        return buf.toString();
    }

    private String formatOffsetISO8601(int offset, boolean isBasic, boolean useUtcIndicator, boolean isShort, boolean ignoreSeconds) {
        int absOffset = offset < 0 ? -offset : offset;
        if (useUtcIndicator && (absOffset < 1000 || (ignoreSeconds && absOffset < 60000))) {
            return ISO8601_UTC;
        }
        OffsetFields minFields = isShort ? OffsetFields.H : OffsetFields.HM;
        OffsetFields maxFields = ignoreSeconds ? OffsetFields.HM : OffsetFields.HMS;
        Object sep = isBasic ? null : Character.valueOf(DEFAULT_GMT_OFFSET_SEP);
        if (absOffset >= 86400000) {
            throw new IllegalArgumentException("Offset out of range :" + offset);
        }
        fields = new int[3];
        absOffset %= 3600000;
        fields[1] = absOffset / 60000;
        fields[2] = (absOffset % 60000) / 1000;
        if (!-assertionsDisabled && (fields[0] < 0 || fields[0] > 23)) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && (fields[1] < 0 || fields[1] > 59)) {
            throw new AssertionError();
        } else if (-assertionsDisabled || (fields[2] >= 0 && fields[2] <= 59)) {
            int idx;
            int lastIdx = maxFields.ordinal();
            while (lastIdx > minFields.ordinal() && fields[lastIdx] == 0) {
                lastIdx--;
            }
            StringBuilder buf = new StringBuilder();
            char sign = '+';
            if (offset < 0) {
                for (idx = 0; idx <= lastIdx; idx++) {
                    if (fields[idx] != 0) {
                        sign = '-';
                        break;
                    }
                }
            }
            buf.append(sign);
            idx = 0;
            while (idx <= lastIdx) {
                if (!(sep == null || idx == 0)) {
                    buf.append(sep);
                }
                if (fields[idx] < 10) {
                    buf.append('0');
                }
                buf.append(fields[idx]);
                idx++;
            }
            return buf.toString();
        } else {
            throw new AssertionError();
        }
    }

    private String formatSpecific(TimeZone tz, NameType stdType, NameType dstType, long date, Output<TimeType> timeType) {
        if (!-assertionsDisabled && stdType != NameType.LONG_STANDARD && stdType != NameType.SHORT_STANDARD) {
            throw new AssertionError();
        } else if (-assertionsDisabled || dstType == NameType.LONG_DAYLIGHT || dstType == NameType.SHORT_DAYLIGHT) {
            String name;
            boolean isDaylight = tz.inDaylightTime(new Date(date));
            if (isDaylight) {
                name = getTimeZoneNames().getDisplayName(ZoneMeta.getCanonicalCLDRID(tz), dstType, date);
            } else {
                name = getTimeZoneNames().getDisplayName(ZoneMeta.getCanonicalCLDRID(tz), stdType, date);
            }
            if (!(name == null || timeType == null)) {
                timeType.value = isDaylight ? TimeType.DAYLIGHT : TimeType.STANDARD;
            }
            return name;
        } else {
            throw new AssertionError();
        }
    }

    private String formatExemplarLocation(TimeZone tz) {
        String location = getTimeZoneNames().getExemplarLocationName(ZoneMeta.getCanonicalCLDRID(tz));
        if (location != null) {
            return location;
        }
        location = getTimeZoneNames().getExemplarLocationName("Etc/Unknown");
        if (location == null) {
            return UNKNOWN_LOCATION;
        }
        return location;
    }

    private String getTimeZoneID(String tzID, String mzID) {
        String id = tzID;
        if (tzID == null) {
            if (-assertionsDisabled || mzID != null) {
                id = this._tznames.getReferenceZoneID(mzID, getTargetRegion());
                if (id == null) {
                    throw new IllegalArgumentException("Invalid mzID: " + mzID);
                }
            }
            throw new AssertionError();
        }
        return id;
    }

    private synchronized String getTargetRegion() {
        if (this._region == null) {
            this._region = this._locale.getCountry();
            if (this._region.length() == 0) {
                this._region = ULocale.addLikelySubtags(this._locale).getCountry();
                if (this._region.length() == 0) {
                    this._region = "001";
                }
            }
        }
        return this._region;
    }

    private TimeType getTimeType(NameType nameType) {
        switch (-getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues()[nameType.ordinal()]) {
            case 1:
            case 3:
                return TimeType.DAYLIGHT;
            case 2:
            case 4:
                return TimeType.STANDARD;
            default:
                return TimeType.UNKNOWN;
        }
    }

    private void initGMTPattern(String gmtPattern) {
        int idx = gmtPattern.indexOf("{0}");
        if (idx < 0) {
            throw new IllegalArgumentException("Bad localized GMT pattern: " + gmtPattern);
        }
        this._gmtPattern = gmtPattern;
        this._gmtPatternPrefix = unquote(gmtPattern.substring(0, idx));
        this._gmtPatternSuffix = unquote(gmtPattern.substring(idx + 3));
    }

    private static String unquote(String s) {
        if (s.indexOf(39) < 0) {
            return s;
        }
        boolean isPrevQuote = false;
        int inQuote = 0;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == PatternTokenizer.SINGLE_QUOTE) {
                if (isPrevQuote) {
                    buf.append(c);
                    isPrevQuote = false;
                } else {
                    isPrevQuote = true;
                }
                inQuote ^= 1;
            } else {
                isPrevQuote = false;
                buf.append(c);
            }
        }
        return buf.toString();
    }

    private void initGMTOffsetPatterns(String[] gmtOffsetPatterns) {
        int size = GMTOffsetPatternType.values().length;
        if (gmtOffsetPatterns.length < size) {
            throw new IllegalArgumentException("Insufficient number of elements in gmtOffsetPatterns");
        }
        Object[][] gmtOffsetPatternItems = new Object[size][];
        for (GMTOffsetPatternType t : GMTOffsetPatternType.values()) {
            int idx = t.ordinal();
            gmtOffsetPatternItems[idx] = parseOffsetPattern(gmtOffsetPatterns[idx], t.required());
        }
        this._gmtOffsetPatterns = new String[size];
        System.arraycopy(gmtOffsetPatterns, 0, this._gmtOffsetPatterns, 0, size);
        this._gmtOffsetPatternItems = gmtOffsetPatternItems;
        checkAbuttingHoursAndMinutes();
    }

    private void checkAbuttingHoursAndMinutes() {
        this._abuttingOffsetHoursAndMinutes = false;
        for (Object[] items : this._gmtOffsetPatternItems) {
            boolean afterH = false;
            for (GMTOffsetField item : r7[r6]) {
                if (item instanceof GMTOffsetField) {
                    GMTOffsetField fld = item;
                    if (afterH) {
                        this._abuttingOffsetHoursAndMinutes = true;
                    } else if (fld.getType() == 'H') {
                        afterH = true;
                    }
                } else if (afterH) {
                    break;
                }
            }
        }
    }

    private static Object[] parseOffsetPattern(String pattern, String letters) {
        boolean isPrevQuote = false;
        int inQuote = 0;
        StringBuilder text = new StringBuilder();
        char itemType = 0;
        int itemLength = 1;
        boolean invalidPattern = false;
        List<Object> items = new ArrayList();
        BitSet checkBits = new BitSet(letters.length());
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (ch == PatternTokenizer.SINGLE_QUOTE) {
                if (!isPrevQuote) {
                    isPrevQuote = true;
                    if (itemType != 0) {
                        if (!GMTOffsetField.isValid(itemType, itemLength)) {
                            invalidPattern = true;
                            break;
                        }
                        items.add(new GMTOffsetField(itemType, itemLength));
                        itemType = 0;
                    }
                } else {
                    text.append(PatternTokenizer.SINGLE_QUOTE);
                    isPrevQuote = false;
                }
                inQuote ^= 1;
            } else {
                isPrevQuote = false;
                if (inQuote != 0) {
                    text.append(ch);
                } else {
                    int patFieldIdx = letters.indexOf(ch);
                    if (patFieldIdx < 0) {
                        if (itemType != 0) {
                            if (!GMTOffsetField.isValid(itemType, itemLength)) {
                                invalidPattern = true;
                                break;
                            }
                            items.add(new GMTOffsetField(itemType, itemLength));
                            itemType = 0;
                        }
                        text.append(ch);
                    } else if (ch == itemType) {
                        itemLength++;
                    } else {
                        if (itemType != 0) {
                            if (!GMTOffsetField.isValid(itemType, itemLength)) {
                                invalidPattern = true;
                                break;
                            }
                            items.add(new GMTOffsetField(itemType, itemLength));
                        } else if (text.length() > 0) {
                            items.add(text.toString());
                            text.setLength(0);
                        }
                        itemType = ch;
                        itemLength = 1;
                        checkBits.set(patFieldIdx);
                    }
                }
            }
        }
        if (!invalidPattern) {
            if (itemType == 0) {
                if (text.length() > 0) {
                    items.add(text.toString());
                    text.setLength(0);
                }
            } else if (GMTOffsetField.isValid(itemType, itemLength)) {
                items.add(new GMTOffsetField(itemType, itemLength));
            } else {
                invalidPattern = true;
            }
        }
        if (!invalidPattern && checkBits.cardinality() == letters.length()) {
            return items.toArray(new Object[items.size()]);
        }
        throw new IllegalStateException("Bad localized GMT offset pattern: " + pattern);
    }

    private static String expandOffsetPattern(String offsetHM) {
        int idx_mm = offsetHM.indexOf("mm");
        if (idx_mm < 0) {
            throw new RuntimeException("Bad time zone hour pattern data");
        }
        String sep = ":";
        int idx_H = offsetHM.substring(0, idx_mm).lastIndexOf(DateFormat.HOUR24);
        if (idx_H >= 0) {
            sep = offsetHM.substring(idx_H + 1, idx_mm);
        }
        return offsetHM.substring(0, idx_mm + 2) + sep + "ss" + offsetHM.substring(idx_mm + 2);
    }

    private static String truncateOffsetPattern(String offsetHM) {
        int idx_mm = offsetHM.indexOf("mm");
        if (idx_mm < 0) {
            throw new RuntimeException("Bad time zone hour pattern data");
        }
        int idx_HH = offsetHM.substring(0, idx_mm).lastIndexOf("HH");
        if (idx_HH >= 0) {
            return offsetHM.substring(0, idx_HH + 2);
        }
        int idx_H = offsetHM.substring(0, idx_mm).lastIndexOf(DateFormat.HOUR24);
        if (idx_H >= 0) {
            return offsetHM.substring(0, idx_H + 1);
        }
        throw new RuntimeException("Bad time zone hour pattern data");
    }

    private void appendOffsetDigits(StringBuilder buf, int n, int minDigits) {
        if (-assertionsDisabled || (n >= 0 && n < 60)) {
            int numDigits = n >= 10 ? 2 : 1;
            for (int i = 0; i < minDigits - numDigits; i++) {
                buf.append(this._gmtOffsetDigits[0]);
            }
            if (numDigits == 2) {
                buf.append(this._gmtOffsetDigits[n / 10]);
            }
            buf.append(this._gmtOffsetDigits[n % 10]);
            return;
        }
        throw new AssertionError();
    }

    private TimeZone getTimeZoneForOffset(int offset) {
        if (offset == 0) {
            return TimeZone.getTimeZone(TZID_GMT);
        }
        return ZoneMeta.getCustomTimeZone(offset);
    }

    private int parseOffsetLocalizedGMT(String text, ParsePosition pos, boolean isShort, Output<Boolean> hasDigitOffset) {
        int start = pos.getIndex();
        int[] parsedLength = new int[]{0};
        if (hasDigitOffset != null) {
            hasDigitOffset.value = Boolean.valueOf(false);
        }
        int offset = parseOffsetLocalizedGMTPattern(text, start, isShort, parsedLength);
        if (parsedLength[0] > 0) {
            if (hasDigitOffset != null) {
                hasDigitOffset.value = Boolean.valueOf(true);
            }
            pos.setIndex(parsedLength[0] + start);
            return offset;
        }
        offset = parseOffsetDefaultLocalizedGMT(text, start, parsedLength);
        if (parsedLength[0] > 0) {
            if (hasDigitOffset != null) {
                hasDigitOffset.value = Boolean.valueOf(true);
            }
            pos.setIndex(parsedLength[0] + start);
            return offset;
        }
        if (text.regionMatches(true, start, this._gmtZeroFormat, 0, this._gmtZeroFormat.length())) {
            pos.setIndex(this._gmtZeroFormat.length() + start);
            return 0;
        }
        for (String defGMTZero : ALT_GMT_STRINGS) {
            if (text.regionMatches(true, start, defGMTZero, 0, defGMTZero.length())) {
                pos.setIndex(defGMTZero.length() + start);
                return 0;
            }
        }
        pos.setErrorIndex(start);
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x001c  */
    /* JADX WARNING: Missing block: B:14:0x0041, code:
            if ((r11.regionMatches(true, r2, r10._gmtPatternSuffix, 0, r5) ^ 1) == 0) goto L_0x0043;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseOffsetLocalizedGMTPattern(String text, int start, boolean isShort, int[] parsedLen) {
        int idx;
        int i;
        int idx2 = start;
        int offset = 0;
        boolean parsed = false;
        int len = this._gmtPatternPrefix.length();
        if (len > 0) {
            if ((text.regionMatches(true, start, this._gmtPatternPrefix, 0, len) ^ 1) != 0) {
                idx = idx2;
                if (parsed) {
                    i = 0;
                } else {
                    i = idx - start;
                }
                parsedLen[0] = i;
                return offset;
            }
        }
        idx = start + len;
        int[] offsetLen = new int[1];
        offset = parseOffsetFields(text, idx, false, offsetLen);
        if (offsetLen[0] != 0) {
            idx += offsetLen[0];
            len = this._gmtPatternSuffix.length();
            if (len > 0) {
            }
            idx += len;
            parsed = true;
        }
        if (parsed) {
        }
        parsedLen[0] = i;
        return offset;
    }

    private int parseOffsetFields(String text, int start, boolean isShort, int[] parsedLen) {
        int i;
        GMTOffsetPatternType gmtPatType;
        Object[] items;
        int outLen = 0;
        int sign = 1;
        if (parsedLen != null && parsedLen.length >= 1) {
            parsedLen[0] = 0;
        }
        int offsetS = 0;
        int offsetM = 0;
        int offsetH = 0;
        int[] fields = new int[]{0, 0, 0};
        GMTOffsetPatternType[] gMTOffsetPatternTypeArr = PARSE_GMT_OFFSET_TYPES;
        int i2 = 0;
        int length = gMTOffsetPatternTypeArr.length;
        while (true) {
            i = i2;
            if (i >= length) {
                break;
            }
            gmtPatType = gMTOffsetPatternTypeArr[i];
            items = this._gmtOffsetPatternItems[gmtPatType.ordinal()];
            if (-assertionsDisabled || items != null) {
                outLen = parseOffsetFieldsWithPattern(text, start, items, false, fields);
                if (outLen > 0) {
                    sign = gmtPatType.isPositive() ? 1 : -1;
                    offsetH = fields[0];
                    offsetM = fields[1];
                    offsetS = fields[2];
                } else {
                    i2 = i + 1;
                }
            } else {
                throw new AssertionError();
            }
        }
        if (outLen > 0 && this._abuttingOffsetHoursAndMinutes) {
            int tmpLen = 0;
            int tmpSign = 1;
            gMTOffsetPatternTypeArr = PARSE_GMT_OFFSET_TYPES;
            i2 = 0;
            length = gMTOffsetPatternTypeArr.length;
            while (true) {
                i = i2;
                if (i >= length) {
                    break;
                }
                gmtPatType = gMTOffsetPatternTypeArr[i];
                items = this._gmtOffsetPatternItems[gmtPatType.ordinal()];
                if (-assertionsDisabled || items != null) {
                    tmpLen = parseOffsetFieldsWithPattern(text, start, items, true, fields);
                    if (tmpLen > 0) {
                        tmpSign = gmtPatType.isPositive() ? 1 : -1;
                    } else {
                        i2 = i + 1;
                    }
                } else {
                    throw new AssertionError();
                }
            }
            if (tmpLen > outLen) {
                outLen = tmpLen;
                sign = tmpSign;
                offsetH = fields[0];
                offsetM = fields[1];
                offsetS = fields[2];
            }
        }
        if (parsedLen != null && parsedLen.length >= 1) {
            parsedLen[0] = outLen;
        }
        if (outLen > 0) {
            return (((((offsetH * 60) + offsetM) * 60) + offsetS) * 1000) * sign;
        }
        return 0;
    }

    private int parseOffsetFieldsWithPattern(String text, int start, Object[] patternItems, boolean forceSingleHourDigit, int[] fields) {
        if (-assertionsDisabled || (fields != null && fields.length >= 3)) {
            fields[2] = 0;
            fields[1] = 0;
            fields[0] = 0;
            boolean failed = false;
            int offsetS = 0;
            int offsetM = 0;
            int offsetH = 0;
            int idx = start;
            int[] tmpParsedLen = new int[]{0};
            int i = 0;
            while (i < patternItems.length) {
                if (patternItems[i] instanceof String) {
                    String patStr = patternItems[i];
                    int len = patStr.length();
                    if (!text.regionMatches(true, idx, patStr, 0, len)) {
                        failed = true;
                        break;
                    }
                    idx += len;
                } else if (-assertionsDisabled || (patternItems[i] instanceof GMTOffsetField)) {
                    char fieldType = patternItems[i].getType();
                    if (fieldType == 'H') {
                        offsetH = parseOffsetFieldWithLocalizedDigits(text, idx, 1, forceSingleHourDigit ? 1 : 2, 0, 23, tmpParsedLen);
                    } else if (fieldType == 'm') {
                        offsetM = parseOffsetFieldWithLocalizedDigits(text, idx, 2, 2, 0, 59, tmpParsedLen);
                    } else if (fieldType == 's') {
                        offsetS = parseOffsetFieldWithLocalizedDigits(text, idx, 2, 2, 0, 59, tmpParsedLen);
                    }
                    if (tmpParsedLen[0] == 0) {
                        failed = true;
                        break;
                    }
                    idx += tmpParsedLen[0];
                } else {
                    throw new AssertionError();
                }
                i++;
            }
            if (failed) {
                return 0;
            }
            fields[0] = offsetH;
            fields[1] = offsetM;
            fields[2] = offsetS;
            return idx - start;
        }
        throw new AssertionError();
    }

    private int parseOffsetDefaultLocalizedGMT(String text, int start, int[] parsedLen) {
        int idx = start;
        int offset = 0;
        int parsed = 0;
        int gmtLen = 0;
        String[] strArr = ALT_GMT_STRINGS;
        int i = 0;
        int length = strArr.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                break;
            }
            String gmt = strArr[i2];
            int len = gmt.length();
            if (text.regionMatches(true, start, gmt, 0, len)) {
                gmtLen = len;
                break;
            }
            i = i2 + 1;
        }
        if (gmtLen != 0) {
            idx = start + gmtLen;
            if (idx + 1 < text.length()) {
                int sign;
                char c = text.charAt(idx);
                if (c == '+') {
                    sign = 1;
                } else if (c == '-') {
                    sign = -1;
                }
                idx++;
                int[] lenWithSep = new int[]{0};
                int offsetWithSep = parseDefaultOffsetFields(text, idx, DEFAULT_GMT_OFFSET_SEP, lenWithSep);
                if (lenWithSep[0] == text.length() - idx) {
                    offset = offsetWithSep * sign;
                    idx += lenWithSep[0];
                } else {
                    int[] lenAbut = new int[]{0};
                    int offsetAbut = parseAbuttingOffsetFields(text, idx, lenAbut);
                    if (lenWithSep[0] > lenAbut[0]) {
                        offset = offsetWithSep * sign;
                        idx += lenWithSep[0];
                    } else {
                        offset = offsetAbut * sign;
                        idx += lenAbut[0];
                    }
                }
                parsed = idx - start;
            }
        }
        parsedLen[0] = parsed;
        return offset;
    }

    private int parseDefaultOffsetFields(String text, int start, char separator, int[] parsedLen) {
        int max = text.length();
        int idx = start;
        int[] len = new int[]{0};
        int min = 0;
        int sec = 0;
        int hour = parseOffsetFieldWithLocalizedDigits(text, start, 1, 2, 0, 23, len);
        if (len[0] != 0) {
            idx = start + len[0];
            if (idx + 1 < max && text.charAt(idx) == separator) {
                min = parseOffsetFieldWithLocalizedDigits(text, idx + 1, 2, 2, 0, 59, len);
                if (len[0] != 0) {
                    idx += len[0] + 1;
                    if (idx + 1 < max && text.charAt(idx) == separator) {
                        sec = parseOffsetFieldWithLocalizedDigits(text, idx + 1, 2, 2, 0, 59, len);
                        if (len[0] != 0) {
                            idx += len[0] + 1;
                        }
                    }
                }
            }
        }
        if (idx == start) {
            parsedLen[0] = 0;
            return 0;
        }
        parsedLen[0] = idx - start;
        return ((3600000 * hour) + (60000 * min)) + (sec * 1000);
    }

    private int parseAbuttingOffsetFields(String text, int start, int[] parsedLen) {
        int[] digits = new int[6];
        int[] parsed = new int[6];
        int idx = start;
        int[] len = new int[]{0};
        int numDigits = 0;
        for (int i = 0; i < 6; i++) {
            digits[i] = parseSingleLocalizedDigit(text, idx, len);
            if (digits[i] < 0) {
                break;
            }
            idx += len[0];
            parsed[i] = idx - start;
            numDigits++;
        }
        if (numDigits == 0) {
            parsedLen[0] = 0;
            return 0;
        }
        int offset = 0;
        while (numDigits > 0) {
            int hour = 0;
            int min = 0;
            int sec = 0;
            if (-assertionsDisabled || (numDigits > 0 && numDigits <= 6)) {
                switch (numDigits) {
                    case 1:
                        hour = digits[0];
                        break;
                    case 2:
                        hour = (digits[0] * 10) + digits[1];
                        break;
                    case 3:
                        hour = digits[0];
                        min = (digits[1] * 10) + digits[2];
                        break;
                    case 4:
                        hour = (digits[0] * 10) + digits[1];
                        min = (digits[2] * 10) + digits[3];
                        break;
                    case 5:
                        hour = digits[0];
                        min = (digits[1] * 10) + digits[2];
                        sec = (digits[3] * 10) + digits[4];
                        break;
                    case 6:
                        hour = (digits[0] * 10) + digits[1];
                        min = (digits[2] * 10) + digits[3];
                        sec = (digits[4] * 10) + digits[5];
                        break;
                }
                if (hour > 23 || min > 59 || sec > 59) {
                    numDigits--;
                } else {
                    offset = ((3600000 * hour) + (60000 * min)) + (sec * 1000);
                    parsedLen[0] = parsed[numDigits - 1];
                    return offset;
                }
            }
            throw new AssertionError();
        }
        return offset;
    }

    private int parseOffsetFieldWithLocalizedDigits(String text, int start, int minDigits, int maxDigits, int minVal, int maxVal, int[] parsedLen) {
        parsedLen[0] = 0;
        int decVal = 0;
        int numDigits = 0;
        int idx = start;
        int[] digitLen = new int[]{0};
        while (idx < text.length() && numDigits < maxDigits) {
            int digit = parseSingleLocalizedDigit(text, idx, digitLen);
            if (digit >= 0) {
                int tmpVal = (decVal * 10) + digit;
                if (tmpVal > maxVal) {
                    break;
                }
                decVal = tmpVal;
                numDigits++;
                idx += digitLen[0];
            } else {
                break;
            }
        }
        if (numDigits < minDigits || decVal < minVal) {
            return -1;
        }
        parsedLen[0] = idx - start;
        return decVal;
    }

    private int parseSingleLocalizedDigit(String text, int start, int[] len) {
        int digit = -1;
        len[0] = 0;
        if (start < text.length()) {
            int cp = Character.codePointAt(text, start);
            for (int i = 0; i < this._gmtOffsetDigits.length; i++) {
                if (cp == this._gmtOffsetDigits[i].codePointAt(0)) {
                    digit = i;
                    break;
                }
            }
            if (digit < 0) {
                digit = UCharacter.digit(cp);
            }
            if (digit >= 0) {
                len[0] = Character.charCount(cp);
            }
        }
        return digit;
    }

    private static String[] toCodePoints(String str) {
        int len = str.codePointCount(0, str.length());
        String[] codePoints = new String[len];
        int offset = 0;
        for (int i = 0; i < len; i++) {
            int codeLen = Character.charCount(str.codePointAt(offset));
            codePoints[i] = str.substring(offset, offset + codeLen);
            offset += codeLen;
        }
        return codePoints;
    }

    private static int parseOffsetISO8601(String text, ParsePosition pos, boolean extendedOnly, Output<Boolean> hasDigitOffset) {
        if (hasDigitOffset != null) {
            hasDigitOffset.value = Boolean.valueOf(false);
        }
        int start = pos.getIndex();
        if (start >= text.length()) {
            pos.setErrorIndex(start);
            return 0;
        }
        char firstChar = text.charAt(start);
        if (Character.toUpperCase(firstChar) == ISO8601_UTC.charAt(0)) {
            pos.setIndex(start + 1);
            return 0;
        }
        int sign;
        if (firstChar == '+') {
            sign = 1;
        } else if (firstChar == '-') {
            sign = -1;
        } else {
            pos.setErrorIndex(start);
            return 0;
        }
        ParsePosition posOffset = new ParsePosition(start + 1);
        int offset = parseAsciiOffsetFields(text, posOffset, DEFAULT_GMT_OFFSET_SEP, OffsetFields.H, OffsetFields.HMS);
        if (posOffset.getErrorIndex() == -1 && (extendedOnly ^ 1) != 0 && posOffset.getIndex() - start <= 3) {
            ParsePosition posBasic = new ParsePosition(start + 1);
            int tmpOffset = parseAbuttingAsciiOffsetFields(text, posBasic, OffsetFields.H, OffsetFields.HMS, false);
            if (posBasic.getErrorIndex() == -1 && posBasic.getIndex() > posOffset.getIndex()) {
                offset = tmpOffset;
                posOffset.setIndex(posBasic.getIndex());
            }
        }
        if (posOffset.getErrorIndex() != -1) {
            pos.setErrorIndex(start);
            return 0;
        }
        pos.setIndex(posOffset.getIndex());
        if (hasDigitOffset != null) {
            hasDigitOffset.value = Boolean.valueOf(true);
        }
        return sign * offset;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0063  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int parseAbuttingAsciiOffsetFields(String text, ParsePosition pos, OffsetFields minFields, OffsetFields maxFields, boolean fixedHourWidth) {
        int start = pos.getIndex();
        int minDigits = ((minFields.ordinal() + 1) * 2) - (fixedHourWidth ? 0 : 1);
        int[] digits = new int[((maxFields.ordinal() + 1) * 2)];
        int numDigits = 0;
        int idx = start;
        while (numDigits < digits.length && idx < text.length()) {
            int digit = ASCII_DIGITS.indexOf(text.charAt(idx));
            if (digit < 0) {
                break;
            }
            digits[numDigits] = digit;
            numDigits++;
            idx++;
        }
        if (fixedHourWidth && (numDigits & 1) != 0) {
            numDigits--;
        }
        if (numDigits < minDigits) {
            pos.setErrorIndex(start);
            return 0;
        }
        int hour = 0;
        int min = 0;
        int sec = 0;
        boolean bParsed = false;
        while (numDigits >= minDigits) {
            switch (numDigits) {
                case 1:
                    hour = digits[0];
                    break;
                case 2:
                    hour = (digits[0] * 10) + digits[1];
                    break;
                case 3:
                    hour = digits[0];
                    min = (digits[1] * 10) + digits[2];
                    break;
                case 4:
                    hour = (digits[0] * 10) + digits[1];
                    min = (digits[2] * 10) + digits[3];
                    break;
                case 5:
                    hour = digits[0];
                    min = (digits[1] * 10) + digits[2];
                    sec = (digits[3] * 10) + digits[4];
                    break;
                case 6:
                    hour = (digits[0] * 10) + digits[1];
                    min = (digits[2] * 10) + digits[3];
                    sec = (digits[4] * 10) + digits[5];
                    break;
            }
            if (hour > 23 || min > 59 || sec > 59) {
                numDigits -= fixedHourWidth ? 2 : 1;
                sec = 0;
                min = 0;
                hour = 0;
            } else {
                bParsed = true;
                if (bParsed) {
                    pos.setErrorIndex(start);
                    return 0;
                }
                pos.setIndex(start + numDigits);
                return ((((hour * 60) + min) * 60) + sec) * 1000;
            }
        }
        if (bParsed) {
        }
    }

    private static int parseAsciiOffsetFields(String text, ParsePosition pos, char sep, OffsetFields minFields, OffsetFields maxFields) {
        int start = pos.getIndex();
        int[] fieldVal = new int[]{0, 0, 0};
        int[] fieldLen = new int[]{0, -1, -1};
        int fieldIdx = 0;
        for (int idx = start; idx < text.length() && fieldIdx <= maxFields.ordinal(); idx++) {
            char c = text.charAt(idx);
            if (c != sep) {
                if (fieldLen[fieldIdx] == -1) {
                    break;
                }
                int digit = ASCII_DIGITS.indexOf(c);
                if (digit < 0) {
                    break;
                }
                fieldVal[fieldIdx] = (fieldVal[fieldIdx] * 10) + digit;
                fieldLen[fieldIdx] = fieldLen[fieldIdx] + 1;
                if (fieldLen[fieldIdx] >= 2) {
                    fieldIdx++;
                }
            } else if (fieldIdx != 0) {
                if (fieldLen[fieldIdx] != -1) {
                    break;
                }
                fieldLen[fieldIdx] = 0;
            } else if (fieldLen[0] == 0) {
                break;
            } else {
                fieldIdx++;
            }
        }
        int offset = 0;
        int parsedLen = 0;
        OffsetFields parsedFields = null;
        if (fieldLen[0] != 0) {
            if (fieldVal[0] > 23) {
                offset = (fieldVal[0] / 10) * 3600000;
                parsedFields = OffsetFields.H;
                parsedLen = 1;
            } else {
                offset = fieldVal[0] * 3600000;
                parsedLen = fieldLen[0];
                parsedFields = OffsetFields.H;
                if (fieldLen[1] == 2 && fieldVal[1] <= 59) {
                    offset += fieldVal[1] * 60000;
                    parsedLen += fieldLen[1] + 1;
                    parsedFields = OffsetFields.HM;
                    if (fieldLen[2] == 2 && fieldVal[2] <= 59) {
                        offset += fieldVal[2] * 1000;
                        parsedLen += fieldLen[2] + 1;
                        parsedFields = OffsetFields.HMS;
                    }
                }
            }
        }
        if (parsedFields == null || parsedFields.ordinal() < minFields.ordinal()) {
            pos.setErrorIndex(start);
            return 0;
        }
        pos.setIndex(start + parsedLen);
        return offset;
    }

    private static String parseZoneID(String text, ParsePosition pos) {
        if (ZONE_ID_TRIE == null) {
            synchronized (TimeZoneFormat.class) {
                if (ZONE_ID_TRIE == null) {
                    TextTrieMap<String> trie = new TextTrieMap(true);
                    for (String id : TimeZone.getAvailableIDs()) {
                        trie.put(id, id);
                    }
                    ZONE_ID_TRIE = trie;
                }
            }
        }
        int[] matchLen = new int[]{0};
        Iterator<String> itr = ZONE_ID_TRIE.get(text, pos.getIndex(), matchLen);
        if (itr != null) {
            String resolvedID = (String) itr.next();
            pos.setIndex(pos.getIndex() + matchLen[0]);
            return resolvedID;
        }
        pos.setErrorIndex(pos.getIndex());
        return null;
    }

    private static String parseShortZoneID(String text, ParsePosition pos) {
        if (SHORT_ZONE_ID_TRIE == null) {
            synchronized (TimeZoneFormat.class) {
                if (SHORT_ZONE_ID_TRIE == null) {
                    TextTrieMap<String> trie = new TextTrieMap(true);
                    for (String id : TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null)) {
                        String shortID = ZoneMeta.getShortID(id);
                        if (shortID != null) {
                            trie.put(shortID, id);
                        }
                    }
                    trie.put(UNKNOWN_SHORT_ZONE_ID, "Etc/Unknown");
                    SHORT_ZONE_ID_TRIE = trie;
                }
            }
        }
        int[] matchLen = new int[]{0};
        Iterator<String> itr = SHORT_ZONE_ID_TRIE.get(text, pos.getIndex(), matchLen);
        if (itr != null) {
            String resolvedID = (String) itr.next();
            pos.setIndex(pos.getIndex() + matchLen[0]);
            return resolvedID;
        }
        pos.setErrorIndex(pos.getIndex());
        return null;
    }

    private String parseExemplarLocation(String text, ParsePosition pos) {
        int startIdx = pos.getIndex();
        int parsedPos = -1;
        String tzID = null;
        Collection<MatchInfo> exemplarMatches = this._tznames.find(text, startIdx, EnumSet.of(NameType.EXEMPLAR_LOCATION));
        if (exemplarMatches != null) {
            MatchInfo exemplarMatch = null;
            for (MatchInfo match : exemplarMatches) {
                if (match.matchLength() + startIdx > parsedPos) {
                    exemplarMatch = match;
                    parsedPos = startIdx + match.matchLength();
                }
            }
            if (exemplarMatch != null) {
                tzID = getTimeZoneID(exemplarMatch.tzID(), exemplarMatch.mzID());
                pos.setIndex(parsedPos);
            }
        }
        if (tzID == null) {
            pos.setErrorIndex(startIdx);
        }
        return tzID;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        PutField fields = oos.putFields();
        fields.put("_locale", this._locale);
        fields.put("_tznames", this._tznames);
        fields.put("_gmtPattern", this._gmtPattern);
        fields.put("_gmtOffsetPatterns", this._gmtOffsetPatterns);
        fields.put("_gmtOffsetDigits", this._gmtOffsetDigits);
        fields.put("_gmtZeroFormat", this._gmtZeroFormat);
        fields.put("_parseAllStyles", this._parseAllStyles);
        oos.writeFields();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        GetField fields = ois.readFields();
        this._locale = (ULocale) fields.get("_locale", null);
        if (this._locale == null) {
            throw new InvalidObjectException("Missing field: locale");
        }
        this._tznames = (TimeZoneNames) fields.get("_tznames", null);
        if (this._tznames == null) {
            throw new InvalidObjectException("Missing field: tznames");
        }
        this._gmtPattern = (String) fields.get("_gmtPattern", null);
        if (this._gmtPattern == null) {
            throw new InvalidObjectException("Missing field: gmtPattern");
        }
        String[] tmpGmtOffsetPatterns = (String[]) fields.get("_gmtOffsetPatterns", null);
        if (tmpGmtOffsetPatterns == null) {
            throw new InvalidObjectException("Missing field: gmtOffsetPatterns");
        } else if (tmpGmtOffsetPatterns.length < 4) {
            throw new InvalidObjectException("Incompatible field: gmtOffsetPatterns");
        } else {
            this._gmtOffsetPatterns = new String[6];
            if (tmpGmtOffsetPatterns.length == 4) {
                for (int i = 0; i < 4; i++) {
                    this._gmtOffsetPatterns[i] = tmpGmtOffsetPatterns[i];
                }
                this._gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_H.ordinal()] = truncateOffsetPattern(this._gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()]);
                this._gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_H.ordinal()] = truncateOffsetPattern(this._gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()]);
            } else {
                this._gmtOffsetPatterns = tmpGmtOffsetPatterns;
            }
            this._gmtOffsetDigits = (String[]) fields.get("_gmtOffsetDigits", null);
            if (this._gmtOffsetDigits == null) {
                throw new InvalidObjectException("Missing field: gmtOffsetDigits");
            } else if (this._gmtOffsetDigits.length != 10) {
                throw new InvalidObjectException("Incompatible field: gmtOffsetDigits");
            } else {
                this._gmtZeroFormat = (String) fields.get("_gmtZeroFormat", null);
                if (this._gmtZeroFormat == null) {
                    throw new InvalidObjectException("Missing field: gmtZeroFormat");
                }
                this._parseAllStyles = fields.get("_parseAllStyles", false);
                if (fields.defaulted("_parseAllStyles")) {
                    throw new InvalidObjectException("Missing field: parseAllStyles");
                }
                if (this._tznames instanceof TimeZoneNamesImpl) {
                    this._tznames = TimeZoneNames.getInstance(this._locale);
                    this._gnames = null;
                } else {
                    this._gnames = new TimeZoneGenericNames(this._locale, this._tznames);
                }
                initGMTPattern(this._gmtPattern);
                initGMTOffsetPatterns(this._gmtOffsetPatterns);
            }
        }
    }

    public boolean isFrozen() {
        return this._frozen;
    }

    public TimeZoneFormat freeze() {
        this._frozen = true;
        return this;
    }

    public TimeZoneFormat cloneAsThawed() {
        TimeZoneFormat copy = (TimeZoneFormat) super.clone();
        copy._frozen = false;
        return copy;
    }
}
