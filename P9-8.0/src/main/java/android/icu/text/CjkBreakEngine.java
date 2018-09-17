package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.CharacterIteration;
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
        super(Integer.valueOf(1));
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
        if (!(obj instanceof CjkBreakEngine)) {
            return false;
        }
        return this.fSet.equals(((CjkBreakEngine) obj).fSet);
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    private static int getKatakanaCost(int wordlength) {
        return wordlength > 8 ? 8192 : new int[]{8192, 984, 408, 240, 204, 252, 300, 372, 480}[wordlength];
    }

    private static boolean isKatakana(int value) {
        if (value >= 12449 && value <= 12542 && value != 12539) {
            return true;
        }
        if (value < 65382 || value > 65439) {
            return false;
        }
        return true;
    }

    public int divideUpDictionaryRange(CharacterIterator inText, int startPos, int endPos, DequeI foundBreaks) {
        if (startPos >= endPos) {
            return 0;
        }
        boolean isNormalized;
        CharacterIterator text;
        int i;
        inText.setIndex(startPos);
        int[] charPositions = new int[((endPos - startPos) + 1)];
        StringBuffer stringBuffer = new StringBuffer("");
        inText.setIndex(startPos);
        while (inText.getIndex() < endPos) {
            stringBuffer.append(inText.current());
            inText.next();
        }
        String prenormstr = stringBuffer.toString();
        if (Normalizer.quickCheck(prenormstr, Normalizer.NFKC) != Normalizer.YES) {
            isNormalized = Normalizer.isNormalized(prenormstr, Normalizer.NFKC, 0);
        } else {
            isNormalized = true;
        }
        int numChars = 0;
        int index;
        if (isNormalized) {
            text = new StringCharacterIterator(prenormstr);
            index = 0;
            charPositions[0] = 0;
            while (index < prenormstr.length()) {
                index += Character.charCount(prenormstr.codePointAt(index));
                numChars++;
                charPositions[numChars] = index;
            }
        } else {
            String normStr = Normalizer.normalize(prenormstr, Normalizer.NFKC);
            text = new StringCharacterIterator(normStr);
            charPositions = new int[(normStr.length() + 1)];
            Normalizer normalizer = new Normalizer(prenormstr, Normalizer.NFKC, 0);
            index = 0;
            charPositions[0] = 0;
            while (index < normalizer.endIndex()) {
                normalizer.next();
                numChars++;
                index = normalizer.getIndex();
                charPositions[numChars] = index;
            }
        }
        int[] bestSnlp = new int[(numChars + 1)];
        bestSnlp[0] = 0;
        for (i = 1; i <= numChars; i++) {
            bestSnlp[i] = Integer.MAX_VALUE;
        }
        int[] prev = new int[(numChars + 1)];
        for (i = 0; i <= numChars; i++) {
            prev[i] = -1;
        }
        int[] values = new int[numChars];
        int[] lengths = new int[numChars];
        boolean is_prev_katakana = false;
        i = 0;
        while (i < numChars) {
            text.setIndex(i);
            if (bestSnlp[i] != Integer.MAX_VALUE) {
                int j;
                int newSnlp;
                int maxSearchLength = i + 20 < numChars ? 20 : numChars - i;
                int[] count_ = new int[1];
                this.fDictionary.matches(text, maxSearchLength, lengths, count_, maxSearchLength, values);
                int count = count_[0];
                text.setIndex(i);
                if (!((count != 0 && lengths[0] == 1) || CharacterIteration.current32(text) == Integer.MAX_VALUE || (fHangulWordSet.contains(CharacterIteration.current32(text)) ^ 1) == 0)) {
                    values[count] = 255;
                    lengths[count] = 1;
                    count++;
                }
                for (j = 0; j < count; j++) {
                    newSnlp = bestSnlp[i] + values[j];
                    if (newSnlp < bestSnlp[lengths[j] + i]) {
                        bestSnlp[lengths[j] + i] = newSnlp;
                        prev[lengths[j] + i] = i;
                    }
                }
                boolean is_katakana = isKatakana(CharacterIteration.current32(text));
                if (!is_prev_katakana && is_katakana) {
                    j = i + 1;
                    CharacterIteration.next32(text);
                    while (j < numChars && j - i < 20 && isKatakana(CharacterIteration.current32(text))) {
                        CharacterIteration.next32(text);
                        j++;
                    }
                    if (j - i < 20) {
                        newSnlp = bestSnlp[i] + getKatakanaCost(j - i);
                        if (newSnlp < bestSnlp[j]) {
                            bestSnlp[j] = newSnlp;
                            prev[j] = i;
                        }
                    }
                }
                is_prev_katakana = is_katakana;
            }
            i++;
        }
        int[] t_boundary = new int[(numChars + 1)];
        int numBreaks = 0;
        if (bestSnlp[numChars] == Integer.MAX_VALUE) {
            t_boundary[0] = numChars;
            numBreaks = 1;
        } else {
            for (i = numChars; i > 0; i = prev[i]) {
                t_boundary[numBreaks] = i;
                numBreaks++;
            }
            Assert.assrt(prev[t_boundary[numBreaks + -1]] == 0);
        }
        if (foundBreaks.size() == 0 || foundBreaks.peek() < startPos) {
            int numBreaks2 = numBreaks + 1;
            t_boundary[numBreaks] = 0;
            numBreaks = numBreaks2;
        }
        int correctedNumBreaks = 0;
        for (i = numBreaks - 1; i >= 0; i--) {
            int pos = charPositions[t_boundary[i]] + startPos;
            Object obj = (foundBreaks.contains(pos) || pos == startPos) ? 1 : null;
            if (obj == null) {
                foundBreaks.push(charPositions[t_boundary[i]] + startPos);
                correctedNumBreaks++;
            }
        }
        if (!foundBreaks.isEmpty() && foundBreaks.peek() == endPos) {
            foundBreaks.pop();
            correctedNumBreaks--;
        }
        if (!foundBreaks.isEmpty()) {
            inText.setIndex(foundBreaks.peek());
        }
        return correctedNumBreaks;
    }
}
