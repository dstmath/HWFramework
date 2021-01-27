package ohos.global.icu.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import ohos.global.icu.impl.CharacterIteration;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.ICUDebug;
import ohos.global.icu.impl.RBBIDataWrapper;
import ohos.global.icu.impl.Trie2;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.DictionaryBreakEngine;

public class RuleBasedBreakIterator extends BreakIterator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String RBBI_DEBUG_ARG = "rbbi";
    private static final int RBBI_END = 2;
    private static final int RBBI_RUN = 1;
    private static final int RBBI_START = 0;
    private static final int START_STATE = 1;
    private static final int STOP_STATE = 0;
    private static final boolean TRACE = (ICUDebug.enabled(RBBI_DEBUG_ARG) && ICUDebug.value(RBBI_DEBUG_ARG).indexOf("trace") >= 0);
    @Deprecated
    public static final String fDebugEnv = (ICUDebug.enabled(RBBI_DEBUG_ARG) ? ICUDebug.value(RBBI_DEBUG_ARG) : null);
    private static final List<LanguageBreakEngine> gAllBreakEngines = new ArrayList();
    private static final UnhandledBreakEngine gUnhandledBreakEngine = new UnhandledBreakEngine();
    private static final int kMaxLookaheads = 8;
    private BreakCache fBreakCache;
    private List<LanguageBreakEngine> fBreakEngines;
    private DictionaryCache fDictionaryCache;
    private int fDictionaryCharCount;
    private boolean fDone;
    private LookAheadResults fLookAheadMatches;
    private int fPosition;
    @Deprecated
    public RBBIDataWrapper fRData;
    private int fRuleStatusIndex;
    private CharacterIterator fText;

    private RuleBasedBreakIterator() {
        this.fText = new StringCharacterIterator("");
        this.fBreakCache = new BreakCache();
        this.fDictionaryCache = new DictionaryCache();
        this.fLookAheadMatches = new LookAheadResults();
        this.fDictionaryCharCount = 0;
        synchronized (gAllBreakEngines) {
            this.fBreakEngines = new ArrayList(gAllBreakEngines);
        }
    }

    public static RuleBasedBreakIterator getInstanceFromCompiledRules(InputStream inputStream) throws IOException {
        RuleBasedBreakIterator ruleBasedBreakIterator = new RuleBasedBreakIterator();
        ruleBasedBreakIterator.fRData = RBBIDataWrapper.get(ICUBinary.getByteBufferFromInputStreamAndCloseStream(inputStream));
        return ruleBasedBreakIterator;
    }

    @Deprecated
    public static RuleBasedBreakIterator getInstanceFromCompiledRules(ByteBuffer byteBuffer) throws IOException {
        RuleBasedBreakIterator ruleBasedBreakIterator = new RuleBasedBreakIterator();
        ruleBasedBreakIterator.fRData = RBBIDataWrapper.get(byteBuffer);
        return ruleBasedBreakIterator;
    }

    public RuleBasedBreakIterator(String str) {
        this();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compileRules(str, byteArrayOutputStream);
            this.fRData = RBBIDataWrapper.get(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("RuleBasedBreakIterator rule compilation internal error: " + e.getMessage());
        }
    }

    @Override // ohos.global.icu.text.BreakIterator, java.lang.Object
    public Object clone() {
        RuleBasedBreakIterator ruleBasedBreakIterator = (RuleBasedBreakIterator) super.clone();
        CharacterIterator characterIterator = this.fText;
        if (characterIterator != null) {
            ruleBasedBreakIterator.fText = (CharacterIterator) characterIterator.clone();
        }
        synchronized (gAllBreakEngines) {
            ruleBasedBreakIterator.fBreakEngines = new ArrayList(gAllBreakEngines);
        }
        ruleBasedBreakIterator.fLookAheadMatches = new LookAheadResults();
        ruleBasedBreakIterator.fBreakCache = new BreakCache(this.fBreakCache);
        ruleBasedBreakIterator.fDictionaryCache = new DictionaryCache(this.fDictionaryCache);
        return ruleBasedBreakIterator;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        CharacterIterator characterIterator;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        try {
            RuleBasedBreakIterator ruleBasedBreakIterator = (RuleBasedBreakIterator) obj;
            if (this.fRData != ruleBasedBreakIterator.fRData && (this.fRData == null || ruleBasedBreakIterator.fRData == null)) {
                return false;
            }
            if (this.fRData != null && ruleBasedBreakIterator.fRData != null && !this.fRData.fRuleSource.equals(ruleBasedBreakIterator.fRData.fRuleSource)) {
                return false;
            }
            if (this.fText == null && ruleBasedBreakIterator.fText == null) {
                return true;
            }
            if (this.fText == null || (characterIterator = ruleBasedBreakIterator.fText) == null) {
                return false;
            }
            if (!this.fText.equals(characterIterator)) {
                return false;
            }
            return this.fPosition == ruleBasedBreakIterator.fPosition;
        } catch (ClassCastException unused) {
            return false;
        }
    }

    @Override // java.lang.Object
    public String toString() {
        RBBIDataWrapper rBBIDataWrapper = this.fRData;
        return rBBIDataWrapper != null ? rBBIDataWrapper.fRuleSource : "";
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.fRData.fRuleSource.hashCode();
    }

    static {
        gAllBreakEngines.add(gUnhandledBreakEngine);
    }

    @Deprecated
    public void dump(PrintStream printStream) {
        if (printStream == null) {
            printStream = System.out;
        }
        this.fRData.dump(printStream);
    }

    public static void compileRules(String str, OutputStream outputStream) throws IOException {
        RBBIRuleBuilder.compileRules(str, outputStream);
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int first() {
        CharacterIterator characterIterator = this.fText;
        if (characterIterator == null) {
            return -1;
        }
        characterIterator.first();
        int index = this.fText.getIndex();
        if (!this.fBreakCache.seek(index)) {
            this.fBreakCache.populateNear(index);
        }
        this.fBreakCache.current();
        return this.fPosition;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int last() {
        CharacterIterator characterIterator = this.fText;
        if (characterIterator == null) {
            return -1;
        }
        int endIndex = characterIterator.getEndIndex();
        isBoundary(endIndex);
        int i = this.fPosition;
        return endIndex;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int next(int i) {
        int i2 = 0;
        if (i > 0) {
            while (i > 0 && i2 != -1) {
                i2 = next();
                i--;
            }
            return i2;
        } else if (i >= 0) {
            return current();
        } else {
            while (i < 0 && i2 != -1) {
                i2 = previous();
                i++;
            }
            return i2;
        }
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int next() {
        this.fBreakCache.next();
        if (this.fDone) {
            return -1;
        }
        return this.fPosition;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int previous() {
        this.fBreakCache.previous();
        if (this.fDone) {
            return -1;
        }
        return this.fPosition;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int following(int i) {
        if (i < this.fText.getBeginIndex()) {
            return first();
        }
        this.fBreakCache.following(CISetIndex32(this.fText, i));
        if (this.fDone) {
            return -1;
        }
        return this.fPosition;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int preceding(int i) {
        CharacterIterator characterIterator = this.fText;
        if (characterIterator == null || i > characterIterator.getEndIndex()) {
            return last();
        }
        if (i < this.fText.getBeginIndex()) {
            return first();
        }
        this.fBreakCache.preceding(i);
        if (this.fDone) {
            return -1;
        }
        return this.fPosition;
    }

    protected static final void checkOffset(int i, CharacterIterator characterIterator) {
        if (i < characterIterator.getBeginIndex() || i > characterIterator.getEndIndex()) {
            throw new IllegalArgumentException("offset out of bounds");
        }
    }

    @Override // ohos.global.icu.text.BreakIterator
    public boolean isBoundary(int i) {
        checkOffset(i, this.fText);
        int CISetIndex32 = CISetIndex32(this.fText, i);
        boolean z = false;
        if ((this.fBreakCache.seek(CISetIndex32) || this.fBreakCache.populateNear(CISetIndex32)) && this.fBreakCache.current() == i) {
            z = true;
        }
        if (!z) {
            next();
        }
        return z;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int current() {
        if (this.fText != null) {
            return this.fPosition;
        }
        return -1;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int getRuleStatus() {
        return this.fRData.fStatusTable[this.fRuleStatusIndex + this.fRData.fStatusTable[this.fRuleStatusIndex]];
    }

    @Override // ohos.global.icu.text.BreakIterator
    public int getRuleStatusVec(int[] iArr) {
        int i = this.fRData.fStatusTable[this.fRuleStatusIndex];
        if (iArr != null) {
            int min = Math.min(i, iArr.length);
            for (int i2 = 0; i2 < min; i2++) {
                iArr[i2] = this.fRData.fStatusTable[this.fRuleStatusIndex + i2 + 1];
            }
        }
        return i;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public CharacterIterator getText() {
        return this.fText;
    }

    @Override // ohos.global.icu.text.BreakIterator
    public void setText(CharacterIterator characterIterator) {
        if (characterIterator != null) {
            this.fBreakCache.reset(characterIterator.getBeginIndex(), 0);
        } else {
            this.fBreakCache.reset();
        }
        this.fDictionaryCache.reset();
        this.fText = characterIterator;
        first();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LanguageBreakEngine getLanguageBreakEngine(int i) {
        LanguageBreakEngine languageBreakEngine;
        for (LanguageBreakEngine languageBreakEngine2 : this.fBreakEngines) {
            if (languageBreakEngine2.handles(i)) {
                return languageBreakEngine2;
            }
        }
        synchronized (gAllBreakEngines) {
            for (LanguageBreakEngine languageBreakEngine3 : gAllBreakEngines) {
                if (languageBreakEngine3.handles(i)) {
                    this.fBreakEngines.add(languageBreakEngine3);
                    return languageBreakEngine3;
                }
            }
            int intPropertyValue = UCharacter.getIntPropertyValue(i, 4106);
            if (intPropertyValue == 22 || intPropertyValue == 20) {
                intPropertyValue = 17;
            }
            if (intPropertyValue == 17) {
                languageBreakEngine = new CjkBreakEngine(false);
            } else if (intPropertyValue == 18) {
                languageBreakEngine = new CjkBreakEngine(true);
            } else if (intPropertyValue == 23) {
                languageBreakEngine = new KhmerBreakEngine();
            } else if (intPropertyValue == 24) {
                languageBreakEngine = new LaoBreakEngine();
            } else if (intPropertyValue == 28) {
                languageBreakEngine = new BurmeseBreakEngine();
            } else if (intPropertyValue != 38) {
                try {
                    gUnhandledBreakEngine.handleChar(i);
                    languageBreakEngine = gUnhandledBreakEngine;
                } catch (IOException unused) {
                    languageBreakEngine = null;
                }
            } else {
                languageBreakEngine = new ThaiBreakEngine();
            }
            if (!(languageBreakEngine == null || languageBreakEngine == gUnhandledBreakEngine)) {
                gAllBreakEngines.add(languageBreakEngine);
                this.fBreakEngines.add(languageBreakEngine);
            }
            return languageBreakEngine;
        }
    }

    /* access modifiers changed from: private */
    public static class LookAheadResults {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        int[] fKeys = new int[8];
        int[] fPositions = new int[8];
        int fUsedSlotLimit = 0;

        LookAheadResults() {
        }

        /* access modifiers changed from: package-private */
        public int getPosition(int i) {
            for (int i2 = 0; i2 < this.fUsedSlotLimit; i2++) {
                if (this.fKeys[i2] == i) {
                    return this.fPositions[i2];
                }
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public void setPosition(int i, int i2) {
            int i3 = 0;
            while (i3 < this.fUsedSlotLimit) {
                if (this.fKeys[i3] == i) {
                    this.fPositions[i3] = i2;
                    return;
                }
                i3++;
            }
            if (i3 >= 8) {
                i3 = 7;
            }
            this.fKeys[i3] = i;
            this.fPositions[i3] = i2;
            this.fUsedSlotLimit = i3 + 1;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.fUsedSlotLimit = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleNext() {
        short s;
        short s2;
        int position;
        if (TRACE) {
            System.out.println("Handle Next   pos      char  state category");
        }
        this.fRuleStatusIndex = 0;
        this.fDictionaryCharCount = 0;
        CharacterIterator characterIterator = this.fText;
        Trie2 trie2 = this.fRData.fTrie;
        short[] sArr = this.fRData.fFTable.fTable;
        int i = this.fPosition;
        characterIterator.setIndex(i);
        int current = characterIterator.current();
        short s3 = 1;
        if (current < 55296 || (current = CharacterIteration.nextTrail32(characterIterator, current)) != Integer.MAX_VALUE) {
            int rowIndex = this.fRData.getRowIndex(1);
            char c = 6;
            char c2 = '\n';
            int i2 = 5;
            if ((this.fRData.fFTable.fFlags & 2) != 0) {
                if (TRACE) {
                    System.out.print("            " + RBBIDataWrapper.intToString(characterIterator.getIndex(), 5));
                    System.out.print(RBBIDataWrapper.intToHexString(current, 10));
                    System.out.println(RBBIDataWrapper.intToString(1, 7) + RBBIDataWrapper.intToString(2, 6));
                }
                s2 = 0;
                s = 2;
            } else {
                s2 = 1;
                s = 3;
            }
            this.fLookAheadMatches.reset();
            int i3 = i;
            int i4 = rowIndex;
            short s4 = s2;
            int i5 = current;
            short s5 = 1;
            while (s5 != 0) {
                if (i5 == Integer.MAX_VALUE) {
                    if (s4 == 2) {
                        break;
                    }
                    s = s3;
                    s4 = 2;
                } else if (s4 == s3) {
                    short s6 = (short) trie2.get(i5);
                    if ((s6 & 16384) != 0) {
                        this.fDictionaryCharCount += s3;
                        s6 = (short) (s6 & -16385);
                    }
                    if (TRACE) {
                        System.out.print("            " + RBBIDataWrapper.intToString(characterIterator.getIndex(), i2));
                        System.out.print(RBBIDataWrapper.intToHexString(i5, 10));
                        System.out.println(RBBIDataWrapper.intToString(s5, 7) + RBBIDataWrapper.intToString(s6, 6));
                    }
                    int next = characterIterator.next();
                    if (next >= 55296) {
                        next = CharacterIteration.nextTrail32(characterIterator, next);
                    }
                    i5 = next;
                    s = s6;
                } else {
                    s4 = 1;
                }
                short s7 = sArr[i4 + 4 + s];
                i4 = this.fRData.getRowIndex(s7);
                int i6 = i4 + 0;
                if (sArr[i6] == -1) {
                    int index = characterIterator.getIndex();
                    if (i5 >= 65536 && i5 <= 1114111) {
                        index--;
                    }
                    this.fRuleStatusIndex = sArr[i4 + 2];
                    i3 = index;
                }
                short s8 = sArr[i6];
                if (s8 <= 0 || (position = this.fLookAheadMatches.getPosition(s8)) < 0) {
                    short s9 = sArr[i4 + 1];
                    if (s9 != 0) {
                        int index2 = characterIterator.getIndex();
                        if (i5 >= 65536 && i5 <= 1114111) {
                            index2--;
                        }
                        this.fLookAheadMatches.setPosition(s9, index2);
                    }
                    s5 = s7;
                    c2 = '\n';
                    i2 = 5;
                    s3 = 1;
                    c = 6;
                } else {
                    this.fRuleStatusIndex = sArr[i4 + 2];
                    this.fPosition = position;
                    return position;
                }
            }
            if (i3 == i) {
                if (TRACE) {
                    System.out.println("Iterator did not move. Advancing by 1.");
                }
                characterIterator.setIndex(i);
                CharacterIteration.next32(characterIterator);
                i3 = characterIterator.getIndex();
                this.fRuleStatusIndex = 0;
            }
            this.fPosition = i3;
            if (TRACE) {
                System.out.println("result = " + i3);
            }
            return i3;
        }
        this.fDone = true;
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleSafePrevious(int i) {
        CharacterIterator characterIterator = this.fText;
        Trie2 trie2 = this.fRData.fTrie;
        short[] sArr = this.fRData.fRTable.fTable;
        CISetIndex32(characterIterator, i);
        if (TRACE) {
            System.out.print("Handle Previous   pos   char  state category");
        }
        if (characterIterator.getIndex() == characterIterator.getBeginIndex()) {
            return -1;
        }
        short s = 1;
        int rowIndex = this.fRData.getRowIndex(1);
        for (int previous32 = CharacterIteration.previous32(characterIterator); previous32 != Integer.MAX_VALUE; previous32 = CharacterIteration.previous32(characterIterator)) {
            short s2 = (short) (((short) trie2.get(previous32)) & -16385);
            if (TRACE) {
                PrintStream printStream = System.out;
                printStream.print("            " + RBBIDataWrapper.intToString(characterIterator.getIndex(), 5));
                System.out.print(RBBIDataWrapper.intToHexString(previous32, 10));
                PrintStream printStream2 = System.out;
                printStream2.println(RBBIDataWrapper.intToString(s, 7) + RBBIDataWrapper.intToString(s2, 6));
            }
            s = sArr[rowIndex + 4 + s2];
            rowIndex = this.fRData.getRowIndex(s);
            if (s == 0) {
                break;
            }
        }
        int index = characterIterator.getIndex();
        if (TRACE) {
            PrintStream printStream3 = System.out;
            printStream3.println("result = " + index);
        }
        return index;
    }

    private static int CISetIndex32(CharacterIterator characterIterator, int i) {
        if (i <= characterIterator.getBeginIndex()) {
            characterIterator.first();
        } else if (i >= characterIterator.getEndIndex()) {
            characterIterator.setIndex(characterIterator.getEndIndex());
        } else if (Character.isLowSurrogate(characterIterator.setIndex(i)) && !Character.isHighSurrogate(characterIterator.previous())) {
            characterIterator.next();
        }
        return characterIterator.getIndex();
    }

    /* access modifiers changed from: package-private */
    public class DictionaryCache {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        int fBoundary;
        DictionaryBreakEngine.DequeI fBreaks;
        int fFirstRuleStatusIndex;
        int fLimit;
        int fOtherRuleStatusIndex;
        int fPositionInCache;
        int fStart;
        int fStatusIndex;

        /* access modifiers changed from: package-private */
        public void reset() {
            this.fPositionInCache = -1;
            this.fStart = 0;
            this.fLimit = 0;
            this.fFirstRuleStatusIndex = 0;
            this.fOtherRuleStatusIndex = 0;
            this.fBreaks.removeAllElements();
        }

        /* access modifiers changed from: package-private */
        public boolean following(int i) {
            if (i >= this.fLimit || i < this.fStart) {
                this.fPositionInCache = -1;
                return false;
            }
            int i2 = this.fPositionInCache;
            if (i2 < 0 || i2 >= this.fBreaks.size() || this.fBreaks.elementAt(this.fPositionInCache) != i) {
                this.fPositionInCache = 0;
                while (this.fPositionInCache < this.fBreaks.size()) {
                    int elementAt = this.fBreaks.elementAt(this.fPositionInCache);
                    if (elementAt > i) {
                        this.fBoundary = elementAt;
                        this.fStatusIndex = this.fOtherRuleStatusIndex;
                        return true;
                    }
                    this.fPositionInCache++;
                }
                this.fPositionInCache = -1;
                return false;
            }
            this.fPositionInCache++;
            if (this.fPositionInCache >= this.fBreaks.size()) {
                this.fPositionInCache = -1;
                return false;
            }
            this.fBoundary = this.fBreaks.elementAt(this.fPositionInCache);
            this.fStatusIndex = this.fOtherRuleStatusIndex;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean preceding(int i) {
            int i2;
            if (i <= this.fStart || i > (i2 = this.fLimit)) {
                this.fPositionInCache = -1;
                return false;
            }
            if (i == i2) {
                this.fPositionInCache = this.fBreaks.size() - 1;
                int i3 = this.fPositionInCache;
            }
            int i4 = this.fPositionInCache;
            if (i4 > 0 && i4 < this.fBreaks.size() && this.fBreaks.elementAt(this.fPositionInCache) == i) {
                this.fPositionInCache--;
                int elementAt = this.fBreaks.elementAt(this.fPositionInCache);
                this.fBoundary = elementAt;
                this.fStatusIndex = elementAt == this.fStart ? this.fFirstRuleStatusIndex : this.fOtherRuleStatusIndex;
                return true;
            } else if (this.fPositionInCache == 0) {
                this.fPositionInCache = -1;
                return false;
            } else {
                int size = this.fBreaks.size();
                while (true) {
                    this.fPositionInCache = size - 1;
                    int i5 = this.fPositionInCache;
                    if (i5 >= 0) {
                        int elementAt2 = this.fBreaks.elementAt(i5);
                        if (elementAt2 < i) {
                            this.fBoundary = elementAt2;
                            this.fStatusIndex = elementAt2 == this.fStart ? this.fFirstRuleStatusIndex : this.fOtherRuleStatusIndex;
                            return true;
                        }
                        size = this.fPositionInCache;
                    } else {
                        this.fPositionInCache = -1;
                        return false;
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void populateDictionary(int i, int i2, int i3, int i4) {
            int i5;
            if (i2 - i > 1) {
                reset();
                this.fFirstRuleStatusIndex = i3;
                this.fOtherRuleStatusIndex = i4;
                RuleBasedBreakIterator.this.fText.setIndex(i);
                int current32 = CharacterIteration.current32(RuleBasedBreakIterator.this.fText);
                short s = (short) RuleBasedBreakIterator.this.fRData.fTrie.get(current32);
                int i6 = 0;
                while (true) {
                    int index = RuleBasedBreakIterator.this.fText.getIndex();
                    if (index < i2 && (s & 16384) == 0) {
                        current32 = CharacterIteration.next32(RuleBasedBreakIterator.this.fText);
                        i5 = RuleBasedBreakIterator.this.fRData.fTrie.get(current32);
                    } else if (index >= i2) {
                        break;
                    } else {
                        LanguageBreakEngine languageBreakEngine = RuleBasedBreakIterator.this.getLanguageBreakEngine(current32);
                        if (languageBreakEngine != null) {
                            i6 += languageBreakEngine.findBreaks(RuleBasedBreakIterator.this.fText, i, i2, this.fBreaks);
                        }
                        current32 = CharacterIteration.current32(RuleBasedBreakIterator.this.fText);
                        i5 = RuleBasedBreakIterator.this.fRData.fTrie.get(current32);
                    }
                    s = (short) i5;
                }
                if (i6 > 0) {
                    if (i < this.fBreaks.elementAt(0)) {
                        this.fBreaks.offer(i);
                    }
                    if (i2 > this.fBreaks.peek()) {
                        this.fBreaks.push(i2);
                    }
                    this.fPositionInCache = 0;
                    this.fStart = this.fBreaks.elementAt(0);
                    this.fLimit = this.fBreaks.peek();
                }
            }
        }

        DictionaryCache() {
            this.fPositionInCache = -1;
            this.fBreaks = new DictionaryBreakEngine.DequeI();
        }

        DictionaryCache(DictionaryCache dictionaryCache) {
            try {
                this.fBreaks = (DictionaryBreakEngine.DequeI) dictionaryCache.fBreaks.clone();
                this.fPositionInCache = dictionaryCache.fPositionInCache;
                this.fStart = dictionaryCache.fStart;
                this.fLimit = dictionaryCache.fLimit;
                this.fFirstRuleStatusIndex = dictionaryCache.fFirstRuleStatusIndex;
                this.fOtherRuleStatusIndex = dictionaryCache.fOtherRuleStatusIndex;
                this.fBoundary = dictionaryCache.fBoundary;
                this.fStatusIndex = dictionaryCache.fStatusIndex;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class BreakCache {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        static final int CACHE_SIZE = 128;
        static final boolean RetainCachePosition = false;
        static final boolean UpdateCachePosition = true;
        int[] fBoundaries;
        int fBufIdx;
        int fEndBufIdx;
        DictionaryBreakEngine.DequeI fSideBuffer;
        int fStartBufIdx;
        short[] fStatuses;
        int fTextIdx;

        private final int modChunkSize(int i) {
            return i & Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT;
        }

        BreakCache() {
            this.fBoundaries = new int[128];
            this.fStatuses = new short[128];
            this.fSideBuffer = new DictionaryBreakEngine.DequeI();
            reset();
        }

        /* access modifiers changed from: package-private */
        public void reset(int i, int i2) {
            this.fStartBufIdx = 0;
            this.fEndBufIdx = 0;
            this.fTextIdx = i;
            this.fBufIdx = 0;
            this.fBoundaries[0] = i;
            this.fStatuses[0] = (short) i2;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            reset(0, 0);
        }

        /* access modifiers changed from: package-private */
        public void next() {
            int i = this.fBufIdx;
            if (i == this.fEndBufIdx) {
                RuleBasedBreakIterator.this.fDone = populateFollowing() ^ UpdateCachePosition;
                RuleBasedBreakIterator.this.fPosition = this.fTextIdx;
                RuleBasedBreakIterator.this.fRuleStatusIndex = this.fStatuses[this.fBufIdx];
                return;
            }
            this.fBufIdx = modChunkSize(i + 1);
            this.fTextIdx = RuleBasedBreakIterator.this.fPosition = this.fBoundaries[this.fBufIdx];
            RuleBasedBreakIterator.this.fRuleStatusIndex = this.fStatuses[this.fBufIdx];
        }

        /* access modifiers changed from: package-private */
        public void previous() {
            int i = this.fBufIdx;
            if (i == this.fStartBufIdx) {
                populatePreceding();
            } else {
                this.fBufIdx = modChunkSize(i - 1);
                this.fTextIdx = this.fBoundaries[this.fBufIdx];
            }
            RuleBasedBreakIterator.this.fDone = this.fBufIdx == i ? UpdateCachePosition : false;
            RuleBasedBreakIterator.this.fPosition = this.fTextIdx;
            RuleBasedBreakIterator.this.fRuleStatusIndex = this.fStatuses[this.fBufIdx];
        }

        /* access modifiers changed from: package-private */
        public void following(int i) {
            if (i == this.fTextIdx || seek(i) || populateNear(i)) {
                RuleBasedBreakIterator.this.fDone = false;
                next();
            }
        }

        /* access modifiers changed from: package-private */
        public void preceding(int i) {
            if (i != this.fTextIdx && !seek(i) && !populateNear(i)) {
                return;
            }
            if (i == this.fTextIdx) {
                previous();
            } else {
                current();
            }
        }

        /* access modifiers changed from: package-private */
        public int current() {
            RuleBasedBreakIterator.this.fPosition = this.fTextIdx;
            RuleBasedBreakIterator.this.fRuleStatusIndex = this.fStatuses[this.fBufIdx];
            RuleBasedBreakIterator.this.fDone = false;
            return this.fTextIdx;
        }

        /* access modifiers changed from: package-private */
        public boolean populateNear(int i) {
            int[] iArr;
            int i2;
            int i3;
            int i4;
            int[] iArr2 = this.fBoundaries;
            if (i < iArr2[this.fStartBufIdx] - 15 || i > iArr2[this.fEndBufIdx] + 15) {
                int beginIndex = RuleBasedBreakIterator.this.fText.getBeginIndex();
                if (i > beginIndex + 20) {
                    int handleSafePrevious = RuleBasedBreakIterator.this.handleSafePrevious(i);
                    if (handleSafePrevious > beginIndex) {
                        RuleBasedBreakIterator.this.fPosition = handleSafePrevious;
                        beginIndex = RuleBasedBreakIterator.this.handleNext();
                        if (beginIndex == handleSafePrevious + 1 || (beginIndex == handleSafePrevious + 2 && Character.isHighSurrogate(RuleBasedBreakIterator.this.fText.setIndex(handleSafePrevious)) && Character.isLowSurrogate(RuleBasedBreakIterator.this.fText.next()))) {
                            beginIndex = RuleBasedBreakIterator.this.handleNext();
                        }
                    }
                    i4 = RuleBasedBreakIterator.this.fRuleStatusIndex;
                } else {
                    i4 = 0;
                }
                reset(beginIndex, i4);
            }
            int[] iArr3 = this.fBoundaries;
            if (iArr3[this.fEndBufIdx] < i) {
                do {
                    int[] iArr4 = this.fBoundaries;
                    int i5 = this.fEndBufIdx;
                    if (iArr4[i5] >= i) {
                        this.fBufIdx = i5;
                        this.fTextIdx = iArr4[this.fBufIdx];
                        while (this.fTextIdx > i) {
                            previous();
                        }
                        return UpdateCachePosition;
                    }
                } while (populateFollowing());
                return false;
            }
            if (iArr3[this.fStartBufIdx] > i) {
                while (true) {
                    iArr = this.fBoundaries;
                    i2 = this.fStartBufIdx;
                    if (iArr[i2] <= i) {
                        break;
                    }
                    populatePreceding();
                }
                this.fBufIdx = i2;
                this.fTextIdx = iArr[this.fBufIdx];
                while (true) {
                    i3 = this.fTextIdx;
                    if (i3 >= i) {
                        break;
                    }
                    next();
                }
                if (i3 > i) {
                    previous();
                }
            }
            return UpdateCachePosition;
        }

        /* access modifiers changed from: package-private */
        public boolean populateFollowing() {
            int handleNext;
            int[] iArr = this.fBoundaries;
            int i = this.fEndBufIdx;
            int i2 = iArr[i];
            short s = this.fStatuses[i];
            if (RuleBasedBreakIterator.this.fDictionaryCache.following(i2)) {
                addFollowing(RuleBasedBreakIterator.this.fDictionaryCache.fBoundary, RuleBasedBreakIterator.this.fDictionaryCache.fStatusIndex, UpdateCachePosition);
                return UpdateCachePosition;
            }
            RuleBasedBreakIterator.this.fPosition = i2;
            int handleNext2 = RuleBasedBreakIterator.this.handleNext();
            if (handleNext2 == -1) {
                return false;
            }
            int i3 = RuleBasedBreakIterator.this.fRuleStatusIndex;
            if (RuleBasedBreakIterator.this.fDictionaryCharCount > 0) {
                RuleBasedBreakIterator.this.fDictionaryCache.populateDictionary(i2, handleNext2, s, i3);
                if (RuleBasedBreakIterator.this.fDictionaryCache.following(i2)) {
                    addFollowing(RuleBasedBreakIterator.this.fDictionaryCache.fBoundary, RuleBasedBreakIterator.this.fDictionaryCache.fStatusIndex, UpdateCachePosition);
                    return UpdateCachePosition;
                }
            }
            addFollowing(handleNext2, i3, UpdateCachePosition);
            for (int i4 = 0; i4 < 6 && (handleNext = RuleBasedBreakIterator.this.handleNext()) != -1 && RuleBasedBreakIterator.this.fDictionaryCharCount <= 0; i4++) {
                addFollowing(handleNext, RuleBasedBreakIterator.this.fRuleStatusIndex, false);
            }
            return UpdateCachePosition;
        }

        /* access modifiers changed from: package-private */
        public boolean populatePreceding() {
            int i;
            int i2;
            boolean z;
            int beginIndex = RuleBasedBreakIterator.this.fText.getBeginIndex();
            int i3 = this.fBoundaries[this.fStartBufIdx];
            if (i3 == beginIndex) {
                return false;
            }
            boolean preceding = RuleBasedBreakIterator.this.fDictionaryCache.preceding(i3);
            boolean z2 = UpdateCachePosition;
            if (preceding) {
                addPreceding(RuleBasedBreakIterator.this.fDictionaryCache.fBoundary, RuleBasedBreakIterator.this.fDictionaryCache.fStatusIndex, UpdateCachePosition);
                return UpdateCachePosition;
            }
            int i4 = i3;
            do {
                int i5 = i4 - 30;
                if (i5 <= beginIndex) {
                    i4 = beginIndex;
                } else {
                    i4 = RuleBasedBreakIterator.this.handleSafePrevious(i5);
                }
                if (i4 == -1 || i4 == beginIndex) {
                    i2 = beginIndex;
                    i = 0;
                    continue;
                } else {
                    RuleBasedBreakIterator.this.fPosition = i4;
                    i2 = RuleBasedBreakIterator.this.handleNext();
                    if (i2 == i4 + 1 || (i2 == i4 + 2 && Character.isHighSurrogate(RuleBasedBreakIterator.this.fText.setIndex(i4)) && Character.isLowSurrogate(RuleBasedBreakIterator.this.fText.next()))) {
                        i2 = RuleBasedBreakIterator.this.handleNext();
                    }
                    i = RuleBasedBreakIterator.this.fRuleStatusIndex;
                    continue;
                }
            } while (i2 >= i3);
            this.fSideBuffer.removeAllElements();
            this.fSideBuffer.push(i2);
            this.fSideBuffer.push(i);
            while (true) {
                int i6 = RuleBasedBreakIterator.this.fPosition = i2;
                int handleNext = RuleBasedBreakIterator.this.handleNext();
                int i7 = RuleBasedBreakIterator.this.fRuleStatusIndex;
                if (handleNext == -1) {
                    break;
                }
                if (RuleBasedBreakIterator.this.fDictionaryCharCount != 0) {
                    RuleBasedBreakIterator.this.fDictionaryCache.populateDictionary(i6, handleNext, i, i7);
                    i = i7;
                    z = false;
                    while (true) {
                        if (!RuleBasedBreakIterator.this.fDictionaryCache.following(i6)) {
                            break;
                        }
                        handleNext = RuleBasedBreakIterator.this.fDictionaryCache.fBoundary;
                        i = RuleBasedBreakIterator.this.fDictionaryCache.fStatusIndex;
                        if (handleNext >= i3) {
                            z = true;
                            break;
                        }
                        this.fSideBuffer.push(handleNext);
                        this.fSideBuffer.push(i);
                        i6 = handleNext;
                        z = true;
                    }
                } else {
                    i = i7;
                    z = false;
                }
                if (!z && handleNext < i3) {
                    this.fSideBuffer.push(handleNext);
                    this.fSideBuffer.push(i);
                }
                if (handleNext >= i3) {
                    break;
                }
                i2 = handleNext;
            }
            if (!this.fSideBuffer.isEmpty()) {
                addPreceding(this.fSideBuffer.pop(), this.fSideBuffer.pop(), UpdateCachePosition);
            } else {
                z2 = false;
            }
            while (!this.fSideBuffer.isEmpty()) {
                if (!addPreceding(this.fSideBuffer.pop(), this.fSideBuffer.pop(), false)) {
                    break;
                }
            }
            return z2;
        }

        /* access modifiers changed from: package-private */
        public void addFollowing(int i, int i2, boolean z) {
            int modChunkSize = modChunkSize(this.fEndBufIdx + 1);
            int i3 = this.fStartBufIdx;
            if (modChunkSize == i3) {
                this.fStartBufIdx = modChunkSize(i3 + 6);
            }
            this.fBoundaries[modChunkSize] = i;
            this.fStatuses[modChunkSize] = (short) i2;
            this.fEndBufIdx = modChunkSize;
            if (z) {
                this.fBufIdx = modChunkSize;
                this.fTextIdx = i;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean addPreceding(int i, int i2, boolean z) {
            int modChunkSize = modChunkSize(this.fStartBufIdx - 1);
            int i3 = this.fEndBufIdx;
            if (modChunkSize == i3) {
                if (this.fBufIdx == i3 && !z) {
                    return false;
                }
                this.fEndBufIdx = modChunkSize(this.fEndBufIdx - 1);
            }
            this.fBoundaries[modChunkSize] = i;
            this.fStatuses[modChunkSize] = (short) i2;
            this.fStartBufIdx = modChunkSize;
            if (z) {
                this.fBufIdx = modChunkSize;
                this.fTextIdx = i;
            }
            return UpdateCachePosition;
        }

        /* access modifiers changed from: package-private */
        public boolean seek(int i) {
            int[] iArr = this.fBoundaries;
            int i2 = this.fStartBufIdx;
            if (i >= iArr[i2]) {
                int i3 = this.fEndBufIdx;
                if (i <= iArr[i3]) {
                    if (i == iArr[i2]) {
                        this.fBufIdx = i2;
                        this.fTextIdx = iArr[this.fBufIdx];
                        return UpdateCachePosition;
                    } else if (i == iArr[i3]) {
                        this.fBufIdx = i3;
                        this.fTextIdx = iArr[this.fBufIdx];
                        return UpdateCachePosition;
                    } else {
                        while (i2 != i3) {
                            int modChunkSize = modChunkSize(((i2 + i3) + (i2 > i3 ? 128 : 0)) / 2);
                            if (this.fBoundaries[modChunkSize] > i) {
                                i3 = modChunkSize;
                            } else {
                                i2 = modChunkSize(modChunkSize + 1);
                            }
                        }
                        this.fBufIdx = modChunkSize(i3 - 1);
                        this.fTextIdx = this.fBoundaries[this.fBufIdx];
                        return UpdateCachePosition;
                    }
                }
            }
            return false;
        }

        BreakCache(BreakCache breakCache) {
            this.fBoundaries = new int[128];
            this.fStatuses = new short[128];
            this.fSideBuffer = new DictionaryBreakEngine.DequeI();
            this.fStartBufIdx = breakCache.fStartBufIdx;
            this.fEndBufIdx = breakCache.fEndBufIdx;
            this.fTextIdx = breakCache.fTextIdx;
            this.fBufIdx = breakCache.fBufIdx;
            this.fBoundaries = (int[]) breakCache.fBoundaries.clone();
            this.fStatuses = (short[]) breakCache.fStatuses.clone();
            this.fSideBuffer = new DictionaryBreakEngine.DequeI();
        }

        /* access modifiers changed from: package-private */
        public void dumpCache() {
            System.out.printf("fTextIdx:%d   fBufIdx:%d%n", Integer.valueOf(this.fTextIdx), Integer.valueOf(this.fBufIdx));
            int i = this.fStartBufIdx;
            while (true) {
                System.out.printf("%d  %d%n", Integer.valueOf(i), Integer.valueOf(this.fBoundaries[i]));
                if (i != this.fEndBufIdx) {
                    i = modChunkSize(i + 1);
                } else {
                    return;
                }
            }
        }
    }
}
