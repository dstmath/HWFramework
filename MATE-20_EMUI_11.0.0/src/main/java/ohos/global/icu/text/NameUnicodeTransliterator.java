package ohos.global.icu.text;

import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.UCharacterName;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Transliterator;

class NameUnicodeTransliterator extends Transliterator {
    static final char CLOSE_DELIM = '}';
    static final char OPEN_DELIM = '\\';
    static final String OPEN_PAT = "\\N~{~";
    static final char SPACE = ' ';
    static final String _ID = "Name-Any";

    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            /* class ohos.global.icu.text.NameUnicodeTransliterator.AnonymousClass1 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new NameUnicodeTransliterator(null);
            }
        });
    }

    public NameUnicodeTransliterator(UnicodeFilter unicodeFilter) {
        super(_ID, unicodeFilter);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004f, code lost:
        if (r4.length() > r2) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0093, code lost:
        if (r4.length() >= r2) goto L_0x0098;
     */
    @Override // ohos.global.icu.text.Transliterator
    public void handleTransliterate(Replaceable replaceable, Transliterator.Position position, boolean z) {
        int i;
        int maxCharNameLength = UCharacterName.INSTANCE.getMaxCharNameLength() + 1;
        StringBuffer stringBuffer = new StringBuffer(maxCharNameLength);
        UnicodeSet unicodeSet = new UnicodeSet();
        UCharacterName.INSTANCE.getCharNameCharacters(unicodeSet);
        int i2 = position.start;
        int i3 = position.limit;
        loop0:
        while (true) {
            i = -1;
            boolean z2 = false;
            while (i2 < i3) {
                int char32At = replaceable.char32At(i2);
                if (z2) {
                    if (z2) {
                        if (PatternProps.isWhiteSpace(char32At)) {
                            if (stringBuffer.length() > 0 && stringBuffer.charAt(stringBuffer.length() - 1) != ' ') {
                                stringBuffer.append(SPACE);
                            }
                        } else if (char32At == 125) {
                            int length = stringBuffer.length();
                            if (length > 0 && stringBuffer.charAt(length - 1) == ' ') {
                                stringBuffer.setLength(length - 1);
                            }
                            int charFromExtendedName = UCharacter.getCharFromExtendedName(stringBuffer.toString());
                            if (charFromExtendedName != -1) {
                                int i4 = i2 + 1;
                                String valueOf = UTF16.valueOf(charFromExtendedName);
                                replaceable.replace(i, i4, valueOf);
                                int length2 = (i4 - i) - valueOf.length();
                                i2 = i4 - length2;
                                i3 -= length2;
                            }
                        } else if (unicodeSet.contains(char32At)) {
                            UTF16.append(stringBuffer, char32At);
                        } else {
                            i2--;
                        }
                        z2 = false;
                    }
                } else if (char32At == 92) {
                    int parsePattern = Utility.parsePattern(OPEN_PAT, replaceable, i2, i3);
                    if (parsePattern < 0 || parsePattern >= i3) {
                        i = i2;
                    } else {
                        stringBuffer.setLength(0);
                        z2 = true;
                        i = i2;
                        i2 = parsePattern;
                    }
                }
                i2 += UTF16.getCharCount(char32At);
            }
            break loop0;
        }
        position.contextLimit += i3 - position.limit;
        position.limit = i3;
        if (z && i >= 0) {
            i2 = i;
        }
        position.start = i2;
    }

    @Override // ohos.global.icu.text.Transliterator
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        UnicodeSet filterAsUnicodeSet = getFilterAsUnicodeSet(unicodeSet);
        if (filterAsUnicodeSet.containsAll("\\N{") && filterAsUnicodeSet.contains(125)) {
            UnicodeSet add = new UnicodeSet().addAll(48, 57).addAll(65, 70).addAll(97, 122).add(60).add(62).add(40).add(41).add(45).add(32).addAll("\\N{").add(125);
            add.retainAll(filterAsUnicodeSet);
            if (add.size() > 0) {
                unicodeSet2.addAll(add);
                unicodeSet3.addAll(0, 1114111);
            }
        }
    }
}
