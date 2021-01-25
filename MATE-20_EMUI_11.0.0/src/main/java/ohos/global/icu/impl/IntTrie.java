package ohos.global.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import ohos.global.icu.impl.Trie;
import ohos.global.icu.text.UTF16;

public class IntTrie extends Trie {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int[] m_data_;
    private int m_initialValue_;

    @Override // ohos.global.icu.impl.Trie
    public int hashCode() {
        return 42;
    }

    public IntTrie(ByteBuffer byteBuffer, Trie.DataManipulate dataManipulate) throws IOException {
        super(byteBuffer, dataManipulate);
        if (!isIntTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a int trie.");
        }
    }

    public IntTrie(int i, int i2, Trie.DataManipulate dataManipulate) {
        super(new char[2080], 512, dataManipulate);
        int i3 = i2 != i ? 288 : 256;
        this.m_data_ = new int[i3];
        this.m_dataLength_ = i3;
        this.m_initialValue_ = i;
        for (int i4 = 0; i4 < 256; i4++) {
            this.m_data_[i4] = i;
        }
        if (i2 != i) {
            char c = (char) 64;
            for (int i5 = 1728; i5 < 1760; i5++) {
                this.m_index_[i5] = c;
            }
            for (int i6 = 256; i6 < 288; i6++) {
                this.m_data_[i6] = i2;
            }
        }
    }

    public final int getCodePointValue(int i) {
        if (i < 0 || i >= 55296) {
            int codePointOffset = getCodePointOffset(i);
            return codePointOffset >= 0 ? this.m_data_[codePointOffset] : this.m_initialValue_;
        }
        return this.m_data_[(this.m_index_[i >> 5] << 2) + (i & 31)];
    }

    public final int getLeadValue(char c) {
        return this.m_data_[getLeadOffset(c)];
    }

    public final int getBMPValue(char c) {
        return this.m_data_[getBMPOffset(c)];
    }

    public final int getSurrogateValue(char c, char c2) {
        if (!UTF16.isLeadSurrogate(c) || !UTF16.isTrailSurrogate(c2)) {
            throw new IllegalArgumentException("Argument characters do not form a supplementary character");
        }
        int surrogateOffset = getSurrogateOffset(c, c2);
        if (surrogateOffset > 0) {
            return this.m_data_[surrogateOffset];
        }
        return this.m_initialValue_;
    }

    public final int getTrailValue(int i, char c) {
        if (this.m_dataManipulate_ != null) {
            int foldingOffset = this.m_dataManipulate_.getFoldingOffset(i);
            if (foldingOffset > 0) {
                return this.m_data_[getRawOffset(foldingOffset, (char) (c & 1023))];
            }
            return this.m_initialValue_;
        }
        throw new NullPointerException("The field DataManipulate in this Trie is null");
    }

    public final int getLatin1LinearValue(char c) {
        return this.m_data_[c + ' '];
    }

    @Override // ohos.global.icu.impl.Trie
    public boolean equals(Object obj) {
        if (super.equals(obj) && (obj instanceof IntTrie)) {
            IntTrie intTrie = (IntTrie) obj;
            if (this.m_initialValue_ == intTrie.m_initialValue_ && Arrays.equals(this.m_data_, intTrie.m_data_)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.Trie
    public final void unserialize(ByteBuffer byteBuffer) {
        super.unserialize(byteBuffer);
        this.m_data_ = ICUBinary.getInts(byteBuffer, this.m_dataLength_, 0);
        this.m_initialValue_ = this.m_data_[0];
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

    IntTrie(char[] cArr, int[] iArr, int i, int i2, Trie.DataManipulate dataManipulate) {
        super(cArr, i2, dataManipulate);
        this.m_data_ = iArr;
        this.m_dataLength_ = this.m_data_.length;
        this.m_initialValue_ = i;
    }
}
