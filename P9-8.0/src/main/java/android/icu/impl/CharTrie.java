package android.icu.impl;

import android.icu.impl.Trie.DataManipulate;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;

public class CharTrie extends Trie {
    static final /* synthetic */ boolean -assertionsDisabled = (CharTrie.class.desiredAssertionStatus() ^ 1);
    private char[] m_data_;
    private char m_initialValue_;

    public CharTrie(ByteBuffer bytes, DataManipulate dataManipulate) {
        super(bytes, dataManipulate);
        if (!isCharTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a char trie.");
        }
    }

    public CharTrie(int initialValue, int leadUnitValue, DataManipulate dataManipulate) {
        int i;
        super(new char[2080], 512, dataManipulate);
        int dataLength = 256;
        if (leadUnitValue != initialValue) {
            dataLength = 288;
        }
        this.m_data_ = new char[dataLength];
        this.m_dataLength_ = dataLength;
        this.m_initialValue_ = (char) initialValue;
        for (i = 0; i < 256; i++) {
            this.m_data_[i] = (char) initialValue;
        }
        if (leadUnitValue != initialValue) {
            char block = (char) 64;
            for (i = 1728; i < 1760; i++) {
                this.m_index_[i] = block;
            }
            for (i = 256; i < 288; i++) {
                this.m_data_[i] = (char) leadUnitValue;
            }
        }
    }

    public final char getCodePointValue(int ch) {
        if (ch < 0 || ch >= 55296) {
            int offset = getCodePointOffset(ch);
            return offset >= 0 ? this.m_data_[offset] : this.m_initialValue_;
        }
        return this.m_data_[(this.m_index_[ch >> 5] << 2) + (ch & 31)];
    }

    public final char getLeadValue(char ch) {
        return this.m_data_[getLeadOffset(ch)];
    }

    public final char getBMPValue(char ch) {
        return this.m_data_[getBMPOffset(ch)];
    }

    public final char getSurrogateValue(char lead, char trail) {
        int offset = getSurrogateOffset(lead, trail);
        if (offset > 0) {
            return this.m_data_[offset];
        }
        return this.m_initialValue_;
    }

    public final char getTrailValue(int leadvalue, char trail) {
        if (this.m_dataManipulate_ == null) {
            throw new NullPointerException("The field DataManipulate in this Trie is null");
        }
        int offset = this.m_dataManipulate_.getFoldingOffset(leadvalue);
        if (offset > 0) {
            return this.m_data_[getRawOffset(offset, (char) (trail & Opcodes.OP_NEW_INSTANCE_JUMBO))];
        }
        return this.m_initialValue_;
    }

    public final char getLatin1LinearValue(char ch) {
        return this.m_data_[(this.m_dataOffset_ + 32) + ch];
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!super.equals(other) || !(other instanceof CharTrie)) {
            return false;
        }
        if (this.m_initialValue_ == ((CharTrie) other).m_initialValue_) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    protected final void unserialize(ByteBuffer bytes) {
        this.m_index_ = ICUBinary.getChars(bytes, this.m_dataOffset_ + this.m_dataLength_, 0);
        this.m_data_ = this.m_index_;
        this.m_initialValue_ = this.m_data_[this.m_dataOffset_];
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
}
