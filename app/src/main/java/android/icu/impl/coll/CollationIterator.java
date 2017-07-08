package android.icu.impl.coll;

import android.icu.impl.Grego;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.impl.Trie2_32;
import android.icu.text.UTF16;
import android.icu.util.BytesTrie.Result;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrie.State;
import android.icu.util.ICUException;
import dalvik.bytecode.Opcodes;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public abstract class CollationIterator {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    protected static final long NO_CP_AND_CE32 = -4294967104L;
    private CEBuffer ceBuffer;
    private int cesIndex;
    protected final CollationData data;
    private boolean isNumeric;
    private int numCpFwd;
    private SkippedState skipped;
    protected final Trie2_32 trie;

    private static final class CEBuffer {
        private static final int INITIAL_CAPACITY = 40;
        private long[] buffer;
        int length;

        CEBuffer() {
            this.length = 0;
            this.buffer = new long[INITIAL_CAPACITY];
        }

        void append(long ce) {
            if (this.length >= INITIAL_CAPACITY) {
                ensureAppendCapacity(1);
            }
            long[] jArr = this.buffer;
            int i = this.length;
            this.length = i + 1;
            jArr[i] = ce;
        }

        void appendUnsafe(long ce) {
            long[] jArr = this.buffer;
            int i = this.length;
            this.length = i + 1;
            jArr[i] = ce;
        }

        void ensureAppendCapacity(int appCap) {
            int capacity = this.buffer.length;
            if (this.length + appCap > capacity) {
                do {
                    if (capacity < Grego.MILLIS_PER_SECOND) {
                        capacity *= 4;
                    } else {
                        capacity *= 2;
                    }
                } while (capacity < this.length + appCap);
                long[] newBuffer = new long[capacity];
                System.arraycopy(this.buffer, 0, newBuffer, 0, this.length);
                this.buffer = newBuffer;
            }
        }

        void incLength() {
            if (this.length >= INITIAL_CAPACITY) {
                ensureAppendCapacity(1);
            }
            this.length++;
        }

        long set(int i, long ce) {
            this.buffer[i] = ce;
            return ce;
        }

        long get(int i) {
            return this.buffer[i];
        }

        long[] getCEs() {
            return this.buffer;
        }
    }

    private static final class SkippedState {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private final StringBuilder newBuffer;
        private final StringBuilder oldBuffer;
        private int pos;
        private int skipLengthAtMatch;
        private State state;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationIterator.SkippedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationIterator.SkippedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationIterator.SkippedState.<clinit>():void");
        }

        SkippedState() {
            this.oldBuffer = new StringBuilder();
            this.newBuffer = new StringBuilder();
            this.state = new State();
        }

        void clear() {
            this.oldBuffer.setLength(0);
            this.pos = 0;
        }

        boolean isEmpty() {
            return this.oldBuffer.length() == 0 ? true : CollationIterator.-assertionsDisabled;
        }

        boolean hasNext() {
            return this.pos < this.oldBuffer.length() ? true : CollationIterator.-assertionsDisabled;
        }

        int next() {
            int c = this.oldBuffer.codePointAt(this.pos);
            this.pos += Character.charCount(c);
            return c;
        }

        void incBeyond() {
            if (!-assertionsDisabled) {
                if ((hasNext() ? null : 1) == null) {
                    throw new AssertionError();
                }
            }
            this.pos++;
        }

        int backwardNumCodePoints(int n) {
            int length = this.oldBuffer.length();
            int beyond = this.pos - length;
            if (beyond <= 0) {
                this.pos = this.oldBuffer.offsetByCodePoints(this.pos, -n);
                return 0;
            } else if (beyond >= n) {
                this.pos -= n;
                return n;
            } else {
                this.pos = this.oldBuffer.offsetByCodePoints(length, beyond - n);
                return beyond;
            }
        }

        void setFirstSkipped(int c) {
            this.skipLengthAtMatch = 0;
            this.newBuffer.setLength(0);
            this.newBuffer.appendCodePoint(c);
        }

        void skip(int c) {
            this.newBuffer.appendCodePoint(c);
        }

        void recordMatch() {
            this.skipLengthAtMatch = this.newBuffer.length();
        }

        void replaceMatch() {
            int oldLength = this.oldBuffer.length();
            if (this.pos > oldLength) {
                this.pos = oldLength;
            }
            this.oldBuffer.delete(0, this.pos).insert(0, this.newBuffer, 0, this.skipLengthAtMatch);
            this.pos = 0;
        }

        void saveTrieState(CharsTrie trie) {
            trie.saveState(this.state);
        }

        void resetToTrieState(CharsTrie trie) {
            trie.resetToState(this.state);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationIterator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationIterator.<clinit>():void");
    }

    protected abstract void backwardNumCodePoints(int i);

    protected abstract void forwardNumCodePoints(int i);

    public abstract int getOffset();

    public abstract int nextCodePoint();

    public abstract int previousCodePoint();

    public abstract void resetToOffset(int i);

    public CollationIterator(CollationData d) {
        this.trie = d.trie;
        this.data = d;
        this.numCpFwd = -1;
        this.isNumeric = -assertionsDisabled;
        this.ceBuffer = null;
    }

    public CollationIterator(CollationData d, boolean numeric) {
        this.trie = d.trie;
        this.data = d;
        this.numCpFwd = -1;
        this.isNumeric = numeric;
        this.ceBuffer = new CEBuffer();
    }

    public boolean equals(Object other) {
        if (other == null || !getClass().equals(other.getClass())) {
            return -assertionsDisabled;
        }
        CollationIterator o = (CollationIterator) other;
        if (this.ceBuffer.length != o.ceBuffer.length || this.cesIndex != o.cesIndex || this.numCpFwd != o.numCpFwd || this.isNumeric != o.isNumeric) {
            return -assertionsDisabled;
        }
        for (int i = 0; i < this.ceBuffer.length; i++) {
            if (this.ceBuffer.get(i) != o.ceBuffer.get(i)) {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    public final long nextCE() {
        Object obj = null;
        if (this.cesIndex < this.ceBuffer.length) {
            CEBuffer cEBuffer = this.ceBuffer;
            int i = this.cesIndex;
            this.cesIndex = i + 1;
            return cEBuffer.get(i);
        }
        if (!-assertionsDisabled) {
            if (this.cesIndex == this.ceBuffer.length) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.ceBuffer.incLength();
        long cAndCE32 = handleNextCE32();
        int c = (int) (cAndCE32 >> 32);
        int ce32 = (int) cAndCE32;
        int t = ce32 & Opcodes.OP_CONST_CLASS_JUMBO;
        if (t < Opcodes.OP_AND_LONG_2ADDR) {
            cEBuffer = this.ceBuffer;
            i = this.cesIndex;
            this.cesIndex = i + 1;
            return cEBuffer.set(i, ((((long) (ce32 & -65536)) << 32) | (((long) (Normalizer2Impl.JAMO_VT & ce32)) << 16)) | ((long) (t << 8)));
        }
        CollationData d;
        if (t != Opcodes.OP_AND_LONG_2ADDR) {
            d = this.data;
        } else if (c < 0) {
            cEBuffer = this.ceBuffer;
            i = this.cesIndex;
            this.cesIndex = i + 1;
            return cEBuffer.set(i, Collation.NO_CE);
        } else {
            d = this.data.base;
            ce32 = d.getCE32(c);
            t = ce32 & Opcodes.OP_CONST_CLASS_JUMBO;
            if (t < Opcodes.OP_AND_LONG_2ADDR) {
                cEBuffer = this.ceBuffer;
                i = this.cesIndex;
                this.cesIndex = i + 1;
                return cEBuffer.set(i, ((((long) (ce32 & -65536)) << 32) | (((long) (Normalizer2Impl.JAMO_VT & ce32)) << 16)) | ((long) (t << 8)));
            }
        }
        if (t != Opcodes.OP_OR_LONG_2ADDR) {
            return nextCEFromCE32(d, c, ce32);
        }
        cEBuffer = this.ceBuffer;
        i = this.cesIndex;
        this.cesIndex = i + 1;
        return cEBuffer.set(i, (((long) (ce32 - t)) << 32) | 83887360);
    }

    public final int fetchCEs() {
        while (nextCE() != Collation.NO_CE) {
            this.cesIndex = this.ceBuffer.length;
        }
        return this.ceBuffer.length;
    }

    final void setCurrentCE(long ce) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (this.cesIndex > 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.ceBuffer.set(this.cesIndex - 1, ce);
    }

    public final long previousCE(UVector32 offsets) {
        if (this.ceBuffer.length > 0) {
            CEBuffer cEBuffer = this.ceBuffer;
            CEBuffer cEBuffer2 = this.ceBuffer;
            int i = cEBuffer2.length - 1;
            cEBuffer2.length = i;
            return cEBuffer.get(i);
        }
        offsets.removeAllElements();
        int limitOffset = getOffset();
        int c = previousCodePoint();
        if (c < 0) {
            return Collation.NO_CE;
        }
        if (this.data.isUnsafeBackward(c, this.isNumeric)) {
            return previousCEUnsafe(c, offsets);
        }
        CollationData d;
        int ce32 = this.data.getCE32(c);
        if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
            d = this.data.base;
            ce32 = d.getCE32(c);
        } else {
            d = this.data;
        }
        if (Collation.isSimpleOrLongCE32(ce32)) {
            return Collation.ceFromCE32(ce32);
        }
        appendCEsFromCE32(d, c, ce32, -assertionsDisabled);
        if (this.ceBuffer.length > 1) {
            offsets.addElement(getOffset());
            while (offsets.size() <= this.ceBuffer.length) {
                offsets.addElement(limitOffset);
            }
        }
        cEBuffer = this.ceBuffer;
        cEBuffer2 = this.ceBuffer;
        i = cEBuffer2.length - 1;
        cEBuffer2.length = i;
        return cEBuffer.get(i);
    }

    public final int getCEsLength() {
        return this.ceBuffer.length;
    }

    public final long getCE(int i) {
        return this.ceBuffer.get(i);
    }

    public final long[] getCEs() {
        return this.ceBuffer.getCEs();
    }

    final void clearCEs() {
        this.ceBuffer.length = 0;
        this.cesIndex = 0;
    }

    public final void clearCEsIfNoneRemaining() {
        if (this.cesIndex == this.ceBuffer.length) {
            clearCEs();
        }
    }

    protected final void reset() {
        this.ceBuffer.length = 0;
        this.cesIndex = 0;
        if (this.skipped != null) {
            this.skipped.clear();
        }
    }

    protected final void reset(boolean numeric) {
        if (this.ceBuffer == null) {
            this.ceBuffer = new CEBuffer();
        }
        reset();
        this.isNumeric = numeric;
    }

    protected long handleNextCE32() {
        int c = nextCodePoint();
        if (c < 0) {
            return NO_CP_AND_CE32;
        }
        return makeCodePointAndCE32Pair(c, this.data.getCE32(c));
    }

    protected long makeCodePointAndCE32Pair(int c, int ce32) {
        return (((long) c) << 32) | (((long) ce32) & 4294967295L);
    }

    protected char handleGetTrailSurrogate() {
        return '\u0000';
    }

    protected boolean forbidSurrogateCodePoints() {
        return -assertionsDisabled;
    }

    protected int getDataCE32(int c) {
        return this.data.getCE32(c);
    }

    protected int getCE32FromBuilderData(int ce32) {
        throw new ICUException("internal program error: should be unreachable");
    }

    protected final void appendCEsFromCE32(CollationData d, int c, int ce32, boolean forward) {
        while (Collation.isSpecialCE32(ce32)) {
            CEBuffer cEBuffer;
            int index;
            int length;
            int index2;
            switch (Collation.tagFromCE32(ce32)) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                case XmlPullParser.END_TAG /*3*/:
                    throw new ICUException("internal program error: should be unreachable");
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    this.ceBuffer.append(Collation.ceFromLongPrimaryCE32(ce32));
                    return;
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    this.ceBuffer.append(Collation.ceFromLongSecondaryCE32(ce32));
                    return;
                case NodeFilter.SHOW_TEXT /*4*/:
                    this.ceBuffer.ensureAppendCapacity(2);
                    this.ceBuffer.set(this.ceBuffer.length, Collation.latinCE0FromCE32(ce32));
                    this.ceBuffer.set(this.ceBuffer.length + 1, Collation.latinCE1FromCE32(ce32));
                    cEBuffer = this.ceBuffer;
                    cEBuffer.length += 2;
                    return;
                case XmlPullParser.CDSECT /*5*/:
                    index = Collation.indexFromCE32(ce32);
                    length = Collation.lengthFromCE32(ce32);
                    this.ceBuffer.ensureAppendCapacity(length);
                    while (true) {
                        index2 = index + 1;
                        this.ceBuffer.appendUnsafe(Collation.ceFromCE32(d.ce32s[index]));
                        length--;
                        if (length > 0) {
                            index = index2;
                        } else {
                            return;
                        }
                    }
                case XmlPullParser.ENTITY_REF /*6*/:
                    index = Collation.indexFromCE32(ce32);
                    length = Collation.lengthFromCE32(ce32);
                    this.ceBuffer.ensureAppendCapacity(length);
                    while (true) {
                        index2 = index + 1;
                        this.ceBuffer.appendUnsafe(d.ces[index]);
                        length--;
                        if (length > 0) {
                            index = index2;
                        } else {
                            return;
                        }
                    }
                case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                    ce32 = getCE32FromBuilderData(ce32);
                    if (ce32 != 192) {
                        break;
                    }
                    d = this.data.base;
                    ce32 = d.getCE32(c);
                    break;
                case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                    if (forward) {
                        backwardNumCodePoints(1);
                    }
                    ce32 = getCE32FromPrefix(d, ce32);
                    if (!forward) {
                        break;
                    }
                    forwardNumCodePoints(1);
                    break;
                case XmlPullParser.COMMENT /*9*/:
                    index = Collation.indexFromCE32(ce32);
                    int defaultCE32 = d.getCE32FromContexts(index);
                    if (!forward) {
                        ce32 = defaultCE32;
                        break;
                    }
                    int nextCp;
                    if (this.skipped == null && this.numCpFwd < 0) {
                        nextCp = nextCodePoint();
                        if (nextCp >= 0) {
                            if (!((ce32 & NodeFilter.SHOW_DOCUMENT_TYPE) == 0 || CollationFCD.mayHaveLccc(nextCp))) {
                                backwardNumCodePoints(1);
                                ce32 = defaultCE32;
                                break;
                            }
                        }
                        ce32 = defaultCE32;
                        break;
                    }
                    nextCp = nextSkippedCodePoint();
                    if (nextCp >= 0) {
                        if (!((ce32 & NodeFilter.SHOW_DOCUMENT_TYPE) == 0 || CollationFCD.mayHaveLccc(nextCp))) {
                            backwardNumSkipped(1);
                            ce32 = defaultCE32;
                            break;
                        }
                    }
                    ce32 = defaultCE32;
                    break;
                    ce32 = nextCE32FromContraction(d, ce32, d.contexts, index + 2, defaultCE32, nextCp);
                    if (ce32 != 1) {
                        break;
                    }
                    return;
                    break;
                case XmlPullParser.DOCDECL /*10*/:
                    if (!this.isNumeric) {
                        ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                        break;
                    } else {
                        appendNumericCEs(ce32, forward);
                        return;
                    }
                case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                    if (!-assertionsDisabled) {
                        if ((c == 0 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    ce32 = d.ce32s[0];
                    break;
                case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                    int[] jamoCE32s = d.jamoCE32s;
                    c -= Hangul.HANGUL_BASE;
                    int t = c % 28;
                    c /= 28;
                    int v = c % 21;
                    c /= 21;
                    if ((ce32 & NodeFilter.SHOW_DOCUMENT) == 0) {
                        appendCEsFromCE32(d, -1, jamoCE32s[c], forward);
                        appendCEsFromCE32(d, -1, jamoCE32s[v + 19], forward);
                        if (t != 0) {
                            ce32 = jamoCE32s[t + 39];
                            c = -1;
                            break;
                        }
                        return;
                    }
                    this.ceBuffer.ensureAppendCapacity(t == 0 ? 2 : 3);
                    this.ceBuffer.set(this.ceBuffer.length, Collation.ceFromCE32(jamoCE32s[c]));
                    this.ceBuffer.set(this.ceBuffer.length + 1, Collation.ceFromCE32(jamoCE32s[v + 19]));
                    cEBuffer = this.ceBuffer;
                    cEBuffer.length += 2;
                    if (t != 0) {
                        this.ceBuffer.appendUnsafe(Collation.ceFromCE32(jamoCE32s[t + 39]));
                    }
                    return;
                case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                    if (!-assertionsDisabled && !forward) {
                        throw new AssertionError();
                    } else if (-assertionsDisabled || isLeadSurrogate(c)) {
                        char trail = handleGetTrailSurrogate();
                        if (!Character.isLowSurrogate(trail)) {
                            ce32 = -1;
                            break;
                        }
                        c = Character.toCodePoint((char) c, trail);
                        ce32 &= CollationSettings.CASE_FIRST_AND_UPPER_MASK;
                        if (ce32 != 0) {
                            if (ce32 != 256) {
                                ce32 = d.getCE32FromSupplementary(c);
                                if (ce32 != 192) {
                                    break;
                                }
                            }
                            d = d.base;
                            ce32 = d.getCE32FromSupplementary(c);
                            break;
                        }
                        ce32 = -1;
                        break;
                    } else {
                        throw new AssertionError();
                    }
                case Opcodes.OP_RETURN_VOID /*14*/:
                    if (!-assertionsDisabled) {
                        if ((c >= 0 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    this.ceBuffer.append(d.getCEFromOffsetCE32(c, ce32));
                    return;
                case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                    if (!-assertionsDisabled) {
                        if ((c >= 0 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (isSurrogate(c) && forbidSurrogateCodePoints()) {
                        ce32 = -195323;
                        break;
                    } else {
                        this.ceBuffer.append(Collation.unassignedCEFromCodePoint(c));
                        return;
                    }
                default:
                    break;
            }
        }
        this.ceBuffer.append(Collation.ceFromSimpleCE32(ce32));
    }

    private static final boolean isSurrogate(int c) {
        return (c & -2048) == UTF16.SURROGATE_MIN_VALUE ? true : -assertionsDisabled;
    }

    protected static final boolean isLeadSurrogate(int c) {
        return (c & -1024) == UTF16.SURROGATE_MIN_VALUE ? true : -assertionsDisabled;
    }

    protected static final boolean isTrailSurrogate(int c) {
        return (c & -1024) == UTF16.TRAIL_SURROGATE_MIN_VALUE ? true : -assertionsDisabled;
    }

    private final long nextCEFromCE32(CollationData d, int c, int ce32) {
        CEBuffer cEBuffer = this.ceBuffer;
        cEBuffer.length--;
        appendCEsFromCE32(d, c, ce32, true);
        cEBuffer = this.ceBuffer;
        int i = this.cesIndex;
        this.cesIndex = i + 1;
        return cEBuffer.get(i);
    }

    private final int getCE32FromPrefix(CollationData d, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        ce32 = d.getCE32FromContexts(index);
        int lookBehind = 0;
        CharsTrie prefixes = new CharsTrie(d.contexts, index + 2);
        Result match;
        do {
            int c = previousCodePoint();
            if (c < 0) {
                break;
            }
            lookBehind++;
            match = prefixes.nextForCodePoint(c);
            if (match.hasValue()) {
                ce32 = prefixes.getValue();
            }
        } while (match.hasNext());
        forwardNumCodePoints(lookBehind);
        return ce32;
    }

    private final int nextSkippedCodePoint() {
        if (this.skipped != null && this.skipped.hasNext()) {
            return this.skipped.next();
        }
        if (this.numCpFwd == 0) {
            return -1;
        }
        int c = nextCodePoint();
        if (!(this.skipped == null || this.skipped.isEmpty() || c < 0)) {
            this.skipped.incBeyond();
        }
        if (this.numCpFwd > 0 && c >= 0) {
            this.numCpFwd--;
        }
        return c;
    }

    private final void backwardNumSkipped(int n) {
        if (!(this.skipped == null || this.skipped.isEmpty())) {
            n = this.skipped.backwardNumCodePoints(n);
        }
        backwardNumCodePoints(n);
        if (this.numCpFwd >= 0) {
            this.numCpFwd += n;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final int nextCE32FromContraction(CollationData d, int contractionCE32, CharSequence trieChars, int trieOffset, int ce32, int c) {
        int lookAhead = 1;
        int sinceMatch = 1;
        CharsTrie suffixes = new CharsTrie(trieChars, trieOffset);
        if (!(this.skipped == null || this.skipped.isEmpty())) {
            this.skipped.saveTrieState(suffixes);
        }
        Result match = suffixes.firstForCodePoint(c);
        while (true) {
            if (!match.hasValue()) {
                if (match == Result.NO_MATCH) {
                    break;
                }
                int nextCp = nextSkippedCodePoint();
                if (nextCp < 0) {
                    break;
                }
                c = nextCp;
                sinceMatch++;
            } else {
                ce32 = suffixes.getValue();
                if (!match.hasNext()) {
                    break;
                }
                c = nextSkippedCodePoint();
                if (c < 0) {
                    break;
                }
                if (!(this.skipped == null || this.skipped.isEmpty())) {
                    this.skipped.saveTrieState(suffixes);
                }
                sinceMatch = 1;
            }
            lookAhead++;
            match = suffixes.nextForCodePoint(c);
        }
        if ((contractionCE32 & NodeFilter.SHOW_DOCUMENT_FRAGMENT) != 0 && ((contractionCE32 & NodeFilter.SHOW_DOCUMENT) == 0 || sinceMatch < lookAhead)) {
            if (sinceMatch > 1) {
                backwardNumSkipped(sinceMatch);
                c = nextSkippedCodePoint();
                lookAhead -= sinceMatch - 1;
                sinceMatch = 1;
            }
            if (d.getFCD16(c) > Opcodes.OP_CONST_CLASS_JUMBO) {
                return nextCE32FromDiscontiguousContraction(d, suffixes, ce32, lookAhead, c);
            }
        }
        backwardNumSkipped(sinceMatch);
        return ce32;
    }

    private final int nextCE32FromDiscontiguousContraction(CollationData d, CharsTrie suffixes, int ce32, int lookAhead, int c) {
        int fcd16 = d.getFCD16(c);
        if (!-assertionsDisabled) {
            if ((fcd16 > Opcodes.OP_CONST_CLASS_JUMBO ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int nextCp = nextSkippedCodePoint();
        if (nextCp < 0) {
            backwardNumSkipped(1);
            return ce32;
        }
        lookAhead++;
        int prevCC = fcd16 & Opcodes.OP_CONST_CLASS_JUMBO;
        fcd16 = d.getFCD16(nextCp);
        if (fcd16 <= Opcodes.OP_CONST_CLASS_JUMBO) {
            backwardNumSkipped(2);
            return ce32;
        }
        if (this.skipped == null || this.skipped.isEmpty()) {
            if (this.skipped == null) {
                this.skipped = new SkippedState();
            }
            suffixes.reset();
            if (lookAhead > 2) {
                backwardNumCodePoints(lookAhead);
                suffixes.firstForCodePoint(nextCodePoint());
                for (int i = 3; i < lookAhead; i++) {
                    suffixes.nextForCodePoint(nextCodePoint());
                }
                forwardNumCodePoints(2);
            }
            this.skipped.saveTrieState(suffixes);
        } else {
            this.skipped.resetToTrieState(suffixes);
        }
        this.skipped.setFirstSkipped(c);
        int sinceMatch = 2;
        c = nextCp;
        do {
            if (prevCC < (fcd16 >> 8)) {
                Result match = suffixes.nextForCodePoint(c);
                if (match.hasValue()) {
                    ce32 = suffixes.getValue();
                    sinceMatch = 0;
                    this.skipped.recordMatch();
                    if (!match.hasNext()) {
                        break;
                    }
                    this.skipped.saveTrieState(suffixes);
                    c = nextSkippedCodePoint();
                    if (c >= 0) {
                        break;
                    }
                    sinceMatch++;
                    fcd16 = d.getFCD16(c);
                }
            }
            this.skipped.skip(c);
            this.skipped.resetToTrieState(suffixes);
            prevCC = fcd16 & Opcodes.OP_CONST_CLASS_JUMBO;
            c = nextSkippedCodePoint();
            if (c >= 0) {
                break;
            }
            sinceMatch++;
            fcd16 = d.getFCD16(c);
        } while (fcd16 > Opcodes.OP_CONST_CLASS_JUMBO);
        backwardNumSkipped(sinceMatch);
        boolean isTopDiscontiguous = this.skipped.isEmpty();
        this.skipped.replaceMatch();
        if (isTopDiscontiguous && !this.skipped.isEmpty()) {
            c = -1;
            while (true) {
                appendCEsFromCE32(d, c, ce32, true);
                if (!this.skipped.hasNext()) {
                    break;
                }
                c = this.skipped.next();
                ce32 = getDataCE32(c);
                if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
                    d = this.data.base;
                    ce32 = d.getCE32(c);
                } else {
                    d = this.data;
                }
            }
            this.skipped.clear();
            ce32 = 1;
        }
        return ce32;
    }

    private final long previousCEUnsafe(int c, UVector32 offsets) {
        int i = 1;
        int numBackward = 1;
        do {
            c = previousCodePoint();
            if (c < 0) {
                break;
            }
            numBackward++;
        } while (this.data.isUnsafeBackward(c, this.isNumeric));
        this.numCpFwd = numBackward;
        this.cesIndex = 0;
        if (!-assertionsDisabled) {
            if ((this.ceBuffer.length == 0 ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        int offset = getOffset();
        while (this.numCpFwd > 0) {
            this.numCpFwd--;
            nextCE();
            if (!-assertionsDisabled) {
                if ((this.ceBuffer.get(this.ceBuffer.length + -1) != Collation.NO_CE ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            this.cesIndex = this.ceBuffer.length;
            if (!-assertionsDisabled) {
                if ((offsets.size() < this.ceBuffer.length ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            offsets.addElement(offset);
            offset = getOffset();
            while (offsets.size() < this.ceBuffer.length) {
                offsets.addElement(offset);
            }
        }
        if (!-assertionsDisabled) {
            if (offsets.size() != this.ceBuffer.length) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        offsets.addElement(offset);
        this.numCpFwd = -1;
        backwardNumCodePoints(numBackward);
        this.cesIndex = 0;
        CEBuffer cEBuffer = this.ceBuffer;
        CEBuffer cEBuffer2 = this.ceBuffer;
        int i2 = cEBuffer2.length - 1;
        cEBuffer2.length = i2;
        return cEBuffer.get(i2);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void appendNumericCEs(int ce32, boolean forward) {
        StringBuilder digits = new StringBuilder();
        int c;
        if (forward) {
            while (true) {
                digits.append(Collation.digitFromCE32(ce32));
                if (this.numCpFwd != 0) {
                    c = nextCodePoint();
                    if (c < 0) {
                        break;
                    }
                    ce32 = this.data.getCE32(c);
                    if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
                        ce32 = this.data.base.getCE32(c);
                    }
                    if (!Collation.hasCE32Tag(ce32, 10)) {
                        break;
                    } else if (this.numCpFwd > 0) {
                        this.numCpFwd--;
                    }
                } else {
                    break;
                }
            }
        }
        do {
            digits.append(Collation.digitFromCE32(ce32));
            c = previousCodePoint();
            if (c < 0) {
                break;
            }
            ce32 = this.data.getCE32(c);
            if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
                ce32 = this.data.base.getCE32(c);
            }
        } while (Collation.hasCE32Tag(ce32, 10));
        forwardNumCodePoints(1);
        digits.reverse();
        int pos = 0;
        while (true) {
            if (pos >= digits.length() - 1 || digits.charAt(pos) != '\u0000') {
                int segmentLength = digits.length() - pos;
                if (segmentLength > SCSU.KATAKANAINDEX) {
                    segmentLength = SCSU.KATAKANAINDEX;
                }
                appendNumericSegmentCEs(digits.subSequence(pos, pos + segmentLength));
                pos += segmentLength;
                if (pos >= digits.length()) {
                    return;
                }
            } else {
                pos++;
            }
        }
    }

    private final void appendNumericSegmentCEs(CharSequence digits) {
        Object obj;
        int pair;
        int pos;
        int length = digits.length();
        if (!-assertionsDisabled) {
            obj = (1 > length || length > SCSU.KATAKANAINDEX) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            obj = (length == 1 || digits.charAt(0) != '\u0000') ? 1 : null;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        long numericPrimary = this.data.numericPrimary;
        if (length <= 7) {
            int value = digits.charAt(0);
            for (int i = 1; i < length; i++) {
                value = (value * 10) + digits.charAt(i);
            }
            if (value < 74) {
                this.ceBuffer.append(Collation.makeCE(numericPrimary | ((long) ((value + 2) << 16))));
                return;
            }
            value -= 74;
            if (value < 10160) {
                this.ceBuffer.append(Collation.makeCE((((long) (((value / SCSU.KATAKANAINDEX) + 76) << 16)) | numericPrimary) | ((long) (((value % SCSU.KATAKANAINDEX) + 2) << 8))));
                return;
            }
            value -= 10160;
            int firstByte = 76 + 40;
            if (value < 1032256) {
                value /= SCSU.KATAKANAINDEX;
                this.ceBuffer.append(Collation.makeCE(((numericPrimary | ((long) ((value % SCSU.KATAKANAINDEX) + 2))) | ((long) (((value % SCSU.KATAKANAINDEX) + 2) << 8))) | ((long) ((((value / SCSU.KATAKANAINDEX) % SCSU.KATAKANAINDEX) + Opcodes.OP_INVOKE_VIRTUAL_RANGE) << 16))));
                return;
            }
        }
        if (!-assertionsDisabled) {
            if ((length >= 7 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        long primary = numericPrimary | ((long) ((((length + 1) / 2) + NodeFilter.SHOW_COMMENT) << 16));
        while (true) {
            if (digits.charAt(length - 1) != '\u0000') {
                break;
            }
            if (digits.charAt(length - 2) != '\u0000') {
                break;
            }
            length -= 2;
        }
        if ((length & 1) != 0) {
            pair = digits.charAt(0);
            pos = 1;
        } else {
            pair = (digits.charAt(0) * 10) + digits.charAt(1);
            pos = 2;
        }
        pair = (pair * 2) + 11;
        int shift = 8;
        for (pos = 
        /* Method generation error in method: android.icu.impl.coll.CollationIterator.appendNumericSegmentCEs(java.lang.CharSequence):void
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r9_1 'pos' int) = (r9_0 'pos' int), (r9_4 'pos' int) binds: {(r9_4 'pos' int)=B:62:0x0141, (r9_0 'pos' int)=B:56:0x0105} in method: android.icu.impl.coll.CollationIterator.appendNumericSegmentCEs(java.lang.CharSequence):void
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: android.icu.impl.coll.CollationIterator.appendNumericSegmentCEs(java.lang.CharSequence):void
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 15 more
 */
    }
