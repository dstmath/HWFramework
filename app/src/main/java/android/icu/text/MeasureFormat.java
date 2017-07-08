package android.icu.text;

import android.icu.impl.DontCareFieldPosition;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimplePatternFormatter;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.TableSink;
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
import dalvik.bytecode.Opcodes;
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
import org.xmlpull.v1.XmlPullParser;

public class MeasureFormat extends UFormat {
    private static final int CURRENCY_FORMAT = 2;
    private static final int MEASURE_FORMAT = 0;
    private static final int TIME_UNIT_FORMAT = 1;
    private static final Map<MeasureUnit, Integer> hmsTo012 = null;
    private static final Map<ULocale, String> localeIdToRangeFormat = null;
    private static final SimpleCache<ULocale, MeasureFormatData> localeMeasureFormatData = null;
    private static final SimpleCache<ULocale, NumericFormatters> localeToNumericDurationFormatters = null;
    static final long serialVersionUID = -7182021401701778240L;
    private final transient MeasureFormatData cache;
    private final transient ImmutableNumberFormat currencyFormat;
    private final transient FormatWidth formatWidth;
    private final transient ImmutableNumberFormat integerFormat;
    private final transient ImmutableNumberFormat numberFormat;
    private final transient NumericFormatters numericFormatters;
    private final transient PluralRules rules;

    public enum FormatWidth {
        ;
        
        private static final int INDEX_COUNT = 3;
        private final int currencyStyle;
        private final Style listFormatterStyle;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.MeasureFormat.FormatWidth.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.MeasureFormat.FormatWidth.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.MeasureFormat.FormatWidth.<clinit>():void");
        }

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
            return positive ? ((DecimalFormat) this.nf).getPositiveSuffix() : ((DecimalFormat) this.nf).getPositiveSuffix();
        }
    }

    private static final class MeasureFormatData {
        static final int PATTERN_COUNT = 0;
        static final int PER_UNIT_INDEX = 0;
        final EnumMap<FormatWidth, String> styleToPerPattern;
        final Map<MeasureUnit, EnumMap<FormatWidth, String[]>> unitToStyleToPatterns;
        final FormatWidth[] widthFallback;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.MeasureFormat.MeasureFormatData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.MeasureFormat.MeasureFormatData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.MeasureFormat.MeasureFormatData.<clinit>():void");
        }

        /* synthetic */ MeasureFormatData(MeasureFormatData measureFormatData) {
            this();
        }

        private MeasureFormatData() {
            this.widthFallback = new FormatWidth[3];
            this.unitToStyleToPatterns = new HashMap();
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

        public MeasureProxy() {
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeByte(MeasureFormat.MEASURE_FORMAT);
            out.writeUTF(this.locale.toLanguageTag());
            out.writeByte(this.formatWidth.ordinal());
            out.writeObject(this.numberFormat);
            out.writeByte(this.subClass);
            out.writeObject(this.keyValues);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readByte();
            this.locale = ULocale.forLanguageTag(in.readUTF());
            this.formatWidth = MeasureFormat.fromFormatWidthOrdinal(in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO);
            this.numberFormat = (NumberFormat) in.readObject();
            if (this.numberFormat == null) {
                throw new InvalidObjectException("Missing number format.");
            }
            this.subClass = in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO;
            this.keyValues = (HashMap) in.readObject();
            if (this.keyValues == null) {
                throw new InvalidObjectException("Missing optional values map.");
            }
        }

        private TimeUnitFormat createTimeUnitFormat() throws InvalidObjectException {
            int style;
            if (this.formatWidth == FormatWidth.WIDE) {
                style = MeasureFormat.MEASURE_FORMAT;
            } else if (this.formatWidth == FormatWidth.SHORT) {
                style = MeasureFormat.TIME_UNIT_FORMAT;
            } else {
                throw new InvalidObjectException("Bad width: " + this.formatWidth);
            }
            TimeUnitFormat result = new TimeUnitFormat(this.locale, style);
            result.setNumberFormat(this.numberFormat);
            return result;
        }

        private Object readResolve() throws ObjectStreamException {
            switch (this.subClass) {
                case MeasureFormat.MEASURE_FORMAT /*0*/:
                    return MeasureFormat.getInstance(this.locale, this.formatWidth, this.numberFormat);
                case MeasureFormat.TIME_UNIT_FORMAT /*1*/:
                    return createTimeUnitFormat();
                case MeasureFormat.CURRENCY_FORMAT /*2*/:
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
            this.prefix = pattern.substring(MeasureFormat.MEASURE_FORMAT, pos);
            this.suffix = pattern.substring(pos + 3);
        }

        public String toString() {
            return this.prefix + "; " + this.suffix;
        }
    }

    private static final class UnitDataSink extends TableSink {
        MeasureFormatData cacheData;
        UnitCompoundSink compoundSink;
        UnitPatternSink patternSink;
        StringBuilder sb;
        UnitSubtypeSink subtypeSink;
        String type;
        UnitTypeSink typeSink;
        MeasureUnit unit;
        FormatWidth width;

        class UnitCompoundSink extends TableSink {
            final /* synthetic */ UnitDataSink this$1;

            UnitCompoundSink(UnitDataSink this$1) {
                this.this$1 = this$1;
            }

            public void put(Key key, Value value) {
                if (key.contentEquals("per")) {
                    this.this$1.cacheData.styleToPerPattern.put(this.this$1.width, SimplePatternFormatter.compileToStringMinMaxPlaceholders(value.getString(), this.this$1.sb, MeasureFormat.CURRENCY_FORMAT, MeasureFormat.CURRENCY_FORMAT));
                }
            }
        }

        class UnitPatternSink extends TableSink {
            String[] patterns;
            final /* synthetic */ UnitDataSink this$1;

            UnitPatternSink(UnitDataSink this$1) {
                this.this$1 = this$1;
            }

            void setFormatterIfAbsent(int index, Value value, int minPlaceholders) {
                if (this.patterns == null) {
                    EnumMap<FormatWidth, String[]> styleToPatterns = (EnumMap) this.this$1.cacheData.unitToStyleToPatterns.get(this.this$1.unit);
                    if (styleToPatterns == null) {
                        styleToPatterns = new EnumMap(FormatWidth.class);
                        this.this$1.cacheData.unitToStyleToPatterns.put(this.this$1.unit, styleToPatterns);
                    } else {
                        this.patterns = (String[]) styleToPatterns.get(this.this$1.width);
                    }
                    if (this.patterns == null) {
                        this.patterns = new String[MeasureFormatData.PATTERN_COUNT];
                        styleToPatterns.put(this.this$1.width, this.patterns);
                    }
                }
                if (this.patterns[index] == null) {
                    this.patterns[index] = SimplePatternFormatter.compileToStringMinMaxPlaceholders(value.getString(), this.this$1.sb, minPlaceholders, MeasureFormat.TIME_UNIT_FORMAT);
                }
            }

            public void put(Key key, Value value) {
                if (!key.contentEquals("dnam")) {
                    if (key.contentEquals("per")) {
                        setFormatterIfAbsent(MeasureFormatData.PER_UNIT_INDEX, value, MeasureFormat.MEASURE_FORMAT);
                    } else {
                        setFormatterIfAbsent(StandardPlural.indexFromString(key), value, MeasureFormat.MEASURE_FORMAT);
                    }
                }
            }
        }

        class UnitSubtypeSink extends TableSink {
            final /* synthetic */ UnitDataSink this$1;

            UnitSubtypeSink(UnitDataSink this$1) {
                this.this$1 = this$1;
            }

            public TableSink getOrCreateTableSink(Key key, int initialSize) {
                this.this$1.unit = MeasureUnit.internalGetInstance(this.this$1.type, key.toString());
                this.this$1.patternSink.patterns = null;
                return this.this$1.patternSink;
            }
        }

        class UnitTypeSink extends TableSink {
            final /* synthetic */ UnitDataSink this$1;

            UnitTypeSink(UnitDataSink this$1) {
                this.this$1 = this$1;
            }

            public TableSink getOrCreateTableSink(Key key, int initialSize) {
                if (!key.contentEquals("currency")) {
                    if (!key.contentEquals("compound")) {
                        this.this$1.type = key.toString();
                        return this.this$1.subtypeSink;
                    } else if (!this.this$1.cacheData.hasPerFormatter(this.this$1.width)) {
                        return this.this$1.compoundSink;
                    }
                }
                return null;
            }
        }

        UnitDataSink(MeasureFormatData outputData) {
            this.patternSink = new UnitPatternSink(this);
            this.subtypeSink = new UnitSubtypeSink(this);
            this.compoundSink = new UnitCompoundSink(this);
            this.typeSink = new UnitTypeSink(this);
            this.sb = new StringBuilder();
            this.cacheData = outputData;
        }

        public void put(Key key, Value value) {
            if (value.getType() == 3) {
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
        }

        public TableSink getOrCreateTableSink(Key key, int initialSize) {
            FormatWidth widthFromKey = widthFromKey(key);
            this.width = widthFromKey;
            if (widthFromKey != null) {
                return this.typeSink;
            }
            return null;
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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.MeasureFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.MeasureFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.MeasureFormat.<clinit>():void");
    }

    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth) {
        return getInstance(locale, formatWidth, NumberFormat.getInstance(locale));
    }

    public static MeasureFormat getInstance(Locale locale, FormatWidth formatWidth) {
        return getInstance(ULocale.forLocale(locale), formatWidth);
    }

    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth, NumberFormat format) {
        PluralRules rules = PluralRules.forLocale(locale);
        NumericFormatters numericFormatters = null;
        MeasureFormatData data = (MeasureFormatData) localeMeasureFormatData.get(locale);
        if (data == null) {
            data = loadLocaleData(locale);
            localeMeasureFormatData.put(locale, data);
        }
        if (formatWidth == FormatWidth.NUMERIC) {
            numericFormatters = (NumericFormatters) localeToNumericDurationFormatters.get(locale);
            if (numericFormatters == null) {
                numericFormatters = loadNumericFormatters(locale);
                localeToNumericDurationFormatters.put(locale, numericFormatters);
            }
        }
        NumberFormat intFormat = NumberFormat.getInstance(locale);
        intFormat.setMaximumFractionDigits(MEASURE_FORMAT);
        intFormat.setMinimumFractionDigits(MEASURE_FORMAT);
        intFormat.setRoundingMode(TIME_UNIT_FORMAT);
        return new MeasureFormat(locale, data, formatWidth, new ImmutableNumberFormat(format), rules, numericFormatters, new ImmutableNumberFormat(NumberFormat.getInstance(locale, formatWidth.getCurrencyStyle())), new ImmutableNumberFormat(intFormat));
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
            int idx = MEASURE_FORMAT;
            for (Object o : coll) {
                if (o instanceof Measure) {
                    int idx2 = idx + TIME_UNIT_FORMAT;
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

    public /* bridge */ /* synthetic */ Object m19parseObject(String source, ParsePosition pos) {
        return parseObject(source, pos);
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
            StringBuffer stringBuffer = null;
            StringBuffer highFormatted = null;
            if (isCurrency) {
                int fracDigits = ((Currency) unit).getDefaultFractionDigits();
                int maxFrac = this.numberFormat.nf.getMaximumFractionDigits();
                int minFrac = this.numberFormat.nf.getMinimumFractionDigits();
                if (!(fracDigits == maxFrac && fracDigits == minFrac)) {
                    DecimalFormat currentNumberFormat = (DecimalFormat) this.numberFormat.get();
                    currentNumberFormat.setMaximumFractionDigits(fracDigits);
                    currentNumberFormat.setMinimumFractionDigits(fracDigits);
                    stringBuffer = currentNumberFormat.format((Object) lowNumber, new StringBuffer(), lowFpos);
                    highFormatted = currentNumberFormat.format((Object) highNumber, new StringBuffer(), highFpos);
                }
            }
            if (stringBuffer == null) {
                stringBuffer = this.numberFormat.format(lowNumber, new StringBuffer(), lowFpos);
                highFormatted = this.numberFormat.format(highNumber, new StringBuffer(), highFpos);
            }
            double lowDouble = lowNumber.doubleValue();
            String keywordLow = this.rules.select(new FixedDecimal(lowDouble, lowFpos.getCountVisibleFractionDigits(), lowFpos.getFractionDigits()));
            double highDouble = highNumber.doubleValue();
            StandardPlural resolvedPlural = Factory.getDefaultFactory().getPluralRanges(getLocale()).get(StandardPlural.fromString(keywordLow), StandardPlural.fromString(this.rules.select(new FixedDecimal(highDouble, highFpos.getCountVisibleFractionDigits(), highFpos.getFractionDigits()))));
            String rangeFormatter = getRangeFormat(getLocale(), this.formatWidth);
            CharSequence[] charSequenceArr = new CharSequence[CURRENCY_FORMAT];
            charSequenceArr[MEASURE_FORMAT] = stringBuffer;
            charSequenceArr[TIME_UNIT_FORMAT] = highFormatted;
            String formattedNumber = SimplePatternFormatter.formatCompiledPattern(rangeFormatter, charSequenceArr);
            if (isCurrency) {
                this.currencyFormat.format(Double.valueOf(1.0d));
                Currency currencyUnit = (Currency) unit;
                StringBuilder result = new StringBuilder();
                appendReplacingCurrency(this.currencyFormat.getPrefix(lowDouble >= 0.0d), currencyUnit, resolvedPlural, result);
                result.append(formattedNumber);
                appendReplacingCurrency(this.currencyFormat.getSuffix(highDouble >= 0.0d), currencyUnit, resolvedPlural, result);
                return result.toString();
            }
            String formatter = getPluralFormatter(lowValue.getUnit(), this.formatWidth, resolvedPlural.ordinal());
            charSequenceArr = new CharSequence[TIME_UNIT_FORMAT];
            charSequenceArr[MEASURE_FORMAT] = formattedNumber;
            return SimplePatternFormatter.formatCompiledPattern(formatter, charSequenceArr);
        }
        throw new IllegalArgumentException("Units must match: " + unit + " \u2260 " + highValue.getUnit());
    }

    private void appendReplacingCurrency(String affix, Currency unit, StandardPlural resolvedPlural, StringBuilder result) {
        int i = MEASURE_FORMAT;
        String replacement = "\u00a4";
        int pos = affix.indexOf(replacement);
        if (pos < 0) {
            replacement = "XXX";
            pos = affix.indexOf(replacement);
        }
        if (pos < 0) {
            result.append(affix);
            return;
        }
        result.append(affix.substring(MEASURE_FORMAT, pos));
        int currentStyle = this.formatWidth.getCurrencyStyle();
        if (currentStyle == 5) {
            result.append(unit.getCurrencyCode());
        } else {
            ULocale locale = this.currencyFormat.nf.getLocale(ULocale.ACTUAL_LOCALE);
            if (currentStyle != TIME_UNIT_FORMAT) {
                i = CURRENCY_FORMAT;
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
        if (measures.length == TIME_UNIT_FORMAT) {
            return formatMeasure(measures[MEASURE_FORMAT], this.numberFormat, appendTo, fieldPosition);
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
        int i = MEASURE_FORMAT;
        while (i < measures.length) {
            results[i] = formatMeasure(measures[i], i == measures.length + -1 ? this.numberFormat : this.integerFormat);
            i += TIME_UNIT_FORMAT;
        }
        return appendTo.append(listFormatter.format(results));
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
        resource.getAllTableItemsWithFallback(XmlPullParser.NO_NAMESPACE, new UnitDataSink(cacheData));
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
        int[] offsets = new int[TIME_UNIT_FORMAT];
        String perUnitPattern = getFormatterOrNull(perUnit, this.formatWidth, MeasureFormatData.PER_UNIT_INDEX);
        if (perUnitPattern != null) {
            CharSequence[] charSequenceArr = new CharSequence[TIME_UNIT_FORMAT];
            charSequenceArr[MEASURE_FORMAT] = formatted;
            SimplePatternFormatter.formatAndAppend(perUnitPattern, appendTo, offsets, charSequenceArr);
            return offsets[MEASURE_FORMAT];
        }
        String perPattern = getPerFormatter(this.formatWidth);
        String perUnitString = SimplePatternFormatter.getTextWithNoPlaceholders(getPluralFormatter(perUnit, this.formatWidth, StandardPlural.ONE.ordinal())).trim();
        charSequenceArr = new CharSequence[CURRENCY_FORMAT];
        charSequenceArr[MEASURE_FORMAT] = formatted;
        charSequenceArr[TIME_UNIT_FORMAT] = perUnitString;
        SimplePatternFormatter.formatAndAppend(perPattern, appendTo, offsets, charSequenceArr);
        return offsets[MEASURE_FORMAT];
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
        return new MeasureProxy(getLocale(), this.formatWidth, this.numberFormat.get(), TIME_UNIT_FORMAT);
    }

    Object toCurrencyProxy() {
        return new MeasureProxy(getLocale(), this.formatWidth, this.numberFormat.get(), CURRENCY_FORMAT);
    }

    private StringBuilder formatMeasuresSlowTrack(ListFormatter listFormatter, StringBuilder appendTo, FieldPosition fieldPosition, Measure... measures) {
        String[] results = new String[measures.length];
        FieldPosition fpos = new FieldPosition(fieldPosition.getFieldAttribute(), fieldPosition.getField());
        int fieldPositionFoundIndex = -1;
        int i = MEASURE_FORMAT;
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
            i += TIME_UNIT_FORMAT;
        }
        FormattedListBuilder builder = listFormatter.format(Arrays.asList(results), fieldPositionFoundIndex);
        if (builder.getOffset() != -1) {
            fieldPosition.setBeginIndex((fpos.getBeginIndex() + builder.getOffset()) + appendTo.length());
            fieldPosition.setEndIndex((fpos.getEndIndex() + builder.getOffset()) + appendTo.length());
        }
        return appendTo.append(builder.toString());
    }

    private static DateFormat loadNumericDurationFormat(ICUResourceBundle r, String type) {
        Object[] objArr = new Object[TIME_UNIT_FORMAT];
        objArr[MEASURE_FORMAT] = type;
        DateFormat result = new SimpleDateFormat(r.getWithFallback(String.format("durationUnits/%s", objArr)).getString().replace("h", DateFormat.HOUR24));
        result.setTimeZone(TimeZone.GMT_ZONE);
        return result;
    }

    private static Number[] toHMS(Measure[] measures) {
        Number[] result = new Number[3];
        int lastIdx = -1;
        int length = measures.length;
        for (int i = MEASURE_FORMAT; i < length; i += TIME_UNIT_FORMAT) {
            Measure m = measures[i];
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
        for (int i = MEASURE_FORMAT; i < hms.length; i += TIME_UNIT_FORMAT) {
            if (hms[i] != null) {
                endIndex = i;
                if (startIndex == -1) {
                    startIndex = endIndex;
                }
            } else {
                hms[i] = Integer.valueOf(MEASURE_FORMAT);
            }
        }
        Date d = new Date((long) (((((Math.floor(hms[MEASURE_FORMAT].doubleValue()) * 60.0d) + Math.floor(hms[TIME_UNIT_FORMAT].doubleValue())) * 60.0d) + Math.floor(hms[CURRENCY_FORMAT].doubleValue())) * 1000.0d));
        if (startIndex == 0 && endIndex == CURRENCY_FORMAT) {
            return formatNumeric(d, this.numericFormatters.getHourMinuteSecond(), Field.SECOND, hms[endIndex], appendable);
        }
        if (startIndex == TIME_UNIT_FORMAT && endIndex == CURRENCY_FORMAT) {
            return formatNumeric(d, this.numericFormatters.getMinuteSecond(), Field.SECOND, hms[endIndex], appendable);
        }
        if (startIndex == 0 && endIndex == TIME_UNIT_FORMAT) {
            return formatNumeric(d, this.numericFormatters.getHourMinute(), Field.MINUTE, hms[endIndex], appendable);
        }
        throw new IllegalStateException();
    }

    private StringBuilder formatNumeric(Date duration, DateFormat formatter, Field smallestField, Number smallestAmount, StringBuilder appendTo) {
        FieldPosition intFieldPosition = new FieldPosition(MEASURE_FORMAT);
        String smallestAmountFormatted = this.numberFormat.format(smallestAmount, new StringBuffer(), intFieldPosition).toString();
        if (intFieldPosition.getBeginIndex() == 0 && intFieldPosition.getEndIndex() == 0) {
            throw new IllegalStateException();
        }
        FieldPosition smallestFieldPosition = new FieldPosition(smallestField);
        String draft = formatter.format(duration, new StringBuffer(), smallestFieldPosition).toString();
        if (smallestFieldPosition.getBeginIndex() == 0 && smallestFieldPosition.getEndIndex() == 0) {
            appendTo.append(draft);
        } else {
            appendTo.append(draft, MEASURE_FORMAT, smallestFieldPosition.getBeginIndex());
            appendTo.append(smallestAmountFormatted, MEASURE_FORMAT, intFieldPosition.getBeginIndex());
            appendTo.append(draft, smallestFieldPosition.getBeginIndex(), smallestFieldPosition.getEndIndex());
            appendTo.append(smallestAmountFormatted, intFieldPosition.getEndIndex(), smallestAmountFormatted.length());
            appendTo.append(draft, smallestFieldPosition.getEndIndex(), draft.length());
        }
        return appendTo;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureProxy(getLocale(), this.formatWidth, this.numberFormat.get(), MEASURE_FORMAT);
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
            ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, forLocale);
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
            result = SimplePatternFormatter.compileToStringMinMaxPlaceholders(resultString, new StringBuilder(), CURRENCY_FORMAT, CURRENCY_FORMAT);
            localeIdToRangeFormat.put(forLocale, result);
            if (!forLocale.equals(realLocale)) {
                localeIdToRangeFormat.put(realLocale, result);
            }
        }
        return result;
    }
}
