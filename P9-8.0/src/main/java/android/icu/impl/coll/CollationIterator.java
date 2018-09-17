package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.impl.Trie2_32;
import android.icu.text.UTF16;
import android.icu.util.BytesTrie.Result;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrie.State;
import android.icu.util.ICUException;

public abstract class CollationIterator {
    static final /* synthetic */ boolean -assertionsDisabled = (CollationIterator.class.desiredAssertionStatus() ^ 1);
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
        private long[] buffer = new long[40];
        int length = 0;

        CEBuffer() {
        }

        void append(long ce) {
            if (this.length >= 40) {
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
                    if (capacity < 1000) {
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
            if (this.length >= 40) {
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
        static final /* synthetic */ boolean -assertionsDisabled = (SkippedState.class.desiredAssertionStatus() ^ 1);
        private final StringBuilder newBuffer = new StringBuilder();
        private final StringBuilder oldBuffer = new StringBuilder();
        private int pos;
        private int skipLengthAtMatch;
        private State state = new State();

        SkippedState() {
        }

        void clear() {
            this.oldBuffer.setLength(0);
            this.pos = 0;
        }

        boolean isEmpty() {
            return this.oldBuffer.length() == 0;
        }

        boolean hasNext() {
            return this.pos < this.oldBuffer.length();
        }

        int next() {
            int c = this.oldBuffer.codePointAt(this.pos);
            this.pos += Character.charCount(c);
            return c;
        }

        void incBeyond() {
            if (-assertionsDisabled || !hasNext()) {
                this.pos++;
                return;
            }
            throw new AssertionError();
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
        this.isNumeric = false;
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
            return false;
        }
        CollationIterator o = (CollationIterator) other;
        if (this.ceBuffer.length != o.ceBuffer.length || this.cesIndex != o.cesIndex || this.numCpFwd != o.numCpFwd || this.isNumeric != o.isNumeric) {
            return false;
        }
        for (int i = 0; i < this.ceBuffer.length; i++) {
            if (this.ceBuffer.get(i) != o.ceBuffer.get(i)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return 0;
    }

    public final long nextCE() {
        CEBuffer cEBuffer;
        int i;
        if (this.cesIndex < this.ceBuffer.length) {
            cEBuffer = this.ceBuffer;
            i = this.cesIndex;
            this.cesIndex = i + 1;
            return cEBuffer.get(i);
        } else if (-assertionsDisabled || this.cesIndex == this.ceBuffer.length) {
            this.ceBuffer.incLength();
            long cAndCE32 = handleNextCE32();
            int c = (int) (cAndCE32 >> 32);
            int ce32 = (int) cAndCE32;
            int t = ce32 & 255;
            if (t < 192) {
                cEBuffer = this.ceBuffer;
                i = this.cesIndex;
                this.cesIndex = i + 1;
                return cEBuffer.set(i, ((((long) (ce32 & -65536)) << 32) | (((long) (ce32 & Normalizer2Impl.JAMO_VT)) << 16)) | ((long) (t << 8)));
            }
            CollationData d;
            if (t != 192) {
                d = this.data;
            } else if (c < 0) {
                cEBuffer = this.ceBuffer;
                i = this.cesIndex;
                this.cesIndex = i + 1;
                return cEBuffer.set(i, Collation.NO_CE);
            } else {
                d = this.data.base;
                ce32 = d.getCE32(c);
                t = ce32 & 255;
                if (t < 192) {
                    cEBuffer = this.ceBuffer;
                    i = this.cesIndex;
                    this.cesIndex = i + 1;
                    return cEBuffer.set(i, ((((long) (ce32 & -65536)) << 32) | (((long) (ce32 & Normalizer2Impl.JAMO_VT)) << 16)) | ((long) (t << 8)));
                }
            }
            if (t != 193) {
                return nextCEFromCE32(d, c, ce32);
            }
            cEBuffer = this.ceBuffer;
            i = this.cesIndex;
            this.cesIndex = i + 1;
            return cEBuffer.set(i, (((long) (ce32 - t)) << 32) | 83887360);
        } else {
            throw new AssertionError();
        }
    }

    public final int fetchCEs() {
        while (nextCE() != Collation.NO_CE) {
            this.cesIndex = this.ceBuffer.length;
        }
        return this.ceBuffer.length;
    }

    final void setCurrentCE(long ce) {
        if (-assertionsDisabled || this.cesIndex > 0) {
            this.ceBuffer.set(this.cesIndex - 1, ce);
            return;
        }
        throw new AssertionError();
    }

    public final long previousCE(UVector32 offsets) {
        CEBuffer cEBuffer;
        CEBuffer cEBuffer2;
        int i;
        if (this.ceBuffer.length > 0) {
            cEBuffer = this.ceBuffer;
            cEBuffer2 = this.ceBuffer;
            i = cEBuffer2.length - 1;
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
        if (ce32 == 192) {
            d = this.data.base;
            ce32 = d.getCE32(c);
        } else {
            d = this.data;
        }
        if (Collation.isSimpleOrLongCE32(ce32)) {
            return Collation.ceFromCE32(ce32);
        }
        appendCEsFromCE32(d, c, ce32, false);
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
        return 0;
    }

    protected boolean forbidSurrogateCodePoints() {
        return false;
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
                case 0:
                case 3:
                    throw new ICUException("internal program error: should be unreachable");
                case 1:
                    this.ceBuffer.append(Collation.ceFromLongPrimaryCE32(ce32));
                    return;
                case 2:
                    this.ceBuffer.append(Collation.ceFromLongSecondaryCE32(ce32));
                    return;
                case 4:
                    this.ceBuffer.ensureAppendCapacity(2);
                    this.ceBuffer.set(this.ceBuffer.length, Collation.latinCE0FromCE32(ce32));
                    this.ceBuffer.set(this.ceBuffer.length + 1, Collation.latinCE1FromCE32(ce32));
                    cEBuffer = this.ceBuffer;
                    cEBuffer.length += 2;
                    return;
                case 5:
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
                case 6:
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
                case 7:
                    ce32 = getCE32FromBuilderData(ce32);
                    if (ce32 != 192) {
                        break;
                    }
                    d = this.data.base;
                    ce32 = d.getCE32(c);
                    break;
                case 8:
                    if (forward) {
                        backwardNumCodePoints(1);
                    }
                    ce32 = getCE32FromPrefix(d, ce32);
                    if (!forward) {
                        break;
                    }
                    forwardNumCodePoints(1);
                    break;
                case 9:
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
                            if (!((ce32 & 512) == 0 || (CollationFCD.mayHaveLccc(nextCp) ^ 1) == 0)) {
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
                        if (!((ce32 & 512) == 0 || (CollationFCD.mayHaveLccc(nextCp) ^ 1) == 0)) {
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
                case 10:
                    if (!this.isNumeric) {
                        ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                        break;
                    } else {
                        appendNumericCEs(ce32, forward);
                        return;
                    }
                case 11:
                    if (-assertionsDisabled || c == 0) {
                        ce32 = d.ce32s[0];
                        break;
                    }
                    throw new AssertionError();
                    break;
                case 12:
                    int[] jamoCE32s = d.jamoCE32s;
                    c -= Hangul.HANGUL_BASE;
                    int t = c % 28;
                    c /= 28;
                    int v = c % 21;
                    c /= 21;
                    if ((ce32 & 256) == 0) {
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
                case 13:
                    if (!-assertionsDisabled && !forward) {
                        throw new AssertionError();
                    } else if (-assertionsDisabled || isLeadSurrogate(c)) {
                        char trail = handleGetTrailSurrogate();
                        if (!Character.isLowSurrogate(trail)) {
                            ce32 = -1;
                            break;
                        }
                        c = Character.toCodePoint((char) c, trail);
                        ce32 &= 768;
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
                case 14:
                    if (-assertionsDisabled || c >= 0) {
                        this.ceBuffer.append(d.getCEFromOffsetCE32(c, ce32));
                        return;
                    }
                    throw new AssertionError();
                case 15:
                    if (-assertionsDisabled || c >= 0) {
                        if (isSurrogate(c) && forbidSurrogateCodePoints()) {
                            ce32 = -195323;
                            break;
                        } else {
                            this.ceBuffer.append(Collation.unassignedCEFromCodePoint(c));
                            return;
                        }
                    }
                    throw new AssertionError();
                default:
                    break;
            }
        }
        this.ceBuffer.append(Collation.ceFromSimpleCE32(ce32));
    }

    private static final boolean isSurrogate(int c) {
        return (c & -2048) == 55296;
    }

    protected static final boolean isLeadSurrogate(int c) {
        return (c & -1024) == 55296;
    }

    protected static final boolean isTrailSurrogate(int c) {
        return (c & -1024) == UTF16.TRAIL_SURROGATE_MIN_VALUE;
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
        if (!(this.skipped == null || (this.skipped.isEmpty() ^ 1) == 0 || c < 0)) {
            this.skipped.incBeyond();
        }
        if (this.numCpFwd > 0 && c >= 0) {
            this.numCpFwd--;
        }
        return c;
    }

    private final void backwardNumSkipped(int n) {
        if (!(this.skipped == null || (this.skipped.isEmpty() ^ 1) == 0)) {
            n = this.skipped.backwardNumCodePoints(n);
        }
        backwardNumCodePoints(n);
        if (this.numCpFwd >= 0) {
            this.numCpFwd += n;
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0034, code:
            return r14;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final int nextCE32FromContraction(CollationData d, int contractionCE32, CharSequence trieChars, int trieOffset, int ce32, int c) {
        int lookAhead = 1;
        int sinceMatch = 1;
        CharsTrie suffixes = new CharsTrie(trieChars, trieOffset);
        if (!(this.skipped == null || (this.skipped.isEmpty() ^ 1) == 0)) {
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
                if (!(this.skipped == null || (this.skipped.isEmpty() ^ 1) == 0)) {
                    this.skipped.saveTrieState(suffixes);
                }
                sinceMatch = 1;
            }
            lookAhead++;
            match = suffixes.nextForCodePoint(c);
        }
        if ((contractionCE32 & 1024) != 0 && ((contractionCE32 & 256) == 0 || sinceMatch < lookAhead)) {
            if (sinceMatch > 1) {
                backwardNumSkipped(sinceMatch);
                c = nextSkippedCodePoint();
                lookAhead -= sinceMatch - 1;
                sinceMatch = 1;
            }
            if (d.getFCD16(c) > 255) {
                return nextCE32FromDiscontiguousContraction(d, suffixes, ce32, lookAhead, c);
            }
        }
        backwardNumSkipped(sinceMatch);
        return ce32;
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x0090 A:{SYNTHETIC, EDGE_INSN: B:57:0x0090->B:35:0x0090 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00cf  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final int nextCE32FromDiscontiguousContraction(CollationData d, CharsTrie suffixes, int ce32, int lookAhead, int c) {
        int fcd16 = d.getFCD16(c);
        if (-assertionsDisabled || fcd16 > 255) {
            int nextCp = nextSkippedCodePoint();
            if (nextCp < 0) {
                backwardNumSkipped(1);
                return ce32;
            }
            lookAhead++;
            int prevCC = fcd16 & 255;
            fcd16 = d.getFCD16(nextCp);
            if (fcd16 <= 255) {
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
                prevCC = fcd16 & 255;
                c = nextSkippedCodePoint();
                if (c >= 0) {
                }
            } while (fcd16 > 255);
            backwardNumSkipped(sinceMatch);
            boolean isTopDiscontiguous = this.skipped.isEmpty();
            this.skipped.replaceMatch();
            if (isTopDiscontiguous && (this.skipped.isEmpty() ^ 1) != 0) {
                c = -1;
                while (true) {
                    appendCEsFromCE32(d, c, ce32, true);
                    if (!this.skipped.hasNext()) {
                        break;
                    }
                    c = this.skipped.next();
                    ce32 = getDataCE32(c);
                    if (ce32 == 192) {
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
        throw new AssertionError();
    }

    private final long previousCEUnsafe(int c, UVector32 offsets) {
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
        if (-assertionsDisabled || this.ceBuffer.length == 0) {
            int offset = getOffset();
            while (this.numCpFwd > 0) {
                this.numCpFwd--;
                nextCE();
                if (-assertionsDisabled || this.ceBuffer.get(this.ceBuffer.length - 1) != Collation.NO_CE) {
                    this.cesIndex = this.ceBuffer.length;
                    if (-assertionsDisabled || offsets.size() < this.ceBuffer.length) {
                        offsets.addElement(offset);
                        offset = getOffset();
                        while (offsets.size() < this.ceBuffer.length) {
                            offsets.addElement(offset);
                        }
                    } else {
                        throw new AssertionError();
                    }
                }
                throw new AssertionError();
            }
            if (-assertionsDisabled || offsets.size() == this.ceBuffer.length) {
                offsets.addElement(offset);
                this.numCpFwd = -1;
                backwardNumCodePoints(numBackward);
                this.cesIndex = 0;
                CEBuffer cEBuffer = this.ceBuffer;
                CEBuffer cEBuffer2 = this.ceBuffer;
                int i = cEBuffer2.length - 1;
                cEBuffer2.length = i;
                return cEBuffer.get(i);
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

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
                    if (ce32 == 192) {
                        ce32 = this.data.base.getCE32(c);
                    }
                    if (!Collation.hasCE32Tag(ce32, 10)) {
                        backwardNumCodePoints(1);
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
            if (ce32 == 192) {
                ce32 = this.data.base.getCE32(c);
            }
        } while (Collation.hasCE32Tag(ce32, 10));
        forwardNumCodePoints(1);
        digits.reverse();
        int pos = 0;
        while (true) {
            if (pos >= digits.length() - 1 || digits.charAt(pos) != 0) {
                int segmentLength = digits.length() - pos;
                if (segmentLength > 254) {
                    segmentLength = 254;
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
        int length = digits.length();
        if (!-assertionsDisabled && (1 > length || length > 254)) {
            throw new AssertionError();
        } else if (-assertionsDisabled || length == 1 || digits.charAt(0) != 0) {
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
                    this.ceBuffer.append(Collation.makeCE((((long) (((value / 254) + 76) << 16)) | numericPrimary) | ((long) (((value % 254) + 2) << 8))));
                    return;
                }
                value -= 10160;
                int firstByte = 76 + 40;
                if (value < 1032256) {
                    value /= 254;
                    this.ceBuffer.append(Collation.makeCE(((numericPrimary | ((long) ((value % 254) + 2))) | ((long) (((value % 254) + 2) << 8))) | ((long) ((((value / 254) % 254) + 116) << 16))));
                    return;
                }
            }
            if (-assertionsDisabled || length >= 7) {
                int pair;
                int pos;
                long primary = numericPrimary | ((long) ((((length + 1) / 2) + 128) << 16));
                while (true) {
                    if (digits.charAt(length - 1) != 0) {
                        break;
                    }
                    if (digits.charAt(length - 2) != 0) {
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
/*
Method generation error in method: android.icu.impl.coll.CollationIterator.appendNumericSegmentCEs(java.lang.CharSequence):void, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r10_1 'pos' int) = (r10_0 'pos' int), (r10_4 'pos' int) binds: {(r10_0 'pos' int)=B:47:0x0102, (r10_4 'pos' int)=B:53:0x0142} in method: android.icu.impl.coll.CollationIterator.appendNumericSegmentCEs(java.lang.CharSequence):void, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:143)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:322)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:260)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:222)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 29 more

*/
}
