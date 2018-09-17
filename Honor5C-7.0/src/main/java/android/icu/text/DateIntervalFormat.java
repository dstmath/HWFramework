package android.icu.text;

import android.icu.impl.CalendarData;
import android.icu.impl.ICUCache;
import android.icu.impl.PatternTokenizer;
import android.icu.lang.UScript;
import android.icu.text.DateIntervalInfo.PatternInfo;
import android.icu.util.Calendar;
import android.icu.util.DateInterval;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.harmony.security.provider.crypto.SHA1Constants;

public class DateIntervalFormat extends UFormat {
    private static ICUCache<String, Map<String, PatternInfo>> LOCAL_PATTERN_CACHE = null;
    private static final long serialVersionUID = 1;
    private SimpleDateFormat fDateFormat;
    private String fDatePattern;
    private String fDateTimeFormat;
    private Calendar fFromCalendar;
    private DateIntervalInfo fInfo;
    private transient Map<String, PatternInfo> fIntervalPatterns;
    private String fSkeleton;
    private String fTimePattern;
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

        SkeletonAndItsBestMatch(String skeleton, String bestMatch) {
            this.skeleton = skeleton;
            this.bestMatchSkeleton = bestMatch;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateIntervalFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateIntervalFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateIntervalFormat.<clinit>():void");
    }

    private DateIntervalFormat() {
        this.fSkeleton = null;
        this.fIntervalPatterns = null;
        this.fDatePattern = null;
        this.fTimePattern = null;
        this.fDateTimeFormat = null;
    }

    @Deprecated
    public DateIntervalFormat(String skeleton, DateIntervalInfo dtItvInfo, SimpleDateFormat simpleDateFormat) {
        this.fSkeleton = null;
        this.fIntervalPatterns = null;
        this.fDatePattern = null;
        this.fTimePattern = null;
        this.fDateTimeFormat = null;
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
        this.fSkeleton = null;
        this.fIntervalPatterns = null;
        this.fDatePattern = null;
        this.fTimePattern = null;
        this.fDateTimeFormat = null;
        this.fDateFormat = simpleDateFormat;
        this.fSkeleton = skeleton;
        this.fInfo = new DateIntervalInfo(locale).freeze();
        this.isDateIntervalInfoDefault = true;
        this.fFromCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        this.fToCalendar = (Calendar) this.fDateFormat.getCalendar().clone();
        initializePattern(LOCAL_PATTERN_CACHE);
    }

    public static final DateIntervalFormat getInstance(String skeleton) {
        return getInstance(skeleton, ULocale.getDefault(Category.FORMAT));
    }

    public static final DateIntervalFormat getInstance(String skeleton, Locale locale) {
        return getInstance(skeleton, ULocale.forLocale(locale));
    }

    public static final DateIntervalFormat getInstance(String skeleton, ULocale locale) {
        return new DateIntervalFormat(skeleton, locale, new SimpleDateFormat(DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton), locale));
    }

    public static final DateIntervalFormat getInstance(String skeleton, DateIntervalInfo dtitvinf) {
        return getInstance(skeleton, ULocale.getDefault(Category.FORMAT), dtitvinf);
    }

    public static final DateIntervalFormat getInstance(String skeleton, Locale locale, DateIntervalInfo dtitvinf) {
        return getInstance(skeleton, ULocale.forLocale(locale), dtitvinf);
    }

    public static final DateIntervalFormat getInstance(String skeleton, ULocale locale, DateIntervalInfo dtitvinf) {
        return new DateIntervalFormat(skeleton, (DateIntervalInfo) dtitvinf.clone(), new SimpleDateFormat(DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton), locale));
    }

    public Object clone() {
        DateIntervalFormat other = (DateIntervalFormat) super.clone();
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

    public final StringBuffer format(DateInterval dtInterval, StringBuffer appendTo, FieldPosition fieldPosition) {
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
        PatternInfo intervalPattern = (PatternInfo) this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
        part2.value = intervalPattern.getSecondPart();
        return intervalPattern.getFirstPart();
    }

    public final StringBuffer format(Calendar fromCalendar, Calendar toCalendar, StringBuffer appendTo, FieldPosition pos) {
        if (fromCalendar.isEquivalentTo(toCalendar)) {
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
                return this.fDateFormat.format(fromCalendar, appendTo, pos);
            } else {
                field = 13;
            }
            boolean fromToOnSameDay = field == 9 || field == 10 || field == 12 || field == 13;
            PatternInfo intervalPattern = (PatternInfo) this.fIntervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
            if (intervalPattern == null) {
                if (this.fDateFormat.isFieldUnitIgnored(field)) {
                    return this.fDateFormat.format(fromCalendar, appendTo, pos);
                }
                return fallbackFormat(fromCalendar, toCalendar, fromToOnSameDay, appendTo, pos);
            } else if (intervalPattern.getFirstPart() == null) {
                return fallbackFormat(fromCalendar, toCalendar, fromToOnSameDay, appendTo, pos, intervalPattern.getSecondPart());
            } else {
                Calendar firstCal;
                Calendar secondCal;
                if (intervalPattern.firstDateInPtnIsLaterDate()) {
                    firstCal = toCalendar;
                    secondCal = fromCalendar;
                } else {
                    firstCal = fromCalendar;
                    secondCal = toCalendar;
                }
                String originalPattern = this.fDateFormat.toPattern();
                this.fDateFormat.applyPattern(intervalPattern.getFirstPart());
                this.fDateFormat.format(firstCal, appendTo, pos);
                if (intervalPattern.getSecondPart() != null) {
                    this.fDateFormat.applyPattern(intervalPattern.getSecondPart());
                    FieldPosition otherPos = new FieldPosition(pos.getField());
                    this.fDateFormat.format(secondCal, appendTo, otherPos);
                    if (pos.getEndIndex() == 0 && otherPos.getEndIndex() > 0) {
                        pos = otherPos;
                    }
                }
                this.fDateFormat.applyPattern(originalPattern);
                return appendTo;
            }
        }
        throw new IllegalArgumentException("can not format on two different calendars");
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
                    index1 += pat0.length() - 3;
                    posResult.setBeginIndex(pos1.getBeginIndex() + index1);
                    posResult.setEndIndex(pos1.getEndIndex() + index1);
                }
            } else if (pos1.getEndIndex() > 0) {
                posResult.setBeginIndex(pos1.getBeginIndex() + index1);
                posResult.setEndIndex(pos1.getEndIndex() + index1);
            } else if (pos0.getEndIndex() > 0) {
                index0 += pat1.length() - 3;
                posResult.setBeginIndex(pos0.getBeginIndex() + index0);
                posResult.setEndIndex(pos0.getEndIndex() + index0);
            }
        }
    }

    private final StringBuffer fallbackFormat(Calendar fromCalendar, Calendar toCalendar, boolean fromToOnSameDay, StringBuffer appendTo, FieldPosition pos) {
        boolean formatDatePlusTimeRange;
        String fullPattern = null;
        if (!fromToOnSameDay || this.fDatePattern == null) {
            formatDatePlusTimeRange = false;
        } else {
            formatDatePlusTimeRange = this.fTimePattern != null;
        }
        if (formatDatePlusTimeRange) {
            fullPattern = this.fDateFormat.toPattern();
            this.fDateFormat.applyPattern(this.fTimePattern);
        }
        FieldPosition otherPos = new FieldPosition(pos.getField());
        StringBuffer earlierDate = this.fDateFormat.format(fromCalendar, new StringBuffer(64), pos);
        StringBuffer laterDate = this.fDateFormat.format(toCalendar, new StringBuffer(64), otherPos);
        String fallbackPattern = this.fInfo.getFallbackIntervalPattern();
        adjustPosition(fallbackPattern, earlierDate.toString(), pos, laterDate.toString(), otherPos, pos);
        String fallbackRange = MessageFormat.format(fallbackPattern, earlierDate.toString(), laterDate.toString());
        if (formatDatePlusTimeRange) {
            this.fDateFormat.applyPattern(this.fDatePattern);
            StringBuffer stringBuffer = new StringBuffer(64);
            otherPos.setBeginIndex(0);
            otherPos.setEndIndex(0);
            adjustPosition(this.fDateTimeFormat, fallbackRange, pos, this.fDateFormat.format(fromCalendar, stringBuffer, otherPos).toString(), otherPos, pos);
            fallbackRange = MessageFormat.format(this.fDateTimeFormat, fallbackRange, datePortion.toString());
        }
        appendTo.append(fallbackRange);
        if (formatDatePlusTimeRange) {
            this.fDateFormat.applyPattern(fullPattern);
        }
        return appendTo;
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

    public DateFormat getDateFormat() {
        return (DateFormat) this.fDateFormat.clone();
    }

    private void initializePattern(ICUCache<String, Map<String, PatternInfo>> cache) {
        String fullPattern = this.fDateFormat.toPattern();
        ULocale locale = this.fDateFormat.getLocale();
        String key = null;
        Map<String, PatternInfo> map = null;
        if (cache != null) {
            if (this.fSkeleton != null) {
                key = locale.toString() + "+" + fullPattern + "+" + this.fSkeleton;
            } else {
                key = locale.toString() + "+" + fullPattern;
            }
            map = (Map) cache.get(key);
        }
        if (map == null) {
            map = Collections.unmodifiableMap(initializeIntervalPattern(fullPattern, locale));
            if (cache != null) {
                cache.put(key, map);
            }
        }
        this.fIntervalPatterns = map;
    }

    private Map<String, PatternInfo> initializeIntervalPattern(String fullPattern, ULocale locale) {
        DateTimePatternGenerator dtpng = DateTimePatternGenerator.getInstance(locale);
        if (this.fSkeleton == null) {
            this.fSkeleton = dtpng.getSkeleton(fullPattern);
        }
        String skeleton = this.fSkeleton;
        HashMap<String, PatternInfo> intervalPatterns = new HashMap();
        StringBuilder date = new StringBuilder(skeleton.length());
        StringBuilder normalizedDate = new StringBuilder(skeleton.length());
        StringBuilder stringBuilder = new StringBuilder(skeleton.length());
        StringBuilder normalizedTime = new StringBuilder(skeleton.length());
        getDateTimeSkeleton(skeleton, date, normalizedDate, stringBuilder, normalizedTime);
        String dateSkeleton = date.toString();
        String timeSkeleton = stringBuilder.toString();
        String normalizedDateSkeleton = normalizedDate.toString();
        String normalizedTimeSkeleton = normalizedTime.toString();
        if (!(stringBuilder.length() == 0 || date.length() == 0)) {
            this.fDateTimeFormat = new CalendarData(locale, null).getDateTimePatterns()[8];
        }
        if (genSeparateDateTimePtn(normalizedDateSkeleton, normalizedTimeSkeleton, intervalPatterns, dtpng)) {
            if (stringBuilder.length() != 0) {
                if (date.length() == 0) {
                    String str = DateFormat.YEAR_NUM_MONTH_DAY + timeSkeleton;
                    PatternInfo patternInfo = new PatternInfo(null, dtpng.getBestPattern(timeSkeleton), this.fInfo.getDefaultOrder());
                    intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5], patternInfo);
                    intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2], patternInfo);
                    intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1], patternInfo);
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
        if (stringBuilder.length() != 0 && date.length() == 0) {
            str = DateFormat.YEAR_NUM_MONTH_DAY + timeSkeleton;
            patternInfo = new PatternInfo(null, dtpng.getBestPattern(timeSkeleton), this.fInfo.getDefaultOrder());
            intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[5], patternInfo);
            intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[2], patternInfo);
            intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[1], patternInfo);
        }
        return intervalPatterns;
    }

    private void genFallbackPattern(int field, String skeleton, Map<String, PatternInfo> intervalPatterns, DateTimePatternGenerator dtpng) {
        intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], new PatternInfo(null, dtpng.getBestPattern(skeleton), this.fInfo.getDefaultOrder()));
    }

    private static void getDateTimeSkeleton(String skeleton, StringBuilder dateSkeleton, StringBuilder normalizedDateSkeleton, StringBuilder timeSkeleton, StringBuilder normalizedTimeSkeleton) {
        int i;
        int ECount = 0;
        int dCount = 0;
        int MCount = 0;
        int yCount = 0;
        int hCount = 0;
        int HCount = 0;
        int mCount = 0;
        int vCount = 0;
        int zCount = 0;
        for (i = 0; i < skeleton.length(); i++) {
            char ch = skeleton.charAt(i);
            switch (ch) {
                case UScript.BRAHMI /*65*/:
                case Opcodes.OP_APUT /*75*/:
                case Opcodes.OP_IGET_WIDE /*83*/:
                case Opcodes.OP_IGET_BYTE /*86*/:
                case Opcodes.OP_IPUT_WIDE /*90*/:
                case Opcodes.OP_SPUT_BOOLEAN /*106*/:
                case Opcodes.OP_SPUT_BYTE /*107*/:
                case UScript.MEITEI_MAYEK /*115*/:
                    timeSkeleton.append(ch);
                    normalizedTimeSkeleton.append(ch);
                    break;
                case Opcodes.OP_AGET /*68*/:
                case Opcodes.OP_AGET_OBJECT /*70*/:
                case Opcodes.OP_AGET_BOOLEAN /*71*/:
                case Opcodes.OP_APUT_WIDE /*76*/:
                case SHA1Constants.BYTES_OFFSET /*81*/:
                case Opcodes.OP_IGET_BOOLEAN /*85*/:
                case Opcodes.OP_IGET_CHAR /*87*/:
                case Opcodes.OP_IPUT /*89*/:
                case Opcodes.OP_SGET_BOOLEAN /*99*/:
                case Opcodes.OP_SGET_CHAR /*101*/:
                case Opcodes.OP_SPUT /*103*/:
                case Opcodes.OP_SPUT_CHAR /*108*/:
                case Opcodes.OP_INVOKE_STATIC /*113*/:
                case Opcodes.OP_INVOKE_INTERFACE /*114*/:
                case Opcodes.OP_INVOKE_SUPER_RANGE /*117*/:
                case Opcodes.OP_INVOKE_STATIC_RANGE /*119*/:
                    normalizedDateSkeleton.append(ch);
                    dateSkeleton.append(ch);
                    break;
                case Opcodes.OP_AGET_WIDE /*69*/:
                    dateSkeleton.append(ch);
                    ECount++;
                    break;
                case Opcodes.OP_AGET_BYTE /*72*/:
                    timeSkeleton.append(ch);
                    HCount++;
                    break;
                case Opcodes.OP_APUT_OBJECT /*77*/:
                    dateSkeleton.append(ch);
                    MCount++;
                    break;
                case Opcodes.OP_SGET_WIDE /*97*/:
                    timeSkeleton.append(ch);
                    break;
                case Opcodes.OP_SGET_BYTE /*100*/:
                    dateSkeleton.append(ch);
                    dCount++;
                    break;
                case Opcodes.OP_SPUT_WIDE /*104*/:
                    timeSkeleton.append(ch);
                    hCount++;
                    break;
                case Opcodes.OP_SPUT_SHORT /*109*/:
                    timeSkeleton.append(ch);
                    mCount++;
                    break;
                case Opcodes.OP_INVOKE_DIRECT_RANGE /*118*/:
                    vCount++;
                    timeSkeleton.append(ch);
                    break;
                case UScript.MANICHAEAN /*121*/:
                    dateSkeleton.append(ch);
                    yCount++;
                    break;
                case UScript.INSCRIPTIONAL_PAHLAVI /*122*/:
                    zCount++;
                    timeSkeleton.append(ch);
                    break;
                default:
                    break;
            }
        }
        if (yCount != 0) {
            for (i = 0; i < yCount; i++) {
                normalizedDateSkeleton.append('y');
            }
        }
        if (MCount != 0) {
            if (MCount < 3) {
                normalizedDateSkeleton.append('M');
            } else {
                i = 0;
                while (i < MCount && i < 5) {
                    normalizedDateSkeleton.append('M');
                    i++;
                }
            }
        }
        if (ECount != 0) {
            if (ECount <= 3) {
                normalizedDateSkeleton.append('E');
            } else {
                i = 0;
                while (i < ECount && i < 5) {
                    normalizedDateSkeleton.append('E');
                    i++;
                }
            }
        }
        if (dCount != 0) {
            normalizedDateSkeleton.append('d');
        }
        if (HCount != 0) {
            normalizedTimeSkeleton.append('H');
        } else if (hCount != 0) {
            normalizedTimeSkeleton.append('h');
        }
        if (mCount != 0) {
            normalizedTimeSkeleton.append('m');
        }
        if (zCount != 0) {
            normalizedTimeSkeleton.append('z');
        }
        if (vCount != 0) {
            normalizedTimeSkeleton.append('v');
        }
    }

    private boolean genSeparateDateTimePtn(String dateSkeleton, String timeSkeleton, Map<String, PatternInfo> intervalPatterns, DateTimePatternGenerator dtpng) {
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
            genIntervalPattern(5, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
            SkeletonAndItsBestMatch skeletons = genIntervalPattern(2, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
            if (skeletons != null) {
                bestSkeleton = skeletons.skeleton;
                skeleton = skeletons.bestMatchSkeleton;
            }
            genIntervalPattern(1, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
        } else {
            genIntervalPattern(12, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
            genIntervalPattern(10, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
            genIntervalPattern(9, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
        }
        return true;
    }

    private SkeletonAndItsBestMatch genIntervalPattern(int field, String skeleton, String bestSkeleton, int differenceInfo, Map<String, PatternInfo> intervalPatterns) {
        PatternInfo pattern;
        SkeletonAndItsBestMatch retValue = null;
        PatternInfo pattern2 = this.fInfo.getIntervalPattern(bestSkeleton, field);
        if (pattern2 == null) {
            if (SimpleDateFormat.isFieldUnitIgnored(bestSkeleton, field)) {
                Map<String, PatternInfo> map = intervalPatterns;
                map.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], new PatternInfo(this.fDateFormat.toPattern(), null, this.fInfo.getDefaultOrder()));
                return null;
            } else if (field == 9) {
                pattern2 = this.fInfo.getIntervalPattern(bestSkeleton, 10);
                if (pattern2 != null) {
                    intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], pattern2);
                }
                return null;
            } else {
                String fieldLetter = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field];
                bestSkeleton = fieldLetter + bestSkeleton;
                skeleton = fieldLetter + skeleton;
                pattern2 = this.fInfo.getIntervalPattern(bestSkeleton, field);
                if (pattern2 == null && differenceInfo == 0) {
                    BestMatchInfo tmpRetValue = this.fInfo.getBestSkeleton(skeleton);
                    String tmpBestSkeleton = tmpRetValue.bestMatchSkeleton;
                    differenceInfo = tmpRetValue.bestMatchDistanceInfo;
                    if (!(tmpBestSkeleton.length() == 0 || differenceInfo == -1)) {
                        pattern2 = this.fInfo.getIntervalPattern(tmpBestSkeleton, field);
                        bestSkeleton = tmpBestSkeleton;
                    }
                }
                if (pattern2 != null) {
                    retValue = new SkeletonAndItsBestMatch(skeleton, bestSkeleton);
                    pattern = pattern2;
                    if (pattern == null) {
                        if (differenceInfo == 0) {
                            pattern2 = new PatternInfo(adjustFieldWidth(skeleton, bestSkeleton, pattern.getFirstPart(), differenceInfo), adjustFieldWidth(skeleton, bestSkeleton, pattern.getSecondPart(), differenceInfo), pattern.firstDateInPtnIsLaterDate());
                        } else {
                            pattern2 = pattern;
                        }
                        intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], pattern2);
                    }
                    return retValue;
                }
            }
        }
        pattern = pattern2;
        if (pattern == null) {
        } else {
            if (differenceInfo == 0) {
                pattern2 = pattern;
            } else {
                pattern2 = new PatternInfo(adjustFieldWidth(skeleton, bestSkeleton, pattern.getFirstPart(), differenceInfo), adjustFieldWidth(skeleton, bestSkeleton, pattern.getSecondPart(), differenceInfo), pattern.firstDateInPtnIsLaterDate());
            }
            intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], pattern2);
        }
        return retValue;
    }

    private static String adjustFieldWidth(String inputSkeleton, String bestMatchSkeleton, String bestMatchIntervalPattern, int differenceInfo) {
        if (bestMatchIntervalPattern == null) {
            return null;
        }
        int fieldCount;
        int j;
        int[] inputSkeletonFieldWidth = new int[58];
        int[] bestMatchSkeletonFieldWidth = new int[58];
        DateIntervalInfo.parseSkeleton(inputSkeleton, inputSkeletonFieldWidth);
        DateIntervalInfo.parseSkeleton(bestMatchSkeleton, bestMatchSkeletonFieldWidth);
        if (differenceInfo == 2) {
            bestMatchIntervalPattern = bestMatchIntervalPattern.replace('v', 'z');
        }
        StringBuilder adjustedPtn = new StringBuilder(bestMatchIntervalPattern);
        boolean inQuote = false;
        char prevCh = '\u0000';
        int count = 0;
        int adjustedPtnLength = adjustedPtn.length();
        int i = 0;
        while (i < adjustedPtnLength) {
            int skeletonChar;
            int inputFieldCount;
            char ch = adjustedPtn.charAt(i);
            if (ch != prevCh && count > 0) {
                skeletonChar = prevCh;
                if (prevCh == 'L') {
                    skeletonChar = 77;
                }
                fieldCount = bestMatchSkeletonFieldWidth[skeletonChar - 65];
                inputFieldCount = inputSkeletonFieldWidth[skeletonChar - 65];
                if (fieldCount == count && inputFieldCount > fieldCount) {
                    count = inputFieldCount - fieldCount;
                    for (j = 0; j < count; j++) {
                        adjustedPtn.insert(i, prevCh);
                    }
                    i += count;
                    adjustedPtnLength += count;
                }
                count = 0;
            }
            if (ch == PatternTokenizer.SINGLE_QUOTE) {
                if (i + 1 >= adjustedPtn.length() || adjustedPtn.charAt(i + 1) != PatternTokenizer.SINGLE_QUOTE) {
                    inQuote = !inQuote;
                } else {
                    i++;
                }
            } else if (!inQuote) {
                if (ch < 'a' || ch > 'z') {
                    if (ch >= 'A' && ch <= 'Z') {
                    }
                }
                prevCh = ch;
                count++;
            }
            i++;
        }
        if (count > 0) {
            skeletonChar = prevCh;
            if (prevCh == 'L') {
                skeletonChar = 77;
            }
            fieldCount = bestMatchSkeletonFieldWidth[skeletonChar - 65];
            inputFieldCount = inputSkeletonFieldWidth[skeletonChar - 65];
            if (fieldCount == count && inputFieldCount > fieldCount) {
                count = inputFieldCount - fieldCount;
                for (j = 0; j < count; j++) {
                    adjustedPtn.append(prevCh);
                }
            }
        }
        return adjustedPtn.toString();
    }

    private void concatSingleDate2TimeInterval(String dtfmt, String datePattern, int field, Map<String, PatternInfo> intervalPatterns) {
        PatternInfo timeItvPtnInfo = (PatternInfo) intervalPatterns.get(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
        if (timeItvPtnInfo != null) {
            String timeIntervalPattern = timeItvPtnInfo.getFirstPart() + timeItvPtnInfo.getSecondPart();
            intervalPatterns.put(DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], DateIntervalInfo.genPatternInfo(MessageFormat.format(dtfmt, timeIntervalPattern, datePattern), timeItvPtnInfo.firstDateInPtnIsLaterDate()));
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
    public Map<String, PatternInfo> getRawPatterns() {
        return this.fIntervalPatterns;
    }
}
