package ohos.global.icu.text;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.impl.CharacterIteration;
import ohos.global.icu.text.DictionaryBreakEngine;
import ohos.msdp.devicevirtualization.EventType;

/* access modifiers changed from: package-private */
public class CjkBreakEngine extends DictionaryBreakEngine {
    private static final UnicodeSet fHanWordSet = new UnicodeSet();
    private static final UnicodeSet fHangulWordSet = new UnicodeSet();
    private static final UnicodeSet fHiraganaWordSet = new UnicodeSet();
    private static final UnicodeSet fKatakanaWordSet = new UnicodeSet();
    private static final int kMaxKatakanaGroupLength = 20;
    private static final int kMaxKatakanaLength = 8;
    private static final int kint32max = Integer.MAX_VALUE;
    private static final int maxSnlp = 255;
    private DictionaryMatcher fDictionary;

    private static boolean isKatakana(int i) {
        return (i >= 12449 && i <= 12542 && i != 12539) || (i >= 65382 && i <= 65439);
    }

    static {
        fHangulWordSet.applyPattern("[\\uac00-\\ud7a3]");
        fHanWordSet.applyPattern("[:Han:]");
        fKatakanaWordSet.applyPattern("[[:Katakana:]\\uff9e\\uff9f]");
        fHiraganaWordSet.applyPattern("[:Hiragana:]");
        fHangulWordSet.freeze();
        fHanWordSet.freeze();
        fKatakanaWordSet.freeze();
        fHiraganaWordSet.freeze();
    }

    public CjkBreakEngine(boolean z) throws IOException {
        this.fDictionary = null;
        this.fDictionary = DictionaryData.loadDictionaryFor("Hira");
        if (z) {
            setCharacters(fHangulWordSet);
            return;
        }
        UnicodeSet unicodeSet = new UnicodeSet();
        unicodeSet.addAll(fHanWordSet);
        unicodeSet.addAll(fKatakanaWordSet);
        unicodeSet.addAll(fHiraganaWordSet);
        unicodeSet.add(65392);
        unicodeSet.add(12540);
        setCharacters(unicodeSet);
    }

    public boolean equals(Object obj) {
        if (obj instanceof CjkBreakEngine) {
            return this.fSet.equals(((CjkBreakEngine) obj).fSet);
        }
        return false;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    private static int getKatakanaCost(int i) {
        int[] iArr = {8192, 984, 408, SCSU.UQUOTEU, EventType.EVENT_DEVICE_CAPABILITY_ENABLE, SCSU.ARMENIANINDEX, 300, 372, 480};
        if (i > 8) {
            return 8192;
        }
        return iArr[i];
    }

    @Override // ohos.global.icu.text.DictionaryBreakEngine
    public int divideUpDictionaryRange(CharacterIterator characterIterator, int i, int i2, DictionaryBreakEngine.DequeI dequeI) {
        int i3;
        StringCharacterIterator stringCharacterIterator;
        int i4;
        int i5;
        int i6;
        int i7;
        int[] iArr;
        int[] iArr2;
        int[] iArr3;
        int i8;
        int i9;
        int i10;
        int katakanaCost;
        if (i >= i2) {
            return 0;
        }
        characterIterator.setIndex(i);
        int[] iArr4 = new int[((i2 - i) + 1)];
        StringBuffer stringBuffer = new StringBuffer("");
        characterIterator.setIndex(i);
        while (characterIterator.getIndex() < i2) {
            stringBuffer.append(characterIterator.current());
            characterIterator.next();
        }
        String stringBuffer2 = stringBuffer.toString();
        if (Normalizer.quickCheck(stringBuffer2, Normalizer.NFKC) == Normalizer.YES || Normalizer.isNormalized(stringBuffer2, Normalizer.NFKC, 0)) {
            stringCharacterIterator = new StringCharacterIterator(stringBuffer2);
            iArr4[0] = 0;
            int i11 = 0;
            i3 = 0;
            while (i11 < stringBuffer2.length()) {
                i11 += Character.charCount(stringBuffer2.codePointAt(i11));
                i3++;
                iArr4[i3] = i11;
            }
        } else {
            String normalize = Normalizer.normalize(stringBuffer2, Normalizer.NFKC);
            stringCharacterIterator = new StringCharacterIterator(normalize);
            iArr4 = new int[(normalize.length() + 1)];
            Normalizer normalizer = new Normalizer(stringBuffer2, Normalizer.NFKC, 0);
            iArr4[0] = 0;
            int i12 = 0;
            i3 = 0;
            while (i12 < normalizer.endIndex()) {
                normalizer.next();
                i3++;
                i12 = normalizer.getIndex();
                iArr4[i3] = i12;
            }
        }
        int i13 = i3 + 1;
        int[] iArr5 = new int[i13];
        iArr5[0] = 0;
        int i14 = 1;
        while (true) {
            i4 = Integer.MAX_VALUE;
            if (i14 > i3) {
                break;
            }
            iArr5[i14] = Integer.MAX_VALUE;
            i14++;
        }
        int[] iArr6 = new int[i13];
        for (int i15 = 0; i15 <= i3; i15++) {
            iArr6[i15] = -1;
        }
        int[] iArr7 = new int[i3];
        int[] iArr8 = new int[i3];
        stringCharacterIterator.setIndex(0);
        int i16 = 0;
        boolean z = false;
        while (i16 < i3) {
            int index = stringCharacterIterator.getIndex();
            if (iArr5[i16] == i4) {
                i10 = index;
                i8 = i16;
                iArr3 = iArr8;
                iArr2 = iArr7;
                iArr = iArr6;
                i9 = i4;
            } else {
                int i17 = i16 + 20 < i3 ? 20 : i3 - i16;
                int[] iArr9 = new int[1];
                i10 = index;
                i8 = i16;
                iArr3 = iArr8;
                iArr2 = iArr7;
                iArr = iArr6;
                i9 = Integer.MAX_VALUE;
                this.fDictionary.matches(stringCharacterIterator, i17, iArr3, iArr9, i17, iArr2);
                int i18 = iArr9[0];
                stringCharacterIterator.setIndex(i10);
                if ((i18 == 0 || iArr3[0] != 1) && CharacterIteration.current32(stringCharacterIterator) != Integer.MAX_VALUE && !fHangulWordSet.contains(CharacterIteration.current32(stringCharacterIterator))) {
                    iArr2[i18] = 255;
                    iArr3[i18] = 1;
                    i18++;
                }
                for (int i19 = 0; i19 < i18; i19++) {
                    int i20 = iArr5[i8] + iArr2[i19];
                    if (i20 < iArr5[iArr3[i19] + i8]) {
                        iArr5[iArr3[i19] + i8] = i20;
                        iArr[iArr3[i19] + i8] = i8;
                    }
                }
                boolean isKatakana = isKatakana(CharacterIteration.current32(stringCharacterIterator));
                if (!z && isKatakana) {
                    int i21 = i8 + 1;
                    CharacterIteration.next32(stringCharacterIterator);
                    while (i21 < i3 && i21 - i8 < 20 && isKatakana(CharacterIteration.current32(stringCharacterIterator))) {
                        CharacterIteration.next32(stringCharacterIterator);
                        i21++;
                    }
                    int i22 = i21 - i8;
                    if (i22 < 20 && (katakanaCost = iArr5[i8] + getKatakanaCost(i22)) < iArr5[i21]) {
                        iArr5[i21] = katakanaCost;
                        iArr[i21] = i8;
                    }
                }
                z = isKatakana;
            }
            i16 = i8 + 1;
            stringCharacterIterator.setIndex(i10);
            CharacterIteration.next32(stringCharacterIterator);
            i4 = i9;
            iArr8 = iArr3;
            iArr7 = iArr2;
            iArr6 = iArr;
        }
        int[] iArr10 = new int[i13];
        if (iArr5[i3] == i4) {
            iArr10[0] = i3;
            i5 = 1;
        } else {
            i5 = 0;
            while (i3 > 0) {
                iArr10[i5] = i3;
                i5++;
                i3 = iArr6[i3];
            }
            Assert.assrt(iArr6[iArr10[i5 + -1]] == 0);
        }
        if (dequeI.size() == 0 || dequeI.peek() < i) {
            i7 = i5 + 1;
            i6 = 0;
            iArr10[i5] = 0;
        } else {
            i7 = i5;
            i6 = 0;
        }
        for (int i23 = i7 - 1; i23 >= 0; i23--) {
            int i24 = iArr4[iArr10[i23]] + i;
            if (!dequeI.contains(i24) && i24 != i) {
                dequeI.push(iArr4[iArr10[i23]] + i);
                i6++;
            }
        }
        if (!dequeI.isEmpty() && dequeI.peek() == i2) {
            dequeI.pop();
            i6--;
        }
        if (!dequeI.isEmpty()) {
            characterIterator.setIndex(dequeI.peek());
        }
        return i6;
    }
}
