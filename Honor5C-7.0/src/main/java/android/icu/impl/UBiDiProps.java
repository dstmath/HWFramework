package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2.Range;
import android.icu.lang.UProperty;
import android.icu.text.UnicodeSet;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public final class UBiDiProps {
    private static final int BIDI_CONTROL_SHIFT = 11;
    private static final int BPT_MASK = 768;
    private static final int BPT_SHIFT = 8;
    private static final int CLASS_MASK = 31;
    private static final String DATA_FILE_NAME = "ubidi.icu";
    private static final String DATA_NAME = "ubidi";
    private static final String DATA_TYPE = "icu";
    private static final int ESC_MIRROR_DELTA = -4;
    private static final int FMT = 1114195049;
    public static final UBiDiProps INSTANCE = null;
    private static final int IS_MIRRORED_SHIFT = 12;
    private static final int IX_JG_LIMIT = 5;
    private static final int IX_JG_LIMIT2 = 7;
    private static final int IX_JG_START = 4;
    private static final int IX_JG_START2 = 6;
    private static final int IX_MAX_VALUES = 15;
    private static final int IX_MIRROR_LENGTH = 3;
    private static final int IX_TOP = 16;
    private static final int IX_TRIE_SIZE = 2;
    private static final int JOIN_CONTROL_SHIFT = 10;
    private static final int JT_MASK = 224;
    private static final int JT_SHIFT = 5;
    private static final int MAX_JG_MASK = 16711680;
    private static final int MAX_JG_SHIFT = 16;
    private static final int MIRROR_DELTA_SHIFT = 13;
    private static final int MIRROR_INDEX_SHIFT = 21;
    private int[] indexes;
    private byte[] jgArray;
    private byte[] jgArray2;
    private int[] mirrors;
    private Trie2_16 trie;

    private static final class IsAcceptable implements Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == UBiDiProps.IX_TRIE_SIZE;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UBiDiProps.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.UBiDiProps.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UBiDiProps.<clinit>():void");
    }

    private UBiDiProps() throws IOException {
        readData(ICUBinary.getData(DATA_FILE_NAME));
    }

    private void readData(ByteBuffer bytes) throws IOException {
        ICUBinary.readHeader(bytes, FMT, new IsAcceptable());
        int count = bytes.getInt();
        if (count < MAX_JG_SHIFT) {
            throw new IOException("indexes[0] too small in ubidi.icu");
        }
        this.indexes = new int[count];
        this.indexes[0] = count;
        for (int i = 1; i < count; i++) {
            this.indexes[i] = bytes.getInt();
        }
        this.trie = Trie2_16.createFromSerialized(bytes);
        int expectedTrieLength = this.indexes[IX_TRIE_SIZE];
        int trieLength = this.trie.getSerializedLength();
        if (trieLength > expectedTrieLength) {
            throw new IOException("ubidi.icu: not enough bytes for the trie");
        }
        ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
        count = this.indexes[IX_MIRROR_LENGTH];
        if (count > 0) {
            this.mirrors = ICUBinary.getInts(bytes, count, 0);
        }
        this.jgArray = new byte[(this.indexes[JT_SHIFT] - this.indexes[IX_JG_START])];
        bytes.get(this.jgArray);
        this.jgArray2 = new byte[(this.indexes[IX_JG_LIMIT2] - this.indexes[IX_JG_START2])];
        bytes.get(this.jgArray2);
    }

    public final void addPropertyStarts(UnicodeSet set) {
        int i;
        Iterator<Range> trieIterator = this.trie.iterator();
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if (range.leadSurrogate) {
                break;
            }
            set.add(range.startCodePoint);
        }
        int length = this.indexes[IX_MIRROR_LENGTH];
        for (i = 0; i < length; i++) {
            int c = getMirrorCodePoint(this.mirrors[i]);
            set.add(c, c + 1);
        }
        int start = this.indexes[IX_JG_START];
        int limit = this.indexes[JT_SHIFT];
        byte[] jga = this.jgArray;
        while (true) {
            length = limit - start;
            byte prev = (byte) 0;
            for (i = 0; i < length; i++) {
                byte jg = jga[i];
                if (jg != prev) {
                    set.add(start);
                    prev = jg;
                }
                start++;
            }
            if (prev != null) {
                set.add(limit);
            }
            if (limit == this.indexes[JT_SHIFT]) {
                start = this.indexes[IX_JG_START2];
                limit = this.indexes[IX_JG_LIMIT2];
                jga = this.jgArray2;
            } else {
                return;
            }
        }
    }

    public final int getMaxValue(int which) {
        int max = this.indexes[IX_MAX_VALUES];
        switch (which) {
            case VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS /*4096*/:
                return max & CLASS_MASK;
            case UProperty.JOINING_GROUP /*4102*/:
                return (MAX_JG_MASK & max) >> MAX_JG_SHIFT;
            case UProperty.JOINING_TYPE /*4103*/:
                return (max & JT_MASK) >> JT_SHIFT;
            case UProperty.BIDI_PAIRED_BRACKET_TYPE /*4117*/:
                return (max & BPT_MASK) >> BPT_SHIFT;
            default:
                return -1;
        }
    }

    public final int getClass(int c) {
        return getClassFromProps(this.trie.get(c));
    }

    public final boolean isMirrored(int c) {
        return getFlagFromProps(this.trie.get(c), IS_MIRRORED_SHIFT);
    }

    private final int getMirror(int c, int props) {
        int delta = getMirrorDeltaFromProps(props);
        if (delta != ESC_MIRROR_DELTA) {
            return c + delta;
        }
        int length = this.indexes[IX_MIRROR_LENGTH];
        for (int i = 0; i < length; i++) {
            int m = this.mirrors[i];
            int c2 = getMirrorCodePoint(m);
            if (c == c2) {
                return getMirrorCodePoint(this.mirrors[getMirrorIndex(m)]);
            }
            if (c < c2) {
                break;
            }
        }
        return c;
    }

    public final int getMirror(int c) {
        return getMirror(c, this.trie.get(c));
    }

    public final boolean isBidiControl(int c) {
        return getFlagFromProps(this.trie.get(c), BIDI_CONTROL_SHIFT);
    }

    public final boolean isJoinControl(int c) {
        return getFlagFromProps(this.trie.get(c), JOIN_CONTROL_SHIFT);
    }

    public final int getJoiningType(int c) {
        return (this.trie.get(c) & JT_MASK) >> JT_SHIFT;
    }

    public final int getJoiningGroup(int c) {
        int start = this.indexes[IX_JG_START];
        int limit = this.indexes[JT_SHIFT];
        if (start <= c && c < limit) {
            return this.jgArray[c - start] & Opcodes.OP_CONST_CLASS_JUMBO;
        }
        start = this.indexes[IX_JG_START2];
        limit = this.indexes[IX_JG_LIMIT2];
        if (start > c || c >= limit) {
            return 0;
        }
        return this.jgArray2[c - start] & Opcodes.OP_CONST_CLASS_JUMBO;
    }

    public final int getPairedBracketType(int c) {
        return (this.trie.get(c) & BPT_MASK) >> BPT_SHIFT;
    }

    public final int getPairedBracket(int c) {
        int props = this.trie.get(c);
        if ((props & BPT_MASK) == 0) {
            return c;
        }
        return getMirror(c, props);
    }

    private static final int getClassFromProps(int props) {
        return props & CLASS_MASK;
    }

    private static final boolean getFlagFromProps(int props, int shift) {
        return ((props >> shift) & 1) != 0;
    }

    private static final int getMirrorDeltaFromProps(int props) {
        return ((short) props) >> MIRROR_DELTA_SHIFT;
    }

    private static final int getMirrorCodePoint(int m) {
        return DictionaryData.TRANSFORM_OFFSET_MASK & m;
    }

    private static final int getMirrorIndex(int m) {
        return m >>> MIRROR_INDEX_SHIFT;
    }
}
