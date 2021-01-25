package ohos.global.icu.impl.locale;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.text.Bidi;
import ohos.global.icu.util.BytesTrie;
import ohos.global.icu.util.LocaleMatcher;
import ohos.global.icu.util.ULocale;

public class LocaleDistance {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ABOVE_THRESHOLD = 100;
    private static final boolean DEBUG_OUTPUT = false;
    private static final int DISTANCE_IS_FINAL = 256;
    private static final int DISTANCE_IS_FINAL_OR_SKIP_SCRIPT = 384;
    public static final int DISTANCE_SKIP_SCRIPT = 128;
    public static final int END_OF_SUBTAG = 128;
    public static final LocaleDistance INSTANCE = new LocaleDistance(Data.load());
    public static final int IX_DEF_LANG_DISTANCE = 0;
    public static final int IX_DEF_REGION_DISTANCE = 2;
    public static final int IX_DEF_SCRIPT_DISTANCE = 1;
    public static final int IX_LIMIT = 4;
    public static final int IX_MIN_REGION_DISTANCE = 3;
    private final int defaultDemotionPerDesiredLocale = (getBestIndexAndDistance(new LSR("en", "Latn", "US"), new LSR[]{new LSR("en", "Latn", "GB")}, 50, LocaleMatcher.FavorSubtag.LANGUAGE) & 255);
    private final int defaultLanguageDistance;
    private final int defaultRegionDistance;
    private final int defaultScriptDistance;
    private final int minRegionDistance;
    private final Set<LSR> paradigmLSRs;
    private final String[] partitionArrays;
    private final byte[] regionToPartitionsIndex;
    private final BytesTrie trie;

    public static final class Data {
        public int[] distances;
        public Set<LSR> paradigmLSRs;
        public String[] partitionArrays;
        public byte[] regionToPartitionsIndex;
        public byte[] trie;

        public Data(byte[] bArr, byte[] bArr2, String[] strArr, Set<LSR> set, int[] iArr) {
            this.trie = bArr;
            this.regionToPartitionsIndex = bArr2;
            this.partitionArrays = strArr;
            this.paradigmLSRs = set;
            this.distances = iArr;
        }

        private static UResource.Value getValue(UResource.Table table, String str, UResource.Value value) {
            if (table.findValue(str, value)) {
                return value;
            }
            throw new MissingResourceException("langInfo.res missing data", "", "match/" + str);
        }

        public static Data load() throws MissingResourceException {
            HashSet hashSet;
            UResource.Value valueWithFallback = ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "langInfo", ICUResourceBundle.ICU_DATA_CLASS_LOADER, ICUResourceBundle.OpenType.DIRECT).getValueWithFallback(Constants.ATTRNAME_MATCH);
            UResource.Table table = valueWithFallback.getTable();
            ByteBuffer binary = getValue(table, "trie", valueWithFallback).getBinary();
            byte[] bArr = new byte[binary.remaining()];
            binary.get(bArr);
            ByteBuffer binary2 = getValue(table, "regionToPartitions", valueWithFallback).getBinary();
            byte[] bArr2 = new byte[binary2.remaining()];
            binary2.get(bArr2);
            if (bArr2.length >= 1677) {
                String[] stringArray = getValue(table, "partitions", valueWithFallback).getStringArray();
                if (table.findValue("paradigms", valueWithFallback)) {
                    String[] stringArray2 = valueWithFallback.getStringArray();
                    HashSet hashSet2 = new HashSet(stringArray2.length / 3);
                    for (int i = 0; i < stringArray2.length; i += 3) {
                        hashSet2.add(new LSR(stringArray2[i], stringArray2[i + 1], stringArray2[i + 2]));
                    }
                    hashSet = hashSet2;
                } else {
                    hashSet = Collections.emptySet();
                }
                int[] intVector = getValue(table, "distances", valueWithFallback).getIntVector();
                if (intVector.length >= 4) {
                    return new Data(bArr, bArr2, stringArray, hashSet, intVector);
                }
                throw new MissingResourceException("langInfo.res intvector too short", "", "match/distances");
            }
            throw new MissingResourceException("langInfo.res binary data too short", "", "match/regionToPartitions");
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!getClass().equals(obj.getClass())) {
                return false;
            }
            Data data = (Data) obj;
            return Arrays.equals(this.trie, data.trie) && Arrays.equals(this.regionToPartitionsIndex, data.regionToPartitionsIndex) && Arrays.equals(this.partitionArrays, data.partitionArrays) && this.paradigmLSRs.equals(data.paradigmLSRs) && Arrays.equals(this.distances, data.distances);
        }
    }

    private LocaleDistance(Data data) {
        this.trie = new BytesTrie(data.trie, 0);
        this.regionToPartitionsIndex = data.regionToPartitionsIndex;
        this.partitionArrays = data.partitionArrays;
        this.paradigmLSRs = data.paradigmLSRs;
        this.defaultLanguageDistance = data.distances[0];
        this.defaultScriptDistance = data.distances[1];
        this.defaultRegionDistance = data.distances[2];
        this.minRegionDistance = data.distances[3];
    }

    public int testOnlyDistance(ULocale uLocale, ULocale uLocale2, int i, LocaleMatcher.FavorSubtag favorSubtag) {
        return getBestIndexAndDistance(XLikelySubtags.INSTANCE.makeMaximizedLsrFrom(uLocale), new LSR[]{XLikelySubtags.INSTANCE.makeMaximizedLsrFrom(uLocale2)}, i, favorSubtag) & 255;
    }

    public int getBestIndexAndDistance(LSR lsr, LSR[] lsrArr, int i, LocaleMatcher.FavorSubtag favorSubtag) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        LSR lsr2 = lsr;
        LSR[] lsrArr2 = lsrArr;
        BytesTrie bytesTrie = new BytesTrie(this.trie);
        int i9 = 0;
        int trieNext = trieNext(bytesTrie, lsr2.language, false);
        boolean z = true;
        long state64 = (trieNext < 0 || lsrArr2.length <= 1) ? 0 : bytesTrie.getState64();
        int i10 = i;
        int i11 = -1;
        int i12 = 0;
        while (i12 < lsrArr2.length) {
            LSR lsr3 = lsrArr2[i12];
            if (trieNext >= 0) {
                if (i12 != 0) {
                    bytesTrie.resetToState64(state64);
                }
                i2 = trieNext(bytesTrie, lsr3.language, z);
            } else {
                i2 = trieNext;
            }
            if (i2 >= 0) {
                int i13 = i2 & 384;
                i5 = i2 & -385;
                i3 = i13;
                i4 = i9;
            } else {
                if (lsr2.language.equals(lsr3.language)) {
                    i5 = i9;
                } else {
                    i5 = this.defaultLanguageDistance;
                }
                i3 = i9;
                int i14 = z ? 1 : 0;
                Object[] objArr = z ? 1 : 0;
                Object[] objArr2 = z ? 1 : 0;
                i4 = i14;
            }
            if (favorSubtag == LocaleMatcher.FavorSubtag.SCRIPT) {
                i5 >>= 2;
            }
            if (i5 < i10) {
                if (i4 == 0 && i3 == 0) {
                    int desSuppScriptDistance = getDesSuppScriptDistance(bytesTrie, bytesTrie.getState64(), lsr2.script, lsr3.script);
                    i3 = desSuppScriptDistance & 256;
                    i7 = desSuppScriptDistance & -257;
                } else {
                    i7 = lsr2.script.equals(lsr3.script) ? 0 : this.defaultScriptDistance;
                }
                int i15 = i7 + i5;
                if (i15 < i10) {
                    if (lsr2.region.equals(lsr3.region)) {
                        i6 = i10;
                    } else {
                        if (i4 == 0 && (i3 & 256) == 0) {
                            int i16 = i10 - i15;
                            if (this.minRegionDistance < i16) {
                                i6 = i10;
                                i8 = getRegionPartitionsDistance(bytesTrie, bytesTrie.getState64(), partitionsForRegion(lsr), partitionsForRegion(lsr3), i16);
                            }
                        } else {
                            i6 = i10;
                            i8 = this.defaultRegionDistance;
                        }
                        i15 += i8;
                    }
                    if (i15 < i6) {
                        if (i15 == 0) {
                            return i12 << 8;
                        }
                        i10 = i15;
                        i11 = i12;
                        i12++;
                        lsr2 = lsr;
                        lsrArr2 = lsrArr;
                        i9 = 0;
                        z = true;
                    }
                    i10 = i6;
                    i12++;
                    lsr2 = lsr;
                    lsrArr2 = lsrArr;
                    i9 = 0;
                    z = true;
                }
            }
            i6 = i10;
            i10 = i6;
            i12++;
            lsr2 = lsr;
            lsrArr2 = lsrArr;
            i9 = 0;
            z = true;
        }
        if (i11 >= 0) {
            return (i11 << 8) | i10;
        }
        return -156;
    }

    private static final int getDesSuppScriptDistance(BytesTrie bytesTrie, long j, String str, String str2) {
        int i = 0;
        int trieNext = trieNext(bytesTrie, str, false);
        if (trieNext >= 0) {
            trieNext = trieNext(bytesTrie, str2, true);
        }
        if (trieNext >= 0) {
            return trieNext;
        }
        BytesTrie.Result next = bytesTrie.resetToState64(j).next(42);
        if (!str.equals(str2)) {
            i = bytesTrie.getValue();
        }
        return next == BytesTrie.Result.FINAL_VALUE ? i | 256 : i;
    }

    private static final int getRegionPartitionsDistance(BytesTrie bytesTrie, long j, String str, String str2, int i) {
        int i2;
        int length = str.length();
        int length2 = str2.length();
        if (length != 1 || length2 != 1) {
            int i3 = 0;
            boolean z = false;
            int i4 = 0;
            while (true) {
                int i5 = i3 + 1;
                if (bytesTrie.next(str.charAt(i3) | 128).hasNext()) {
                    long state64 = length2 > 1 ? bytesTrie.getState64() : 0;
                    int i6 = 0;
                    while (true) {
                        int i7 = i6 + 1;
                        if (bytesTrie.next(str2.charAt(i6) | 128).hasValue()) {
                            i2 = bytesTrie.getValue();
                        } else if (z) {
                            i2 = 0;
                        } else {
                            i2 = getFallbackRegionDistance(bytesTrie, j);
                            z = true;
                        }
                        if (i2 < i) {
                            if (i4 < i2) {
                                i4 = i2;
                            }
                            if (i7 >= length2) {
                                break;
                            }
                            bytesTrie.resetToState64(state64);
                            i6 = i7;
                        } else {
                            return i2;
                        }
                    }
                } else if (!z) {
                    int fallbackRegionDistance = getFallbackRegionDistance(bytesTrie, j);
                    if (fallbackRegionDistance >= i) {
                        return fallbackRegionDistance;
                    }
                    if (i4 < fallbackRegionDistance) {
                        i4 = fallbackRegionDistance;
                    }
                    z = true;
                }
                if (i5 >= length) {
                    return i4;
                }
                bytesTrie.resetToState64(j);
                i3 = i5;
            }
        } else if (!bytesTrie.next(str.charAt(0) | 128).hasNext() || !bytesTrie.next(str2.charAt(0) | 128).hasValue()) {
            return getFallbackRegionDistance(bytesTrie, j);
        } else {
            return bytesTrie.getValue();
        }
    }

    private static final int getFallbackRegionDistance(BytesTrie bytesTrie, long j) {
        bytesTrie.resetToState64(j).next(42);
        return bytesTrie.getValue();
    }

    private static final int trieNext(BytesTrie bytesTrie, String str, boolean z) {
        if (str.isEmpty()) {
            return -1;
        }
        int length = str.length() - 1;
        int i = 0;
        while (true) {
            char charAt = str.charAt(i);
            if (i >= length) {
                BytesTrie.Result next = bytesTrie.next(charAt | 128);
                if (z) {
                    if (next.hasValue()) {
                        int value = bytesTrie.getValue();
                        return next == BytesTrie.Result.FINAL_VALUE ? value | 256 : value;
                    }
                } else if (next.hasNext()) {
                    return 0;
                }
                return -1;
            } else if (!bytesTrie.next(charAt).hasNext()) {
                return -1;
            } else {
                i++;
            }
        }
    }

    public String toString() {
        return testOnlyGetDistanceTable().toString();
    }

    private String partitionsForRegion(LSR lsr) {
        return this.partitionArrays[this.regionToPartitionsIndex[lsr.regionIndex]];
    }

    public boolean isParadigmLSR(LSR lsr) {
        return this.paradigmLSRs.contains(lsr);
    }

    public int getDefaultScriptDistance() {
        return this.defaultScriptDistance;
    }

    /* access modifiers changed from: package-private */
    public int getDefaultRegionDistance() {
        return this.defaultRegionDistance;
    }

    public int getDefaultDemotionPerDesiredLocale() {
        return this.defaultDemotionPerDesiredLocale;
    }

    public Map<String, Integer> testOnlyGetDistanceTable() {
        TreeMap treeMap = new TreeMap();
        StringBuilder sb = new StringBuilder();
        BytesTrie.Iterator it = this.trie.iterator();
        while (it.hasNext()) {
            BytesTrie.Entry entry = (BytesTrie.Entry) it.next();
            sb.setLength(0);
            int bytesLength = entry.bytesLength();
            for (int i = 0; i < bytesLength; i++) {
                byte byteAt = entry.byteAt(i);
                if (byteAt == 42) {
                    sb.append("*-*-");
                } else if (byteAt >= 0) {
                    sb.append((char) byteAt);
                } else {
                    sb.append((char) (byteAt & Bidi.LEVEL_DEFAULT_RTL));
                    sb.append(LocaleUtility.IETF_SEPARATOR);
                }
            }
            sb.setLength(sb.length() - 1);
            treeMap.put(sb.toString(), Integer.valueOf(entry.value));
        }
        return treeMap;
    }

    public void testOnlyPrintDistanceTable() {
        String str;
        for (Map.Entry<String, Integer> entry : testOnlyGetDistanceTable().entrySet()) {
            int intValue = entry.getValue().intValue();
            if ((intValue & 128) != 0) {
                intValue &= -129;
                str = " skip script";
            } else {
                str = "";
            }
            PrintStream printStream = System.out;
            printStream.println(entry.getKey() + '=' + intValue + str);
        }
    }
}
