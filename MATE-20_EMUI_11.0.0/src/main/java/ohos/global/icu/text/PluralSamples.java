package ohos.global.icu.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import ohos.bluetooth.A2dpCodecInfo;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.Output;

@Deprecated
public class PluralSamples {
    private static final int LIMIT_FRACTION_SAMPLES = 3;
    private static final int[] TENS = {1, 10, 100, 1000, 10000, 100000, A2dpCodecInfo.CODEC_PRIORITY_HIGHEST};
    private final Set<PluralRules.FixedDecimal> _fractionSamples;
    private final Map<String, Set<PluralRules.FixedDecimal>> _keyFractionSamplesMap;
    @Deprecated
    public final Map<String, Boolean> _keyLimitedMap;
    private final Map<String, List<Double>> _keySamplesMap;
    private PluralRules pluralRules;

    @Deprecated
    public PluralSamples(PluralRules pluralRules2) {
        this.pluralRules = pluralRules2;
        Set<String> keywords = pluralRules2.getKeywords();
        HashMap hashMap = new HashMap();
        for (String str : keywords) {
            hashMap.put(str, pluralRules2.isLimited(str));
        }
        this._keyLimitedMap = hashMap;
        HashMap hashMap2 = new HashMap();
        int i = 0;
        int size = keywords.size();
        while (size > 0 && i < 128) {
            size = addSimpleSamples(pluralRules2, 3, hashMap2, size, ((double) i) / 2.0d);
            i++;
        }
        int addSimpleSamples = addSimpleSamples(pluralRules2, 3, hashMap2, size, 1000000.0d);
        HashMap hashMap3 = new HashMap();
        TreeSet treeSet = new TreeSet();
        HashMap hashMap4 = new HashMap();
        for (PluralRules.FixedDecimal fixedDecimal : treeSet) {
            addRelation(hashMap4, pluralRules2.select(fixedDecimal), fixedDecimal);
        }
        if (hashMap4.size() != keywords.size()) {
            int i2 = 1;
            while (true) {
                if (i2 >= 1000) {
                    int i3 = 10;
                    while (true) {
                        if (i3 >= 1000) {
                            System.out.println("Failed to find sample for each keyword: " + hashMap4 + "\n\t" + pluralRules2 + "\n\t" + treeSet);
                            break;
                        } else if (addIfNotPresent(((double) i3) / 10.0d, treeSet, hashMap4)) {
                            break;
                        } else {
                            i3++;
                        }
                    }
                } else if (addIfNotPresent((double) i2, treeSet, hashMap4)) {
                    break;
                } else {
                    i2++;
                }
            }
        }
        treeSet.add(new PluralRules.FixedDecimal(0L));
        treeSet.add(new PluralRules.FixedDecimal(1L));
        treeSet.add(new PluralRules.FixedDecimal(2L));
        treeSet.add(new PluralRules.FixedDecimal(0.1d, 1));
        treeSet.add(new PluralRules.FixedDecimal(1.99d, 2));
        treeSet.addAll(fractions(treeSet));
        for (PluralRules.FixedDecimal fixedDecimal2 : treeSet) {
            String select = pluralRules2.select(fixedDecimal2);
            Set set = (Set) hashMap3.get(select);
            if (set == null) {
                set = new LinkedHashSet();
                hashMap3.put(select, set);
            }
            set.add(fixedDecimal2);
        }
        if (addSimpleSamples > 0) {
            for (String str2 : keywords) {
                if (!hashMap2.containsKey(str2)) {
                    hashMap2.put(str2, Collections.emptyList());
                }
                if (!hashMap3.containsKey(str2)) {
                    hashMap3.put(str2, Collections.emptySet());
                }
            }
        }
        for (Map.Entry<String, List<Double>> entry : hashMap2.entrySet()) {
            hashMap2.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        for (Map.Entry entry2 : hashMap3.entrySet()) {
            hashMap3.put((String) entry2.getKey(), Collections.unmodifiableSet((Set) entry2.getValue()));
        }
        this._keySamplesMap = hashMap2;
        this._keyFractionSamplesMap = hashMap3;
        this._fractionSamples = Collections.unmodifiableSet(treeSet);
    }

    private int addSimpleSamples(PluralRules pluralRules2, int i, Map<String, List<Double>> map, int i2, double d) {
        String select = pluralRules2.select(d);
        boolean booleanValue = this._keyLimitedMap.get(select).booleanValue();
        List<Double> list = map.get(select);
        if (list == null) {
            list = new ArrayList<>(i);
            map.put(select, list);
        } else if (!booleanValue && list.size() == i) {
            return i2;
        }
        list.add(Double.valueOf(d));
        return (booleanValue || list.size() != i) ? i2 : i2 - 1;
    }

    private void addRelation(Map<String, Set<PluralRules.FixedDecimal>> map, String str, PluralRules.FixedDecimal fixedDecimal) {
        Set<PluralRules.FixedDecimal> set = map.get(str);
        if (set == null) {
            set = new HashSet<>();
            map.put(str, set);
        }
        set.add(fixedDecimal);
    }

    private boolean addIfNotPresent(double d, Set<PluralRules.FixedDecimal> set, Map<String, Set<PluralRules.FixedDecimal>> map) {
        PluralRules.FixedDecimal fixedDecimal = new PluralRules.FixedDecimal(d);
        String select = this.pluralRules.select(fixedDecimal);
        if (map.containsKey(select) && !select.equals("other")) {
            return false;
        }
        addRelation(map, select, fixedDecimal);
        set.add(fixedDecimal);
        return select.equals("other") && map.get("other").size() > 1;
    }

    private Set<PluralRules.FixedDecimal> fractions(Set<PluralRules.FixedDecimal> set) {
        ArrayList arrayList;
        HashSet hashSet;
        HashSet hashSet2 = new HashSet();
        HashSet hashSet3 = new HashSet();
        for (PluralRules.FixedDecimal fixedDecimal : set) {
            hashSet3.add(Integer.valueOf((int) fixedDecimal.integerValue));
        }
        ArrayList arrayList2 = new ArrayList(hashSet3);
        HashSet hashSet4 = new HashSet();
        int i = 0;
        while (i < arrayList2.size()) {
            Integer num = arrayList2.get(i);
            String select = this.pluralRules.select((double) num.intValue());
            if (!hashSet4.contains(select)) {
                hashSet4.add(select);
                hashSet2.add(new PluralRules.FixedDecimal((double) num.intValue(), 1));
                hashSet2.add(new PluralRules.FixedDecimal((double) num.intValue(), 2));
                Integer differentCategory = getDifferentCategory(arrayList2, select);
                if (differentCategory.intValue() >= TENS[2]) {
                    hashSet2.add(new PluralRules.FixedDecimal(num + "." + differentCategory));
                } else {
                    for (int i2 = 1; i2 < 3; i2++) {
                        int i3 = 1;
                        while (i3 <= i2) {
                            if (differentCategory.intValue() >= TENS[i3]) {
                                hashSet = hashSet4;
                                arrayList = arrayList2;
                            } else {
                                hashSet = hashSet4;
                                arrayList = arrayList2;
                                hashSet2.add(new PluralRules.FixedDecimal(((double) num.intValue()) + (((double) differentCategory.intValue()) / ((double) TENS[i3])), i2));
                            }
                            i3++;
                            arrayList2 = arrayList;
                            hashSet4 = hashSet;
                        }
                    }
                }
            }
            i++;
            arrayList2 = arrayList2;
            hashSet4 = hashSet4;
        }
        return hashSet2;
    }

    private Integer getDifferentCategory(List<Integer> list, String str) {
        for (int size = list.size() - 1; size >= 0; size--) {
            Integer num = list.get(size);
            if (!this.pluralRules.select((double) num.intValue()).equals(str)) {
                return num;
            }
        }
        return 37;
    }

    @Deprecated
    public PluralRules.KeywordStatus getStatus(String str, int i, Set<Double> set, Output<Double> output) {
        if (output != null) {
            output.value = null;
        }
        if (!this.pluralRules.getKeywords().contains(str)) {
            return PluralRules.KeywordStatus.INVALID;
        }
        Collection<Double> allKeywordValues = this.pluralRules.getAllKeywordValues(str);
        if (allKeywordValues == null) {
            return PluralRules.KeywordStatus.UNBOUNDED;
        }
        int size = allKeywordValues.size();
        if (set == null) {
            set = Collections.emptySet();
        }
        if (size <= set.size()) {
            HashSet hashSet = new HashSet(allKeywordValues);
            for (Double d : set) {
                hashSet.remove(Double.valueOf(d.doubleValue() - ((double) i)));
            }
            if (hashSet.size() == 0) {
                return PluralRules.KeywordStatus.SUPPRESSED;
            }
            if (output != null && hashSet.size() == 1) {
                output.value = hashSet.iterator().next();
            }
            return size == 1 ? PluralRules.KeywordStatus.UNIQUE : PluralRules.KeywordStatus.BOUNDED;
        } else if (size != 1) {
            return PluralRules.KeywordStatus.BOUNDED;
        } else {
            if (output != null) {
                output.value = allKeywordValues.iterator().next();
            }
            return PluralRules.KeywordStatus.UNIQUE;
        }
    }

    /* access modifiers changed from: package-private */
    public Map<String, List<Double>> getKeySamplesMap() {
        return this._keySamplesMap;
    }

    /* access modifiers changed from: package-private */
    public Map<String, Set<PluralRules.FixedDecimal>> getKeyFractionSamplesMap() {
        return this._keyFractionSamplesMap;
    }

    /* access modifiers changed from: package-private */
    public Set<PluralRules.FixedDecimal> getFractionSamples() {
        return this._fractionSamples;
    }

    /* access modifiers changed from: package-private */
    public Collection<Double> getAllKeywordValues(String str) {
        if (!this.pluralRules.getKeywords().contains(str)) {
            return Collections.emptyList();
        }
        List<Double> list = getKeySamplesMap().get(str);
        if (list.size() <= 2 || this._keyLimitedMap.get(str).booleanValue()) {
            return list;
        }
        return null;
    }
}
