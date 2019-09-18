package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.UResource;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.TimeUnit;
import android.icu.util.TimeUnitAmount;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.ObjectStreamException;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

@Deprecated
public class TimeUnitFormat extends MeasureFormat {
    @Deprecated
    public static final int ABBREVIATED_NAME = 1;
    private static final String DEFAULT_PATTERN_FOR_DAY = "{0} d";
    private static final String DEFAULT_PATTERN_FOR_HOUR = "{0} h";
    private static final String DEFAULT_PATTERN_FOR_MINUTE = "{0} min";
    private static final String DEFAULT_PATTERN_FOR_MONTH = "{0} m";
    private static final String DEFAULT_PATTERN_FOR_SECOND = "{0} s";
    private static final String DEFAULT_PATTERN_FOR_WEEK = "{0} w";
    private static final String DEFAULT_PATTERN_FOR_YEAR = "{0} y";
    @Deprecated
    public static final int FULL_NAME = 0;
    private static final int TOTAL_STYLES = 2;
    private static final long serialVersionUID = -3707773153184971529L;
    private NumberFormat format;
    private transient boolean isReady;
    private ULocale locale;
    private transient MeasureFormat mf;
    private transient PluralRules pluralRules;
    private int style;
    private transient Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns;

    private static final class TimeUnitFormatSetupSink extends UResource.Sink {
        boolean beenHere = false;
        ULocale locale;
        Set<String> pluralKeywords;
        int style;
        Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns;

        TimeUnitFormatSetupSink(Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns2, int style2, Set<String> pluralKeywords2, ULocale locale2) {
            this.timeUnitToCountToPatterns = timeUnitToCountToPatterns2;
            this.style = style2;
            this.pluralKeywords = pluralKeywords2;
            this.locale = locale2;
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            TimeUnit timeUnit;
            UResource.Key key2 = key;
            UResource.Value value2 = value;
            if (!this.beenHere) {
                this.beenHere = true;
                UResource.Table units = value.getTable();
                for (int i = 0; units.getKeyAndValue(i, key2, value2); i++) {
                    String timeUnitName = key.toString();
                    if (timeUnitName.equals("year")) {
                        timeUnit = TimeUnit.YEAR;
                    } else if (timeUnitName.equals("month")) {
                        timeUnit = TimeUnit.MONTH;
                    } else if (timeUnitName.equals("day")) {
                        timeUnit = TimeUnit.DAY;
                    } else if (timeUnitName.equals("hour")) {
                        timeUnit = TimeUnit.HOUR;
                    } else if (timeUnitName.equals("minute")) {
                        timeUnit = TimeUnit.MINUTE;
                    } else if (timeUnitName.equals("second")) {
                        timeUnit = TimeUnit.SECOND;
                    } else if (timeUnitName.equals("week")) {
                        timeUnit = TimeUnit.WEEK;
                    }
                    Map<String, Object[]> countToPatterns = this.timeUnitToCountToPatterns.get(timeUnit);
                    if (countToPatterns == null) {
                        countToPatterns = new TreeMap<>();
                        this.timeUnitToCountToPatterns.put(timeUnit, countToPatterns);
                    }
                    UResource.Table countsToPatternTable = value.getTable();
                    for (int j = 0; countsToPatternTable.getKeyAndValue(j, key2, value2); j++) {
                        String pluralCount = key.toString();
                        if (this.pluralKeywords.contains(pluralCount)) {
                            Object[] pair = countToPatterns.get(pluralCount);
                            if (pair == null) {
                                pair = new Object[2];
                                countToPatterns.put(pluralCount, pair);
                            }
                            if (pair[this.style] == null) {
                                pair[this.style] = new MessageFormat(value.getString(), this.locale);
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated
    public TimeUnitFormat() {
        this.mf = MeasureFormat.getInstance(ULocale.getDefault(), MeasureFormat.FormatWidth.WIDE);
        this.isReady = false;
        this.style = 0;
    }

    @Deprecated
    public TimeUnitFormat(ULocale locale2) {
        this(locale2, 0);
    }

    @Deprecated
    public TimeUnitFormat(Locale locale2) {
        this(locale2, 0);
    }

    @Deprecated
    public TimeUnitFormat(ULocale locale2, int style2) {
        if (style2 < 0 || style2 >= 2) {
            throw new IllegalArgumentException("style should be either FULL_NAME or ABBREVIATED_NAME style");
        }
        this.mf = MeasureFormat.getInstance(locale2, style2 == 0 ? MeasureFormat.FormatWidth.WIDE : MeasureFormat.FormatWidth.SHORT);
        this.style = style2;
        setLocale(locale2, locale2);
        this.locale = locale2;
        this.isReady = false;
    }

    private TimeUnitFormat(ULocale locale2, int style2, NumberFormat numberFormat) {
        this(locale2, style2);
        if (numberFormat != null) {
            setNumberFormat((NumberFormat) numberFormat.clone());
        }
    }

    @Deprecated
    public TimeUnitFormat(Locale locale2, int style2) {
        this(ULocale.forLocale(locale2), style2);
    }

    @Deprecated
    public TimeUnitFormat setLocale(ULocale locale2) {
        if (locale2 != this.locale) {
            this.mf = this.mf.withLocale(locale2);
            setLocale(locale2, locale2);
            this.locale = locale2;
            this.isReady = false;
        }
        return this;
    }

    @Deprecated
    public TimeUnitFormat setLocale(Locale locale2) {
        return setLocale(ULocale.forLocale(locale2));
    }

    @Deprecated
    public TimeUnitFormat setNumberFormat(NumberFormat format2) {
        if (format2 == this.format) {
            return this;
        }
        if (format2 != null) {
            this.format = format2;
            this.mf = this.mf.withNumberFormat(this.format);
        } else if (this.locale == null) {
            this.isReady = false;
            this.mf = this.mf.withLocale(ULocale.getDefault());
        } else {
            this.format = NumberFormat.getNumberInstance(this.locale);
            this.mf = this.mf.withNumberFormat(this.format);
        }
        return this;
    }

    @Deprecated
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return this.mf.format(obj, toAppendTo, pos);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v0, resolved type: java.lang.Object} */
    /* JADX WARNING: Multi-variable type inference failed */
    @Deprecated
    public TimeUnitAmount parseObject(String source, ParsePosition pos) {
        Number temp;
        TimeUnitFormat timeUnitFormat = this;
        ParsePosition parsePosition = pos;
        if (!timeUnitFormat.isReady) {
            setup();
        }
        int resultNumber = null;
        TimeUnit resultTimeUnit = null;
        int oldPos = pos.getIndex();
        int newPos = -1;
        int longestParseDistance = 0;
        String countOfLongestMatch = null;
        Iterator<TimeUnit> it = timeUnitFormat.timeUnitToCountToPatterns.keySet().iterator();
        while (true) {
            int i = 2;
            int i2 = -1;
            if (!it.hasNext()) {
                break;
            }
            TimeUnit timeUnit = it.next();
            for (Map.Entry<String, Object[]> patternEntry : timeUnitFormat.timeUnitToCountToPatterns.get(timeUnit).entrySet()) {
                String count = patternEntry.getKey();
                int newPos2 = newPos;
                TimeUnit resultTimeUnit2 = resultTimeUnit;
                Number resultNumber2 = resultNumber;
                int styl = 0;
                while (true) {
                    int styl2 = styl;
                    if (styl2 >= i) {
                        break;
                    }
                    parsePosition.setErrorIndex(i2);
                    parsePosition.setIndex(oldPos);
                    Object parsed = ((MessageFormat) patternEntry.getValue()[styl2]).parseObject(source, parsePosition);
                    Number resultNumber3 = resultNumber2;
                    if (pos.getErrorIndex() == -1 && pos.getIndex() != oldPos) {
                        if (((Object[]) parsed).length != 0) {
                            Object tempObj = ((Object[]) parsed)[0];
                            if (tempObj instanceof Number) {
                                temp = (Number) tempObj;
                            } else {
                                try {
                                    temp = timeUnitFormat.format.parse(tempObj.toString());
                                } catch (ParseException e) {
                                }
                            }
                        } else {
                            temp = null;
                        }
                        int parseDistance = pos.getIndex() - oldPos;
                        if (parseDistance > longestParseDistance) {
                            resultNumber2 = temp;
                            resultTimeUnit2 = timeUnit;
                            newPos2 = pos.getIndex();
                            longestParseDistance = parseDistance;
                            countOfLongestMatch = count;
                            styl = styl2 + 1;
                            timeUnitFormat = this;
                            i = 2;
                            i2 = -1;
                        }
                    }
                    resultNumber2 = resultNumber3;
                    styl = styl2 + 1;
                    timeUnitFormat = this;
                    i = 2;
                    i2 = -1;
                }
                String str = source;
                Number resultNumber4 = resultNumber2;
                resultTimeUnit = resultTimeUnit2;
                newPos = newPos2;
                resultNumber = resultNumber4;
                timeUnitFormat = this;
                i = 2;
                i2 = -1;
            }
            String str2 = source;
            timeUnitFormat = this;
        }
        String str3 = source;
        if (resultNumber == null && longestParseDistance != 0) {
            if (countOfLongestMatch.equals(PluralRules.KEYWORD_ZERO)) {
                resultNumber = 0;
            } else if (countOfLongestMatch.equals(PluralRules.KEYWORD_ONE)) {
                resultNumber = 1;
            } else if (countOfLongestMatch.equals(PluralRules.KEYWORD_TWO)) {
                resultNumber = 2;
            } else {
                resultNumber = 3;
            }
        }
        if (longestParseDistance == 0) {
            parsePosition.setIndex(oldPos);
            parsePosition.setErrorIndex(0);
            return null;
        }
        parsePosition.setIndex(newPos);
        parsePosition.setErrorIndex(-1);
        return new TimeUnitAmount(resultNumber, resultTimeUnit);
    }

    private void setup() {
        if (this.locale == null) {
            if (this.format != null) {
                this.locale = this.format.getLocale(null);
            } else {
                this.locale = ULocale.getDefault(ULocale.Category.FORMAT);
            }
            setLocale(this.locale, this.locale);
        }
        if (this.format == null) {
            this.format = NumberFormat.getNumberInstance(this.locale);
        }
        this.pluralRules = PluralRules.forLocale(this.locale);
        this.timeUnitToCountToPatterns = new HashMap();
        Set<String> pluralKeywords = this.pluralRules.getKeywords();
        setup("units/duration", this.timeUnitToCountToPatterns, 0, pluralKeywords);
        setup("unitsShort/duration", this.timeUnitToCountToPatterns, 1, pluralKeywords);
        this.isReady = true;
    }

    private void setup(String resourceKey, Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns2, int style2, Set<String> pluralKeywords) {
        String str;
        Map<String, Object[]> countToPatterns;
        Iterator<String> it;
        Map<TimeUnit, Map<String, Object[]>> map = timeUnitToCountToPatterns2;
        int i = style2;
        try {
            try {
                str = resourceKey;
                try {
                    ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, this.locale)).getAllItemsWithFallback(str, new TimeUnitFormatSetupSink(map, i, pluralKeywords, this.locale));
                } catch (MissingResourceException e) {
                }
            } catch (MissingResourceException e2) {
                str = resourceKey;
            }
        } catch (MissingResourceException e3) {
            str = resourceKey;
            Set<String> set = pluralKeywords;
        }
        TimeUnit[] timeUnits = TimeUnit.values();
        Set<String> keywords = this.pluralRules.getKeywords();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < timeUnits.length) {
                TimeUnit timeUnit = timeUnits[i3];
                Map<String, Object[]> countToPatterns2 = map.get(timeUnit);
                if (countToPatterns2 == null) {
                    countToPatterns2 = new TreeMap<>();
                    map.put(timeUnit, countToPatterns2);
                }
                Map<String, Object[]> countToPatterns3 = countToPatterns2;
                Iterator<String> it2 = keywords.iterator();
                while (it2.hasNext()) {
                    String pluralCount = it2.next();
                    if (countToPatterns3.get(pluralCount) == null || countToPatterns3.get(pluralCount)[i] == null) {
                        it = it2;
                        countToPatterns = countToPatterns3;
                        searchInTree(str, i, timeUnit, pluralCount, pluralCount, countToPatterns3);
                    } else {
                        it = it2;
                        countToPatterns = countToPatterns3;
                    }
                    it2 = it;
                    countToPatterns3 = countToPatterns;
                }
                i2 = i3 + 1;
            } else {
                return;
            }
        }
    }

    private void searchInTree(String resourceKey, int styl, TimeUnit timeUnit, String srcPluralCount, String searchPluralCount, Map<String, Object[]> countToPatterns) {
        String str = resourceKey;
        TimeUnit timeUnit2 = timeUnit;
        String str2 = srcPluralCount;
        String str3 = searchPluralCount;
        Map<String, Object[]> map = countToPatterns;
        ULocale parentLocale = this.locale;
        String srcTimeUnitName = timeUnit.toString();
        ULocale parentLocale2 = parentLocale;
        while (true) {
            String srcTimeUnitName2 = srcTimeUnitName;
            if (parentLocale2 != null) {
                try {
                    MessageFormat messageFormat = new MessageFormat(((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, parentLocale2)).getWithFallback(str).getWithFallback(srcTimeUnitName2).getStringWithFallback(str3), this.locale);
                    Object[] pair = map.get(str2);
                    if (pair == null) {
                        pair = new Object[2];
                        map.put(str2, pair);
                    }
                    pair[styl] = messageFormat;
                    return;
                } catch (MissingResourceException e) {
                    parentLocale2 = parentLocale2.getFallback();
                    srcTimeUnitName = srcTimeUnitName2;
                }
            } else {
                if (parentLocale2 != null || !str.equals("unitsShort")) {
                } else {
                    String str4 = srcTimeUnitName2;
                    searchInTree("units", styl, timeUnit2, str2, str3, map);
                    if (!(map.get(str2) == null || map.get(str2)[styl] == null)) {
                        return;
                    }
                }
                if (str3.equals(PluralRules.KEYWORD_OTHER)) {
                    MessageFormat messageFormat2 = null;
                    if (timeUnit2 == TimeUnit.SECOND) {
                        messageFormat2 = new MessageFormat(DEFAULT_PATTERN_FOR_SECOND, this.locale);
                    } else if (timeUnit2 == TimeUnit.MINUTE) {
                        messageFormat2 = new MessageFormat(DEFAULT_PATTERN_FOR_MINUTE, this.locale);
                    } else if (timeUnit2 == TimeUnit.HOUR) {
                        messageFormat2 = new MessageFormat(DEFAULT_PATTERN_FOR_HOUR, this.locale);
                    } else if (timeUnit2 == TimeUnit.WEEK) {
                        messageFormat2 = new MessageFormat(DEFAULT_PATTERN_FOR_WEEK, this.locale);
                    } else if (timeUnit2 == TimeUnit.DAY) {
                        messageFormat2 = new MessageFormat(DEFAULT_PATTERN_FOR_DAY, this.locale);
                    } else if (timeUnit2 == TimeUnit.MONTH) {
                        messageFormat2 = new MessageFormat(DEFAULT_PATTERN_FOR_MONTH, this.locale);
                    } else if (timeUnit2 == TimeUnit.YEAR) {
                        messageFormat2 = new MessageFormat(DEFAULT_PATTERN_FOR_YEAR, this.locale);
                    }
                    Object[] pair2 = map.get(str2);
                    if (pair2 == null) {
                        pair2 = new Object[2];
                        map.put(str2, pair2);
                    }
                    pair2[styl] = messageFormat2;
                } else {
                    searchInTree(str, styl, timeUnit2, str2, PluralRules.KEYWORD_OTHER, map);
                }
                return;
            }
        }
    }

    @Deprecated
    public StringBuilder formatMeasures(StringBuilder appendTo, FieldPosition fieldPosition, Measure... measures) {
        return this.mf.formatMeasures(appendTo, fieldPosition, measures);
    }

    @Deprecated
    public MeasureFormat.FormatWidth getWidth() {
        return this.mf.getWidth();
    }

    @Deprecated
    public NumberFormat getNumberFormat() {
        return this.mf.getNumberFormat();
    }

    @Deprecated
    public Object clone() {
        TimeUnitFormat result = (TimeUnitFormat) super.clone();
        result.format = (NumberFormat) this.format.clone();
        return result;
    }

    private Object writeReplace() throws ObjectStreamException {
        return this.mf.toTimeUnitProxy();
    }

    private Object readResolve() throws ObjectStreamException {
        return new TimeUnitFormat(this.locale, this.style, this.format);
    }
}
