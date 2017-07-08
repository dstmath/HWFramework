package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.CharTrie;
import android.icu.impl.CharacterIteration;
import android.icu.impl.ICUBinary;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.util.AnnualTimeZoneRule;
import dalvik.bytecode.Opcodes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.ConcurrentHashMap;
import libcore.icu.DateUtilsBridge;
import libcore.io.IoBridge;
import org.xmlpull.v1.XmlPullParser;

public class RuleBasedBreakIterator extends BreakIterator {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final String RBBI_DEBUG_ARG = "rbbi";
    private static final int RBBI_END = 2;
    private static final int RBBI_RUN = 1;
    private static final int RBBI_START = 0;
    private static final int START_STATE = 1;
    private static final int STOP_STATE = 0;
    private static final boolean TRACE = false;
    static final String fDebugEnv = null;
    private final ConcurrentHashMap<Integer, LanguageBreakEngine> fBreakEngines;
    private int fBreakType;
    private int[] fCachedBreakPositions;
    private int fDictionaryCharCount;
    private int fLastRuleStatusIndex;
    private boolean fLastStatusIndexValid;
    private int fPositionInCache;
    RBBIDataWrapper fRData;
    private CharacterIterator fText;
    private final UnhandledBreakEngine fUnhandledBreakEngine;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.RuleBasedBreakIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.RuleBasedBreakIterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RuleBasedBreakIterator.<clinit>():void");
    }

    private RuleBasedBreakIterator() {
        this.fText = new StringCharacterIterator(XmlPullParser.NO_NAMESPACE);
        this.fBreakType = RBBI_END;
        this.fUnhandledBreakEngine = new UnhandledBreakEngine();
        this.fBreakEngines = new ConcurrentHashMap();
        this.fLastStatusIndexValid = true;
        this.fDictionaryCharCount = RBBI_START;
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
            return -assertionsDisabled;
        }
        if (this == that) {
            return true;
        }
        try {
            RuleBasedBreakIterator other = (RuleBasedBreakIterator) that;
            if (this.fRData != other.fRData && (this.fRData == null || other.fRData == null)) {
                return -assertionsDisabled;
            }
            if (this.fRData != null && other.fRData != null && !this.fRData.fRuleSource.equals(other.fRData.fRuleSource)) {
                return -assertionsDisabled;
            }
            if (this.fText == null && other.fText == null) {
                return true;
            }
            if (this.fText == null || other.fText == null) {
                return -assertionsDisabled;
            }
            return this.fText.equals(other.fText);
        } catch (ClassCastException e) {
            return -assertionsDisabled;
        }
    }

    public String toString() {
        String retStr = XmlPullParser.NO_NAMESPACE;
        if (this.fRData != null) {
            return this.fRData.fRuleSource;
        }
        return retStr;
    }

    public int hashCode() {
        return this.fRData.fRuleSource.hashCode();
    }

    private void reset() {
        this.fCachedBreakPositions = null;
        this.fDictionaryCharCount = RBBI_START;
        this.fPositionInCache = RBBI_START;
    }

    @Deprecated
    public void dump() {
        this.fRData.dump();
    }

    public static void compileRules(String rules, OutputStream ruleBinary) throws IOException {
        RBBIRuleBuilder.compileRules(rules, ruleBinary);
    }

    public int first() {
        this.fCachedBreakPositions = null;
        this.fDictionaryCharCount = RBBI_START;
        this.fPositionInCache = RBBI_START;
        this.fLastRuleStatusIndex = RBBI_START;
        this.fLastStatusIndexValid = true;
        if (this.fText == null) {
            return -1;
        }
        this.fText.first();
        return this.fText.getIndex();
    }

    public int last() {
        this.fCachedBreakPositions = null;
        this.fDictionaryCharCount = RBBI_START;
        this.fPositionInCache = RBBI_START;
        if (this.fText == null) {
            this.fLastRuleStatusIndex = RBBI_START;
            this.fLastStatusIndexValid = true;
            return -1;
        }
        this.fLastStatusIndexValid = -assertionsDisabled;
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
            n += START_STATE;
        }
        return result;
    }

    public int next() {
        if (this.fCachedBreakPositions != null) {
            if (this.fPositionInCache < this.fCachedBreakPositions.length - 1) {
                this.fPositionInCache += START_STATE;
                int pos = this.fCachedBreakPositions[this.fPositionInCache];
                this.fText.setIndex(pos);
                return pos;
            }
            reset();
        }
        int startPos = current();
        this.fDictionaryCharCount = RBBI_START;
        int result = handleNext(this.fRData.fFTable);
        if (this.fDictionaryCharCount > 0) {
            result = checkDictionary(startPos, result, -assertionsDisabled);
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int checkDictionary(int startPos, int endPos, boolean reverse) {
        reset();
        if (endPos - startPos <= START_STATE) {
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
        int foundBreakCount = RBBI_START;
        int c = CharacterIteration.current32(this.fText);
        int category = (short) this.fRData.fTrie.getCodePointValue(c);
        if ((category & DateUtilsBridge.FORMAT_ABBREV_TIME) != 0) {
            if (reverse) {
                do {
                    CharacterIteration.next32(this.fText);
                    c = CharacterIteration.current32(this.fText);
                    category = (short) this.fRData.fTrie.getCodePointValue(c);
                    if (c == AnnualTimeZoneRule.MAX_YEAR) {
                        break;
                    }
                } while ((category & DateUtilsBridge.FORMAT_ABBREV_TIME) != 0);
                rangeEnd = this.fText.getIndex();
                if (c == AnnualTimeZoneRule.MAX_YEAR) {
                    c = CharacterIteration.previous32(this.fText);
                } else {
                    c = CharacterIteration.previous32(this.fText);
                }
            } else {
                do {
                    c = CharacterIteration.previous32(this.fText);
                    category = (short) this.fRData.fTrie.getCodePointValue(c);
                    if (c == AnnualTimeZoneRule.MAX_YEAR) {
                        break;
                    }
                } while ((category & DateUtilsBridge.FORMAT_ABBREV_TIME) != 0);
                if (c == AnnualTimeZoneRule.MAX_YEAR) {
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
        Object obj = null;
        while (true) {
            int current = this.fText.getIndex();
            if (current < rangeEnd && (category & DateUtilsBridge.FORMAT_ABBREV_TIME) == 0) {
                CharacterIteration.next32(this.fText);
                c = CharacterIteration.current32(this.fText);
                category = (short) this.fRData.fTrie.getCodePointValue(c);
            } else if (current >= rangeEnd) {
                break;
            } else {
                obj = getLanguageBreakEngine(c);
                if (obj != null) {
                    int startingIdx = this.fText.getIndex();
                    foundBreakCount += obj.findBreaks(this.fText, rangeStart, rangeEnd, -assertionsDisabled, this.fBreakType, breaks);
                    if (!-assertionsDisabled) {
                        if ((this.fText.getIndex() > startingIdx ? START_STATE : null) == null) {
                            break;
                        }
                    }
                }
                c = CharacterIteration.current32(this.fText);
                category = (short) this.fRData.fTrie.getCodePointValue(c);
            }
        }
        if (foundBreakCount > 0) {
            if (foundBreakCount != breaks.size()) {
                System.out.println("oops, foundBreakCount != breaks.size().  LBE = " + obj.getClass());
            }
            if (!-assertionsDisabled) {
                Object obj2;
                if (foundBreakCount == breaks.size()) {
                    obj2 = START_STATE;
                } else {
                    obj2 = null;
                }
                if (obj2 == null) {
                    throw new AssertionError();
                }
            }
            if (startPos < breaks.peekLast()) {
                breaks.offer(startPos);
            }
            if (endPos > breaks.peek()) {
                breaks.push(endPos);
            }
            this.fCachedBreakPositions = new int[breaks.size()];
            int i2 = RBBI_START;
            while (breaks.size() > 0) {
                int i3 = i2 + START_STATE;
                this.fCachedBreakPositions[i2] = breaks.pollLast();
                i2 = i3;
            }
            if (reverse) {
                return preceding(endPos);
            }
            return following(startPos);
        }
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int previous() {
        CharacterIterator text = getText();
        this.fLastStatusIndexValid = -assertionsDisabled;
        if (this.fCachedBreakPositions != null) {
            if (this.fPositionInCache > 0) {
                this.fPositionInCache--;
                if (this.fPositionInCache <= 0) {
                    this.fLastStatusIndexValid = -assertionsDisabled;
                }
                int pos = this.fCachedBreakPositions[this.fPositionInCache];
                text.setIndex(pos);
                return pos;
            }
            reset();
        }
        int startPos = current();
        if (this.fText == null || startPos == this.fText.getBeginIndex()) {
            this.fLastRuleStatusIndex = RBBI_START;
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
            r4 = lastResult;
            int lastTag = RBBI_START;
            boolean breakTagValid = -assertionsDisabled;
            while (true) {
                r4 = next();
                if (r4 == -1 || r4 >= start) {
                    this.fText.setIndex(lastResult);
                    this.fLastRuleStatusIndex = lastTag;
                    this.fLastStatusIndexValid = breakTagValid;
                } else {
                    lastResult = r4;
                    lastTag = this.fLastRuleStatusIndex;
                    breakTagValid = true;
                }
            }
            this.fText.setIndex(lastResult);
            this.fLastRuleStatusIndex = lastTag;
            this.fLastStatusIndexValid = breakTagValid;
            return lastResult;
        } else {
            r4 = handlePrevious(this.fRData.fRTable);
            if (this.fDictionaryCharCount > 0) {
                r4 = checkDictionary(r4, startPos, true);
            }
            return r4;
        }
    }

    public int following(int offset) {
        CharacterIterator text = getText();
        if (this.fCachedBreakPositions == null || offset < this.fCachedBreakPositions[RBBI_START] || offset >= this.fCachedBreakPositions[this.fCachedBreakPositions.length - 1]) {
            this.fCachedBreakPositions = null;
            return rulesFollowing(offset);
        }
        this.fPositionInCache = RBBI_START;
        while (this.fPositionInCache < this.fCachedBreakPositions.length && offset >= this.fCachedBreakPositions[this.fPositionInCache]) {
            this.fPositionInCache += START_STATE;
        }
        text.setIndex(this.fCachedBreakPositions[this.fPositionInCache]);
        return text.getIndex();
    }

    private int rulesFollowing(int offset) {
        this.fLastRuleStatusIndex = RBBI_START;
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
        if (this.fCachedBreakPositions == null || offset <= this.fCachedBreakPositions[RBBI_START] || offset > this.fCachedBreakPositions[this.fCachedBreakPositions.length - 1]) {
            this.fCachedBreakPositions = null;
            return rulesPreceding(offset);
        }
        this.fPositionInCache = RBBI_START;
        while (this.fPositionInCache < this.fCachedBreakPositions.length && offset > this.fCachedBreakPositions[this.fPositionInCache]) {
            this.fPositionInCache += START_STATE;
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
        boolean result = true;
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
            if (following(this.fText.getIndex()) != offset) {
                result = -assertionsDisabled;
            }
            return result;
        }
    }

    public int current() {
        return this.fText != null ? this.fText.getIndex() : -1;
    }

    private void makeRuleStatusValid() {
        boolean z = -assertionsDisabled;
        if (!this.fLastStatusIndexValid) {
            boolean z2;
            int curr = current();
            if (curr == -1 || curr == this.fText.getBeginIndex()) {
                this.fLastRuleStatusIndex = RBBI_START;
                this.fLastStatusIndexValid = true;
            } else {
                int pa = this.fText.getIndex();
                first();
                int pb = current();
                while (this.fText.getIndex() < pa) {
                    pb = next();
                }
                Assert.assrt(pa == pb ? true : -assertionsDisabled);
            }
            if (this.fLastStatusIndexValid) {
                z2 = true;
            } else {
                z2 = -assertionsDisabled;
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
            for (int i = RBBI_START; i < numToCopy; i += START_STATE) {
                fillInArray[i] = this.fRData.fStatusTable[(this.fLastRuleStatusIndex + i) + START_STATE];
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

    /* JADX WARNING: inconsistent code. */
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
            case IoBridge.JAVA_IP_MULTICAST_TTL /*17*/:
                if (getBreakType() != START_STATE) {
                    this.fUnhandledBreakEngine.handleChar(c, getBreakType());
                    eng = this.fUnhandledBreakEngine;
                    break;
                }
                eng = new CjkBreakEngine(-assertionsDisabled);
                break;
            case Opcodes.OP_CONST_4 /*18*/:
                if (getBreakType() != START_STATE) {
                    this.fUnhandledBreakEngine.handleChar(c, getBreakType());
                    eng = this.fUnhandledBreakEngine;
                    break;
                }
                eng = new CjkBreakEngine(true);
                break;
            case IoBridge.JAVA_MCAST_BLOCK_SOURCE /*23*/:
                eng = new KhmerBreakEngine();
                break;
            case IoBridge.JAVA_MCAST_UNBLOCK_SOURCE /*24*/:
                eng = new LaoBreakEngine();
                break;
            case Opcodes.OP_CONST_CLASS /*28*/:
                eng = new BurmeseBreakEngine();
                break;
            case Opcodes.OP_FILL_ARRAY_DATA /*38*/:
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
        this.fLastRuleStatusIndex = RBBI_START;
        CharacterIterator text = this.fText;
        CharTrie trie = this.fRData.fTrie;
        int c = text.current();
        if (c >= UTF16.SURROGATE_MIN_VALUE) {
            c = CharacterIteration.nextTrail32(text, c);
            if (c == AnnualTimeZoneRule.MAX_YEAR) {
                return -1;
            }
        }
        int initialPosition = text.getIndex();
        int result = initialPosition;
        int state = START_STATE;
        int row = this.fRData.getRowIndex(START_STATE);
        short category = (short) 3;
        int flagsState = this.fRData.getStateTableFlags(stateTable);
        int mode = START_STATE;
        if ((flagsState & RBBI_END) != 0) {
            category = (short) 2;
            mode = RBBI_START;
            if (TRACE) {
                System.out.print("            " + RBBIDataWrapper.intToString(text.getIndex(), 5));
                System.out.print(RBBIDataWrapper.intToHexString(c, 10));
                System.out.println(RBBIDataWrapper.intToString(START_STATE, 7) + RBBIDataWrapper.intToString(RBBI_END, 6));
            }
        }
        int lookaheadStatus = RBBI_START;
        int lookaheadTagIdx = RBBI_START;
        int lookaheadResult = RBBI_START;
        while (state != 0) {
            if (c == AnnualTimeZoneRule.MAX_YEAR) {
                if (mode == RBBI_END) {
                    if (lookaheadResult > result) {
                        result = lookaheadResult;
                        this.fLastRuleStatusIndex = lookaheadTagIdx;
                    }
                    if (result != initialPosition) {
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
                mode = RBBI_END;
                category = (short) 1;
            } else if (mode == START_STATE) {
                category = (short) trie.getCodePointValue(c);
                if ((category & DateUtilsBridge.FORMAT_ABBREV_TIME) != 0) {
                    this.fDictionaryCharCount += START_STATE;
                    category = (short) (category & -16385);
                }
                if (TRACE) {
                    System.out.print("            " + RBBIDataWrapper.intToString(text.getIndex(), 5));
                    System.out.print(RBBIDataWrapper.intToHexString(c, 10));
                    System.out.println(RBBIDataWrapper.intToString(state, 7) + RBBIDataWrapper.intToString(category, 6));
                }
                c = text.next();
                if (c >= UTF16.SURROGATE_MIN_VALUE) {
                    c = CharacterIteration.nextTrail32(text, c);
                }
            } else {
                mode = START_STATE;
            }
            state = stateTable[(row + 4) + category];
            row = this.fRData.getRowIndex(state);
            if (stateTable[row + RBBI_START] == (short) -1) {
                result = text.getIndex();
                if (c >= DateUtilsBridge.FORMAT_ABBREV_MONTH && c <= UnicodeSet.MAX_VALUE) {
                    result--;
                }
                this.fLastRuleStatusIndex = stateTable[row + RBBI_END];
            }
            if (stateTable[row + START_STATE] != (short) 0) {
                if (lookaheadStatus == 0 || stateTable[row + RBBI_START] != lookaheadStatus) {
                    lookaheadResult = text.getIndex();
                    if (c >= DateUtilsBridge.FORMAT_ABBREV_MONTH && c <= UnicodeSet.MAX_VALUE) {
                        lookaheadResult--;
                    }
                    lookaheadStatus = stateTable[row + START_STATE];
                    lookaheadTagIdx = stateTable[row + RBBI_END];
                } else {
                    result = lookaheadResult;
                    this.fLastRuleStatusIndex = lookaheadTagIdx;
                    lookaheadStatus = RBBI_START;
                    if ((flagsState & START_STATE) != 0) {
                        text.setIndex(result);
                        return result;
                    }
                }
            } else if (stateTable[row + RBBI_START] != (short) 0) {
                lookaheadStatus = RBBI_START;
            }
        }
        if (result != initialPosition) {
            text.setIndex(result);
        } else {
            if (TRACE) {
                System.out.println("Iterator did not move. Advancing by 1.");
            }
            text.setIndex(initialPosition);
            CharacterIteration.next32(text);
            result = text.getIndex();
        }
        if (TRACE) {
            System.out.println("result = " + result);
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int handlePrevious(short[] stateTable) {
        if (this.fText == null || stateTable == null) {
            return RBBI_START;
        }
        int lookaheadStatus = RBBI_START;
        int lookaheadResult = RBBI_START;
        boolean lookAheadHardBreak = (this.fRData.getStateTableFlags(stateTable) & START_STATE) != 0 ? true : -assertionsDisabled;
        this.fLastStatusIndexValid = -assertionsDisabled;
        this.fLastRuleStatusIndex = RBBI_START;
        int initialPosition = this.fText.getIndex();
        int result = initialPosition;
        int c = CharacterIteration.previous32(this.fText);
        int state = START_STATE;
        int row = this.fRData.getRowIndex(START_STATE);
        int category = 3;
        int mode = START_STATE;
        if ((this.fRData.getStateTableFlags(stateTable) & RBBI_END) != 0) {
            category = RBBI_END;
            mode = RBBI_START;
        }
        if (TRACE) {
            System.out.println("Handle Prev   pos   char  state category ");
        }
        while (true) {
            if (c == AnnualTimeZoneRule.MAX_YEAR) {
                if (mode != RBBI_END && this.fRData.fHeader.fVersion != START_STATE) {
                    mode = RBBI_END;
                    category = START_STATE;
                } else if (lookaheadResult < result) {
                    result = lookaheadResult;
                } else if (result == initialPosition) {
                    this.fText.setIndex(initialPosition);
                    CharacterIteration.previous32(this.fText);
                }
            }
            if (mode == START_STATE) {
                category = (short) this.fRData.fTrie.getCodePointValue(c);
                if ((category & DateUtilsBridge.FORMAT_ABBREV_TIME) != 0) {
                    this.fDictionaryCharCount += START_STATE;
                    category &= -16385;
                }
            }
            if (TRACE) {
                System.out.print("             " + this.fText.getIndex() + "   ");
                if (32 > c || c >= Opcodes.OP_NEG_FLOAT) {
                    System.out.print(" " + Integer.toHexString(c) + " ");
                } else {
                    System.out.print("  " + c + "  ");
                }
                System.out.println(" " + state + "  " + category + " ");
            }
            state = stateTable[(row + 4) + category];
            row = this.fRData.getRowIndex(state);
            if (stateTable[row + RBBI_START] == (short) -1) {
                result = this.fText.getIndex();
            }
            if (stateTable[row + START_STATE] != (short) 0) {
                if (lookaheadStatus != 0 && stateTable[row + RBBI_START] == lookaheadStatus) {
                    result = lookaheadResult;
                    lookaheadStatus = RBBI_START;
                    if (lookAheadHardBreak) {
                        break;
                    }
                }
                lookaheadResult = this.fText.getIndex();
                lookaheadStatus = stateTable[row + START_STATE];
            } else if (!(stateTable[row + RBBI_START] == (short) 0 || lookAheadHardBreak)) {
                lookaheadStatus = RBBI_START;
            }
            if (state == 0) {
                break;
            } else if (mode == START_STATE) {
                c = CharacterIteration.previous32(this.fText);
            } else if (mode == 0) {
                mode = START_STATE;
            }
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
