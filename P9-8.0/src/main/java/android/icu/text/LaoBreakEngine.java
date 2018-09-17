package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import java.io.IOException;
import java.text.CharacterIterator;

class LaoBreakEngine extends DictionaryBreakEngine {
    private static final byte LAO_LOOKAHEAD = (byte) 3;
    private static final byte LAO_MIN_WORD = (byte) 2;
    private static final byte LAO_PREFIX_COMBINE_THRESHOLD = (byte) 3;
    private static final byte LAO_ROOT_COMBINE_THRESHOLD = (byte) 3;
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
        super(Integer.valueOf(1), Integer.valueOf(2));
        setCharacters(fLaoWordSet);
    }

    public boolean equals(Object obj) {
        return obj instanceof LaoBreakEngine;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean handles(int c, int breakType) {
        boolean z = true;
        if (breakType != 1 && breakType != 2) {
            return false;
        }
        if (UCharacter.getIntPropertyValue(c, UProperty.SCRIPT) != 24) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x0029 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01b0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int divideUpDictionaryRange(CharacterIterator fIter, int rangeStart, int rangeEnd, DequeI foundBreaks) {
        if (rangeEnd - rangeStart < 2) {
            return 0;
        }
        int wordsFound = 0;
        PossibleWord[] words = new PossibleWord[3];
        for (int i = 0; i < 3; i++) {
            words[i] = new PossibleWord();
        }
        fIter.setIndex(rangeStart);
        while (true) {
            int current = fIter.getIndex();
            if (current >= rangeEnd) {
                break;
            }
            int wordLength = 0;
            int candidates = words[wordsFound % 3].candidates(fIter, this.fDictionary, rangeEnd);
            if (candidates == 1) {
                wordLength = words[wordsFound % 3].acceptMarked(fIter);
                wordsFound++;
            } else if (candidates > 1) {
                boolean foundBest = false;
                if (fIter.getIndex() < rangeEnd) {
                    while (true) {
                        if (words[(wordsFound + 1) % 3].candidates(fIter, this.fDictionary, rangeEnd) > 0) {
                            words[wordsFound % 3].markCurrent();
                            if (fIter.getIndex() >= rangeEnd) {
                                break;
                            }
                            do {
                                if (words[(wordsFound + 2) % 3].candidates(fIter, this.fDictionary, rangeEnd) > 0) {
                                    words[wordsFound % 3].markCurrent();
                                    foundBest = true;
                                    break;
                                }
                            } while (words[(wordsFound + 1) % 3].backUp(fIter));
                        }
                        if (!words[wordsFound % 3].backUp(fIter) || (foundBest ^ 1) == 0) {
                            break;
                        }
                    }
                }
                wordLength = words[wordsFound % 3].acceptMarked(fIter);
                wordsFound++;
            }
            if (fIter.getIndex() < rangeEnd && wordLength < 3) {
                if (words[wordsFound % 3].candidates(fIter, this.fDictionary, rangeEnd) > 0 || (wordLength != 0 && words[wordsFound % 3].longestPrefix() >= 3)) {
                    fIter.setIndex(current + wordLength);
                } else {
                    int remaining = rangeEnd - (current + wordLength);
                    int pc = fIter.current();
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
                            int candidate = words[(wordsFound + 1) % 3].candidates(fIter, this.fDictionary, rangeEnd);
                            fIter.setIndex((current + wordLength) + chars);
                            if (candidate > 0) {
                                break;
                            }
                        }
                        pc = uc;
                    }
                    if (wordLength <= 0) {
                        wordsFound++;
                    }
                    wordLength += chars;
                }
            }
            while (true) {
                int currPos = fIter.getIndex();
                if (currPos < rangeEnd && fMarkSet.contains(fIter.current())) {
                    fIter.next();
                    wordLength += fIter.getIndex() - currPos;
                } else if (wordLength <= 0) {
                    foundBreaks.push(Integer.valueOf(current + wordLength).intValue());
                }
            }
            if (wordLength <= 0) {
            }
        }
        if (foundBreaks.peek() >= rangeEnd) {
            foundBreaks.pop();
            wordsFound--;
        }
        return wordsFound;
    }
}
