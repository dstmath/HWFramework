package android.icu.impl;

import android.icu.impl.TrieBuilder.DataManipulate;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class IntTrieBuilder extends TrieBuilder {
    protected int[] m_data_;
    protected int m_initialValue_;
    private int m_leadUnitValue_;

    public IntTrieBuilder(IntTrieBuilder table) {
        super(table);
        this.m_data_ = new int[this.m_dataCapacity_];
        System.arraycopy(table.m_data_, 0, this.m_data_, 0, this.m_dataLength_);
        this.m_initialValue_ = table.m_initialValue_;
        this.m_leadUnitValue_ = table.m_leadUnitValue_;
    }

    public IntTrieBuilder(int[] aliasdata, int maxdatalength, int initialvalue, int leadunitvalue, boolean latin1linear) {
        if (maxdatalength < 32 || (latin1linear && maxdatalength < 1024)) {
            throw new IllegalArgumentException("Argument maxdatalength is too small");
        }
        if (aliasdata != null) {
            this.m_data_ = aliasdata;
        } else {
            this.m_data_ = new int[maxdatalength];
        }
        int j = 32;
        if (latin1linear) {
            int i = 0;
            while (true) {
                int i2 = i + 1;
                this.m_index_[i] = j;
                j += 32;
                if (i2 >= 8) {
                    break;
                }
                i = i2;
            }
        }
        this.m_dataLength_ = j;
        Arrays.fill(this.m_data_, 0, this.m_dataLength_, initialvalue);
        this.m_initialValue_ = initialvalue;
        this.m_leadUnitValue_ = leadunitvalue;
        this.m_dataCapacity_ = maxdatalength;
        this.m_isLatin1Linear_ = latin1linear;
        this.m_isCompacted_ = false;
    }

    public int getValue(int ch) {
        if (this.m_isCompacted_ || ch > 1114111 || ch < 0) {
            return 0;
        }
        return this.m_data_[Math.abs(this.m_index_[ch >> 5]) + (ch & 31)];
    }

    public int getValue(int ch, boolean[] inBlockZero) {
        boolean z = true;
        if (this.m_isCompacted_ || ch > 1114111 || ch < 0) {
            if (inBlockZero != null) {
                inBlockZero[0] = true;
            }
            return 0;
        }
        int block = this.m_index_[ch >> 5];
        if (inBlockZero != null) {
            if (block != 0) {
                z = false;
            }
            inBlockZero[0] = z;
        }
        return this.m_data_[Math.abs(block) + (ch & 31)];
    }

    public boolean setValue(int ch, int value) {
        if (this.m_isCompacted_ || ch > 1114111 || ch < 0) {
            return false;
        }
        int block = getDataBlock(ch);
        if (block < 0) {
            return false;
        }
        this.m_data_[(ch & 31) + block] = value;
        return true;
    }

    public IntTrie serialize(DataManipulate datamanipulate, Trie.DataManipulate triedatamanipulate) {
        if (datamanipulate == null) {
            throw new IllegalArgumentException("Parameters can not be null");
        }
        if (!this.m_isCompacted_) {
            compact(false);
            fold(datamanipulate);
            compact(true);
            this.m_isCompacted_ = true;
        }
        if (this.m_dataLength_ >= 262144) {
            throw new ArrayIndexOutOfBoundsException("Data length too small");
        }
        char[] index = new char[this.m_indexLength_];
        int[] data = new int[this.m_dataLength_];
        for (int i = 0; i < this.m_indexLength_; i++) {
            index[i] = (char) (this.m_index_[i] >>> 2);
        }
        System.arraycopy(this.m_data_, 0, data, 0, this.m_dataLength_);
        int options = 293;
        if (this.m_isLatin1Linear_) {
            options = 293 | 512;
        }
        return new IntTrie(index, data, this.m_initialValue_, options, triedatamanipulate);
    }

    public int serialize(OutputStream os, boolean reduceTo16Bits, DataManipulate datamanipulate) throws IOException {
        if (datamanipulate == null) {
            throw new IllegalArgumentException("Parameters can not be null");
        }
        int length;
        if (!this.m_isCompacted_) {
            compact(false);
            fold(datamanipulate);
            compact(true);
            this.m_isCompacted_ = true;
        }
        if (reduceTo16Bits) {
            length = this.m_dataLength_ + this.m_indexLength_;
        } else {
            length = this.m_dataLength_;
        }
        if (length >= 262144) {
            throw new ArrayIndexOutOfBoundsException("Data length too small");
        }
        length = (this.m_indexLength_ * 2) + 16;
        if (reduceTo16Bits) {
            length += this.m_dataLength_ * 2;
        } else {
            length += this.m_dataLength_ * 4;
        }
        if (os == null) {
            return length;
        }
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(1416784229);
        int options = 37;
        if (!reduceTo16Bits) {
            options = 293;
        }
        if (this.m_isLatin1Linear_) {
            options |= 512;
        }
        dos.writeInt(options);
        dos.writeInt(this.m_indexLength_);
        dos.writeInt(this.m_dataLength_);
        int i;
        if (reduceTo16Bits) {
            for (i = 0; i < this.m_indexLength_; i++) {
                dos.writeChar((this.m_index_[i] + this.m_indexLength_) >>> 2);
            }
            for (i = 0; i < this.m_dataLength_; i++) {
                dos.writeChar(this.m_data_[i] & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH);
            }
        } else {
            for (i = 0; i < this.m_indexLength_; i++) {
                dos.writeChar(this.m_index_[i] >>> 2);
            }
            for (i = 0; i < this.m_dataLength_; i++) {
                dos.writeInt(this.m_data_[i]);
            }
        }
        return length;
    }

    public boolean setRange(int start, int limit, int value, boolean overwrite) {
        if (this.m_isCompacted_ || start < 0 || start > 1114111 || limit < 0 || limit > 1114112 || start > limit) {
            return false;
        }
        if (start == limit) {
            return true;
        }
        int block;
        if ((start & 31) != 0) {
            block = getDataBlock(start);
            if (block < 0) {
                return false;
            }
            int nextStart = (start + 32) & -32;
            if (nextStart <= limit) {
                fillBlock(block, start & 31, 32, value, overwrite);
                start = nextStart;
            } else {
                fillBlock(block, start & 31, limit & 31, value, overwrite);
                return true;
            }
        }
        int rest = limit & 31;
        limit &= -32;
        int repeatBlock = 0;
        if (value != this.m_initialValue_) {
            repeatBlock = -1;
        }
        while (true) {
            int repeatBlock2 = repeatBlock;
            if (start < limit) {
                block = this.m_index_[start >> 5];
                if (block > 0) {
                    fillBlock(block, 0, 32, value, overwrite);
                    repeatBlock = repeatBlock2;
                } else if (this.m_data_[-block] == value) {
                    repeatBlock = repeatBlock2;
                } else if (block != 0 && !overwrite) {
                    repeatBlock = repeatBlock2;
                } else if (repeatBlock2 >= 0) {
                    this.m_index_[start >> 5] = -repeatBlock2;
                    repeatBlock = repeatBlock2;
                } else {
                    repeatBlock = getDataBlock(start);
                    if (repeatBlock < 0) {
                        return false;
                    }
                    this.m_index_[start >> 5] = -repeatBlock;
                    fillBlock(repeatBlock, 0, 32, value, true);
                }
                start += 32;
            } else {
                if (rest > 0) {
                    block = getDataBlock(start);
                    if (block < 0) {
                        return false;
                    }
                    fillBlock(block, 0, rest, value, overwrite);
                }
                return true;
            }
        }
    }

    private int allocDataBlock() {
        int newBlock = this.m_dataLength_;
        int newTop = newBlock + 32;
        if (newTop > this.m_dataCapacity_) {
            return -1;
        }
        this.m_dataLength_ = newTop;
        return newBlock;
    }

    private int getDataBlock(int ch) {
        ch >>= 5;
        int indexValue = this.m_index_[ch];
        if (indexValue > 0) {
            return indexValue;
        }
        int newBlock = allocDataBlock();
        if (newBlock < 0) {
            return -1;
        }
        this.m_index_[ch] = newBlock;
        System.arraycopy(this.m_data_, Math.abs(indexValue), this.m_data_, newBlock, 128);
        return newBlock;
    }

    private void compact(boolean overlap) {
        if (!this.m_isCompacted_) {
            int i;
            findUnusedBlocks();
            int overlapStart = 32;
            if (this.m_isLatin1Linear_) {
                overlapStart = 288;
            }
            int newStart = 32;
            int start = 32;
            while (start < this.m_dataLength_) {
                if (this.m_map_[start >>> 5] < 0) {
                    start += 32;
                } else {
                    int start2;
                    int newStart2;
                    if (start >= overlapStart) {
                        i = findSameDataBlock(this.m_data_, newStart, start, overlap ? 4 : 32);
                        if (i >= 0) {
                            this.m_map_[start >>> 5] = i;
                            start += 32;
                        }
                    }
                    if (!overlap || start < overlapStart) {
                        i = 0;
                    } else {
                        i = 28;
                        while (i > 0 && (TrieBuilder.equal_int(this.m_data_, newStart - i, start, i) ^ 1) != 0) {
                            i -= 4;
                        }
                    }
                    if (i > 0) {
                        this.m_map_[start >>> 5] = newStart - i;
                        start += i;
                        i = 32 - i;
                        start2 = start;
                        newStart2 = newStart;
                        while (i > 0) {
                            newStart = newStart2 + 1;
                            start = start2 + 1;
                            this.m_data_[newStart2] = this.m_data_[start2];
                            i--;
                            start2 = start;
                            newStart2 = newStart;
                        }
                    } else if (newStart < start) {
                        this.m_map_[start >>> 5] = newStart;
                        i = 32;
                        start2 = start;
                        newStart2 = newStart;
                        while (i > 0) {
                            newStart = newStart2 + 1;
                            start = start2 + 1;
                            this.m_data_[newStart2] = this.m_data_[start2];
                            i--;
                            start2 = start;
                            newStart2 = newStart;
                        }
                    } else {
                        this.m_map_[start >>> 5] = start;
                        newStart += 32;
                        start = newStart;
                    }
                    start = start2;
                    newStart = newStart2;
                }
            }
            for (i = 0; i < this.m_indexLength_; i++) {
                this.m_index_[i] = this.m_map_[Math.abs(this.m_index_[i]) >>> 5];
            }
            this.m_dataLength_ = newStart;
        }
    }

    private static final int findSameDataBlock(int[] data, int dataLength, int otherBlock, int step) {
        dataLength -= 32;
        int block = 0;
        while (block <= dataLength) {
            if (TrieBuilder.equal_int(data, block, otherBlock, 32)) {
                return block;
            }
            block += step;
        }
        return -1;
    }

    private final void fold(DataManipulate manipulate) {
        int c;
        int[] leadIndexes = new int[32];
        int[] index = this.m_index_;
        System.arraycopy(index, 1728, leadIndexes, 0, 32);
        int block = 0;
        if (this.m_leadUnitValue_ != this.m_initialValue_) {
            block = allocDataBlock();
            if (block < 0) {
                throw new IllegalStateException("Internal error: Out of memory space");
            }
            fillBlock(block, 0, 32, this.m_leadUnitValue_, true);
            block = -block;
        }
        for (c = 1728; c < 1760; c++) {
            this.m_index_[c] = block;
        }
        int indexLength = 2048;
        c = 65536;
        while (c < 1114112) {
            if (index[c >> 5] != 0) {
                c &= -1024;
                block = TrieBuilder.findSameIndexBlock(index, indexLength, c >> 5);
                int value = manipulate.getFoldedValue(c, block + 32);
                if (value != getValue(UTF16.getLeadSurrogate(c))) {
                    if (!setValue(UTF16.getLeadSurrogate(c), value)) {
                        throw new ArrayIndexOutOfBoundsException("Data table overflow");
                    } else if (block == indexLength) {
                        System.arraycopy(index, c >> 5, index, indexLength, 32);
                        indexLength += 32;
                    }
                }
                c += 1024;
            } else {
                c += 32;
            }
        }
        if (indexLength >= 34816) {
            throw new ArrayIndexOutOfBoundsException("Index table overflow");
        }
        System.arraycopy(index, 2048, index, 2080, indexLength - 2048);
        System.arraycopy(leadIndexes, 0, index, 2048, 32);
        this.m_indexLength_ = indexLength + 32;
    }

    private void fillBlock(int block, int start, int limit, int value, boolean overwrite) {
        limit += block;
        block += start;
        if (overwrite) {
            while (true) {
                int block2 = block;
                if (block2 < limit) {
                    block = block2 + 1;
                    this.m_data_[block2] = value;
                } else {
                    return;
                }
            }
        }
        while (block < limit) {
            if (this.m_data_[block] == this.m_initialValue_) {
                this.m_data_[block] = value;
            }
            block++;
        }
    }
}
