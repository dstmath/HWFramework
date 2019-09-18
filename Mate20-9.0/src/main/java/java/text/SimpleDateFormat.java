package java.text;

import android.icu.text.TimeZoneNames;
import android.icu.util.ULocale;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.Format;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.LocaleData;
import sun.util.calendar.CalendarUtils;

public class SimpleDateFormat extends DateFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Set<TimeZoneNames.NameType> DST_NAME_TYPES = Collections.unmodifiableSet(EnumSet.of(TimeZoneNames.NameType.LONG_DAYLIGHT, TimeZoneNames.NameType.SHORT_DAYLIGHT));
    private static final String GMT = "GMT";
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final EnumSet<TimeZoneNames.NameType> NAME_TYPES = EnumSet.of(TimeZoneNames.NameType.LONG_GENERIC, (E[]) new TimeZoneNames.NameType[]{TimeZoneNames.NameType.LONG_STANDARD, TimeZoneNames.NameType.LONG_DAYLIGHT, TimeZoneNames.NameType.SHORT_GENERIC, TimeZoneNames.NameType.SHORT_STANDARD, TimeZoneNames.NameType.SHORT_DAYLIGHT});
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD = {0, 1, 2, 5, 11, 11, 12, 13, 14, 7, 6, 8, 3, 4, 9, 10, 10, 15, 15, 17, 1000, 15, 2, 7, 9, 9};
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 17, 1, 9, 17, 2, 9, 14, 14};
    private static final DateFormat.Field[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID = {DateFormat.Field.ERA, DateFormat.Field.YEAR, DateFormat.Field.MONTH, DateFormat.Field.DAY_OF_MONTH, DateFormat.Field.HOUR_OF_DAY1, DateFormat.Field.HOUR_OF_DAY0, DateFormat.Field.MINUTE, DateFormat.Field.SECOND, DateFormat.Field.MILLISECOND, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.DAY_OF_YEAR, DateFormat.Field.DAY_OF_WEEK_IN_MONTH, DateFormat.Field.WEEK_OF_YEAR, DateFormat.Field.WEEK_OF_MONTH, DateFormat.Field.AM_PM, DateFormat.Field.HOUR1, DateFormat.Field.HOUR0, DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE, DateFormat.Field.YEAR, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.TIME_ZONE, DateFormat.Field.MONTH, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.AM_PM, DateFormat.Field.AM_PM};
    private static final int TAG_QUOTE_ASCII_CHAR = 100;
    private static final int TAG_QUOTE_CHARS = 101;
    private static final ConcurrentMap<Locale, NumberFormat> cachedNumberFormatData = new ConcurrentHashMap(3);
    static final int currentSerialVersion = 1;
    static final long serialVersionUID = 4774881970558875024L;
    private transient char[] compiledPattern;
    private Date defaultCenturyStart;
    private transient int defaultCenturyStartYear;
    private DateFormatSymbols formatData;
    private transient boolean hasFollowingMinusSign;
    private Locale locale;
    private transient char minusSign;
    private transient NumberFormat originalNumberFormat;
    private transient String originalNumberPattern;
    private String pattern;
    private int serialVersionOnStream;
    private transient TimeZoneNames timeZoneNames;
    transient boolean useDateFormatSymbols;
    private transient char zeroDigit;

    public SimpleDateFormat() {
        this(3, 3, Locale.getDefault(Locale.Category.FORMAT));
    }

    SimpleDateFormat(int timeStyle, int dateStyle, Locale locale2) {
        this(getDateTimeFormat(timeStyle, dateStyle, locale2), locale2);
    }

    private static String getDateTimeFormat(int timeStyle, int dateStyle, Locale locale2) {
        LocaleData localeData = LocaleData.get(locale2);
        if (timeStyle >= 0 && dateStyle >= 0) {
            return MessageFormat.format("{0} {1}", localeData.getDateFormat(dateStyle), localeData.getTimeFormat(timeStyle));
        } else if (timeStyle >= 0) {
            return localeData.getTimeFormat(timeStyle);
        } else {
            if (dateStyle >= 0) {
                return localeData.getDateFormat(dateStyle);
            }
            throw new IllegalArgumentException("No date or time style specified");
        }
    }

    public SimpleDateFormat(String pattern2) {
        this(pattern2, Locale.getDefault(Locale.Category.FORMAT));
    }

    public SimpleDateFormat(String pattern2, Locale locale2) {
        this.serialVersionOnStream = 1;
        this.minusSign = '-';
        this.hasFollowingMinusSign = $assertionsDisabled;
        if (pattern2 == null || locale2 == null) {
            throw new NullPointerException();
        }
        initializeCalendar(locale2);
        this.pattern = pattern2;
        this.formatData = DateFormatSymbols.getInstanceRef(locale2);
        this.locale = locale2;
        initialize(locale2);
    }

    public SimpleDateFormat(String pattern2, DateFormatSymbols formatSymbols) {
        this.serialVersionOnStream = 1;
        this.minusSign = '-';
        this.hasFollowingMinusSign = $assertionsDisabled;
        if (pattern2 == null || formatSymbols == null) {
            throw new NullPointerException();
        }
        this.pattern = pattern2;
        this.formatData = (DateFormatSymbols) formatSymbols.clone();
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        initializeCalendar(this.locale);
        initialize(this.locale);
        this.useDateFormatSymbols = true;
    }

    private void initialize(Locale loc) {
        this.compiledPattern = compile(this.pattern);
        this.numberFormat = cachedNumberFormatData.get(loc);
        if (this.numberFormat == null) {
            this.numberFormat = NumberFormat.getIntegerInstance(loc);
            this.numberFormat.setGroupingUsed($assertionsDisabled);
            cachedNumberFormatData.putIfAbsent(loc, this.numberFormat);
        }
        this.numberFormat = (NumberFormat) this.numberFormat.clone();
        initializeDefaultCentury();
    }

    private void initializeCalendar(Locale loc) {
        if (this.calendar == null) {
            this.calendar = Calendar.getInstance(TimeZone.getDefault(), loc);
        }
    }

    private char[] compile(String pattern2) {
        int lastTag;
        String str = pattern2;
        int length = pattern2.length();
        StringBuilder compiledCode = new StringBuilder(length * 2);
        int i = 0;
        int lastTag2 = -1;
        int count = 0;
        StringBuilder tmpBuffer = null;
        boolean inQuote = false;
        int i2 = 0;
        while (i2 < length) {
            char c = str.charAt(i2);
            if (c == '\'') {
                if (i2 + 1 < length) {
                    char c2 = str.charAt(i2 + 1);
                    if (c2 == '\'') {
                        i2++;
                        if (count != 0) {
                            encode(lastTag2, count, compiledCode);
                            lastTag2 = -1;
                            count = 0;
                        }
                        if (inQuote) {
                            tmpBuffer.append(c2);
                        } else {
                            compiledCode.append((char) (25600 | c2));
                        }
                    }
                }
                if (!inQuote) {
                    if (count != 0) {
                        encode(lastTag2, count, compiledCode);
                        lastTag2 = -1;
                        count = 0;
                    }
                    if (tmpBuffer == null) {
                        tmpBuffer = new StringBuilder(length);
                    } else {
                        tmpBuffer.setLength(i);
                    }
                    inQuote = true;
                } else {
                    int len = tmpBuffer.length();
                    if (len == 1) {
                        char ch = tmpBuffer.charAt(i);
                        if (ch < 128) {
                            compiledCode.append((char) (25600 | ch));
                        } else {
                            compiledCode.append(25857);
                            compiledCode.append(ch);
                        }
                    } else {
                        encode(TAG_QUOTE_CHARS, len, compiledCode);
                        compiledCode.append((CharSequence) tmpBuffer);
                    }
                    inQuote = $assertionsDisabled;
                }
            } else if (inQuote) {
                tmpBuffer.append(c);
            } else if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
                if (count != 0) {
                    encode(lastTag2, count, compiledCode);
                    lastTag2 = -1;
                    count = 0;
                }
                if (c < 128) {
                    compiledCode.append((char) (25600 | c));
                } else {
                    int j = i2 + 1;
                    while (j < length) {
                        char d = str.charAt(j);
                        if (d == '\'' || ((d >= 'a' && d <= 'z') || (d >= 'A' && d <= 'Z'))) {
                            break;
                        }
                        j++;
                    }
                    compiledCode.append((char) (25856 | (j - i2)));
                    while (i2 < j) {
                        compiledCode.append(str.charAt(i2));
                        i2++;
                    }
                    i2--;
                }
            } else {
                int indexOf = "GyMdkHmsSEDFwWahKzZYuXLcbB".indexOf((int) c);
                int tag = indexOf;
                if (indexOf != -1) {
                    if (lastTag2 == -1 || lastTag2 == tag) {
                        lastTag = tag;
                        count++;
                    } else {
                        encode(lastTag2, count, compiledCode);
                        lastTag = tag;
                        count = 1;
                    }
                    lastTag2 = lastTag;
                } else {
                    throw new IllegalArgumentException("Illegal pattern character '" + c + "'");
                }
            }
            i2++;
            i = 0;
        }
        if (!inQuote) {
            if (count != 0) {
                encode(lastTag2, count, compiledCode);
            }
            int len2 = compiledCode.length();
            char[] r = new char[len2];
            compiledCode.getChars(0, len2, r, 0);
            return r;
        }
        throw new IllegalArgumentException("Unterminated quote");
    }

    private static void encode(int tag, int length, StringBuilder buffer) {
        if (tag == 21 && length >= 4) {
            throw new IllegalArgumentException("invalid ISO 8601 format: length=" + length);
        } else if (length < 255) {
            buffer.append((char) ((tag << 8) | length));
        } else {
            buffer.append((char) (255 | (tag << 8)));
            buffer.append((char) (length >>> 16));
            buffer.append((char) (65535 & length));
        }
    }

    private void initializeDefaultCentury() {
        this.calendar.setTimeInMillis(System.currentTimeMillis());
        this.calendar.add(1, -80);
        parseAmbiguousDatesAsAfter(this.calendar.getTime());
    }

    private void parseAmbiguousDatesAsAfter(Date startDate) {
        this.defaultCenturyStart = startDate;
        this.calendar.setTime(startDate);
        this.defaultCenturyStartYear = this.calendar.get(1);
    }

    public void set2DigitYearStart(Date startDate) {
        parseAmbiguousDatesAsAfter(new Date(startDate.getTime()));
    }

    public Date get2DigitYearStart() {
        return (Date) this.defaultCenturyStart.clone();
    }

    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        pos.endIndex = 0;
        pos.beginIndex = 0;
        return format(date, toAppendTo, pos.getFieldDelegate());
    }

    private StringBuffer format(Date date, StringBuffer toAppendTo, Format.FieldDelegate delegate) {
        this.calendar.setTime(date);
        boolean useDateFormatSymbols2 = useDateFormatSymbols();
        int i = 0;
        while (i < this.compiledPattern.length) {
            int tag = this.compiledPattern[i] >>> 8;
            int i2 = i + 1;
            int count = this.compiledPattern[i] & 255;
            if (count == 255) {
                int i3 = i2 + 1;
                i2 = i3 + 1;
                count = (this.compiledPattern[i2] << 16) | this.compiledPattern[i3];
            }
            int count2 = count;
            int i4 = i2;
            switch (tag) {
                case TAG_QUOTE_ASCII_CHAR /*100*/:
                    toAppendTo.append((char) count2);
                    break;
                case TAG_QUOTE_CHARS /*101*/:
                    toAppendTo.append(this.compiledPattern, i4, count2);
                    i4 += count2;
                    break;
                default:
                    subFormat(tag, count2, delegate, toAppendTo, useDateFormatSymbols2);
                    break;
            }
            i = i4;
        }
        return toAppendTo;
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer sb = new StringBuffer();
        CharacterIteratorFieldDelegate delegate = new CharacterIteratorFieldDelegate();
        if (obj instanceof Date) {
            format((Date) obj, sb, (Format.FieldDelegate) delegate);
        } else if (obj instanceof Number) {
            format(new Date(((Number) obj).longValue()), sb, (Format.FieldDelegate) delegate);
        } else if (obj == null) {
            throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Date");
        }
        return delegate.getIterator(sb.toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:104:0x0221  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x023d  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00e6  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x010a  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0189  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01a4  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01b3  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01d9  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01f2  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0203  */
    private void subFormat(int patternCharIndex, int count, Format.FieldDelegate delegate, StringBuffer buffer, boolean useDateFormatSymbols2) {
        int patternCharIndex2;
        int field;
        int value;
        int style;
        String current;
        String zoneString;
        TimeZoneNames.NameType nameType;
        int i = count;
        StringBuffer stringBuffer = buffer;
        boolean z = useDateFormatSymbols2;
        String current2 = null;
        int beginOffset = buffer.length();
        int field2 = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        if (field2 == 17) {
            if (this.calendar.isWeekDateSupported()) {
                value = this.calendar.getWeekYear();
            } else {
                int field3 = PATTERN_INDEX_TO_CALENDAR_FIELD[1];
                value = this.calendar.get(field3);
                field = field3;
                patternCharIndex2 = 1;
                int value2 = value;
                boolean includeGmt = true;
                style = i < 4 ? 2 : 1;
                if (!z && field != 1000) {
                    current2 = this.calendar.getDisplayName(field, style, this.locale);
                }
                String current3 = current2;
                switch (patternCharIndex2) {
                    case 0:
                        int field4 = style;
                        if (z) {
                            String[] eras = this.formatData.getEras();
                            if (value2 < eras.length) {
                                current3 = eras[value2];
                            }
                        }
                        if (current3 == null) {
                            current3 = "";
                            break;
                        }
                        break;
                    case 1:
                    case 19:
                        int i2 = 1;
                        int i3 = field;
                        int style2 = style;
                        if (!(this.calendar instanceof GregorianCalendar)) {
                            if (current3 == null) {
                                if (style2 != 2) {
                                    i2 = i;
                                }
                                zeroPaddingNumber(value2, i2, Integer.MAX_VALUE, stringBuffer);
                                break;
                            }
                        } else if (i == 2) {
                            zeroPaddingNumber(value2, 2, 2, stringBuffer);
                            break;
                        } else {
                            zeroPaddingNumber(value2, i, Integer.MAX_VALUE, stringBuffer);
                            break;
                        }
                        break;
                    case 2:
                        int field5 = style;
                        if (z) {
                            current3 = formatMonth(i, value2, Integer.MAX_VALUE, stringBuffer, z, $assertionsDisabled);
                            break;
                        }
                        break;
                    case 4:
                        int field6 = style;
                        if (current3 == null) {
                            if (value2 != 0) {
                                zeroPaddingNumber(value2, i, Integer.MAX_VALUE, stringBuffer);
                                break;
                            } else {
                                zeroPaddingNumber(this.calendar.getMaximum(11) + 1, i, Integer.MAX_VALUE, stringBuffer);
                                break;
                            }
                        }
                        break;
                    case 8:
                        int field7 = style;
                        if (current3 == null) {
                            value2 = (int) ((((double) value2) / 1000.0d) * Math.pow(10.0d, (double) i));
                            zeroPaddingNumber(value2, i, i, stringBuffer);
                            break;
                        }
                        break;
                    case 9:
                        int field8 = style;
                        if (current3 == null) {
                            current3 = formatWeekday(i, value2, z, $assertionsDisabled);
                            break;
                        }
                        break;
                    case 14:
                        int field9 = style;
                        if (z) {
                            current3 = this.formatData.getAmPmStrings()[value2];
                            break;
                        }
                        break;
                    case 15:
                        int field10 = style;
                        if (current3 == null) {
                            if (value2 != 0) {
                                zeroPaddingNumber(value2, i, Integer.MAX_VALUE, stringBuffer);
                                break;
                            } else {
                                zeroPaddingNumber(this.calendar.getLeastMaximum(10) + 1, i, Integer.MAX_VALUE, stringBuffer);
                                break;
                            }
                        }
                        break;
                    case 17:
                        int field11 = style;
                        if (current3 == null) {
                            TimeZone tz = this.calendar.getTimeZone();
                            boolean daylight = this.calendar.get(16) != 0 ? true : $assertionsDisabled;
                            if (this.formatData.isZoneStringsSet) {
                                zoneString = libcore.icu.TimeZoneNames.getDisplayName(this.formatData.getZoneStringsWrapper(), tz.getID(), daylight, i < 4 ? 0 : 1);
                                TimeZone timeZone = tz;
                            } else {
                                if (i < 4) {
                                    if (daylight) {
                                        nameType = TimeZoneNames.NameType.SHORT_DAYLIGHT;
                                    } else {
                                        nameType = TimeZoneNames.NameType.SHORT_STANDARD;
                                    }
                                } else if (daylight) {
                                    nameType = TimeZoneNames.NameType.LONG_DAYLIGHT;
                                } else {
                                    nameType = TimeZoneNames.NameType.LONG_STANDARD;
                                }
                                TimeZone timeZone2 = tz;
                                zoneString = getTimeZoneNames().getDisplayName(android.icu.util.TimeZone.getCanonicalID(tz.getID()), nameType, this.calendar.getTimeInMillis());
                            }
                            if (zoneString == null) {
                                stringBuffer.append(TimeZone.createGmtOffsetString(true, true, this.calendar.get(15) + this.calendar.get(16)));
                                break;
                            } else {
                                stringBuffer.append(zoneString);
                                break;
                            }
                        }
                        break;
                    case 18:
                        int field12 = style;
                        value2 = this.calendar.get(15) + this.calendar.get(16);
                        boolean includeSeparator = i >= 4 ? true : $assertionsDisabled;
                        if (i != 4) {
                            includeGmt = $assertionsDisabled;
                        }
                        stringBuffer.append(TimeZone.createGmtOffsetString(includeGmt, includeSeparator, value2));
                        break;
                    case 21:
                        int field13 = style;
                        value2 = this.calendar.get(15) + this.calendar.get(16);
                        if (value2 != 0) {
                            value2 /= MILLIS_PER_MINUTE;
                            if (value2 >= 0) {
                                stringBuffer.append('+');
                            } else {
                                stringBuffer.append('-');
                                value2 = -value2;
                            }
                            CalendarUtils.sprintf0d(stringBuffer, value2 / 60, 2);
                            if (i != 1) {
                                if (i == 3) {
                                    stringBuffer.append(':');
                                }
                                CalendarUtils.sprintf0d(stringBuffer, value2 % 60, 2);
                                break;
                            }
                        } else {
                            stringBuffer.append('Z');
                            break;
                        }
                        break;
                    case 22:
                        if (!z) {
                            int field14 = style;
                            break;
                        } else {
                            int i4 = field;
                            int field15 = style;
                            current3 = formatMonth(i, value2, Integer.MAX_VALUE, stringBuffer, z, true);
                            break;
                        }
                    case 23:
                        if (current3 == null) {
                            current3 = formatWeekday(i, value2, z, true);
                            break;
                        }
                        break;
                    case 24:
                    case 25:
                        current3 = "";
                        break;
                    default:
                        int i5 = field;
                        int field16 = style;
                        if (current3 == null) {
                            zeroPaddingNumber(value2, i, Integer.MAX_VALUE, stringBuffer);
                            break;
                        }
                        break;
                }
                String str = current3;
                current = str;
                if (current != null) {
                    stringBuffer.append(current);
                }
                int fieldID = PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex2];
                DateFormat.Field f = PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID[patternCharIndex2];
                delegate.formatted(fieldID, f, f, beginOffset, buffer.length(), stringBuffer);
            }
        } else if (field2 == 1000) {
            value = CalendarBuilder.toISODayOfWeek(this.calendar.get(7));
        } else {
            value = this.calendar.get(field2);
        }
        patternCharIndex2 = patternCharIndex;
        field = field2;
        int value22 = value;
        boolean includeGmt2 = true;
        style = i < 4 ? 2 : 1;
        current2 = this.calendar.getDisplayName(field, style, this.locale);
        String current32 = current2;
        switch (patternCharIndex2) {
            case 0:
                break;
            case 1:
            case 19:
                break;
            case 2:
                break;
            case 4:
                break;
            case 8:
                break;
            case 9:
                break;
            case 14:
                break;
            case 15:
                break;
            case 17:
                break;
            case 18:
                break;
            case 21:
                break;
            case 22:
                break;
            case 23:
                break;
            case 24:
            case 25:
                break;
        }
        String str2 = current32;
        current = str2;
        if (current != null) {
        }
        int fieldID2 = PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex2];
        DateFormat.Field f2 = PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID[patternCharIndex2];
        delegate.formatted(fieldID2, f2, f2, beginOffset, buffer.length(), stringBuffer);
    }

    private String formatWeekday(int count, int value, boolean useDateFormatSymbols2, boolean standalone) {
        String[] weekdays;
        if (!useDateFormatSymbols2) {
            return null;
        }
        if (count == 4) {
            weekdays = standalone ? this.formatData.getStandAloneWeekdays() : this.formatData.getWeekdays();
        } else if (count == 5) {
            weekdays = standalone ? this.formatData.getTinyStandAloneWeekdays() : this.formatData.getTinyWeekdays();
        } else {
            weekdays = standalone ? this.formatData.getShortStandAloneWeekdays() : this.formatData.getShortWeekdays();
        }
        return weekdays[value];
    }

    private String formatMonth(int count, int value, int maxIntCount, StringBuffer buffer, boolean useDateFormatSymbols2, boolean standalone) {
        String[] months;
        String current = null;
        if (useDateFormatSymbols2) {
            if (count == 4) {
                months = standalone ? this.formatData.getStandAloneMonths() : this.formatData.getMonths();
            } else if (count == 5) {
                months = standalone ? this.formatData.getTinyStandAloneMonths() : this.formatData.getTinyMonths();
            } else if (count == 3) {
                months = standalone ? this.formatData.getShortStandAloneMonths() : this.formatData.getShortMonths();
            } else {
                months = null;
            }
            if (months != null) {
                current = months[value];
            }
        } else if (count < 3) {
            current = null;
        }
        if (current == null) {
            zeroPaddingNumber(value + 1, count, maxIntCount, buffer);
        }
        return current;
    }

    private void zeroPaddingNumber(int value, int minDigits, int maxDigits, StringBuffer buffer) {
        try {
            if (this.zeroDigit == 0) {
                this.zeroDigit = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getZeroDigit();
            }
            if (value >= 0) {
                if (value < TAG_QUOTE_ASCII_CHAR && minDigits >= 1 && minDigits <= 2) {
                    if (value < 10) {
                        if (minDigits == 2) {
                            buffer.append(this.zeroDigit);
                        }
                        buffer.append((char) (this.zeroDigit + value));
                    } else {
                        buffer.append((char) (this.zeroDigit + (value / 10)));
                        buffer.append((char) (this.zeroDigit + (value % 10)));
                    }
                    return;
                } else if (value >= 1000 && value < 10000) {
                    if (minDigits == 4) {
                        buffer.append((char) (this.zeroDigit + (value / 1000)));
                        int value2 = value % 1000;
                        buffer.append((char) (this.zeroDigit + (value2 / TAG_QUOTE_ASCII_CHAR)));
                        int value3 = value2 % TAG_QUOTE_ASCII_CHAR;
                        buffer.append((char) (this.zeroDigit + (value3 / 10)));
                        buffer.append((char) (this.zeroDigit + (value3 % 10)));
                        return;
                    } else if (minDigits == 2 && maxDigits == 2) {
                        zeroPaddingNumber(value % TAG_QUOTE_ASCII_CHAR, 2, 2, buffer);
                        return;
                    }
                }
            }
        } catch (Exception e) {
        }
        this.numberFormat.setMinimumIntegerDigits(minDigits);
        this.numberFormat.setMaximumIntegerDigits(maxDigits);
        this.numberFormat.format((long) value, buffer, DontCareFieldPosition.INSTANCE);
    }

    public Date parse(String text, ParsePosition pos) {
        TimeZone tz = getTimeZone();
        try {
            return parseInternal(text, pos);
        } finally {
            setTimeZone(tz);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x006a, code lost:
        if (r8 <= 0) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x006c, code lost:
        if (r9 >= r15) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x006e, code lost:
        r4 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0078, code lost:
        if (r12.charAt(r9) == r11.compiledPattern[r0]) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x007a, code lost:
        r0 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0081, code lost:
        r13.index = r14;
        r13.errorIndex = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0085, code lost:
        return null;
     */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v18, types: [char] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00da  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00d7 A[SYNTHETIC] */
    private Date parseInternal(String text, ParsePosition pos) {
        int count;
        CalendarBuilder calb;
        int i;
        boolean useFollowingMinusSignAsDelimiter;
        boolean obeyCount;
        int start;
        int c;
        String str = text;
        ParsePosition parsePosition = pos;
        checkNegativeNumberExpression();
        int start2 = parsePosition.index;
        int oldStart = start2;
        int textLength = text.length();
        boolean[] ambiguousYear = {$assertionsDisabled};
        CalendarBuilder calb2 = new CalendarBuilder();
        int start3 = start2;
        int count2 = 0;
        while (count2 < this.compiledPattern.length) {
            int tag = this.compiledPattern[count2] >>> 8;
            int i2 = count2 + 1;
            int i3 = this.compiledPattern[count2] & 255;
            if (i3 == 255) {
                int i4 = i2 + 1;
                count = (this.compiledPattern[i2] << 16) | this.compiledPattern[i4];
                count2 = i4 + 1;
            } else {
                count = i3;
                count2 = i2;
            }
            switch (tag) {
                case TAG_QUOTE_ASCII_CHAR /*100*/:
                    if (start3 < textLength && str.charAt(start3) == ((char) count)) {
                        start3++;
                        break;
                    } else {
                        parsePosition.index = oldStart;
                        parsePosition.errorIndex = start3;
                        return null;
                    }
                case TAG_QUOTE_CHARS /*101*/:
                    while (true) {
                        int count3 = count - 1;
                        start3++;
                        count = count3;
                        count2 = i;
                        break;
                    }
                default:
                    boolean obeyCount2 = $assertionsDisabled;
                    if (count2 < this.compiledPattern.length) {
                        int nextTag = this.compiledPattern[count2] >>> 8;
                        if (!(nextTag == TAG_QUOTE_ASCII_CHAR || nextTag == TAG_QUOTE_CHARS)) {
                            obeyCount2 = true;
                        }
                        if (this.hasFollowingMinusSign && (nextTag == TAG_QUOTE_ASCII_CHAR || nextTag == TAG_QUOTE_CHARS)) {
                            if (nextTag == TAG_QUOTE_ASCII_CHAR) {
                                c = 255 & this.compiledPattern[count2];
                            } else {
                                c = this.compiledPattern[count2 + 1];
                            }
                            if (c == this.minusSign) {
                                useFollowingMinusSignAsDelimiter = true;
                                obeyCount = obeyCount2;
                                int i5 = count;
                                int i6 = start3;
                                calb = calb2;
                                start = subParse(str, start3, tag, count, obeyCount, ambiguousYear, parsePosition, useFollowingMinusSignAsDelimiter, calb2);
                                if (start >= 0) {
                                    parsePosition.index = oldStart;
                                    return null;
                                }
                                start3 = start;
                                continue;
                            }
                        }
                    }
                    obeyCount = obeyCount2;
                    useFollowingMinusSignAsDelimiter = false;
                    int i52 = count;
                    int i62 = start3;
                    calb = calb2;
                    start = subParse(str, start3, tag, count, obeyCount, ambiguousYear, parsePosition, useFollowingMinusSignAsDelimiter, calb2);
                    if (start >= 0) {
                    }
            }
            calb = calb2;
            calb2 = calb;
            str = text;
        }
        int start4 = start3;
        CalendarBuilder calb3 = calb2;
        parsePosition.index = start4;
        try {
            CalendarBuilder calb4 = calb3;
            try {
                Date parsedDate = calb4.establish(this.calendar).getTime();
                if (ambiguousYear[0] && parsedDate.before(this.defaultCenturyStart)) {
                    parsedDate = calb4.addYear(TAG_QUOTE_ASCII_CHAR).establish(this.calendar).getTime();
                }
                return parsedDate;
            } catch (IllegalArgumentException e) {
                parsePosition.errorIndex = start4;
                parsePosition.index = oldStart;
                return null;
            }
        } catch (IllegalArgumentException e2) {
            CalendarBuilder calendarBuilder = calb3;
            parsePosition.errorIndex = start4;
            parsePosition.index = oldStart;
            return null;
        }
    }

    private int matchString(String text, int start, int field, String[] data, CalendarBuilder calb) {
        int bestMatch;
        int i = start;
        int i2 = field;
        String[] strArr = data;
        int i3 = 0;
        int count = strArr.length;
        if (i2 == 7) {
            i3 = 1;
        }
        int bestMatch2 = -1;
        int i4 = i3;
        int bestMatchLength = 0;
        while (true) {
            bestMatch = bestMatch2;
            if (i4 >= count) {
                break;
            }
            int length = strArr[i4].length();
            if (length > bestMatchLength) {
                if (text.regionMatches(true, i, strArr[i4], 0, length)) {
                    bestMatch = i4;
                    bestMatchLength = length;
                }
            }
            if (strArr[i4].charAt(length - 1) == '.' && length - 1 > bestMatchLength) {
                if (text.regionMatches(true, i, strArr[i4], 0, length - 1)) {
                    bestMatch2 = i4;
                    bestMatchLength = length - 1;
                    i4++;
                }
            }
            bestMatch2 = bestMatch;
            i4++;
        }
        if (bestMatch >= 0) {
            calb.set(i2, bestMatch);
            return i + bestMatchLength;
        }
        CalendarBuilder calendarBuilder = calb;
        return -i;
    }

    private int matchString(String text, int start, int field, Map<String, Integer> data, CalendarBuilder calb) {
        if (data != null) {
            String bestMatch = null;
            for (String name : data.keySet()) {
                int length = name.length();
                if ((bestMatch == null || length > bestMatch.length()) && text.regionMatches(true, start, name, 0, length)) {
                    bestMatch = name;
                }
            }
            if (bestMatch != null) {
                calb.set(field, data.get(bestMatch).intValue());
                return bestMatch.length() + start;
            }
        }
        return -start;
    }

    private int matchZoneString(String text, int start, String[] zoneNames) {
        for (int i = 1; i <= 4; i++) {
            String zoneName = zoneNames[i];
            if (text.regionMatches(true, start, zoneName, 0, zoneName.length())) {
                return i;
            }
        }
        return -1;
    }

    private int subParseZoneString(String text, int start, CalendarBuilder calb) {
        if (this.formatData.isZoneStringsSet) {
            return subParseZoneStringFromSymbols(text, start, calb);
        }
        return subParseZoneStringFromICU(text, start, calb);
    }

    private TimeZoneNames getTimeZoneNames() {
        if (this.timeZoneNames == null) {
            this.timeZoneNames = TimeZoneNames.getInstance(this.locale);
        }
        return this.timeZoneNames;
    }

    private int subParseZoneStringFromICU(String text, int start, CalendarBuilder calb) {
        String currentTimeZoneID = android.icu.util.TimeZone.getCanonicalID(getTimeZone().getID());
        TimeZoneNames tzNames = getTimeZoneNames();
        TimeZoneNames.MatchInfo bestMatch = null;
        Set<String> currentTzMetaZoneIds = null;
        Iterator<TimeZoneNames.MatchInfo> it = tzNames.find(text, start, NAME_TYPES).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            TimeZoneNames.MatchInfo match = it.next();
            if (bestMatch == null || bestMatch.matchLength() < match.matchLength()) {
                bestMatch = match;
            } else if (bestMatch.matchLength() != match.matchLength()) {
                continue;
            } else if (currentTimeZoneID.equals(match.tzID())) {
                bestMatch = match;
                break;
            } else if (match.mzID() == null) {
                continue;
            } else {
                if (currentTzMetaZoneIds == null) {
                    currentTzMetaZoneIds = tzNames.getAvailableMetaZoneIDs(currentTimeZoneID);
                }
                if (currentTzMetaZoneIds.contains(match.mzID())) {
                    bestMatch = match;
                    break;
                }
            }
        }
        if (bestMatch == null) {
            return -start;
        }
        String tzId = bestMatch.tzID();
        if (tzId == null) {
            if (currentTzMetaZoneIds == null) {
                currentTzMetaZoneIds = tzNames.getAvailableMetaZoneIDs(currentTimeZoneID);
            }
            if (currentTzMetaZoneIds.contains(bestMatch.mzID())) {
                tzId = currentTimeZoneID;
            } else {
                ULocale uLocale = ULocale.forLocale(this.locale);
                String region = uLocale.getCountry();
                if (region.length() == 0) {
                    region = ULocale.addLikelySubtags(uLocale).getCountry();
                }
                tzId = tzNames.getReferenceZoneID(bestMatch.mzID(), region);
            }
        }
        TimeZone newTimeZone = TimeZone.getTimeZone(tzId);
        if (!currentTimeZoneID.equals(tzId)) {
            setTimeZone(newTimeZone);
        }
        boolean isDst = DST_NAME_TYPES.contains(bestMatch.nameType());
        int dstAmount = isDst ? newTimeZone.getDSTSavings() : 0;
        if (!isDst || dstAmount != 0) {
            calb.clear(15).set(16, dstAmount);
        }
        return bestMatch.matchLength() + start;
    }

    private int subParseZoneStringFromSymbols(String text, int start, CalendarBuilder calb) {
        int nameIndex;
        String[] zoneNames;
        boolean useSameName = $assertionsDisabled;
        TimeZone currentTimeZone = getTimeZone();
        int zoneIndex = this.formatData.getZoneIndex(currentTimeZone.getID());
        TimeZone tz = null;
        String[][] zoneStrings = this.formatData.getZoneStringsWrapper();
        String[] zoneNames2 = null;
        int nameIndex2 = 0;
        int i = 0;
        if (zoneIndex != -1) {
            zoneNames2 = zoneStrings[zoneIndex];
            int matchZoneString = matchZoneString(text, start, zoneNames2);
            nameIndex2 = matchZoneString;
            if (matchZoneString > 0) {
                if (nameIndex2 <= 2) {
                    useSameName = zoneNames2[nameIndex2].equalsIgnoreCase(zoneNames2[nameIndex2 + 2]);
                }
                tz = TimeZone.getTimeZone(zoneNames2[0]);
            }
        }
        if (tz == null) {
            int zoneIndex2 = this.formatData.getZoneIndex(TimeZone.getDefault().getID());
            if (zoneIndex2 != -1) {
                zoneNames2 = zoneStrings[zoneIndex2];
                int matchZoneString2 = matchZoneString(text, start, zoneNames2);
                nameIndex2 = matchZoneString2;
                if (matchZoneString2 > 0) {
                    if (nameIndex2 <= 2) {
                        useSameName = zoneNames2[nameIndex2].equalsIgnoreCase(zoneNames2[nameIndex2 + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames2[0]);
                }
            }
        }
        if (tz == null) {
            int len = zoneStrings.length;
            nameIndex = nameIndex2;
            zoneNames = zoneNames2;
            int i2 = 0;
            while (true) {
                if (i2 >= len) {
                    break;
                }
                zoneNames = zoneStrings[i2];
                int matchZoneString3 = matchZoneString(text, start, zoneNames);
                nameIndex = matchZoneString3;
                if (matchZoneString3 > 0) {
                    if (nameIndex <= 2) {
                        useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames[0]);
                } else {
                    i2++;
                }
            }
        } else {
            nameIndex = nameIndex2;
            zoneNames = zoneNames2;
        }
        if (tz == null) {
            return -start;
        }
        if (!tz.equals(currentTimeZone)) {
            setTimeZone(tz);
        }
        if (nameIndex >= 3) {
            i = tz.getDSTSavings();
        }
        int dstAmount = i;
        if (!useSameName && (nameIndex < 3 || dstAmount != 0)) {
            calb.clear(15).set(16, dstAmount);
        }
        return zoneNames[nameIndex].length() + start;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0053  */
    private int subParseNumericZone(String text, int start, int sign, int count, boolean colonRequired, CalendarBuilder calb) {
        int index = start;
        int index2 = index + 1;
        try {
            char c = text.charAt(index);
            if (isDigit(c)) {
                int hours = c - '0';
                int index3 = index2 + 1;
                try {
                    if (isDigit(text.charAt(index2))) {
                        hours = (hours * 10) + (c - '0');
                    } else {
                        index3--;
                    }
                    index2 = index3;
                    if (hours <= 23) {
                        int minutes = 0;
                        if (count != 1) {
                            int index4 = index2 + 1;
                            try {
                                char c2 = text.charAt(index2);
                                if (c2 == ':') {
                                    index2 = index4 + 1;
                                    c2 = text.charAt(index4);
                                    if (isDigit(c2) != 0) {
                                        int minutes2 = c2 - '0';
                                        index4 = index2 + 1;
                                        if (isDigit(text.charAt(index2))) {
                                            minutes = (minutes2 * 10) + (c - '0');
                                            if (minutes <= 59) {
                                                index2 = index4;
                                            }
                                        }
                                    }
                                } else if (!colonRequired) {
                                    index2 = index4;
                                    if (isDigit(c2) != 0) {
                                    }
                                }
                                index2 = index4;
                            } catch (IndexOutOfBoundsException e) {
                                index2 = index4;
                            }
                        }
                        calb.set(15, MILLIS_PER_MINUTE * (minutes + (hours * 60)) * sign).set(16, 0);
                        return index2;
                    }
                } catch (IndexOutOfBoundsException e2) {
                    index2 = index3;
                }
            }
        } catch (IndexOutOfBoundsException e3) {
        }
        return 1 - index2;
    }

    private boolean isDigit(char c) {
        if (c < '0' || c > '9') {
            return $assertionsDisabled;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:133:0x025d, code lost:
        if (r9 <= 12) goto L_0x0262;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x0294, code lost:
        r27 = r9;
        r8 = r10;
        r4 = r15;
        r15 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x02cb, code lost:
        r27 = r9;
        r4 = r15;
        r26 = r21;
        r15 = r8;
        r8 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x03c6, code lost:
        r26 = r21;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x0450, code lost:
        if (r12.charAt(r4.index - 1) == r11.minusSign) goto L_0x0452;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007f, code lost:
        if ((r11.calendar instanceof java.util.GregorianCalendar) == false) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x011c, code lost:
        r8 = r37;
     */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0228 A[Catch:{ IndexOutOfBoundsException -> 0x0240 }] */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01a0  */
    private int subParse(String text, int start, int patternCharIndex, int count, boolean obeyCount, boolean[] ambiguousYear, ParsePosition origPos, boolean useFollowingMinusSignAsDelimiter, CalendarBuilder calb) {
        ParsePosition pos;
        int i;
        int patternCharIndex2;
        int value;
        ParsePosition pos2;
        CalendarBuilder calendarBuilder;
        int field;
        int value2;
        int value3;
        int field2;
        int value4;
        int value5;
        int patternCharIndex3;
        int field3;
        ParsePosition pos3;
        CalendarBuilder calendarBuilder2;
        int sign;
        int sign2;
        int sign3;
        int sign4;
        Number number;
        int width;
        int i2;
        Number number2;
        String str = text;
        int i3 = start;
        int i4 = count;
        ParsePosition parsePosition = origPos;
        CalendarBuilder calendarBuilder3 = calb;
        int value6 = 0;
        int field4 = 0;
        ParsePosition pos4 = new ParsePosition(0);
        pos4.index = i3;
        int i5 = patternCharIndex;
        if (i5 == 19 && !this.calendar.isWeekDateSupported()) {
            i5 = 1;
        }
        int patternCharIndex4 = i5;
        int field5 = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex4];
        while (true) {
            int field6 = field5;
            if (pos4.index < text.length()) {
                char c = str.charAt(pos4.index);
                if (c != ' ' && c != 9) {
                    if (patternCharIndex4 == 4 || patternCharIndex4 == 15 || ((patternCharIndex4 == 2 && i4 <= 2) || patternCharIndex4 == 1 || patternCharIndex4 == 19)) {
                        if (obeyCount) {
                            if (i3 + i4 <= text.length()) {
                                number2 = this.numberFormat.parse(str.substring(0, i3 + i4), pos4);
                            }
                            int i6 = field6;
                            int i7 = patternCharIndex4;
                            pos = pos4;
                            CalendarBuilder calendarBuilder4 = calendarBuilder3;
                            origPos.errorIndex = pos.index;
                            return -1;
                        }
                        number2 = this.numberFormat.parse(str, pos4);
                        if (number2 == null) {
                            if (patternCharIndex4 == 1) {
                            }
                            int i62 = field6;
                            int i72 = patternCharIndex4;
                            pos = pos4;
                            CalendarBuilder calendarBuilder42 = calendarBuilder3;
                            origPos.errorIndex = pos.index;
                            return -1;
                        }
                        value6 = number2.intValue();
                        if (useFollowingMinusSignAsDelimiter && value6 < 0 && ((pos4.index < text.length() && str.charAt(pos4.index) != this.minusSign) || (pos4.index == text.length() && str.charAt(pos4.index - 1) == this.minusSign))) {
                            value6 = -value6;
                            pos4.index--;
                        }
                    }
                    int value7 = value6;
                    boolean useDateFormatSymbols2 = useDateFormatSymbols();
                    switch (patternCharIndex4) {
                        case 0:
                            int field7 = field6;
                            patternCharIndex2 = patternCharIndex4;
                            ParsePosition parsePosition2 = pos4;
                            CalendarBuilder calendarBuilder5 = calendarBuilder3;
                            int value8 = value7;
                            if (useDateFormatSymbols2) {
                                int matchString = matchString(str, i3, 0, this.formatData.getEras(), calendarBuilder5);
                                int index = matchString;
                                if (matchString > 0) {
                                    return index;
                                }
                            } else {
                                int matchString2 = matchString(str, i3, field7, this.calendar.getDisplayNames(field7, 0, this.locale), calendarBuilder5);
                                int index2 = matchString2;
                                if (matchString2 > 0) {
                                    return index2;
                                }
                            }
                            i = value8;
                            pos = parsePosition2;
                            break;
                        case 1:
                        case 19:
                            int field8 = field6;
                            int i8 = patternCharIndex4;
                            int value9 = value7;
                            ParsePosition pos5 = pos4;
                            if (!(this.calendar instanceof GregorianCalendar)) {
                                Map<String, Integer> map = this.calendar.getDisplayNames(field8, i4 >= 4 ? 2 : 1, this.locale);
                                if (map != null) {
                                    int matchString3 = matchString(str, i3, field8, map, calb);
                                    int index3 = matchString3;
                                    if (matchString3 > 0) {
                                        return index3;
                                    }
                                }
                                calb.set(field8, value9);
                                return pos5.index;
                            }
                            CalendarBuilder calendarBuilder6 = calb;
                            ParsePosition pos6 = pos5;
                            int value10 = value9;
                            if (i4 > 2 || pos6.index - i3 != 2 || !Character.isDigit(text.charAt(start)) || !Character.isDigit(str.charAt(i3 + 1))) {
                                value = value10;
                            } else {
                                int i9 = this.defaultCenturyStartYear;
                                int i10 = TAG_QUOTE_ASCII_CHAR;
                                int ambiguousTwoDigitYear = i9 % TAG_QUOTE_ASCII_CHAR;
                                ambiguousYear[0] = value10 == ambiguousTwoDigitYear ? true : $assertionsDisabled;
                                int i11 = (this.defaultCenturyStartYear / TAG_QUOTE_ASCII_CHAR) * TAG_QUOTE_ASCII_CHAR;
                                if (value10 >= ambiguousTwoDigitYear) {
                                    i10 = 0;
                                }
                                value = value10 + i11 + i10;
                            }
                            calendarBuilder6.set(field8, value);
                            return pos6.index;
                        case 2:
                            int patternCharIndex5 = patternCharIndex4;
                            ParsePosition pos7 = pos4;
                            CalendarBuilder calendarBuilder7 = calendarBuilder3;
                            int field9 = field6;
                            int value11 = value7;
                            ParsePosition parsePosition3 = pos7;
                            ParsePosition pos8 = pos7;
                            int i12 = field9;
                            int value12 = value11;
                            int idx = parseMonth(str, i4, value11, i3, field9, parsePosition3, useDateFormatSymbols2, $assertionsDisabled, calb);
                            if (idx <= 0) {
                                CalendarBuilder calendarBuilder8 = calb;
                                int i13 = patternCharIndex5;
                                pos = pos8;
                                i = value12;
                                break;
                            } else {
                                return idx;
                            }
                        case 4:
                            patternCharIndex2 = patternCharIndex4;
                            pos2 = pos4;
                            calendarBuilder = calendarBuilder3;
                            field = field6;
                            value2 = value7;
                            if (isLenient() || (value2 >= 1 && value2 <= 24)) {
                                if (value2 == this.calendar.getMaximum(11) + 1) {
                                    value3 = 0;
                                } else {
                                    value3 = value2;
                                }
                                calendarBuilder.set(11, value3);
                                return pos2.index;
                            }
                        case 9:
                            patternCharIndex2 = patternCharIndex4;
                            pos2 = pos4;
                            calendarBuilder = calendarBuilder3;
                            field = field6;
                            value2 = value7;
                            int idx2 = parseWeekday(str, i3, field, useDateFormatSymbols2, $assertionsDisabled, calendarBuilder);
                            if (idx2 > 0) {
                                return idx2;
                            }
                            break;
                        case 14:
                            field2 = field6;
                            patternCharIndex2 = patternCharIndex4;
                            pos2 = pos4;
                            calendarBuilder = calendarBuilder3;
                            value2 = value7;
                            if (useDateFormatSymbols2) {
                                int matchString4 = matchString(str, i3, 9, this.formatData.getAmPmStrings(), calendarBuilder);
                                int index4 = matchString4;
                                if (matchString4 > 0) {
                                    return index4;
                                }
                            } else {
                                field = field2;
                                int matchString5 = matchString(str, i3, field, this.calendar.getDisplayNames(field, 0, this.locale), calendarBuilder);
                                int index5 = matchString5;
                                if (matchString5 > 0) {
                                    return index5;
                                }
                            }
                            break;
                        case 15:
                            field2 = field6;
                            patternCharIndex2 = patternCharIndex4;
                            int value13 = value7;
                            pos2 = pos4;
                            calendarBuilder = calendarBuilder3;
                            if (!isLenient()) {
                                value2 = value13;
                                if (value2 >= 1) {
                                    break;
                                }
                            } else {
                                value2 = value13;
                            }
                            if (value2 == this.calendar.getLeastMaximum(10) + 1) {
                                value4 = 0;
                            } else {
                                value4 = value2;
                            }
                            calendarBuilder.set(10, value4);
                            return pos2.index;
                        case 17:
                        case 18:
                            field3 = field6;
                            patternCharIndex3 = patternCharIndex4;
                            value5 = value7;
                            pos3 = pos4;
                            calendarBuilder2 = calendarBuilder3;
                            try {
                                char c2 = str.charAt(pos3.index);
                                if (c2 != '+') {
                                    if (c2 != '-') {
                                        sign = 0;
                                        if (sign == 0) {
                                            int i14 = pos3.index + 1;
                                            pos3.index = i14;
                                            int i15 = subParseNumericZone(str, i14, sign, 0, $assertionsDisabled, calendarBuilder2);
                                            if (i15 <= 0) {
                                                pos3.index = -i15;
                                                break;
                                            } else {
                                                return i15;
                                            }
                                        } else {
                                            if (c2 == 'G' || c2 == 'g') {
                                                try {
                                                    if (text.length() - i3 >= GMT.length()) {
                                                        if (str.regionMatches(true, i3, GMT, 0, GMT.length())) {
                                                            pos3.index = GMT.length() + i3;
                                                            if (text.length() - pos3.index > 0) {
                                                                char c3 = str.charAt(pos3.index);
                                                                if (c3 == '+') {
                                                                    sign2 = 1;
                                                                } else if (c3 == '-') {
                                                                    sign2 = -1;
                                                                }
                                                                sign = sign2;
                                                            }
                                                            if (sign != 0) {
                                                                int i16 = pos3.index + 1;
                                                                pos3.index = i16;
                                                                int i17 = subParseNumericZone(str, i16, sign, 0, $assertionsDisabled, calendarBuilder2);
                                                                if (i17 <= 0) {
                                                                    pos3.index = -i17;
                                                                    break;
                                                                } else {
                                                                    return i17;
                                                                }
                                                            } else {
                                                                calendarBuilder2.set(15, 0).set(16, 0);
                                                                return pos3.index;
                                                            }
                                                        }
                                                    }
                                                } catch (IndexOutOfBoundsException e) {
                                                    break;
                                                }
                                            }
                                            int i18 = subParseZoneString(str, pos3.index, calendarBuilder2);
                                            if (i18 <= 0) {
                                                pos3.index = -i18;
                                                break;
                                            } else {
                                                return i18;
                                            }
                                        }
                                    } else {
                                        sign3 = -1;
                                    }
                                } else {
                                    sign3 = 1;
                                }
                                sign = sign3;
                                if (sign == 0) {
                                }
                            } catch (IndexOutOfBoundsException e2) {
                                break;
                            }
                        case 21:
                            field3 = field6;
                            patternCharIndex3 = patternCharIndex4;
                            value5 = value7;
                            pos3 = pos4;
                            if (text.length() - pos3.index > 0) {
                                char c4 = str.charAt(pos3.index);
                                if (c4 != 'Z') {
                                    calendarBuilder2 = calb;
                                    if (c4 != '+') {
                                        if (c4 != '-') {
                                            pos3.index++;
                                            break;
                                        } else {
                                            sign4 = -1;
                                        }
                                    } else {
                                        sign4 = 1;
                                    }
                                    int sign5 = sign4;
                                    int i19 = pos3.index + 1;
                                    pos3.index = i19;
                                    int i20 = subParseNumericZone(str, i19, sign5, i4, i4 == 3 ? true : $assertionsDisabled, calendarBuilder2);
                                    if (i20 <= 0) {
                                        pos3.index = -i20;
                                        break;
                                    } else {
                                        return i20;
                                    }
                                } else {
                                    calb.set(15, 0).set(16, 0);
                                    int i21 = pos3.index + 1;
                                    pos3.index = i21;
                                    return i21;
                                }
                            }
                            break;
                        case 22:
                            field3 = field6;
                            patternCharIndex3 = patternCharIndex4;
                            value5 = value7;
                            pos3 = pos4;
                            int idx3 = parseMonth(str, i4, value5, i3, field3, pos4, useDateFormatSymbols2, true, calb);
                            if (idx3 > 0) {
                                return idx3;
                            }
                            break;
                        case 23:
                            field3 = field6;
                            patternCharIndex3 = patternCharIndex4;
                            value5 = value7;
                            int idx4 = parseWeekday(str, i3, field6, useDateFormatSymbols2, true, calendarBuilder3);
                            if (idx4 <= 0) {
                                pos = pos4;
                                CalendarBuilder calendarBuilder9 = calendarBuilder3;
                                break;
                            } else {
                                return idx4;
                            }
                        default:
                            int field10 = field6;
                            patternCharIndex2 = patternCharIndex4;
                            ParsePosition pos9 = pos4;
                            CalendarBuilder calendarBuilder10 = calendarBuilder3;
                            int value14 = value7;
                            int parseStart = pos9.getIndex();
                            pos = pos9;
                            if (!obeyCount) {
                                number = this.numberFormat.parse(str, pos);
                            } else if (i3 + i4 > text.length()) {
                                i = value14;
                                break;
                            } else {
                                number = this.numberFormat.parse(str.substring(0, i3 + i4), pos);
                            }
                            if (number == null) {
                                i = value14;
                                int i22 = patternCharIndex2;
                                break;
                            } else {
                                int patternCharIndex6 = patternCharIndex2;
                                if (patternCharIndex6 == 8) {
                                    int i23 = patternCharIndex6;
                                    int i24 = value14;
                                    width = (int) ((number.doubleValue() / Math.pow(10.0d, (double) (pos.getIndex() - parseStart))) * 1000.0d);
                                } else {
                                    int i25 = value14;
                                    width = number.intValue();
                                }
                                if (useFollowingMinusSignAsDelimiter && width < 0) {
                                    if (pos.index >= text.length() || str.charAt(pos.index) == this.minusSign) {
                                        if (pos.index == text.length()) {
                                            i2 = 1;
                                            break;
                                        }
                                    } else {
                                        i2 = 1;
                                    }
                                    width = -width;
                                    pos.index -= i2;
                                }
                                calendarBuilder10.set(field10, width);
                                return pos.index;
                            }
                    }
                } else {
                    ParsePosition pos10 = pos4;
                    pos10.index++;
                    field4 = field4;
                    parsePosition = parsePosition;
                    calendarBuilder3 = calendarBuilder3;
                    field5 = field6;
                    patternCharIndex4 = patternCharIndex4;
                    pos4 = pos10;
                }
            } else {
                parsePosition.errorIndex = i3;
                return -1;
            }
        }
        CalendarBuilder calendarBuilder11 = calendarBuilder2;
        pos = pos3;
        int i26 = patternCharIndex3;
        i = value5;
        int i27 = i;
        origPos.errorIndex = pos.index;
        return -1;
    }

    private int parseMonth(String text, int count, int value, int start, int field, ParsePosition pos, boolean useDateFormatSymbols2, boolean standalone, CalendarBuilder out) {
        int index;
        if (count <= 2) {
            out.set(2, value - 1);
            return pos.index;
        }
        ParsePosition parsePosition = pos;
        CalendarBuilder calendarBuilder = out;
        if (useDateFormatSymbols2) {
            int matchString = matchString(text, start, 2, standalone ? this.formatData.getStandAloneMonths() : this.formatData.getMonths(), calendarBuilder);
            int index2 = matchString;
            if (matchString > 0) {
                return index2;
            }
            int matchString2 = matchString(text, start, 2, standalone ? this.formatData.getShortStandAloneMonths() : this.formatData.getShortMonths(), calendarBuilder);
            index = matchString2;
            if (matchString2 > 0) {
                return index;
            }
            int i = field;
        } else {
            int i2 = field;
            int matchString3 = matchString(text, start, i2, this.calendar.getDisplayNames(i2, 0, this.locale), calendarBuilder);
            index = matchString3;
            if (matchString3 > 0) {
                return index;
            }
        }
        return index;
    }

    private int parseWeekday(String text, int start, int field, boolean useDateFormatSymbols2, boolean standalone, CalendarBuilder out) {
        int index;
        if (useDateFormatSymbols2) {
            int matchString = matchString(text, start, 7, standalone ? this.formatData.getStandAloneWeekdays() : this.formatData.getWeekdays(), out);
            int index2 = matchString;
            if (matchString > 0) {
                return index2;
            }
            int matchString2 = matchString(text, start, 7, standalone ? this.formatData.getShortStandAloneWeekdays() : this.formatData.getShortWeekdays(), out);
            int index3 = matchString2;
            if (matchString2 > 0) {
                return index3;
            }
            int i = field;
            index = index3;
        } else {
            index = -1;
            for (int style : new int[]{2, 1}) {
                int i2 = field;
                int matchString3 = matchString(text, start, i2, this.calendar.getDisplayNames(i2, style, this.locale), out);
                index = matchString3;
                if (matchString3 > 0) {
                    return index;
                }
            }
            int i3 = field;
        }
        return index;
    }

    private final String getCalendarName() {
        return this.calendar.getClass().getName();
    }

    private boolean useDateFormatSymbols() {
        boolean z = true;
        if (this.useDateFormatSymbols) {
            return true;
        }
        if (!isGregorianCalendar() && this.locale != null) {
            z = $assertionsDisabled;
        }
        return z;
    }

    private boolean isGregorianCalendar() {
        return "java.util.GregorianCalendar".equals(getCalendarName());
    }

    private String translatePattern(String pattern2, String from, String to) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = $assertionsDisabled;
        for (int i = 0; i < pattern2.length(); i++) {
            char c = pattern2.charAt(i);
            if (inQuote) {
                if (c == '\'') {
                    inQuote = $assertionsDisabled;
                }
            } else if (c == '\'') {
                inQuote = true;
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                int ci = from.indexOf((int) c);
                if (ci < 0) {
                    throw new IllegalArgumentException("Illegal pattern  character '" + c + "'");
                } else if (ci < to.length()) {
                    c = to.charAt(ci);
                }
            }
            result.append(c);
        }
        if (!inQuote) {
            return result.toString();
        }
        throw new IllegalArgumentException("Unfinished quote in pattern");
    }

    public String toPattern() {
        return this.pattern;
    }

    public String toLocalizedPattern() {
        return translatePattern(this.pattern, "GyMdkHmsSEDFwWahKzZYuXLcbB", this.formatData.getLocalPatternChars());
    }

    public void applyPattern(String pattern2) {
        this.compiledPattern = compile(pattern2);
        this.pattern = pattern2;
    }

    public void applyLocalizedPattern(String pattern2) {
        String p = translatePattern(pattern2, this.formatData.getLocalPatternChars(), "GyMdkHmsSEDFwWahKzZYuXLcbB");
        this.compiledPattern = compile(p);
        this.pattern = p;
    }

    public DateFormatSymbols getDateFormatSymbols() {
        return (DateFormatSymbols) this.formatData.clone();
    }

    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        this.formatData = (DateFormatSymbols) newFormatSymbols.clone();
        this.useDateFormatSymbols = true;
    }

    public Object clone() {
        SimpleDateFormat other = (SimpleDateFormat) super.clone();
        other.formatData = (DateFormatSymbols) this.formatData.clone();
        return other;
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        boolean z = $assertionsDisabled;
        if (!equals) {
            return $assertionsDisabled;
        }
        SimpleDateFormat that = (SimpleDateFormat) obj;
        if (this.pattern.equals(that.pattern) && this.formatData.equals(that.formatData)) {
            z = true;
        }
        return z;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        try {
            this.compiledPattern = compile(this.pattern);
            if (this.serialVersionOnStream < 1) {
                initializeDefaultCentury();
            } else {
                parseAmbiguousDatesAsAfter(this.defaultCenturyStart);
            }
            this.serialVersionOnStream = 1;
            TimeZone tz = getTimeZone();
            if (tz instanceof SimpleTimeZone) {
                String id = tz.getID();
                TimeZone zi = TimeZone.getTimeZone(id);
                if (zi != null && zi.hasSameRules(tz) && zi.getID().equals(id)) {
                    setTimeZone(zi);
                }
            }
        } catch (Exception e) {
            throw new InvalidObjectException("invalid pattern");
        }
    }

    private void checkNegativeNumberExpression() {
        if ((this.numberFormat instanceof DecimalFormat) && !this.numberFormat.equals(this.originalNumberFormat)) {
            String numberPattern = ((DecimalFormat) this.numberFormat).toPattern();
            if (!numberPattern.equals(this.originalNumberPattern)) {
                this.hasFollowingMinusSign = $assertionsDisabled;
                int separatorIndex = numberPattern.indexOf(59);
                if (separatorIndex > -1) {
                    int minusIndex = numberPattern.indexOf(45, separatorIndex);
                    if (minusIndex > numberPattern.lastIndexOf(48) && minusIndex > numberPattern.lastIndexOf(35)) {
                        this.hasFollowingMinusSign = true;
                        this.minusSign = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getMinusSign();
                    }
                }
                this.originalNumberPattern = numberPattern;
            }
            this.originalNumberFormat = this.numberFormat;
        }
    }
}
