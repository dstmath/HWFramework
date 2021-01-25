package ohos.global.icu.text;

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
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.global.icu.impl.CalendarAstronomer;
import ohos.global.icu.impl.DateNumberFormat;
import ohos.global.icu.impl.DayPeriodRules;
import ohos.global.icu.impl.ICUCache;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.SimpleCache;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.text.DateFormatSymbols;
import ohos.global.icu.text.DisplayContext;
import ohos.global.icu.text.TimeZoneFormat;
import ohos.global.icu.util.BasicTimeZone;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.HebrewCalendar;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.TimeZone;
import ohos.global.icu.util.TimeZoneTransition;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

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
    private transient boolean hasHanYearChar;
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

    private boolean allowNumericFallback(int i) {
        return i == 26 || i == 19 || i == 25 || i == 30 || i == 27 || i == 28;
    }

    private static int getLevelFromChar(char c) {
        int[] iArr = PATTERN_CHAR_TO_LEVEL;
        if (c < iArr.length) {
            return iArr[c & 255];
        }
        return -1;
    }

    private static boolean isSyntaxChar(char c) {
        boolean[] zArr = PATTERN_CHAR_IS_SYNTAX;
        if (c < zArr.length) {
            return zArr[c & 255];
        }
        return false;
    }

    public SimpleDateFormat() {
        this(getDefaultPattern(), null, null, null, null, true, null);
    }

    public SimpleDateFormat(String str) {
        this(str, null, null, null, null, true, null);
    }

    public SimpleDateFormat(String str, Locale locale2) {
        this(str, null, null, null, ULocale.forLocale(locale2), true, null);
    }

    public SimpleDateFormat(String str, ULocale uLocale) {
        this(str, null, null, null, uLocale, true, null);
    }

    public SimpleDateFormat(String str, String str2, ULocale uLocale) {
        this(str, null, null, null, uLocale, false, str2);
    }

    public SimpleDateFormat(String str, DateFormatSymbols dateFormatSymbols) {
        this(str, (DateFormatSymbols) dateFormatSymbols.clone(), null, null, null, true, null);
    }

    @Deprecated
    public SimpleDateFormat(String str, DateFormatSymbols dateFormatSymbols, ULocale uLocale) {
        this(str, (DateFormatSymbols) dateFormatSymbols.clone(), null, null, uLocale, true, null);
    }

    SimpleDateFormat(String str, DateFormatSymbols dateFormatSymbols, Calendar calendar, ULocale uLocale, boolean z, String str2) {
        this(str, (DateFormatSymbols) dateFormatSymbols.clone(), (Calendar) calendar.clone(), null, uLocale, z, str2);
    }

    private SimpleDateFormat(String str, DateFormatSymbols dateFormatSymbols, Calendar calendar, NumberFormat numberFormat, ULocale uLocale, boolean z, String str2) {
        this.serialVersionOnStream = 2;
        this.capitalizationBrkIter = null;
        this.pattern = str;
        this.formatData = dateFormatSymbols;
        this.calendar = calendar;
        this.numberFormat = numberFormat;
        this.locale = uLocale;
        this.useFastFormat = z;
        this.override = str2;
        initialize();
    }

    @Deprecated
    public static SimpleDateFormat getInstance(Calendar.FormatConfiguration formatConfiguration) {
        String overrideString = formatConfiguration.getOverrideString();
        return new SimpleDateFormat(formatConfiguration.getPatternString(), formatConfiguration.getDateFormatSymbols(), formatConfiguration.getCalendar(), null, formatConfiguration.getLocale(), overrideString != null && overrideString.length() > 0, formatConfiguration.getOverrideString());
    }

    private void initialize() {
        ULocale uLocale;
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
            NumberingSystem instance = NumberingSystem.getInstance(this.locale);
            String description = instance.getDescription();
            if (instance.isAlgorithmic() || description.length() != 10) {
                this.numberFormat = NumberFormat.getInstance(this.locale);
            } else {
                this.numberFormat = new DateNumberFormat(this.locale, description, instance.getName());
            }
        }
        if (this.numberFormat instanceof DecimalFormat) {
            fixNumberFormatForDates(this.numberFormat);
        }
        this.defaultCenturyBase = System.currentTimeMillis();
        setLocale(this.calendar.getLocale(ULocale.VALID_LOCALE), this.calendar.getLocale(ULocale.ACTUAL_LOCALE));
        initLocalZeroPaddingNumberFormat();
        parsePattern();
        if (this.override == null && this.hasHanYearChar && this.calendar != null && this.calendar.getType().equals("japanese") && (uLocale = this.locale) != null && uLocale.getLanguage().equals("ja")) {
            this.override = "y=jpanyear";
        }
        if (this.override != null) {
            initNumberFormatters(this.locale);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        if (r4.tzFormat == null) goto L_0x0007;
     */
    private synchronized void initializeTimeZoneFormat(boolean z) {
        if (!z) {
        }
        this.tzFormat = TimeZoneFormat.getInstance(this.locale);
        String str = null;
        if (this.numberFormat instanceof DecimalFormat) {
            String[] digitStringsLocal = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getDigitStringsLocal();
            StringBuilder sb = new StringBuilder();
            for (String str2 : digitStringsLocal) {
                sb.append(str2);
            }
            str = sb.toString();
        } else if (this.numberFormat instanceof DateNumberFormat) {
            str = new String(((DateNumberFormat) this.numberFormat).getDigits());
        }
        if (str != null && !this.tzFormat.getGMTOffsetDigits().equals(str)) {
            if (this.tzFormat.isFrozen()) {
                this.tzFormat = this.tzFormat.cloneAsThawed();
            }
            this.tzFormat.setGMTOffsetDigits(str);
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
            ULocale uLocale = ULocale.getDefault(ULocale.Category.FORMAT);
            if (!uLocale.equals(cachedDefaultLocale)) {
                cachedDefaultLocale = uLocale;
                Calendar instance = Calendar.getInstance(cachedDefaultLocale);
                try {
                    ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, cachedDefaultLocale);
                    ICUResourceBundle findWithFallback = bundleInstance.findWithFallback("calendar/" + instance.getType() + "/DateTimePatterns");
                    if (findWithFallback == null) {
                        findWithFallback = bundleInstance.findWithFallback("calendar/gregorian/DateTimePatterns");
                    }
                    if (findWithFallback != null) {
                        if (findWithFallback.getSize() >= 9) {
                            int i = 8;
                            if (findWithFallback.getSize() >= 13) {
                                i = 12;
                            }
                            cachedDefaultPattern = SimpleFormatterImpl.formatRawPattern(findWithFallback.getString(i), 2, 2, findWithFallback.getString(3), findWithFallback.getString(7));
                        }
                    }
                    cachedDefaultPattern = FALLBACKPATTERN;
                } catch (MissingResourceException unused) {
                    cachedDefaultPattern = FALLBACKPATTERN;
                }
            }
            str = cachedDefaultPattern;
        }
        return str;
    }

    private void parseAmbiguousDatesAsAfter(Date date) {
        this.defaultCenturyStart = date;
        this.calendar.setTime(date);
        this.defaultCenturyStartYear = this.calendar.get(1);
    }

    private void initializeDefaultCenturyStart(long j) {
        this.defaultCenturyBase = j;
        Calendar calendar = (Calendar) this.calendar.clone();
        calendar.setTimeInMillis(j);
        calendar.add(1, -80);
        this.defaultCenturyStart = calendar.getTime();
        this.defaultCenturyStartYear = calendar.get(1);
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

    public void set2DigitYearStart(Date date) {
        parseAmbiguousDatesAsAfter(date);
    }

    public Date get2DigitYearStart() {
        return getDefaultCenturyStart();
    }

    @Override // ohos.global.icu.text.DateFormat
    public void setContext(DisplayContext displayContext) {
        super.setContext(displayContext);
        if (this.capitalizationBrkIter != null) {
            return;
        }
        if (displayContext == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || displayContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || displayContext == DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
        }
    }

    @Override // ohos.global.icu.text.DateFormat
    public StringBuffer format(Calendar calendar, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        return format(calendar, stringBuffer, fieldPosition, null);
    }

    /* access modifiers changed from: package-private */
    public StringBuffer format(Calendar calendar, StringBuffer stringBuffer, FieldPosition fieldPosition, List<FieldPosition> list) {
        TimeZone timeZone;
        if (calendar == this.calendar || calendar.getType().equals(this.calendar.getType())) {
            timeZone = null;
        } else {
            this.calendar.setTimeInMillis(calendar.getTimeInMillis());
            timeZone = this.calendar.getTimeZone();
            this.calendar.setTimeZone(calendar.getTimeZone());
            calendar = this.calendar;
        }
        StringBuffer format = format(calendar, getContext(DisplayContext.Type.CAPITALIZATION), stringBuffer, fieldPosition, list);
        if (timeZone != null) {
            this.calendar.setTimeZone(timeZone);
        }
        return format;
    }

    private StringBuffer format(Calendar calendar, DisplayContext displayContext, StringBuffer stringBuffer, FieldPosition fieldPosition, List<FieldPosition> list) {
        int i;
        PatternItem patternItem;
        int i2 = 0;
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        Object[] patternItems2 = getPatternItems();
        int i3 = 0;
        while (i3 < patternItems2.length) {
            if (patternItems2[i3] instanceof String) {
                stringBuffer.append((String) patternItems2[i3]);
            } else {
                PatternItem patternItem2 = (PatternItem) patternItems2[i3];
                int length = list != null ? stringBuffer.length() : i2;
                if (this.useFastFormat) {
                    i = length;
                    patternItem = patternItem2;
                    subFormat(stringBuffer, patternItem2.type, patternItem2.length, stringBuffer.length(), i3, displayContext, fieldPosition, calendar);
                } else {
                    i = length;
                    patternItem = patternItem2;
                    stringBuffer.append(subFormat(patternItem.type, patternItem.length, stringBuffer.length(), i3, displayContext, fieldPosition, calendar));
                }
                if (list != null) {
                    int length2 = stringBuffer.length();
                    if (length2 - i > 0) {
                        FieldPosition fieldPosition2 = new FieldPosition(patternCharToDateFormatField(patternItem.type));
                        fieldPosition2.setBeginIndex(i);
                        fieldPosition2.setEndIndex(length2);
                        list.add(fieldPosition2);
                    }
                }
            }
            i3++;
            i2 = 0;
        }
        return stringBuffer;
    }

    private static int getIndexFromChar(char c) {
        int[] iArr = PATTERN_CHAR_TO_INDEX;
        if (c < iArr.length) {
            return iArr[c & 255];
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public DateFormat.Field patternCharToDateFormatField(char c) {
        int indexFromChar = getIndexFromChar(c);
        if (indexFromChar != -1) {
            return PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE[indexFromChar];
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String subFormat(char c, int i, int i2, FieldPosition fieldPosition, DateFormatSymbols dateFormatSymbols, Calendar calendar) throws IllegalArgumentException {
        return subFormat(c, i, i2, 0, DisplayContext.CAPITALIZATION_NONE, fieldPosition, calendar);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public String subFormat(char c, int i, int i2, int i3, DisplayContext displayContext, FieldPosition fieldPosition, Calendar calendar) {
        StringBuffer stringBuffer = new StringBuffer();
        subFormat(stringBuffer, c, i, i2, i3, displayContext, fieldPosition, calendar);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x0495  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x04a3  */
    /* JADX WARNING: Removed duplicated region for block: B:309:0x0657  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0663  */
    /* JADX WARNING: Removed duplicated region for block: B:332:0x06df A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:342:0x0701  */
    /* JADX WARNING: Removed duplicated region for block: B:344:0x0704  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0735  */
    /* JADX WARNING: Removed duplicated region for block: B:357:? A[RETURN, SYNTHETIC] */
    @Deprecated
    public void subFormat(StringBuffer stringBuffer, char c, int i, int i2, int i3, DisplayContext displayContext, FieldPosition fieldPosition, Calendar calendar) {
        int i4;
        int i5;
        int i6;
        DateFormatSymbols.CapitalizationContextUsage capitalizationContextUsage;
        int i7;
        int i8;
        int i9;
        int i10;
        DateFormatSymbols.CapitalizationContextUsage capitalizationContextUsage2;
        String str;
        String str2;
        int i11;
        String str3;
        DayPeriodRules.DayPeriod dayPeriod;
        int length = stringBuffer.length();
        TimeZone timeZone = calendar.getTimeZone();
        long timeInMillis = calendar.getTimeInMillis();
        int indexFromChar = getIndexFromChar(c);
        if (indexFromChar != -1) {
            int i12 = PATTERN_INDEX_TO_CALENDAR_FIELD[indexFromChar];
            int relatedYear = i12 >= 0 ? indexFromChar != 34 ? calendar.get(i12) : calendar.getRelatedYear() : 0;
            NumberFormat numberFormat = getNumberFormat(c);
            DateFormatSymbols.CapitalizationContextUsage capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.OTHER;
            String str4 = null;
            switch (indexFromChar) {
                case 0:
                    i6 = 1;
                    i5 = 2;
                    i4 = 0;
                    if (calendar.getType().equals("chinese") || calendar.getType().equals("dangi")) {
                        zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, 1, 9);
                        capitalizationContextUsage = capitalizationContextUsage3;
                        if (i3 == 0 && displayContext != null && stringBuffer.length() > length && UCharacter.isLowerCase(stringBuffer.codePointAt(length))) {
                            i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                            if (i7 == i6) {
                                i4 = i6;
                            } else if ((i7 == i5 || i7 == 3) && this.formatData.capitalization != null) {
                                boolean[] zArr = this.formatData.capitalization.get(capitalizationContextUsage);
                                boolean z = displayContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? zArr[i4] : zArr[i6];
                                int i13 = z ? 1 : 0;
                                Object[] objArr = z ? 1 : 0;
                                Object[] objArr2 = z ? 1 : 0;
                                i4 = i13;
                            }
                            if (i4 != 0) {
                                if (this.capitalizationBrkIter == null) {
                                    this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
                                }
                                stringBuffer.replace(length, stringBuffer.length(), UCharacter.toTitleCase(this.locale, stringBuffer.substring(length), (BreakIterator) this.capitalizationBrkIter.clone(), 768));
                            }
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                            return;
                        }
                        if (fieldPosition.getField() == PATTERN_INDEX_TO_DATE_FORMAT_FIELD[indexFromChar]) {
                            fieldPosition.setBeginIndex(i2);
                            fieldPosition.setEndIndex((i2 + stringBuffer.length()) - length);
                            return;
                        } else if (fieldPosition.getFieldAttribute() == PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE[indexFromChar]) {
                            fieldPosition.setBeginIndex(i2);
                            fieldPosition.setEndIndex((i2 + stringBuffer.length()) - length);
                            return;
                        } else {
                            return;
                        }
                    } else {
                        if (i == 5) {
                            safeAppend(this.formatData.narrowEras, relatedYear, stringBuffer);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.ERA_NARROW;
                        } else if (i == 4) {
                            safeAppend(this.formatData.eraNames, relatedYear, stringBuffer);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.ERA_WIDE;
                        } else {
                            safeAppend(this.formatData.eras, relatedYear, stringBuffer);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.ERA_ABBREV;
                        }
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                    }
                    break;
                case 1:
                case 18:
                    i6 = 1;
                    i5 = 2;
                    i4 = 0;
                    String str5 = this.override;
                    if (str5 != null && ((str5.compareTo("hebr") == 0 || this.override.indexOf("y=hebr") >= 0) && relatedYear > HEBREW_CAL_CUR_MILLENIUM_START_YEAR && relatedYear < HEBREW_CAL_CUR_MILLENIUM_END_YEAR)) {
                        relatedYear -= 5000;
                    }
                    if (i != i5) {
                        zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, 2, 2);
                    } else {
                        zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, i, Integer.MAX_VALUE);
                    }
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 2:
                case 26:
                    i6 = 1;
                    i5 = 2;
                    if (calendar.getType().equals("hebrew")) {
                        boolean isLeapYear = HebrewCalendar.isLeapYear(calendar.get(1));
                        if (isLeapYear && relatedYear == 6 && i >= 3) {
                            relatedYear = 13;
                        }
                        if (!isLeapYear && relatedYear >= 6 && i < 3) {
                            relatedYear--;
                        }
                    }
                    if (this.formatData.leapMonthPatterns == null || this.formatData.leapMonthPatterns.length < 7) {
                        i9 = 5;
                        i8 = 0;
                    } else {
                        i8 = calendar.get(22);
                        i9 = 5;
                    }
                    if (i != i9) {
                        if (i != 4) {
                            i4 = 0;
                            if (i != 3) {
                                StringBuffer stringBuffer2 = new StringBuffer();
                                zeroPaddingNumber(numberFormat, stringBuffer2, relatedYear + 1, i, Integer.MAX_VALUE);
                                String[] strArr = {stringBuffer2.toString()};
                                if (i8 != 0) {
                                    str4 = this.formatData.leapMonthPatterns[6];
                                }
                                safeAppendWithMonthPattern(strArr, 0, stringBuffer, str4);
                            } else if (indexFromChar == 2) {
                                String[] strArr2 = this.formatData.shortMonths;
                                if (i8 != 0) {
                                    str4 = this.formatData.leapMonthPatterns[1];
                                }
                                safeAppendWithMonthPattern(strArr2, relatedYear, stringBuffer, str4);
                                capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.MONTH_FORMAT;
                            } else {
                                String[] strArr3 = this.formatData.standaloneShortMonths;
                                if (i8 != 0) {
                                    str4 = this.formatData.leapMonthPatterns[4];
                                }
                                safeAppendWithMonthPattern(strArr3, relatedYear, stringBuffer, str4);
                                capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.MONTH_STANDALONE;
                            }
                        } else if (indexFromChar == 2) {
                            String[] strArr4 = this.formatData.months;
                            if (i8 != 0) {
                                i4 = 0;
                                str4 = this.formatData.leapMonthPatterns[0];
                            } else {
                                i4 = 0;
                            }
                            safeAppendWithMonthPattern(strArr4, relatedYear, stringBuffer, str4);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.MONTH_FORMAT;
                        } else {
                            i4 = 0;
                            String[] strArr5 = this.formatData.standaloneMonths;
                            if (i8 != 0) {
                                str4 = this.formatData.leapMonthPatterns[3];
                            }
                            safeAppendWithMonthPattern(strArr5, relatedYear, stringBuffer, str4);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.MONTH_STANDALONE;
                        }
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                        break;
                    } else {
                        if (indexFromChar == 2) {
                            String[] strArr6 = this.formatData.narrowMonths;
                            if (i8 != 0) {
                                str4 = this.formatData.leapMonthPatterns[2];
                            }
                            safeAppendWithMonthPattern(strArr6, relatedYear, stringBuffer, str4);
                        } else {
                            String[] strArr7 = this.formatData.standaloneNarrowMonths;
                            if (i8 != 0) {
                                str4 = this.formatData.leapMonthPatterns[5];
                            }
                            safeAppendWithMonthPattern(strArr7, relatedYear, stringBuffer, str4);
                        }
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.MONTH_NARROW;
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i4 = 0;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                        break;
                    }
                    break;
                case 3:
                case 5:
                case 6:
                case 7:
                case 10:
                case 11:
                case 12:
                case 13:
                case 16:
                case 20:
                case 21:
                case 22:
                case 34:
                default:
                    i6 = 1;
                    i5 = 2;
                    i4 = 0;
                    zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, i, Integer.MAX_VALUE);
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 4:
                    i6 = 1;
                    i5 = 2;
                    if (relatedYear == 0) {
                        zeroPaddingNumber(numberFormat, stringBuffer, calendar.getMaximum(11) + 1, i, Integer.MAX_VALUE);
                    } else {
                        zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, i, Integer.MAX_VALUE);
                    }
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 8:
                    i6 = 1;
                    i5 = 2;
                    this.numberFormat.setMinimumIntegerDigits(Math.min(3, i));
                    this.numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
                    if (i == 1) {
                        relatedYear /= 100;
                    } else if (i == 2) {
                        relatedYear /= 10;
                    }
                    FieldPosition fieldPosition2 = new FieldPosition(-1);
                    this.numberFormat.format((long) relatedYear, stringBuffer, fieldPosition2);
                    if (i > 3) {
                        this.numberFormat.setMinimumIntegerDigits(i - 3);
                        this.numberFormat.format(0L, stringBuffer, fieldPosition2);
                    }
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 9:
                    i6 = 1;
                    i5 = 2;
                    if (i != 5) {
                        safeAppend(this.formatData.narrowWeekdays, relatedYear, stringBuffer);
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_NARROW;
                    } else if (i == 4) {
                        safeAppend(this.formatData.weekdays, relatedYear, stringBuffer);
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_FORMAT;
                    } else if (i != 6 || this.formatData.shorterWeekdays == null) {
                        safeAppend(this.formatData.shortWeekdays, relatedYear, stringBuffer);
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_FORMAT;
                    } else {
                        safeAppend(this.formatData.shorterWeekdays, relatedYear, stringBuffer);
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_FORMAT;
                    }
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i4 = 0;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 14:
                    i6 = 1;
                    i5 = 2;
                    if (i < 5 || this.formatData.ampmsNarrow == null) {
                        safeAppend(this.formatData.ampms, relatedYear, stringBuffer);
                    } else {
                        safeAppend(this.formatData.ampmsNarrow, relatedYear, stringBuffer);
                    }
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 15:
                    i6 = 1;
                    if (relatedYear == 0) {
                        i5 = 2;
                        zeroPaddingNumber(numberFormat, stringBuffer, calendar.getLeastMaximum(10) + 1, i, Integer.MAX_VALUE);
                    } else {
                        i5 = 2;
                        zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, i, Integer.MAX_VALUE);
                    }
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 17:
                    i10 = 2;
                    i6 = 1;
                    if (i < 4) {
                        str = tzFormat().format(TimeZoneFormat.Style.SPECIFIC_SHORT, timeZone, timeInMillis);
                        capitalizationContextUsage2 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_SHORT;
                    } else {
                        str = tzFormat().format(TimeZoneFormat.Style.SPECIFIC_LONG, timeZone, timeInMillis);
                        capitalizationContextUsage2 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_LONG;
                    }
                    capitalizationContextUsage3 = capitalizationContextUsage2;
                    stringBuffer.append(str);
                    i5 = i10;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i4 = 0;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 19:
                    i6 = 1;
                    if (i >= 3) {
                        i5 = 2;
                        relatedYear = calendar.get(7);
                        if (i != 5) {
                        }
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i4 = 0;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                        break;
                    } else {
                        i5 = 2;
                        zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, i, Integer.MAX_VALUE);
                        i4 = 0;
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                        break;
                    }
                    break;
                case 23:
                    i6 = 1;
                    if (i < 4) {
                        str2 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL, timeZone, timeInMillis);
                    } else if (i == 5) {
                        str2 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_FULL, timeZone, timeInMillis);
                    } else {
                        str2 = tzFormat().format(TimeZoneFormat.Style.LOCALIZED_GMT, timeZone, timeInMillis);
                    }
                    stringBuffer.append(str2);
                    i5 = 2;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 24:
                    i10 = 2;
                    i6 = 1;
                    if (i == 1) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.GENERIC_SHORT, timeZone, timeInMillis);
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_SHORT;
                    } else if (i == 4) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.GENERIC_LONG, timeZone, timeInMillis);
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.METAZONE_LONG;
                    }
                    stringBuffer.append(str4);
                    i5 = i10;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i4 = 0;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 25:
                    i6 = 1;
                    if (i >= 3) {
                        int i14 = calendar.get(7);
                        if (i == 5) {
                            safeAppend(this.formatData.standaloneNarrowWeekdays, i14, stringBuffer);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_NARROW;
                        } else if (i == 4) {
                            safeAppend(this.formatData.standaloneWeekdays, i14, stringBuffer);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_STANDALONE;
                        } else if (i != 6 || this.formatData.standaloneShorterWeekdays == null) {
                            safeAppend(this.formatData.standaloneShortWeekdays, i14, stringBuffer);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_STANDALONE;
                        } else {
                            safeAppend(this.formatData.standaloneShorterWeekdays, i14, stringBuffer);
                            capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.DAY_STANDALONE;
                        }
                        i5 = 2;
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i4 = 0;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                        break;
                    } else {
                        zeroPaddingNumber(numberFormat, stringBuffer, relatedYear, 1, Integer.MAX_VALUE);
                        i5 = 2;
                        i4 = 0;
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                        break;
                    }
                    break;
                case 27:
                    i6 = 1;
                    i11 = 2;
                    if (i >= 4) {
                        safeAppend(this.formatData.quarters, relatedYear / 3, stringBuffer);
                    } else if (i == 3) {
                        safeAppend(this.formatData.shortQuarters, relatedYear / 3, stringBuffer);
                    } else {
                        zeroPaddingNumber(numberFormat, stringBuffer, (relatedYear / 3) + 1, i, Integer.MAX_VALUE);
                    }
                    i5 = i11;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 28:
                    i6 = 1;
                    i11 = 2;
                    if (i >= 4) {
                        safeAppend(this.formatData.standaloneQuarters, relatedYear / 3, stringBuffer);
                    } else if (i == 3) {
                        safeAppend(this.formatData.standaloneShortQuarters, relatedYear / 3, stringBuffer);
                    } else {
                        zeroPaddingNumber(numberFormat, stringBuffer, (relatedYear / 3) + 1, i, Integer.MAX_VALUE);
                    }
                    i5 = i11;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 29:
                    i6 = 1;
                    if (i == 1) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.ZONE_ID_SHORT, timeZone, timeInMillis);
                    } else if (i == 2) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.ZONE_ID, timeZone, timeInMillis);
                    } else if (i == 3) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.EXEMPLAR_LOCATION, timeZone, timeInMillis);
                    } else if (i == 4) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.GENERIC_LOCATION, timeZone, timeInMillis);
                        capitalizationContextUsage3 = DateFormatSymbols.CapitalizationContextUsage.ZONE_LONG;
                    }
                    stringBuffer.append(str4);
                    i5 = 2;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i4 = 0;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 30:
                    i6 = 1;
                    i11 = 2;
                    if (this.formatData.shortYearNames == null || relatedYear > this.formatData.shortYearNames.length) {
                        i5 = 2;
                        i4 = 0;
                        String str52 = this.override;
                        relatedYear -= 5000;
                        if (i != i5) {
                        }
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                        break;
                    } else {
                        safeAppend(this.formatData.shortYearNames, relatedYear - 1, stringBuffer);
                        i5 = i11;
                        i4 = 0;
                        capitalizationContextUsage = capitalizationContextUsage3;
                        i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                        if (i7 == i6) {
                        }
                        if (i4 != 0) {
                        }
                        if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                        }
                    }
                    break;
                case 31:
                    i6 = 1;
                    i11 = 2;
                    if (i == 1) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.LOCALIZED_GMT_SHORT, timeZone, timeInMillis);
                    } else if (i == 4) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.LOCALIZED_GMT, timeZone, timeInMillis);
                    }
                    stringBuffer.append(str4);
                    i5 = i11;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 32:
                    i6 = 1;
                    i11 = 2;
                    if (i == 1) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_SHORT, timeZone, timeInMillis);
                    } else if (i == 2) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_FIXED, timeZone, timeInMillis);
                    } else if (i == 3) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_FIXED, timeZone, timeInMillis);
                    } else if (i == 4) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_FULL, timeZone, timeInMillis);
                    } else if (i == 5) {
                        str4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_FULL, timeZone, timeInMillis);
                    }
                    stringBuffer.append(str4);
                    i5 = i11;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 33:
                    i6 = 1;
                    if (i == 1) {
                        str3 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_SHORT, timeZone, timeInMillis);
                        i11 = 2;
                    } else {
                        i11 = 2;
                        if (i == 2) {
                            str4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_FIXED, timeZone, timeInMillis);
                        } else if (i == 3) {
                            str4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FIXED, timeZone, timeInMillis);
                        } else if (i == 4) {
                            str4 = tzFormat().format(TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL, timeZone, timeInMillis);
                        } else if (i == 5) {
                            str4 = tzFormat().format(TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FULL, timeZone, timeInMillis);
                        }
                        str3 = str4;
                    }
                    stringBuffer.append(str3);
                    i5 = i11;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 35:
                    if (calendar.get(11) == 12 && ((!this.hasMinute || calendar.get(12) == 0) && (!this.hasSecond || calendar.get(13) == 0))) {
                        int i15 = calendar.get(9);
                        str4 = i <= 3 ? this.formatData.abbreviatedDayPeriods[i15] : (i == 4 || i > 5) ? this.formatData.wideDayPeriods[i15] : this.formatData.narrowDayPeriods[i15];
                    }
                    if (str4 == null) {
                        subFormat(stringBuffer, 'a', i, i2, i3, displayContext, fieldPosition, calendar);
                    } else {
                        stringBuffer.append(str4);
                    }
                    i6 = 1;
                    i5 = 2;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 36:
                    DayPeriodRules instance = DayPeriodRules.getInstance(getLocale());
                    if (instance == null) {
                        subFormat(stringBuffer, 'a', i, i2, i3, displayContext, fieldPosition, calendar);
                    } else {
                        int i16 = calendar.get(11);
                        int i17 = this.hasMinute ? calendar.get(12) : 0;
                        int i18 = this.hasSecond ? calendar.get(13) : 0;
                        if (i16 == 0 && i17 == 0 && i18 == 0 && instance.hasMidnight()) {
                            dayPeriod = DayPeriodRules.DayPeriod.MIDNIGHT;
                        } else if (i16 == 12 && i17 == 0 && i18 == 0 && instance.hasNoon()) {
                            dayPeriod = DayPeriodRules.DayPeriod.NOON;
                        } else {
                            dayPeriod = instance.getDayPeriodForHour(i16);
                        }
                        if (!(dayPeriod == DayPeriodRules.DayPeriod.AM || dayPeriod == DayPeriodRules.DayPeriod.PM || dayPeriod == DayPeriodRules.DayPeriod.MIDNIGHT)) {
                            int ordinal = dayPeriod.ordinal();
                            str4 = i <= 3 ? this.formatData.abbreviatedDayPeriods[ordinal] : (i == 4 || i > 5) ? this.formatData.wideDayPeriods[ordinal] : this.formatData.narrowDayPeriods[ordinal];
                        }
                        if (str4 == null && (dayPeriod == DayPeriodRules.DayPeriod.MIDNIGHT || dayPeriod == DayPeriodRules.DayPeriod.NOON)) {
                            dayPeriod = instance.getDayPeriodForHour(i16);
                            int ordinal2 = dayPeriod.ordinal();
                            if (i <= 3) {
                                str4 = this.formatData.abbreviatedDayPeriods[ordinal2];
                            } else if (i == 4 || i > 5) {
                                str4 = this.formatData.wideDayPeriods[ordinal2];
                            } else {
                                str4 = this.formatData.narrowDayPeriods[ordinal2];
                            }
                        }
                        if (dayPeriod == DayPeriodRules.DayPeriod.AM || dayPeriod == DayPeriodRules.DayPeriod.PM || str4 == null) {
                            subFormat(stringBuffer, 'a', i, i2, i3, displayContext, fieldPosition, calendar);
                        } else {
                            stringBuffer.append(str4);
                        }
                    }
                    i6 = 1;
                    i5 = 2;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
                case 37:
                    stringBuffer.append(this.formatData.getTimeSeparatorString());
                    i6 = 1;
                    i5 = 2;
                    i4 = 0;
                    capitalizationContextUsage = capitalizationContextUsage3;
                    i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext[displayContext.ordinal()];
                    if (i7 == i6) {
                    }
                    if (i4 != 0) {
                    }
                    if (fieldPosition.getBeginIndex() != fieldPosition.getEndIndex()) {
                    }
                    break;
            }
        } else if (c != 'l') {
            throw new IllegalArgumentException("Illegal pattern character '" + c + "' in \"" + this.pattern + '\"');
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.text.SimpleDateFormat$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$text$DisplayContext = new int[DisplayContext.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$text$DisplayContext[DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$DisplayContext[DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$DisplayContext[DisplayContext.CAPITALIZATION_FOR_STANDALONE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private static void safeAppend(String[] strArr, int i, StringBuffer stringBuffer) {
        if (strArr != null && i >= 0 && i < strArr.length) {
            stringBuffer.append(strArr[i]);
        }
    }

    private static void safeAppendWithMonthPattern(String[] strArr, int i, StringBuffer stringBuffer, String str) {
        if (strArr != null && i >= 0 && i < strArr.length) {
            if (str == null) {
                stringBuffer.append(strArr[i]);
            } else {
                stringBuffer.append(SimpleFormatterImpl.formatRawPattern(str, 1, 1, strArr[i]));
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PatternItem {
        final boolean isNumeric;
        final int length;
        final char type;

        PatternItem(char c, int i) {
            this.type = c;
            this.length = i;
            this.isNumeric = SimpleDateFormat.isNumeric(c, i);
        }
    }

    private Object[] getPatternItems() {
        char c;
        boolean z;
        Object[] objArr = this.patternItems;
        if (objArr != null) {
            return objArr;
        }
        this.patternItems = PARSED_PATTERN_CACHE.get(this.pattern);
        Object[] objArr2 = this.patternItems;
        if (objArr2 != null) {
            return objArr2;
        }
        StringBuilder sb = new StringBuilder();
        ArrayList arrayList = new ArrayList();
        int i = 1;
        char c2 = 0;
        boolean z2 = false;
        boolean z3 = false;
        for (int i2 = 0; i2 < this.pattern.length(); i2++) {
            char charAt = this.pattern.charAt(i2);
            if (charAt == '\'') {
                if (z3) {
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                    c = c2;
                    z = false;
                } else if (c2 != 0) {
                    arrayList.add(new PatternItem(c2, i));
                    z = true;
                    c = 0;
                } else {
                    c = c2;
                    z = true;
                }
                z2 = !z2;
                z3 = z;
                c2 = c;
            } else {
                if (z2) {
                    sb.append(charAt);
                } else if (!isSyntaxChar(charAt)) {
                    if (c2 != 0) {
                        arrayList.add(new PatternItem(c2, i));
                        c2 = 0;
                    }
                    sb.append(charAt);
                } else if (charAt == c2) {
                    i++;
                } else {
                    if (c2 != 0) {
                        arrayList.add(new PatternItem(c2, i));
                    } else if (sb.length() > 0) {
                        arrayList.add(sb.toString());
                        sb.setLength(0);
                    }
                    i = 1;
                    z3 = false;
                    c2 = charAt;
                }
                z3 = false;
            }
        }
        if (c2 != 0) {
            arrayList.add(new PatternItem(c2, i));
        } else if (sb.length() > 0) {
            arrayList.add(sb.toString());
            sb.setLength(0);
        }
        this.patternItems = arrayList.toArray(new Object[arrayList.size()]);
        PARSED_PATTERN_CACHE.put(this.pattern, this.patternItems);
        return this.patternItems;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void zeroPaddingNumber(NumberFormat numberFormat, StringBuffer stringBuffer, int i, int i2, int i3) {
        if (!this.useLocalZeroPaddingNumberFormat || i < 0) {
            numberFormat.setMinimumIntegerDigits(i2);
            numberFormat.setMaximumIntegerDigits(i3);
            numberFormat.format((long) i, stringBuffer, new FieldPosition(-1));
            return;
        }
        fastZeroPaddingNumber(stringBuffer, i, i2, i3);
    }

    @Override // ohos.global.icu.text.DateFormat
    public void setNumberFormat(NumberFormat numberFormat) {
        super.setNumberFormat(numberFormat);
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
            String[] digitStringsLocal = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getDigitStringsLocal();
            this.useLocalZeroPaddingNumberFormat = true;
            this.decDigits = new char[10];
            int i = 0;
            while (true) {
                if (i >= 10) {
                    break;
                } else if (digitStringsLocal[i].length() > 1) {
                    this.useLocalZeroPaddingNumberFormat = false;
                    break;
                } else {
                    this.decDigits[i] = digitStringsLocal[i].charAt(0);
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

    private void fastZeroPaddingNumber(StringBuffer stringBuffer, int i, int i2, int i3) {
        char[] cArr = this.decimalBuf;
        if (cArr.length < i3) {
            i3 = cArr.length;
        }
        int i4 = i3 - 1;
        while (true) {
            this.decimalBuf[i4] = this.decDigits[i % 10];
            i /= 10;
            if (i4 == 0 || i == 0) {
                break;
            }
            i4--;
        }
        int i5 = i2 - (i3 - i4);
        while (i5 > 0 && i4 > 0) {
            i4--;
            this.decimalBuf[i4] = this.decDigits[0];
            i5--;
        }
        while (i5 > 0) {
            stringBuffer.append(this.decDigits[0]);
            i5--;
        }
        stringBuffer.append(this.decimalBuf, i4, i3 - i4);
    }

    /* access modifiers changed from: protected */
    public String zeroPaddingNumber(long j, int i, int i2) {
        this.numberFormat.setMinimumIntegerDigits(i);
        this.numberFormat.setMaximumIntegerDigits(i2);
        return this.numberFormat.format(j);
    }

    /* access modifiers changed from: private */
    public static final boolean isNumeric(char c, int i) {
        return NUMERIC_FORMAT_CHARS.indexOf(c) >= 0 || (i <= 2 && NUMERIC_FORMAT_CHARS2.indexOf(c) >= 0);
    }

    @Override // ohos.global.icu.text.DateFormat
    public void parse(String str, Calendar calendar, ParsePosition parsePosition) {
        Calendar calendar2;
        TimeZone timeZone;
        Calendar calendar3;
        int i;
        int i2;
        TimeZoneTransition previousTransition;
        TimeZoneTransition nextTransition;
        int i3;
        Output<TimeZoneFormat.TimeType> output;
        boolean[] zArr;
        int i4;
        int i5;
        Calendar calendar4;
        Output<DayPeriodRules.DayPeriod> output2;
        TimeZone timeZone2;
        int i6;
        Object[] objArr;
        int i7;
        int i8;
        int i9;
        int i10;
        if (calendar == this.calendar || calendar.getType().equals(this.calendar.getType())) {
            calendar2 = calendar;
            calendar3 = null;
            timeZone = null;
        } else {
            this.calendar.setTimeInMillis(calendar.getTimeInMillis());
            TimeZone timeZone3 = this.calendar.getTimeZone();
            this.calendar.setTimeZone(calendar.getTimeZone());
            timeZone = timeZone3;
            calendar3 = calendar;
            calendar2 = this.calendar;
        }
        int index = parsePosition.getIndex();
        if (index < 0) {
            parsePosition.setErrorIndex(0);
            return;
        }
        Output<DayPeriodRules.DayPeriod> output3 = new Output<>((Object) null);
        Output<TimeZoneFormat.TimeType> output4 = new Output<>(TimeZoneFormat.TimeType.UNKNOWN);
        boolean[] zArr2 = {false};
        MessageFormat messageFormat = (this.formatData.leapMonthPatterns == null || this.formatData.leapMonthPatterns.length < 7) ? null : new MessageFormat(this.formatData.leapMonthPatterns[6], this.locale);
        Object[] patternItems2 = getPatternItems();
        int i11 = -1;
        int i12 = -1;
        int i13 = 0;
        int i14 = 0;
        int i15 = 0;
        int i16 = index;
        while (i13 < patternItems2.length) {
            if (patternItems2[i13] instanceof PatternItem) {
                PatternItem patternItem = (PatternItem) patternItems2[i13];
                if (!patternItem.isNumeric || i12 != i11 || (i10 = i13 + 1) >= patternItems2.length || !(patternItems2[i10] instanceof PatternItem) || !((PatternItem) patternItems2[i10]).isNumeric) {
                    i7 = i12;
                } else {
                    i15 = i16;
                    i7 = i13;
                    i14 = patternItem.length;
                }
                if (i7 != -1) {
                    int i17 = patternItem.length;
                    if (i7 == i13) {
                        i17 = i14;
                    }
                    i4 = -1;
                    zArr = zArr2;
                    output = output4;
                    output2 = output3;
                    i8 = i7;
                    i16 = subParse(str, i16, patternItem.type, i17, true, false, zArr, calendar2, messageFormat, output);
                    if (i16 < 0) {
                        i14--;
                        if (i14 == 0) {
                            parsePosition.setIndex(index);
                            parsePosition.setErrorIndex(i16);
                            if (timeZone != null) {
                                this.calendar.setTimeZone(timeZone);
                                return;
                            }
                            return;
                        }
                        output4 = output;
                        calendar3 = calendar3;
                        i16 = i15;
                        i13 = i8;
                        i12 = i13;
                        output3 = output2;
                        i11 = -1;
                        patternItems2 = patternItems2;
                        zArr2 = zArr;
                        index = index;
                    } else {
                        calendar4 = calendar3;
                        i9 = i13;
                        objArr = patternItems2;
                        i6 = index;
                        timeZone2 = timeZone;
                    }
                } else {
                    i4 = -1;
                    zArr = zArr2;
                    output = output4;
                    output2 = output3;
                    i8 = i7;
                    if (patternItem.type != 'l') {
                        calendar4 = calendar3;
                        timeZone2 = timeZone;
                        i16 = subParse(str, i16, patternItem.type, patternItem.length, false, true, zArr, calendar2, messageFormat, output, output2);
                        if (i16 >= 0) {
                            objArr = patternItems2;
                            i6 = index;
                        } else if (i16 == ISOSpecialEra) {
                            i9 = i13 + 1;
                            objArr = patternItems2;
                            if (i9 < objArr.length) {
                                try {
                                    String str2 = (String) objArr[i9];
                                    if (str2 == null) {
                                        str2 = (String) objArr[i9];
                                    }
                                    int length = str2.length();
                                    int i18 = 0;
                                    while (i18 < length && PatternProps.isWhiteSpace(str2.charAt(i18))) {
                                        i18++;
                                    }
                                    if (i18 != length) {
                                        i9 = i13;
                                    }
                                    i16 = i16;
                                    i8 = -1;
                                    i6 = index;
                                } catch (ClassCastException unused) {
                                    parsePosition.setIndex(index);
                                    parsePosition.setErrorIndex(i16);
                                    if (timeZone2 != null) {
                                        this.calendar.setTimeZone(timeZone2);
                                        return;
                                    }
                                    return;
                                }
                            } else {
                                i6 = index;
                                i16 = i16;
                            }
                        } else {
                            parsePosition.setIndex(index);
                            parsePosition.setErrorIndex(i16);
                            if (timeZone2 != null) {
                                this.calendar.setTimeZone(timeZone2);
                                return;
                            }
                            return;
                        }
                        i9 = i13;
                        i8 = -1;
                    } else {
                        i6 = index;
                        calendar4 = calendar3;
                        objArr = patternItems2;
                        timeZone2 = timeZone;
                        i16 = i16;
                        i9 = i13;
                    }
                }
                i5 = i9;
                i12 = i8;
            } else {
                i5 = i13;
                i4 = i11;
                zArr = zArr2;
                output = output4;
                output2 = output3;
                i6 = index;
                calendar4 = calendar3;
                timeZone2 = timeZone;
                objArr = patternItems2;
                boolean[] zArr3 = new boolean[1];
                i16 = matchLiteral(str, i16, patternItems2, i5, zArr3);
                if (!zArr3[0]) {
                    parsePosition.setIndex(i6);
                    parsePosition.setErrorIndex(i16);
                    if (timeZone2 != null) {
                        this.calendar.setTimeZone(timeZone2);
                        return;
                    }
                    return;
                }
                i12 = i4;
            }
            i13 = i5 + 1;
            patternItems2 = objArr;
            index = i6;
            timeZone = timeZone2;
            output3 = output2;
            calendar3 = calendar4;
            i11 = i4;
            zArr2 = zArr;
            output4 = output;
        }
        int i19 = i16;
        if (i19 < str.length() && str.charAt(i19) == '.' && getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE) && patternItems2.length != 0) {
            Object obj = patternItems2[patternItems2.length - 1];
            if ((obj instanceof PatternItem) && !((PatternItem) obj).isNumeric) {
                i19++;
            }
        }
        if (output3.value != null) {
            DayPeriodRules instance = DayPeriodRules.getInstance(getLocale());
            if (calendar2.isSet(10) || calendar2.isSet(11)) {
                if (calendar2.isSet(11)) {
                    i3 = calendar2.get(11);
                } else {
                    i3 = calendar2.get(10);
                    if (i3 == 0) {
                        i3 = 12;
                    }
                }
                if (i3 == 0 || (13 <= i3 && i3 <= 23)) {
                    calendar2.set(11, i3);
                } else {
                    if (i3 == 12) {
                        i3 = 0;
                    }
                    double midPointForDayPeriod = (((double) i3) + (((double) calendar2.get(12)) / 60.0d)) - instance.getMidPointForDayPeriod((DayPeriodRules.DayPeriod) output3.value);
                    if (-6.0d > midPointForDayPeriod || midPointForDayPeriod >= 6.0d) {
                        calendar2.set(9, 1);
                    } else {
                        calendar2.set(9, 0);
                    }
                }
            } else {
                double midPointForDayPeriod2 = instance.getMidPointForDayPeriod((DayPeriodRules.DayPeriod) output3.value);
                int i20 = (int) midPointForDayPeriod2;
                int i21 = midPointForDayPeriod2 - ((double) i20) > XPath.MATCH_SCORE_QNAME ? 30 : 0;
                calendar2.set(11, i20);
                calendar2.set(12, i21);
            }
        }
        parsePosition.setIndex(i19);
        try {
            TimeZoneFormat.TimeType timeType = (TimeZoneFormat.TimeType) output4.value;
            if (zArr2[0] || timeType != TimeZoneFormat.TimeType.UNKNOWN) {
                if (zArr2[0] && ((Calendar) calendar2.clone()).getTime().before(getDefaultCenturyStart())) {
                    calendar2.set(1, getDefaultCenturyStartYear() + 100);
                }
                if (timeType != TimeZoneFormat.TimeType.UNKNOWN) {
                    Calendar calendar5 = (Calendar) calendar2.clone();
                    BasicTimeZone timeZone4 = calendar5.getTimeZone();
                    BasicTimeZone basicTimeZone = timeZone4 instanceof BasicTimeZone ? timeZone4 : null;
                    calendar5.set(15, 0);
                    calendar5.set(16, 0);
                    long timeInMillis = calendar5.getTimeInMillis();
                    int[] iArr = new int[2];
                    if (basicTimeZone == null) {
                        timeZone4.getOffset(timeInMillis, true, iArr);
                        if ((timeType == TimeZoneFormat.TimeType.STANDARD && iArr[1] != 0) || (timeType == TimeZoneFormat.TimeType.DAYLIGHT && iArr[1] == 0)) {
                            timeZone4.getOffset(timeInMillis - CalendarAstronomer.DAY_MS, true, iArr);
                        }
                    } else if (timeType == TimeZoneFormat.TimeType.STANDARD) {
                        basicTimeZone.getOffsetFromLocal(timeInMillis, 1, 1, iArr);
                    } else {
                        basicTimeZone.getOffsetFromLocal(timeInMillis, 3, 3, iArr);
                    }
                    int i22 = iArr[1];
                    if (timeType == TimeZoneFormat.TimeType.STANDARD) {
                        if (iArr[1] != 0) {
                            i = 0;
                            calendar2.set(15, iArr[0]);
                            calendar2.set(16, i);
                        }
                    } else if (iArr[1] == 0) {
                        if (basicTimeZone != null) {
                            long j = timeInMillis + ((long) iArr[0]);
                            i2 = 0;
                            long j2 = j;
                            while (true) {
                                previousTransition = basicTimeZone.getPreviousTransition(j2, true);
                                if (previousTransition != null) {
                                    j2 = previousTransition.getTime() - 1;
                                    i2 = previousTransition.getFrom().getDSTSavings();
                                    if (i2 != 0) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            int i23 = 0;
                            long j3 = j;
                            while (true) {
                                nextTransition = basicTimeZone.getNextTransition(j3, false);
                                if (nextTransition != null) {
                                    j3 = nextTransition.getTime();
                                    i23 = nextTransition.getTo().getDSTSavings();
                                    if (i23 != 0) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (previousTransition == null || nextTransition == null) {
                                if (previousTransition == null || i2 == 0) {
                                    if (nextTransition == null || i23 == 0) {
                                        i2 = basicTimeZone.getDSTSavings();
                                    }
                                }
                            } else if (j - j2 > j3 - j) {
                            }
                            i2 = i23;
                        } else {
                            i2 = timeZone4.getDSTSavings();
                        }
                        i = i2;
                        if (i == 0) {
                            i = 3600000;
                        }
                        calendar2.set(15, iArr[0]);
                        calendar2.set(16, i);
                    }
                    i = i22;
                    calendar2.set(15, iArr[0]);
                    calendar2.set(16, i);
                }
            }
            if (calendar3 != null) {
                calendar3.setTimeZone(calendar2.getTimeZone());
                calendar3.setTimeInMillis(calendar2.getTimeInMillis());
            }
            if (timeZone != null) {
                this.calendar.setTimeZone(timeZone);
            }
        } catch (IllegalArgumentException unused2) {
            parsePosition.setErrorIndex(i19);
            parsePosition.setIndex(index);
            if (timeZone != null) {
                this.calendar.setTimeZone(timeZone);
            }
        }
    }

    private int matchLiteral(String str, int i, Object[] objArr, int i2, boolean[] zArr) {
        boolean z;
        String str2 = (String) objArr[i2];
        int length = str2.length();
        int length2 = str.length();
        int i3 = i;
        int i4 = 0;
        while (true) {
            z = true;
            if (i4 >= length || i3 >= length2) {
                break;
            }
            char charAt = str2.charAt(i4);
            char charAt2 = str.charAt(i3);
            if (!PatternProps.isWhiteSpace(charAt) || !PatternProps.isWhiteSpace(charAt2)) {
                if (charAt != charAt2) {
                    if (charAt2 != '.' || i3 != i || i2 <= 0 || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE)) {
                        if (((charAt != ' ' && charAt != '.') || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE)) && (i3 == i || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH))) {
                            break;
                        }
                        i4++;
                    } else {
                        Object obj = objArr[i2 - 1];
                        if (!(obj instanceof PatternItem) || ((PatternItem) obj).isNumeric) {
                            break;
                        }
                        i3++;
                    }
                }
            } else {
                while (true) {
                    int i5 = i4 + 1;
                    if (i5 >= length || !PatternProps.isWhiteSpace(str2.charAt(i5))) {
                        break;
                    }
                    i4 = i5;
                }
                while (true) {
                    int i6 = i3 + 1;
                    if (i6 >= length2 || !PatternProps.isWhiteSpace(str.charAt(i6))) {
                        break;
                    }
                    i3 = i6;
                }
            }
            i4++;
            i3++;
        }
        zArr[0] = i4 == length;
        if (!zArr[0] && getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_WHITESPACE) && i2 > 0 && i2 < objArr.length - 1 && i < length2) {
            Object obj2 = objArr[i2 - 1];
            Object obj3 = objArr[i2 + 1];
            if ((obj2 instanceof PatternItem) && (obj3 instanceof PatternItem)) {
                if (DATE_PATTERN_TYPE.contains(((PatternItem) obj2).type) != DATE_PATTERN_TYPE.contains(((PatternItem) obj3).type)) {
                    i3 = i;
                    while (i3 < length2 && PatternProps.isWhiteSpace(str.charAt(i3))) {
                        i3++;
                    }
                    if (i3 <= i) {
                        z = false;
                    }
                    zArr[0] = z;
                }
            }
        }
        return i3;
    }

    /* access modifiers changed from: protected */
    public int matchString(String str, int i, int i2, String[] strArr, Calendar calendar) {
        return matchString(str, i, i2, strArr, null, calendar);
    }

    @Deprecated
    private int matchString(String str, int i, int i2, String[] strArr, String str2, Calendar calendar) {
        String formatRawPattern;
        int length;
        int regionMatchesWithOptionalDot;
        int regionMatchesWithOptionalDot2;
        int length2 = strArr.length;
        int i3 = -1;
        int i4 = 0;
        int i5 = 0;
        for (int i6 = i2 == 7 ? 1 : 0; i6 < length2; i6++) {
            int length3 = strArr[i6].length();
            if (length3 > i4 && (regionMatchesWithOptionalDot2 = regionMatchesWithOptionalDot(str, i, strArr[i6], length3)) >= 0) {
                i5 = 0;
                i3 = i6;
                i4 = regionMatchesWithOptionalDot2;
            }
            if (str2 != null && (length = (formatRawPattern = SimpleFormatterImpl.formatRawPattern(str2, 1, 1, strArr[i6])).length()) > i4 && (regionMatchesWithOptionalDot = regionMatchesWithOptionalDot(str, i, formatRawPattern, length)) >= 0) {
                i5 = 1;
                i3 = i6;
                i4 = regionMatchesWithOptionalDot;
            }
        }
        if (i3 < 0) {
            return ~i;
        }
        if (i2 >= 0) {
            if (i2 == 1) {
                i3++;
            }
            calendar.set(i2, i3);
            if (str2 != null) {
                calendar.set(22, i5);
            }
        }
        return i + i4;
    }

    private int regionMatchesWithOptionalDot(String str, int i, String str2, int i2) {
        if (str.regionMatches(true, i, str2, 0, i2)) {
            return i2;
        }
        if (str2.length() <= 0 || str2.charAt(str2.length() - 1) != '.') {
            return -1;
        }
        int i3 = i2 - 1;
        if (str.regionMatches(true, i, str2, 0, i3)) {
            return i3;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int matchQuarterString(String str, int i, int i2, String[] strArr, Calendar calendar) {
        int regionMatchesWithOptionalDot;
        int length = strArr.length;
        int i3 = -1;
        int i4 = 0;
        for (int i5 = 0; i5 < length; i5++) {
            int length2 = strArr[i5].length();
            if (length2 > i4 && (regionMatchesWithOptionalDot = regionMatchesWithOptionalDot(str, i, strArr[i5], length2)) >= 0) {
                i3 = i5;
                i4 = regionMatchesWithOptionalDot;
            }
        }
        if (i3 < 0) {
            return -i;
        }
        calendar.set(i2, i3 * 3);
        return i + i4;
    }

    private int matchDayPeriodString(String str, int i, String[] strArr, int i2, Output<DayPeriodRules.DayPeriod> output) {
        int length;
        int regionMatchesWithOptionalDot;
        int i3 = -1;
        int i4 = 0;
        for (int i5 = 0; i5 < i2; i5++) {
            if (strArr[i5] != null && (length = strArr[i5].length()) > i4 && (regionMatchesWithOptionalDot = regionMatchesWithOptionalDot(str, i, strArr[i5], length)) >= 0) {
                i3 = i5;
                i4 = regionMatchesWithOptionalDot;
            }
        }
        if (i3 < 0) {
            return -i;
        }
        output.value = DayPeriodRules.DayPeriod.VALUES[i3];
        return i + i4;
    }

    /* access modifiers changed from: protected */
    public int subParse(String str, int i, char c, int i2, boolean z, boolean z2, boolean[] zArr, Calendar calendar) {
        return subParse(str, i, c, i2, z, z2, zArr, calendar, null, null);
    }

    private int subParse(String str, int i, char c, int i2, boolean z, boolean z2, boolean[] zArr, Calendar calendar, MessageFormat messageFormat, Output<TimeZoneFormat.TimeType> output) {
        return subParse(str, i, c, i2, z, z2, zArr, calendar, null, null, null);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:70:0x010e */
    /* JADX WARN: Type inference failed for: r9v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r9v23 */
    /* JADX WARN: Type inference failed for: r9v24 */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x020a  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0295  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x02c4  */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x02f1  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x030d  */
    /* JADX WARNING: Removed duplicated region for block: B:206:0x034c  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x0373  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x03d2  */
    /* JADX WARNING: Removed duplicated region for block: B:263:0x0431  */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x04b2  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x04ce  */
    /* JADX WARNING: Removed duplicated region for block: B:312:0x04ef  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x050d  */
    /* JADX WARNING: Removed duplicated region for block: B:330:0x0529  */
    /* JADX WARNING: Removed duplicated region for block: B:335:0x053c  */
    /* JADX WARNING: Removed duplicated region for block: B:354:0x058c  */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x05b0 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x0617  */
    /* JADX WARNING: Removed duplicated region for block: B:394:0x063c  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x064f  */
    /* JADX WARNING: Removed duplicated region for block: B:456:0x073c  */
    /* JADX WARNING: Removed duplicated region for block: B:488:0x07aa  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00e5  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0118  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0130  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x013e  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0193  */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Deprecated
    private int subParse(String str, int i, char c, int i2, boolean z, boolean z2, boolean[] zArr, Calendar calendar, MessageFormat messageFormat, Output<TimeZoneFormat.TimeType> output, Output<DayPeriodRules.DayPeriod> output2) {
        Number number;
        int i3;
        int i4;
        ?? r9;
        int i5;
        int i6;
        int i7;
        boolean z3;
        char c2;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        int i13;
        int i14;
        int matchString;
        int i15;
        int i16;
        int i17;
        int i18;
        TimeZoneFormat.Style style;
        int i19;
        TimeZoneFormat.Style style2;
        TimeZoneFormat.Style style3;
        int i20;
        int i21;
        int i22;
        Number number2;
        boolean z4;
        Number number3;
        boolean z5;
        boolean z6;
        Number number4;
        int i23;
        int i24;
        ParsePosition parsePosition = new ParsePosition(0);
        int indexFromChar = getIndexFromChar(c);
        if (indexFromChar == -1) {
            return ~i;
        }
        NumberFormat numberFormat = getNumberFormat(c);
        int i25 = PATTERN_INDEX_TO_CALENDAR_FIELD[indexFromChar];
        if (messageFormat != null) {
            messageFormat.setFormatByArgumentIndex(0, numberFormat);
        }
        boolean z7 = calendar.getType().equals("chinese") || calendar.getType().equals("dangi");
        int i26 = i;
        while (i26 < str.length()) {
            int charAt = UTF16.charAt(str, i26);
            if (!UCharacter.isUWhiteSpace(charAt) || !PatternProps.isWhiteSpace(charAt)) {
                parsePosition.setIndex(i26);
                if (indexFromChar == 4 || indexFromChar == 15 || ((indexFromChar == 2 && i2 <= 2) || indexFromChar == 26 || indexFromChar == 19 || indexFromChar == 25 || indexFromChar == 1 || indexFromChar == 18 || indexFromChar == 30 || ((indexFromChar == 0 && z7) || indexFromChar == 27 || indexFromChar == 28 || indexFromChar == 8))) {
                    if (messageFormat != null && (indexFromChar == 2 || indexFromChar == 26)) {
                        Object[] parse = messageFormat.parse(str, parsePosition);
                        if (parse == null || parsePosition.getIndex() <= i26) {
                            i24 = 22;
                            i23 = 0;
                        } else {
                            i23 = 0;
                            if (parse[0] instanceof Number) {
                                calendar.set(22, 1);
                                number3 = (Number) parse[0];
                                z5 = true;
                                if (z5) {
                                    if (!z) {
                                        i4 = i26;
                                        z6 = true;
                                        i3 = i25;
                                        number4 = parseInt(str, parsePosition, z2, numberFormat);
                                    } else if (i26 + i2 > str.length()) {
                                        return ~i26;
                                    } else {
                                        i4 = i26;
                                        z6 = true;
                                        i3 = i25;
                                        number4 = parseInt(str, i2, parsePosition, z2, numberFormat);
                                    }
                                    if (number4 == null && !allowNumericFallback(indexFromChar)) {
                                        return ~i4;
                                    }
                                } else {
                                    i4 = i26;
                                    z6 = true;
                                    i3 = i25;
                                    number4 = number3;
                                }
                                if (number4 == null) {
                                    number = number4;
                                    i5 = number4.intValue();
                                    r9 = z6;
                                    switch (indexFromChar) {
                                        case 0:
                                            if (z7) {
                                                calendar.set(0, i5);
                                                return parsePosition.getIndex();
                                            }
                                            if (i2 == 5) {
                                                i6 = matchString(str, i4, 0, this.formatData.narrowEras, null, calendar);
                                            } else if (i2 == 4) {
                                                i6 = matchString(str, i4, 0, this.formatData.eraNames, null, calendar);
                                            } else {
                                                i6 = matchString(str, i4, 0, this.formatData.eras, null, calendar);
                                            }
                                            return i6 == (~i4) ? ISOSpecialEra : i6;
                                        case 1:
                                        case 18:
                                            String str2 = this.override;
                                            if (str2 != null && ((str2.compareTo("hebr") == 0 || this.override.indexOf("y=hebr") >= 0) && i5 < 1000)) {
                                                i7 = i5 + HEBREW_CAL_CUR_MILLENIUM_START_YEAR;
                                            } else if (i2 == 2 && countDigits(str, i4, parsePosition.getIndex()) == 2 && calendar.haveDefaultCentury()) {
                                                int i27 = 100;
                                                int defaultCenturyStartYear2 = getDefaultCenturyStartYear() % 100;
                                                if (i5 == defaultCenturyStartYear2) {
                                                    z3 = r9;
                                                    c2 = 0;
                                                } else {
                                                    c2 = 0;
                                                    z3 = false;
                                                }
                                                zArr[c2] = z3;
                                                int defaultCenturyStartYear3 = (getDefaultCenturyStartYear() / 100) * 100;
                                                if (i5 >= defaultCenturyStartYear2) {
                                                    i27 = 0;
                                                }
                                                i7 = i5 + defaultCenturyStartYear3 + i27;
                                            } else {
                                                i7 = i5;
                                            }
                                            calendar.set(i3, i7);
                                            if (DelayedHebrewMonthCheck) {
                                                if (!HebrewCalendar.isLeapYear(i7)) {
                                                    calendar.add(2, (int) r9);
                                                }
                                                DelayedHebrewMonthCheck = false;
                                            }
                                            return parsePosition.getIndex();
                                        case 2:
                                        case 26:
                                            if (i2 <= 2 || (number != null && getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC))) {
                                                calendar.set(2, i5 - 1);
                                                if (calendar.getType().equals("hebrew") && i5 >= 6) {
                                                    if (!calendar.isSet((int) r9)) {
                                                        DelayedHebrewMonthCheck = r9;
                                                    } else if (!HebrewCalendar.isLeapYear(calendar.get((int) r9))) {
                                                        calendar.set(2, i5);
                                                    }
                                                }
                                                return parsePosition.getIndex();
                                            }
                                            boolean z8 = (this.formatData.leapMonthPatterns == null || this.formatData.leapMonthPatterns.length < 7) ? false : r9;
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) {
                                                if (indexFromChar == 2) {
                                                    i8 = 3;
                                                    i9 = matchString(str, i4, 2, this.formatData.months, z8 ? this.formatData.leapMonthPatterns[0] : null, calendar);
                                                } else {
                                                    i8 = 3;
                                                    i9 = matchString(str, i4, 2, this.formatData.standaloneMonths, z8 ? this.formatData.leapMonthPatterns[3] : null, calendar);
                                                }
                                                if (i9 > 0) {
                                                    return i9;
                                                }
                                            } else {
                                                i8 = 3;
                                                i9 = 0;
                                            }
                                            if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && i2 != i8) {
                                                return i9;
                                            }
                                            if (indexFromChar == 2) {
                                                return matchString(str, i4, 2, this.formatData.shortMonths, z8 ? this.formatData.leapMonthPatterns[r9] : null, calendar);
                                            }
                                            return matchString(str, i4, 2, this.formatData.standaloneShortMonths, z8 ? this.formatData.leapMonthPatterns[4] : null, calendar);
                                        case 3:
                                        case 5:
                                        case 6:
                                        case 7:
                                        case 10:
                                        case 11:
                                        case 12:
                                        case 13:
                                        case 16:
                                        case 20:
                                        case 21:
                                        case 22:
                                        case 34:
                                        default:
                                            if (!z) {
                                                i22 = i3;
                                                number2 = parseInt(str, parsePosition, z2, numberFormat);
                                            } else if (i4 + i2 > str.length()) {
                                                return -i4;
                                            } else {
                                                i22 = i3;
                                                number2 = parseInt(str, i2, parsePosition, z2, numberFormat);
                                            }
                                            if (number2 == null) {
                                                return ~i4;
                                            }
                                            if (indexFromChar != 34) {
                                                calendar.set(i22, number2.intValue());
                                            } else {
                                                calendar.setRelatedYear(number2.intValue());
                                            }
                                            return parsePosition.getIndex();
                                        case 4:
                                            int i28 = i5;
                                            if (i28 == calendar.getMaximum(11) + r9) {
                                                i28 = 0;
                                            }
                                            calendar.set(11, i28);
                                            return parsePosition.getIndex();
                                        case 8:
                                            int i29 = i5;
                                            int countDigits = countDigits(str, i4, parsePosition.getIndex());
                                            int i30 = r9;
                                            if (countDigits < 3) {
                                                while (countDigits < 3) {
                                                    i29 *= 10;
                                                    countDigits++;
                                                }
                                            } else {
                                                while (countDigits > 3) {
                                                    countDigits--;
                                                    i30 *= 10;
                                                }
                                                i29 /= i30 == 1 ? 1 : 0;
                                            }
                                            calendar.set(14, i29);
                                            return parsePosition.getIndex();
                                        case 9:
                                            i10 = 3;
                                            i12 = 5;
                                            i11 = 6;
                                            break;
                                        case 14:
                                            if (this.formatData.ampmsNarrow == null || i2 < 5 || getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH)) {
                                                i14 = 5;
                                                int matchString2 = matchString(str, i4, 9, this.formatData.ampms, null, calendar);
                                                if (matchString2 > 0) {
                                                    return matchString2;
                                                }
                                            } else {
                                                i14 = 5;
                                            }
                                            return (this.formatData.ampmsNarrow == null || (i2 < i14 && !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH)) || (matchString = matchString(str, i4, 9, this.formatData.ampmsNarrow, null, calendar)) <= 0) ? ~i4 : matchString;
                                        case 15:
                                            int i31 = i5;
                                            if (i31 == calendar.getLeastMaximum(10) + r9) {
                                                i31 = 0;
                                            }
                                            calendar.set(10, i31);
                                            return parsePosition.getIndex();
                                        case 17:
                                            TimeZone parse2 = tzFormat().parse(i2 < 4 ? TimeZoneFormat.Style.SPECIFIC_SHORT : TimeZoneFormat.Style.SPECIFIC_LONG, str, parsePosition, output);
                                            if (parse2 == null) {
                                                return ~i4;
                                            }
                                            calendar.setTimeZone(parse2);
                                            return parsePosition.getIndex();
                                        case 19:
                                            i10 = 3;
                                            i11 = 6;
                                            if (i2 > 2 && (number == null || !getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC))) {
                                                i12 = 5;
                                                break;
                                            } else {
                                                calendar.set(i3, i5);
                                                return parsePosition.getIndex();
                                            }
                                        case 23:
                                            TimeZone parse3 = tzFormat().parse(i2 < 4 ? TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL : i2 == 5 ? TimeZoneFormat.Style.ISO_EXTENDED_FULL : TimeZoneFormat.Style.LOCALIZED_GMT, str, parsePosition, output);
                                            if (parse3 == null) {
                                                return ~i4;
                                            }
                                            calendar.setTimeZone(parse3);
                                            return parsePosition.getIndex();
                                        case 24:
                                            TimeZone parse4 = tzFormat().parse(i2 < 4 ? TimeZoneFormat.Style.GENERIC_SHORT : TimeZoneFormat.Style.GENERIC_LONG, str, parsePosition, output);
                                            if (parse4 == null) {
                                                return ~i4;
                                            }
                                            calendar.setTimeZone(parse4);
                                            return parsePosition.getIndex();
                                        case 25:
                                            if (i2 == r9 || (number != null && getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC))) {
                                                calendar.set(i3, i5);
                                                return parsePosition.getIndex();
                                            }
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) {
                                                i15 = 6;
                                                i16 = matchString(str, i4, 7, this.formatData.standaloneWeekdays, null, calendar);
                                                if (i16 > 0) {
                                                    return i16;
                                                }
                                            } else {
                                                i15 = 6;
                                                i16 = 0;
                                            }
                                            if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 3) && (i16 = matchString(str, i4, 7, this.formatData.standaloneShortWeekdays, null, calendar)) > 0) {
                                                return i16;
                                            }
                                            return ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == i15) && this.formatData.standaloneShorterWeekdays != null) ? matchString(str, i4, 7, this.formatData.standaloneShorterWeekdays, null, calendar) : i16;
                                        case 27:
                                            if (i2 <= 2 || (number != null && getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC))) {
                                                calendar.set(2, (i5 - 1) * 3);
                                                return parsePosition.getIndex();
                                            }
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) {
                                                i17 = matchQuarterString(str, i4, 2, this.formatData.quarters, calendar);
                                                if (i17 > 0) {
                                                    return i17;
                                                }
                                            } else {
                                                i17 = 0;
                                            }
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 3) {
                                                return matchQuarterString(str, i4, 2, this.formatData.shortQuarters, calendar);
                                            }
                                            return i17;
                                        case 28:
                                            if (i2 <= 2 || (number != null && getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC))) {
                                                calendar.set(2, (i5 - 1) * 3);
                                                return parsePosition.getIndex();
                                            }
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) {
                                                i18 = matchQuarterString(str, i4, 2, this.formatData.standaloneQuarters, calendar);
                                                if (i18 > 0) {
                                                    return i18;
                                                }
                                            } else {
                                                i18 = 0;
                                            }
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 3) {
                                                return matchQuarterString(str, i4, 2, this.formatData.standaloneShortQuarters, calendar);
                                            }
                                            return i18;
                                        case 29:
                                            if (i2 == r9) {
                                                style = TimeZoneFormat.Style.ZONE_ID_SHORT;
                                            } else if (i2 == 2) {
                                                style = TimeZoneFormat.Style.ZONE_ID;
                                            } else if (i2 != 3) {
                                                style = TimeZoneFormat.Style.GENERIC_LOCATION;
                                            } else {
                                                style = TimeZoneFormat.Style.EXEMPLAR_LOCATION;
                                            }
                                            TimeZone parse5 = tzFormat().parse(style, str, parsePosition, output);
                                            if (parse5 == null) {
                                                return ~i4;
                                            }
                                            calendar.setTimeZone(parse5);
                                            return parsePosition.getIndex();
                                        case 30:
                                            if (this.formatData.shortYearNames != null) {
                                                i19 = i5;
                                                int matchString3 = matchString(str, i4, 1, this.formatData.shortYearNames, null, calendar);
                                                if (matchString3 > 0) {
                                                    return matchString3;
                                                }
                                            } else {
                                                i19 = i5;
                                            }
                                            if (number == null || (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC) && this.formatData.shortYearNames != null && i19 <= this.formatData.shortYearNames.length)) {
                                                return ~i4;
                                            }
                                            calendar.set((int) r9, i19);
                                            return parsePosition.getIndex();
                                        case 31:
                                            TimeZone parse6 = tzFormat().parse(i2 < 4 ? TimeZoneFormat.Style.LOCALIZED_GMT_SHORT : TimeZoneFormat.Style.LOCALIZED_GMT, str, parsePosition, output);
                                            if (parse6 == null) {
                                                return ~i4;
                                            }
                                            calendar.setTimeZone(parse6);
                                            return parsePosition.getIndex();
                                        case 32:
                                            if (i2 == r9) {
                                                style2 = TimeZoneFormat.Style.ISO_BASIC_SHORT;
                                            } else if (i2 == 2) {
                                                style2 = TimeZoneFormat.Style.ISO_BASIC_FIXED;
                                            } else if (i2 == 3) {
                                                style2 = TimeZoneFormat.Style.ISO_EXTENDED_FIXED;
                                            } else if (i2 != 4) {
                                                style2 = TimeZoneFormat.Style.ISO_EXTENDED_FULL;
                                            } else {
                                                style2 = TimeZoneFormat.Style.ISO_BASIC_FULL;
                                            }
                                            TimeZone parse7 = tzFormat().parse(style2, str, parsePosition, output);
                                            if (parse7 == null) {
                                                return ~i4;
                                            }
                                            calendar.setTimeZone(parse7);
                                            return parsePosition.getIndex();
                                        case 33:
                                            if (i2 == r9) {
                                                style3 = TimeZoneFormat.Style.ISO_BASIC_LOCAL_SHORT;
                                            } else if (i2 == 2) {
                                                style3 = TimeZoneFormat.Style.ISO_BASIC_LOCAL_FIXED;
                                            } else if (i2 == 3) {
                                                style3 = TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FIXED;
                                            } else if (i2 != 4) {
                                                style3 = TimeZoneFormat.Style.ISO_EXTENDED_LOCAL_FULL;
                                            } else {
                                                style3 = TimeZoneFormat.Style.ISO_BASIC_LOCAL_FULL;
                                            }
                                            TimeZone parse8 = tzFormat().parse(style3, str, parsePosition, output);
                                            if (parse8 == null) {
                                                return ~i4;
                                            }
                                            calendar.setTimeZone(parse8);
                                            return parsePosition.getIndex();
                                        case 35:
                                            int subParse = subParse(str, i4, 'a', i2, z, z2, zArr, calendar, messageFormat, output, output2);
                                            if (subParse > 0) {
                                                return subParse;
                                            }
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 3) {
                                                i20 = matchDayPeriodString(str, i4, this.formatData.abbreviatedDayPeriods, 2, output2);
                                                if (i20 > 0) {
                                                    return i20;
                                                }
                                            } else {
                                                i20 = 0;
                                            }
                                            if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) && (i20 = matchDayPeriodString(str, i4, this.formatData.wideDayPeriods, 2, output2)) > 0) {
                                                return i20;
                                            }
                                            if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) && (i20 = matchDayPeriodString(str, i4, this.formatData.narrowDayPeriods, 2, output2)) > 0) {
                                            }
                                            return i20;
                                        case 36:
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 3) {
                                                i21 = matchDayPeriodString(str, i4, this.formatData.abbreviatedDayPeriods, this.formatData.abbreviatedDayPeriods.length, output2);
                                                if (i21 > 0) {
                                                    return i21;
                                                }
                                            } else {
                                                i21 = 0;
                                            }
                                            if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) && (i21 = matchDayPeriodString(str, i4, this.formatData.wideDayPeriods, this.formatData.wideDayPeriods.length, output2)) > 0) {
                                                return i21;
                                            }
                                            if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) && (i21 = matchDayPeriodString(str, i4, this.formatData.narrowDayPeriods, this.formatData.narrowDayPeriods.length, output2)) > 0) {
                                            }
                                            return i21;
                                        case 37:
                                            ArrayList arrayList = new ArrayList(3);
                                            arrayList.add(this.formatData.getTimeSeparatorString());
                                            if (!this.formatData.getTimeSeparatorString().equals(":")) {
                                                arrayList.add(":");
                                            }
                                            if (getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH) && !this.formatData.getTimeSeparatorString().equals(".")) {
                                                arrayList.add(".");
                                            }
                                            return matchString(str, i4, -1, (String[]) arrayList.toArray(new String[0]), calendar);
                                    }
                                    if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == 4) {
                                        i13 = matchString(str, i4, 7, this.formatData.weekdays, null, calendar);
                                        if (i13 > 0) {
                                            return i13;
                                        }
                                    } else {
                                        i13 = 0;
                                    }
                                    if ((!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == i10) && (i13 = matchString(str, i4, 7, this.formatData.shortWeekdays, null, calendar)) > 0) {
                                        return i13;
                                    }
                                    if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == i11) && this.formatData.shorterWeekdays != null && (i13 = matchString(str, i4, 7, this.formatData.shorterWeekdays, null, calendar)) > 0) {
                                        return i13;
                                    }
                                    if ((getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || i2 == i12) && this.formatData.narrowWeekdays != null && (i13 = matchString(str, i4, 7, this.formatData.narrowWeekdays, null, calendar)) > 0) {
                                    }
                                    return i13;
                                }
                                number = number4;
                                z4 = z6;
                            } else {
                                i24 = 22;
                            }
                        }
                        parsePosition.setIndex(i26);
                        calendar.set(i24, i23);
                    }
                    number3 = null;
                    z5 = false;
                    if (z5) {
                    }
                    if (number4 == null) {
                    }
                } else {
                    i4 = i26;
                    z4 = true;
                    i3 = i25;
                    number = null;
                }
                i5 = 0;
                r9 = z4;
                switch (indexFromChar) {
                }
                if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH)) {
                }
                i13 = matchString(str, i4, 7, this.formatData.weekdays, null, calendar);
                if (i13 > 0) {
                }
                if (!getBooleanAttribute(DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH)) {
                }
                return i13;
            }
            i26 += UTF16.getCharCount(charAt);
        }
        return ~i26;
    }

    private Number parseInt(String str, ParsePosition parsePosition, boolean z, NumberFormat numberFormat) {
        return parseInt(str, -1, parsePosition, z, numberFormat);
    }

    private Number parseInt(String str, int i, ParsePosition parsePosition, boolean z, NumberFormat numberFormat) {
        Number number;
        int index;
        int index2 = parsePosition.getIndex();
        if (z) {
            number = numberFormat.parse(str, parsePosition);
        } else if (numberFormat instanceof DecimalFormat) {
            DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
            String negativePrefix = decimalFormat.getNegativePrefix();
            decimalFormat.setNegativePrefix(SUPPRESS_NEGATIVE_PREFIX);
            number = numberFormat.parse(str, parsePosition);
            ((DecimalFormat) numberFormat).setNegativePrefix(negativePrefix);
        } else {
            boolean z2 = numberFormat instanceof DateNumberFormat;
            if (z2) {
                ((DateNumberFormat) numberFormat).setParsePositiveOnly(true);
            }
            number = numberFormat.parse(str, parsePosition);
            if (z2) {
                ((DateNumberFormat) numberFormat).setParsePositiveOnly(false);
            }
        }
        if (i <= 0 || (index = parsePosition.getIndex() - index2) <= i) {
            return number;
        }
        double doubleValue = number.doubleValue();
        for (int i2 = index - i; i2 > 0; i2--) {
            doubleValue /= 10.0d;
        }
        parsePosition.setIndex(index2 + i);
        return Integer.valueOf((int) doubleValue);
    }

    private static int countDigits(String str, int i, int i2) {
        int i3 = 0;
        while (i < i2) {
            int codePointAt = str.codePointAt(i);
            if (UCharacter.isDigit(codePointAt)) {
                i3++;
            }
            i += UCharacter.charCount(codePointAt);
        }
        return i3;
    }

    private String translatePattern(String str, String str2, String str3) {
        int indexOf;
        StringBuilder sb = new StringBuilder();
        boolean z = false;
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (z) {
                if (charAt == '\'') {
                    z = false;
                }
            } else if (charAt == '\'') {
                z = true;
            } else if (isSyntaxChar(charAt) && (indexOf = str2.indexOf(charAt)) != -1) {
                charAt = str3.charAt(indexOf);
            }
            sb.append(charAt);
        }
        if (!z) {
            return sb.toString();
        }
        throw new IllegalArgumentException("Unfinished quote in pattern");
    }

    public String toPattern() {
        return this.pattern;
    }

    public String toLocalizedPattern() {
        return translatePattern(this.pattern, "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB", this.formatData.localPatternChars);
    }

    public void applyPattern(String str) {
        ULocale uLocale;
        this.pattern = str;
        parsePattern();
        setLocale(null, null);
        this.patternItems = null;
        if (this.calendar != null && this.calendar.getType().equals("japanese") && (uLocale = this.locale) != null && uLocale.getLanguage().equals("ja")) {
            String str2 = this.override;
            if (str2 != null && str2.equals("y=jpanyear") && !this.hasHanYearChar) {
                this.numberFormatters = null;
                this.overrideMap = null;
                this.override = null;
            } else if (this.override == null && this.hasHanYearChar) {
                this.numberFormatters = new HashMap<>();
                this.overrideMap = new HashMap<>();
                this.overrideMap.put('y', "jpanyear");
                NumberFormat createInstance = NumberFormat.createInstance(new ULocale(this.locale.getBaseName() + "@numbers=jpanyear"), 0);
                createInstance.setGroupingUsed(false);
                this.useLocalZeroPaddingNumberFormat = false;
                this.numberFormatters.put("jpanyear", createInstance);
                this.override = "y=jpanyear";
            }
        }
    }

    public void applyLocalizedPattern(String str) {
        this.pattern = translatePattern(str, this.formatData.localPatternChars, "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB");
        setLocale(null, null);
    }

    public DateFormatSymbols getDateFormatSymbols() {
        return (DateFormatSymbols) this.formatData.clone();
    }

    public void setDateFormatSymbols(DateFormatSymbols dateFormatSymbols) {
        this.formatData = (DateFormatSymbols) dateFormatSymbols.clone();
    }

    /* access modifiers changed from: protected */
    public DateFormatSymbols getSymbols() {
        return this.formatData;
    }

    public TimeZoneFormat getTimeZoneFormat() {
        return tzFormat().freeze();
    }

    public void setTimeZoneFormat(TimeZoneFormat timeZoneFormat) {
        if (timeZoneFormat.isFrozen()) {
            this.tzFormat = timeZoneFormat;
        } else {
            this.tzFormat = timeZoneFormat.cloneAsThawed().freeze();
        }
    }

    @Override // ohos.global.icu.text.DateFormat, java.text.Format, java.lang.Object
    public Object clone() {
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) super.clone();
        simpleDateFormat.formatData = (DateFormatSymbols) this.formatData.clone();
        if (this.decimalBuf != null) {
            simpleDateFormat.decimalBuf = new char[10];
        }
        return simpleDateFormat;
    }

    @Override // ohos.global.icu.text.DateFormat, java.lang.Object
    public int hashCode() {
        return this.pattern.hashCode();
    }

    @Override // ohos.global.icu.text.DateFormat, java.lang.Object
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) obj;
        if (!this.pattern.equals(simpleDateFormat.pattern) || !this.formatData.equals(simpleDateFormat.formatData)) {
            return false;
        }
        return true;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        if (this.defaultCenturyStart == null) {
            initializeDefaultCenturyStart(this.defaultCenturyBase);
        }
        initializeTimeZoneFormat(false);
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeInt(getContext(DisplayContext.Type.CAPITALIZATION).value());
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        int readInt = this.serialVersionOnStream > 1 ? objectInputStream.readInt() : -1;
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
        if (readInt >= 0) {
            DisplayContext[] values = DisplayContext.values();
            int length = values.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                DisplayContext displayContext = values[i];
                if (displayContext.value() == readInt) {
                    setContext(displayContext);
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

    @Override // java.text.Format
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        Calendar calendar = this.calendar;
        if (obj instanceof Calendar) {
            calendar = (Calendar) obj;
        } else if (obj instanceof Date) {
            this.calendar.setTime((Date) obj);
        } else if (obj instanceof Number) {
            this.calendar.setTimeInMillis(((Number) obj).longValue());
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Date");
        }
        StringBuffer stringBuffer = new StringBuffer();
        FieldPosition fieldPosition = new FieldPosition(0);
        ArrayList arrayList = new ArrayList();
        format(calendar, getContext(DisplayContext.Type.CAPITALIZATION), stringBuffer, fieldPosition, arrayList);
        AttributedString attributedString = new AttributedString(stringBuffer.toString());
        for (int i = 0; i < arrayList.size(); i++) {
            FieldPosition fieldPosition2 = arrayList.get(i);
            Format.Field fieldAttribute = fieldPosition2.getFieldAttribute();
            attributedString.addAttribute(fieldAttribute, fieldAttribute, fieldPosition2.getBeginIndex(), fieldPosition2.getEndIndex());
        }
        return attributedString.getIterator();
    }

    /* access modifiers changed from: package-private */
    public ULocale getLocale() {
        return this.locale;
    }

    /* access modifiers changed from: package-private */
    public boolean isFieldUnitIgnored(int i) {
        return isFieldUnitIgnored(this.pattern, i);
    }

    static boolean isFieldUnitIgnored(String str, int i) {
        int i2 = CALENDAR_FIELD_TO_LEVEL[i];
        int i3 = 0;
        int i4 = 0;
        char c = 0;
        boolean z = false;
        while (i3 < str.length()) {
            char charAt = str.charAt(i3);
            if (charAt != c && i4 > 0) {
                if (i2 <= getLevelFromChar(c)) {
                    return false;
                }
                i4 = 0;
            }
            if (charAt == '\'') {
                int i5 = i3 + 1;
                if (i5 >= str.length() || str.charAt(i5) != '\'') {
                    z = !z;
                } else {
                    i3 = i5;
                }
            } else if (!z && isSyntaxChar(charAt)) {
                i4++;
                c = charAt;
            }
            i3++;
        }
        if (i4 <= 0 || i2 > getLevelFromChar(c)) {
            return true;
        }
        return false;
    }

    @Deprecated
    public final StringBuffer intervalFormatByAlgorithm(Calendar calendar, Calendar calendar2, StringBuffer stringBuffer, FieldPosition fieldPosition) throws IllegalArgumentException {
        int i;
        int i2;
        int i3;
        int i4;
        if (calendar.isEquivalentTo(calendar2)) {
            Object[] patternItems2 = getPatternItems();
            int i5 = 0;
            while (true) {
                try {
                    if (i5 >= patternItems2.length) {
                        i5 = -1;
                        break;
                    } else if (diffCalFieldValue(calendar, calendar2, patternItems2, i5)) {
                        break;
                    } else {
                        i5++;
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(e.toString());
                }
            }
            if (i5 == -1) {
                return format(calendar, stringBuffer, fieldPosition);
            }
            int length = patternItems2.length - 1;
            while (true) {
                if (length < i5) {
                    length = -1;
                    break;
                } else if (diffCalFieldValue(calendar, calendar2, patternItems2, length)) {
                    break;
                } else {
                    length--;
                }
            }
            if (i5 == 0 && length == patternItems2.length - 1) {
                format(calendar, stringBuffer, fieldPosition);
                stringBuffer.append(" – ");
                format(calendar2, stringBuffer, fieldPosition);
                return stringBuffer;
            }
            int i6 = 1000;
            for (int i7 = i5; i7 <= length; i7++) {
                if (!(patternItems2[i7] instanceof String)) {
                    char c = ((PatternItem) patternItems2[i7]).type;
                    int indexFromChar = getIndexFromChar(c);
                    if (indexFromChar == -1) {
                        throw new IllegalArgumentException("Illegal pattern character '" + c + "' in \"" + this.pattern + '\"');
                    } else if (indexFromChar < i6) {
                        i6 = indexFromChar;
                    }
                }
            }
            int i8 = 0;
            while (true) {
                if (i8 >= i5) {
                    i = i5;
                    break;
                }
                try {
                    if (lowerLevel(patternItems2, i8, i6)) {
                        i = i8;
                        break;
                    }
                    i8++;
                } catch (IllegalArgumentException e2) {
                    throw new IllegalArgumentException(e2.toString());
                }
            }
            int length2 = patternItems2.length - 1;
            while (true) {
                if (length2 <= length) {
                    i2 = length;
                    break;
                } else if (lowerLevel(patternItems2, length2, i6)) {
                    i2 = length2;
                    break;
                } else {
                    length2--;
                }
            }
            if (i == 0 && i2 == patternItems2.length - 1) {
                format(calendar, stringBuffer, fieldPosition);
                stringBuffer.append(" – ");
                format(calendar2, stringBuffer, fieldPosition);
                return stringBuffer;
            }
            fieldPosition.setBeginIndex(0);
            fieldPosition.setEndIndex(0);
            DisplayContext context = getContext(DisplayContext.Type.CAPITALIZATION);
            int i9 = 0;
            while (i9 <= i2) {
                if (patternItems2[i9] instanceof String) {
                    stringBuffer.append((String) patternItems2[i9]);
                    i4 = i9;
                    i3 = i2;
                } else {
                    PatternItem patternItem = (PatternItem) patternItems2[i9];
                    if (this.useFastFormat) {
                        i4 = i9;
                        i3 = i2;
                        subFormat(stringBuffer, patternItem.type, patternItem.length, stringBuffer.length(), i9, context, fieldPosition, calendar);
                    } else {
                        i4 = i9;
                        i3 = i2;
                        stringBuffer.append(subFormat(patternItem.type, patternItem.length, stringBuffer.length(), i4, context, fieldPosition, calendar));
                    }
                }
                i9 = i4 + 1;
                i2 = i3;
            }
            stringBuffer.append(" – ");
            for (int i10 = i; i10 < patternItems2.length; i10++) {
                if (patternItems2[i10] instanceof String) {
                    stringBuffer.append((String) patternItems2[i10]);
                } else {
                    PatternItem patternItem2 = (PatternItem) patternItems2[i10];
                    if (this.useFastFormat) {
                        subFormat(stringBuffer, patternItem2.type, patternItem2.length, stringBuffer.length(), i10, context, fieldPosition, calendar2);
                    } else {
                        stringBuffer.append(subFormat(patternItem2.type, patternItem2.length, stringBuffer.length(), i10, context, fieldPosition, calendar2));
                    }
                }
            }
            return stringBuffer;
        }
        throw new IllegalArgumentException("can not format on two different calendars");
    }

    private boolean diffCalFieldValue(Calendar calendar, Calendar calendar2, Object[] objArr, int i) throws IllegalArgumentException {
        if (objArr[i] instanceof String) {
            return false;
        }
        char c = ((PatternItem) objArr[i]).type;
        int indexFromChar = getIndexFromChar(c);
        if (indexFromChar != -1) {
            int i2 = PATTERN_INDEX_TO_CALENDAR_FIELD[indexFromChar];
            if (i2 < 0 || calendar.get(i2) == calendar2.get(i2)) {
                return false;
            }
            return true;
        }
        throw new IllegalArgumentException("Illegal pattern character '" + c + "' in \"" + this.pattern + '\"');
    }

    private boolean lowerLevel(Object[] objArr, int i, int i2) throws IllegalArgumentException {
        if (objArr[i] instanceof String) {
            return false;
        }
        char c = ((PatternItem) objArr[i]).type;
        int levelFromChar = getLevelFromChar(c);
        if (levelFromChar == -1) {
            throw new IllegalArgumentException("Illegal pattern character '" + c + "' in \"" + this.pattern + '\"');
        } else if (levelFromChar >= i2) {
            return true;
        } else {
            return false;
        }
    }

    public void setNumberFormat(String str, NumberFormat numberFormat) {
        numberFormat.setGroupingUsed(false);
        String str2 = "$" + UUID.randomUUID().toString();
        if (this.numberFormatters == null) {
            this.numberFormatters = new HashMap<>();
        }
        if (this.overrideMap == null) {
            this.overrideMap = new HashMap<>();
        }
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if ("GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB".indexOf(charAt) != -1) {
                this.overrideMap.put(Character.valueOf(charAt), str2);
                this.numberFormatters.put(str2, numberFormat);
            } else {
                throw new IllegalArgumentException("Illegal field character '" + charAt + "' in setNumberFormat.");
            }
        }
        this.useLocalZeroPaddingNumberFormat = false;
    }

    public NumberFormat getNumberFormat(char c) {
        Character valueOf = Character.valueOf(c);
        HashMap<Character, String> hashMap = this.overrideMap;
        if (hashMap == null || !hashMap.containsKey(valueOf)) {
            return this.numberFormat;
        }
        return this.numberFormatters.get(this.overrideMap.get(valueOf).toString());
    }

    private void initNumberFormatters(ULocale uLocale) {
        this.numberFormatters = new HashMap<>();
        this.overrideMap = new HashMap<>();
        processOverrideString(uLocale, this.override);
    }

    private void processOverrideString(ULocale uLocale, String str) {
        boolean z;
        int i;
        boolean z2;
        if (str != null && str.length() != 0) {
            boolean z3 = true;
            int i2 = 0;
            while (z3) {
                int indexOf = str.indexOf(";", i2);
                if (indexOf == -1) {
                    i = str.length();
                    z = false;
                } else {
                    z = z3;
                    i = indexOf;
                }
                String substring = str.substring(i2, i);
                int indexOf2 = substring.indexOf("=");
                if (indexOf2 == -1) {
                    z2 = true;
                } else {
                    String substring2 = substring.substring(indexOf2 + 1);
                    this.overrideMap.put(Character.valueOf(substring.charAt(0)), substring2);
                    substring = substring2;
                    z2 = false;
                }
                NumberFormat createInstance = NumberFormat.createInstance(new ULocale(uLocale.getBaseName() + "@numbers=" + substring), 0);
                createInstance.setGroupingUsed(false);
                if (z2) {
                    setNumberFormat(createInstance);
                } else {
                    this.useLocalZeroPaddingNumberFormat = false;
                }
                if (!z2 && !this.numberFormatters.containsKey(substring)) {
                    this.numberFormatters.put(substring, createInstance);
                }
                i2 = indexOf + 1;
                z3 = z;
            }
        }
    }

    private void parsePattern() {
        this.hasMinute = false;
        this.hasSecond = false;
        this.hasHanYearChar = false;
        boolean z = false;
        for (int i = 0; i < this.pattern.length(); i++) {
            char charAt = this.pattern.charAt(i);
            if (charAt == '\'') {
                z = !z;
            }
            if (charAt == 24180) {
                this.hasHanYearChar = true;
            }
            if (!z) {
                if (charAt == 'm') {
                    this.hasMinute = true;
                }
                if (charAt == 's') {
                    this.hasSecond = true;
                }
            }
        }
    }
}
