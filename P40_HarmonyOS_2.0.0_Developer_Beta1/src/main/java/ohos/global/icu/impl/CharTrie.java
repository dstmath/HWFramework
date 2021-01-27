package ohos.global.icu.impl;

import java.nio.ByteBuffer;
import ohos.global.icu.impl.Trie;

public class CharTrie extends Trie {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private char[] m_data_;
    private char m_initialValue_;

    @Override // ohos.global.icu.impl.Trie
    public int hashCode() {
        return 42;
    }

    public CharTrie(ByteBuffer byteBuffer, Trie.DataManipulate dataManipulate) {
        super(byteBuffer, dataManipulate);
        if (!isCharTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a char trie.");
        }
    }

    public CharTrie(int i, int i2, Trie.DataManipulate dataManipulate) {
        super(new char[2080], 512, dataManipulate);
        int i3 = i2 != i ? 288 : 256;
        this.m_data_ = new char[i3];
        this.m_dataLength_ = i3;
        char c = (char) i;
        this.m_initialValue_ = c;
        for (int i4 = 0; i4 < 256; i4++) {
            this.m_data_[i4] = c;
        }
        if (i2 != i) {
            char c2 = (char) 64;
            for (int i5 = 1728; i5 < 1760; i5++) {
                this.m_index_[i5] = c2;
            }
            for (int i6 = 256; i6 < 288; i6++) {
                this.m_data_[i6] = (char) i2;
            }
        }
    }

    public final char getCodePointValue(int i) {
        if (i < 0 || i >= 55296) {
            int codePointOffset = getCodePointOffset(i);
            return codePointOffset >= 0 ? this.m_data_[codePointOffset] : this.m_initialValue_;
        }
        return this.m_data_[(this.m_index_[i >> 5] << 2) + (i & 31)];
    }

    public final char getLeadValue(char c) {
        return this.m_data_[getLeadOffset(c)];
    }

    public final char getBMPValue(char c) {
        return this.m_data_[getBMPOffset(c)];
    }

    public final char getSurrogateValue(char c, char c2) {
        int surrogateOffset = getSurrogateOffset(c, c2);
        if (surrogateOffset > 0) {
            return this.m_data_[surrogateOffset];
        }
        return this.m_initialValue_;
    }

    public final char getTrailValue(int i, char c) {
        if (this.m_dataManipulate_ != null) {
            int foldingOffset = this.m_dataManipulate_.getFoldingOffset(i);
            if (foldingOffset > 0) {
                return this.m_data_[getRawOffset(foldingOffset, (char) (c & 1023))];
            }
            return this.m_initialValue_;
        }
        throw new NullPointerException("The field DataManipulate in this Trie is null");
    }

    public final char getLatin1LinearValue(char c) {
        return this.m_data_[this.m_dataOffset_ + 32 + c];
    }

    @Override // ohos.global.icu.impl.Trie
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof CharTrie) || this.m_initialValue_ != ((CharTrie) obj).m_initialValue_) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.Trie
    public final void unserialize(ByteBuffer byteBuffer) {
        this.m_index_ = ICUBinary.getChars(byteBuffer, this.m_dataOffset_ + this.m_dataLength_, 0);
        this.m_data_ = this.m_index_;
        this.m_initialValue_ = this.m_data_[this.m_dataOffset_];
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.Trie
    public final int getSurrogateOffset(char c, char c2) {
        if (this.m_dataManipulate_ != null) {
            int foldingOffset = this.m_dataManipulate_.getFoldingOffset(getLeadValue(c));
            if (foldingOffset > 0) {
                return getRawOffset(foldingOffset, (char) (c2 & 1023));
            }
            return -1;
        }
        throw new NullPointerException("The field DataManipulate in this Trie is null");
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.Trie
    public final int getValue(int i) {
        return this.m_data_[i];
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.Trie
    public final int getInitialValue() {
        return this.m_initialValue_;
    }
}
