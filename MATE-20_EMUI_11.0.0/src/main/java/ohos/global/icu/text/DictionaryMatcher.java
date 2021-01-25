package ohos.global.icu.text;

import java.text.CharacterIterator;

abstract class DictionaryMatcher {
    public abstract int getType();

    public abstract int matches(CharacterIterator characterIterator, int i, int[] iArr, int[] iArr2, int i2, int[] iArr3);

    DictionaryMatcher() {
    }

    public int matches(CharacterIterator characterIterator, int i, int[] iArr, int[] iArr2, int i2) {
        return matches(characterIterator, i, iArr, iArr2, i2, null);
    }
}
