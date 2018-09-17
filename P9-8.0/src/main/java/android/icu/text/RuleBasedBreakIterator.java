package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.CharTrie;
import android.icu.impl.CharacterIteration;
import android.icu.impl.ICUBinary;
import android.icu.impl.ICUDebug;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.ConcurrentHashMap;

public class RuleBasedBreakIterator extends BreakIterator {
    static final /* synthetic */ boolean -assertionsDisabled = (RuleBasedBreakIterator.class.desiredAssertionStatus() ^ 1);
    private static final String RBBI_DEBUG_ARG = "rbbi";
    private static final int RBBI_END = 2;
    private static final int RBBI_RUN = 1;
    private static final int RBBI_START = 0;
    private static final int START_STATE = 1;
    private static final int STOP_STATE = 0;
    private static final boolean TRACE;
    static final String fDebugEnv = (ICUDebug.enabled(RBBI_DEBUG_ARG) ? ICUDebug.value(RBBI_DEBUG_ARG) : null);
    private static final int kMaxLookaheads = 8;
    private final ConcurrentHashMap<Integer, LanguageBreakEngine> fBreakEngines;
    private int fBreakType;
    private int[] fCachedBreakPositions;
    private int fDictionaryCharCount;
    private int fLastRuleStatusIndex;
    private boolean fLastStatusIndexValid;
    private LookAheadResults fLookAheadMatches;
    private int fPositionInCache;
    RBBIDataWrapper fRData;
    private CharacterIterator fText;
    private final UnhandledBreakEngine fUnhandledBreakEngine;

    private static class LookAheadResults {
        static final /* synthetic */ boolean -assertionsDisabled = (LookAheadResults.class.desiredAssertionStatus() ^ 1);
        int[] fKeys = new int[8];
        int[] fPositions = new int[8];
        int fUsedSlotLimit = 0;

        LookAheadResults() {
        }

        int getPosition(int key) {
            for (int i = 0; i < this.fUsedSlotLimit; i++) {
                if (this.fKeys[i] == key) {
                    return this.fPositions[i];
                }
            }
            if (-assertionsDisabled) {
                return -1;
            }
            throw new AssertionError();
        }

        void setPosition(int key, int position) {
            int i = 0;
            while (i < this.fUsedSlotLimit) {
                if (this.fKeys[i] == key) {
                    this.fPositions[i] = position;
                    return;
                }
                i++;
            }
            if (i >= 8) {
                if (-assertionsDisabled) {
                    i = 7;
                } else {
                    throw new AssertionError();
                }
            }
            this.fKeys[i] = key;
            this.fPositions[i] = position;
            if (-assertionsDisabled || this.fUsedSlotLimit == i) {
                this.fUsedSlotLimit = i + 1;
                return;
            }
            throw new AssertionError();
        }

        void reset() {
            this.fUsedSlotLimit = 0;
        }
    }

    private RuleBasedBreakIterator() {
        this.fText = new StringCharacterIterator("");
        this.fBreakType = 2;
        this.fUnhandledBreakEngine = new UnhandledBreakEngine();
        this.fBreakEngines = new ConcurrentHashMap();
        this.fLookAheadMatches = new LookAheadResults();
        this.fLastStatusIndexValid = true;
        this.fDictionaryCharCount = 0;
        this.fBreakEngines.put(Integer.valueOf(-1), this.fUnhandledBreakEngine);
    }

    public static RuleBasedBreakIterator getInstanceFromCompiledRules(InputStream is) throws IOException {
        RuleBasedBreakIterator This = new RuleBasedBreakIterator();
        This.fRData = RBBIDataWrapper.get(ICUBinary.getByteBufferFromInputStreamAndCloseStream(is));
        return This;
    }

    @Deprecated
    public static RuleBasedBreakIterator getInstanceFromCompiledRules(ByteBuffer bytes) throws IOException {
        RuleBasedBreakIterator This = new RuleBasedBreakIterator();
        This.fRData = RBBIDataWrapper.get(bytes);
        return This;
    }

    public RuleBasedBreakIterator(String rules) {
        this();
        try {
            ByteArrayOutputStream ruleOS = new ByteArrayOutputStream();
            compileRules(rules, ruleOS);
            this.fRData = RBBIDataWrapper.get(ByteBuffer.wrap(ruleOS.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("RuleBasedBreakIterator rule compilation internal error: " + e.getMessage());
        }
    }

    public Object clone() {
        RuleBasedBreakIterator result = (RuleBasedBreakIterator) super.clone();
        if (this.fText != null) {
            result.fText = (CharacterIterator) this.fText.clone();
        }
        return result;
    }

    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }
        try {
            RuleBasedBreakIterator other = (RuleBasedBreakIterator) that;
            if (this.fRData != other.fRData && (this.fRData == null || other.fRData == null)) {
                return false;
            }
            if (this.fRData != null && other.fRData != null && (this.fRData.fRuleSource.equals(other.fRData.fRuleSource) ^ 1) != 0) {
                return false;
            }
            if (this.fText == null && other.fText == null) {
                return true;
            }
            if (this.fText == null || other.fText == null) {
                return false;
            }
            return this.fText.equals(other.fText);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String retStr = "";
        if (this.fRData != null) {
            return this.fRData.fRuleSource;
        }
        return retStr;
    }

    public int hashCode() {
        return this.fRData.fRuleSource.hashCode();
    }

    static {
        boolean z = false;
        if (ICUDebug.enabled(RBBI_DEBUG_ARG) && ICUDebug.value(RBBI_DEBUG_ARG).indexOf("trace") >= 0) {
            z = true;
        }
        TRACE = z;
    }

    private void reset() {
        this.fCachedBreakPositions = null;
        this.fDictionaryCharCount = 0;
        this.fPositionInCache = 0;
    }

    @Deprecated
    public void dump(PrintStream out) {
        if (out == null) {
            out = System.out;
        }
        this.fRData.dump(out);
    }

    public static void compileRules(String rules, OutputStream ruleBinary) throws IOException {
        RBBIRuleBuilder.compileRules(rules, ruleBinary);
    }

    public int first() {
        this.fCachedBreakPositions = null;
        this.fDictionaryCharCount = 0;
        this.fPositionInCache = 0;
        this.fLastRuleStatusIndex = 0;
        this.fLastStatusIndexValid = true;
        if (this.fText == null) {
            return -1;
        }
        this.fText.first();
        return this.fText.getIndex();
    }

    public int last() {
        this.fCachedBreakPositions = null;
        this.fDictionaryCharCount = 0;
        this.fPositionInCache = 0;
        if (this.fText == null) {
            this.fLastRuleStatusIndex = 0;
            this.fLastStatusIndexValid = true;
            return -1;
        }
        this.fLastStatusIndexValid = false;
        int pos = this.fText.getEndIndex();
        this.fText.setIndex(pos);
        return pos;
    }

    public int next(int n) {
        int result = current();
        while (n > 0) {
            result = next();
            n--;
        }
        while (n < 0) {
            result = previous();
            n++;
        }
        return result;
    }

    public int next() {
        if (this.fCachedBreakPositions != null) {
            if (this.fPositionInCache < this.fCachedBreakPositions.length - 1) {
                this.fPositionInCache++;
                int pos = this.fCachedBreakPositions[this.fPositionInCache];
                this.fText.setIndex(pos);
                return pos;
            }
            reset();
        }
        int startPos = current();
        this.fDictionaryCharCount = 0;
        int result = handleNext(this.fRData.fFTable);
        if (this.fDictionaryCharCount > 0) {
            result = checkDictionary(startPos, result, false);
        }
        return result;
    }

    private int checkDictionary(int startPos, int endPos, boolean reverse) {
        reset();
        if (endPos - startPos <= 1) {
            if (!reverse) {
                startPos = endPos;
            }
            return startPos;
        }
        int i;
        CharacterIterator characterIterator = this.fText;
        if (reverse) {
            i = endPos;
        } else {
            i = startPos;
        }
        characterIterator.setIndex(i);
        if (reverse) {
            CharacterIteration.previous32(this.fText);
        }
        int rangeStart = startPos;
        int rangeEnd = endPos;
        DequeI breaks = new DequeI();
        int foundBreakCount = 0;
        int c = CharacterIteration.current32(this.fText);
        int category = (short) this.fRData.fTrie.getCodePointValue(c);
        if ((category & 16384) != 0) {
            if (reverse) {
                do {
                    CharacterIteration.next32(this.fText);
                    c = CharacterIteration.current32(this.fText);
                    category = (short) this.fRData.fTrie.getCodePointValue(c);
                    if (c == Integer.MAX_VALUE) {
                        break;
                    }
                } while ((category & 16384) != 0);
                rangeEnd = this.fText.getIndex();
                if (c == Integer.MAX_VALUE) {
                    c = CharacterIteration.previous32(this.fText);
                } else {
                    c = CharacterIteration.previous32(this.fText);
                }
            } else {
                do {
                    c = CharacterIteration.previous32(this.fText);
                    category = (short) this.fRData.fTrie.getCodePointValue(c);
                    if (c == Integer.MAX_VALUE) {
                        break;
                    }
                } while ((category & 16384) != 0);
                if (c == Integer.MAX_VALUE) {
                    c = CharacterIteration.current32(this.fText);
                } else {
                    CharacterIteration.next32(this.fText);
                    c = CharacterIteration.current32(this.fText);
                }
                rangeStart = this.fText.getIndex();
            }
            category = (short) this.fRData.fTrie.getCodePointValue(c);
        }
        if (reverse) {
            this.fText.setIndex(rangeStart);
            c = CharacterIteration.current32(this.fText);
            category = (short) this.fRData.fTrie.getCodePointValue(c);
        }
        Object lbe = null;
        while (true) {
            int current = this.fText.getIndex();
            if (current < rangeEnd && (category & 16384) == 0) {
                CharacterIteration.next32(this.fText);
                c = CharacterIteration.current32(this.fText);
                category = (short) this.fRData.fTrie.getCodePointValue(c);
            } else if (current < rangeEnd) {
                lbe = getLanguageBreakEngine(c);
                if (lbe != null) {
                    int startingIdx = this.fText.getIndex();
                    foundBreakCount += lbe.findBreaks(this.fText, rangeStart, rangeEnd, false, this.fBreakType, breaks);
                    if (!-assertionsDisabled && this.fText.getIndex() <= startingIdx) {
                        throw new AssertionError();
                    }
                }
                c = CharacterIteration.current32(this.fText);
                category = (short) this.fRData.fTrie.getCodePointValue(c);
            } else if (foundBreakCount > 0) {
                if (foundBreakCount != breaks.size()) {
                    System.out.println("oops, foundBreakCount != breaks.size().  LBE = " + lbe.getClass());
                }
                if (-assertionsDisabled || foundBreakCount == breaks.size()) {
                    if (startPos < breaks.peekLast()) {
                        breaks.offer(startPos);
                    }
                    if (endPos > breaks.peek()) {
                        breaks.push(endPos);
                    }
                    this.fCachedBreakPositions = new int[breaks.size()];
                    int i2 = 0;
                    while (breaks.size() > 0) {
                        int i3 = i2 + 1;
                        this.fCachedBreakPositions[i2] = breaks.pollLast();
                        i2 = i3;
                    }
                    if (reverse) {
                        return preceding(endPos);
                    }
                    return following(startPos);
                }
                throw new AssertionError();
            } else {
                characterIterator = this.fText;
                if (reverse) {
                    i = startPos;
                } else {
                    i = endPos;
                }
                characterIterator.setIndex(i);
                if (!reverse) {
                    startPos = endPos;
                }
                return startPos;
            }
        }
    }

    public int previous() {
        CharacterIterator text = getText();
        this.fLastStatusIndexValid = false;
        if (this.fCachedBreakPositions != null) {
            if (this.fPositionInCache > 0) {
                this.fPositionInCache--;
                if (this.fPositionInCache <= 0) {
                    this.fLastStatusIndexValid = false;
                }
                int pos = this.fCachedBreakPositions[this.fPositionInCache];
                text.setIndex(pos);
                return pos;
            }
            reset();
        }
        int startPos = current();
        int i;
        if (this.fText == null || startPos == this.fText.getBeginIndex()) {
            this.fLastRuleStatusIndex = 0;
            this.fLastStatusIndexValid = true;
            return -1;
        } else if (this.fRData.fSRTable == null && this.fRData.fSFTable == null) {
            int start = current();
            CharacterIteration.previous32(this.fText);
            int lastResult = handlePrevious(this.fRData.fRTable);
            if (lastResult == -1) {
                lastResult = this.fText.getBeginIndex();
                this.fText.setIndex(lastResult);
            }
            i = lastResult;
            int lastTag = 0;
            boolean breakTagValid = false;
            while (true) {
                i = next();
                if (i == -1 || i >= start) {
                    this.fText.setIndex(lastResult);
                    this.fLastRuleStatusIndex = lastTag;
                    this.fLastStatusIndexValid = breakTagValid;
                } else {
                    lastResult = i;
                    lastTag = this.fLastRuleStatusIndex;
                    breakTagValid = true;
                }
            }
            this.fText.setIndex(lastResult);
            this.fLastRuleStatusIndex = lastTag;
            this.fLastStatusIndexValid = breakTagValid;
            return lastResult;
        } else {
            i = handlePrevious(this.fRData.fRTable);
            if (this.fDictionaryCharCount > 0) {
                i = checkDictionary(i, startPos, true);
            }
            return i;
        }
    }

    public int following(int offset) {
        CharacterIterator text = getText();
        if (this.fCachedBreakPositions == null || offset < this.fCachedBreakPositions[0] || offset >= this.fCachedBreakPositions[this.fCachedBreakPositions.length - 1]) {
            this.fCachedBreakPositions = null;
            return rulesFollowing(offset);
        }
        this.fPositionInCache = 0;
        while (this.fPositionInCache < this.fCachedBreakPositions.length && offset >= this.fCachedBreakPositions[this.fPositionInCache]) {
            this.fPositionInCache++;
        }
        text.setIndex(this.fCachedBreakPositions[this.fPositionInCache]);
        return text.getIndex();
    }

    private int rulesFollowing(int offset) {
        this.fLastRuleStatusIndex = 0;
        this.fLastStatusIndexValid = true;
        if (this.fText == null || offset >= this.fText.getEndIndex()) {
            last();
            return next();
        } else if (offset < this.fText.getBeginIndex()) {
            return first();
        } else {
            int result;
            if (this.fRData.fSRTable != null) {
                this.fText.setIndex(offset);
                CharacterIteration.next32(this.fText);
                handlePrevious(this.fRData.fSRTable);
                result = next();
                while (result <= offset) {
                    result = next();
                }
                return result;
            } else if (this.fRData.fSFTable != null) {
                this.fText.setIndex(offset);
                CharacterIteration.previous32(this.fText);
                handleNext(this.fRData.fSFTable);
                int oldresult = previous();
                while (oldresult > offset) {
                    result = previous();
                    if (result <= offset) {
                        return oldresult;
                    }
                    oldresult = result;
                }
                result = next();
                if (result <= offset) {
                    return next();
                }
                return result;
            } else {
                this.fText.setIndex(offset);
                if (offset == this.fText.getBeginIndex()) {
                    return next();
                }
                result = previous();
                while (result != -1 && result <= offset) {
                    result = next();
                }
                return result;
            }
        }
    }

    public int preceding(int offset) {
        CharacterIterator text = getText();
        if (this.fCachedBreakPositions == null || offset <= this.fCachedBreakPositions[0] || offset > this.fCachedBreakPositions[this.fCachedBreakPositions.length - 1]) {
            this.fCachedBreakPositions = null;
            return rulesPreceding(offset);
        }
        this.fPositionInCache = 0;
        while (this.fPositionInCache < this.fCachedBreakPositions.length && offset > this.fCachedBreakPositions[this.fPositionInCache]) {
            this.fPositionInCache++;
        }
        this.fPositionInCache--;
        text.setIndex(this.fCachedBreakPositions[this.fPositionInCache]);
        return text.getIndex();
    }

    private int rulesPreceding(int offset) {
        if (this.fText == null || offset > this.fText.getEndIndex()) {
            return last();
        }
        if (offset < this.fText.getBeginIndex()) {
            return first();
        }
        int result;
        if (this.fRData.fSFTable != null) {
            this.fText.setIndex(offset);
            CharacterIteration.previous32(this.fText);
            handleNext(this.fRData.fSFTable);
            result = previous();
            while (result >= offset) {
                result = previous();
            }
            return result;
        } else if (this.fRData.fSRTable != null) {
            this.fText.setIndex(offset);
            CharacterIteration.next32(this.fText);
            handlePrevious(this.fRData.fSRTable);
            int oldresult = next();
            while (oldresult < offset) {
                result = next();
                if (result >= offset) {
                    return oldresult;
                }
                oldresult = result;
            }
            result = previous();
            if (result >= offset) {
                return previous();
            }
            return result;
        } else {
            this.fText.setIndex(offset);
            return previous();
        }
    }

    protected static final void checkOffset(int offset, CharacterIterator text) {
        if (offset < text.getBeginIndex() || offset > text.getEndIndex()) {
            throw new IllegalArgumentException("offset out of bounds");
        }
    }

    public boolean isBoundary(int offset) {
        checkOffset(offset, this.fText);
        if (offset == this.fText.getBeginIndex()) {
            first();
            return true;
        } else if (offset == this.fText.getEndIndex()) {
            last();
            return true;
        } else {
            this.fText.setIndex(offset);
            CharacterIteration.previous32(this.fText);
            return following(this.fText.getIndex()) == offset;
        }
    }

    public int current() {
        return this.fText != null ? this.fText.getIndex() : -1;
    }

    private void makeRuleStatusValid() {
        boolean z = false;
        if (!this.fLastStatusIndexValid) {
            boolean z2;
            int curr = current();
            if (curr == -1 || curr == this.fText.getBeginIndex()) {
                this.fLastRuleStatusIndex = 0;
                this.fLastStatusIndexValid = true;
            } else {
                int pa = this.fText.getIndex();
                first();
                int pb = current();
                while (this.fText.getIndex() < pa) {
                    pb = next();
                }
                Assert.assrt(pa == pb);
            }
            if (this.fLastStatusIndexValid) {
                z2 = true;
            } else {
                z2 = false;
            }
            Assert.assrt(z2);
            if (this.fLastRuleStatusIndex >= 0 && this.fLastRuleStatusIndex < this.fRData.fStatusTable.length) {
                z = true;
            }
            Assert.assrt(z);
        }
    }

    public int getRuleStatus() {
        makeRuleStatusValid();
        return this.fRData.fStatusTable[this.fLastRuleStatusIndex + this.fRData.fStatusTable[this.fLastRuleStatusIndex]];
    }

    public int getRuleStatusVec(int[] fillInArray) {
        makeRuleStatusValid();
        int numStatusVals = this.fRData.fStatusTable[this.fLastRuleStatusIndex];
        if (fillInArray != null) {
            int numToCopy = Math.min(numStatusVals, fillInArray.length);
            for (int i = 0; i < numToCopy; i++) {
                fillInArray[i] = this.fRData.fStatusTable[(this.fLastRuleStatusIndex + i) + 1];
            }
        }
        return numStatusVals;
    }

    public CharacterIterator getText() {
        return this.fText;
    }

    public void setText(CharacterIterator newText) {
        this.fText = newText;
        first();
    }

    void setBreakType(int type) {
        this.fBreakType = type;
    }

    int getBreakType() {
        return this.fBreakType;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private LanguageBreakEngine getLanguageBreakEngine(int c) {
        for (LanguageBreakEngine candidate : this.fBreakEngines.values()) {
            if (candidate.handles(c, this.fBreakType)) {
                return candidate;
            }
        }
        int script = UCharacter.getIntPropertyValue(c, UProperty.SCRIPT);
        if (script == 22 || script == 20) {
            script = 17;
        }
        LanguageBreakEngine eng = (LanguageBreakEngine) this.fBreakEngines.get(Integer.valueOf(script));
        switch (script) {
            case 17:
                if (getBreakType() != 1) {
                    this.fUnhandledBreakEngine.handleChar(c, getBreakType());
                    eng = this.fUnhandledBreakEngine;
                    break;
                }
                eng = new CjkBreakEngine(false);
                break;
            case 18:
                if (getBreakType() != 1) {
                    this.fUnhandledBreakEngine.handleChar(c, getBreakType());
                    eng = this.fUnhandledBreakEngine;
                    break;
                }
                eng = new CjkBreakEngine(true);
                break;
            case 23:
                eng = new KhmerBreakEngine();
                break;
            case 24:
                eng = new LaoBreakEngine();
                break;
            case 28:
                eng = new BurmeseBreakEngine();
                break;
            case 38:
                eng = new ThaiBreakEngine();
                break;
            default:
                try {
                    this.fUnhandledBreakEngine.handleChar(c, getBreakType());
                    eng = this.fUnhandledBreakEngine;
                    break;
                } catch (IOException e) {
                    eng = null;
                    break;
                }
        }
        if (!(eng == null || eng == this.fUnhandledBreakEngine)) {
            LanguageBreakEngine existingEngine = (LanguageBreakEngine) this.fBreakEngines.putIfAbsent(Integer.valueOf(script), eng);
            if (existingEngine != null) {
                eng = existingEngine;
            }
        }
        return eng;
    }

    private int handleNext(short[] stateTable) {
        if (TRACE) {
            System.out.println("Handle Next   pos      char  state category");
        }
        this.fLastStatusIndexValid = true;
        this.fLastRuleStatusIndex = 0;
        CharacterIterator text = this.fText;
        CharTrie trie = this.fRData.fTrie;
        int c = text.current();
        if (c >= 55296) {
            c = CharacterIteration.nextTrail32(text, c);
            if (c == Integer.MAX_VALUE) {
                return -1;
            }
        }
        int initialPosition = text.getIndex();
        int result = initialPosition;
        int state = 1;
        int row = this.fRData.getRowIndex(1);
        short category = (short) 3;
        int mode = 1;
        if ((this.fRData.getStateTableFlags(stateTable) & 2) != 0) {
            category = (short) 2;
            mode = 0;
            if (TRACE) {
                System.out.print("            " + RBBIDataWrapper.intToString(text.getIndex(), 5));
                System.out.print(RBBIDataWrapper.intToHexString(c, 10));
                System.out.println(RBBIDataWrapper.intToString(1, 7) + RBBIDataWrapper.intToString(2, 6));
            }
        }
        this.fLookAheadMatches.reset();
        while (state != 0) {
            if (c == Integer.MAX_VALUE) {
                if (mode == 2) {
                    break;
                }
                mode = 2;
                category = (short) 1;
            } else if (mode == 1) {
                category = (short) trie.getCodePointValue(c);
                if ((category & 16384) != 0) {
                    this.fDictionaryCharCount++;
                    category = (short) (category & -16385);
                }
                if (TRACE) {
                    System.out.print("            " + RBBIDataWrapper.intToString(text.getIndex(), 5));
                    System.out.print(RBBIDataWrapper.intToHexString(c, 10));
                    System.out.println(RBBIDataWrapper.intToString(state, 7) + RBBIDataWrapper.intToString(category, 6));
                }
                c = text.next();
                if (c >= 55296) {
                    c = CharacterIteration.nextTrail32(text, c);
                }
            } else {
                mode = 1;
            }
            state = stateTable[(row + 4) + category];
            row = this.fRData.getRowIndex(state);
            if (stateTable[row + 0] == (short) -1) {
                result = text.getIndex();
                if (c >= 65536 && c <= 1114111) {
                    result--;
                }
                this.fLastRuleStatusIndex = stateTable[row + 2];
            }
            int completedRule = stateTable[row + 0];
            if (completedRule > 0) {
                int lookaheadResult = this.fLookAheadMatches.getPosition(completedRule);
                if (lookaheadResult >= 0) {
                    this.fLastRuleStatusIndex = stateTable[row + 2];
                    text.setIndex(lookaheadResult);
                    return lookaheadResult;
                }
            }
            int rule = stateTable[row + 1];
            if (rule != 0) {
                int pos = text.getIndex();
                if (c >= 65536 && c <= 1114111) {
                    pos--;
                }
                this.fLookAheadMatches.setPosition(rule, pos);
            }
        }
        if (result == initialPosition) {
            if (TRACE) {
                System.out.println("Iterator did not move. Advancing by 1.");
            }
            text.setIndex(initialPosition);
            CharacterIteration.next32(text);
            result = text.getIndex();
        } else {
            text.setIndex(result);
        }
        if (TRACE) {
            System.out.println("result = " + result);
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0057  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int handlePrevious(short[] stateTable) {
        if (this.fText == null || stateTable == null) {
            return 0;
        }
        this.fLookAheadMatches.reset();
        this.fLastStatusIndexValid = false;
        this.fLastRuleStatusIndex = 0;
        int initialPosition = this.fText.getIndex();
        int result = initialPosition;
        int c = CharacterIteration.previous32(this.fText);
        int state = 1;
        int row = this.fRData.getRowIndex(1);
        int category = 3;
        int mode = 1;
        if ((this.fRData.getStateTableFlags(stateTable) & 2) != 0) {
            category = 2;
            mode = 0;
        }
        if (TRACE) {
            System.out.println("Handle Prev   pos   char  state category ");
        }
        while (true) {
            if (c == Integer.MAX_VALUE) {
                if (mode != 2 && this.fRData.fHeader.fVersion != 1) {
                    mode = 2;
                    category = 1;
                } else if (result == initialPosition) {
                    this.fText.setIndex(initialPosition);
                    CharacterIteration.previous32(this.fText);
                }
            }
            if (mode == 1) {
                category = (short) this.fRData.fTrie.getCodePointValue(c);
                if ((category & 16384) != 0) {
                    this.fDictionaryCharCount++;
                    category &= -16385;
                }
            }
            if (TRACE) {
                System.out.print("             " + this.fText.getIndex() + "   ");
                if (32 > c || c >= 127) {
                    System.out.print(" " + Integer.toHexString(c) + " ");
                } else {
                    System.out.print("  " + c + "  ");
                }
                System.out.println(" " + state + "  " + category + " ");
            }
            state = stateTable[(row + 4) + category];
            row = this.fRData.getRowIndex(state);
            if (stateTable[row + 0] == (short) -1) {
                result = this.fText.getIndex();
            }
            int completedRule = stateTable[row + 0];
            if (completedRule > 0) {
                int lookaheadResult = this.fLookAheadMatches.getPosition(completedRule);
                if (lookaheadResult >= 0) {
                    result = lookaheadResult;
                    break;
                }
            }
            int rule = stateTable[row + 1];
            if (rule != 0) {
                this.fLookAheadMatches.setPosition(rule, this.fText.getIndex());
            }
            if (state == 0) {
                break;
            } else if (mode == 1) {
                c = CharacterIteration.previous32(this.fText);
            } else if (mode == 0) {
                mode = 1;
            }
        }
        if (result == initialPosition) {
        }
        if (result == initialPosition) {
            result = this.fText.setIndex(initialPosition);
            CharacterIteration.previous32(this.fText);
            result = this.fText.getIndex();
        }
        this.fText.setIndex(result);
        if (TRACE) {
            System.out.println("Result = " + result);
        }
        return result;
    }
}
