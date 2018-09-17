package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
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

    private static final class TimeUnitFormatSetupSink extends Sink {
        boolean beenHere = false;
        ULocale locale;
        Set<String> pluralKeywords;
        int style;
        Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns;

        TimeUnitFormatSetupSink(Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns, int style, Set<String> pluralKeywords, ULocale locale) {
            this.timeUnitToCountToPatterns = timeUnitToCountToPatterns;
            this.style = style;
            this.pluralKeywords = pluralKeywords;
            this.locale = locale;
        }

        public void put(Key key, Value value, boolean noFallback) {
            if (!this.beenHere) {
                this.beenHere = true;
                Table units = value.getTable();
                for (int i = 0; units.getKeyAndValue(i, key, value); i++) {
                    TimeUnit timeUnit;
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
                    } else {
                    }
                    Map<String, Object[]> countToPatterns = (Map) this.timeUnitToCountToPatterns.get(timeUnit);
                    if (countToPatterns == null) {
                        countToPatterns = new TreeMap();
                        this.timeUnitToCountToPatterns.put(timeUnit, countToPatterns);
                    }
                    Table countsToPatternTable = value.getTable();
                    for (int j = 0; countsToPatternTable.getKeyAndValue(j, key, value); j++) {
                        String pluralCount = key.toString();
                        if (this.pluralKeywords.contains(pluralCount)) {
                            Object[] pair = (Object[]) countToPatterns.get(pluralCount);
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
        this.mf = MeasureFormat.getInstance(ULocale.getDefault(), FormatWidth.WIDE);
        this.isReady = false;
        this.style = 0;
    }

    @Deprecated
    public TimeUnitFormat(ULocale locale) {
        this(locale, 0);
    }

    @Deprecated
    public TimeUnitFormat(Locale locale) {
        this(locale, 0);
    }

    @Deprecated
    public TimeUnitFormat(ULocale locale, int style) {
        if (style < 0 || style >= 2) {
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
        int longestParseDistance = 0;
        String countOfLongestMatch = null;
        for (TimeUnit timeUnit : this.timeUnitToCountToPatterns.keySet()) {
            for (Entry<String, Object[]> patternEntry : ((Map) this.timeUnitToCountToPatterns.get(timeUnit)).entrySet()) {
                String count = (String) patternEntry.getKey();
                for (int styl = 0; styl < 2; styl++) {
                    MessageFormat pattern = ((Object[]) patternEntry.getValue())[styl];
                    pos.setErrorIndex(-1);
                    pos.setIndex(oldPos);
                    Object parsed = pattern.parseObject(source, pos);
                    if (pos.getErrorIndex() == -1 && pos.getIndex() != oldPos) {
                        Number temp = null;
                        if (((Object[]) parsed).length != 0) {
                            Number tempObj = ((Object[]) parsed)[0];
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
                resultNumber = Integer.valueOf(0);
            } else if (countOfLongestMatch.equals(PluralRules.KEYWORD_ONE)) {
                resultNumber = Integer.valueOf(1);
            } else if (countOfLongestMatch.equals(PluralRules.KEYWORD_TWO)) {
                resultNumber = Integer.valueOf(2);
            } else {
                resultNumber = Integer.valueOf(3);
            }
        }
        if (longestParseDistance == 0) {
            pos.setIndex(oldPos);
            pos.setErrorIndex(0);
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
        setup("units/duration", this.timeUnitToCountToPatterns, 0, pluralKeywords);
        setup("unitsShort/duration", this.timeUnitToCountToPatterns, 1, pluralKeywords);
        this.isReady = true;
    }

    private void setup(String resourceKey, Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns, int style, Set<String> pluralKeywords) {
        try {
            ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, this.locale)).getAllItemsWithFallback(resourceKey, new TimeUnitFormatSetupSink(timeUnitToCountToPatterns, style, pluralKeywords, this.locale));
        } catch (MissingResourceException e) {
        }
        TimeUnit[] timeUnits = TimeUnit.values();
        Set<String> keywords = this.pluralRules.getKeywords();
        for (TimeUnit timeUnit : timeUnits) {
            Map<String, Object[]> countToPatterns = (Map) timeUnitToCountToPatterns.get(timeUnit);
            if (countToPatterns == null) {
                countToPatterns = new TreeMap();
                timeUnitToCountToPatterns.put(timeUnit, countToPatterns);
            }
            for (String pluralCount : keywords) {
                if (countToPatterns.get(pluralCount) == null || ((Object[]) countToPatterns.get(pluralCount))[style] == null) {
                    searchInTree(resourceKey, style, timeUnit, pluralCount, pluralCount, countToPatterns);
                }
            }
        }
    }

    private void searchInTree(String resourceKey, int styl, TimeUnit timeUnit, String srcPluralCount, String searchPluralCount, Map<String, Object[]> countToPatterns) {
        MessageFormat messageFormat;
        Object[] pair;
        ULocale parentLocale = this.locale;
        String srcTimeUnitName = timeUnit.toString();
        while (parentLocale != null) {
            try {
                messageFormat = new MessageFormat(((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, parentLocale)).getWithFallback(resourceKey).getWithFallback(srcTimeUnitName).getStringWithFallback(searchPluralCount), this.locale);
                pair = (Object[]) countToPatterns.get(srcPluralCount);
                if (pair == null) {
                    pair = new Object[2];
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
                if (!(countToPatterns.get(srcPluralCount) == null || ((Object[]) countToPatterns.get(srcPluralCount))[styl] == null)) {
                    return;
                }
            }
        }
        if (searchPluralCount.equals("other")) {
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
                pair = new Object[2];
                countToPatterns.put(srcPluralCount, pair);
            }
            pair[styl] = messageFormat;
        } else {
            searchInTree(resourceKey, styl, timeUnit, srcPluralCount, "other", countToPatterns);
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
