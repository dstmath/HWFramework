package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.text.DateIntervalInfo;
import android.icu.text.MessagePattern;
import android.icu.util.Calendar;
import android.icu.util.DateInterval;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    static final class BestMatchInfo {
        final int bestMatchDistanceInfo;
        final String bestMatchSkeleton;

        BestMatchInfo(String bestSkeleton, int difference) {
            this.bestMatchSkeleton = bestSkeleton;
            this.bestMatchDistanceInfo = difference;
        }
    }

    private static final class SkeletonAndItsBestMatch {
        final String bestMatchSkeleton;
        final String skeleton;

        SkeletonAndItsBestMatch(String skeleton2, String bestMatch) {
            this.skeleton = skeleton2;
            this.bestMatchSkeleton = bestMatch;
        }
    }

    private DateIntervalFormat() {
    }

    @Deprecated
    public DateIntervalFormat(String skeleton, DateIntervalInfo dtItvInfo, SimpleDateFormat simpleDateFormat) {
        this.fDateFormat = simpleDateFormat;
        dtItvInfo.freeze();
        this.fSkeleton = skeleton;
        this.fInfo = dtItvInfo;
        this.isDateIntervalInfoDefault = false;
        this.fFromCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        this.fToCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        initializePattern(null);
    }

    private DateIntervalFormat(String skeleton, ULocale locale, SimpleDateFormat simpleDateFormat) {
        this.fDateFormat = simpleDateFormat;
        this.fSkeleton = skeleton;
        this.fInfo = new DateIntervalInfo(locale).freeze();
        this.isDateIntervalInfoDefault = true;
        this.fFromCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        this.fToCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        initializePattern(LOCAL_PATTERN_CACHE);
    }

    public static final DateIntervalFormat getInstance(String skeleton) {
        return getInstance(skeleton, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public static final DateIntervalFormat getInstance(String skeleton, Locale locale) {
        return getInstance(skeleton, ULocale.forLocale(locale));
    }

    public static final DateIntervalFormat getInstance(String skeleton, ULocale locale) {
        return new DateIntervalFormat(skeleton, locale, new SimpleDateFormat(DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton), locale));
    }

    public static final DateIntervalFormat getInstance(String skeleton, DateIntervalInfo dtitvinf) {
        return getInstance(skeleton, ULocale.getDefault(ULocale.Category.FORMAT), dtitvinf);
    }

    public static final DateIntervalFormat getInstance(String skeleton, Locale locale, DateIntervalInfo dtitvinf) {
        return getInstance(skeleton, ULocale.forLocale(locale), dtitvinf);
    }

    public static final DateIntervalFormat getInstance(String skeleton, ULocale locale, DateIntervalInfo dtitvinf) {
        return new DateIntervalFormat(skeleton, (DateIntervalInfo) dtitvinf.clone(), new SimpleDateFormat(DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton), locale));
    }

    public synchronized Object clone() {
        DateIntervalFormat other;
        other = (DateIntervalFormat) super.clone();
        other.fDateFormat = (SimpleDateFormat) this.fDateFormat.clone();
        other.fInfo = (DateIntervalInfo) this.fInfo.clone();
        other.fFromCalendar = (Calendar) this.fFromCalendar.clone();
        other.fToCalendar = (Calendar) this.fToCalendar.clone();
        other.fDatePattern = this.fDatePattern;
        other.fTimePattern = this.fTimePattern;
        other.fDateTimeFormat = this.fDateTimeFormat;
        return other;
    }

    public final StringBuffer format(Object obj, StringBuffer appendTo, FieldPosition fieldPosition) {
        if (obj instanceof DateInterval) {
            return format((DateInterval) obj, appendTo, fieldPosition);
        }
        throw new IllegalArgumentException("Cannot format given Object (" + obj.getClass().getName() + ") as a DateInterval");
    }

    public final synchronized StringBuffer format(DateInterval dtInterval, StringBuffer appendTo, FieldPosition fieldPosition) {
        this.fFromCalendar.setTimeInMillis(dtInterval.getFromDate());
        this.fToCalendar.setTimeInMillis(dtInterval.getToDate());
        return format(this.fFromCalendar, this.fToCalendar, appendTo, fieldPosition);
    }

    @Deprecated
    public String getPatterns(Calendar fromCalendar, Calendar toCalendar, Output<String> part2) {
        int field;
        if (fromCalendar.get(0) != toCalendar.get(0)) {
            field = 0;
        } else if (fromCalendar.get(1) != toCalendar.get(1)) {
            field = 1;
        } else if (fromCalendar.get(2) != toCalendar.get(2)) {
            field = 2;
        } else if (fromCalendar.get(5) != toCalendar.get(5)) {
            field = 5;
        } else if (fromCalendar.get(9) != toCalendar.get(9)) {
            field = 9;
        } else if (fromCalendar.get(10) != toCalendar.get(10)) {
            field = 10;
        } else if (fromCalendar.get(12) != toCalendar.get(12)) {
            field = 12;
        } else if (fromCalendar.get(13) == toCalendar.get(13)) {
            return null;
        } else {
            field = 13;
        }
        DateIntervalInfo.PatternInfo intervalPattern = this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
        part2.value = intervalPattern.getSecondPart();
        return intervalPattern.getFirstPart();
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00b9 A[SYNTHETIC, Splitter:B:49:0x00b9] */
    public final synchronized StringBuffer format(Calendar fromCalendar, Calendar toCalendar, StringBuffer appendTo, FieldPosition pos) {
        int field;
        boolean fromToOnSameDay;
        DateIntervalInfo.PatternInfo intervalPattern;
        Calendar secondCal;
        Calendar firstCal;
        Calendar calendar = fromCalendar;
        Calendar calendar2 = toCalendar;
        StringBuffer stringBuffer = appendTo;
        FieldPosition fieldPosition = pos;
        synchronized (this) {
            if (fromCalendar.isEquivalentTo(toCalendar)) {
                if (calendar.get(0) != calendar2.get(0)) {
                    field = 0;
                } else if (calendar.get(1) != calendar2.get(1)) {
                    field = 1;
                } else if (calendar.get(2) != calendar2.get(2)) {
                    field = 2;
                } else if (calendar.get(5) != calendar2.get(5)) {
                    field = 5;
                } else if (calendar.get(9) != calendar2.get(9)) {
                    field = 9;
                } else if (calendar.get(10) != calendar2.get(10)) {
                    field = 10;
                } else if (calendar.get(12) != calendar2.get(12)) {
                    field = 12;
                } else if (calendar.get(13) != calendar2.get(13)) {
                    field = 13;
                } else {
                    StringBuffer format = this.fDateFormat.format(calendar, stringBuffer, fieldPosition);
                    return format;
                }
                int field2 = field;
                if (!(field2 == 9 || field2 == 10 || field2 == 12)) {
                    if (field2 != 13) {
                        fromToOnSameDay = false;
                        intervalPattern = this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field2]);
                        if (intervalPattern != null) {
                            if (this.fDateFormat.isFieldUnitIgnored(field2)) {
                                StringBuffer format2 = this.fDateFormat.format(calendar, stringBuffer, fieldPosition);
                                return format2;
                            }
                            StringBuffer fallbackFormat = fallbackFormat(calendar, calendar2, fromToOnSameDay, stringBuffer, fieldPosition);
                            return fallbackFormat;
                        } else if (intervalPattern.getFirstPart() == null) {
                            DateIntervalInfo.PatternInfo patternInfo = intervalPattern;
                            int i = field2;
                            StringBuffer fallbackFormat2 = fallbackFormat(calendar, calendar2, fromToOnSameDay, stringBuffer, fieldPosition, intervalPattern.getSecondPart());
                            return fallbackFormat2;
                        } else {
                            DateIntervalInfo.PatternInfo intervalPattern2 = intervalPattern;
                            int i2 = field2;
                            if (intervalPattern2.firstDateInPtnIsLaterDate()) {
                                firstCal = calendar2;
                                secondCal = calendar;
                            } else {
                                firstCal = calendar;
                                secondCal = calendar2;
                            }
                            String originalPattern = this.fDateFormat.toPattern();
                            this.fDateFormat.applyPattern(intervalPattern2.getFirstPart());
                            this.fDateFormat.format(firstCal, stringBuffer, fieldPosition);
                            if (intervalPattern2.getSecondPart() != null) {
                                this.fDateFormat.applyPattern(intervalPattern2.getSecondPart());
                                FieldPosition otherPos = new FieldPosition(pos.getField());
                                this.fDateFormat.format(secondCal, stringBuffer, otherPos);
                                if (pos.getEndIndex() == 0 && otherPos.getEndIndex() > 0) {
                                    fieldPosition.setBeginIndex(otherPos.getBeginIndex());
                                    fieldPosition.setEndIndex(otherPos.getEndIndex());
                                }
                            }
                            this.fDateFormat.applyPattern(originalPattern);
                            return stringBuffer;
                        }
                    }
                }
                fromToOnSameDay = true;
                intervalPattern = this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field2]);
                if (intervalPattern != null) {
                }
            } else {
                throw new IllegalArgumentException("can not format on two different calendars");
            }
        }
    }

    private void adjustPosition(String combiningPattern, String pat0, FieldPosition pos0, String pat1, FieldPosition pos1, FieldPosition posResult) {
        int index0 = combiningPattern.indexOf("{0}");
        int index1 = combiningPattern.indexOf("{1}");
        if (index0 >= 0 && index1 >= 0) {
            if (index0 < index1) {
                if (pos0.getEndIndex() > 0) {
                    posResult.setBeginIndex(pos0.getBeginIndex() + index0);
                    posResult.setEndIndex(pos0.getEndIndex() + index0);
                } else if (pos1.getEndIndex() > 0) {
                    int index12 = index1 + (pat0.length() - 3);
                    posResult.setBeginIndex(pos1.getBeginIndex() + index12);
                    posResult.setEndIndex(pos1.getEndIndex() + index12);
                }
            } else if (pos1.getEndIndex() > 0) {
                posResult.setBeginIndex(pos1.getBeginIndex() + index1);
                posResult.setEndIndex(pos1.getEndIndex() + index1);
            } else if (pos0.getEndIndex() > 0) {
                int index02 = index0 + (pat1.length() - 3);
                posResult.setBeginIndex(pos0.getBeginIndex() + index02);
                posResult.setEndIndex(pos0.getEndIndex() + index02);
            }
        }
    }

    private final StringBuffer fallbackFormat(Calendar fromCalendar, Calendar toCalendar, boolean fromToOnSameDay, StringBuffer appendTo, FieldPosition pos) {
        String fallbackRange;
        Calendar calendar = fromCalendar;
        StringBuffer stringBuffer = appendTo;
        String fullPattern = null;
        boolean formatDatePlusTimeRange = (!fromToOnSameDay || this.fDatePattern == null || this.fTimePattern == null) ? false : true;
        if (formatDatePlusTimeRange) {
            fullPattern = this.fDateFormat.toPattern();
            this.fDateFormat.applyPattern(this.fTimePattern);
        }
        String fullPattern2 = fullPattern;
        FieldPosition otherPos = new FieldPosition(pos.getField());
        FieldPosition fieldPosition = pos;
        StringBuffer earlierDate = this.fDateFormat.format(calendar, new StringBuffer(64), fieldPosition);
        StringBuffer laterDate = this.fDateFormat.format(toCalendar, new StringBuffer(64), otherPos);
        String fallbackPattern = this.fInfo.getFallbackIntervalPattern();
        adjustPosition(fallbackPattern, earlierDate.toString(), fieldPosition, laterDate.toString(), otherPos, pos);
        String fallbackPattern2 = fallbackPattern;
        String fallbackRange2 = SimpleFormatterImpl.formatRawPattern(fallbackPattern2, 2, 2, earlierDate, laterDate);
        if (formatDatePlusTimeRange) {
            this.fDateFormat.applyPattern(this.fDatePattern);
            StringBuffer datePortion = new StringBuffer(64);
            otherPos.setBeginIndex(0);
            otherPos.setEndIndex(0);
            StringBuffer datePortion2 = this.fDateFormat.format(calendar, datePortion, otherPos);
            String str = fallbackPattern2;
            adjustPosition(this.fDateTimeFormat, fallbackRange2, pos, datePortion2.toString(), otherPos, pos);
            MessageFormat msgFmt = new MessageFormat("");
            msgFmt.applyPattern(this.fDateTimeFormat, MessagePattern.ApostropheMode.DOUBLE_REQUIRED);
            fallbackRange = msgFmt.format(new Object[]{fallbackRange2, datePortion2}, new StringBuffer(128), new FieldPosition(0)).toString();
        } else {
            fallbackRange = fallbackRange2;
        }
        stringBuffer.append(fallbackRange);
        if (formatDatePlusTimeRange) {
            this.fDateFormat.applyPattern(fullPattern2);
        }
        return stringBuffer;
    }

    private final StringBuffer fallbackFormat(Calendar fromCalendar, Calendar toCalendar, boolean fromToOnSameDay, StringBuffer appendTo, FieldPosition pos, String fullPattern) {
        String originalPattern = this.fDateFormat.toPattern();
        this.fDateFormat.applyPattern(fullPattern);
        fallbackFormat(fromCalendar, toCalendar, fromToOnSameDay, appendTo, pos);
        this.fDateFormat.applyPattern(originalPattern);
        return appendTo;
    }

    @Deprecated
    public Object parseObject(String source, ParsePosition parse_pos) {
        throw new UnsupportedOperationException("parsing is not supported");
    }

    public DateIntervalInfo getDateIntervalInfo() {
        return (DateIntervalInfo) this.fInfo.clone();
    }

    public void setDateIntervalInfo(DateIntervalInfo newItvPattern) {
        this.fInfo = (DateIntervalInfo) newItvPattern.clone();
        this.isDateIntervalInfoDefault = false;
        this.fInfo.freeze();
        if (this.fDateFormat != null) {
            initializePattern(null);
        }
    }

    public TimeZone getTimeZone() {
        if (this.fDateFormat != null) {
            return (TimeZone) this.fDateFormat.getTimeZone().clone();
        }
        return TimeZone.getDefault();
    }

    public void setTimeZone(TimeZone zone) {
        TimeZone zoneToSet = (TimeZone) zone.clone();
        if (this.fDateFormat != null) {
            this.fDateFormat.setTimeZone(zoneToSet);
        }
        if (this.fFromCalendar != null) {
            this.fFromCalendar.setTimeZone(zoneToSet);
        }
        if (this.fToCalendar != null) {
            this.fToCalendar.setTimeZone(zoneToSet);
        }
    }

    public synchronized DateFormat getDateFormat() {
        return (DateFormat) this.fDateFormat.clone();
    }

    private void initializePattern(ICUCache<String, Map<String, DateIntervalInfo.PatternInfo>> cache) {
        String fullPattern = this.fDateFormat.toPattern();
        ULocale locale = this.fDateFormat.getLocale();
        String key = null;
        Map<String, DateIntervalInfo.PatternInfo> patterns = null;
        if (cache != null) {
            if (this.fSkeleton != null) {
                key = locale.toString() + "+" + fullPattern + "+" + this.fSkeleton;
            } else {
                key = locale.toString() + "+" + fullPattern;
            }
            patterns = cache.get(key);
        }
        if (patterns == null) {
            patterns = Collections.unmodifiableMap(initializeIntervalPattern(fullPattern, locale));
            if (cache != null) {
                cache.put(key, patterns);
            }
        }
        this.fIntervalPatterns = patterns;
    }

    private Map<String, DateIntervalInfo.PatternInfo> initializeIntervalPattern(String fullPattern, ULocale locale) {
        DateTimePatternGenerator dtpng = DateTimePatternGenerator.getInstance(locale);
        if (this.fSkeleton == null) {
            this.fSkeleton = dtpng.getSkeleton(fullPattern);
        } else {
            String str = fullPattern;
        }
        String skeleton = this.fSkeleton;
        HashMap<String, DateIntervalInfo.PatternInfo> intervalPatterns = new HashMap<>();
        StringBuilder date = new StringBuilder(skeleton.length());
        StringBuilder normalizedDate = new StringBuilder(skeleton.length());
        StringBuilder time = new StringBuilder(skeleton.length());
        StringBuilder normalizedTime = new StringBuilder(skeleton.length());
        getDateTimeSkeleton(skeleton, date, normalizedDate, time, normalizedTime);
        String dateSkeleton = date.toString();
        String timeSkeleton = time.toString();
        String normalizedDateSkeleton = normalizedDate.toString();
        String normalizedTimeSkeleton = normalizedTime.toString();
        if (time.length() == 0 || date.length() == 0) {
            ULocale uLocale = locale;
        } else {
            this.fDateTimeFormat = getConcatenationPattern(locale);
        }
        if (!genSeparateDateTimePtn(normalizedDateSkeleton, normalizedTimeSkeleton, intervalPatterns, dtpng)) {
            if (time.length() == 0 || date.length() != 0) {
                StringBuilder sb = normalizedTime;
            } else {
                StringBuilder sb2 = normalizedDate;
                StringBuilder sb3 = normalizedTime;
                DateIntervalInfo.PatternInfo ptn = new DateIntervalInfo.PatternInfo(null, dtpng.getBestPattern(DateFormat.YEAR_NUM_MONTH_DAY + timeSkeleton), this.fInfo.getDefaultOrder());
                intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5], ptn);
                intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2], ptn);
                intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1], ptn);
            }
            return intervalPatterns;
        }
        StringBuilder sb4 = normalizedTime;
        if (time.length() != 0) {
            if (date.length() == 0) {
                DateIntervalInfo.PatternInfo ptn2 = new DateIntervalInfo.PatternInfo(null, dtpng.getBestPattern(DateFormat.YEAR_NUM_MONTH_DAY + timeSkeleton), this.fInfo.getDefaultOrder());
                intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5], ptn2);
                intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2], ptn2);
                intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1], ptn2);
            } else {
                if (!fieldExistsInSkeleton(5, dateSkeleton)) {
                    skeleton = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5] + skeleton;
                    genFallbackPattern(5, skeleton, intervalPatterns, dtpng);
                }
                if (!fieldExistsInSkeleton(2, dateSkeleton)) {
                    skeleton = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2] + skeleton;
                    genFallbackPattern(2, skeleton, intervalPatterns, dtpng);
                }
                if (!fieldExistsInSkeleton(1, dateSkeleton)) {
                    genFallbackPattern(1, DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1] + skeleton, intervalPatterns, dtpng);
                }
                if (this.fDateTimeFormat == null) {
                    this.fDateTimeFormat = "{1} {0}";
                }
                String datePattern = dtpng.getBestPattern(dateSkeleton);
                concatSingleDate2TimeInterval(this.fDateTimeFormat, datePattern, 9, intervalPatterns);
                concatSingleDate2TimeInterval(this.fDateTimeFormat, datePattern, 10, intervalPatterns);
                concatSingleDate2TimeInterval(this.fDateTimeFormat, datePattern, 12, intervalPatterns);
            }
        }
        return intervalPatterns;
    }

    private String getConcatenationPattern(ULocale locale) {
        ICUResourceBundle concatenationPatternRb = (ICUResourceBundle) ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale)).getWithFallback("calendar/gregorian/DateTimePatterns").get(8);
        if (concatenationPatternRb.getType() == 0) {
            return concatenationPatternRb.getString();
        }
        return concatenationPatternRb.getString(0);
    }

    private void genFallbackPattern(int field, String skeleton, Map<String, DateIntervalInfo.PatternInfo> intervalPatterns, DateTimePatternGenerator dtpng) {
        intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], new DateIntervalInfo.PatternInfo(null, dtpng.getBestPattern(skeleton), this.fInfo.getDefaultOrder()));
    }

    private static void getDateTimeSkeleton(String skeleton, StringBuilder dateSkeleton, StringBuilder normalizedDateSkeleton, StringBuilder timeSkeleton, StringBuilder normalizedTimeSkeleton) {
        StringBuilder sb = dateSkeleton;
        StringBuilder sb2 = normalizedDateSkeleton;
        StringBuilder sb3 = timeSkeleton;
        StringBuilder sb4 = normalizedTimeSkeleton;
        int ECount = 0;
        int dCount = 0;
        int MCount = 0;
        int yCount = 0;
        int hCount = 0;
        int HCount = 0;
        int mCount = 0;
        int vCount = 0;
        int zCount = 0;
        for (int i = 0; i < skeleton.length(); i++) {
            char ch = skeleton.charAt(i);
            switch (ch) {
                case 'A':
                case 'K':
                case 'S':
                case 'V':
                case 'Z':
                case 'j':
                case 'k':
                case 's':
                    sb3.append(ch);
                    sb4.append(ch);
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
                    sb2.append(ch);
                    sb.append(ch);
                    break;
                case 'E':
                    sb.append(ch);
                    ECount++;
                    break;
                case 'H':
                    sb3.append(ch);
                    HCount++;
                    break;
                case 'M':
                    sb.append(ch);
                    MCount++;
                    break;
                case 'a':
                    sb3.append(ch);
                    break;
                case 'd':
                    sb.append(ch);
                    dCount++;
                    break;
                case 'h':
                    sb3.append(ch);
                    hCount++;
                    break;
                case 'm':
                    sb3.append(ch);
                    mCount++;
                    break;
                case 'v':
                    vCount++;
                    sb3.append(ch);
                    break;
                case 'y':
                    sb.append(ch);
                    yCount++;
                    break;
                case 'z':
                    zCount++;
                    sb3.append(ch);
                    break;
            }
        }
        String str = skeleton;
        if (yCount != 0) {
            for (int i2 = 0; i2 < yCount; i2++) {
                sb2.append('y');
            }
        }
        if (MCount != 0) {
            if (MCount < 3) {
                sb2.append('M');
            } else {
                int i3 = 0;
                while (i3 < MCount && i3 < 5) {
                    sb2.append('M');
                    i3++;
                }
            }
        }
        if (ECount != 0) {
            if (ECount <= 3) {
                sb2.append('E');
            } else {
                int i4 = 0;
                while (i4 < ECount && i4 < 5) {
                    sb2.append('E');
                    i4++;
                }
            }
        }
        if (dCount != 0) {
            sb2.append('d');
        }
        if (HCount != 0) {
            sb4.append('H');
        } else if (hCount != 0) {
            sb4.append('h');
        }
        if (mCount != 0) {
            sb4.append('m');
        }
        if (zCount != 0) {
            sb4.append('z');
        }
        if (vCount != 0) {
            sb4.append('v');
        }
    }

    private boolean genSeparateDateTimePtn(String dateSkeleton, String timeSkeleton, Map<String, DateIntervalInfo.PatternInfo> intervalPatterns, DateTimePatternGenerator dtpng) {
        String skeleton;
        if (timeSkeleton.length() != 0) {
            skeleton = timeSkeleton;
        } else {
            skeleton = dateSkeleton;
        }
        BestMatchInfo retValue = this.fInfo.getBestSkeleton(skeleton);
        String bestSkeleton = retValue.bestMatchSkeleton;
        int differenceInfo = retValue.bestMatchDistanceInfo;
        if (dateSkeleton.length() != 0) {
            this.fDatePattern = dtpng.getBestPattern(dateSkeleton);
        }
        if (timeSkeleton.length() != 0) {
            this.fTimePattern = dtpng.getBestPattern(timeSkeleton);
        }
        if (differenceInfo == -1) {
            return false;
        }
        if (timeSkeleton.length() == 0) {
            String str = skeleton;
            String str2 = bestSkeleton;
            int i = differenceInfo;
            Map<String, DateIntervalInfo.PatternInfo> map = intervalPatterns;
            genIntervalPattern(5, str, str2, i, map);
            SkeletonAndItsBestMatch skeletons = genIntervalPattern(2, str, str2, i, map);
            if (skeletons != null) {
                bestSkeleton = skeletons.skeleton;
                skeleton = skeletons.bestMatchSkeleton;
            }
            genIntervalPattern(1, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
        } else {
            String str3 = skeleton;
            String str4 = bestSkeleton;
            int i2 = differenceInfo;
            Map<String, DateIntervalInfo.PatternInfo> map2 = intervalPatterns;
            genIntervalPattern(12, str3, str4, i2, map2);
            genIntervalPattern(10, str3, str4, i2, map2);
            genIntervalPattern(9, str3, str4, i2, map2);
        }
        return true;
    }

    private SkeletonAndItsBestMatch genIntervalPattern(int field, String skeleton, String bestSkeleton, int differenceInfo, Map<String, DateIntervalInfo.PatternInfo> intervalPatterns) {
        SkeletonAndItsBestMatch retValue = null;
        DateIntervalInfo.PatternInfo pattern = this.fInfo.getIntervalPattern(bestSkeleton, field);
        if (pattern == null) {
            if (SimpleDateFormat.isFieldUnitIgnored(bestSkeleton, field)) {
                intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], new DateIntervalInfo.PatternInfo(this.fDateFormat.toPattern(), null, this.fInfo.getDefaultOrder()));
                return null;
            } else if (field == 9) {
                DateIntervalInfo.PatternInfo pattern2 = this.fInfo.getIntervalPattern(bestSkeleton, 10);
                if (pattern2 != null) {
                    intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], pattern2);
                }
                return null;
            } else {
                String fieldLetter = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field];
                bestSkeleton = fieldLetter + bestSkeleton;
                skeleton = fieldLetter + skeleton;
                pattern = this.fInfo.getIntervalPattern(bestSkeleton, field);
                if (pattern == null && differenceInfo == 0) {
                    BestMatchInfo tmpRetValue = this.fInfo.getBestSkeleton(skeleton);
                    String tmpBestSkeleton = tmpRetValue.bestMatchSkeleton;
                    differenceInfo = tmpRetValue.bestMatchDistanceInfo;
                    if (!(tmpBestSkeleton.length() == 0 || differenceInfo == -1)) {
                        pattern = this.fInfo.getIntervalPattern(tmpBestSkeleton, field);
                        bestSkeleton = tmpBestSkeleton;
                    }
                }
                if (pattern != null) {
                    retValue = new SkeletonAndItsBestMatch(skeleton, bestSkeleton);
                }
            }
        }
        if (pattern != null) {
            if (differenceInfo != 0) {
                pattern = new DateIntervalInfo.PatternInfo(adjustFieldWidth(skeleton, bestSkeleton, pattern.getFirstPart(), differenceInfo), adjustFieldWidth(skeleton, bestSkeleton, pattern.getSecondPart(), differenceInfo), pattern.firstDateInPtnIsLaterDate());
            }
            intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], pattern);
        }
        return retValue;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x009b, code lost:
        if (r13 > 'z') goto L_0x00a0;
     */
    private static String adjustFieldWidth(String inputSkeleton, String bestMatchSkeleton, String bestMatchIntervalPattern, int differenceInfo) {
        String bestMatchIntervalPattern2;
        char c;
        char skeletonChar;
        String bestMatchIntervalPattern3 = bestMatchIntervalPattern;
        if (bestMatchIntervalPattern3 == null) {
            return null;
        }
        int[] inputSkeletonFieldWidth = new int[58];
        int[] bestMatchSkeletonFieldWidth = new int[58];
        DateIntervalInfo.parseSkeleton(inputSkeleton, inputSkeletonFieldWidth);
        DateIntervalInfo.parseSkeleton(bestMatchSkeleton, bestMatchSkeletonFieldWidth);
        if (differenceInfo == 2) {
            bestMatchIntervalPattern3 = bestMatchIntervalPattern3.replace('v', 'z');
        }
        StringBuilder adjustedPtn = new StringBuilder(bestMatchIntervalPattern3);
        char prevCh = 0;
        int count = 0;
        int adjustedPtnLength = adjustedPtn.length();
        boolean inQuote = false;
        int i = 0;
        while (i < adjustedPtnLength) {
            char ch = adjustedPtn.charAt(i);
            if (ch == prevCh || count <= 0) {
                bestMatchIntervalPattern2 = bestMatchIntervalPattern3;
            } else {
                char skeletonChar2 = prevCh;
                if (skeletonChar2 == 'L') {
                    skeletonChar = 'M';
                } else {
                    skeletonChar = skeletonChar2;
                }
                int fieldCount = bestMatchSkeletonFieldWidth[skeletonChar - 'A'];
                int inputFieldCount = inputSkeletonFieldWidth[skeletonChar - 'A'];
                if (fieldCount != count || inputFieldCount <= fieldCount) {
                    bestMatchIntervalPattern2 = bestMatchIntervalPattern3;
                } else {
                    int count2 = inputFieldCount - fieldCount;
                    int j = 0;
                    while (true) {
                        bestMatchIntervalPattern2 = bestMatchIntervalPattern3;
                        int j2 = j;
                        if (j2 >= count2) {
                            break;
                        }
                        adjustedPtn.insert(i, prevCh);
                        j = j2 + 1;
                        bestMatchIntervalPattern3 = bestMatchIntervalPattern2;
                    }
                    i += count2;
                    adjustedPtnLength += count2;
                }
                count = 0;
            }
            if (ch == '\'') {
                if (i + 1 >= adjustedPtn.length() || adjustedPtn.charAt(i + 1) != '\'') {
                    inQuote = !inQuote;
                } else {
                    i++;
                }
            } else if (!inQuote) {
                if (ch >= 'a') {
                    c = 'z';
                } else {
                    c = 'z';
                }
                if (ch >= 'A') {
                    if (ch > 'Z') {
                    }
                    count++;
                    prevCh = ch;
                }
                i++;
                char c2 = c;
                bestMatchIntervalPattern3 = bestMatchIntervalPattern2;
            }
            c = 'z';
            i++;
            char c22 = c;
            bestMatchIntervalPattern3 = bestMatchIntervalPattern2;
        }
        if (count > 0) {
            char skeletonChar3 = prevCh;
            if (skeletonChar3 == 'L') {
                skeletonChar3 = 'M';
            }
            int fieldCount2 = bestMatchSkeletonFieldWidth[skeletonChar3 - 'A'];
            int inputFieldCount2 = inputSkeletonFieldWidth[skeletonChar3 - 'A'];
            if (fieldCount2 == count && inputFieldCount2 > fieldCount2) {
                int count3 = inputFieldCount2 - fieldCount2;
                int j3 = 0;
                while (true) {
                    int j4 = j3;
                    if (j4 >= count3) {
                        break;
                    }
                    adjustedPtn.append(prevCh);
                    j3 = j4 + 1;
                }
            }
        }
        return adjustedPtn.toString();
    }

    private void concatSingleDate2TimeInterval(String dtfmt, String datePattern, int field, Map<String, DateIntervalInfo.PatternInfo> intervalPatterns) {
        DateIntervalInfo.PatternInfo timeItvPtnInfo = intervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
        if (timeItvPtnInfo != null) {
            intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], DateIntervalInfo.genPatternInfo(SimpleFormatterImpl.formatRawPattern(dtfmt, 2, 2, timeItvPtnInfo.getFirstPart() + timeItvPtnInfo.getSecondPart(), datePattern), timeItvPtnInfo.firstDateInPtnIsLaterDate()));
        }
    }

    private static boolean fieldExistsInSkeleton(int field, String skeleton) {
        return skeleton.indexOf(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]) != -1;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initializePattern(this.isDateIntervalInfoDefault ? LOCAL_PATTERN_CACHE : null);
    }

    @Deprecated
    public Map<String, DateIntervalInfo.PatternInfo> getRawPatterns() {
        return this.fIntervalPatterns;
    }
}
