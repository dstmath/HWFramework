package android.icu.impl;

import android.icu.impl.Trie.DataManipulate;
import android.icu.text.UTF16;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class IntTrie extends Trie {
    static final /* synthetic */ boolean -assertionsDisabled = (IntTrie.class.desiredAssertionStatus() ^ 1);
    private int[] m_data_;
    private int m_initialValue_;

    public IntTrie(ByteBuffer bytes, DataManipulate dataManipulate) throws IOException {
        super(bytes, dataManipulate);
        if (!isIntTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a int trie.");
        }
    }

    public IntTrie(int initialValue, int leadUnitValue, DataManipulate dataManipulate) {
        int i;
        super(new char[2080], 512, dataManipulate);
        int dataLength = 256;
        if (leadUnitValue != initialValue) {
            dataLength = 288;
        }
        this.m_data_ = new int[dataLength];
        this.m_dataLength_ = dataLength;
        this.m_initialValue_ = initialValue;
        for (i = 0; i < 256; i++) {
            this.m_data_[i] = initialValue;
        }
        if (leadUnitValue != initialValue) {
            char block = (char) 64;
            for (i = 1728; i < 1760; i++) {
                this.m_index_[i] = block;
            }
            for (i = 256; i < 288; i++) {
                this.m_data_[i] = leadUnitValue;
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
        if (UTF16.isLeadSurrogate(lead) && (UTF16.isTrailSurrogate(trail) ^ 1) == 0) {
            int offset = getSurrogateOffset(lead, trail);
            if (offset > 0) {
                return this.m_data_[offset];
            }
            return this.m_initialValue_;
        }
        throw new IllegalArgumentException("Argument characters do not form a supplementary character");
    }

    public final int getTrailValue(int leadvalue, char trail) {
        if (this.m_dataManipulate_ == null) {
            throw new NullPointerException("The field DataManipulate in this Trie is null");
        }
        int offset = this.m_dataManipulate_.getFoldingOffset(leadvalue);
        if (offset > 0) {
            return this.m_data_[getRawOffset(offset, (char) (trail & Opcodes.OP_NEW_INSTANCE_JUMBO))];
        }
        return this.m_initialValue_;
    }

    public final int getLatin1LinearValue(char ch) {
        return this.m_data_[ch + 32];
    }

    public boolean equals(Object other) {
        if (!super.equals(other) || !(other instanceof IntTrie)) {
            return false;
        }
        IntTrie othertrie = (IntTrie) other;
        if (this.m_initialValue_ == othertrie.m_initialValue_ && (Arrays.equals(this.m_data_, othertrie.m_data_) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    protected final void unserialize(ByteBuffer bytes) {
        super.unserialize(bytes);
        this.m_data_ = ICUBinary.getInts(bytes, this.m_dataLength_, 0);
        this.m_initialValue_ = this.m_data_[0];
    }

    protected final int getSurrogateOffset(char lead, char trail) {
        if (this.m_dataManipulate_ == null) {
            throw new NullPointerException("The field DataManipulate in this Trie is null");
        }
        int offset = this.m_dataManipulate_.getFoldingOffset(getLeadValue(lead));
        if (offset > 0) {
            return getRawOffset(offset, (char) (trail & Opcodes.OP_NEW_INSTANCE_JUMBO));
        }
        return -1;
    }

    protected final int getValue(int index) {
        return this.m_data_[index];
    }

    protected final int getInitialValue() {
        return this.m_initialValue_;
    }

    IntTrie(char[] index, int[] data, int initialvalue, int options, DataManipulate datamanipulate) {
        super(index, options, datamanipulate);
        this.m_data_ = data;
        this.m_dataLength_ = this.m_data_.length;
        this.m_initialValue_ = initialvalue;
    }
}
