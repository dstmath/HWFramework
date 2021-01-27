package ohos.global.icu.impl.coll;

import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.Trie2_32;
import ohos.global.icu.util.BytesTrie;
import ohos.global.icu.util.CharsTrie;
import ohos.global.icu.util.ICUException;

public abstract class CollationIterator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected static final long NO_CP_AND_CE32 = -4294967104L;
    private CEBuffer ceBuffer;
    private int cesIndex;
    protected final CollationData data;
    private boolean isNumeric;
    private int numCpFwd;
    private SkippedState skipped;
    protected final Trie2_32 trie;

    protected static final boolean isLeadSurrogate(int i) {
        return (i & -1024) == 55296;
    }

    private static final boolean isSurrogate(int i) {
        return (i & -2048) == 55296;
    }

    protected static final boolean isTrailSurrogate(int i) {
        return (i & -1024) == 56320;
    }

    /* access modifiers changed from: protected */
    public abstract void backwardNumCodePoints(int i);

    /* access modifiers changed from: protected */
    public boolean forbidSurrogateCodePoints() {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract void forwardNumCodePoints(int i);

    public abstract int getOffset();

    /* access modifiers changed from: protected */
    public char handleGetTrailSurrogate() {
        return 0;
    }

    public int hashCode() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public long makeCodePointAndCE32Pair(int i, int i2) {
        return (((long) i) << 32) | (((long) i2) & 4294967295L);
    }

    public abstract int nextCodePoint();

    public abstract int previousCodePoint();

    public abstract void resetToOffset(int i);

    /* access modifiers changed from: private */
    public static final class CEBuffer {
        private static final int INITIAL_CAPACITY = 40;
        private long[] buffer = new long[40];
        int length = 0;

        CEBuffer() {
        }

        /* access modifiers changed from: package-private */
        public void append(long j) {
            if (this.length >= 40) {
                ensureAppendCapacity(1);
            }
            long[] jArr = this.buffer;
            int i = this.length;
            this.length = i + 1;
            jArr[i] = j;
        }

        /* access modifiers changed from: package-private */
        public void appendUnsafe(long j) {
            long[] jArr = this.buffer;
            int i = this.length;
            this.length = i + 1;
            jArr[i] = j;
        }

        /* access modifiers changed from: package-private */
        public void ensureAppendCapacity(int i) {
            int i2;
            int length2 = this.buffer.length;
            if (this.length + i > length2) {
                do {
                    length2 = length2 < 1000 ? length2 * 4 : length2 * 2;
                    i2 = this.length;
                } while (length2 < i2 + i);
                long[] jArr = new long[length2];
                System.arraycopy(this.buffer, 0, jArr, 0, i2);
                this.buffer = jArr;
            }
        }

        /* access modifiers changed from: package-private */
        public void incLength() {
            if (this.length >= 40) {
                ensureAppendCapacity(1);
            }
            this.length++;
        }

        /* access modifiers changed from: package-private */
        public long set(int i, long j) {
            this.buffer[i] = j;
            return j;
        }

        /* access modifiers changed from: package-private */
        public long get(int i) {
            return this.buffer[i];
        }

        /* access modifiers changed from: package-private */
        public long[] getCEs() {
            return this.buffer;
        }
    }

    /* access modifiers changed from: private */
    public static final class SkippedState {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final StringBuilder newBuffer = new StringBuilder();
        private final StringBuilder oldBuffer = new StringBuilder();
        private int pos;
        private int skipLengthAtMatch;
        private CharsTrie.State state = new CharsTrie.State();

        SkippedState() {
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.oldBuffer.setLength(0);
            this.pos = 0;
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return this.oldBuffer.length() == 0;
        }

        /* access modifiers changed from: package-private */
        public boolean hasNext() {
            return this.pos < this.oldBuffer.length();
        }

        /* access modifiers changed from: package-private */
        public int next() {
            int codePointAt = this.oldBuffer.codePointAt(this.pos);
            this.pos += Character.charCount(codePointAt);
            return codePointAt;
        }

        /* access modifiers changed from: package-private */
        public void incBeyond() {
            this.pos++;
        }

        /* access modifiers changed from: package-private */
        public int backwardNumCodePoints(int i) {
            int length = this.oldBuffer.length();
            int i2 = this.pos;
            int i3 = i2 - length;
            if (i3 <= 0) {
                this.pos = this.oldBuffer.offsetByCodePoints(i2, -i);
                return 0;
            } else if (i3 >= i) {
                this.pos = i2 - i;
                return i;
            } else {
                this.pos = this.oldBuffer.offsetByCodePoints(length, i3 - i);
                return i3;
            }
        }

        /* access modifiers changed from: package-private */
        public void setFirstSkipped(int i) {
            this.skipLengthAtMatch = 0;
            this.newBuffer.setLength(0);
            this.newBuffer.appendCodePoint(i);
        }

        /* access modifiers changed from: package-private */
        public void skip(int i) {
            this.newBuffer.appendCodePoint(i);
        }

        /* access modifiers changed from: package-private */
        public void recordMatch() {
            this.skipLengthAtMatch = this.newBuffer.length();
        }

        /* access modifiers changed from: package-private */
        public void replaceMatch() {
            int length = this.oldBuffer.length();
            if (this.pos > length) {
                this.pos = length;
            }
            this.oldBuffer.delete(0, this.pos).insert(0, this.newBuffer, 0, this.skipLengthAtMatch);
            this.pos = 0;
        }

        /* access modifiers changed from: package-private */
        public void saveTrieState(CharsTrie charsTrie) {
            charsTrie.saveState(this.state);
        }

        /* access modifiers changed from: package-private */
        public void resetToTrieState(CharsTrie charsTrie) {
            charsTrie.resetToState(this.state);
        }
    }

    public CollationIterator(CollationData collationData) {
        this.trie = collationData.trie;
        this.data = collationData;
        this.numCpFwd = -1;
        this.isNumeric = false;
        this.ceBuffer = null;
    }

    public CollationIterator(CollationData collationData, boolean z) {
        this.trie = collationData.trie;
        this.data = collationData;
        this.numCpFwd = -1;
        this.isNumeric = z;
        this.ceBuffer = new CEBuffer();
    }

    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        CollationIterator collationIterator = (CollationIterator) obj;
        if (!(this.ceBuffer.length == collationIterator.ceBuffer.length && this.cesIndex == collationIterator.cesIndex && this.numCpFwd == collationIterator.numCpFwd && this.isNumeric == collationIterator.isNumeric)) {
            return false;
        }
        for (int i = 0; i < this.ceBuffer.length; i++) {
            if (this.ceBuffer.get(i) != collationIterator.ceBuffer.get(i)) {
                return false;
            }
        }
        return true;
    }

    public final long nextCE() {
        int i;
        CollationData collationData;
        if (this.cesIndex < this.ceBuffer.length) {
            CEBuffer cEBuffer = this.ceBuffer;
            int i2 = this.cesIndex;
            this.cesIndex = i2 + 1;
            return cEBuffer.get(i2);
        }
        this.ceBuffer.incLength();
        long handleNextCE32 = handleNextCE32();
        int i3 = (int) (handleNextCE32 >> 32);
        int i4 = (int) handleNextCE32;
        int i5 = i4 & 255;
        if (i5 < 192) {
            CEBuffer cEBuffer2 = this.ceBuffer;
            int i6 = this.cesIndex;
            this.cesIndex = i6 + 1;
            long j = ((long) (i4 & 65280)) << 16;
            return cEBuffer2.set(i6, ((long) (i5 << 8)) | j | (((long) (i4 & -65536)) << 32));
        }
        if (i5 != 192) {
            i = i4;
            collationData = this.data;
        } else if (i3 < 0) {
            CEBuffer cEBuffer3 = this.ceBuffer;
            int i7 = this.cesIndex;
            this.cesIndex = i7 + 1;
            return cEBuffer3.set(i7, Collation.NO_CE);
        } else {
            collationData = this.data.base;
            int ce32 = collationData.getCE32(i3);
            int i8 = ce32 & 255;
            if (i8 < 192) {
                CEBuffer cEBuffer4 = this.ceBuffer;
                int i9 = this.cesIndex;
                this.cesIndex = i9 + 1;
                return cEBuffer4.set(i9, (((long) (ce32 & 65280)) << 16) | (((long) (ce32 & -65536)) << 32) | ((long) (i8 << 8)));
            }
            i = ce32;
            i5 = i8;
        }
        if (i5 != 193) {
            return nextCEFromCE32(collationData, i3, i);
        }
        CEBuffer cEBuffer5 = this.ceBuffer;
        int i10 = this.cesIndex;
        this.cesIndex = i10 + 1;
        return cEBuffer5.set(i10, (((long) (i - i5)) << 32) | 83887360);
    }

    public final int fetchCEs() {
        while (nextCE() != Collation.NO_CE) {
            this.cesIndex = this.ceBuffer.length;
        }
        return this.ceBuffer.length;
    }

    /* access modifiers changed from: package-private */
    public final void setCurrentCE(long j) {
        this.ceBuffer.set(this.cesIndex - 1, j);
    }

    public final long previousCE(UVector32 uVector32) {
        CollationData collationData;
        if (this.ceBuffer.length > 0) {
            CEBuffer cEBuffer = this.ceBuffer;
            int i = cEBuffer.length - 1;
            cEBuffer.length = i;
            return cEBuffer.get(i);
        }
        uVector32.removeAllElements();
        int offset = getOffset();
        int previousCodePoint = previousCodePoint();
        if (previousCodePoint < 0) {
            return Collation.NO_CE;
        }
        if (this.data.isUnsafeBackward(previousCodePoint, this.isNumeric)) {
            return previousCEUnsafe(previousCodePoint, uVector32);
        }
        int ce32 = this.data.getCE32(previousCodePoint);
        if (ce32 == 192) {
            CollationData collationData2 = this.data.base;
            collationData = collationData2;
            ce32 = collationData2.getCE32(previousCodePoint);
        } else {
            collationData = this.data;
        }
        if (Collation.isSimpleOrLongCE32(ce32)) {
            return Collation.ceFromCE32(ce32);
        }
        appendCEsFromCE32(collationData, previousCodePoint, ce32, false);
        if (this.ceBuffer.length > 1) {
            uVector32.addElement(getOffset());
            while (uVector32.size() <= this.ceBuffer.length) {
                uVector32.addElement(offset);
            }
        }
        CEBuffer cEBuffer2 = this.ceBuffer;
        int i2 = cEBuffer2.length - 1;
        cEBuffer2.length = i2;
        return cEBuffer2.get(i2);
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

    /* access modifiers changed from: package-private */
    public final void clearCEs() {
        this.ceBuffer.length = 0;
        this.cesIndex = 0;
    }

    public final void clearCEsIfNoneRemaining() {
        if (this.cesIndex == this.ceBuffer.length) {
            clearCEs();
        }
    }

    /* access modifiers changed from: protected */
    public final void reset() {
        this.ceBuffer.length = 0;
        this.cesIndex = 0;
        SkippedState skippedState = this.skipped;
        if (skippedState != null) {
            skippedState.clear();
        }
    }

    /* access modifiers changed from: protected */
    public final void reset(boolean z) {
        if (this.ceBuffer == null) {
            this.ceBuffer = new CEBuffer();
        }
        reset();
        this.isNumeric = z;
    }

    /* access modifiers changed from: protected */
    public long handleNextCE32() {
        int nextCodePoint = nextCodePoint();
        if (nextCodePoint < 0) {
            return NO_CP_AND_CE32;
        }
        return makeCodePointAndCE32Pair(nextCodePoint, this.data.getCE32(nextCodePoint));
    }

    /* access modifiers changed from: protected */
    public int getDataCE32(int i) {
        return this.data.getCE32(i);
    }

    /* access modifiers changed from: protected */
    public int getCE32FromBuilderData(int i) {
        throw new ICUException("internal program error: should be unreachable");
    }

    /* access modifiers changed from: protected */
    public final void appendCEsFromCE32(CollationData collationData, int i, int i2, boolean z) {
        int i3;
        while (true) {
            int i4 = i2;
            while (Collation.isSpecialCE32(i4)) {
                switch (Collation.tagFromCE32(i4)) {
                    case 0:
                    case 3:
                        throw new ICUException("internal program error: should be unreachable");
                    case 1:
                        this.ceBuffer.append(Collation.ceFromLongPrimaryCE32(i4));
                        return;
                    case 2:
                        this.ceBuffer.append(Collation.ceFromLongSecondaryCE32(i4));
                        return;
                    case 4:
                        this.ceBuffer.ensureAppendCapacity(2);
                        CEBuffer cEBuffer = this.ceBuffer;
                        cEBuffer.set(cEBuffer.length, Collation.latinCE0FromCE32(i4));
                        CEBuffer cEBuffer2 = this.ceBuffer;
                        cEBuffer2.set(cEBuffer2.length + 1, Collation.latinCE1FromCE32(i4));
                        this.ceBuffer.length += 2;
                        return;
                    case 5:
                        int indexFromCE32 = Collation.indexFromCE32(i4);
                        int lengthFromCE32 = Collation.lengthFromCE32(i4);
                        this.ceBuffer.ensureAppendCapacity(lengthFromCE32);
                        while (true) {
                            int i5 = indexFromCE32 + 1;
                            this.ceBuffer.appendUnsafe(Collation.ceFromCE32(collationData.ce32s[indexFromCE32]));
                            lengthFromCE32--;
                            if (lengthFromCE32 > 0) {
                                indexFromCE32 = i5;
                            } else {
                                return;
                            }
                        }
                    case 6:
                        int indexFromCE322 = Collation.indexFromCE32(i4);
                        int lengthFromCE322 = Collation.lengthFromCE32(i4);
                        this.ceBuffer.ensureAppendCapacity(lengthFromCE322);
                        while (true) {
                            int i6 = indexFromCE322 + 1;
                            this.ceBuffer.appendUnsafe(collationData.ces[indexFromCE322]);
                            lengthFromCE322--;
                            if (lengthFromCE322 > 0) {
                                indexFromCE322 = i6;
                            } else {
                                return;
                            }
                        }
                    case 7:
                        i2 = getCE32FromBuilderData(i4);
                        if (i2 != 192) {
                            break;
                        } else {
                            collationData = this.data.base;
                            i2 = collationData.getCE32(i);
                            break;
                        }
                    case 8:
                        if (z) {
                            backwardNumCodePoints(1);
                        }
                        i2 = getCE32FromPrefix(collationData, i4);
                        if (!z) {
                            break;
                        } else {
                            forwardNumCodePoints(1);
                            break;
                        }
                    case 9:
                        int indexFromCE323 = Collation.indexFromCE32(i4);
                        int cE32FromContexts = collationData.getCE32FromContexts(indexFromCE323);
                        if (z) {
                            if (this.skipped != null || this.numCpFwd >= 0) {
                                i3 = nextSkippedCodePoint();
                                if (i3 >= 0) {
                                    if ((i4 & 512) != 0 && !CollationFCD.mayHaveLccc(i3)) {
                                        backwardNumSkipped(1);
                                    }
                                }
                            } else {
                                i3 = nextCodePoint();
                                if (i3 >= 0) {
                                    if ((i4 & 512) != 0 && !CollationFCD.mayHaveLccc(i3)) {
                                        backwardNumCodePoints(1);
                                    }
                                }
                            }
                            i2 = nextCE32FromContraction(collationData, i4, collationData.contexts, indexFromCE323 + 2, cE32FromContexts, i3);
                            if (i2 != 1) {
                                break;
                            } else {
                                return;
                            }
                        }
                        i4 = cE32FromContexts;
                        break;
                    case 10:
                        if (!this.isNumeric) {
                            i2 = collationData.ce32s[Collation.indexFromCE32(i4)];
                            break;
                        } else {
                            appendNumericCEs(i4, z);
                            return;
                        }
                    case 11:
                        i2 = collationData.ce32s[0];
                        break;
                    case 12:
                        int[] iArr = collationData.jamoCE32s;
                        int i7 = i - Normalizer2Impl.Hangul.HANGUL_BASE;
                        int i8 = i7 % 28;
                        int i9 = i7 / 28;
                        int i10 = i9 % 21;
                        int i11 = i9 / 21;
                        if ((i4 & 256) != 0) {
                            this.ceBuffer.ensureAppendCapacity(i8 == 0 ? 2 : 3);
                            CEBuffer cEBuffer3 = this.ceBuffer;
                            cEBuffer3.set(cEBuffer3.length, Collation.ceFromCE32(iArr[i11]));
                            CEBuffer cEBuffer4 = this.ceBuffer;
                            cEBuffer4.set(cEBuffer4.length + 1, Collation.ceFromCE32(iArr[i10 + 19]));
                            this.ceBuffer.length += 2;
                            if (i8 != 0) {
                                this.ceBuffer.appendUnsafe(Collation.ceFromCE32(iArr[i8 + 39]));
                                return;
                            }
                            return;
                        }
                        appendCEsFromCE32(collationData, -1, iArr[i11], z);
                        appendCEsFromCE32(collationData, -1, iArr[i10 + 19], z);
                        if (i8 != 0) {
                            i4 = iArr[i8 + 39];
                            i = -1;
                        } else {
                            return;
                        }
                    case 13:
                        char handleGetTrailSurrogate = handleGetTrailSurrogate();
                        if (Character.isLowSurrogate(handleGetTrailSurrogate)) {
                            i = Character.toCodePoint((char) i, handleGetTrailSurrogate);
                            int i12 = i4 & 768;
                            if (i12 != 0) {
                                if (i12 != 256 && (i2 = collationData.getCE32FromSupplementary(i)) != 192) {
                                    break;
                                } else {
                                    collationData = collationData.base;
                                    i2 = collationData.getCE32FromSupplementary(i);
                                    break;
                                }
                            }
                        }
                        i4 = -1;
                        break;
                    case 14:
                        this.ceBuffer.append(collationData.getCEFromOffsetCE32(i, i4));
                        return;
                    case 15:
                        if (isSurrogate(i) && forbidSurrogateCodePoints()) {
                            i2 = -195323;
                            break;
                        }
                        break;
                }
            }
            this.ceBuffer.append(Collation.ceFromSimpleCE32(i4));
            return;
        }
        this.ceBuffer.append(Collation.unassignedCEFromCodePoint(i));
    }

    private final long nextCEFromCE32(CollationData collationData, int i, int i2) {
        this.ceBuffer.length--;
        appendCEsFromCE32(collationData, i, i2, true);
        CEBuffer cEBuffer = this.ceBuffer;
        int i3 = this.cesIndex;
        this.cesIndex = i3 + 1;
        return cEBuffer.get(i3);
    }

    private final int getCE32FromPrefix(CollationData collationData, int i) {
        BytesTrie.Result nextForCodePoint;
        int indexFromCE32 = Collation.indexFromCE32(i);
        int cE32FromContexts = collationData.getCE32FromContexts(indexFromCE32);
        CharsTrie charsTrie = new CharsTrie(collationData.contexts, indexFromCE32 + 2);
        int i2 = 0;
        do {
            int previousCodePoint = previousCodePoint();
            if (previousCodePoint < 0) {
                break;
            }
            i2++;
            nextForCodePoint = charsTrie.nextForCodePoint(previousCodePoint);
            if (nextForCodePoint.hasValue()) {
                cE32FromContexts = charsTrie.getValue();
            }
        } while (nextForCodePoint.hasNext());
        forwardNumCodePoints(i2);
        return cE32FromContexts;
    }

    private final int nextSkippedCodePoint() {
        SkippedState skippedState = this.skipped;
        if (skippedState != null && skippedState.hasNext()) {
            return this.skipped.next();
        }
        if (this.numCpFwd == 0) {
            return -1;
        }
        int nextCodePoint = nextCodePoint();
        SkippedState skippedState2 = this.skipped;
        if (skippedState2 != null && !skippedState2.isEmpty() && nextCodePoint >= 0) {
            this.skipped.incBeyond();
        }
        int i = this.numCpFwd;
        if (i > 0 && nextCodePoint >= 0) {
            this.numCpFwd = i - 1;
        }
        return nextCodePoint;
    }

    private final void backwardNumSkipped(int i) {
        SkippedState skippedState = this.skipped;
        if (skippedState != null && !skippedState.isEmpty()) {
            i = this.skipped.backwardNumCodePoints(i);
        }
        backwardNumCodePoints(i);
        int i2 = this.numCpFwd;
        if (i2 >= 0) {
            this.numCpFwd = i2 + i;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005f, code lost:
        if ((r8 & 1024) == 0) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0063, code lost:
        if ((r8 & 256) == 0) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0065, code lost:
        if (r11 >= r12) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0067, code lost:
        if (r11 <= 1) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0069, code lost:
        backwardNumSkipped(r11);
        r12 = r12 - (r11 - 1);
        r5 = nextSkippedCodePoint();
        r11 = 1;
        r4 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0077, code lost:
        r4 = r12;
        r5 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007f, code lost:
        if (r7.getFCD16(r5) <= 255) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0087, code lost:
        return nextCE32FromDiscontiguousContraction(r7, r2, r3, r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0088, code lost:
        backwardNumSkipped(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x008b, code lost:
        return r3;
     */
    private final int nextCE32FromContraction(CollationData collationData, int i, CharSequence charSequence, int i2, int i3, int i4) {
        int nextSkippedCodePoint;
        int value;
        int nextSkippedCodePoint2;
        CharsTrie charsTrie = new CharsTrie(charSequence, i2);
        SkippedState skippedState = this.skipped;
        if (skippedState != null && !skippedState.isEmpty()) {
            this.skipped.saveTrieState(charsTrie);
        }
        BytesTrie.Result firstForCodePoint = charsTrie.firstForCodePoint(i4);
        int i5 = i3;
        int i6 = i4;
        int i7 = 1;
        int i8 = 1;
        while (true) {
            if (firstForCodePoint.hasValue()) {
                value = charsTrie.getValue();
                if (!firstForCodePoint.hasNext() || (nextSkippedCodePoint2 = nextSkippedCodePoint()) < 0) {
                    break;
                }
                SkippedState skippedState2 = this.skipped;
                if (skippedState2 != null && !skippedState2.isEmpty()) {
                    this.skipped.saveTrieState(charsTrie);
                }
                i6 = nextSkippedCodePoint2;
                i5 = value;
                i7 = 1;
            } else if (firstForCodePoint == BytesTrie.Result.NO_MATCH || (nextSkippedCodePoint = nextSkippedCodePoint()) < 0) {
                break;
            } else {
                i7++;
                i6 = nextSkippedCodePoint;
            }
            i8++;
            firstForCodePoint = charsTrie.nextForCodePoint(i6);
        }
        return value;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00a4 A[EDGE_INSN: B:50:0x00a4->B:36:0x00a4 ?: BREAK  , SYNTHETIC] */
    private final int nextCE32FromDiscontiguousContraction(CollationData collationData, CharsTrie charsTrie, int i, int i2, int i3) {
        int fcd16 = collationData.getFCD16(i3);
        int nextSkippedCodePoint = nextSkippedCodePoint();
        if (nextSkippedCodePoint < 0) {
            backwardNumSkipped(1);
            return i;
        }
        int i4 = i2 + 1;
        int i5 = fcd16 & 255;
        int fcd162 = collationData.getFCD16(nextSkippedCodePoint);
        int i6 = 2;
        if (fcd162 <= 255) {
            backwardNumSkipped(2);
            return i;
        }
        SkippedState skippedState = this.skipped;
        if (skippedState == null || skippedState.isEmpty()) {
            if (this.skipped == null) {
                this.skipped = new SkippedState();
            }
            charsTrie.reset();
            if (i4 > 2) {
                backwardNumCodePoints(i4);
                charsTrie.firstForCodePoint(nextCodePoint());
                for (int i7 = 3; i7 < i4; i7++) {
                    charsTrie.nextForCodePoint(nextCodePoint());
                }
                forwardNumCodePoints(2);
            }
            this.skipped.saveTrieState(charsTrie);
        } else {
            this.skipped.resetToTrieState(charsTrie);
        }
        this.skipped.setFirstSkipped(i3);
        do {
            if (i5 < (fcd162 >> 8)) {
                BytesTrie.Result nextForCodePoint = charsTrie.nextForCodePoint(nextSkippedCodePoint);
                if (nextForCodePoint.hasValue()) {
                    i = charsTrie.getValue();
                    i6 = 0;
                    this.skipped.recordMatch();
                    if (!nextForCodePoint.hasNext()) {
                        break;
                    }
                    this.skipped.saveTrieState(charsTrie);
                    nextSkippedCodePoint = nextSkippedCodePoint();
                    if (nextSkippedCodePoint >= 0) {
                        break;
                    }
                    i6++;
                    fcd162 = collationData.getFCD16(nextSkippedCodePoint);
                }
            }
            this.skipped.skip(nextSkippedCodePoint);
            this.skipped.resetToTrieState(charsTrie);
            i5 = fcd162 & 255;
            nextSkippedCodePoint = nextSkippedCodePoint();
            if (nextSkippedCodePoint >= 0) {
            }
        } while (fcd162 > 255);
        backwardNumSkipped(i6);
        boolean isEmpty = this.skipped.isEmpty();
        this.skipped.replaceMatch();
        if (!isEmpty || this.skipped.isEmpty()) {
            return i;
        }
        int i8 = -1;
        while (true) {
            appendCEsFromCE32(collationData, i8, i, true);
            if (!this.skipped.hasNext()) {
                this.skipped.clear();
                return 1;
            }
            i8 = this.skipped.next();
            i = getDataCE32(i8);
            if (i == 192) {
                collationData = this.data.base;
                i = collationData.getCE32(i8);
            } else {
                collationData = this.data;
            }
        }
    }

    private final long previousCEUnsafe(int i, UVector32 uVector32) {
        int previousCodePoint;
        int i2 = 1;
        do {
            previousCodePoint = previousCodePoint();
            if (previousCodePoint < 0) {
                break;
            }
            i2++;
        } while (this.data.isUnsafeBackward(previousCodePoint, this.isNumeric));
        this.numCpFwd = i2;
        this.cesIndex = 0;
        int offset = getOffset();
        while (true) {
            int i3 = this.numCpFwd;
            if (i3 > 0) {
                this.numCpFwd = i3 - 1;
                nextCE();
                this.cesIndex = this.ceBuffer.length;
                uVector32.addElement(offset);
                offset = getOffset();
                while (uVector32.size() < this.ceBuffer.length) {
                    uVector32.addElement(offset);
                }
            } else {
                uVector32.addElement(offset);
                this.numCpFwd = -1;
                backwardNumCodePoints(i2);
                this.cesIndex = 0;
                CEBuffer cEBuffer = this.ceBuffer;
                int i4 = cEBuffer.length - 1;
                cEBuffer.length = i4;
                return cEBuffer.get(i4);
            }
        }
    }

    private final void appendNumericCEs(int i, boolean z) {
        int nextCodePoint;
        StringBuilder sb = new StringBuilder();
        if (z) {
            while (true) {
                sb.append(Collation.digitFromCE32(i));
                if (this.numCpFwd == 0 || (nextCodePoint = nextCodePoint()) < 0) {
                    break;
                }
                int ce32 = this.data.getCE32(nextCodePoint);
                i = ce32 == 192 ? this.data.base.getCE32(nextCodePoint) : ce32;
                if (!Collation.hasCE32Tag(i, 10)) {
                    backwardNumCodePoints(1);
                    break;
                }
                int i2 = this.numCpFwd;
                if (i2 > 0) {
                    this.numCpFwd = i2 - 1;
                }
            }
        } else {
            while (true) {
                sb.append(Collation.digitFromCE32(i));
                int previousCodePoint = previousCodePoint();
                if (previousCodePoint < 0) {
                    break;
                }
                int ce322 = this.data.getCE32(previousCodePoint);
                i = ce322 == 192 ? this.data.base.getCE32(previousCodePoint) : ce322;
                if (!Collation.hasCE32Tag(i, 10)) {
                    forwardNumCodePoints(1);
                    break;
                }
            }
            sb.reverse();
        }
        int i3 = 0;
        while (true) {
            if (i3 >= sb.length() - 1 || sb.charAt(i3) != 0) {
                int length = sb.length() - i3;
                if (length > 254) {
                    length = 254;
                }
                int i4 = length + i3;
                appendNumericSegmentCEs(sb.subSequence(i3, i4));
                if (i4 < sb.length()) {
                    i3 = i4;
                } else {
                    return;
                }
            } else {
                i3++;
            }
        }
    }

    private final void appendNumericSegmentCEs(CharSequence charSequence) {
        int i;
        int i2;
        int length = charSequence.length();
        long j = this.data.numericPrimary;
        int i3 = 8;
        if (length <= 7) {
            int charAt = charSequence.charAt(0);
            for (int i4 = 1; i4 < length; i4++) {
                charAt = (charAt * 10) + charSequence.charAt(i4);
            }
            if (charAt < 74) {
                this.ceBuffer.append(Collation.makeCE(j | ((long) ((charAt + 2) << 16))));
                return;
            }
            int i5 = charAt - 74;
            if (i5 < 10160) {
                this.ceBuffer.append(Collation.makeCE(j | ((long) ((76 + (i5 / 254)) << 16)) | ((long) (((i5 % 254) + 2) << 8))));
                return;
            }
            int i6 = i5 - 10160;
            if (i6 < 1032256) {
                int i7 = i6 / 254;
                this.ceBuffer.append(Collation.makeCE(j | ((long) ((i6 % 254) + 2)) | ((long) (((i7 % 254) + 2) << 8)) | ((long) ((116 + ((i7 / 254) % 254)) << 16))));
                return;
            }
        }
        long j2 = ((long) ((((length + 1) / 2) + 128) << 16)) | j;
        while (charSequence.charAt(length - 1) == 0 && charSequence.charAt(length - 2) == 0) {
            length -= 2;
        }
        if ((length & 1) != 0) {
            i2 = charSequence.charAt(0);
            i = 1;
        } else {
            i2 = (charSequence.charAt(0) * '\n') + charSequence.charAt(1);
            i = 2;
        }
        int i8 = (i2 * 2) + 11;
        while (i < length) {
            if (i3 == 0) {
                this.ceBuffer.append(Collation.makeCE(((long) i8) | j2));
                j2 = j;
                i3 = 16;
            } else {
                j2 |= (long) (i8 << i3);
                i3 -= 8;
            }
            i8 = (((charSequence.charAt(i) * '\n') + charSequence.charAt(i + 1)) * 2) + 11;
            i += 2;
        }
        this.ceBuffer.append(Collation.makeCE(((long) ((i8 - 1) << i3)) | j2));
    }
}
