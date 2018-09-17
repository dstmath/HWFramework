package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.UCharacterName;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.Transliterator.Factory;
import android.icu.text.Transliterator.Position;

class NameUnicodeTransliterator extends Transliterator {
    static final char CLOSE_DELIM = '}';
    static final char OPEN_DELIM = '\\';
    static final String OPEN_PAT = "\\N~{~";
    static final char SPACE = ' ';
    static final String _ID = "Name-Any";

    static void register() {
        Transliterator.registerFactory(_ID, new Factory() {
            public Transliterator getInstance(String ID) {
                return new NameUnicodeTransliterator(null);
            }
        });
    }

    public NameUnicodeTransliterator(UnicodeFilter filter) {
        super(_ID, filter);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void handleTransliterate(Replaceable text, Position offsets, boolean isIncremental) {
        int maxLen = UCharacterName.INSTANCE.getMaxCharNameLength() + 1;
        StringBuffer name = new StringBuffer(maxLen);
        UnicodeSet legal = new UnicodeSet();
        UCharacterName.INSTANCE.getCharNameCharacters(legal);
        int cursor = offsets.start;
        int limit = offsets.limit;
        int mode = 0;
        int openPos = -1;
        while (cursor < limit) {
            int c = text.char32At(cursor);
            switch (mode) {
                case 0:
                    if (c == 92) {
                        openPos = cursor;
                        int i = Utility.parsePattern(OPEN_PAT, text, cursor, limit);
                        if (i >= 0 && i < limit) {
                            mode = 1;
                            name.setLength(0);
                            cursor = i;
                            break;
                        }
                    }
                case 1:
                    if (PatternProps.isWhiteSpace(c)) {
                        if (name.length() > 0 && name.charAt(name.length() - 1) != SPACE) {
                            name.append(SPACE);
                            if (name.length() > maxLen) {
                                mode = 0;
                            }
                        }
                    } else if (c == 125) {
                        int len = name.length();
                        if (len > 0 && name.charAt(len - 1) == SPACE) {
                            name.setLength(len - 1);
                        }
                        c = UCharacter.getCharFromExtendedName(name.toString());
                        if (c != -1) {
                            cursor++;
                            String str = UTF16.valueOf(c);
                            text.replace(openPos, cursor, str);
                            int delta = (cursor - openPos) - str.length();
                            cursor -= delta;
                            limit -= delta;
                        }
                        mode = 0;
                        openPos = -1;
                        break;
                    } else if (legal.contains(c)) {
                        UTF16.append(name, c);
                        if (name.length() >= maxLen) {
                            mode = 0;
                        }
                    } else {
                        cursor--;
                        mode = 0;
                    }
                default:
                    cursor += UTF16.getCharCount(c);
                    break;
            }
        }
        offsets.contextLimit += limit - offsets.limit;
        offsets.limit = limit;
        if (!isIncremental || openPos < 0) {
            openPos = cursor;
        }
        offsets.start = openPos;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        if (myFilter.containsAll("\\N{") && (myFilter.contains(125) ^ 1) == 0) {
            UnicodeSet items = new UnicodeSet().addAll(48, 57).addAll(65, 70).addAll(97, 122).add(60).add(62).add(40).add(41).add(45).add(32).addAll((CharSequence) "\\N{").add(125);
            items.retainAll(myFilter);
            if (items.size() > 0) {
                sourceSet.addAll(items);
                targetSet.addAll(0, 1114111);
            }
        }
    }
}
