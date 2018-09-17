package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.security.SecureRandom;
import java.text.DateFormat.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;
import libcore.icu.LocaleData;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarUtils;
import sun.util.logging.PlatformLogger;

public class SimpleDateFormat extends DateFormat {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final String GMT = "GMT";
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD = null;
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = null;
    private static final Field[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID = null;
    private static final int TAG_QUOTE_ASCII_CHAR = 100;
    private static final int TAG_QUOTE_CHARS = 101;
    private static final ConcurrentMap<Locale, String[]> cachedLocaleData = null;
    private static final ConcurrentMap<Locale, NumberFormat> cachedNumberFormatData = null;
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
    transient boolean useDateFormatSymbols;
    private transient char zeroDigit;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.SimpleDateFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.SimpleDateFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.text.SimpleDateFormat.<clinit>():void");
    }

    public SimpleDateFormat() {
        this(3, 3, Locale.getDefault(Category.FORMAT));
    }

    public SimpleDateFormat(String pattern) {
        this(pattern, Locale.getDefault(Category.FORMAT));
    }

    public SimpleDateFormat(String pattern, Locale locale) {
        this.serialVersionOnStream = currentSerialVersion;
        this.minusSign = '-';
        this.hasFollowingMinusSign = -assertionsDisabled;
        if (pattern == null || locale == null) {
            throw new NullPointerException();
        }
        initializeCalendar(locale);
        this.pattern = pattern;
        this.formatData = DateFormatSymbols.getInstanceRef(locale);
        this.locale = locale;
        initialize(locale);
    }

    public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
        this.serialVersionOnStream = currentSerialVersion;
        this.minusSign = '-';
        this.hasFollowingMinusSign = -assertionsDisabled;
        if (pattern == null || formatSymbols == null) {
            throw new NullPointerException();
        }
        this.pattern = pattern;
        this.formatData = (DateFormatSymbols) formatSymbols.clone();
        this.locale = Locale.getDefault(Category.FORMAT);
        initializeCalendar(this.locale);
        initialize(this.locale);
        this.useDateFormatSymbols = true;
    }

    SimpleDateFormat(int timeStyle, int dateStyle, Locale loc) {
        this.serialVersionOnStream = currentSerialVersion;
        this.minusSign = '-';
        this.hasFollowingMinusSign = -assertionsDisabled;
        if (loc == null) {
            throw new NullPointerException();
        }
        this.locale = loc;
        initializeCalendar(loc);
        String[] dateTimePatterns = (String[]) cachedLocaleData.get(loc);
        if (dateTimePatterns == null) {
            LocaleData localeData = LocaleData.get(loc);
            dateTimePatterns = new String[]{localeData.getDateFormat(3), localeData.getDateFormat(2), localeData.getDateFormat(currentSerialVersion), localeData.getDateFormat(0), localeData.getTimeFormat(3), localeData.getTimeFormat(2), localeData.getTimeFormat(currentSerialVersion), localeData.getTimeFormat(0), "{0} {1}"};
            cachedLocaleData.putIfAbsent(loc, dateTimePatterns);
        }
        this.formatData = DateFormatSymbols.getInstanceRef(loc);
        if (timeStyle >= 0 && dateStyle >= 0) {
            this.pattern = MessageFormat.format(dateTimePatterns[8], dateTimePatterns[dateStyle + 4], dateTimePatterns[timeStyle]);
        } else if (timeStyle >= 0) {
            this.pattern = dateTimePatterns[timeStyle];
        } else if (dateStyle >= 0) {
            this.pattern = dateTimePatterns[dateStyle + 4];
        } else {
            throw new IllegalArgumentException("No date or time style specified");
        }
        initialize(loc);
    }

    private void initialize(Locale loc) {
        this.compiledPattern = compile(this.pattern);
        this.numberFormat = (NumberFormat) cachedNumberFormatData.get(loc);
        if (this.numberFormat == null) {
            this.numberFormat = NumberFormat.getIntegerInstance(loc);
            this.numberFormat.setGroupingUsed(-assertionsDisabled);
            cachedNumberFormatData.putIfAbsent(loc, this.numberFormat);
        }
        this.numberFormat = (NumberFormat) this.numberFormat.clone();
        initializeDefaultCentury();
    }

    private void initializeCalendar(Locale loc) {
        if (this.calendar == null) {
            if (!-assertionsDisabled) {
                if ((loc != null ? currentSerialVersion : null) == null) {
                    throw new AssertionError();
                }
            }
            this.calendar = Calendar.getInstance(TimeZone.getDefault(), loc);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private char[] compile(String pattern) {
        int length = pattern.length();
        boolean inQuote = -assertionsDisabled;
        StringBuilder compiledPattern = new StringBuilder(length * 2);
        CharSequence tmpBuffer = null;
        int count = 0;
        int lastTag = -1;
        int i = 0;
        while (i < length) {
            int len;
            char c = pattern.charAt(i);
            if (c == '\'') {
                if (i + currentSerialVersion < length) {
                    c = pattern.charAt(i + currentSerialVersion);
                    if (c == '\'') {
                        i += currentSerialVersion;
                        if (count != 0) {
                            encode(lastTag, count, compiledPattern);
                            lastTag = -1;
                            count = 0;
                        }
                        if (inQuote) {
                            tmpBuffer.append(c);
                        } else {
                            compiledPattern.append((char) (c | 25600));
                        }
                    }
                }
                if (inQuote) {
                    len = tmpBuffer.length();
                    if (len == currentSerialVersion) {
                        char ch = tmpBuffer.charAt(0);
                        if (ch < '\u0080') {
                            compiledPattern.append((char) (ch | 25600));
                        } else {
                            compiledPattern.append('\u6501');
                            compiledPattern.append(ch);
                        }
                    } else {
                        encode(TAG_QUOTE_CHARS, len, compiledPattern);
                        compiledPattern.append(tmpBuffer);
                    }
                    inQuote = -assertionsDisabled;
                } else {
                    if (count != 0) {
                        encode(lastTag, count, compiledPattern);
                        lastTag = -1;
                        count = 0;
                    }
                    if (tmpBuffer == null) {
                        tmpBuffer = new StringBuilder(length);
                    } else {
                        tmpBuffer.setLength(0);
                    }
                    inQuote = true;
                }
            } else if (inQuote) {
                tmpBuffer.append(c);
            } else if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
                if (count != 0) {
                    encode(lastTag, count, compiledPattern);
                    lastTag = -1;
                    count = 0;
                }
                if (c < '\u0080') {
                    compiledPattern.append((char) (c | 25600));
                } else {
                    int j = i + currentSerialVersion;
                    while (j < length) {
                        char d = pattern.charAt(j);
                        if (d != '\'' && ((d < 'a' || d > 'z') && (d < 'A' || d > 'Z'))) {
                            j += currentSerialVersion;
                        }
                    }
                    compiledPattern.append((char) ((j - i) | 25856));
                    while (i < j) {
                        compiledPattern.append(pattern.charAt(i));
                        i += currentSerialVersion;
                    }
                    i--;
                }
            } else {
                int tag = "GyMdkHmsSEDFwWahKzZYuXLc".indexOf((int) c);
                if (tag == -1) {
                    throw new IllegalArgumentException("Illegal pattern character '" + c + "'");
                } else if (lastTag == -1 || lastTag == tag) {
                    lastTag = tag;
                    count += currentSerialVersion;
                } else {
                    encode(lastTag, count, compiledPattern);
                    lastTag = tag;
                    count = currentSerialVersion;
                }
            }
            i += currentSerialVersion;
        }
        if (inQuote) {
            throw new IllegalArgumentException("Unterminated quote");
        }
        if (count != 0) {
            encode(lastTag, count, compiledPattern);
        }
        len = compiledPattern.length();
        char[] r = new char[len];
        compiledPattern.getChars(0, len, r, 0);
        return r;
    }

    private static final void encode(int tag, int length, StringBuilder buffer) {
        if (tag == 21 && length >= 4) {
            throw new IllegalArgumentException("invalid ISO 8601 format: length=" + length);
        } else if (length < 255) {
            buffer.append((char) ((tag << 8) | length));
        } else {
            buffer.append((char) ((tag << 8) | 255));
            buffer.append((char) (length >>> 16));
            buffer.append((char) (65535 & length));
        }
    }

    private void initializeDefaultCentury() {
        this.calendar.setTimeInMillis(System.currentTimeMillis());
        this.calendar.add(currentSerialVersion, -80);
        parseAmbiguousDatesAsAfter(this.calendar.getTime());
    }

    private void parseAmbiguousDatesAsAfter(Date startDate) {
        this.defaultCenturyStart = startDate;
        this.calendar.setTime(startDate);
        this.defaultCenturyStartYear = this.calendar.get(currentSerialVersion);
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

    private StringBuffer format(Date date, StringBuffer toAppendTo, FieldDelegate delegate) {
        this.calendar.setTime(date);
        boolean useDateFormatSymbols = useDateFormatSymbols();
        int i = 0;
        while (i < this.compiledPattern.length) {
            int tag = this.compiledPattern[i] >>> 8;
            int i2 = i + currentSerialVersion;
            int count = this.compiledPattern[i] & 255;
            if (count == 255) {
                i = i2 + currentSerialVersion;
                count = (this.compiledPattern[i2] << 16) | this.compiledPattern[i];
                i += currentSerialVersion;
            } else {
                i = i2;
            }
            switch (tag) {
                case TAG_QUOTE_ASCII_CHAR /*100*/:
                    toAppendTo.append((char) count);
                    break;
                case TAG_QUOTE_CHARS /*101*/:
                    toAppendTo.append(this.compiledPattern, i, count);
                    i += count;
                    break;
                default:
                    subFormat(tag, count, delegate, toAppendTo, useDateFormatSymbols);
                    break;
            }
        }
        return toAppendTo;
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer sb = new StringBuffer();
        FieldDelegate delegate = new CharacterIteratorFieldDelegate();
        if (obj instanceof Date) {
            format((Date) obj, sb, delegate);
        } else if (obj instanceof Number) {
            format(new Date(((Number) obj).longValue()), sb, delegate);
        } else if (obj == null) {
            throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Date");
        }
        return delegate.getIterator(sb.toString());
    }

    private void subFormat(int patternCharIndex, int count, FieldDelegate delegate, StringBuffer buffer, boolean useDateFormatSymbols) {
        int value;
        String current = null;
        int beginOffset = buffer.length();
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        if (field == 17) {
            if (this.calendar.isWeekDateSupported()) {
                value = this.calendar.getWeekYear();
            } else {
                patternCharIndex = currentSerialVersion;
                field = PATTERN_INDEX_TO_CALENDAR_FIELD[currentSerialVersion];
                value = this.calendar.get(field);
            }
        } else if (field == 1000) {
            value = CalendarBuilder.toISODayOfWeek(this.calendar.get(7));
        } else {
            value = this.calendar.get(field);
        }
        int style = count >= 4 ? 2 : currentSerialVersion;
        if (!(useDateFormatSymbols || field == 1000)) {
            current = this.calendar.getDisplayName(field, style, this.locale);
        }
        switch (patternCharIndex) {
            case GeneralNameInterface.NAME_MATCH /*0*/:
                if (useDateFormatSymbols) {
                    String[] eras = this.formatData.getEras();
                    if (value < eras.length) {
                        current = eras[value];
                    }
                }
                if (current == null) {
                    current = "";
                    break;
                }
                break;
            case currentSerialVersion /*1*/:
            case DigitList.MAX_COUNT /*19*/:
                if (!(this.calendar instanceof GregorianCalendar)) {
                    if (current == null) {
                        if (style == 2) {
                            count = currentSerialVersion;
                        }
                        zeroPaddingNumber(value, count, PlatformLogger.OFF, buffer);
                        break;
                    }
                } else if (count == 2) {
                    zeroPaddingNumber(value, 2, 2, buffer);
                    break;
                } else {
                    zeroPaddingNumber(value, count, PlatformLogger.OFF, buffer);
                    break;
                }
                break;
            case BaseCalendar.MONDAY /*2*/:
                current = formatMonth(count, value, PlatformLogger.OFF, buffer, useDateFormatSymbols, -assertionsDisabled);
                break;
            case BaseCalendar.WEDNESDAY /*4*/:
                if (current == null) {
                    if (value != 0) {
                        zeroPaddingNumber(value, count, PlatformLogger.OFF, buffer);
                        break;
                    } else {
                        zeroPaddingNumber(this.calendar.getMaximum(11) + currentSerialVersion, count, PlatformLogger.OFF, buffer);
                        break;
                    }
                }
                break;
            case BaseCalendar.AUGUST /*8*/:
                if (current == null) {
                    zeroPaddingNumber((int) ((((double) value) / 1000.0d) * Math.pow(10.0d, (double) count)), count, count, buffer);
                    break;
                }
                break;
            case BaseCalendar.SEPTEMBER /*9*/:
                current = formatWeekday(count, value, useDateFormatSymbols, -assertionsDisabled);
                break;
            case ZipConstants.LOCCRC /*14*/:
                if (useDateFormatSymbols) {
                    current = this.formatData.getAmPmStrings()[value];
                    break;
                }
                break;
            case Calendar.ZONE_OFFSET /*15*/:
                if (current == null) {
                    if (value != 0) {
                        zeroPaddingNumber(value, count, PlatformLogger.OFF, buffer);
                        break;
                    } else {
                        zeroPaddingNumber(this.calendar.getLeastMaximum(10) + currentSerialVersion, count, PlatformLogger.OFF, buffer);
                        break;
                    }
                }
                break;
            case Calendar.FIELD_COUNT /*17*/:
                if (current == null) {
                    String zoneString = this.calendar.getTimeZone().getDisplayName(this.calendar.get(16) != 0 ? true : -assertionsDisabled, count < 4 ? 0 : currentSerialVersion, this.formatData.locale);
                    if (zoneString == null) {
                        buffer.append(TimeZone.createGmtOffsetString(true, true, this.calendar.get(15) + this.calendar.get(16)));
                        break;
                    } else {
                        buffer.append(zoneString);
                        break;
                    }
                }
                break;
            case ZipConstants.LOCSIZ /*18*/:
                value = this.calendar.get(15) + this.calendar.get(16);
                buffer.append(TimeZone.createGmtOffsetString(count == 4 ? true : -assertionsDisabled, count >= 4 ? true : -assertionsDisabled, value));
                break;
            case 21:
                value = this.calendar.get(15) + this.calendar.get(16);
                if (value != 0) {
                    value /= MILLIS_PER_MINUTE;
                    if (value >= 0) {
                        buffer.append('+');
                    } else {
                        buffer.append('-');
                        value = -value;
                    }
                    CalendarUtils.sprintf0d(buffer, value / 60, 2);
                    if (count != currentSerialVersion) {
                        if (count == 3) {
                            buffer.append(':');
                        }
                        CalendarUtils.sprintf0d(buffer, value % 60, 2);
                        break;
                    }
                }
                buffer.append('Z');
                break;
                break;
            case ZipConstants.LOCLEN /*22*/:
                current = formatMonth(count, value, PlatformLogger.OFF, buffer, useDateFormatSymbols, true);
                break;
            case SecureRandom.DEFAULT_SDK_TARGET_FOR_CRYPTO_PROVIDER_WORKAROUND /*23*/:
                current = formatWeekday(count, value, useDateFormatSymbols, true);
                break;
            default:
                if (current == null) {
                    zeroPaddingNumber(value, count, PlatformLogger.OFF, buffer);
                    break;
                }
                break;
        }
        if (current != null) {
            buffer.append(current);
        }
        int fieldID = PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex];
        Field f = PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID[patternCharIndex];
        delegate.formatted(fieldID, f, f, beginOffset, buffer.length(), buffer);
    }

    private String formatWeekday(int count, int value, boolean useDateFormatSymbols, boolean standalone) {
        if (!useDateFormatSymbols) {
            return null;
        }
        String[] weekdays = count == 4 ? standalone ? this.formatData.getStandAloneWeekdays() : this.formatData.getWeekdays() : count == 5 ? standalone ? this.formatData.getTinyStandAloneWeekdays() : this.formatData.getTinyWeekdays() : standalone ? this.formatData.getShortStandAloneWeekdays() : this.formatData.getShortWeekdays();
        return weekdays[value];
    }

    private String formatMonth(int count, int value, int maxIntCount, StringBuffer buffer, boolean useDateFormatSymbols, boolean standalone) {
        String current = null;
        if (useDateFormatSymbols) {
            String[] standAloneMonths = count == 4 ? standalone ? this.formatData.getStandAloneMonths() : this.formatData.getMonths() : count == 5 ? standalone ? this.formatData.getTinyStandAloneMonths() : this.formatData.getTinyMonths() : count == 3 ? standalone ? this.formatData.getShortStandAloneMonths() : this.formatData.getShortMonths() : null;
            if (standAloneMonths != null) {
                current = standAloneMonths[value];
            }
        } else if (count < 3) {
            current = null;
        }
        if (current == null) {
            zeroPaddingNumber(value + currentSerialVersion, count, maxIntCount, buffer);
        }
        return current;
    }

    private final void zeroPaddingNumber(int value, int minDigits, int maxDigits, StringBuffer buffer) {
        try {
            if (this.zeroDigit == '\u0000') {
                this.zeroDigit = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getZeroDigit();
            }
            if (value >= 0) {
                if (value < TAG_QUOTE_ASCII_CHAR && minDigits >= currentSerialVersion && minDigits <= 2) {
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
                } else if (value >= PlatformLogger.SEVERE && value < 10000) {
                    if (minDigits == 4) {
                        buffer.append((char) (this.zeroDigit + (value / PlatformLogger.SEVERE)));
                        value %= PlatformLogger.SEVERE;
                        buffer.append((char) (this.zeroDigit + (value / TAG_QUOTE_ASCII_CHAR)));
                        value %= TAG_QUOTE_ASCII_CHAR;
                        buffer.append((char) (this.zeroDigit + (value / 10)));
                        buffer.append((char) (this.zeroDigit + (value % 10)));
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
            Date parseInternal = parseInternal(text, pos);
            return parseInternal;
        } finally {
            setTimeZone(tz);
        }
    }

    private Date parseInternal(String text, ParsePosition pos) {
        checkNegativeNumberExpression();
        int start = pos.index;
        int oldStart = start;
        int textLength = text.length();
        boolean[] ambiguousYear = new boolean[currentSerialVersion];
        ambiguousYear[0] = -assertionsDisabled;
        CalendarBuilder calb = new CalendarBuilder();
        int i = 0;
        while (i < this.compiledPattern.length) {
            int tag = this.compiledPattern[i] >>> 8;
            int i2 = i + currentSerialVersion;
            int count = this.compiledPattern[i] & 255;
            if (count == 255) {
                i = i2 + currentSerialVersion;
                count = (this.compiledPattern[i2] << 16) | this.compiledPattern[i];
                i += currentSerialVersion;
            } else {
                i = i2;
            }
            switch (tag) {
                case TAG_QUOTE_ASCII_CHAR /*100*/:
                    if (start < textLength && text.charAt(start) == ((char) count)) {
                        start += currentSerialVersion;
                        break;
                    }
                    pos.index = oldStart;
                    pos.errorIndex = start;
                    return null;
                    break;
                case TAG_QUOTE_CHARS /*101*/:
                    int count2 = count;
                    i2 = i;
                    while (true) {
                        count = count2 - 1;
                        if (count2 <= 0) {
                            i = i2;
                            break;
                        }
                        if (start < textLength) {
                            i = i2 + currentSerialVersion;
                            if (text.charAt(start) == this.compiledPattern[i2]) {
                                start += currentSerialVersion;
                                count2 = count;
                                i2 = i;
                            }
                        }
                        pos.index = oldStart;
                        pos.errorIndex = start;
                        return null;
                    }
                default:
                    boolean obeyCount = -assertionsDisabled;
                    boolean useFollowingMinusSignAsDelimiter = -assertionsDisabled;
                    if (i < this.compiledPattern.length) {
                        int nextTag = this.compiledPattern[i] >>> 8;
                        if (!(nextTag == TAG_QUOTE_ASCII_CHAR || nextTag == TAG_QUOTE_CHARS)) {
                            obeyCount = true;
                        }
                        if (this.hasFollowingMinusSign && (nextTag == TAG_QUOTE_ASCII_CHAR || nextTag == TAG_QUOTE_CHARS)) {
                            char c;
                            if (nextTag == TAG_QUOTE_ASCII_CHAR) {
                                c = this.compiledPattern[i] & 255;
                            } else {
                                c = this.compiledPattern[i + currentSerialVersion];
                            }
                            if (c == this.minusSign) {
                                useFollowingMinusSignAsDelimiter = true;
                            }
                        }
                    }
                    start = subParse(text, start, tag, count, obeyCount, ambiguousYear, pos, useFollowingMinusSignAsDelimiter, calb);
                    if (start >= 0) {
                        break;
                    }
                    pos.index = oldStart;
                    return null;
            }
        }
        pos.index = start;
        try {
            Date parsedDate = calb.establish(this.calendar).getTime();
            if (ambiguousYear[0]) {
                if (parsedDate.before(this.defaultCenturyStart)) {
                    parsedDate = calb.addYear(TAG_QUOTE_ASCII_CHAR).establish(this.calendar).getTime();
                }
            }
            return parsedDate;
        } catch (IllegalArgumentException e) {
            pos.errorIndex = start;
            pos.index = oldStart;
            return null;
        }
    }

    private int matchString(String text, int start, int field, String[] data, CalendarBuilder calb) {
        int i = 0;
        int count = data.length;
        if (field == 7) {
            i = currentSerialVersion;
        }
        int bestMatchLength = 0;
        int bestMatch = -1;
        for (i = 
        /* Method generation error in method: java.text.SimpleDateFormat.matchString(java.lang.String, int, int, java.lang.String[], java.text.CalendarBuilder):int
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r17_2 'i' int) = (r17_0 'i' int), (r17_1 'i' int) binds: {(r17_1 'i' int)=B:2:0x000c, (r17_0 'i' int)=B:1:0x000a} in method: java.text.SimpleDateFormat.matchString(java.lang.String, int, int, java.lang.String[], java.text.CalendarBuilder):int
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: java.text.SimpleDateFormat.matchString(java.lang.String, int, int, java.lang.String[], java.text.CalendarBuilder):int
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 15 more
 */

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
                    calb.set(field, ((Integer) data.get(bestMatch)).intValue());
                    return bestMatch.length() + start;
                }
            }
            return -start;
        }

        private int matchZoneString(String text, int start, String[] zoneNames) {
            for (int i = currentSerialVersion; i <= 4; i += currentSerialVersion) {
                String zoneName = zoneNames[i];
                if (text.regionMatches(true, start, zoneName, 0, zoneName.length())) {
                    return i;
                }
            }
            return -1;
        }

        private boolean matchDSTString(String text, int start, int zoneIndex, int standardIndex, String[][] zoneStrings) {
            String zoneName = zoneStrings[zoneIndex][standardIndex + 2];
            return text.regionMatches(true, start, zoneName, 0, zoneName.length()) ? true : -assertionsDisabled;
        }

        private int subParseZoneString(String text, int start, CalendarBuilder calb) {
            boolean z = -assertionsDisabled;
            TimeZone currentTimeZone = getTimeZone();
            int zoneIndex = this.formatData.getZoneIndex(currentTimeZone.getID());
            TimeZone tz = null;
            String[][] zoneStrings = this.formatData.getZoneStringsWrapper();
            String[] strArr = null;
            int nameIndex = 0;
            if (zoneIndex != -1) {
                strArr = zoneStrings[zoneIndex];
                nameIndex = matchZoneString(text, start, strArr);
                if (nameIndex > 0) {
                    if (nameIndex <= 2) {
                        z = strArr[nameIndex].equalsIgnoreCase(strArr[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(strArr[0]);
                }
            }
            if (tz == null) {
                zoneIndex = this.formatData.getZoneIndex(TimeZone.getDefault().getID());
                if (zoneIndex != -1) {
                    strArr = zoneStrings[zoneIndex];
                    nameIndex = matchZoneString(text, start, strArr);
                    if (nameIndex > 0) {
                        if (nameIndex <= 2) {
                            z = strArr[nameIndex].equalsIgnoreCase(strArr[nameIndex + 2]);
                        }
                        tz = TimeZone.getTimeZone(strArr[0]);
                    }
                }
            }
            if (tz == null) {
                int len = zoneStrings.length;
                int i = 0;
                while (i < len) {
                    strArr = zoneStrings[i];
                    nameIndex = matchZoneString(text, start, strArr);
                    if (nameIndex > 0) {
                        if (nameIndex <= 2) {
                            z = strArr[nameIndex].equalsIgnoreCase(strArr[nameIndex + 2]);
                        }
                        tz = TimeZone.getTimeZone(strArr[0]);
                    } else {
                        i += currentSerialVersion;
                    }
                }
            }
            if (tz == null) {
                return 0;
            }
            if (!tz.equals(currentTimeZone)) {
                setTimeZone(tz);
            }
            int dstAmount = nameIndex >= 3 ? tz.getDSTSavings() : 0;
            Object obj = (z || (nameIndex >= 3 && dstAmount == 0)) ? currentSerialVersion : null;
            if (obj == null) {
                calb.clear(15).set(16, dstAmount);
            }
            return strArr[nameIndex].length() + start;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int subParseNumericZone(String text, int start, int sign, int count, boolean colonRequired, CalendarBuilder calb) {
            int i = start;
            i = start + currentSerialVersion;
            try {
                char c = text.charAt(start);
                if (isDigit(c)) {
                    int hours = c - 48;
                    int index = i + currentSerialVersion;
                    try {
                        c = text.charAt(i);
                        if (isDigit(c)) {
                            hours = (hours * 10) + (c - 48);
                        } else {
                            index--;
                        }
                        if (hours > 23) {
                            i = index;
                        } else {
                            int minutes = 0;
                            if (count != currentSerialVersion) {
                                i = index + currentSerialVersion;
                                c = text.charAt(index);
                                if (c == ':') {
                                    index = i + currentSerialVersion;
                                    c = text.charAt(i);
                                } else if (!colonRequired) {
                                    index = i;
                                }
                                if (isDigit(c)) {
                                    minutes = c - 48;
                                    i = index + currentSerialVersion;
                                    c = text.charAt(index);
                                    if (isDigit(c)) {
                                        minutes = (minutes * 10) + (c - 48);
                                    }
                                } else {
                                    i = index;
                                }
                            } else {
                                i = index;
                            }
                            calb.set(15, (MILLIS_PER_MINUTE * (minutes + (hours * 60))) * sign).set(16, 0);
                            return i;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        i = index;
                    }
                }
            } catch (IndexOutOfBoundsException e2) {
            }
            return 1 - i;
        }

        private boolean isDigit(char c) {
            return (c < '0' || c > '9') ? -assertionsDisabled : true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int subParse(String text, int start, int patternCharIndex, int count, boolean obeyCount, boolean[] ambiguousYear, ParsePosition origPos, boolean useFollowingMinusSignAsDelimiter, CalendarBuilder calb) {
            int value = 0;
            ParsePosition pos = new ParsePosition(0);
            pos.index = start;
            if (patternCharIndex == 19 && !this.calendar.isWeekDateSupported()) {
                patternCharIndex = currentSerialVersion;
            }
            int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
            while (pos.index < text.length()) {
                char c = text.charAt(pos.index);
                if (c == ' ' || c == '\t') {
                    pos.index += currentSerialVersion;
                } else {
                    Number number;
                    if (patternCharIndex == 4 || patternCharIndex == 15 || ((patternCharIndex == 2 && count <= 2) || patternCharIndex == currentSerialVersion || patternCharIndex == 19)) {
                        if (obeyCount) {
                            if (start + count <= text.length()) {
                                number = this.numberFormat.parse(text.substring(0, start + count), pos);
                            }
                            origPos.errorIndex = pos.index;
                            return -1;
                        }
                        number = this.numberFormat.parse(text, pos);
                        if (number == null) {
                            if (patternCharIndex == currentSerialVersion) {
                                if (this.calendar instanceof GregorianCalendar) {
                                }
                            }
                            origPos.errorIndex = pos.index;
                            return -1;
                        }
                        value = number.intValue();
                        if (useFollowingMinusSignAsDelimiter && value < 0) {
                            if (pos.index < text.length()) {
                            }
                            if (pos.index == text.length()) {
                                if (text.charAt(pos.index - 1) == this.minusSign) {
                                    value = -value;
                                    pos.index--;
                                }
                            }
                        }
                    }
                    boolean useDateFormatSymbols = useDateFormatSymbols();
                    int index;
                    int idx;
                    int i;
                    int i2;
                    int i3;
                    switch (patternCharIndex) {
                        case GeneralNameInterface.NAME_MATCH /*0*/:
                            if (useDateFormatSymbols) {
                                index = matchString(text, start, 0, this.formatData.getEras(), calb);
                                if (index > 0) {
                                    return index;
                                }
                            }
                            index = matchString(text, start, field, this.calendar.getDisplayNames(field, 0, this.locale), calb);
                            if (index > 0) {
                                return index;
                            }
                            break;
                        case currentSerialVersion /*1*/:
                        case DigitList.MAX_COUNT /*19*/:
                            if (this.calendar instanceof GregorianCalendar) {
                                if (count <= 2 && pos.index - start == 2 && Character.isDigit(text.charAt(start))) {
                                    if (Character.isDigit(text.charAt(start + currentSerialVersion))) {
                                        int ambiguousTwoDigitYear = this.defaultCenturyStartYear % TAG_QUOTE_ASCII_CHAR;
                                        ambiguousYear[0] = value == ambiguousTwoDigitYear ? true : -assertionsDisabled;
                                        value += (value < ambiguousTwoDigitYear ? TAG_QUOTE_ASCII_CHAR : 0) + ((this.defaultCenturyStartYear / TAG_QUOTE_ASCII_CHAR) * TAG_QUOTE_ASCII_CHAR);
                                    }
                                }
                                calb.set(field, value);
                                return pos.index;
                            }
                            Map map = this.calendar.getDisplayNames(field, count >= 4 ? 2 : currentSerialVersion, this.locale);
                            if (map != null) {
                                index = matchString(text, start, field, map, calb);
                                if (index > 0) {
                                    return index;
                                }
                            }
                            calb.set(field, value);
                            return pos.index;
                        case BaseCalendar.MONDAY /*2*/:
                            idx = parseMonth(text, count, value, start, field, pos, useDateFormatSymbols, -assertionsDisabled, calb);
                            if (idx > 0) {
                                return idx;
                            }
                            break;
                        case BaseCalendar.WEDNESDAY /*4*/:
                            if (isLenient() || (value >= currentSerialVersion && value <= 24)) {
                                if (value == this.calendar.getMaximum(11) + currentSerialVersion) {
                                    value = 0;
                                }
                                calb.set(11, value);
                                return pos.index;
                            }
                        case BaseCalendar.SEPTEMBER /*9*/:
                            idx = parseWeekday(text, start, field, useDateFormatSymbols, -assertionsDisabled, calb);
                            if (idx > 0) {
                                return idx;
                            }
                            break;
                        case ZipConstants.LOCCRC /*14*/:
                            if (useDateFormatSymbols) {
                                index = matchString(text, start, 9, this.formatData.getAmPmStrings(), calb);
                                if (index > 0) {
                                    return index;
                                }
                            }
                            index = matchString(text, start, field, this.calendar.getDisplayNames(field, 0, this.locale), calb);
                            if (index > 0) {
                                return index;
                            }
                            break;
                        case Calendar.ZONE_OFFSET /*15*/:
                            if (isLenient() || (value >= currentSerialVersion && value <= 12)) {
                                if (value == this.calendar.getLeastMaximum(10) + currentSerialVersion) {
                                    value = 0;
                                }
                                calb.set(10, value);
                                return pos.index;
                            }
                        case Calendar.FIELD_COUNT /*17*/:
                        case ZipConstants.LOCSIZ /*18*/:
                            try {
                                int sign;
                                c = text.charAt(pos.index);
                                if (c == '+') {
                                    sign = currentSerialVersion;
                                } else if (c == '-') {
                                    sign = -1;
                                } else {
                                    sign = 0;
                                }
                                if (sign != 0) {
                                    int i4 = pos.index + currentSerialVersion;
                                    pos.index = i4;
                                    i = subParseNumericZone(text, i4, sign, 0, -assertionsDisabled, calb);
                                    if (i <= 0) {
                                        pos.index = -i;
                                        i2 = sign;
                                        break;
                                    }
                                    return i;
                                }
                                if (c == 'G' || c == 'g') {
                                    try {
                                        if (text.length() - start >= GMT.length()) {
                                            if (text.regionMatches(true, start, GMT, 0, GMT.length())) {
                                                pos.index = GMT.length() + start;
                                                if (text.length() - pos.index > 0) {
                                                    c = text.charAt(pos.index);
                                                    if (c == '+') {
                                                        i2 = currentSerialVersion;
                                                    } else if (c == '-') {
                                                        i2 = -1;
                                                    }
                                                    if (i2 == 0) {
                                                        i3 = pos.index + currentSerialVersion;
                                                        pos.index = i3;
                                                        i = subParseNumericZone(text, i3, i2, 0, -assertionsDisabled, calb);
                                                        if (i <= 0) {
                                                            pos.index = -i;
                                                            break;
                                                        }
                                                        return i;
                                                    }
                                                    calb.set(15, 0).set(16, 0);
                                                    return pos.index;
                                                }
                                                i2 = sign;
                                                if (i2 == 0) {
                                                    i3 = pos.index + currentSerialVersion;
                                                    pos.index = i3;
                                                    i = subParseNumericZone(text, i3, i2, 0, -assertionsDisabled, calb);
                                                    if (i <= 0) {
                                                        return i;
                                                    }
                                                    pos.index = -i;
                                                } else {
                                                    calb.set(15, 0).set(16, 0);
                                                    return pos.index;
                                                }
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        i2 = sign;
                                        break;
                                    }
                                }
                                i = subParseZoneString(text, pos.index, calb);
                                if (i <= 0) {
                                    pos.index = -i;
                                    i2 = sign;
                                    break;
                                }
                                return i;
                            } catch (IndexOutOfBoundsException e2) {
                                break;
                            }
                        case 21:
                            if (text.length() - pos.index > 0) {
                                c = text.charAt(pos.index);
                                if (c != 'Z') {
                                    if (c != '+') {
                                        if (c != '-') {
                                            pos.index += currentSerialVersion;
                                            break;
                                        }
                                        i2 = -1;
                                    } else {
                                        i2 = currentSerialVersion;
                                    }
                                    i3 = pos.index + currentSerialVersion;
                                    pos.index = i3;
                                    i = subParseNumericZone(text, i3, i2, count, count == 3 ? true : -assertionsDisabled, calb);
                                    if (i <= 0) {
                                        pos.index = -i;
                                        break;
                                    }
                                    return i;
                                }
                                calb.set(15, 0).set(16, 0);
                                int i5 = pos.index + currentSerialVersion;
                                pos.index = i5;
                                return i5;
                            }
                            break;
                        case ZipConstants.LOCLEN /*22*/:
                            idx = parseMonth(text, count, value, start, field, pos, useDateFormatSymbols, true, calb);
                            if (idx > 0) {
                                return idx;
                            }
                            break;
                        case SecureRandom.DEFAULT_SDK_TARGET_FOR_CRYPTO_PROVIDER_WORKAROUND /*23*/:
                            idx = parseWeekday(text, start, field, useDateFormatSymbols, -assertionsDisabled, calb);
                            if (idx > 0) {
                                return idx;
                            }
                            break;
                        default:
                            int parseStart = pos.getIndex();
                            if (!obeyCount) {
                                number = this.numberFormat.parse(text, pos);
                            } else if (start + count <= text.length()) {
                                number = this.numberFormat.parse(text.substring(0, start + count), pos);
                            }
                            if (number != null) {
                                if (patternCharIndex == 8) {
                                    value = (int) ((number.doubleValue() / Math.pow(10.0d, (double) (pos.getIndex() - parseStart))) * 1000.0d);
                                } else {
                                    value = number.intValue();
                                }
                                if (useFollowingMinusSignAsDelimiter && value < 0) {
                                    if (pos.index < text.length()) {
                                        break;
                                    }
                                    if (pos.index == text.length()) {
                                        if (text.charAt(pos.index - 1) == this.minusSign) {
                                            value = -value;
                                            pos.index--;
                                        }
                                    }
                                }
                                calb.set(field, value);
                                return pos.index;
                            }
                            break;
                    }
                    origPos.errorIndex = pos.index;
                    return -1;
                }
            }
            origPos.errorIndex = start;
            return -1;
        }

        private int parseMonth(String text, int count, int value, int start, int field, ParsePosition pos, boolean useDateFormatSymbols, boolean standalone, CalendarBuilder out) {
            if (count <= 2) {
                out.set(2, value - 1);
                return pos.index;
            }
            int index;
            if (useDateFormatSymbols) {
                index = matchString(text, start, 2, standalone ? this.formatData.getStandAloneMonths() : this.formatData.getMonths(), out);
                if (index > 0) {
                    return index;
                }
                index = matchString(text, start, 2, standalone ? this.formatData.getShortStandAloneMonths() : this.formatData.getShortMonths(), out);
                if (index > 0) {
                    return index;
                }
            }
            index = matchString(text, start, field, this.calendar.getDisplayNames(field, 0, this.locale), out);
            if (index > 0) {
                return index;
            }
            return index;
        }

        private int parseWeekday(String text, int start, int field, boolean useDateFormatSymbols, boolean standalone, CalendarBuilder out) {
            int index = -1;
            if (useDateFormatSymbols) {
                index = matchString(text, start, 7, standalone ? this.formatData.getStandAloneWeekdays() : this.formatData.getWeekdays(), out);
                if (index > 0) {
                    return index;
                }
                index = matchString(text, start, 7, standalone ? this.formatData.getShortStandAloneWeekdays() : this.formatData.getShortWeekdays(), out);
                if (index > 0) {
                    return index;
                }
            }
            int[] styles = new int[]{2, currentSerialVersion};
            int length = styles.length;
            for (int i = 0; i < length; i += currentSerialVersion) {
                index = matchString(text, start, field, this.calendar.getDisplayNames(field, styles[i], this.locale), out);
                if (index > 0) {
                    return index;
                }
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
            if (!(isGregorianCalendar() || this.locale == null)) {
                z = -assertionsDisabled;
            }
            return z;
        }

        private boolean isGregorianCalendar() {
            return "java.util.GregorianCalendar".equals(getCalendarName());
        }

        private String translatePattern(String pattern, String from, String to) {
            StringBuilder result = new StringBuilder();
            boolean inQuote = -assertionsDisabled;
            for (int i = 0; i < pattern.length(); i += currentSerialVersion) {
                char c = pattern.charAt(i);
                if (inQuote) {
                    if (c == '\'') {
                        inQuote = -assertionsDisabled;
                    }
                } else if (c == '\'') {
                    inQuote = true;
                } else {
                    if (c < 'a' || c > 'z') {
                        if (c >= 'A' && c <= 'Z') {
                        }
                    }
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
            return translatePattern(this.pattern, "GyMdkHmsSEDFwWahKzZYuXLc", this.formatData.getLocalPatternChars());
        }

        public void applyPattern(String pattern) {
            this.compiledPattern = compile(pattern);
            this.pattern = pattern;
        }

        public void applyLocalizedPattern(String pattern) {
            String p = translatePattern(pattern, this.formatData.getLocalPatternChars(), "GyMdkHmsSEDFwWahKzZYuXLc");
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
            boolean z = -assertionsDisabled;
            if (!super.equals(obj)) {
                return -assertionsDisabled;
            }
            SimpleDateFormat that = (SimpleDateFormat) obj;
            if (this.pattern.equals(that.pattern)) {
                z = this.formatData.equals(that.formatData);
            }
            return z;
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            try {
                this.compiledPattern = compile(this.pattern);
                if (this.serialVersionOnStream < currentSerialVersion) {
                    initializeDefaultCentury();
                } else {
                    parseAmbiguousDatesAsAfter(this.defaultCenturyStart);
                }
                this.serialVersionOnStream = currentSerialVersion;
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
                    this.hasFollowingMinusSign = -assertionsDisabled;
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
