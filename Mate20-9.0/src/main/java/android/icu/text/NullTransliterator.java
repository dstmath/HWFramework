package android.icu.text;

import android.icu.text.Transliterator;

class NullTransliterator extends Transliterator {
    static final String SHORT_ID = "Null";
    static final String _ID = "Any-Null";

    public NullTransliterator() {
        super(_ID, null);
    }

    /* access modifiers changed from: protected */
    public void handleTransliterate(Replaceable text, Transliterator.Position offsets, boolean incremental) {
        offsets.start = offsets.limit;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
    }
}
