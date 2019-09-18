package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.DictionaryBreakEngine;
import java.io.IOException;
import java.text.CharacterIterator;

class KhmerBreakEngine extends DictionaryBreakEngine {
    private static final byte KHMER_LOOKAHEAD = 3;
    private static final byte KHMER_MIN_WORD = 2;
    private static final byte KHMER_MIN_WORD_SPAN = 4;
    private static final byte KHMER_PREFIX_COMBINE_THRESHOLD = 3;
    private static final byte KHMER_ROOT_COMBINE_THRESHOLD = 3;
    private static UnicodeSet fBeginWordSet = new UnicodeSet();
    private static UnicodeSet fEndWordSet = new UnicodeSet(fKhmerWordSet);
    private static UnicodeSet fKhmerWordSet = new UnicodeSet();
    private static UnicodeSet fMarkSet = new UnicodeSet();
    private DictionaryMatcher fDictionary = DictionaryData.loadDictionaryFor("Khmr");

    static {
        fKhmerWordSet.applyPattern("[[:Khmer:]&[:LineBreak=SA:]]");
        fKhmerWordSet.compact();
        fMarkSet.applyPattern("[[:Khmer:]&[:LineBreak=SA:]&[:M:]]");
        fMarkSet.add(32);
        fBeginWordSet.add(6016, 6067);
        fEndWordSet.remove(6098);
        fMarkSet.compact();
        fEndWordSet.compact();
        fBeginWordSet.compact();
        fKhmerWordSet.freeze();
        fMarkSet.freeze();
        fEndWordSet.freeze();
        fBeginWordSet.freeze();
    }

    public KhmerBreakEngine() throws IOException {
        super(1, 2);
        setCharacters(fKhmerWordSet);
    }

    public boolean equals(Object obj) {
        return obj instanceof KhmerBreakEngine;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean handles(int c, int breakType) {
        boolean z = false;
        if (breakType != 1 && breakType != 2) {
            return false;
        }
        if (UCharacter.getIntPropertyValue(c, UProperty.SCRIPT) == 23) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x013b  */
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
                boolean foundBest = false;
                if (fIter.getIndex() < i) {
                    do {
                        if (words[(wordsFound + 1) % i3].candidates(characterIterator, this.fDictionary, i) > 0) {
                            if (1 < 2) {
                                words[wordsFound % 3].markCurrent();
                            }
                            if (fIter.getIndex() < i) {
                                while (true) {
                                    if (words[(wordsFound + 2) % i3].candidates(characterIterator, this.fDictionary, i) <= 0) {
                                        if (!words[(wordsFound + 1) % i3].backUp(characterIterator)) {
                                            break;
                                        }
                                    } else {
                                        words[wordsFound % 3].markCurrent();
                                        foundBest = true;
                                        break;
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                        if (!words[wordsFound % 3].backUp(characterIterator)) {
                            break;
                        }
                    } while (!foundBest);
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
                } else if (wordLength <= 0) {
                    foundBreaks.push(Integer.valueOf(current + wordLength).intValue());
                } else {
                    DictionaryBreakEngine.DequeI dequeI = foundBreaks;
                }
            }
            if (wordLength <= 0) {
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
