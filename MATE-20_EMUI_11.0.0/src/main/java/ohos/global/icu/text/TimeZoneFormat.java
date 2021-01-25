package ohos.global.icu.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.Locale;
import java.util.MissingResourceException;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.SoftCache;
import ohos.global.icu.impl.TZDBTimeZoneNames;
import ohos.global.icu.impl.TextTrieMap;
import ohos.global.icu.impl.TimeZoneGenericNames;
import ohos.global.icu.impl.TimeZoneNamesImpl;
import ohos.global.icu.impl.ZoneMeta;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.text.TimeZoneNames;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.Freezable;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.TimeZone;
import ohos.global.icu.util.ULocale;

public class TimeZoneFormat extends UFormat implements Freezable<TimeZoneFormat>, Serializable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final EnumSet<TimeZoneGenericNames.GenericNameType> ALL_GENERIC_NAME_TYPES = EnumSet.of(TimeZoneGenericNames.GenericNameType.LOCATION, TimeZoneGenericNames.GenericNameType.LONG, TimeZoneGenericNames.GenericNameType.SHORT);
    private static final EnumSet<TimeZoneNames.NameType> ALL_SIMPLE_NAME_TYPES = EnumSet.of(TimeZoneNames.NameType.LONG_STANDARD, TimeZoneNames.NameType.LONG_DAYLIGHT, TimeZoneNames.NameType.SHORT_STANDARD, TimeZoneNames.NameType.SHORT_DAYLIGHT, TimeZoneNames.NameType.EXEMPLAR_LOCATION);
    private static final String[] ALT_GMT_STRINGS = {DEFAULT_GMT_ZERO, "UTC", "UT"};
    private static final String ASCII_DIGITS = "0123456789";
    private static final String[] DEFAULT_GMT_DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
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
    private static final GMTOffsetPatternType[] PARSE_GMT_OFFSET_TYPES = {GMTOffsetPatternType.POSITIVE_HMS, GMTOffsetPatternType.NEGATIVE_HMS, GMTOffsetPatternType.POSITIVE_HM, GMTOffsetPatternType.NEGATIVE_HM, GMTOffsetPatternType.POSITIVE_H, GMTOffsetPatternType.NEGATIVE_H};
    private static volatile TextTrieMap<String> SHORT_ZONE_ID_TRIE = null;
    private static final String TZID_GMT = "Etc/GMT";
    private static final String UNKNOWN_LOCATION = "Unknown";
    private static final int UNKNOWN_OFFSET = Integer.MAX_VALUE;
    private static final String UNKNOWN_SHORT_ZONE_ID = "unk";
    private static final String UNKNOWN_ZONE_ID = "Etc/Unknown";
    private static volatile TextTrieMap<String> ZONE_ID_TRIE = null;
    private static TimeZoneFormatCache _tzfCache = new TimeZoneFormatCache(null);
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("_locale", ULocale.class), new ObjectStreamField("_tznames", TimeZoneNames.class), new ObjectStreamField("_gmtPattern", String.class), new ObjectStreamField("_gmtOffsetPatterns", String[].class), new ObjectStreamField("_gmtOffsetDigits", String[].class), new ObjectStreamField("_gmtZeroFormat", String.class), new ObjectStreamField("_parseAllStyles", Boolean.TYPE)};
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

    /* access modifiers changed from: private */
    public enum OffsetFields {
        H,
        HM,
        HMS
    }

    public enum ParseOption {
        ALL_STYLES,
        TZ_DATABASE_ABBREVIATIONS
    }

    public enum TimeType {
        UNKNOWN,
        STANDARD,
        DAYLIGHT
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

        private Style(int i) {
            this.flag = i;
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

        private GMTOffsetPatternType(String str, String str2, boolean z) {
            this._defaultPattern = str;
            this._required = str2;
            this._isPositive = z;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String defaultPattern() {
            return this._defaultPattern;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String required() {
            return this._required;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isPositive() {
            return this._isPositive;
        }
    }

    protected TimeZoneFormat(ULocale uLocale) {
        String str;
        String str2;
        this._locale = uLocale;
        this._tznames = TimeZoneNames.getInstance(uLocale);
        String str3 = null;
        try {
            ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(ICUData.ICU_ZONE_BASE_NAME, uLocale);
            try {
                str2 = bundleInstance.getStringWithFallback("zoneStrings/gmtFormat");
            } catch (MissingResourceException unused) {
                str2 = null;
            }
            try {
                str3 = bundleInstance.getStringWithFallback("zoneStrings/hourFormat");
            } catch (MissingResourceException unused2) {
            }
            try {
                this._gmtZeroFormat = bundleInstance.getStringWithFallback("zoneStrings/gmtZeroFormat");
            } catch (MissingResourceException unused3) {
            }
            str = str3;
            str3 = str2;
        } catch (MissingResourceException unused4) {
            str = null;
        }
        initGMTPattern(str3 == null ? DEFAULT_GMT_PATTERN : str3);
        String[] strArr = new String[GMTOffsetPatternType.values().length];
        if (str != null) {
            String[] split = str.split(";", 2);
            strArr[GMTOffsetPatternType.POSITIVE_H.ordinal()] = truncateOffsetPattern(split[0]);
            strArr[GMTOffsetPatternType.POSITIVE_HM.ordinal()] = split[0];
            strArr[GMTOffsetPatternType.POSITIVE_HMS.ordinal()] = expandOffsetPattern(split[0]);
            strArr[GMTOffsetPatternType.NEGATIVE_H.ordinal()] = truncateOffsetPattern(split[1]);
            strArr[GMTOffsetPatternType.NEGATIVE_HM.ordinal()] = split[1];
            strArr[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()] = expandOffsetPattern(split[1]);
        } else {
            GMTOffsetPatternType[] values = GMTOffsetPatternType.values();
            for (GMTOffsetPatternType gMTOffsetPatternType : values) {
                strArr[gMTOffsetPatternType.ordinal()] = gMTOffsetPatternType.defaultPattern();
            }
        }
        initGMTOffsetPatterns(strArr);
        this._gmtOffsetDigits = DEFAULT_GMT_DIGITS;
        NumberingSystem instance = NumberingSystem.getInstance(uLocale);
        if (!instance.isAlgorithmic()) {
            this._gmtOffsetDigits = toCodePoints(instance.getDescription());
        }
    }

    public static TimeZoneFormat getInstance(ULocale uLocale) {
        if (uLocale != null) {
            return (TimeZoneFormat) _tzfCache.getInstance(uLocale, uLocale);
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

    public TimeZoneFormat setTimeZoneNames(TimeZoneNames timeZoneNames) {
        if (!isFrozen()) {
            this._tznames = timeZoneNames;
            this._gnames = new TimeZoneGenericNames(this._locale, this._tznames);
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify frozen object");
    }

    public String getGMTPattern() {
        return this._gmtPattern;
    }

    public TimeZoneFormat setGMTPattern(String str) {
        if (!isFrozen()) {
            initGMTPattern(str);
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify frozen object");
    }

    public String getGMTOffsetPattern(GMTOffsetPatternType gMTOffsetPatternType) {
        return this._gmtOffsetPatterns[gMTOffsetPatternType.ordinal()];
    }

    public TimeZoneFormat setGMTOffsetPattern(GMTOffsetPatternType gMTOffsetPatternType, String str) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        } else if (str != null) {
            Object[] parseOffsetPattern = parseOffsetPattern(str, gMTOffsetPatternType.required());
            this._gmtOffsetPatterns[gMTOffsetPatternType.ordinal()] = str;
            this._gmtOffsetPatternItems[gMTOffsetPatternType.ordinal()] = parseOffsetPattern;
            checkAbuttingHoursAndMinutes();
            return this;
        } else {
            throw new NullPointerException("Null GMT offset pattern");
        }
    }

    public String getGMTOffsetDigits() {
        StringBuilder sb = new StringBuilder(this._gmtOffsetDigits.length);
        for (String str : this._gmtOffsetDigits) {
            sb.append(str);
        }
        return sb.toString();
    }

    public TimeZoneFormat setGMTOffsetDigits(String str) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        } else if (str != null) {
            String[] codePoints = toCodePoints(str);
            if (codePoints.length == 10) {
                this._gmtOffsetDigits = codePoints;
                return this;
            }
            throw new IllegalArgumentException("Length of digits must be 10");
        } else {
            throw new NullPointerException("Null GMT offset digits");
        }
    }

    public String getGMTZeroFormat() {
        return this._gmtZeroFormat;
    }

    public TimeZoneFormat setGMTZeroFormat(String str) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        } else if (str == null) {
            throw new NullPointerException("Null GMT zero format");
        } else if (str.length() != 0) {
            this._gmtZeroFormat = str;
            return this;
        } else {
            throw new IllegalArgumentException("Empty GMT zero format");
        }
    }

    public TimeZoneFormat setDefaultParseOptions(EnumSet<ParseOption> enumSet) {
        this._parseAllStyles = enumSet.contains(ParseOption.ALL_STYLES);
        this._parseTZDBNames = enumSet.contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);
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

    public final String formatOffsetISO8601Basic(int i, boolean z, boolean z2, boolean z3) {
        return formatOffsetISO8601(i, true, z, z2, z3);
    }

    public final String formatOffsetISO8601Extended(int i, boolean z, boolean z2, boolean z3) {
        return formatOffsetISO8601(i, false, z, z2, z3);
    }

    public String formatOffsetLocalizedGMT(int i) {
        return formatOffsetLocalizedGMT(i, false);
    }

    public String formatOffsetShortLocalizedGMT(int i) {
        return formatOffsetLocalizedGMT(i, true);
    }

    public final String format(Style style, TimeZone timeZone, long j) {
        return format(style, timeZone, j, null);
    }

    public String format(Style style, TimeZone timeZone, long j, Output<TimeType> output) {
        boolean z;
        String str;
        if (output != null) {
            output.value = TimeType.UNKNOWN;
        }
        switch (style) {
            case GENERIC_LOCATION:
                str = getTimeZoneGenericNames().getGenericLocationName(ZoneMeta.getCanonicalCLDRID(timeZone));
                z = false;
                break;
            case GENERIC_LONG:
                str = getTimeZoneGenericNames().getDisplayName(timeZone, TimeZoneGenericNames.GenericNameType.LONG, j);
                z = false;
                break;
            case GENERIC_SHORT:
                str = getTimeZoneGenericNames().getDisplayName(timeZone, TimeZoneGenericNames.GenericNameType.SHORT, j);
                z = false;
                break;
            case SPECIFIC_LONG:
                str = formatSpecific(timeZone, TimeZoneNames.NameType.LONG_STANDARD, TimeZoneNames.NameType.LONG_DAYLIGHT, j, output);
                z = false;
                break;
            case SPECIFIC_SHORT:
                str = formatSpecific(timeZone, TimeZoneNames.NameType.SHORT_STANDARD, TimeZoneNames.NameType.SHORT_DAYLIGHT, j, output);
                z = false;
                break;
            case ZONE_ID:
                str = timeZone.getID();
                z = true;
                break;
            case ZONE_ID_SHORT:
                str = ZoneMeta.getShortID(timeZone);
                if (str == null) {
                    str = UNKNOWN_SHORT_ZONE_ID;
                }
                z = true;
                break;
            case EXEMPLAR_LOCATION:
                str = formatExemplarLocation(timeZone);
                z = true;
                break;
            default:
                str = null;
                z = false;
                break;
        }
        if (str == null && !z) {
            int[] iArr = {0, 0};
            timeZone.getOffset(j, false, iArr);
            int i = iArr[0] + iArr[1];
            switch (style) {
                case GENERIC_LOCATION:
                case GENERIC_LONG:
                case SPECIFIC_LONG:
                case LOCALIZED_GMT:
                    str = formatOffsetLocalizedGMT(i);
                    break;
                case GENERIC_SHORT:
                case SPECIFIC_SHORT:
                case LOCALIZED_GMT_SHORT:
                    str = formatOffsetShortLocalizedGMT(i);
                    break;
                case ISO_BASIC_SHORT:
                    str = formatOffsetISO8601Basic(i, true, true, true);
                    break;
                case ISO_BASIC_LOCAL_SHORT:
                    str = formatOffsetISO8601Basic(i, false, true, true);
                    break;
                case ISO_BASIC_FIXED:
                    str = formatOffsetISO8601Basic(i, true, false, true);
                    break;
                case ISO_BASIC_LOCAL_FIXED:
                    str = formatOffsetISO8601Basic(i, false, false, true);
                    break;
                case ISO_BASIC_FULL:
                    str = formatOffsetISO8601Basic(i, true, false, false);
                    break;
                case ISO_BASIC_LOCAL_FULL:
                    str = formatOffsetISO8601Basic(i, false, false, false);
                    break;
                case ISO_EXTENDED_FIXED:
                    str = formatOffsetISO8601Extended(i, true, false, true);
                    break;
                case ISO_EXTENDED_LOCAL_FIXED:
                    str = formatOffsetISO8601Extended(i, false, false, true);
                    break;
                case ISO_EXTENDED_FULL:
                    str = formatOffsetISO8601Extended(i, true, false, false);
                    break;
                case ISO_EXTENDED_LOCAL_FULL:
                    str = formatOffsetISO8601Extended(i, false, false, false);
                    break;
            }
            if (output != null) {
                output.value = iArr[1] != 0 ? TimeType.DAYLIGHT : TimeType.STANDARD;
            }
        }
        return str;
    }

    public final int parseOffsetISO8601(String str, ParsePosition parsePosition) {
        return parseOffsetISO8601(str, parsePosition, false, null);
    }

    public int parseOffsetLocalizedGMT(String str, ParsePosition parsePosition) {
        return parseOffsetLocalizedGMT(str, parsePosition, false, null);
    }

    public int parseOffsetShortLocalizedGMT(String str, ParsePosition parsePosition) {
        return parseOffsetLocalizedGMT(str, parsePosition, true, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:171:0x03b1  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x03bc  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x03c4  */
    /* JADX WARNING: Removed duplicated region for block: B:230:0x04f6  */
    /* JADX WARNING: Removed duplicated region for block: B:232:0x04fa  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x050b  */
    public TimeZone parse(Style style, String str, ParsePosition parsePosition, EnumSet<ParseOption> enumSet, Output<TimeType> output) {
        int i;
        int i2;
        int i3;
        boolean z;
        int i4;
        TimeType timeType;
        boolean z2;
        String str2;
        TimeZone timeZone;
        int i5;
        String str3;
        String str4;
        TimeZoneGenericNames.GenericMatchInfo findBestMatch;
        String str5;
        Collection<TimeZoneNames.MatchInfo> find;
        TimeZoneNames.MatchInfo matchInfo;
        EnumSet<TimeZoneGenericNames.GenericNameType> enumSet2;
        EnumSet<TimeZoneNames.NameType> enumSet3;
        Collection<TimeZoneNames.MatchInfo> find2;
        int i6;
        Output<TimeType> output2 = output;
        if (output2 == null) {
            output2 = new Output<>(TimeType.UNKNOWN);
        } else {
            output2.value = TimeType.UNKNOWN;
        }
        int index = parsePosition.getIndex();
        int length = str.length();
        boolean z3 = style == Style.SPECIFIC_LONG || style == Style.GENERIC_LONG || style == Style.GENERIC_LOCATION;
        boolean z4 = style == Style.SPECIFIC_SHORT || style == Style.GENERIC_SHORT;
        ParsePosition parsePosition2 = new ParsePosition(index);
        if (z3 || z4) {
            Output<Boolean> output3 = new Output<>(false);
            i2 = parseOffsetLocalizedGMT(str, parsePosition2, z4, output3);
            if (parsePosition2.getErrorIndex() != -1) {
                i3 = -1;
                i2 = Integer.MAX_VALUE;
            } else if (parsePosition2.getIndex() == length || ((Boolean) output3.value).booleanValue()) {
                parsePosition.setIndex(parsePosition2.getIndex());
                return getTimeZoneForOffset(i2);
            } else {
                i3 = parsePosition2.getIndex();
            }
            i = Style.LOCALIZED_GMT_SHORT.flag | Style.LOCALIZED_GMT.flag | 0;
        } else {
            i = 0;
            i3 = -1;
            i2 = Integer.MAX_VALUE;
        }
        if (enumSet == null) {
            z = getDefaultParseOptions().contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        } else {
            z = enumSet.contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        }
        switch (style) {
            case GENERIC_LOCATION:
            case GENERIC_LONG:
            case GENERIC_SHORT:
                int i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[style.ordinal()];
                if (i7 == 1) {
                    enumSet2 = EnumSet.of(TimeZoneGenericNames.GenericNameType.LOCATION);
                } else if (i7 == 2) {
                    enumSet2 = EnumSet.of(TimeZoneGenericNames.GenericNameType.LONG, TimeZoneGenericNames.GenericNameType.LOCATION);
                } else if (i7 != 3) {
                    enumSet2 = null;
                } else {
                    enumSet2 = EnumSet.of(TimeZoneGenericNames.GenericNameType.SHORT, TimeZoneGenericNames.GenericNameType.LOCATION);
                }
                TimeZoneGenericNames.GenericMatchInfo findBestMatch2 = getTimeZoneGenericNames().findBestMatch(str, index, enumSet2);
                if (findBestMatch2 != null && findBestMatch2.matchLength() + index > i3) {
                    output2.value = findBestMatch2.timeType();
                    parsePosition.setIndex(index + findBestMatch2.matchLength());
                    return TimeZone.getTimeZone(findBestMatch2.tzID());
                }
            case SPECIFIC_LONG:
            case SPECIFIC_SHORT:
                if (style == Style.SPECIFIC_LONG) {
                    enumSet3 = EnumSet.of(TimeZoneNames.NameType.LONG_STANDARD, TimeZoneNames.NameType.LONG_DAYLIGHT);
                } else {
                    enumSet3 = EnumSet.of(TimeZoneNames.NameType.SHORT_STANDARD, TimeZoneNames.NameType.SHORT_DAYLIGHT);
                }
                Collection<TimeZoneNames.MatchInfo> find3 = this._tznames.find(str, index, enumSet3);
                if (find3 != null) {
                    int i8 = i3;
                    TimeZoneNames.MatchInfo matchInfo2 = null;
                    for (Iterator<TimeZoneNames.MatchInfo> it = find3.iterator(); it.hasNext(); it = it) {
                        TimeZoneNames.MatchInfo next = it.next();
                        if (index + next.matchLength() > i8) {
                            i8 = next.matchLength() + index;
                            matchInfo2 = next;
                        }
                    }
                    if (matchInfo2 != null) {
                        output2.value = getTimeType(matchInfo2.nameType());
                        parsePosition.setIndex(i8);
                        return TimeZone.getTimeZone(getTimeZoneID(matchInfo2.tzID(), matchInfo2.mzID()));
                    }
                    i3 = i8;
                }
                if (z && style == Style.SPECIFIC_SHORT && (find2 = getTZDBTimeZoneNames().find(str, index, enumSet3)) != null) {
                    int i9 = i3;
                    TimeZoneNames.MatchInfo matchInfo3 = null;
                    for (TimeZoneNames.MatchInfo matchInfo4 : find2) {
                        if (matchInfo4.matchLength() + index > i9) {
                            i9 = matchInfo4.matchLength() + index;
                            matchInfo3 = matchInfo4;
                        }
                    }
                    if (matchInfo3 == null) {
                        i3 = i9;
                        break;
                    } else {
                        output2.value = getTimeType(matchInfo3.nameType());
                        parsePosition.setIndex(i9);
                        return TimeZone.getTimeZone(getTimeZoneID(matchInfo3.tzID(), matchInfo3.mzID()));
                    }
                }
            case ZONE_ID:
                parsePosition2.setIndex(index);
                parsePosition2.setErrorIndex(-1);
                String parseZoneID = parseZoneID(str, parsePosition2);
                if (parsePosition2.getErrorIndex() == -1) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return TimeZone.getTimeZone(parseZoneID);
                }
                break;
            case ZONE_ID_SHORT:
                parsePosition2.setIndex(index);
                parsePosition2.setErrorIndex(-1);
                String parseShortZoneID = parseShortZoneID(str, parsePosition2);
                if (parsePosition2.getErrorIndex() == -1) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return TimeZone.getTimeZone(parseShortZoneID);
                }
                break;
            case EXEMPLAR_LOCATION:
                parsePosition2.setIndex(index);
                parsePosition2.setErrorIndex(-1);
                String parseExemplarLocation = parseExemplarLocation(str, parsePosition2);
                if (parsePosition2.getErrorIndex() == -1) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return TimeZone.getTimeZone(parseExemplarLocation);
                }
                break;
            case LOCALIZED_GMT:
                parsePosition2.setIndex(index);
                parsePosition2.setErrorIndex(-1);
                int parseOffsetLocalizedGMT = parseOffsetLocalizedGMT(str, parsePosition2);
                if (parsePosition2.getErrorIndex() != -1) {
                    i6 = Style.LOCALIZED_GMT_SHORT.flag;
                    i |= i6;
                    break;
                } else {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return getTimeZoneForOffset(parseOffsetLocalizedGMT);
                }
            case LOCALIZED_GMT_SHORT:
                parsePosition2.setIndex(index);
                parsePosition2.setErrorIndex(-1);
                int parseOffsetShortLocalizedGMT = parseOffsetShortLocalizedGMT(str, parsePosition2);
                if (parsePosition2.getErrorIndex() != -1) {
                    i6 = Style.LOCALIZED_GMT.flag;
                    i |= i6;
                    break;
                } else {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return getTimeZoneForOffset(parseOffsetShortLocalizedGMT);
                }
            case ISO_BASIC_SHORT:
            case ISO_BASIC_FIXED:
            case ISO_BASIC_FULL:
            case ISO_EXTENDED_FIXED:
            case ISO_EXTENDED_FULL:
                parsePosition2.setIndex(index);
                parsePosition2.setErrorIndex(-1);
                int parseOffsetISO8601 = parseOffsetISO8601(str, parsePosition2);
                if (parsePosition2.getErrorIndex() == -1) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return getTimeZoneForOffset(parseOffsetISO8601);
                }
                break;
            case ISO_BASIC_LOCAL_SHORT:
            case ISO_BASIC_LOCAL_FIXED:
            case ISO_BASIC_LOCAL_FULL:
            case ISO_EXTENDED_LOCAL_FIXED:
            case ISO_EXTENDED_LOCAL_FULL:
                parsePosition2.setIndex(index);
                parsePosition2.setErrorIndex(-1);
                Output output4 = new Output(false);
                int parseOffsetISO86012 = parseOffsetISO8601(str, parsePosition2, false, output4);
                if (parsePosition2.getErrorIndex() == -1 && ((Boolean) output4.value).booleanValue()) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return getTimeZoneForOffset(parseOffsetISO86012);
                }
        }
        int i10 = style.flag | i;
        if (i3 > index) {
            parsePosition.setIndex(i3);
            return getTimeZoneForOffset(i2);
        }
        TimeType timeType2 = TimeType.UNKNOWN;
        if (i3 < length && ((i10 & 128) == 0 || (i10 & 256) == 0)) {
            parsePosition2.setIndex(index);
            parsePosition2.setErrorIndex(-1);
            Output output5 = new Output(false);
            int parseOffsetISO86013 = parseOffsetISO8601(str, parsePosition2, false, output5);
            if (parsePosition2.getErrorIndex() == -1) {
                if (parsePosition2.getIndex() == length || ((Boolean) output5.value).booleanValue()) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return getTimeZoneForOffset(parseOffsetISO86013);
                } else if (i3 < parsePosition2.getIndex()) {
                    timeType2 = TimeType.UNKNOWN;
                    i3 = parsePosition2.getIndex();
                    i2 = parseOffsetISO86013;
                }
            }
        }
        if (i3 < length && (Style.LOCALIZED_GMT.flag & i10) == 0) {
            parsePosition2.setIndex(index);
            parsePosition2.setErrorIndex(-1);
            Output<Boolean> output6 = new Output<>(false);
            int parseOffsetLocalizedGMT2 = parseOffsetLocalizedGMT(str, parsePosition2, false, output6);
            if (parsePosition2.getErrorIndex() == -1) {
                if (parsePosition2.getIndex() == length || ((Boolean) output6.value).booleanValue()) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return getTimeZoneForOffset(parseOffsetLocalizedGMT2);
                } else if (i3 < parsePosition2.getIndex()) {
                    timeType2 = TimeType.UNKNOWN;
                    i3 = parsePosition2.getIndex();
                    i2 = parseOffsetLocalizedGMT2;
                }
            }
        }
        if (i3 < length && (Style.LOCALIZED_GMT_SHORT.flag & i10) == 0) {
            parsePosition2.setIndex(index);
            parsePosition2.setErrorIndex(-1);
            Output<Boolean> output7 = new Output<>(false);
            int parseOffsetLocalizedGMT3 = parseOffsetLocalizedGMT(str, parsePosition2, true, output7);
            if (parsePosition2.getErrorIndex() == -1) {
                if (parsePosition2.getIndex() == length || ((Boolean) output7.value).booleanValue()) {
                    parsePosition.setIndex(parsePosition2.getIndex());
                    return getTimeZoneForOffset(parseOffsetLocalizedGMT3);
                } else if (i3 < parsePosition2.getIndex()) {
                    TimeType timeType3 = TimeType.UNKNOWN;
                    i3 = parsePosition2.getIndex();
                    timeType = timeType3;
                    i4 = parseOffsetLocalizedGMT3;
                    if (enumSet != null) {
                        z2 = getDefaultParseOptions().contains(ParseOption.ALL_STYLES);
                    } else {
                        z2 = enumSet.contains(ParseOption.ALL_STYLES);
                    }
                    if (!z2) {
                        if (i3 < length) {
                            Collection<TimeZoneNames.MatchInfo> find4 = this._tznames.find(str, index, ALL_SIMPLE_NAME_TYPES);
                            if (find4 != null) {
                                matchInfo = null;
                                i5 = -1;
                                for (Iterator<TimeZoneNames.MatchInfo> it2 = find4.iterator(); it2.hasNext(); it2 = it2) {
                                    TimeZoneNames.MatchInfo next2 = it2.next();
                                    if (index + next2.matchLength() > i5) {
                                        i5 = index + next2.matchLength();
                                        matchInfo = next2;
                                    }
                                }
                            } else {
                                matchInfo = null;
                                i5 = -1;
                            }
                            if (i3 < i5) {
                                str3 = getTimeZoneID(matchInfo.tzID(), matchInfo.mzID());
                                timeType = getTimeType(matchInfo.nameType());
                                i4 = Integer.MAX_VALUE;
                                if (z || i5 >= length || (Style.SPECIFIC_SHORT.flag & i10) != 0 || (find = getTZDBTimeZoneNames().find(str, index, ALL_SIMPLE_NAME_TYPES)) == null) {
                                    str5 = str3;
                                } else {
                                    TimeZoneNames.MatchInfo matchInfo5 = null;
                                    int i11 = -1;
                                    for (TimeZoneNames.MatchInfo matchInfo6 : find) {
                                        if (index + matchInfo6.matchLength() > i11) {
                                            i11 = matchInfo6.matchLength() + index;
                                            matchInfo5 = matchInfo6;
                                        }
                                        str3 = str3;
                                    }
                                    str5 = str3;
                                    if (i5 < i11) {
                                        str4 = getTimeZoneID(matchInfo5.tzID(), matchInfo5.mzID());
                                        timeType = getTimeType(matchInfo5.nameType());
                                        i5 = i11;
                                        i4 = Integer.MAX_VALUE;
                                        if (i5 < length && (findBestMatch = getTimeZoneGenericNames().findBestMatch(str, index, ALL_GENERIC_NAME_TYPES)) != null && i5 < findBestMatch.matchLength() + index) {
                                            i5 = index + findBestMatch.matchLength();
                                            str4 = findBestMatch.tzID();
                                            timeType = findBestMatch.timeType();
                                            i4 = Integer.MAX_VALUE;
                                        }
                                        if (i5 < length && (Style.ZONE_ID.flag & i10) == 0) {
                                            parsePosition2.setIndex(index);
                                            parsePosition2.setErrorIndex(-1);
                                            String parseZoneID2 = parseZoneID(str, parsePosition2);
                                            if (parsePosition2.getErrorIndex() == -1 && i5 < parsePosition2.getIndex()) {
                                                int index2 = parsePosition2.getIndex();
                                                timeType = TimeType.UNKNOWN;
                                                i4 = Integer.MAX_VALUE;
                                                i3 = index2;
                                                str4 = parseZoneID2;
                                                if (i3 < length && (i10 & Style.ZONE_ID_SHORT.flag) == 0) {
                                                    parsePosition2.setIndex(index);
                                                    parsePosition2.setErrorIndex(-1);
                                                    str2 = parseShortZoneID(str, parsePosition2);
                                                    if (parsePosition2.getErrorIndex() == -1 && i3 < parsePosition2.getIndex()) {
                                                        i3 = parsePosition2.getIndex();
                                                        timeType = TimeType.UNKNOWN;
                                                        i4 = Integer.MAX_VALUE;
                                                    }
                                                }
                                                str2 = str4;
                                            }
                                        }
                                        i3 = i5;
                                        parsePosition2.setIndex(index);
                                        parsePosition2.setErrorIndex(-1);
                                        str2 = parseShortZoneID(str, parsePosition2);
                                        i3 = parsePosition2.getIndex();
                                        timeType = TimeType.UNKNOWN;
                                        i4 = Integer.MAX_VALUE;
                                    }
                                }
                                str4 = str5;
                                i5 = index + findBestMatch.matchLength();
                                str4 = findBestMatch.tzID();
                                timeType = findBestMatch.timeType();
                                i4 = Integer.MAX_VALUE;
                                parsePosition2.setIndex(index);
                                parsePosition2.setErrorIndex(-1);
                                String parseZoneID22 = parseZoneID(str, parsePosition2);
                                int index22 = parsePosition2.getIndex();
                                timeType = TimeType.UNKNOWN;
                                i4 = Integer.MAX_VALUE;
                                i3 = index22;
                                str4 = parseZoneID22;
                                parsePosition2.setIndex(index);
                                parsePosition2.setErrorIndex(-1);
                                str2 = parseShortZoneID(str, parsePosition2);
                                i3 = parsePosition2.getIndex();
                                timeType = TimeType.UNKNOWN;
                                i4 = Integer.MAX_VALUE;
                            }
                        }
                        i5 = i3;
                        str3 = null;
                        if (z) {
                        }
                        str5 = str3;
                        str4 = str5;
                        i5 = index + findBestMatch.matchLength();
                        str4 = findBestMatch.tzID();
                        timeType = findBestMatch.timeType();
                        i4 = Integer.MAX_VALUE;
                        parsePosition2.setIndex(index);
                        parsePosition2.setErrorIndex(-1);
                        String parseZoneID222 = parseZoneID(str, parsePosition2);
                        int index222 = parsePosition2.getIndex();
                        timeType = TimeType.UNKNOWN;
                        i4 = Integer.MAX_VALUE;
                        i3 = index222;
                        str4 = parseZoneID222;
                        parsePosition2.setIndex(index);
                        parsePosition2.setErrorIndex(-1);
                        str2 = parseShortZoneID(str, parsePosition2);
                        i3 = parsePosition2.getIndex();
                        timeType = TimeType.UNKNOWN;
                        i4 = Integer.MAX_VALUE;
                    } else {
                        str2 = null;
                    }
                    if (i3 <= index) {
                        if (str2 != null) {
                            timeZone = TimeZone.getTimeZone(str2);
                        } else {
                            timeZone = getTimeZoneForOffset(i4);
                        }
                        output2.value = timeType;
                        parsePosition.setIndex(i3);
                        return timeZone;
                    }
                    parsePosition.setErrorIndex(index);
                    return null;
                }
            }
        }
        timeType = timeType2;
        i4 = i2;
        if (enumSet != null) {
        }
        if (!z2) {
        }
        if (i3 <= index) {
        }
    }

    public TimeZone parse(Style style, String str, ParsePosition parsePosition, Output<TimeType> output) {
        return parse(style, str, parsePosition, null, output);
    }

    public final TimeZone parse(String str, ParsePosition parsePosition) {
        return parse(Style.GENERIC_LOCATION, str, parsePosition, EnumSet.of(ParseOption.ALL_STYLES), null);
    }

    public final TimeZone parse(String str) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        TimeZone parse = parse(str, parsePosition);
        if (parsePosition.getErrorIndex() < 0) {
            return parse;
        }
        throw new ParseException("Unparseable time zone: \"" + str + "\"", 0);
    }

    @Override // java.text.Format
    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        TimeZone timeZone;
        long currentTimeMillis = System.currentTimeMillis();
        if (obj instanceof TimeZone) {
            timeZone = (TimeZone) obj;
        } else if (obj instanceof Calendar) {
            Calendar calendar = (Calendar) obj;
            TimeZone timeZone2 = calendar.getTimeZone();
            long timeInMillis = calendar.getTimeInMillis();
            timeZone = timeZone2;
            currentTimeMillis = timeInMillis;
        } else {
            throw new IllegalArgumentException("Cannot format given Object (" + obj.getClass().getName() + ") as a time zone");
        }
        String formatOffsetLocalizedGMT = formatOffsetLocalizedGMT(timeZone.getOffset(currentTimeMillis));
        stringBuffer.append(formatOffsetLocalizedGMT);
        if (fieldPosition.getFieldAttribute() == DateFormat.Field.TIME_ZONE || fieldPosition.getField() == 17) {
            fieldPosition.setBeginIndex(0);
            fieldPosition.setEndIndex(formatOffsetLocalizedGMT.length());
        }
        return stringBuffer;
    }

    @Override // java.text.Format
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        AttributedString attributedString = new AttributedString(format(obj, new StringBuffer(), new FieldPosition(0)).toString());
        attributedString.addAttribute(DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE);
        return attributedString.getIterator();
    }

    @Override // java.text.Format
    public Object parseObject(String str, ParsePosition parsePosition) {
        return parse(str, parsePosition);
    }

    private String formatOffsetLocalizedGMT(int i, boolean z) {
        boolean z2;
        Object[] objArr;
        if (i == 0) {
            return this._gmtZeroFormat;
        }
        StringBuilder sb = new StringBuilder();
        if (i < 0) {
            i = -i;
            z2 = false;
        } else {
            z2 = true;
        }
        int i2 = i / 3600000;
        int i3 = i % 3600000;
        int i4 = i3 / 60000;
        int i5 = i3 % 60000;
        int i6 = i5 / 1000;
        if (i2 > 23 || i4 > 59 || i6 > 59) {
            throw new IllegalArgumentException("Offset out of range :" + i5);
        }
        if (z2) {
            if (i6 != 0) {
                objArr = this._gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HMS.ordinal()];
            } else if (i4 != 0 || !z) {
                objArr = this._gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HM.ordinal()];
            } else {
                objArr = this._gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_H.ordinal()];
            }
        } else if (i6 != 0) {
            objArr = this._gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()];
        } else if (i4 != 0 || !z) {
            objArr = this._gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HM.ordinal()];
        } else {
            objArr = this._gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_H.ordinal()];
        }
        sb.append(this._gmtPatternPrefix);
        for (Object obj : objArr) {
            if (obj instanceof String) {
                sb.append((String) obj);
            } else if (obj instanceof GMTOffsetField) {
                char type = ((GMTOffsetField) obj).getType();
                int i7 = 2;
                if (type == 'H') {
                    if (z) {
                        i7 = 1;
                    }
                    appendOffsetDigits(sb, i2, i7);
                } else if (type == 'm') {
                    appendOffsetDigits(sb, i4, 2);
                } else if (type == 's') {
                    appendOffsetDigits(sb, i6, 2);
                }
            }
        }
        sb.append(this._gmtPatternSuffix);
        return sb.toString();
    }

    private String formatOffsetISO8601(int i, boolean z, boolean z2, boolean z3, boolean z4) {
        Character ch;
        int i2 = i < 0 ? -i : i;
        if (z2) {
            if (i2 < 1000) {
                return "Z";
            }
            if (z4 && i2 < 60000) {
                return "Z";
            }
        }
        OffsetFields offsetFields = z3 ? OffsetFields.H : OffsetFields.HM;
        OffsetFields offsetFields2 = z4 ? OffsetFields.HM : OffsetFields.HMS;
        if (z) {
            ch = null;
        } else {
            ch = Character.valueOf(DEFAULT_GMT_OFFSET_SEP);
        }
        if (i2 < 86400000) {
            int i3 = i2 % 3600000;
            int[] iArr = {i2 / 3600000, i3 / 60000, (i3 % 60000) / 1000};
            int ordinal = offsetFields2.ordinal();
            while (ordinal > offsetFields.ordinal() && iArr[ordinal] == 0) {
                ordinal--;
            }
            StringBuilder sb = new StringBuilder();
            char c = '+';
            if (i < 0) {
                int i4 = 0;
                while (true) {
                    if (i4 > ordinal) {
                        break;
                    } else if (iArr[i4] != 0) {
                        c = LocaleUtility.IETF_SEPARATOR;
                        break;
                    } else {
                        i4++;
                    }
                }
            }
            sb.append(c);
            for (int i5 = 0; i5 <= ordinal; i5++) {
                if (!(ch == null || i5 == 0)) {
                    sb.append(ch);
                }
                if (iArr[i5] < 10) {
                    sb.append('0');
                }
                sb.append(iArr[i5]);
            }
            return sb.toString();
        }
        throw new IllegalArgumentException("Offset out of range :" + i);
    }

    private String formatSpecific(TimeZone timeZone, TimeZoneNames.NameType nameType, TimeZoneNames.NameType nameType2, long j, Output<TimeType> output) {
        String str;
        boolean inDaylightTime = timeZone.inDaylightTime(new Date(j));
        if (inDaylightTime) {
            str = getTimeZoneNames().getDisplayName(ZoneMeta.getCanonicalCLDRID(timeZone), nameType2, j);
        } else {
            str = getTimeZoneNames().getDisplayName(ZoneMeta.getCanonicalCLDRID(timeZone), nameType, j);
        }
        if (!(str == null || output == null)) {
            output.value = inDaylightTime ? TimeType.DAYLIGHT : TimeType.STANDARD;
        }
        return str;
    }

    private String formatExemplarLocation(TimeZone timeZone) {
        String exemplarLocationName = getTimeZoneNames().getExemplarLocationName(ZoneMeta.getCanonicalCLDRID(timeZone));
        if (exemplarLocationName != null) {
            return exemplarLocationName;
        }
        String exemplarLocationName2 = getTimeZoneNames().getExemplarLocationName(UNKNOWN_ZONE_ID);
        return exemplarLocationName2 == null ? UNKNOWN_LOCATION : exemplarLocationName2;
    }

    private String getTimeZoneID(String str, String str2) {
        if (str != null || (str = this._tznames.getReferenceZoneID(str2, getTargetRegion())) != null) {
            return str;
        }
        throw new IllegalArgumentException("Invalid mzID: " + str2);
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

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.text.TimeZoneFormat$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType = new int[TimeZoneNames.NameType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[TimeZoneNames.NameType.LONG_STANDARD.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[TimeZoneNames.NameType.SHORT_STANDARD.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[TimeZoneNames.NameType.LONG_DAYLIGHT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[TimeZoneNames.NameType.SHORT_DAYLIGHT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style = new int[Style.values().length];
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.GENERIC_LOCATION.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.GENERIC_LONG.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.GENERIC_SHORT.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.SPECIFIC_LONG.ordinal()] = 4;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.SPECIFIC_SHORT.ordinal()] = 5;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ZONE_ID.ordinal()] = 6;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ZONE_ID_SHORT.ordinal()] = 7;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.EXEMPLAR_LOCATION.ordinal()] = 8;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.LOCALIZED_GMT.ordinal()] = 9;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.LOCALIZED_GMT_SHORT.ordinal()] = 10;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_BASIC_SHORT.ordinal()] = 11;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_BASIC_LOCAL_SHORT.ordinal()] = 12;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_BASIC_FIXED.ordinal()] = 13;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_BASIC_LOCAL_FIXED.ordinal()] = 14;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_BASIC_FULL.ordinal()] = 15;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_BASIC_LOCAL_FULL.ordinal()] = 16;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_EXTENDED_FIXED.ordinal()] = 17;
            } catch (NoSuchFieldError unused21) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_EXTENDED_LOCAL_FIXED.ordinal()] = 18;
            } catch (NoSuchFieldError unused22) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_EXTENDED_FULL.ordinal()] = 19;
            } catch (NoSuchFieldError unused23) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneFormat$Style[Style.ISO_EXTENDED_LOCAL_FULL.ordinal()] = 20;
            } catch (NoSuchFieldError unused24) {
            }
        }
    }

    private TimeType getTimeType(TimeZoneNames.NameType nameType) {
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[nameType.ordinal()];
        if (i == 1 || i == 2) {
            return TimeType.STANDARD;
        }
        if (i == 3 || i == 4) {
            return TimeType.DAYLIGHT;
        }
        return TimeType.UNKNOWN;
    }

    private void initGMTPattern(String str) {
        int indexOf = str.indexOf("{0}");
        if (indexOf >= 0) {
            this._gmtPattern = str;
            this._gmtPatternPrefix = unquote(str.substring(0, indexOf));
            this._gmtPatternSuffix = unquote(str.substring(indexOf + 3));
            return;
        }
        throw new IllegalArgumentException("Bad localized GMT pattern: " + str);
    }

    private static String unquote(String str) {
        if (str.indexOf(39) < 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        boolean z = false;
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt != '\'') {
                sb.append(charAt);
            } else if (z) {
                sb.append(charAt);
            } else {
                z = true;
            }
            z = false;
        }
        return sb.toString();
    }

    private void initGMTOffsetPatterns(String[] strArr) {
        int length = GMTOffsetPatternType.values().length;
        if (strArr.length >= length) {
            Object[][] objArr = new Object[length][];
            GMTOffsetPatternType[] values = GMTOffsetPatternType.values();
            for (GMTOffsetPatternType gMTOffsetPatternType : values) {
                int ordinal = gMTOffsetPatternType.ordinal();
                objArr[ordinal] = parseOffsetPattern(strArr[ordinal], gMTOffsetPatternType.required());
            }
            this._gmtOffsetPatterns = new String[length];
            System.arraycopy(strArr, 0, this._gmtOffsetPatterns, 0, length);
            this._gmtOffsetPatternItems = objArr;
            checkAbuttingHoursAndMinutes();
            return;
        }
        throw new IllegalArgumentException("Insufficient number of elements in gmtOffsetPatterns");
    }

    private void checkAbuttingHoursAndMinutes() {
        this._abuttingOffsetHoursAndMinutes = false;
        Object[][] objArr = this._gmtOffsetPatternItems;
        for (Object[] objArr2 : objArr) {
            boolean z = false;
            for (Object obj : objArr2) {
                if (obj instanceof GMTOffsetField) {
                    GMTOffsetField gMTOffsetField = (GMTOffsetField) obj;
                    if (z) {
                        this._abuttingOffsetHoursAndMinutes = true;
                    } else if (gMTOffsetField.getType() == 'H') {
                        z = true;
                    }
                } else if (z) {
                    break;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class GMTOffsetField {
        final char _type;
        final int _width;

        static boolean isValid(char c, int i) {
            return i == 1 || i == 2;
        }

        GMTOffsetField(char c, int i) {
            this._type = c;
            this._width = i;
        }

        /* access modifiers changed from: package-private */
        public char getType() {
            return this._type;
        }

        /* access modifiers changed from: package-private */
        public int getWidth() {
            return this._width;
        }
    }

    private static Object[] parseOffsetPattern(String str, String str2) {
        boolean z;
        StringBuilder sb = new StringBuilder();
        ArrayList arrayList = new ArrayList();
        BitSet bitSet = new BitSet(str2.length());
        boolean z2 = true;
        int i = 1;
        int i2 = 0;
        boolean z3 = false;
        boolean z4 = false;
        char c = 0;
        while (true) {
            if (i2 >= str.length()) {
                z = false;
                break;
            }
            char charAt = str.charAt(i2);
            if (charAt == '\'') {
                if (z4) {
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                    z4 = false;
                } else if (c != 0) {
                    if (!GMTOffsetField.isValid(c, i)) {
                        break;
                    }
                    arrayList.add(new GMTOffsetField(c, i));
                    z4 = true;
                    c = 0;
                } else {
                    z4 = true;
                }
                z3 = !z3;
                i2++;
            } else {
                if (z3) {
                    sb.append(charAt);
                } else {
                    int indexOf = str2.indexOf(charAt);
                    if (indexOf < 0) {
                        if (c != 0) {
                            if (!GMTOffsetField.isValid(c, i)) {
                                break;
                            }
                            arrayList.add(new GMTOffsetField(c, i));
                            c = 0;
                        }
                        sb.append(charAt);
                    } else if (charAt == c) {
                        i++;
                    } else {
                        if (c != 0) {
                            if (!GMTOffsetField.isValid(c, i)) {
                                break;
                            }
                            arrayList.add(new GMTOffsetField(c, i));
                        } else if (sb.length() > 0) {
                            arrayList.add(sb.toString());
                            sb.setLength(0);
                        }
                        bitSet.set(indexOf);
                        i = 1;
                        z4 = false;
                        c = charAt;
                        i2++;
                    }
                }
                z4 = false;
                i2++;
            }
        }
        z = true;
        if (!z) {
            if (c != 0) {
                if (GMTOffsetField.isValid(c, i)) {
                    arrayList.add(new GMTOffsetField(c, i));
                }
                if (z2 && bitSet.cardinality() == str2.length()) {
                    return arrayList.toArray(new Object[arrayList.size()]);
                }
                throw new IllegalStateException("Bad localized GMT offset pattern: " + str);
            } else if (sb.length() > 0) {
                arrayList.add(sb.toString());
                sb.setLength(0);
            }
        }
        z2 = z;
        if (z2) {
        }
        throw new IllegalStateException("Bad localized GMT offset pattern: " + str);
    }

    private static String expandOffsetPattern(String str) {
        int indexOf = str.indexOf("mm");
        if (indexOf >= 0) {
            int lastIndexOf = str.substring(0, indexOf).lastIndexOf(DateFormat.HOUR24);
            String substring = lastIndexOf >= 0 ? str.substring(lastIndexOf + 1, indexOf) : ":";
            StringBuilder sb = new StringBuilder();
            int i = indexOf + 2;
            sb.append(str.substring(0, i));
            sb.append(substring);
            sb.append("ss");
            sb.append(str.substring(i));
            return sb.toString();
        }
        throw new RuntimeException("Bad time zone hour pattern data");
    }

    private static String truncateOffsetPattern(String str) {
        int indexOf = str.indexOf("mm");
        if (indexOf >= 0) {
            int lastIndexOf = str.substring(0, indexOf).lastIndexOf("HH");
            if (lastIndexOf >= 0) {
                return str.substring(0, lastIndexOf + 2);
            }
            int lastIndexOf2 = str.substring(0, indexOf).lastIndexOf(DateFormat.HOUR24);
            if (lastIndexOf2 >= 0) {
                return str.substring(0, lastIndexOf2 + 1);
            }
            throw new RuntimeException("Bad time zone hour pattern data");
        }
        throw new RuntimeException("Bad time zone hour pattern data");
    }

    private void appendOffsetDigits(StringBuilder sb, int i, int i2) {
        int i3 = i >= 10 ? 2 : 1;
        for (int i4 = 0; i4 < i2 - i3; i4++) {
            sb.append(this._gmtOffsetDigits[0]);
        }
        if (i3 == 2) {
            sb.append(this._gmtOffsetDigits[i / 10]);
        }
        sb.append(this._gmtOffsetDigits[i % 10]);
    }

    private TimeZone getTimeZoneForOffset(int i) {
        if (i == 0) {
            return TimeZone.getTimeZone(TZID_GMT);
        }
        return ZoneMeta.getCustomTimeZone(i);
    }

    private int parseOffsetLocalizedGMT(String str, ParsePosition parsePosition, boolean z, Output<Boolean> output) {
        int index = parsePosition.getIndex();
        int[] iArr = {0};
        if (output != null) {
            output.value = false;
        }
        int parseOffsetLocalizedGMTPattern = parseOffsetLocalizedGMTPattern(str, index, z, iArr);
        if (iArr[0] > 0) {
            if (output != null) {
                output.value = true;
            }
            parsePosition.setIndex(index + iArr[0]);
            return parseOffsetLocalizedGMTPattern;
        }
        int parseOffsetDefaultLocalizedGMT = parseOffsetDefaultLocalizedGMT(str, index, iArr);
        if (iArr[0] > 0) {
            if (output != null) {
                output.value = true;
            }
            parsePosition.setIndex(index + iArr[0]);
            return parseOffsetDefaultLocalizedGMT;
        }
        String str2 = this._gmtZeroFormat;
        if (str.regionMatches(true, index, str2, 0, str2.length())) {
            parsePosition.setIndex(index + this._gmtZeroFormat.length());
            return 0;
        }
        String[] strArr = ALT_GMT_STRINGS;
        for (String str3 : strArr) {
            if (str.regionMatches(true, index, str3, 0, str3.length())) {
                parsePosition.setIndex(index + str3.length());
                return 0;
            }
        }
        parsePosition.setErrorIndex(index);
        return 0;
    }

    private int parseOffsetLocalizedGMTPattern(String str, int i, boolean z, int[] iArr) {
        int i2;
        int i3;
        int length = this._gmtPatternPrefix.length();
        boolean z2 = true;
        if (length <= 0 || str.regionMatches(true, i, this._gmtPatternPrefix, 0, length)) {
            i3 = i + length;
            int[] iArr2 = new int[1];
            i2 = parseOffsetFields(str, i3, false, iArr2);
            if (iArr2[0] != 0) {
                i3 += iArr2[0];
                int length2 = this._gmtPatternSuffix.length();
                if (length2 <= 0 || str.regionMatches(true, i3, this._gmtPatternSuffix, 0, length2)) {
                    i3 += length2;
                }
            }
            z2 = false;
        } else {
            i3 = i;
            i2 = 0;
            z2 = false;
        }
        iArr[0] = z2 ? i3 - i : 0;
        return i2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0089, code lost:
        if (r18.isPositive() != false) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0093, code lost:
        r15 = 1;
     */
    private int parseOffsetFields(String str, int i, boolean z, int[] iArr) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        if (iArr != null && iArr.length >= 1) {
            iArr[0] = 0;
        }
        int[] iArr2 = {0, 0, 0};
        GMTOffsetPatternType[] gMTOffsetPatternTypeArr = PARSE_GMT_OFFSET_TYPES;
        int length = gMTOffsetPatternTypeArr.length;
        int i8 = 0;
        int i9 = 0;
        while (true) {
            i2 = -1;
            if (i9 >= length) {
                i3 = i8;
                i4 = 1;
                i5 = 0;
                i6 = 0;
                i7 = 0;
                break;
            }
            GMTOffsetPatternType gMTOffsetPatternType = gMTOffsetPatternTypeArr[i9];
            i8 = parseOffsetFieldsWithPattern(str, i, this._gmtOffsetPatternItems[gMTOffsetPatternType.ordinal()], false, iArr2);
            if (i8 > 0) {
                i3 = i8;
                i4 = gMTOffsetPatternType.isPositive() ? 1 : -1;
                i5 = iArr2[0];
                i6 = iArr2[1];
                i7 = iArr2[2];
            } else {
                i9++;
            }
        }
        if (i3 > 0 && this._abuttingOffsetHoursAndMinutes) {
            GMTOffsetPatternType[] gMTOffsetPatternTypeArr2 = PARSE_GMT_OFFSET_TYPES;
            int length2 = gMTOffsetPatternTypeArr2.length;
            int i10 = 0;
            int i11 = 0;
            while (true) {
                if (i11 >= length2) {
                    break;
                }
                GMTOffsetPatternType gMTOffsetPatternType2 = gMTOffsetPatternTypeArr2[i11];
                i10 = parseOffsetFieldsWithPattern(str, i, this._gmtOffsetPatternItems[gMTOffsetPatternType2.ordinal()], true, iArr2);
                if (i10 <= 0) {
                    i11++;
                    length2 = length2;
                    gMTOffsetPatternTypeArr2 = gMTOffsetPatternTypeArr2;
                }
            }
            if (i10 > i3) {
                i5 = iArr2[0];
                i6 = iArr2[1];
                i7 = iArr2[2];
                i3 = i10;
                i4 = i2;
            }
        }
        if (iArr != null && iArr.length >= 1) {
            iArr[0] = i3;
        }
        if (i3 > 0) {
            return ((((i5 * 60) + i6) * 60) + i7) * 1000 * i4;
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0063 A[EDGE_INSN: B:45:0x0063->B:21:0x0063 ?: BREAK  , SYNTHETIC] */
    private int parseOffsetFieldsWithPattern(String str, int i, Object[] objArr, boolean z, int[] iArr) {
        boolean z2;
        int i2;
        int i3;
        iArr[2] = 0;
        iArr[1] = 0;
        iArr[0] = 0;
        int[] iArr2 = {0};
        int i4 = i;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        while (true) {
            if (i5 >= objArr.length) {
                z2 = false;
                break;
            } else if (objArr[i5] instanceof String) {
                String str2 = (String) objArr[i5];
                int length = str2.length();
                if (i5 == 0 && i4 < str.length()) {
                    if (!PatternProps.isWhiteSpace(str.codePointAt(i4))) {
                        int i9 = 0;
                        while (length > 0) {
                            int codePointAt = str2.codePointAt(i9);
                            if (!PatternProps.isWhiteSpace(codePointAt)) {
                                break;
                            }
                            int charCount = Character.charCount(codePointAt);
                            length -= charCount;
                            i9 += charCount;
                        }
                        i2 = length;
                        i3 = i9;
                        if (str.regionMatches(true, i4, str2, i3, i2)) {
                            break;
                        }
                        i4 += i2;
                        i5++;
                    }
                }
                i3 = 0;
                i2 = length;
                if (str.regionMatches(true, i4, str2, i3, i2)) {
                }
            } else {
                char type = ((GMTOffsetField) objArr[i5]).getType();
                if (type == 'H') {
                    i6 = parseOffsetFieldWithLocalizedDigits(str, i4, 1, z ? 1 : 2, 0, 23, iArr2);
                } else if (type == 'm') {
                    i7 = parseOffsetFieldWithLocalizedDigits(str, i4, 2, 2, 0, 59, iArr2);
                } else if (type == 's') {
                    i8 = parseOffsetFieldWithLocalizedDigits(str, i4, 2, 2, 0, 59, iArr2);
                }
                if (iArr2[0] == 0) {
                    break;
                }
                i4 += iArr2[0];
                i5++;
            }
        }
        z2 = true;
        if (z2) {
            return 0;
        }
        iArr[0] = i6;
        iArr[1] = i7;
        iArr[2] = i8;
        return i4 - i;
    }

    private int parseOffsetDefaultLocalizedGMT(String str, int i, int[] iArr) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        String[] strArr = ALT_GMT_STRINGS;
        int length = strArr.length;
        int i10 = 0;
        while (true) {
            if (i10 >= length) {
                i2 = 0;
                break;
            }
            String str2 = strArr[i10];
            i2 = str2.length();
            if (str.regionMatches(true, i, str2, 0, i2)) {
                break;
            }
            i10++;
        }
        if (i2 != 0 && (i6 = (i5 = i2 + i) + 1) < str.length()) {
            char charAt = str.charAt(i5);
            if (charAt == '+') {
                i7 = 1;
            } else if (charAt == '-') {
                i7 = -1;
            }
            int[] iArr2 = {0};
            int parseDefaultOffsetFields = parseDefaultOffsetFields(str, i6, DEFAULT_GMT_OFFSET_SEP, iArr2);
            if (iArr2[0] == str.length() - i6) {
                i4 = parseDefaultOffsetFields * i7;
                i9 = iArr2[0];
            } else {
                int[] iArr3 = {0};
                int parseAbuttingOffsetFields = parseAbuttingOffsetFields(str, i6, iArr3);
                if (iArr2[0] > iArr3[0]) {
                    i4 = parseDefaultOffsetFields * i7;
                    i9 = iArr2[0];
                } else {
                    i8 = i6 + iArr3[0];
                    i4 = parseAbuttingOffsetFields * i7;
                    i3 = i8 - i;
                    iArr[0] = i3;
                    return i4;
                }
            }
            i8 = i6 + i9;
            i3 = i8 - i;
            iArr[0] = i3;
            return i4;
        }
        i3 = 0;
        i4 = 0;
        iArr[0] = i3;
        return i4;
    }

    private int parseDefaultOffsetFields(String str, int i, char c, int[] iArr) {
        int i2;
        int i3;
        int i4;
        int i5;
        int length = str.length();
        int[] iArr2 = {0};
        int parseOffsetFieldWithLocalizedDigits = parseOffsetFieldWithLocalizedDigits(str, i, 1, 2, 0, 23, iArr2);
        if (iArr2[0] == 0) {
            i3 = i;
            i4 = 0;
            i2 = 0;
        } else {
            int i6 = i + iArr2[0];
            int i7 = i6 + 1;
            if (i7 >= length || str.charAt(i6) != c) {
                i5 = i6;
                i4 = 0;
                i2 = 0;
            } else {
                i5 = i6;
                i2 = parseOffsetFieldWithLocalizedDigits(str, i7, 2, 2, 0, 59, iArr2);
                if (iArr2[0] == 0) {
                    i4 = 0;
                } else {
                    int i8 = i5 + iArr2[0] + 1;
                    int i9 = i8 + 1;
                    if (i9 >= length || str.charAt(i8) != c) {
                        i3 = i8;
                        i4 = 0;
                    } else {
                        i3 = i8;
                        i4 = parseOffsetFieldWithLocalizedDigits(str, i9, 2, 2, 0, 59, iArr2);
                        if (iArr2[0] != 0) {
                            i3 = iArr2[0] + 1 + i3;
                        }
                    }
                }
            }
            i3 = i5;
        }
        if (i3 == i) {
            iArr[0] = 0;
            return 0;
        }
        iArr[0] = i3 - i;
        return (parseOffsetFieldWithLocalizedDigits * 3600000) + (i2 * 60000) + (i4 * 1000);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a5 A[ADDED_TO_REGION, SYNTHETIC] */
    private int parseAbuttingOffsetFields(String str, int i, int[] iArr) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int[] iArr2 = new int[6];
        int[] iArr3 = new int[6];
        int[] iArr4 = {0};
        int i7 = i;
        int i8 = 0;
        for (int i9 = 0; i9 < 6; i9++) {
            iArr2[i9] = parseSingleLocalizedDigit(str, i7, iArr4);
            if (iArr2[i9] < 0) {
                break;
            }
            i7 += iArr4[0];
            iArr3[i9] = i7 - i;
            i8++;
        }
        if (i8 == 0) {
            iArr[0] = 0;
            return 0;
        }
        while (i8 > 0) {
            switch (i8) {
                case 1:
                    i4 = iArr2[0];
                    i3 = 0;
                    i2 = i3;
                    if (i4 > 23 || i3 > 59 || i2 > 59) {
                        i8--;
                    } else {
                        int i10 = (i4 * 3600000) + (i3 * 60000) + (i2 * 1000);
                        iArr[0] = iArr3[i8 - 1];
                        return i10;
                    }
                    break;
                case 2:
                    i4 = (iArr2[0] * 10) + iArr2[1];
                    i3 = 0;
                    i2 = i3;
                    if (i4 > 23) {
                        break;
                    }
                    i8--;
                case 3:
                    i4 = iArr2[0];
                    i3 = (iArr2[1] * 10) + iArr2[2];
                    i2 = 0;
                    if (i4 > 23) {
                    }
                    i8--;
                    break;
                case 4:
                    i4 = (iArr2[0] * 10) + iArr2[1];
                    i3 = iArr2[3] + (iArr2[2] * 10);
                    i2 = 0;
                    if (i4 > 23) {
                    }
                    i8--;
                    break;
                case 5:
                    i6 = iArr2[0];
                    int i11 = iArr2[2];
                    i5 = iArr2[4] + (iArr2[3] * 10);
                    i3 = i11 + (iArr2[1] * 10);
                    i2 = i5;
                    i4 = i6;
                    if (i4 > 23) {
                    }
                    i8--;
                    break;
                case 6:
                    i6 = (iArr2[0] * 10) + iArr2[1];
                    i3 = iArr2[3] + (iArr2[2] * 10);
                    i5 = (iArr2[4] * 10) + iArr2[5];
                    i2 = i5;
                    i4 = i6;
                    if (i4 > 23) {
                    }
                    i8--;
                    break;
                default:
                    i4 = 0;
                    i3 = 0;
                    i2 = i3;
                    if (i4 > 23) {
                    }
                    i8--;
                    break;
            }
        }
        return 0;
    }

    private int parseOffsetFieldWithLocalizedDigits(String str, int i, int i2, int i3, int i4, int i5, int[] iArr) {
        int i6;
        iArr[0] = 0;
        int[] iArr2 = {0};
        int i7 = i;
        int i8 = 0;
        int i9 = 0;
        while (i7 < str.length() && i8 < i3 && (r5 = parseSingleLocalizedDigit(str, i7, iArr2)) >= 0 && (i6 = r5 + (i9 * 10)) <= i5) {
            i8++;
            i7 += iArr2[0];
            i9 = i6;
        }
        if (i8 < i2 || i9 < i4) {
            return -1;
        }
        iArr[0] = i7 - i;
        return i9;
    }

    private int parseSingleLocalizedDigit(String str, int i, int[] iArr) {
        iArr[0] = 0;
        int i2 = -1;
        if (i < str.length()) {
            int codePointAt = Character.codePointAt(str, i);
            int i3 = 0;
            while (true) {
                String[] strArr = this._gmtOffsetDigits;
                if (i3 >= strArr.length) {
                    i3 = -1;
                    break;
                } else if (codePointAt == strArr[i3].codePointAt(0)) {
                    break;
                } else {
                    i3++;
                }
            }
            i2 = i3 < 0 ? UCharacter.digit(codePointAt) : i3;
            if (i2 >= 0) {
                iArr[0] = Character.charCount(codePointAt);
            }
        }
        return i2;
    }

    private static String[] toCodePoints(String str) {
        int i = 0;
        int codePointCount = str.codePointCount(0, str.length());
        String[] strArr = new String[codePointCount];
        int i2 = 0;
        while (i < codePointCount) {
            int charCount = Character.charCount(str.codePointAt(i2)) + i2;
            strArr[i] = str.substring(i2, charCount);
            i++;
            i2 = charCount;
        }
        return strArr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x008a  */
    private static int parseOffsetISO8601(String str, ParsePosition parsePosition, boolean z, Output<Boolean> output) {
        int i;
        int i2;
        if (output != null) {
            output.value = false;
        }
        int index = parsePosition.getIndex();
        if (index >= str.length()) {
            parsePosition.setErrorIndex(index);
            return 0;
        }
        char charAt = str.charAt(index);
        if (Character.toUpperCase(charAt) == "Z".charAt(0)) {
            parsePosition.setIndex(index + 1);
            return 0;
        }
        if (charAt == '+') {
            i = 1;
        } else if (charAt == '-') {
            i = -1;
        } else {
            parsePosition.setErrorIndex(index);
            return 0;
        }
        int i3 = index + 1;
        ParsePosition parsePosition2 = new ParsePosition(i3);
        int parseAsciiOffsetFields = parseAsciiOffsetFields(str, parsePosition2, DEFAULT_GMT_OFFSET_SEP, OffsetFields.H, OffsetFields.HMS);
        if (parsePosition2.getErrorIndex() == -1 && !z && parsePosition2.getIndex() - index <= 3) {
            ParsePosition parsePosition3 = new ParsePosition(i3);
            i2 = parseAbuttingAsciiOffsetFields(str, parsePosition3, OffsetFields.H, OffsetFields.HMS, false);
            if (parsePosition3.getErrorIndex() == -1 && parsePosition3.getIndex() > parsePosition2.getIndex()) {
                parsePosition2.setIndex(parsePosition3.getIndex());
                if (parsePosition2.getErrorIndex() == -1) {
                    parsePosition.setErrorIndex(index);
                    return 0;
                }
                parsePosition.setIndex(parsePosition2.getIndex());
                if (output != null) {
                    output.value = true;
                }
                return i * i2;
            }
        }
        i2 = parseAsciiOffsetFields;
        if (parsePosition2.getErrorIndex() == -1) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b1  */
    private static int parseAbuttingAsciiOffsetFields(String str, ParsePosition parsePosition, OffsetFields offsetFields, OffsetFields offsetFields2, boolean z) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int indexOf;
        int index = parsePosition.getIndex();
        boolean z2 = true;
        int ordinal = ((offsetFields.ordinal() + 1) * 2) - (!z ? 1 : 0);
        int[] iArr = new int[((offsetFields2.ordinal() + 1) * 2)];
        int i6 = index;
        int i7 = 0;
        while (i7 < iArr.length && i6 < str.length() && (indexOf = ASCII_DIGITS.indexOf(str.charAt(i6))) >= 0) {
            iArr[i7] = indexOf;
            i7++;
            i6++;
        }
        if (z && (i7 & 1) != 0) {
            i7--;
        }
        if (i7 < ordinal) {
            parsePosition.setErrorIndex(index);
            return 0;
        }
        while (true) {
            if (i7 >= ordinal) {
                switch (i7) {
                    case 1:
                        i = iArr[0];
                        i2 = 0;
                        i3 = i2;
                        if (i > 23 && i2 <= 59 && i3 <= 59) {
                            break;
                        } else {
                            i7 -= z ? 2 : 1;
                        }
                        break;
                    case 2:
                        i = (iArr[0] * 10) + iArr[1];
                        i2 = 0;
                        i3 = i2;
                        if (i > 23) {
                            break;
                        }
                        i7 -= z ? 2 : 1;
                        break;
                    case 3:
                        i = iArr[0];
                        i2 = (iArr[1] * 10) + iArr[2];
                        i3 = 0;
                        if (i > 23) {
                        }
                        i7 -= z ? 2 : 1;
                        break;
                    case 4:
                        i = (iArr[0] * 10) + iArr[1];
                        i2 = iArr[3] + (iArr[2] * 10);
                        i3 = 0;
                        if (i > 23) {
                        }
                        i7 -= z ? 2 : 1;
                        break;
                    case 5:
                        i5 = iArr[0];
                        i4 = iArr[4] + (iArr[3] * 10);
                        i2 = (iArr[1] * 10) + iArr[2];
                        i3 = i4;
                        i = i5;
                        if (i > 23) {
                        }
                        i7 -= z ? 2 : 1;
                        break;
                    case 6:
                        i5 = (iArr[0] * 10) + iArr[1];
                        i2 = iArr[3] + (iArr[2] * 10);
                        i4 = (iArr[4] * 10) + iArr[5];
                        i3 = i4;
                        i = i5;
                        if (i > 23) {
                        }
                        i7 -= z ? 2 : 1;
                        break;
                    default:
                        i = 0;
                        i2 = 0;
                        i3 = i2;
                        if (i > 23) {
                        }
                        i7 -= z ? 2 : 1;
                        break;
                }
            } else {
                i = 0;
                z2 = false;
                i2 = 0;
                i3 = 0;
            }
        }
        if (!z2) {
            parsePosition.setErrorIndex(index);
            return 0;
        }
        parsePosition.setIndex(index + i7);
        return ((((i * 60) + i2) * 60) + i3) * 1000;
    }

    private static int parseAsciiOffsetFields(String str, ParsePosition parsePosition, char c, OffsetFields offsetFields, OffsetFields offsetFields2) {
        OffsetFields offsetFields3;
        int i;
        int i2;
        int indexOf;
        int index = parsePosition.getIndex();
        int[] iArr = {0, 0, 0};
        int[] iArr2 = {0, -1, -1};
        int i3 = 0;
        for (int i4 = index; i4 < str.length() && i3 <= offsetFields2.ordinal(); i4++) {
            char charAt = str.charAt(i4);
            if (charAt != c) {
                if (iArr2[i3] == -1 || (indexOf = ASCII_DIGITS.indexOf(charAt)) < 0) {
                    break;
                }
                iArr[i3] = (iArr[i3] * 10) + indexOf;
                iArr2[i3] = iArr2[i3] + 1;
                if (iArr2[i3] < 2) {
                }
            } else if (i3 == 0) {
                if (iArr2[0] == 0) {
                    break;
                }
            } else if (iArr2[i3] != -1) {
                break;
            } else {
                iArr2[i3] = 0;
            }
            i3++;
        }
        if (iArr2[0] == 0) {
            offsetFields3 = null;
            i2 = 0;
            i = 0;
        } else if (iArr[0] > 23) {
            i2 = (iArr[0] / 10) * 3600000;
            offsetFields3 = OffsetFields.H;
            i = 1;
        } else {
            i2 = iArr[0] * 3600000;
            i = iArr2[0];
            offsetFields3 = OffsetFields.H;
            if (iArr2[1] == 2 && iArr[1] <= 59) {
                i2 += iArr[1] * 60000;
                i += iArr2[1] + 1;
                offsetFields3 = OffsetFields.HM;
                if (iArr2[2] == 2 && iArr[2] <= 59) {
                    i2 += iArr[2] * 1000;
                    i += iArr2[2] + 1;
                    offsetFields3 = OffsetFields.HMS;
                }
            }
        }
        if (offsetFields3 == null || offsetFields3.ordinal() < offsetFields.ordinal()) {
            parsePosition.setErrorIndex(index);
            return 0;
        }
        parsePosition.setIndex(index + i);
        return i2;
    }

    private static String parseZoneID(String str, ParsePosition parsePosition) {
        if (ZONE_ID_TRIE == null) {
            synchronized (TimeZoneFormat.class) {
                if (ZONE_ID_TRIE == null) {
                    TextTrieMap<String> textTrieMap = new TextTrieMap<>(true);
                    String[] availableIDs = TimeZone.getAvailableIDs();
                    for (String str2 : availableIDs) {
                        textTrieMap.put(str2, str2);
                    }
                    ZONE_ID_TRIE = textTrieMap;
                }
            }
        }
        TextTrieMap.Output output = new TextTrieMap.Output();
        Iterator<String> it = ZONE_ID_TRIE.get(str, parsePosition.getIndex(), output);
        if (it != null) {
            String next = it.next();
            parsePosition.setIndex(parsePosition.getIndex() + output.matchLength);
            return next;
        }
        parsePosition.setErrorIndex(parsePosition.getIndex());
        return null;
    }

    private static String parseShortZoneID(String str, ParsePosition parsePosition) {
        if (SHORT_ZONE_ID_TRIE == null) {
            synchronized (TimeZoneFormat.class) {
                if (SHORT_ZONE_ID_TRIE == null) {
                    TextTrieMap<String> textTrieMap = new TextTrieMap<>(true);
                    for (String str2 : TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.CANONICAL, (String) null, (Integer) null)) {
                        String shortID = ZoneMeta.getShortID(str2);
                        if (shortID != null) {
                            textTrieMap.put(shortID, str2);
                        }
                    }
                    textTrieMap.put(UNKNOWN_SHORT_ZONE_ID, UNKNOWN_ZONE_ID);
                    SHORT_ZONE_ID_TRIE = textTrieMap;
                }
            }
        }
        TextTrieMap.Output output = new TextTrieMap.Output();
        Iterator<String> it = SHORT_ZONE_ID_TRIE.get(str, parsePosition.getIndex(), output);
        if (it != null) {
            String next = it.next();
            parsePosition.setIndex(parsePosition.getIndex() + output.matchLength);
            return next;
        }
        parsePosition.setErrorIndex(parsePosition.getIndex());
        return null;
    }

    private String parseExemplarLocation(String str, ParsePosition parsePosition) {
        int index = parsePosition.getIndex();
        Collection<TimeZoneNames.MatchInfo> find = this._tznames.find(str, index, EnumSet.of(TimeZoneNames.NameType.EXEMPLAR_LOCATION));
        String str2 = null;
        if (find != null) {
            int i = -1;
            TimeZoneNames.MatchInfo matchInfo = null;
            for (TimeZoneNames.MatchInfo matchInfo2 : find) {
                if (matchInfo2.matchLength() + index > i) {
                    i = matchInfo2.matchLength() + index;
                    matchInfo = matchInfo2;
                }
            }
            if (matchInfo != null) {
                str2 = getTimeZoneID(matchInfo.tzID(), matchInfo.mzID());
                parsePosition.setIndex(i);
            }
        }
        if (str2 == null) {
            parsePosition.setErrorIndex(index);
        }
        return str2;
    }

    /* access modifiers changed from: private */
    public static class TimeZoneFormatCache extends SoftCache<ULocale, TimeZoneFormat, ULocale> {
        private TimeZoneFormatCache() {
        }

        /* synthetic */ TimeZoneFormatCache(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: protected */
        public TimeZoneFormat createInstance(ULocale uLocale, ULocale uLocale2) {
            TimeZoneFormat timeZoneFormat = new TimeZoneFormat(uLocale2);
            timeZoneFormat.freeze();
            return timeZoneFormat;
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        ObjectOutputStream.PutField putFields = objectOutputStream.putFields();
        putFields.put("_locale", this._locale);
        putFields.put("_tznames", this._tznames);
        putFields.put("_gmtPattern", this._gmtPattern);
        putFields.put("_gmtOffsetPatterns", this._gmtOffsetPatterns);
        putFields.put("_gmtOffsetDigits", this._gmtOffsetDigits);
        putFields.put("_gmtZeroFormat", this._gmtZeroFormat);
        putFields.put("_parseAllStyles", this._parseAllStyles);
        objectOutputStream.writeFields();
    }

    private void readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField readFields = objectInputStream.readFields();
        this._locale = (ULocale) readFields.get("_locale", (Object) null);
        if (this._locale != null) {
            this._tznames = (TimeZoneNames) readFields.get("_tznames", (Object) null);
            if (this._tznames != null) {
                this._gmtPattern = (String) readFields.get("_gmtPattern", (Object) null);
                if (this._gmtPattern != null) {
                    String[] strArr = (String[]) readFields.get("_gmtOffsetPatterns", (Object) null);
                    if (strArr == null) {
                        throw new InvalidObjectException("Missing field: gmtOffsetPatterns");
                    } else if (strArr.length >= 4) {
                        this._gmtOffsetPatterns = new String[6];
                        if (strArr.length == 4) {
                            for (int i = 0; i < 4; i++) {
                                this._gmtOffsetPatterns[i] = strArr[i];
                            }
                            this._gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_H.ordinal()] = truncateOffsetPattern(this._gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()]);
                            this._gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_H.ordinal()] = truncateOffsetPattern(this._gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()]);
                        } else {
                            this._gmtOffsetPatterns = strArr;
                        }
                        this._gmtOffsetDigits = (String[]) readFields.get("_gmtOffsetDigits", (Object) null);
                        String[] strArr2 = this._gmtOffsetDigits;
                        if (strArr2 == null) {
                            throw new InvalidObjectException("Missing field: gmtOffsetDigits");
                        } else if (strArr2.length == 10) {
                            this._gmtZeroFormat = (String) readFields.get("_gmtZeroFormat", (Object) null);
                            if (this._gmtZeroFormat != null) {
                                this._parseAllStyles = readFields.get("_parseAllStyles", false);
                                if (!readFields.defaulted("_parseAllStyles")) {
                                    TimeZoneNames timeZoneNames = this._tznames;
                                    if (timeZoneNames instanceof TimeZoneNamesImpl) {
                                        this._tznames = TimeZoneNames.getInstance(this._locale);
                                        this._gnames = null;
                                    } else {
                                        this._gnames = new TimeZoneGenericNames(this._locale, timeZoneNames);
                                    }
                                    initGMTPattern(this._gmtPattern);
                                    initGMTOffsetPatterns(this._gmtOffsetPatterns);
                                    return;
                                }
                                throw new InvalidObjectException("Missing field: parseAllStyles");
                            }
                            throw new InvalidObjectException("Missing field: gmtZeroFormat");
                        } else {
                            throw new InvalidObjectException("Incompatible field: gmtOffsetDigits");
                        }
                    } else {
                        throw new InvalidObjectException("Incompatible field: gmtOffsetPatterns");
                    }
                } else {
                    throw new InvalidObjectException("Missing field: gmtPattern");
                }
            } else {
                throw new InvalidObjectException("Missing field: tznames");
            }
        } else {
            throw new InvalidObjectException("Missing field: locale");
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
        TimeZoneFormat timeZoneFormat = (TimeZoneFormat) super.clone();
        timeZoneFormat._frozen = false;
        return timeZoneFormat;
    }
}
