package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import libcore.icu.DateUtilsBridge;

public abstract class Trie {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    protected static final int BMP_INDEX_LENGTH = 2048;
    protected static final int DATA_BLOCK_LENGTH = 32;
    protected static final int HEADER_LENGTH_ = 16;
    protected static final int HEADER_OPTIONS_DATA_IS_32_BIT_ = 256;
    protected static final int HEADER_OPTIONS_INDEX_SHIFT_ = 4;
    protected static final int HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_ = 512;
    private static final int HEADER_OPTIONS_SHIFT_MASK_ = 15;
    protected static final int HEADER_SIGNATURE_ = 1416784229;
    protected static final int INDEX_STAGE_1_SHIFT_ = 5;
    protected static final int INDEX_STAGE_2_SHIFT_ = 2;
    protected static final int INDEX_STAGE_3_MASK_ = 31;
    protected static final int LEAD_INDEX_OFFSET_ = 320;
    protected static final int SURROGATE_BLOCK_BITS = 5;
    protected static final int SURROGATE_BLOCK_COUNT = 32;
    protected static final int SURROGATE_MASK_ = 1023;
    protected int m_dataLength_;
    protected DataManipulate m_dataManipulate_;
    protected int m_dataOffset_;
    protected char[] m_index_;
    private boolean m_isLatin1Linear_;
    private int m_options_;

    public interface DataManipulate {
        int getFoldingOffset(int i);
    }

    private static class DefaultGetFoldingOffset implements DataManipulate {
        private DefaultGetFoldingOffset() {
        }

        public int getFoldingOffset(int value) {
            return value;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.Trie.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.Trie.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie.<clinit>():void");
    }

    protected abstract int getInitialValue();

    protected abstract int getSurrogateOffset(char c, char c2);

    protected abstract int getValue(int i);

    public final boolean isLatin1Linear() {
        return this.m_isLatin1Linear_;
    }

    public boolean equals(Object other) {
        boolean z = -assertionsDisabled;
        if (other == this) {
            return true;
        }
        if (!(other instanceof Trie)) {
            return -assertionsDisabled;
        }
        Trie othertrie = (Trie) other;
        if (this.m_isLatin1Linear_ == othertrie.m_isLatin1Linear_ && this.m_options_ == othertrie.m_options_ && this.m_dataLength_ == othertrie.m_dataLength_) {
            z = Arrays.equals(this.m_index_, othertrie.m_index_);
        }
        return z;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public int getSerializedDataSize() {
        int result = (this.m_dataOffset_ << 1) + HEADER_LENGTH_;
        if (isCharTrie()) {
            return result + (this.m_dataLength_ << 1);
        }
        if (isIntTrie()) {
            return result + (this.m_dataLength_ << INDEX_STAGE_2_SHIFT_);
        }
        return result;
    }

    protected Trie(ByteBuffer bytes, DataManipulate dataManipulate) {
        boolean z = -assertionsDisabled;
        int signature = bytes.getInt();
        this.m_options_ = bytes.getInt();
        if (checkHeader(signature)) {
            if (dataManipulate != null) {
                this.m_dataManipulate_ = dataManipulate;
            } else {
                this.m_dataManipulate_ = new DefaultGetFoldingOffset();
            }
            if ((this.m_options_ & HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_) != 0) {
                z = true;
            }
            this.m_isLatin1Linear_ = z;
            this.m_dataOffset_ = bytes.getInt();
            this.m_dataLength_ = bytes.getInt();
            unserialize(bytes);
            return;
        }
        throw new IllegalArgumentException("ICU data file error: Trie header authentication failed, please check if you have the most updated ICU data file");
    }

    protected Trie(char[] index, int options, DataManipulate dataManipulate) {
        boolean z = -assertionsDisabled;
        this.m_options_ = options;
        if (dataManipulate != null) {
            this.m_dataManipulate_ = dataManipulate;
        } else {
            this.m_dataManipulate_ = new DefaultGetFoldingOffset();
        }
        if ((this.m_options_ & HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_) != 0) {
            z = true;
        }
        this.m_isLatin1Linear_ = z;
        this.m_index_ = index;
        this.m_dataOffset_ = this.m_index_.length;
    }

    protected final int getRawOffset(int offset, char ch) {
        return (this.m_index_[(ch >> SURROGATE_BLOCK_BITS) + offset] << INDEX_STAGE_2_SHIFT_) + (ch & INDEX_STAGE_3_MASK_);
    }

    protected final int getBMPOffset(char ch) {
        if (ch < UCharacter.MIN_SURROGATE || ch > UCharacter.MAX_HIGH_SURROGATE) {
            return getRawOffset(0, ch);
        }
        return getRawOffset(LEAD_INDEX_OFFSET_, ch);
    }

    protected final int getLeadOffset(char ch) {
        return getRawOffset(0, ch);
    }

    protected final int getCodePointOffset(int ch) {
        if (ch < 0) {
            return -1;
        }
        if (ch < UTF16.SURROGATE_MIN_VALUE) {
            return getRawOffset(0, (char) ch);
        }
        if (ch < DateUtilsBridge.FORMAT_ABBREV_MONTH) {
            return getBMPOffset((char) ch);
        }
        if (ch <= UnicodeSet.MAX_VALUE) {
            return getSurrogateOffset(UTF16.getLeadSurrogate(ch), (char) (ch & SURROGATE_MASK_));
        }
        return -1;
    }

    protected void unserialize(ByteBuffer bytes) {
        this.m_index_ = ICUBinary.getChars(bytes, this.m_dataOffset_, 0);
    }

    protected final boolean isIntTrie() {
        return (this.m_options_ & HEADER_OPTIONS_DATA_IS_32_BIT_) != 0 ? true : -assertionsDisabled;
    }

    protected final boolean isCharTrie() {
        return (this.m_options_ & HEADER_OPTIONS_DATA_IS_32_BIT_) == 0 ? true : -assertionsDisabled;
    }

    private final boolean checkHeader(int signature) {
        if (signature == HEADER_SIGNATURE_ && (this.m_options_ & HEADER_OPTIONS_SHIFT_MASK_) == SURROGATE_BLOCK_BITS && ((this.m_options_ >> HEADER_OPTIONS_INDEX_SHIFT_) & HEADER_OPTIONS_SHIFT_MASK_) == INDEX_STAGE_2_SHIFT_) {
            return true;
        }
        return -assertionsDisabled;
    }
}
