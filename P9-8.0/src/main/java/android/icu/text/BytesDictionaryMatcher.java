package android.icu.text;

import android.icu.impl.Assert;
import android.icu.util.BytesTrie;
import android.icu.util.BytesTrie.Result;
import java.text.CharacterIterator;

class BytesDictionaryMatcher extends DictionaryMatcher {
    private final byte[] characters;
    private final int transform;

    public BytesDictionaryMatcher(byte[] chars, int transform) {
        this.characters = chars;
        Assert.assrt((DictionaryData.TRANSFORM_TYPE_MASK & transform) == 16777216);
        this.transform = transform;
    }

    private int transform(int c) {
        if (c == 8205) {
            return 255;
        }
        if (c == 8204) {
            return 254;
        }
        int delta = c - (this.transform & DictionaryData.TRANSFORM_OFFSET_MASK);
        if (delta < 0 || 253 < delta) {
            return -1;
        }
        return delta;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0036 A:{SYNTHETIC, EDGE_INSN: B:23:0x0036->B:12:0x0036 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int matches(CharacterIterator text_, int maxLength, int[] lengths, int[] count_, int limit, int[] values) {
        UCharacterIterator text = UCharacterIterator.getInstance(text_);
        BytesTrie bt = new BytesTrie(this.characters, 0);
        int c = text.nextCodePoint();
        if (c == -1) {
            return 0;
        }
        Result result = bt.first(transform(c));
        int numChars = 1;
        int count = 0;
        while (true) {
            if (result.hasValue()) {
                if (count < limit) {
                    if (values != null) {
                        values[count] = bt.getValue();
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
                result = bt.next(transform(c));
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
        return 0;
    }
}
