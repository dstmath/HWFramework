package android.icu.text;

import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.lang.UCharacter;
import android.icu.text.UTF16.StringComparator;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class AlphabeticIndex<V> implements Iterable<Bucket<V>> {
    static final /* synthetic */ boolean -assertionsDisabled = (AlphabeticIndex.class.desiredAssertionStatus() ^ 1);
    private static final String BASE = "﷐";
    private static final char CGJ = '͏';
    private static final int GC_CN_MASK = 1;
    private static final int GC_LL_MASK = 4;
    private static final int GC_LM_MASK = 16;
    private static final int GC_LO_MASK = 32;
    private static final int GC_LT_MASK = 8;
    private static final int GC_LU_MASK = 2;
    private static final int GC_L_MASK = 62;
    private static final Comparator<String> binaryCmp = new StringComparator(true, false, 0);
    private BucketList<V> buckets;
    private RuleBasedCollator collatorExternal;
    private final RuleBasedCollator collatorOriginal;
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
        private Bucket<V> displayBucket;
        private int displayIndex;
        private final String label;
        private final LabelType labelType;
        private final String lowerBoundary;
        private List<Record<V>> records;

        public enum LabelType {
            NORMAL,
            UNDERFLOW,
            INFLOW,
            OVERFLOW
        }

        /* synthetic */ Bucket(String label, String lowerBoundary, LabelType labelType, Bucket -this3) {
            this(label, lowerBoundary, labelType);
        }

        private Bucket(String label, String lowerBoundary, LabelType labelType) {
            this.label = label;
            this.lowerBoundary = lowerBoundary;
            this.labelType = labelType;
        }

        public String getLabel() {
            return this.label;
        }

        public LabelType getLabelType() {
            return this.labelType;
        }

        public int size() {
            return this.records == null ? 0 : this.records.size();
        }

        public Iterator<Record<V>> iterator() {
            if (this.records == null) {
                return Collections.emptyList().iterator();
            }
            return this.records.iterator();
        }

        public String toString() {
            return "{labelType=" + this.labelType + ", " + "lowerBoundary=" + this.lowerBoundary + ", " + "label=" + this.label + "}";
        }
    }

    private static class BucketList<V> implements Iterable<Bucket<V>> {
        private final ArrayList<Bucket<V>> bucketList;
        private final List<Bucket<V>> immutableVisibleList;

        /* synthetic */ BucketList(ArrayList bucketList, ArrayList publicBucketList, BucketList -this2) {
            this(bucketList, publicBucketList);
        }

        private BucketList(ArrayList<Bucket<V>> bucketList, ArrayList<Bucket<V>> publicBucketList) {
            this.bucketList = bucketList;
            int displayIndex = 0;
            for (Bucket<V> bucket : publicBucketList) {
                int displayIndex2 = displayIndex + 1;
                bucket.displayIndex = displayIndex;
                displayIndex = displayIndex2;
            }
            this.immutableVisibleList = Collections.unmodifiableList(publicBucketList);
        }

        private int getBucketCount() {
            return this.immutableVisibleList.size();
        }

        private int getBucketIndex(CharSequence name, Collator collatorPrimaryOnly) {
            int start = 0;
            int limit = this.bucketList.size();
            while (start + 1 < limit) {
                int i = (start + limit) / 2;
                if (collatorPrimaryOnly.compare((Object) name, ((Bucket) this.bucketList.get(i)).lowerBoundary) < 0) {
                    limit = i;
                } else {
                    start = i;
                }
            }
            Bucket<V> bucket = (Bucket) this.bucketList.get(start);
            if (bucket.displayBucket != null) {
                bucket = bucket.displayBucket;
            }
            return bucket.displayIndex;
        }

        private Iterator<Bucket<V>> fullIterator() {
            return this.bucketList.iterator();
        }

        public Iterator<Bucket<V>> iterator() {
            return this.immutableVisibleList.iterator();
        }
    }

    public static final class ImmutableIndex<V> implements Iterable<Bucket<V>> {
        private final BucketList<V> buckets;
        private final Collator collatorPrimaryOnly;

        /* synthetic */ ImmutableIndex(BucketList bucketList, Collator collatorPrimaryOnly, ImmutableIndex -this2) {
            this(bucketList, collatorPrimaryOnly);
        }

        private ImmutableIndex(BucketList<V> bucketList, Collator collatorPrimaryOnly) {
            this.buckets = bucketList;
            this.collatorPrimaryOnly = collatorPrimaryOnly;
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
        private final CharSequence name;

        /* synthetic */ Record(CharSequence name, Object data, Record -this2) {
            this(name, data);
        }

        private Record(CharSequence name, V data) {
            this.name = name;
            this.data = data;
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
        if (collator == null) {
            collator = (RuleBasedCollator) Collator.getInstance(locale);
        }
        this.collatorOriginal = collator;
        try {
            this.collatorPrimaryOnly = this.collatorOriginal.cloneAsThawed();
            this.collatorPrimaryOnly.setStrength(0);
            this.collatorPrimaryOnly.freeze();
            this.firstCharsInScripts = getFirstCharactersInScripts();
            Collections.sort(this.firstCharsInScripts, this.collatorPrimaryOnly);
            while (!this.firstCharsInScripts.isEmpty()) {
                if (this.collatorPrimaryOnly.compare((String) this.firstCharsInScripts.get(0), "") == 0) {
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

    public AlphabeticIndex<V> setOverflowLabel(String overflowLabel) {
        this.overflowLabel = overflowLabel;
        this.buckets = null;
        return this;
    }

    public String getUnderflowLabel() {
        return this.underflowLabel;
    }

    public AlphabeticIndex<V> setUnderflowLabel(String underflowLabel) {
        this.underflowLabel = underflowLabel;
        this.buckets = null;
        return this;
    }

    public String getOverflowLabel() {
        return this.overflowLabel;
    }

    public AlphabeticIndex<V> setInflowLabel(String inflowLabel) {
        this.inflowLabel = inflowLabel;
        this.buckets = null;
        return this;
    }

    public String getInflowLabel() {
        return this.inflowLabel;
    }

    public int getMaxLabelCount() {
        return this.maxLabelCount;
    }

    public AlphabeticIndex<V> setMaxLabelCount(int maxLabelCount) {
        this.maxLabelCount = maxLabelCount;
        this.buckets = null;
        return this;
    }

    private List<String> initLabels() {
        Normalizer2 nfkdNormalizer = Normalizer2.getNFKDInstance();
        List<String> indexCharacters = new ArrayList();
        String firstScriptBoundary = (String) this.firstCharsInScripts.get(0);
        String overflowBoundary = (String) this.firstCharsInScripts.get(this.firstCharsInScripts.size() - 1);
        for (String item : this.initialLabels) {
            String item2;
            boolean checkDistinct;
            if (!UTF16.hasMoreCodePointsThan(item2, 1)) {
                checkDistinct = false;
            } else if (item2.charAt(item2.length() - 1) != '*' || item2.charAt(item2.length() - 2) == '*') {
                checkDistinct = true;
            } else {
                item2 = item2.substring(0, item2.length() - 1);
                checkDistinct = false;
            }
            if (this.collatorPrimaryOnly.compare(item2, firstScriptBoundary) >= 0 && this.collatorPrimaryOnly.compare(item2, overflowBoundary) < 0) {
                if (!checkDistinct || this.collatorPrimaryOnly.compare(item2, separated(item2)) != 0) {
                    int insertionPoint = Collections.binarySearch(indexCharacters, item2, this.collatorPrimaryOnly);
                    if (insertionPoint < 0) {
                        indexCharacters.add(~insertionPoint, item2);
                    } else if (isOneLabelBetterThanOther(nfkdNormalizer, item2, (String) indexCharacters.get(insertionPoint))) {
                        indexCharacters.set(insertionPoint, item2);
                    }
                }
            }
        }
        int size = indexCharacters.size() - 1;
        if (size > this.maxLabelCount) {
            int count = 0;
            int old = -1;
            Iterator<String> it = indexCharacters.iterator();
            while (it.hasNext()) {
                count++;
                it.next();
                int bump = (this.maxLabelCount * count) / size;
                if (bump == old) {
                    it.remove();
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
        if (exemplars != null) {
            this.initialLabels.addAll(exemplars);
            return;
        }
        UnicodeSet<String> exemplars2 = LocaleData.getExemplarSet(locale, 0, 0).cloneAsThawed();
        if (exemplars2.containsSome(97, 122) || exemplars2.size() == 0) {
            exemplars2.addAll(97, 122);
        }
        if (exemplars2.containsSome(Hangul.HANGUL_BASE, Hangul.HANGUL_END)) {
            exemplars2.remove(Hangul.HANGUL_BASE, Hangul.HANGUL_END).add((int) Hangul.HANGUL_BASE).add(45208).add(45796).add(46972).add(47560).add(48148).add(49324).add(50500).add(51088).add(52264).add(52852).add(53440).add(54028).add(54616);
        }
        if (exemplars2.containsSome(4608, 4991)) {
            UnicodeSetIterator it = new UnicodeSetIterator(new UnicodeSet("[[:Block=Ethiopic:]&[:Script=Ethiopic:]]"));
            while (it.next() && it.codepoint != UnicodeSetIterator.IS_STRING) {
                if ((it.codepoint & 7) != 0) {
                    exemplars2.remove(it.codepoint);
                }
            }
        }
        for (String item : exemplars2) {
            this.initialLabels.add(UCharacter.toUpperCase(locale, item));
        }
    }

    private boolean addChineseIndexCharacters() {
        UnicodeSet<String> contractions = new UnicodeSet();
        try {
            this.collatorPrimaryOnly.internalAddContractions(BASE.charAt(0), contractions);
            if (contractions.isEmpty()) {
                return false;
            }
            this.initialLabels.addAll((UnicodeSet) contractions);
            for (String s : contractions) {
                if (-assertionsDisabled || s.startsWith(BASE)) {
                    char c = s.charAt(s.length() - 1);
                    if ('A' <= c && c <= 'Z') {
                        this.initialLabels.add(65, 90);
                        break;
                    }
                } else {
                    throw new AssertionError();
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
            if (!UCharacter.isHighSurrogate(last) || (UCharacter.isLowSurrogate(ch) ^ 1) != 0) {
                result.append(CGJ);
            }
            result.append(ch);
            last = ch;
        }
        return result.toString();
    }

    public ImmutableIndex<V> buildImmutableIndex() {
        BucketList<V> immutableBucketList;
        if (this.inputList == null || (this.inputList.isEmpty() ^ 1) == 0) {
            if (this.buckets == null) {
                this.buckets = createBucketList();
            }
            immutableBucketList = this.buckets;
        } else {
            immutableBucketList = createBucketList();
        }
        return new ImmutableIndex(immutableBucketList, this.collatorPrimaryOnly, null);
    }

    public List<String> getBucketLabels() {
        initBuckets();
        ArrayList<String> result = new ArrayList();
        for (Bucket<V> bucket : this.buckets) {
            result.add(bucket.getLabel());
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
        this.inputList.add(new Record(name, data, null));
        return this;
    }

    public int getBucketIndex(CharSequence name) {
        initBuckets();
        return this.buckets.getBucketIndex(name, this.collatorPrimaryOnly);
    }

    public AlphabeticIndex<V> clearRecords() {
        if (!(this.inputList == null || (this.inputList.isEmpty() ^ 1) == 0)) {
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
        return this.inputList != null ? this.inputList.size() : 0;
    }

    public Iterator<Bucket<V>> iterator() {
        initBuckets();
        return this.buckets.iterator();
    }

    private void initBuckets() {
        if (this.buckets == null) {
            this.buckets = createBucketList();
            if (this.inputList != null && !this.inputList.isEmpty()) {
                Bucket<V> nextBucket;
                String upperBoundary;
                Collections.sort(this.inputList, this.recordComparator);
                Iterator<Bucket<V>> bucketIterator = this.buckets.fullIterator();
                Bucket<V> currentBucket = (Bucket) bucketIterator.next();
                if (bucketIterator.hasNext()) {
                    nextBucket = (Bucket) bucketIterator.next();
                    upperBoundary = nextBucket.lowerBoundary;
                } else {
                    nextBucket = null;
                    upperBoundary = null;
                }
                for (Record<V> r : this.inputList) {
                    while (upperBoundary != null && this.collatorPrimaryOnly.compare((Object) r.name, (Object) upperBoundary) >= 0) {
                        currentBucket = nextBucket;
                        if (bucketIterator.hasNext()) {
                            nextBucket = (Bucket) bucketIterator.next();
                            upperBoundary = nextBucket.lowerBoundary;
                        } else {
                            upperBoundary = null;
                        }
                    }
                    Bucket<V> bucket = currentBucket;
                    if (currentBucket.displayBucket != null) {
                        bucket = bucket.displayBucket;
                    }
                    if (bucket.records == null) {
                        bucket.records = new ArrayList();
                    }
                    bucket.records.add(r);
                }
            }
        }
    }

    private static boolean isOneLabelBetterThanOther(Normalizer2 nfkdNormalizer, String one, String other) {
        boolean z = true;
        String n1 = nfkdNormalizer.normalize(one);
        String n2 = nfkdNormalizer.normalize(other);
        int result = n1.codePointCount(0, n1.length()) - n2.codePointCount(0, n2.length());
        if (result != 0) {
            if (result >= 0) {
                z = false;
            }
            return z;
        }
        result = binaryCmp.compare(n1, n2);
        if (result != 0) {
            if (result >= 0) {
                z = false;
            }
            return z;
        }
        if (binaryCmp.compare(one, other) >= 0) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:92:0x0059 A:{SYNTHETIC, EDGE_INSN: B:92:0x0059->B:87:0x0059 ?: BREAK  , EDGE_INSN: B:92:0x0059->B:87:0x0059 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0141  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private BucketList<V> createBucketList() {
        long variableTop;
        Bucket<V> bucket;
        int i;
        List<String> indexCharacters = initLabels();
        if (this.collatorPrimaryOnly.isAlternateHandlingShifted()) {
            variableTop = ((long) this.collatorPrimaryOnly.getVariableTop()) & 4294967295L;
        } else {
            variableTop = 0;
        }
        boolean hasInvisibleBuckets = false;
        Bucket<V>[] asciiBuckets = new Bucket[26];
        Bucket<V>[] pinyinBuckets = new Bucket[26];
        boolean hasPinyin = false;
        ArrayList<Bucket<V>> bucketList = new ArrayList();
        bucketList.add(new Bucket(getUnderflowLabel(), "", LabelType.UNDERFLOW, null));
        int scriptIndex = -1;
        String scriptUpperBoundary = "";
        for (String current : indexCharacters) {
            char c;
            Bucket<V> singleBucket;
            if (this.collatorPrimaryOnly.compare(current, scriptUpperBoundary) >= 0) {
                String inflowBoundary = scriptUpperBoundary;
                boolean skippedScript = false;
                while (true) {
                    scriptIndex++;
                    scriptUpperBoundary = (String) this.firstCharsInScripts.get(scriptIndex);
                    if (this.collatorPrimaryOnly.compare(current, scriptUpperBoundary) < 0) {
                        break;
                    }
                    skippedScript = true;
                }
                if (skippedScript && bucketList.size() > 1) {
                    bucketList.add(new Bucket(getInflowLabel(), inflowBoundary, LabelType.INFLOW, null));
                }
            }
            bucket = new Bucket(fixLabel(current), current, LabelType.NORMAL, null);
            bucketList.add(bucket);
            if (current.length() == 1) {
                c = current.charAt(0);
                if ('A' <= c && c <= 'Z') {
                    asciiBuckets[c - 65] = bucket;
                    if (!current.startsWith(BASE) && hasMultiplePrimaryWeights(this.collatorPrimaryOnly, variableTop, current) && (current.endsWith("￿") ^ 1) != 0) {
                        i = bucketList.size() - 2;
                        while (true) {
                            singleBucket = (Bucket) bucketList.get(i);
                            if (singleBucket.labelType != LabelType.NORMAL) {
                                break;
                            }
                            if (singleBucket.displayBucket == null) {
                                if ((hasMultiplePrimaryWeights(this.collatorPrimaryOnly, variableTop, singleBucket.lowerBoundary) ^ 1) != 0) {
                                    bucket = new Bucket("", current + "￿", LabelType.NORMAL, null);
                                    bucket.displayBucket = singleBucket;
                                    bucketList.add(bucket);
                                    hasInvisibleBuckets = true;
                                    break;
                                }
                            }
                            i--;
                        }
                    }
                }
            }
            if (current.length() == BASE.length() + 1 && current.startsWith(BASE)) {
                c = current.charAt(BASE.length());
                if ('A' <= c && c <= 'Z') {
                    pinyinBuckets[c - 65] = bucket;
                    hasPinyin = true;
                }
            }
            i = bucketList.size() - 2;
            while (true) {
                singleBucket = (Bucket) bucketList.get(i);
                if (singleBucket.labelType != LabelType.NORMAL) {
                }
                i--;
            }
        }
        if (bucketList.size() == 1) {
            return new BucketList(bucketList, bucketList, null);
        }
        bucketList.add(new Bucket(getOverflowLabel(), scriptUpperBoundary, LabelType.OVERFLOW, null));
        if (hasPinyin) {
            Bucket<V> asciiBucket = null;
            for (i = 0; i < 26; i++) {
                if (asciiBuckets[i] != null) {
                    asciiBucket = asciiBuckets[i];
                }
                if (!(pinyinBuckets[i] == null || asciiBucket == null)) {
                    pinyinBuckets[i].displayBucket = asciiBucket;
                    hasInvisibleBuckets = true;
                }
            }
        }
        if (!hasInvisibleBuckets) {
            return new BucketList(bucketList, bucketList, null);
        }
        i = bucketList.size() - 1;
        Bucket<V> nextBucket = (Bucket) bucketList.get(i);
        while (true) {
            i--;
            if (i <= 0) {
                break;
            }
            bucket = (Bucket) bucketList.get(i);
            if (bucket.displayBucket == null) {
                if (bucket.labelType != LabelType.INFLOW || nextBucket.labelType == LabelType.NORMAL) {
                    nextBucket = bucket;
                } else {
                    bucket.displayBucket = nextBucket;
                }
            }
        }
        ArrayList<Bucket<V>> publicBucketList = new ArrayList();
        for (Bucket<V> bucket2 : bucketList) {
            if (bucket2.displayBucket == null) {
                publicBucketList.add(bucket2);
            }
        }
        return new BucketList(bucketList, publicBucketList, null);
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
        List<String> dest = new ArrayList(200);
        UnicodeSet<String> set = new UnicodeSet();
        this.collatorPrimaryOnly.internalAddContractions(64977, set);
        if (set.isEmpty()) {
            throw new UnsupportedOperationException("AlphabeticIndex requires script-first-primary contractions");
        }
        for (String boundary : set) {
            if (((1 << UCharacter.getType(boundary.codePointAt(1))) & 63) != 0) {
                dest.add(boundary);
            }
        }
        return dest;
    }
}
