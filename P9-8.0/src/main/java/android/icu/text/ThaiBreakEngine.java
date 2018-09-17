package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import java.io.IOException;
import java.text.CharacterIterator;

class ThaiBreakEngine extends DictionaryBreakEngine {
    private static final byte THAI_LOOKAHEAD = (byte) 3;
    private static final char THAI_MAIYAMOK = 'ๆ';
    private static final byte THAI_MIN_WORD = (byte) 2;
    private static final byte THAI_MIN_WORD_SPAN = (byte) 4;
    private static final char THAI_PAIYANNOI = 'ฯ';
    private static final byte THAI_PREFIX_COMBINE_THRESHOLD = (byte) 3;
    private static final byte THAI_ROOT_COMBINE_THRESHOLD = (byte) 3;
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
        super(Integer.valueOf(1), Integer.valueOf(2));
        setCharacters(fThaiWordSet);
    }

    public boolean equals(Object obj) {
        return obj instanceof ThaiBreakEngine;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean handles(int c, int breakType) {
        boolean z = true;
        if (breakType != 1 && breakType != 2) {
            return false;
        }
        if (UCharacter.getIntPropertyValue(c, UProperty.SCRIPT) != 38) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:98:0x010f A:{SYNTHETIC, EDGE_INSN: B:98:0x010f->B:42:0x010f ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x015f A:{LOOP_END, LOOP:4: B:38:0x00e0->B:50:0x015f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int divideUpDictionaryRange(CharacterIterator fIter, int rangeStart, int rangeEnd, DequeI foundBreaks) {
        if (rangeEnd - rangeStart < 4) {
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
            int uc;
            int wordLength = 0;
            int candidates = words[wordsFound % 3].candidates(fIter, this.fDictionary, rangeEnd);
            if (candidates == 1) {
                wordLength = words[wordsFound % 3].acceptMarked(fIter);
                wordsFound++;
            } else if (candidates > 1) {
                if (fIter.getIndex() < rangeEnd) {
                    while (true) {
                        if (words[(wordsFound + 1) % 3].candidates(fIter, this.fDictionary, rangeEnd) > 0) {
                            words[wordsFound % 3].markCurrent();
                            if (fIter.getIndex() < rangeEnd) {
                                do {
                                    if (words[(wordsFound + 2) % 3].candidates(fIter, this.fDictionary, rangeEnd) > 0) {
                                        words[wordsFound % 3].markCurrent();
                                        break;
                                    }
                                } while (words[(wordsFound + 1) % 3].backUp(fIter));
                                if (words[wordsFound % 3].backUp(fIter)) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else if (words[wordsFound % 3].backUp(fIter)) {
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
                        uc = fIter.current();
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
                }
            }
            if (fIter.getIndex() < rangeEnd && wordLength > 0) {
                if (words[wordsFound % 3].candidates(fIter, this.fDictionary, rangeEnd) <= 0) {
                    UnicodeSet unicodeSet = fSuffixSet;
                    uc = fIter.current();
                    if (unicodeSet.contains(uc)) {
                        if (uc == 3631) {
                            if (fSuffixSet.contains(fIter.previous())) {
                                fIter.next();
                            } else {
                                fIter.next();
                                fIter.next();
                                wordLength++;
                                uc = fIter.current();
                            }
                        }
                        if (uc == 3654) {
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
                fIter.setIndex(current + wordLength);
            }
            if (wordLength > 0) {
                foundBreaks.push(Integer.valueOf(current + wordLength).intValue());
            }
        }
        if (foundBreaks.peek() >= rangeEnd) {
            foundBreaks.pop();
            wordsFound--;
        }
        return wordsFound;
    }
}
