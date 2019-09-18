package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.CharacterIteration;
import android.icu.text.DictionaryBreakEngine;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

class CjkBreakEngine extends DictionaryBreakEngine {
    private static final UnicodeSet fHanWordSet = new UnicodeSet();
    private static final UnicodeSet fHangulWordSet = new UnicodeSet();
    private static final UnicodeSet fHiraganaWordSet = new UnicodeSet();
    private static final UnicodeSet fKatakanaWordSet = new UnicodeSet();
    private static final int kMaxKatakanaGroupLength = 20;
    private static final int kMaxKatakanaLength = 8;
    private static final int kint32max = Integer.MAX_VALUE;
    private static final int maxSnlp = 255;
    private DictionaryMatcher fDictionary;

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

    public CjkBreakEngine(boolean korean) throws IOException {
        super(1);
        this.fDictionary = null;
        this.fDictionary = DictionaryData.loadDictionaryFor("Hira");
        if (korean) {
            setCharacters(fHangulWordSet);
            return;
        }
        UnicodeSet cjSet = new UnicodeSet();
        cjSet.addAll(fHanWordSet);
        cjSet.addAll(fKatakanaWordSet);
        cjSet.addAll(fHiraganaWordSet);
        cjSet.add(65392);
        cjSet.add(12540);
        setCharacters(cjSet);
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

    private static int getKatakanaCost(int wordlength) {
        int[] katakanaCost = {8192, 984, 408, 240, 204, 252, 300, 372, 480};
        if (wordlength > 8) {
            return 8192;
        }
        return katakanaCost[wordlength];
    }

    private static boolean isKatakana(int value) {
        return (value >= 12449 && value <= 12542 && value != 12539) || (value >= 65382 && value <= 65439);
    }

    public int divideUpDictionaryRange(CharacterIterator inText, int startPos, int endPos, DictionaryBreakEngine.DequeI foundBreaks) {
        int[] charPositions;
        CharacterIterator text;
        int i;
        int numBreaks;
        StringBuffer s;
        int[] prev;
        int[] values;
        int[] bestSnlp;
        int inputLength;
        int[] lengths;
        int i2 = startPos;
        int i3 = endPos;
        DictionaryBreakEngine.DequeI dequeI = foundBreaks;
        if (i2 >= i3) {
            return 0;
        }
        inText.setIndex(startPos);
        int inputLength2 = i3 - i2;
        int[] charPositions2 = new int[(inputLength2 + 1)];
        StringBuffer s2 = new StringBuffer("");
        inText.setIndex(startPos);
        while (inText.getIndex() < i3) {
            s2.append(inText.current());
            inText.next();
        }
        String prenormstr = s2.toString();
        int numChars = 0;
        if (Normalizer.quickCheck(prenormstr, Normalizer.NFKC) == Normalizer.YES || Normalizer.isNormalized(prenormstr, Normalizer.NFKC, 0)) {
            CharacterIterator text2 = new StringCharacterIterator(prenormstr);
            int index = 0;
            charPositions2[0] = 0;
            while (index < prenormstr.length()) {
                index += Character.charCount(prenormstr.codePointAt(index));
                numChars++;
                charPositions2[numChars] = index;
            }
            charPositions = charPositions2;
            text = text2;
        } else {
            String normStr = Normalizer.normalize(prenormstr, Normalizer.NFKC);
            CharacterIterator text3 = new StringCharacterIterator(normStr);
            int[] charPositions3 = new int[(normStr.length() + 1)];
            Normalizer normalizer = new Normalizer(prenormstr, Normalizer.NFKC, 0);
            int index2 = 0;
            charPositions3[0] = 0;
            while (index2 < normalizer.endIndex()) {
                normalizer.next();
                numChars++;
                index2 = normalizer.getIndex();
                charPositions3[numChars] = index2;
            }
            charPositions = charPositions3;
            text = text3;
        }
        int[] bestSnlp2 = new int[(numChars + 1)];
        bestSnlp2[0] = 0;
        int i4 = 1;
        while (true) {
            i = Integer.MAX_VALUE;
            if (i4 > numChars) {
                break;
            }
            bestSnlp2[i4] = Integer.MAX_VALUE;
            i4++;
        }
        int[] prev2 = new int[(numChars + 1)];
        for (int i5 = 0; i5 <= numChars; i5++) {
            prev2[i5] = -1;
        }
        int[] values2 = new int[numChars];
        int[] lengths2 = new int[numChars];
        boolean is_prev_katakana = false;
        int i6 = 0;
        while (true) {
            int i7 = i6;
            if (i7 >= numChars) {
                break;
            }
            text.setIndex(i7);
            if (bestSnlp2[i7] == i) {
                inputLength = inputLength2;
                s = s2;
                values = values2;
                prev = prev2;
                lengths = lengths2;
                bestSnlp = bestSnlp2;
            } else {
                inputLength = inputLength2;
                lengths = lengths2;
                int maxSearchLength = i7 + 20 < numChars ? 20 : numChars - i7;
                int[] count_ = new int[1];
                values = values2;
                prev = prev2;
                s = s2;
                bestSnlp = bestSnlp2;
                this.fDictionary.matches(text, maxSearchLength, lengths, count_, maxSearchLength, values);
                int count = count_[0];
                text.setIndex(i7);
                if ((count == 0 || lengths[0] != 1) && CharacterIteration.current32(text) != Integer.MAX_VALUE && !fHangulWordSet.contains(CharacterIteration.current32(text))) {
                    values[count] = 255;
                    lengths[count] = 1;
                    count++;
                }
                for (int j = 0; j < count; j++) {
                    int newSnlp = bestSnlp[i7] + values[j];
                    if (newSnlp < bestSnlp[lengths[j] + i7]) {
                        bestSnlp[lengths[j] + i7] = newSnlp;
                        prev[lengths[j] + i7] = i7;
                    }
                }
                boolean is_katakana = isKatakana(CharacterIteration.current32(text));
                if (!is_prev_katakana && is_katakana) {
                    int j2 = i7 + 1;
                    CharacterIteration.next32(text);
                    while (j2 < numChars && j2 - i7 < 20 && isKatakana(CharacterIteration.current32(text))) {
                        CharacterIteration.next32(text);
                        j2++;
                    }
                    if (j2 - i7 < 20) {
                        int newSnlp2 = bestSnlp[i7] + getKatakanaCost(j2 - i7);
                        if (newSnlp2 < bestSnlp[j2]) {
                            bestSnlp[j2] = newSnlp2;
                            prev[j2] = i7;
                        }
                    }
                }
                is_prev_katakana = is_katakana;
            }
            i6 = i7 + 1;
            lengths2 = lengths;
            inputLength2 = inputLength;
            bestSnlp2 = bestSnlp;
            values2 = values;
            prev2 = prev;
            s2 = s;
            i = Integer.MAX_VALUE;
        }
        int i8 = inputLength2;
        StringBuffer stringBuffer = s2;
        int[] iArr = values2;
        int[] prev3 = prev2;
        int[] iArr2 = lengths2;
        int[] t_boundary = new int[(numChars + 1)];
        if (bestSnlp2[numChars] == Integer.MAX_VALUE) {
            t_boundary[0] = numChars;
            numBreaks = 0 + 1;
        } else {
            boolean z = true;
            numBreaks = 0;
            for (int i9 = numChars; i9 > 0; i9 = prev3[i9]) {
                t_boundary[numBreaks] = i9;
                numBreaks++;
            }
            if (prev3[t_boundary[numBreaks - 1]] != 0) {
                z = false;
            }
            Assert.assrt(z);
        }
        if (foundBreaks.size() == 0 || foundBreaks.peek() < i2) {
            t_boundary[numBreaks] = 0;
            numBreaks++;
        }
        int correctedNumBreaks = 0;
        for (int i10 = numBreaks - 1; i10 >= 0; i10--) {
            int pos = charPositions[t_boundary[i10]] + i2;
            if (!dequeI.contains(pos) && pos != i2) {
                dequeI.push(charPositions[t_boundary[i10]] + i2);
                correctedNumBreaks++;
            }
        }
        if (foundBreaks.isEmpty() == 0 && foundBreaks.peek() == i3) {
            foundBreaks.pop();
            correctedNumBreaks--;
        }
        if (!foundBreaks.isEmpty()) {
            inText.setIndex(foundBreaks.peek());
        } else {
            CharacterIterator characterIterator = inText;
        }
        return correctedNumBreaks;
    }
}
