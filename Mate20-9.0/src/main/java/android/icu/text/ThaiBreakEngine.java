package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.DictionaryBreakEngine;
import java.io.IOException;
import java.text.CharacterIterator;

class ThaiBreakEngine extends DictionaryBreakEngine {
    private static final byte THAI_LOOKAHEAD = 3;
    private static final char THAI_MAIYAMOK = 'ๆ';
    private static final byte THAI_MIN_WORD = 2;
    private static final byte THAI_MIN_WORD_SPAN = 4;
    private static final char THAI_PAIYANNOI = 'ฯ';
    private static final byte THAI_PREFIX_COMBINE_THRESHOLD = 3;
    private static final byte THAI_ROOT_COMBINE_THRESHOLD = 3;
    private static UnicodeSet fBeginWordSet = new UnicodeSet();
    private static UnicodeSet fEndWordSet = new UnicodeSet(fThaiWordSet);
    private static UnicodeSet fMarkSet = new UnicodeSet();
    private static UnicodeSet fSuffixSet = new UnicodeSet();
    private static UnicodeSet fThaiWordSet = new UnicodeSet();
    private DictionaryMatcher fDictionary = DictionaryData.loadDictionaryFor("Thai");

    static {
        fThaiWordSet.applyPattern("[[:Thai:]&[:LineBreak=SA:]]");
        fThaiWordSet.compact();
        fMarkSet.applyPattern("[[:Thai:]&[:LineBreak=SA:]&[:M:]]");
        fMarkSet.add(32);
        fEndWordSet.remove(3633);
        fEndWordSet.remove(3648, 3652);
        fBeginWordSet.add(3585, 3630);
        fBeginWordSet.add(3648, 3652);
        fSuffixSet.add(3631);
        fSuffixSet.add(3654);
        fMarkSet.compact();
        fEndWordSet.compact();
        fBeginWordSet.compact();
        fSuffixSet.compact();
        fThaiWordSet.freeze();
        fMarkSet.freeze();
        fEndWordSet.freeze();
        fBeginWordSet.freeze();
        fSuffixSet.freeze();
    }

    public ThaiBreakEngine() throws IOException {
        super(1, 2);
        setCharacters(fThaiWordSet);
    }

    public boolean equals(Object obj) {
        return obj instanceof ThaiBreakEngine;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean handles(int c, int breakType) {
        boolean z = false;
        if (breakType != 1 && breakType != 2) {
            return false;
        }
        if (UCharacter.getIntPropertyValue(c, UProperty.SCRIPT) == 38) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:90:0x0093 A[EDGE_INSN: B:90:0x0093->B:29:0x0093 ?: BREAK  , SYNTHETIC] */
    public int divideUpDictionaryRange(CharacterIterator fIter, int rangeStart, int rangeEnd, DictionaryBreakEngine.DequeI foundBreaks) {
        CharacterIterator characterIterator = fIter;
        int i = rangeEnd;
        int i2 = 0;
        if (i - rangeStart < 4) {
            return 0;
        }
        int wordsFound = 0;
        int i3 = 3;
        DictionaryBreakEngine.PossibleWord[] words = new DictionaryBreakEngine.PossibleWord[3];
        for (int i4 = 0; i4 < 3; i4++) {
            words[i4] = new DictionaryBreakEngine.PossibleWord();
        }
        fIter.setIndex(rangeStart);
        while (true) {
            int index = fIter.getIndex();
            int current = index;
            if (index >= i) {
                break;
            }
            int wordLength = 0;
            int candidates = words[wordsFound % 3].candidates(characterIterator, this.fDictionary, i);
            if (candidates == 1) {
                wordLength = words[wordsFound % 3].acceptMarked(characterIterator);
                wordsFound++;
            } else if (candidates > 1) {
                if (fIter.getIndex() < i) {
                    while (true) {
                        if (words[(wordsFound + 1) % i3].candidates(characterIterator, this.fDictionary, i) <= 0) {
                            if (!words[wordsFound % 3].backUp(characterIterator)) {
                                break;
                            }
                        } else {
                            if (1 < 2) {
                                words[wordsFound % 3].markCurrent();
                            }
                            if (fIter.getIndex() >= i) {
                                break;
                            }
                            while (true) {
                                if (words[(wordsFound + 2) % i3].candidates(characterIterator, this.fDictionary, i) <= 0) {
                                    if (!words[(wordsFound + 1) % i3].backUp(characterIterator)) {
                                        break;
                                    }
                                } else {
                                    words[wordsFound % 3].markCurrent();
                                    break;
                                }
                            }
                            if (!words[wordsFound % 3].backUp(characterIterator)) {
                            }
                        }
                    }
                }
                wordLength = words[wordsFound % 3].acceptMarked(characterIterator);
                wordsFound++;
            }
            if (fIter.getIndex() < i && wordLength < i3) {
                if (words[wordsFound % 3].candidates(characterIterator, this.fDictionary, i) > 0 || (wordLength != 0 && words[wordsFound % 3].longestPrefix() >= i3)) {
                    characterIterator.setIndex(current + wordLength);
                } else {
                    int pc = fIter.current();
                    int remaining = i - (current + wordLength);
                    int chars = i2;
                    while (true) {
                        fIter.next();
                        int uc = fIter.current();
                        chars++;
                        remaining--;
                        if (remaining <= 0) {
                            break;
                        }
                        if (fEndWordSet.contains(pc) && fBeginWordSet.contains(uc)) {
                            int candidate = words[(wordsFound + 1) % i3].candidates(characterIterator, this.fDictionary, i);
                            characterIterator.setIndex(current + wordLength + chars);
                            if (candidate > 0) {
                                break;
                            }
                        }
                        pc = uc;
                        i3 = 3;
                    }
                    if (wordLength <= 0) {
                        wordsFound++;
                    }
                    wordLength += chars;
                }
            }
            while (true) {
                int index2 = fIter.getIndex();
                int currPos = index2;
                if (index2 < i && fMarkSet.contains((int) fIter.current())) {
                    fIter.next();
                    wordLength += fIter.getIndex() - currPos;
                }
            }
            if (fIter.getIndex() < i && wordLength > 0) {
                if (words[wordsFound % 3].candidates(characterIterator, this.fDictionary, i) <= 0) {
                    UnicodeSet unicodeSet = fSuffixSet;
                    int current2 = fIter.current();
                    int uc2 = current2;
                    if (unicodeSet.contains(current2)) {
                        if (uc2 == 3631) {
                            if (!fSuffixSet.contains((int) fIter.previous())) {
                                fIter.next();
                                fIter.next();
                                wordLength++;
                                uc2 = fIter.current();
                            } else {
                                fIter.next();
                            }
                        }
                        if (uc2 == 3654) {
                            if (fIter.previous() != 3654) {
                                fIter.next();
                                fIter.next();
                                wordLength++;
                            } else {
                                fIter.next();
                            }
                        }
                    }
                }
                characterIterator.setIndex(current + wordLength);
            }
            if (wordLength > 0) {
                foundBreaks.push(Integer.valueOf(current + wordLength).intValue());
            } else {
                DictionaryBreakEngine.DequeI dequeI = foundBreaks;
            }
            i2 = 0;
            i3 = 3;
        }
        DictionaryBreakEngine.DequeI dequeI2 = foundBreaks;
        if (foundBreaks.peek() >= i) {
            foundBreaks.pop();
            wordsFound--;
        }
        return wordsFound;
    }
}
