package android.icu.text;

import android.icu.lang.CharSequences;
import java.util.HashSet;
import java.util.Set;

class SourceTargetUtility {
    static Normalizer2 NFC = Normalizer2.getNFCInstance();
    static final UnicodeSet NON_STARTERS = new UnicodeSet("[:^ccc=0:]").freeze();
    final UnicodeSet sourceCache;
    final Set<String> sourceStrings;
    final Transform<String, String> transform;

    public SourceTargetUtility(Transform<String, String> transform) {
        this(transform, null);
    }

    public SourceTargetUtility(Transform<String, String> transform, Normalizer2 normalizer) {
        this.transform = transform;
        if (normalizer != null) {
            this.sourceCache = new UnicodeSet("[:^ccc=0:]");
        } else {
            this.sourceCache = new UnicodeSet();
        }
        this.sourceStrings = new HashSet();
        int i = 0;
        while (i <= 1114111) {
            boolean added = false;
            if (!CharSequences.equals(i, (String) transform.transform(UTF16.valueOf(i)))) {
                this.sourceCache.add(i);
                added = true;
            }
            if (normalizer != null) {
                String d = NFC.getDecomposition(i);
                if (d != null) {
                    if (!d.equals((String) transform.transform(d))) {
                        this.sourceStrings.add(d);
                    }
                    if (!(added || normalizer.isInert(i))) {
                        this.sourceCache.add(i);
                    }
                }
            }
            i++;
        }
        this.sourceCache.freeze();
    }

    public void addSourceTargetSet(Transliterator transliterator, UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = transliterator.getFilterAsUnicodeSet(inputFilter);
        UnicodeSet<String> affectedCharacters = new UnicodeSet(this.sourceCache).retainAll(myFilter);
        sourceSet.addAll((UnicodeSet) affectedCharacters);
        for (String s : affectedCharacters) {
            targetSet.addAll((CharSequence) this.transform.transform(s));
        }
        for (CharSequence s2 : this.sourceStrings) {
            if (myFilter.containsAll((String) s2)) {
                CharSequence t = (String) this.transform.transform(s2);
                if (!s2.equals(t)) {
                    targetSet.addAll(t);
                    sourceSet.addAll(s2);
                }
            }
        }
    }
}
