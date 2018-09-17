package android.icu.text;

import android.icu.text.PluralRules.FixedDecimal;
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
import org.w3c.dom.traversal.NodeFilter;

public class CompactDecimalFormat extends DecimalFormat {
    private static final /* synthetic */ int[] -android-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues = null;
    private static final CompactDecimalDataCache cache = null;
    private static final long serialVersionUID = 4716293295276629682L;
    private final long[] divisor;
    private final PluralRules pluralRules;
    private final Map<String, Unit> pluralToCurrencyAffixes;
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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CompactDecimalFormat.CompactStyle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CompactDecimalFormat.CompactStyle.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CompactDecimalFormat.CompactStyle.<clinit>():void");
        }
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CompactDecimalFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CompactDecimalFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CompactDecimalFormat.<clinit>():void");
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
        this.units = data.units;
        this.divisor = data.divisors;
        this.pluralToCurrencyAffixes = null;
        finishInit(style, format.toPattern(), format.getDecimalFormatSymbols());
    }

    @Deprecated
    public CompactDecimalFormat(String pattern, DecimalFormatSymbols formatSymbols, CompactStyle style, PluralRules pluralRules, long[] divisor, Map<String, String[][]> pluralAffixes, Map<String, String[]> currencyAffixes, Collection<String> debugCreationErrors) {
        this.pluralRules = pluralRules;
        this.units = otherPluralVariant(pluralAffixes, divisor, debugCreationErrors);
        if (!pluralRules.getKeywords().equals(this.units.keySet())) {
            debugCreationErrors.add("Missmatch in pluralCategories, should be: " + pluralRules.getKeywords() + ", was actually " + this.units.keySet());
        }
        this.divisor = (long[]) divisor.clone();
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
                if (!Arrays.equals((Object[]) entry.getValue(), value)) {
                }
            }
            return false;
        }
        return true;
    }

    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        Output<Unit> currencyUnit = new Output();
        Amount amount = toAmount(number, currencyUnit);
        if (currencyUnit.value != null) {
            ((Unit) currencyUnit.value).writePrefix(toAppendTo);
        }
        Unit unit = amount.getUnit();
        unit.writePrefix(toAppendTo);
        super.format(amount.getQty(), toAppendTo, pos);
        unit.writeSuffix(toAppendTo);
        if (currencyUnit.value != null) {
            ((Unit) currencyUnit.value).writeSuffix(toAppendTo);
        }
        return toAppendTo;
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj instanceof Number) {
            Amount amount = toAmount(((Number) obj).doubleValue(), null);
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

    public Number parse(String text, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException();
    }

    private Amount toAmount(double number, Output<Unit> currencyUnit) {
        boolean negative = isNumberNegative(number);
        number = adjustNumberAsInFormatting(number);
        int base = number <= 1.0d ? 0 : (int) Math.log10(number);
        if (base >= 15) {
            base = 14;
        }
        number /= (double) this.divisor[base];
        String pluralVariant = getPluralForm(getFixedDecimal(number, toDigitList(number)));
        if (!(this.pluralToCurrencyAffixes == null || currencyUnit == null)) {
            currencyUnit.value = (Unit) this.pluralToCurrencyAffixes.get(pluralVariant);
        }
        if (negative) {
            number = -number;
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
        int length = divisor.length;
        if (r0 < 15) {
            recordError(debugCreationErrors, "Must have at least 15 prefix items.");
        }
        long oldDivisor = 0;
        int i = 0;
        while (true) {
            length = divisor.length;
            if (i >= r0) {
                break;
            }
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
            i++;
        }
        Map<String, Unit[]> result = new HashMap();
        Map<String, Integer> seen = new HashMap();
        String[][] defaultPower10ToAffix = (String[][]) pluralCategoryToPower10ToAffix.get(PluralRules.KEYWORD_OTHER);
        for (Entry<String, String[][]> pluralCategoryAndPower10ToAffix : pluralCategoryToPower10ToAffix.entrySet()) {
            String pluralCategory = (String) pluralCategoryAndPower10ToAffix.getKey();
            String[][] power10ToAffix = (String[][]) pluralCategoryAndPower10ToAffix.getValue();
            if (power10ToAffix.length != divisor.length) {
                recordError(debugCreationErrors, "Prefixes & suffixes must be present for all divisors " + pluralCategory);
            }
            Object units = new Unit[power10ToAffix.length];
            i = 0;
            while (true) {
                length = power10ToAffix.length;
                if (i >= r0) {
                    break;
                }
                String[] pair = power10ToAffix[i];
                if (pair == null) {
                    pair = defaultPower10ToAffix[i];
                }
                length = pair.length;
                if (r0 != 2 || pair[0] == null || pair[1] == null) {
                    recordError(debugCreationErrors, "Prefix or suffix is null for " + pluralCategory + ", " + i + ", " + Arrays.asList(pair));
                } else {
                    String key = pair[0] + "\uffff" + pair[1] + "\uffff" + (i - ((int) Math.log10((double) divisor[i])));
                    Integer old = (Integer) seen.get(key);
                    if (old == null) {
                        seen.put(key, Integer.valueOf(i));
                    } else if (old.intValue() != i) {
                        recordError(debugCreationErrors, "Collision between values for " + i + " and " + old + " for [prefix/suffix/index-log(divisor)" + key.replace(UnicodeMatcher.ETHER, ';'));
                    }
                    units[i] = new Unit(pair[0], pair[1]);
                }
                i++;
            }
            result.put(pluralCategory, units);
        }
        return result;
    }

    private String getPluralForm(FixedDecimal fixedDecimal) {
        if (this.pluralRules == null) {
            return PluralRules.KEYWORD_OTHER;
        }
        return this.pluralRules.select(fixedDecimal);
    }

    private Data getData(ULocale locale, CompactStyle style) {
        DataBundle bundle = cache.get(locale);
        switch (-getandroid-icu-text-CompactDecimalFormat$CompactStyleSwitchesValues()[style.ordinal()]) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return bundle.longData;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return bundle.shortData;
            default:
                return bundle.shortData;
        }
    }
}
