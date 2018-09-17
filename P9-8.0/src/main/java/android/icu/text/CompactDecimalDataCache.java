package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;

class CompactDecimalDataCache {
    private static final String CURRENCY_FORMAT = "currencyFormat";
    private static final String DECIMAL_FORMAT = "decimalFormat";
    private static final String LATIN_NUMBERING_SYSTEM = "latn";
    private static final String LONG_STYLE = "long";
    static final int MAX_DIGITS = 15;
    private static final String NUMBER_ELEMENTS = "NumberElements";
    public static final String OTHER = "other";
    private static final String PATTERNS_LONG = "patternsLong";
    private static final String PATTERNS_SHORT = "patternsShort";
    private static final String SHORT_CURRENCY_STYLE = "shortCurrency";
    private static final String SHORT_STYLE = "short";
    private final ICUCache<ULocale, DataBundle> cache = new SimpleCache();

    private static final class CompactDecimalDataSink extends Sink {
        private DataBundle dataBundle;
        private boolean isFallback;
        private boolean isLatin;
        private ULocale locale;

        public CompactDecimalDataSink(DataBundle dataBundle, ULocale locale) {
            this.dataBundle = dataBundle;
            this.locale = locale;
        }

        public void put(Key key, Value value, boolean isRoot) {
            if (!isRoot || (this.isLatin ^ 1) == 0) {
                Table patternsTable = value.getTable();
                for (int i1 = 0; patternsTable.getKeyAndValue(i1, key, value); i1++) {
                    PatternsTableKey patternsTableKey;
                    if (key.contentEquals(CompactDecimalDataCache.PATTERNS_SHORT)) {
                        patternsTableKey = PatternsTableKey.PATTERNS_SHORT;
                    } else {
                        if (key.contentEquals(CompactDecimalDataCache.PATTERNS_LONG)) {
                            patternsTableKey = PatternsTableKey.PATTERNS_LONG;
                        } else {
                            continue;
                        }
                    }
                    Table formatsTable = value.getTable();
                    for (int i2 = 0; formatsTable.getKeyAndValue(i2, key, value); i2++) {
                        String style;
                        Data destination;
                        FormatsTableKey formatsTableKey;
                        if (key.contentEquals(CompactDecimalDataCache.DECIMAL_FORMAT)) {
                            formatsTableKey = FormatsTableKey.DECIMAL_FORMAT;
                        } else {
                            if (key.contentEquals(CompactDecimalDataCache.CURRENCY_FORMAT)) {
                                formatsTableKey = FormatsTableKey.CURRENCY_FORMAT;
                            } else {
                                continue;
                            }
                        }
                        if (patternsTableKey == PatternsTableKey.PATTERNS_LONG && formatsTableKey == FormatsTableKey.DECIMAL_FORMAT) {
                            style = CompactDecimalDataCache.LONG_STYLE;
                            destination = this.dataBundle.longData;
                        } else if (patternsTableKey == PatternsTableKey.PATTERNS_SHORT && formatsTableKey == FormatsTableKey.DECIMAL_FORMAT) {
                            style = CompactDecimalDataCache.SHORT_STYLE;
                            destination = this.dataBundle.shortData;
                        } else {
                            if (patternsTableKey == PatternsTableKey.PATTERNS_SHORT && formatsTableKey == FormatsTableKey.CURRENCY_FORMAT) {
                                style = CompactDecimalDataCache.SHORT_CURRENCY_STYLE;
                                destination = this.dataBundle.shortCurrencyData;
                            }
                        }
                        if ((!this.isFallback || style != CompactDecimalDataCache.LONG_STYLE || (this.dataBundle.shortData.isEmpty() ^ 1) == 0 || (this.dataBundle.shortData.fromFallback ^ 1) == 0) && !(isRoot && style == CompactDecimalDataCache.LONG_STYLE && this.dataBundle.longData.isEmpty() && (this.dataBundle.shortData.isEmpty() ^ 1) != 0)) {
                            destination.fromFallback = this.isFallback;
                            Table powersOfTenTable = value.getTable();
                            for (int i3 = 0; powersOfTenTable.getKeyAndValue(i3, key, value); i3++) {
                                long power10 = Long.parseLong(key.toString());
                                int log10Value = (int) Math.log10((double) power10);
                                if (log10Value < 15) {
                                    Table pluralVariantsTable = value.getTable();
                                    for (int i4 = 0; pluralVariantsTable.getKeyAndValue(i4, key, value); i4++) {
                                        String pluralVariant = key.toString();
                                        String template = value.toString();
                                        int numZeros = CompactDecimalDataCache.populatePrefixSuffix(pluralVariant, log10Value, template, this.locale, style, destination, false);
                                        if (numZeros >= 0) {
                                            long divisor = CompactDecimalDataCache.calculateDivisor(power10, numZeros);
                                            if (destination.divisors[log10Value] == 0 || destination.divisors[log10Value] == divisor) {
                                                destination.divisors[log10Value] = divisor;
                                            } else {
                                                throw new IllegalArgumentException("Plural variant '" + pluralVariant + "' template '" + template + "' for 10^" + log10Value + " has wrong number of zeros in " + CompactDecimalDataCache.localeAndStyle(this.locale, style));
                                            }
                                        }
                                    }
                                    continue;
                                }
                            }
                            continue;
                        }
                    }
                    continue;
                }
            }
        }
    }

    static class Data {
        long[] divisors;
        boolean fromFallback;
        Map<String, Unit[]> units;

        Data(long[] divisors, Map<String, Unit[]> units) {
            this.divisors = divisors;
            this.units = units;
        }

        public boolean isEmpty() {
            return this.units != null ? this.units.isEmpty() : true;
        }
    }

    static class DataBundle {
        Data longData;
        Data shortCurrencyData;
        Data shortData;

        private DataBundle(Data shortData, Data longData, Data shortCurrencyData) {
            this.shortData = shortData;
            this.longData = longData;
            this.shortCurrencyData = shortCurrencyData;
        }

        private static DataBundle createEmpty() {
            return new DataBundle(new Data(new long[15], new HashMap()), new Data(new long[15], new HashMap()), new Data(new long[15], new HashMap()));
        }
    }

    private enum FormatsTableKey {
        DECIMAL_FORMAT,
        CURRENCY_FORMAT
    }

    private enum PatternsTableKey {
        PATTERNS_LONG,
        PATTERNS_SHORT
    }

    CompactDecimalDataCache() {
    }

    DataBundle get(ULocale locale) {
        DataBundle result = (DataBundle) this.cache.get(locale);
        if (result != null) {
            return result;
        }
        result = load(locale);
        this.cache.put(locale, result);
        return result;
    }

    private static DataBundle load(ULocale ulocale) throws MissingResourceException {
        DataBundle dataBundle = DataBundle.createEmpty();
        String nsName = NumberingSystem.getInstance(ulocale).getName();
        ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ulocale);
        CompactDecimalDataSink sink = new CompactDecimalDataSink(dataBundle, ulocale);
        sink.isFallback = false;
        if (!nsName.equals(LATIN_NUMBERING_SYSTEM)) {
            sink.isLatin = false;
            try {
                r.getAllItemsWithFallback("NumberElements/" + nsName, sink);
            } catch (MissingResourceException e) {
            }
            sink.isFallback = true;
        }
        sink.isLatin = true;
        r.getAllItemsWithFallback("NumberElements/latn", sink);
        if (dataBundle.longData.isEmpty()) {
            dataBundle.longData = dataBundle.shortData;
        }
        checkForOtherVariants(dataBundle.longData, ulocale, LONG_STYLE);
        checkForOtherVariants(dataBundle.shortData, ulocale, SHORT_STYLE);
        checkForOtherVariants(dataBundle.shortCurrencyData, ulocale, SHORT_CURRENCY_STYLE);
        fillInMissing(dataBundle.longData);
        fillInMissing(dataBundle.shortData);
        fillInMissing(dataBundle.shortCurrencyData);
        return dataBundle;
    }

    private static int populatePrefixSuffix(String pluralVariant, int idx, String template, ULocale locale, String style, Data destination, boolean overwrite) {
        int firstIdx = template.indexOf(AndroidHardcodedSystemProperties.JAVA_VERSION);
        int lastIdx = template.lastIndexOf(AndroidHardcodedSystemProperties.JAVA_VERSION);
        if (firstIdx == -1) {
            throw new IllegalArgumentException("Expect at least one zero in template '" + template + "' for variant '" + pluralVariant + "' for 10^" + idx + " in " + localeAndStyle(locale, style));
        }
        String prefix = template.substring(0, firstIdx);
        String suffix = template.substring(lastIdx + 1);
        if (!saveUnit(new Unit(prefix, suffix), pluralVariant, idx, destination.units, overwrite)) {
            return -1;
        }
        if (prefix.trim().length() == 0 && suffix.trim().length() == 0) {
            return idx + 1;
        }
        int i = firstIdx + 1;
        while (i <= lastIdx && template.charAt(i) == '0') {
            i++;
        }
        return i - firstIdx;
    }

    private static long calculateDivisor(long power10, int numZeros) {
        long divisor = power10;
        for (int i = 1; i < numZeros; i++) {
            divisor /= 10;
        }
        return divisor;
    }

    private static String localeAndStyle(ULocale locale, String style) {
        return "locale '" + locale + "' style '" + style + "'";
    }

    private static void checkForOtherVariants(Data data, ULocale locale, String style) {
        Unit[] otherByBase = (Unit[]) data.units.get("other");
        if (otherByBase == null) {
            throw new IllegalArgumentException("No 'other' plural variants defined in " + localeAndStyle(locale, style));
        }
        for (Entry<String, Unit[]> entry : data.units.entrySet()) {
            if (entry.getKey() != "other") {
                Unit[] variantByBase = (Unit[]) entry.getValue();
                int log10Value = 0;
                while (log10Value < 15) {
                    if (variantByBase[log10Value] == null || otherByBase[log10Value] != null) {
                        log10Value++;
                    } else {
                        throw new IllegalArgumentException("No 'other' plural variant defined for 10^" + log10Value + " but a '" + ((String) entry.getKey()) + "' variant is defined" + " in " + localeAndStyle(locale, style));
                    }
                }
                continue;
            }
        }
    }

    private static void fillInMissing(Data result) {
        long lastDivisor = 1;
        for (int i = 0; i < result.divisors.length; i++) {
            if (((Unit[]) result.units.get("other"))[i] == null) {
                result.divisors[i] = lastDivisor;
                copyFromPreviousIndex(i, result.units);
            } else {
                lastDivisor = result.divisors[i];
                propagateOtherToMissing(i, result.units);
            }
        }
    }

    private static void propagateOtherToMissing(int idx, Map<String, Unit[]> units) {
        Unit otherVariantValue = ((Unit[]) units.get("other"))[idx];
        for (Unit[] byBase : units.values()) {
            if (byBase[idx] == null) {
                byBase[idx] = otherVariantValue;
            }
        }
    }

    private static void copyFromPreviousIndex(int idx, Map<String, Unit[]> units) {
        for (Unit[] byBase : units.values()) {
            if (idx == 0) {
                byBase[idx] = DecimalFormat.NULL_UNIT;
            } else {
                byBase[idx] = byBase[idx - 1];
            }
        }
    }

    private static boolean saveUnit(Unit unit, String pluralVariant, int idx, Map<String, Unit[]> units, boolean overwrite) {
        Unit[] byBase = (Unit[]) units.get(pluralVariant);
        if (byBase == null) {
            byBase = new Unit[15];
            units.put(pluralVariant, byBase);
        }
        if (!overwrite && byBase[idx] != null) {
            return false;
        }
        byBase[idx] = unit;
        return true;
    }

    static Unit getUnit(Map<String, Unit[]> units, String variant, int base) {
        Unit[] byBase = (Unit[]) units.get(variant);
        if (byBase == null) {
            byBase = (Unit[]) units.get("other");
        }
        return byBase[base];
    }
}
