package ohos.global.icu.impl.number;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.text.CompactDecimalFormat;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class CompactData implements MultiplierProducer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int COMPACT_MAX_DIGITS = 15;
    private static final String USE_FALLBACK = "<USE FALLBACK>";
    private boolean isEmpty = true;
    private byte largestMagnitude = 0;
    private final byte[] multipliers = new byte[16];
    private final String[] patterns = new String[(StandardPlural.COUNT * 16)];

    public enum CompactType {
        DECIMAL,
        CURRENCY
    }

    public void populate(ULocale uLocale, String str, CompactDecimalFormat.CompactStyle compactStyle, CompactType compactType) {
        CompactDataSink compactDataSink = new CompactDataSink(this);
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        boolean equals = str.equals("latn");
        boolean z = compactStyle == CompactDecimalFormat.CompactStyle.SHORT;
        StringBuilder sb = new StringBuilder();
        getResourceBundleKey(str, compactStyle, compactType, sb);
        bundleInstance.getAllItemsWithFallbackNoFail(sb.toString(), compactDataSink);
        if (this.isEmpty && !equals) {
            getResourceBundleKey("latn", compactStyle, compactType, sb);
            bundleInstance.getAllItemsWithFallbackNoFail(sb.toString(), compactDataSink);
        }
        if (this.isEmpty && !z) {
            getResourceBundleKey(str, CompactDecimalFormat.CompactStyle.SHORT, compactType, sb);
            bundleInstance.getAllItemsWithFallbackNoFail(sb.toString(), compactDataSink);
        }
        if (this.isEmpty && !equals && !z) {
            getResourceBundleKey("latn", CompactDecimalFormat.CompactStyle.SHORT, compactType, sb);
            bundleInstance.getAllItemsWithFallbackNoFail(sb.toString(), compactDataSink);
        }
        if (this.isEmpty) {
            throw new ICUException("Could not load compact decimal data for locale " + uLocale);
        }
    }

    private static void getResourceBundleKey(String str, CompactDecimalFormat.CompactStyle compactStyle, CompactType compactType, StringBuilder sb) {
        sb.setLength(0);
        sb.append("NumberElements/");
        sb.append(str);
        sb.append(compactStyle == CompactDecimalFormat.CompactStyle.SHORT ? "/patternsShort" : "/patternsLong");
        sb.append(compactType == CompactType.DECIMAL ? "/decimalFormat" : "/currencyFormat");
    }

    public void populate(Map<String, Map<String, String>> map) {
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            byte length = (byte) (entry.getKey().length() - 1);
            for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                StandardPlural fromString = StandardPlural.fromString(entry2.getKey().toString());
                String str = entry2.getValue().toString();
                this.patterns[getIndex(length, fromString)] = str;
                int countZeros = countZeros(str);
                if (countZeros > 0) {
                    this.multipliers[length] = (byte) ((countZeros - length) - 1);
                    if (length > this.largestMagnitude) {
                        this.largestMagnitude = length;
                    }
                    this.isEmpty = false;
                }
            }
        }
    }

    @Override // ohos.global.icu.impl.number.MultiplierProducer
    public int getMultiplier(int i) {
        if (i < 0) {
            return 0;
        }
        byte b = this.largestMagnitude;
        if (i > b) {
            i = b;
        }
        return this.multipliers[i];
    }

    public String getPattern(int i, StandardPlural standardPlural) {
        if (i < 0) {
            return null;
        }
        byte b = this.largestMagnitude;
        if (i > b) {
            i = b;
        }
        String str = this.patterns[getIndex(i, standardPlural)];
        String str2 = (str != null || standardPlural == StandardPlural.OTHER) ? str : this.patterns[getIndex(i, StandardPlural.OTHER)];
        if (str2 == USE_FALLBACK) {
            return null;
        }
        return str2;
    }

    public void getUniquePatterns(Set<String> set) {
        set.addAll(Arrays.asList(this.patterns));
        set.remove(USE_FALLBACK);
        set.remove(null);
    }

    private static final class CompactDataSink extends UResource.Sink {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        CompactData data;

        public CompactDataSink(CompactData compactData) {
            this.data = compactData;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            int countZeros;
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                byte length = (byte) (key.length() - 1);
                byte b = this.data.multipliers[length];
                UResource.Table table2 = value.getTable();
                byte b2 = b;
                for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                    StandardPlural fromString = StandardPlural.fromString(key.toString());
                    if (this.data.patterns[CompactData.getIndex(length, fromString)] == null) {
                        String value2 = value.toString();
                        if (value2.equals("0")) {
                            value2 = CompactData.USE_FALLBACK;
                        }
                        this.data.patterns[CompactData.getIndex(length, fromString)] = value2;
                        if (b2 == 0 && (countZeros = CompactData.countZeros(value2)) > 0) {
                            b2 = (byte) ((countZeros - length) - 1);
                        }
                    }
                }
                if (this.data.multipliers[length] == 0) {
                    this.data.multipliers[length] = b2;
                    if (length > this.data.largestMagnitude) {
                        this.data.largestMagnitude = length;
                    }
                    this.data.isEmpty = false;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final int getIndex(int i, StandardPlural standardPlural) {
        return (i * StandardPlural.COUNT) + standardPlural.ordinal();
    }

    /* access modifiers changed from: private */
    public static final int countZeros(String str) {
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (str.charAt(i2) == '0') {
                i++;
            } else if (i > 0) {
                break;
            }
        }
        return i;
    }
}
