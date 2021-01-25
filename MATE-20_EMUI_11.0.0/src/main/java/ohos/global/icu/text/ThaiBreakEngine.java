package ohos.global.icu.text;

import java.io.IOException;
import java.text.CharacterIterator;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.lang.UProperty;
import ohos.global.icu.text.DictionaryBreakEngine;

/* access modifiers changed from: package-private */
public class ThaiBreakEngine extends DictionaryBreakEngine {
    private static final byte THAI_LOOKAHEAD = 3;
    private static final char THAI_MAIYAMOK = 3654;
    private static final byte THAI_MIN_WORD = 2;
    private static final byte THAI_MIN_WORD_SPAN = 4;
    private static final char THAI_PAIYANNOI = 3631;
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
        setCharacters(fThaiWordSet);
    }

    public boolean equals(Object obj) {
        return obj instanceof ThaiBreakEngine;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    @Override // ohos.global.icu.text.DictionaryBreakEngine, ohos.global.icu.text.LanguageBreakEngine
    public boolean handles(int i) {
        return UCharacter.getIntPropertyValue(i, UProperty.SCRIPT) == 38;
    }

    /* JADX WARNING: Removed duplicated region for block: B:61:0x011e  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0168  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0178  */
    @Override // ohos.global.icu.text.DictionaryBreakEngine
    public int divideUpDictionaryRange(CharacterIterator characterIterator, int i, int i2, DictionaryBreakEngine.DequeI dequeI) {
        int i3;
        int index;
        int i4;
        if (i2 - i < 4) {
            return 0;
        }
        DictionaryBreakEngine.PossibleWord[] possibleWordArr = new DictionaryBreakEngine.PossibleWord[3];
        for (int i5 = 0; i5 < 3; i5++) {
            possibleWordArr[i5] = new DictionaryBreakEngine.PossibleWord();
        }
        characterIterator.setIndex(i);
        int i6 = 0;
        while (true) {
            int index2 = characterIterator.getIndex();
            if (index2 >= i2) {
                break;
            }
            int i7 = i6 % 3;
            int candidates = possibleWordArr[i7].candidates(characterIterator, this.fDictionary, i2);
            if (candidates == 1) {
                i3 = possibleWordArr[i7].acceptMarked(characterIterator);
            } else if (candidates > 1) {
                if (characterIterator.getIndex() < i2) {
                    while (true) {
                        int i8 = (i6 + 1) % 3;
                        if (possibleWordArr[i8].candidates(characterIterator, this.fDictionary, i2) <= 0) {
                            if (!possibleWordArr[i7].backUp(characterIterator)) {
                                break;
                            }
                        } else {
                            possibleWordArr[i7].markCurrent();
                            if (characterIterator.getIndex() >= i2) {
                                break;
                            }
                            while (true) {
                                if (possibleWordArr[(i6 + 2) % 3].candidates(characterIterator, this.fDictionary, i2) <= 0) {
                                    if (!possibleWordArr[i8].backUp(characterIterator)) {
                                        break;
                                    }
                                } else {
                                    possibleWordArr[i7].markCurrent();
                                    break;
                                }
                            }
                        }
                    }
                }
                i3 = possibleWordArr[i7].acceptMarked(characterIterator);
            } else {
                i3 = 0;
                if (characterIterator.getIndex() < i2 && i3 < 3) {
                    i4 = i6 % 3;
                    if (possibleWordArr[i4].candidates(characterIterator, this.fDictionary, i2) <= 0 || (i3 != 0 && possibleWordArr[i4].longestPrefix() >= 3)) {
                        characterIterator.setIndex(index2 + i3);
                    } else {
                        int i9 = index2 + i3;
                        char current = characterIterator.current();
                        int i10 = i2 - i9;
                        int i11 = 0;
                        while (true) {
                            characterIterator.next();
                            char current2 = characterIterator.current();
                            i11++;
                            i10--;
                            if (i10 <= 0) {
                                break;
                            }
                            if (fEndWordSet.contains(current) && fBeginWordSet.contains(current2)) {
                                int candidates2 = possibleWordArr[(i6 + 1) % 3].candidates(characterIterator, this.fDictionary, i2);
                                characterIterator.setIndex(i9 + i11);
                                if (candidates2 > 0) {
                                    break;
                                }
                            }
                            current = current2;
                        }
                        if (i3 <= 0) {
                            i6++;
                        }
                        i3 += i11;
                    }
                }
                while (true) {
                    index = characterIterator.getIndex();
                    if (index >= i2 || !fMarkSet.contains(characterIterator.current())) {
                        break;
                    }
                    characterIterator.next();
                    i3 += characterIterator.getIndex() - index;
                }
                if (characterIterator.getIndex() < i2 && i3 > 0) {
                    if (possibleWordArr[i6 % 3].candidates(characterIterator, this.fDictionary, i2) <= 0) {
                        UnicodeSet unicodeSet = fSuffixSet;
                        char current3 = characterIterator.current();
                        if (unicodeSet.contains(current3)) {
                            if (current3 == 3631) {
                                if (!fSuffixSet.contains(characterIterator.previous())) {
                                    characterIterator.next();
                                    characterIterator.next();
                                    i3++;
                                    current3 = characterIterator.current();
                                } else {
                                    characterIterator.next();
                                }
                            }
                            if (current3 == 3654) {
                                if (characterIterator.previous() != 3654) {
                                    characterIterator.next();
                                    characterIterator.next();
                                    i3++;
                                } else {
                                    characterIterator.next();
                                }
                            }
                        }
                    }
                    characterIterator.setIndex(index2 + i3);
                }
                if (i3 <= 0) {
                    dequeI.push(Integer.valueOf(index2 + i3).intValue());
                }
            }
            i6++;
            i4 = i6 % 3;
            if (possibleWordArr[i4].candidates(characterIterator, this.fDictionary, i2) <= 0) {
            }
            characterIterator.setIndex(index2 + i3);
            while (true) {
                index = characterIterator.getIndex();
                if (index >= i2) {
                    break;
                }
                break;
                characterIterator.next();
                i3 += characterIterator.getIndex() - index;
            }
            if (possibleWordArr[i6 % 3].candidates(characterIterator, this.fDictionary, i2) <= 0) {
            }
            characterIterator.setIndex(index2 + i3);
            if (i3 <= 0) {
            }
        }
        if (dequeI.peek() < i2) {
            return i6;
        }
        dequeI.pop();
        return i6 - 1;
    }
}
