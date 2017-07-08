package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.util.Measure;
import android.icu.util.TimeUnit;
import android.icu.util.TimeUnitAmount;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.io.ObjectStreamException;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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

    @Deprecated
    public TimeUnitFormat() {
        this.mf = MeasureFormat.getInstance(ULocale.getDefault(), FormatWidth.WIDE);
        this.isReady = false;
        this.style = FULL_NAME;
    }

    @Deprecated
    public TimeUnitFormat(ULocale locale) {
        this(locale, (int) FULL_NAME);
    }

    @Deprecated
    public TimeUnitFormat(Locale locale) {
        this(locale, (int) FULL_NAME);
    }

    @Deprecated
    public TimeUnitFormat(ULocale locale, int style) {
        if (style < 0 || style >= TOTAL_STYLES) {
            throw new IllegalArgumentException("style should be either FULL_NAME or ABBREVIATED_NAME style");
        }
        this.mf = MeasureFormat.getInstance(locale, style == 0 ? FormatWidth.WIDE : FormatWidth.SHORT);
        this.style = style;
        setLocale(locale, locale);
        this.locale = locale;
        this.isReady = false;
    }

    private TimeUnitFormat(ULocale locale, int style, NumberFormat numberFormat) {
        this(locale, style);
        if (numberFormat != null) {
            setNumberFormat((NumberFormat) numberFormat.clone());
        }
    }

    @Deprecated
    public TimeUnitFormat(Locale locale, int style) {
        this(ULocale.forLocale(locale), style);
    }

    @Deprecated
    public TimeUnitFormat setLocale(ULocale locale) {
        if (locale != this.locale) {
            this.mf = this.mf.withLocale(locale);
            setLocale(locale, locale);
            this.locale = locale;
            this.isReady = false;
        }
        return this;
    }

    @Deprecated
    public TimeUnitFormat setLocale(Locale locale) {
        return setLocale(ULocale.forLocale(locale));
    }

    @Deprecated
    public TimeUnitFormat setNumberFormat(NumberFormat format) {
        if (format == this.format) {
            return this;
        }
        if (format != null) {
            this.format = format;
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

    @Deprecated
    public TimeUnitAmount parseObject(String source, ParsePosition pos) {
        if (!this.isReady) {
            setup();
        }
        Number resultNumber = null;
        TimeUnit resultTimeUnit = null;
        int oldPos = pos.getIndex();
        int newPos = -1;
        int longestParseDistance = FULL_NAME;
        String countOfLongestMatch = null;
        for (TimeUnit timeUnit : this.timeUnitToCountToPatterns.keySet()) {
            for (Entry<String, Object[]> patternEntry : ((Map) this.timeUnitToCountToPatterns.get(timeUnit)).entrySet()) {
                String count = (String) patternEntry.getKey();
                for (int styl = FULL_NAME; styl < TOTAL_STYLES; styl += ABBREVIATED_NAME) {
                    MessageFormat pattern = ((Object[]) patternEntry.getValue())[styl];
                    pos.setErrorIndex(-1);
                    pos.setIndex(oldPos);
                    Object parsed = pattern.parseObject(source, pos);
                    if (pos.getErrorIndex() == -1 && pos.getIndex() != oldPos) {
                        Number temp = null;
                        if (((Object[]) parsed).length != 0) {
                            Number tempObj = ((Object[]) parsed)[FULL_NAME];
                            if (tempObj instanceof Number) {
                                temp = tempObj;
                            } else {
                                try {
                                    temp = this.format.parse(tempObj.toString());
                                } catch (ParseException e) {
                                }
                            }
                        }
                        int parseDistance = pos.getIndex() - oldPos;
                        if (parseDistance > longestParseDistance) {
                            resultNumber = temp;
                            resultTimeUnit = timeUnit;
                            newPos = pos.getIndex();
                            longestParseDistance = parseDistance;
                            countOfLongestMatch = count;
                        }
                    }
                }
            }
        }
        if (resultNumber == null && longestParseDistance != 0) {
            if (countOfLongestMatch.equals(PluralRules.KEYWORD_ZERO)) {
                resultNumber = Integer.valueOf(FULL_NAME);
            } else {
                if (countOfLongestMatch.equals(PluralRules.KEYWORD_ONE)) {
                    resultNumber = Integer.valueOf(ABBREVIATED_NAME);
                } else {
                    if (countOfLongestMatch.equals(PluralRules.KEYWORD_TWO)) {
                        resultNumber = Integer.valueOf(TOTAL_STYLES);
                    } else {
                        resultNumber = Integer.valueOf(3);
                    }
                }
            }
        }
        if (longestParseDistance == 0) {
            pos.setIndex(oldPos);
            pos.setErrorIndex(FULL_NAME);
            return null;
        }
        pos.setIndex(newPos);
        pos.setErrorIndex(-1);
        return new TimeUnitAmount(resultNumber, resultTimeUnit);
    }

    private void setup() {
        if (this.locale == null) {
            if (this.format != null) {
                this.locale = this.format.getLocale(null);
            } else {
                this.locale = ULocale.getDefault(Category.FORMAT);
            }
            setLocale(this.locale, this.locale);
        }
        if (this.format == null) {
            this.format = NumberFormat.getNumberInstance(this.locale);
        }
        this.pluralRules = PluralRules.forLocale(this.locale);
        this.timeUnitToCountToPatterns = new HashMap();
        Set<String> pluralKeywords = this.pluralRules.getKeywords();
        setup("units/duration", this.timeUnitToCountToPatterns, FULL_NAME, pluralKeywords);
        setup("unitsShort/duration", this.timeUnitToCountToPatterns, ABBREVIATED_NAME, pluralKeywords);
        this.isReady = true;
    }

    private void setup(String resourceKey, Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns, int style, Set<String> pluralKeywords) {
        TimeUnit timeUnit;
        Map<String, Object[]> countToPatterns;
        String pluralCount;
        try {
            ICUResourceBundle unitsRes = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, this.locale)).getWithFallback(resourceKey);
            int size = unitsRes.getSize();
            for (int index = FULL_NAME; index < size; index += ABBREVIATED_NAME) {
                String timeUnitName = unitsRes.get(index).getKey();
                if (timeUnitName.equals("year")) {
                    timeUnit = TimeUnit.YEAR;
                } else {
                    if (timeUnitName.equals("month")) {
                        timeUnit = TimeUnit.MONTH;
                    } else {
                        if (timeUnitName.equals("day")) {
                            timeUnit = TimeUnit.DAY;
                        } else {
                            if (timeUnitName.equals("hour")) {
                                timeUnit = TimeUnit.HOUR;
                            } else {
                                if (timeUnitName.equals("minute")) {
                                    timeUnit = TimeUnit.MINUTE;
                                } else {
                                    if (timeUnitName.equals("second")) {
                                        timeUnit = TimeUnit.SECOND;
                                    } else {
                                        if (timeUnitName.equals("week")) {
                                            timeUnit = TimeUnit.WEEK;
                                        } else {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ICUResourceBundle oneUnitRes = unitsRes.getWithFallback(timeUnitName);
                int count = oneUnitRes.getSize();
                countToPatterns = (Map) timeUnitToCountToPatterns.get(timeUnit);
                if (countToPatterns == null) {
                    countToPatterns = new TreeMap();
                    timeUnitToCountToPatterns.put(timeUnit, countToPatterns);
                }
                for (int pluralIndex = FULL_NAME; pluralIndex < count; pluralIndex += ABBREVIATED_NAME) {
                    pluralCount = oneUnitRes.get(pluralIndex).getKey();
                    if (pluralKeywords.contains(pluralCount)) {
                        MessageFormat messageFormat = new MessageFormat(oneUnitRes.get(pluralIndex).getString(), this.locale);
                        Object[] pair = (Object[]) countToPatterns.get(pluralCount);
                        if (pair == null) {
                            pair = new Object[TOTAL_STYLES];
                            countToPatterns.put(pluralCount, pair);
                        }
                        pair[style] = messageFormat;
                    }
                }
            }
        } catch (MissingResourceException e) {
        }
        TimeUnit[] timeUnits = TimeUnit.values();
        Set<String> keywords = this.pluralRules.getKeywords();
        for (int i = FULL_NAME; i < timeUnits.length; i += ABBREVIATED_NAME) {
            timeUnit = timeUnits[i];
            countToPatterns = (Map) timeUnitToCountToPatterns.get(timeUnit);
            if (countToPatterns == null) {
                countToPatterns = new TreeMap();
                timeUnitToCountToPatterns.put(timeUnit, countToPatterns);
            }
            for (String pluralCount2 : keywords) {
                if (countToPatterns.get(pluralCount2) == null || ((Object[]) countToPatterns.get(pluralCount2))[style] == null) {
                    searchInTree(resourceKey, style, timeUnit, pluralCount2, pluralCount2, countToPatterns);
                }
            }
        }
    }

    private void searchInTree(String resourceKey, int styl, TimeUnit timeUnit, String srcPluralCount, String searchPluralCount, Map<String, Object[]> countToPatterns) {
        Object[] pair;
        ULocale parentLocale = this.locale;
        String srcTimeUnitName = timeUnit.toString();
        while (parentLocale != null) {
            try {
                MessageFormat messageFormat = new MessageFormat(((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, parentLocale)).getWithFallback(resourceKey).getWithFallback(srcTimeUnitName).getStringWithFallback(searchPluralCount), this.locale);
                pair = (Object[]) countToPatterns.get(srcPluralCount);
                if (pair == null) {
                    pair = new Object[TOTAL_STYLES];
                    countToPatterns.put(srcPluralCount, pair);
                }
                pair[styl] = messageFormat;
                return;
            } catch (MissingResourceException e) {
                parentLocale = parentLocale.getFallback();
            }
        }
        if (parentLocale == null) {
            if (resourceKey.equals("unitsShort")) {
                searchInTree("units", styl, timeUnit, srcPluralCount, searchPluralCount, countToPatterns);
                if (!(countToPatterns == null || countToPatterns.get(srcPluralCount) == null || ((Object[]) countToPatterns.get(srcPluralCount))[styl] == null)) {
                    return;
                }
            }
        }
        if (searchPluralCount.equals(PluralRules.KEYWORD_OTHER)) {
            messageFormat = null;
            if (timeUnit == TimeUnit.SECOND) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_SECOND, this.locale);
            } else if (timeUnit == TimeUnit.MINUTE) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_MINUTE, this.locale);
            } else if (timeUnit == TimeUnit.HOUR) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_HOUR, this.locale);
            } else if (timeUnit == TimeUnit.WEEK) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_WEEK, this.locale);
            } else if (timeUnit == TimeUnit.DAY) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_DAY, this.locale);
            } else if (timeUnit == TimeUnit.MONTH) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_MONTH, this.locale);
            } else if (timeUnit == TimeUnit.YEAR) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_YEAR, this.locale);
            }
            pair = (Object[]) countToPatterns.get(srcPluralCount);
            if (pair == null) {
                pair = new Object[TOTAL_STYLES];
                countToPatterns.put(srcPluralCount, pair);
            }
            pair[styl] = messageFormat;
        } else {
            searchInTree(resourceKey, styl, timeUnit, srcPluralCount, PluralRules.KEYWORD_OTHER, countToPatterns);
        }
    }

    @Deprecated
    public StringBuilder formatMeasures(StringBuilder appendTo, FieldPosition fieldPosition, Measure... measures) {
        return this.mf.formatMeasures(appendTo, fieldPosition, measures);
    }

    @Deprecated
    public FormatWidth getWidth() {
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
