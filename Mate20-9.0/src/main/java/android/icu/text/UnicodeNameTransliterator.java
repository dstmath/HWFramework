package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.text.Transliterator;

class UnicodeNameTransliterator extends Transliterator {
    static final char CLOSE_DELIM = '}';
    static final String OPEN_DELIM = "\\N{";
    static final int OPEN_DELIM_LEN = 3;
    static final String _ID = "Any-Name";

    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnicodeNameTransliterator(null);
            }
        });
    }

    public UnicodeNameTransliterator(UnicodeFilter filter) {
        super(_ID, filter);
    }

    /* access modifiers changed from: protected */
    public void handleTransliterate(Replaceable text, Transliterator.Position offsets, boolean isIncremental) {
        int cursor = offsets.start;
        int limit = offsets.limit;
        StringBuilder str = new StringBuilder();
        str.append(OPEN_DELIM);
        while (cursor < limit) {
            int c = text.char32At(cursor);
            String extendedName = UCharacter.getExtendedName(c);
            String name = extendedName;
            if (extendedName != null) {
                str.setLength(3);
                str.append(name);
                str.append(CLOSE_DELIM);
                int clen = UTF16.getCharCount(c);
                text.replace(cursor, cursor + clen, str.toString());
                int len = str.length();
                cursor += len;
                limit += len - clen;
            } else {
                cursor++;
            }
        }
        offsets.contextLimit += limit - offsets.limit;
        offsets.limit = limit;
        offsets.start = cursor;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        if (myFilter.size() > 0) {
            sourceSet.addAll(myFilter);
            targetSet.addAll(48, 57).addAll(65, 90).add(45).add(32).addAll((CharSequence) OPEN_DELIM).add(125).addAll(97, 122).add(60).add(62).add(40).add(41);
        }
    }
}
