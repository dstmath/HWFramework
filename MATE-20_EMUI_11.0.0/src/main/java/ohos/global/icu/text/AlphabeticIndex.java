package ohos.global.icu.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import ohos.agp.render.opengl.GLES20;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.util.LocaleData;
import ohos.global.icu.util.ULocale;

public final class AlphabeticIndex<V> implements Iterable<Bucket<V>> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String BASE = "﷐";
    private static final char CGJ = 847;
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

    public static final class ImmutableIndex<V> implements Iterable<Bucket<V>> {
        private final BucketList<V> buckets;
        private final Collator collatorPrimaryOnly;

        private ImmutableIndex(BucketList<V> bucketList, Collator collator) {
            this.buckets = bucketList;
            this.collatorPrimaryOnly = collator;
        }

        public int getBucketCount() {
            return this.buckets.getBucketCount();
        }

        public int getBucketIndex(CharSequence charSequence) {
            return this.buckets.getBucketIndex(charSequence, this.collatorPrimaryOnly);
        }

        public Bucket<V> getBucket(int i) {
            if (i < 0 || i >= this.buckets.getBucketCount()) {
                return null;
            }
            return (Bucket) ((BucketList) this.buckets).immutableVisibleList.get(i);
        }

        @Override // java.lang.Iterable
        public Iterator<Bucket<V>> iterator() {
            return this.buckets.iterator();
        }
    }

    public AlphabeticIndex(ULocale uLocale) {
        this(uLocale, null);
    }

    public AlphabeticIndex(Locale locale) {
        this(ULocale.forLocale(locale), null);
    }

    public AlphabeticIndex(RuleBasedCollator ruleBasedCollator) {
        this(null, ruleBasedCollator);
    }

    private AlphabeticIndex(ULocale uLocale, RuleBasedCollator ruleBasedCollator) {
        this.recordComparator = new Comparator<Record<V>>() {
            /* class ohos.global.icu.text.AlphabeticIndex.AnonymousClass1 */

            @Override // java.util.Comparator
            public /* bridge */ /* synthetic */ int compare(Object obj, Object obj2) {
                return compare((Record) ((Record) obj), (Record) ((Record) obj2));
            }

            public int compare(Record<V> record, Record<V> record2) {
                return AlphabeticIndex.this.collatorOriginal.compare(((Record) record).name, ((Record) record2).name);
            }
        };
        this.initialLabels = new UnicodeSet();
        this.overflowLabel = "…";
        this.underflowLabel = "…";
        this.inflowLabel = "…";
        this.maxLabelCount = 99;
        this.collatorOriginal = ruleBasedCollator == null ? (RuleBasedCollator) Collator.getInstance(uLocale) : ruleBasedCollator;
        try {
            this.collatorPrimaryOnly = this.collatorOriginal.cloneAsThawed();
            this.collatorPrimaryOnly.setStrength(0);
            this.collatorPrimaryOnly.freeze();
            this.firstCharsInScripts = getFirstCharactersInScripts();
            Collections.sort(this.firstCharsInScripts, this.collatorPrimaryOnly);
            while (!this.firstCharsInScripts.isEmpty()) {
                if (this.collatorPrimaryOnly.compare(this.firstCharsInScripts.get(0), "") == 0) {
                    this.firstCharsInScripts.remove(0);
                } else if (!addChineseIndexCharacters() && uLocale != null) {
                    addIndexExemplars(uLocale);
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

    public AlphabeticIndex<V> addLabels(UnicodeSet unicodeSet) {
        this.initialLabels.addAll(unicodeSet);
        this.buckets = null;
        return this;
    }

    public AlphabeticIndex<V> addLabels(ULocale... uLocaleArr) {
        for (ULocale uLocale : uLocaleArr) {
            addIndexExemplars(uLocale);
        }
        this.buckets = null;
        return this;
    }

    public AlphabeticIndex<V> addLabels(Locale... localeArr) {
        for (Locale locale : localeArr) {
            addIndexExemplars(ULocale.forLocale(locale));
        }
        this.buckets = null;
        return this;
    }

    public AlphabeticIndex<V> setOverflowLabel(String str) {
        this.overflowLabel = str;
        this.buckets = null;
        return this;
    }

    public String getUnderflowLabel() {
        return this.underflowLabel;
    }

    public AlphabeticIndex<V> setUnderflowLabel(String str) {
        this.underflowLabel = str;
        this.buckets = null;
        return this;
    }

    public String getOverflowLabel() {
        return this.overflowLabel;
    }

    public AlphabeticIndex<V> setInflowLabel(String str) {
        this.inflowLabel = str;
        this.buckets = null;
        return this;
    }

    public String getInflowLabel() {
        return this.inflowLabel;
    }

    public int getMaxLabelCount() {
        return this.maxLabelCount;
    }

    public AlphabeticIndex<V> setMaxLabelCount(int i) {
        this.maxLabelCount = i;
        this.buckets = null;
        return this;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x008c  */
    private List<String> initLabels() {
        boolean z;
        int binarySearch;
        Normalizer2 nFKDInstance = Normalizer2.getNFKDInstance();
        ArrayList arrayList = new ArrayList();
        int i = 0;
        String str = this.firstCharsInScripts.get(0);
        List<String> list = this.firstCharsInScripts;
        String str2 = list.get(list.size() - 1);
        Iterator<String> it = this.initialLabels.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (UTF16.hasMoreCodePointsThan(next, 1)) {
                if (next.charAt(next.length() - 1) != '*' || next.charAt(next.length() - 2) == '*') {
                    z = true;
                    if (this.collatorPrimaryOnly.compare(next, str) >= 0 && this.collatorPrimaryOnly.compare(next, str2) < 0) {
                        if (z || this.collatorPrimaryOnly.compare(next, separated(next)) != 0) {
                            binarySearch = Collections.binarySearch(arrayList, next, this.collatorPrimaryOnly);
                            if (binarySearch >= 0) {
                                arrayList.add(~binarySearch, next);
                            } else if (isOneLabelBetterThanOther(nFKDInstance, next, (String) arrayList.get(binarySearch))) {
                                arrayList.set(binarySearch, next);
                            }
                        }
                    }
                } else {
                    next = next.substring(0, next.length() - 1);
                }
            }
            z = false;
            if (z) {
            }
            binarySearch = Collections.binarySearch(arrayList, next, this.collatorPrimaryOnly);
            if (binarySearch >= 0) {
            }
        }
        int size = arrayList.size() - 1;
        if (size > this.maxLabelCount) {
            int i2 = -1;
            Iterator it2 = arrayList.iterator();
            while (it2.hasNext()) {
                i++;
                it2.next();
                int i3 = (this.maxLabelCount * i) / size;
                if (i3 == i2) {
                    it2.remove();
                } else {
                    i2 = i3;
                }
            }
        }
        return arrayList;
    }

    private static String fixLabel(String str) {
        if (!str.startsWith(BASE)) {
            return str;
        }
        char charAt = str.charAt(1);
        if (10240 >= charAt || charAt > 10495) {
            return str.substring(1);
        }
        return (charAt - GLES20.GL_TEXTURE_MAG_FILTER) + "劃";
    }

    private void addIndexExemplars(ULocale uLocale) {
        UnicodeSet exemplarSet = LocaleData.getExemplarSet(uLocale, 0, 2);
        if (exemplarSet == null || exemplarSet.isEmpty()) {
            UnicodeSet cloneAsThawed = LocaleData.getExemplarSet(uLocale, 0, 0).cloneAsThawed();
            if (cloneAsThawed.containsSome(97, 122) || cloneAsThawed.isEmpty()) {
                cloneAsThawed.addAll(97, 122);
            }
            if (cloneAsThawed.containsSome(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END)) {
                cloneAsThawed.remove(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END).add(Normalizer2Impl.Hangul.HANGUL_BASE).add(45208).add(45796).add(46972).add(47560).add(48148).add(49324).add(50500).add(51088).add(52264).add(52852).add(53440).add(54028).add(54616);
            }
            if (cloneAsThawed.containsSome(4608, 4991)) {
                UnicodeSet unicodeSet = new UnicodeSet("[ሀለሐመሠረሰሸቀቈቐቘበቨተቸኀኈነኘአከኰኸዀወዐዘዠየደዸጀገጐጘጠጨጰጸፀፈፐፘ]");
                unicodeSet.retainAll(cloneAsThawed);
                cloneAsThawed.remove(4608, 4991).addAll(unicodeSet);
            }
            Iterator<String> it = cloneAsThawed.iterator();
            while (it.hasNext()) {
                this.initialLabels.add(UCharacter.toUpperCase(uLocale, it.next()));
            }
            return;
        }
        this.initialLabels.addAll(exemplarSet);
    }

    private boolean addChineseIndexCharacters() {
        UnicodeSet unicodeSet = new UnicodeSet();
        try {
            this.collatorPrimaryOnly.internalAddContractions(BASE.charAt(0), unicodeSet);
            if (unicodeSet.isEmpty()) {
                return false;
            }
            this.initialLabels.addAll(unicodeSet);
            Iterator<String> it = unicodeSet.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String next = it.next();
                char charAt = next.charAt(next.length() - 1);
                if ('A' <= charAt && charAt <= 'Z') {
                    this.initialLabels.add(65, 90);
                    break;
                }
            }
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    private String separated(String str) {
        StringBuilder sb = new StringBuilder();
        char charAt = str.charAt(0);
        sb.append(charAt);
        int i = 1;
        while (i < str.length()) {
            char charAt2 = str.charAt(i);
            if (!UCharacter.isHighSurrogate(charAt) || !UCharacter.isLowSurrogate(charAt2)) {
                sb.append(CGJ);
            }
            sb.append(charAt2);
            i++;
            charAt = charAt2;
        }
        return sb.toString();
    }

    public ImmutableIndex<V> buildImmutableIndex() {
        BucketList<V> bucketList;
        List<Record<V>> list = this.inputList;
        if (list == null || list.isEmpty()) {
            if (this.buckets == null) {
                this.buckets = createBucketList();
            }
            bucketList = this.buckets;
        } else {
            bucketList = createBucketList();
        }
        return new ImmutableIndex<>(bucketList, this.collatorPrimaryOnly);
    }

    public List<String> getBucketLabels() {
        initBuckets();
        ArrayList arrayList = new ArrayList();
        Iterator<Bucket<V>> it = this.buckets.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().getLabel());
        }
        return arrayList;
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

    public AlphabeticIndex<V> addRecord(CharSequence charSequence, V v) {
        this.buckets = null;
        if (this.inputList == null) {
            this.inputList = new ArrayList();
        }
        this.inputList.add(new Record<>(charSequence, v));
        return this;
    }

    public int getBucketIndex(CharSequence charSequence) {
        initBuckets();
        return this.buckets.getBucketIndex(charSequence, this.collatorPrimaryOnly);
    }

    public AlphabeticIndex<V> clearRecords() {
        List<Record<V>> list = this.inputList;
        if (list != null && !list.isEmpty()) {
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
        List<Record<V>> list = this.inputList;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override // java.lang.Iterable
    public Iterator<Bucket<V>> iterator() {
        initBuckets();
        return this.buckets.iterator();
    }

    private void initBuckets() {
        String str;
        Bucket bucket;
        if (this.buckets == null) {
            this.buckets = createBucketList();
            List<Record<V>> list = this.inputList;
            if (list != null && !list.isEmpty()) {
                Collections.sort(this.inputList, this.recordComparator);
                Iterator fullIterator = this.buckets.fullIterator();
                Bucket bucket2 = (Bucket) fullIterator.next();
                if (fullIterator.hasNext()) {
                    bucket = (Bucket) fullIterator.next();
                    str = bucket.lowerBoundary;
                } else {
                    bucket = null;
                    str = null;
                }
                Iterator<Record<V>> it = this.inputList.iterator();
                while (it.hasNext()) {
                    Record<V> next = it.next();
                    while (str != null && this.collatorPrimaryOnly.compare(((Record) next).name, str) >= 0) {
                        if (fullIterator.hasNext()) {
                            Bucket bucket3 = (Bucket) fullIterator.next();
                            str = bucket3.lowerBoundary;
                            bucket = bucket3;
                            bucket2 = bucket;
                        } else {
                            bucket2 = bucket;
                            str = null;
                        }
                    }
                    Bucket bucket4 = bucket2.displayBucket != null ? bucket2.displayBucket : bucket2;
                    if (bucket4.records == null) {
                        bucket4.records = new ArrayList();
                    }
                    bucket4.records.add(next);
                }
            }
        }
    }

    private static boolean isOneLabelBetterThanOther(Normalizer2 normalizer2, String str, String str2) {
        String normalize = normalizer2.normalize(str);
        String normalize2 = normalizer2.normalize(str2);
        int codePointCount = normalize.codePointCount(0, normalize.length()) - normalize2.codePointCount(0, normalize2.length());
        if (codePointCount != 0) {
            return codePointCount < 0;
        }
        int compare = binaryCmp.compare(normalize, normalize2);
        return compare != 0 ? compare < 0 : binaryCmp.compare(str, str2) < 0;
    }

    public static class Record<V> {
        private final V data;
        private final CharSequence name;

        private Record(CharSequence charSequence, V v) {
            this.name = charSequence;
            this.data = v;
        }

        public CharSequence getName() {
            return this.name;
        }

        public V getData() {
            return this.data;
        }

        public String toString() {
            return ((Object) this.name) + "=" + ((Object) this.data);
        }
    }

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

        private Bucket(String str, String str2, LabelType labelType2) {
            this.label = str;
            this.lowerBoundary = str2;
            this.labelType = labelType2;
        }

        public String getLabel() {
            return this.label;
        }

        public LabelType getLabelType() {
            return this.labelType;
        }

        public int size() {
            List<Record<V>> list = this.records;
            if (list == null) {
                return 0;
            }
            return list.size();
        }

        @Override // java.lang.Iterable
        public Iterator<Record<V>> iterator() {
            List<Record<V>> list = this.records;
            if (list == null) {
                return Collections.emptyList().iterator();
            }
            return list.iterator();
        }

        @Override // java.lang.Object
        public String toString() {
            return "{labelType=" + this.labelType + ", lowerBoundary=" + this.lowerBoundary + ", label=" + this.label + "}";
        }
    }

    private BucketList<V> createBucketList() {
        Iterator<String> it;
        String str;
        char charAt;
        char charAt2;
        String str2;
        String str3;
        List<String> initLabels = initLabels();
        long variableTop = this.collatorPrimaryOnly.isAlternateHandlingShifted() ? ((long) this.collatorPrimaryOnly.getVariableTop()) & 4294967295L : 0;
        Bucket[] bucketArr = new Bucket[26];
        Bucket[] bucketArr2 = new Bucket[26];
        ArrayList arrayList = new ArrayList();
        AnonymousClass1 r12 = null;
        arrayList.add(new Bucket(getUnderflowLabel(), "", Bucket.LabelType.UNDERFLOW));
        Iterator<String> it2 = initLabels.iterator();
        String str4 = "";
        boolean z = false;
        int i = -1;
        boolean z2 = false;
        while (true) {
            int i2 = 1;
            if (!it2.hasNext()) {
                break;
            }
            String next = it2.next();
            if (this.collatorPrimaryOnly.compare(next, str4) >= 0) {
                boolean z3 = false;
                while (true) {
                    i += i2;
                    str2 = this.firstCharsInScripts.get(i);
                    if (this.collatorPrimaryOnly.compare(next, str2) < 0) {
                        break;
                    }
                    i2 = 1;
                    z3 = true;
                }
                if (!z3 || arrayList.size() <= 1) {
                    it = it2;
                    str3 = str2;
                    r12 = null;
                } else {
                    it = it2;
                    str3 = str2;
                    r12 = null;
                    arrayList.add(new Bucket(getInflowLabel(), str4, Bucket.LabelType.INFLOW));
                }
                str4 = str3;
            } else {
                it = it2;
            }
            Bucket bucket = new Bucket(fixLabel(next), next, Bucket.LabelType.NORMAL);
            arrayList.add(bucket);
            if (next.length() == 1 && 'A' <= (charAt2 = next.charAt(0)) && charAt2 <= 'Z') {
                bucketArr[charAt2 - 'A'] = bucket;
            } else if (next.length() == 2 && next.startsWith(BASE) && 'A' <= (charAt = next.charAt(1)) && charAt <= 'Z') {
                bucketArr2[charAt - 'A'] = bucket;
                z = true;
            }
            if (!next.startsWith(BASE) && hasMultiplePrimaryWeights(this.collatorPrimaryOnly, variableTop, next) && !next.endsWith("￿")) {
                int size = arrayList.size() - 2;
                while (true) {
                    Bucket bucket2 = (Bucket) arrayList.get(size);
                    str = str4;
                    if (bucket2.labelType != Bucket.LabelType.NORMAL) {
                        break;
                    }
                    if (bucket2.displayBucket == null && !hasMultiplePrimaryWeights(this.collatorPrimaryOnly, variableTop, bucket2.lowerBoundary)) {
                        Bucket bucket3 = new Bucket("", next + "￿", Bucket.LabelType.NORMAL);
                        bucket3.displayBucket = bucket2;
                        arrayList.add(bucket3);
                        z2 = true;
                        break;
                    }
                    size--;
                    str4 = str;
                }
            } else {
                str = str4;
            }
            it2 = it;
            str4 = str;
            r12 = null;
        }
        if (arrayList.size() == 1) {
            return new BucketList<>(arrayList, arrayList);
        }
        arrayList.add(new Bucket(getOverflowLabel(), str4, Bucket.LabelType.OVERFLOW));
        if (z) {
            Bucket bucket4 = null;
            for (int i3 = 0; i3 < 26; i3++) {
                if (bucketArr[i3] != null) {
                    bucket4 = bucketArr[i3];
                }
                if (!(bucketArr2[i3] == null || bucket4 == null)) {
                    bucketArr2[i3].displayBucket = bucket4;
                    z2 = true;
                }
            }
        }
        if (!z2) {
            return new BucketList<>(arrayList, arrayList);
        }
        int size2 = arrayList.size() - 1;
        Bucket bucket5 = (Bucket) arrayList.get(size2);
        while (true) {
            size2--;
            if (size2 <= 0) {
                break;
            }
            Bucket bucket6 = (Bucket) arrayList.get(size2);
            if (bucket6.displayBucket == null) {
                if (bucket6.labelType != Bucket.LabelType.INFLOW || bucket5.labelType == Bucket.LabelType.NORMAL) {
                    bucket5 = bucket6;
                } else {
                    bucket6.displayBucket = bucket5;
                }
            }
        }
        ArrayList arrayList2 = new ArrayList();
        Iterator it3 = arrayList.iterator();
        while (it3.hasNext()) {
            Bucket bucket7 = (Bucket) it3.next();
            if (bucket7.displayBucket == null) {
                arrayList2.add(bucket7);
            }
        }
        return new BucketList<>(arrayList, arrayList2);
    }

    /* access modifiers changed from: private */
    public static class BucketList<V> implements Iterable<Bucket<V>> {
        private final ArrayList<Bucket<V>> bucketList;
        private final List<Bucket<V>> immutableVisibleList;

        private BucketList(ArrayList<Bucket<V>> arrayList, ArrayList<Bucket<V>> arrayList2) {
            this.bucketList = arrayList;
            Iterator<Bucket<V>> it = arrayList2.iterator();
            int i = 0;
            while (it.hasNext()) {
                ((Bucket) it.next()).displayIndex = i;
                i++;
            }
            this.immutableVisibleList = Collections.unmodifiableList(arrayList2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getBucketCount() {
            return this.immutableVisibleList.size();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getBucketIndex(CharSequence charSequence, Collator collator) {
            int size = this.bucketList.size();
            int i = 0;
            while (i + 1 < size) {
                int i2 = (i + size) / 2;
                if (collator.compare(charSequence, ((Bucket) this.bucketList.get(i2)).lowerBoundary) < 0) {
                    size = i2;
                } else {
                    i = i2;
                }
            }
            Bucket<V> bucket = this.bucketList.get(i);
            if (((Bucket) bucket).displayBucket != null) {
                bucket = ((Bucket) bucket).displayBucket;
            }
            return ((Bucket) bucket).displayIndex;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Iterator<Bucket<V>> fullIterator() {
            return this.bucketList.iterator();
        }

        @Override // java.lang.Iterable
        public Iterator<Bucket<V>> iterator() {
            return this.immutableVisibleList.iterator();
        }
    }

    private static boolean hasMultiplePrimaryWeights(RuleBasedCollator ruleBasedCollator, long j, String str) {
        long[] internalGetCEs;
        boolean z = false;
        for (long j2 : ruleBasedCollator.internalGetCEs(str)) {
            if ((j2 >>> 32) > j) {
                if (z) {
                    return true;
                }
                z = true;
            }
        }
        return false;
    }

    @Deprecated
    public List<String> getFirstCharactersInScripts() {
        ArrayList arrayList = new ArrayList(200);
        UnicodeSet unicodeSet = new UnicodeSet();
        this.collatorPrimaryOnly.internalAddContractions(64977, unicodeSet);
        if (!unicodeSet.isEmpty()) {
            Iterator<String> it = unicodeSet.iterator();
            while (it.hasNext()) {
                String next = it.next();
                if (((1 << UCharacter.getType(next.codePointAt(1))) & 63) != 0) {
                    arrayList.add(next);
                }
            }
            return arrayList;
        }
        throw new UnsupportedOperationException("AlphabeticIndex requires script-first-primary contractions");
    }
}
