package ohos.global.icu.text;

import java.io.IOException;
import java.text.CharacterIterator;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.lang.UProperty;
import ohos.global.icu.text.DictionaryBreakEngine;

/* access modifiers changed from: package-private */
public class BurmeseBreakEngine extends DictionaryBreakEngine {
    private static final byte BURMESE_LOOKAHEAD = 3;
    private static final byte BURMESE_MIN_WORD = 2;
    private static final byte BURMESE_PREFIX_COMBINE_THRESHOLD = 3;
    private static final byte BURMESE_ROOT_COMBINE_THRESHOLD = 3;
    private static UnicodeSet fBeginWordSet = new UnicodeSet();
    private static UnicodeSet fBurmeseWordSet = new UnicodeSet();
    private static UnicodeSet fEndWordSet = new UnicodeSet(fBurmeseWordSet);
    private static UnicodeSet fMarkSet = new UnicodeSet();
    private DictionaryMatcher fDictionary = DictionaryData.loadDictionaryFor("Mymr");

    static {
        fBurmeseWordSet.applyPattern("[[:Mymr:]&[:LineBreak=SA:]]");
        fBurmeseWordSet.compact();
        fMarkSet.applyPattern("[[:Mymr:]&[:LineBreak=SA:]&[:M:]]");
        fMarkSet.add(32);
        fBeginWordSet.add(4096, 4138);
        fMarkSet.compact();
        fEndWordSet.compact();
        fBeginWordSet.compact();
        fBurmeseWordSet.freeze();
        fMarkSet.freeze();
        fEndWordSet.freeze();
        fBeginWordSet.freeze();
    }

    public BurmeseBreakEngine() throws IOException {
        setCharacters(fBurmeseWordSet);
    }

    public boolean equals(Object obj) {
        return obj instanceof BurmeseBreakEngine;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    @Override // ohos.global.icu.text.DictionaryBreakEngine, ohos.global.icu.text.LanguageBreakEngine
    public boolean handles(int i) {
        return UCharacter.getIntPropertyValue(i, UProperty.SCRIPT) == 28;
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0120  */
    @Override // ohos.global.icu.text.DictionaryBreakEngine
    public int divideUpDictionaryRange(CharacterIterator characterIterator, int i, int i2, DictionaryBreakEngine.DequeI dequeI) {
        int i3;
        int index;
        int i4;
        if (i2 - i < 2) {
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
                    boolean z = false;
                    do {
                        int i8 = (i6 + 1) % 3;
                        if (possibleWordArr[i8].candidates(characterIterator, this.fDictionary, i2) > 0) {
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
                                    z = true;
                                    break;
                                }
                            }
                        }
                        if (!possibleWordArr[i7].backUp(characterIterator)) {
                            break;
                        }
                    } while (!z);
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
