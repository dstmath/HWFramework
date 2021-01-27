package ohos.global.icu.text;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.math.RoundingMode;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import ohos.global.icu.impl.DontCareFieldPosition;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SimpleCache;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.impl.number.LongNameHandler;
import ohos.global.icu.impl.number.RoundingUtils;
import ohos.global.icu.number.FormattedNumber;
import ohos.global.icu.number.IntegerWidth;
import ohos.global.icu.number.LocalizedNumberFormatter;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.Precision;
import ohos.global.icu.text.ListFormatter;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.Measure;
import ohos.global.icu.util.MeasureUnit;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class MeasureFormat extends UFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int CURRENCY_FORMAT = 2;
    private static final int MEASURE_FORMAT = 0;
    static final int NUMBER_FORMATTER_CURRENCY = 2;
    static final int NUMBER_FORMATTER_INTEGER = 3;
    static final int NUMBER_FORMATTER_STANDARD = 1;
    private static final int TIME_UNIT_FORMAT = 1;
    private static final Map<MeasureUnit, Integer> hmsTo012 = new HashMap();
    private static final Map<ULocale, String> localeIdToRangeFormat = new ConcurrentHashMap();
    private static final SimpleCache<ULocale, NumericFormatters> localeToNumericDurationFormatters = new SimpleCache<>();
    static final long serialVersionUID = -7182021401701778240L;
    private final transient FormatWidth formatWidth;
    private transient NumberFormatterCacheEntry formatter1;
    private transient NumberFormatterCacheEntry formatter2;
    private transient NumberFormatterCacheEntry formatter3;
    private final transient NumberFormat numberFormat;
    private final transient LocalizedNumberFormatter numberFormatter;
    private final transient NumericFormatters numericFormatters;
    private final transient PluralRules rules;

    static {
        hmsTo012.put(MeasureUnit.HOUR, 0);
        hmsTo012.put(MeasureUnit.MINUTE, 1);
        hmsTo012.put(MeasureUnit.SECOND, 2);
    }

    public enum FormatWidth {
        WIDE(ListFormatter.Style.UNIT, NumberFormatter.UnitWidth.FULL_NAME, NumberFormatter.UnitWidth.FULL_NAME),
        SHORT(ListFormatter.Style.UNIT_SHORT, NumberFormatter.UnitWidth.SHORT, NumberFormatter.UnitWidth.ISO_CODE),
        NARROW(ListFormatter.Style.UNIT_NARROW, NumberFormatter.UnitWidth.NARROW, NumberFormatter.UnitWidth.SHORT),
        NUMERIC(ListFormatter.Style.UNIT_NARROW, NumberFormatter.UnitWidth.NARROW, NumberFormatter.UnitWidth.SHORT),
        DEFAULT_CURRENCY(ListFormatter.Style.UNIT, NumberFormatter.UnitWidth.FULL_NAME, NumberFormatter.UnitWidth.SHORT);
        
        final NumberFormatter.UnitWidth currencyWidth;
        private final ListFormatter.Style listFormatterStyle;
        final NumberFormatter.UnitWidth unitWidth;

        private FormatWidth(ListFormatter.Style style, NumberFormatter.UnitWidth unitWidth2, NumberFormatter.UnitWidth unitWidth3) {
            this.listFormatterStyle = style;
            this.unitWidth = unitWidth2;
            this.currencyWidth = unitWidth3;
        }

        /* access modifiers changed from: package-private */
        public ListFormatter.Style getListFormatterStyle() {
            return this.listFormatterStyle;
        }
    }

    public static MeasureFormat getInstance(ULocale uLocale, FormatWidth formatWidth2) {
        return getInstance(uLocale, formatWidth2, NumberFormat.getInstance(uLocale));
    }

    public static MeasureFormat getInstance(Locale locale, FormatWidth formatWidth2) {
        return getInstance(ULocale.forLocale(locale), formatWidth2);
    }

    public static MeasureFormat getInstance(ULocale uLocale, FormatWidth formatWidth2, NumberFormat numberFormat2) {
        return new MeasureFormat(uLocale, formatWidth2, numberFormat2, null, null);
    }

    public static MeasureFormat getInstance(Locale locale, FormatWidth formatWidth2, NumberFormat numberFormat2) {
        return getInstance(ULocale.forLocale(locale), formatWidth2, numberFormat2);
    }

    @Override // java.text.Format
    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        int length = stringBuffer.length();
        int i = 0;
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            Measure[] measureArr = new Measure[collection.size()];
            for (Object obj2 : collection) {
                if (obj2 instanceof Measure) {
                    measureArr[i] = (Measure) obj2;
                    i++;
                } else {
                    throw new IllegalArgumentException(obj.toString());
                }
            }
            formatMeasuresInternal(stringBuffer, fieldPosition, measureArr);
        } else if (obj instanceof Measure[]) {
            formatMeasuresInternal(stringBuffer, fieldPosition, (Measure[]) obj);
        } else if (obj instanceof Measure) {
            FormattedNumber formatMeasure = formatMeasure((Measure) obj);
            formatMeasure.nextFieldPosition(fieldPosition);
            formatMeasure.appendTo(stringBuffer);
        } else {
            throw new IllegalArgumentException(obj.toString());
        }
        if (length > 0 && fieldPosition.getEndIndex() != 0) {
            fieldPosition.setBeginIndex(fieldPosition.getBeginIndex() + length);
            fieldPosition.setEndIndex(fieldPosition.getEndIndex() + length);
        }
        return stringBuffer;
    }

    @Override // java.text.Format
    public Measure parseObject(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    public final String formatMeasures(Measure... measureArr) {
        return formatMeasures(new StringBuilder(), DontCareFieldPosition.INSTANCE, measureArr).toString();
    }

    public StringBuilder formatMeasurePerUnit(Measure measure, MeasureUnit measureUnit, StringBuilder sb, FieldPosition fieldPosition) {
        FormattedNumber format = getUnitFormatterFromCache(1, measure.getUnit(), measureUnit).format(measure.getNumber());
        DecimalFormat.fieldPositionHelper(format, fieldPosition, sb.length());
        format.appendTo(sb);
        return sb;
    }

    public StringBuilder formatMeasures(StringBuilder sb, FieldPosition fieldPosition, Measure... measureArr) {
        int length = sb.length();
        formatMeasuresInternal(sb, fieldPosition, measureArr);
        if (length > 0 && fieldPosition.getEndIndex() > 0) {
            fieldPosition.setBeginIndex(fieldPosition.getBeginIndex() + length);
            fieldPosition.setEndIndex(fieldPosition.getEndIndex() + length);
        }
        return sb;
    }

    private void formatMeasuresInternal(Appendable appendable, FieldPosition fieldPosition, Measure... measureArr) {
        Number[] hms;
        if (measureArr.length != 0) {
            if (measureArr.length == 1) {
                FormattedNumber formatMeasure = formatMeasure(measureArr[0]);
                formatMeasure.nextFieldPosition(fieldPosition);
                formatMeasure.appendTo(appendable);
            } else if (this.formatWidth != FormatWidth.NUMERIC || (hms = toHMS(measureArr)) == null) {
                ListFormatter instance = ListFormatter.getInstance(getLocale(), this.formatWidth.getListFormatterStyle());
                if (fieldPosition != DontCareFieldPosition.INSTANCE) {
                    formatMeasuresSlowTrack(instance, appendable, fieldPosition, measureArr);
                    return;
                }
                String[] strArr = new String[measureArr.length];
                for (int i = 0; i < measureArr.length; i++) {
                    if (i == measureArr.length - 1) {
                        strArr[i] = formatMeasure(measureArr[i]).toString();
                    } else {
                        strArr[i] = formatMeasureInteger(measureArr[i]).toString();
                    }
                }
                instance.format(Arrays.asList(strArr), -1).appendTo(appendable);
            } else {
                formatNumeric(hms, appendable);
            }
        }
    }

    public String getUnitDisplayName(MeasureUnit measureUnit) {
        return LongNameHandler.getUnitDisplayName(getLocale(), measureUnit, this.formatWidth.unitWidth);
    }

    @Override // java.lang.Object
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MeasureFormat)) {
            return false;
        }
        MeasureFormat measureFormat = (MeasureFormat) obj;
        return getWidth() == measureFormat.getWidth() && getLocale().equals(measureFormat.getLocale()) && getNumberFormatInternal().equals(measureFormat.getNumberFormatInternal());
    }

    @Override // java.lang.Object
    public final int hashCode() {
        return (((getLocale().hashCode() * 31) + getNumberFormatInternal().hashCode()) * 31) + getWidth().hashCode();
    }

    public FormatWidth getWidth() {
        if (this.formatWidth == FormatWidth.DEFAULT_CURRENCY) {
            return FormatWidth.WIDE;
        }
        return this.formatWidth;
    }

    public final ULocale getLocale() {
        return getLocale(ULocale.VALID_LOCALE);
    }

    public NumberFormat getNumberFormat() {
        return (NumberFormat) this.numberFormat.clone();
    }

    /* access modifiers changed from: package-private */
    public NumberFormat getNumberFormatInternal() {
        return this.numberFormat;
    }

    public static MeasureFormat getCurrencyFormat(ULocale uLocale) {
        return new CurrencyFormat(uLocale);
    }

    public static MeasureFormat getCurrencyFormat(Locale locale) {
        return getCurrencyFormat(ULocale.forLocale(locale));
    }

    public static MeasureFormat getCurrencyFormat() {
        return getCurrencyFormat(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    /* access modifiers changed from: package-private */
    public MeasureFormat withLocale(ULocale uLocale) {
        return getInstance(uLocale, getWidth());
    }

    /* access modifiers changed from: package-private */
    public MeasureFormat withNumberFormat(NumberFormat numberFormat2) {
        return new MeasureFormat(getLocale(), this.formatWidth, numberFormat2, this.rules, this.numericFormatters);
    }

    MeasureFormat(ULocale uLocale, FormatWidth formatWidth2) {
        this(uLocale, formatWidth2, null, null, null);
    }

    private MeasureFormat(ULocale uLocale, FormatWidth formatWidth2, NumberFormat numberFormat2, PluralRules pluralRules, NumericFormatters numericFormatters2) {
        NumberFormat numberFormat3;
        this.formatter1 = null;
        this.formatter2 = null;
        this.formatter3 = null;
        setLocale(uLocale, uLocale);
        this.formatWidth = formatWidth2;
        this.rules = pluralRules == null ? PluralRules.forLocale(uLocale) : pluralRules;
        if (numberFormat2 == null) {
            numberFormat3 = NumberFormat.getInstance(uLocale);
        } else {
            numberFormat3 = (NumberFormat) numberFormat2.clone();
        }
        this.numberFormat = numberFormat3;
        if (numericFormatters2 == null && formatWidth2 == FormatWidth.NUMERIC && (numericFormatters2 = (NumericFormatters) localeToNumericDurationFormatters.get(uLocale)) == null) {
            numericFormatters2 = loadNumericFormatters(uLocale);
            localeToNumericDurationFormatters.put(uLocale, numericFormatters2);
        }
        this.numericFormatters = numericFormatters2;
        if (numberFormat3 instanceof DecimalFormat) {
            this.numberFormatter = ((DecimalFormat) numberFormat3).toNumberFormatter().unitWidth(formatWidth2.unitWidth);
            return;
        }
        throw new IllegalArgumentException();
    }

    MeasureFormat(ULocale uLocale, FormatWidth formatWidth2, NumberFormat numberFormat2, PluralRules pluralRules) {
        this(uLocale, formatWidth2, numberFormat2, pluralRules, null);
        if (formatWidth2 == FormatWidth.NUMERIC) {
            throw new IllegalArgumentException("The format width 'numeric' is not allowed by this constructor");
        }
    }

    /* access modifiers changed from: package-private */
    public static class NumericFormatters {
        private String hourMinute;
        private String hourMinuteSecond;
        private String minuteSecond;

        public NumericFormatters(String str, String str2, String str3) {
            this.hourMinute = str;
            this.minuteSecond = str2;
            this.hourMinuteSecond = str3;
        }

        public String getHourMinute() {
            return this.hourMinute;
        }

        public String getMinuteSecond() {
            return this.minuteSecond;
        }

        public String getHourMinuteSecond() {
            return this.hourMinuteSecond;
        }
    }

    private static NumericFormatters loadNumericFormatters(ULocale uLocale) {
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b/unit", uLocale);
        return new NumericFormatters(loadNumericDurationFormat(bundleInstance, "hm"), loadNumericDurationFormat(bundleInstance, DateFormat.MINUTE_SECOND), loadNumericDurationFormat(bundleInstance, "hms"));
    }

    /* access modifiers changed from: package-private */
    public static class NumberFormatterCacheEntry {
        LocalizedNumberFormatter formatter;
        MeasureUnit perUnit;
        int type;
        MeasureUnit unit;

        NumberFormatterCacheEntry() {
        }
    }

    private synchronized LocalizedNumberFormatter getUnitFormatterFromCache(int i, MeasureUnit measureUnit, MeasureUnit measureUnit2) {
        LocalizedNumberFormatter localizedNumberFormatter;
        if (this.formatter1 != null) {
            if (this.formatter1.type == i && this.formatter1.unit == measureUnit && this.formatter1.perUnit == measureUnit2) {
                return this.formatter1.formatter;
            } else if (this.formatter2 != null) {
                if (this.formatter2.type == i && this.formatter2.unit == measureUnit && this.formatter2.perUnit == measureUnit2) {
                    return this.formatter2.formatter;
                } else if (this.formatter3 != null && this.formatter3.type == i && this.formatter3.unit == measureUnit && this.formatter3.perUnit == measureUnit2) {
                    return this.formatter3.formatter;
                }
            }
        }
        if (i == 1) {
            localizedNumberFormatter = (LocalizedNumberFormatter) getNumberFormatter().unit(measureUnit).perUnit(measureUnit2).unitWidth(this.formatWidth.unitWidth);
        } else if (i == 2) {
            localizedNumberFormatter = (LocalizedNumberFormatter) NumberFormatter.withLocale(getLocale()).unit(measureUnit).perUnit(measureUnit2).unitWidth(this.formatWidth.currencyWidth);
        } else {
            localizedNumberFormatter = (LocalizedNumberFormatter) getNumberFormatter().unit(measureUnit).perUnit(measureUnit2).unitWidth(this.formatWidth.unitWidth).precision(Precision.integer().withMode(RoundingUtils.mathContextUnlimited(RoundingMode.DOWN)));
        }
        this.formatter3 = this.formatter2;
        this.formatter2 = this.formatter1;
        this.formatter1 = new NumberFormatterCacheEntry();
        this.formatter1.type = i;
        this.formatter1.unit = measureUnit;
        this.formatter1.perUnit = measureUnit2;
        this.formatter1.formatter = localizedNumberFormatter;
        return localizedNumberFormatter;
    }

    /* access modifiers changed from: package-private */
    public synchronized void clearCache() {
        this.formatter1 = null;
        this.formatter2 = null;
        this.formatter3 = null;
    }

    /* access modifiers changed from: package-private */
    public LocalizedNumberFormatter getNumberFormatter() {
        return this.numberFormatter;
    }

    private FormattedNumber formatMeasure(Measure measure) {
        MeasureUnit unit = measure.getUnit();
        if (unit instanceof Currency) {
            return getUnitFormatterFromCache(2, unit, null).format(measure.getNumber());
        }
        return getUnitFormatterFromCache(1, unit, null).format(measure.getNumber());
    }

    private FormattedNumber formatMeasureInteger(Measure measure) {
        return getUnitFormatterFromCache(3, measure.getUnit(), null).format(measure.getNumber());
    }

    private void formatMeasuresSlowTrack(ListFormatter listFormatter, Appendable appendable, FieldPosition fieldPosition, Measure... measureArr) {
        FormattedNumber formattedNumber;
        String[] strArr = new String[measureArr.length];
        FieldPosition fieldPosition2 = new FieldPosition(fieldPosition.getFieldAttribute(), fieldPosition.getField());
        int i = -1;
        for (int i2 = 0; i2 < measureArr.length; i2++) {
            if (i2 == measureArr.length - 1) {
                formattedNumber = formatMeasure(measureArr[i2]);
            } else {
                formattedNumber = formatMeasureInteger(measureArr[i2]);
            }
            if (i == -1) {
                formattedNumber.nextFieldPosition(fieldPosition2);
                if (fieldPosition2.getEndIndex() != 0) {
                    i = i2;
                }
            }
            strArr[i2] = formattedNumber.toString();
        }
        ListFormatter.FormattedListBuilder format = listFormatter.format(Arrays.asList(strArr), i);
        if (format.getOffset() != -1) {
            fieldPosition.setBeginIndex(fieldPosition2.getBeginIndex() + format.getOffset());
            fieldPosition.setEndIndex(fieldPosition2.getEndIndex() + format.getOffset());
        }
        format.appendTo(appendable);
    }

    private static String loadNumericDurationFormat(ICUResourceBundle iCUResourceBundle, String str) {
        return iCUResourceBundle.getWithFallback(String.format("durationUnits/%s", str)).getString().replace("h", DateFormat.HOUR24);
    }

    private static Number[] toHMS(Measure[] measureArr) {
        Integer num;
        int intValue;
        Number[] numberArr = new Number[3];
        int length = measureArr.length;
        int i = -1;
        int i2 = 0;
        while (i2 < length) {
            Measure measure = measureArr[i2];
            if (measure.getNumber().doubleValue() < 0.0d || (num = hmsTo012.get(measure.getUnit())) == null || (intValue = num.intValue()) <= i) {
                return null;
            }
            numberArr[intValue] = measure.getNumber();
            i2++;
            i = intValue;
        }
        return numberArr;
    }

    private void formatNumeric(Number[] numberArr, Appendable appendable) {
        String str;
        Integer num;
        int i;
        char c = 0;
        if (numberArr[0] != null && numberArr[2] != null) {
            str = this.numericFormatters.getHourMinuteSecond();
            if (numberArr[1] == null) {
                numberArr[1] = 0;
            }
            numberArr[1] = Double.valueOf(Math.floor(numberArr[1].doubleValue()));
            numberArr[0] = Double.valueOf(Math.floor(numberArr[0].doubleValue()));
        } else if (numberArr[0] != null && numberArr[1] != null) {
            str = this.numericFormatters.getHourMinute();
            numberArr[0] = Double.valueOf(Math.floor(numberArr[0].doubleValue()));
        } else if (numberArr[1] == null || numberArr[2] == null) {
            throw new IllegalStateException();
        } else {
            str = this.numericFormatters.getMinuteSecond();
            numberArr[1] = Double.valueOf(Math.floor(numberArr[1].doubleValue()));
        }
        LocalizedNumberFormatter integerWidth = this.numberFormatter.integerWidth(IntegerWidth.zeroFillTo(2));
        FormattedStringBuilder formattedStringBuilder = new FormattedStringBuilder();
        int i2 = 0;
        boolean z = false;
        while (i2 < str.length()) {
            char charAt = str.charAt(i2);
            if (charAt == 'H') {
                num = numberArr[c];
            } else if (charAt == 'm') {
                num = numberArr[1];
            } else if (charAt != 's') {
                num = 0;
            } else {
                num = numberArr[2];
            }
            if (charAt == '\'') {
                i = i2 + 1;
                if (i >= str.length() || str.charAt(i) != charAt) {
                    z = !z;
                    i2++;
                    c = 0;
                } else {
                    formattedStringBuilder.appendChar16(charAt, (Format.Field) null);
                }
            } else if (charAt == 'H' || charAt == 'm' || charAt == 's') {
                if (z) {
                    formattedStringBuilder.appendChar16(charAt, (Format.Field) null);
                } else {
                    i = i2 + 1;
                    if (i >= str.length() || str.charAt(i) != charAt) {
                        formattedStringBuilder.append(this.numberFormatter.format(num), (Format.Field) null);
                    } else {
                        formattedStringBuilder.append(integerWidth.format(num), (Format.Field) null);
                    }
                }
                i2++;
                c = 0;
            } else {
                formattedStringBuilder.appendChar16(charAt, (Format.Field) null);
                i2++;
                c = 0;
            }
            i2 = i;
            i2++;
            c = 0;
        }
        try {
            appendable.append((CharSequence) formattedStringBuilder);
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public Object toTimeUnitProxy() {
        return new MeasureProxy(getLocale(), this.formatWidth, getNumberFormatInternal(), 1);
    }

    /* access modifiers changed from: package-private */
    public Object toCurrencyProxy() {
        return new MeasureProxy(getLocale(), this.formatWidth, getNumberFormatInternal(), 2);
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureProxy(getLocale(), this.formatWidth, getNumberFormatInternal(), 0);
    }

    static class MeasureProxy implements Externalizable {
        private static final long serialVersionUID = -6033308329886716770L;
        private FormatWidth formatWidth;
        private HashMap<Object, Object> keyValues;
        private ULocale locale;
        private NumberFormat numberFormat;
        private int subClass;

        public MeasureProxy(ULocale uLocale, FormatWidth formatWidth2, NumberFormat numberFormat2, int i) {
            this.locale = uLocale;
            this.formatWidth = formatWidth2;
            this.numberFormat = numberFormat2;
            this.subClass = i;
            this.keyValues = new HashMap<>();
        }

        public MeasureProxy() {
        }

        @Override // java.io.Externalizable
        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            objectOutput.writeByte(0);
            objectOutput.writeUTF(this.locale.toLanguageTag());
            objectOutput.writeByte(this.formatWidth.ordinal());
            objectOutput.writeObject(this.numberFormat);
            objectOutput.writeByte(this.subClass);
            objectOutput.writeObject(this.keyValues);
        }

        @Override // java.io.Externalizable
        public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
            objectInput.readByte();
            this.locale = ULocale.forLanguageTag(objectInput.readUTF());
            this.formatWidth = MeasureFormat.fromFormatWidthOrdinal(objectInput.readByte() & 255);
            this.numberFormat = (NumberFormat) objectInput.readObject();
            if (this.numberFormat != null) {
                this.subClass = objectInput.readByte() & 255;
                this.keyValues = (HashMap) objectInput.readObject();
                if (this.keyValues == null) {
                    throw new InvalidObjectException("Missing optional values map.");
                }
                return;
            }
            throw new InvalidObjectException("Missing number format.");
        }

        private TimeUnitFormat createTimeUnitFormat() throws InvalidObjectException {
            int i;
            if (this.formatWidth == FormatWidth.WIDE) {
                i = 0;
            } else if (this.formatWidth == FormatWidth.SHORT) {
                i = 1;
            } else {
                throw new InvalidObjectException("Bad width: " + this.formatWidth);
            }
            TimeUnitFormat timeUnitFormat = new TimeUnitFormat(this.locale, i);
            timeUnitFormat.setNumberFormat(this.numberFormat);
            return timeUnitFormat;
        }

        private Object readResolve() throws ObjectStreamException {
            int i = this.subClass;
            if (i == 0) {
                return MeasureFormat.getInstance(this.locale, this.formatWidth, this.numberFormat);
            }
            if (i == 1) {
                return createTimeUnitFormat();
            }
            if (i == 2) {
                return MeasureFormat.getCurrencyFormat(this.locale);
            }
            throw new InvalidObjectException("Unknown subclass: " + this.subClass);
        }
    }

    /* access modifiers changed from: private */
    public static FormatWidth fromFormatWidthOrdinal(int i) {
        FormatWidth[] values = FormatWidth.values();
        if (i < 0 || i >= values.length) {
            return FormatWidth.SHORT;
        }
        return values[i];
    }

    @Deprecated
    public static String getRangeFormat(ULocale uLocale, FormatWidth formatWidth2) {
        String str;
        String str2;
        if (uLocale.getLanguage().equals("fr")) {
            return getRangeFormat(ULocale.ROOT, formatWidth2);
        }
        String str3 = localeIdToRangeFormat.get(uLocale);
        if (str3 == null) {
            ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", uLocale);
            ULocale uLocale2 = bundleInstance.getULocale();
            if (uLocale.equals(uLocale2) || (str2 = localeIdToRangeFormat.get(uLocale)) == null) {
                NumberingSystem instance = NumberingSystem.getInstance(uLocale);
                try {
                    str = bundleInstance.getStringWithFallback("NumberElements/" + instance.getName() + "/miscPatterns/range");
                } catch (MissingResourceException unused) {
                    str = bundleInstance.getStringWithFallback("NumberElements/latn/patterns/range");
                }
                str3 = SimpleFormatterImpl.compileToStringMinMaxArguments(str, new StringBuilder(), 2, 2);
                localeIdToRangeFormat.put(uLocale, str3);
                if (!uLocale.equals(uLocale2)) {
                    localeIdToRangeFormat.put(uLocale2, str3);
                }
            } else {
                localeIdToRangeFormat.put(uLocale, str2);
                return str2;
            }
        }
        return str3;
    }
}
