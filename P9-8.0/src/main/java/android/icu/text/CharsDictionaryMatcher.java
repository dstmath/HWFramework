package android.icu.text;

import android.icu.util.BytesTrie.Result;
import android.icu.util.CharsTrie;
import java.text.CharacterIterator;

class CharsDictionaryMatcher extends DictionaryMatcher {
    private CharSequence characters;

    public CharsDictionaryMatcher(CharSequence chars) {
        this.characters = chars;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0032 A:{SYNTHETIC, EDGE_INSN: B:22:0x0032->B:12:0x0032 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int matches(CharacterIterator text_, int maxLength, int[] lengths, int[] count_, int limit, int[] values) {
        UCharacterIterator text = UCharacterIterator.getInstance(text_);
        CharsTrie uct = new CharsTrie(this.characters, 0);
        int c = text.nextCodePoint();
        if (c == -1) {
            return 0;
        }
        Result result = uct.firstForCodePoint(c);
        int numChars = 1;
        int count = 0;
        while (true) {
            if (result.hasValue()) {
                if (count < limit) {
                    if (values != null) {
                        values[count] = uct.getValue();
                    }
                    lengths[count] = numChars;
                    count++;
                }
                if (result == Result.FINAL_VALUE) {
                    break;
                }
                if (numChars < maxLength) {
                    break;
                }
                c = text.nextCodePoint();
                if (c == -1) {
                    break;
                }
                numChars++;
                result = uct.nextForCodePoint(c);
            } else {
                if (result == Result.NO_MATCH) {
                    break;
                }
                if (numChars < maxLength) {
                }
            }
        }
        count_[0] = count;
        return numChars;
    }

    public int getType() {
        return 1;
    }
}
