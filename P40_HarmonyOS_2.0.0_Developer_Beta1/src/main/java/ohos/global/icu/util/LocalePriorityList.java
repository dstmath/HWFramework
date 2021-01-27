package ohos.global.icu.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalePriorityList implements Iterable<ULocale> {
    private static final Double D1 = Double.valueOf(1.0d);
    private static final Pattern languageSplitter = Pattern.compile("\\s*,\\s*");
    private static Comparator<Double> myDescendingDouble = new Comparator<Double>() {
        /* class ohos.global.icu.util.LocalePriorityList.AnonymousClass1 */

        public int compare(Double d, Double d2) {
            int compareTo = d.compareTo(d2);
            if (compareTo > 0) {
                return -1;
            }
            return compareTo < 0 ? 1 : 0;
        }
    };
    private static final Pattern weightSplitter = Pattern.compile("\\s*(\\S*)\\s*;\\s*q\\s*=\\s*(\\S*)");
    private final Map<ULocale, Double> languagesAndWeights;

    public static Builder add(ULocale... uLocaleArr) {
        return new Builder().add(uLocaleArr);
    }

    public static Builder add(ULocale uLocale, double d) {
        return new Builder().add(uLocale, d);
    }

    public static Builder add(LocalePriorityList localePriorityList) {
        return new Builder();
    }

    public static Builder add(String str) {
        return new Builder().add(str);
    }

    public Double getWeight(ULocale uLocale) {
        return this.languagesAndWeights.get(uLocale);
    }

    public Set<ULocale> getULocales() {
        return this.languagesAndWeights.keySet();
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ULocale, Double> entry : this.languagesAndWeights.entrySet()) {
            ULocale key = entry.getKey();
            double doubleValue = entry.getValue().doubleValue();
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(key);
            if (doubleValue != 1.0d) {
                sb.append(";q=");
                sb.append(doubleValue);
            }
        }
        return sb.toString();
    }

    @Override // java.lang.Iterable
    public Iterator<ULocale> iterator() {
        return this.languagesAndWeights.keySet().iterator();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        try {
            return this.languagesAndWeights.equals(((LocalePriorityList) obj).languagesAndWeights);
        } catch (RuntimeException unused) {
            return false;
        }
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.languagesAndWeights.hashCode();
    }

    private LocalePriorityList(Map<ULocale, Double> map) {
        this.languagesAndWeights = map;
    }

    public static class Builder {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private LocalePriorityList built;
        private boolean hasWeights;
        private Map<ULocale, Double> languageToWeight;

        private Builder() {
            this.hasWeights = false;
            this.languageToWeight = new LinkedHashMap();
        }

        private Builder(LocalePriorityList localePriorityList) {
            this.hasWeights = false;
            this.built = localePriorityList;
            for (Double d : localePriorityList.languagesAndWeights.values()) {
                if (d.doubleValue() != 1.0d) {
                    this.hasWeights = true;
                    return;
                }
            }
        }

        public LocalePriorityList build() {
            return build(false);
        }

        public LocalePriorityList build(boolean z) {
            Map<ULocale, Double> map;
            LocalePriorityList localePriorityList = this.built;
            if (localePriorityList != null) {
                return localePriorityList;
            }
            if (this.hasWeights) {
                TreeMap treeMap = new TreeMap(LocalePriorityList.myDescendingDouble);
                for (Map.Entry<ULocale, Double> entry : this.languageToWeight.entrySet()) {
                    ULocale key = entry.getKey();
                    Double value = entry.getValue();
                    List list = (List) treeMap.get(value);
                    if (list == null) {
                        list = new LinkedList();
                        treeMap.put(value, list);
                    }
                    list.add(key);
                }
                if (treeMap.size() <= 1) {
                    map = this.languageToWeight;
                    if (treeMap.isEmpty() || ((Double) treeMap.firstKey()).doubleValue() == 1.0d) {
                        this.hasWeights = false;
                    }
                } else {
                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    for (Map.Entry entry2 : treeMap.entrySet()) {
                        Double d = z ? (Double) entry2.getKey() : LocalePriorityList.D1;
                        for (ULocale uLocale : (List) entry2.getValue()) {
                            linkedHashMap.put(uLocale, d);
                        }
                    }
                    map = linkedHashMap;
                }
            } else {
                map = this.languageToWeight;
            }
            this.languageToWeight = null;
            LocalePriorityList localePriorityList2 = new LocalePriorityList(Collections.unmodifiableMap(map));
            this.built = localePriorityList2;
            return localePriorityList2;
        }

        public Builder add(LocalePriorityList localePriorityList) {
            for (Map.Entry entry : localePriorityList.languagesAndWeights.entrySet()) {
                add((ULocale) entry.getKey(), ((Double) entry.getValue()).doubleValue());
            }
            return this;
        }

        public Builder add(ULocale uLocale) {
            return add(uLocale, 1.0d);
        }

        public Builder add(ULocale... uLocaleArr) {
            for (ULocale uLocale : uLocaleArr) {
                add(uLocale, 1.0d);
            }
            return this;
        }

        public Builder add(ULocale uLocale, double d) {
            Double d2;
            if (this.languageToWeight == null) {
                this.languageToWeight = new LinkedHashMap(this.built.languagesAndWeights);
                this.built = null;
            }
            if (this.languageToWeight.containsKey(uLocale)) {
                this.languageToWeight.remove(uLocale);
            }
            if (d <= 0.0d) {
                return this;
            }
            if (d >= 1.0d) {
                d2 = LocalePriorityList.D1;
            } else {
                d2 = Double.valueOf(d);
                this.hasWeights = true;
            }
            this.languageToWeight.put(uLocale, d2);
            return this;
        }

        public Builder add(String str) {
            String[] split = LocalePriorityList.languageSplitter.split(str.trim());
            Matcher matcher = LocalePriorityList.weightSplitter.matcher("");
            for (String str2 : split) {
                if (matcher.reset(str2).matches()) {
                    ULocale uLocale = new ULocale(matcher.group(1));
                    double parseDouble = Double.parseDouble(matcher.group(2));
                    if (0.0d > parseDouble || parseDouble > 1.0d) {
                        throw new IllegalArgumentException("Illegal weight, must be 0..1: " + parseDouble);
                    }
                    add(uLocale, parseDouble);
                } else if (str2.length() != 0) {
                    add(new ULocale(str2));
                }
            }
            return this;
        }
    }
}
