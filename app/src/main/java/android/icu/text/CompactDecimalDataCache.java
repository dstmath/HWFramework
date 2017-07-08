package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.SimpleCache;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

class CompactDecimalDataCache {
    private static final /* synthetic */ int[] -android-icu-text-CompactDecimalDataCache$QuoteStateSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-text-CompactDecimalDataCache$UResFlagsSwitchesValues = null;
    private static final String LATIN_NUMBERING_SYSTEM = "latn";
    private static final String LONG_STYLE = "long";
    static final int MAX_DIGITS = 15;
    private static final String NUMBER_ELEMENTS = "NumberElements";
    static final String OTHER = "other";
    private static final String PATTERNS_SHORT_PATH = "patternsShort/decimalFormat";
    private static final String PATTERN_LONG_PATH = "patternsLong/decimalFormat";
    private static final String SHORT_STYLE = "short";
    private final ICUCache<ULocale, DataBundle> cache;

    static class Data {
        long[] divisors;
        Map<String, Unit[]> units;

        Data(long[] divisors, Map<String, Unit[]> units) {
            this.divisors = divisors;
            this.units = units;
        }
    }

    static class DataBundle {
        Data longData;
        Data shortData;

        DataBundle(Data shortData, Data longData) {
            this.shortData = shortData;
            this.longData = longData;
        }
    }

    private enum QuoteState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CompactDecimalDataCache.QuoteState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CompactDecimalDataCache.QuoteState.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CompactDecimalDataCache.QuoteState.<clinit>():void");
        }
    }

    private enum UResFlags {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CompactDecimalDataCache.UResFlags.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CompactDecimalDataCache.UResFlags.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CompactDecimalDataCache.UResFlags.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-text-CompactDecimalDataCache$QuoteStateSwitchesValues() {
        if (-android-icu-text-CompactDecimalDataCache$QuoteStateSwitchesValues != null) {
            return -android-icu-text-CompactDecimalDataCache$QuoteStateSwitchesValues;
        }
        int[] iArr = new int[QuoteState.values().length];
        try {
            iArr[QuoteState.INSIDE_EMPTY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[QuoteState.INSIDE_FULL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[QuoteState.OUTSIDE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-icu-text-CompactDecimalDataCache$QuoteStateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-text-CompactDecimalDataCache$UResFlagsSwitchesValues() {
        if (-android-icu-text-CompactDecimalDataCache$UResFlagsSwitchesValues != null) {
            return -android-icu-text-CompactDecimalDataCache$UResFlagsSwitchesValues;
        }
        int[] iArr = new int[UResFlags.values().length];
        try {
            iArr[UResFlags.ANY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[UResFlags.NOT_ROOT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-icu-text-CompactDecimalDataCache$UResFlagsSwitchesValues = iArr;
        return iArr;
    }

    CompactDecimalDataCache() {
        this.cache = new SimpleCache();
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

    private static DataBundle load(ULocale ulocale) {
        Data longData;
        NumberingSystem ns = NumberingSystem.getInstance(ulocale);
        ICUResourceBundle r = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, ulocale)).getWithFallback(NUMBER_ELEMENTS);
        String numberingSystemName = ns.getName();
        ICUResourceBundle iCUResourceBundle = null;
        ICUResourceBundle longDataBundle = null;
        if (!LATIN_NUMBERING_SYSTEM.equals(numberingSystemName)) {
            ICUResourceBundle bundle = findWithFallback(r, numberingSystemName, UResFlags.NOT_ROOT);
            iCUResourceBundle = findWithFallback(bundle, PATTERNS_SHORT_PATH, UResFlags.NOT_ROOT);
            longDataBundle = findWithFallback(bundle, PATTERN_LONG_PATH, UResFlags.NOT_ROOT);
        }
        if (iCUResourceBundle == null) {
            bundle = getWithFallback(r, LATIN_NUMBERING_SYSTEM, UResFlags.ANY);
            iCUResourceBundle = getWithFallback(bundle, PATTERNS_SHORT_PATH, UResFlags.ANY);
            if (longDataBundle == null) {
                longDataBundle = findWithFallback(bundle, PATTERN_LONG_PATH, UResFlags.ANY);
                if (!(longDataBundle == null || !isRoot(longDataBundle) || isRoot(iCUResourceBundle))) {
                    longDataBundle = null;
                }
            }
        }
        Data shortData = loadStyle(iCUResourceBundle, ulocale, SHORT_STYLE);
        if (longDataBundle == null) {
            longData = shortData;
        } else {
            longData = loadStyle(longDataBundle, ulocale, LONG_STYLE);
        }
        return new DataBundle(shortData, longData);
    }

    private static ICUResourceBundle findWithFallback(ICUResourceBundle r, String path, UResFlags flags) {
        if (r == null) {
            return null;
        }
        ICUResourceBundle result = r.findWithFallback(path);
        if (result == null) {
            return null;
        }
        switch (-getandroid-icu-text-CompactDecimalDataCache$UResFlagsSwitchesValues()[flags.ordinal()]) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return result;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                if (isRoot(result)) {
                    result = null;
                }
                return result;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static ICUResourceBundle getWithFallback(ICUResourceBundle r, String path, UResFlags flags) {
        ICUResourceBundle result = findWithFallback(r, path, flags);
        if (result != null) {
            return result;
        }
        throw new MissingResourceException("Cannot find " + path, ICUResourceBundle.class.getName(), path);
    }

    private static boolean isRoot(ICUResourceBundle r) {
        ULocale bundleLocale = r.getULocale();
        return !bundleLocale.equals(ULocale.ROOT) ? bundleLocale.toString().equals("root") : true;
    }

    private static Data loadStyle(ICUResourceBundle r, ULocale locale, String style) {
        int size = r.getSize();
        Data result = new Data(new long[MAX_DIGITS], new HashMap());
        for (int i = 0; i < size; i++) {
            populateData(r.get(i), locale, style, result);
        }
        fillInMissing(result);
        return result;
    }

    private static void populateData(UResourceBundle divisorData, ULocale locale, String style, Data result) {
        long magnitude = Long.parseLong(divisorData.getKey());
        int thisIndex = (int) Math.log10((double) magnitude);
        if (thisIndex < MAX_DIGITS) {
            int i;
            int size = divisorData.getSize();
            int numZeros = 0;
            boolean otherVariantDefined = false;
            for (i = 0; i < size; i++) {
                UResourceBundle pluralVariantData = divisorData.get(i);
                String pluralVariant = pluralVariantData.getKey();
                String template = pluralVariantData.getString();
                if (pluralVariant.equals(OTHER)) {
                    otherVariantDefined = true;
                }
                int nz = populatePrefixSuffix(pluralVariant, thisIndex, template, locale, style, result);
                if (nz != numZeros) {
                    if (numZeros != 0) {
                        throw new IllegalArgumentException("Plural variant '" + pluralVariant + "' template '" + template + "' for 10^" + thisIndex + " has wrong number of zeros in " + localeAndStyle(locale, style));
                    }
                    numZeros = nz;
                }
            }
            if (otherVariantDefined) {
                long divisor = magnitude;
                for (i = 1; i < numZeros; i++) {
                    divisor /= 10;
                }
                result.divisors[thisIndex] = divisor;
                return;
            }
            throw new IllegalArgumentException("No 'other' plural variant defined for 10^" + thisIndex + "in " + localeAndStyle(locale, style));
        }
    }

    private static int populatePrefixSuffix(String pluralVariant, int idx, String template, ULocale locale, String style, Data result) {
        int firstIdx = template.indexOf(AndroidHardcodedSystemProperties.JAVA_VERSION);
        int lastIdx = template.lastIndexOf(AndroidHardcodedSystemProperties.JAVA_VERSION);
        if (firstIdx == -1) {
            throw new IllegalArgumentException("Expect at least one zero in template '" + template + "' for variant '" + pluralVariant + "' for 10^" + idx + " in " + localeAndStyle(locale, style));
        }
        String prefix = fixQuotes(template.substring(0, firstIdx));
        String suffix = fixQuotes(template.substring(lastIdx + 1));
        saveUnit(new Unit(prefix, suffix), pluralVariant, idx, result.units);
        if (prefix.trim().length() == 0 && suffix.trim().length() == 0) {
            return idx + 1;
        }
        int i = firstIdx + 1;
        while (i <= lastIdx && template.charAt(i) == '0') {
            i++;
        }
        return i - firstIdx;
    }

    private static String fixQuotes(String prefixOrSuffix) {
        StringBuilder result = new StringBuilder();
        int len = prefixOrSuffix.length();
        QuoteState state = QuoteState.OUTSIDE;
        for (int idx = 0; idx < len; idx++) {
            char ch = prefixOrSuffix.charAt(idx);
            if (ch != PatternTokenizer.SINGLE_QUOTE) {
                result.append(ch);
            } else if (state == QuoteState.INSIDE_EMPTY) {
                result.append(PatternTokenizer.SINGLE_QUOTE);
            }
            switch (-getandroid-icu-text-CompactDecimalDataCache$QuoteStateSwitchesValues()[state.ordinal()]) {
                case NodeFilter.SHOW_ELEMENT /*1*/:
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    if (ch != PatternTokenizer.SINGLE_QUOTE) {
                        state = QuoteState.INSIDE_FULL;
                        break;
                    }
                    state = QuoteState.OUTSIDE;
                    break;
                case XmlPullParser.END_TAG /*3*/:
                    if (ch != PatternTokenizer.SINGLE_QUOTE) {
                        state = QuoteState.OUTSIDE;
                        break;
                    }
                    state = QuoteState.INSIDE_EMPTY;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return result.toString();
    }

    private static String localeAndStyle(ULocale locale, String style) {
        return "locale '" + locale + "' style '" + style + "'";
    }

    private static void fillInMissing(Data result) {
        long lastDivisor = 1;
        for (int i = 0; i < result.divisors.length; i++) {
            if (((Unit[]) result.units.get(OTHER))[i] == null) {
                result.divisors[i] = lastDivisor;
                copyFromPreviousIndex(i, result.units);
            } else {
                lastDivisor = result.divisors[i];
                propagateOtherToMissing(i, result.units);
            }
        }
    }

    private static void propagateOtherToMissing(int idx, Map<String, Unit[]> units) {
        Unit otherVariantValue = ((Unit[]) units.get(OTHER))[idx];
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

    private static void saveUnit(Unit unit, String pluralVariant, int idx, Map<String, Unit[]> units) {
        Unit[] byBase = (Unit[]) units.get(pluralVariant);
        if (byBase == null) {
            byBase = new Unit[MAX_DIGITS];
            units.put(pluralVariant, byBase);
        }
        byBase[idx] = unit;
    }

    static Unit getUnit(Map<String, Unit[]> units, String variant, int base) {
        Unit[] byBase = (Unit[]) units.get(variant);
        if (byBase == null) {
            byBase = (Unit[]) units.get(OTHER);
        }
        return byBase[base];
    }
}
