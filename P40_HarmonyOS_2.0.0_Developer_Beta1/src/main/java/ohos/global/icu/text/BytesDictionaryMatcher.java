package ohos.global.icu.text;

import java.text.CharacterIterator;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.util.BytesTrie;

/* access modifiers changed from: package-private */
public class BytesDictionaryMatcher extends DictionaryMatcher {
    private final byte[] characters;
    private final int transform;

    @Override // ohos.global.icu.text.DictionaryMatcher
    public int getType() {
        return 0;
    }

    public BytesDictionaryMatcher(byte[] bArr, int i) {
        this.characters = bArr;
        Assert.assrt((2130706432 & i) == 16777216);
        this.transform = i;
    }

    private int transform(int i) {
        if (i == 8205) {
            return 255;
        }
        if (i == 8204) {
            return SCSU.KATAKANAINDEX;
        }
        int i2 = i - (this.transform & DictionaryData.TRANSFORM_OFFSET_MASK);
        if (i2 < 0 || 253 < i2) {
            return -1;
        }
        return i2;
    }

    @Override // ohos.global.icu.text.DictionaryMatcher
    public int matches(CharacterIterator characterIterator, int i, int[] iArr, int[] iArr2, int i2, int[] iArr3) {
        int nextCodePoint;
        UCharacterIterator instance = UCharacterIterator.getInstance(characterIterator);
        BytesTrie bytesTrie = new BytesTrie(this.characters, 0);
        int nextCodePoint2 = instance.nextCodePoint();
        if (nextCodePoint2 == -1) {
            return 0;
        }
        BytesTrie.Result first = bytesTrie.first(transform(nextCodePoint2));
        int i3 = 1;
        int i4 = 0;
        while (true) {
            if (!first.hasValue()) {
                if (first == BytesTrie.Result.NO_MATCH) {
                    break;
                }
            } else {
                if (i4 < i2) {
                    if (iArr3 != null) {
                        iArr3[i4] = bytesTrie.getValue();
                    }
                    iArr[i4] = i3;
                    i4++;
                }
                if (first == BytesTrie.Result.FINAL_VALUE) {
                    break;
                }
            }
            if (i3 >= i || (nextCodePoint = instance.nextCodePoint()) == -1) {
                break;
            }
            i3++;
            first = bytesTrie.next(transform(nextCodePoint));
        }
        iArr2[0] = i4;
        return i3;
    }
}
