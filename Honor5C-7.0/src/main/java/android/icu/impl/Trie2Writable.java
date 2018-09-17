package android.icu.impl;

import android.icu.impl.Trie2.Range;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import dalvik.bytecode.Opcodes;
import libcore.icu.DateUtilsBridge;
import org.w3c.dom.traversal.NodeFilter;

public class Trie2Writable extends Trie2 {
    private static final /* synthetic */ int[] -android-icu-impl-Trie2$ValueWidthSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int UNEWTRIE2_DATA_0800_OFFSET = 2176;
    private static final int UNEWTRIE2_DATA_NULL_OFFSET = 192;
    private static final int UNEWTRIE2_DATA_START_OFFSET = 256;
    private static final int UNEWTRIE2_INDEX_2_NULL_OFFSET = 2656;
    private static final int UNEWTRIE2_INDEX_2_START_OFFSET = 2720;
    private static final int UNEWTRIE2_INITIAL_DATA_LENGTH = 16384;
    private static final int UNEWTRIE2_MEDIUM_DATA_LENGTH = 131072;
    private static final int UTRIE2_MAX_DATA_LENGTH = 262140;
    private static final int UTRIE2_MAX_INDEX_LENGTH = 65535;
    private boolean UTRIE2_DEBUG;
    private int[] data;
    private int dataCapacity;
    private int firstFreeBlock;
    private int[] index1;
    private int[] index2;
    private int index2Length;
    private int index2NullOffset;
    private boolean isCompacted;
    private int[] map;

    private static /* synthetic */ int[] -getandroid-icu-impl-Trie2$ValueWidthSwitchesValues() {
        if (-android-icu-impl-Trie2$ValueWidthSwitchesValues != null) {
            return -android-icu-impl-Trie2$ValueWidthSwitchesValues;
        }
        int[] iArr = new int[ValueWidth.values().length];
        try {
            iArr[ValueWidth.BITS_16.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ValueWidth.BITS_32.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-icu-impl-Trie2$ValueWidthSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.Trie2Writable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.Trie2Writable.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2Writable.<clinit>():void");
    }

    public Trie2Writable(int initialValueP, int errorValueP) {
        this.index1 = new int[544];
        this.index2 = new int[35488];
        this.map = new int[34852];
        this.UTRIE2_DEBUG = -assertionsDisabled;
        init(initialValueP, errorValueP);
    }

    private void init(int initialValueP, int errorValueP) {
        this.initialValue = initialValueP;
        this.errorValue = errorValueP;
        this.highStart = PropsVectors.INITIAL_VALUE_CP;
        this.data = new int[UNEWTRIE2_INITIAL_DATA_LENGTH];
        this.dataCapacity = UNEWTRIE2_INITIAL_DATA_LENGTH;
        this.initialValue = initialValueP;
        this.errorValue = errorValueP;
        this.highStart = PropsVectors.INITIAL_VALUE_CP;
        this.firstFreeBlock = 0;
        this.isCompacted = -assertionsDisabled;
        int i = 0;
        while (i < NodeFilter.SHOW_COMMENT) {
            this.data[i] = this.initialValue;
            i++;
        }
        while (i < UNEWTRIE2_DATA_NULL_OFFSET) {
            this.data[i] = this.errorValue;
            i++;
        }
        for (i = UNEWTRIE2_DATA_NULL_OFFSET; i < UNEWTRIE2_DATA_START_OFFSET; i++) {
            this.data[i] = this.initialValue;
        }
        this.dataNullOffset = UNEWTRIE2_DATA_NULL_OFFSET;
        this.dataLength = UNEWTRIE2_DATA_START_OFFSET;
        i = 0;
        int j = 0;
        while (j < NodeFilter.SHOW_COMMENT) {
            this.index2[i] = j;
            this.map[i] = 1;
            i++;
            j += 32;
        }
        int i2 = i;
        while (j < UNEWTRIE2_DATA_NULL_OFFSET) {
            this.map[i2] = 0;
            j += 32;
            i2++;
        }
        i = i2 + 1;
        this.map[i2] = 34845;
        for (j += 32; j < UNEWTRIE2_DATA_START_OFFSET; j += 32) {
            this.map[i] = 0;
            i++;
        }
        for (i = 4; i < 2080; i++) {
            this.index2[i] = UNEWTRIE2_DATA_NULL_OFFSET;
        }
        for (i = 0; i < 576; i++) {
            this.index2[i + 2080] = -1;
        }
        for (i = 0; i < 64; i++) {
            this.index2[i + UNEWTRIE2_INDEX_2_NULL_OFFSET] = UNEWTRIE2_DATA_NULL_OFFSET;
        }
        this.index2NullOffset = UNEWTRIE2_INDEX_2_NULL_OFFSET;
        this.index2Length = UNEWTRIE2_INDEX_2_START_OFFSET;
        i = 0;
        j = 0;
        while (i < 32) {
            this.index1[i] = j;
            i++;
            j += 64;
        }
        while (i < 544) {
            this.index1[i] = UNEWTRIE2_INDEX_2_NULL_OFFSET;
            i++;
        }
        for (i = NodeFilter.SHOW_COMMENT; i < NodeFilter.SHOW_NOTATION; i += 32) {
            set(i, this.initialValue);
        }
    }

    public Trie2Writable(Trie2 source) {
        this.index1 = new int[544];
        this.index2 = new int[35488];
        this.map = new int[34852];
        this.UTRIE2_DEBUG = -assertionsDisabled;
        init(source.initialValue, source.errorValue);
        for (Range r : source) {
            setRange(r, true);
        }
    }

    private boolean isInNullBlock(int c, boolean forLSCP) {
        int i2;
        if (Character.isHighSurrogate((char) c) && forLSCP) {
            i2 = (c >> 5) + 320;
        } else {
            i2 = this.index1[c >> 11] + ((c >> 5) & 63);
        }
        return this.index2[i2] == this.dataNullOffset ? true : -assertionsDisabled;
    }

    private int allocIndex2Block() {
        int newBlock = this.index2Length;
        int newTop = newBlock + 64;
        if (newTop > this.index2.length) {
            throw new IllegalStateException("Internal error in Trie2 creation.");
        }
        this.index2Length = newTop;
        System.arraycopy(this.index2, this.index2NullOffset, this.index2, newBlock, 64);
        return newBlock;
    }

    private int getIndex2Block(int c, boolean forLSCP) {
        if (c >= UTF16.SURROGATE_MIN_VALUE && c < UTF16.TRAIL_SURROGATE_MIN_VALUE && forLSCP) {
            return NodeFilter.SHOW_NOTATION;
        }
        int i1 = c >> 11;
        int i2 = this.index1[i1];
        if (i2 == this.index2NullOffset) {
            i2 = allocIndex2Block();
            this.index1[i1] = i2;
        }
        return i2;
    }

    private int allocDataBlock(int copyBlock) {
        int newBlock;
        if (this.firstFreeBlock != 0) {
            newBlock = this.firstFreeBlock;
            this.firstFreeBlock = -this.map[newBlock >> 5];
        } else {
            newBlock = this.dataLength;
            int newTop = newBlock + 32;
            if (newTop > this.dataCapacity) {
                int capacity;
                if (this.dataCapacity < UNEWTRIE2_MEDIUM_DATA_LENGTH) {
                    capacity = UNEWTRIE2_MEDIUM_DATA_LENGTH;
                } else if (this.dataCapacity < 1115264) {
                    capacity = 1115264;
                } else {
                    throw new IllegalStateException("Internal error in Trie2 creation.");
                }
                int[] newData = new int[capacity];
                System.arraycopy(this.data, 0, newData, 0, this.dataLength);
                this.data = newData;
                this.dataCapacity = capacity;
            }
            this.dataLength = newTop;
        }
        System.arraycopy(this.data, copyBlock, this.data, newBlock, 32);
        this.map[newBlock >> 5] = 0;
        return newBlock;
    }

    private void releaseDataBlock(int block) {
        this.map[block >> 5] = -this.firstFreeBlock;
        this.firstFreeBlock = block;
    }

    private boolean isWritableBlock(int block) {
        return (block == this.dataNullOffset || 1 != this.map[block >> 5]) ? -assertionsDisabled : true;
    }

    private void setIndex2Entry(int i2, int block) {
        int[] iArr = this.map;
        int i = block >> 5;
        iArr[i] = iArr[i] + 1;
        int oldBlock = this.index2[i2];
        iArr = this.map;
        i = oldBlock >> 5;
        int i3 = iArr[i] - 1;
        iArr[i] = i3;
        if (i3 == 0) {
            releaseDataBlock(oldBlock);
        }
        this.index2[i2] = block;
    }

    private int getDataBlock(int c, boolean forLSCP) {
        int i2 = getIndex2Block(c, forLSCP) + ((c >> 5) & 63);
        int oldBlock = this.index2[i2];
        if (isWritableBlock(oldBlock)) {
            return oldBlock;
        }
        int newBlock = allocDataBlock(oldBlock);
        setIndex2Entry(i2, newBlock);
        return newBlock;
    }

    public Trie2Writable set(int c, int value) {
        if (c < 0 || c > UnicodeSet.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point.");
        }
        set(c, true, value);
        this.fHash = 0;
        return this;
    }

    private Trie2Writable set(int c, boolean forLSCP, int value) {
        if (this.isCompacted) {
            uncompact();
        }
        this.data[(c & 31) + getDataBlock(c, forLSCP)] = value;
        return this;
    }

    private void uncompact() {
        Trie2Writable tempTrie = new Trie2Writable(this);
        this.index1 = tempTrie.index1;
        this.index2 = tempTrie.index2;
        this.data = tempTrie.data;
        this.index2Length = tempTrie.index2Length;
        this.dataCapacity = tempTrie.dataCapacity;
        this.isCompacted = tempTrie.isCompacted;
        this.header = tempTrie.header;
        this.index = tempTrie.index;
        this.data16 = tempTrie.data16;
        this.data32 = tempTrie.data32;
        this.indexLength = tempTrie.indexLength;
        this.dataLength = tempTrie.dataLength;
        this.index2NullOffset = tempTrie.index2NullOffset;
        this.initialValue = tempTrie.initialValue;
        this.errorValue = tempTrie.errorValue;
        this.highStart = tempTrie.highStart;
        this.highValueIndex = tempTrie.highValueIndex;
        this.dataNullOffset = tempTrie.dataNullOffset;
    }

    private void writeBlock(int block, int value) {
        int limit = block + 32;
        int block2 = block;
        while (block2 < limit) {
            block = block2 + 1;
            this.data[block2] = value;
            block2 = block;
        }
    }

    private void fillBlock(int block, int start, int limit, int value, int initialValue, boolean overwrite) {
        int pLimit = block + limit;
        int i;
        if (overwrite) {
            for (i = block + start; i < pLimit; i++) {
                this.data[i] = value;
            }
            return;
        }
        for (i = block + start; i < pLimit; i++) {
            if (this.data[i] == initialValue) {
                this.data[i] = value;
            }
        }
    }

    public Trie2Writable setRange(int start, int end, int value, boolean overwrite) {
        if (start > UnicodeSet.MAX_VALUE || start < 0 || end > 1114111 || end < 0 || start > end) {
            throw new IllegalArgumentException("Invalid code point range.");
        } else if (!overwrite && value == this.initialValue) {
            return this;
        } else {
            int block;
            int repeatBlock;
            this.fHash = 0;
            if (this.isCompacted) {
                uncompact();
            }
            int limit = end + 1;
            if ((start & 31) != 0) {
                block = getDataBlock(start, true);
                int nextStart = (start + 32) & -32;
                if (nextStart <= limit) {
                    fillBlock(block, start & 31, 32, value, this.initialValue, overwrite);
                    start = nextStart;
                } else {
                    fillBlock(block, start & 31, limit & 31, value, this.initialValue, overwrite);
                    return this;
                }
            }
            int rest = limit & 31;
            limit &= -32;
            if (value == this.initialValue) {
                repeatBlock = this.dataNullOffset;
            } else {
                repeatBlock = -1;
            }
            while (start < limit) {
                boolean setRepeatBlock = -assertionsDisabled;
                if (value == this.initialValue && isInNullBlock(start, true)) {
                    start += 32;
                } else {
                    int i2 = getIndex2Block(start, true) + ((start >> 5) & 63);
                    block = this.index2[i2];
                    if (isWritableBlock(block)) {
                        if (!overwrite || block < UNEWTRIE2_DATA_0800_OFFSET) {
                            fillBlock(block, 0, 32, value, this.initialValue, overwrite);
                        } else {
                            setRepeatBlock = true;
                        }
                    } else if (this.data[block] != value && (overwrite || block == this.dataNullOffset)) {
                        setRepeatBlock = true;
                    }
                    if (setRepeatBlock) {
                        if (repeatBlock >= 0) {
                            setIndex2Entry(i2, repeatBlock);
                        } else {
                            repeatBlock = getDataBlock(start, true);
                            writeBlock(repeatBlock, value);
                        }
                    }
                    start += 32;
                }
            }
            if (rest > 0) {
                fillBlock(getDataBlock(start, true), 0, rest, value, this.initialValue, overwrite);
            }
            return this;
        }
    }

    public Trie2Writable setRange(Range range, boolean overwrite) {
        this.fHash = 0;
        if (range.leadSurrogate) {
            int c = range.startCodePoint;
            while (c <= range.endCodePoint) {
                if (overwrite || getFromU16SingleLead((char) c) == this.initialValue) {
                    setForLeadSurrogateCodeUnit((char) c, range.value);
                }
                c++;
            }
        } else {
            setRange(range.startCodePoint, range.endCodePoint, range.value, overwrite);
        }
        return this;
    }

    public Trie2Writable setForLeadSurrogateCodeUnit(char codeUnit, int value) {
        this.fHash = 0;
        set(codeUnit, -assertionsDisabled, value);
        return this;
    }

    public int get(int codePoint) {
        if (codePoint < 0 || codePoint > UnicodeSet.MAX_VALUE) {
            return this.errorValue;
        }
        return get(codePoint, true);
    }

    private int get(int c, boolean fromLSCP) {
        if (c >= this.highStart && (c < UTF16.SURROGATE_MIN_VALUE || c >= UTF16.TRAIL_SURROGATE_MIN_VALUE || fromLSCP)) {
            return this.data[this.dataLength - 4];
        }
        int i2;
        if (c < UTF16.SURROGATE_MIN_VALUE || c >= UTF16.TRAIL_SURROGATE_MIN_VALUE || !fromLSCP) {
            i2 = this.index1[c >> 11] + ((c >> 5) & 63);
        } else {
            i2 = (c >> 5) + 320;
        }
        return this.data[(c & 31) + this.index2[i2]];
    }

    public int getFromU16SingleLead(char c) {
        return get(c, -assertionsDisabled);
    }

    private boolean equal_int(int[] a, int s, int t, int length) {
        for (int i = 0; i < length; i++) {
            if (a[s + i] != a[t + i]) {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    private int findSameIndex2Block(int index2Length, int otherBlock) {
        index2Length -= 64;
        for (int block = 0; block <= index2Length; block++) {
            if (equal_int(this.index2, block, otherBlock, 64)) {
                return block;
            }
        }
        return -1;
    }

    private int findSameDataBlock(int dataLength, int otherBlock, int blockLength) {
        dataLength -= blockLength;
        for (int block = 0; block <= dataLength; block += 4) {
            if (equal_int(this.data, block, otherBlock, blockLength)) {
                return block;
            }
        }
        return -1;
    }

    private int findHighStart(int highValue) {
        int prevI2Block;
        int prevBlock;
        if (highValue == this.initialValue) {
            prevI2Block = this.index2NullOffset;
            prevBlock = this.dataNullOffset;
        } else {
            prevI2Block = -1;
            prevBlock = -1;
        }
        int i1 = 544;
        int c = PropsVectors.INITIAL_VALUE_CP;
        while (c > 0) {
            i1--;
            int i2Block = this.index1[i1];
            if (i2Block == prevI2Block) {
                c -= 2048;
            } else {
                prevI2Block = i2Block;
                if (i2Block != this.index2NullOffset) {
                    int i2 = 64;
                    while (i2 > 0) {
                        i2--;
                        int block = this.index2[i2Block + i2];
                        if (block == prevBlock) {
                            c -= 32;
                        } else {
                            prevBlock = block;
                            if (block != this.dataNullOffset) {
                                int j = 32;
                                while (j > 0) {
                                    j--;
                                    if (this.data[block + j] != highValue) {
                                        return c;
                                    }
                                    c--;
                                }
                                continue;
                            } else if (highValue != this.initialValue) {
                                return c;
                            } else {
                                c -= 32;
                            }
                        }
                    }
                    continue;
                } else if (highValue != this.initialValue) {
                    return c;
                } else {
                    c -= 2048;
                }
            }
        }
        return 0;
    }

    private void compactData() {
        int newStart;
        int newStart2 = UNEWTRIE2_DATA_NULL_OFFSET;
        int start = 0;
        int i = 0;
        while (start < UNEWTRIE2_DATA_NULL_OFFSET) {
            this.map[i] = start;
            start += 32;
            i++;
        }
        int blockLength = 64;
        int blockCount = 2;
        start = UNEWTRIE2_DATA_NULL_OFFSET;
        while (start < this.dataLength) {
            if (start == UNEWTRIE2_DATA_0800_OFFSET) {
                blockLength = 32;
                blockCount = 1;
            }
            if (this.map[start >> 5] <= 0) {
                start += blockLength;
            } else {
                int movedStart = findSameDataBlock(newStart2, start, blockLength);
                int mapIndex;
                int mapIndex2;
                if (movedStart >= 0) {
                    i = blockCount;
                    mapIndex = start >> 5;
                    while (i > 0) {
                        mapIndex2 = mapIndex + 1;
                        this.map[mapIndex] = movedStart;
                        movedStart += 32;
                        i--;
                        mapIndex = mapIndex2;
                    }
                    start += blockLength;
                } else {
                    int overlap = blockLength - 4;
                    while (overlap > 0 && !equal_int(this.data, newStart2 - overlap, start, overlap)) {
                        overlap -= 4;
                    }
                    if (overlap > 0 || newStart2 < start) {
                        movedStart = newStart2 - overlap;
                        i = blockCount;
                        mapIndex = start >> 5;
                        while (i > 0) {
                            mapIndex2 = mapIndex + 1;
                            this.map[mapIndex] = movedStart;
                            movedStart += 32;
                            i--;
                            mapIndex = mapIndex2;
                        }
                        i = blockLength - overlap;
                        newStart = newStart2;
                        int start2 = start + overlap;
                        while (i > 0) {
                            newStart2 = newStart + 1;
                            start = start2 + 1;
                            this.data[newStart] = this.data[start2];
                            i--;
                            newStart = newStart2;
                            start2 = start;
                        }
                        newStart2 = newStart;
                        start = start2;
                    } else {
                        i = blockCount;
                        mapIndex = start >> 5;
                        while (i > 0) {
                            mapIndex2 = mapIndex + 1;
                            this.map[mapIndex] = start;
                            start += 32;
                            i--;
                            mapIndex = mapIndex2;
                        }
                        newStart2 = start;
                    }
                }
            }
        }
        i = 0;
        while (i < this.index2Length) {
            if (i == 2080) {
                i += 576;
            }
            this.index2[i] = this.map[this.index2[i] >> 5];
            i++;
        }
        this.dataNullOffset = this.map[this.dataNullOffset >> 5];
        newStart = newStart2;
        while ((newStart & 3) != 0) {
            newStart2 = newStart + 1;
            this.data[newStart] = this.initialValue;
            newStart = newStart2;
        }
        if (this.UTRIE2_DEBUG) {
            System.out.printf("compacting UTrie2: count of 32-bit data words %d->%d\n", new Object[]{Integer.valueOf(this.dataLength), Integer.valueOf(newStart)});
        }
        this.dataLength = newStart;
    }

    private void compactIndex2() {
        int newStart;
        int start = 0;
        int i = 0;
        while (start < 2080) {
            this.map[i] = start;
            start += 64;
            i++;
        }
        int newStart2 = (((this.highStart - DateUtilsBridge.FORMAT_ABBREV_MONTH) >> 11) + 32) + 2080;
        start = UNEWTRIE2_INDEX_2_NULL_OFFSET;
        while (start < this.index2Length) {
            int movedStart = findSameIndex2Block(newStart2, start);
            if (movedStart >= 0) {
                this.map[start >> 6] = movedStart;
                start += 64;
            } else {
                int overlap = 63;
                while (overlap > 0 && !equal_int(this.index2, newStart2 - overlap, start, overlap)) {
                    overlap--;
                }
                if (overlap > 0 || newStart2 < start) {
                    this.map[start >> 6] = newStart2 - overlap;
                    i = 64 - overlap;
                    newStart = newStart2;
                    int start2 = start + overlap;
                    while (i > 0) {
                        newStart2 = newStart + 1;
                        start = start2 + 1;
                        this.index2[newStart] = this.index2[start2];
                        i--;
                        newStart = newStart2;
                        start2 = start;
                    }
                    newStart2 = newStart;
                    start = start2;
                } else {
                    this.map[start >> 6] = start;
                    start += 64;
                    newStart2 = start;
                }
            }
        }
        for (i = 0; i < 544; i++) {
            this.index1[i] = this.map[this.index1[i] >> 6];
        }
        this.index2NullOffset = this.map[this.index2NullOffset >> 6];
        newStart = newStart2;
        while ((newStart & 3) != 0) {
            newStart2 = newStart + 1;
            this.index2[newStart] = UTRIE2_MAX_DATA_LENGTH;
            newStart = newStart2;
        }
        if (this.UTRIE2_DEBUG) {
            System.out.printf("compacting UTrie2: count of 16-bit index-2 words %d->%d\n", new Object[]{Integer.valueOf(this.index2Length), Integer.valueOf(newStart)});
        }
        this.index2Length = newStart;
    }

    private void compactTrie() {
        int highValue = get(UnicodeSet.MAX_VALUE);
        int localHighStart = (findHighStart(highValue) + Opcodes.OP_IGET_WIDE_JUMBO) & -2048;
        if (localHighStart == PropsVectors.INITIAL_VALUE_CP) {
            highValue = this.errorValue;
        }
        this.highStart = localHighStart;
        if (this.UTRIE2_DEBUG) {
            System.out.printf("UTrie2: highStart U+%04x  highValue 0x%x  initialValue 0x%x\n", new Object[]{Integer.valueOf(this.highStart), Integer.valueOf(highValue), Integer.valueOf(this.initialValue)});
        }
        if (this.highStart < PropsVectors.INITIAL_VALUE_CP) {
            setRange(this.highStart <= DateUtilsBridge.FORMAT_ABBREV_MONTH ? DateUtilsBridge.FORMAT_ABBREV_MONTH : this.highStart, UnicodeSet.MAX_VALUE, this.initialValue, true);
        }
        compactData();
        if (this.highStart > DateUtilsBridge.FORMAT_ABBREV_MONTH) {
            compactIndex2();
        } else if (this.UTRIE2_DEBUG) {
            System.out.printf("UTrie2: highStart U+%04x  count of 16-bit index-2 words %d->%d\n", new Object[]{Integer.valueOf(this.highStart), Integer.valueOf(this.index2Length), Integer.valueOf(2112)});
        }
        int[] iArr = this.data;
        int i = this.dataLength;
        this.dataLength = i + 1;
        iArr[i] = highValue;
        while ((this.dataLength & 3) != 0) {
            iArr = this.data;
            i = this.dataLength;
            this.dataLength = i + 1;
            iArr[i] = this.initialValue;
        }
        this.isCompacted = true;
    }

    public Trie2_16 toTrie2_16() {
        Trie2_16 frozenTrie = new Trie2_16();
        freeze(frozenTrie, ValueWidth.BITS_16);
        return frozenTrie;
    }

    public Trie2_32 toTrie2_32() {
        Trie2_32 frozenTrie = new Trie2_32();
        freeze(frozenTrie, ValueWidth.BITS_32);
        return frozenTrie;
    }

    private void freeze(Trie2 dest, ValueWidth valueBits) {
        int allIndexesLength;
        int dataMove;
        if (!this.isCompacted) {
            compactTrie();
        }
        if (this.highStart <= DateUtilsBridge.FORMAT_ABBREV_MONTH) {
            allIndexesLength = 2112;
        } else {
            allIndexesLength = this.index2Length;
        }
        if (valueBits == ValueWidth.BITS_16) {
            dataMove = allIndexesLength;
        } else {
            dataMove = 0;
        }
        if (allIndexesLength > UTRIE2_MAX_INDEX_LENGTH || this.dataNullOffset + dataMove > UTRIE2_MAX_INDEX_LENGTH || dataMove + UNEWTRIE2_DATA_0800_OFFSET > UTRIE2_MAX_INDEX_LENGTH || this.dataLength + dataMove > UTRIE2_MAX_DATA_LENGTH) {
            throw new UnsupportedOperationException("Trie2 data is too large.");
        }
        int indexLength = allIndexesLength;
        if (valueBits == ValueWidth.BITS_16) {
            indexLength += this.dataLength;
        } else {
            dest.data32 = new int[this.dataLength];
        }
        dest.index = new char[indexLength];
        dest.indexLength = allIndexesLength;
        dest.dataLength = this.dataLength;
        if (this.highStart <= DateUtilsBridge.FORMAT_ABBREV_MONTH) {
            dest.index2NullOffset = UTRIE2_MAX_INDEX_LENGTH;
        } else {
            dest.index2NullOffset = this.index2NullOffset + 0;
        }
        dest.initialValue = this.initialValue;
        dest.errorValue = this.errorValue;
        dest.highStart = this.highStart;
        dest.highValueIndex = (this.dataLength + dataMove) - 4;
        dest.dataNullOffset = this.dataNullOffset + dataMove;
        dest.header = new UTrie2Header();
        dest.header.signature = 1416784178;
        dest.header.options = valueBits == ValueWidth.BITS_16 ? 0 : 1;
        dest.header.indexLength = dest.indexLength;
        dest.header.shiftedDataLength = dest.dataLength >> 2;
        dest.header.index2NullOffset = dest.index2NullOffset;
        dest.header.dataNullOffset = dest.dataNullOffset;
        dest.header.shiftedHighStart = dest.highStart >> 11;
        int i = 0;
        int destIdx = 0;
        while (i < 2080) {
            int destIdx2 = destIdx + 1;
            dest.index[destIdx] = (char) ((this.index2[i] + dataMove) >> 2);
            i++;
            destIdx = destIdx2;
        }
        if (this.UTRIE2_DEBUG) {
            System.out.println("\n\nIndex2 for BMP limit is " + Integer.toHexString(destIdx));
        }
        i = 0;
        while (i < 2) {
            destIdx2 = destIdx + 1;
            dest.index[destIdx] = (char) (dataMove + NodeFilter.SHOW_COMMENT);
            i++;
            destIdx = destIdx2;
        }
        while (i < 32) {
            destIdx2 = destIdx + 1;
            dest.index[destIdx] = (char) (this.index2[i << 1] + dataMove);
            i++;
            destIdx = destIdx2;
        }
        if (this.UTRIE2_DEBUG) {
            System.out.println("Index2 for UTF-8 2byte values limit is " + Integer.toHexString(destIdx));
        }
        if (this.highStart > DateUtilsBridge.FORMAT_ABBREV_MONTH) {
            int index1Length = (this.highStart - DateUtilsBridge.FORMAT_ABBREV_MONTH) >> 11;
            int index2Offset = index1Length + 2112;
            i = 0;
            while (i < index1Length) {
                destIdx2 = destIdx + 1;
                dest.index[destIdx] = (char) (this.index1[i + 32] + 0);
                i++;
                destIdx = destIdx2;
            }
            if (this.UTRIE2_DEBUG) {
                System.out.println("Index 1 for supplementals, limit is " + Integer.toHexString(destIdx));
            }
            i = 0;
            destIdx2 = destIdx;
            while (i < this.index2Length - index2Offset) {
                destIdx = destIdx2 + 1;
                dest.index[destIdx2] = (char) ((this.index2[index2Offset + i] + dataMove) >> 2);
                i++;
                destIdx2 = destIdx;
            }
            if (this.UTRIE2_DEBUG) {
                System.out.println("Index 2 for supplementals, limit is " + Integer.toHexString(destIdx2));
            }
        } else {
            destIdx2 = destIdx;
        }
        switch (-getandroid-icu-impl-Trie2$ValueWidthSwitchesValues()[valueBits.ordinal()]) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                if (!-assertionsDisabled) {
                    if ((destIdx2 == dataMove ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                dest.data16 = destIdx2;
                i = 0;
                while (i < this.dataLength) {
                    destIdx = destIdx2 + 1;
                    dest.index[destIdx2] = (char) this.data[i];
                    i++;
                    destIdx2 = destIdx;
                }
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                for (i = 0; i < this.dataLength; i++) {
                    dest.data32[i] = this.data[i];
                }
            default:
        }
    }
}
