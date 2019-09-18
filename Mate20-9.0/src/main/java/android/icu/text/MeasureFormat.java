package android.icu.text;

import android.icu.impl.DontCareFieldPosition;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource;
import android.icu.lang.UCharacterEnums;
import android.icu.text.DateFormat;
import android.icu.text.ListFormatter;
import android.icu.text.PluralRules;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.ICUException;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
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
    private static final SimpleCache<ULocale, MeasureFormatData> localeMeasureFormatData = new SimpleCache<>();
    private static final SimpleCache<ULocale, NumericFormatters> localeToNumericDurationFormatters = new SimpleCache<>();
    static final long serialVersionUID = -7182021401701778240L;
    private final transient MeasureFormatData cache;
    private final transient ImmutableNumberFormat currencyFormat;
    private final transient FormatWidth formatWidth;
    private final transient ImmutableNumberFormat integerFormat;
    private final transient ImmutableNumberFormat numberFormat;
    private final transient NumericFormatters numericFormatters;
    private final transient PluralRules rules;

    public enum FormatWidth {
        WIDE(ListFormatter.Style.DURATION, 6),
        SHORT(ListFormatter.Style.DURATION_SHORT, 5),
        NARROW(ListFormatter.Style.DURATION_NARROW, 1),
        NUMERIC(ListFormatter.Style.DURATION_NARROW, 1);
        
        private static final int INDEX_COUNT = 3;
        private final int currencyStyle;
        private final ListFormatter.Style listFormatterStyle;

        private FormatWidth(ListFormatter.Style style, int currencyStyle2) {
            this.listFormatterStyle = style;
            this.currencyStyle = currencyStyle2;
        }

        /* access modifiers changed from: package-private */
        public ListFormatter.Style getListFormatterStyle() {
            return this.listFormatterStyle;
        }

        /* access modifiers changed from: package-private */
        public int getCurrencyStyle() {
            return this.currencyStyle;
        }
    }

    private static final class ImmutableNumberFormat {
        /* access modifiers changed from: private */
        public NumberFormat nf;

        public ImmutableNumberFormat(NumberFormat nf2) {
            this.nf = (NumberFormat) nf2.clone();
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

        private MeasureFormatData() {
            this.widthFallback = new FormatWidth[3];
            this.unitToStyleToPatterns = new HashMap();
            this.unitToStyleToDnam = new HashMap();
            this.styleToPerPattern = new EnumMap<>(FormatWidth.class);
        }

        /* access modifiers changed from: package-private */
        public boolean hasPerFormatter(FormatWidth width) {
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

        public MeasureProxy(ULocale locale2, FormatWidth width, NumberFormat numberFormat2, int subClass2) {
            this.locale = locale2;
            this.formatWidth = width;
            this.numberFormat = numberFormat2;
            this.subClass = subClass2;
            this.keyValues = new HashMap<>();
        }

        public MeasureProxy() {
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
            this.formatWidth = MeasureFormat.fromFormatWidthOrdinal(in.readByte() & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED);
            this.numberFormat = (NumberFormat) in.readObject();
            if (this.numberFormat != null) {
                this.subClass = in.readByte() & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED;
                this.keyValues = (HashMap) in.readObject();
                if (this.keyValues == null) {
                    throw new InvalidObjectException("Missing optional values map.");
                }
                return;
            }
            throw new InvalidObjectException("Missing number format.");
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

        public NumericFormatters(DateFormat hourMinute2, DateFormat minuteSecond2, DateFormat hourMinuteSecond2) {
            this.hourMinute = hourMinute2;
            this.minuteSecond = minuteSecond2;
            this.hourMinuteSecond = hourMinuteSecond2;
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

    private static final class UnitDataSink extends UResource.Sink {
        MeasureFormatData cacheData;
        String[] patterns;
        StringBuilder sb = new StringBuilder();
        String type;
        MeasureUnit unit;
        FormatWidth width;

        /* access modifiers changed from: package-private */
        public void setFormatterIfAbsent(int index, UResource.Value value, int minPlaceholders) {
            if (this.patterns == null) {
                EnumMap<FormatWidth, String[]> styleToPatterns = this.cacheData.unitToStyleToPatterns.get(this.unit);
                if (styleToPatterns == null) {
                    styleToPatterns = new EnumMap<>(FormatWidth.class);
                    this.cacheData.unitToStyleToPatterns.put(this.unit, styleToPatterns);
                } else {
                    this.patterns = styleToPatterns.get(this.width);
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

        /* access modifiers changed from: package-private */
        public void setDnamIfAbsent(UResource.Value value) {
            EnumMap<FormatWidth, String> styleToDnam = this.cacheData.unitToStyleToDnam.get(this.unit);
            if (styleToDnam == null) {
                styleToDnam = new EnumMap<>(FormatWidth.class);
                this.cacheData.unitToStyleToDnam.put(this.unit, styleToDnam);
            }
            if (styleToDnam.get(this.width) == null) {
                styleToDnam.put(this.width, value.getString());
            }
        }

        /* access modifiers changed from: package-private */
        public void consumePattern(UResource.Key key, UResource.Value value) {
            if (key.contentEquals("dnam")) {
                setDnamIfAbsent(value);
            } else if (key.contentEquals("per")) {
                setFormatterIfAbsent(MeasureFormatData.PER_UNIT_INDEX, value, 1);
            } else {
                setFormatterIfAbsent(StandardPlural.indexFromString(key), value, 0);
            }
        }

        /* access modifiers changed from: package-private */
        public void consumeSubtypeTable(UResource.Key key, UResource.Value value) {
            this.unit = MeasureUnit.internalGetInstance(this.type, key.toString());
            this.patterns = null;
            if (value.getType() == 2) {
                UResource.Table patternTableTable = value.getTable();
                for (int i = 0; patternTableTable.getKeyAndValue(i, key, value); i++) {
                    consumePattern(key, value);
                }
                return;
            }
            throw new ICUException("Data for unit '" + this.unit + "' is in an unknown format");
        }

        /* access modifiers changed from: package-private */
        public void consumeCompoundPattern(UResource.Key key, UResource.Value value) {
            if (key.contentEquals("per")) {
                this.cacheData.styleToPerPattern.put(this.width, SimpleFormatterImpl.compileToStringMinMaxArguments(value.getString(), this.sb, 2, 2));
            }
        }

        /* access modifiers changed from: package-private */
        public void consumeUnitTypesTable(UResource.Key key, UResource.Value value) {
            if (!key.contentEquals("currency")) {
                int i = 0;
                if (key.contentEquals("compound")) {
                    if (!this.cacheData.hasPerFormatter(this.width)) {
                        UResource.Table compoundTable = value.getTable();
                        while (compoundTable.getKeyAndValue(i, key, value)) {
                            consumeCompoundPattern(key, value);
                            i++;
                        }
                    }
                } else if (!key.contentEquals("coordinate")) {
                    this.type = key.toString();
                    UResource.Table subtypeTable = value.getTable();
                    while (subtypeTable.getKeyAndValue(i, key, value)) {
                        consumeSubtypeTable(key, value);
                        i++;
                    }
                }
            }
        }

        UnitDataSink(MeasureFormatData outputData) {
            this.cacheData = outputData;
        }

        /* access modifiers changed from: package-private */
        public void consumeAlias(UResource.Key key, UResource.Value value) {
            FormatWidth sourceWidth = widthFromKey(key);
            if (sourceWidth != null) {
                FormatWidth targetWidth = widthFromAlias(value);
                if (targetWidth == null) {
                    throw new ICUException("Units data fallback from " + key + " to unknown " + value.getAliasString());
                } else if (this.cacheData.widthFallback[targetWidth.ordinal()] == null) {
                    this.cacheData.widthFallback[sourceWidth.ordinal()] = targetWidth;
                } else {
                    throw new ICUException("Units data fallback from " + key + " to " + value.getAliasString() + " which falls back to something else");
                }
            }
        }

        public void consumeTable(UResource.Key key, UResource.Value value) {
            FormatWidth widthFromKey = widthFromKey(key);
            this.width = widthFromKey;
            if (widthFromKey != null) {
                UResource.Table unitTypesTable = value.getTable();
                for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                    consumeUnitTypesTable(key, value);
                }
            }
        }

        static FormatWidth widthFromKey(UResource.Key key) {
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

        static FormatWidth widthFromAlias(UResource.Value value) {
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

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table widthsTable = value.getTable();
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
        hmsTo012.put(MeasureUnit.HOUR, 0);
        hmsTo012.put(MeasureUnit.MINUTE, 1);
        hmsTo012.put(MeasureUnit.SECOND, 2);
    }

    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth2) {
        return getInstance(locale, formatWidth2, NumberFormat.getInstance(locale));
    }

    public static MeasureFormat getInstance(Locale locale, FormatWidth formatWidth2) {
        return getInstance(ULocale.forLocale(locale), formatWidth2);
    }

    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth2, NumberFormat format) {
        ULocale uLocale = locale;
        PluralRules rules2 = PluralRules.forLocale(locale);
        NumericFormatters formatters = null;
        MeasureFormatData data = localeMeasureFormatData.get(uLocale);
        if (data == null) {
            data = loadLocaleData(locale);
            localeMeasureFormatData.put(uLocale, data);
        }
        MeasureFormatData data2 = data;
        FormatWidth formatWidth3 = formatWidth2;
        if (formatWidth3 == FormatWidth.NUMERIC) {
            formatters = localeToNumericDurationFormatters.get(uLocale);
            if (formatters == null) {
                formatters = loadNumericFormatters(locale);
                localeToNumericDurationFormatters.put(uLocale, formatters);
            }
        }
        NumberFormat intFormat = NumberFormat.getInstance(locale);
        intFormat.setMaximumFractionDigits(0);
        intFormat.setMinimumFractionDigits(0);
        intFormat.setRoundingMode(1);
        ImmutableNumberFormat immutableNumberFormat = new ImmutableNumberFormat(format);
        MeasureFormat measureFormat = new MeasureFormat(uLocale, data2, formatWidth3, immutableNumberFormat, rules2, formatters, new ImmutableNumberFormat(NumberFormat.getInstance(uLocale, formatWidth2.getCurrencyStyle())), new ImmutableNumberFormat(intFormat));
        return measureFormat;
    }

    public static MeasureFormat getInstance(Locale locale, FormatWidth formatWidth2, NumberFormat format) {
        return getInstance(ULocale.forLocale(locale), formatWidth2, format);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v0, resolved type: android.icu.util.Measure} */
    /* JADX WARNING: Multi-variable type inference failed */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        int prevLength = toAppendTo.length();
        FieldPosition fpos = new FieldPosition(pos.getFieldAttribute(), pos.getField());
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            Measure[] measures = new Measure[collection.size()];
            int idx = 0;
            for (Object o : collection) {
                if (o instanceof Measure) {
                    measures[idx] = o;
                    idx++;
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
        boolean z;
        MeasureUnit unit = lowValue.getUnit();
        if (unit.equals(highValue.getUnit())) {
            Number lowNumber = lowValue.getNumber();
            Number highNumber = highValue.getNumber();
            boolean isCurrency = unit instanceof Currency;
            UFieldPosition lowFpos = new UFieldPosition();
            UFieldPosition highFpos = new UFieldPosition();
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
                    lowFormatted = currentNumberFormat.format((Object) lowNumber, new StringBuffer(), (FieldPosition) lowFpos);
                    highFormatted = currentNumberFormat.format((Object) highNumber, new StringBuffer(), (FieldPosition) highFpos);
                }
            }
            if (lowFormatted == null) {
                lowFormatted = this.numberFormat.format(lowNumber, new StringBuffer(), (FieldPosition) lowFpos);
                highFormatted = this.numberFormat.format(highNumber, new StringBuffer(), (FieldPosition) highFpos);
            }
            double lowDouble = lowNumber.doubleValue();
            PluralRules pluralRules = this.rules;
            int countVisibleFractionDigits = lowFpos.getCountVisibleFractionDigits();
            long fractionDigits = lowFpos.getFractionDigits();
            Number number = lowNumber;
            UFieldPosition uFieldPosition = lowFpos;
            PluralRules.FixedDecimal fixedDecimal = r9;
            PluralRules.FixedDecimal fixedDecimal2 = new PluralRules.FixedDecimal(lowDouble, countVisibleFractionDigits, fractionDigits);
            String keywordLow = pluralRules.select((PluralRules.IFixedDecimal) fixedDecimal);
            double highDouble = highNumber.doubleValue();
            PluralRules pluralRules2 = this.rules;
            Number number2 = highNumber;
            PluralRules.FixedDecimal fixedDecimal3 = r9;
            PluralRules.FixedDecimal fixedDecimal4 = new PluralRules.FixedDecimal(highDouble, highFpos.getCountVisibleFractionDigits(), highFpos.getFractionDigits());
            StandardPlural resolvedPlural = PluralRules.Factory.getDefaultFactory().getPluralRanges(getLocale()).get(StandardPlural.fromString(keywordLow), StandardPlural.fromString(pluralRules2.select((PluralRules.IFixedDecimal) fixedDecimal3)));
            String formattedNumber = SimpleFormatterImpl.formatCompiledPattern(getRangeFormat(getLocale(), this.formatWidth), lowFormatted, highFormatted);
            if (isCurrency) {
                this.currencyFormat.format(Double.valueOf(1.0d));
                Currency currencyUnit = (Currency) unit;
                StringBuilder result = new StringBuilder();
                ImmutableNumberFormat immutableNumberFormat = this.currencyFormat;
                if (lowDouble >= 0.0d) {
                    String str = keywordLow;
                    z = true;
                } else {
                    String str2 = keywordLow;
                    z = false;
                }
                appendReplacingCurrency(immutableNumberFormat.getPrefix(z), currencyUnit, resolvedPlural, result);
                result.append(formattedNumber);
                appendReplacingCurrency(this.currencyFormat.getSuffix(highDouble >= 0.0d), currencyUnit, resolvedPlural, result);
                return result.toString();
            }
            return SimpleFormatterImpl.formatCompiledPattern(getPluralFormatter(lowValue.getUnit(), this.formatWidth, resolvedPlural.ordinal()), formattedNumber);
        }
        throw new IllegalArgumentException("Units must match: " + unit + " ≠ " + highValue.getUnit());
    }

    private void appendReplacingCurrency(String affix, Currency unit, StandardPlural resolvedPlural, StringBuilder result) {
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
        int i = 0;
        result.append(affix.substring(0, pos));
        int currentStyle = this.formatWidth.getCurrencyStyle();
        if (currentStyle == 5) {
            result.append(unit.getCurrencyCode());
        } else {
            ULocale locale = this.currencyFormat.nf.getLocale(ULocale.ACTUAL_LOCALE);
            if (currentStyle != 1) {
                i = 2;
            }
            result.append(unit.getName(locale, i, resolvedPlural.getKeyword(), (boolean[]) null));
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
        int i = 0;
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
        String[] results = new String[measures.length];
        while (i < measures.length) {
            results[i] = formatMeasure(measures[i], i == measures.length - 1 ? this.numberFormat : this.integerFormat);
            i++;
        }
        appendTo.append(listFormatter.format((Object[]) results));
        return appendTo;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    public String getUnitDisplayName(MeasureUnit unit) {
        FormatWidth width = getRegularWidth(this.formatWidth);
        Map<FormatWidth, String> styleToDnam = this.cache.unitToStyleToDnam.get(unit);
        if (styleToDnam == null) {
            return null;
        }
        String dnam = styleToDnam.get(width);
        if (dnam != null) {
            return dnam;
        }
        FormatWidth fallbackWidth = this.cache.widthFallback[width.ordinal()];
        if (fallbackWidth != null) {
            dnam = styleToDnam.get(fallbackWidth);
        }
        return dnam;
    }

    public final boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof MeasureFormat)) {
            return false;
        }
        MeasureFormat rhs = (MeasureFormat) other;
        if (getWidth() != rhs.getWidth() || !getLocale().equals(rhs.getLocale()) || !getNumberFormat().equals(rhs.getNumberFormat())) {
            z = false;
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
        return getCurrencyFormat(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    /* access modifiers changed from: package-private */
    public MeasureFormat withLocale(ULocale locale) {
        return getInstance(locale, getWidth());
    }

    /* access modifiers changed from: package-private */
    public MeasureFormat withNumberFormat(NumberFormat format) {
        MeasureFormat measureFormat = new MeasureFormat(getLocale(), this.cache, this.formatWidth, new ImmutableNumberFormat(format), this.rules, this.numericFormatters, this.currencyFormat, this.integerFormat);
        return measureFormat;
    }

    private MeasureFormat(ULocale locale, MeasureFormatData data, FormatWidth formatWidth2, ImmutableNumberFormat format, PluralRules rules2, NumericFormatters formatters, ImmutableNumberFormat currencyFormat2, ImmutableNumberFormat integerFormat2) {
        setLocale(locale, locale);
        this.cache = data;
        this.formatWidth = formatWidth2;
        this.numberFormat = format;
        this.rules = rules2;
        this.numericFormatters = formatters;
        this.currencyFormat = currencyFormat2;
        this.integerFormat = integerFormat2;
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
        MeasureFormatData cacheData = new MeasureFormatData();
        ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, locale)).getAllItemsWithFallback("", new UnitDataSink(cacheData));
        return cacheData;
    }

    private static final FormatWidth getRegularWidth(FormatWidth width) {
        if (width == FormatWidth.NUMERIC) {
            return FormatWidth.NARROW;
        }
        return width;
    }

    private String getFormatterOrNull(MeasureUnit unit, FormatWidth width, int index) {
        FormatWidth width2 = getRegularWidth(width);
        Map<FormatWidth, String[]> styleToPatterns = this.cache.unitToStyleToPatterns.get(unit);
        String[] patterns = styleToPatterns.get(width2);
        if (patterns != null && patterns[index] != null) {
            return patterns[index];
        }
        FormatWidth fallbackWidth = this.cache.widthFallback[width2.ordinal()];
        if (fallbackWidth != null) {
            String[] patterns2 = styleToPatterns.get(fallbackWidth);
            if (!(patterns2 == null || patterns2[index] == null)) {
                return patterns2[index];
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

    @Deprecated
    public String getPluralFormatter(MeasureUnit unit, FormatWidth width, int index) {
        if (index != StandardPlural.OTHER_INDEX) {
            String pattern = getFormatterOrNull(unit, width, index);
            if (pattern != null) {
                return pattern;
            }
        }
        return getFormatter(unit, width, StandardPlural.OTHER_INDEX);
    }

    private String getPerFormatter(FormatWidth width) {
        FormatWidth width2 = getRegularWidth(width);
        String perPattern = this.cache.styleToPerPattern.get(width2);
        if (perPattern != null) {
            return perPattern;
        }
        FormatWidth fallbackWidth = this.cache.widthFallback[width2.ordinal()];
        if (fallbackWidth != null) {
            String perPattern2 = this.cache.styleToPerPattern.get(fallbackWidth);
            if (perPattern2 != null) {
                return perPattern2;
            }
        }
        throw new MissingResourceException("no x-per-y pattern for width " + width2, null, null);
    }

    private int withPerUnitAndAppend(CharSequence formatted, MeasureUnit perUnit, StringBuilder appendTo) {
        int[] offsets = new int[1];
        String perUnitPattern = getFormatterOrNull(perUnit, this.formatWidth, MeasureFormatData.PER_UNIT_INDEX);
        if (perUnitPattern != null) {
            SimpleFormatterImpl.formatAndAppend(perUnitPattern, appendTo, offsets, formatted);
            return offsets[0];
        }
        SimpleFormatterImpl.formatAndAppend(getPerFormatter(this.formatWidth), appendTo, offsets, formatted, SimpleFormatterImpl.getTextWithNoArguments(getPluralFormatter(perUnit, this.formatWidth, StandardPlural.ONE.ordinal())).trim());
        return offsets[0];
    }

    private String formatMeasure(Measure measure, ImmutableNumberFormat nf) {
        return formatMeasure(measure, nf, new StringBuilder(), DontCareFieldPosition.INSTANCE).toString();
    }

    private StringBuilder formatMeasure(Measure measure, ImmutableNumberFormat nf, StringBuilder appendTo, FieldPosition fieldPosition) {
        Number n = measure.getNumber();
        MeasureUnit unit = measure.getUnit();
        if (unit instanceof Currency) {
            appendTo.append(this.currencyFormat.format(new CurrencyAmount(n, (Currency) unit), new StringBuffer(), fieldPosition));
            return appendTo;
        }
        StringBuffer formattedNumber = new StringBuffer();
        return QuantityFormatter.format(getPluralFormatter(unit, this.formatWidth, QuantityFormatter.selectPlural(n, nf.nf, this.rules, formattedNumber, fieldPosition).ordinal()), formattedNumber, appendTo, fieldPosition);
    }

    /* access modifiers changed from: package-private */
    public Object toTimeUnitProxy() {
        return new MeasureProxy(getLocale(), this.formatWidth, this.numberFormat.get(), 1);
    }

    /* access modifiers changed from: package-private */
    public Object toCurrencyProxy() {
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
        ListFormatter.FormattedListBuilder builder = listFormatter.format(Arrays.asList(results), fieldPositionFoundIndex);
        if (builder.getOffset() != -1) {
            fieldPosition.setBeginIndex(fpos.getBeginIndex() + builder.getOffset() + appendTo.length());
            fieldPosition.setEndIndex(fpos.getEndIndex() + builder.getOffset() + appendTo.length());
        }
        appendTo.append(builder.toString());
        return appendTo;
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
            Integer idxObj = hmsTo012.get(m.getUnit());
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
        Number[] numberArr = hms;
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < numberArr.length; i++) {
            if (numberArr[i] != null) {
                int endIndex2 = i;
                if (startIndex == -1) {
                    endIndex = endIndex2;
                    startIndex = endIndex2;
                } else {
                    endIndex = endIndex2;
                }
            } else {
                numberArr[i] = 0;
            }
        }
        Date d = new Date((long) (((((Math.floor(numberArr[0].doubleValue()) * 60.0d) + Math.floor(numberArr[1].doubleValue())) * 60.0d) + Math.floor(numberArr[2].doubleValue())) * 1000.0d));
        if (startIndex == 0 && endIndex == 2) {
            return formatNumeric(d, this.numericFormatters.getHourMinuteSecond(), DateFormat.Field.SECOND, numberArr[endIndex], appendable);
        } else if (startIndex == 1 && endIndex == 2) {
            return formatNumeric(d, this.numericFormatters.getMinuteSecond(), DateFormat.Field.SECOND, numberArr[endIndex], appendable);
        } else if (startIndex == 0 && endIndex == 1) {
            return formatNumeric(d, this.numericFormatters.getHourMinute(), DateFormat.Field.MINUTE, numberArr[endIndex], appendable);
        } else {
            throw new IllegalStateException();
        }
    }

    private StringBuilder formatNumeric(Date duration, DateFormat formatter, DateFormat.Field smallestField, Number smallestAmount, StringBuilder appendTo) {
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

    /* access modifiers changed from: private */
    public static FormatWidth fromFormatWidthOrdinal(int ordinal) {
        FormatWidth[] values = FormatWidth.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return FormatWidth.SHORT;
        }
        return values[ordinal];
    }

    @Deprecated
    public static String getRangeFormat(ULocale forLocale, FormatWidth width) {
        String resultString;
        if (forLocale.getLanguage().equals("fr")) {
            return getRangeFormat(ULocale.ROOT, width);
        }
        String result = localeIdToRangeFormat.get(forLocale);
        if (result == null) {
            ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, forLocale);
            ULocale realLocale = rb.getULocale();
            if (!forLocale.equals(realLocale)) {
                String result2 = localeIdToRangeFormat.get(forLocale);
                if (result2 != null) {
                    localeIdToRangeFormat.put(forLocale, result2);
                    return result2;
                }
            }
            NumberingSystem ns = NumberingSystem.getInstance(forLocale);
            try {
                resultString = rb.getStringWithFallback("NumberElements/" + ns.getName() + "/miscPatterns/range");
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
