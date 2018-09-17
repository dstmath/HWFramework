package android.icu.text;

import android.icu.text.Transliterator.Position;

class NullTransliterator extends Transliterator {
    static final String SHORT_ID = "Null";
    static final String _ID = "Any-Null";

    public NullTransliterator() {
        super(_ID, null);
    }

    protected void handleTransliterate(Replaceable text, Position offsets, boolean incremental) {
        offsets.start = offsets.limit;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
    }
}
