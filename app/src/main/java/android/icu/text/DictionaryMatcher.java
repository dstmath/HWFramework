package android.icu.text;

import java.text.CharacterIterator;

abstract class DictionaryMatcher {
    public abstract int getType();

    public abstract int matches(CharacterIterator characterIterator, int i, int[] iArr, int[] iArr2, int i2, int[] iArr3);

    DictionaryMatcher() {
    }

    public int matches(CharacterIterator text, int maxLength, int[] lengths, int[] count, int limit) {
        return matches(text, maxLength, lengths, count, limit, null);
    }
}
