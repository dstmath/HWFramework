package android.icu.impl;

import android.icu.text.UTF16;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class Trie {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    /* access modifiers changed from: protected */
    public abstract int getInitialValue();

    /* access modifiers changed from: protected */
    public abstract int getSurrogateOffset(char c, char c2);

    /* access modifiers changed from: protected */
    public abstract int getValue(int i);

    public final boolean isLatin1Linear() {
        return this.m_isLatin1Linear_;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof Trie)) {
            return false;
        }
        Trie othertrie = (Trie) other;
        if (!(this.m_isLatin1Linear_ == othertrie.m_isLatin1Linear_ && this.m_options_ == othertrie.m_options_ && this.m_dataLength_ == othertrie.m_dataLength_ && Arrays.equals(this.m_index_, othertrie.m_index_))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return 42;
    }

    public int getSerializedDataSize() {
        int result = 16 + (this.m_dataOffset_ << 1);
        if (isCharTrie()) {
            return result + (this.m_dataLength_ << 1);
        }
        if (isIntTrie()) {
            return result + (this.m_dataLength_ << 2);
        }
        return result;
    }

    protected Trie(ByteBuffer bytes, DataManipulate dataManipulate) {
        int signature = bytes.getInt();
        this.m_options_ = bytes.getInt();
        if (checkHeader(signature)) {
            if (dataManipulate != null) {
                this.m_dataManipulate_ = dataManipulate;
            } else {
                this.m_dataManipulate_ = new DefaultGetFoldingOffset();
            }
            this.m_isLatin1Linear_ = (this.m_options_ & 512) != 0;
            this.m_dataOffset_ = bytes.getInt();
            this.m_dataLength_ = bytes.getInt();
            unserialize(bytes);
            return;
        }
        throw new IllegalArgumentException("ICU data file error: Trie header authentication failed, please check if you have the most updated ICU data file");
    }

    protected Trie(char[] index, int options, DataManipulate dataManipulate) {
        this.m_options_ = options;
        if (dataManipulate != null) {
            this.m_dataManipulate_ = dataManipulate;
        } else {
            this.m_dataManipulate_ = new DefaultGetFoldingOffset();
        }
        this.m_isLatin1Linear_ = (this.m_options_ & 512) != 0;
        this.m_index_ = index;
        this.m_dataOffset_ = this.m_index_.length;
    }

    /* access modifiers changed from: protected */
    public final int getRawOffset(int offset, char ch) {
        return (this.m_index_[(ch >> 5) + offset] << 2) + (ch & 31);
    }

    /* access modifiers changed from: protected */
    public final int getBMPOffset(char ch) {
        if (ch < 55296 || ch > 56319) {
            return getRawOffset(0, ch);
        }
        return getRawOffset(LEAD_INDEX_OFFSET_, ch);
    }

    /* access modifiers changed from: protected */
    public final int getLeadOffset(char ch) {
        return getRawOffset(0, ch);
    }

    /* access modifiers changed from: protected */
    public final int getCodePointOffset(int ch) {
        if (ch < 0) {
            return -1;
        }
        if (ch < 55296) {
            return getRawOffset(0, (char) ch);
        }
        if (ch < 65536) {
            return getBMPOffset((char) ch);
        }
        if (ch <= 1114111) {
            return getSurrogateOffset(UTF16.getLeadSurrogate(ch), (char) (ch & 1023));
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void unserialize(ByteBuffer bytes) {
        this.m_index_ = ICUBinary.getChars(bytes, this.m_dataOffset_, 0);
    }

    /* access modifiers changed from: protected */
    public final boolean isIntTrie() {
        return (this.m_options_ & 256) != 0;
    }

    /* access modifiers changed from: protected */
    public final boolean isCharTrie() {
        return (this.m_options_ & 256) == 0;
    }

    private final boolean checkHeader(int signature) {
        if (signature == HEADER_SIGNATURE_ && (this.m_options_ & 15) == 5 && ((this.m_options_ >> 4) & 15) == 2) {
            return true;
        }
        return false;
    }
}
