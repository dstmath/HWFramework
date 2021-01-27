package ohos.global.icu.text;

import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Transliterator;

class UnicodeNameTransliterator extends Transliterator {
    static final char CLOSE_DELIM = '}';
    static final String OPEN_DELIM = "\\N{";
    static final int OPEN_DELIM_LEN = 3;
    static final String _ID = "Any-Name";

    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnicodeNameTransliterator.AnonymousClass1 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnicodeNameTransliterator(null);
            }
        });
    }

    public UnicodeNameTransliterator(UnicodeFilter unicodeFilter) {
        super(_ID, unicodeFilter);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.Transliterator
    public void handleTransliterate(Replaceable replaceable, Transliterator.Position position, boolean z) {
        int i = position.start;
        int i2 = position.limit;
        StringBuilder sb = new StringBuilder();
        sb.append(OPEN_DELIM);
        while (i < i2) {
            int char32At = replaceable.char32At(i);
            String extendedName = UCharacter.getExtendedName(char32At);
            if (extendedName != null) {
                sb.setLength(3);
                sb.append(extendedName);
                sb.append(CLOSE_DELIM);
                int charCount = UTF16.getCharCount(char32At);
                replaceable.replace(i, i + charCount, sb.toString());
                int length = sb.length();
                i += length;
                i2 += length - charCount;
            } else {
                i++;
            }
        }
        position.contextLimit += i2 - position.limit;
        position.limit = i2;
        position.start = i;
    }

    @Override // ohos.global.icu.text.Transliterator
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        UnicodeSet filterAsUnicodeSet = getFilterAsUnicodeSet(unicodeSet);
        if (filterAsUnicodeSet.size() > 0) {
            unicodeSet2.addAll(filterAsUnicodeSet);
            unicodeSet3.addAll(48, 57).addAll(65, 90).add(45).add(32).addAll(OPEN_DELIM).add(125).addAll(97, 122).add(60).add(62).add(40).add(41);
        }
    }
}
