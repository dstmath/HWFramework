package android.icu.text;

import android.icu.text.PluralRules.FixedDecimal;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.Output;
import android.icu.util.ULocale;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class CompactDecimalFormat extends DecimalFormat {
    private static final /* synthetic */ int[] -android-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues = null;
    private static final Pattern UNESCAPE_QUOTE = Pattern.compile("((?<!'))'");
    private static final CompactDecimalDataCache cache = new CompactDecimalDataCache();
    private static final long serialVersionUID = 4716293295276629682L;
    private final long[] currencyDivisor;
    private final Map<String, Unit[]> currencyUnits;
    private final long[] divisor;
    private final PluralRules pluralRules;
    private final Map<String, Unit> pluralToCurrencyAffixes;
    private CompactStyle style;
    private final Map<String, Unit[]> units;

    private static class Amount {
        private final double qty;
        private final Unit unit;

        public Amount(double qty, Unit unit) {
            this.qty = qty;
            this.unit = unit;
        }

        public double getQty() {
            return this.qty;
        }

        public Unit getUnit() {
            return this.unit;
        }
    }

    public enum CompactStyle {
        SHORT,
        LONG
    }

    private static /* synthetic */ int[] -getandroid-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues() {
        if (-android-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues != null) {
            return -android-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues;
        }
        int[] iArr = new int[CompactStyle.values().length];
        try {
            iArr[CompactStyle.LONG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CompactStyle.SHORT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues = iArr;
        return iArr;
    }

    public static CompactDecimalFormat getInstance(ULocale locale, CompactStyle style) {
        return new CompactDecimalFormat(locale, style);
    }

    public static CompactDecimalFormat getInstance(Locale locale, CompactStyle style) {
        return new CompactDecimalFormat(ULocale.forLocale(locale), style);
    }

    CompactDecimalFormat(ULocale locale, CompactStyle style) {
        this.pluralRules = PluralRules.forLocale(locale);
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(locale);
        Data data = getData(locale, style);
        Data currencyData = getCurrencyData(locale);
        this.units = data.units;
        this.divisor = data.divisors;
        this.currencyUnits = currencyData.units;
        this.currencyDivisor = currencyData.divisors;
        this.style = style;
        this.pluralToCurrencyAffixes = null;
        finishInit(style, format.toPattern(), format.getDecimalFormatSymbols());
    }

    @Deprecated
    public CompactDecimalFormat(String pattern, DecimalFormatSymbols formatSymbols, CompactStyle style, PluralRules pluralRules, long[] divisor, Map<String, String[][]> pluralAffixes, Map<String, String[]> currencyAffixes, Collection<String> debugCreationErrors) {
        this.pluralRules = pluralRules;
        this.units = otherPluralVariant(pluralAffixes, divisor, debugCreationErrors);
        this.currencyUnits = otherPluralVariant(pluralAffixes, divisor, debugCreationErrors);
        if (!pluralRules.getKeywords().equals(this.units.keySet())) {
            debugCreationErrors.add("Missmatch in pluralCategories, should be: " + pluralRules.getKeywords() + ", was actually " + this.units.keySet());
        }
        this.divisor = (long[]) divisor.clone();
        this.currencyDivisor = (long[]) divisor.clone();
        if (currencyAffixes == null) {
            this.pluralToCurrencyAffixes = null;
        } else {
            this.pluralToCurrencyAffixes = new HashMap();
            for (Entry<String, String[]> s : currencyAffixes.entrySet()) {
                String[] pair = (String[]) s.getValue();
                this.pluralToCurrencyAffixes.put((String) s.getKey(), new Unit(pair[0], pair[1]));
            }
        }
        finishInit(style, pattern, formatSymbols);
    }

    private void finishInit(CompactStyle style, String pattern, DecimalFormatSymbols formatSymbols) {
        applyPattern(pattern);
        setDecimalFormatSymbols(formatSymbols);
        setMaximumSignificantDigits(2);
        setSignificantDigitsUsed(true);
        if (style == CompactStyle.SHORT) {
            setGroupingUsed(false);
        }
        setCurrency(null);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !super.equals(obj)) {
            return false;
        }
        CompactDecimalFormat other = (CompactDecimalFormat) obj;
        if (mapsAreEqual(this.units, other.units) && Arrays.equals(this.divisor, other.divisor) && (this.pluralToCurrencyAffixes == other.pluralToCurrencyAffixes || (this.pluralToCurrencyAffixes != null && this.pluralToCurrencyAffixes.equals(other.pluralToCurrencyAffixes)))) {
            z = this.pluralRules.equals(other.pluralRules);
        }
        return z;
    }

    private boolean mapsAreEqual(Map<String, Unit[]> lhs, Map<String, Unit[]> rhs) {
        if (lhs.size() != rhs.size()) {
            return false;
        }
        for (Entry<String, Unit[]> entry : lhs.entrySet()) {
            Unit[] value = (Unit[]) rhs.get(entry.getKey());
            if (value != null) {
                if ((Arrays.equals((Object[]) entry.getValue(), value) ^ 1) != 0) {
                }
            }
            return false;
        }
        return true;
    }

    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number, null, toAppendTo, pos);
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj instanceof Number) {
            Amount amount = toAmount(((Number) obj).doubleValue(), null, null);
            return super.formatToCharacterIterator(Double.valueOf(amount.getQty()), amount.getUnit());
        }
        throw new IllegalArgumentException();
    }

    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        return format((double) number, toAppendTo, pos);
    }

    public StringBuffer format(BigInteger number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number.doubleValue(), toAppendTo, pos);
    }

    public StringBuffer format(BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number.doubleValue(), toAppendTo, pos);
    }

    public StringBuffer format(android.icu.math.BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number.doubleValue(), toAppendTo, pos);
    }

    @Deprecated
    public StringBuffer format(CurrencyAmount currAmt, StringBuffer toAppendTo, FieldPosition pos) {
        return format(currAmt.getNumber().doubleValue(), currAmt.getCurrency(), toAppendTo, pos);
    }

    public Number parse(String text, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException();
    }

    private StringBuffer format(double number, Currency curr, StringBuffer toAppendTo, FieldPosition pos) {
        if (curr == null || this.style != CompactStyle.LONG) {
            Output<Unit> currencyUnit = new Output();
            Amount amount = toAmount(number, curr, currencyUnit);
            Unit unit = amount.getUnit();
            StringBuffer prefix = new StringBuffer();
            StringBuffer suffix = new StringBuffer();
            if (currencyUnit.value != null) {
                ((Unit) currencyUnit.value).writePrefix(prefix);
            }
            unit.writePrefix(prefix);
            unit.writeSuffix(suffix);
            if (currencyUnit.value != null) {
                ((Unit) currencyUnit.value).writeSuffix(suffix);
            }
            if (curr == null) {
                toAppendTo.append(escape(prefix.toString()));
                super.format(amount.getQty(), toAppendTo, pos);
                toAppendTo.append(escape(suffix.toString()));
            } else {
                synchronized (this) {
                    String originalPattern = toPattern();
                    Currency originalCurrency = getCurrency();
                    StringBuffer newPattern = new StringBuffer();
                    int semicolonPos = originalPattern.indexOf(59);
                    newPattern.append(prefix);
                    if (semicolonPos != -1) {
                        newPattern.append(originalPattern, 0, semicolonPos);
                        newPattern.append(suffix);
                        newPattern.append(';');
                        newPattern.append(prefix);
                    }
                    newPattern.append(originalPattern, semicolonPos + 1, originalPattern.length());
                    newPattern.append(suffix);
                    setCurrency(curr);
                    applyPattern(newPattern.toString());
                    super.format(amount.getQty(), toAppendTo, pos);
                    setCurrency(originalCurrency);
                    applyPattern(originalPattern);
                }
            }
            return toAppendTo;
        }
        throw new UnsupportedOperationException("CompactDecimalFormat does not support LONG style for currency.");
    }

    private static String escape(String string) {
        if (string.indexOf(39) >= 0) {
            return UNESCAPE_QUOTE.matcher(string).replaceAll("$1");
        }
        return string;
    }

    private Amount toAmount(double number, Currency curr, Output<Unit> currencyUnit) {
        boolean negative = isNumberNegative(number);
        number = adjustNumberAsInFormatting(number);
        int base = number <= 1.0d ? 0 : (int) Math.log10(number);
        if (base >= 15) {
            base = 14;
        }
        if (curr != null) {
            number /= (double) this.currencyDivisor[base];
        } else {
            number /= (double) this.divisor[base];
        }
        String pluralVariant = getPluralForm(getFixedDecimal(number, toDigitList(number)));
        if (!(this.pluralToCurrencyAffixes == null || currencyUnit == null)) {
            currencyUnit.value = (Unit) this.pluralToCurrencyAffixes.get(pluralVariant);
        }
        if (negative) {
            number = -number;
        }
        if (curr != null) {
            return new Amount(number, CompactDecimalDataCache.getUnit(this.currencyUnits, pluralVariant, base));
        }
        return new Amount(number, CompactDecimalDataCache.getUnit(this.units, pluralVariant, base));
    }

    private void recordError(Collection<String> creationErrors, String errorMessage) {
        if (creationErrors == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        creationErrors.add(errorMessage);
    }

    private Map<String, Unit[]> otherPluralVariant(Map<String, String[][]> pluralCategoryToPower10ToAffix, long[] divisor, Collection<String> debugCreationErrors) {
        int i;
        if (divisor.length < 15) {
            recordError(debugCreationErrors, "Must have at least 15 prefix items.");
        }
        long oldDivisor = 0;
        for (i = 0; i < divisor.length; i++) {
            int log = (int) Math.log10((double) divisor[i]);
            if (log > i) {
                recordError(debugCreationErrors, "Divisor[" + i + "] must be less than or equal to 10^" + i + ", but is: " + divisor[i]);
            }
            if (((long) Math.pow(10.0d, (double) log)) != divisor[i]) {
                recordError(debugCreationErrors, "Divisor[" + i + "] must be a power of 10, but is: " + divisor[i]);
            }
            if (divisor[i] < oldDivisor) {
                recordError(debugCreationErrors, "Bad divisor, the divisor for 10E" + i + "(" + divisor[i] + ") is less than the divisor for the divisor for 10E" + (i - 1) + "(" + oldDivisor + ")");
            }
            oldDivisor = divisor[i];
        }
        Map<String, Unit[]> result = new HashMap();
        Map<String, Integer> seen = new HashMap();
        String[][] defaultPower10ToAffix = (String[][]) pluralCategoryToPower10ToAffix.get("other");
        for (Entry<String, String[][]> pluralCategoryAndPower10ToAffix : pluralCategoryToPower10ToAffix.entrySet()) {
            String pluralCategory = (String) pluralCategoryAndPower10ToAffix.getKey();
            String[][] power10ToAffix = (String[][]) pluralCategoryAndPower10ToAffix.getValue();
            if (power10ToAffix.length != divisor.length) {
                recordError(debugCreationErrors, "Prefixes & suffixes must be present for all divisors " + pluralCategory);
            }
            Object units = new Unit[power10ToAffix.length];
            for (i = 0; i < power10ToAffix.length; i++) {
                String[] pair = power10ToAffix[i];
                if (pair == null) {
                    pair = defaultPower10ToAffix[i];
                }
                if (pair.length != 2 || pair[0] == null || pair[1] == null) {
                    recordError(debugCreationErrors, "Prefix or suffix is null for " + pluralCategory + ", " + i + ", " + Arrays.asList(pair));
                } else {
                    String key = pair[0] + "￿" + pair[1] + "￿" + (i - ((int) Math.log10((double) divisor[i])));
                    Integer old = (Integer) seen.get(key);
                    if (old == null) {
                        seen.put(key, Integer.valueOf(i));
                    } else if (old.intValue() != i) {
                        recordError(debugCreationErrors, "Collision between values for " + i + " and " + old + " for [prefix/suffix/index-log(divisor)" + key.replace(65535, ';'));
                    }
                    units[i] = new Unit(pair[0], pair[1]);
                }
            }
            result.put(pluralCategory, units);
        }
        return result;
    }

    private String getPluralForm(FixedDecimal fixedDecimal) {
        if (this.pluralRules == null) {
            return "other";
        }
        return this.pluralRules.select(fixedDecimal);
    }

    private Data getData(ULocale locale, CompactStyle style) {
        DataBundle bundle = cache.get(locale);
        switch (-getandroid-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues()[style.ordinal()]) {
            case 1:
                return bundle.longData;
            case 2:
                return bundle.shortData;
            default:
                return bundle.shortData;
        }
    }

    private Data getCurrencyData(ULocale locale) {
        return cache.get(locale).shortCurrencyData;
    }
}
