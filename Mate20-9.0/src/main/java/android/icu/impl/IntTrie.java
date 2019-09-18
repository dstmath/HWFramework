package android.icu.impl;

import android.icu.impl.Trie;
import android.icu.text.UTF16;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class IntTrie extends Trie {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int[] m_data_;
    private int m_initialValue_;

    public IntTrie(ByteBuffer bytes, Trie.DataManipulate dataManipulate) throws IOException {
        super(bytes, dataManipulate);
        if (!isIntTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a int trie.");
        }
    }

    public IntTrie(int initialValue, int leadUnitValue, Trie.DataManipulate dataManipulate) {
        super(new char[2080], 512, dataManipulate);
        int dataLength = 256;
        dataLength = leadUnitValue != initialValue ? 256 + 32 : dataLength;
        this.m_data_ = new int[dataLength];
        this.m_dataLength_ = dataLength;
        this.m_initialValue_ = initialValue;
        for (int i = 0; i < 256; i++) {
            this.m_data_[i] = initialValue;
        }
        if (leadUnitValue != initialValue) {
            char block = (char) (256 >> 2);
            for (int i2 = 1728; i2 < 1760; i2++) {
                this.m_index_[i2] = block;
            }
            int limit = 256 + 32;
            for (int i3 = 256; i3 < limit; i3++) {
                this.m_data_[i3] = leadUnitValue;
            }
        }
    }

    public final int getCodePointValue(int ch) {
        if (ch < 0 || ch >= 55296) {
            int offset = getCodePointOffset(ch);
            return offset >= 0 ? this.m_data_[offset] : this.m_initialValue_;
        }
        return this.m_data_[(this.m_index_[ch >> 5] << 2) + (ch & 31)];
    }

    public final int getLeadValue(char ch) {
        return this.m_data_[getLeadOffset(ch)];
    }

    public final int getBMPValue(char ch) {
        return this.m_data_[getBMPOffset(ch)];
    }

    public final int getSurrogateValue(char lead, char trail) {
        if (!UTF16.isLeadSurrogate(lead) || !UTF16.isTrailSurrogate(trail)) {
            throw new IllegalArgumentException("Argument characters do not form a supplementary character");
        }
        int offset = getSurrogateOffset(lead, trail);
        if (offset > 0) {
            return this.m_data_[offset];
        }
        return this.m_initialValue_;
    }

    public final int getTrailValue(int leadvalue, char trail) {
        if (this.m_dataManipulate_ != null) {
            int offset = this.m_dataManipulate_.getFoldingOffset(leadvalue);
            if (offset > 0) {
                return this.m_data_[getRawOffset(offset, (char) (trail & 1023))];
            }
            return this.m_initialValue_;
        }
        throw new NullPointerException("The field DataManipulate in this Trie is null");
    }

    public final int getLatin1LinearValue(char ch) {
        return this.m_data_[' ' + ch];
    }

    public boolean equals(Object other) {
        if (!super.equals(other) || !(other instanceof IntTrie)) {
            return false;
        }
        IntTrie othertrie = (IntTrie) other;
        if (this.m_initialValue_ != othertrie.m_initialValue_ || !Arrays.equals(this.m_data_, othertrie.m_data_)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return 42;
    }

    /* access modifiers changed from: protected */
    public final void unserialize(ByteBuffer bytes) {
        super.unserialize(bytes);
        this.m_data_ = ICUBinary.getInts(bytes, this.m_dataLength_, 0);
        this.m_initialValue_ = this.m_data_[0];
    }

    /* access modifiers changed from: protected */
    public final int getSurrogateOffset(char lead, char trail) {
        if (this.m_dataManipulate_ != null) {
            int offset = this.m_dataManipulate_.getFoldingOffset(getLeadValue(lead));
            if (offset > 0) {
                return getRawOffset(offset, (char) (trail & 1023));
            }
            return -1;
        }
        throw new NullPointerException("The field DataManipulate in this Trie is null");
    }

    /* access modifiers changed from: protected */
    public final int getValue(int index) {
        return this.m_data_[index];
    }

    /* access modifiers changed from: protected */
    public final int getInitialValue() {
        return this.m_initialValue_;
    }

    IntTrie(char[] index, int[] data, int initialvalue, int options, Trie.DataManipulate datamanipulate) {
        super(index, options, datamanipulate);
        this.m_data_ = data;
        this.m_dataLength_ = this.m_data_.length;
        this.m_initialValue_ = initialvalue;
    }
}
