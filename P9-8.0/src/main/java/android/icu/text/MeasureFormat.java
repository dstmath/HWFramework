package android.icu.text;

import android.icu.impl.DontCareFieldPosition;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.text.DateFormat.Field;
import android.icu.text.ListFormatter.Style;
import android.icu.text.PluralRules.Factory;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.ICUException;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;

public class MeasureFormat extends UFormat {
    private static final int CURRENCY_FORMAT = 2;
    private static final int MEASURE_FORMAT = 0;
    private static final int TIME_UNIT_FORMAT = 1;
    private static final Map<MeasureUnit, Integer> hmsTo012 = new HashMap();
    private static final Map<ULocale, String> localeIdToRangeFormat = new ConcurrentHashMap();
    private static final SimpleCache<ULocale, MeasureFormatData> localeMeasureFormatData = new SimpleCache();
    private static final SimpleCache<ULocale, NumericFormatters> localeToNumericDurationFormatters = new SimpleCache();
    static final long serialVersionUID = -7182021401701778240L;
    private final transient MeasureFormatData cache;
    private final transient ImmutableNumberFormat currencyFormat;
    private final transient FormatWidth formatWidth;
    private final transient ImmutableNumberFormat integerFormat;
    private final transient ImmutableNumberFormat numberFormat;
    private final transient NumericFormatters numericFormatters;
    private final transient PluralRules rules;

    public enum FormatWidth {
        WIDE(Style.DURATION, 6),
        SHORT(Style.DURATION_SHORT, 5),
        NARROW(Style.DURATION_NARROW, 1),
        NUMERIC(Style.DURATION_NARROW, 1);
        
        private static final int INDEX_COUNT = 3;
        private final int currencyStyle;
        private final Style listFormatterStyle;

        private FormatWidth(Style style, int currencyStyle) {
            this.listFormatterStyle = style;
            this.currencyStyle = currencyStyle;
        }

        Style getListFormatterStyle() {
            return this.listFormatterStyle;
        }

        int getCurrencyStyle() {
            return this.currencyStyle;
        }
    }

    private static final class ImmutableNumberFormat {
        private NumberFormat nf;

        public ImmutableNumberFormat(NumberFormat nf) {
            this.nf = (NumberFormat) nf.clone();
        }

        public synchronized NumberFormat get() {
            return (NumberFormat) this.nf.clone();
        }

        public synchronized StringBuffer format(Number n, StringBuffer buffer, FieldPosition pos) {
            return this.nf.format((Object) n, buffer, pos);
        }

        public synchronized StringBuffer format(CurrencyAmount n, StringBuffer buffer, FieldPosition pos) {
            return this.nf.format(n, buffer, pos);
        }

        public synchronized String format(Number number) {
            return this.nf.format(number);
        }

        public String getPrefix(boolean positive) {
            return positive ? ((DecimalFormat) this.nf).getPositivePrefix() : ((DecimalFormat) this.nf).getNegativePrefix();
        }

        public String getSuffix(boolean positive) {
            return positive ? ((DecimalFormat) this.nf).getPositiveSuffix() : ((DecimalFormat) this.nf).getNegativeSuffix();
        }
    }

    private static final class MeasureFormatData {
        static final int PATTERN_COUNT = (PER_UNIT_INDEX + 1);
        static final int PER_UNIT_INDEX = StandardPlural.COUNT;
        final EnumMap<FormatWidth, String> styleToPerPattern;
        final Map<MeasureUnit, EnumMap<FormatWidth, String>> unitToStyleToDnam;
        final Map<MeasureUnit, EnumMap<FormatWidth, String[]>> unitToStyleToPatterns;
        final FormatWidth[] widthFallback;

        /* synthetic */ MeasureFormatData(MeasureFormatData -this0) {
            this();
        }

        private MeasureFormatData() {
            this.widthFallback = new FormatWidth[3];
            this.unitToStyleToPatterns = new HashMap();
            this.unitToStyleToDnam = new HashMap();
            this.styleToPerPattern = new EnumMap(FormatWidth.class);
        }

        boolean hasPerFormatter(FormatWidth width) {
            return this.styleToPerPattern.containsKey(width);
        }
    }

    static class MeasureProxy implements Externalizable {
        private static final long serialVersionUID = -6033308329886716770L;
        private FormatWidth formatWidth;
        private HashMap<Object, Object> keyValues;
        private ULocale locale;
        private NumberFormat numberFormat;
        private int subClass;

        public MeasureProxy(ULocale locale, FormatWidth width, NumberFormat numberFormat, int subClass) {
            this.locale = locale;
            this.formatWidth = width;
            this.numberFormat = numberFormat;
            this.subClass = subClass;
            this.keyValues = new HashMap();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeByte(0);
            out.writeUTF(this.locale.toLanguageTag());
            out.writeByte(this.formatWidth.ordinal());
            out.writeObject(this.numberFormat);
            out.writeByte(this.subClass);
            out.writeObject(this.keyValues);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readByte();
            this.locale = ULocale.forLanguageTag(in.readUTF());
            this.formatWidth = MeasureFormat.fromFormatWidthOrdinal(in.readByte() & 255);
            this.numberFormat = (NumberFormat) in.readObject();
            if (this.numberFormat == null) {
                throw new InvalidObjectException("Missing number format.");
            }
            this.subClass = in.readByte() & 255;
            this.keyValues = (HashMap) in.readObject();
            if (this.keyValues == null) {
                throw new InvalidObjectException("Missing optional values map.");
            }
        }

        private TimeUnitFormat createTimeUnitFormat() throws InvalidObjectException {
            int style;
            if (this.formatWidth == FormatWidth.WIDE) {
                style = 0;
            } else if (this.formatWidth == FormatWidth.SHORT) {
                style = 1;
            } else {
                throw new InvalidObjectException("Bad width: " + this.formatWidth);
            }
            TimeUnitFormat result = new TimeUnitFormat(this.locale, style);
            result.setNumberFormat(this.numberFormat);
            return result;
        }

        private Object readResolve() throws ObjectStreamException {
            switch (this.subClass) {
                case 0:
                    return MeasureFormat.getInstance(this.locale, this.formatWidth, this.numberFormat);
                case 1:
                    return createTimeUnitFormat();
                case 2:
                    return new CurrencyFormat(this.locale);
                default:
                    throw new InvalidObjectException("Unknown subclass: " + this.subClass);
            }
        }
    }

    static class NumericFormatters {
        private DateFormat hourMinute;
        private DateFormat hourMinuteSecond;
        private DateFormat minuteSecond;

        public NumericFormatters(DateFormat hourMinute, DateFormat minuteSecond, DateFormat hourMinuteSecond) {
            this.hourMinute = hourMinute;
            this.minuteSecond = minuteSecond;
            this.hourMinuteSecond = hourMinuteSecond;
        }

        public DateFormat getHourMinute() {
            return this.hourMinute;
        }

        public DateFormat getMinuteSecond() {
            return this.minuteSecond;
        }

        public DateFormat getHourMinuteSecond() {
            return this.hourMinuteSecond;
        }
    }

    static final class PatternData {
        final String prefix;
        final String suffix;

        public PatternData(String pattern) {
            int pos = pattern.indexOf("{0}");
            if (pos < 0) {
                this.prefix = pattern;
                this.suffix = null;
                return;
            }
            this.prefix = pattern.substring(0, pos);
            this.suffix = pattern.substring(pos + 3);
        }

        public String toString() {
            return this.prefix + "; " + this.suffix;
        }
    }

    private static final class UnitDataSink extends Sink {
        MeasureFormatData cacheData;
        String[] patterns;
        StringBuilder sb = new StringBuilder();
        String type;
        MeasureUnit unit;
        FormatWidth width;

        void setFormatterIfAbsent(int index, Value value, int minPlaceholders) {
            if (this.patterns == null) {
                EnumMap<FormatWidth, String[]> styleToPatterns = (EnumMap) this.cacheData.unitToStyleToPatterns.get(this.unit);
                if (styleToPatterns == null) {
                    styleToPatterns = new EnumMap(FormatWidth.class);
                    this.cacheData.unitToStyleToPatterns.put(this.unit, styleToPatterns);
                } else {
                    this.patterns = (String[]) styleToPatterns.get(this.width);
                }
                if (this.patterns == null) {
                    this.patterns = new String[MeasureFormatData.PATTERN_COUNT];
                    styleToPatterns.put(this.width, this.patterns);
                }
            }
            if (this.patterns[index] == null) {
                this.patterns[index] = SimpleFormatterImpl.compileToStringMinMaxArguments(value.getString(), this.sb, minPlaceholders, 1);
            }
        }

        void setDnamIfAbsent(Value value) {
            EnumMap<FormatWidth, String> styleToDnam = (EnumMap) this.cacheData.unitToStyleToDnam.get(this.unit);
            if (styleToDnam == null) {
                styleToDnam = new EnumMap(FormatWidth.class);
                this.cacheData.unitToStyleToDnam.put(this.unit, styleToDnam);
            }
            if (styleToDnam.get(this.width) == null) {
                styleToDnam.put(this.width, value.getString());
            }
        }

        void consumePattern(Key key, Value value) {
            if (key.contentEquals("dnam")) {
                setDnamIfAbsent(value);
            } else if (key.contentEquals("per")) {
                setFormatterIfAbsent(MeasureFormatData.PER_UNIT_INDEX, value, 1);
            } else {
                setFormatterIfAbsent(StandardPlural.indexFromString(key), value, 0);
            }
        }

        void consumeSubtypeTable(Key key, Value value) {
            this.unit = MeasureUnit.internalGetInstance(this.type, key.toString());
            this.patterns = null;
            if (value.getType() == 0) {
                setFormatterIfAbsent(StandardPlural.OTHER.ordinal(), value, 0);
            } else if (value.getType() == 2) {
                Table patternTableTable = value.getTable();
                for (int i = 0; patternTableTable.getKeyAndValue(i, key, value); i++) {
                    consumePattern(key, value);
                }
            } else {
                throw new ICUException("Data for unit '" + this.unit + "' is in an unknown format");
            }
        }

        void consumeCompoundPattern(Key key, Value value) {
            if (key.contentEquals("per")) {
                this.cacheData.styleToPerPattern.put(this.width, SimpleFormatterImpl.compileToStringMinMaxArguments(value.getString(), this.sb, 2, 2));
            }
        }

        void consumeUnitTypesTable(Key key, Value value) {
            if (!key.contentEquals("currency")) {
                int i;
                if (!key.contentEquals("compound")) {
                    this.type = key.toString();
                    Table subtypeTable = value.getTable();
                    for (i = 0; subtypeTable.getKeyAndValue(i, key, value); i++) {
                        consumeSubtypeTable(key, value);
                    }
                } else if (!this.cacheData.hasPerFormatter(this.width)) {
                    Table compoundTable = value.getTable();
                    for (i = 0; compoundTable.getKeyAndValue(i, key, value); i++) {
                        consumeCompoundPattern(key, value);
                    }
                }
            }
        }

        UnitDataSink(MeasureFormatData outputData) {
            this.cacheData = outputData;
        }

        void consumeAlias(Key key, Value value) {
            FormatWidth sourceWidth = widthFromKey(key);
            if (sourceWidth != null) {
                FormatWidth targetWidth = widthFromAlias(value);
                if (targetWidth == null) {
                    throw new ICUException("Units data fallback from " + key + " to unknown " + value.getAliasString());
                } else if (this.cacheData.widthFallback[targetWidth.ordinal()] != null) {
                    throw new ICUException("Units data fallback from " + key + " to " + value.getAliasString() + " which falls back to something else");
                } else {
                    this.cacheData.widthFallback[sourceWidth.ordinal()] = targetWidth;
                }
            }
        }

        public void consumeTable(Key key, Value value) {
            FormatWidth widthFromKey = widthFromKey(key);
            this.width = widthFromKey;
            if (widthFromKey != null) {
                Table unitTypesTable = value.getTable();
                for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                    consumeUnitTypesTable(key, value);
                }
            }
        }

        static FormatWidth widthFromKey(Key key) {
            if (key.startsWith("units")) {
                if (key.length() == 5) {
                    return FormatWidth.WIDE;
                }
                if (key.regionMatches(5, "Short")) {
                    return FormatWidth.SHORT;
                }
                if (key.regionMatches(5, "Narrow")) {
                    return FormatWidth.NARROW;
                }
            }
            return null;
        }

        static FormatWidth widthFromAlias(Value value) {
            String s = value.getAliasString();
            if (s.startsWith("/LOCALE/units")) {
                if (s.length() == 13) {
                    return FormatWidth.WIDE;
                }
                if (s.length() == 18 && s.endsWith("Short")) {
                    return FormatWidth.SHORT;
                }
                if (s.length() == 19 && s.endsWith("Narrow")) {
                    return FormatWidth.NARROW;
                }
            }
            return null;
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table widthsTable = value.getTable();
            for (int i = 0; widthsTable.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == 3) {
                    consumeAlias(key, value);
                } else {
                    consumeTable(key, value);
                }
            }
        }
    }

    static {
        hmsTo012.put(MeasureUnit.HOUR, Integer.valueOf(0));
        hmsTo012.put(MeasureUnit.MINUTE, Integer.valueOf(1));
        hmsTo012.put(MeasureUnit.SECOND, Integer.valueOf(2));
    }

    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth) {
        return getInstance(locale, formatWidth, NumberFormat.getInstance(locale));
    }

    public static MeasureFormat getInstance(Locale locale, FormatWidth formatWidth) {
        return getInstance(ULocale.forLocale(locale), formatWidth);
    }

    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth, NumberFormat format) {
        PluralRules rules = PluralRules.forLocale(locale);
        NumericFormatters formatters = null;
        MeasureFormatData data = (MeasureFormatData) localeMeasureFormatData.get(locale);
        if (data == null) {
            data = loadLocaleData(locale);
            localeMeasureFormatData.put(locale, data);
        }
        if (formatWidth == FormatWidth.NUMERIC) {
            formatters = (NumericFormatters) localeToNumericDurationFormatters.get(locale);
            if (formatters == null) {
                formatters = loadNumericFormatters(locale);
                localeToNumericDurationFormatters.put(locale, formatters);
            }
        }
        NumberFormat intFormat = NumberFormat.getInstance(locale);
        intFormat.setMaximumFractionDigits(0);
        intFormat.setMinimumFractionDigits(0);
        intFormat.setRoundingMode(1);
        return new MeasureFormat(locale, data, formatWidth, new ImmutableNumberFormat(format), rules, formatters, new ImmutableNumberFormat(NumberFormat.getInstance(locale, formatWidth.getCurrencyStyle())), new ImmutableNumberFormat(intFormat));
    }

    public static MeasureFormat getInstance(Locale locale, FormatWidth formatWidth, NumberFormat format) {
        return getInstance(ULocale.forLocale(locale), formatWidth, format);
    }

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        int prevLength = toAppendTo.length();
        FieldPosition fpos = new FieldPosition(pos.getFieldAttribute(), pos.getField());
        if (obj instanceof Collection) {
            Collection<?> coll = (Collection) obj;
            Measure[] measures = new Measure[coll.size()];
            int idx = 0;
            for (Object o : coll) {
                if (o instanceof Measure) {
                    int idx2 = idx + 1;
                    measures[idx] = (Measure) o;
                    idx = idx2;
                } else {
                    throw new IllegalArgumentException(obj.toString());
                }
            }
            toAppendTo.append(formatMeasures(new StringBuilder(), fpos, measures));
        } else if (obj instanceof Measure[]) {
            toAppendTo.append(formatMeasures(new StringBuilder(), fpos, (Measure[]) obj));
        } else if (obj instanceof Measure) {
            toAppendTo.append(formatMeasure((Measure) obj, this.numberFormat, new StringBuilder(), fpos));
        } else {
            throw new IllegalArgumentException(obj.toString());
        }
        if (!(fpos.getBeginIndex() == 0 && fpos.getEndIndex() == 0)) {
            pos.setBeginIndex(fpos.getBeginIndex() + prevLength);
            pos.setEndIndex(fpos.getEndIndex() + prevLength);
        }
        return toAppendTo;
    }

    public Measure parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    public final String formatMeasures(Measure... measures) {
        return formatMeasures(new StringBuilder(), DontCareFieldPosition.INSTANCE, measures).toString();
    }

    @Deprecated
    public final String formatMeasureRange(Measure lowValue, Measure highValue) {
        MeasureUnit unit = lowValue.getUnit();
        if (unit.equals(highValue.getUnit())) {
            Number lowNumber = lowValue.getNumber();
            Number highNumber = highValue.getNumber();
            boolean isCurrency = unit instanceof Currency;
            FieldPosition lowFpos = new UFieldPosition();
            FieldPosition highFpos = new UFieldPosition();
            StringBuffer lowFormatted = null;
            StringBuffer highFormatted = null;
            if (isCurrency) {
                int fracDigits = ((Currency) unit).getDefaultFractionDigits();
                int maxFrac = this.numberFormat.nf.getMaximumFractionDigits();
                int minFrac = this.numberFormat.nf.getMinimumFractionDigits();
                if (!(fracDigits == maxFrac && fracDigits == minFrac)) {
                    DecimalFormat currentNumberFormat = (DecimalFormat) this.numberFormat.get();
                    currentNumberFormat.setMaximumFractionDigits(fracDigits);
                    currentNumberFormat.setMinimumFractionDigits(fracDigits);
                    lowFormatted = currentNumberFormat.format((Object) lowNumber, new StringBuffer(), lowFpos);
                    highFormatted = currentNumberFormat.format((Object) highNumber, new StringBuffer(), highFpos);
                }
            }
            if (lowFormatted == null) {
                lowFormatted = this.numberFormat.format(lowNumber, new StringBuffer(), lowFpos);
                highFormatted = this.numberFormat.format(highNumber, new StringBuffer(), highFpos);
            }
            double lowDouble = lowNumber.doubleValue();
            String keywordLow = this.rules.select(new FixedDecimal(lowDouble, lowFpos.getCountVisibleFractionDigits(), lowFpos.getFractionDigits()));
            double highDouble = highNumber.doubleValue();
            StandardPlural resolvedPlural = Factory.getDefaultFactory().getPluralRanges(getLocale()).get(StandardPlural.fromString(keywordLow), StandardPlural.fromString(this.rules.select(new FixedDecimal(highDouble, highFpos.getCountVisibleFractionDigits(), highFpos.getFractionDigits()))));
            String formattedNumber = SimpleFormatterImpl.formatCompiledPattern(getRangeFormat(getLocale(), this.formatWidth), lowFormatted, highFormatted);
            if (isCurrency) {
                this.currencyFormat.format(Double.valueOf(1.0d));
                Currency currencyUnit = (Currency) unit;
                StringBuilder result = new StringBuilder();
                appendReplacingCurrency(this.currencyFormat.getPrefix(lowDouble >= 0.0d), currencyUnit, resolvedPlural, result);
                result.append(formattedNumber);
                appendReplacingCurrency(this.currencyFormat.getSuffix(highDouble >= 0.0d), currencyUnit, resolvedPlural, result);
                return result.toString();
            }
            return SimpleFormatterImpl.formatCompiledPattern(getPluralFormatter(lowValue.getUnit(), this.formatWidth, resolvedPlural.ordinal()), formattedNumber);
        }
        throw new IllegalArgumentException("Units must match: " + unit + " ≠ " + highValue.getUnit());
    }

    private void appendReplacingCurrency(String affix, Currency unit, StandardPlural resolvedPlural, StringBuilder result) {
        int i = 0;
        String replacement = "¤";
        int pos = affix.indexOf(replacement);
        if (pos < 0) {
            replacement = "XXX";
            pos = affix.indexOf(replacement);
        }
        if (pos < 0) {
            result.append(affix);
            return;
        }
        result.append(affix.substring(0, pos));
        int currentStyle = this.formatWidth.getCurrencyStyle();
        if (currentStyle == 5) {
            result.append(unit.getCurrencyCode());
        } else {
            ULocale locale = this.currencyFormat.nf.getLocale(ULocale.ACTUAL_LOCALE);
            if (currentStyle != 1) {
                i = 2;
            }
            result.append(unit.getName(locale, i, resolvedPlural.getKeyword(), null));
        }
        result.append(affix.substring(replacement.length() + pos));
    }

    public StringBuilder formatMeasurePerUnit(Measure measure, MeasureUnit perUnit, StringBuilder appendTo, FieldPosition pos) {
        MeasureUnit resolvedUnit = MeasureUnit.resolveUnitPerUnit(measure.getUnit(), perUnit);
        if (resolvedUnit != null) {
            return formatMeasure(new Measure(measure.getNumber(), resolvedUnit), this.numberFormat, appendTo, pos);
        }
        FieldPosition fpos = new FieldPosition(pos.getFieldAttribute(), pos.getField());
        int offset = withPerUnitAndAppend(formatMeasure(measure, this.numberFormat, new StringBuilder(), fpos), perUnit, appendTo);
        if (!(fpos.getBeginIndex() == 0 && fpos.getEndIndex() == 0)) {
            pos.setBeginIndex(fpos.getBeginIndex() + offset);
            pos.setEndIndex(fpos.getEndIndex() + offset);
        }
        return appendTo;
    }

    public StringBuilder formatMeasures(StringBuilder appendTo, FieldPosition fieldPosition, Measure... measures) {
        if (measures.length == 0) {
            return appendTo;
        }
        if (measures.length == 1) {
            return formatMeasure(measures[0], this.numberFormat, appendTo, fieldPosition);
        }
        if (this.formatWidth == FormatWidth.NUMERIC) {
            Number[] hms = toHMS(measures);
            if (hms != null) {
                return formatNumeric(hms, appendTo);
            }
        }
        ListFormatter listFormatter = ListFormatter.getInstance(getLocale(), this.formatWidth.getListFormatterStyle());
        if (fieldPosition != DontCareFieldPosition.INSTANCE) {
            return formatMeasuresSlowTrack(listFormatter, appendTo, fieldPosition, measures);
        }
        Object[] results = new String[measures.length];
        int i = 0;
        while (i < measures.length) {
            results[i] = formatMeasure(measures[i], i == measures.length + -1 ? this.numberFormat : this.integerFormat);
            i++;
        }
        return appendTo.append(listFormatter.format(results));
    }

    public String getUnitDisplayName(MeasureUnit unit) {
        FormatWidth width = getRegularWidth(this.formatWidth);
        Map<FormatWidth, String> styleToDnam = (Map) this.cache.unitToStyleToDnam.get(unit);
        if (styleToDnam == null) {
            return null;
        }
        String dnam = (String) styleToDnam.get(width);
        if (dnam != null) {
            return dnam;
        }
        FormatWidth fallbackWidth = this.cache.widthFallback[width.ordinal()];
        if (fallbackWidth != null) {
            dnam = (String) styleToDnam.get(fallbackWidth);
        }
        return dnam;
    }

    public final boolean equals(Object other) {
        boolean z = false;
        if (this == other) {
            return true;
        }
        if (!(other instanceof MeasureFormat)) {
            return false;
        }
        MeasureFormat rhs = (MeasureFormat) other;
        if (getWidth() == rhs.getWidth() && getLocale().equals(rhs.getLocale())) {
            z = getNumberFormat().equals(rhs.getNumberFormat());
        }
        return z;
    }

    public final int hashCode() {
        return (((getLocale().hashCode() * 31) + getNumberFormat().hashCode()) * 31) + getWidth().hashCode();
    }

    public FormatWidth getWidth() {
        return this.formatWidth;
    }

    public final ULocale getLocale() {
        return getLocale(ULocale.VALID_LOCALE);
    }

    public NumberFormat getNumberFormat() {
        return this.numberFormat.get();
    }

    public static MeasureFormat getCurrencyFormat(ULocale locale) {
        return new CurrencyFormat(locale);
    }

    public static MeasureFormat getCurrencyFormat(Locale locale) {
        return getCurrencyFormat(ULocale.forLocale(locale));
    }

    public static MeasureFormat getCurrencyFormat() {
        return getCurrencyFormat(ULocale.getDefault(Category.FORMAT));
    }

    MeasureFormat withLocale(ULocale locale) {
        return getInstance(locale, getWidth());
    }

    MeasureFormat withNumberFormat(NumberFormat format) {
        return new MeasureFormat(getLocale(), this.cache, this.formatWidth, new ImmutableNumberFormat(format), this.rules, this.numericFormatters, this.currencyFormat, this.integerFormat);
    }

    private MeasureFormat(ULocale locale, MeasureFormatData data, FormatWidth formatWidth, ImmutableNumberFormat format, PluralRules rules, NumericFormatters formatters, ImmutableNumberFormat currencyFormat, ImmutableNumberFormat integerFormat) {
        setLocale(locale, locale);
        this.cache = data;
        this.formatWidth = formatWidth;
        this.numberFormat = format;
        this.rules = rules;
        this.numericFormatters = formatters;
        this.currencyFormat = currencyFormat;
        this.integerFormat = integerFormat;
    }

    MeasureFormat() {
        this.cache = null;
        this.formatWidth = null;
        this.numberFormat = null;
        this.rules = null;
        this.numericFormatters = null;
        this.currencyFormat = null;
        this.integerFormat = null;
    }

    private static NumericFormatters loadNumericFormatters(ULocale locale) {
        ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, locale);
        return new NumericFormatters(loadNumericDurationFormat(r, "hm"), loadNumericDurationFormat(r, DateFormat.MINUTE_SECOND), loadNumericDurationFormat(r, "hms"));
    }

    private static MeasureFormatData loadLocaleData(ULocale locale) {
        ICUResourceBundle resource = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, locale);
        MeasureFormatData cacheData = new MeasureFormatData();
        resource.getAllItemsWithFallback("", new UnitDataSink(cacheData));
        return cacheData;
    }

    private static final FormatWidth getRegularWidth(FormatWidth width) {
        if (width == FormatWidth.NUMERIC) {
            return FormatWidth.NARROW;
        }
        return width;
    }

    private String getFormatterOrNull(MeasureUnit unit, FormatWidth width, int index) {
        width = getRegularWidth(width);
        Map<FormatWidth, String[]> styleToPatterns = (Map) this.cache.unitToStyleToPatterns.get(unit);
        String[] patterns = (String[]) styleToPatterns.get(width);
        if (patterns != null && patterns[index] != null) {
            return patterns[index];
        }
        FormatWidth fallbackWidth = this.cache.widthFallback[width.ordinal()];
        if (fallbackWidth != null) {
            patterns = (String[]) styleToPatterns.get(fallbackWidth);
            if (!(patterns == null || patterns[index] == null)) {
                return patterns[index];
            }
        }
        return null;
    }

    private String getFormatter(MeasureUnit unit, FormatWidth width, int index) {
        String pattern = getFormatterOrNull(unit, width, index);
        if (pattern != null) {
            return pattern;
        }
        throw new MissingResourceException("no formatting pattern for " + unit + ", width " + width + ", index " + index, null, null);
    }

    private String getPluralFormatter(MeasureUnit unit, FormatWidth width, int index) {
        if (index != StandardPlural.OTHER_INDEX) {
            String pattern = getFormatterOrNull(unit, width, index);
            if (pattern != null) {
                return pattern;
            }
        }
        return getFormatter(unit, width, StandardPlural.OTHER_INDEX);
    }

    private String getPerFormatter(FormatWidth width) {
        width = getRegularWidth(width);
        String perPattern = (String) this.cache.styleToPerPattern.get(width);
        if (perPattern != null) {
            return perPattern;
        }
        FormatWidth fallbackWidth = this.cache.widthFallback[width.ordinal()];
        if (fallbackWidth != null) {
            perPattern = (String) this.cache.styleToPerPattern.get(fallbackWidth);
            if (perPattern != null) {
                return perPattern;
            }
        }
        throw new MissingResourceException("no x-per-y pattern for width " + width, null, null);
    }

    private int withPerUnitAndAppend(CharSequence formatted, MeasureUnit perUnit, StringBuilder appendTo) {
        int[] offsets = new int[1];
        String perUnitPattern = getFormatterOrNull(perUnit, this.formatWidth, MeasureFormatData.PER_UNIT_INDEX);
        if (perUnitPattern != null) {
            SimpleFormatterImpl.formatAndAppend(perUnitPattern, appendTo, offsets, formatted);
            return offsets[0];
        }
        String perPattern = getPerFormatter(this.formatWidth);
        String perUnitString = SimpleFormatterImpl.getTextWithNoArguments(getPluralFormatter(perUnit, this.formatWidth, StandardPlural.ONE.ordinal())).trim();
        SimpleFormatterImpl.formatAndAppend(perPattern, appendTo, offsets, formatted, perUnitString);
        return offsets[0];
    }

    private String formatMeasure(Measure measure, ImmutableNumberFormat nf) {
        return formatMeasure(measure, nf, new StringBuilder(), DontCareFieldPosition.INSTANCE).toString();
    }

    private StringBuilder formatMeasure(Measure measure, ImmutableNumberFormat nf, StringBuilder appendTo, FieldPosition fieldPosition) {
        Number n = measure.getNumber();
        MeasureUnit unit = measure.getUnit();
        if (unit instanceof Currency) {
            return appendTo.append(this.currencyFormat.format(new CurrencyAmount(n, (Currency) unit), new StringBuffer(), fieldPosition));
        }
        StringBuffer formattedNumber = new StringBuffer();
        return QuantityFormatter.format(getPluralFormatter(unit, this.formatWidth, QuantityFormatter.selectPlural(n, nf.nf, this.rules, formattedNumber, fieldPosition).ordinal()), formattedNumber, appendTo, fieldPosition);
    }

    Object toTimeUnitProxy() {
        return new MeasureProxy(getLocale(), this.formatWidth, this.numberFormat.get(), 1);
    }

    Object toCurrencyProxy() {
        return new MeasureProxy(getLocale(), this.formatWidth, this.numberFormat.get(), 2);
    }

    private StringBuilder formatMeasuresSlowTrack(ListFormatter listFormatter, StringBuilder appendTo, FieldPosition fieldPosition, Measure... measures) {
        String[] results = new String[measures.length];
        FieldPosition fpos = new FieldPosition(fieldPosition.getFieldAttribute(), fieldPosition.getField());
        int fieldPositionFoundIndex = -1;
        int i = 0;
        while (i < measures.length) {
            ImmutableNumberFormat nf = i == measures.length + -1 ? this.numberFormat : this.integerFormat;
            if (fieldPositionFoundIndex == -1) {
                results[i] = formatMeasure(measures[i], nf, new StringBuilder(), fpos).toString();
                if (fpos.getBeginIndex() != 0 || fpos.getEndIndex() != 0) {
                    fieldPositionFoundIndex = i;
                }
            } else {
                results[i] = formatMeasure(measures[i], nf);
            }
            i++;
        }
        FormattedListBuilder builder = listFormatter.format(Arrays.asList(results), fieldPositionFoundIndex);
        if (builder.getOffset() != -1) {
            fieldPosition.setBeginIndex((fpos.getBeginIndex() + builder.getOffset()) + appendTo.length());
            fieldPosition.setEndIndex((fpos.getEndIndex() + builder.getOffset()) + appendTo.length());
        }
        return appendTo.append(builder.toString());
    }

    private static DateFormat loadNumericDurationFormat(ICUResourceBundle r, String type) {
        DateFormat result = new SimpleDateFormat(r.getWithFallback(String.format("durationUnits/%s", new Object[]{type})).getString().replace("h", DateFormat.HOUR24));
        result.setTimeZone(TimeZone.GMT_ZONE);
        return result;
    }

    private static Number[] toHMS(Measure[] measures) {
        Number[] result = new Number[3];
        int lastIdx = -1;
        for (Measure m : measures) {
            if (m.getNumber().doubleValue() < 0.0d) {
                return null;
            }
            Integer idxObj = (Integer) hmsTo012.get(m.getUnit());
            if (idxObj == null) {
                return null;
            }
            int idx = idxObj.intValue();
            if (idx <= lastIdx) {
                return null;
            }
            lastIdx = idx;
            result[idx] = m.getNumber();
        }
        return result;
    }

    private StringBuilder formatNumeric(Number[] hms, StringBuilder appendable) {
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < hms.length; i++) {
            if (hms[i] != null) {
                endIndex = i;
                if (startIndex == -1) {
                    startIndex = endIndex;
                }
            } else {
                hms[i] = Integer.valueOf(0);
            }
        }
        Date d = new Date((long) (((((Math.floor(hms[0].doubleValue()) * 60.0d) + Math.floor(hms[1].doubleValue())) * 60.0d) + Math.floor(hms[2].doubleValue())) * 1000.0d));
        if (startIndex == 0 && endIndex == 2) {
            return formatNumeric(d, this.numericFormatters.getHourMinuteSecond(), Field.SECOND, hms[endIndex], appendable);
        } else if (startIndex == 1 && endIndex == 2) {
            return formatNumeric(d, this.numericFormatters.getMinuteSecond(), Field.SECOND, hms[endIndex], appendable);
        } else if (startIndex == 0 && endIndex == 1) {
            return formatNumeric(d, this.numericFormatters.getHourMinute(), Field.MINUTE, hms[endIndex], appendable);
        } else {
            throw new IllegalStateException();
        }
    }

    private StringBuilder formatNumeric(Date duration, DateFormat formatter, Field smallestField, Number smallestAmount, StringBuilder appendTo) {
        FieldPosition intFieldPosition = new FieldPosition(0);
        String smallestAmountFormatted = this.numberFormat.format(smallestAmount, new StringBuffer(), intFieldPosition).toString();
        if (intFieldPosition.getBeginIndex() == 0 && intFieldPosition.getEndIndex() == 0) {
            throw new IllegalStateException();
        }
        FieldPosition smallestFieldPosition = new FieldPosition(smallestField);
        String draft = formatter.format(duration, new StringBuffer(), smallestFieldPosition).toString();
        if (smallestFieldPosition.getBeginIndex() == 0 && smallestFieldPosition.getEndIndex() == 0) {
            appendTo.append(draft);
        } else {
            appendTo.append(draft, 0, smallestFieldPosition.getBeginIndex());
            appendTo.append(smallestAmountFormatted, 0, intFieldPosition.getBeginIndex());
            appendTo.append(draft, smallestFieldPosition.getBeginIndex(), smallestFieldPosition.getEndIndex());
            appendTo.append(smallestAmountFormatted, intFieldPosition.getEndIndex(), smallestAmountFormatted.length());
            appendTo.append(draft, smallestFieldPosition.getEndIndex(), draft.length());
        }
        return appendTo;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureProxy(getLocale(), this.formatWidth, this.numberFormat.get(), 0);
    }

    private static FormatWidth fromFormatWidthOrdinal(int ordinal) {
        FormatWidth[] values = FormatWidth.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return FormatWidth.SHORT;
        }
        return values[ordinal];
    }

    @Deprecated
    public static String getRangeFormat(ULocale forLocale, FormatWidth width) {
        if (forLocale.getLanguage().equals("fr")) {
            return getRangeFormat(ULocale.ROOT, width);
        }
        String result = (String) localeIdToRangeFormat.get(forLocale);
        if (result == null) {
            String resultString;
            ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, forLocale);
            ULocale realLocale = rb.getULocale();
            if (!forLocale.equals(realLocale)) {
                result = (String) localeIdToRangeFormat.get(forLocale);
                if (result != null) {
                    localeIdToRangeFormat.put(forLocale, result);
                    return result;
                }
            }
            try {
                resultString = rb.getStringWithFallback("NumberElements/" + NumberingSystem.getInstance(forLocale).getName() + "/miscPatterns/range");
            } catch (MissingResourceException e) {
                resultString = rb.getStringWithFallback("NumberElements/latn/patterns/range");
            }
            result = SimpleFormatterImpl.compileToStringMinMaxArguments(resultString, new StringBuilder(), 2, 2);
            localeIdToRangeFormat.put(forLocale, result);
            if (!forLocale.equals(realLocale)) {
                localeIdToRangeFormat.put(realLocale, result);
            }
        }
        return result;
    }
}
