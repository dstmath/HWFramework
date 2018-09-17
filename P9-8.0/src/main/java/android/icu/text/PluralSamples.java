package android.icu.text;

import android.icu.text.PluralRules.FixedDecimal;
import android.icu.text.PluralRules.KeywordStatus;
import android.icu.util.Output;
import dalvik.system.VMRuntime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

@Deprecated
public class PluralSamples {
    private static final int LIMIT_FRACTION_SAMPLES = 3;
    private static final int[] TENS = new int[]{1, 10, 100, 1000, VMRuntime.SDK_VERSION_CUR_DEVELOPMENT, 100000, 1000000};
    private final Set<FixedDecimal> _fractionSamples;
    private final Map<String, Set<FixedDecimal>> _keyFractionSamplesMap;
    @Deprecated
    public final Map<String, Boolean> _keyLimitedMap;
    private final Map<String, List<Double>> _keySamplesMap;
    private PluralRules pluralRules;

    @Deprecated
    public PluralSamples(PluralRules pluralRules) {
        this.pluralRules = pluralRules;
        Set<String> keywords = pluralRules.getKeywords();
        Map<String, Boolean> temp = new HashMap();
        for (String k : keywords) {
            temp.put(k, pluralRules.isLimited(k));
        }
        this._keyLimitedMap = temp;
        Map<String, List<Double>> sampleMap = new HashMap();
        int keywordsRemaining = keywords.size();
        int i = 0;
        while (keywordsRemaining > 0 && i < 128) {
            keywordsRemaining = addSimpleSamples(pluralRules, 3, sampleMap, keywordsRemaining, ((double) i) / 2.0d);
            i++;
        }
        keywordsRemaining = addSimpleSamples(pluralRules, 3, sampleMap, keywordsRemaining, 1000000.0d);
        Map<String, Set<FixedDecimal>> sampleFractionMap = new HashMap();
        Set<FixedDecimal> mentioned = new TreeSet();
        Map<String, Set<FixedDecimal>> foundKeywords = new HashMap();
        for (FixedDecimal s : mentioned) {
            addRelation(foundKeywords, pluralRules.select(s), s);
        }
        if (foundKeywords.size() != keywords.size()) {
            for (i = 1; i < 1000; i++) {
                if (addIfNotPresent((double) i, mentioned, foundKeywords)) {
                    break;
                }
            }
            for (i = 10; i < 1000; i++) {
                if (addIfNotPresent(((double) i) / 10.0d, mentioned, foundKeywords)) {
                    break;
                }
            }
            System.out.println("Failed to find sample for each keyword: " + foundKeywords + "\n\t" + pluralRules + "\n\t" + mentioned);
        }
        mentioned.add(new FixedDecimal(0));
        mentioned.add(new FixedDecimal(1));
        mentioned.add(new FixedDecimal(2));
        mentioned.add(new FixedDecimal(0.1d, 1));
        mentioned.add(new FixedDecimal(1.99d, 2));
        mentioned.addAll(fractions(mentioned));
        for (FixedDecimal s2 : mentioned) {
            String keyword = pluralRules.select(s2);
            Set<FixedDecimal> list = (Set) sampleFractionMap.get(keyword);
            if (list == null) {
                list = new LinkedHashSet();
                sampleFractionMap.put(keyword, list);
            }
            list.add(s2);
        }
        if (keywordsRemaining > 0) {
            for (String k2 : keywords) {
                if (!sampleMap.containsKey(k2)) {
                    sampleMap.put(k2, Collections.emptyList());
                }
                if (!sampleFractionMap.containsKey(k2)) {
                    sampleFractionMap.put(k2, Collections.emptySet());
                }
            }
        }
        for (Entry<String, List<Double>> entry : sampleMap.entrySet()) {
            sampleMap.put((String) entry.getKey(), Collections.unmodifiableList((List) entry.getValue()));
        }
        for (Entry<String, Set<FixedDecimal>> entry2 : sampleFractionMap.entrySet()) {
            sampleFractionMap.put((String) entry2.getKey(), Collections.unmodifiableSet((Set) entry2.getValue()));
        }
        this._keySamplesMap = sampleMap;
        this._keyFractionSamplesMap = sampleFractionMap;
        this._fractionSamples = Collections.unmodifiableSet(mentioned);
    }

    private int addSimpleSamples(PluralRules pluralRules, int MAX_SAMPLES, Map<String, List<Double>> sampleMap, int keywordsRemaining, double val) {
        String keyword = pluralRules.select(val);
        boolean keyIsLimited = ((Boolean) this._keyLimitedMap.get(keyword)).booleanValue();
        List<Double> list = (List) sampleMap.get(keyword);
        if (list == null) {
            list = new ArrayList(MAX_SAMPLES);
            sampleMap.put(keyword, list);
        } else if (!keyIsLimited && list.size() == MAX_SAMPLES) {
            return keywordsRemaining;
        }
        list.add(Double.valueOf(val));
        if (!keyIsLimited && list.size() == MAX_SAMPLES) {
            keywordsRemaining--;
        }
        return keywordsRemaining;
    }

    private void addRelation(Map<String, Set<FixedDecimal>> foundKeywords, String keyword, FixedDecimal s) {
        Set<FixedDecimal> set = (Set) foundKeywords.get(keyword);
        if (set == null) {
            set = new HashSet();
            foundKeywords.put(keyword, set);
        }
        set.add(s);
    }

    private boolean addIfNotPresent(double d, Set<FixedDecimal> mentioned, Map<String, Set<FixedDecimal>> foundKeywords) {
        FixedDecimal numberInfo = new FixedDecimal(d);
        String keyword = this.pluralRules.select(numberInfo);
        if (!foundKeywords.containsKey(keyword) || keyword.equals("other")) {
            addRelation(foundKeywords, keyword, numberInfo);
            mentioned.add(numberInfo);
            if (keyword.equals("other") && ((Set) foundKeywords.get("other")).size() > 1) {
                return true;
            }
        }
        return false;
    }

    private Set<FixedDecimal> fractions(Set<FixedDecimal> original) {
        Set<FixedDecimal> toAddTo = new HashSet();
        Set<Integer> result = new HashSet();
        for (FixedDecimal base1 : original) {
            result.add(Integer.valueOf((int) base1.integerValue));
        }
        List<Integer> ints = new ArrayList(result);
        Set<String> keywords = new HashSet();
        for (int j = 0; j < ints.size(); j++) {
            Integer base = (Integer) ints.get(j);
            String keyword = this.pluralRules.select((double) base.intValue());
            if (!keywords.contains(keyword)) {
                keywords.add(keyword);
                toAddTo.add(new FixedDecimal((double) base.intValue(), 1));
                toAddTo.add(new FixedDecimal((double) base.intValue(), 2));
                Integer fract = getDifferentCategory(ints, keyword);
                if (fract.intValue() >= TENS[2]) {
                    toAddTo.add(new FixedDecimal(base + "." + fract));
                } else {
                    for (int visibleFractions = 1; visibleFractions < 3; visibleFractions++) {
                        for (int i = 1; i <= visibleFractions; i++) {
                            if (fract.intValue() < TENS[i]) {
                                toAddTo.add(new FixedDecimal(((double) base.intValue()) + (((double) fract.intValue()) / ((double) TENS[i])), visibleFractions));
                            }
                        }
                    }
                }
            }
        }
        return toAddTo;
    }

    private Integer getDifferentCategory(List<Integer> ints, String keyword) {
        for (int i = ints.size() - 1; i >= 0; i--) {
            Integer other = (Integer) ints.get(i);
            if (!this.pluralRules.select((double) other.intValue()).equals(keyword)) {
                return other;
            }
        }
        return Integer.valueOf(37);
    }

    @Deprecated
    public KeywordStatus getStatus(String keyword, int offset, Set<Double> explicits, Output<Double> uniqueValue) {
        if (uniqueValue != null) {
            uniqueValue.value = null;
        }
        if (!this.pluralRules.getKeywords().contains(keyword)) {
            return KeywordStatus.INVALID;
        }
        Collection<Double> values = this.pluralRules.getAllKeywordValues(keyword);
        if (values == null) {
            return KeywordStatus.UNBOUNDED;
        }
        int originalSize = values.size();
        if (explicits == null) {
            explicits = Collections.emptySet();
        }
        if (originalSize <= explicits.size()) {
            HashSet<Double> subtractedSet = new HashSet(values);
            for (Double explicit : explicits) {
                subtractedSet.remove(Double.valueOf(explicit.doubleValue() - ((double) offset)));
            }
            if (subtractedSet.size() == 0) {
                return KeywordStatus.SUPPRESSED;
            }
            if (uniqueValue != null && subtractedSet.size() == 1) {
                uniqueValue.value = (Double) subtractedSet.iterator().next();
            }
            return originalSize == 1 ? KeywordStatus.UNIQUE : KeywordStatus.BOUNDED;
        } else if (originalSize != 1) {
            return KeywordStatus.BOUNDED;
        } else {
            if (uniqueValue != null) {
                uniqueValue.value = (Double) values.iterator().next();
            }
            return KeywordStatus.UNIQUE;
        }
    }

    Map<String, List<Double>> getKeySamplesMap() {
        return this._keySamplesMap;
    }

    Map<String, Set<FixedDecimal>> getKeyFractionSamplesMap() {
        return this._keyFractionSamplesMap;
    }

    Set<FixedDecimal> getFractionSamples() {
        return this._fractionSamples;
    }

    Collection<Double> getAllKeywordValues(String keyword) {
        if (!this.pluralRules.getKeywords().contains(keyword)) {
            return Collections.emptyList();
        }
        Collection<Double> result = (Collection) getKeySamplesMap().get(keyword);
        if (result.size() <= 2 || (((Boolean) this._keyLimitedMap.get(keyword)).booleanValue() ^ 1) == 0) {
            return result;
        }
        return null;
    }
}
