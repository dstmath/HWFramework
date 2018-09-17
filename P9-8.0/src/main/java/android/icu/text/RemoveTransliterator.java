package android.icu.text;

import android.icu.text.Transliterator.Factory;
import android.icu.text.Transliterator.Position;

class RemoveTransliterator extends Transliterator {
    private static final String _ID = "Any-Remove";

    static void register() {
        Transliterator.registerFactory(_ID, new Factory() {
            public Transliterator getInstance(String ID) {
                return new RemoveTransliterator();
            }
        });
        Transliterator.registerSpecialInverse("Remove", "Null", false);
    }

    public RemoveTransliterator() {
        super(_ID, null);
    }

    protected void handleTransliterate(Replaceable text, Position index, boolean incremental) {
        text.replace(index.start, index.limit, "");
        int len = index.limit - index.start;
        index.contextLimit -= len;
        index.limit -= len;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        sourceSet.addAll(getFilterAsUnicodeSet(inputFilter));
    }
}
