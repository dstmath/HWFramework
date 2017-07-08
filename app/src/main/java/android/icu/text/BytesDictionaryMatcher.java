package android.icu.text;

import android.icu.impl.Assert;
import android.icu.util.BytesTrie;
import android.icu.util.BytesTrie.Result;
import dalvik.bytecode.Opcodes;
import java.text.CharacterIterator;

class BytesDictionaryMatcher extends DictionaryMatcher {
    private final byte[] characters;
    private final int transform;

    public BytesDictionaryMatcher(byte[] chars, int transform) {
        this.characters = chars;
        Assert.assrt((DictionaryData.TRANSFORM_TYPE_MASK & transform) == DictionaryData.TRANSFORM_TYPE_OFFSET);
        this.transform = transform;
    }

    private int transform(int c) {
        if (c == 8205) {
            return Opcodes.OP_CONST_CLASS_JUMBO;
        }
        if (c == 8204) {
            return SCSU.KATAKANAINDEX;
        }
        int delta = c - (this.transform & DictionaryData.TRANSFORM_OFFSET_MASK);
        if (delta < 0 || SCSU.HIRAGANAINDEX < delta) {
            return -1;
        }
        return delta;
    }

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
                if (c != -1) {
                    break;
                }
                numChars++;
                result = bt.next(transform(c));
            } else {
                if (result == Result.NO_MATCH) {
                    break;
                }
                if (numChars < maxLength) {
                    break;
                }
                c = text.nextCodePoint();
                if (c != -1) {
                    break;
                }
                numChars++;
                result = bt.next(transform(c));
            }
        }
        count_[0] = count;
        return numChars;
    }

    public int getType() {
        return 0;
    }
}
