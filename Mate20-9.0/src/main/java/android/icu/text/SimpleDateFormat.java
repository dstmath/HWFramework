package android.icu.text;

import android.icu.impl.DateNumberFormat;
import android.icu.impl.DayPeriodRules;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.lang.UCharacter;
import android.icu.text.DateFormat;
import android.icu.text.DateFormatSymbols;
import android.icu.text.DisplayContext;
import android.icu.text.TimeZoneFormat;
import android.icu.util.BasicTimeZone;
import android.icu.util.Calendar;
import android.icu.util.HebrewCalendar;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneTransition;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.UUID;

public class SimpleDateFormat extends DateFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int[] CALENDAR_FIELD_TO_LEVEL = {0, 10, 20, 20, 30, 30, 20, 30, 30, 40, 50, 50, 60, 70, 80, 0, 0, 10, 30, 10, 0, 40, 0, 0};
    static final UnicodeSet DATE_PATTERN_TYPE = new UnicodeSet("[GyYuUQqMLlwWd]").freeze();
    private static final int DECIMAL_BUF_SIZE = 10;
    static boolean DelayedHebrewMonthCheck = false;
    private static final String FALLBACKPATTERN = "yy/MM/dd HH:mm";
    private static final int HEBREW_CAL_CUR_MILLENIUM_END_YEAR = 6000;
    private static final int HEBREW_CAL_CUR_MILLENIUM_START_YEAR = 5000;
    private static final int ISOSpecialEra = -32000;
    private static final String NUMERIC_FORMAT_CHARS = "ADdFgHhKkmrSsuWwYy";
    private static final String NUMERIC_FORMAT_CHARS2 = "ceLMQq";
    private static ICUCache<String, Object[]> PARSED_PATTERN_CACHE = new SimpleCache();
    private static final boolean[] PATTERN_CHAR_IS_SYNTAX = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false};
    private static final int[] PATTERN_CHAR_TO_INDEX = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 22, 36, -1, 10, 9, 11, 0, 5, -1, -1, 16, 26, 2, -1, 31, -1, 27, -1, 8, -1, 30, 29, 13, 32, 18, 23, -1, -1, -1, -1, -1, -1, 14, 35, 25, 3, 19, -1, 21, 15, -1, -1, 4, -1, 6, -1, -1, -1, 28, 34, 7, -1, 20, 24, 12, 33, 1, 17, -1, -1, -1, -1, -1};
    private static final int[] PATTERN_CHAR_TO_LEVEL = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 40, -1, -1, 20, 30, 30, 0, 50, -1, -1, 50, 20, 20, -1, 0, -1, 20, -1, 80, -1, 10, 0, 30, 0, 10, 0, -1, -1, -1, -1, -1, -1, 40, -1, 30, 30, 30, -1, 0, 50, -1, -1, 50, -1, 60, -1, -1, -1, 20, 10, 70, -1, 10, 0, 20, 0, 10, 0, -1, -1, -1, -1, -1};
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD = {0, 1, 2, 5, 11, 11, 12, 13, 14, 7, 6, 8, 3, 4, 9, 10, 10, 15, 17, 18, 19, 20, 21, 15, 15, 18, 2, 2, 2, 15, 1, 15, 15, 15, 19, -1, -2};
    private static final DateFormat.Field[] PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE = {DateFormat.Field.ERA, DateFormat.Field.YEAR, DateFormat.Field.MONTH, DateFormat.Field.DAY_OF_MONTH, DateFormat.Field.HOUR_OF_DAY1, DateFormat.Field.HOUR_OF_DAY0, DateFormat.Field.MINUTE, DateFormat.Field.SECOND, DateFormat.Field.MILLISECOND, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.DAY_OF_YEAR, DateFormat.Field.DAY_OF_WEEK_IN_MONTH, DateFormat.Field.WEEK_OF_YEAR, DateFormat.Field.WEEK_OF_MONTH, DateFormat.Field.AM_PM, DateFormat.Field.HOUR1, DateFormat.Field.HOUR0, DateFormat.Field.TIME_ZONE, DateFormat.Field.YEAR_WOY, DateFormat.Field.DOW_LOCAL, DateFormat.Field.EXTENDED_YEAR, DateFormat.Field.JULIAN_DAY, DateFormat.Field.MILLISECONDS_IN_DAY, DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.MONTH, DateFormat.Field.QUARTER, DateFormat.Field.QUARTER, DateFormat.Field.TIME_ZONE, DateFormat.Field.YEAR, DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE, DateFormat.Field.RELATED_YEAR, DateFormat.Field.AM_PM_MIDNIGHT_NOON, DateFormat.Field.FLEXIBLE_DAY_PERIOD, DateFormat.Field.TIME_SEPARATOR};
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37};
    private static final String SUPPRESS_NEGATIVE_PREFIX = "꬀";
    private static ULocale cachedDefaultLocale = null;
    private static String cachedDefaultPattern = null;
    static final int currentSerialVersion = 2;
    private static final int millisPerHour = 3600000;
    private static final long serialVersionUID = 4774881970558875024L;
    private transient BreakIterator capitalizationBrkIter;
    private transient char[] decDigits;
    private transient char[] decimalBuf;
    private transient long defaultCenturyBase;
    private Date defaultCenturyStart;
    private transient int defaultCenturyStartYear;
    private DateFormatSymbols formatData;
    private transient boolean hasMinute;
    private transient boolean hasSecond;
    private transient ULocale locale;
    private HashMap<String, NumberFormat> numberFormatters;
    private String override;
    private HashMap<Character, String> overrideMap;
    private String pattern;
    private transient Object[] patternItems;
    private int serialVersionOnStream;
    private volatile TimeZoneFormat tzFormat;
    private transient boolean useFastFormat;
    private transient boolean useLocalZeroPaddingNumberFormat;

    private enum ContextValue {
        UNKNOWN,
        CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE,
        CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE,
        CAPITALIZATION_FOR_UI_LIST_OR_MENU,
        CAPITALIZATION_FOR_STANDALONE
    }

    private static class PatternItem {
        final boolean isNumeric;
        final int length;
        final char type;

        PatternItem(char type2, int length2) {
            this.type = type2;
            this.length = length2;
            this.isNumeric = SimpleDateFormat.isNumeric(type2, length2);
        }
    }

    private static int getLevelFromChar(char ch) {
        if (ch < PATTERN_CHAR_TO_LEVEL.length) {
            return PATTERN_CHAR_TO_LEVEL[ch & 255];
        }
        return -1;
    }

    private static boolean isSyntaxChar(char ch) {
        if (ch < PATTERN_CHAR_IS_SYNTAX.length) {
            return PATTERN_CHAR_IS_SYNTAX[ch & 255];
        }
        return false;
    }

    public SimpleDateFormat() {
        this(getDefaultPattern(), null, null, null, null, true, null);
    }

    public SimpleDateFormat(String pattern2) {
        this(pattern2, null, null, null, null, true, null);
    }

    public SimpleDateFormat(String pattern2, Locale loc) {
        this(pattern2, null, null, null, ULocale.forLocale(loc), true, null);
    }

    public SimpleDateFormat(String pattern2, ULocale loc) {
        this(pattern2, null, null, null, loc, true, null);
    }

    public SimpleDateFormat(String pattern2, String override2, ULocale loc) {
        this(pattern2, null, null, null, loc, false, override2);
    }

    public SimpleDateFormat(String pattern2, DateFormatSymbols formatData2) {
        this(pattern2, (DateFormatSymbols) formatData2.clone(), null, null, null, true, null);
    }

    @Deprecated
    public SimpleDateFormat(String pattern2, DateFormatSymbols formatData2, ULocale loc) {
        this(pattern2, (DateFormatSymbols) formatData2.clone(), null, null, loc, true, null);
    }

    SimpleDateFormat(String pattern2, DateFormatSymbols formatData2, Calendar calendar, ULocale locale2, boolean useFastFormat2, String override2) {
        this(pattern2, (DateFormatSymbols) formatData2.clone(), (Calendar) calendar.clone(), null, locale2, useFastFormat2, override2);
    }

    private SimpleDateFormat(String pattern2, DateFormatSymbols formatData2, Calendar calendar, NumberFormat numberFormat, ULocale locale2, boolean useFastFormat2, String override2) {
        this.serialVersionOnStream = 2;
        this.capitalizationBrkIter = null;
        this.pattern = pattern2;
        this.formatData = formatData2;
        this.calendar = calendar;
        this.numberFormat = numberFormat;
        this.locale = locale2;
        this.useFastFormat = useFastFormat2;
        this.override = override2;
        initialize();
    }

    @Deprecated
    public static SimpleDateFormat getInstance(Calendar.FormatConfiguration formatConfig) {
        String ostr = formatConfig.getOverrideString();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatConfig.getPatternString(), formatConfig.getDateFormatSymbols(), formatConfig.getCalendar(), null, formatConfig.getLocale(), ostr != null && ostr.length() > 0, formatConfig.getOverrideString());
        return simpleDateFormat;
    }

    private void initialize() {
        if (this.locale == null) {
            this.locale = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        if (this.formatData == null) {
            this.formatData = new DateFormatSymbols(this.locale);
        }
        if (this.calendar == null) {
            this.calendar = Calendar.getInstance(this.locale);
        }
        if (this.numberFormat == null) {
            NumberingSystem ns = NumberingSystem.getInstance(this.locale);
            String digitString = ns.getDescription();
            if (ns.isAlgorithmic() || digitString.length() != 10) {
                this.numberFormat = NumberFormat.getInstance(this.locale);
            } else {
                this.numberFormat = new DateNumberFormat(this.locale, digitString, ns.getName());
            }
        }
        if (this.numberFormat instanceof DecimalFormat) {
            fixNumberFormatForDates(this.numberFormat);
        }
        this.defaultCenturyBase = System.currentTimeMillis();
        setLocale(this.calendar.getLocale(ULocale.VALID_LOCALE), this.calendar.getLocale(ULocale.ACTUAL_LOCALE));
        initLocalZeroPaddingNumberFormat();
        if (this.override != null) {
            initNumberFormatters(this.locale);
        }
        parsePattern();
    }

    private synchronized void initializeTimeZoneFormat(boolean bForceUpdate) {
        if (!bForceUpdate) {
            try {
                if (this.tzFormat == null) {
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        this.tzFormat = TimeZoneFormat.getInstance(this.locale);
        String digits = null;
        if (this.numberFormat instanceof DecimalFormat) {
            String[] strDigits = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getDigitStringsLocal();
            StringBuilder digitsBuf = new StringBuilder();
            for (String digit : strDigits) {
                digitsBuf.append(digit);
            }
            digits = digitsBuf.toString();
        } else if (this.numberFormat instanceof DateNumberFormat) {
            digits = new String(((DateNumberFormat) this.numberFormat).getDigits());
        }
        if (digits != null && !this.tzFormat.getGMTOffsetDigits().equals(digits)) {
            if (this.tzFormat.isFrozen()) {
                this.tzFormat = this.tzFormat.cloneAsThawed();
            }
            this.tzFormat.setGMTOffsetDigits(digits);
        }
    }

    private TimeZoneFormat tzFormat() {
        if (this.tzFormat == null) {
            initializeTimeZoneFormat(false);
        }
        return this.tzFormat;
    }

    private static synchronized String getDefaultPattern() {
        String str;
        synchronized (SimpleDateFormat.class) {
            ULocale defaultLocale = ULocale.getDefault(ULocale.Category.FORMAT);
            if (!defaultLocale.equals(cachedDefaultLocale)) {
                cachedDefaultLocale = defaultLocale;
                Calendar cal = Calendar.getInstance(cachedDefaultLocale);
                try {
                    ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, cachedDefaultLocale);
                    ICUResourceBundle patternsRb = rb.findWithFallback("calendar/" + cal.getType() + "/DateTimePatterns");
                    if (patternsRb == null) {
                        patternsRb = rb.findWithFallback("calendar/gregorian/DateTimePatterns");
                    }
                    if (patternsRb != null) {
                        if (patternsRb.getSize() >= 9) {
                            int defaultIndex = 8;
                            if (patternsRb.getSize() >= 13) {
                                defaultIndex = 8 + 4;
                            }
                            cachedDefaultPattern = SimpleFormatterImpl.formatRawPattern(patternsRb.getString(defaultIndex), 2, 2, patternsRb.getString(3), patternsRb.getString(7));
                        }
                    }
                    cachedDefaultPattern = FALLBACKPATTERN;
                } catch (MissingResourceException e) {
                    cachedDefaultPattern = FALLBACKPATTERN;
                }
            }
            str = cachedDefaultPattern;
        }
        return str;
    }

    private void parseAmbiguousDatesAsAfter(Date startDate) {
        this.defaultCenturyStart = startDate;
        this.calendar.setTime(startDate);
        this.defaultCenturyStartYear = this.calendar.get(1);
    }

    private void initializeDefaultCenturyStart(long baseTime) {
        this.defaultCenturyBase = baseTime;
        Calendar tmpCal = (Calendar) this.calendar.clone();
        tmpCal.setTimeInMillis(baseTime);
        tmpCal.add(1, -80);
        this.defaultCenturyStart = tmpCal.getTime();
        this.defaultCenturyStartYear = tmpCal.get(1);
    }

    private Date getDefaultCenturyStart() {
        if (this.defaultCenturyStart == null) {
            initializeDefaultCenturyStart(this.defaultCenturyBase);
        }
        return this.defaultCenturyStart;
    }

    private int getDefaultCenturyStartYear() {
        if (this.defaultCenturyStart == null) {
            initializeDefaultCenturyStart(this.defaultCenturyBase);
        }
        return this.defaultCenturyStartYear;
    }

    public void set2DigitYearStart(Date startDate) {
        parseAmbiguousDatesAsAfter(startDate);
    }

    public Date get2DigitYearStart() {
        return getDefaultCenturyStart();
    }

    public void setContext(DisplayContext context) {
        super.setContext(context);
        if (this.capitalizationBrkIter != null) {
            return;
        }
        if (context == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || context == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || context == DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
        }
    }

    public StringBuffer format(Calendar cal, StringBuffer toAppendTo, FieldPosition pos) {
        TimeZone backupTZ = null;
        if (cal != this.calendar && !cal.getType().equals(this.calendar.getType())) {
            this.calendar.setTimeInMillis(cal.getTimeInMillis());
            backupTZ = this.calendar.getTimeZone();
            this.calendar.setTimeZone(cal.getTimeZone());
            cal = this.calendar;
        }
        StringBuffer result = format(cal, getContext(DisplayContext.Type.CAPITALIZATION), toAppendTo, pos, null);
        if (backupTZ != null) {
            this.calendar.setTimeZone(backupTZ);
        }
        return result;
    }

    private StringBuffer format(Calendar cal, DisplayContext capitalizationContext, StringBuffer toAppendTo, FieldPosition pos, List<FieldPosition> attributes) {
        Object[] items;
        int start;
        StringBuffer stringBuffer = toAppendTo;
        FieldPosition fieldPosition = pos;
        List<FieldPosition> list = attributes;
        int end = 0;
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        Object[] items2 = getPatternItems();
        while (true) {
            int i = end;
            if (i < items2.length) {
                if (items2[i] instanceof String) {
                    stringBuffer.append((String) items2[i]);
                    items = items2;
                } else {
                    PatternItem item = (PatternItem) items2[i];
                    int start2 = 0;
                    if (list != null) {
                        start2 = toAppendTo.length();
                    }
                    int start3 = start2;
                    if (this.useFastFormat) {
                        items = items2;
                        start = start3;
                        subFormat(stringBuffer, item.type, item.length, toAppendTo.length(), i, capitalizationContext, fieldPosition, cal);
                    } else {
                        items = items2;
                        start = start3;
                        stringBuffer.append(subFormat(item.type, item.length, toAppendTo.length(), i, capitalizationContext, fieldPosition, cal));
                    }
                    if (list != null) {
                        int end2 = toAppendTo.length();
                        if (end2 - start > 0) {
                            FieldPosition fp = new FieldPosition(patternCharToDateFormatField(item.type));
                            fp.setBeginIndex(start);
                            fp.setEndIndex(end2);
                            list.add(fp);
                        }
                    }
                }
                end = i + 1;
                items2 = items;
            } else {
                return stringBuffer;
            }
        }
    }

    private static int getIndexFromChar(char ch) {
        if (ch < PATTERN_CHAR_TO_INDEX.length) {
            return PATTERN_CHAR_TO_INDEX[ch & 255];
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public DateFormat.Field patternCharToDateFormatField(char ch) {
        int patternCharIndex = getIndexFromChar(ch);
        if (patternCharIndex != -1) {
            return PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE[patternCharIndex];
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String subFormat(char ch, int count, int beginOffset, FieldPosition pos, DateFormatSymbols fmtData, Calendar cal) throws IllegalArgumentException {
        return subFormat(ch, count, beginOffset, 0, DisplayContext.CAPITALIZATION_NONE, pos, cal);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public String subFormat(char ch, int count, int beginOffset, int fieldNum, DisplayContext capitalizationContext, FieldPosition pos, Calendar cal) {
        StringBuffer buf = new StringBuffer();
        subFormat(buf, ch, count, beginOffset, fieldNum, capitalizationContext, pos, cal);
        return buf.toString();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0306, code lost:
        r17 = r0;
        r38 = r6;
        r0 = r20;
        r25 = r27;
        r1 = r30;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x04de, code lost:
        r38 = r6;
        r25 = r27;
        r6 = r35;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0518, code lost:
        r17 = r0;
        r38 = r6;
        r0 = r20;
        r25 = r27;
        r1 = r35;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x059a, code lost:
        if (r12 != 5) goto L_0x05ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x059c, code lost:
        safeAppend(r9.formatData.narrowWeekdays, r1, r10);
        r20 = android.icu.text.DateFormatSymbols.CapitalizationContextUsage.DAY_NARROW;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x05a5, code lost:
        r38 = r6;
        r0 = r20;
        r25 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x05ad, code lost:
        if (r12 != 4) goto L_0x05b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x05af, code lost:
        safeAppend(r9.formatData.weekdays, r1, r10);
        r20 = android.icu.text.DateFormatSymbols.CapitalizationContextUsage.DAY_FORMAT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x05ba, code lost:
        if (r12 != 6) goto L_0x05cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x05c0, code lost:
        if (r9.formatData.shorterWeekdays == null) goto L_0x05cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x05c2, code lost:
        safeAppend(r9.formatData.shorterWeekdays, r1, r10);
        r20 = android.icu.text.DateFormatSymbols.CapitalizationContextUsage.DAY_FORMAT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x05cc, code lost:
        safeAppend(r9.formatData.shortWeekdays, r1, r10);
        r20 = android.icu.text.DateFormatSymbols.CapitalizationContextUsage.DAY_FORMAT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x0794, code lost:
        if (r9.override == null) goto L_0x07b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x079e, code lost:
        if (r9.override.compareTo("hebr") == 0) goto L_0x07aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x07a8, code lost:
        if (r9.override.indexOf("y=hebr") < 0) goto L_0x07b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x07ac, code lost:
        if (r3 <= HEBREW_CAL_CUR_MILLENIUM_START_YEAR) goto L_0x07b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:0x07b0, code lost:
        if (r3 >= HEBREW_CAL_CUR_MILLENIUM_END_YEAR) goto L_0x07b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x07b2, code lost:
        r6 = r3 - 5000;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:337:0x07b6, code lost:
        r6 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:338:0x07b7, code lost:
        if (r12 != 2) goto L_0x07c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:339:0x07b9, code lost:
        zeroPaddingNumber(r18, r10, r6, 2, 2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:340:0x07c5, code lost:
        zeroPaddingNumber(r18, r10, r6, r12, Integer.MAX_VALUE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:355:0x0827, code lost:
        if (r46 != 0) goto L_0x0890;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:356:0x0829, code lost:
        if (r14 == null) goto L_0x0890;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:0x082b, code lost:
        r3 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:0x0835, code lost:
        if (android.icu.lang.UCharacter.isLowerCase(r10.codePointAt(r3)) == false) goto L_0x088b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:359:0x0837, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:360:0x0840, code lost:
        switch(r47) {
            case android.icu.text.DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE :android.icu.text.DisplayContext: goto L_0x0861;
            case android.icu.text.DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU :android.icu.text.DisplayContext: goto L_0x0844;
            case android.icu.text.DisplayContext.CAPITALIZATION_FOR_STANDALONE :android.icu.text.DisplayContext: goto L_0x0844;
            default: goto L_0x0843;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:362:0x0848, code lost:
        if (r9.formatData.capitalization == null) goto L_0x0863;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:363:0x084a, code lost:
        r5 = r9.formatData.capitalization.get(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:0x0856, code lost:
        if (r14 != android.icu.text.DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU) goto L_0x085c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:365:0x0858, code lost:
        r6 = r5[0];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:366:0x085c, code lost:
        r6 = r5[1];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:367:0x085f, code lost:
        r4 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:368:0x0861, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:369:0x0863, code lost:
        if (r4 == false) goto L_0x088b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:371:0x0867, code lost:
        if (r9.capitalizationBrkIter != null) goto L_0x0871;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:372:0x0869, code lost:
        r9.capitalizationBrkIter = android.icu.text.BreakIterator.getSentenceInstance(r9.locale);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:373:0x0871, code lost:
        r39 = r0;
        r40 = r1;
        r10.replace(r3, r42.length(), android.icu.lang.UCharacter.toTitleCase(r9.locale, r10.substring(r3), r9.capitalizationBrkIter, (int) android.icu.impl.coll.CollationSettings.CASE_FIRST_AND_UPPER_MASK));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:374:0x088b, code lost:
        r39 = r0;
        r40 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:375:0x0890, code lost:
        r39 = r0;
        r40 = r1;
        r3 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:377:0x089e, code lost:
        if (r48.getBeginIndex() != r48.getEndIndex()) goto L_0x08cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:379:0x08a8, code lost:
        if (r48.getField() != PATTERN_INDEX_TO_DATE_FORMAT_FIELD[r25]) goto L_0x08b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:380:0x08aa, code lost:
        r15.setBeginIndex(r13);
        r15.setEndIndex((r42.length() + r13) - r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:382:0x08bf, code lost:
        if (r48.getFieldAttribute() != PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE[r25]) goto L_0x08cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:383:0x08c1, code lost:
        r15.setBeginIndex(r13);
        r15.setEndIndex((r42.length() + r13) - r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:384:0x08cd, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:264:0x067f  */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x0687  */
    @Deprecated
    public void subFormat(StringBuffer buf, char ch, int count, int beginOffset, int fieldNum, DisplayContext capitalizationContext, FieldPosition pos, Calendar cal) {
        int value;
        int bufstart;
        int patternCharIndex;
        DateFormatSymbols.CapitalizationContextUsage capContextUsageType;
        int value2;
        int value3;
        DateFormatSymbols.CapitalizationContextUsage capContextUsageType2;
        int value4;
        int i;
        int value5;
        int patternCharIndex2;
        int value6;
        int value7;
        int value8;
        int value9;
        int patternCharIndex3;
        int value10;
        int patternCharIndex4;
        int value11;
        int value12;
        int patternCharIndex5;
        String result;
        int bufstart2;
        String result2;
        DateFormatSymbols.CapitalizationContextUsage capContextUsageType3;
        int value13;
        int value14;
        int value15;
        int patternCharIndex6;
        String result3;
        int bufstart3;
        int value16;
        int patternCharIndex7;
        TimeZone tz;
        long date;
        DayPeriodRules.DayPeriod periodType;
        StringBuffer stringBuffer = buf;
        char c = ch;
        int patternCharIndex8 = count;
        int i2 = beginOffset;
        DisplayContext displayContext = capitalizationContext;
        FieldPosition fieldPosition = pos;
        Calendar calendar = cal;
        int bufstart4 = buf.length();
        TimeZone tz2 = cal.getTimeZone();
        long date2 = cal.getTimeInMillis();
        String result4 = null;
        int patternCharIndex9 = getIndexFromChar(ch);
        if (patternCharIndex9 != -1) {
            int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex9];
            if (field >= 0) {
                value = patternCharIndex9 != 34 ? calendar.get(field) : cal.getRelatedYear();
            } else {
                value = 0;
            }
            NumberFormat currentNumberFormat = getNumberFormat(c);
            DateFormatSymbols.CapitalizationContextUsage capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.OTHER;
            int i3 = field;
            long date3 = date2;
            switch (patternCharIndex9) {
                case 0:
                    bufstart = bufstart4;
                    Calendar calendar2 = calendar;
                    long j = date3;
                    patternCharIndex = patternCharIndex9;
                    int value17 = value;
                    if (cal.getType().equals("chinese") || cal.getType().equals("dangi")) {
                        value2 = value17;
                        zeroPaddingNumber(currentNumberFormat, stringBuffer, value17, 1, 9);
                    } else {
                        if (patternCharIndex8 == 5) {
                            safeAppend(this.formatData.narrowEras, value17, stringBuffer);
                            capContextUsageType2 = DateFormatSymbols.CapitalizationContextUsage.ERA_NARROW;
                        } else if (patternCharIndex8 == 4) {
                            safeAppend(this.formatData.eraNames, value17, stringBuffer);
                            capContextUsageType2 = DateFormatSymbols.CapitalizationContextUsage.ERA_WIDE;
                        } else {
                            safeAppend(this.formatData.eras, value17, stringBuffer);
                            capContextUsageType2 = DateFormatSymbols.CapitalizationContextUsage.ERA_ABBREV;
                        }
                        value3 = value17;
                    }
                    break;
                case 1:
                case 18:
                    bufstart = bufstart4;
                    Calendar calendar3 = calendar;
                    long j2 = date3;
                    patternCharIndex = patternCharIndex9;
                    value4 = value;
                    break;
                case 2:
                case 26:
                    int value18 = value;
                    int patternCharIndex10 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar4 = calendar;
                    long j3 = date3;
                    if (cal.getType().equals("hebrew")) {
                        boolean isLeap = HebrewCalendar.isLeapYear(calendar4.get(1));
                        if (isLeap) {
                            value8 = value18;
                            if (value8 == 6 && patternCharIndex8 >= 3) {
                                value6 = 13;
                                if (isLeap) {
                                    value7 = 6;
                                    if (value6 >= 6 && patternCharIndex8 < 3) {
                                        value6--;
                                    }
                                } else {
                                    value7 = 6;
                                }
                                i = value7;
                                value5 = value6;
                            }
                        } else {
                            value8 = value18;
                        }
                        value6 = value8;
                        if (isLeap) {
                        }
                        i = value7;
                        value5 = value6;
                    } else {
                        value5 = value18;
                        i = 6;
                    }
                    int isLeapMonth = (this.formatData.leapMonthPatterns == null || this.formatData.leapMonthPatterns.length < 7) ? 0 : calendar4.get(22);
                    String str = null;
                    if (patternCharIndex8 != 5) {
                        patternCharIndex2 = patternCharIndex10;
                        if (patternCharIndex8 != 4) {
                            if (patternCharIndex8 != 3) {
                                StringBuffer monthNumber = new StringBuffer();
                                patternCharIndex = patternCharIndex2;
                                TimeZone timeZone = tz2;
                                int value19 = value5;
                                zeroPaddingNumber(currentNumberFormat, monthNumber, value5 + 1, patternCharIndex8, Integer.MAX_VALUE);
                                String[] monthNumberStrings = {monthNumber.toString()};
                                if (isLeapMonth != 0) {
                                    str = this.formatData.leapMonthPatterns[i];
                                }
                                safeAppendWithMonthPattern(monthNumberStrings, 0, stringBuffer, str);
                                capContextUsageType = capContextUsageType4;
                                value = value19;
                                break;
                            } else if (patternCharIndex2 == 2) {
                                String[] strArr = this.formatData.shortMonths;
                                if (isLeapMonth != 0) {
                                    str = this.formatData.leapMonthPatterns[1];
                                }
                                safeAppendWithMonthPattern(strArr, value5, stringBuffer, str);
                                capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.MONTH_FORMAT;
                            } else {
                                String[] strArr2 = this.formatData.standaloneShortMonths;
                                if (isLeapMonth != 0) {
                                    str = this.formatData.leapMonthPatterns[4];
                                }
                                safeAppendWithMonthPattern(strArr2, value5, stringBuffer, str);
                                capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.MONTH_STANDALONE;
                            }
                        } else if (patternCharIndex2 == 2) {
                            String[] strArr3 = this.formatData.months;
                            if (isLeapMonth != 0) {
                                str = this.formatData.leapMonthPatterns[0];
                            }
                            safeAppendWithMonthPattern(strArr3, value5, stringBuffer, str);
                            capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.MONTH_FORMAT;
                        } else {
                            String[] strArr4 = this.formatData.standaloneMonths;
                            if (isLeapMonth != 0) {
                                str = this.formatData.leapMonthPatterns[3];
                            }
                            safeAppendWithMonthPattern(strArr4, value5, stringBuffer, str);
                            capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.MONTH_STANDALONE;
                        }
                    } else {
                        patternCharIndex2 = patternCharIndex10;
                        if (patternCharIndex2 == 2) {
                            String[] strArr5 = this.formatData.narrowMonths;
                            if (isLeapMonth != 0) {
                                str = this.formatData.leapMonthPatterns[2];
                            }
                            safeAppendWithMonthPattern(strArr5, value5, stringBuffer, str);
                        } else {
                            String[] strArr6 = this.formatData.standaloneNarrowMonths;
                            if (isLeapMonth != 0) {
                                str = this.formatData.leapMonthPatterns[5];
                            }
                            safeAppendWithMonthPattern(strArr6, value5, stringBuffer, str);
                        }
                        capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.MONTH_NARROW;
                    }
                    patternCharIndex = patternCharIndex2;
                    value3 = value5;
                    break;
                case 4:
                    int patternCharIndex11 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar5 = calendar;
                    long j4 = date3;
                    int value20 = value;
                    if (value20 == 0) {
                        value9 = value20;
                        zeroPaddingNumber(currentNumberFormat, stringBuffer, calendar5.getMaximum(11) + 1, patternCharIndex8, Integer.MAX_VALUE);
                    } else {
                        value9 = value20;
                        zeroPaddingNumber(currentNumberFormat, stringBuffer, value20, patternCharIndex8, Integer.MAX_VALUE);
                    }
                    patternCharIndex = patternCharIndex11;
                    value2 = value9;
                    value3 = value2;
                    capContextUsageType = capContextUsageType4;
                    break;
                case 8:
                    patternCharIndex3 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar6 = calendar;
                    long j5 = date3;
                    int value21 = value;
                    this.numberFormat.setMinimumIntegerDigits(Math.min(3, patternCharIndex8));
                    this.numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
                    if (patternCharIndex8 == 1) {
                        value10 = value21 / 100;
                    } else if (patternCharIndex8 == 2) {
                        value10 = value21 / 10;
                    } else {
                        value10 = value21;
                    }
                    FieldPosition p = new FieldPosition(-1);
                    this.numberFormat.format((long) value, stringBuffer, p);
                    if (patternCharIndex8 > 3) {
                        this.numberFormat.setMinimumIntegerDigits(patternCharIndex8 - 3);
                        this.numberFormat.format(0, stringBuffer, p);
                        break;
                    }
                    break;
                case 9:
                    patternCharIndex3 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar7 = calendar;
                    long j6 = date3;
                    int patternCharIndex12 = value;
                    break;
                case 14:
                    int value22 = value;
                    patternCharIndex4 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar8 = calendar;
                    long j7 = date3;
                    if (patternCharIndex8 < 5) {
                        value4 = value22;
                    } else if (this.formatData.ampmsNarrow == null) {
                        value4 = value22;
                    } else {
                        value4 = value22;
                        safeAppend(this.formatData.ampmsNarrow, value4, stringBuffer);
                    }
                    safeAppend(this.formatData.ampms, value4, stringBuffer);
                case 15:
                    int i4 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar9 = calendar;
                    long j8 = date3;
                    int value23 = value;
                    if (value23 == 0) {
                        value11 = value23;
                        zeroPaddingNumber(currentNumberFormat, stringBuffer, calendar9.getLeastMaximum(10) + 1, patternCharIndex8, Integer.MAX_VALUE);
                    } else {
                        value11 = value23;
                        zeroPaddingNumber(currentNumberFormat, stringBuffer, value11, patternCharIndex8, Integer.MAX_VALUE);
                    }
                    patternCharIndex = i4;
                    value2 = value11;
                    value3 = value2;
                    capContextUsageType = capContextUsageType4;
                    break;
                case 17:
                    value12 = value;
                    patternCharIndex5 = patternCharIndex9;
                    bufstart2 = bufstart4;
                    Calendar calendar10 = calendar;
                    long date4 = date3;
                    if (patternCharIndex8 < 4) {
                        result2 = tzFormat().format(TimeZoneFormat.Style.SPECIFIC_SHORT, tz2, date4);
                        capContextUsageType3 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_SHORT;
                    } else {
                        result2 = tzFormat().format(TimeZoneFormat.Style.SPECIFIC_LONG, tz2, date4);
                        capContextUsageType3 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_LONG;
                    }
                    capContextUsageType4 = capContextUsageType3;
                    stringBuffer.append(result);
                    break;
                case 19:
                    value13 = value;
                    patternCharIndex3 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar11 = calendar;
                    long j9 = date3;
                    if (patternCharIndex8 >= 3) {
                        value = calendar11.get(7);
                        break;
                    } else {
                        zeroPaddingNumber(currentNumberFormat, stringBuffer, value13, patternCharIndex8, Integer.MAX_VALUE);
                    }
                case 23:
                    value12 = value;
                    patternCharIndex5 = patternCharIndex9;
                    bufstart2 = bufstart4;
                    Calendar calendar12 = calendar;
                    long date5 = date3;
                    if (patternCharIndex8 < 4) {
                        result = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL, tz2, date5);
                    } else if (patternCharIndex8 == 5) {
                        result = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_FULL, tz2, date5);
                    } else {
                        result = tzFormat().format(TimeZoneFormat.Style.LOCALIZED_GMT, tz2, date5);
                    }
                    stringBuffer.append(result);
                    break;
                case 24:
                    value12 = value;
                    patternCharIndex5 = patternCharIndex9;
                    bufstart2 = bufstart4;
                    Calendar calendar13 = calendar;
                    long date6 = date3;
                    if (patternCharIndex8 == 1) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.GENERIC_SHORT, tz2, date6);
                        capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_SHORT;
                    } else if (patternCharIndex8 == 4) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.GENERIC_LONG, tz2, date6);
                        capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_LONG;
                    }
                    result = result4;
                    stringBuffer.append(result);
                    break;
                case 25:
                    value13 = value;
                    patternCharIndex3 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar14 = calendar;
                    long j10 = date3;
                    if (patternCharIndex8 >= 3) {
                        value = calendar14.get(7);
                        if (patternCharIndex8 != 5) {
                            if (patternCharIndex8 != 4) {
                                if (patternCharIndex8 == 6 && this.formatData.standaloneShorterWeekdays != null) {
                                    safeAppend(this.formatData.standaloneShorterWeekdays, value, stringBuffer);
                                    capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.DAY_STANDALONE;
                                    break;
                                } else {
                                    safeAppend(this.formatData.standaloneShortWeekdays, value, stringBuffer);
                                    capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.DAY_STANDALONE;
                                    break;
                                }
                            } else {
                                safeAppend(this.formatData.standaloneWeekdays, value, stringBuffer);
                                capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.DAY_STANDALONE;
                                break;
                            }
                        } else {
                            safeAppend(this.formatData.standaloneNarrowWeekdays, value, stringBuffer);
                            capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.DAY_NARROW;
                            break;
                        }
                    } else {
                        zeroPaddingNumber(currentNumberFormat, stringBuffer, value13, 1, Integer.MAX_VALUE);
                    }
                    break;
                case 27:
                    int value24 = value;
                    patternCharIndex3 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar15 = calendar;
                    long j11 = date3;
                    if (patternCharIndex8 >= 4) {
                        value14 = value24;
                        safeAppend(this.formatData.quarters, value14 / 3, stringBuffer);
                    } else {
                        value14 = value24;
                        if (patternCharIndex8 == 3) {
                            safeAppend(this.formatData.shortQuarters, value14 / 3, stringBuffer);
                        } else {
                            value13 = value14;
                            zeroPaddingNumber(currentNumberFormat, stringBuffer, (value14 / 3) + 1, patternCharIndex8, Integer.MAX_VALUE);
                        }
                    }
                    patternCharIndex = patternCharIndex3;
                    value2 = value14;
                    value3 = value2;
                    capContextUsageType = capContextUsageType4;
                    break;
                case 28:
                    patternCharIndex4 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar16 = calendar;
                    long j12 = date3;
                    value4 = value;
                    if (patternCharIndex8 < 4) {
                        if (patternCharIndex8 != 3) {
                            zeroPaddingNumber(currentNumberFormat, stringBuffer, (value4 / 3) + 1, patternCharIndex8, Integer.MAX_VALUE);
                            TimeZone timeZone2 = tz2;
                            patternCharIndex = patternCharIndex4;
                            value2 = value4;
                            value3 = value2;
                            capContextUsageType = capContextUsageType4;
                            break;
                        } else {
                            safeAppend(this.formatData.standaloneShortQuarters, value4 / 3, stringBuffer);
                        }
                    } else {
                        safeAppend(this.formatData.standaloneQuarters, value4 / 3, stringBuffer);
                    }
                case 29:
                    patternCharIndex3 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar17 = calendar;
                    long date7 = date3;
                    int value25 = value;
                    if (patternCharIndex8 == 1) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.ZONE_ID_SHORT, tz2, date7);
                    } else if (patternCharIndex8 == 2) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.ZONE_ID, tz2, date7);
                    } else if (patternCharIndex8 == 3) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.EXEMPLAR_LOCATION, tz2, date7);
                    } else if (patternCharIndex8 == 4) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.GENERIC_LOCATION, tz2, date7);
                        capContextUsageType4 = DateFormatSymbols.CapitalizationContextUsage.ZONE_LONG;
                    }
                    String result5 = result4;
                    stringBuffer.append(result5);
                    String str2 = result5;
                    value = value25;
                    break;
                case 30:
                    int value26 = value;
                    patternCharIndex4 = patternCharIndex9;
                    bufstart = bufstart4;
                    Calendar calendar18 = calendar;
                    long j13 = date3;
                    if (this.formatData.shortYearNames == null) {
                        TimeZone timeZone3 = tz2;
                        patternCharIndex = patternCharIndex4;
                        value4 = value26;
                        break;
                    } else {
                        value4 = value26;
                        if (value4 > this.formatData.shortYearNames.length) {
                            TimeZone timeZone4 = tz2;
                            patternCharIndex = patternCharIndex4;
                            break;
                        } else {
                            safeAppend(this.formatData.shortYearNames, value4 - 1, stringBuffer);
                            patternCharIndex = patternCharIndex4;
                            value2 = value4;
                            value3 = value2;
                            capContextUsageType = capContextUsageType4;
                            break;
                        }
                    }
                case 31:
                    value15 = value;
                    patternCharIndex6 = patternCharIndex9;
                    bufstart3 = bufstart4;
                    Calendar calendar19 = calendar;
                    long date8 = date3;
                    if (patternCharIndex8 == 1) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.LOCALIZED_GMT_SHORT, tz2, date8);
                    } else if (patternCharIndex8 == 4) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.LOCALIZED_GMT, tz2, date8);
                    }
                    result3 = result4;
                    stringBuffer.append(result3);
                    break;
                case 32:
                    value15 = value;
                    patternCharIndex6 = patternCharIndex9;
                    bufstart3 = bufstart4;
                    Calendar calendar20 = calendar;
                    long date9 = date3;
                    if (patternCharIndex8 == 1) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_SHORT, tz2, date9);
                    } else if (patternCharIndex8 == 2) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_FIXED, tz2, date9);
                    } else if (patternCharIndex8 == 3) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_FIXED, tz2, date9);
                    } else if (patternCharIndex8 == 4) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_FULL, tz2, date9);
                    } else if (patternCharIndex8 == 5) {
                        result4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_FULL, tz2, date9);
                    }
                    result3 = result4;
                    stringBuffer.append(result3);
                    break;
                case 33:
                    value15 = value;
                    patternCharIndex6 = patternCharIndex9;
                    TimeZone tz3 = tz2;
                    bufstart3 = bufstart4;
                    Calendar calendar21 = calendar;
                    long date10 = date3;
                    if (patternCharIndex8 == 1) {
                        tz2 = tz3;
                        result4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_SHORT, tz2, date10);
                    } else {
                        long date11 = date10;
                        tz2 = tz3;
                        if (patternCharIndex8 == 2) {
                            result4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_FIXED, tz2, date11);
                        } else if (patternCharIndex8 == 3) {
                            result4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FIXED, tz2, date11);
                        } else if (patternCharIndex8 == 4) {
                            result4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL, tz2, date11);
                        } else if (patternCharIndex8 == 5) {
                            result4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FULL, tz2, date11);
                        }
                    }
                    result3 = result4;
                    stringBuffer.append(result3);
                    break;
                case 35:
                    int value27 = value;
                    int patternCharIndex13 = patternCharIndex9;
                    TimeZone tz4 = tz2;
                    bufstart = bufstart4;
                    long date12 = date3;
                    Calendar calendar22 = cal;
                    int hour = calendar22.get(11);
                    String toAppend = null;
                    if (hour == 12 && ((!this.hasMinute || calendar22.get(12) == 0) && (!this.hasSecond || calendar22.get(13) == 0))) {
                        int value28 = calendar22.get(9);
                        if (patternCharIndex8 <= 3) {
                            toAppend = this.formatData.abbreviatedDayPeriods[value28];
                        } else if (patternCharIndex8 == 4 || patternCharIndex8 > 5) {
                            toAppend = this.formatData.wideDayPeriods[value28];
                        } else {
                            toAppend = this.formatData.narrowDayPeriods[value28];
                        }
                        value27 = value28;
                    }
                    String toAppend2 = toAppend;
                    if (toAppend2 == null) {
                        String str3 = toAppend2;
                        int i5 = hour;
                        subFormat(stringBuffer, 'a', patternCharIndex8, i2, fieldNum, displayContext, fieldPosition, calendar22);
                    } else {
                        int i6 = hour;
                        stringBuffer.append(toAppend2);
                    }
                    capContextUsageType = capContextUsageType4;
                    patternCharIndex = patternCharIndex13;
                    long j14 = date12;
                    value = value27;
                    TimeZone timeZone5 = tz4;
                    break;
                case 36:
                    DayPeriodRules ruleSet = DayPeriodRules.getInstance(getLocale());
                    if (ruleSet != null) {
                        value16 = value;
                        patternCharIndex7 = patternCharIndex9;
                        DayPeriodRules ruleSet2 = ruleSet;
                        tz = tz2;
                        bufstart = bufstart4;
                        date = date3;
                        Calendar calendar23 = cal;
                        int hour2 = calendar23.get(11);
                        int minute = 0;
                        int second = 0;
                        if (this.hasMinute) {
                            minute = calendar23.get(12);
                        }
                        int minute2 = minute;
                        if (this.hasSecond) {
                            second = calendar23.get(13);
                        }
                        int second2 = second;
                        if (hour2 == 0 && minute2 == 0 && second2 == 0 && ruleSet2.hasMidnight()) {
                            periodType = DayPeriodRules.DayPeriod.MIDNIGHT;
                        } else if (hour2 == 12 && minute2 == 0 && second2 == 0 && ruleSet2.hasNoon()) {
                            periodType = DayPeriodRules.DayPeriod.NOON;
                        } else {
                            periodType = ruleSet2.getDayPeriodForHour(hour2);
                        }
                        String toAppend3 = null;
                        if (!(periodType == DayPeriodRules.DayPeriod.AM || periodType == DayPeriodRules.DayPeriod.PM || periodType == DayPeriodRules.DayPeriod.MIDNIGHT)) {
                            int index = periodType.ordinal();
                            toAppend3 = patternCharIndex8 <= 3 ? this.formatData.abbreviatedDayPeriods[index] : (patternCharIndex8 == 4 || patternCharIndex8 > 5) ? this.formatData.wideDayPeriods[index] : this.formatData.narrowDayPeriods[index];
                        }
                        if (toAppend3 == null && (periodType == DayPeriodRules.DayPeriod.MIDNIGHT || periodType == DayPeriodRules.DayPeriod.NOON)) {
                            periodType = ruleSet2.getDayPeriodForHour(hour2);
                            int index2 = periodType.ordinal();
                            if (patternCharIndex8 <= 3) {
                                toAppend3 = this.formatData.abbreviatedDayPeriods[index2];
                            } else if (patternCharIndex8 == 4 || patternCharIndex8 > 5) {
                                toAppend3 = this.formatData.wideDayPeriods[index2];
                            } else {
                                toAppend3 = this.formatData.narrowDayPeriods[index2];
                            }
                        }
                        DayPeriodRules.DayPeriod periodType2 = periodType;
                        String toAppend4 = toAppend3;
                        if (periodType2 != DayPeriodRules.DayPeriod.AM && periodType2 != DayPeriodRules.DayPeriod.PM && toAppend4 != null) {
                            stringBuffer.append(toAppend4);
                            Calendar calendar24 = calendar23;
                            patternCharIndex = patternCharIndex7;
                            value2 = value16;
                            value3 = value2;
                            capContextUsageType = capContextUsageType4;
                            break;
                        } else {
                            String str4 = toAppend4;
                            DayPeriodRules.DayPeriod dayPeriod = periodType2;
                            int i7 = hour2;
                            subFormat(stringBuffer, 'a', patternCharIndex8, i2, fieldNum, displayContext, fieldPosition, cal);
                        }
                    } else {
                        patternCharIndex7 = patternCharIndex9;
                        DayPeriodRules dayPeriodRules = ruleSet;
                        date = date3;
                        value16 = value;
                        tz = tz2;
                        bufstart = bufstart4;
                        subFormat(stringBuffer, 'a', patternCharIndex8, i2, fieldNum, displayContext, fieldPosition, cal);
                    }
                    Calendar calendar25 = cal;
                    patternCharIndex = patternCharIndex7;
                    value2 = value16;
                    value3 = value2;
                    capContextUsageType = capContextUsageType4;
                    break;
                case 37:
                    stringBuffer.append(this.formatData.getTimeSeparatorString());
                    TimeZone timeZone6 = tz2;
                    bufstart = bufstart4;
                    Calendar calendar26 = calendar;
                    long j15 = date3;
                    value2 = value;
                    patternCharIndex = patternCharIndex9;
                    value3 = value2;
                    capContextUsageType = capContextUsageType4;
                    break;
                default:
                    TimeZone timeZone7 = tz2;
                    bufstart = bufstart4;
                    Calendar calendar27 = calendar;
                    long j16 = date3;
                    value2 = value;
                    patternCharIndex = patternCharIndex9;
                    zeroPaddingNumber(currentNumberFormat, stringBuffer, value2, patternCharIndex8, Integer.MAX_VALUE);
            }
        } else if (c != 'l') {
            throw new IllegalArgumentException("Illegal pattern character '" + c + "' in \"" + this.pattern + '\"');
        }
    }

    private static void safeAppend(String[] array, int value, StringBuffer appendTo) {
        if (array != null && value >= 0 && value < array.length) {
            appendTo.append(array[value]);
        }
    }

    private static void safeAppendWithMonthPattern(String[] array, int value, StringBuffer appendTo, String monthPattern) {
        if (array != null && value >= 0 && value < array.length) {
            if (monthPattern == null) {
                appendTo.append(array[value]);
                return;
            }
            appendTo.append(SimpleFormatterImpl.formatRawPattern(monthPattern, 1, 1, array[value]));
        }
    }

    private Object[] getPatternItems() {
        if (this.patternItems != null) {
            return this.patternItems;
        }
        this.patternItems = PARSED_PATTERN_CACHE.get(this.pattern);
        if (this.patternItems != null) {
            return this.patternItems;
        }
        StringBuilder text = new StringBuilder();
        char itemType = 0;
        List<Object> items = new ArrayList<>();
        int itemLength = 1;
        boolean inQuote = false;
        boolean isPrevQuote = false;
        for (int i = 0; i < this.pattern.length(); i++) {
            char ch = this.pattern.charAt(i);
            if (ch == '\'') {
                if (isPrevQuote) {
                    text.append(PatternTokenizer.SINGLE_QUOTE);
                    isPrevQuote = false;
                } else {
                    isPrevQuote = true;
                    if (itemType != 0) {
                        items.add(new PatternItem(itemType, itemLength));
                        itemType = 0;
                    }
                }
                inQuote = !inQuote;
            } else {
                isPrevQuote = false;
                if (inQuote) {
                    text.append(ch);
                } else if (!isSyntaxChar(ch)) {
                    if (itemType != 0) {
                        items.add(new PatternItem(itemType, itemLength));
                        itemType = 0;
                    }
                    text.append(ch);
                } else if (ch == itemType) {
                    itemLength++;
                } else {
                    if (itemType != 0) {
                        items.add(new PatternItem(itemType, itemLength));
                    } else if (text.length() > 0) {
                        items.add(text.toString());
                        text.setLength(0);
                    }
                    itemType = ch;
                    itemLength = 1;
                }
            }
        }
        if (itemType != 0) {
            items.add(new PatternItem(itemType, itemLength));
        } else if (text.length() > 0) {
            items.add(text.toString());
            text.setLength(0);
        }
        this.patternItems = items.toArray(new Object[items.size()]);
        PARSED_PATTERN_CACHE.put(this.pattern, this.patternItems);
        return this.patternItems;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void zeroPaddingNumber(NumberFormat nf, StringBuffer buf, int value, int minDigits, int maxDigits) {
        if (!this.useLocalZeroPaddingNumberFormat || value < 0) {
            nf.setMinimumIntegerDigits(minDigits);
            nf.setMaximumIntegerDigits(maxDigits);
            nf.format((long) value, buf, new FieldPosition(-1));
            return;
        }
        fastZeroPaddingNumber(buf, value, minDigits, maxDigits);
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        super.setNumberFormat(newNumberFormat);
        initLocalZeroPaddingNumberFormat();
        initializeTimeZoneFormat(true);
        if (this.numberFormatters != null) {
            this.numberFormatters = null;
        }
        if (this.overrideMap != null) {
            this.overrideMap = null;
        }
    }

    private void initLocalZeroPaddingNumberFormat() {
        if (this.numberFormat instanceof DecimalFormat) {
            String[] tmpDigits = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getDigitStringsLocal();
            this.useLocalZeroPaddingNumberFormat = true;
            this.decDigits = new char[10];
            int i = 0;
            while (true) {
                if (i >= 10) {
                    break;
                } else if (tmpDigits[i].length() > 1) {
                    this.useLocalZeroPaddingNumberFormat = false;
                    break;
                } else {
                    this.decDigits[i] = tmpDigits[i].charAt(0);
                    i++;
                }
            }
        } else if (this.numberFormat instanceof DateNumberFormat) {
            this.decDigits = ((DateNumberFormat) this.numberFormat).getDigits();
            this.useLocalZeroPaddingNumberFormat = true;
        } else {
            this.useLocalZeroPaddingNumberFormat = false;
        }
        if (this.useLocalZeroPaddingNumberFormat) {
            this.decimalBuf = new char[10];
        }
    }

    private void fastZeroPaddingNumber(StringBuffer buf, int value, int minDigits, int maxDigits) {
        int limit = this.decimalBuf.length < maxDigits ? this.decimalBuf.length : maxDigits;
        int index = limit - 1;
        while (true) {
            this.decimalBuf[index] = this.decDigits[value % 10];
            value /= 10;
            if (index == 0 || value == 0) {
                int padding = minDigits - (limit - index);
            } else {
                index--;
            }
        }
        int padding2 = minDigits - (limit - index);
        while (padding2 > 0 && index > 0) {
            index--;
            this.decimalBuf[index] = this.decDigits[0];
            padding2--;
        }
        while (padding2 > 0) {
            buf.append(this.decDigits[0]);
            padding2--;
        }
        buf.append(this.decimalBuf, index, limit - index);
    }

    /* access modifiers changed from: protected */
    public String zeroPaddingNumber(long value, int minDigits, int maxDigits) {
        this.numberFormat.setMinimumIntegerDigits(minDigits);
        this.numberFormat.setMaximumIntegerDigits(maxDigits);
        return this.numberFormat.format(value);
    }

    /* access modifiers changed from: private */
    public static final boolean isNumeric(char formatChar, int count) {
        return NUMERIC_FORMAT_CHARS.indexOf(formatChar) >= 0 || (count <= 2 && NUMERIC_FORMAT_CHARS2.indexOf(formatChar) >= 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v13, resolved type: boolean} */
    /* JADX WARNING: type inference failed for: r12v5 */
    /* JADX WARNING: type inference failed for: r12v15 */
    /* JADX WARNING: type inference failed for: r12v16 */
    /* JADX WARNING: type inference failed for: r12v18 */
    /* JADX WARNING: type inference failed for: r12v20 */
    /* JADX WARNING: type inference failed for: r12v30 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x04ec  */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x04fc  */
    /* JADX WARNING: Removed duplicated region for block: B:265:0x051b  */
    public void parse(String text, Calendar cal, ParsePosition parsePos) {
        Calendar cal2;
        TimeZone backupTZ;
        Calendar resultCal;
        int pos;
        Calendar cal3;
        SimpleDateFormat simpleDateFormat;
        Calendar resultCal2;
        ? r12;
        long localMillis;
        int resolvedSavings;
        int i;
        int resolvedSavings2;
        TimeZoneTransition beforeTrs;
        TimeZoneTransition afterTrs;
        long afterT;
        int hourOfDay;
        Calendar cal4;
        Calendar resultCal3;
        Output<DayPeriodRules.DayPeriod> dayPeriod;
        Output<TimeZoneFormat.TimeType> tzTimeType;
        int i2;
        TimeZone backupTZ2;
        Object[] items;
        int pos2;
        int numericFieldStart;
        Calendar resultCal4;
        int numericFieldStart2;
        int numericFieldStart3;
        int pos3;
        int i3;
        ParsePosition parsePosition = parsePos;
        Calendar resultCal5 = cal;
        if (resultCal5 == this.calendar || cal.getType().equals(this.calendar.getType())) {
            backupTZ = null;
            resultCal = null;
            cal2 = resultCal5;
        } else {
            this.calendar.setTimeInMillis(cal.getTimeInMillis());
            TimeZone backupTZ3 = this.calendar.getTimeZone();
            this.calendar.setTimeZone(cal.getTimeZone());
            backupTZ = backupTZ3;
            resultCal = resultCal5;
            cal2 = this.calendar;
        }
        int pos4 = parsePos.getIndex();
        if (pos4 < 0) {
            parsePosition.setErrorIndex(0);
            return;
        }
        int start = pos4;
        Output<DayPeriodRules.DayPeriod> dayPeriod2 = new Output<>(null);
        Output<TimeZoneFormat.TimeType> tzTimeType2 = new Output<>(TimeZoneFormat.TimeType.UNKNOWN);
        boolean[] ambiguousYear = {false};
        int i4 = -1;
        MessageFormat numericLeapMonthFormatter = null;
        if (this.formatData.leapMonthPatterns != null && this.formatData.leapMonthPatterns.length >= 7) {
            numericLeapMonthFormatter = new MessageFormat(this.formatData.leapMonthPatterns[6], this.locale);
        }
        MessageFormat numericLeapMonthFormatter2 = numericLeapMonthFormatter;
        Object[] items2 = getPatternItems();
        int pos5 = pos4;
        int numericFieldLength = 0;
        int numericStartPos = 0;
        int numericFieldStart4 = 0;
        while (true) {
            int i5 = numericFieldStart4;
            if (i5 < items2.length) {
                if (items2[i5] instanceof PatternItem) {
                    PatternItem field = (PatternItem) items2[i5];
                    if (!field.isNumeric || i4 != -1 || i5 + 1 >= items2.length || !(items2[i5 + 1] instanceof PatternItem) || !((PatternItem) items2[i5 + 1]).isNumeric) {
                        numericFieldStart2 = i4;
                    } else {
                        numericFieldStart2 = i5;
                        numericFieldLength = field.length;
                        numericStartPos = pos5;
                    }
                    if (numericFieldStart2 != -1) {
                        int len = field.length;
                        if (numericFieldStart2 == i5) {
                            len = numericFieldLength;
                        }
                        PatternItem patternItem = field;
                        int i6 = i5;
                        tzTimeType = tzTimeType2;
                        dayPeriod = dayPeriod2;
                        int i7 = pos5;
                        int start2 = start;
                        Object[] items3 = items2;
                        resultCal3 = resultCal;
                        pos3 = subParse(text, pos5, field.type, len, true, false, ambiguousYear, cal2, numericLeapMonthFormatter2, tzTimeType);
                        if (pos3 < 0) {
                            numericFieldLength--;
                            if (numericFieldLength == 0) {
                                parsePosition.setIndex(start2);
                                parsePosition.setErrorIndex(pos3);
                                if (backupTZ != null) {
                                    this.calendar.setTimeZone(backupTZ);
                                }
                                return;
                            }
                            i4 = numericFieldStart2;
                            pos5 = numericStartPos;
                            start = start2;
                            tzTimeType2 = tzTimeType;
                            dayPeriod2 = dayPeriod;
                            items2 = items3;
                            resultCal4 = resultCal3;
                            numericFieldStart4 = i4;
                        } else {
                            numericFieldStart3 = numericFieldStart2;
                            start = start2;
                            cal4 = cal2;
                            i3 = i6;
                            items = items3;
                            backupTZ2 = backupTZ;
                        }
                    } else {
                        int i8 = i5;
                        tzTimeType = tzTimeType2;
                        dayPeriod = dayPeriod2;
                        int pos6 = pos5;
                        Object[] items4 = items2;
                        resultCal3 = resultCal;
                        int start3 = start;
                        PatternItem field2 = field;
                        if (field2.type != 'l') {
                            numericFieldStart3 = -1;
                            int s = pos6;
                            PatternItem patternItem2 = field2;
                            int start4 = start3;
                            cal4 = cal2;
                            backupTZ2 = backupTZ;
                            pos3 = subParse(text, pos6, field2.type, field2.length, false, true, ambiguousYear, cal2, numericLeapMonthFormatter2, tzTimeType, dayPeriod);
                            if (pos3 >= 0) {
                                items = items4;
                                start = start4;
                                i3 = i8;
                            } else if (pos3 == ISOSpecialEra) {
                                pos3 = s;
                                items = items4;
                                if (i8 + 1 < items.length) {
                                    try {
                                        String patl = (String) items[i8 + 1];
                                        if (patl == null) {
                                            patl = (String) items[i8 + 1];
                                        }
                                        int plen = patl.length();
                                        int idx = 0;
                                        while (idx < plen && PatternProps.isWhiteSpace(patl.charAt(idx))) {
                                            idx++;
                                        }
                                        if (idx == plen) {
                                            i8++;
                                        }
                                        i3 = i8;
                                        start = start4;
                                    } catch (ClassCastException e) {
                                        parsePosition.setIndex(start4);
                                        parsePosition.setErrorIndex(s);
                                        if (backupTZ2 != null) {
                                            this.calendar.setTimeZone(backupTZ2);
                                        }
                                        return;
                                    }
                                } else {
                                    start = start4;
                                    i3 = i8;
                                }
                            } else {
                                parsePosition.setIndex(start4);
                                parsePosition.setErrorIndex(s);
                                if (backupTZ2 != null) {
                                    this.calendar.setTimeZone(backupTZ2);
                                }
                                return;
                            }
                        } else {
                            start = start3;
                            cal4 = cal2;
                            items = items4;
                            backupTZ2 = backupTZ;
                            numericFieldStart3 = numericFieldStart2;
                            i3 = i8;
                            pos3 = pos6;
                        }
                    }
                    i2 = i3;
                    pos2 = pos3;
                    numericFieldStart = numericFieldStart3;
                } else {
                    i2 = i5;
                    tzTimeType = tzTimeType2;
                    dayPeriod = dayPeriod2;
                    items = items2;
                    resultCal3 = resultCal;
                    cal4 = cal2;
                    backupTZ2 = backupTZ;
                    numericFieldStart = -1;
                    boolean[] complete = new boolean[1];
                    pos2 = matchLiteral(text, pos5, items, i5, complete);
                    if (!complete[0]) {
                        parsePosition.setIndex(start);
                        parsePosition.setErrorIndex(pos2);
                        if (backupTZ2 != null) {
                            this.calendar.setTimeZone(backupTZ2);
                        }
                        return;
                    }
                }
                pos5 = pos2;
                items2 = items;
                backupTZ = backupTZ2;
                tzTimeType2 = tzTimeType;
                dayPeriod2 = dayPeriod;
                resultCal4 = resultCal3;
                cal2 = cal4;
                i4 = numericFieldStart;
                numericFieldStart4 = i2 + 1;
            } else {
                int i9 = i5;
                Output<TimeZoneFormat.TimeType> tzTimeType3 = tzTimeType2;
                Output<DayPeriodRules.DayPeriod> dayPeriod3 = dayPeriod2;
                Object[] items5 = items2;
                Calendar resultCal6 = resultCal;
                Calendar cal5 = cal2;
                TimeZone backupTZ4 = backupTZ;
                int pos7 = pos5;
                if (pos7 >= text.length()) {
                    String str = text;
                } else if (text.charAt(pos7) == '.' && getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE) && items5.length != 0) {
                    Object lastItem = items5[items5.length - 1];
                    if ((lastItem instanceof PatternItem) && !((PatternItem) lastItem).isNumeric) {
                        pos7++;
                    }
                }
                Output<DayPeriodRules.DayPeriod> dayPeriod4 = dayPeriod3;
                if (dayPeriod4.value != null) {
                    DayPeriodRules ruleSet = DayPeriodRules.getInstance(getLocale());
                    cal3 = cal5;
                    if (cal3.isSet(10) || cal3.isSet(11)) {
                        pos = pos7;
                        if (cal3.isSet(11)) {
                            hourOfDay = cal3.get(11);
                        } else {
                            hourOfDay = cal3.get(10);
                            if (hourOfDay == 0) {
                                hourOfDay = 12;
                            }
                        }
                        if (hourOfDay == 0) {
                            int numericFieldStart5 = i4;
                        } else if (13 > hourOfDay || hourOfDay > 23) {
                            if (hourOfDay == 12) {
                                hourOfDay = 0;
                            }
                            int i10 = i4;
                            int i11 = hourOfDay;
                            double hoursAheadMidPoint = (((double) hourOfDay) + (((double) cal3.get(12)) / 60.0d)) - ruleSet.getMidPointForDayPeriod(dayPeriod4.value);
                            if (-6.0d > hoursAheadMidPoint || hoursAheadMidPoint >= 6.0d) {
                                cal3.set(9, 1);
                            } else {
                                cal3.set(9, 0);
                            }
                        } else {
                            int i12 = i4;
                        }
                        cal3.set(11, hourOfDay);
                    } else {
                        pos = pos7;
                        double midPoint = ruleSet.getMidPointForDayPeriod(dayPeriod4.value);
                        int midPointHour = (int) midPoint;
                        int midPointMinute = midPoint - ((double) midPointHour) > 0.0d ? 30 : 0;
                        cal3.set(11, midPointHour);
                        cal3.set(12, midPointMinute);
                        int i13 = i4;
                    }
                } else {
                    int numericFieldStart6 = i4;
                    pos = pos7;
                    cal3 = cal5;
                }
                int pos8 = pos;
                parsePosition.setIndex(pos8);
                Output<TimeZoneFormat.TimeType> tzTimeType4 = tzTimeType3;
                try {
                    TimeZoneFormat.TimeType tztype = tzTimeType4.value;
                    if (!ambiguousYear[0]) {
                        try {
                            if (tztype == TimeZoneFormat.TimeType.UNKNOWN) {
                                Output<TimeZoneFormat.TimeType> output = tzTimeType4;
                                Output<DayPeriodRules.DayPeriod> output2 = dayPeriod4;
                                simpleDateFormat = this;
                                resultCal2 = resultCal6;
                                if (resultCal2 != null) {
                                    resultCal2.setTimeZone(cal3.getTimeZone());
                                    resultCal2.setTimeInMillis(cal3.getTimeInMillis());
                                }
                                if (backupTZ4 != null) {
                                    simpleDateFormat.calendar.setTimeZone(backupTZ4);
                                }
                                return;
                            }
                        } catch (IllegalArgumentException e2) {
                            Output<TimeZoneFormat.TimeType> output3 = tzTimeType4;
                            Output<DayPeriodRules.DayPeriod> output4 = dayPeriod4;
                            Calendar calendar = resultCal6;
                            simpleDateFormat = this;
                            parsePosition.setErrorIndex(pos8);
                            parsePosition.setIndex(start);
                            if (backupTZ4 != null) {
                            }
                            return;
                        }
                    }
                    if (ambiguousYear[0]) {
                        try {
                            simpleDateFormat = this;
                        } catch (IllegalArgumentException e3) {
                            simpleDateFormat = this;
                            parsePosition.setErrorIndex(pos8);
                            parsePosition.setIndex(start);
                            if (backupTZ4 != null) {
                            }
                            return;
                        }
                        try {
                            if (((Calendar) cal3.clone()).getTime().before(getDefaultCenturyStart())) {
                                cal3.set(1, getDefaultCenturyStartYear() + 100);
                            }
                        } catch (IllegalArgumentException e4) {
                            parsePosition.setErrorIndex(pos8);
                            parsePosition.setIndex(start);
                            if (backupTZ4 != null) {
                            }
                            return;
                        }
                    } else {
                        simpleDateFormat = this;
                    }
                    try {
                        if (tztype != TimeZoneFormat.TimeType.UNKNOWN) {
                            try {
                                Calendar copy = (Calendar) cal3.clone();
                                TimeZone tz = copy.getTimeZone();
                                BasicTimeZone btz = null;
                                if (tz instanceof BasicTimeZone) {
                                    btz = (BasicTimeZone) tz;
                                }
                                copy.set(15, 0);
                                copy.set(16, 0);
                                long localMillis2 = copy.getTimeInMillis();
                                int[] offsets = new int[2];
                                if (btz != null) {
                                    if (tztype == TimeZoneFormat.TimeType.STANDARD) {
                                        btz.getOffsetFromLocal(localMillis2, 1, 1, offsets);
                                    } else {
                                        btz.getOffsetFromLocal(localMillis2, 3, 3, offsets);
                                    }
                                    Output<TimeZoneFormat.TimeType> output5 = tzTimeType4;
                                    Calendar calendar2 = copy;
                                    Output<DayPeriodRules.DayPeriod> output6 = dayPeriod4;
                                    localMillis = localMillis2;
                                    r12 = 1;
                                } else {
                                    Output<TimeZoneFormat.TimeType> output7 = tzTimeType4;
                                    Calendar calendar3 = copy;
                                    localMillis = localMillis2;
                                    try {
                                        tz.getOffset(localMillis, true, offsets);
                                        if (tztype == TimeZoneFormat.TimeType.STANDARD) {
                                            try {
                                                if (offsets[1] != 0) {
                                                    r12 = 1;
                                                    Output<DayPeriodRules.DayPeriod> output8 = dayPeriod4;
                                                    try {
                                                        tz.getOffset(localMillis - 86400000, r12, offsets);
                                                        r12 = r12;
                                                    } catch (IllegalArgumentException e5) {
                                                        parsePosition.setErrorIndex(pos8);
                                                        parsePosition.setIndex(start);
                                                        if (backupTZ4 != null) {
                                                        }
                                                        return;
                                                    }
                                                }
                                            } catch (IllegalArgumentException e6) {
                                                parsePosition.setErrorIndex(pos8);
                                                parsePosition.setIndex(start);
                                                if (backupTZ4 != null) {
                                                }
                                                return;
                                            }
                                        }
                                        if (tztype == TimeZoneFormat.TimeType.DAYLIGHT) {
                                            r12 = 1;
                                            if (offsets[1] != 0) {
                                            }
                                            Output<DayPeriodRules.DayPeriod> output82 = dayPeriod4;
                                            tz.getOffset(localMillis - 86400000, r12, offsets);
                                            r12 = r12;
                                        } else {
                                            Output<DayPeriodRules.DayPeriod> output9 = dayPeriod4;
                                            r12 = 1;
                                        }
                                    } catch (IllegalArgumentException e7) {
                                        Output<DayPeriodRules.DayPeriod> output10 = dayPeriod4;
                                        Calendar calendar4 = resultCal6;
                                        parsePosition.setErrorIndex(pos8);
                                        parsePosition.setIndex(start);
                                        if (backupTZ4 != null) {
                                            simpleDateFormat.calendar.setTimeZone(backupTZ4);
                                        }
                                        return;
                                    }
                                }
                                int resolvedSavings3 = offsets[r12];
                                if (tztype == TimeZoneFormat.TimeType.STANDARD) {
                                    if (offsets[r12] != 0) {
                                        resolvedSavings = 0;
                                        TimeZoneFormat.TimeType timeType = tztype;
                                        long j = localMillis;
                                        cal3.set(15, offsets[0]);
                                        cal3.set(16, resolvedSavings);
                                    } else {
                                        TimeZoneFormat.TimeType timeType2 = tztype;
                                        long j2 = localMillis;
                                        i = resolvedSavings3;
                                    }
                                } else if (offsets[r12] == 0) {
                                    if (btz != null) {
                                        int i14 = resolvedSavings3;
                                        long time = localMillis + ((long) offsets[0]);
                                        long beforeT = time;
                                        int beforeSav = 0;
                                        TimeZoneFormat.TimeType timeType3 = tztype;
                                        long j3 = localMillis;
                                        long afterT2 = time;
                                        int afterSav = 0;
                                        while (true) {
                                            beforeTrs = btz.getPreviousTransition(afterT2, true);
                                            if (beforeTrs != null) {
                                                afterT2 = beforeTrs.getTime() - 1;
                                                beforeSav = beforeTrs.getFrom().getDSTSavings();
                                                if (beforeSav != 0) {
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                        long j4 = afterT2;
                                        long afterT3 = beforeT;
                                        long beforeT2 = j4;
                                        while (true) {
                                            afterTrs = btz.getNextTransition(afterT3, false);
                                            if (afterTrs == null) {
                                                afterT = afterT3;
                                                break;
                                            }
                                            afterT = afterTrs.getTime();
                                            afterSav = afterTrs.getTo().getDSTSavings();
                                            if (afterSav != 0) {
                                                break;
                                            }
                                            afterT3 = afterT;
                                        }
                                        if (beforeTrs == null || afterTrs == null) {
                                            if (beforeTrs != null && beforeSav != 0) {
                                                resolvedSavings2 = beforeSav;
                                            } else if (afterTrs == null || afterSav == 0) {
                                                resolvedSavings2 = btz.getDSTSavings();
                                            } else {
                                                resolvedSavings2 = afterSav;
                                            }
                                        } else if (time - beforeT2 > afterT - time) {
                                            resolvedSavings2 = afterSav;
                                        } else {
                                            resolvedSavings2 = beforeSav;
                                        }
                                    } else {
                                        long j5 = localMillis;
                                        int i15 = resolvedSavings3;
                                        resolvedSavings2 = tz.getDSTSavings();
                                    }
                                    resolvedSavings = resolvedSavings2;
                                    if (resolvedSavings == 0) {
                                        resolvedSavings = 3600000;
                                    }
                                    cal3.set(15, offsets[0]);
                                    cal3.set(16, resolvedSavings);
                                } else {
                                    long j6 = localMillis;
                                    i = resolvedSavings3;
                                }
                                resolvedSavings = i;
                                cal3.set(15, offsets[0]);
                                cal3.set(16, resolvedSavings);
                            } catch (IllegalArgumentException e8) {
                                Output<TimeZoneFormat.TimeType> output11 = tzTimeType4;
                                Output<DayPeriodRules.DayPeriod> output12 = dayPeriod4;
                                Calendar calendar5 = resultCal6;
                                parsePosition.setErrorIndex(pos8);
                                parsePosition.setIndex(start);
                                if (backupTZ4 != null) {
                                }
                                return;
                            }
                        } else {
                            Output<DayPeriodRules.DayPeriod> output13 = dayPeriod4;
                        }
                        resultCal2 = resultCal6;
                        if (resultCal2 != null) {
                        }
                        if (backupTZ4 != null) {
                        }
                        return;
                    } catch (IllegalArgumentException e9) {
                        Output<TimeZoneFormat.TimeType> output14 = tzTimeType4;
                        Output<DayPeriodRules.DayPeriod> output15 = dayPeriod4;
                        Calendar calendar6 = resultCal6;
                        parsePosition.setErrorIndex(pos8);
                        parsePosition.setIndex(start);
                        if (backupTZ4 != null) {
                        }
                        return;
                    }
                } catch (IllegalArgumentException e10) {
                    Output<TimeZoneFormat.TimeType> output16 = tzTimeType4;
                    Output<DayPeriodRules.DayPeriod> output17 = dayPeriod4;
                    Calendar calendar7 = resultCal6;
                    simpleDateFormat = this;
                    parsePosition.setErrorIndex(pos8);
                    parsePosition.setIndex(start);
                    if (backupTZ4 != null) {
                    }
                    return;
                }
            }
        }
    }

    private int matchLiteral(String text, int pos, Object[] items, int itemIndex, boolean[] complete) {
        String str = text;
        Object[] objArr = items;
        int i = itemIndex;
        int originalPos = pos;
        String patternLiteral = (String) objArr[i];
        int plen = patternLiteral.length();
        int tlen = text.length();
        int pos2 = pos;
        int idx = 0;
        while (idx < plen && pos2 < tlen) {
            char pch = patternLiteral.charAt(idx);
            char ich = str.charAt(pos2);
            if (!PatternProps.isWhiteSpace(pch) || !PatternProps.isWhiteSpace(ich)) {
                if (pch != ich) {
                    if (ich != '.' || pos2 != originalPos || i <= 0 || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE)) {
                        if ((pch != ' ' && pch != '.') || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE)) {
                            if (pos2 == originalPos || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH)) {
                                break;
                            }
                            idx++;
                        } else {
                            idx++;
                        }
                    } else {
                        Object before = objArr[i - 1];
                        if (!(before instanceof PatternItem) || ((PatternItem) before).isNumeric) {
                            break;
                        }
                        pos2++;
                    }
                }
            } else {
                while (idx + 1 < plen && PatternProps.isWhiteSpace(patternLiteral.charAt(idx + 1))) {
                    idx++;
                }
                while (pos2 + 1 < tlen && PatternProps.isWhiteSpace(str.charAt(pos2 + 1))) {
                    pos2++;
                }
            }
            idx++;
            pos2++;
        }
        complete[0] = idx == plen;
        if (complete[0] || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE) || i <= 0 || i >= objArr.length - 1 || originalPos >= tlen) {
            return pos2;
        }
        Object before2 = objArr[i - 1];
        Object after = objArr[i + 1];
        if (!(before2 instanceof PatternItem) || !(after instanceof PatternItem)) {
            return pos2;
        }
        if (DATE_PATTERN_TYPE.contains((int) ((PatternItem) before2).type) == DATE_PATTERN_TYPE.contains((int) ((PatternItem) after).type)) {
            return pos2;
        }
        int newPos = originalPos;
        while (PatternProps.isWhiteSpace(str.charAt(newPos))) {
            newPos++;
        }
        complete[0] = newPos > originalPos;
        return newPos;
    }

    /* access modifiers changed from: protected */
    public int matchString(String text, int start, int field, String[] data, Calendar cal) {
        return matchString(text, start, field, data, null, cal);
    }

    @Deprecated
    private int matchString(String text, int start, int field, String[] data, String monthPattern, Calendar cal) {
        String str = text;
        int i = start;
        int i2 = field;
        String[] strArr = data;
        String str2 = monthPattern;
        Calendar calendar = cal;
        int i3 = 0;
        int count = strArr.length;
        if (i2 == 7) {
            i3 = 1;
        }
        int isLeapMonth = 0;
        int bestMatch = -1;
        int bestMatchLength = 0;
        for (int i4 = i3; i4 < count; i4++) {
            int length = strArr[i4].length();
            if (length > bestMatchLength) {
                int regionMatchesWithOptionalDot = regionMatchesWithOptionalDot(str, i, strArr[i4], length);
                int matchLength = regionMatchesWithOptionalDot;
                if (regionMatchesWithOptionalDot >= 0) {
                    bestMatch = i4;
                    bestMatchLength = matchLength;
                    isLeapMonth = 0;
                }
            }
            if (str2 != null) {
                String leapMonthName = SimpleFormatterImpl.formatRawPattern(str2, 1, 1, strArr[i4]);
                int length2 = leapMonthName.length();
                if (length2 > bestMatchLength) {
                    int regionMatchesWithOptionalDot2 = regionMatchesWithOptionalDot(str, i, leapMonthName, length2);
                    int matchLength2 = regionMatchesWithOptionalDot2;
                    if (regionMatchesWithOptionalDot2 >= 0) {
                        bestMatch = i4;
                        bestMatchLength = matchLength2;
                        isLeapMonth = 1;
                    }
                }
            }
        }
        if (bestMatch < 0) {
            return ~i;
        }
        if (i2 >= 0) {
            if (i2 == 1) {
                bestMatch++;
            }
            calendar.set(i2, bestMatch);
            if (str2 != null) {
                calendar.set(22, isLeapMonth);
            }
        }
        return i + bestMatchLength;
    }

    private int regionMatchesWithOptionalDot(String text, int start, String data, int length) {
        if (text.regionMatches(true, start, data, 0, length)) {
            return length;
        }
        if (data.length() > 0 && data.charAt(data.length() - 1) == '.') {
            if (text.regionMatches(true, start, data, 0, length - 1)) {
                return length - 1;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int matchQuarterString(String text, int start, int field, String[] data, Calendar cal) {
        int count = data.length;
        int bestMatchLength = 0;
        int bestMatch = -1;
        for (int i = 0; i < count; i++) {
            int length = data[i].length();
            if (length > bestMatchLength) {
                int regionMatchesWithOptionalDot = regionMatchesWithOptionalDot(text, start, data[i], length);
                int matchLength = regionMatchesWithOptionalDot;
                if (regionMatchesWithOptionalDot >= 0) {
                    bestMatch = i;
                    bestMatchLength = matchLength;
                }
            }
        }
        if (bestMatch < 0) {
            return -start;
        }
        cal.set(field, bestMatch * 3);
        return start + bestMatchLength;
    }

    private int matchDayPeriodString(String text, int start, String[] data, int dataLength, Output<DayPeriodRules.DayPeriod> dayPeriod) {
        int bestMatchLength = 0;
        int bestMatch = -1;
        for (int i = 0; i < dataLength; i++) {
            if (data[i] != null) {
                int length = data[i].length();
                if (length > bestMatchLength) {
                    int regionMatchesWithOptionalDot = regionMatchesWithOptionalDot(text, start, data[i], length);
                    int matchLength = regionMatchesWithOptionalDot;
                    if (regionMatchesWithOptionalDot >= 0) {
                        bestMatch = i;
                        bestMatchLength = matchLength;
                    }
                }
            }
        }
        if (bestMatch < 0) {
            return -start;
        }
        dayPeriod.value = DayPeriodRules.DayPeriod.VALUES[bestMatch];
        return start + bestMatchLength;
    }

    /* access modifiers changed from: protected */
    public int subParse(String text, int start, char ch, int count, boolean obeyCount, boolean allowNegative, boolean[] ambiguousYear, Calendar cal) {
        return subParse(text, start, ch, count, obeyCount, allowNegative, ambiguousYear, cal, null, null);
    }

    private int subParse(String text, int start, char ch, int count, boolean obeyCount, boolean allowNegative, boolean[] ambiguousYear, Calendar cal, MessageFormat numericLeapMonthFormatter, Output<TimeZoneFormat.TimeType> output) {
        return subParse(text, start, ch, count, obeyCount, allowNegative, ambiguousYear, cal, null, null, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:134:0x027b, code lost:
        if (r14 == 4) goto L_0x027f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x0385, code lost:
        if (r6 > r12.formatData.shortYearNames.length) goto L_0x038a;
     */
    @Deprecated
    private int subParse(String text, int start, char ch, int count, boolean obeyCount, boolean allowNegative, boolean[] ambiguousYear, Calendar cal, MessageFormat numericLeapMonthFormatter, Output<TimeZoneFormat.TimeType> tzTimeType, Output<DayPeriodRules.DayPeriod> dayPeriod) {
        Number number;
        boolean isChineseCalendar;
        int field;
        int start2;
        int patternCharIndex;
        ParsePosition pos;
        NumberFormat currentNumberFormat;
        int ps;
        int value;
        int i;
        int start3;
        int patternCharIndex2;
        int i2;
        String str;
        String str2;
        int i3;
        String str3;
        String str4;
        Calendar calendar;
        int start4;
        int value2;
        int field2;
        int field3;
        int i4;
        int start5;
        int field4;
        int i5;
        int start6;
        int newStart;
        int value3;
        int start7;
        int start8;
        int start9;
        TimeZoneFormat.Style style;
        int value4;
        TimeZoneFormat.Style style2;
        TimeZoneFormat.Style style3;
        int i6;
        Number number2;
        boolean parsedNumericLeapMonth;
        boolean parsedNumericLeapMonth2;
        Number number3;
        String str5 = text;
        int start10 = count;
        boolean z = allowNegative;
        Calendar calendar2 = cal;
        MessageFormat messageFormat = numericLeapMonthFormatter;
        Output<TimeZoneFormat.TimeType> output = tzTimeType;
        int value5 = 0;
        ParsePosition pos2 = new ParsePosition(0);
        int patternCharIndex3 = getIndexFromChar(ch);
        if (patternCharIndex3 == -1) {
            return ~start;
        }
        int start11 = start;
        NumberFormat currentNumberFormat2 = getNumberFormat(ch);
        int field5 = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex3];
        if (messageFormat != null) {
            number = null;
            messageFormat.setFormatByArgumentIndex(0, currentNumberFormat2);
        } else {
            number = null;
        }
        NumberFormat currentNumberFormat3 = currentNumberFormat2;
        boolean isChineseCalendar2 = cal.getType().equals("chinese") || cal.getType().equals("dangi");
        while (true) {
            isChineseCalendar = isChineseCalendar2;
            if (start11 >= text.length()) {
                return ~start11;
            }
            int c = UTF16.charAt(str5, start11);
            if (!UCharacter.isUWhiteSpace(c) || !PatternProps.isWhiteSpace(c)) {
                pos2.setIndex(start11);
            } else {
                start11 += UTF16.getCharCount(c);
                isChineseCalendar2 = isChineseCalendar;
            }
        }
        pos2.setIndex(start11);
        if (patternCharIndex3 == 4 || patternCharIndex3 == 15 || ((patternCharIndex3 == 2 && start10 <= 2) || patternCharIndex3 == 26 || patternCharIndex3 == 19 || patternCharIndex3 == 25 || patternCharIndex3 == 1 || patternCharIndex3 == 18 || patternCharIndex3 == 30 || ((patternCharIndex3 == 0 && isChineseCalendar) || patternCharIndex3 == 27 || patternCharIndex3 == 28 || patternCharIndex3 == 8))) {
            if (messageFormat == null) {
                parsedNumericLeapMonth = false;
                parsedNumericLeapMonth2 = true;
            } else if (patternCharIndex3 == 2 || patternCharIndex3 == 26) {
                Object[] args = messageFormat.parse(str5, pos2);
                parsedNumericLeapMonth = false;
                if (args == null || pos2.getIndex() <= start11 || !(args[0] instanceof Number)) {
                    parsedNumericLeapMonth2 = true;
                    pos2.setIndex(start11);
                    calendar2.set(22, 0);
                } else {
                    Object[] objArr = args;
                    parsedNumericLeapMonth2 = true;
                    calendar2.set(22, 1);
                    parsedNumericLeapMonth = true;
                    number = (Number) args[0];
                }
            } else {
                parsedNumericLeapMonth = false;
                parsedNumericLeapMonth2 = true;
            }
            if (!parsedNumericLeapMonth) {
                if (!obeyCount) {
                    start2 = start11;
                    patternCharIndex = patternCharIndex3;
                    field = field5;
                    pos = pos2;
                    currentNumberFormat = currentNumberFormat3;
                    number3 = parseInt(str5, pos, z, currentNumberFormat);
                } else if (start11 + start10 > text.length()) {
                    return ~start11;
                } else {
                    field = field5;
                    int field6 = parsedNumericLeapMonth2;
                    start2 = start11;
                    patternCharIndex = patternCharIndex3;
                    pos = pos2;
                    currentNumberFormat = currentNumberFormat3;
                    number3 = parseInt(str5, start10, pos2, z, currentNumberFormat);
                }
                if (number3 == null && !allowNumericFallback(patternCharIndex)) {
                    return ~start2;
                }
            } else {
                start2 = start11;
                patternCharIndex = patternCharIndex3;
                field = field5;
                pos = pos2;
                currentNumberFormat = currentNumberFormat3;
                number3 = number;
            }
            if (number3 != null) {
                value5 = number3.intValue();
            }
            number = number3;
        } else {
            start2 = start11;
            patternCharIndex = patternCharIndex3;
            field = field5;
            pos = pos2;
            currentNumberFormat = currentNumberFormat3;
        }
        switch (patternCharIndex) {
            case 0:
                Calendar calendar3 = calendar2;
                int field7 = field;
                ParsePosition pos3 = pos;
                Output<TimeZoneFormat.TimeType> output2 = tzTimeType;
                if (isChineseCalendar) {
                    calendar3.set(0, value5);
                    return pos3.getIndex();
                }
                if (start10 == 5) {
                    int i7 = field7;
                    int i8 = value5;
                    ps = matchString(str5, start2, 0, this.formatData.narrowEras, null, calendar3);
                } else {
                    int i9 = value5;
                    if (start10 == 4) {
                        ps = matchString(str5, start2, 0, this.formatData.eraNames, null, calendar3);
                    } else {
                        ps = matchString(str5, start2, 0, this.formatData.eras, null, calendar3);
                    }
                }
                if (ps == (~start2)) {
                    ps = ISOSpecialEra;
                }
                return ps;
            case 1:
            case 18:
                int start12 = start2;
                Calendar calendar4 = calendar2;
                int field8 = field;
                ParsePosition pos4 = pos;
                Output<TimeZoneFormat.TimeType> output3 = tzTimeType;
                if (this.override != null && ((this.override.compareTo("hebr") == 0 || this.override.indexOf("y=hebr") >= 0) && value5 < 1000)) {
                    value5 += HEBREW_CAL_CUR_MILLENIUM_START_YEAR;
                    int i10 = start12;
                } else if (start10 == 2) {
                    if (countDigits(str5, start12, pos4.getIndex()) == 2 && cal.haveDefaultCentury()) {
                        int i11 = 100;
                        int ambiguousTwoDigitYear = getDefaultCenturyStartYear() % 100;
                        ambiguousYear[0] = value5 == ambiguousTwoDigitYear;
                        int defaultCenturyStartYear2 = (getDefaultCenturyStartYear() / 100) * 100;
                        if (value5 >= ambiguousTwoDigitYear) {
                            i11 = 0;
                        }
                        value5 += defaultCenturyStartYear2 + i11;
                    }
                }
                calendar4.set(field8, value5);
                if (DelayedHebrewMonthCheck) {
                    if (!HebrewCalendar.isLeapYear(value5)) {
                        calendar4.add(2, 1);
                    }
                    DelayedHebrewMonthCheck = false;
                }
                return pos4.getIndex();
            case 2:
            case 26:
                int value6 = value5;
                int patternCharIndex4 = patternCharIndex;
                int start13 = start2;
                Calendar calendar5 = calendar2;
                int field9 = field;
                ParsePosition pos5 = pos;
                Output<TimeZoneFormat.TimeType> output4 = tzTimeType;
                if (start10 <= 2) {
                    i = 2;
                    value = value6;
                    int i12 = start13;
                    int i13 = patternCharIndex4;
                } else if (number == null || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC)) {
                    boolean haveMonthPat = this.formatData.leapMonthPatterns != null && this.formatData.leapMonthPatterns.length >= 7;
                    int newStart2 = 0;
                    if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 4) {
                        int patternCharIndex5 = patternCharIndex4;
                        if (patternCharIndex5 == 2) {
                            String[] strArr = this.formatData.months;
                            if (haveMonthPat) {
                                str4 = this.formatData.leapMonthPatterns[0];
                            } else {
                                str4 = null;
                            }
                            patternCharIndex2 = patternCharIndex5;
                            int i14 = field9;
                            int i15 = value6;
                            start3 = start13;
                            i3 = matchString(str5, start13, 2, strArr, str4, calendar5);
                        } else {
                            patternCharIndex2 = patternCharIndex5;
                            int i16 = field9;
                            int i17 = value6;
                            start3 = start13;
                            String[] strArr2 = this.formatData.standaloneMonths;
                            if (haveMonthPat) {
                                str3 = this.formatData.leapMonthPatterns[3];
                            } else {
                                str3 = null;
                            }
                            i3 = matchString(str5, start3, 2, strArr2, str3, calendar5);
                        }
                        newStart2 = i3;
                        if (newStart2 > 0) {
                            return newStart2;
                        }
                    } else {
                        int i18 = field9;
                        int i19 = value6;
                        start3 = start13;
                        patternCharIndex2 = patternCharIndex4;
                    }
                    if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && start10 != 3) {
                        return newStart2;
                    }
                    if (patternCharIndex2 == 2) {
                        String[] strArr3 = this.formatData.shortMonths;
                        if (haveMonthPat) {
                            str2 = this.formatData.leapMonthPatterns[1];
                        } else {
                            str2 = null;
                        }
                        i2 = matchString(str5, start3, 2, strArr3, str2, calendar5);
                    } else {
                        String[] strArr4 = this.formatData.standaloneShortMonths;
                        if (haveMonthPat) {
                            str = this.formatData.leapMonthPatterns[4];
                        } else {
                            str = null;
                        }
                        i2 = matchString(str5, start3, 2, strArr4, str, calendar5);
                    }
                    return i2;
                } else {
                    int i20 = field9;
                    i = 2;
                    value = value6;
                    int i21 = start13;
                    int i22 = patternCharIndex4;
                }
                int start14 = value;
                calendar5.set(i, start14 - 1);
                if (cal.getType().equals("hebrew") && start14 >= 6) {
                    if (!calendar5.isSet(1)) {
                        DelayedHebrewMonthCheck = true;
                    } else if (!HebrewCalendar.isLeapYear(calendar5.get(1))) {
                        calendar5.set(i, start14);
                    }
                }
                return pos5.getIndex();
            case 4:
                int value7 = value5;
                int i23 = patternCharIndex;
                int value8 = start2;
                Calendar calendar6 = calendar2;
                int i24 = field;
                ParsePosition pos6 = pos;
                Output<TimeZoneFormat.TimeType> output5 = tzTimeType;
                if (value7 == calendar6.getMaximum(11) + 1) {
                    value7 = 0;
                }
                calendar6.set(11, value7);
                return pos6.getIndex();
            case 8:
                int value9 = value5;
                int i25 = patternCharIndex;
                Calendar calendar7 = calendar2;
                int i26 = field;
                ParsePosition pos7 = pos;
                Output<TimeZoneFormat.TimeType> output6 = tzTimeType;
                int i27 = countDigits(str5, start2, pos7.getIndex());
                if (i27 < 3) {
                    while (i27 < 3) {
                        value9 *= 10;
                        i27++;
                    }
                } else {
                    int a = 1;
                    while (true) {
                        int a2 = a;
                        if (i27 > 3) {
                            a = a2 * 10;
                            i27--;
                        } else {
                            value9 /= a2;
                        }
                    }
                }
                calendar7.set(14, value9);
                return pos7.getIndex();
            case 9:
                int i28 = value5;
                int i29 = patternCharIndex;
                field2 = start2;
                calendar = calendar2;
                start4 = 4;
                value2 = 6;
                ParsePosition parsePosition = pos;
                field3 = field;
                Output<TimeZoneFormat.TimeType> output7 = tzTimeType;
                i4 = 3;
                break;
            case 14:
                int value10 = value5;
                int i30 = patternCharIndex;
                int start15 = start2;
                Calendar calendar8 = calendar2;
                int field10 = field;
                ParsePosition parsePosition2 = pos;
                Output<TimeZoneFormat.TimeType> output8 = tzTimeType;
                if (this.formatData.ampmsNarrow == null || start10 < 5 || getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH)) {
                    start6 = start15;
                    int i31 = value10;
                    newStart = 5;
                    int i32 = field10;
                    int matchString = matchString(str5, start6, 9, this.formatData.ampms, null, calendar8);
                    int newStart3 = matchString;
                    if (matchString > 0) {
                        return newStart3;
                    }
                    int i33 = newStart3;
                } else {
                    start6 = start15;
                    int i34 = value10;
                    int i35 = field10;
                    newStart = 5;
                }
                if (this.formatData.ampmsNarrow != null && (start10 >= newStart || getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH))) {
                    int matchString2 = matchString(str5, start6, 9, this.formatData.ampmsNarrow, null, calendar8);
                    int newStart4 = matchString2;
                    if (matchString2 > 0) {
                        return newStart4;
                    }
                }
                return ~start6;
            case 15:
                int value11 = value5;
                int i36 = patternCharIndex;
                int i37 = start2;
                Calendar calendar9 = calendar2;
                int i38 = field;
                ParsePosition pos8 = pos;
                Output<TimeZoneFormat.TimeType> output9 = tzTimeType;
                if (value11 == calendar9.getLeastMaximum(10) + 1) {
                    value11 = 0;
                }
                calendar9.set(10, value11);
                return pos8.getIndex();
            case 17:
                int i39 = value5;
                int i40 = patternCharIndex;
                int start16 = start2;
                Calendar calendar10 = calendar2;
                int i41 = field;
                ParsePosition pos9 = pos;
                TimeZone tz = tzFormat().parse(start10 < 4 ? TimeZoneFormat.Style.SPECIFIC_SHORT : TimeZoneFormat.Style.SPECIFIC_LONG, str5, pos9, tzTimeType);
                if (tz == null) {
                    return ~start16;
                }
                calendar10.setTimeZone(tz);
                return pos9.getIndex();
            case 19:
                int i42 = patternCharIndex;
                int start17 = start2;
                calendar = calendar2;
                int field11 = field;
                start4 = 4;
                int value12 = value5;
                ParsePosition pos10 = pos;
                Output<TimeZoneFormat.TimeType> output10 = tzTimeType;
                value2 = 6;
                if (start10 > 2 && (number == null || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC))) {
                    int i43 = value12;
                    field3 = field11;
                    field2 = start17;
                    i4 = 3;
                    break;
                } else {
                    calendar.set(field11, value12);
                    return pos10.getIndex();
                }
            case 23:
                int i44 = value5;
                int i45 = patternCharIndex;
                int start18 = start2;
                Calendar calendar11 = calendar2;
                int i46 = field;
                ParsePosition pos11 = pos;
                TimeZone tz2 = tzFormat().parse(start10 < 4 ? TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL : start10 == 5 ? TimeZoneFormat.Style.ISO_EXTENDED_FULL : TimeZoneFormat.Style.LOCALIZED_GMT, str5, pos11, tzTimeType);
                if (tz2 == null) {
                    return ~start18;
                }
                calendar11.setTimeZone(tz2);
                return pos11.getIndex();
            case 24:
                int i47 = value5;
                int i48 = patternCharIndex;
                int start19 = start2;
                Calendar calendar12 = calendar2;
                int i49 = field;
                ParsePosition pos12 = pos;
                TimeZone tz3 = tzFormat().parse(start10 < 4 ? TimeZoneFormat.Style.GENERIC_SHORT : TimeZoneFormat.Style.GENERIC_LONG, str5, pos12, tzTimeType);
                if (tz3 == null) {
                    return ~start19;
                }
                calendar12.setTimeZone(tz3);
                return pos12.getIndex();
            case 25:
                int i50 = patternCharIndex;
                int start20 = start2;
                Calendar calendar13 = calendar2;
                int field12 = field;
                ParsePosition pos13 = pos;
                Output<TimeZoneFormat.TimeType> output11 = tzTimeType;
                if (start10 == 1) {
                    value3 = value5;
                } else if (number == null || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC)) {
                    int newStart5 = 0;
                    if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 4) {
                        start7 = start20;
                        int i51 = value5;
                        int matchString3 = matchString(str5, start20, 7, this.formatData.standaloneWeekdays, null, calendar13);
                        newStart5 = matchString3;
                        if (matchString3 > 0) {
                            return newStart5;
                        }
                    } else {
                        start7 = start20;
                        int i52 = value5;
                    }
                    if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 3) {
                        int matchString4 = matchString(str5, start7, 7, this.formatData.standaloneShortWeekdays, null, calendar13);
                        newStart5 = matchString4;
                        if (matchString4 > 0) {
                            return newStart5;
                        }
                    }
                    if ((!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && start10 != 6) || this.formatData.standaloneShorterWeekdays == null) {
                        return newStart5;
                    }
                    return matchString(str5, start7, 7, this.formatData.standaloneShorterWeekdays, null, calendar13);
                } else {
                    int i53 = start20;
                    value3 = value5;
                }
                calendar13.set(field12, value3);
                return pos13.getIndex();
            case 27:
                int i54 = patternCharIndex;
                int start21 = start2;
                Calendar calendar14 = calendar2;
                int i55 = field;
                ParsePosition pos14 = pos;
                Output<TimeZoneFormat.TimeType> output12 = tzTimeType;
                if (start10 <= 2) {
                } else if (number == null || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC)) {
                    int newStart6 = 0;
                    if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 4) {
                        start8 = start21;
                        int matchQuarterString = matchQuarterString(str5, start21, 2, this.formatData.quarters, calendar14);
                        newStart6 = matchQuarterString;
                        if (matchQuarterString > 0) {
                            return newStart6;
                        }
                    } else {
                        start8 = start21;
                    }
                    if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && start10 != 3) {
                        return newStart6;
                    }
                    return matchQuarterString(str5, start8, 2, this.formatData.shortQuarters, calendar14);
                } else {
                    int i56 = start21;
                }
                calendar14.set(2, (value5 - 1) * 3);
                return pos14.getIndex();
            case 28:
                int i57 = patternCharIndex;
                int start22 = start2;
                Calendar calendar15 = calendar2;
                int i58 = field;
                ParsePosition pos15 = pos;
                Output<TimeZoneFormat.TimeType> output13 = tzTimeType;
                if (start10 <= 2) {
                } else if (number == null || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC)) {
                    int newStart7 = 0;
                    if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 4) {
                        start9 = start22;
                        int matchQuarterString2 = matchQuarterString(str5, start22, 2, this.formatData.standaloneQuarters, calendar15);
                        newStart7 = matchQuarterString2;
                        if (matchQuarterString2 > 0) {
                            return newStart7;
                        }
                    } else {
                        start9 = start22;
                    }
                    if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && start10 != 3) {
                        return newStart7;
                    }
                    return matchQuarterString(str5, start9, 2, this.formatData.standaloneShortQuarters, calendar15);
                } else {
                    int i59 = start22;
                }
                calendar15.set(2, (value5 - 1) * 3);
                return pos15.getIndex();
            case 29:
                int i60 = patternCharIndex;
                int start23 = start2;
                Calendar calendar16 = calendar2;
                int i61 = field;
                ParsePosition pos16 = pos;
                Output<TimeZoneFormat.TimeType> output14 = tzTimeType;
                switch (start10) {
                    case 1:
                        style = TimeZoneFormat.Style.ZONE_ID_SHORT;
                        break;
                    case 2:
                        style = TimeZoneFormat.Style.ZONE_ID;
                        break;
                    case 3:
                        style = TimeZoneFormat.Style.EXEMPLAR_LOCATION;
                        break;
                    default:
                        style = TimeZoneFormat.Style.GENERIC_LOCATION;
                        break;
                }
                TimeZone tz4 = tzFormat().parse(style, str5, pos16, output14);
                if (tz4 == null) {
                    return ~start23;
                }
                calendar16.setTimeZone(tz4);
                return pos16.getIndex();
            case 30:
                int value13 = value5;
                int i62 = patternCharIndex;
                int start24 = start2;
                Calendar calendar17 = calendar2;
                int i63 = field;
                ParsePosition pos17 = pos;
                Output<TimeZoneFormat.TimeType> output15 = tzTimeType;
                if (this.formatData.shortYearNames != null) {
                    int newStart8 = matchString(str5, start24, 1, this.formatData.shortYearNames, null, calendar17);
                    if (newStart8 > 0) {
                        return newStart8;
                    }
                }
                if (number != null) {
                    if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC) && this.formatData.shortYearNames != null) {
                        value4 = value13;
                        break;
                    } else {
                        value4 = value13;
                    }
                    calendar17.set(1, value4);
                    return pos17.getIndex();
                }
                return ~start24;
            case 31:
                int i64 = value5;
                int i65 = patternCharIndex;
                int start25 = start2;
                Calendar calendar18 = calendar2;
                int i66 = field;
                ParsePosition pos18 = pos;
                TimeZone tz5 = tzFormat().parse(start10 < 4 ? TimeZoneFormat.Style.LOCALIZED_GMT_SHORT : TimeZoneFormat.Style.LOCALIZED_GMT, str5, pos18, tzTimeType);
                if (tz5 == null) {
                    return ~start25;
                }
                calendar18.setTimeZone(tz5);
                return pos18.getIndex();
            case 32:
                int i67 = value5;
                int i68 = patternCharIndex;
                int start26 = start2;
                Calendar calendar19 = calendar2;
                int i69 = field;
                ParsePosition pos19 = pos;
                Output<TimeZoneFormat.TimeType> output16 = tzTimeType;
                switch (start10) {
                    case 1:
                        style2 = TimeZoneFormat.Style.ISO_BASIC_SHORT;
                        break;
                    case 2:
                        style2 = TimeZoneFormat.Style.ISO_BASIC_FIXED;
                        break;
                    case 3:
                        style2 = TimeZoneFormat.Style.ISO_EXTENDED_FIXED;
                        break;
                    case 4:
                        style2 = TimeZoneFormat.Style.ISO_BASIC_FULL;
                        break;
                    default:
                        style2 = TimeZoneFormat.Style.ISO_EXTENDED_FULL;
                        break;
                }
                TimeZone tz6 = tzFormat().parse(style2, str5, pos19, output16);
                if (tz6 == null) {
                    return ~start26;
                }
                calendar19.setTimeZone(tz6);
                return pos19.getIndex();
            case 33:
                int i70 = value5;
                ParsePosition pos20 = pos;
                int i71 = patternCharIndex;
                int start27 = start2;
                int i72 = field;
                switch (start10) {
                    case 1:
                        style3 = TimeZoneFormat.Style.ISO_BASIC_LOCAL_SHORT;
                        break;
                    case 2:
                        style3 = TimeZoneFormat.Style.ISO_BASIC_LOCAL_FIXED;
                        break;
                    case 3:
                        style3 = TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FIXED;
                        break;
                    case 4:
                        style3 = TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL;
                        break;
                    default:
                        style3 = TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FULL;
                        break;
                }
                ParsePosition pos21 = pos20;
                TimeZone tz7 = tzFormat().parse(style3, str5, pos21, tzTimeType);
                if (tz7 != null) {
                    cal.setTimeZone(tz7);
                    return pos21.getIndex();
                }
                Calendar calendar20 = cal;
                return ~start27;
            case 35:
                int i73 = value5;
                ParsePosition parsePosition3 = pos;
                int i74 = field;
                int i75 = patternCharIndex;
                int start28 = start2;
                int ampmStart = subParse(str5, start2, 'a', start10, obeyCount, z, ambiguousYear, calendar2, numericLeapMonthFormatter, tzTimeType, dayPeriod);
                if (ampmStart > 0) {
                    return ampmStart;
                }
                int newStart9 = 0;
                if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 3) {
                    int matchDayPeriodString = matchDayPeriodString(str5, start28, this.formatData.abbreviatedDayPeriods, 2, dayPeriod);
                    newStart9 = matchDayPeriodString;
                    if (matchDayPeriodString > 0) {
                        return newStart9;
                    }
                }
                if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH)) {
                    i6 = 4;
                    break;
                } else {
                    i6 = 4;
                }
                int matchDayPeriodString2 = matchDayPeriodString(str5, start28, this.formatData.wideDayPeriods, 2, dayPeriod);
                newStart9 = matchDayPeriodString2;
                if (matchDayPeriodString2 > 0) {
                    return newStart9;
                }
                if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == i6) {
                    int matchDayPeriodString3 = matchDayPeriodString(str5, start28, this.formatData.narrowDayPeriods, 2, dayPeriod);
                    newStart9 = matchDayPeriodString3;
                    if (matchDayPeriodString3 > 0) {
                        return newStart9;
                    }
                }
                return newStart9;
            case 36:
                int newStart10 = 0;
                if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 3) {
                    int matchDayPeriodString4 = matchDayPeriodString(str5, start2, this.formatData.abbreviatedDayPeriods, this.formatData.abbreviatedDayPeriods.length, dayPeriod);
                    newStart10 = matchDayPeriodString4;
                    if (matchDayPeriodString4 > 0) {
                        return newStart10;
                    }
                }
                if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 4) {
                    int matchDayPeriodString5 = matchDayPeriodString(str5, start2, this.formatData.wideDayPeriods, this.formatData.wideDayPeriods.length, dayPeriod);
                    newStart10 = matchDayPeriodString5;
                    if (matchDayPeriodString5 > 0) {
                        return newStart10;
                    }
                }
                if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 4) {
                    int matchDayPeriodString6 = matchDayPeriodString(str5, start2, this.formatData.narrowDayPeriods, this.formatData.narrowDayPeriods.length, dayPeriod);
                    newStart10 = matchDayPeriodString6;
                    if (matchDayPeriodString6 > 0) {
                        return newStart10;
                    }
                }
                return newStart10;
            case 37:
                ArrayList<String> data = new ArrayList<>(3);
                data.add(this.formatData.getTimeSeparatorString());
                if (!this.formatData.getTimeSeparatorString().equals(":")) {
                    data.add(":");
                }
                if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH) && !this.formatData.getTimeSeparatorString().equals(".")) {
                    data.add(".");
                }
                ArrayList<String> arrayList = data;
                NumberFormat numberFormat = currentNumberFormat;
                return matchString(str5, start2, -1, (String[]) data.toArray(new String[0]), calendar2);
            default:
                NumberFormat currentNumberFormat4 = currentNumberFormat;
                int i76 = value5;
                Calendar calendar21 = calendar2;
                int field13 = field;
                ParsePosition pos22 = pos;
                Output<TimeZoneFormat.TimeType> output17 = tzTimeType;
                if (!obeyCount) {
                    number2 = parseInt(str5, pos22, allowNegative, currentNumberFormat4);
                } else if (start2 + start10 > text.length()) {
                    return -start2;
                } else {
                    number2 = parseInt(str5, start10, pos22, allowNegative, currentNumberFormat4);
                    NumberFormat numberFormat2 = currentNumberFormat4;
                    boolean z2 = allowNegative;
                }
                if (number2 != null) {
                    if (patternCharIndex != 34) {
                        calendar21.set(field13, number2.intValue());
                    } else {
                        calendar21.setRelatedYear(number2.intValue());
                    }
                    return pos22.getIndex();
                }
                return ~start2;
        }
        int newStart11 = 0;
        if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == start4) {
            i5 = i4;
            start5 = field2;
            int i77 = field3;
            field4 = value2;
            int matchString5 = matchString(str5, field2, 7, this.formatData.weekdays, null, calendar);
            newStart11 = matchString5;
            if (matchString5 > 0) {
                return newStart11;
            }
        } else {
            i5 = i4;
            int i78 = field3;
            start5 = field2;
            field4 = value2;
        }
        if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == i5) {
            int matchString6 = matchString(str5, start5, 7, this.formatData.shortWeekdays, null, calendar);
            newStart11 = matchString6;
            if (matchString6 > 0) {
                return newStart11;
            }
        }
        if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == field4) && this.formatData.shorterWeekdays != null) {
            int matchString7 = matchString(str5, start5, 7, this.formatData.shorterWeekdays, null, calendar);
            newStart11 = matchString7;
            if (matchString7 > 0) {
                return newStart11;
            }
        }
        if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || start10 == 5) && this.formatData.narrowWeekdays != null) {
            int matchString8 = matchString(str5, start5, 7, this.formatData.narrowWeekdays, null, calendar);
            newStart11 = matchString8;
            if (matchString8 > 0) {
                return newStart11;
            }
        }
        return newStart11;
    }

    private boolean allowNumericFallback(int patternCharIndex) {
        if (patternCharIndex == 26 || patternCharIndex == 19 || patternCharIndex == 25 || patternCharIndex == 30 || patternCharIndex == 27 || patternCharIndex == 28) {
            return true;
        }
        return false;
    }

    private Number parseInt(String text, ParsePosition pos, boolean allowNegative, NumberFormat fmt) {
        return parseInt(text, -1, pos, allowNegative, fmt);
    }

    private Number parseInt(String text, int maxDigits, ParsePosition pos, boolean allowNegative, NumberFormat fmt) {
        Number number;
        int oldPos = pos.getIndex();
        if (allowNegative) {
            number = fmt.parse(text, pos);
        } else if (fmt instanceof DecimalFormat) {
            String oldPrefix = ((DecimalFormat) fmt).getNegativePrefix();
            ((DecimalFormat) fmt).setNegativePrefix(SUPPRESS_NEGATIVE_PREFIX);
            number = fmt.parse(text, pos);
            ((DecimalFormat) fmt).setNegativePrefix(oldPrefix);
        } else {
            boolean dateNumberFormat = fmt instanceof DateNumberFormat;
            if (dateNumberFormat) {
                ((DateNumberFormat) fmt).setParsePositiveOnly(true);
            }
            number = fmt.parse(text, pos);
            if (dateNumberFormat) {
                ((DateNumberFormat) fmt).setParsePositiveOnly(false);
            }
        }
        if (maxDigits <= 0) {
            return number;
        }
        int nDigits = pos.getIndex() - oldPos;
        if (nDigits <= maxDigits) {
            return number;
        }
        double val = number.doubleValue();
        for (int nDigits2 = nDigits - maxDigits; nDigits2 > 0; nDigits2--) {
            val /= 10.0d;
        }
        pos.setIndex(oldPos + maxDigits);
        return Integer.valueOf((int) val);
    }

    private static int countDigits(String text, int start, int end) {
        int numDigits = 0;
        int idx = start;
        while (idx < end) {
            int cp = text.codePointAt(idx);
            if (UCharacter.isDigit(cp)) {
                numDigits++;
            }
            idx += UCharacter.charCount(cp);
        }
        return numDigits;
    }

    private String translatePattern(String pat, String from, String to) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < pat.length(); i++) {
            char c = pat.charAt(i);
            if (inQuote) {
                if (c == '\'') {
                    inQuote = false;
                }
            } else if (c == '\'') {
                inQuote = true;
            } else if (isSyntaxChar(c)) {
                int ci = from.indexOf(c);
                if (ci != -1) {
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
        return translatePattern(this.pattern, "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB", this.formatData.localPatternChars);
    }

    public void applyPattern(String pat) {
        this.pattern = pat;
        parsePattern();
        setLocale(null, null);
        this.patternItems = null;
    }

    public void applyLocalizedPattern(String pat) {
        this.pattern = translatePattern(pat, this.formatData.localPatternChars, "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB");
        setLocale(null, null);
    }

    public DateFormatSymbols getDateFormatSymbols() {
        return (DateFormatSymbols) this.formatData.clone();
    }

    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        this.formatData = (DateFormatSymbols) newFormatSymbols.clone();
    }

    /* access modifiers changed from: protected */
    public DateFormatSymbols getSymbols() {
        return this.formatData;
    }

    public TimeZoneFormat getTimeZoneFormat() {
        return tzFormat().freeze();
    }

    public void setTimeZoneFormat(TimeZoneFormat tzfmt) {
        if (tzfmt.isFrozen()) {
            this.tzFormat = tzfmt;
        } else {
            this.tzFormat = tzfmt.cloneAsThawed().freeze();
        }
    }

    public Object clone() {
        SimpleDateFormat other = (SimpleDateFormat) super.clone();
        other.formatData = (DateFormatSymbols) this.formatData.clone();
        if (this.decimalBuf != null) {
            other.decimalBuf = new char[10];
        }
        return other;
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!super.equals(obj)) {
            return false;
        }
        SimpleDateFormat that = (SimpleDateFormat) obj;
        if (this.pattern.equals(that.pattern) && this.formatData.equals(that.formatData)) {
            z = true;
        }
        return z;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (this.defaultCenturyStart == null) {
            initializeDefaultCenturyStart(this.defaultCenturyBase);
        }
        initializeTimeZoneFormat(false);
        stream.defaultWriteObject();
        stream.writeInt(getContext(DisplayContext.Type.CAPITALIZATION).value());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        int capitalizationSettingValue = this.serialVersionOnStream > 1 ? stream.readInt() : -1;
        if (this.serialVersionOnStream < 1) {
            this.defaultCenturyBase = System.currentTimeMillis();
        } else {
            parseAmbiguousDatesAsAfter(this.defaultCenturyStart);
        }
        this.serialVersionOnStream = 2;
        this.locale = getLocale(ULocale.VALID_LOCALE);
        if (this.locale == null) {
            this.locale = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        initLocalZeroPaddingNumberFormat();
        setContext(DisplayContext.CAPITALIZATION_NONE);
        if (capitalizationSettingValue >= 0) {
            DisplayContext[] values = DisplayContext.values();
            int length = values.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                DisplayContext context = values[i];
                if (context.value() == capitalizationSettingValue) {
                    setContext(context);
                    break;
                }
                i++;
            }
        }
        if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_PARTIAL_MATCH)) {
            setBooleanAttribute(DateFormat.BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH, false);
        }
        parsePattern();
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        Calendar cal = this.calendar;
        if (obj instanceof Calendar) {
            cal = (Calendar) obj;
        } else if (obj instanceof Date) {
            this.calendar.setTime((Date) obj);
        } else if (obj instanceof Number) {
            this.calendar.setTimeInMillis(((Number) obj).longValue());
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Date");
        }
        StringBuffer toAppendTo = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        ArrayList arrayList = new ArrayList();
        format(cal, getContext(DisplayContext.Type.CAPITALIZATION), toAppendTo, pos, arrayList);
        AttributedString as = new AttributedString(toAppendTo.toString());
        for (int i = 0; i < arrayList.size(); i++) {
            FieldPosition fp = (FieldPosition) arrayList.get(i);
            Format.Field attribute = fp.getFieldAttribute();
            as.addAttribute(attribute, attribute, fp.getBeginIndex(), fp.getEndIndex());
        }
        return as.getIterator();
    }

    /* access modifiers changed from: package-private */
    public ULocale getLocale() {
        return this.locale;
    }

    /* access modifiers changed from: package-private */
    public boolean isFieldUnitIgnored(int field) {
        return isFieldUnitIgnored(this.pattern, field);
    }

    static boolean isFieldUnitIgnored(String pattern2, int field) {
        int fieldLevel = CALENDAR_FIELD_TO_LEVEL[field];
        char prevCh = 0;
        int count = 0;
        boolean inQuote = false;
        int i = 0;
        while (i < pattern2.length()) {
            char ch = pattern2.charAt(i);
            if (ch != prevCh && count > 0) {
                if (fieldLevel <= getLevelFromChar(prevCh)) {
                    return false;
                }
                count = 0;
            }
            if (ch == '\'') {
                if (i + 1 >= pattern2.length() || pattern2.charAt(i + 1) != '\'') {
                    inQuote = !inQuote;
                } else {
                    i++;
                }
            } else if (!inQuote && isSyntaxChar(ch)) {
                prevCh = ch;
                count++;
            }
            i++;
        }
        return count <= 0 || fieldLevel > getLevelFromChar(prevCh);
    }

    @Deprecated
    public final StringBuffer intervalFormatByAlgorithm(Calendar fromCalendar, Calendar toCalendar, StringBuffer appendTo, FieldPosition pos) throws IllegalArgumentException {
        int highestLevel;
        int i;
        int diffEnd;
        Calendar calendar = fromCalendar;
        Calendar calendar2 = toCalendar;
        StringBuffer stringBuffer = appendTo;
        FieldPosition fieldPosition = pos;
        if (fromCalendar.isEquivalentTo(toCalendar)) {
            Object[] items = getPatternItems();
            int diffBegin = -1;
            int diffEnd2 = -1;
            int i2 = 0;
            int i3 = 0;
            while (true) {
                try {
                    if (i3 >= items.length) {
                        break;
                    } else if (diffCalFieldValue(calendar, calendar2, items, i3)) {
                        diffBegin = i3;
                        break;
                    } else {
                        i3++;
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(e.toString());
                }
            }
            if (diffBegin == -1) {
                return format(calendar, stringBuffer, fieldPosition);
            }
            int i4 = items.length - 1;
            while (true) {
                if (i4 < diffBegin) {
                    break;
                } else if (diffCalFieldValue(calendar, calendar2, items, i4)) {
                    diffEnd2 = i4;
                    break;
                } else {
                    i4--;
                }
            }
            if (diffBegin == 0 && diffEnd2 == items.length - 1) {
                format(calendar, stringBuffer, fieldPosition);
                stringBuffer.append(" – ");
                format(calendar2, stringBuffer, fieldPosition);
                return stringBuffer;
            }
            int highestLevel2 = 1000;
            for (int i5 = diffBegin; i5 <= diffEnd2; i5++) {
                if (!(items[i5] instanceof String)) {
                    int patternCharIndex = getIndexFromChar(((PatternItem) items[i5]).type);
                    if (patternCharIndex == -1) {
                        throw new IllegalArgumentException("Illegal pattern character '" + ch + "' in \"" + this.pattern + '\"');
                    } else if (patternCharIndex < highestLevel2) {
                        highestLevel2 = patternCharIndex;
                    }
                }
            }
            int i6 = 0;
            while (true) {
                if (i6 >= diffBegin) {
                    break;
                }
                try {
                    if (lowerLevel(items, i6, highestLevel2)) {
                        diffBegin = i6;
                        break;
                    }
                    i6++;
                } catch (IllegalArgumentException e2) {
                    e = e2;
                    int i7 = diffBegin;
                    throw new IllegalArgumentException(e.toString());
                }
            }
            int diffBegin2 = diffBegin;
            try {
                int i8 = items.length - 1;
                while (true) {
                    if (i8 <= diffEnd2) {
                        i8 = diffEnd2;
                        break;
                    }
                    try {
                        if (lowerLevel(items, i8, highestLevel2)) {
                            int diffEnd3 = i8;
                            break;
                        }
                        i8--;
                    } catch (IllegalArgumentException e3) {
                        e = e3;
                        throw new IllegalArgumentException(e.toString());
                    }
                }
                if (diffBegin2 == 0 && i8 == items.length - 1) {
                    format(calendar, stringBuffer, fieldPosition);
                    stringBuffer.append(" – ");
                    format(calendar2, stringBuffer, fieldPosition);
                    return stringBuffer;
                }
                fieldPosition.setBeginIndex(0);
                fieldPosition.setEndIndex(0);
                DisplayContext capSetting = getContext(DisplayContext.Type.CAPITALIZATION);
                while (true) {
                    int i9 = i2;
                    if (i9 > i8) {
                        break;
                    }
                    if (items[i9] instanceof String) {
                        stringBuffer.append((String) items[i9]);
                        diffEnd = i8;
                        i = i9;
                        highestLevel = highestLevel2;
                    } else {
                        PatternItem item = (PatternItem) items[i9];
                        if (this.useFastFormat) {
                            diffEnd = i8;
                            PatternItem patternItem = item;
                            i = i9;
                            highestLevel = highestLevel2;
                            subFormat(stringBuffer, item.type, item.length, appendTo.length(), i9, capSetting, fieldPosition, calendar);
                        } else {
                            diffEnd = i8;
                            PatternItem item2 = item;
                            i = i9;
                            highestLevel = highestLevel2;
                            stringBuffer.append(subFormat(item2.type, item2.length, appendTo.length(), i, capSetting, fieldPosition, calendar));
                        }
                    }
                    i2 = i + 1;
                    i8 = diffEnd;
                    highestLevel2 = highestLevel;
                }
                int diffEnd4 = i8;
                int i10 = highestLevel2;
                stringBuffer.append(" – ");
                int i11 = diffBegin2;
                while (i11 < items.length) {
                    if (items[i11] instanceof String) {
                        stringBuffer.append((String) items[i11]);
                    } else {
                        PatternItem item3 = (PatternItem) items[i11];
                        if (this.useFastFormat) {
                            PatternItem patternItem2 = item3;
                            subFormat(stringBuffer, item3.type, item3.length, appendTo.length(), i11, capSetting, fieldPosition, calendar2);
                        } else {
                            PatternItem item4 = item3;
                            stringBuffer.append(subFormat(item4.type, item4.length, appendTo.length(), i11, capSetting, fieldPosition, calendar2));
                        }
                    }
                    i11++;
                    Calendar calendar3 = fromCalendar;
                }
                return stringBuffer;
            } catch (IllegalArgumentException e4) {
                e = e4;
                int i12 = highestLevel2;
                throw new IllegalArgumentException(e.toString());
            }
        } else {
            throw new IllegalArgumentException("can not format on two different calendars");
        }
    }

    private boolean diffCalFieldValue(Calendar fromCalendar, Calendar toCalendar, Object[] items, int i) throws IllegalArgumentException {
        if (items[i] instanceof String) {
            return false;
        }
        char ch = items[i].type;
        int patternCharIndex = getIndexFromChar(ch);
        if (patternCharIndex != -1) {
            int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
            if (field < 0 || fromCalendar.get(field) == toCalendar.get(field)) {
                return false;
            }
            return true;
        }
        throw new IllegalArgumentException("Illegal pattern character '" + ch + "' in \"" + this.pattern + '\"');
    }

    private boolean lowerLevel(Object[] items, int i, int level) throws IllegalArgumentException {
        if (items[i] instanceof String) {
            return false;
        }
        char ch = items[i].type;
        int patternCharIndex = getLevelFromChar(ch);
        if (patternCharIndex == -1) {
            throw new IllegalArgumentException("Illegal pattern character '" + ch + "' in \"" + this.pattern + '\"');
        } else if (patternCharIndex >= level) {
            return true;
        } else {
            return false;
        }
    }

    public void setNumberFormat(String fields, NumberFormat overrideNF) {
        overrideNF.setGroupingUsed(false);
        String nsName = "$" + UUID.randomUUID().toString();
        if (this.numberFormatters == null) {
            this.numberFormatters = new HashMap<>();
        }
        if (this.overrideMap == null) {
            this.overrideMap = new HashMap<>();
        }
        int i = 0;
        while (i < fields.length()) {
            char field = fields.charAt(i);
            if ("GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB".indexOf(field) != -1) {
                this.overrideMap.put(Character.valueOf(field), nsName);
                this.numberFormatters.put(nsName, overrideNF);
                i++;
            } else {
                throw new IllegalArgumentException("Illegal field character '" + field + "' in setNumberFormat.");
            }
        }
        this.useLocalZeroPaddingNumberFormat = false;
    }

    public NumberFormat getNumberFormat(char field) {
        Character ovrField = Character.valueOf(field);
        if (this.overrideMap == null || !this.overrideMap.containsKey(ovrField)) {
            return this.numberFormat;
        }
        return this.numberFormatters.get(this.overrideMap.get(ovrField).toString());
    }

    private void initNumberFormatters(ULocale loc) {
        this.numberFormatters = new HashMap<>();
        this.overrideMap = new HashMap<>();
        processOverrideString(loc, this.override);
    }

    private void processOverrideString(ULocale loc, String str) {
        int end;
        boolean fullOverride;
        String nsName;
        if (str != null && str.length() != 0) {
            int start = 0;
            boolean moreToProcess = true;
            while (moreToProcess) {
                int delimiterPosition = str.indexOf(";", start);
                if (delimiterPosition == -1) {
                    moreToProcess = false;
                    end = str.length();
                } else {
                    end = delimiterPosition;
                }
                String currentString = str.substring(start, end);
                int equalSignPosition = currentString.indexOf("=");
                if (equalSignPosition == -1) {
                    nsName = currentString;
                    fullOverride = true;
                } else {
                    nsName = currentString.substring(equalSignPosition + 1);
                    this.overrideMap.put(Character.valueOf(currentString.charAt(0)), nsName);
                    fullOverride = false;
                }
                NumberFormat nf = NumberFormat.createInstance(new ULocale(loc.getBaseName() + "@numbers=" + nsName), 0);
                nf.setGroupingUsed(false);
                if (fullOverride) {
                    setNumberFormat(nf);
                } else {
                    this.useLocalZeroPaddingNumberFormat = false;
                }
                if (!fullOverride && !this.numberFormatters.containsKey(nsName)) {
                    this.numberFormatters.put(nsName, nf);
                }
                start = delimiterPosition + 1;
            }
        }
    }

    private void parsePattern() {
        this.hasMinute = false;
        this.hasSecond = false;
        boolean inQuote = false;
        for (int i = 0; i < this.pattern.length(); i++) {
            char ch = this.pattern.charAt(i);
            if (ch == '\'') {
                inQuote = !inQuote;
            }
            if (!inQuote) {
                if (ch == 'm') {
                    this.hasMinute = true;
                }
                if (ch == 's') {
                    this.hasSecond = true;
                }
            }
        }
    }
}
