package ohos.global.icu.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import ohos.global.icu.impl.FormattedValueFieldPositionIteratorImpl;
import ohos.global.icu.impl.ICUCache;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SimpleCache;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.DateIntervalInfo;
import ohos.global.icu.text.UFormat;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.DateInterval;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.TimeZone;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class DateIntervalFormat extends UFormat {
    private static ICUCache<String, Map<String, DateIntervalInfo.PatternInfo>> LOCAL_PATTERN_CACHE = new SimpleCache();
    private static final long serialVersionUID = 1;
    private SimpleDateFormat fDateFormat;
    private String fDatePattern = null;
    private String fDateTimeFormat = null;
    private Calendar fFromCalendar;
    private DateIntervalInfo fInfo;
    private transient Map<String, DateIntervalInfo.PatternInfo> fIntervalPatterns = null;
    private String fSkeleton = null;
    private String fTimePattern = null;
    private Calendar fToCalendar;
    private boolean isDateIntervalInfoDefault;

    public static final class FormattedDateInterval implements FormattedValue {
        private final List<FieldPosition> attributes;
        private final String string;

        FormattedDateInterval(CharSequence charSequence, List<FieldPosition> list) {
            this.string = charSequence.toString();
            this.attributes = Collections.unmodifiableList(list);
        }

        @Override // ohos.global.icu.text.FormattedValue, java.lang.CharSequence, java.lang.Object
        public String toString() {
            return this.string;
        }

        @Override // java.lang.CharSequence
        public int length() {
            return this.string.length();
        }

        @Override // java.lang.CharSequence
        public char charAt(int i) {
            return this.string.charAt(i);
        }

        @Override // java.lang.CharSequence
        public CharSequence subSequence(int i, int i2) {
            return this.string.subSequence(i, i2);
        }

        @Override // ohos.global.icu.text.FormattedValue
        public <A extends Appendable> A appendTo(A a) {
            return (A) Utility.appendTo(this.string, a);
        }

        @Override // ohos.global.icu.text.FormattedValue
        public boolean nextPosition(ConstrainedFieldPosition constrainedFieldPosition) {
            return FormattedValueFieldPositionIteratorImpl.nextPosition(this.attributes, constrainedFieldPosition);
        }

        @Override // ohos.global.icu.text.FormattedValue
        public AttributedCharacterIterator toCharacterIterator() {
            return FormattedValueFieldPositionIteratorImpl.toCharacterIterator(this.string, this.attributes);
        }
    }

    public static final class SpanField extends UFormat.SpanField {
        public static final SpanField DATE_INTERVAL_SPAN = new SpanField("date-interval-span");
        private static final long serialVersionUID = -6330879259553618133L;

        private SpanField(String str) {
            super(str);
        }

        /* access modifiers changed from: protected */
        @Override // java.text.AttributedCharacterIterator.Attribute
        public Object readResolve() throws InvalidObjectException {
            if (getName().equals(DATE_INTERVAL_SPAN.getName())) {
                return DATE_INTERVAL_SPAN;
            }
            throw new InvalidObjectException("An invalid object.");
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BestMatchInfo {
        final int bestMatchDistanceInfo;
        final String bestMatchSkeleton;

        BestMatchInfo(String str, int i) {
            this.bestMatchSkeleton = str;
            this.bestMatchDistanceInfo = i;
        }
    }

    /* access modifiers changed from: private */
    public static final class SkeletonAndItsBestMatch {
        final String bestMatchSkeleton;
        final String skeleton;

        SkeletonAndItsBestMatch(String str, String str2) {
            this.skeleton = str;
            this.bestMatchSkeleton = str2;
        }
    }

    /* access modifiers changed from: private */
    public static final class FormatOutput {
        int firstIndex;

        private FormatOutput() {
            this.firstIndex = -1;
        }

        public void register(int i) {
            if (this.firstIndex == -1) {
                this.firstIndex = i;
            }
        }
    }

    private DateIntervalFormat() {
    }

    @Deprecated
    public DateIntervalFormat(String str, DateIntervalInfo dateIntervalInfo, SimpleDateFormat simpleDateFormat) {
        this.fDateFormat = simpleDateFormat;
        dateIntervalInfo.freeze();
        this.fSkeleton = str;
        this.fInfo = dateIntervalInfo;
        this.isDateIntervalInfoDefault = false;
        this.fFromCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        this.fToCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        initializePattern(null);
    }

    private DateIntervalFormat(String str, ULocale uLocale, SimpleDateFormat simpleDateFormat) {
        this.fDateFormat = simpleDateFormat;
        this.fSkeleton = str;
        this.fInfo = new DateIntervalInfo(uLocale).freeze();
        this.isDateIntervalInfoDefault = true;
        this.fFromCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        this.fToCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        initializePattern(LOCAL_PATTERN_CACHE);
    }

    public static final DateIntervalFormat getInstance(String str) {
        return getInstance(str, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public static final DateIntervalFormat getInstance(String str, Locale locale) {
        return getInstance(str, ULocale.forLocale(locale));
    }

    public static final DateIntervalFormat getInstance(String str, ULocale uLocale) {
        return new DateIntervalFormat(str, uLocale, new SimpleDateFormat(DateTimePatternGenerator.getInstance(uLocale).getBestPattern(str), uLocale));
    }

    public static final DateIntervalFormat getInstance(String str, DateIntervalInfo dateIntervalInfo) {
        return getInstance(str, ULocale.getDefault(ULocale.Category.FORMAT), dateIntervalInfo);
    }

    public static final DateIntervalFormat getInstance(String str, Locale locale, DateIntervalInfo dateIntervalInfo) {
        return getInstance(str, ULocale.forLocale(locale), dateIntervalInfo);
    }

    public static final DateIntervalFormat getInstance(String str, ULocale uLocale, DateIntervalInfo dateIntervalInfo) {
        return new DateIntervalFormat(str, (DateIntervalInfo) dateIntervalInfo.clone(), new SimpleDateFormat(DateTimePatternGenerator.getInstance(uLocale).getBestPattern(str), uLocale));
    }

    @Override // java.text.Format, java.lang.Object
    public synchronized Object clone() {
        DateIntervalFormat dateIntervalFormat;
        dateIntervalFormat = (DateIntervalFormat) super.clone();
        dateIntervalFormat.fDateFormat = (SimpleDateFormat) this.fDateFormat.clone();
        dateIntervalFormat.fInfo = (DateIntervalInfo) this.fInfo.clone();
        dateIntervalFormat.fFromCalendar = (Calendar) this.fFromCalendar.clone();
        dateIntervalFormat.fToCalendar = (Calendar) this.fToCalendar.clone();
        dateIntervalFormat.fDatePattern = this.fDatePattern;
        dateIntervalFormat.fTimePattern = this.fTimePattern;
        dateIntervalFormat.fDateTimeFormat = this.fDateTimeFormat;
        return dateIntervalFormat;
    }

    @Override // java.text.Format
    public final StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (obj instanceof DateInterval) {
            return format((DateInterval) obj, stringBuffer, fieldPosition);
        }
        throw new IllegalArgumentException("Cannot format given Object (" + obj.getClass().getName() + ") as a DateInterval");
    }

    public final StringBuffer format(DateInterval dateInterval, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        return formatIntervalImpl(dateInterval, stringBuffer, fieldPosition, null, null);
    }

    public FormattedDateInterval formatToValue(DateInterval dateInterval) {
        StringBuffer stringBuffer = new StringBuffer();
        FieldPosition fieldPosition = new FieldPosition(0);
        FormatOutput formatOutput = new FormatOutput();
        ArrayList arrayList = new ArrayList();
        formatIntervalImpl(dateInterval, stringBuffer, fieldPosition, formatOutput, arrayList);
        if (formatOutput.firstIndex != -1) {
            FormattedValueFieldPositionIteratorImpl.addOverlapSpans(arrayList, SpanField.DATE_INTERVAL_SPAN, formatOutput.firstIndex);
            FormattedValueFieldPositionIteratorImpl.sort(arrayList);
        }
        return new FormattedDateInterval(stringBuffer, arrayList);
    }

    private synchronized StringBuffer formatIntervalImpl(DateInterval dateInterval, StringBuffer stringBuffer, FieldPosition fieldPosition, FormatOutput formatOutput, List<FieldPosition> list) {
        this.fFromCalendar.setTimeInMillis(dateInterval.getFromDate());
        this.fToCalendar.setTimeInMillis(dateInterval.getToDate());
        return formatImpl(this.fFromCalendar, this.fToCalendar, stringBuffer, fieldPosition, formatOutput, list);
    }

    @Deprecated
    public String getPatterns(Calendar calendar, Calendar calendar2, Output<String> output) {
        char c = 0;
        if (calendar.get(0) == calendar2.get(0)) {
            if (calendar.get(1) != calendar2.get(1)) {
                c = 1;
            } else if (calendar.get(2) != calendar2.get(2)) {
                c = 2;
            } else if (calendar.get(5) != calendar2.get(5)) {
                c = 5;
            } else if (calendar.get(9) != calendar2.get(9)) {
                c = '\t';
            } else if (calendar.get(10) != calendar2.get(10)) {
                c = '\n';
            } else if (calendar.get(12) != calendar2.get(12)) {
                c = '\f';
            } else if (calendar.get(13) == calendar2.get(13)) {
                return null;
            } else {
                c = '\r';
            }
        }
        DateIntervalInfo.PatternInfo patternInfo = this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[c]);
        output.value = patternInfo.getSecondPart();
        return patternInfo.getFirstPart();
    }

    public final StringBuffer format(Calendar calendar, Calendar calendar2, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        return formatImpl(calendar, calendar2, stringBuffer, fieldPosition, null, null);
    }

    public FormattedDateInterval formatToValue(Calendar calendar, Calendar calendar2) {
        StringBuffer stringBuffer = new StringBuffer();
        FieldPosition fieldPosition = new FieldPosition(0);
        FormatOutput formatOutput = new FormatOutput();
        ArrayList arrayList = new ArrayList();
        formatImpl(calendar, calendar2, stringBuffer, fieldPosition, formatOutput, arrayList);
        if (formatOutput.firstIndex != -1) {
            FormattedValueFieldPositionIteratorImpl.addOverlapSpans(arrayList, SpanField.DATE_INTERVAL_SPAN, formatOutput.firstIndex);
            FormattedValueFieldPositionIteratorImpl.sort(arrayList);
        }
        return new FormattedDateInterval(stringBuffer, arrayList);
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00bd  */
    private synchronized StringBuffer formatImpl(Calendar calendar, Calendar calendar2, StringBuffer stringBuffer, FieldPosition fieldPosition, FormatOutput formatOutput, List<FieldPosition> list) {
        boolean z;
        DateIntervalInfo.PatternInfo patternInfo;
        Calendar calendar3 = calendar;
        Calendar calendar4 = calendar2;
        synchronized (this) {
            if (calendar.isEquivalentTo(calendar2)) {
                int i = 5;
                if (calendar3.get(0) != calendar4.get(0)) {
                    i = 0;
                } else if (calendar3.get(1) != calendar4.get(1)) {
                    i = 1;
                } else if (calendar3.get(2) != calendar4.get(2)) {
                    i = 2;
                } else if (calendar3.get(5) == calendar4.get(5)) {
                    if (calendar3.get(9) != calendar4.get(9)) {
                        i = 9;
                    } else if (calendar3.get(10) != calendar4.get(10)) {
                        i = 10;
                    } else if (calendar3.get(12) != calendar4.get(12)) {
                        i = 12;
                    } else if (calendar3.get(13) != calendar4.get(13)) {
                        i = 13;
                    } else {
                        return this.fDateFormat.format(calendar3, stringBuffer, fieldPosition, list);
                    }
                }
                if (!(i == 9 || i == 10 || i == 12)) {
                    if (i != 13) {
                        z = false;
                        patternInfo = this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i]);
                        if (patternInfo != null) {
                            if (this.fDateFormat.isFieldUnitIgnored(i)) {
                                return this.fDateFormat.format(calendar3, stringBuffer, fieldPosition, list);
                            }
                            return fallbackFormat(calendar, calendar2, z, stringBuffer, fieldPosition, formatOutput, list);
                        } else if (patternInfo.getFirstPart() == null) {
                            return fallbackFormat(calendar, calendar2, z, stringBuffer, fieldPosition, formatOutput, list, patternInfo.getSecondPart());
                        } else {
                            if (patternInfo.firstDateInPtnIsLaterDate()) {
                                if (formatOutput != null) {
                                    formatOutput.register(1);
                                }
                                calendar4 = calendar3;
                                calendar3 = calendar4;
                            } else if (formatOutput != null) {
                                formatOutput.register(0);
                            }
                            String pattern = this.fDateFormat.toPattern();
                            this.fDateFormat.applyPattern(patternInfo.getFirstPart());
                            this.fDateFormat.format(calendar3, stringBuffer, fieldPosition, list);
                            FieldPosition fieldPosition2 = fieldPosition.getEndIndex() > 0 ? new FieldPosition(0) : fieldPosition;
                            if (patternInfo.getSecondPart() != null) {
                                this.fDateFormat.applyPattern(patternInfo.getSecondPart());
                                this.fDateFormat.format(calendar4, stringBuffer, fieldPosition2, list);
                            }
                            this.fDateFormat.applyPattern(pattern);
                            return stringBuffer;
                        }
                    }
                }
                z = true;
                patternInfo = this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i]);
                if (patternInfo != null) {
                }
            } else {
                throw new IllegalArgumentException("can not format on two different calendars");
            }
        }
    }

    private final void fallbackFormatRange(Calendar calendar, Calendar calendar2, StringBuffer stringBuffer, StringBuilder sb, FieldPosition fieldPosition, FormatOutput formatOutput, List<FieldPosition> list) {
        String compileToStringMinMaxArguments = SimpleFormatterImpl.compileToStringMinMaxArguments(this.fInfo.getFallbackIntervalPattern(), sb, 2, 2);
        long j = 0;
        while (true) {
            j = SimpleFormatterImpl.Int64Iterator.step(compileToStringMinMaxArguments, j, stringBuffer);
            if (j != -1) {
                if (SimpleFormatterImpl.Int64Iterator.getArgIndex(j) == 0) {
                    if (formatOutput != null) {
                        formatOutput.register(0);
                    }
                    this.fDateFormat.format(calendar, stringBuffer, fieldPosition, list);
                } else {
                    if (formatOutput != null) {
                        formatOutput.register(1);
                    }
                    this.fDateFormat.format(calendar2, stringBuffer, fieldPosition, list);
                }
                if (fieldPosition.getEndIndex() > 0) {
                    fieldPosition = new FieldPosition(0);
                }
            } else {
                return;
            }
        }
    }

    private final StringBuffer fallbackFormat(Calendar calendar, Calendar calendar2, boolean z, StringBuffer stringBuffer, FieldPosition fieldPosition, FormatOutput formatOutput, List<FieldPosition> list) {
        StringBuilder sb = new StringBuilder();
        if ((!z || this.fDatePattern == null || this.fTimePattern == null) ? false : true) {
            String compileToStringMinMaxArguments = SimpleFormatterImpl.compileToStringMinMaxArguments(this.fDateTimeFormat, sb, 2, 2);
            String pattern = this.fDateFormat.toPattern();
            long j = 0;
            FieldPosition fieldPosition2 = fieldPosition;
            while (true) {
                long step = SimpleFormatterImpl.Int64Iterator.step(compileToStringMinMaxArguments, j, stringBuffer);
                if (step == -1) {
                    break;
                }
                if (SimpleFormatterImpl.Int64Iterator.getArgIndex(step) == 0) {
                    this.fDateFormat.applyPattern(this.fTimePattern);
                    fallbackFormatRange(calendar, calendar2, stringBuffer, sb, fieldPosition2, formatOutput, list);
                } else {
                    this.fDateFormat.applyPattern(this.fDatePattern);
                    this.fDateFormat.format(calendar, stringBuffer, fieldPosition2, list);
                }
                if (fieldPosition2.getEndIndex() > 0) {
                    fieldPosition2 = new FieldPosition(0);
                }
                j = step;
            }
            this.fDateFormat.applyPattern(pattern);
        } else {
            fallbackFormatRange(calendar, calendar2, stringBuffer, sb, fieldPosition, formatOutput, list);
        }
        return stringBuffer;
    }

    private final StringBuffer fallbackFormat(Calendar calendar, Calendar calendar2, boolean z, StringBuffer stringBuffer, FieldPosition fieldPosition, FormatOutput formatOutput, List<FieldPosition> list, String str) {
        String pattern = this.fDateFormat.toPattern();
        this.fDateFormat.applyPattern(str);
        fallbackFormat(calendar, calendar2, z, stringBuffer, fieldPosition, formatOutput, list);
        this.fDateFormat.applyPattern(pattern);
        return stringBuffer;
    }

    @Override // java.text.Format
    @Deprecated
    public Object parseObject(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException("parsing is not supported");
    }

    public DateIntervalInfo getDateIntervalInfo() {
        return (DateIntervalInfo) this.fInfo.clone();
    }

    public void setDateIntervalInfo(DateIntervalInfo dateIntervalInfo) {
        this.fInfo = (DateIntervalInfo) dateIntervalInfo.clone();
        this.isDateIntervalInfoDefault = false;
        this.fInfo.freeze();
        if (this.fDateFormat != null) {
            initializePattern(null);
        }
    }

    public TimeZone getTimeZone() {
        SimpleDateFormat simpleDateFormat = this.fDateFormat;
        if (simpleDateFormat != null) {
            return (TimeZone) simpleDateFormat.getTimeZone().clone();
        }
        return TimeZone.getDefault();
    }

    public void setTimeZone(TimeZone timeZone) {
        TimeZone timeZone2 = (TimeZone) timeZone.clone();
        SimpleDateFormat simpleDateFormat = this.fDateFormat;
        if (simpleDateFormat != null) {
            simpleDateFormat.setTimeZone(timeZone2);
        }
        Calendar calendar = this.fFromCalendar;
        if (calendar != null) {
            calendar.setTimeZone(timeZone2);
        }
        Calendar calendar2 = this.fToCalendar;
        if (calendar2 != null) {
            calendar2.setTimeZone(timeZone2);
        }
    }

    public synchronized DateFormat getDateFormat() {
        return (DateFormat) this.fDateFormat.clone();
    }

    private void initializePattern(ICUCache<String, Map<String, DateIntervalInfo.PatternInfo>> iCUCache) {
        String str;
        String str2;
        String pattern = this.fDateFormat.toPattern();
        ULocale locale = this.fDateFormat.getLocale();
        Map<String, DateIntervalInfo.PatternInfo> map = null;
        if (iCUCache != null) {
            if (this.fSkeleton != null) {
                str2 = locale.toString() + "+" + pattern + "+" + this.fSkeleton;
            } else {
                str2 = locale.toString() + "+" + pattern;
            }
            str = str2;
            map = iCUCache.get(str2);
        } else {
            str = null;
        }
        if (map == null) {
            map = Collections.unmodifiableMap(initializeIntervalPattern(pattern, locale));
            if (iCUCache != null) {
                iCUCache.put(str, map);
            }
        }
        this.fIntervalPatterns = map;
    }

    private Map<String, DateIntervalInfo.PatternInfo> initializeIntervalPattern(String str, ULocale uLocale) {
        DateTimePatternGenerator instance = DateTimePatternGenerator.getInstance(uLocale);
        if (this.fSkeleton == null) {
            this.fSkeleton = instance.getSkeleton(str);
        }
        String str2 = this.fSkeleton;
        HashMap hashMap = new HashMap();
        StringBuilder sb = new StringBuilder(str2.length());
        StringBuilder sb2 = new StringBuilder(str2.length());
        StringBuilder sb3 = new StringBuilder(str2.length());
        StringBuilder sb4 = new StringBuilder(str2.length());
        getDateTimeSkeleton(str2, sb, sb2, sb3, sb4);
        String sb5 = sb.toString();
        String sb6 = sb3.toString();
        String sb7 = sb2.toString();
        String sb8 = sb4.toString();
        if (!(sb3.length() == 0 || sb.length() == 0)) {
            this.fDateTimeFormat = getConcatenationPattern(uLocale);
        }
        if (!genSeparateDateTimePtn(sb7, sb8, hashMap, instance)) {
            if (sb3.length() != 0 && sb.length() == 0) {
                DateIntervalInfo.PatternInfo patternInfo = new DateIntervalInfo.PatternInfo(null, instance.getBestPattern(DateFormat.YEAR_NUM_MONTH_DAY + sb6), this.fInfo.getDefaultOrder());
                hashMap.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5], patternInfo);
                hashMap.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2], patternInfo);
                hashMap.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1], patternInfo);
            }
            return hashMap;
        }
        if (sb3.length() != 0) {
            if (sb.length() == 0) {
                DateIntervalInfo.PatternInfo patternInfo2 = new DateIntervalInfo.PatternInfo(null, instance.getBestPattern(DateFormat.YEAR_NUM_MONTH_DAY + sb6), this.fInfo.getDefaultOrder());
                hashMap.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5], patternInfo2);
                hashMap.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2], patternInfo2);
                hashMap.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1], patternInfo2);
            } else {
                if (!fieldExistsInSkeleton(5, sb5)) {
                    str2 = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5] + str2;
                    genFallbackPattern(5, str2, hashMap, instance);
                }
                if (!fieldExistsInSkeleton(2, sb5)) {
                    str2 = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2] + str2;
                    genFallbackPattern(2, str2, hashMap, instance);
                }
                if (!fieldExistsInSkeleton(1, sb5)) {
                    genFallbackPattern(1, DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1] + str2, hashMap, instance);
                }
                if (this.fDateTimeFormat == null) {
                    this.fDateTimeFormat = "{1} {0}";
                }
                String bestPattern = instance.getBestPattern(sb5);
                concatSingleDate2TimeInterval(this.fDateTimeFormat, bestPattern, 9, hashMap);
                concatSingleDate2TimeInterval(this.fDateTimeFormat, bestPattern, 10, hashMap);
                concatSingleDate2TimeInterval(this.fDateTimeFormat, bestPattern, 12, hashMap);
            }
        }
        return hashMap;
    }

    private String getConcatenationPattern(ULocale uLocale) {
        ICUResourceBundle iCUResourceBundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale).getWithFallback("calendar/gregorian/DateTimePatterns").get(8);
        if (iCUResourceBundle.getType() == 0) {
            return iCUResourceBundle.getString();
        }
        return iCUResourceBundle.getString(0);
    }

    private void genFallbackPattern(int i, String str, Map<String, DateIntervalInfo.PatternInfo> map, DateTimePatternGenerator dateTimePatternGenerator) {
        map.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i], new DateIntervalInfo.PatternInfo(null, dateTimePatternGenerator.getBestPattern(str), this.fInfo.getDefaultOrder()));
    }

    private static void getDateTimeSkeleton(String str, StringBuilder sb, StringBuilder sb2, StringBuilder sb3, StringBuilder sb4) {
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        for (int i10 = 0; i10 < str.length(); i10++) {
            char charAt = str.charAt(i10);
            switch (charAt) {
                case 'A':
                case 'K':
                case 'S':
                case 'V':
                case 'Z':
                case 'j':
                case 'k':
                case 's':
                    sb3.append(charAt);
                    sb4.append(charAt);
                    break;
                case 'D':
                case 'F':
                case 'G':
                case 'L':
                case 'Q':
                case 'U':
                case 'W':
                case 'Y':
                case 'c':
                case 'e':
                case 'g':
                case 'l':
                case 'q':
                case 'r':
                case 'u':
                case 'w':
                    sb2.append(charAt);
                    sb.append(charAt);
                    break;
                case 'E':
                    sb.append(charAt);
                    i2++;
                    break;
                case 'H':
                    sb3.append(charAt);
                    i3++;
                    break;
                case 'M':
                    sb.append(charAt);
                    i4++;
                    break;
                case 'a':
                    sb3.append(charAt);
                    break;
                case 'd':
                    sb.append(charAt);
                    i5++;
                    break;
                case 'h':
                    sb3.append(charAt);
                    i6++;
                    break;
                case 'm':
                    sb3.append(charAt);
                    i7++;
                    break;
                case 'v':
                    i8++;
                    sb3.append(charAt);
                    break;
                case 'y':
                    sb.append(charAt);
                    i++;
                    break;
                case 'z':
                    i9++;
                    sb3.append(charAt);
                    break;
            }
        }
        if (i != 0) {
            for (int i11 = 0; i11 < i; i11++) {
                sb2.append('y');
            }
        }
        if (i4 != 0) {
            if (i4 < 3) {
                sb2.append('M');
            } else {
                int i12 = 0;
                while (i12 < i4 && i12 < 5) {
                    sb2.append('M');
                    i12++;
                }
            }
        }
        if (i2 != 0) {
            if (i2 <= 3) {
                sb2.append('E');
            } else {
                int i13 = 0;
                while (i13 < i2 && i13 < 5) {
                    sb2.append('E');
                    i13++;
                }
            }
        }
        if (i5 != 0) {
            sb2.append('d');
        }
        if (i3 != 0) {
            sb4.append('H');
        } else if (i6 != 0) {
            sb4.append('h');
        }
        if (i7 != 0) {
            sb4.append('m');
        }
        if (i9 != 0) {
            sb4.append('z');
        }
        if (i8 != 0) {
            sb4.append('v');
        }
    }

    private boolean genSeparateDateTimePtn(String str, String str2, Map<String, DateIntervalInfo.PatternInfo> map, DateTimePatternGenerator dateTimePatternGenerator) {
        String str3 = str2.length() != 0 ? str2 : str;
        BestMatchInfo bestSkeleton = this.fInfo.getBestSkeleton(str3);
        String str4 = bestSkeleton.bestMatchSkeleton;
        int i = bestSkeleton.bestMatchDistanceInfo;
        if (str.length() != 0) {
            this.fDatePattern = dateTimePatternGenerator.getBestPattern(str);
        }
        if (str2.length() != 0) {
            this.fTimePattern = dateTimePatternGenerator.getBestPattern(str2);
        }
        if (i == -1) {
            return false;
        }
        if (str2.length() == 0) {
            genIntervalPattern(5, str3, str4, i, map);
            SkeletonAndItsBestMatch genIntervalPattern = genIntervalPattern(2, str3, str4, i, map);
            if (genIntervalPattern != null) {
                String str5 = genIntervalPattern.skeleton;
                str3 = genIntervalPattern.bestMatchSkeleton;
                str4 = str5;
            }
            genIntervalPattern(1, str3, str4, i, map);
            genIntervalPattern(0, str3, str4, i, map);
            return true;
        }
        genIntervalPattern(12, str3, str4, i, map);
        genIntervalPattern(10, str3, str4, i, map);
        genIntervalPattern(9, str3, str4, i, map);
        return true;
    }

    private SkeletonAndItsBestMatch genIntervalPattern(int i, String str, String str2, int i2, Map<String, DateIntervalInfo.PatternInfo> map) {
        DateIntervalInfo.PatternInfo intervalPattern = this.fInfo.getIntervalPattern(str2, i);
        SkeletonAndItsBestMatch skeletonAndItsBestMatch = null;
        if (intervalPattern == null) {
            if (SimpleDateFormat.isFieldUnitIgnored(str2, i)) {
                map.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i], new DateIntervalInfo.PatternInfo(this.fDateFormat.toPattern(), null, this.fInfo.getDefaultOrder()));
                return null;
            } else if (i == 9) {
                DateIntervalInfo.PatternInfo intervalPattern2 = this.fInfo.getIntervalPattern(str2, 10);
                if (intervalPattern2 != null) {
                    map.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i], intervalPattern2);
                }
                return null;
            } else {
                String str3 = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i];
                str2 = str3 + str2;
                str = str3 + str;
                intervalPattern = this.fInfo.getIntervalPattern(str2, i);
                if (intervalPattern == null && i2 == 0) {
                    BestMatchInfo bestSkeleton = this.fInfo.getBestSkeleton(str);
                    String str4 = bestSkeleton.bestMatchSkeleton;
                    i2 = bestSkeleton.bestMatchDistanceInfo;
                    if (!(str4.length() == 0 || i2 == -1)) {
                        intervalPattern = this.fInfo.getIntervalPattern(str4, i);
                        str2 = str4;
                    }
                }
                if (intervalPattern != null) {
                    skeletonAndItsBestMatch = new SkeletonAndItsBestMatch(str, str2);
                }
            }
        }
        if (intervalPattern != null) {
            map.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i], i2 != 0 ? new DateIntervalInfo.PatternInfo(adjustFieldWidth(str, str2, intervalPattern.getFirstPart(), i2), adjustFieldWidth(str, str2, intervalPattern.getSecondPart(), i2), intervalPattern.firstDateInPtnIsLaterDate()) : intervalPattern);
        }
        return skeletonAndItsBestMatch;
    }

    private static String adjustFieldWidth(String str, String str2, String str3, int i) {
        char c;
        if (str3 == null) {
            return null;
        }
        int[] iArr = new int[58];
        int[] iArr2 = new int[58];
        DateIntervalInfo.parseSkeleton(str, iArr);
        DateIntervalInfo.parseSkeleton(str2, iArr2);
        if (i == 2) {
            str3 = str3.replace('v', 'z');
        }
        StringBuilder sb = new StringBuilder(str3);
        int length = sb.length();
        int i2 = 0;
        int i3 = 0;
        char c2 = 0;
        boolean z = false;
        while (true) {
            c = 'M';
            if (i2 >= length) {
                break;
            }
            char charAt = sb.charAt(i2);
            if (charAt != c2 && i3 > 0) {
                if (c2 != 'L') {
                    c = c2;
                }
                int i4 = c - 'A';
                int i5 = iArr2[i4];
                int i6 = iArr[i4];
                if (i5 == i3 && i6 > i5) {
                    int i7 = i6 - i5;
                    for (int i8 = 0; i8 < i7; i8++) {
                        sb.insert(i2, c2);
                    }
                    i2 += i7;
                    length += i7;
                }
                i3 = 0;
            }
            if (charAt == '\'') {
                int i9 = i2 + 1;
                if (i9 >= sb.length() || sb.charAt(i9) != '\'') {
                    z = !z;
                } else {
                    i2 = i9;
                }
            } else if (!z && ((charAt >= 'a' && charAt <= 'z') || (charAt >= 'A' && charAt <= 'Z'))) {
                i3++;
                c2 = charAt;
            }
            i2++;
        }
        if (i3 > 0) {
            if (c2 != 'L') {
                c = c2;
            }
            int i10 = c - 'A';
            int i11 = iArr2[i10];
            int i12 = iArr[i10];
            if (i11 == i3 && i12 > i11) {
                int i13 = i12 - i11;
                for (int i14 = 0; i14 < i13; i14++) {
                    sb.append(c2);
                }
            }
        }
        return sb.toString();
    }

    private void concatSingleDate2TimeInterval(String str, String str2, int i, Map<String, DateIntervalInfo.PatternInfo> map) {
        DateIntervalInfo.PatternInfo patternInfo = map.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i]);
        if (patternInfo != null) {
            map.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i], DateIntervalInfo.genPatternInfo(SimpleFormatterImpl.formatRawPattern(str, 2, 2, patternInfo.getFirstPart() + patternInfo.getSecondPart(), str2), patternInfo.firstDateInPtnIsLaterDate()));
        }
    }

    private static boolean fieldExistsInSkeleton(int i, String str) {
        return str.indexOf(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[i]) != -1;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        initializePattern(this.isDateIntervalInfoDefault ? LOCAL_PATTERN_CACHE : null);
    }

    @Deprecated
    public Map<String, DateIntervalInfo.PatternInfo> getRawPatterns() {
        return this.fIntervalPatterns;
    }
}
