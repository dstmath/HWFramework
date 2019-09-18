package android.icu.text;

import android.icu.lang.CharSequences;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class SourceTargetUtility {
    static Normalizer2 NFC = Normalizer2.getNFCInstance();
    static final UnicodeSet NON_STARTERS = new UnicodeSet("[:^ccc=0:]").freeze();
    final UnicodeSet sourceCache;
    final Set<String> sourceStrings;
    final Transform<String, String> transform;

    public SourceTargetUtility(Transform<String, String> transform2) {
        this(transform2, null);
    }

    public SourceTargetUtility(Transform<String, String> transform2, Normalizer2 normalizer) {
        this.transform = transform2;
        if (normalizer != null) {
            this.sourceCache = new UnicodeSet("[:^ccc=0:]");
        } else {
            this.sourceCache = new UnicodeSet();
        }
        this.sourceStrings = new HashSet();
        for (int i = 0; i <= 1114111; i++) {
            boolean added = false;
            if (!CharSequences.equals(i, (CharSequence) transform2.transform(UTF16.valueOf(i)))) {
                this.sourceCache.add(i);
                added = true;
            }
            if (normalizer != null) {
                String d = NFC.getDecomposition(i);
                if (d != null) {
                    if (!d.equals(transform2.transform(d))) {
                        this.sourceStrings.add(d);
                    }
                    if (!added && !normalizer.isInert(i)) {
                        this.sourceCache.add(i);
                    }
                }
            }
        }
        this.sourceCache.freeze();
    }

    public void addSourceTargetSet(Transliterator transliterator, UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = transliterator.getFilterAsUnicodeSet(inputFilter);
        UnicodeSet affectedCharacters = new UnicodeSet(this.sourceCache).retainAll(myFilter);
        sourceSet.addAll(affectedCharacters);
        Iterator<String> it = affectedCharacters.iterator();
        while (it.hasNext()) {
            targetSet.addAll((CharSequence) this.transform.transform(it.next()));
        }
        for (String s : this.sourceStrings) {
            if (myFilter.containsAll(s)) {
                String t = this.transform.transform(s);
                if (!s.equals(t)) {
                    targetSet.addAll((CharSequence) t);
                    sourceSet.addAll((CharSequence) s);
                }
            }
        }
    }
}
