package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.UCharacterName;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.Transliterator;

class NameUnicodeTransliterator extends Transliterator {
    static final char CLOSE_DELIM = '}';
    static final char OPEN_DELIM = '\\';
    static final String OPEN_PAT = "\\N~{~";
    static final char SPACE = ' ';
    static final String _ID = "Name-Any";

    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new NameUnicodeTransliterator(null);
            }
        });
    }

    public NameUnicodeTransliterator(UnicodeFilter filter) {
        super(_ID, filter);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    public void handleTransliterate(Replaceable text, Transliterator.Position offsets, boolean isIncremental) {
        Replaceable replaceable = text;
        Transliterator.Position position = offsets;
        int maxLen = UCharacterName.INSTANCE.getMaxCharNameLength() + 1;
        StringBuffer name = new StringBuffer(maxLen);
        UnicodeSet legal = new UnicodeSet();
        UCharacterName.INSTANCE.getCharNameCharacters(legal);
        int cursor = position.start;
        int mode = 0;
        int limit = position.limit;
        int cursor2 = cursor;
        int openPos = -1;
        while (cursor2 < limit) {
            int c = replaceable.char32At(cursor2);
            switch (mode) {
                case 0:
                    if (c == 92) {
                        openPos = cursor2;
                        int i = Utility.parsePattern(OPEN_PAT, replaceable, cursor2, limit);
                        if (i >= 0 && i < limit) {
                            mode = 1;
                            name.setLength(0);
                            cursor2 = i;
                            break;
                        }
                    }
                case 1:
                    if (PatternProps.isWhiteSpace(c)) {
                        if (name.length() > 0 && name.charAt(name.length() - 1) != ' ') {
                            name.append(SPACE);
                            if (name.length() > maxLen) {
                                mode = 0;
                            }
                        }
                    } else if (c == 125) {
                        if (name.length() > 0 && name.charAt(len - 1) == ' ') {
                            name.setLength(len - 1);
                        }
                        int c2 = UCharacter.getCharFromExtendedName(name.toString());
                        if (c2 != -1) {
                            int cursor3 = cursor2 + 1;
                            String str = UTF16.valueOf(c2);
                            replaceable.replace(openPos, cursor3, str);
                            int delta = (cursor3 - openPos) - str.length();
                            cursor2 = cursor3 - delta;
                            limit -= delta;
                        }
                        mode = 0;
                        openPos = -1;
                        break;
                    } else if (legal.contains(c) != 0) {
                        UTF16.append(name, c);
                        if (name.length() >= maxLen) {
                            mode = 0;
                        }
                    } else {
                        cursor2--;
                        mode = 0;
                    }
                default:
                    cursor2 += UTF16.getCharCount(c);
                    break;
            }
        }
        position.contextLimit += limit - position.limit;
        position.limit = limit;
        position.start = (!isIncremental || openPos < 0) ? cursor2 : openPos;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        if (myFilter.containsAll("\\N{") && myFilter.contains(125)) {
            UnicodeSet items = new UnicodeSet().addAll(48, 57).addAll(65, 70).addAll(97, 122).add(60).add(62).add(40).add(41).add(45).add(32).addAll((CharSequence) "\\N{").add(125);
            items.retainAll(myFilter);
            if (items.size() > 0) {
                sourceSet.addAll(items);
                targetSet.addAll(0, 1114111);
            }
        }
    }
}
