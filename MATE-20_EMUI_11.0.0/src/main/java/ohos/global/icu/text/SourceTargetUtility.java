package ohos.global.icu.text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import ohos.global.icu.lang.CharSequences;

class SourceTargetUtility {
    static Normalizer2 NFC = Normalizer2.getNFCInstance();
    static final UnicodeSet NON_STARTERS = new UnicodeSet("[:^ccc=0:]").freeze();
    final UnicodeSet sourceCache;
    final Set<String> sourceStrings;
    final Transform<String, String> transform;

    public SourceTargetUtility(Transform<String, String> transform2) {
        this(transform2, null);
    }

    public SourceTargetUtility(Transform<String, String> transform2, Normalizer2 normalizer2) {
        boolean z;
        String decomposition;
        this.transform = transform2;
        if (normalizer2 != null) {
            this.sourceCache = new UnicodeSet("[:^ccc=0:]");
        } else {
            this.sourceCache = new UnicodeSet();
        }
        this.sourceStrings = new HashSet();
        for (int i = 0; i <= 1114111; i++) {
            if (!CharSequences.equals(i, transform2.transform(UTF16.valueOf(i)))) {
                this.sourceCache.add(i);
                z = true;
            } else {
                z = false;
            }
            if (!(normalizer2 == null || (decomposition = NFC.getDecomposition(i)) == null)) {
                if (!decomposition.equals(transform2.transform(decomposition))) {
                    this.sourceStrings.add(decomposition);
                }
                if (!z && !normalizer2.isInert(i)) {
                    this.sourceCache.add(i);
                }
            }
        }
        this.sourceCache.freeze();
    }

    public void addSourceTargetSet(Transliterator transliterator, UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        UnicodeSet filterAsUnicodeSet = transliterator.getFilterAsUnicodeSet(unicodeSet);
        UnicodeSet retainAll = new UnicodeSet(this.sourceCache).retainAll(filterAsUnicodeSet);
        unicodeSet2.addAll(retainAll);
        Iterator<String> it = retainAll.iterator();
        while (it.hasNext()) {
            unicodeSet3.addAll(this.transform.transform(it.next()));
        }
        for (String str : this.sourceStrings) {
            if (filterAsUnicodeSet.containsAll(str)) {
                String transform2 = this.transform.transform(str);
                if (!str.equals(transform2)) {
                    unicodeSet3.addAll(transform2);
                    unicodeSet2.addAll(str);
                }
            }
        }
    }
}
