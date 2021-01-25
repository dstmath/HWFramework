package ohos.global.icu.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import ohos.global.icu.text.UTF16;

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

    /* access modifiers changed from: protected */
    public abstract int getInitialValue();

    /* access modifiers changed from: protected */
    public abstract int getSurrogateOffset(char c, char c2);

    /* access modifiers changed from: protected */
    public abstract int getValue(int i);

    public int hashCode() {
        return 42;
    }

    private static class DefaultGetFoldingOffset implements DataManipulate {
        @Override // ohos.global.icu.impl.Trie.DataManipulate
        public int getFoldingOffset(int i) {
            return i;
        }

        private DefaultGetFoldingOffset() {
        }
    }

    public final boolean isLatin1Linear() {
        return this.m_isLatin1Linear_;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Trie)) {
            return false;
        }
        Trie trie = (Trie) obj;
        return this.m_isLatin1Linear_ == trie.m_isLatin1Linear_ && this.m_options_ == trie.m_options_ && this.m_dataLength_ == trie.m_dataLength_ && Arrays.equals(this.m_index_, trie.m_index_);
    }

    public int getSerializedDataSize() {
        int i;
        int i2 = (this.m_dataOffset_ << 1) + 16;
        if (isCharTrie()) {
            i = this.m_dataLength_ << 1;
        } else if (!isIntTrie()) {
            return i2;
        } else {
            i = this.m_dataLength_ << 2;
        }
        return i2 + i;
    }

    protected Trie(ByteBuffer byteBuffer, DataManipulate dataManipulate) {
        int i = byteBuffer.getInt();
        this.m_options_ = byteBuffer.getInt();
        if (checkHeader(i)) {
            if (dataManipulate != null) {
                this.m_dataManipulate_ = dataManipulate;
            } else {
                this.m_dataManipulate_ = new DefaultGetFoldingOffset();
            }
            this.m_isLatin1Linear_ = (this.m_options_ & 512) != 0;
            this.m_dataOffset_ = byteBuffer.getInt();
            this.m_dataLength_ = byteBuffer.getInt();
            unserialize(byteBuffer);
            return;
        }
        throw new IllegalArgumentException("ICU data file error: Trie header authentication failed, please check if you have the most updated ICU data file");
    }

    protected Trie(char[] cArr, int i, DataManipulate dataManipulate) {
        this.m_options_ = i;
        if (dataManipulate != null) {
            this.m_dataManipulate_ = dataManipulate;
        } else {
            this.m_dataManipulate_ = new DefaultGetFoldingOffset();
        }
        this.m_isLatin1Linear_ = (this.m_options_ & 512) != 0;
        this.m_index_ = cArr;
        this.m_dataOffset_ = this.m_index_.length;
    }

    /* access modifiers changed from: protected */
    public final int getRawOffset(int i, char c) {
        return (this.m_index_[i + (c >> 5)] << 2) + (c & 31);
    }

    /* access modifiers changed from: protected */
    public final int getBMPOffset(char c) {
        if (c < 55296 || c > 56319) {
            return getRawOffset(0, c);
        }
        return getRawOffset(320, c);
    }

    /* access modifiers changed from: protected */
    public final int getLeadOffset(char c) {
        return getRawOffset(0, c);
    }

    /* access modifiers changed from: protected */
    public final int getCodePointOffset(int i) {
        if (i < 0) {
            return -1;
        }
        if (i < 55296) {
            return getRawOffset(0, (char) i);
        }
        if (i < 65536) {
            return getBMPOffset((char) i);
        }
        if (i <= 1114111) {
            return getSurrogateOffset(UTF16.getLeadSurrogate(i), (char) (i & 1023));
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void unserialize(ByteBuffer byteBuffer) {
        this.m_index_ = ICUBinary.getChars(byteBuffer, this.m_dataOffset_, 0);
    }

    /* access modifiers changed from: protected */
    public final boolean isIntTrie() {
        return (this.m_options_ & 256) != 0;
    }

    /* access modifiers changed from: protected */
    public final boolean isCharTrie() {
        return (this.m_options_ & 256) == 0;
    }

    private final boolean checkHeader(int i) {
        if (i != HEADER_SIGNATURE_) {
            return false;
        }
        int i2 = this.m_options_;
        return (i2 & 15) == 5 && ((i2 >> 4) & 15) == 2;
    }
}
