package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.DictionaryBreakEngine;
import java.io.IOException;
import java.text.CharacterIterator;

class LaoBreakEngine extends DictionaryBreakEngine {
    private static final byte LAO_LOOKAHEAD = 3;
    private static final byte LAO_MIN_WORD = 2;
    private static final byte LAO_PREFIX_COMBINE_THRESHOLD = 3;
    private static final byte LAO_ROOT_COMBINE_THRESHOLD = 3;
    private static UnicodeSet fBeginWordSet = new UnicodeSet();
    private static UnicodeSet fEndWordSet = new UnicodeSet(fLaoWordSet);
    private static UnicodeSet fLaoWordSet = new UnicodeSet();
    private static UnicodeSet fMarkSet = new UnicodeSet();
    private DictionaryMatcher fDictionary = DictionaryData.loadDictionaryFor("Laoo");

    static {
        fLaoWordSet.applyPattern("[[:Laoo:]&[:LineBreak=SA:]]");
        fLaoWordSet.compact();
        fMarkSet.applyPattern("[[:Laoo:]&[:LineBreak=SA:]&[:M:]]");
        fMarkSet.add(32);
        fEndWordSet.remove(3776, 3780);
        fBeginWordSet.add(3713, 3758);
        fBeginWordSet.add(3804, 3805);
        fBeginWordSet.add(3776, 3780);
        fMarkSet.compact();
        fEndWordSet.compact();
        fBeginWordSet.compact();
        fLaoWordSet.freeze();
        fMarkSet.freeze();
        fEndWordSet.freeze();
        fBeginWordSet.freeze();
    }

    public LaoBreakEngine() throws IOException {
        super(1, 2);
        setCharacters(fLaoWordSet);
    }

    public boolean equals(Object obj) {
        return obj instanceof LaoBreakEngine;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean handles(int c, int breakType) {
        boolean z = false;
        if (breakType != 1 && breakType != 2) {
            return false;
        }
        if (UCharacter.getIntPropertyValue(c, UProperty.SCRIPT) == 24) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x012f  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x013f  */
    public int divideUpDictionaryRange(CharacterIterator fIter, int rangeStart, int rangeEnd, DictionaryBreakEngine.DequeI foundBreaks) {
        CharacterIterator characterIterator = fIter;
        int i = rangeEnd;
        char c = 2;
        if (i - rangeStart < 2) {
            return 0;
        }
        int wordsFound = 0;
        int i2 = 3;
        DictionaryBreakEngine.PossibleWord[] words = new DictionaryBreakEngine.PossibleWord[3];
        for (int i3 = 0; i3 < 3; i3++) {
            words[i3] = new DictionaryBreakEngine.PossibleWord();
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
                    while (true) {
                        if (words[(wordsFound + 1) % i2].candidates(characterIterator, this.fDictionary, i) > 0) {
                            if (1 < c) {
                                words[wordsFound % 3].markCurrent();
                            }
                            if (fIter.getIndex() < i) {
                                while (true) {
                                    if (words[(wordsFound + 2) % i2].candidates(characterIterator, this.fDictionary, i) <= 0) {
                                        if (!words[(wordsFound + 1) % i2].backUp(characterIterator)) {
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
                        if (!words[wordsFound % 3].backUp(characterIterator) || foundBest) {
                            break;
                        }
                    }
                }
                wordLength = words[wordsFound % 3].acceptMarked(characterIterator);
                wordsFound++;
            }
            if (fIter.getIndex() < i && wordLength < i2) {
                if (words[wordsFound % 3].candidates(characterIterator, this.fDictionary, i) > 0 || (wordLength != 0 && words[wordsFound % 3].longestPrefix() >= i2)) {
                    characterIterator.setIndex(current + wordLength);
                } else {
                    int pc = fIter.current();
                    int remaining = i - (current + wordLength);
                    int chars = 0;
                    while (true) {
                        fIter.next();
                        int uc = fIter.current();
                        chars++;
                        remaining--;
                        if (remaining <= 0) {
                            break;
                        }
                        if (fEndWordSet.contains(pc) && fBeginWordSet.contains(uc)) {
                            int candidate = words[(wordsFound + 1) % i2].candidates(characterIterator, this.fDictionary, i);
                            characterIterator.setIndex(current + wordLength + chars);
                            if (candidate > 0) {
                                break;
                            }
                        }
                        pc = uc;
                        i2 = 3;
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
            c = 2;
            i2 = 3;
        }
        DictionaryBreakEngine.DequeI dequeI2 = foundBreaks;
        if (foundBreaks.peek() >= i) {
            foundBreaks.pop();
            wordsFound--;
        }
        return wordsFound;
    }
}
