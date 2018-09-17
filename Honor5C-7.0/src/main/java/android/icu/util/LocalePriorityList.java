package android.icu.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;

public class LocalePriorityList implements Iterable<ULocale> {
    private static final double D0 = 0.0d;
    private static final Double D1 = null;
    private static final Pattern languageSplitter = null;
    private static Comparator<Double> myDescendingDouble;
    private static final Pattern weightSplitter = null;
    private final Map<ULocale, Double> languagesAndWeights;

    public static class Builder {
        private final Map<ULocale, Double> languageToWeight;

        private Builder() {
            this.languageToWeight = new LinkedHashMap();
        }

        public LocalePriorityList build() {
            return build(false);
        }

        public LocalePriorityList build(boolean preserveWeights) {
            Map<Double, Set<ULocale>> doubleCheck = new TreeMap(LocalePriorityList.myDescendingDouble);
            for (ULocale lang : this.languageToWeight.keySet()) {
                Double weight = (Double) this.languageToWeight.get(lang);
                Set<ULocale> s = (Set) doubleCheck.get(weight);
                if (s == null) {
                    s = new LinkedHashSet();
                    doubleCheck.put(weight, s);
                }
                s.add(lang);
            }
            Map<ULocale, Double> temp = new LinkedHashMap();
            for (Entry<Double, Set<ULocale>> langEntry : doubleCheck.entrySet()) {
                weight = (Double) langEntry.getKey();
                for (ULocale lang2 : (Set) langEntry.getValue()) {
                    temp.put(lang2, preserveWeights ? weight : LocalePriorityList.D1);
                }
            }
            return new LocalePriorityList(null);
        }

        public Builder add(LocalePriorityList languagePriorityList) {
            for (ULocale language : languagePriorityList.languagesAndWeights.keySet()) {
                add(language, ((Double) languagePriorityList.languagesAndWeights.get(language)).doubleValue());
            }
            return this;
        }

        public Builder add(ULocale languageCode) {
            return add(languageCode, LocalePriorityList.D1.doubleValue());
        }

        public Builder add(ULocale... languageCodes) {
            for (ULocale languageCode : languageCodes) {
                add(languageCode, LocalePriorityList.D1.doubleValue());
            }
            return this;
        }

        public Builder add(ULocale languageCode, double weight) {
            if (this.languageToWeight.containsKey(languageCode)) {
                this.languageToWeight.remove(languageCode);
            }
            if (weight <= 0.0d) {
                return this;
            }
            if (weight > LocalePriorityList.D1.doubleValue()) {
                weight = LocalePriorityList.D1.doubleValue();
            }
            this.languageToWeight.put(languageCode, Double.valueOf(weight));
            return this;
        }

        public Builder add(String acceptLanguageList) {
            String[] items = LocalePriorityList.languageSplitter.split(acceptLanguageList.trim());
            Matcher itemMatcher = LocalePriorityList.weightSplitter.matcher(XmlPullParser.NO_NAMESPACE);
            for (String item : items) {
                if (itemMatcher.reset(item).matches()) {
                    int i;
                    ULocale language = new ULocale(itemMatcher.group(1));
                    double weight = Double.parseDouble(itemMatcher.group(2));
                    if (weight < 0.0d || weight > LocalePriorityList.D1.doubleValue()) {
                        i = 0;
                    } else {
                        i = 1;
                    }
                    if (i == 0) {
                        throw new IllegalArgumentException("Illegal weight, must be 0..1: " + weight);
                    }
                    add(language, weight);
                } else if (item.length() != 0) {
                    add(new ULocale(item));
                }
            }
            return this;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.LocalePriorityList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.LocalePriorityList.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.LocalePriorityList.<clinit>():void");
    }

    public static Builder add(ULocale... languageCode) {
        return new Builder().add(languageCode);
    }

    public static Builder add(ULocale languageCode, double weight) {
        return new Builder().add(languageCode, weight);
    }

    public static Builder add(LocalePriorityList languagePriorityList) {
        return new Builder().add(languagePriorityList);
    }

    public static Builder add(String acceptLanguageString) {
        return new Builder().add(acceptLanguageString);
    }

    public Double getWeight(ULocale language) {
        return (Double) this.languagesAndWeights.get(language);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ULocale language : this.languagesAndWeights.keySet()) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(language);
            double weight = ((Double) this.languagesAndWeights.get(language)).doubleValue();
            if (weight != D1.doubleValue()) {
                result.append(";q=").append(weight);
            }
        }
        return result.toString();
    }

    public Iterator<ULocale> iterator() {
        return this.languagesAndWeights.keySet().iterator();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        try {
            return this.languagesAndWeights.equals(((LocalePriorityList) o).languagesAndWeights);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public int hashCode() {
        return this.languagesAndWeights.hashCode();
    }

    private LocalePriorityList(Map<ULocale, Double> languageToWeight) {
        this.languagesAndWeights = languageToWeight;
    }
}
