package android.icu.text;

import android.icu.impl.Normalizer2Impl;
import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class AlphabeticIndex<V> implements Iterable<Bucket<V>> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String BASE = "﷐";
    private static final char CGJ = '͏';
    private static final int GC_CN_MASK = 1;
    private static final int GC_LL_MASK = 4;
    private static final int GC_LM_MASK = 16;
    private static final int GC_LO_MASK = 32;
    private static final int GC_LT_MASK = 8;
    private static final int GC_LU_MASK = 2;
    private static final int GC_L_MASK = 62;
    private static final Comparator<String> binaryCmp = new UTF16.StringComparator(true, false, 0);
    private BucketList<V> buckets;
    private RuleBasedCollator collatorExternal;
    /* access modifiers changed from: private */
    public final RuleBasedCollator collatorOriginal;
    private final RuleBasedCollator collatorPrimaryOnly;
    private final List<String> firstCharsInScripts;
    private String inflowLabel;
    private final UnicodeSet initialLabels;
    private List<Record<V>> inputList;
    private int maxLabelCount;
    private String overflowLabel;
    private final Comparator<Record<V>> recordComparator;
    private String underflowLabel;

    public static class Bucket<V> implements Iterable<Record<V>> {
        /* access modifiers changed from: private */
        public Bucket<V> displayBucket;
        /* access modifiers changed from: private */
        public int displayIndex;
        private final String label;
        /* access modifiers changed from: private */
        public final LabelType labelType;
        /* access modifiers changed from: private */
        public final String lowerBoundary;
        /* access modifiers changed from: private */
        public List<Record<V>> records;

        public enum LabelType {
            NORMAL,
            UNDERFLOW,
            INFLOW,
            OVERFLOW
        }

        private Bucket(String label2, String lowerBoundary2, LabelType labelType2) {
            this.label = label2;
            this.lowerBoundary = lowerBoundary2;
            this.labelType = labelType2;
        }

        public String getLabel() {
            return this.label;
        }

        public LabelType getLabelType() {
            return this.labelType;
        }

        public int size() {
            if (this.records == null) {
                return 0;
            }
            return this.records.size();
        }

        public Iterator<Record<V>> iterator() {
            if (this.records == null) {
                return Collections.emptyList().iterator();
            }
            return this.records.iterator();
        }

        public String toString() {
            return "{labelType=" + this.labelType + ", lowerBoundary=" + this.lowerBoundary + ", label=" + this.label + "}";
        }
    }

    private static class BucketList<V> implements Iterable<Bucket<V>> {
        private final ArrayList<Bucket<V>> bucketList;
        /* access modifiers changed from: private */
        public final List<Bucket<V>> immutableVisibleList;

        private BucketList(ArrayList<Bucket<V>> bucketList2, ArrayList<Bucket<V>> publicBucketList) {
            this.bucketList = bucketList2;
            int displayIndex = 0;
            Iterator<Bucket<V>> it = publicBucketList.iterator();
            while (it.hasNext()) {
                int unused = it.next().displayIndex = displayIndex;
                displayIndex++;
            }
            this.immutableVisibleList = Collections.unmodifiableList(publicBucketList);
        }

        /* access modifiers changed from: private */
        public int getBucketCount() {
            return this.immutableVisibleList.size();
        }

        /* access modifiers changed from: private */
        public int getBucketIndex(CharSequence name, Collator collatorPrimaryOnly) {
            int start = 0;
            int limit = this.bucketList.size();
            while (start + 1 < limit) {
                int i = (start + limit) / 2;
                if (collatorPrimaryOnly.compare((Object) name, (Object) this.bucketList.get(i).lowerBoundary) < 0) {
                    limit = i;
                } else {
                    start = i;
                }
            }
            Bucket<V> bucket = this.bucketList.get(start);
            if (bucket.displayBucket != null) {
                bucket = bucket.displayBucket;
            }
            return bucket.displayIndex;
        }

        /* access modifiers changed from: private */
        public Iterator<Bucket<V>> fullIterator() {
            return this.bucketList.iterator();
        }

        public Iterator<Bucket<V>> iterator() {
            return this.immutableVisibleList.iterator();
        }
    }

    public static final class ImmutableIndex<V> implements Iterable<Bucket<V>> {
        private final BucketList<V> buckets;
        private final Collator collatorPrimaryOnly;

        private ImmutableIndex(BucketList<V> bucketList, Collator collatorPrimaryOnly2) {
            this.buckets = bucketList;
            this.collatorPrimaryOnly = collatorPrimaryOnly2;
        }

        public int getBucketCount() {
            return this.buckets.getBucketCount();
        }

        public int getBucketIndex(CharSequence name) {
            return this.buckets.getBucketIndex(name, this.collatorPrimaryOnly);
        }

        public Bucket<V> getBucket(int index) {
            if (index < 0 || index >= this.buckets.getBucketCount()) {
                return null;
            }
            return (Bucket) this.buckets.immutableVisibleList.get(index);
        }

        public Iterator<Bucket<V>> iterator() {
            return this.buckets.iterator();
        }
    }

    public static class Record<V> {
        private final V data;
        /* access modifiers changed from: private */
        public final CharSequence name;

        private Record(CharSequence name2, V data2) {
            this.name = name2;
            this.data = data2;
        }

        public CharSequence getName() {
            return this.name;
        }

        public V getData() {
            return this.data;
        }

        public String toString() {
            return this.name + "=" + this.data;
        }
    }

    public AlphabeticIndex(ULocale locale) {
        this(locale, null);
    }

    public AlphabeticIndex(Locale locale) {
        this(ULocale.forLocale(locale), null);
    }

    public AlphabeticIndex(RuleBasedCollator collator) {
        this(null, collator);
    }

    private AlphabeticIndex(ULocale locale, RuleBasedCollator collator) {
        this.recordComparator = new Comparator<Record<V>>() {
            public int compare(Record<V> o1, Record<V> o2) {
                return AlphabeticIndex.this.collatorOriginal.compare((Object) o1.name, (Object) o2.name);
            }
        };
        this.initialLabels = new UnicodeSet();
        this.overflowLabel = "…";
        this.underflowLabel = "…";
        this.inflowLabel = "…";
        this.maxLabelCount = 99;
        this.collatorOriginal = collator != null ? collator : (RuleBasedCollator) Collator.getInstance(locale);
        try {
            this.collatorPrimaryOnly = this.collatorOriginal.cloneAsThawed();
            this.collatorPrimaryOnly.setStrength(0);
            this.collatorPrimaryOnly.freeze();
            this.firstCharsInScripts = getFirstCharactersInScripts();
            Collections.sort(this.firstCharsInScripts, this.collatorPrimaryOnly);
            while (!this.firstCharsInScripts.isEmpty()) {
                if (this.collatorPrimaryOnly.compare(this.firstCharsInScripts.get(0), "") == 0) {
                    this.firstCharsInScripts.remove(0);
                } else if (!addChineseIndexCharacters() && locale != null) {
                    addIndexExemplars(locale);
                    return;
                } else {
                    return;
                }
            }
            throw new IllegalArgumentException("AlphabeticIndex requires some non-ignorable script boundary strings");
        } catch (Exception e) {
            throw new IllegalStateException("Collator cannot be cloned", e);
        }
    }

    public AlphabeticIndex<V> addLabels(UnicodeSet additions) {
        this.initialLabels.addAll(additions);
        this.buckets = null;
        return this;
    }

    public AlphabeticIndex<V> addLabels(ULocale... additions) {
        for (ULocale addition : additions) {
            addIndexExemplars(addition);
        }
        this.buckets = null;
        return this;
    }

    public AlphabeticIndex<V> addLabels(Locale... additions) {
        for (Locale addition : additions) {
            addIndexExemplars(ULocale.forLocale(addition));
        }
        this.buckets = null;
        return this;
    }

    public AlphabeticIndex<V> setOverflowLabel(String overflowLabel2) {
        this.overflowLabel = overflowLabel2;
        this.buckets = null;
        return this;
    }

    public String getUnderflowLabel() {
        return this.underflowLabel;
    }

    public AlphabeticIndex<V> setUnderflowLabel(String underflowLabel2) {
        this.underflowLabel = underflowLabel2;
        this.buckets = null;
        return this;
    }

    public String getOverflowLabel() {
        return this.overflowLabel;
    }

    public AlphabeticIndex<V> setInflowLabel(String inflowLabel2) {
        this.inflowLabel = inflowLabel2;
        this.buckets = null;
        return this;
    }

    public String getInflowLabel() {
        return this.inflowLabel;
    }

    public int getMaxLabelCount() {
        return this.maxLabelCount;
    }

    public AlphabeticIndex<V> setMaxLabelCount(int maxLabelCount2) {
        this.maxLabelCount = maxLabelCount2;
        this.buckets = null;
        return this;
    }

    private List<String> initLabels() {
        boolean checkDistinct;
        Normalizer2 nfkdNormalizer = Normalizer2.getNFKDInstance();
        List<String> indexCharacters = new ArrayList<>();
        String firstScriptBoundary = this.firstCharsInScripts.get(0);
        String overflowBoundary = this.firstCharsInScripts.get(this.firstCharsInScripts.size() - 1);
        Iterator<String> it = this.initialLabels.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if (!UTF16.hasMoreCodePointsThan(item, 1)) {
                checkDistinct = false;
            } else if (item.charAt(item.length() - 1) != '*' || item.charAt(item.length() - 2) == '*') {
                checkDistinct = true;
            } else {
                item = item.substring(0, item.length() - 1);
                checkDistinct = false;
            }
            if (this.collatorPrimaryOnly.compare(item, firstScriptBoundary) >= 0 && this.collatorPrimaryOnly.compare(item, overflowBoundary) < 0) {
                if (!checkDistinct || this.collatorPrimaryOnly.compare(item, separated(item)) != 0) {
                    int insertionPoint = Collections.binarySearch(indexCharacters, item, this.collatorPrimaryOnly);
                    if (insertionPoint < 0) {
                        indexCharacters.add(~insertionPoint, item);
                    } else if (isOneLabelBetterThanOther(nfkdNormalizer, item, indexCharacters.get(insertionPoint))) {
                        indexCharacters.set(insertionPoint, item);
                    }
                }
            }
        }
        int size = indexCharacters.size() - 1;
        if (size > this.maxLabelCount) {
            int count = 0;
            int old = -1;
            Iterator<String> it2 = indexCharacters.iterator();
            while (it2.hasNext()) {
                count++;
                it2.next();
                int bump = (this.maxLabelCount * count) / size;
                if (bump == old) {
                    it2.remove();
                } else {
                    old = bump;
                }
            }
        }
        return indexCharacters;
    }

    private static String fixLabel(String current) {
        if (!current.startsWith(BASE)) {
            return current;
        }
        int rest = current.charAt(BASE.length());
        if (10240 >= rest || rest > 10495) {
            return current.substring(BASE.length());
        }
        return (rest - 10240) + "劃";
    }

    private void addIndexExemplars(ULocale locale) {
        UnicodeSet exemplars = LocaleData.getExemplarSet(locale, 0, 2);
        if (exemplars == null || exemplars.isEmpty()) {
            UnicodeSet exemplars2 = LocaleData.getExemplarSet(locale, 0, 0).cloneAsThawed();
            if (exemplars2.containsSome(97, 122) || exemplars2.size() == 0) {
                exemplars2.addAll(97, 122);
            }
            if (exemplars2.containsSome(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END)) {
                exemplars2.remove(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END).add((int) Normalizer2Impl.Hangul.HANGUL_BASE).add(45208).add(45796).add(46972).add(47560).add(48148).add(49324).add(50500).add(51088).add(52264).add(52852).add(53440).add(54028).add(54616);
            }
            if (exemplars2.containsSome(4608, 4991)) {
                UnicodeSetIterator it = new UnicodeSetIterator(new UnicodeSet("[[:Block=Ethiopic:]&[:Script=Ethiopic:]]"));
                while (it.next() && it.codepoint != UnicodeSetIterator.IS_STRING) {
                    if ((it.codepoint & 7) != 0) {
                        exemplars2.remove(it.codepoint);
                    }
                }
            }
            Iterator<String> it2 = exemplars2.iterator();
            while (it2.hasNext()) {
                this.initialLabels.add((CharSequence) UCharacter.toUpperCase(locale, it2.next()));
            }
            return;
        }
        this.initialLabels.addAll(exemplars);
    }

    private boolean addChineseIndexCharacters() {
        UnicodeSet contractions = new UnicodeSet();
        try {
            this.collatorPrimaryOnly.internalAddContractions(BASE.charAt(0), contractions);
            if (contractions.isEmpty()) {
                return false;
            }
            this.initialLabels.addAll(contractions);
            Iterator<String> it = contractions.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String s = it.next();
                char c = s.charAt(s.length() - 1);
                if ('A' <= c && c <= 'Z') {
                    this.initialLabels.add(65, 90);
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String separated(String item) {
        StringBuilder result = new StringBuilder();
        char last = item.charAt(0);
        result.append(last);
        for (int i = 1; i < item.length(); i++) {
            char ch = item.charAt(i);
            if (!UCharacter.isHighSurrogate(last) || !UCharacter.isLowSurrogate(ch)) {
                result.append(CGJ);
            }
            result.append(ch);
            last = ch;
        }
        return result.toString();
    }

    public ImmutableIndex<V> buildImmutableIndex() {
        BucketList<V> immutableBucketList;
        if (this.inputList == null || this.inputList.isEmpty()) {
            if (this.buckets == null) {
                this.buckets = createBucketList();
            }
            immutableBucketList = this.buckets;
        } else {
            immutableBucketList = createBucketList();
        }
        return new ImmutableIndex<>(immutableBucketList, this.collatorPrimaryOnly);
    }

    public List<String> getBucketLabels() {
        initBuckets();
        ArrayList<String> result = new ArrayList<>();
        Iterator<Bucket<V>> it = this.buckets.iterator();
        while (it.hasNext()) {
            result.add(it.next().getLabel());
        }
        return result;
    }

    public RuleBasedCollator getCollator() {
        if (this.collatorExternal == null) {
            try {
                this.collatorExternal = (RuleBasedCollator) this.collatorOriginal.clone();
            } catch (Exception e) {
                throw new IllegalStateException("Collator cannot be cloned", e);
            }
        }
        return this.collatorExternal;
    }

    public AlphabeticIndex<V> addRecord(CharSequence name, V data) {
        this.buckets = null;
        if (this.inputList == null) {
            this.inputList = new ArrayList();
        }
        this.inputList.add(new Record(name, data));
        return this;
    }

    public int getBucketIndex(CharSequence name) {
        initBuckets();
        return this.buckets.getBucketIndex(name, this.collatorPrimaryOnly);
    }

    public AlphabeticIndex<V> clearRecords() {
        if (this.inputList != null && !this.inputList.isEmpty()) {
            this.inputList.clear();
            this.buckets = null;
        }
        return this;
    }

    public int getBucketCount() {
        initBuckets();
        return this.buckets.getBucketCount();
    }

    public int getRecordCount() {
        if (this.inputList != null) {
            return this.inputList.size();
        }
        return 0;
    }

    public Iterator<Bucket<V>> iterator() {
        initBuckets();
        return this.buckets.iterator();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: android.icu.text.AlphabeticIndex$Bucket} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void initBuckets() {
        String upperBoundary;
        Bucket<V> nextBucket;
        String upperBoundary2;
        if (this.buckets == null) {
            this.buckets = createBucketList();
            if (this.inputList != null && !this.inputList.isEmpty()) {
                Collections.sort(this.inputList, this.recordComparator);
                Iterator<Bucket<V>> bucketIterator = this.buckets.fullIterator();
                Bucket<V> currentBucket = bucketIterator.next();
                if (bucketIterator.hasNext()) {
                    nextBucket = bucketIterator.next();
                    upperBoundary = nextBucket.lowerBoundary;
                } else {
                    nextBucket = null;
                    upperBoundary = null;
                }
                Iterator<Record<V>> it = this.inputList.iterator();
                while (it.hasNext()) {
                    Record<V> r = it.next();
                    while (upperBoundary != null && this.collatorPrimaryOnly.compare((Object) r.name, (Object) upperBoundary) >= 0) {
                        currentBucket = nextBucket;
                        if (bucketIterator.hasNext()) {
                            nextBucket = bucketIterator.next();
                            upperBoundary2 = nextBucket.lowerBoundary;
                        } else {
                            upperBoundary2 = null;
                        }
                    }
                    Bucket<V> bucket = currentBucket;
                    if (bucket.displayBucket != null) {
                        bucket = bucket.displayBucket;
                    }
                    if (bucket.records == null) {
                        List unused = bucket.records = new ArrayList();
                    }
                    bucket.records.add(r);
                }
            }
        }
    }

    private static boolean isOneLabelBetterThanOther(Normalizer2 nfkdNormalizer, String one, String other) {
        String n1 = nfkdNormalizer.normalize(one);
        String n2 = nfkdNormalizer.normalize(other);
        boolean z = false;
        int result = n1.codePointCount(0, n1.length()) - n2.codePointCount(0, n2.length());
        if (result != 0) {
            if (result < 0) {
                z = true;
            }
            return z;
        }
        int result2 = binaryCmp.compare(n1, n2);
        if (result2 != 0) {
            if (result2 < 0) {
                z = true;
            }
            return z;
        }
        if (binaryCmp.compare(one, other) < 0) {
            z = true;
        }
        return z;
    }

    private BucketList<V> createBucketList() {
        long variableTop;
        boolean hasInvisibleBuckets;
        List<String> indexCharacters;
        long variableTop2;
        String scriptUpperBoundary;
        List<String> indexCharacters2 = initLabels();
        if (this.collatorPrimaryOnly.isAlternateHandlingShifted()) {
            variableTop = ((long) this.collatorPrimaryOnly.getVariableTop()) & 4294967295L;
        } else {
            variableTop = 0;
        }
        boolean hasInvisibleBuckets2 = false;
        Bucket<V>[] asciiBuckets = new Bucket[26];
        Bucket<V>[] pinyinBuckets = new Bucket[26];
        boolean hasPinyin = false;
        ArrayList<Bucket<V>> bucketList = new ArrayList<>();
        bucketList.add(new Bucket(getUnderflowLabel(), "", Bucket.LabelType.UNDERFLOW));
        int scriptIndex = -1;
        String scriptUpperBoundary2 = "";
        Iterator<String> it = indexCharacters2.iterator();
        while (true) {
            int i = 1;
            if (!it.hasNext()) {
                break;
            }
            String current = it.next();
            if (this.collatorPrimaryOnly.compare(current, scriptUpperBoundary2) >= 0) {
                String inflowBoundary = scriptUpperBoundary2;
                String str = scriptUpperBoundary2;
                int scriptIndex2 = scriptIndex;
                boolean skippedScript = false;
                while (true) {
                    scriptIndex2 += i;
                    scriptUpperBoundary = this.firstCharsInScripts.get(scriptIndex2);
                    if (this.collatorPrimaryOnly.compare(current, scriptUpperBoundary) < 0) {
                        break;
                    }
                    boolean z = hasInvisibleBuckets2;
                    boolean z2 = skippedScript;
                    skippedScript = true;
                    String str2 = scriptUpperBoundary;
                    i = 1;
                }
                if (skippedScript) {
                    indexCharacters = indexCharacters2;
                    if (bucketList.size() > 1) {
                        hasInvisibleBuckets = hasInvisibleBuckets2;
                        boolean z3 = skippedScript;
                        bucketList.add(new Bucket(getInflowLabel(), inflowBoundary, Bucket.LabelType.INFLOW));
                    } else {
                        hasInvisibleBuckets = hasInvisibleBuckets2;
                    }
                } else {
                    indexCharacters = indexCharacters2;
                    hasInvisibleBuckets = hasInvisibleBuckets2;
                }
                scriptIndex = scriptIndex2;
                scriptUpperBoundary2 = scriptUpperBoundary;
            } else {
                indexCharacters = indexCharacters2;
                hasInvisibleBuckets = hasInvisibleBuckets2;
            }
            Bucket<V> bucket = new Bucket<>(fixLabel(current), current, Bucket.LabelType.NORMAL);
            bucketList.add(bucket);
            if (current.length() == 1) {
                char charAt = current.charAt(0);
                char c = charAt;
                if ('A' <= charAt && c <= 'Z') {
                    asciiBuckets[c - 'A'] = bucket;
                    if (!current.startsWith(BASE) || !hasMultiplePrimaryWeights(this.collatorPrimaryOnly, variableTop, current) || current.endsWith("￿")) {
                        variableTop2 = variableTop;
                    } else {
                        int i2 = bucketList.size() - 2;
                        while (true) {
                            Bucket<V> singleBucket = bucketList.get(i2);
                            if (singleBucket.labelType == Bucket.LabelType.NORMAL) {
                                if (singleBucket.displayBucket == null && !hasMultiplePrimaryWeights(this.collatorPrimaryOnly, variableTop, singleBucket.lowerBoundary)) {
                                    Bucket<V> bucket2 = bucket;
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(current);
                                    variableTop2 = variableTop;
                                    sb.append("￿");
                                    Bucket bucket3 = new Bucket("", sb.toString(), Bucket.LabelType.NORMAL);
                                    Bucket unused = bucket3.displayBucket = singleBucket;
                                    bucketList.add(bucket3);
                                    hasInvisibleBuckets2 = true;
                                    break;
                                }
                                i2--;
                                bucket = bucket;
                                variableTop = variableTop;
                            } else {
                                variableTop2 = variableTop;
                                break;
                            }
                        }
                        indexCharacters2 = indexCharacters;
                        variableTop = variableTop2;
                    }
                    hasInvisibleBuckets2 = hasInvisibleBuckets;
                    indexCharacters2 = indexCharacters;
                    variableTop = variableTop2;
                }
            }
            if (current.length() == BASE.length() + 1 && current.startsWith(BASE)) {
                char charAt2 = current.charAt(BASE.length());
                char c2 = charAt2;
                if ('A' <= charAt2 && c2 <= 'Z') {
                    pinyinBuckets[c2 - 'A'] = bucket;
                    hasPinyin = true;
                }
            }
            if (!current.startsWith(BASE)) {
            }
            variableTop2 = variableTop;
            hasInvisibleBuckets2 = hasInvisibleBuckets;
            indexCharacters2 = indexCharacters;
            variableTop = variableTop2;
        }
        long j = variableTop;
        boolean hasInvisibleBuckets3 = hasInvisibleBuckets2;
        int i3 = 0;
        if (bucketList.size() == 1) {
            return new BucketList<>(bucketList, bucketList);
        }
        bucketList.add(new Bucket(getOverflowLabel(), scriptUpperBoundary2, Bucket.LabelType.OVERFLOW));
        if (hasPinyin) {
            Bucket<V> asciiBucket = null;
            while (true) {
                int i4 = i3;
                if (i4 >= 26) {
                    break;
                }
                if (asciiBuckets[i4] != null) {
                    asciiBucket = asciiBuckets[i4];
                }
                if (!(pinyinBuckets[i4] == null || asciiBucket == null)) {
                    Bucket unused2 = pinyinBuckets[i4].displayBucket = asciiBucket;
                    hasInvisibleBuckets3 = true;
                }
                i3 = i4 + 1;
            }
        }
        if (!hasInvisibleBuckets3) {
            return new BucketList<>(bucketList, bucketList);
        }
        int i5 = bucketList.size() - 1;
        Bucket<V> nextBucket = bucketList.get(i5);
        while (true) {
            i5--;
            if (i5 <= 0) {
                break;
            }
            Bucket<V> bucket4 = bucketList.get(i5);
            if (bucket4.displayBucket == null) {
                if (bucket4.labelType != Bucket.LabelType.INFLOW || nextBucket.labelType == Bucket.LabelType.NORMAL) {
                    nextBucket = bucket4;
                } else {
                    Bucket unused3 = bucket4.displayBucket = nextBucket;
                }
            }
        }
        ArrayList<Bucket<V>> publicBucketList = new ArrayList<>();
        Iterator<Bucket<V>> it2 = bucketList.iterator();
        while (it2.hasNext()) {
            Bucket<V> bucket5 = it2.next();
            if (bucket5.displayBucket == null) {
                publicBucketList.add(bucket5);
            }
        }
        return new BucketList<>(bucketList, publicBucketList);
    }

    private static boolean hasMultiplePrimaryWeights(RuleBasedCollator coll, long variableTop, String s) {
        long[] ces = coll.internalGetCEs(s);
        boolean seenPrimary = false;
        for (long ce : ces) {
            if ((ce >>> 32) > variableTop) {
                if (seenPrimary) {
                    return true;
                }
                seenPrimary = true;
            }
        }
        return false;
    }

    @Deprecated
    public List<String> getFirstCharactersInScripts() {
        List<String> dest = new ArrayList<>(200);
        UnicodeSet set = new UnicodeSet();
        this.collatorPrimaryOnly.internalAddContractions(64977, set);
        if (!set.isEmpty()) {
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String boundary = it.next();
                if (((1 << UCharacter.getType(boundary.codePointAt(1))) & 63) != 0) {
                    dest.add(boundary);
                }
            }
            return dest;
        }
        throw new UnsupportedOperationException("AlphabeticIndex requires script-first-primary contractions");
    }
}
